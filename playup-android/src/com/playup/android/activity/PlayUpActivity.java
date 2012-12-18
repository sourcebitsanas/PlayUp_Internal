package com.playup.android.activity;


import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.res.Resources.NotFoundException;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebViewFragment;
import android.widget.RemoteViews;

import com.flurry.android.FlurryAgent;
import com.mobileapptracker.MobileAppTracker;
import com.playup.android.R;
import com.playup.android.application.AirbrakeNotifier;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.connection.methods.Crypto;
import com.playup.android.database.DatabaseWrapper;
import com.playup.android.interfaces.ActivityInterface;
import com.playup.android.receiver.IntentReceiver;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Keys;
import com.playup.android.util.Logs;
import com.playup.android.util.PreferenceManagerUtil;
import com.playup.android.util.Util;
import com.urbanairship.push.PushManager;


public class PlayUpActivity extends FragmentActivity implements ActivityInterface {

	public static Hashtable<String, Boolean > runnableList = new Hashtable <String, Boolean > ( );
	
	/** Called when the activity is first created. */
	public static PlayUpActivity context;
	public static PlayUpActivity fragmentActivity;
	
	public static ExecutorService executorPool;
	public static IBinder mBinder;

	public static boolean isHomePressed;
	
	public static NotificationManager mNotificationManager;
	public static Handler handler = new Handler();
	
	public  MobileAppTracker mobileAppTracker = null;

	public static boolean isXhdpi = false;


	// timer and tasks 
	private static TimerTask refreshAllSportsTask;
	private static Timer refreshAllSportsTimer = new Timer();

	private static TimerTask refreshNotificationTask;
	private static Timer refreshNotificationTimer = new Timer();

	private static Timer refreshRecentActivityTimer;
	private static TimerTask refreshRecentActivityTask;

	private static Timer directConversationTimer;
	private static TimerTask directConversationTask;

	private static Timer playUpFriendsTimer;
	private static TimerTask playUpFriendsTask;
	
	

	//private static Timer refreshProviderTokensTimer;
	//private static TimerTask refreshProviderTokensTask;
	private static AlertDialog alertDialog;
	

	@Override
	protected void onSaveInstanceState ( Bundle outState ) {
		super.onSaveInstanceState ( outState );
		Constants.isCurrent = false;
	}

	@Override
	public void onCreate ( Bundle savedInstanceState ) {
		try {
			super.onCreate ( savedInstanceState );
			requestWindowFeature ( Window.FEATURE_NO_TITLE );

			PreferenceManagerUtil preferenceManagerUtil = new PreferenceManagerUtil();
			String locale = preferenceManagerUtil.get( "locale", null );
			String curr_locale = PlayupLiveApplication.getInstance().getResources().getConfiguration().locale.getLanguage();


			TelephonyManager tManager = (TelephonyManager)PlayUpActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
			String uid = tManager.getDeviceId();
			//Log.e("123", "Serial number--------------------------->>>>>>>>>>> "+uid);
			
			

			
			if ( locale != null && !locale.equalsIgnoreCase( curr_locale ) ) {

			new Thread ( new Runnable() {

				@Override
				public void run() {
						new Util().getDataFromServer(false);
						DatabaseUtil.getInstance().dropTables();
					}
				}).start();
			}

			Constants.isGrayBar = false;
			mobileAppTracker = new MobileAppTracker ( getBaseContext() );
			int response = mobileAppTracker.trackInstall();

			try {

				ProgressDialog pd = new  ProgressDialog( this );
				pd.setCancelable( false );
				pd.show ();
				
				PlayupLiveApplication.databaseWrapper = DatabaseWrapper.getInstance();
				
				pd.cancel();

			} catch ( Exception e ) {
				Logs.show ( e );
			} catch ( Error e ) {
				Logs.show ( e );
			} finally {

				if ( PlayupLiveApplication.databaseWrapper == null ) {
					PlayupLiveApplication.databaseWrapper = DatabaseWrapper.getInstance();
				}
			}

			// this block is for call MAT update while upgrading to new version
			String current_version = null;
			try {
				PackageInfo pInfo = getBaseContext().getPackageManager().getPackageInfo(getBaseContext().getPackageName(), 0);
				current_version = pInfo.versionName;

				if( current_version != null )
					current_version = current_version.trim();
				pInfo = null;
			} catch (Exception e) {
			}

			if( response == 3 && current_version != null && current_version.trim().length() >0 ) {
				preferenceManagerUtil = new PreferenceManagerUtil();
				if( ! preferenceManagerUtil.get(current_version, false) ) {			
					mobileAppTracker.trackUpdate();
					preferenceManagerUtil.set(current_version, true);
				}
				preferenceManagerUtil = null;


			} 




			new Util().getAppUpdateDetails( Keys.PLAYUP_UPGRADE_URL);



			setContentView(R.layout.main);


			registerReceivers();
			setValues();
		} catch (Exception e) {
			// TODO Auto-generated catch block
		Logs.show(e);
		}
	}

