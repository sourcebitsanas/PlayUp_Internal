package com.playup.android.fragment;



import java.util.ArrayList; 


import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.MatchHeaderGenerator;
import com.playup.android.adapters.MatchRoomGridAdapter;
import com.playup.android.adapters.MatchRoomMessage;
import com.playup.android.adapters.ProviderAdapter;
import com.playup.android.adapters.PublicMatchesTicketAdapter;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.dialog.InternetConnectivityDialog;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;

import com.playup.android.util.Util;
/**
 * Shows
 * 1) list of rooms for selected contest <br>
 * 2) selected contest data along with its associated contests
 */
public class MatchRoomFragment extends MainFragment implements OnClickListener,OnTouchListener, OnItemClickListener{	

	// Views declaration
	private TextView liveCountDown;
	private TextView stripLiveCount;
	private TextView liveCountUp;
	private RelativeLayout stripDown, stripUp, commonStrip,matchHeaderBottom;
	private Gallery matchGallery;
	private GridView roomBase;
	private RelativeLayout progressView;
	private RelativeLayout content_layout;
	private RelativeLayout matchHeaderLayout;
	public  RelativeLayout header;	
	private ListView leaderBoardList;

	
	//Datatype variable declaration
	public  String vContestId = null;
	private String vContestLobbyUrl = null;
	private boolean isAgain =  false;
	private String vSecColor = null;
	private String vSecTitleColor = null;
	private String vMainColor = null;
	private String vMainTitleColor = null;	
	private String fromFragment = null;
	public static boolean menuClicked = false;
	private float yPosition = 0;
	private boolean onclick = false;
	private String vRoundId = null;
	private ArrayList <MatchRoomMessage> roomData;
	private ArrayList<MatchRoomMessage> thirdPartyTilesData;
	private boolean showTiles = false;
	private boolean expand = true;
	private boolean animation_completed = true;

	
	//Object declaration
	private PublicMatchesTicketAdapter publicMatchesTicketAdapter;
	private MatchRoomGridAdapter matchRoomGridAdapter ;
	private MatchHeaderGenerator matchHeaderGenerator = null;
	private ImageDownloader imageDownloader = new ImageDownloader();
	private AlertDialog alert;
	private LayoutInflater inflater ;
	private LayoutParams params;
	private Dialog shareDialog;
	private boolean isStripExpanded = false;
	
	
	//Timer and Timer task declaration
	private Hashtable<String,TimerTask> refreshDisplayTask = new Hashtable<String,TimerTask>();
	private Hashtable<String,Timer>  refreshDisplayTimer = new Hashtable<String,Timer>();
	private TimerTask timerTask;
	private Timer timer;
	private TimerTask refreshScoreTask;
	private Timer refreshSubjectTimer;
	private Timer refreshMatchesTimer;
	private TimerTask refreshMatchesTask;
	private boolean isContestLobbyUrlHref = false;


	@Override
	public void onDestroy () {
		super.onDestroy();
		vContestId = null;
		// if dialog is visible, close it
		if( alert != null && alert.isShowing() ) {
			alert.dismiss();
			alert = null;
		}
	}

	@Override
	public void onStop () {
		super.onStop();

		// cancelling timers and timertasks
		if ( timer != null ) {
			timer.cancel();
			timer = null;
		}
		if ( timerTask != null ) {
			timerTask.cancel();
			timerTask = null;
		}

		if ( refreshSubjectTimer != null ) {
			refreshSubjectTimer.cancel();
			refreshSubjectTimer = null;
		}
		if ( refreshScoreTask != null ) {
			refreshScoreTask.cancel();
			refreshScoreTask = null;
		}

		if ( refreshMatchesTimer != null ) {
			refreshMatchesTimer.cancel();
			refreshMatchesTimer = null;
		}

		if ( refreshMatchesTask != null ) {
			refreshMatchesTask.cancel();
			refreshMatchesTask = null;
		}

		
		Iterator it = refreshDisplayTask.values().iterator();
		while(it.hasNext()){
			((TimerTask) it.next()).cancel();
		}

		it = refreshDisplayTimer.values().iterator();
		while(it.hasNext()){
			((Timer) it.next()).cancel();
		}

		refreshDisplayTask.clear();
		refreshDisplayTimer.clear();

	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		content_layout = ( RelativeLayout ) inflater.inflate( R.layout.match_room, null );

		if ( !isAgain ) {
			setContestId ( getArguments() );
		}

		FlurryAgent.onEvent("lobby");
		return content_layout;
	}


