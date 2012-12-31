package com.playup.android.fragment;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.SectionGenerator;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;

import com.playup.android.util.Util;
/**
 * Called from league selection fragment, Menu fragment( optional) when any league is selected
 */
public class LeagueLobbyFragment extends MainFragment {


	private RelativeLayout progressBar;

	RelativeLayout content_layout ;

	private boolean isAgain = false;
	private LinearLayout sectionBase;
	private String vSectionId;
	private String vSectionUrl;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;


	private LinearLayout dots;

	
	private String vMainColor = null;
	private String vMainTitleColor = null;

	private boolean isDownLoaded = false;
	
	private Boolean isHrefURL	= false;

	private ScrollView scrollView;

	private Hashtable<Integer, Hashtable<String, List<String>>> sectionData;
	private SectionGenerator sectionGenerator;
	
	private  Hashtable<String,TimerTask> refreshMatchesTask = new Hashtable<String,TimerTask>();
	private  Hashtable<String,Timer> refreshMatchesTimer = new Hashtable<String,Timer>();


	private Hashtable<String,TimerTask> refreshDisplayTask = new Hashtable<String,TimerTask>();
	private Hashtable<String,Timer>  refreshDisplayTimer = new Hashtable<String,Timer>();
	
	private String fromFragment = null;
	
	private String vCompetitionId = null;



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		content_layout = (RelativeLayout) inflater.inflate( R.layout.section_base, null);
		if ( !isAgain ) {
			setFromFragment ( getArguments() );
		}

