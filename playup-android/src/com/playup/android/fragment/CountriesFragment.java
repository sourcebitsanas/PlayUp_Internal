package com.playup.android.fragment;


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
import com.playup.android.adapters.CountriesAdapter;
import com.playup.android.application.PlayupLiveApplication;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;

import com.playup.android.util.Util;

/**
 * Show the countries list <br>
 * Will be displayed when selecting region from RegionFragment
 */
public class CountriesFragment extends MainFragment{

	private RelativeLayout content_layout;

	private ListView countriesList;
	private CountriesAdapter countriesAdapter;

	private String vRegionId = null ;

	private boolean isAgain = false;

	private Timer countriesTimer;

	private TimerTask countriesTask;
	
	private LinearLayout progressLinear;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		content_layout = (RelativeLayout)inflater.inflate(R.layout.update_region, null);
		if(!isAgain)
		setRegionId(getArguments());
		return content_layout;
	}
	@Override
	public void onAgainActivated(Bundle args) {

		super.onAgainActivated(args);
		isAgain = true;
		setRegionId(args);
	}

	/**
	 * Fetching values passed from previous fragment
	 * @param args (vRegionId) used to identify which region been selected
	 */
	private void setRegionId(Bundle args) {
		vRegionId = null;
		if(args != null && args.containsKey("vRegionId"))
			vRegionId = args.getString("vRegionId");
		
	}
	@Override
	public void onResume() {

		super.onResume();
		countriesAdapter = null;
		initializeViews();		
		countriesList.setVisibility(View.GONE);
		progressLinear.setVisibility(View.VISIBLE);		
		setValues();	
	}

	/**
	 * Fetching country details and showing them as a list
	 */
	private void setValues() {	//verfied
		
		// if Internet is not available show Toast
		try {
			if( !Util.isInternetAvailable() && isVisible()) {
				PlayupLiveApplication.showToast( R.string.no_network );
			}

		} catch (Exception e) {
//			Logs.show(e);
		}
		
		try{
			if(vRegionId != null && vRegionId.trim().length() > 0){
				getCountriesForCacheTime();	
				setTopBar();
				setCountries();
			
			}
		}catch(Exception e){
		}

	}
	
	
	/**
	 * setting topbar data
	 */
	private void setTopBar() { //verified
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					
					String regionName = DatabaseUtil.getInstance().getRegionName(vRegionId);
					Bundle b = new Bundle();
					b.putString("vMainColor",null );
					b.putString("vMainTitleColor",null );
				
					Message m = new Message ();
					
					if(regionName != null ){				
						b.putString("vTitle", regionName);
					}else{
						b.putString("vTitle", "");
					}	
					m.setData(b);
					PlayupLiveApplication.callUpdateTopBarFragments(m);
				} catch (Exception e) {
//					Logs.show(e);
				}
				
			}
		}).start();
		
	}
	
	/**
	 * hitting countries URL for getting cachetime ( Used for polling )
	 */
	private void getCountriesForCacheTime() {	//verfiied
		new Util().getCountriesDataForCacheTime(vRegionId);	
	}
	
	
	/**
	 * API call for fetching countries data
	 */
	private void getCountries() {	//verfied
		try{
		if(runnableList != null && !runnableList.containsKey(Constants.GET_COUNTRIES_DATA) &&  Util.isInternetAvailable() )
			runnableList.put(Constants.GET_COUNTRIES_DATA,new Util().getCountriesData(vRegionId,runnableList));
		}catch(Exception e){
//			Logs.show(e);
		}
	
	}
	
	/**
	 * Setting countries data to the Listview
	 */
	public void setCountries() {	//verfied
		try{
			if(countriesAdapter != null){
				countriesAdapter.setData(vRegionId);

			}else{	
				progressLinear.setVisibility(View.GONE);
				countriesList.setVisibility(View.VISIBLE);
				countriesAdapter = new CountriesAdapter(vRegionId);
				countriesList.setAdapter(countriesAdapter);
			}
		}catch(Exception e){
//			Logs.show(e);
		}
	}

	
	/**
	 * Refreshing ( Polling) countries data
	 */
	private void refreshCountries() {	//verfied
		try{
		if(countriesTimer == null)
			countriesTimer = new Timer();
	
		if(countriesTask == null){
			countriesTask = new TimerTask() {
				
				@Override
				public void run() {
					getCountries();
					
				}
			};
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						String vRegionUrl = dbUtil.getRegionUrlForRefresh(vRegionId);
						int cacheTime=0;
						if(vRegionUrl!=null){
							cacheTime = Integer.parseInt(dbUtil.getCacheTime(vRegionUrl));
						}
						
						if(cacheTime > 0){
							countriesTimer.schedule(countriesTask,cacheTime * 1000, cacheTime * 1000);
						}else{
							countriesTask = null;
							countriesTimer = null;
						}
					} catch (Exception e) {
//						Logs.show(e);
					}
					
				}
			}).start();
				
			
		}	
		}catch(Exception e){
//			Logs.show(e);
		}
		
	}
	


	/**
	 * initializing the views
	 */
	private void initializeViews() {	//verfied

		countriesList = (ListView)content_layout.findViewById(R.id.regions);
		
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
						
						// Will be called After Fetching and Storing of Countries data
						if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("Countries")) {
							
							// if data is updated, set the countries again
							if(msg.arg1 == 1) {		
								setCountries();
							}
						
							refreshCountries();
						}else if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
							
							
							
							getCountriesForCacheTime();
							
						}
							
					} catch (Exception e) {
						// TODO Auto-generated catch block
//						Logs.show(e);
					}

					
				}
			});

		}


	}


	/**
	 * When Fragment loss its visibility <br>
	 * All the timers, timertasks should be cancelled
	 */
	@Override
	public void onStop() {

		super.onStop();
		countriesAdapter = null;
		
		if(countriesTimer != null )
			countriesTimer.cancel();

		if(countriesTask != null)
			countriesTask.cancel();
		
		countriesTimer = null;
		countriesTask = null;
	}


	@Override
	public void onConnectionChanged(boolean isConnectionActive) {	//verfied
		super.onConnectionChanged(isConnectionActive);
		
		new Util().getRecentActivityData();
	}


}