/*
 * Copyright 2011 PlayUP. All Rights Reserved.

 */
package com.playup.android.application;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.FragmentManager;

import android.widget.Toast;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.connection.HttpRequest;
import com.playup.android.database.DatabaseWrapper;
import com.playup.android.receiver.IntentReceiver;
import com.playup.android.util.Constants;
import com.playup.android.util.FragmentManagerUtil;
import com.playup.android.util.Keys;

import com.playup.android.util.PollManager;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.BasicPushNotificationBuilder;
import com.urbanairship.push.PushManager;


public class PlayupLiveApplication extends Application {

	private static PollManager pollMangar;
	private static PlayupLiveApplication applicationContext;
	private static FragmentManager fragmentManager ;
	private static FragmentManagerUtil fragmentManagerUtil ;

	private static ThreadPoolExecutor threadPoolExecutor;

	public static DatabaseWrapper databaseWrapper;

	private static Handler handler = new Handler ();


	private static List < HttpRequest > list = new ArrayList < HttpRequest > ();

	public PlayupLiveApplication() {
		applicationContext = this;
	}

	public static PlayupLiveApplication getInstance () {

		return applicationContext;

	}

	public static void setFragmentManager ( FragmentManager fmanager)  {
		fragmentManager = fmanager;
	}


	public static FragmentManager getFragmentManager () {

		return fragmentManager;
	}

	public static FragmentManagerUtil getFragmentManagerUtil () {

		if ( fragmentManagerUtil == null ) {
			fragmentManagerUtil = new FragmentManagerUtil();
		} 
		return fragmentManagerUtil;
	}

	public static ThreadPoolExecutor getThreadPoolExecutor () {

		return threadPoolExecutor;
	}

	public static void removeHttRequest ( HttpRequest obj ) {
		try {
			list.remove( obj );
		} catch ( Exception e) {

		}
	}
	public static List < HttpRequest > getHttpRequestList () {
		return list;
	}

	public static void addHttRequest ( HttpRequest command ) {
		try {
			list.add( command );
		} catch ( Exception e) {

		}
	}


	public static DatabaseWrapper getDatabaseWrapper () {
		if ( databaseWrapper == null ) {

			databaseWrapper = DatabaseWrapper.getInstance();


		}
		return databaseWrapper;
	}


	public static PollManager getPollManager () {

		if ( pollMangar == null ) {
			pollMangar = new PollManager();
		} 
		return pollMangar;
	}
	
	
	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA

		super.onCreate();



		initialise ();
		
		
		




		// start the service
		// register for connection receivers 
		// copy database 
		// sync database

