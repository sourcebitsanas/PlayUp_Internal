package com.playup.android.fragment;

import java.util.Hashtable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.service.MediaPlayerService;
import com.playup.android.util.Constants;

import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;
import com.playup.android.util.PreferenceManagerUtil;
import com.playup.android.util.Util;

public class MenuFragment extends MainFragment implements OnClickListener, OnTouchListener {

	
	private TextView userNameTextView;
	private TextView nameTextView;
	private ImageView avatarImageView;
	private RelativeLayout notificationLi;
	private RelativeLayout directMessagesLi;
	private RelativeLayout homeViewLi;

	private LinearLayout  whatsHotItemsView;

	private LinearLayout allsportsLi;
	private LinearLayout friendsLi;
	private LinearLayout aboutLi;
	private LinearLayout recentActivityLi;
	private LinearLayout favouriteLi;
	private LinearLayout recentActivityTextLinearLayout;
	private LinearLayout featureTextLinearLayout;
	private LinearLayout favouriteTextLinearLayout;
	private LinearLayout username_layout;
	private ImageDownloader imageDownloader = new ImageDownloader(); 
	private Hashtable<String, List<String>> recentData;
	

	
	private Timer myProfileTimer;
	private TimerTask myProfileTimerTask;
	

	
	private ScrollView content_layout;
	private TextView txtView;
	private TextView directMessagesTxtView;
	TextView txt;
	private TextView myRecentActivityText;
	private TextView favourite_text,feature_text;
	public int recentSize=-1;
	public int whatsHotSize=-1;
	public int favouriteSize=-1;
	
	private boolean isBaseSectionTargetHrefUrl	= false;

	RelativeLayout tempLi;

	View directmMsgDivider ;