		return content_layout;
	}

	@Override
	public void onAgainActivated(Bundle args) {
		isAgain = true;
		setFromFragment( args );

	}

	/**
	 * getting data sent from previous fragment
	 * @param bundle
	 */
	private void setFromFragment ( Bundle bundle ) {

		//		vSectionId = null;
		fromFragment = null;
		vCompetitionId = null;
		
		vMainColor = null;
		vMainTitleColor = null;
		
		vSecColor = null;
		 vSecTitleColor = null;
		 isHrefURL		= 		false;

		if (bundle != null && bundle.containsKey("fromFragment")) {
			fromFragment = bundle.getString("fromFragment");
		} if (bundle != null && bundle.containsKey("vCompetitionId")) {
			vCompetitionId = bundle.getString("vCompetitionId");
		} 
		if (bundle != null && bundle.containsKey("vMainColor")) {
			vMainColor = bundle.getString("vMainColor");
		}if (bundle != null && bundle.containsKey("vMainTitleColor")) {
			vMainTitleColor = bundle.getString("vMainTitleColor");
		}if (bundle != null && bundle.containsKey("vSecColor")) {
			vSecColor = bundle.getString("vSecColor");
		}if (bundle != null && bundle.containsKey("vSecTitleColor")) {
			vSecTitleColor = bundle.getString("vSecTitleColor");
		}
		/*if(bundle != null && bundle.containsKey("vSecTitleColor")){
			isHrefURL 	= 	 bundle.getBoolean("isHref");
		}*/

	}



	@Override
	public void onDestroy () {
		super.onDestroy();
		fromFragment = null;


	}
	@Override
	public void onStop () {
		super.onStop();
		try {
			Iterator it = refreshMatchesTask.values().iterator();
			while(it.hasNext()){
				((TimerTask) it.next()).cancel();
			}


			it = refreshMatchesTimer.values().iterator();
			while(it.hasNext()){
				((Timer) it.next()).cancel();
			}


			it = refreshDisplayTask.values().iterator();
			while(it.hasNext()){
				((TimerTask) it.next()).cancel();
			}


			it = refreshDisplayTimer.values().iterator();
			while(it.hasNext()){
				((Timer) it.next()).cancel();
			}

			refreshMatchesTask.clear();
			refreshMatchesTimer.clear();

			refreshDisplayTask.clear();
			refreshDisplayTimer.clear();

		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
				
		
		

	}

	@Override
	public void onPause() {

		super.onPause();

		try {
			Iterator it = refreshMatchesTask.values().iterator();
			while(it.hasNext()){
				((TimerTask) it.next()).cancel();
			}


			it = refreshMatchesTimer.values().iterator();
			while(it.hasNext()){
				((Timer) it.next()).cancel();
			}


			it = refreshDisplayTask.values().iterator();
			while(it.hasNext()){
				((TimerTask) it.next()).cancel();
			}


			it = refreshDisplayTimer.values().iterator();
			while(it.hasNext()){
				((Timer) it.next()).cancel();
			}

			refreshMatchesTask.clear();
			refreshMatchesTimer.clear();

			refreshDisplayTask.clear();
			refreshDisplayTimer.clear();
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}

	}




	@Override
	public void onResume() {
		super.onResume();

		refreshMatchesTask = new Hashtable<String,TimerTask>();
		refreshMatchesTimer = new Hashtable<String,Timer>();
		
		refreshDisplayTask = new Hashtable<String,TimerTask>();
		refreshDisplayTimer = new Hashtable<String,Timer>();
			
	
		if(content_layout != null){

			try {
				initializeViews();			
				setValues();

			} catch ( Exception e ) {
//				Logs.show(e);

			}
		}


	}


	/**
	 * initializing views
	 */
	private void initializeViews( ) {

		try {
			fetchSections();
			if ( content_layout == null ) {
				return;
			}

			scrollView = (ScrollView) content_layout.findViewById(R.id.scrollView);	
			sectionBase = (LinearLayout) content_layout.findViewById(R.id.section_base);
			progressBar = (RelativeLayout) content_layout.findViewById(R.id.progressBar);
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}

	}


	/**
	 * Calling section API
	 */
	private void fetchSections ( ) {

		new Thread(new Runnable() {

			@Override
			public void run() {
			
				try {
				
					Hashtable<String, Object> result = null;
					if(vCompetitionId != null && vCompetitionId.trim().length() > 0)
	
					/*	vSectionUrl = DatabaseUtil.getInstance().getSectionUrlForLeagueLobby(vCompetitionId);*/
						result = DatabaseUtil.getInstance().getSectionUrlForLeagueLobby(vCompetitionId);
						if( result!=null && result.get("url") != null && result.get("url").toString().trim().length() > 0){
							
							vSectionUrl = (String) result.get("url");
							isHrefURL = (Boolean) result.get("isHref");
						
							if ( vSectionUrl != null  && runnableList != null && !runnableList.containsKey ( vSectionUrl ) && Util.isInternetAvailable() ) {
								isDownLoaded = false;

								runnableList.put ( vSectionUrl, new Util ( ).getLeagueLobby ( vSectionUrl,isHrefURL, vCompetitionId, runnableList) );

							}
						
						}else{
							// null from db..
							
						}
						
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}
			}
		}).start();


	}

	/**
	 * setting data on UI
	 */
	private void setValues() {

		try {
			// checking network status
			if( !Util.isInternetAvailable() && isVisible()) {
				progressBar.setVisibility(View.GONE);
				PlayupLiveApplication.showToast( R.string.no_network );
					isDownLoaded = true;	

			}

		} catch (Exception e) {
//			Logs.show(e);
		}

		try{
				new Thread(new Runnable() {
					
				@Override
				public void run() {
					try {
						if(vCompetitionId!=null && !vCompetitionId.equalsIgnoreCase("")){
							
							DatabaseUtil dbUtil = DatabaseUtil.getInstance();	
							
						String vChildMainColor = dbUtil.getSectionMainColorForLeague(vCompetitionId);
						String vChildMainTitleColor = dbUtil.getSectionMainTitleColorForLeague ( vCompetitionId );
						

						 String vChildSecondaryColor = dbUtil.getSectionSecColorForLeague ( vCompetitionId );
						 String vChildSecondaryTitleColor = dbUtil.getSectionSecTitleColorForLeague ( vCompetitionId );
						
						
						if(vChildMainColor != null && vChildMainColor.trim().length() > 0)
							 vMainColor = vChildMainColor;
						 
						 if(vChildMainTitleColor != null && vChildMainTitleColor.trim().length() > 0)
							 vMainTitleColor = vChildMainTitleColor;
						 
						 
						 if(vChildSecondaryColor != null && vChildSecondaryColor.trim().length() > 0)
							 vSecColor = vChildSecondaryColor;
						 
						 if(vChildSecondaryTitleColor != null && vChildSecondaryTitleColor.trim().length() > 0)
							 vSecTitleColor = vChildSecondaryTitleColor;
						 
						 
						 
						 if(PlayUpActivity.handler != null){
							 
							 PlayUpActivity.handler.post(new Runnable() {
								
								@Override
								public void run() {
									try {
										if(!isVisible())
											return;
										
										setTopBar();
										setSectionData();
									} catch (Exception e) {
//										Logs.show(e);
									}

									
								}
							});
						 }
						 
						 
						}
					} catch (Exception e) {
//						Logs.show(e);
					}
					
				}
			}).start();
			
			
		}catch(Exception e){
//			Logs.show(e);

		}

	}

	
	/**
	 * setting topbar data
	 */
	private void setTopBar() {
		
		new Thread(new Runnable(){

			@Override
			public void run() {

				try {
					HashMap< String, String > map = new HashMap<String, String>();

					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT c.vCompetitonName FROM competition c WHERE c.vCompetitionId ='"+vCompetitionId+"'");
					
					if ( c != null ) {

						if ( c.getCount() > 0 ) {
							c.moveToFirst();
							map.put( "vCompetitionName", c.getString( c.getColumnIndex( "vCompetitonName" ) ) );
							
						}
						c.close();
						c = null;
						
						Bundle b = new Bundle();
						b.putString("vMainColor",vMainColor );
						b.putString("vMainTitleColor",vMainTitleColor );
						Message msg = new Message ();
						msg.setData(b);
						msg.obj = map;
						PlayupLiveApplication.callUpdateTopBarFragments(msg);
					}
				} catch (Exception e) {
//					Logs.show ( e );
				} 

			}

		}).start();


	}

	
	/**
	 * setting section data
	 */
	public void setSectionData() {
		
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				
				try {
					
					// fetching sectionId using competionIds
					if( vSectionId == null ) {
						
						if( vCompetitionId != null ) {
							vSectionId = DatabaseUtil.getInstance().getSectionId( vCompetitionId );
						}
						
					}
						
					if( vSectionId == null || vSectionId.trim().length() ==0 )
						return;
					
					sectionData = DatabaseUtil.getInstance().getSectionData( vSectionId );

					if( PlayUpActivity.handler != null  ) {
						PlayUpActivity.handler.post( new Runnable() {
							
							@Override
							public void run() {
								try {
									if( sectionData !=null && sectionData.size() >0  ) {
										progressBar.setVisibility( View.GONE );

										if( sectionGenerator == null )
											sectionGenerator = new  SectionGenerator(sectionBase, sectionData, vMainColor, vMainTitleColor, vSecColor, vSecTitleColor);								
										else
											sectionGenerator.setData( sectionBase, sectionData );
									} else if( isDownLoaded ) {
										progressBar.setVisibility( View.GONE );
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
//									Logs.show(e);
								}
							}
							} );
						}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show(e);
				}
				
			}
		}).start();

	
	}
	
	

	@Override
	public void onUpdate(final Message msg) {
		super.onUpdate(msg);

		try {
			if(PlayUpActivity.handler != null){

				PlayUpActivity.handler.post(new Runnable() {

					@Override
					public void run() {

						try {
								if(!isVisible()) {
									return;
								}
							
								// handling chevron
								if(msg != null && msg.obj != null &&  msg.obj.toString().equalsIgnoreCase("callChevron")){
									Constants.isGrayBar = false;								
									PlayupLiveApplication.getFragmentManagerUtil().popBackStackImmediate();
								}
							
								// called from section API
								if( msg != null && msg.obj != null &&  msg.obj.toString().equalsIgnoreCase("Sectiondata")){
									
									isDownLoaded = true;
									
									if( msg.arg1 ==1 ) {
										setValues();
									} else {
										if( progressBar!=null && progressBar.getVisibility() == View.VISIBLE )
											progressBar.setVisibility( View.GONE );
									}
									
									refreshContests();
									refreshDisplayItems();
									
								} else if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("updateSectionItems") ) {
									// individual item refresh
									setSectionData();
								} else  if(msg != null && msg.obj != null &&  ( msg.obj.toString().equalsIgnoreCase("MatchHomeFragment_getScores")
										||msg.obj.toString().equalsIgnoreCase("LiveMatches") )){
							
									if( sectionGenerator!=null  ) {
										sectionGenerator.resetFeaturesData();
									}
									
									if( msg.obj.toString().equalsIgnoreCase("MatchHomeFragment_getScores") && msg.getData()!=null  ) {
										String contestUrl = msg.getData().getString("vContestUrl");
										refreshMatches( contestUrl, true ,msg.getData().getBoolean("isHref"));
									}
									
								} else if ( msg != null && msg.obj != null &&  msg.obj.toString().equalsIgnoreCase("handleBackButton") ) {
									// back button handling
									
											if(PlayUpActivity.popUp != null && PlayUpActivity.popUp.popupWindow.isShowing()){
												PlayUpActivity.popUp.popupWindow.dismiss();
												PlayUpActivity.popUp = null;
												PlayUpActivity.mediaPlayerService.stopTimer();
											}else{
												Constants.isGrayBar = false;
			//								PlayupLiveApplication.getFragmentManagerUtil().popBackStack();
											
											PlayupLiveApplication.getFragmentManagerUtil().popBackStackImmediate();
										}
									
								}else if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
									
									fetchSections();
									
								}else if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("UpdateTime")){
									
									

										
												try {
													if ( !isVisible() ) {
														return;
													}
													if(PlayUpActivity.popUp != null && PlayUpActivity.popUp.popupWindow.isShowing()){
														PlayUpActivity.popUp.updateTime(msg.getData().getString("time"));
													}

													
													
													
													
												} catch (Exception e) {
													// TODO Auto-generated catch block
													Logs.show ( e );
												}
											
										
										
									
									
									
									
									
								}else if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("ShowBuffering")){
									
									
												try {
													if ( !isVisible() ) {
														return;
													}
													
													if(PlayUpActivity.popUp != null && PlayUpActivity.popUp.popupWindow.isShowing()){
														PlayUpActivity.popUp.showBuffering();
													}
													
													
													
													
												} catch (Exception e) {
													// TODO Auto-generated catch block
													Logs.show ( e );
												}
										
									
									
									
								}
						} catch (Exception e) {
							// TODO Auto-generated catch block
//							Logs.show ( e );
						}



					}
				});
			}
		} catch (Exception e) {
//			Logs.show ( e );
		}


	}