	@Override
	public void onResume() {
		try {
			super.onResume();
			
			//Initializing variables with default values
			showTiles = false;
			expand = true;
			matchHeaderGenerator=null;
			roomData  = new ArrayList <MatchRoomMessage>();
			matchRoomGridAdapter = null;
			if ( imageDownloader == null ) {
				imageDownloader = new ImageDownloader();
			}
			refreshDisplayTask = new Hashtable<String,TimerTask>();
			refreshDisplayTimer = new Hashtable<String,Timer>();
			publicMatchesTicketAdapter= null;
			if(vContestId == null)			
				isDownloading = true;			
			else				
				isDownloading = false;


			initialize( content_layout );

			InputMethodManager inputManager = (InputMethodManager)PlayUpActivity.context.getSystemService( PlayUpActivity.context.INPUT_METHOD_SERVICE); 
			inputManager.hideSoftInputFromWindow( roomBase.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			inputManager = null;
			
			
			refreshData ();
			refreshScores();
			setValues() ;
			
			showAssociatedContests();
			setLiveCount();
			getCompetitionData();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}


	
	public void getAssociatedData () {

		try {
			Hashtable<String, Object> result = DatabaseUtil.getInstance().getAssociatedContestUrl( vContestId );

			if(result != null && result.containsKey("isHref")){
				
				String assoicatedContestUrl = (String) result.get("url");
				
				if((Boolean) result.get("isHref")){
					
					if ( assoicatedContestUrl != null && runnableList != null && !runnableList.containsKey( Constants.GET_ASSOCIATED_CONTEST_DATA ) && Util.isInternetAvailable() ) {
						runnableList.put(Constants.GET_ASSOCIATED_CONTEST_DATA, new Util().getAssoiciatedContestsData(assoicatedContestUrl,runnableList,true) ) ;
					}
					
				}else{
					
					
					
					if ( assoicatedContestUrl != null && runnableList != null && !runnableList.containsKey( Constants.GET_ASSOCIATED_CONTEST_DATA ) && Util.isInternetAvailable() ) {
						runnableList.put(Constants.GET_ASSOCIATED_CONTEST_DATA, new Util().getAssoiciatedContestsData(assoicatedContestUrl,runnableList,false) ) ;
					}
				}
				
				
				
			}
			
			
		} catch (Exception e) {
			Logs.show(e);
		}

	}

	
	
	/**
	 * Setting associated contest data
	 */
	private void showAssociatedContests () {

		try {

			if (publicMatchesTicketAdapter == null) {
				
				publicMatchesTicketAdapter = new PublicMatchesTicketAdapter( vContestId, matchGallery );
				matchGallery.setAdapter( publicMatchesTicketAdapter );
			} else {
				publicMatchesTicketAdapter.setData( vContestId );
			}
		} catch ( Exception e ) {
			Logs.show ( e );
		} 
	}
	
	

	public void onPause () {
		super.onPause();

		matchHeaderGenerator = null;
		cancelRunnable();

	}

	
	/**
	 * setting the contest id from other fragment. 
	 */
	private void setContestId ( Bundle bundle ) {
		try {
			vMainColor = null;
			vMainTitleColor = null;
			
			 vSecColor = null;
			 vSecTitleColor = null;

			if ( bundle != null && bundle.containsKey( "vContestId" ) ) {
				vContestId = bundle.getString( "vContestId" );
			}

			if ( bundle != null && bundle.containsKey ( "vContestLobbyUrl" ) ) {
				vContestLobbyUrl = bundle.getString( "vContestLobbyUrl" );
				
				
				if ( vContestId == null ) {	
					vContestId = DatabaseUtil.getInstance().getContestIdFromContestLobbyUrl(vContestLobbyUrl);
				}
				
			}
			
			if ( bundle != null && bundle.containsKey ( "isHref" ) ) {
				isContestLobbyUrlHref  = bundle.getBoolean("isHref");
			}
				
					
			if(vContestId == null)
				DatabaseUtil.getInstance().removeEtag( vContestLobbyUrl );
	
			
			if(vContestLobbyUrl == null){
				Hashtable<String,Object> result = DatabaseUtil.getInstance().getContestLobbyUrl(vContestId);
				vContestLobbyUrl = 	(String) result.get("url");
				isContestLobbyUrlHref = (Boolean) result.get("isHref");
			}
			
			Log.e("123","vContestLobbyUrl >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   "+vContestLobbyUrl);
			
			
			Log.e("123","isContestLobbyUrlHref >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   "+isContestLobbyUrlHref);
			

			if ( bundle != null && bundle.containsKey( "fromFragment" ) ) {
				fromFragment = bundle.getString( "fromFragment" );
			} else {
				fromFragment = null;
			} if (bundle != null && bundle.containsKey("vMainColor")) {
				vMainColor = bundle.getString("vMainColor");
			} if (bundle != null && bundle.containsKey("vMainTitleColor")) {
				vMainTitleColor = bundle.getString("vMainTitleColor");
			} if (bundle != null && bundle.containsKey("vSecColor")) {
				vSecColor = bundle.getString("vSecColor");
			} if (bundle != null && bundle.containsKey("vSecTitleColor")) {
				vSecTitleColor = bundle.getString("vSecTitleColor");
			}
		} catch (Exception e) {
			
			Logs.show(e);
		}

	}


	@Override
	public void onUpdate(final Message msg) {

		try {
			// getting current visible fragment name
			String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();

			if (msg == null) {
				return;
			}

			
			if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
				
				getCompetitionData();
				getContestLobby();
				getConversationSubject();
				
			}
			//Handling score sharing, will be called from topbarFragment when tapped on share button
			if (msg.obj != null && msg.obj.toString().contains("share")) {
				
				if (msg.obj.toString().equalsIgnoreCase("shareScores")) {
					
					
					if (DatabaseUtil.getInstance().isUserAnnonymous()) {
						// if user is not logged in, navigate to login screen
						Bundle bundle = new Bundle();
						bundle.putBoolean("sharing", true);
						bundle.putString("fromFragment", topFragmentName);
						PlayupLiveApplication.getFragmentManagerUtil().setFragment("ProviderFragment", bundle);

					} else {
						// for logged in user show dialog with available providers
						if (vContestId != null)
							showShareDialog(false);
					}

				} else if (msg.obj.toString().equalsIgnoreCase("share")) {
					// if( vContestId != null )
					// showShareDialog( true );

				} else if (msg.obj.toString().contains("share_response")) {

					// display share response as a Toast
					String provider = msg.obj.toString();
					if (provider.split(":").length > 0)
						provider = provider.split(":")[1];
					final String toastMessage = PlayUpActivity.context
							.getResources().getString(R.string.shareResponse)
							+ " " + provider;

					if (msg.arg1 == 1) {
						PlayupLiveApplication.showToast(toastMessage);
					} else {
						PlayupLiveApplication.showToast(R.string.shareFailure);
					}

				}
				return;
			}

			// handling back button
			if (msg.obj != null && msg.obj.toString().equalsIgnoreCase("handleBackButton")) {
				if (fromFragment != null
						&& (fromFragment.equalsIgnoreCase("WelcomeFragment")
								|| fromFragment.equalsIgnoreCase("DefaultFragment")
								|| fromFragment.contains("LeagueLobbyFragment")
								|| fromFragment.contains("FixturesAndResultsFragment")
								|| fromFragment.contains("LiveSportsFragment")
								|| fromFragment.contains("NewsFragment") 
								|| fromFragment.contains("MatchHomeFragment")
								|| fromFragment.contains("TeamScheduleFragment"))) {
					PlayupLiveApplication.getFragmentManagerUtil()
							.popBackStackTill(fromFragment);
					return;
				}

			}
			
			// handling chevron
			if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase(
							"callPreviousFragment")) {

				if (fromFragment != null
						&& (fromFragment.equalsIgnoreCase("WelcomeFragment")
								|| fromFragment.contains("LeagueLobbyFragment")
								|| fromFragment.equalsIgnoreCase("DefaultFragment")
								|| fromFragment.contains("LiveSportsFragment")
								|| fromFragment.contains("FixturesAndResultsFragment") 
								|| fromFragment.contains("NewsFragment")
								|| fromFragment.contains("TeamScheduleFragment"))) {

					PlayupLiveApplication.getFragmentManagerUtil()
							.popBackStackTill(fromFragment);
					return;
				}
				
				// if this fragment called from menu, it should navigate to AllSportsFragment
				Bundle bundle = new Bundle();
				bundle.putString("vMainColor", vMainColor);
				bundle.putString("vMainTitleColor", vMainTitleColor);
				bundle.putString("vSecColor", vSecColor);
				bundle.putString("vSecTitleColor", vSecTitleColor);

				cancelRunnable();
				PlayupLiveApplication.getFragmentManagerUtil().setFragment("AllSportsFragment", bundle);
				menuClicked = false;

			}
			
			
			// called after completion of contestLobbyUrl API call
			if (msg.obj != null && msg.obj.toString().equalsIgnoreCase("ContestLobbyData")) {	
				getCompetitionData();

				if (PlayUpActivity.handler != null) {

					try {
						if (vContestId == null && msg.getData() != null && msg.getData().containsKey("vContestId") && msg.getData().getString("vContestId") != null) {						
							
							//getting contestId from message (ContestLobby API response) and setting contest data
							vContestId = msg.getData().getString("vContestId");
							isDownloading = false;
							
							//calling contestUrl API
							if (runnableList != null && Util.isInternetAvailable()) {

								Hashtable<String,Object> result = DatabaseUtil.getInstance().getContestUrlFromContestId(vContestId);
								String vContestUrl = (String) result.get("url");
								boolean isHref = ((Boolean) result.get("isHref")).booleanValue();
								if(isHref){
									
									runnableList.put(Constants.GET_CONTEST_CONVERSATION_MESSAGES,new Util().getContestsData(vContestUrl,runnableList,true));
									
								}else{
									runnableList.put(Constants.GET_CONTEST_CONVERSATION_MESSAGES,new Util().getContestsData(vContestUrl,runnableList,false));
								}
								
								

							}

							PlayUpActivity.handler.post(new Runnable() {

								@Override
								public void run() {
									try {
										if (!isVisible()) {
											return;
										}
										onResume();
									} catch (Exception e) {
										Logs.show(e);
									}
								}
							});
						} else {

							PlayUpActivity.handler.post(new Runnable() {

								@Override
								public void run() {
									try {
										if (!isVisible()) {
											return;
										}
										showTiles = true;
										refreshData();
										getConversationSubject();

										// setValues();
										setTopBar();
										showPanels();
										// setStackList();
										refreshTiles();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										Logs.show(e);
									}
								}
							});
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show(e);
					}

				}
			}
			
	
			if (msg.obj != null && msg.obj.toString().equalsIgnoreCase("ContestLobbyDataRefresh")) {
				if (PlayUpActivity.handler != null) {
					try {
							PlayUpActivity.handler.post(new Runnable() {

								@Override
								public void run() {
									try {
										if (!isVisible()) {
											return;
										}										
										showPanels();
										
									} catch (Exception e) {
										
										Logs.show(e);
									}
								}
							});
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show(e);
					}

				}
			}
			
			// called from third party tiles API
			if (msg != null && msg.obj != null
					&& msg.obj.toString().equalsIgnoreCase("UpdateTiles")) {

				if (PlayUpActivity.handler != null)
					PlayUpActivity.handler.post(new Runnable() {

						@Override
						public void run() {
							try {
								if (!isVisible()) {
									return;
								}

								showPanels();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								Logs.show(e);
							}

						}
					});

				// refreshSectiondata();
			}
			
			
			// called from contest API
			if (msg.obj != null
					&& msg.obj.toString().equalsIgnoreCase(
							"MatchHomeFragment_getScores")) {

				if (PlayUpActivity.handler != null) {
					PlayUpActivity.handler.post(new Runnable() {

						@Override
						public void run() {
							try {
								if (!isVisible()) {
									return;
								}

								getAssociatedData();
								showAssociatedContests();
								refreshMatches();
								refreshScores();
								setRoomData();
							} catch (Exception e) {
								Logs.show(e);
							}
						}
					});

				}
			}

			
			// called from after matchRoomGridAdapter, to create new room
			if (msg.obj != null
					&& msg.obj.toString().equalsIgnoreCase("callCreateRoom")) {

				DatabaseUtil dbUtil = DatabaseUtil.getInstance();
				/**
				 * if user is anonymous then show provider fragment.
				 */
				if (dbUtil.isUserAnnonymous()) {
					Bundle bundle = new Bundle();
					bundle.putString("fromFragment", topFragmentName);
					// PlayupLiveApplication.showToast(R.string.loginCreateMessage);
					PlayupLiveApplication.getFragmentManagerUtil().setFragment(
							"ProviderFragment", bundle);

					dbUtil = null;
					return;

				} else {

					Bundle bundle = new Bundle();
					bundle.putString("vContestId", vContestId);
					bundle.putString("vMainColor", vMainColor);
					bundle.putString("vMainTitleColor", vMainTitleColor);
					bundle.putString("vSecColor", vSecColor);
					bundle.putString("vSecTitleColor", vSecTitleColor);
					bundle.putString("fromFragment", topFragmentName);

					PlayupLiveApplication.getFragmentManagerUtil().setFragment(
							"CreateRoomFragment", bundle, -1, false);

					dbUtil = null;
					return;
				}

			}
			
			
			if (msg.obj != null && msg.obj.toString().equalsIgnoreCase( "callDirectCreateRoom")) {

				Bundle bundle = new Bundle();
				bundle.putString("fromFragment", topFragmentName);
				bundle.putString("vContestId", vContestId);
				bundle.putString("vMainColor", vMainColor);
				bundle.putString("vMainTitleColor", vMainTitleColor);
				bundle.putString("vSecColor", vSecColor);
				bundle.putString("vSecTitleColor", vSecTitleColor);
				PlayupLiveApplication.getFragmentManagerUtil().setFragment("CreateRoomFragment", bundle, -1, false);
				return;
			}

			// for updating the red ticket data
			if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("assoicated_contest_update")) {
				if (PlayUpActivity.handler != null) {

					PlayUpActivity.handler.post(new Runnable() {

						@Override
						public void run() {
							try {
								isFetchingCompetitionData = false;

								if (!isVisible()) {
									return;
								}

								showAssociatedContests();
								refreshMatches();
								setLiveCount();

							} catch (Exception e) {
								// TODO Auto-generated catch block
								Logs.show(e);
							}
						}
					});
				}

			}
		} catch (NotFoundException e) {
			Logs.show(e);
		}

	}


