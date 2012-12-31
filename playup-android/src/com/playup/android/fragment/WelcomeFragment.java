package com.playup.android.fragment;



import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.R.bool;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.SectionGenerator;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Util;


public class WelcomeFragment extends MainFragment {


	private RelativeLayout progressBar;
	RelativeLayout content_layout ;
	private boolean isAgain = false;
	private LinearLayout sectionBase;
	private String vSectionId;
	private String vSectionUrl;	
	private Boolean isHref	= 	false;
	private String vSecColor = null;
	private String vSecTitleColor = null;
	private String vMainColor = null;
	private String vMainTitleColor = null;

	private boolean isDownloaded = false;



	private Hashtable<Integer, Hashtable<String, List<String>>> sectionData;
	private SectionGenerator sectionGenerator;
	
	private  Hashtable<String,TimerTask> refreshMatchesTask = new Hashtable<String,TimerTask>();
	private  Hashtable<String,Timer> refreshMatchesTimer = new Hashtable<String,Timer>();


	private Hashtable<String,TimerTask> refreshDisplayTask = new Hashtable<String,TimerTask>();
	private Hashtable<String,Timer>  refreshDisplayTimer = new Hashtable<String,Timer>();
	
	private String vBaseSectionTargetId;

	
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

	
	
	private void setFromFragment ( Bundle bundle ) {

		vSectionUrl = null;
		vSectionId = null;
		vMainColor = null;
		vMainTitleColor = null;
		
		vSecColor = null;
		 vSecTitleColor = null;

		if (bundle != null && bundle.containsKey("vSectionUrl")) {
			vSectionUrl = bundle.getString("vSectionUrl");
		} if (bundle != null && bundle.containsKey("vSectionId")) {
			vSectionId = bundle.getString("vSectionId");
		}if (bundle != null &&bundle.containsKey("vMainColor")) {
			vMainColor = bundle.getString("vMainColor");
		}if (bundle != null && bundle.containsKey("vMainTitleColor")) {
			vMainTitleColor = bundle.getString("vMainTitleColor");
		}if (bundle != null && bundle.containsKey("vSecColor")) {
			vSecColor = bundle.getString("vSecColor");
		}if (bundle != null && bundle.containsKey("vSecTitleColor")) {
			vSecTitleColor = bundle.getString("vSecTitleColor");
		}
		if (bundle != null && bundle.containsKey("isHref")) {
			isHref = bundle.getBoolean("isHref");
		}
	}



	@Override
	public void onDestroy () {
		super.onDestroy();
	}

	@Override
	public void onStop () {
		try {
			super.onStop();
			
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
//			Logs.show(e);
		}catch (Error e) {
//			Logs.show(e);
		}

	}


