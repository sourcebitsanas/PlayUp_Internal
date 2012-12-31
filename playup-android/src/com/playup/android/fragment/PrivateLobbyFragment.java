package com.playup.android.fragment;

import java.util.ArrayList;


import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;



import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.MatchGalleryAdapter;
import com.playup.android.adapters.MatchHeaderGenerator;
import com.playup.android.adapters.PrivateLobbyAdapter;
import com.playup.android.adapters.ProviderAdapter;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;

import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;

import com.playup.android.util.Util;

public class PrivateLobbyFragment extends MainFragment implements
OnClickListener, OnTouchListener {

	private RelativeLayout content_layout;
	private RelativeLayout progress;
	private GridView gridView;
	private TextView liveCountDown;
	private TextView liveCountUp;


	private PrivateLobbyAdapter adapter;
	private ArrayList<String> follow ;

	private TextView lobbyMessage;
	private RelativeLayout stripDown, stripUp;
	private Gallery matchGallery;

	private boolean isDownloading;
	private float  yPosition = 0;
	private boolean onclick = false;

	private MatchGalleryAdapter matchGalleryAdapter;

	public static String vContestId = null;

	boolean favouriteLiveMatchs = false;

	private Dialog shareDialog;

	private Hashtable<String, TimerTask> refreshMatchesTask = new Hashtable<String, TimerTask>();
	private Hashtable<String, Timer> refreshMatchesTimer = new Hashtable<String, Timer>();
	
	private Hashtable<String, TimerTask> refreshCompetitionTask = new Hashtable<String, TimerTask>();
	private Hashtable<String, Timer>refreshCompetitionTimer = new Hashtable<String, Timer>();

	private Timer timer;
	private TimerTask timerTask;

	RelativeLayout matchHeaderLayout;
	LayoutInflater inflater ;
	
	private boolean isAgain = false;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;

	private String prevSportType = null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
			content_layout = (RelativeLayout) inflater.inflate(R.layout.private_lobby, null);
		
		if(!isAgain)
			setColors(getArguments());

		
		return content_layout;
	}

	@Override
	public void onAgainActivated(Bundle args) {
		// TODO Auto-generated method stub
		super.onAgainActivated(args);
		
		isAgain = true;
		setColors( args );
		
	}
	
	/**
	 * setting the various params passed on from the previous fragment
	 * @param bundle
	 */
	private void setColors(Bundle bundle){
		
		
		vMainColor = null;
		vMainTitleColor = null;
		
		vSecColor = null;
		vSecTitleColor = null;
		
		if (bundle != null &&bundle.containsKey("vMainColor")) {
			vMainColor = bundle.getString("vMainColor");
		}if (bundle != null && bundle.containsKey("vMainTitleColor")) {
			vMainTitleColor = bundle.getString("vMainTitleColor");
		}if (bundle != null && bundle.containsKey("vSecColor")) {
			vSecColor = bundle.getString("vSecColor");
		}if (bundle != null && bundle.containsKey("vSecTitleColor")) {
			vSecTitleColor = bundle.getString("vSecTitleColor");
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		matchHeaderGenerator= null;
		isDownloading = false;
		matchGalleryAdapter = null;
		adapter = null;
		refreshMatchesTask = new Hashtable<String, TimerTask>();
		refreshMatchesTimer = new Hashtable<String, Timer>();
		refreshCompetitionTask = new Hashtable<String, TimerTask>();
		refreshCompetitionTimer = new Hashtable<String, Timer>();
		
		favouriteLiveMatchs = false;
		vContestId = null;
		initialise();
		setListeners();
		setTopBar();

		follow = new ArrayList<String>();

		if (matchGalleryAdapter == null) {
			matchGalleryAdapter = new MatchGalleryAdapter( matchGallery );
			matchGallery.setAdapter(matchGalleryAdapter);
		} else {
			matchGalleryAdapter.setData();
		}
		matchGallery.setOnItemClickListener(matchGalleryItemListener);

		//matchGallery.setVisibility(View.VISIBLE);
		//content_layout.findViewById(R.id.matchHeaderBottom).setVisibility(View.VISIBLE);

		// onClick( stripDown );
		setValues();

		getPrivateLobbyData();
		getLiveMatchesCount();
		refreshFavouriteCompetitions();


	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		matchHeaderGenerator = null;
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		if( shareDialog != null && shareDialog.isShowing() ) {
			shareDialog.dismiss();
		}
	}

	/**
	 * setting the title and color of the topBar
	 */
	
	private void setTopBar() {

		Thread th = new Thread ( new Runnable () {

			@Override
			public void run() {


				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String vUserLobbyUrl = dbUtil.getUserLobbyUrl ();
					
					
					
					String vChildColor = dbUtil.getSectionMainColor("", vUserLobbyUrl);
					String vChildTitleColor = dbUtil.getSectionMainTitleColor("", vUserLobbyUrl);
					
					if(vChildColor != null && vChildColor.trim().length() > 0 )
						vMainColor = vChildColor;
					
					if(vChildTitleColor != null && vChildTitleColor.trim().length() > 0 )
						vMainTitleColor = vChildTitleColor;
						
					String vChildSecondaryColor = dbUtil.getSectionSecondaryColor ("", vUserLobbyUrl);
					String vChildSecondaryTitleColor = dbUtil.getSectionSecondaryTitleColor ( "", vUserLobbyUrl );
					
					 
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
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}
		});
		th.start();

	}

	/**
	 * intializing 
	 */
	private void initialise() {

		initialiseViews();

	}
	
	/**
	 * setting the listeners
	 */

	private void setListeners() {
		stripDown.setOnTouchListener(this);
		stripUp.setOnTouchListener(this);
	}
	
	
	/**
	 * initializing the views
	 */

	private void initialiseViews() {

		progress = (RelativeLayout) content_layout
		.findViewById(R.id.progressView);
		gridView = (GridView) content_layout.findViewById(R.id.roomBase);


		stripDown = (RelativeLayout) content_layout
		.findViewById(R.id.stripDown);
		stripDown.setVisibility(View.VISIBLE);
		stripUp = (RelativeLayout) content_layout.findViewById(R.id.stripUp);
		matchGallery = (Gallery) content_layout.findViewById(R.id.matchGallery);

		liveCountDown = (TextView)content_layout.findViewById(R.id.stripLiveCountDown);
		liveCountUp = (TextView)content_layout.findViewById(R.id.stripLiveCountUp);
		matchHeaderLayout = ( RelativeLayout) content_layout.findViewById(R.id.matchHeaderLayout);

		// setting properties of lobby message
		lobbyMessage = new TextView(PlayUpActivity.context);
		lobbyMessage.setGravity(Gravity.CENTER);
		lobbyMessage.setTextSize(20 );
		lobbyMessage.setTextColor( Color.parseColor("#696B6E"));
		lobbyMessage.setTypeface(Constants.BEBAS_NEUE);
		lobbyMessage.setText( R.string.privateLobbyRoomMessage);
		
		
		
		liveCountDown.setTypeface( Constants.BEBAS_NEUE );
		liveCountUp.setTypeface( Constants.BEBAS_NEUE );

	}

	
	/**
	 * Fetching the details of the LobbySubject(last selected contest by the user that is got from the
	 * response of private lobby api) to be shown in the header
	 */
	
	private void setDefaultMatchHeader() {

		Runnable r = new Runnable () {

			@Override
			public void run() {

				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					vContestId = dbUtil.getLobbySubjectId();
					
					
					
					Hashtable<String, Object> result = dbUtil.getLobbySubjectUrl();
					if( result!=null && result.containsKey("url") && result.get("url") != null && result.get("url").toString().trim().length() > 0 && result.containsKey("isHref") && result.get("isHref") != null && result.get("isHref").toString().trim().length() > 0){
						
					
					String vContestUrl =	(String) result.get("url");
					boolean isHref		=	(Boolean) result.get("isHref");;
					
					
					
					
					if(vContestUrl != null && vContestUrl.trim().length() > 0){

						isDownloading = true;
						
						if(runnableList != null && !runnableList.containsKey(vContestUrl)  && Util.isInternetAvailable() )

							runnableList.put(vContestUrl, new Util().getPrivateContestsData(vContestUrl,isHref, runnableList,false));

					}
					
					}else{
						//null from db 
					}
					if ( PlayUpActivity.handler != null ) {
						PlayUpActivity.handler.post( new Runnable () {

							@Override
							public void run() {
								if ( !isVisible() ) {
									return;
								}
								showMatchHeader();
							}

						});
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}


			}

		};
		Thread th = new Thread ( r );
		th.start();

	}
	
	/**
	 * calling the respective functions to display the various parts of the screen
	 */

	private void setValues() {

		try{

		setDefaultMatchHeader();

		Runnable r = new Runnable () {

			@Override
			public void run() {

				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();

					if (dbUtil.isUserAnnonymous()) {
						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable () {

								@Override
								public void run() {
									if ( !isVisible() ) {
										return;
									}
									progress.setVisibility(View.GONE);
									gridView.setVisibility(View.VISIBLE);
									if (adapter == null) {
										adapter = new PrivateLobbyAdapter( gridView, null, true,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor);
										gridView.setAdapter(adapter);
									} else {
										adapter.setData(null, true,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor );
									}
								}
							});
						}

						return;



					}
					/**
					 * if the gallery is animating then delay the display by 500ms
					 */
					final Hashtable<String, List<String>> data = dbUtil.getFriendLobbyConversation();	//Praveen

					if (data != null && data.containsKey("vConversationId") && data.get("vConversationId").size() > 0) {

						if ( isGalleryAnimating ) {
							if ( PlayUpActivity.handler != null ) {
								PlayUpActivity.handler.postDelayed( new Runnable () {

									@Override
									public void run() {
										progress.setVisibility(View.GONE);
										gridView.setVisibility(View.VISIBLE);
										if (adapter == null) {

											adapter = new PrivateLobbyAdapter(gridView, data, false,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor );
											gridView.setAdapter(adapter);
										} else {
											adapter.setData(data, false,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor  );
										}
									}
								}, 500 );
							}

						} else {
							if ( PlayUpActivity.handler != null ) {
								PlayUpActivity.handler.post( new Runnable () {

									@Override
									public void run() {

										if ( !isVisible() ) {
											return;
										}
										progress.setVisibility(View.GONE);
										gridView.setVisibility(View.VISIBLE);
										if (adapter == null) {

											adapter = new PrivateLobbyAdapter(gridView, data, false,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor );
											gridView.setAdapter(adapter);
										} else {
											adapter.setData(data, false,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor );
										}
									}
								});
							}

						}

					} else {
						
						/**
						 * if data is not available and in the process of downloading , then show a progress indicator
						 */

						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable () {

								@Override
								public void run() {

									if ( !isVisible() ) {
										return;
									}
									if (Constants.isDownloadingFriendLobbyConversation) {

										if( Util.isInternetAvailable() )
											progress.setVisibility(View.VISIBLE);
										else
											progress.setVisibility(View.GONE);

										gridView.setVisibility(View.GONE);
									} else {
										getPrivateLobbyData();
									}
								}
							});
						}

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}

			}

		};

		Thread th = new Thread ( r );
		th.start();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		//Logs.show ( e );
	}

	}

	/**
	 * on selecting the red tickets,updating the header
	 */

	private void setHeaders() {
		
		

		
		setLiveCount(true);
	



		Runnable r = new Runnable () {

			@Override
			public void run() {

				try {
					int fav = PlayupLiveApplication.getDatabaseWrapper().getTotalCount( "  SELECT vCompetitionId FROM competition WHERE isFavourite = '1'  ");

					if (fav > 0) {
						int live = PlayupLiveApplication .getDatabaseWrapper().getTotalCount("  SELECT vCompetitionId FROM competition WHERE isFavourite = '1'  AND iLiveNum > 0  ");

						if ( live > 0 ) {
							Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT vLobbySubjectId FROM user WHERE  isPrimaryUser = '1' AND vLobbySubjectId IS NOT NULL " );
							if ( c != null ) {
								if ( c.getCount() > 0 ) {

									c.moveToFirst();
									vContestId = c.getString(c.getColumnIndex("vLobbySubjectId"));
								}
								c.close();
							}

							if ( vContestId != null ) {					
								if ( PlayUpActivity.handler != null ) {
									PlayUpActivity.handler.post( new Runnable () {

										@Override
										public void run() {

											if ( !isVisible() ) {
												return;
											}
											showMatchHeader();
										}

									} );
								}

							}else{


								if ( PlayUpActivity.handler != null ) {
									PlayUpActivity.handler.post( new Runnable () {

										@Override
										public void run() {

											if ( !isVisible() ) {
												return;
											}
											
											showLobbyMessage ();

										}

									} );
								}

							}


						} else{


							if ( PlayUpActivity.handler != null ) {
								PlayUpActivity.handler.post( new Runnable () {

									@Override
									public void run() {

										if ( !isVisible() ) {
											return;
										}
										showLobbyMessage ();

									}

								} );
							}

						}
					} else{


						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable () {

								@Override
								public void run() {

									if ( !isVisible() ) {
										return;
									}
									showLobbyMessage ();
								}

							} );
						}

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}

			}

		};
		Thread th = new Thread ( r );
		th.start();



	}
	
	/**
	 * showing lobby message, it will remove all the views from the header view
	 * and adds the lobby message textview with alignment
	 */
	public void showLobbyMessage() {
		if( matchHeaderLayout!=null ) {
			matchHeaderLayout.removeAllViews();	
			
			LayoutParams params = new RelativeLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			matchHeaderLayout.setBackgroundResource(R.drawable.ticket_texture);
			matchHeaderLayout.setPadding( 0, 0, 0, 0 );
			params.addRule(RelativeLayout.CENTER_IN_PARENT);		
			matchHeaderLayout.addView( lobbyMessage ,params);
		}
	}
	
	/**
	 * making api call to fetch the private lobby data
	 */
	
	
	private void getPrivateLobbyData() {
		if (!Constants.isDownloadingFriendLobbyConversation) {

			new Util().getUserLobbyData();

		}

	}
	
	/**
	 * scheduling for the refresh of lobby data
	 */

	private void refreshLobbyConversationData() {

		if (timer != null) {
			timer.cancel();
			timer = null;
		}

		timer = new Timer();

		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}

		timerTask = new TimerTask() {

			@Override
			public void run() {

				try {
					getPrivateLobbyData();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}

			}
		};

		Runnable r = new Runnable () {

			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String vLobbyUrl = dbUtil.getUserLobbyUrl();

					final int cacheTime = Integer.parseInt(dbUtil.getCacheTime(vLobbyUrl));

					dbUtil = null;
					vLobbyUrl = null;


					if (cacheTime > 0) {
						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable () {

								@Override
								public void run() {

									try {
										timer = new Timer();
										timer.scheduleAtFixedRate(timerTask, cacheTime * 1000, cacheTime * 1000);
									} catch ( Exception e  ) {
									//	Logs.show ( e );
									}
								}

							});
						}

					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}

		};
		Thread th = new Thread (  r );
		th.start();



	}

	@Override
	public void onStop() {
		super.onStop();

		if (timer != null) {
			timer.cancel();
		}

		if (timerTask != null) {
			timerTask.cancel();
		}

		Iterator it;
		it = refreshMatchesTask.values().iterator();
		while (it.hasNext()) {
			((TimerTask) it.next()).cancel();
		}

		it = refreshMatchesTimer.values().iterator();
		while (it.hasNext()) {
			((Timer) it.next()).cancel();
		}
		
		refreshMatchesTask.clear();
		refreshMatchesTimer.clear();
		
		
		it = refreshMatchesTask.values().iterator();
		while (it.hasNext()) {
			((TimerTask) it.next()).cancel();
		}

		it = refreshCompetitionTimer.values().iterator();
		while (it.hasNext()) {
			((Timer) it.next()).cancel();
		}
	
		refreshCompetitionTask.clear();
		refreshCompetitionTimer.clear();



	}


	private boolean isGalleryAnimating = false;

	@Override
	public void onClick(View v) {


		LinearLayout li;
		TranslateAnimation translateAnimation;
		if (v == null) {
			return;
		}
		switch (v.getId()) {

		case R.id.stripDown:

			//animating the gallery (top to bottom)
			li = (LinearLayout) content_layout.findViewById(R.id.matchHeaderBottom);
			matchGallery.setVisibility(View.VISIBLE);
			content_layout.findViewById(R.id.matchHeaderBottom).setVisibility(View.INVISIBLE);

			translateAnimation = new TranslateAnimation(
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, -1.0f,
					Animation.RELATIVE_TO_SELF, 0.0f);

			isGalleryAnimating = true;
			translateAnimation.setDuration(500);
			translateAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					content_layout.findViewById(R.id.matchHeaderBottom).setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					// shadowDown.setVisibility(View.GONE);
					isGalleryAnimating = false;

				}
			});

			content_layout.findViewById(R.id.matchHeaderBottom).startAnimation(	translateAnimation);

			stripUp.setVisibility(View.VISIBLE);

			// stripDown.setVisibility(View.GONE );



			break;

		case R.id.stripUp:
			
			//animating the gallery (bottom to top)

			isGalleryAnimating = true;
			li = (LinearLayout) content_layout
			.findViewById(R.id.matchHeaderBottom);
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
					stripDown.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					content_layout.findViewById(R.id.matchHeaderBottom)
					.setVisibility(View.GONE);
					isGalleryAnimating = false;

				}
			});
			content_layout.findViewById(R.id.matchHeaderBottom).startAnimation(
					translateAnimation);

			break;
		}
	}

	@Override
	public void onUpdate ( final Message msg ) {

		String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
		try {
			if( msg != null && msg.obj !=null && msg.obj.toString().contains("share")) {
				if( msg.obj.toString().equalsIgnoreCase("shareScores") ) {
					if( DatabaseUtil.getInstance().isUserAnnonymous() ) {
						Bundle bundle = new Bundle();
						bundle.putBoolean("sharing", true );
						bundle.putString("fromFragment", topFragmentName);
						PlayupLiveApplication.getFragmentManagerUtil().setFragment( "ProviderFragment", bundle);

					} else {
						if( vContestId != null )
							showShareDialog( false );
					}

				} else if( msg.obj.toString().equalsIgnoreCase("share") ) {
					
				} else if( msg.obj.toString().contains("share_response") ) {

					String provider = msg.obj.toString();
					if( provider.split(":").length > 0 )
						provider = provider.split(":")[1];
					final String toastMessage = PlayUpActivity.context.getResources().getString(R.string.shareResponse)+" "+provider;

					if( msg.arg1 == 1 ) {				
						PlayupLiveApplication.showToast(toastMessage);
					} else {
						PlayupLiveApplication.showToast(R.string.shareFailure);
					}

				}
				return;
			}
			

			if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
				//Praveen: Following APIS are getting called from onResume
				
				setValues();
				getPrivateLobbyData();
				getLiveMatchesCount();
				refreshFavouriteCompetitions();
			
				
			}
			
			if (msg != null && msg.obj != null 	&& msg.obj.toString().equalsIgnoreCase("PlayUpLobby")) {
				
				
				/**
				 * first display/refreshing the private lobby conversation
				 */
				if ( PlayUpActivity.handler != null) {


					if ( isGalleryAnimating ) {
						PlayUpActivity.handler.postDelayed( new Runnable() {

							@Override
							public void run() {


								try {
									setTopBar();
									setValues();

									refreshLobbyConversationData();
								} catch (Exception e) {
									/// TODO Auto-generated catch block
									//Logs.show ( e );
								}
							}

						}, 500 );
					} else {
						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {

								try {
									if ( !isVisible() ) {
										return;
									}
									setTopBar();
									setValues();

									refreshLobbyConversationData();
								} catch (Exception e) {
									// TODO Auto-generated catch block
								//	Logs.show ( e );
								}
							}

						});
					}

				}
			}


			if (msg != null && msg.obj != null 	&& msg.obj.toString().equalsIgnoreCase("PrivateLobbyPutFollowMessage")) {
				
				/**
				 * when the user selects a red ticket to follow,sending the 'following information' to the server
				 */

				if(follow != null && follow.size() > 0){
					Hashtable<String, Object> result =  DatabaseUtil.getInstance().getPrivateLobbyUrl();
					if(result != null && result.contains("isHref")){
						
						new Util().putFollowMessage((String) result.get("url"),	follow.get(0),follow,null,
								((Boolean) result.get("isHref")).booleanValue());
					}

				}
			}




			if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("MatchHomeFragment_getScores")) {
				isDownloading = false;
				
				/**
				 * match header information
				 */

				if (PlayUpActivity.handler != null) {

					if ( isGalleryAnimating ) {
						PlayUpActivity.handler.postDelayed( new Runnable() {

							public void run() {
								try {
									if (!isVisible()) {
										return;
									}

									//							setHeaders();
									showMatchHeader();
								} catch (Exception e) {
									// TODO Auto-generated catch block
								//	Logs.show ( e );
								}
							}
						}, 500 );
					} else {
						PlayUpActivity.handler.post( new Runnable() {

							public void run() {
								try {
									if (!isVisible()) {
										return;
									}
									
									

									//							setHeaders();
									showMatchHeader();
								} catch (Exception e) {
									// TODO Auto-generated catch block
								//	Logs.show ( e );
								}
							}
						});
					}


				}

			}

			if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("LiveMatchesCount")) {
				/**
				 * getting the list of all the live matches under the favourited competition
				 */

				if (PlayUpActivity.handler != null) {

					if ( isGalleryAnimating ) {
						PlayUpActivity.handler.postDelayed(new Runnable() {

							@Override
							public void run() {

								try {
									if (!isVisible()) {
										return;
									}
									if(msg != null && msg.getData() != null && msg.getData().getString("vCompetionUrl") != null)								
										getLiveMatches(msg.getData().getString("vCompetionUrl"));


									//							setHeaders();
								} catch (Exception e) {
									// TODO Auto-generated catch block
								//	Logs.show ( e );
								}
							}

						}, 500 );
					} else {
						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {

								try {
									if (!isVisible()) {
										return;
									}
									if(msg != null && msg.getData() != null && msg.getData().getString("vCompetionUrl") != null)								
										getLiveMatches(msg.getData().getString("vCompetionUrl"));

									//							setHeaders();
								} catch (Exception e) {
									// TODO Auto-generated catch block
								///	Logs.show ( e );
								}
							}

						});
					}

				}
			}

			if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("LiveMatches")) {

				/**
				 * setting the contests in the red tickets and header
				 */
				if (msg.getData() != null && msg.getData().containsKey("vContestLiveUrl")) {
					
				//	Log.e("123","insde onUpdate >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+(msg.getData().getString("vContestLiveUrl")));
					
					refreshMatches(msg.getData().getString("vContestLiveUrl"),msg.getData().getBoolean("isHref"));
				}
				if (PlayUpActivity.handler != null) {

					if ( isGalleryAnimating ) {
						PlayUpActivity.handler.postDelayed ( new Runnable() {

							@Override
							public void run() {

								try {
									if (!isVisible()) {
										return;
									}
									
										showMatchHeader();
									
									try {
										if (matchGallery.getVisibility() == View.VISIBLE) {
											matchGalleryAdapter.setValues();
										}else{
											if (matchGalleryAdapter == null) {
												matchGalleryAdapter = new MatchGalleryAdapter( matchGallery );
												matchGallery.setAdapter(matchGalleryAdapter);
											} else {
												matchGalleryAdapter.setData();
											}

										}
									} catch ( Exception e ) {
									//	Logs.show ( e );
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
								//	Logs.show ( e );
								}



							}

						}, 500 );
					} else {
						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {

								try {
									if (!isVisible()) {
										return;
									}
									

									
										showMatchHeader();
									


									try {
										if (matchGallery.getVisibility() == View.VISIBLE) {
											matchGalleryAdapter.setValues();
										}else{
											if (matchGalleryAdapter == null) {
												matchGalleryAdapter = new MatchGalleryAdapter( matchGallery );
												matchGallery.setAdapter(matchGalleryAdapter);
											} else {
												matchGalleryAdapter.setData();
											}

										}
									} catch ( Exception e ) {
										//Logs.show ( e );
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
								//	Logs.show ( e );
								}



							}

						});
					}

				}



			}
			
			if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("RefreshMatches")) {
				//Log.e("123","insde onUpdate RefreshMatches >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+(msg.getData().getString("vContestLiveUrl")));
				if (msg.getData() != null	&& msg.getData().containsKey("vContestLiveUrl")) {
					refreshMatches(msg.getData().getString("vContestLiveUrl"),msg.getData().getBoolean("isHref"));

				}

			}
			
			if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("RefreshRedTicket")) {
				
				if(isGalleryAnimating){
					if(PlayUpActivity.handler != null){
						PlayUpActivity.handler.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								try {
									if(!isVisible())
										return;
									
									if(matchGalleryAdapter != null)
										matchGalleryAdapter.setData();
									
									setLiveCount(true);
								} catch (Exception e) {
									// TODO Auto-generated catch block
								//	Logs.show(e);
								}
								
							}
						},500);
					}
					
					
				}else{
					
					if(PlayUpActivity.handler != null){
						PlayUpActivity.handler.post(new Runnable() {
							
							@Override
							public void run() {
								try {
									if(!isVisible())
										return;
									
									if(matchGalleryAdapter != null)
										matchGalleryAdapter.setData();
									
									setLiveCount(true);
								} catch (Exception e) {
									//Logs.show(e);
								}
								
							}
						});
					}
				}
				

			}
			
			if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("refreshFavouriteTiles")) {

				if(PlayUpActivity.handler != null){
					PlayUpActivity.handler.post(new Runnable() {
						
						@Override
						public void run() {
							try {
								if ( adapter != null  )
									adapter.refreshFavouriteTiles();
							} catch (Exception e) {
								// TODO Auto-generated catch block
							//	Logs.show(e);
							}	
							}
						});
				}
							
			}
			
			
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
		//	Logs.show(e);
		}

	}
	
	/**
	 * making api call to get the updated number of live contests under favourited competition
	 */

	private void getLiveMatchesCount() {

		Runnable r = new Runnable () {

			@Override
			public void run() {

				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
				///	Log.e("123","inside getLiveMatchesCount >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  ");
					Hashtable<String, List<String>> competitionUrl = dbUtil.getCompetitionLiveUrl();
					
				//	Log.e("123","inside getLiveMatchesCount >>>>>>>>>>>>>competitionUrl>>>>>>>>>>>>>>>>>>>>>>>  "+competitionUrl);
					
					if (competitionUrl != null && competitionUrl.get("vCompetitionLiveUrl").size() > 0 ) {

						favouriteLiveMatchs = true;

						if (Util.isInternetAvailable()) {

							for (int i = 0; i < competitionUrl.get("vCompetitionLiveUrl").size(); i++) {
								
								
								
								if(competitionUrl.get("vCompetitionLiveHref").get(i) != null && 
										competitionUrl.get("vCompetitionLiveHref").get(i).trim().length() > 0){
									
									if (runnableList != null && !runnableList.containsKey(competitionUrl.get( "vCompetitionLiveHref").get(i))  && Util.isInternetAvailable() ) {
										runnableList.put(competitionUrl.get("vCompetitionLiveHref").get(i), 
												new Util().getLiveMatchesCount(competitionUrl.get("vCompetitionLiveHref").get(i),runnableList,true));
									}
									
								}else if(competitionUrl.get("vCompetitionLiveUrl").get(i) != null && 
										competitionUrl.get("vCompetitionLiveUrl").get(i).trim().length() > 0){
									
									if (runnableList != null && !runnableList.containsKey(competitionUrl.get( "vCompetitionLiveUrl").get(i))  && Util.isInternetAvailable() ) {
										runnableList.put(competitionUrl.get("vCompetitionLiveUrl").get(i), 
												new Util().getLiveMatchesCount(competitionUrl.get("vCompetitionLiveUrl").get(i),runnableList,false));
									}
								}

								
							}
						}
					} else {

						favouriteLiveMatchs = false;

						Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery("SELECT dScheduledStartTime," +
								"dStartTime,dEndTime " +
								"FROM contests " +
								"WHERE vContestId = \""+vContestId+"\" ");
						try {

							if (c != null && c.getCount() > 0) {

								c.moveToFirst();

								String output = null;
								try {
									output = new DateUtil().Match_TimeRoomFragment(c.getString(c.getColumnIndex("dStartTime")), c.getString(c.getColumnIndex("dEndTime")), c.getString(c
											.getColumnIndex("dScheduledStartTime")));

									if(output != null && output.trim().length() != 0){
									}else{
										Hashtable<String, Object> result = DatabaseUtil.getInstance().
										getContestUrlFromContestId( vContestId );


										if(result != null && result.containsKey("isHref")){
											String vContestUrl = (String) result.get("url");
											
												
												if(runnableList != null && !runnableList.containsKey(vContestUrl)  && Util.isInternetAvailable() )									
													runnableList.put(vContestUrl,new Util().getLiveContests( vContestUrl, runnableList,((Boolean) result.get("isHref")).booleanValue() ));
												
												
											
											
											
										}
										
										

									}

								}catch(Exception e){
								//	Logs.show(e);


								}
							}
							if ( c != null) {
								c.close();
							}


						} catch ( Exception e ) {
							//Logs.show(e);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}

			}

		};
		Thread th = new Thread ( r );
		th.start();


	}
	
	/**
	 * making api call to get the updated live contest info under favourited competitions
	 * @param vCompetitionUrl
	 */

	private void getLiveMatches(final String vCompetitionUrl) {


		Runnable r = new Runnable () {

			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, List<String>> contestsLiveUrl = dbUtil.getPrivateSelectedContestUrl(vCompetitionUrl);



					if (contestsLiveUrl != null && contestsLiveUrl.get("vContestUrl").size() > 0) {		
						if(contestsLiveUrl.get("vContestHref").get(0) != null && 
								contestsLiveUrl.get("vContestHref").get(0).trim().length() > 0 ){
							
							if (runnableList != null && !runnableList.containsKey(contestsLiveUrl.get("vContestHref").get(0))  && 
									Util.isInternetAvailable() ) {
								runnableList.put(contestsLiveUrl.get("vContestHref").get(0), 
										new Util().getLiveContests(contestsLiveUrl.get("vContestHref").get(0), runnableList,true));

							}
						}else if(contestsLiveUrl.get("vContestUrl").get(0) != null && 
								contestsLiveUrl.get("vContestUrl").get(0).trim().length() > 0 ){
							
							if (runnableList != null && !runnableList.containsKey(contestsLiveUrl.get("vContestUrl").get(0))  && Util.isInternetAvailable() ) {
								runnableList.put(contestsLiveUrl.get("vContestUrl").get(0), new Util().getLiveContests(	contestsLiveUrl.get("vContestUrl").get(0),runnableList,false));

							}
							
						}

						







						if ( ( vContestId != null && vContestId.trim().length() > 0 ) && ! ( contestsLiveUrl.containsValue ( vContestId ) ) ) {

							Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery ( "SELECT dScheduledStartTime, " +
									"dStartTime, dEndTime " +
									"FROM contests " +
									"WHERE vContestId = \"" + vContestId + "\" ");
							try {
								if ( c != null && c.getCount() > 0 ) {

									c.moveToFirst();

									String output = null;
									try {
										output = new DateUtil().Match_TimeRoomFragment(c.getString(c.getColumnIndex("dStartTime")), c.getString(c.getColumnIndex("dEndTime")), c.getString(c
												.getColumnIndex("dScheduledStartTime")));

										if ( output != null && output.trim().length() != 0 ) { 

										} else {

											Hashtable<String, Object> result = DatabaseUtil.getInstance().getContestUrlFromContestId( vContestId );

											if(result != null && result.containsKey("isHref")){
												
												String vContestUrl = (String) result.get("url");
												
												
													
													if(runnableList != null && !runnableList.containsKey(vContestUrl)  && Util.isInternetAvailable() )
														runnableList.put(vContestUrl,new Util().getLiveContests( vContestUrl, runnableList ,((Boolean) result.get("isHref")).booleanValue()));
												
												
											}
											

										}

									} catch ( Exception e ) {
									//	Logs.show  ( e );
									}
								}
								if ( c != null ) {
									c.close();
								}
							} catch ( Exception e ) {
								//Logs.show  ( e );
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();




	}
	
	/**
	 * refreshin gthe red tickets
	 * @param vContestLiveUrl
	 */


	private void refreshMatches (final String vContestLiveUrl,final boolean isHref) {

		Runnable r =  new Runnable () {

			@Override
			public void run() {

				try {
				//	Log.e("123","insde  RefreshMatches >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+vContestLiveUrl);
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					int cacheTime = Integer.parseInt(dbUtil.getCacheTime(vContestLiveUrl));
					
					//Log.e("123","insde  RefreshMatches >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+vContestLiveUrl+">>>>>>>>>>>>> cacheTime >>>>>>"+cacheTime);

					try {
						if (refreshMatchesTask != null) { 
							if (refreshMatchesTask.containsKey(vContestLiveUrl)) {
								refreshMatchesTask.get(vContestLiveUrl).cancel();
							}

							refreshMatchesTask.put(vContestLiveUrl, new TimerTask() {

								@Override
								public void run() {
									if (runnableList != null && !runnableList.containsKey(vContestLiveUrl)  && Util.isInternetAvailable() ) {
										runnableList.put(vContestLiveUrl, new Util().getLiveContests(vContestLiveUrl, runnableList,isHref));
									}
								}
							});

							if (refreshMatchesTimer != null) {
								if (refreshMatchesTimer.containsKey(vContestLiveUrl)) {
									refreshMatchesTimer.get(vContestLiveUrl).cancel();
								}
							}
							refreshMatchesTimer.put(vContestLiveUrl, new Timer());
							if (cacheTime > 0) {
								try {
									refreshMatchesTimer.get(vContestLiveUrl).schedule(refreshMatchesTask.get(vContestLiveUrl), 	(cacheTime * 1000), (cacheTime * 1000) );
								} catch ( Exception e ) {
								//	Logs.show  ( e ) ;
								}
							}
						} 
					} catch ( Exception e ) {
						//Logs.show (  e );
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}

			}

		};
		Thread th = new Thread ( r) ; 
		th.start();

	}

	/***
	 * clicking on the red tickets,sending 'following information'
	 */

	private OnItemClickListener matchGalleryItemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {

			if (view != null && view.getTag(R.id.about_txtview) != null) {
				vContestId = (String) view.getTag(R.id.about_txtview);
				final String vContestUrl = (String) view.getTag(R.id.aboutScrollView);

				Runnable r = new Runnable () {

					@Override
					public void run() {

						try {
							DatabaseUtil dbUtil = DatabaseUtil.getInstance();
							dbUtil.setPrivateLobbySelectContest(vContestId);

							Hashtable<String, Object> result =  dbUtil.getPrivateLobbyUrl();
							String vLobbyUrl = (String) result.get("url");

							if ( PlayUpActivity.handler !=  null ) {
								PlayUpActivity.handler.post( new Runnable ()  {

									@Override
									public void run() {
										try {
											if ( !isVisible() ) {
												return;
											}
											setHeaders();
										} catch (Exception e) {
											// TODO Auto-generated catch block
										//	Logs.show(e);
										}
									}

								});
							}

							// post the following message
							try {
								JSONObject jObj = new JSONObject();

								jObj.put( ":type", "application/vnd.playup.my.lobby+json" );
								jObj.put( ":self",  vLobbyUrl );
								jObj.put( ":uid",  dbUtil.getUserLobbyId() );

								JSONObject subject_jObj = new JSONObject();
								subject_jObj.put(":self", vContestUrl);
								subject_jObj.put(":type", dbUtil.getHeader(vContestUrl));
								subject_jObj.put(":uid", vContestId);


								jObj.put("subject", subject_jObj);


								if(follow != null && follow.size() == 0){

									follow.add(jObj.toString());
									
									if(result != null && result.contains("isHref")){
										
										new Util().putFollowMessage(vLobbyUrl,	jObj.toString(),follow,null,
												((Boolean) result.get("isHref")).booleanValue());
									}
									
									
								}

								else{


									follow.add(jObj.toString());


								}


								//							
							} catch (JSONException e) {
							//	Logs.show(e);
							}
						} catch (Exception e) {
							//Logs.show ( e );
						}


					}

				};
				Thread th = new Thread ( r );
				th.start();

			}
		}
	};

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
		
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


	MatchHeaderGenerator matchHeaderGenerator = null;
	/**
	 * showing match header
	 * 
	 * @param vContestId
	 */
	private void showMatchHeader() {
		

		if(vContestId == null) {
			
			setHeaders();
			return;
		} else {
			setLiveCount(true);

		}
		
		
		

		Runnable r = new Runnable () {



			@Override
			public void run() {


				try {


			


					final Hashtable<String, List<String>> data = PlayupLiveApplication.getDatabaseWrapper().select("SELECT vContestId,dScheduledStartTime,"
							+ "dStartTime,dEndTime,iTotal1,iTotal2,vHomeTeamId,vAwayTeamId,vSummary,vSportsName," +
									"vCompetitionName,iWickets1, iWickets2,vOvers,vRunRate,vLastBall," +
									"vPlayerFirstName1,vPlayerFirstName2,vPlayerLastName1,vPlayerLastName2"
							+ ",vRole2,vRole1,vStats1,vStats2,vStrikerFirstName,vStrikerLastName,vStrikerStats," +
									"vNonStrikerFirstName,vNonStrikerLastName,vNonStrikerStats,vAnnotation," +
									"vAnnotation2,vSummary1,vSummary2,iGoals1,iBehinds1,iSuperGoals1," +
									"iGoals2,iBehinds2,iSuperGoals2,iInnnings,vInnningsHalf,iBalls1," +
									"iBalls2,iOut1,iOut2,iStrikes1,iStrikes2,vBase1,vBase2,iHasLiveUpdates," +
									"vLastEventName, vShortMessage, vLongMessage,vSportType, iActive1,iActive2, " +
							"( SELECT vDisplayName FROM teams WHERE vTeamId = vHomeTeamId  ) AS vHomeDisplayName, " +
							"( SELECT vDisplayName FROM teams WHERE vTeamId = vAwayTeamId  ) AS vAwayDisplayName, " +
							"(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vHomeTeamId ) AS vHomeCalendarUrl," +
							"(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vAwayTeamId ) AS vAwayCalendarUrl  "+
							"FROM contests WHERE vContestId = '"
							+ vContestId + "'");
					

//					if(!isDownloading && (data != null && data.get( "vHomeTeamId" ) != null  && data.get( "vHomeTeamId" ).size() == 0) )
					if(!isDownloading && (data != null && ((data.get( "vContestId" ) != null  && 
							data.get( "vContestId" ).size() == 0) ||
							data.get( "vContestId" ) == null ))){

						if(PlayUpActivity.handler != null){
							PlayUpActivity.handler.post(new Runnable() {

								@Override
								public void run() {

									try {
										if ( !isVisible() ) {
											return;
										}

										
										
										
										showLobbyMessage ();
									} catch (Exception e) {
										// TODO Auto-generated catch block
									//	Logs.show ( e );
									}

								}
							});

						}


						return;

					}



					if(PlayUpActivity.handler != null ){

						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {
								try {

									
									String sportType = null;
									if( data != null && data.get("vSportType") != null )
										sportType = data.get("vSportType").get(0);
									if( sportType == null )
										 return;
									// adding layouts to the headr view based on sport type

									if( ( sportType!=null && prevSportType!=null && !sportType.equalsIgnoreCase(prevSportType) ) ||

											matchHeaderGenerator == null ) {
										prevSportType = sportType;
										View headerLayout;
										if( inflater == null )
											inflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
										
										if( sportType != null && sportType.equalsIgnoreCase(Constants.LEADERBOARD)  ) {
											headerLayout = (View) inflater.inflate(R.layout.match_header_leaderboard, null);
										} else if( sportType!= null && sportType.equalsIgnoreCase(Constants.SET_BASED_DATA)  )  {
											headerLayout = (View) inflater.inflate(R.layout.match_header_setbased, null);
										} else {
											headerLayout = (View) inflater.inflate(R.layout.match_header_normal, null);
										}
										
										if(sportType != null){
										


										matchHeaderLayout.setBackgroundResource(0);
										matchHeaderLayout.removeAllViews();
										matchHeaderLayout.addView( headerLayout );
										matchHeaderGenerator = new MatchHeaderGenerator(data, matchHeaderLayout, false , false );
										
										headerLayout = null;
										}
									} else if (matchHeaderGenerator != null ){
										matchHeaderGenerator.setData(data);
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
								//	Logs.show ( e );
								}


							}
						});
					}





				} catch (Exception e) {

				//	Logs.show ( e );
				}

			}

		};

		Thread th = new Thread ( r );
		th.start();



	}

	/**
	 * setting live favourites count on strips
	 * @param show
	 */
	private void setLiveCount( boolean show) {
		if( show ) {

			Runnable r = new Runnable () {

				@Override
				public void run() {

					try {
						final int live = PlayupLiveApplication.getDatabaseWrapper().getTotalCount("SELECT vContestId FROM contests c  LEFT JOIN competition cp ON cp.vCompetitionId = c.vCompetitionId WHERE cp.isFavourite = '1'  AND cp.iLiveNum > 0 AND ( c.dEndTime IS NULL AND c.dStartTime IS NOT NULL )");

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
										liveCountDown.setText(""+live);
										liveCountUp.setText(""+live);
									} catch (Exception e) {
										// TODO Auto-generated catch block
									//	Logs.show(e);
									}
								}

							});
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//Logs.show ( e );
					}

				}

			};
			Thread th = new Thread ( r );
			th.start();


		} else {
			liveCountDown.setVisibility(View.GONE);
			liveCountUp.setVisibility(View.GONE);
		}
	}

	/**
	 * A dialog to show providers to share scores
	 */
	private void showShareDialog( boolean refresh ) {

		if ( PlayUpActivity.context != null && Constants.isCurrent ) {
			TextView dialogTitle;
			ListView providerList;
			RelativeLayout dialogRoot;
			final DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			final Hashtable<String, Object> result = dbUtil.getContestShareUrl( vContestId );
			String vShareUrl = "";
			boolean isHref = false;
			if(result != null && result.containsKey("isHref") && result.containsKey("url")){
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

	}
	
	
	/**
	 * scheduling to refresh favourite competitions
	 */
	
	private void refreshFavouriteCompetitions () {
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				
				Hashtable< String,  List< String >>  favData = DatabaseUtil.getInstance().getFavouriteSports();
				
				if( favData != null && favData.get("vCompetitionUrl")!=null ) {
					
					for ( int i=0; i< favData.get("vCompetitionUrl").size() ; i++ ) {
						
						if(favData.get("vCompetitionHref").get(i) != null &&
								favData.get("vCompetitionHref").get(i).trim().length() > 0){
							
							refreshIndividualFavouriteCompetitions(favData.get("vCompetitionHref").get(i),true  );
							
						}else{
							
							refreshIndividualFavouriteCompetitions(favData.get("vCompetitionUrl").get(i),false  );
							
						}
						
						
						
					}
					
				}
				
				
			}
		}  ).start();
		
		
		
	}
	
	
	/**
	 * scheduling to refresh individual favourite competitions
	 */
	
	private void refreshIndividualFavouriteCompetitions ( final String vCompetitionUrl ,final boolean isHref) {
		
		new Thread( new Runnable() {
			
			@Override
			public void run() {
			
				try {
					if( vCompetitionUrl == null || vCompetitionUrl.trim().length() ==0 )
						return;
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					int cacheTime = Integer.parseInt ( dbUtil.getCacheTime ( vCompetitionUrl ) );
					if ( refreshCompetitionTask != null ) {
						if ( refreshCompetitionTask.containsKey ( vCompetitionUrl ) ) {
							refreshCompetitionTask.get( vCompetitionUrl ).cancel();
						}
					}
					refreshCompetitionTask.put ( vCompetitionUrl, new TimerTask() {

						@Override
						public void run() {
							try {
								if ( runnableList!=null && !runnableList.containsKey ( vCompetitionUrl ) && Util.isInternetAvailable() ) {
									runnableList.put ( vCompetitionUrl, new Util().getLeagueData ( vCompetitionUrl, runnableList,isHref ) );
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								//Logs.show ( e );
							}
						}
					});

					if ( refreshCompetitionTimer != null ) {
						if ( refreshCompetitionTimer.containsKey ( vCompetitionUrl ) ) {
							refreshCompetitionTimer.get(vCompetitionUrl).cancel();
						}
					}
					refreshCompetitionTimer.put(vCompetitionUrl, new Timer() );

					if(cacheTime <= 0 )
						cacheTime = 60;
					
					refreshCompetitionTimer.get(vCompetitionUrl).schedule( refreshCompetitionTask.get(vCompetitionUrl), (cacheTime * 1000), (cacheTime * 1000));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}catch(Exception e){
				//	Logs.show(e);
				}

				
				
				
				
			}
		}).start();
		
		
	}



}