	@Override
	public void onAgainActivated(Bundle args) {

		isAgain = true;
		vContestId = null;
		setContestId( args );

	}


	/** 
	 * refreshing contestLobby ( data contains list of rooms )
	 */
	private void refreshData () {	//verified
		if ( timer ==  null ) {			
			timer = new Timer();		
		}

		if ( timerTask == null ) {
			
			new Thread(new Runnable() {	
				@Override
				public void run() {	
					try {
	
						timerTask = new TimerTask() {
	
							@Override
							public void run() {
								
								
									if(runnableList != null && !runnableList.containsKey(Constants.GET_CONTEST_LOBBY)){		
										
										runnableList.put(Constants.GET_CONTEST_LOBBY,new Util().getContestLobbyData(vContestLobbyUrl, runnableList,true,isContestLobbyUrlHref));
									}
								}
								
								
							
						};
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						
						
	
						int cacheTime = Integer.parseInt( dbUtil.getCacheTime( vContestLobbyUrl ) );						
						
	
						dbUtil = null;
						if ( cacheTime > 0 ) {							
							timer.scheduleAtFixedRate( timerTask, cacheTime * 1000, cacheTime * 1000 );
						}else{
							timerTask	=	null;
							timer = null;
						}
					} catch (Exception e) {
						// TODO: handle exception
						Logs.show ( e );
					}
				}
			}).start();
		
		}

	}