	@Override
	public void onConnectionChanged(boolean isConnectionActive) {
		super.onConnectionChanged(isConnectionActive);
		
		if ( isConnectionActive ) {
			onResume();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		
		refreshMatchesTask = new Hashtable<String,TimerTask>();
		refreshMatchesTimer = new Hashtable<String,Timer>();
		
		refreshDisplayTask = new Hashtable<String,TimerTask>();
		refreshDisplayTimer = new Hashtable<String,Timer>();

		
		initializeViews();


		setValues();


	}

		/**
		 * 
		 * initializing all the views
		 * 
		 * also calling the method to fetch sections		
		 */
	private void initializeViews( ) {

		try {
			fetchSections();

			
			if ( content_layout == null ) {
				return;
			}

			if ( content_layout == null ) {
				return;
			}

		
			sectionBase = (LinearLayout) content_layout.findViewById(R.id.section_base);
			progressBar = (RelativeLayout) content_layout.findViewById(R.id.progressBar);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}

		
	}

	/**
	 * making the api call to fetch sections data
	 * 
	 */
	private void fetchSections() {

		
		
		try {
			if ( vSectionUrl != null && vSectionUrl.trim().length ( )  > 0 ) {

				if ( runnableList != null &&  ! runnableList.containsKey ( vSectionUrl ) && Util.isInternetAvailable() ) {
					isDownloaded = false;
					runnableList.put ( vSectionUrl, new Util().getWelComeData ( vSectionUrl,isHref, vSectionId, runnableList,  false ) );
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}

	}
	
	
	/**
	 * calling functions to display data in the respective part of the screen
	 */
	
	
	private void setValues() {

		try {


			if( !Util.isInternetAvailable() && isVisible()) {	
				   progressBar.setVisibility( View.GONE );
					PlayupLiveApplication.showToast( R.string.no_network );
					isDownloaded = true;

			}

		} catch (Exception e) {
			
//			Logs.show(e);

		}

		
		new Thread(new Runnable(){

			

			@Override
			public void run() {


				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					vBaseSectionTargetId = dbUtil.getBaseSectionTargetId( vSectionId );
					
					if(vBaseSectionTargetId!=null && !vBaseSectionTargetId.equalsIgnoreCase("")){
					
					String vChildMainColor = dbUtil.getSectionMainColor (vBaseSectionTargetId, vSectionUrl );
					String vChildMainTitleColor = dbUtil.getSectionMainTitleColor ( vBaseSectionTargetId, vSectionUrl );
					
					String vChildSecondaryColor = dbUtil.getSectionSecondaryColor ( vBaseSectionTargetId, vSectionUrl );
					 String vChildSecondaryTitleColor = dbUtil.getSectionSecondaryTitleColor ( vBaseSectionTargetId, vSectionUrl );
					
					
					if(vChildMainColor != null && vChildMainColor.trim().length() > 0)
						 vMainColor = vChildMainColor;
					 
					 if(vChildMainTitleColor != null && vChildMainTitleColor.trim().length() > 0)
						 vMainTitleColor = vChildMainTitleColor;
					 
					 if(vChildSecondaryColor != null && vChildSecondaryColor.trim().length() > 0)
						 vSecColor = vChildSecondaryColor;
					 
					 if(vChildSecondaryTitleColor != null && vChildSecondaryTitleColor.trim().length() > 0)
						 vSecTitleColor = vChildSecondaryTitleColor;
					 
					
					

					if ( PlayUpActivity.handler != null ) {
						PlayUpActivity.handler.post( new Runnable( ) {

							@Override
							public void run() {
								
								try {
									if(!isVisible())
										return;
							
									setTopBar();
									setSectionData();

								} catch (Exception e) {
//									Logs.show(e);
								}

							

							}
						});
					}
				}
				} catch (Exception e) {
//					Logs.show ( e );
				}


			}
		}).start();




	}

	/**
	 * setting the topbar color and title
	 * 
	 */
	private void setTopBar() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					
					HashMap< String, String > map = new HashMap<String, String>();
					
					
				
					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT vTitle FROM sections WHERE VsectionId ='"+vBaseSectionTargetId+"'");
					
					if ( c != null ) {

						if ( c.getCount() > 0 ) {
							c.moveToFirst();

							
							map.put( "title", c.getString( c.getColumnIndex( "vTitle" ) ) );
							


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
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}

			}
		}).start();



	}

	/**
	 * 
	 * fetching and displaying the section data
	 * 
	 * 
	 */
	public void setSectionData() {
		
		try {
			new Thread( new Runnable() {
				
				@Override
				public void run() {
					
					try {
						sectionData = DatabaseUtil.getInstance().getSectionData( vBaseSectionTargetId );

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
										}	else if( isDownloaded ) {
											progressBar.setVisibility( View.GONE );
										}
									} catch (Exception e) {
//										Logs.show(e);
									}	
								}
								} );
							}
					} catch (Exception e) {
//						Logs.show(e);
					}
					
				}
			}).start();
		} catch (Exception e) {
//			Logs.show(e);
		}

	
	}
	
	
	



	@Override
	public void onUpdate(final Message msg) {
	
		try {
			super.onUpdate(msg);

			
			if(PlayUpActivity.handler != null){

				PlayUpActivity.handler.post(new Runnable() {

					@Override
					public void run() {

						try {
							if(!isVisible())
								return;


							if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
								//Log.e("123", "Onupdate of whats hot");
								fetchSections();
								
							}
							if( msg != null && msg.obj != null &&  msg.obj.toString().equalsIgnoreCase("Sectiondata")){
								//update for the  display  of all the data
								isDownloaded = true;
								if( msg.arg1 ==1 ) {
									setValues();
								} else {
									if( progressBar!=null && progressBar.getVisibility() == View.VISIBLE )
										progressBar.setVisibility( View.GONE );
								}
								refreshContests();
								refreshDisplayItems();
								
							}
							else if (msg != null && msg.obj != null &&  msg.obj.toString().equalsIgnoreCase("updateSectionItems") ) {
								 //refresh for the section data
								 setSectionData();
							} else  if(msg != null && msg.obj != null &&  ( msg.obj.toString().equalsIgnoreCase("MatchHomeFragment_getScores")
									||msg.obj.toString().equalsIgnoreCase("LiveMatches") )){
								// setting the data for the contests in the features
								if( sectionGenerator!=null  ) {
									sectionGenerator.resetFeaturesData();
								}
								if( msg.obj.toString().equalsIgnoreCase("MatchHomeFragment_getScores") && msg.getData()!=null  ) {
									//refresh for the contests in the features
									String contestUrl = msg.getData().getString("vContestUrl");
									refreshMatches( contestUrl, true,msg.getData().getBoolean("isHref") );
								}
							}

						} catch (Exception e) {
//							Logs.show ( e );
						}
					}


				});

			}
		} catch (Exception e) {
//			Logs.show(e);
		}






	}

	/**
	 * getting the list of list of live and upcoming contests in the featues data 
	 * 
	 * starting the refresh for the contests
	 * 
	 */

	private void refreshContests() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
					if(vBaseSectionTargetId!=null && !vBaseSectionTargetId.equalsIgnoreCase("")){
						Hashtable<String, List<String>> contestData = DatabaseUtil.getInstance().getLiveContestsOnWelocmeScreenData(vBaseSectionTargetId);

						if( contestData!= null && contestData.get("vHighlightId").size()>0) {
							DatabaseUtil dbUtil = DatabaseUtil.getInstance();
							Hashtable<String,Object> result;
							for( int i =0; i< contestData.get("vHighlightId").size(); i++ ) {

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
	 * getting the list of display urls for refreshing the tiles
	 * 
	 */
	
	private void refreshDisplayItems() {

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					if( vSectionId!=null && !vSectionId.equalsIgnoreCase("")){
						Hashtable<String, List<String>> contestData = DatabaseUtil.getInstance().getDisplayUrlForSection( vSectionId );
						if( contestData!= null && contestData.get("vDisplayHrefUrl").size()>0) {
							for( int i =0; i< contestData.get("vDisplayHrefUrl").size(); i++ ) {
								refreshDisplay(contestData.get("vDisplayHrefUrl").get(i),true,contestData.get("vContentId").get(i),contestData.get("vBlockContentId").get(i));
							}

						}
						else if( contestData!= null && contestData.get("vDisplayUrl").size()>0) {
							for( int i =0; i< contestData.get("vDisplayUrl").size(); i++ ) {
								refreshDisplay(contestData.get("vDisplayUrl").get(i),false,contestData.get("vContentId").get(i),contestData.get("vBlockContentId").get(i));
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
	 * 
	 * 
	 * @param vDisplayUrl
	 * @param vContentId
	 * @param vBlockContentId
	 * 
	 * refresh for each individual tile
	 * 
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
									runnableList.put ( vDisplayUrl, new Util().getDisplayUpdateForTiles( vDisplayUrl,isHref,vContentId,vBlockContentId, runnableList ) );
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
	 * refresh for the individual contests in the features
	 * 
	 * @param vCompetitionLiveUrl
	 * @param fromOnUpdate
	 */
	private void refreshMatches ( final String vCompetitionLiveUrl, final boolean fromOnUpdate,final boolean isHref ) {

		Runnable r = new Runnable () {

			@Override
			public void run() {
				try {
					
				
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();

					if(vCompetitionLiveUrl == null || vCompetitionLiveUrl.trim().length() == 0)
						return;
					
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