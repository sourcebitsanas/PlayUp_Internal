package com.playup.android.database;

import java.io.FileOutputStream;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.mobileapptracker.MobileAppTracker;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.Keys;
import com.playup.android.util.Logs;
import com.playup.android.util.PreferenceManagerUtil;
import com.playup.android.util.Util;

public class DatabaseControl extends SQLiteOpenHelper {

	//private  final boolean DATABASE_EXISTS 		= true;
	public static final boolean  DATABASE_EXISTS 		= true;
	private final boolean DATABASE_NOT_EXISTS 	= false;

	public DatabaseControl ( ) {

		super( PlayupLiveApplication.getInstance(), Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION );

		PreferenceManagerUtil preferenceManagerUtil = new PreferenceManagerUtil();
		// check if the database already exists in application or not.


		if ( !preferenceManagerUtil.get(  Keys.IS_DATABASE_EXISTS , DATABASE_NOT_EXISTS ) )  {

			// create the database in the application
			SQLiteDatabase db = PlayupLiveApplication.getInstance().openOrCreateDatabase ( Constants.DATABASE_NAME , 0 , null); 
			
			db.setLockingEnabled(false);
			Constants.DATABASE_PATH  = db.getPath();
			db.close();
			db = null;

			// copy the structure of database from asset into the database that just got created in the application.
			copyDatabase();

		}
		
		preferenceManagerUtil = null;
	}

	/**
	 * Copies the database from the asset folder in the database of application. 
	 */
	private void copyDatabase  () {
		CopyFromAsset();
		// PlayupLiveApplication.getThreadPoolExecutor().execute( copyDatabaseRunnable );
	}

	@Override
	public void onCreate(SQLiteDatabase db) {}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		// left for later purpose if needed.
	}



	/**
	 * Runnable for copying the database in the application . 
	 */
	private Runnable copyDatabaseRunnable = new Runnable() {
		@Override
		public void run() {
			/*try {
				Thread.sleep( 4000 );
			} catch (InterruptedException e) {
				Logs.show ( e );
			}*/
			CopyFromAsset();
		}
	};


	private void CopyFromAsset () {


		try {
			
			/**
			 * Sending TO GA. Without Any Condition Check.
			 */
			
			
			GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();

			tracker.startNewSession( Keys.GOOGLE_ANALYTICS_TRACKING_ID, 10,PlayupLiveApplication.getInstance().getApplicationContext());
			tracker.setCustomVar(1, "Medium", "Mobile App");
			tracker.trackPageView("First Launch");
			
			tracker.setDebug(true);
			tracker.dispatch();

			
			/**
			 * tracking first launch here Event name: First Launch and Id:11881
			 */
			try{
			MobileAppTracker mobileAppTracker = new MobileAppTracker(PlayupLiveApplication.getInstance().getApplicationContext());
			mobileAppTracker.trackAction("11881");
			}catch(Exception e){
				
			}

			// copies the stream
			Util.copyStreams( PlayupLiveApplication.getInstance().getAssets().open(  Constants.DATABASE_NAME + ".mp3" ) , new FileOutputStream( Constants.DATABASE_PATH ) );

			// set the values in shared preferences.
			PreferenceManagerUtil preferenceManagerUtil = new PreferenceManagerUtil();
			preferenceManagerUtil.set (  Keys.IS_DATABASE_EXISTS, DATABASE_EXISTS);
			preferenceManagerUtil = null;
			
			
			

		} catch (Exception e)  {
			Logs.show( e ); 
		} catch ( Error e ) {
			Logs.show ( e );
		}


	}
}