/**
 * refreshing contests/matches	
 */
	private void refreshContests() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					
					if( vSectionId == null ) {
						
						if( vCompetitionId != null ) {
							vSectionId = DatabaseUtil.getInstance().getSectionId( vCompetitionId );
						}
						
					}
					
					if( vSectionId!=null && !vSectionId.equalsIgnoreCase("")){
						Hashtable<String, List<String>> contestData = DatabaseUtil.getInstance().getLiveContestsOnWelocmeScreenData( vSectionId );
						
						if( contestData!= null && contestData.get("vHighlightId").size()>0) {
							DatabaseUtil dbUtil = DatabaseUtil.getInstance();
							Hashtable<String,Object> result;
							for( int i =0; i< contestData.get("vHighlightId").size(); i++ ) {
					result = new Hashtable<String, Object>();
								result = dbUtil.getContestUrlFromContestId( contestData.get("vHighlightId").get(i));
								if(result != null && result.containsKey("isHref")){
									boolean isHref = ((Boolean) result.get("isHref")).booleanValue();
									String vContestUrl = (String) result.get("vContestUrl");
									refreshMatches( vContestUrl, false,isHref );
								}
								
								
								

							}

						}

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}
				
			}
		}).start();



	}

	/**
	 * refreshing section items ( if it needs to be refreshed )
	 */
	private void refreshDisplayItems() {

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					
					if( vSectionId == null ) {
						
						if( vCompetitionId != null ) {
							vSectionId = DatabaseUtil.getInstance().getSectionId( vCompetitionId );
						}
						
					}
					
					if( vSectionId!=null && !vSectionId.equalsIgnoreCase("")){
						Hashtable<String, List<String>> contestData = DatabaseUtil.getInstance().getDisplayUrlForSection( vSectionId );
						if( contestData!= null && contestData.get("vDisplayHrefUrl").size()>0 ) {
							for( int i =0; i< contestData.get("vDisplayHrefUrl").size(); i++ ) {
								refreshDisplay( contestData.get("vDisplayHrefUrl").get(i),true,contestData.get("vContentId").get(i),contestData.get("vBlockContentId").get(i));
							}

						}
						else if( contestData!= null && contestData.get("vDisplayUrl").size()>0 ) {
							for( int i =0; i< contestData.get("vDisplayUrl").size(); i++ ) {
								refreshDisplay( contestData.get("vDisplayUrl").get(i),false,contestData.get("vContentId").get(i),contestData.get("vBlockContentId").get(i));
							}

						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}
				

			}
		}).start();



	}


	/**
	 * Polling  section items
	 */
	private void refreshDisplay ( final String vDisplayUrl,final boolean isHref,final String vContentId,final String vBlockContentId ) {

		Runnable r = new Runnable () {



			@Override
			public void run() {
				try {

					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					int cacheTime = Integer.parseInt ( dbUtil.getCacheTime ( vDisplayUrl ) );
					if ( refreshDisplayTask != null ) {
						if ( refreshDisplayTask.containsKey ( vDisplayUrl ) ) {
							refreshDisplayTask.get(vDisplayUrl).cancel();
						}
					}
					refreshDisplayTask.put ( vDisplayUrl, new TimerTask() {

						@Override
						public void run() {
							try {
								if ( runnableList!=null && !runnableList.containsKey ( vDisplayUrl ) &&  Util.isInternetAvailable() ) {
									runnableList.put ( vDisplayUrl, new Util().getDisplayUpdateForSectionItems( vDisplayUrl,isHref,vContentId,vBlockContentId, runnableList ) );
								}
							} catch (Exception e) {
//								Logs.show ( e );
							}
						}
					});


					if ( refreshDisplayTimer != null ) {
						if ( refreshDisplayTimer.containsKey ( vDisplayUrl ) ) {
							refreshDisplayTimer.get(vDisplayUrl).cancel();
						}
					}
					refreshDisplayTimer.put(vDisplayUrl, new Timer() );


					if(cacheTime > 0) {
						refreshDisplayTimer.get(vDisplayUrl).schedule(refreshDisplayTask.get(vDisplayUrl), (cacheTime * 1000), (cacheTime * 1000));
					}

				} catch ( Exception e )  {
//					Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();



	}


/**
 * polling contests/matches
 */
	private void refreshMatches ( final String vCompetitionLiveUrl , final boolean fromOnUpdate,final boolean isHref ) {

		Runnable r = new Runnable () {

			@Override
			public void run() {
				try {
					
				
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					
					if( fromOnUpdate && ! dbUtil.isLiveContestUrl(vCompetitionLiveUrl) )
						return;
				
					
					int cacheTime = Integer.parseInt ( dbUtil.getCacheTime ( vCompetitionLiveUrl ) );
					if ( refreshMatchesTask != null ) {
						if ( refreshMatchesTask.containsKey ( vCompetitionLiveUrl ) ) {
							refreshMatchesTask.get(vCompetitionLiveUrl).cancel();
						}
					}
					refreshMatchesTask.put ( vCompetitionLiveUrl, new TimerTask() {

						@Override
						public void run() {
							try {
								if ( runnableList!=null && !runnableList.containsKey ( vCompetitionLiveUrl ) && Util.isInternetAvailable() ) {
									runnableList.put ( vCompetitionLiveUrl, new Util().getLiveContests ( vCompetitionLiveUrl, runnableList,isHref ) );
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
//								Logs.show ( e );
							}
						}
					});

					if ( refreshMatchesTimer != null ) {
						if ( refreshMatchesTimer.containsKey ( vCompetitionLiveUrl ) ) {
							refreshMatchesTimer.get(vCompetitionLiveUrl).cancel();
						}
					}
					refreshMatchesTimer.put(vCompetitionLiveUrl, new Timer() );
	
					if(cacheTime > 0) {
						refreshMatchesTimer.get(vCompetitionLiveUrl).schedule(refreshMatchesTask.get(vCompetitionLiveUrl), (cacheTime * 1000), (cacheTime * 1000));
					}

				} catch ( Exception e )  {
//					Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();



	}
	


}
