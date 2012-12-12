package com.playup.android.fragment;



import java.util.Hashtable;




import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Message;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;


import com.playup.android.adapters.TeamScheduleAdapter;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

public class TeamScheduleFragment extends MainFragment {

	private ListView teamScheduleList;
	private TeamScheduleAdapter teamScheduleAdapter;
	private LinearLayout progressLinear;
	
	
	
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;
	
	boolean isAgain = false;


	private RelativeLayout content_layout;
	private Hashtable< String , List < String > > teamScheduleData  = null;
	private String vTeamScheduleUrl = null;
	private String vTeamScheduleId = null;
	
	private Timer refreshTeamScheduleTimer = null;
	private TimerTask refreshTeamScheduleTimerTask = null;
	
	private boolean isHrefURL	= false;
	
	int scrollIndex = -1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
	
		
		content_layout = (RelativeLayout) inflater.inflate( R.layout.team_schedule, null);

		if ( !isAgain ) {
			setTeamScheduleValues ( getArguments() ); 
		}

		return content_layout;
	}







	@Override
	public void onResume () {
		try {
			super.onResume();
			fetchTeamScheduleData();
			initialize( content_layout );
			teamScheduleAdapter = null;		
			setValues();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
		
	}
	
	
	/**
	 * making apiCall to fetch the team schedule  data
	 */
	
	private void fetchTeamScheduleData() {


		try {
			if ( vTeamScheduleUrl!= null && vTeamScheduleUrl.trim().length() > 0 ) {

				if (Util.isInternetAvailable() && runnableList != null && !runnableList.containsKey ( vTeamScheduleUrl )  ) {
//Praveen : changed
					runnableList.put ( vTeamScheduleUrl, new Util ( ).getTeamScheduleData( vTeamScheduleUrl,isHrefURL, runnableList)  );

				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
	}
	
	
	

	/**
	 * setting the sports id  
	 */
	private void setTeamScheduleValues ( Bundle bundle ) {
		
		vMainColor = null;
		vMainTitleColor = null;
		
		vSecColor = null;
		 vSecTitleColor = null;
		 
		
		 
		 
		 vTeamScheduleUrl = null;

		
		
		if ( bundle != null && bundle.containsKey( "vTeamScheduleUrl") ) {
			vTeamScheduleUrl = bundle.getString( "vTeamScheduleUrl" );
		}
		
		if (bundle != null && bundle.containsKey("vMainColor")) {
			vMainColor = bundle.getString("vMainColor");
		}if (bundle != null && bundle.containsKey("vMainTitleColor")) {
			vMainTitleColor = bundle.getString("vMainTitleColor");
		}if (bundle != null && bundle.containsKey("vSecColor")) {
			vSecColor = bundle.getString("vSecColor");
		}if (bundle != null && bundle.containsKey("vSecTitleColor")) {
			vSecTitleColor = bundle.getString("vSecTitleColor");
		}if (bundle != null && bundle.containsKey("isHref")) {
			isHrefURL 	= 	bundle.getBoolean("isHref");
		}
	}

	@Override
	public void onStop () {
		super.onStop();	

		try {

			if( teamScheduleList!= null ) {
				scrollIndex = teamScheduleList.getFirstVisiblePosition();
			} else{
				scrollIndex = -1;
			}
			
			if(refreshTeamScheduleTimer != null){
				refreshTeamScheduleTimer.cancel();
				refreshTeamScheduleTimer = null;
			}
	
			if(refreshTeamScheduleTimerTask != null){
				refreshTeamScheduleTimerTask.cancel();
				refreshTeamScheduleTimerTask = null;
			}
			
			

		} catch ( Exception e ) {
			Logs.show ( e );
		} 
	}
	/**
	 *  setting the color and title in the top bar fragment.
	 **/
	private void setTopBar () {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					
					String vTitle = null;
					
					
					if( vTeamScheduleId != null && vTeamScheduleId.trim().length() > 0) {
						vTitle =  DatabaseUtil.getInstance().getTitleFromTeamSchedule( vTeamScheduleId );
					}
					
					
					
					try {
						Bundle b = new Bundle();
						b.putString("vMainColor",vMainColor );
						b.putString("vMainTitleColor",vMainTitleColor );
						
						Message msg = new Message ();
						HashMap<String, String> data = new HashMap<String, String>();
						data.put("vTitle",vTitle);						
						msg.obj = data;
						
						msg.setData(b);					
						PlayupLiveApplication.callUpdateTopBarFragments(msg);
					} catch (Exception e) {
						Logs.show(e);
					}
					
				
			
					
				} catch (Exception e) {
					Logs.show(e);
				}
				
			}
		}).start();
		
		
	}


	
	@Override 
	public void onUpdate ( final Message msg ) {

		if(PlayUpActivity.handler != null){

			PlayUpActivity.handler.post( new Runnable() {
				
				@Override
				public void run() {
					
					try {
								if( msg!= null && msg.obj!= null ) {
									//firstDisplay/refreshing the teamScheduleData
									if( msg.obj.toString().equalsIgnoreCase("refreshTeamSchedule") ) {
										if( msg.arg1 == 1 ){
											setValues();
										} else {
											if( progressLinear!=null && progressLinear.getVisibility() != View.VISIBLE )
													progressLinear.setVisibility(View.GONE);
										}
									
									
								
									refreshTeamSchedule();
								} else if( msg.obj.toString().equalsIgnoreCase("callChevron")  ) {
									//going to parent
									PlayupLiveApplication.getFragmentManagerUtil().popBackStackImmediate();
									return;
		
									
								}else if( msg.obj.toString().equalsIgnoreCase("credentials stored")  ) {
									//going to parent
									fetchTeamScheduleData();
		
									
								}
							}
						} catch (Exception e) {
						// TODO Auto-generated catch block
					
						Logs.show(e);
					}
					
					
					
				}
			});
		
		}
	}


	@Override
	public void onAgainActivated(Bundle args) {

		isAgain = true;
		setTeamScheduleValues ( args );

	}

	/**
	 * initializing the viewa
	 * @param content_layout
	 */
	private void initialize(RelativeLayout content_layout) {
		
		teamScheduleList = ( ListView ) content_layout.findViewById ( R.id.teamScheduleList );
		progressLinear	=	(LinearLayout) content_layout.findViewById ( R.id.loadingView );

		
	}

	




	/**
	 * calling the respective functions to fetch and display data for various parts of the screen
	 */
	private void setValues() {
		try{
			if( !Util.isInternetAvailable() && isVisible()) {		
				progressLinear.setVisibility(View.GONE);
				PlayupLiveApplication.showToast( R.string.no_network );
			}
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						vTeamScheduleId =  DatabaseUtil.getInstance().getSectionIdFromLinkUrl( vTeamScheduleUrl );
						
						

						
						
						if(vTeamScheduleId != null && vTeamScheduleId.trim().length() > 0){
						
						
						if(PlayUpActivity.handler != null ){
							
							
							try {
								PlayUpActivity.handler.post(new Runnable() {
									
									@Override
									public void run() {
										try {
											setTopBar();	
											
											showTeamSchedule();
										} catch (Exception e) {
											Logs.show(e);
										}
										
									}
								});
							} catch (Exception e) {
								Logs.show(e);
							}
							
						}
}
					} catch (Exception e) {
						Logs.show(e);
					}
					
					
				}
			}).start();
			
			
		}catch (Exception e) {
			Logs.show(e);
		}
		
		
	}
	
	/**
	 * fetching and displaying the teamScheduleData
	 */
	
	private void showTeamSchedule(){
		
		
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					if(vTeamScheduleId != null && vTeamScheduleId.trim().length() > 0)
						
						
						
						teamScheduleData = DatabaseUtil.getInstance().getTeamScheduleData( vTeamScheduleId );
						
						
						
					
					
					if(PlayUpActivity.handler != null){
						
						
						try {
							PlayUpActivity.handler.post(new Runnable() {
								
								@Override
								public void run() {
									try {
										if( teamScheduleData != null && teamScheduleData.get("vContestId")!= null && teamScheduleData.get("vContestId").size() > 0 ) {
											
											progressLinear.setVisibility( View.GONE );
											teamScheduleList.setVisibility( View.VISIBLE );
											
											if( teamScheduleAdapter == null ) {
												
												teamScheduleAdapter = new TeamScheduleAdapter( teamScheduleData, vMainColor, vMainTitleColor, vSecColor, vSecTitleColor);
												teamScheduleList.setAdapter( teamScheduleAdapter );
												if( scrollIndex!=-1 ) {
													teamScheduleList.setSelection( scrollIndex );
												} else {
													teamScheduleList.setSelection( getNextLiveOrUpcomingContestIndex() );
												}
												
											} else {
												
												teamScheduleAdapter.setData( teamScheduleData, vMainColor, vMainTitleColor, vSecColor, vSecTitleColor);
											
											}
											
											
											
										} else {
											progressLinear.setVisibility( View.VISIBLE );
											teamScheduleList.setVisibility( View.GONE );
										}
									} catch (Exception e) {
										Logs.show(e);
									}
									
								}
							});
						} catch (Exception e) {
							Logs.show(e);
						}
						
					}
				} catch (Exception e) {
					Logs.show(e);
				}
				
			}
		}).start();

	}

	
	/**
	 * getting index of next live or upcoming match index
	 * this index will be used to automatically scroll to the respective match
	 */
	private int getNextLiveOrUpcomingContestIndex () {
		
		try {
			if( teamScheduleData!=null && teamScheduleData.get("vContestId")!=null && teamScheduleData.get("vContestId").size() >0 ) {
				
				int stackSize = teamScheduleData.get("vContestId").size() ;
				for ( int i=0; i<stackSize ; i++ ) {
					
					if( teamScheduleData.get("dEndTime")!=null &&  ( teamScheduleData.get("dEndTime").get(i) == null || teamScheduleData.get("dEndTime").get(i).trim().length() ==  0)) 
						return i;
				}
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
		
		return 0;
	}
	
	
	/**
	 * scheduling the refresh for the teamSchedule
	 */
	
	private void refreshTeamSchedule () {
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					int teamScheduleCacheTime = Integer.parseInt(dbUtil .getCacheTime( vTeamScheduleUrl ));
							
					if(teamScheduleCacheTime <= 0)
						teamScheduleCacheTime = 30;

					dbUtil = null;


					if( refreshTeamScheduleTimer == null)
						refreshTeamScheduleTimer = new Timer();
					
					if(refreshTeamScheduleTimerTask == null){
						refreshTeamScheduleTimerTask = new TimerTask() {
							@Override
							public void run() {
								if (Util.isInternetAvailable()) {
									fetchTeamScheduleData();
								}
							}
						};
						
						try{
							if (teamScheduleCacheTime > 0) {			
								refreshTeamScheduleTimer.schedule(refreshTeamScheduleTimerTask,(teamScheduleCacheTime * 1000),(teamScheduleCacheTime * 1000));

							}
						}catch(Exception e){
							Logs.show(e);
						}
					}
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show ( e );
				}
			}



		}).start();

		
	}
	



}