	/**
	 * refreshing the subject/scores
	 */
	public void refreshScores () { //verified


		if (refreshSubjectTimer != null) {
			refreshSubjectTimer.cancel();
		}
		refreshSubjectTimer = new Timer();

		if (refreshScoreTask != null) {
			refreshScoreTask.cancel();
			refreshScoreTask = null;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					int cacheTime_refreshScore = 0;
					refreshScoreTask = new TimerTask() {

						@Override
						public void run() {
							getConversationSubject();
						}
					};

					DatabaseUtil dbUtil = DatabaseUtil.getInstance();

					Hashtable<String, Object> result = dbUtil.getContestUrlFromContestId ( vContestId );
					String vContestUrl = (String) result.get("url");
					//Boolean isHrefURl = (Boolean) result.get("isHref");



					if(vContestUrl != null){

						String tmp 	=	dbUtil.getCacheTime(vContestUrl);
						if(tmp != null)
							cacheTime_refreshScore	=	Integer.parseInt(tmp);
					}
					vContestUrl = null;
					dbUtil = null;

					
					if (cacheTime_refreshScore > 0) {
						try {
							if ( refreshScoreTask != null && refreshSubjectTimer !=  null ) {
								refreshSubjectTimer.schedule(refreshScoreTask, cacheTime_refreshScore * 1000, (cacheTime_refreshScore * 1000));
							}
						} catch ( Exception e ) {
							Logs.show ( e );
						}
					}

				} catch (Exception e) {
					// TODO: handle exception
					Logs.show ( e );
				}


			}
		}).start();


	}


	/**
	 * calling contest API
	 */
	private void getConversationSubject() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result = dbUtil.getContestUrlFromContestId ( vContestId );
					
					if(result != null && result.containsKey("url") && result.containsKey("isHref")){
					
					String vContestUrl = (String) result.get("url");

					Boolean isHref = (Boolean) result.get("isHref");


					dbUtil = null;
					
					if(isHref){
						if( vContestUrl != null && runnableList!=null&&!runnableList.containsKey(Constants.GET_SCORE ) && Util.isInternetAvailable() ){
							runnableList.put(Constants.GET_SCORE,new Util().getScoreData(vContestUrl,runnableList,true));
						}
					}else{
						if( vContestUrl != null && runnableList!=null&&!runnableList.containsKey(Constants.GET_SCORE ) && Util.isInternetAvailable() ){
							runnableList.put(Constants.GET_SCORE,new Util().getScoreData(vContestUrl,runnableList,false));
						}
					}
					}
					
				} catch (Exception e) {
					Logs.show ( e );
				}

			}
		}).start();

	}


	/**
	 * setting  all the content in the views 
	 */
	private void initialize ( RelativeLayout content_layout ) {
		try{
			// initialize views
			initializeViews(content_layout);

			getContestLobby ();

			getConversationSubject();
			// set listeners
			setListeners();

			//Set Typeface
			
		}catch(Exception e){
			Logs.show ( e );
		}


	}

	/**
	 * contestLobby API ( rooms data)
	 */
	private void getContestLobby () {

		if(runnableList!=null&&!runnableList.containsKey(Constants.GET_CONTEST_LOBBY)  && Util.isInternetAvailable() ){
			runnableList.put(Constants.GET_CONTEST_LOBBY,new Util ().getContestLobby ( vContestLobbyUrl ,runnableList,isContestLobbyUrlHref));
		}
	}

	/**
	 *  initialising views
	 * @param content_layout
	 */
	private void initializeViews ( RelativeLayout content_layout)  {

		try{
			
			if ( content_layout == null ) {
				return;
			}
			roomBase = (GridView)content_layout.findViewById(R.id.roomBase);

			progressView=(RelativeLayout)content_layout.findViewById(R.id.progressView);
			matchHeaderLayout = ( RelativeLayout) content_layout.findViewById(R.id.matchHeaderLayout);
			matchHeaderBottom = ( RelativeLayout) content_layout.findViewById(R.id.matchHeaderBottom);

			stripDown = (RelativeLayout) content_layout.findViewById(R.id.stripDown);
			
			if ( stripDown != null && stripDown.getVisibility() == View.VISIBLE ) {
				stripDown.setVisibility(View.VISIBLE); 
			}
			
			stripUp = (RelativeLayout) content_layout.findViewById(R.id.stripUp);
			commonStrip = (RelativeLayout) content_layout.findViewById(R.id.commonStrip);
			matchGallery = (Gallery) content_layout.findViewById(R.id.matchGallery);
			liveCountDown = (TextView)content_layout.findViewById(R.id.stripLiveCountDown);
			stripLiveCount = (TextView)content_layout.findViewById(R.id.commonStripCount);
			liveCountUp = (TextView)content_layout.findViewById(R.id.stripLiveCountUp);
			liveCountDown.setTypeface( Constants.BEBAS_NEUE );
			liveCountUp.setTypeface( Constants.BEBAS_NEUE );
			stripLiveCount.setTypeface( Constants.BEBAS_NEUE );

			matchGallery.setBackgroundColor(Color.parseColor("#8C9696"));
			matchGallery.setUnselectedAlpha(1.0f);
			

		}catch(Exception e){

			Logs.show(e);
		}

	}


	/**
	 * setting the listeners
	 */
	private void setListeners () {
		//hero_base.setOnTouchListener(this);
		stripDown.setOnTouchListener(this);
		stripUp.setOnTouchListener(this);
		matchHeaderLayout.setOnClickListener(this);
	}


	/**
	 *  setting the values
	 */
	private void setValues () {

		showProgress(true);
		setTopBar();
		setRoomData();
		showPanels();

	}

	
	
	/**
	 * setting top bar data
	 */
	
	private void setTopBar(){	//verfied
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					
				
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					
					String contestLobbyUrl = dbUtil.getContestLobbyUrlForRefresh(vContestId);
					
					String vChildColor = dbUtil.getSectionMainColor("", contestLobbyUrl);
					String vChildTitleColor = dbUtil.getSectionMainTitleColor("", contestLobbyUrl);
					
					if(vChildColor != null && vChildColor.trim().length() > 0 )
						vMainColor = vChildColor;
					
					if(vChildTitleColor != null && vChildTitleColor.trim().length() > 0 )
						vMainTitleColor = vChildTitleColor;
					
					String vChildSecondaryColor = dbUtil.getSectionSecondaryColor ( "", contestLobbyUrl );
					String vChildSecondaryTitleColor = dbUtil.getSectionSecondaryTitleColor ( "", contestLobbyUrl );
					
					 
					 if(vChildSecondaryColor != null && vChildSecondaryColor.trim().length() > 0)
						 vSecColor = vChildSecondaryColor;
					 
					 if(vChildSecondaryTitleColor != null && vChildSecondaryTitleColor.trim().length() > 0)
						 vSecTitleColor = vChildSecondaryTitleColor;
						
					Bundle b = new Bundle();
					b.putString("vMainColor",vMainColor );
					b.putString("vMainTitleColor",vMainTitleColor );
					Message msg = new Message ();
					msg.setData(b);
					
					PlayupLiveApplication.callUpdateTopBarFragments(msg);
				} catch (Exception e) {
					Logs.show(e);
				}
				
			}
		}).start();
	
	}
	
	
	/**
	 * Setting the room data
	 */
	private void setRoomData() {

		Runnable r = new Runnable(){

			@Override
			public void run() {
				try {

					if(PlayUpActivity.handler != null){

						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {
	
								try {
									final Hashtable<String, List<String>> data = PlayupLiveApplication.getDatabaseWrapper().select("SELECT vContestId,dScheduledStartTime," +
											"dStartTime,dEndTime,iTotal1,iTotal2,vSummary1,vSummary2,vHomeTeamId,vAwayTeamId,vSummary,vSportsName,vCompetitionName,iWickets1, iWickets2,vOvers,vRunRate,vLastBall,vPlayerFirstName1,vPlayerFirstName2,vPlayerLastName1,vPlayerLastName2" +
											",vRole2,vRole1,vStats1,vStats2,vStrikerFirstName,vStrikerLastName,vStrikerStats,vNonStrikerFirstName,vNonStrikerLastName,vNonStrikerStats,vAnnotation,vAnnotation2,vSummary1,vSummary2,iGoals1,iBehinds1,iSuperGoals1,iGoals2,iBehinds2,iSuperGoals2,iInnnings,vInnningsHalf,iBalls1,iBalls2,iOut1,iOut2,iStrikes1,iStrikes2,vBase1,vBase2,iHasLiveUpdates,vLastEventName,vShortMessage, vLongMessage,vSportType,iActive1,iActive2, " +
											"( SELECT vDisplayName FROM teams WHERE vTeamId = vHomeTeamId  ) AS vHomeDisplayName, " +
											"( SELECT vDisplayName FROM teams WHERE vTeamId = vAwayTeamId  ) AS vAwayDisplayName," +
											"(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vHomeTeamId ) AS vHomeCalendarUrl," +
											"(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vAwayTeamId ) AS vAwayCalendarUrl  FROM contests WHERE vContestId = \""+vContestId+"\"");

								
									String sportType = null;
									if( data!= null && data.get("vSportType")!=null )
										sportType = data.get("vSportType").get(0);
									
									if( sportType == null )
										return;
									
									//setting and inflating the layouts for match header based on sport type
									
									if( matchHeaderGenerator== null ) {
										View headerLayout;
										if( inflater == null )
											inflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
										
										if( sportType!= null && sportType.equalsIgnoreCase(Constants.LEADERBOARD)  ) {
											headerLayout = (View) inflater.inflate(R.layout.match_header_leaderboard, null);
											leaderBoardList = (ListView) headerLayout.findViewById(R.id.leaderBoardList);
										} else if( sportType!= null && sportType.equalsIgnoreCase(Constants.SET_BASED_DATA)  )  {
											headerLayout = (View) inflater.inflate(R.layout.match_header_setbased, null);
										} else {
											headerLayout = (View) inflater.inflate(R.layout.match_header_normal, null);
										}
									
										matchHeaderLayout.setBackgroundResource(0);
										matchHeaderLayout.removeAllViews();
										matchHeaderLayout.addView( headerLayout );
										matchHeaderGenerator = new MatchHeaderGenerator(data, matchHeaderLayout, false, true );
										
										setDummyViewsHeight( sportType );
										headerLayout = null;
									} else {										
										matchHeaderGenerator.setData(data);
//										if( expand )
//											setDummyViewsHeight( sportType );
									}

								} catch (Exception e) {
									Logs.show ( e );
								}								


							}
						});

					}


				}catch(Exception e){

					Logs.show ( e );

				}

			}

		};

		Thread t = new Thread(r);
		t.start();

	}
	
	
	
	

