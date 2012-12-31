package com.playup.android.fragment;

import java.util.HashMap;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Util;

public class NewsFragment extends MainFragment {

	private RelativeLayout progressBar;

	RelativeLayout content_layout ;

	private boolean isAgain = false;
	private LinearLayout sectionBase;
	private String vSectionId;
	private String vSectionUrl;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;


	
	
	private String vMainColor = null;
	private String vMainTitleColor = null;

	private boolean isDownLoaded = false;
	
	private Boolean isHrefURL	= false;



	private Hashtable<Integer, Hashtable<String, List<String>>> sectionData;
	private SectionGenerator sectionGenerator;
	
	private  Hashtable<String,TimerTask> refreshMatchesTask = new Hashtable<String,TimerTask>();
	private  Hashtable<String,Timer> refreshMatchesTimer = new Hashtable<String,Timer>();


	private Hashtable<String,TimerTask> refreshDisplayTask = new Hashtable<String,TimerTask>();
	private Hashtable<String,Timer>  refreshDisplayTimer = new Hashtable<String,Timer>();
	
	


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		content_layout = (RelativeLayout) inflater.inflate(R.layout.section_base, null);
		
		if (!isAgain) {
			setFromFragment(getArguments());
		}
		return content_layout;
	}

	@Override
	public void onAgainActivated(Bundle args) {
		isAgain = true;
		setFromFragment(args);

	}

	private void setFromFragment(Bundle bundle) {
		vSectionUrl = null;
	
		vMainColor = null;
		vMainTitleColor = null;

		vSecColor = null;
		vSecTitleColor = null;
		isHrefURL	 = false;

		if (bundle != null) {

			
			if (bundle.containsKey("vSectionUrl")) {
				vSectionUrl = bundle.getString("vSectionUrl");
			}
			if (bundle.containsKey("vMainColor")) {
				vMainColor = bundle.getString("vMainColor");
			}
			if (bundle.containsKey("vMainTitleColor")) {
				vMainTitleColor = bundle.getString("vMainTitleColor");
			}
			if (bundle != null && bundle.containsKey("vSecColor")) {
				vSecColor = bundle.getString("vSecColor");
			}
			if (bundle != null && bundle.containsKey("vSecTitleColor")) {
				vSecTitleColor = bundle.getString("vSecTitleColor");
			}
			if(bundle != null && bundle.containsKey("isHref")){
				isHrefURL 	= 	bundle.getBoolean("isHref");
			}

		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onStop() {
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
			//Logs.show(e);
		} catch (Error e) {
			//Logs.show(e);
		}

	}

	@Override
	public void onConnectionChanged(boolean isConnectionActive) {
		super.onConnectionChanged(isConnectionActive);

		if (isConnectionActive) {
			onResume();
		}
	}

	@Override
	public void onResume() {
		try {
			super.onResume();

			refreshMatchesTask = new Hashtable<String,TimerTask>();
			refreshMatchesTimer = new Hashtable<String,Timer>();
			
			refreshDisplayTask = new Hashtable<String,TimerTask>();
			refreshDisplayTimer = new Hashtable<String,Timer>();
				
			initializeViews();
			setValues();

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show(e);
		}

	}
	
	/**
	 * initializing the views
	 */

	private void initializeViews() {

		try {
			fetchSections();

			if (content_layout == null) {
				return;
			}
		
			sectionBase = (LinearLayout) content_layout.findViewById(R.id.section_base);
			progressBar = (RelativeLayout) content_layout.findViewById(R.id.progressBar);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	Logs.show(e);
		}

	}

	/**
	 * making api call to fetch the list of sections
	 */
	
	private void fetchSections( ) {

		try {
			if (vSectionUrl != null && vSectionUrl.trim().length() > 0) {

				if (Util.isInternetAvailable() && runnableList != null
						&& !runnableList.containsKey(vSectionUrl)) {

					
					isDownLoaded = false;
					runnableList.put(vSectionUrl, new Util().getNewsData(vSectionUrl,isHrefURL, vSectionUrl,runnableList ));

				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show(e);
		}
	}

	
	/**
	 * call the respective functions to fetch and display data on the screen
	 */
	
	private void setValues() {

		try {

			if (!Util.isInternetAvailable() && isVisible()) {
				progressBar.setVisibility(View.GONE);

				PlayupLiveApplication.showToast(R.string.no_network);
				isDownLoaded = true;

			}

		} catch (Exception e) {
			//Logs.show(e);
		}

		try {

			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						vSectionId = dbUtil.getSectionIdFromLinkUrl(vSectionUrl);
						
						//Log.e("123","vSectionId >>>>>>>>>>>>>>>>>>>>>>>> "+vSectionId);
						
						if (vSectionId != null
								&& !vSectionId.equalsIgnoreCase("")) {

							String vChildMainColor = dbUtil.getSectionMainColor(vSectionId,vSectionUrl);
							String vChildMainTitleColor = dbUtil.getSectionMainTitleColor(vSectionId,vSectionUrl);

							String vChildSecondaryColor = dbUtil
									.getSectionSecondaryColor(vSectionId,vSectionUrl);
							String vChildSecondaryTitleColor = dbUtil.getSectionSecondaryTitleColor(vSectionId,vSectionUrl);

							if (vChildMainColor != null
									&& vChildMainColor.trim().length() > 0)
								vMainColor = vChildMainColor;

							if (vChildMainTitleColor != null
									&& vChildMainTitleColor.trim().length() > 0)
								vMainTitleColor = vChildMainTitleColor;

							if (vChildSecondaryColor != null
									&& vChildSecondaryColor.trim().length() > 0)
								vSecColor = vChildSecondaryColor;

							if (vChildSecondaryTitleColor != null
									&& vChildSecondaryTitleColor.trim()
											.length() > 0)
								vSecTitleColor = vChildSecondaryTitleColor;

							setTopBar();
							setSectionData();

						}
					} catch (Exception e) {
						//Logs.show(e);
					}

				}
			}).start();

		} catch (Exception e) {
		//	Logs.show(e);
		}
	}
	
	/**
	 * setting the color and title of topbar
	 */

	private void setTopBar() {

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {

					String vSectionTitle = DatabaseUtil.getInstance()
							.getSectionTile(vSectionId);

					

					Bundle b = new Bundle();
					b.putString("vMainColor", vMainColor);
					b.putString("vMainTitleColor", vMainTitleColor);
					Message msg = new Message();
					msg.setData(b);
					HashMap<String, String> title = new HashMap<String, String>();
					title.put("vTitle", vSectionTitle);
					msg.obj = title;
					PlayupLiveApplication.callUpdateTopBarFragments(msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}

			}
		}).start();

	}
	
	/**
	 * fetching and displaying the sectionData
	 */

	public void setSectionData() {
		
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				
				try {
					sectionData = DatabaseUtil.getInstance().getSectionData( vSectionId );

					if( PlayUpActivity.handler != null  ) {
						PlayUpActivity.handler.post( new Runnable() {
							
							@Override
							public void run() {
								try {
									if( sectionData !=null && sectionData.size() >0  ) {
										progressBar.setVisibility( View.GONE );

										if( sectionGenerator == null )
											{
												sectionGenerator = new  SectionGenerator(sectionBase, sectionData, vMainColor, vMainTitleColor, vSecColor, vSecTitleColor);								
											}
										else
											sectionGenerator.setData( sectionBase, sectionData );
									} else if( isDownLoaded ) {
										progressBar.setVisibility( View.GONE );
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									//Logs.show(e);
								}			
							}
							} );
						}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
				
			}
		}).start();

	
	}
	
	
	


	
	@Override
	public void onUpdate(final Message msg) {
		super.onUpdate(msg);



		if(PlayUpActivity.handler != null){

			PlayUpActivity.handler.post(new Runnable() {



				@Override
				public void run() {


					try {
						if(!isVisible()) {
							return;
						}
						
						
						if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
							
							fetchSections();
							
						}
	
						if(msg != null && msg.obj != null &&  msg.obj.toString().equalsIgnoreCase("callChevron")) {
							//moving to the parent
							PlayupLiveApplication.getFragmentManagerUtil().popBackStackImmediate();

						}

						if( msg != null && msg.obj != null &&  msg.obj.toString().equalsIgnoreCase("Sectiondata")){
							//first dsiplay of the sectionData
							isDownLoaded = true;
							if( msg.arg1 ==1 ) {
								setValues();
							} else {
								if( progressBar!=null && progressBar.getVisibility() == View.VISIBLE )
									progressBar.setVisibility( View.GONE );
							}
							refreshContests();
							refreshDisplayItems();
							
						} else if (msg != null && msg.obj != null &&  msg.obj.toString().equalsIgnoreCase("updateSectionItems") ) {
							//refresh of the sectionData
							setSectionData();
						} else  if(msg != null && msg.obj != null &&  ( msg.obj.toString().equalsIgnoreCase("MatchHomeFragment_getScores")
								||msg.obj.toString().equalsIgnoreCase("LiveMatches") )){
							//contest data in features
							if( sectionGenerator!=null  ) {
								sectionGenerator.resetFeaturesData();
							}
							
							if( msg.obj.toString().equalsIgnoreCase("MatchHomeFragment_getScores") && msg.getData()!=null  ) {
								String contestUrl = msg.getData().getString("vContestUrl");
								refreshMatches( contestUrl, true,msg.getData().getBoolean("isHref") );
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
					//	Logs.show ( e );
					}


				}
			});
		}


	}


	/**
	 * getting the list of live/upcoming contests and scheduling the refresh.
	 */
	
	private void refreshContests() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				
				try {
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
					//Logs.show ( e );
				}
				
			}
		}).start();



	}

	
	/**
	 * getting the displayUrl for tiles and scheduling the refresh
	 */
	private void refreshDisplayItems() {

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					if( vSectionId!=null && !vSectionId.equalsIgnoreCase("")){
						Hashtable<String, List<String>> contestData = DatabaseUtil.getInstance().getDisplayUrlForSection( vSectionId );
						if( contestData!= null && contestData.get("vDisplayHrefUrl")!=null &&  contestData.get("vDisplayHrefUrl").size()>0) {
							for( int i =0; i< contestData.get("vDisplayHrefUrl").size(); i++ ) {
								refreshDisplay(contestData.get("vDisplayHrefUrl").get(i),true,contestData.get("vContentId").get(i),contestData.get("vBlockContentId").get(i));
							}

						}else if( contestData!= null && contestData.get("vDisplayUrl").size()>0) {
							for( int i =0; i< contestData.get("vDisplayUrl").size(); i++ ) {
								refreshDisplay(contestData.get("vDisplayUrl").get(i),false,contestData.get("vContentId").get(i),contestData.get("vBlockContentId").get(i));
							}

						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}
				

			}
		}).start();



	}

/**
 * scheduling the refresh for the individual tiles
 * @param vDisplayUrl
 * @param vContentId
 * @param vBlockContentId
 */
	private void refreshDisplay ( final String vDisplayUrl,final Boolean isHref,final String vContentId,final String vBlockContentId ) {

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
								//Logs.show ( e );
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
					//Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();



	}

	/**
	 * scheduling the refresh for the individual contests in features
	 * @param vCompetitionLiveUrl
	 * @param fromOnUpdate
	 */

	
	private void refreshMatches ( final String vCompetitionLiveUrl, final boolean fromOnUpdate,final boolean isHref ) {

		Runnable r = new Runnable () {

			@Override
			public void run() {
				try {
					
				
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					
					if( fromOnUpdate && ! dbUtil.isLiveContestUrl( vCompetitionLiveUrl) )
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
							//	Logs.show ( e );
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
				//	Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();



	}
}
