package com.playup.android.fragment;

import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Message;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.RegionsAdapter;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

public class UpdateRegionFragment extends MainFragment{

	private RelativeLayout content_layout;

	private ListView regionsList;
	private RegionsAdapter regionAdapter;

	private String vMainColor = null;

	private String vMainTitleColor = null;

	private Timer regionsTimer;

	private TimerTask regionsTask;

	private LinearLayout progressLinear;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		

		content_layout = (RelativeLayout)inflater.inflate(R.layout.update_region, null);


		return content_layout;
	}
	@Override
	public void onAgainActivated(Bundle args) {

		super.onAgainActivated(args);



	}

	@Override
	public void onResume() {	//verified

		super.onResume();
		
		
		
		regionAdapter = null;


		getRegionsForCacheTime();
		initializeViews();
		regionsList.setVisibility(View.GONE);
		progressLinear.setVisibility(View.VISIBLE);
		
		setTopBar();
		setValues();
		
		
	}

	
	/**
	 * refresh for the regions
	 * 
	 */
	private void refreshRegions() {	//verified
		
		
		try{
		
		if(regionsTimer == null)
			regionsTimer = new Timer();
		
		
		if(regionsTask == null){
			regionsTask = new TimerTask() {
				
				@Override
				public void run() {
					getRegions();
					
				}
			};
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					try {
						
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						Hashtable<String, Object> result = dbUtil.getRegionUrlFromRoot();
						String vRegionUrl = (String) result.get("url");
						
						
						int cacheTime = Integer.parseInt(dbUtil.getCacheTime(vRegionUrl));
						
						
						if(cacheTime > 0){
							regionsTimer.schedule(regionsTask,cacheTime * 1000, cacheTime * 1000);
						}else{
							regionsTask = null;
							regionsTimer = null;
						}
					} catch (Exception e) {
						Logs.show(e);
					}
					
					
				}
			}).start();
			
			
		}
		
		} catch (Exception e) {
			Logs.show(e);
		}

		
		
	}
	
	/**
	 * calling various functions to display data in the various parts of the screen
	 * 
	 */
	private void setValues() {	//verified
		try{
			if( !Util.isInternetAvailable() && isVisible()) {


				PlayupLiveApplication.showToast( R.string.no_network );


			}

		} catch (Exception e) {
			Logs.show(e);
		}
		
		setRegions();



	}
	/**
	 * making api call to refresh the list of regions
	 */
	private void getRegions() {	//verfieid
		try {
			// Praveen : Chnaged
			if(runnableList != null && !runnableList.containsKey(Constants.GET_REGIONS)  && Util.isInternetAvailable() )
				runnableList.put(Constants.GET_REGIONS,new Util().getRegionData(runnableList));
		} catch (Exception e) {
			Logs.show ( e );
		}

	}
	/**
	 * TO SOLVE THE ANR
	 * making api call just to get the cachetime of regionsUrl for refresh
	 * 
	 */
	
	private void getRegionsForCacheTime() {	//verified
		new Util().getRegionsForCacheTime();

	}
	
	
	/**
	 * 
	 * fetching and displaying the list of regions
	 * 
	 */
	
	
	private void setRegions() {	

		try {
			new Thread ( new Runnable () {

				@Override
				public void run() {
					
					

					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					final Hashtable<String, List<String>> regions = dbUtil.getRegions();
//					if(regions.get("vRegionLogo")!= null && regions.get ( "vRegionLogo" ).size ( ) > 0){
//					Log.e("123", "regions data---logo---->>>>>>>>>>"+regions.get("vRegionLogo"));
//					}else{
//						Log.e("123", "regions data---logo---->>>>>>>>>>null");
//					}
					if ( regions != null && regions.get ( "vRegionId" ) != null && regions.get ( "vRegionId" ).size ( ) > 0 ) {

						if ( PlayUpActivity.handler  != null ) {
							PlayUpActivity.handler.post( new  Runnable() {

								@Override
								public void run() {
									try {

										
										
										regionsList.setVisibility(View.VISIBLE);
										progressLinear.setVisibility(View.GONE);
										
										if ( regionAdapter != null ) {
											regionAdapter.setData ( regions );
										} else {
											regionAdapter = new RegionsAdapter ( regions );
											regionsList.setAdapter ( regionAdapter );
										}

									} catch ( Exception e ) {
										Logs.show ( e );
									}
								}
							});
						}
					}
				}
			}).start();


		} catch (Exception e) {
			Logs.show(e);
		}

	}
	
	
	/**
	 * 
	 * initializing the various views
	 * 
	 */

	private void initializeViews() {	//verified

		regionsList = (ListView)content_layout.findViewById(R.id.regions);
		
		progressLinear = (LinearLayout) content_layout.findViewById(R.id.progressLeagues);

	}
	@Override
	public void onUpdate(final Message msg) {

		super.onUpdate(msg);


		if(PlayUpActivity.handler != null){

			PlayUpActivity.handler.post(new Runnable() {

				@Override
				public void run() {

					try {
						if(!isVisible()){
							return;
						}

						if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("Regions")){
							//refreshing the regions
							if(msg.arg1 == 1)
								setRegions();
							
							
							refreshRegions();
							
						}
						

						if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
							
							getRegionsForCacheTime();
							
						}
							
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show(e);
					}

				}
			});

		}


	}
	/**
	 * setting the color and title of the topbar
	 */
	
	private void setTopBar() {	//verified
		new Thread(new Runnable() {
	
		@Override
		public void run() {
			try {
				DatabaseUtil dbUtil = DatabaseUtil.getInstance();
				//String regionUrl = dbUtil.getRegionUrlFromRoot();.
				Hashtable<String, Object> result = dbUtil.getRegionUrlFromRoot();
				String vRegionUrl = (String) result.get("url");
				
				String vChildColor = dbUtil.getSectionMainColor("", vRegionUrl);
				String vChildTitleColor = dbUtil.getSectionMainTitleColor("", vRegionUrl);
				
				if(vChildColor != null && vChildColor.trim().length() > 0 )
					vMainColor = vChildColor;
				
				if(vChildTitleColor != null && vChildTitleColor.trim().length() > 0 )
					vMainTitleColor = vChildTitleColor;
					
	
				
				Bundle b = new Bundle();
				b.putString("vMainColor",vMainColor );
				b.putString("vMainTitleColor",vMainTitleColor );
				Message msg = new Message ();
				msg.setData(b);
				
				
				PlayupLiveApplication.callUpdateTopBarFragments(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Logs.show ( e );
			}
			
		}
		}).start();
		
		
	}

	@Override
	public void onStop() {

		super.onStop();
		regionAdapter = null;
		
		if(regionsTimer != null )
			regionsTimer.cancel();
		
		
		if(regionsTask != null)
			regionsTask.cancel();
		
		regionsTimer = null;
		regionsTask = null;
	}


	@Override
	public void onConnectionChanged(boolean isConnectionActive) {
		super.onConnectionChanged(isConnectionActive);
		//Praveen :" Changed
		new Util().getRecentActivityData();
	}


}