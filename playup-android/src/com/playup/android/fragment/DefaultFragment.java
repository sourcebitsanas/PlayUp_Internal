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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.SectionGenerator;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

/**
 * This fragment called from splash on launch, if there is featured data. <br>
 */

public class DefaultFragment extends MainFragment {


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

	private boolean isDownloaded = false;
	private boolean isHref	= 	false;

	private ScrollView scrollView;

	private Hashtable<Integer, Hashtable<String, List<String>>> sectionData;
	private SectionGenerator sectionGenerator;
	
	private  Hashtable<String,TimerTask> refreshMatchesTask = new Hashtable<String,TimerTask>();
	private  Hashtable<String,Timer> refreshMatchesTimer = new Hashtable<String,Timer>();


	private Hashtable<String,TimerTask> refreshDisplayTask = new Hashtable<String,TimerTask>();
	private Hashtable<String,Timer>  refreshDisplayTimer = new Hashtable<String,Timer>();
	
	
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
	 * getting dat sent from previous fragment
	 */
	private void setFromFragment ( Bundle bundle ) {
		try{
		vSectionUrl = null;
		vSectionId = null;

		if (bundle != null && bundle.containsKey("vSectionUrl")) {
			vSectionUrl = bundle.getString("vSectionUrl");
			
		} if (bundle != null && bundle.containsKey("isHref")) {
			isHref = bundle.getBoolean("isHref");
			
		}
		
		}catch(Exception e){
			Logs.show(e);
		}
		
	}



	@Override
	public void onDestroy () {
		super.onDestroy();

	}

	@Override
	public void onPause () {
		super.onPause();
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
			Logs.show(e);
		} catch ( Error e) {
			Logs.show(e);
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
	 * initializing views
	 */
	private void initializeViews( ) {
		fetchSections();
		if ( content_layout == null ) {
			return;
		}
		scrollView = (ScrollView) content_layout.findViewById(R.id.scrollView);	
		sectionBase = (LinearLayout) content_layout.findViewById(R.id.section_base);
		progressBar = (RelativeLayout) content_layout.findViewById(R.id.progressBar);

	}


	/**
	 * calling section API
	 */
	private void fetchSections( ) {
		
		try{
		if ( vSectionUrl != null && vSectionUrl.trim().length ( )  > 0 ) {

			if ( runnableList != null &&  ! runnableList.containsKey ( vSectionUrl ) && Util.isInternetAvailable() ) {
				isDownloaded = false;
				
				
				runnableList.put ( vSectionUrl, new Util().getWelComeData ( vSectionUrl,isHref, vSectionUrl, runnableList,  false ) );
			}

		}
		}catch(Exception e){
			Logs.show(e);
		}

	}

	
	/**
	 * displaying data on UI
	 */
	private void setValues() {
		try{

		try {


			if( !Util.isInternetAvailable() && isVisible()) {	
				   progressBar.setVisibility( View.GONE );
					PlayupLiveApplication.showToast( R.string.no_network );
					isDownloaded = true;

			}

		} catch (Exception e) {

		}


		new Thread(new Runnable(){

			@Override
			public void run() {

				DatabaseUtil dbUtil = DatabaseUtil.getInstance();
				vSectionId = DatabaseUtil.getInstance().getDefaultSectionFromRootResource();
		
				vMainColor = dbUtil.getSectionMainColor (vSectionId, vSectionUrl );
				 vMainTitleColor = dbUtil.getSectionMainTitleColor ( vSectionId, vSectionUrl );
				 
				  vSecColor = dbUtil.getSectionSecondaryColor ( vSectionId, vSectionUrl );
				  vSecTitleColor = dbUtil.getSectionSecondaryTitleColor ( vSectionId, vSectionUrl );
				
				
				
				if(vSectionId!=null && !vSectionId.equalsIgnoreCase("")){
					try {

						setTopBar();
						setSectionData();

					} catch ( Exception e ) {
						Logs.show ( e );
					}

				}
			}
		}).start();
		
		}catch(Exception e){
			Logs.show(e);
		}

	}

	
	/**
	 * showing section data ( features, tiles, stacked)  
	 */
	public void setSectionData() {
		try{
	
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
										//Toast.makeText(PlayUpActivity.context, "i am in if", Toast.LENGTH_SHORT).show();
										if( sectionGenerator == null )
											sectionGenerator = new  SectionGenerator(sectionBase, sectionData, vMainColor, vMainTitleColor, vSecColor, vSecTitleColor);								
										else
											sectionGenerator.setData( sectionBase, sectionData );
									} else if( isDownloaded ) {
										//Toast.makeText(PlayUpActivity.context, "i am in else", Toast.LENGTH_SHORT).show();
										progressBar.setVisibility( View.GONE );
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									Logs.show(e);
								}			
							}
							} );
						}else{
							//Toast.makeText(PlayUpActivity.context, "i am in handler  else", Toast.LENGTH_SHORT).show();
						}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show(e);
				}
				
			}
		}).start();

		}catch(Exception e){
			Logs.show(e);
		}
	}
	
	/**
	 * setting topbar
	 */

	private void setTopBar() {

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					HashMap< String, String > map = new HashMap<String, String>();			
					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( "SELECT vTitle "+
							"FROM sections WHERE vSectionId ='"+vSectionId+"'");
					
					if ( c != null ) {

						if ( c.getCount() > 0 ) {
							c.moveToFirst();
							map.put( "title", c.getString( c.getColumnIndex( "vTitle" ) ) );							
							
						}
						c.close();
						c = null;
						
						Message msg = new Message ();
						Bundle b = new Bundle();
						b.putString("vMainColor",vMainColor );
						b.putString("vMainTitleColor",vMainTitleColor );
						msg.setData(b);
						msg.obj = map;
						PlayupLiveApplication.callUpdateTopBarFragments( msg );
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show ( e );
				}

			}
		}).start();

	}

	

	@Override
	public void onUpdate(final Message msg) {
		// TODO Auto-generated method stub
		super.onUpdate(msg);

		try{
		if(PlayUpActivity.handler != null){

			PlayUpActivity.handler.post(new Runnable() {

				@Override
				public void run() {

					try {
						if( !isVisible() )
							return;

						
						

						// called from section API 
						if( msg != null && msg.obj != null &&  msg.obj.toString().equalsIgnoreCase("Sectiondata")){
							Log.e("123", "INSIDE ONUPDATE DEFAULT SECTION");
							isDownloaded = true;
							if( msg.arg1 ==1 ) {
								setValues();
							} else {
								if( progressBar != null && progressBar.getVisibility() == View.VISIBLE )
									progressBar.setVisibility( View.GONE );
							}
							refreshContests();
							refreshDisplayItems();
							
						} else if (msg != null && msg.obj != null &&  msg.obj.toString().equalsIgnoreCase("updateSectionItems") ) {
							// called from individual section items's update API
							setSectionData();
						} else  if(msg != null && msg.obj != null &&  ( msg.obj.toString().equalsIgnoreCase("MatchHomeFragment_getScores")
								||msg.obj.toString().equalsIgnoreCase("LiveMatches") )){

							// called from contest API
							if( sectionGenerator!=null  ) {
								sectionGenerator.resetFeaturesData();
							}
							
							if( msg.obj.toString().equalsIgnoreCase("MatchHomeFragment_getScores") && msg.getData()!=null  ) {
								String contestUrl = msg.getData().getString("vContestUrl");
								refreshMatches( contestUrl, true,msg.getData().getBoolean("isHref"));
							}
						}else if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
							
							fetchSections();
							
						}
							

					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show ( e );
					}
				}


			});

		}
		}catch(Exception e){
			Logs.show(e);
		}


	}

	