//	private Hashtable< Integer , MatchRoomMessage > getConversationRooms ( ) {
//		final Hashtable< Integer , MatchRoomMessage> roomData  = new Hashtable< Integer , MatchRoomMessage> ();
//		new Thread(new Runnable(){
//
//			@Override
//			public void run() {
//
//				try {
//					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
//					DateUtil dateUtil = new DateUtil();
//
//					String contestLobbyUid =  dbUtil.getContestLobbyUid(vContestId);
//
//
//
//					Hashtable<String, List<String>> data = PlayupLiveApplication.getDatabaseWrapper().select ( " SELECT mcn.vConversationId AS vConversationId, UPPER ( mcn.vConversationName ) AS vConversationName, mcn.iTotalMessagePosts AS iTotalMessagePosts, " +
//							"(  SELECT  m.message_timestamp " +
//							"FROM message m " +
//							"LEFT JOIN conversation_message cm ON m.vConversationMessageId = cm.vConversationMessageId " +
//							"WHERE cm.vConversationId = mcn.vConversationId " +
//							"ORDER BY  m.message_timestamp " +
//							"DESC LIMIT 0, 1 ) AS dMessageTimeStamp " +
//							"FROM match_conversation_node mcn " +
//							"WHERE mcn.vContestLobbyUid = \"" + contestLobbyUid + "\" " +
//					"ORDER BY mcn.iOrder DESC" );
//
//
//					if ( data != null && data.get( "vConversationId" ).size() > 0 ) {
//
//						for ( int i = 0, len = data.get( "vConversationId" ).size() ; i < len ; i++ ) {
//
//							MatchRoomMessage m = new MatchRoomMessage ();
//
//
//
//
//							Cursor convMsg = PlayupLiveApplication.getDatabaseWrapper().selectQuery ( "SELECT message, message_timestamp, posted_by, fan_thumb_url " +
//									"FROM message  m " +
//									"LEFT JOIN conversation_message cm ON cm.vConversationMessageId = m.vConversationMessageId " +
//									"WHERE cm.vConversationId =  \"" + data.get( "vConversationId" ).get( i ) + "\" " +
//							"ORDER BY message_timestamp DESC " );
//
//							m.conversationId = data.get( "vConversationId" ).get( i ) ;
//							m.conversationName = data.get( "vConversationName" ).get( i ) ; 
//							m.totalMessages = data.get( "iTotalMessagePosts" ).get( i ) ;
//
//
//
//							if ( convMsg != null ) {
//
//								if ( convMsg.getCount() > 0 ) {
//
//									convMsg.moveToFirst();
//
//									if ( m.totalMessages == null ) {
//
//										m.totalMessages = convMsg.getCount() + "";
//									}
//
//									m.postedBy =  convMsg.getString( convMsg.getColumnIndex("posted_by"));
//									m.postedTime = dateUtil. gmt_to_local_timezone( convMsg.getString( convMsg.getColumnIndex("message_timestamp"))) ;
//									m.message = convMsg.getString( convMsg.getColumnIndex("message"));
//									m.imageUrl	=	convMsg.getString( convMsg.getColumnIndex("fan_thumb_url"));
//
//									//if(commentsNumber.getText().length() > 0){
//									//	commentsNumber.setBackgroundResource(R.drawable.comments_bubble);
//
//									//}
//
//
//								}
//								convMsg.close();
//								convMsg = null;
//							}
//
//							roomData.put( i , m );
//
//						}
//					}
//
//					if ( data != null ) {
//						data.clear();
//						data = null;
//
//					}
//
//					if(PlayUpActivity.handler != null){
//
//						PlayUpActivity.handler.post(new Runnable() {
//
//							@Override
//							public void run() {
//								try {
//									if(roomData.size()>0)
//
//										showProgress(false);
//									else
//										showProgress(true);
//								} catch (Exception e) {
//									// TODO Auto-generated catch block
//									Logs.show(e);
//								}
//
//							}
//						});
//
//					}
//
//
//					dbUtil = null;
//					dateUtil = null;
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					Logs.show ( e );
//				}
//
//
//			}
//
//
//		}).start();
//
//
//		return roomData;
//
//
//
//	}


	/**
	 * refreshing third party tiles
	 */
	private void refreshTiles() {	//verified

		new Thread(new Runnable() {

			@Override
			public void run() {


				try {
					Hashtable<String, List<String>> contestData = DatabaseUtil.getInstance().getDisplayUrlForContestLobby(vContestId);

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
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show ( e );
				}

			}
		}).start();



	}


	/**
	 * refreshing third party tiles
	 */
	private void refreshDisplay ( final String vDisplayUrl,final boolean isHref,final String vContentId,final String vBlockContentId ) {	//verified

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
								if ( runnableList!=null && !runnableList.containsKey ( vDisplayUrl )  && Util.isInternetAvailable() ) {
									runnableList.put ( vDisplayUrl, new Util().getDisplayUpdateForTiles ( vDisplayUrl,isHref,vContentId,vBlockContentId, runnableList ) );
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
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
	 * fetching third party tiles
	 */
	private void getTilesData(){


		Hashtable<String,List<String>> tilesData = DatabaseUtil.getInstance().getTilesDataFromContestId(vContestId);


		thirdPartyTilesData = new ArrayList<MatchRoomMessage>();



		if(tilesData != null && tilesData.get("vContentId") != null && tilesData.get("vContentId").size() > 0){

			for(int  i =0;i<tilesData.get("vContentId").size();i++){

				MatchRoomMessage tiles = new MatchRoomMessage();



				tiles.isThirdPartyTile = true;
				tiles.vContentId = tilesData.get("vContentId").get(i);
				tiles.vContentType = tilesData.get("vContentType").get(i);
				if(tilesData.get("vContentHrefUrl").get(i)!=null && tilesData.get("vContentHrefUrl").get(i).trim().length()>0){
					tiles.vContentUrl = tilesData.get("vContentHrefUrl").get(i);
				}
				else
				{
					tiles.vContentUrl = tilesData.get("vContentUrl").get(i);
				}
				//tiles.vContentUrl = tilesData.get("vContentUrl").get(i);
				if(tilesData.get("vDisplayHrefUrl").get(i)!=null && tilesData.get("vDisplayHrefUrl").get(i).trim().length()>0){
					tiles.vDisplayUrl = tilesData.get("vDisplayHrefUrl").get(i);
				}
				else
				{
					tiles.vDisplayUrl = tilesData.get("vDisplayUrl").get(i);
				}

				tiles.vDisplayType = tilesData.get("vDisplayType").get(i);

				tiles.vSummary = tilesData.get("vSummary").get(i);
				tiles.vFooterTitle = tilesData.get("vFooterTitle").get(i);
				tiles.vFooterSubTitle = tilesData.get("vFooterSubTitle").get(i);
				tiles.vSourceIcon = tilesData.get("vSourceIcon").get(i);
				tiles.vSocialIcon = tilesData.get("vSocialIcon").get(i);
				tiles.vImageUrl = tilesData.get("vImageUrl").get(i);
				if(tilesData.get("vLinkHrefUrl").get(i)!=null && tilesData.get("vLinkHrefUrl").get(i).trim().length()>0){
					tiles.vLinkUrl = tilesData.get("vLinkHrefUrl").get(i);
				}
				else
				{
					tiles.vLinkUrl = tilesData.get("vLinkUrl").get(i);
				}
			//	tiles.vLinkUrl = tilesData.get("vLinkUrl").get(i);
				tiles.vLinkType = tilesData.get("vLinkType").get(i);
				tiles.VName = tilesData.get("VName").get(i);
				tiles.vTimeStamp = tilesData.get("vTimeStamp").get(i);
				tiles.vBackgroundColor = tilesData.get("vBackgroundColor").get(i);
				tiles.vTitle = tilesData.get("vTitle").get(i);
				tiles.vBackgroundImage = tilesData.get("vBackgroundImage").get(i);
				tiles.vSource = tilesData.get("vSource").get(i);
				tiles.iLive = tilesData.get("iLive").get(i);

				thirdPartyTilesData.add(tiles);


			}


		}

	}

	/**
	 * Fetching the data from database  
	 */
	private void showPanels() {


		if ( roomBase == null  ) {
			return;
		}
		
		if(vContestId == null){
			
			if(isDownloading){
				
				
				return;
				
			}
			
		}

		new Thread(new Runnable(){

			@Override
			public void run() {

				try {
					
					final ArrayList<MatchRoomMessage> conversationData  = new ArrayList <MatchRoomMessage> ();
					
					if(vContestId != null){
					
					getTilesData();

					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					DateUtil dateUtil = new DateUtil();

					
					

					String contestLobbyUid =  dbUtil.getContestLobbyUid(vContestId);

					Hashtable<String, List<String>> data = PlayupLiveApplication.getDatabaseWrapper().select ( " SELECT mcn.vConversationId AS vConversationId, UPPER ( mcn.vConversationName ) AS vConversationName, mcn.iTotalMessagePosts AS iTotalMessagePosts, " +
							"(  SELECT  m.message_timestamp " +
							"FROM message m " +
							"LEFT JOIN conversation_message cm ON m.vConversationMessageId = cm.vConversationMessageId " +
							"WHERE cm.vConversationId = mcn.vConversationId " +
							"ORDER BY  m.message_timestamp " +
							"DESC LIMIT 0, 1 ) AS dMessageTimeStamp " +
							"FROM match_conversation_node mcn " +
							"WHERE mcn.vContestLobbyUid = \"" + contestLobbyUid + "\" " +
					"ORDER BY mcn.iOrder DESC" );

					
					if ( data != null && data.get( "vConversationId" ).size() > 0 ) {

						for ( int i = 0, len = data.get( "vConversationId" ).size() ; i < len ; i++ ) {

							MatchRoomMessage m = new MatchRoomMessage ();




							Cursor convMsg = PlayupLiveApplication.getDatabaseWrapper().selectQuery ( "SELECT message, message_timestamp, posted_by, fan_thumb_url " +
									"FROM message  m " +
									"LEFT JOIN conversation_message cm ON cm.vConversationMessageId = m.vConversationMessageId " +
									"WHERE cm.vConversationId =  \"" + data.get( "vConversationId" ).get( i ) + "\" " +
							" ORDER BY message_timestamp DESC " );

							m.conversationId = data.get( "vConversationId" ).get( i ) ;
							m.conversationName = data.get( "vConversationName" ).get( i ) ; 
							m.totalMessages = data.get( "iTotalMessagePosts" ).get( i ) ;



							if ( convMsg != null ) {

								if ( convMsg.getCount() > 0 ) {

									convMsg.moveToFirst();

									if ( m.totalMessages == null ) {

										m.totalMessages = convMsg.getCount() + "";
									}

									m.postedBy =  convMsg.getString( convMsg.getColumnIndex("posted_by"));
									m.postedTime = dateUtil. gmt_to_local_timezone( convMsg.getString( convMsg.getColumnIndex("message_timestamp"))) ;
									m.message = convMsg.getString( convMsg.getColumnIndex("message"));
									m.imageUrl	=	convMsg.getString( convMsg.getColumnIndex("fan_thumb_url"));

								}
								convMsg.close();
								convMsg = null;
							}
							m.isThirdPartyTile = false;
							conversationData.add(m);

						}
					}
					if ( data != null ) {
						data.clear();
						data = null;
					}
					
					
					}
					
					
					

					if ( PlayUpActivity.handler != null ) { 

						PlayUpActivity.handler.post ( new Runnable (  ) {

							@Override
							public void run() {
								try {
									roomData = new ArrayList < MatchRoomMessage >  ( ) ;

									// adding conversation data into room data 
									if ( conversationData != null && conversationData.size() > 0 ) {
										roomData.add ( conversationData.get ( 0 ) );								
									}

									// adding third party data in room data 
									if ( thirdPartyTilesData != null && thirdPartyTilesData.size ( )  > 0 ) {
										roomData.addAll ( roomData.size(), thirdPartyTilesData.subList ( 0, thirdPartyTilesData.size ( ) ) ) ;
									}
									

									if ( conversationData.size  ( ) > 1 ) {
										roomData.addAll ( thirdPartyTilesData.size ( ) + 1 , conversationData.subList ( 1, conversationData.size ( ) ) ) ;
									}

									
									// showing hiding progress dialog 
									showProgress( ( roomData.size ( ) > 0 || showTiles )? false : true );

									
									
									
									// inflating on UI
									if ( matchRoomGridAdapter == null ) {
										matchRoomGridAdapter = new MatchRoomGridAdapter ( roomData, vContestId ,
												roomBase,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor);
										roomBase.setAdapter( matchRoomGridAdapter );
									} else {
										matchRoomGridAdapter.setData( roomData ,vContestId,roomBase,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor );
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									Logs.show(e);
								}
							}
						});
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show ( e );
				}
				
			}
		}).start();
	}

	private void showProgress( boolean show ){
		if( !Util.isInternetAvailable() )
			show = false;


		try {

			if( show ) {
				roomBase.setVisibility(View.GONE);
				progressView.setVisibility(View.VISIBLE);
			} else {
				roomBase.setVisibility(View.VISIBLE);
				progressView.setVisibility(View.GONE);
			}

		} catch ( Exception e)  {
			Logs.show(e);
		}

	}


	/**
	 * A dialog to show providers to share scores
	 */
	private void showShareDialog( boolean refresh ) {

		try {
			if ( PlayUpActivity.context != null && Constants.isCurrent ) {
				TextView dialogTitle;
				ListView providerList;
				RelativeLayout dialogRoot;
				final DatabaseUtil dbUtil = DatabaseUtil.getInstance();
				final Hashtable<String, Object> result = dbUtil.getContestShareUrl( vContestId );
				String vShareUrl = "";
				boolean isHref = false;
				if(result != null && result.containsKey("isHref") &&  result.containsKey("url")){
					vShareUrl = (String) result.get("url");
					isHref = ((Boolean) result.get("isHref")).booleanValue();
					
				}
				final Hashtable<String, List<String>> data = DatabaseUtil.getInstance().getLoginProviders();

				ProviderAdapter providerAdapter = new ProviderAdapter( data );
				
				shareDialog = new Dialog(PlayUpActivity.context, android.R.style.Theme_Translucent_NoTitleBar);
				shareDialog.setContentView( R.layout.share_dialog);
	
				View dialogView = shareDialog.findViewById( R.id.dialogView );
				dialogView.setBackgroundColor( Color.WHITE );
				
				dialogTitle = ( TextView ) shareDialog.findViewById( R.id.dialogTitle);
				providerList = ( ListView ) shareDialog.findViewById(R.id.providerList);
				dialogRoot = (RelativeLayout )shareDialog.findViewById(R.id.dialogRoot);
				
				providerAdapter.setData( shareDialog, vShareUrl,isHref);
				providerList.setAdapter(providerAdapter);	
				dialogTitle.setTypeface( Constants.OPEN_SANS_REGULAR );
				
				shareDialog.show();


				dialogRoot.setOnClickListener( new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						shareDialog.dismiss();
						
					}
				});
				

			}
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			event.getRawX();
			yPosition = event.getRawY();
			onclick = false;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (!onclick && (event.getRawY() - yPosition) < 5
					&& (event.getRawY() - yPosition) > -5)
				onClick(v);
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (!onclick && (event.getRawY() - yPosition) > 0) {
				if (v.getId() == R.id.stripDown) {
					onClick(v);
					onclick = true;

				}
			} else if (!onclick && (event.getRawY() - yPosition) < 0) {
				if (v.getId() == R.id.stripUp) {
					onClick(v);
					onclick = true;
				}
			}
		}

		return true;
	}



	@Override
	public void onClick(View v) {
		TranslateAnimation translateAnimation;
		if (v == null) {
			return;
		}
		switch (v.getId()) {

		// will show redtickets with animation
		case R.id.stripDown:
			isStripExpanded = true;
			if (publicMatchesTicketAdapter == null) {
				publicMatchesTicketAdapter = new PublicMatchesTicketAdapter( vContestId, matchGallery);
				matchGallery.setAdapter(publicMatchesTicketAdapter);
			} 

			
			matchGallery.setVisibility(View.VISIBLE);
			stripUp.setVisibility( View.VISIBLE );

			matchHeaderBottom.setVisibility(View.INVISIBLE);
			
			translateAnimation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, -1.0f,
					Animation.RELATIVE_TO_SELF, 0.0f);

			translateAnimation.setDuration(500);
			translateAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					matchHeaderBottom.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					matchGallery.setSelection(publicMatchesTicketAdapter.getSelectedPosition(), true);

				}
			});

			matchHeaderBottom.startAnimation(	translateAnimation);


			break;

			// will collapse redtickets with animation
		case R.id.stripUp:
			isStripExpanded = false;