	int hotItemId = 100;
	private ImageView pauseButton;
	private TextView radioName;
	private TextView radiodesc;
	private TextView bufferingText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		tempLi = ( RelativeLayout ) inflater.inflate(R.layout.menu, null);
		return tempLi;
	}

	@Override
	public void onResume () {
		super.onResume();

		if ( imageDownloader == null ) {
			imageDownloader = new ImageDownloader();
		}
		
		isSetWhatsHotItems = false;

		content_layout = (ScrollView) tempLi.findViewById( R.id.menu_scroll );

		
		
		initialize(content_layout);
		InputMethodManager imm = (InputMethodManager)PlayUpActivity.context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(PlayUpActivity.mBinder, 0);
		imm = null;

		//Praveen : changed
		new Util().getProfileData();
		



	}

	/**
	 * setting all the content in the views
	 */
	private void initialize(final ScrollView content_layout) {	//verified
		
		username_layout = (LinearLayout) content_layout .findViewById(R.id.username_layout);
		
		
		txtView = (TextView) content_layout	.findViewById(R.id.topbar_notification_text);
		directMessagesTxtView = (TextView) content_layout	.findViewById(R.id.topbar_directMessages_text);
		myRecentActivityText = (TextView) content_layout.findViewById(R.id.my_recent_activity_text);
		directmMsgDivider = ( View ) content_layout.findViewById( R.id.directmMsgDivider );

		pauseButton = (ImageView)content_layout.findViewById(R.id.pause);
		radioName = (TextView) content_layout.findViewById(R.id.radioName);
		radiodesc = (TextView) content_layout.findViewById(R.id.radioDesc);
		
		radioName.setTypeface(Constants.OPEN_SANS_REGULAR);
		radiodesc.setTypeface(Constants.OPEN_SANS_LIGHT);
		bufferingText	=	(TextView) content_layout.findViewById(R.id.bufferingText);
		bufferingText.setTypeface(Constants.OPEN_SANS_LIGHT);
		
		content_layout.findViewById(R.id.bufferingLayout).setVisibility(View.GONE);
		content_layout.findViewById(R.id.timeMain).setVisibility(View.VISIBLE);
		
		
		// initialize views
		initializeViews(content_layout);
		
		
		setTopBar();

		// set listeners
		setListeners();
		//set direct messages 
		setDirectMessageView();
		// set values
		setValues();
	}
	
	
	/**
	 * setting the color and title in the top bar
	 */
	private void setTopBar() {
		
		Bundle b = new Bundle();
		b.putString("vMainColor",null );
		b.putString("vMainTitleColor",null );
		Message msg = new Message ();
		msg.setData(b);
		
		
		PlayupLiveApplication.callUpdateTopBarFragments(msg);
		




	}
	
	
	/**
	 * scheduling the refresh of the primary user profile
	 */
	
	private void refreshMyProfile() {	//verified
		if(myProfileTimer == null)
			myProfileTimer = new Timer();
		
		
		if(myProfileTimerTask == null){
			
			myProfileTimerTask = new TimerTask() {
				
				@Override
				public void run() {
					new Util().getProfileData();
				}
			};
			
			new Thread(new  Runnable() {
				
				@Override
				public void run() {
					try {
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						Hashtable<String, Object> result = dbUtil.getProfileURLFromRootResource();
						if(result!=null&& result.containsKey("url") && result.get("url")!=null &&  result.get("url").toString().trim().length()>0){
							String myProfileUrl = (String) result.get("url");
							//Boolean isHref = (Boolean) result.get("isHref");
							
							int cacheTime = Integer.parseInt(dbUtil.getCacheTime(myProfileUrl));
							
							
							
							if(cacheTime > 0){
								if(myProfileTimer!=null){
									myProfileTimer.schedule(myProfileTimerTask, cacheTime * 1000,cacheTime * 1000);
								}else{
									myProfileTimer = new Timer();
									myProfileTimer.schedule(myProfileTimerTask, cacheTime * 1000,cacheTime * 1000);
								}
								
							}else{
								
								myProfileTimer = null;
								myProfileTimerTask = null;
							}
						}
						else{
							
						}
						
					} catch (Exception e) {
//						Logs.show(e);
					}
					
				}
			}).start();
			
			
			
		}
		
	}
	
	
	@Override
	public void onStop() {

		super.onStop();
		
		if(myProfileTimer != null)
			myProfileTimer.cancel();
		
		
		if(myProfileTimerTask != null)			
			myProfileTimerTask.cancel();
		
		
		myProfileTimer = null;
		myProfileTimerTask = null;
	}

	
	/**
	 * initializing the views
	 * @param content_layout
	 */

	private void initializeViews(final ScrollView content_layout) {	//verified


		userNameTextView = (TextView) content_layout	.findViewById(R.id.text_user);
		nameTextView = (TextView) content_layout	.findViewById(R.id.text_user_subtitle);
		feature_text = (TextView ) content_layout.findViewById(R.id.feature_text);
		favourite_text = (TextView ) content_layout.findViewById(R.id.favourite_text);

		userNameTextView.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		nameTextView.setTypeface(Constants.OPEN_SANS_REGULAR);
		myRecentActivityText.setTypeface(Constants.OPEN_SANS_REGULAR);
		feature_text.setTypeface(Constants.OPEN_SANS_REGULAR);
		favourite_text.setTypeface(Constants.OPEN_SANS_REGULAR);

		avatarImageView = (ImageView) content_layout		.findViewById(R.id.img_avtar);
		recentActivityTextLinearLayout = (LinearLayout) content_layout		.findViewById(R.id.recentActivityText);
		recentActivityLi = (LinearLayout) content_layout		.findViewById(R.id.menu_recent_activity_li);
		notificationLi = (RelativeLayout) content_layout		.findViewById(R.id.menu_notifications);
		directMessagesLi = (RelativeLayout) content_layout		.findViewById(R.id.menu_directMessages);
		allsportsLi = (LinearLayout) content_layout		.findViewById(R.id.menu_allSportsResults);
		friendsLi = (LinearLayout) content_layout		.findViewById(R.id.menu_friends);
		aboutLi = (LinearLayout) content_layout.findViewById(R.id.menu_about);
		homeViewLi = (RelativeLayout) content_layout.findViewById(R.id.menu_home);

		whatsHotItemsView =(LinearLayout) content_layout.findViewById(R.id.menu_whatshot_items);

		featureTextLinearLayout = (LinearLayout) content_layout.findViewById(R.id.featureTextHeader);
		favouriteTextLinearLayout = (LinearLayout) content_layout.findViewById(R.id.favouriteTextHeader);
		favouriteLi = (LinearLayout) content_layout.findViewById(R.id.favourite_items);

		((TextView) notificationLi.findViewById(R.id.notification_textview))
		.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		((TextView) directMessagesLi.findViewById(R.id.directMessages_textview))
		.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		((TextView) allsportsLi.findViewById(R.id.allSport_txtview))
		.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		((TextView) friendsLi.findViewById(R.id.menuFriends_txtview))
		.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		((TextView) aboutLi.findViewById(R.id.about_txtview))
		.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		((TextView) homeViewLi.findViewById(R.id.home_textview))
		.setTypeface(Constants.OPEN_SANS_SEMIBOLD);

	}

	/**
	 * setting the listeners
	 */
	private void setListeners() {	//verified

		pauseButton.setOnClickListener(this);
		recentActivityLi.setOnClickListener(this);
		userNameTextView.setOnClickListener(this);
		nameTextView.setOnClickListener(this);
		
		notificationLi.setOnTouchListener(this);
		directMessagesLi.setOnTouchListener(this);
		
		allsportsLi.setOnTouchListener(this);
		
		friendsLi.setOnTouchListener(this);
		
		aboutLi.setOnTouchListener(this);
		username_layout.setOnClickListener(this);
		homeViewLi.setOnTouchListener(this);


		changeItemColor ( notificationLi, false );
		changeItemColor ( directMessagesLi, false );
		changeItemColor ( allsportsLi, false );
		changeItemColor ( friendsLi, false );
		changeItemColor ( homeViewLi, false );

	}
	
	/**
	 * calling the respective functions to display data on the screen
	 */

	private void setValues() {	//verified

		try {
			setRadio();
			setWhatsHotItems();
			setFavouriteItems();
			setUserName();
			setUserAvatar();
			setRecentActivities();
			setNotification();
			setDirectMessages();


		} catch ( Exception e ) {
//			Logs.show(e);
		}
	}





	private void setRadio() {
		DatabaseUtil db = DatabaseUtil.getInstance();
		Hashtable<String, List<String>> currentRadio = db.getCurrentRadio();
		if(currentRadio != null && currentRadio.get("vRadioId") != null && currentRadio.get("vRadioId").size() > 0){
			
			PlayUpActivity.mediaPlayerService.startTimer();
			Log.e("123", "setting radio name and desc------"+currentRadio.get("vRadioTitle").get(0)+"--------"+currentRadio.get("vRadioSubTitle").get(0));
			radioName.setText(currentRadio.get("vRadioTitle").get(0));
			radiodesc.setText(currentRadio.get("vRadioSubTitle").get(0));
			pauseButton.setTag(R.id.aboutScrollView, false);
			pauseButton.setTag(R.id.about_txtview, currentRadio.get("vRadioStationUrl").get(0));
		}else{
			Log.e("123", "else of setting radio details");
			radioName.setText("Welcome To Playup");
			radiodesc.setText("About Us");
			pauseButton.setTag(R.id.aboutScrollView, true);
			pauseButton.setTag(R.id.about_txtview, "file:///android_asset/default.mp3");
		}
		
	}

	/**
	 * for setting favourite items
	 * 
	 * Date:18/07/2012
	 * Sprint:20
	 * 
	 */
	private void setFavouriteItems() {	//verified

		try {
			Runnable r = new Runnable() {

				@Override
				public void run() {
					try {
						final Hashtable<String, List<String>> favouriteData =DatabaseUtil.getInstance().getFavouriteSports();

						if( favouriteData!= null ) 
							favouriteSize = favouriteData.get("vCompetitionId").size();
						else
							favouriteSize = -1;
						if(PlayUpActivity.handler != null){

							PlayUpActivity.handler.post(new Runnable(){

								@Override
								public void run() {

									try {



										if( favouriteSize > 0 ) {
											favouriteTextLinearLayout.setVisibility( View.VISIBLE );
										} else {
											favouriteTextLinearLayout.setVisibility( View.GONE );
										}

										favouriteLi.setVisibility(View.VISIBLE);
										favouriteLi.removeAllViews();	

										LayoutInflater inflater = LayoutInflater.from(PlayUpActivity.context);

										for(int i=0;i<favouriteSize;i++)
										{

											RelativeLayout favourieItem  = ( RelativeLayout )inflater.inflate ( R.layout.menu_favourite_item, null );
											((TextView)favourieItem.findViewById(R.id.favSportName)).setText( favouriteData.get("vCompetitonName").get( i ) );
											((TextView)favourieItem.findViewById(R.id.favSportName)).setTypeface( Constants.OPEN_SANS_SEMIBOLD );
											((TextView)favourieItem.findViewById(R.id.regionName)).setText( favouriteData.get("vRegion").get( i ) );
											((TextView)favourieItem.findViewById(R.id.regionName)).setTypeface( Constants.OPEN_SANS_REGULAR );

											favourieItem.setId(whatsHotSize + hotItemId + i);
											favourieItem.setTag( favouriteData.get( "vCompetitionId").get( i ));

											favourieItem.setOnTouchListener( MenuFragment.this);

											favouriteLi.addView(favourieItem);

											//Adding divider for each recent element
											if( i != (favouriteSize -1) ) {
												View divider  = ( View )inflater.inflate ( R.layout.menu_body_divider, null );
												favouriteLi.addView(divider);
											}										

										}


										inflater = null;
									} catch ( Exception e ) {
//										Logs.show ( e );
									}
								}


							});


						}
					} catch (Exception e) {
//						Logs.show ( e );
					}





				}
			};


			Thread t = new Thread(r);
			t.start();
		} catch (Exception e) {
//			Logs.show ( e );
		}
	}

	/**
	 * for setting hot items in the menu
	 */
	private boolean isSetWhatsHotItems = false;
	private void setWhatsHotItems() {	//verified

		try {
			if ( isSetWhatsHotItems ) {
				return;
			}
			isSetWhatsHotItems = true;
			whatsHotItemsView.setVisibility(View.VISIBLE);
			whatsHotItemsView.removeAllViews();

			Runnable r = new Runnable() {

				@Override
				public void run() {
					try {

						final Hashtable<String, List<String>> hotItemData = DatabaseUtil.getInstance().getBaseSectionData();
						if(PlayUpActivity.handler != null){

							PlayUpActivity.handler.post(new Runnable(){

								@Override
								public void run() {

									try {
										if( hotItemData != null ) {
											whatsHotSize = hotItemData.get("vBaseSectionTitle").size();
											if ( whatsHotSize > 0 ) {
												featureTextLinearLayout.setVisibility( View.VISIBLE );
											} else {
												whatsHotSize = -1;
												featureTextLinearLayout.setVisibility( View.GONE );
											}
										}  else {
											whatsHotSize = -1;
											featureTextLinearLayout.setVisibility( View.GONE );
											return;
										}


										LayoutInflater inflater = LayoutInflater.from(PlayUpActivity.context);


										for ( int i = 0 ; i < whatsHotSize ; i++ ) {


											RelativeLayout hotItem  = ( RelativeLayout )inflater.inflate ( R.layout.hot_item_view, null );

											((TextView)hotItem.findViewById(R.id.hot_textview_item)).setText( hotItemData.get("vBaseSectionTitle").get( i ) );
											((TextView)hotItem.findViewById(R.id.hot_textview_item)).setTypeface( Constants.OPEN_SANS_SEMIBOLD );
											ImageView hotIcon = (ImageView) hotItem.findViewById(R.id.icon_whats_hot);

											imageDownloader.download(hotItemData.get("vBaseSectionImageUrl").get( i ), hotIcon, false, null);

											hotItem.setId(hotItemId+i);
											hotItem.setTag( R.id.aboutScrollView,hotItemData.get( "vBaseSectionId").get( i ));
											hotItem.setTag( R.id.about_txtview,hotItemData.get( "vBaseSectionTargetId").get( i ));
											
											isBaseSectionTargetHrefUrl = false;
											if(hotItemData.get( "vBaseSectionTargetHrefUrl").get( i )!=null && hotItemData.get( "vBaseSectionTargetHrefUrl").get( i ).trim().length()> 0 ){ 
												hotItem.setTag( R.id.aboutText,hotItemData.get( "vBaseSectionTargetHrefUrl").get( i ));
												isBaseSectionTargetHrefUrl = true;
											}
											else{
												hotItem.setTag( R.id.aboutText,hotItemData.get( "vBaseSectionTargetUrl").get( i ));
											}
											//hotItem.setTag( R.id.aboutText,hotItemData.get( "vBaseSectionTargetUrl").get( i ));
											hotItem.setOnTouchListener( MenuFragment.this);

											whatsHotItemsView.addView(hotItem);

											//Adding divider for each recent element
											if( i != (whatsHotSize -1) ) {
												View divider  = ( View )inflater.inflate ( R.layout.menu_body_divider, null );
												whatsHotItemsView.addView(divider);
											}	


										}

										inflater = null;
									} catch (Exception e) {
										// TODO Auto-generated catch block
//										Logs.show(e);
									}

								}


							});


						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
//						Logs.show ( e );
					}





				}
			};


			Thread t = new Thread(r);
			t.start();
		} catch (Exception e) {
//			Logs.show ( e );
		}

	}
	
	/**
	 * setting the number of unread notifications
	 */

	private void setNotification() {	//verified


		Runnable r = new Runnable () {

			@Override
			public void run() {

				try {
					// String notifications = dbUtil.getNotification();
					DatabaseUtil  dbUtil = DatabaseUtil.getInstance();

					if (!dbUtil.isUserAnnonymous()) {

						//img.setVisibility(View.VISIBLE);
						/*((TextView) notificationLi
								.findViewById(R.id.topbar_notification_text))
								.setTypeface(open_sans_semibold);*/

						final int notificationCount = dbUtil.getUnReadNotificationCount ();

						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable () {

								@Override
								public void run() {

									try {
										if ( !isVisible() ) {
											return;
										}
										txtView.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
										txtView.setVisibility(View.VISIBLE);

										if ( notificationCount > 99 ) {
											notificationLi.setPadding(0,0,21,0);
											txtView.setText( 99 + "+" );
										} else if ( notificationCount > 9 ) {
											notificationLi.setPadding(0,0,21,0);
											txtView.setText( notificationCount + "" );
										} else if(notificationCount == 0) {
											txtView.setVisibility(View.GONE);
										} else {
											notificationLi.setPadding(0,0,23,0);
											txtView.setText( notificationCount + "" );
										}
									} catch (Exception e) {
										// TODO Auto-generated catch block
//										Logs.show(e);
									}
								}

							});
						}

					} else {


						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable () {

								@Override
								public void run() {

									try {
										if ( !isVisible() ) {
											return;
										}
										PreferenceManagerUtil preferenceManagerUtil = new PreferenceManagerUtil ();
										if(  preferenceManagerUtil.get ( Constants.IS_ANONYMOUS_NOTIFICATIONS_VIEWED, false)) {
											txtView.setVisibility(View.GONE);
										} else {
											txtView.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
											txtView.setVisibility(View.VISIBLE);
											txtView.setText( "1" );
										}
									} catch (Exception e) {
										// TODO Auto-generated catch block
//										Logs.show(e);
									}
								}

							});
						}


					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}

			}

		};
		Thread  th = new Thread ( r );
		th.start();



	}


	/***
	 * setting the number of unread direct messages
	 */
	private void setDirectMessages() {	//verified

		Runnable r = new Runnable () {

			@Override
			public void run() {

				try {
					DatabaseUtil  dbUtil = DatabaseUtil.getInstance();

					if (!dbUtil.isUserAnnonymous()) {

						String vUserId = dbUtil.getPrimaryUserId();
						final int directMessagesCount = dbUtil.getUnReadDirectMessagesCount (vUserId);

						if ( PlayUpActivity.handler != null  ) {
							PlayUpActivity.handler.post( new Runnable() {

								@Override
								public void run() {

									try {
										if ( !isVisible() ) {
											return;
										}
										directMessagesTxtView.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
										directMessagesTxtView.setVisibility(View.VISIBLE);
										if ( directMessagesCount > 99 ) {
											directMessagesLi.setPadding(0,0,21,0);
											directMessagesTxtView.setText( 99 + "+" );
										} else if ( directMessagesCount > 9 ) {
											directMessagesLi.setPadding(0,0,21,0);
											directMessagesTxtView.setText( directMessagesCount + "" );
										} else if(directMessagesCount == 0) {
											directMessagesTxtView.setVisibility(View.GONE);
										} else {
											directMessagesLi.setPadding(0,0,23,0);
											directMessagesTxtView.setText( directMessagesCount + "" );
										}
									} catch (Exception e) {
										// TODO Auto-generated catch block
//										Logs.show(e);
									}
								}
							});
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				} 
			}
		};
		Thread th = new Thread ( r );
		th.start();



	}


	/**
	 * setting the user name
	 */
	private void setUserName() {	//verified


		if ( Constants.userName == null ) {
			userNameTextView.setText( "" );
			nameTextView.setText( "" );
		} else {

			if ( Constants.userName.equalsIgnoreCase( "Anonymous" ) ) {
				userNameTextView.setText(R.string.logIn);
				nameTextView.setText(R.string.hangout_with_friends);
			} else {
				userNameTextView.setText( Constants.userName );
				nameTextView.setText( Constants.name );
			}
		}

		new Thread( new Runnable() {

			@Override
			public void run() {

				try {
					DatabaseUtil  dbUtil = DatabaseUtil.getInstance();

					final String userName = dbUtil.getUserName();
					final boolean isUserAnonymous  = dbUtil.isUserAnnonymous();
					final String name = dbUtil.getName();

					if ( PlayUpActivity.handler  != null)  {
						PlayUpActivity.handler.post( new Runnable () {

							@Override
							public void run() {

								try {
									if ( !isVisible() ) {
										return;
									}
									// if the user name is there then enter the username else take the
									// default one "Annonymous"

									if (userName != null && userName.trim().length() > 0) {
										if (!userName.equalsIgnoreCase( "Anonymous" )) {
											userNameTextView.setOnClickListener( MenuFragment.this );
										}
										if (userName.equalsIgnoreCase("null")) {
											userNameTextView.setText( R.string.anonymous );
										} else {
											userNameTextView.setText( new Util().getSmiledText( userName ) );
										}


									} else {
										//	userNameTextView.setText("Anonymous");
										userNameTextView.setText(  R.string.anonymous );
									}

									if ( isUserAnonymous ) {
										userNameTextView.setText(R.string.logIn);
										nameTextView.setText(R.string.hangout_with_friends);

									} else {
										nameTextView.setOnClickListener( MenuFragment.this );

										if (name == null) {
											nameTextView.setText( "" );
										} else {
											new Util().getSmiledText( name , nameTextView );
										}

									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
//									Logs.show(e);
								}
							}
						});
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}
			}
		}).start();
	}

	
	/**
	 * setting the user avatar
	 */
	private void setUserAvatar() {	//verified

		Runnable r = new Runnable () {

			@Override
			public void run() {


				try {
					DatabaseUtil  dbUtil = DatabaseUtil.getInstance();

					String url = dbUtil.getUserAvatarUrl();

					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT vProviderName,vAvatarUrl FROM providers WHERE isSet = \"1\" " );

					if(c != null){

						if(c.getCount() == 1){
							c.moveToFirst();			
							if(c.getString(c.getColumnIndex("vAvatarUrl")) != null && c.getString(c.getColumnIndex("vAvatarUrl")).trim().length() > 0){
								if(c.getString(c.getColumnIndex("vProviderName")).equalsIgnoreCase("facebook")  ){						
									url = c.getString(c.getColumnIndex("vAvatarUrl"));						
								}
								else if(c.getString(c.getColumnIndex("vProviderName")).equalsIgnoreCase("twitter")  ){
									url = c.getString(c.getColumnIndex("vAvatarUrl"));					
								}
							}
						}
						c.close();
						c = null;
					}

					final String avartar_url = url;
					final String vUserId = dbUtil.getUserId();

					if ( PlayUpActivity.handler != null ) {
						PlayUpActivity.handler.post( new Runnable( )  {

							@Override
							public void run() {
								try {
									if ( !isVisible() ) {
										return;
									}
									imageDownloader.download( vUserId , avartar_url, avatarImageView, true,null);
								} catch (Exception e) {
									// TODO Auto-generated catch block
//									Logs.show(e);
								}
							}
						});
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();

	}

	
	/**
	 * setting the list of recent activities
	 */
	private void setRecentActivities() {	//verified


		Runnable r = new Runnable () {

			@Override
			public void run() {

				try {
					// get recent activities
					// manage the data and show in form of listview or something like that.
					DatabaseUtil  dbUtil = DatabaseUtil.getInstance();


					// show recent activity
					String userId = dbUtil.getPrimaryUserId();
					
					
					
					recentData = dbUtil.getRecentData (userId);
					
					


					if ( PlayUpActivity.handler != null ) {
						PlayUpActivity.handler.post( new Runnable () {

							@Override
							public void run() {

								try {
									if ( !isVisible() ) {
					
										return;
									}
									
									
									
									
									recentActivityTextLinearLayout.setVisibility(View.GONE);
									recentActivityLi.setVisibility(View.GONE);
									if ( recentData != null && recentData.get( "vRecentId" ).size() > 0 ) {

										recentActivityLi.removeAllViews();
										recentActivityTextLinearLayout.setVisibility(View.VISIBLE);
										recentActivityLi.setVisibility(View.VISIBLE);
									
										recentSize= recentData.get( "vRecentId" ).size() ;
										LayoutInflater inflater = LayoutInflater.from(PlayUpActivity.context);
										for(int i=0;i<recentSize;i++)
										{
											RelativeLayout recentItem  = ( RelativeLayout )inflater.inflate ( R.layout.activity_layout_list, null );

											// Checking whether the recent number is 0 or not
											if( recentData.get( "iUnRead").get( i ) == null || recentData.get( "iUnRead").get( i ).trim().equalsIgnoreCase("0") ) {
												((TextView)recentItem.findViewById(R.id.recent_activity_sub_text_view)).setVisibility(View.INVISIBLE);
											} else {
												((TextView)recentItem.findViewById(R.id.recent_activity_sub_text_view)).setVisibility(View.VISIBLE);
												((TextView)recentItem.findViewById(R.id.recent_activity_sub_text_view)).setText( recentData.get( "iUnRead").get( i ) );
											}

											//((TextView)recentItem.findViewById(R.id.recent_activity_main_text_view)).setText( recentData.get( "vRecentName").get( i ) );

											String hangoutName = recentData.get( "vRecentName").get( i );
											((TextView)recentItem.findViewById(R.id.recent_activity_main_text_view)).setText( new Util().getSmiledText(hangoutName) );

											((TextView)recentItem.findViewById(R.id.recentMatchName)).setText( recentData.get( "vSubjectTitle").get( i ) );

											((TextView)recentItem.findViewById(R.id.recent_activity_sub_text_view)).setTypeface(Constants.OPEN_SANS_REGULAR);
											((TextView)recentItem.findViewById(R.id.recent_activity_main_text_view)).setTypeface(Constants.OPEN_SANS_REGULAR);
											((TextView)recentItem.findViewById(R.id.recentMatchName)).setTypeface(Constants.OPEN_SANS_REGULAR);

											recentItem.setId(i);
											recentItem.setTag( recentData.get( "vSubjectId").get( i ));
										/*	recentItem.setTag( R.id.about_txtview,recentData.get( "vSubjectUrl").get( i ));*/
											if(recentData.get( "vSubjectHref").get( i )!=null && recentData.get( "vSubjectHref").get( i ).trim().length()>0){
												recentItem.setTag( R.id.about_txtview,recentData.get( "vSubjectHref").get( i ));
											}else{
												recentItem.setTag( R.id.about_txtview,recentData.get( "vSubjectUrl").get( i ));
											}

											recentItem.setOnTouchListener( MenuFragment.this);
											recentActivityLi.addView(recentItem);

											//Adding divider for each recent element
											if( i!= (recentSize -1) ) {
												View divider  = ( View )inflater.inflate ( R.layout.menu_body_divider, null );
												recentActivityLi.addView(divider);
											}
										}
										inflater = null;


									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
//									Logs.show(e);
								} 

							}

						});
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}


			}

		};
		Thread th = new Thread ( r );
		th.start();


	}

	/**
	 * handling data if any change in database has occured.
	 */
	@Override

	public void onUpdate( final Message msg) {	//verified


		if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
			
			new Util().getProfileData();
			
		}else if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("UpdateTime")){
			
			if ( PlayUpActivity.handler != null) {

				PlayUpActivity.handler.post(new Runnable() {

					@Override
					public void run() {
						try {
							if ( !isVisible() ) {
								return;
							}
							

							
							content_layout.findViewById(R.id.bufferingLayout).setVisibility(View.GONE);
							content_layout.findViewById(R.id.timeMain).setVisibility(View.VISIBLE);
							((TextView)content_layout.findViewById(R.id.time)).setTypeface(Constants.OPEN_SANS_LIGHT);
							((TextView)content_layout.findViewById(R.id.time)).setText(msg.getData().getString("time"));
							
							refreshMyProfile();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Logs.show ( e );
						}
					}
				});
				
				
			
			}
			
			
			
		}else if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("ShowBuffering")){
			
			if ( PlayUpActivity.handler != null) {

				PlayUpActivity.handler.post(new Runnable() {

					@Override
					public void run() {
						try {
							if ( !isVisible() ) {
								return;
							}
							

							
							content_layout.findViewById(R.id.bufferingLayout).setVisibility(View.VISIBLE);
							content_layout.findViewById(R.id.timeMain).setVisibility(View.GONE);
							
							refreshMyProfile();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Logs.show ( e );
						}
					}
				});
				
				
			
			}
			
			
			
		}
		
		else if ( PlayUpActivity.handler != null) {

			PlayUpActivity.handler.post(new Runnable() {

				@Override
				public void run() {
					try {
						if ( !isVisible() ) {
							return;
						}
						

						
						setValues();
						
						refreshMyProfile();
					} catch (Exception e) {
						// TODO Auto-generated catch block
//						Logs.show ( e );
					}
				}
			});
			
			
		
		}

	}

	/**
	 * handle the click events
	 */
	@Override
	public void onClick(View v) {

		

		Constants.isGrayBar = false;

		switch (v.getId()) {
		
		
		case R.id.pause:
			
			
			
			
				
				
			if(PlayUpActivity.mediaPlayerService != null)
				Log.e("123"," MediaPlayerService.isServiceStarted  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+PlayUpActivity.mediaPlayerService.isServiceStarted);
				
			if(PlayUpActivity.mediaPlayerService != null && PlayUpActivity.mediaPlayerService.isServiceStarted){
					
					
					if(PlayUpActivity.mediaPlayerService.isPaused){	
						PlayUpActivity.mediaPlayerService.play();
					}else{
						PlayUpActivity.mediaPlayerService.pause();
						
					}
					
					
				}else{
					Intent startMediaPlayer = new Intent(PlayUpActivity.context,MediaPlayerService.class);
					startMediaPlayer.putExtra("vRadioUrl", pauseButton.getTag(R.id.about_txtview).toString());
					startMediaPlayer.putExtra("isDefault", Boolean.parseBoolean(pauseButton.getTag(R.id.aboutScrollView).toString()));
					PlayUpActivity.context.startService(startMediaPlayer);
				}
				
				
			
			
			
			
			break;

		case R.id.menu_notifications:
			PlayupLiveApplication.getFragmentManagerUtil().setFragment( "NotificationFragment" );
			break;


		case R.id.menu_directMessages:
			
			
			/***
			 * if the user is annonymous,then redirecting to provider fragment
			 * else DirectConversationFragment
			 */

			new Thread( new Runnable() {

				@Override
				public void run() {

					try {
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						if ( dbUtil.isUserAnnonymous() ) {
							if ( PlayUpActivity.handler != null) {
								PlayUpActivity.handler.post( new Runnable( ) {

									@Override
									public void run() {

										try {
											if ( !isVisible() ) {
												return;
											}
											Bundle bundle = new Bundle();
											bundle.putString( "fromFragment", "DirectConversationFragment" );
											PlayupLiveApplication.getFragmentManagerUtil().setFragment( "ProviderFragment", bundle );
										} catch (Exception e) {
											// TODO Auto-generated catch block
//											Logs.show(e);
										}
									}
								});
							}
						} else {
							if ( PlayUpActivity.handler != null) {
								PlayUpActivity.handler.post( new Runnable( ) {

									@Override
									public void run() {
										try {
											if ( !isVisible() ) {
												return;
											}
											PlayupLiveApplication.getFragmentManagerUtil().setFragment("DirectConversationFragment");
										} catch (Exception e) {
											// TODO Auto-generated catch block
//											Logs.show(e);
										}
									}
								});
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
//						Logs.show ( e );
					}
				}
			}).start();

			break;

		case R.id.menu_allSportsResults:
			PlayupLiveApplication.getFragmentManagerUtil().setFragment("AllSportsFragment");
			break;
		case R.id.menu_friends:
			PlayupLiveApplication.getFragmentManagerUtil().setFragment("FriendsFragment");
			break;

		case R.id.menu_about:
			PlayupLiveApplication.getFragmentManagerUtil().setFragment( "AboutFragment");
			break;

		case R.id.text_user:
		case R.id.text_user_subtitle:
		case R.id.username_layout:

			PlayupLiveApplication.getFragmentManagerUtil().setFragment( "MyProfileFragment");

			break;

		case R.id.menu_home:
			

			PlayupLiveApplication.getFragmentManagerUtil().setFragment( "PrivateLobbyFragment");
			break;

		default:
			 /**
			  * clicking on what's hot data
			  */

			if( v.getId()>=hotItemId &&  v.getId()< (hotItemId+whatsHotSize)) {
				


				if(v.getTag(R.id.about_txtview) != null){


					
					{


						Bundle bundle = new Bundle();
						if( v.getTag(R.id.aboutScrollView)!= null )
							bundle.putString("vSectionId", v.getTag(R.id.aboutScrollView).toString() );
						if( v.getTag(R.id.aboutText)!= null )
							{
								bundle.putString("vSectionUrl", v.getTag(R.id.aboutText).toString() );
								bundle.putBoolean("isHrefUrl", isBaseSectionTargetHrefUrl);
							}

						PlayupLiveApplication.getFragmentManagerUtil().setFragment( "WelcomeFragment",bundle);

					} 


				}
			}
			 
			
			/**
			 * clicking on favourite matches
			 */

			if( v.getId() >= hotItemId+whatsHotSize  && v.getId() < hotItemId+whatsHotSize+favouriteSize ) {
				Bundle b = new Bundle();
				String vCompetitionId = null;
				if( v.getTag() != null )
					vCompetitionId = v.getTag().toString();

				b.putString("vCompetitionId",vCompetitionId);
				b.putString("fromFragment","MenuFragment");

				PlayupLiveApplication.getFragmentManagerUtil().setFragment("LeagueLobbyFragment",b);

			}

			
			/***
			 * clicking on recent activites
			 */

			if ( v.getId() >=0 && v.getId() < recentSize ) {
				for( int i=0; i<recentSize ;i++){
					if ( v.getId()==i ) {
						final Bundle bundle = new Bundle ();
						bundle.putString( "vConversationId", v.getTag().toString());
						bundle.putString ("fromFragment", "MenuFragment" );

						if(v.getTag(R.id.about_txtview) != null) {

							final String url = v.getTag(R.id.about_txtview).toString();

							Runnable r = new Runnable () {

								@Override
								public void run() {

									try {
										DatabaseUtil dbUtil = DatabaseUtil.getInstance();
										final String type = dbUtil.getHeader( url );

										if(type != null){
											if ( PlayUpActivity.handler != null)  {
												PlayUpActivity.handler.post( new Runnable() {

													@Override
													public void run() {

														try {
															if ( !isVisible() ) {
																return;
															}
															if(type.equalsIgnoreCase(Constants.ACCEPT_TYPE_COVERSATION)){
																PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchHomeFragment",bundle);
															}else{
																PlayupLiveApplication.getFragmentManagerUtil().setFragment("PrivateLobbyRoomFragment",bundle);
															}
														} catch (Exception e) {
															// TODO Auto-generated catch block
//															Logs.show(e);
														}			
													}
												});
											}

										}
									} catch (Exception e) {
										// TODO Auto-generated catch block
//										Logs.show ( e );
									}

								}

							};
							Thread th = new Thread ( r );
							th.start();


						}
						break;
					}
				}
			} else {
				PlayUpActivity.handler.postDelayed(null, (long) 300);
			}
			break;
		}
	}

	private void showHighLightColor(View v) {

		v.setBackgroundColor(Color.parseColor("#B0E6FF"));
		changeItemColor(v, true);
	}

	
	// handlnig the style and color
	private void changeItemColor(View v, boolean isSelected) {

		if (v.getId() >=0 && v.getId()<recentSize) {

			TextView sub_text_view = (TextView) v.findViewById(R.id.recent_activity_sub_text_view);
			TextView main_text_view = (TextView) v.findViewById(R.id.recent_activity_main_text_view);
			TextView recentMatchName = (TextView) v.findViewById(R.id.recentMatchName);
			if (isSelected) {
				//sub_text_view.setBackgroundColor(0xffffffff);
				sub_text_view.setBackgroundResource(R.drawable.post_count_d);
				sub_text_view.setTextColor(Color.parseColor("#B0E6FF"));
				main_text_view.setTextColor(0xffffffff);
				recentMatchName.setTextColor(0xffffffff);
			} else {
				v.setBackgroundColor(0xff575757);
				//sub_text_view.setBackgroundColor(0xff464646);
				sub_text_view.setBackgroundResource(R.drawable.post_count);
				sub_text_view.setTextColor(0xffffffff);
				main_text_view.setTextColor(0xff46FF64);
				recentMatchName.setTextColor(Color.parseColor("#979797"));
			}

			sub_text_view = null;
			main_text_view = null;
			recentMatchName = null;

		} else if( v.getId() >= hotItemId+whatsHotSize  && v.getId() < hotItemId+whatsHotSize+favouriteSize ) {
			TextView favouriteRegion = (TextView) v.findViewById(R.id.regionName);
			if (isSelected) {
				favouriteRegion.setTextColor(Color.parseColor("#FFFFFF"));
			} else {
				v.setBackgroundColor(0xff575757);
				favouriteRegion.setTextColor(Color.parseColor("#979797"));
			}
			favouriteRegion = null;		
			
		} else if(v.getId() == R.id.menu_notifications){
			if(isSelected){
				( (TextView)v.findViewById(R.id.notification_textview) ).setTextColor(Color.parseColor("#FFFFFF"));
			}			
			else{
				v.setBackgroundColor(Color.parseColor("#575757"));
				( (TextView)v.findViewById(R.id.notification_textview) ).setTextColor(Color.parseColor("#FFFFFF"));
			}
		}
		else if(v.getId() == R.id.menu_directMessages){
			if(isSelected){
				( (TextView)v.findViewById(R.id.directMessages_textview) ).setTextColor(Color.parseColor("#FFFFFF"));
			}			
			else{
				v.setBackgroundColor(Color.parseColor("#575757"));
				( (TextView)v.findViewById(R.id.directMessages_textview) ).setTextColor(Color.parseColor("#FFFFFF"));
			}
		}else if(v.getId() == R.id.menu_allSportsResults){

			if(isSelected){
				( (TextView)v.findViewById(R.id.allSport_txtview) ).setTextColor(Color.parseColor("#FFFFFF"));
			}	else{
				v.setBackgroundColor(Color.parseColor("#575757"));
				( (TextView)v.findViewById(R.id.allSport_txtview) ).setTextColor(Color.parseColor("#FFFFFF"));
			}


		}else if(v.getId() == R.id.menu_friends){

			if(isSelected){
				( (TextView)v.findViewById(R.id.menuFriends_txtview) ).setTextColor(Color.parseColor("#FFFFFF"));
			}	else{
				v.setBackgroundColor(Color.parseColor("#575757"));
				( (TextView)v.findViewById(R.id.menuFriends_txtview) ).setTextColor(Color.parseColor("#FFFFFF"));
			}


		}else if(v.getId() == R.id.menu_about){

			if(isSelected){
				( (TextView)v.findViewById(R.id.about_txtview) ).setTextColor(Color.parseColor("#FFFFFF"));
			}	else{
				v.setBackgroundColor(Color.parseColor("#575757"));
				( (TextView)v.findViewById(R.id.about_txtview) ).setTextColor(Color.parseColor("#FFFFFF"));
			}


		}else if(v.getId() == R.id.menu_home){

			if(isSelected){
				( (TextView)v.findViewById(R.id.home_textview) ).setTextColor(Color.parseColor("#FFFFFF"));
			}	else{
				v.setBackgroundColor(Color.parseColor("#575757"));
				( (TextView)v.findViewById(R.id.home_textview) ).setTextColor(Color.parseColor("#FFFFFF"));
			}


		}else{
			if(!isSelected){
				v.setBackgroundColor(0xff575757);
			}
		}
	}




	
	private  float rawY	=	0;
	long downTime		=	0;
	@Override
	public boolean onTouch ( View v, MotionEvent event ) {

		if(event.getAction()==MotionEvent.ACTION_DOWN){
			downTime 		=	event.getDownTime();
			
			rawY			=	event.getRawY();
			// For Showing highlight color while clicking
			showHighLightColor(v);

		}
		if(event.getAction()==MotionEvent.ACTION_UP){
			if(event.getRawY()==rawY||(event.getEventTime()-downTime)<200){
				if ((v.getId() != R.id.username_layout)
						&& (v.getId() != R.id.text_user_subtitle)
						&& (v.getId() != R.id.text_user)) {
					showHighLightColor(v);
				}
			}else{
				//				changeItemColor(v, true);
				changeItemColor(v, false);
			}
			onClick( v );
			return false;

		}
		if ( event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_CANCEL ) {
			changeItemColor(v, false);
		}


		if(event.getEventTime()>(downTime+Constants.highightDelay)){

			if(event.getRawY()>=rawY-10&&event.getRawY()<=rawY+10){

				if ((v.getId() != R.id.username_layout)
						&& (v.getId() != R.id.text_user_subtitle)
						&& (v.getId() != R.id.text_user)) {

					showHighLightColor(v);
				}
			}

		}

		return true;
	}



	private void setDirectMessageView(){	//verified
		directMessagesLi.setVisibility( View.VISIBLE );
		directmMsgDivider.setVisibility(View.VISIBLE); 

	}

}