/**
 * refreshing contests	
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
								
								if(result  != null && result.containsKey("isHref") && result.containsKey("url")){
									
									boolean isHref = ((Boolean) result.get("isHref")).booleanValue();
									String vContestUrl = (String) result.get("url");
									
									/*refreshMatches( vContestUrl, false,isHref );*/
									refreshMatches( vContestUrl, false,isHref );
								}
								
								

							}

						}

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show ( e );
				}
				
			}
		}).start();



	}

	
	/**
	 * refreshing section items
	 */
	private void refreshDisplayItems() {

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					//Praveem : added if condi.
					if( vSectionId!=null && !vSectionId.equalsIgnoreCase("")){
						Hashtable<String, List<String>> contestData = DatabaseUtil.getInstance().getDisplayUrlForSection( vSectionId );
						if( contestData!= null && contestData.get("vDisplayHrefUrl").size()>0) {
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
					Logs.show ( e );
				}
				

			}
		}).start();



	}

	/**
	 * polling section items
	 */

	private void refreshDisplay ( final String vDisplayUrl,final boolean isHref, final String vContentId,final String vBlockContentId ) {

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
									/*runnableList.put ( vDisplayUrl, new Util().getDisplayUpdateForSectionItems( vDisplayUrl,vContentId,vBlockContentId, runnableList ) );*/
									runnableList.put ( vDisplayUrl, new Util().getDisplayUpdateForSectionItems( vDisplayUrl,isHref,vContentId,vBlockContentId, runnableList ) );
								}
							} catch (Exception e) {
								Logs.show ( e );
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
					Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();



	}


	/**
	 *  polling contests
	 *//*
	private void refreshMatches ( final String vCompetitionLiveUrl, final boolean fromOnUpdate,final boolean isHref ) {

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
								Logs.show ( e );
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
					Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();



	}
*/
	/**Praveen : asper  the href
	 *  polling contests
	 */
	private void refreshMatches ( final String vCompetitionLiveUrl,final boolean fromOnUpdate,final boolean isHref ) {

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
								Logs.show ( e );
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
					Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();



	}

	



}