//			if( !expand ) {
//				commonStrip.setVisibility(View.VISIBLE);
//				matchHeaderBottom.setVisibility(View.GONE);
//				animateHeader();
//				break;
//			}
//			
			
			translateAnimation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, -1.0f);

			translateAnimation.setDuration(500);

			translateAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					// shadowDown.setVisibility(View.VISIBLE);
					
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					commonStrip.setVisibility( View.GONE );
					matchHeaderBottom.setVisibility(View.GONE);

				}
			});
			matchHeaderBottom.startAnimation(translateAnimation);

			break;

		case R.id.matchHeaderLayout:

			Hashtable<String, Object> result = DatabaseUtil.getInstance().getExtensionLinkUrl(vContestId);
			String vExtensionLinkUrl = (String) result.get("url");
			Boolean isHref = (Boolean) result.get("isHref");
			if(vExtensionLinkUrl != null && vExtensionLinkUrl.trim().length() > 0){

				Bundle b = new Bundle();
				b.putString("url",vExtensionLinkUrl );
				b.putBoolean("isHref",isHref );
				b.putString("fromFragment","MatchRoomFragment");

				PlayupLiveApplication.getFragmentManagerUtil().setFragment("WebViewFragment", b);


			}

		}

	}


	private boolean isFetchingCompetitionData = false;
	protected boolean isDownloading = false;
	/**
	 * Trying to fetch the competition data from the server 
	 */
	private void getCompetitionData () {


		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();



					Hashtable<String, Object> result = dbUtil.getAssociatedContestUrl( vContestId );
					if(result != null && result.containsKey("isHref")){
						String assoicatedContestUrl = (String) result.get("url");
						
						if(((Boolean) result.get("isHref")).booleanValue()){
							
							if ( assoicatedContestUrl != null && runnableList != null && Util.isInternetAvailable() ) {
								runnableList.put(Constants.GET_ASSOCIATED_CONTEST_DATA, new Util().getAssoiciatedContestsData(assoicatedContestUrl,runnableList,true) ) ;
							}
						
						}else{
							if ( assoicatedContestUrl != null && runnableList != null && Util.isInternetAvailable() ) {
								runnableList.put(Constants.GET_ASSOCIATED_CONTEST_DATA, new Util().getAssoiciatedContestsData(assoicatedContestUrl,runnableList,false) ) ;
							}
						}
						
					}


						
					

					// try to re launch the timer to refresh the data 
					refreshMatches ();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show ( e );
				}

			}
		}).start();

	}


	/**
	 * refreshing the matches
	 */
	public void refreshMatches ( ) {

		if ( refreshMatchesTimer  != null) {

			refreshMatchesTimer.cancel();
			refreshMatchesTimer = null;
		}

		refreshMatchesTimer = new Timer ();

		if ( refreshMatchesTask != null ) {
			refreshMatchesTask.cancel();
			refreshMatchesTask = null;
		}


		new Thread( new Runnable() {

			@Override
			public void run() {
				try {
					// creating the timer task to get the contest data from the server
					refreshMatchesTask = new TimerTask() {

						@Override
						public void run() {

							if ( runnableList!=null && !runnableList.containsKey ( Constants.GET_ASSOCIATED_CONTEST_DATA) ) {
								// checking if this is the current fragment or not and internet is available

								if ( Util.isInternetAvailable() ) {
									// fetching the contest data
									getAssociatedData();
								}
							}
						}
					};

					String vAssoiciatedContestUrl = null;
					vAssoiciatedContestUrl = DatabaseUtil.getInstance().getAssociatedContestUrlForRefresh(vContestId);
					int cacheTime = Integer.parseInt( DatabaseUtil.getInstance().getCacheTime(vAssoiciatedContestUrl) );
					try{
						
			
					
						if (cacheTime > 0 ) {
							refreshMatchesTimer.schedule(refreshMatchesTask , cacheTime * 1000, cacheTime * 1000);
						}else{
							refreshMatchesTask=null;
							refreshMatchesTimer = null;
						}
					}catch(Exception e){

						Logs.show(e);
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					Logs.show ( e );
				}


			}
		}).start();


	}

	/**
	 * setting live favourites cout on strips
	 * @param show
	 */
	private void setLiveCount() {
		Runnable r = new Runnable () {

			@Override
			public void run() {
				try {
					if( vContestId !=null  ) {


						final int live = PlayupLiveApplication.getDatabaseWrapper().getTotalCount("SELECT vContestId FROM   " +
								"associatedContestsData c  left join associatedContest ac on c.vContestId= ac.contestId "+
								" where ac.associatedContestId = ( select associatedContestId from contests where vContestId = '"+vContestId+"') AND ( c.dEndTime IS NULL AND c.dStartTime IS NOT NULL ) ");

						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable () {

								@Override
								public void run() {
									try {
										if ( !isVisible() ) {
											return;
										}
										liveCountDown.setVisibility(View.VISIBLE);
										liveCountUp.setVisibility(View.VISIBLE);
										stripLiveCount.setVisibility(View.VISIBLE);
										
										liveCountDown.setText(""+live);
										liveCountUp.setText(""+live);
										stripLiveCount.setText(""+live);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										Logs.show(e);
									}
								}

							});
						}

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show ( e );
				}


			}

		};
		Thread th = new Thread ( r );
		th.start();

	}


	
	/*
	 * this wil set the dummy views height so that the red tickets and strip can alingn with respect to match header
	 */

	public void setDummyViewsHeight( String sportType ) {
		
	    header = (RelativeLayout) matchHeaderLayout.findViewById(R.id.header);
		LinearLayout dummyHeaderView = (LinearLayout) content_layout.findViewById(R.id.dummyHeaderView);
		int height = 0;
		
		if( Constants.DENSITY.equalsIgnoreCase("low") ) {
			height = 90;	
		} else if( Constants.DENSITY.equalsIgnoreCase("medium") ) {
			height = 100;	
		} else if( Constants.DENSITY.equalsIgnoreCase("high") ) {
			height = 170;	
		}

		if( PlayUpActivity.isXhdpi ) {
			height = 210;		
		}
		
		if(sportType!=null &&  sportType.equalsIgnoreCase(Constants.LEADERBOARD) ) {
			Hashtable<String, List<String>> leaderBoardData = DatabaseUtil.getInstance().getLeaderBoardData(  vContestId );
			if ( leaderBoardData != null && leaderBoardData.get ( "vTeamId" ) != null && leaderBoardData.get ( "vTeamId" ) .size ( ) > 2 ) {
				if( Constants.DENSITY.equalsIgnoreCase("low") ) {
					height = 110;
				} else if( Constants.DENSITY.equalsIgnoreCase("medium") ) {
					height = 130;
				} else if( Constants.DENSITY.equalsIgnoreCase("high") ) {
					height = 180;
				}

				if( PlayUpActivity.isXhdpi ) {
					height = 235;
				}  		
			}	
			
		} 
		
		    header.getLayoutParams().height = height;
		    header.requestFocus();
		    
		    params = (LayoutParams) dummyHeaderView.getLayoutParams();
		    params.height = height;
		    dummyHeaderView.setLayoutParams(params);
		    
	
		    // this part is for expanding leaderboard data
		
		if( sportType!= null && sportType.equalsIgnoreCase(Constants.LEADERBOARD ) && leaderBoardList!=null ) {
			
			leaderBoardList.setOnItemClickListener( this );

		}
		
	
	}
	

	/**
	 * expanding and collapsing animation
	 */
	
 public Animation expandTheView( final boolean shoudlExpand,final int height,final int galleryHeight,  final View v ) {
		
		Animation a = new Animation() {
			  @Override
			protected void applyTransformation(float interpolatedTime,Transformation t) {
					
		            if (shoudlExpand) {
		            	v.getLayoutParams().height = (int) ( height  + ( float) galleryHeight * interpolatedTime );
		            } else {
		            	v.getLayoutParams().height = (int) ( height  -  ( float) galleryHeight * (  interpolatedTime));
		            }
		            
		            if( interpolatedTime == 1.0 ) {
		            	animation_completed = true;
		            	setViewsAfterAnimation( shoudlExpand );
		            } else
		            	animation_completed = false;
		            v.requestLayout();
			    }
			  
			  @Override
			    public boolean willChangeBounds() {
			    	   return true;
			    }
	    };
		
	  
	    a.setDuration(100);
	    a.scaleCurrentDuration(5);
	    a.setInterpolator( new AccelerateInterpolator() );
		return a;
		
	}
	
 
 /**
  * called after completing animation
  */
 public void setViewsAfterAnimation ( boolean isExpanded ) {
	
	 if( isExpanded ) {
		 isStripExpanded = true;
		 matchGallery.setVisibility(View.VISIBLE);
		 matchHeaderBottom.setVisibility( View.VISIBLE);
	 } else {
		
	 }
	 commonStrip.setVisibility( View.GONE );
 }

	
 
 @Override
public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		
	// animateHeader();
 }
	
 
 
 /**
  * called to animate header
  */
// public void animateHeader () {
//	 if( !animation_completed )
//		 return;
//	 
//	 if( expand ) {
//			expand = false;
//			animation_completed = false;
//			if( !isStripExpanded )
//				 commonStrip.setVisibility( View.VISIBLE );
//			header.startAnimation( expandTheView(true, header.getHeight(), matchGallery.getHeight(),  header));
//		} else {
//			expand = true;
//			animation_completed = false;						    
//			header.startAnimation( expandTheView(false, header.getHeight(),matchGallery.getHeight(),  header));
//		}
// }
	
}