	/**
	 * method to be invoked to register the receiver
	 */
	private void registerReceivers() { 
		
		registerReceiver ( mConnReceiver, new IntentFilter ( ConnectivityManager.CONNECTIVITY_ACTION ) );
	
	}

	/**
	 * BroadCast receiver for internet connectivity
	 */
	private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive ( Context context, Intent intent ) {
			
			PlayupLiveApplication.getFragmentManagerUtil().onConnectionChanged ( !intent.getBooleanExtra ( ConnectivityManager.EXTRA_NO_CONNECTIVITY, false ) );

		}
	};

	private void setValues() {

		

		try {
			Constants.isCurrent = true;




			initialise();

			setListeners();

			setAdapters();


			Intent intent = getIntent();
			
			if (intent != null && intent.getBundleExtra("data") != null ) {
				
				Bundle b = intent.getBundleExtra("data");
				PlayupLiveApplication.getFragmentManagerUtil().popBackStack();
				PlayupLiveApplication.getFragmentManagerUtil().startTransaction();
				PlayupLiveApplication.getFragmentManagerUtil().setFragment("TopBarFragment", R.id.topbar);

				if ( b.getInt("pushType") == 0 ) {
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("DirectMessageFragment",b);
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, List<String>> notificationId = dbUtil.getNotificationId(b.getString("vShortUrl"));

					mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

					if(notificationId != null && notificationId.get("iNotificationId").size() > 0){
						for(int i = 0;i<notificationId.get("iNotificationId").size();i++){
							mNotificationManager.cancel(Integer.parseInt(notificationId.get("iNotificationId").get(i)));

						}
					}
					dbUtil.setIsRead(b.getString("vShortUrl"));

				}else if (b.getInt("pushType") == 1) {
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchHomeFragment", b);
				}else if (b.getInt("pushType") == 2) {
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("NotificationFragment");

				} else if (b.getInt("pushType") == 3) {
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("PrivateLobbyRoomFragment",b);
				} 

				PlayupLiveApplication.getFragmentManagerUtil().endTransaction();
			} else {

				PlayupLiveApplication.getFragmentManagerUtil().setFragment("SplashFragment");

			}


			AirbrakeNotifier.register(this, Keys.AIRBRAKE_API_KEY);

			if ( executorPool == null ) {
				executorPool = Executors.newFixedThreadPool(Constants.threadPoolSize);
			}
		} catch (NumberFormatException e) {

			Logs.show(e);
		}

	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Constants.isCurrent = true;
	}


	private boolean fromNewIntent = false;
	private Bundle b = null;


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		
		

		if (intent != null && intent.getBundleExtra("data") != null) {
			fromNewIntent = true;

			b = intent.getBundleExtra("data");
		} 

	}


	@Override
	public void onStart() {
		super.onStart();

		FlurryAgent.setLogEnabled( false );
		FlurryAgent.onStartSession( this, Keys.PLAYUP_FLURRY_KEY );

	}

	@Override
	protected void onResume() {

		try {
			super.onResume();

			context =  this;
			// call to set playup keys
			Crypto	c	=	new Crypto();
			c.setPlayUpKey_For_Credentials();

			Constants.isCurrent = true;


			new Thread( new Runnable() {


				@Override
				public void run() {

					boolean credentialAvailable = DatabaseUtil.getInstance().isCredentialAvailable();
					if ( credentialAvailable && !Constants.isAllSportsDownloading  
							&& runnableList != null && !runnableList.containsKey ( Constants.GET_ALL_SPORTS ) 
							&& Util.isInternetAvailable() ) {

						new Util().getAllSports(null);


					}
				}
			}).start();

			new Util().getUserNotificationData(true); // verified		
			new Util().getRecentActivityData(); //verified
//		new Util().getProviderTokens();




			refreshDirectConversation();
			mNotificationManager = ( NotificationManager ) getSystemService(NOTIFICATION_SERVICE);

			try {
				if (!PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName().equalsIgnoreCase("PostMessageFragment")) {
					isHomePressed = false;
				}
			} catch (Exception e) {
				Logs.show ( e );
			}
			
			


			if ( fromNewIntent ) {
				fromNewIntent = false;
				PlayupLiveApplication.getFragmentManagerUtil().popBackStack();
				PlayupLiveApplication.getFragmentManagerUtil().startTransaction();
				PlayupLiveApplication.getFragmentManagerUtil().setFragment("TopBarFragment", R.id.topbar);
				
				//Log.e("234", "PlayUp Activiy======>>>>pushType====>>>"+b.getInt("pushType"));
			
				
				
				if (b.getInt("pushType") == 0) {
					
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("DirectMessageFragment",b);
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, List<String>> notificationId = dbUtil.getNotificationId(b.getString("vShortUrl"));
					mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
					
				//	Log.e("234", "PlayUp Activiy======>>>>notificationId====>>>"+b.getInt("notificationId"));
					
					
					if(notificationId != null && notificationId.get("iNotificationId") != null && notificationId.get("iNotificationId").size() > 0){
						for(int i = 0;i<notificationId.get("iNotificationId").size();i++){
							mNotificationManager.cancel(Integer.parseInt(notificationId.get("iNotificationId").get(i)));

						}			 
					}


					dbUtil.setIsRead(b.getString("vShortUrl"));


				}else if (b.getInt("pushType") == 1) {
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchHomeFragment", b);
				}else if (b.getInt("pushType") == 2) {

					PlayupLiveApplication.getFragmentManagerUtil().setFragment("NotificationFragment");
				} else if (b.getInt("pushType") == 3) {
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("PrivateLobbyRoomFragment",b);

				} 
				PlayupLiveApplication.getFragmentManagerUtil().endTransaction();
				b = null;
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}



	
	public static void showDialog(){
		
		
//		PlayUpActivity.handler.post(new Runnable() {
//			
//			@Override
//			public void run() {
//			
//				AlertDialog.Builder alertBuilder = new AlertDialog.Builder(PlayUpActivity.context);
//				alertBuilder.setMessage("Error! Please try again later.")
//	               .setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
//	                   public void onClick(DialogInterface dialog, int id) {
//	                       // FIRE ZE MISSILES!
//	                	   Log.e("123", "Positive button clicked");
//	                   }
//	               })
//	               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//	                   public void onClick(DialogInterface dialog, int id) {
//	                       // User cancelled the dialog
//	                	   Log.e("123", "Positive button clicked");
//	                   }
//	               });
//	        // Create the AlertDialog object and return it
//				alertBuilder.create();
//				alertBuilder.show();
//				
//				
//			}
//		});
		
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Constants.isCurrent = true;
	}

	@Override
	public void setListeners() {
	}

	@Override
	public void initialise() {

		// getting screen density
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		Constants.Y = metrics.heightPixels;
		Constants.X = metrics.widthPixels;
		Constants.DPI = metrics.densityDpi;

		if( metrics.densityDpi == DisplayMetrics.DENSITY_XHIGH) {
			isXhdpi = true;
		} else {
			isXhdpi = false;
		}
		switch (metrics.densityDpi) {

		case DisplayMetrics.DENSITY_HIGH:
			Constants.DENSITY = "high";
			Constants.ANIMATION_HT = 306;
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			Constants.DENSITY = "medium";
			Constants.ANIMATION_HT = 255;
			break;
		case DisplayMetrics.DENSITY_LOW:
			Constants.DENSITY = "low";
			Constants.ANIMATION_HT = 236;
			break;

		}
		if (metrics.heightPixels > 480) {
			Constants.ANIMATION_HT = 306;
		}
		if (metrics.heightPixels > 800) {
			Constants.ANIMATION_HT = 146;
		}

		// FIX FOR GALAXY NEXUS
		if (metrics.densityDpi >= 300) {
			Constants.DENSITY = "high";
		}

		PlayupLiveApplication.setFragmentManager(getSupportFragmentManager());
		Constants.CACHE_DIR_PATH = getCacheDir().getAbsolutePath();

	}

	protected void onDestroy() {
		super.onDestroy();
		Constants.isCurrent = false;
		unregisterReceiver(mConnReceiver);
	}

	@Override
	public void setAdapters() {
	}

	// menu related code



	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		// checking menu for null state
		// clear the menu for all the calls so that we can have the most latest
		// menu.
		if (menu != null) {
			menu.clear();
		}

		// getting the top fragment from the fragment back stack.
		String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();

		// checking validating conditions for topFragmentName

		if (topFragmentName != null && topFragmentName.trim().length() > 0) {

			// initlialising the resource Id.
			int resourceId = -1;

			// checking for menu map for the topFragmentName key
			if (Constants.menuMap != null
					&& Constants.menuMap.containsKey(topFragmentName)) {
				
				
				
				resourceId = Constants.menuMap.get(topFragmentName);
			}

			// back chevron  click from MatchroomFragment to currentLeagueRoundFragment to display context menu

			// resource > -1 -- the menu.xml has been made for that screen and
			// is available.
			// resource = -1 -- menu..xml are not yet available for this screen.


			if (resourceId > -1) {

				MenuInflater inflater = getMenuInflater();
				inflater.inflate(resourceId, menu);

			}
		}
		topFragmentName = null;
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle item selection
		switch (item.getItemId()) {

		case R.id.menu_edit_profile:
			PlayupLiveApplication.getFragmentManagerUtil().setFragment(	"EditProfileFragment" );
			break;

		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	/**
	 * handling the back button especially to handle the life cycle of the
	 * fragment.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {


		if ((keyCode == KeyEvent.KEYCODE_BACK)) {


			int backStackEntryCount = PlayupLiveApplication.getFragmentManager().getBackStackEntryCount();

			if ( backStackEntryCount <= 1 ) {
				finish();
				Constants.isCurrent = false;
				return true;
			} else {

				if ( backStackEntryCount == 2 ) {
					if ( PlayupLiveApplication.getFragmentManager().getBackStackEntryAt( 0 ).getName().equalsIgnoreCase( "TopBarFragment") || 
							PlayupLiveApplication.getFragmentManager().getBackStackEntryAt( 1 ).getName().equalsIgnoreCase( "TopBarFragment") ) {

						finish();
						Constants.isCurrent = false;
						return true;

					}
				}
				
				
				String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();

				
				String[] fragmentName = topFragmentName.split("%", 2);
				
				
				
				if(fragmentName != null && fragmentName.length > 0)
					topFragmentName = fragmentName[0];
				
				

				if (topFragmentName != null

						&& ( topFragmentName.equalsIgnoreCase("LeagueSelectionFragment") 
								|| topFragmentName.equalsIgnoreCase("MatchHomeFragment"))
								|| topFragmentName.equalsIgnoreCase("LiveSportsFragment")
								|| topFragmentName.equalsIgnoreCase("MatchRoomFragment")
								|| topFragmentName.equalsIgnoreCase("InviteFriendFragment")
								|| topFragmentName.equalsIgnoreCase("PrivateLobbyInviteFriendFragment")
								
								|| topFragmentName.equalsIgnoreCase("PlayupFriendsFragment") 
								|| topFragmentName.equalsIgnoreCase("DirectConversationFragment") 
								|| topFragmentName.equalsIgnoreCase("DirectMessageFragment")
								|| topFragmentName.equalsIgnoreCase("WebViewFragment")
								|| topFragmentName.equalsIgnoreCase("LeagueLobbyFragment")
								|| topFragmentName.equalsIgnoreCase("AllSportsFragment")
								|| topFragmentName.equalsIgnoreCase("PostMessageFragment")) {



					Message msg = new Message();
					msg.obj = "handleBackButton";

					PlayupLiveApplication.callUpdateOnFragments(msg);

					topFragmentName = null;
					return true;
				} else if (topFragmentName != null
						&& topFragmentName.equalsIgnoreCase("FriendsFragment")) {

					int backStackCount = PlayupLiveApplication.getFragmentManager().getBackStackEntryCount();
					BackStackEntry entry = null;
					BackStackEntry entry2 = null;
					if ( backStackCount - 2 > -1 ) {
						entry = PlayupLiveApplication.getFragmentManager().getBackStackEntryAt( backStackCount - 2 );
					}
					if ( entry != null && entry.getName().contains( "PlayupFriendsFragment" ) ) {
						entry = null;
						
						if ( backStackCount - 3 > -1 ) {
							entry2 = PlayupLiveApplication.getFragmentManager().getBackStackEntryAt( backStackCount - 3 );
						}
						
						if ( entry2 != null && entry2.getName().contains( "DirectConversationFragment" ) ) {
							
							PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( entry2.getName() );
							entry2 = null;
						}
						
						
					} else {
						Message msg = new Message();
						msg.obj = "handleBackButton";

						PlayupLiveApplication.callUpdateOnFragments(msg);

						topFragmentName = null;
					}
					return true;
				} else {
					return super.onKeyDown(keyCode, event);
				}

			}
		}


		return super.onKeyDown(keyCode, event);
	}


	public static void refreshAllSports() {			


		

		try {
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();

			Hashtable<String, Object> result = dbUtil.getAllSportsURL();
			
			if(result == null || !result.containsKey("url") || result.get("url") == null
					|| result.get("url").toString().trim().length() > 0)
				return;
			
			String allSportsUrl = (String) result.get("url");
			

			final int allSportscacheTime = Integer.parseInt(dbUtil.getCacheTime(allSportsUrl));

			allSportsUrl = null;
			dbUtil = null;
			if (refreshAllSportsTimer != null) {
				refreshAllSportsTimer.cancel();
				refreshAllSportsTimer = null;
			}
			refreshAllSportsTimer = new Timer();

			if (refreshAllSportsTask != null) {
				refreshAllSportsTask.cancel();
				refreshAllSportsTask = null;
			}

			refreshAllSportsTask = new TimerTask() {

				@Override
				public void run() {
					try {
						if (Util.isInternetAvailable()) {

							new Thread( new Runnable() {

								@Override
								public void run() {

									try {
										boolean credentialAvailable = DatabaseUtil.getInstance().isCredentialAvailable();
										if ( credentialAvailable &&  !Constants.isAllSportsDownloading  
												&& runnableList != null && !runnableList.containsKey ( Constants.GET_ALL_SPORTS ) 
												) {

											new Util().getAllSports(null);


										}
									} catch (Exception e) {
										Logs.show(e);
									}
								}
							}).start();

						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show(e);
					}
				}

			};
			
			try {
				if (allSportscacheTime > 0) {

					refreshAllSportsTimer.schedule(refreshAllSportsTask,
							(allSportscacheTime * 1000), (allSportscacheTime * 1000));

				}
			} catch (Exception e) {
				Logs.show(e);
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block


			Logs.show(e);
		}

	}
	
	
//	public static void refreshProviderTokens() {
//		
//	try {
//		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
//		String vProviderTokenUrl = dbUtil.getProviderTokensUrlForRefresh();
//		final int vProviderTokencacheTime = Integer.parseInt(dbUtil.getCacheTime(vProviderTokenUrl));
//		
//		Logs.show("get the provider token vProviderTokenUrl >>>>>>>>>>>>>>>>>>>>>>>>>  "+vProviderTokenUrl);
//		
//		Logs.show("get the provider token cacheTime >>>>>>>>>>>>>>>>>>>>>>>>>  "+vProviderTokencacheTime);
//
//		vProviderTokenUrl = null;
//		dbUtil = null;
//		if (refreshProviderTokensTimer == null) {
//			
//			refreshProviderTokensTimer = new Timer();
//		}
//
//		if (refreshProviderTokensTask == null) {
//			
//
//				refreshProviderTokensTask = new TimerTask() {
//
//			@Override
//			public void run() {
//				try {
//					if (Util.isInternetAvailable()) {
//
//						new Thread( new Runnable() {
//
//							@Override
//							public void run() {
//
//								try {
//									
//									
//
//										new Util().getProviderTokens();
//
//									
//								} catch (Exception e) {
//									Logs.show(e);
//								}
//							}
//						}).start();
//
//					}
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//
//				}
//			}
//			
//			
//			
//		};
//		
//		
//		if (vProviderTokencacheTime > 0) {
//
//			refreshProviderTokensTimer.schedule(refreshProviderTokensTask,
//					(vProviderTokencacheTime * 1000), (vProviderTokencacheTime * 1000));
//
//
//		}
//		
//		}
//	} catch (Exception e) {
//		
//		Logs.show(e);
//		
//	}
//}
//	
	



	public static void cancelTimers() {
		if (refreshNotificationTimer != null) {
			refreshNotificationTimer.cancel();
			refreshNotificationTimer = null;
		}
		

		if (refreshNotificationTask != null) {
			refreshNotificationTask.cancel();
			refreshNotificationTask = null;
		}

		if (refreshRecentActivityTimer != null) {
			refreshRecentActivityTimer.cancel();
			refreshRecentActivityTimer = null;
		}

		if (refreshRecentActivityTask != null) {
			refreshRecentActivityTask.cancel();
			refreshRecentActivityTask = null;
		}


		if (directConversationTimer != null) {
			directConversationTimer.cancel();
			directConversationTimer = null;
			
		}

		if( directConversationTask!= null) {
			directConversationTask.cancel();
			directConversationTask = null;
		}


		if (playUpFriendsTimer != null) {
			playUpFriendsTimer.cancel();
			playUpFriendsTimer = null;
		}

		if (playUpFriendsTask != null) {
			playUpFriendsTask.cancel();
			playUpFriendsTask = null;
		}
		
//		if (refreshProviderTokensTimer != null) {
//			refreshProviderTokensTimer.cancel();
//		}
//
//		if (refreshProviderTokensTask != null) {
//			refreshProviderTokensTask.cancel();
//			refreshProviderTokensTask = null;
//		}


	}


	public static void refreshNotification() {  

		try {
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			
			Hashtable<String, Object> result = dbUtil.getUserNotificationUrl();
			String vUserNotificationUrl = (String) result.get("url");
			Boolean isHrefUrl = (Boolean) result.get("isHref");
			
			
			final int vUserNotificationCacheTime = Integer.parseInt(dbUtil.getCacheTime(vUserNotificationUrl));

			vUserNotificationUrl = null;
			dbUtil = null;
			if (refreshNotificationTimer == null) 			
				refreshNotificationTimer = new Timer();

			if (refreshNotificationTask == null) {


				refreshNotificationTask = new TimerTask() {

					@Override
					public void run() {
						try {
							if (Util.isInternetAvailable()) {

								DatabaseUtil dbUtil = DatabaseUtil.getInstance();

								if (!dbUtil.isUserAnnonymous()) {

									new Util().getUserNotificationData(true);
								}
								dbUtil = null;
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Logs.show(e);
						}

					}
				};
				try{

					if (vUserNotificationCacheTime > 0) {

						refreshNotificationTimer.schedule(refreshNotificationTask, (vUserNotificationCacheTime * 1000), (vUserNotificationCacheTime * 1000));
					}
				}catch(Exception e){
					Logs.show(e);
				}

			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}




	}

	@Override
	protected void onStop() {

		super.onStop();
		
		Constants.isCurrent = false;

		
		FlurryAgent.onEndSession( PlayUpActivity.context );

		if (refreshAllSportsTimer != null) {
			refreshAllSportsTimer.cancel();
			refreshAllSportsTimer = null;
		}

		if (refreshAllSportsTask != null) {
			refreshAllSportsTask.cancel();
			refreshAllSportsTask = null;
		}

		if (refreshRecentActivityTimer != null) {
			refreshRecentActivityTimer.cancel();
			refreshRecentActivityTimer = null;
		}

		if (refreshRecentActivityTask != null) {
			refreshRecentActivityTask.cancel();
			refreshRecentActivityTask = null;
		}

		if (refreshNotificationTimer != null) {
			refreshNotificationTimer.cancel();
			refreshNotificationTimer = null;
		}

		if (refreshNotificationTask != null) {
			refreshNotificationTask.cancel();
			refreshNotificationTask = null;
		}

		if (directConversationTimer != null) {
			directConversationTimer.cancel();
			directConversationTimer = null;
		}

		if( directConversationTask!= null) {
			directConversationTask.cancel();
			directConversationTask = null;
		}


		if (playUpFriendsTimer != null) {
			playUpFriendsTimer.cancel();
			playUpFriendsTimer = null;
		}

		if (playUpFriendsTask != null) {
			playUpFriendsTask.cancel();
			playUpFriendsTask = null;
		}
		
//		if (refreshProviderTokensTimer != null) {
//			refreshProviderTokensTimer.cancel();
//		}
//
//		if (refreshProviderTokensTask != null) {
//			refreshProviderTokensTask.cancel();
//			refreshProviderTokensTask  = null;
//		}




		isHomePressed = true;
		
		b = null;

	}

	public static void registerC2DM() {
		new Util().postC2DM();
	}



	public static void refreshRecentActivity() {	


		try {
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			final Hashtable<String, Object> result = dbUtil.getRecentActivityUrl();
			final String vRecentActivityUrl = (String) result.get("url");
			
			
			
			final int vRecentActivityCacheTime = Integer.parseInt(dbUtil .getCacheTime(vRecentActivityUrl));

//	Log.e("123","insde refreshe recentAcitivtiy vRecentActivityCacheTime>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+vRecentActivityCacheTime);
			
			dbUtil = null;


//	Log.e("123","insde refreshe recentAcitivtiy vRecentActivityCacheTime>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+vRecentActivityCacheTime);
			
			dbUtil = null;



			if(refreshRecentActivityTimer == null)
				refreshRecentActivityTimer = new Timer();



			if(refreshRecentActivityTask == null){
				refreshRecentActivityTask = new TimerTask() {

					@Override
					public void run() {
						try {
							if (Util.isInternetAvailable()) {
								
								new Util().getRecentActivityData();

							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Logs.show(e);
						}
					}
				};
				try{
					if (vRecentActivityCacheTime > 0) {			
						refreshRecentActivityTimer.schedule(refreshRecentActivityTask,(vRecentActivityCacheTime * 1000),(vRecentActivityCacheTime * 1000));

					}
				}catch(Exception e){
					Logs.show(e);
				}


			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}

	public static void stopDirectConversation () {

		if ( directConversationTimer != null) {
			directConversationTimer.cancel();
			directConversationTimer = null;
		}

		if ( directConversationTask != null) {
			directConversationTask.cancel();
			directConversationTask = null;
		}

	}
	
	
	public static void refreshDirectConversation ( ) {   // verfied

		new Thread ( new Runnable () {

			@Override
			public void run() {

				try {
					// get the direct conversation url  
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					
					
					Hashtable<String, Object> result = dbUtil.getDirectConversationUrl();
					if(result!=null && result.containsKey("url") && result.containsKey("isHref") && result.get("url")!=null && result.get("url").toString().trim().length()>0 && result.get("isHref")!=null && result.get("isHref").toString().trim().length()>0){

						
					
					final String vDirectConversationUrl = (String) result.get("url");
					

					// getting the cache time
					final int vDirectConversationCacheTime = Integer.parseInt(dbUtil .getCacheTime( vDirectConversationUrl ));

					// reseting the timer and  tasks
					if ( directConversationTimer != null ) {
						directConversationTimer.cancel();
						directConversationTimer = null;
					}
					if ( directConversationTask != null ) {
						directConversationTask.cancel();
						directConversationTask = null;
					}


					if ( directConversationTimer == null)  {
						directConversationTimer = new Timer();
					}
					if ( directConversationTask == null) {
						directConversationTask = new TimerTask() {

							@Override
							public void run() {
								try {
									if ( Util.isInternetAvailable() ) {
										new Util().getDirectConversationData();
									}
								} catch (Exception e ) {
									Logs.show ( e );
								} catch ( Error e ) {
									Logs.show ( e );
								}
							}
						};
						try{
							if ( vDirectConversationCacheTime > 0) {
								directConversationTimer.schedule ( directConversationTask, ( vDirectConversationCacheTime * 1000 ),( vDirectConversationCacheTime * 1000 ) );	
							}
						} catch ( Exception e ) {
							Logs.show(e);
						} catch ( Error e ) {
							Logs.show ( e );
						}
					}
					}else {
						//
					}
					
				} catch (Exception e) {
					
					Logs.show(e);
					
				}
			}
		}).start();
	}


	public static void refreshPlayUpFriends () {  

		try {
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			
			final Hashtable<String, Object> result = dbUtil.getPlayUpFriendsUrl();
			final String vPlayUpFriendsUrl = (String) result.get("url");
			
			
			
			final int vPlayUpFriendsUrlCacheTime = Integer.parseInt(dbUtil .getCacheTime( vPlayUpFriendsUrl ));

			dbUtil = null;
			if ( playUpFriendsTimer == null) 
				playUpFriendsTimer = new Timer();
			if ( playUpFriendsTask == null) {			
				playUpFriendsTask = new TimerTask() {
					@Override
					public void run() {
						try {
							if (Util.isInternetAvailable()) {					
								if ( runnableList != null && !runnableList.containsKey ( Constants.GET_PLAYUP_FREINDS_DATA ) ) {
									new Util().getPlayUpFriendsData();
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Logs.show(e);
						}
					}
				};
				try {
					if ( vPlayUpFriendsUrlCacheTime > 0) {
						playUpFriendsTimer.schedule( playUpFriendsTask,(vPlayUpFriendsUrlCacheTime * 1000),(vPlayUpFriendsUrlCacheTime * 1000));
					}	
				} catch ( Exception e ) {
					Logs.show ( e );
				}

			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}

	public static void showNotification(Intent intent,int type) {	
		try {
			if(type == 0 && Constants.inDirectMessageFragment)
				return; 


			int icon = R.drawable.notification_g;
			CharSequence tickerText = intent.getStringExtra(PushManager.EXTRA_ALERT);
			long when = System.currentTimeMillis();

			Notification notification = new Notification(icon,tickerText,when);
			
			RemoteViews contentView = new RemoteViews(PlayUpActivity.context.getPackageName(), R.layout.notification1);
			contentView.setImageViewResource(R.id.icon, R.drawable.notification_g);
			contentView.setTextViewText(R.id.subject, PlayUpActivity.context.getResources().getString(R.string.app_name));
			contentView.setTextViewText(R.id.message, intent.getStringExtra(PushManager.EXTRA_ALERT));
			notification.contentView = contentView;

			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			Intent notificationIntent = new Intent(PlayUpActivity.context, IntentReceiver.class);
			notificationIntent.setAction(PushManager.ACTION_NOTIFICATION_OPENED);
			notificationIntent.fillIn(intent, Intent.FILL_IN_DATA);
			PendingIntent contentIntent = PendingIntent.getBroadcast(PlayUpActivity.context, 0, notificationIntent, 0);
			notification.contentIntent = contentIntent;

			mNotificationManager.notify(12, notification);
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}

	
	/**
	 * Vaibhav:
	 * @param msg
	 */
	
	public static void showErrorDialog(String msg){
		
		try {
			//Log.e("123", "Inside handle the error condition!!!!!!!!!!Activity!!!!!!!!!!!!!!");
			if(msg != null &&  msg.equalsIgnoreCase("errorFethchingCredentials")){
				if(PlayUpActivity.handler != null){
					
					PlayUpActivity.handler.post(new Runnable() {
						
						@Override
						public void run() {
							alertDialog = new AlertDialog.Builder(PlayUpActivity.context).create();
							alertDialog.setTitle("Error!");
							alertDialog.setMessage("Please Try Again");
							alertDialog.setButton("Try Again", new DialogInterface.OnClickListener() {
							      public void onClick(DialogInterface dialog, int which) {
							 
							       
							    	  new Util().callRootApi();	
							    } });
							alertDialog.setButton2("Quit", new DialogInterface.OnClickListener() {
							      public void onClick(DialogInterface dialog, int which) {
							 
							       	//  PlayupLiveApplication.getFragmentManagerUtil().popBackStack();
							      	int backStackEntryCount = PlayupLiveApplication.getFragmentManager().getBackStackEntryCount();
							      	
							        //Log.e("123", "backStackEntryCount------->>>"+backStackEntryCount);
							       	//Log.e("123", "PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName()---->>>"+PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName());
							       						      
										PlayUpActivity.context.finish();
										Constants.isCurrent = false;
										
									
							       	 
							    } });
							if(!alertDialog.isShowing())
							alertDialog.show();
							
						}
					});
					
				}
				
					
				
			}
			if(msg != null &&  msg.equalsIgnoreCase("editProfileError")){
				if(PlayUpActivity.handler != null){
					
					PlayUpActivity.handler.post(new Runnable() {
						
						@Override
						public void run() {
							alertDialog = new AlertDialog.Builder(PlayUpActivity.context).create();
							alertDialog.setTitle("Error!");
							alertDialog.setMessage("Profile details was not saved successfully.");
							alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
							      public void onClick(DialogInterface dialog, int which) {
							 					       						    	  	
							    } });
//						alertDialog.setButton2("Quit", new DialogInterface.OnClickListener() {
//						      public void onClick(DialogInterface dialog, int which) {
//						 
//						       	//  PlayupLiveApplication.getFragmentManagerUtil().popBackStack();
//						      	int backStackEntryCount = PlayupLiveApplication.getFragmentManager().getBackStackEntryCount();
//						      	
//						        Log.e("123", "backStackEntryCount------->>>"+backStackEntryCount);
//						       	Log.e("123", "PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName()---->>>"+PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName());
//						       						      
//									PlayUpActivity.context.finish();
//									Constants.isCurrent = false;
//									
//								
//						       	 
//						    } });
							if(!alertDialog.isShowing())
							alertDialog.show();
							
						}
					});
					
				}
				
					
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
		
		

	}




}