		// force the load of the end points early on

	}


	class PlayUpRejectedExecutionHandler implements RejectedExecutionHandler {

		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

		}

	}


	/**
	 * initialises databaseWrapper, fragment Util and threadpoolExecutor. 
	 */
	private void initialise () {

		pollMangar = new PollManager();

		fragmentManagerUtil = new FragmentManagerUtil();

		threadPoolExecutor = new ThreadPoolExecutor ( 5, 5, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>( 100  ), new PlayUpRejectedExecutionHandler ( ) );




		// create the menu mapping


		/**
		 * FIKSU TRACKING
		 */

		//FiksuTrackingManager.initialize(this);

		/**
		 * 
		 * GOOGLE ANALYTICS
		 */



		//String deviceId = Settings.System.getString(getContentResolver(),Settings.System.ANDROID_ID);


		/**
		 * Urban Airship Notification 
		 */
		final AirshipConfigOptions options = AirshipConfigOptions.loadDefaultOptions(this);
		// Optionally, customize your config at runtime:

		UAirship.takeOff( PlayupLiveApplication.this, options );
		PushManager.shared().setIntentReceiver(IntentReceiver.class);
		PushManager.enablePush();


		// Handling push notification by defalult settings  
		BasicPushNotificationBuilder nb = new BasicPushNotificationBuilder();
		nb.iconDrawableId = R.drawable.notification_g;
		PushManager.shared().setNotificationBuilder(nb);




		if (Keys.DEVELOPER_MODE) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
			.detectDiskReads()
			.detectDiskWrites()
			.detectNetwork()   // or .detectAll() for all detectable problems
			.penaltyLog()
			.build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
			.detectLeakedSqlLiteObjects()
			.detectLeakedSqlLiteObjects()
			.penaltyLog()
			.penaltyDeath()
			.build());
		}else{
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

			StrictMode.setThreadPolicy(policy); 
		}


	}



	/**
	 * updating the fragments.  
	 */
	public static void callUpdateOnFragments ( Message msg ) {

		if ( fragmentManagerUtil != null ) {
			fragmentManagerUtil.onUpdate( msg );
		} else {
			fragmentManagerUtil = new FragmentManagerUtil();
			fragmentManagerUtil.onUpdate( msg );
		}
	}


	/**
	 * updating the fragments.  
	 */
	public static void callUpdateTopBarFragments ( Message msg ) {

		if ( fragmentManagerUtil != null ) {
			fragmentManagerUtil.updateTopBarFragment ( msg );
		} else {
			fragmentManagerUtil = new FragmentManagerUtil();
			fragmentManagerUtil.updateTopBarFragment ( msg );
		}
	}




	/**
	 * updating the fragments except topbar fragmnt .  
	 */
	public static void callUpdateOnFragmentsNotTopBar ( Message msg ) {

		if ( fragmentManagerUtil != null ) {
			fragmentManagerUtil.onUpdateNotTopBar( msg );
		} else {
			fragmentManagerUtil = new FragmentManagerUtil();
			fragmentManagerUtil.onUpdateNotTopBar( msg );
		}
	}


	/**
	 * for showing any toast message 
	 */ 
	public static void showToast ( final int resourceId ) {
		
		

		if ( handler != null ) {

			handler.post( new Runnable () {

				@Override
				public void run() {

					try {
						Toast.makeText( PlayupLiveApplication.getInstance().getBaseContext(), resourceId, Toast.LENGTH_SHORT ).show();
					} catch (NotFoundException e) {
						// TODO Auto-generated catch block
						//Logs.show ( e );
					}
				}

			});
		}
	}

	/**
	 * for showing any toast message 
	 */ 
	public static void showToast ( final String toastMessage ) {

		if ( handler != null ) {

			handler.post( new Runnable () {

				@Override
				public void run() {

					try {
						Toast.makeText( PlayupLiveApplication.getInstance().getBaseContext(), toastMessage, Toast.LENGTH_SHORT ).show();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//Logs.show ( e );
					}
				}

			});
		}
	}


	/**
	 * shows upgrade dialog to the user
	 * @param apk_path
	 */

	public static void showUpgradeDialog( final String apk_path) {


		if( PlayUpActivity.handler!= null) {
			PlayUpActivity.handler.post(new Runnable() {

				@Override
				public void run() {

					try {

						if ( Constants.isCurrent && PlayUpActivity.context != null ) {


							final AlertDialog alertDialog = new AlertDialog.Builder( PlayUpActivity.context ).create();
							alertDialog.setMessage( PlayUpActivity.context.getResources().getString(R.string.updateVersion) );
							String confirm = PlayUpActivity.context.getResources().getString( R.string.confirm );
							String cancel = PlayUpActivity.context.getResources().getString(R.string.cancel );

							alertDialog.setButton( confirm, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									if(PlayUpActivity.context!= null ) {
										try{

											Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(apk_path));
											PlayUpActivity.context.startActivity(myIntent);
										}catch (Exception e) {

											try {
												PlayUpActivity.context.startActivity(new Intent(Intent.ACTION_VIEW, 
														Uri.parse( Keys.MARKET_URL ) ) );
											}catch (Exception e1) {

											}
										}
									}
								}

							}); 

							alertDialog.setButton2( cancel, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {

									if ( alertDialog != null ) {
										alertDialog.cancel();
									}
								}
							});

							if ( alertDialog != null && !alertDialog.isShowing() ) {
								alertDialog.show();
							}

						}
					} catch (NotFoundException e) {
						//Logs.show ( e );
					}catch (Exception e) {

						//Logs.show ( e );
					} catch ( Error e ) {
						//Logs.show ( e );
					}
				}
			} );

		}


	}
	
	
//	public static void callRefreshOnFragments( Message msg) {
//		if ( fragmentManagerUtil != null ) {
//			fragmentManagerUtil.onRefresh( msg );
//		} else {
//			fragmentManagerUtil = new FragmentManagerUtil();
//			fragmentManagerUtil.onRefresh( msg );
//		}
//	}

}
