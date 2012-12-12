/*
 * Android ASO Tracking Code
 *
 * Version: 1.2
 *
 * Copyright (C) 2011 Fiksu Incorporated
 * All Rights Reserved. 
 *
 */

package com.playup.android.receiver;

import java.net.URLDecoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.mobileapptracker.Tracker;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Keys;

public class InstallTracking extends BroadcastReceiver {
	
	public void onReceive(Context context, Intent intent) {
		/**
		 * 
		 * Need to check whether we need to track this  for Fiksu or GA
		 */

		String referrer = intent.getStringExtra("referrer");
		if(referrer!=null){


			/**
			 * Sending TO Fiksu. With Condition Check. fiksu_adid on referrer Strin. 
			 * Currently Commenting out Client has requested. 
			 */

			//			if(referrer.contains("fiksu_adid")){
			//				uploadConversionEvent(context, intent);
			//				forwardToOtherReceivers(context, intent);
			//			}

			/**
			 * 
			 * VServ Tracking
			 */

//			if((referrer!=null && referrer.toLowerCase().startsWith("vserv")) || ( referrer != null && referrer.contains("vserv") ) )
//			{
//				VservInstallReceiver vservInstallReceiver=new VservInstallReceiver();
//				vservInstallReceiver.onReceive(context,intent);
//			}
			
			
			/*
			 * Mobile App Tracking.
			 */
			Tracker mTracker	=	new Tracker();
		
			mTracker.onReceive(context,intent);

			/**
			 * Sending TO GA. Without Any Condition Check.
			 */
			GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();

			tracker.startNewSession( Keys.GOOGLE_ANALYTICS_TRACKING_ID, 10,PlayupLiveApplication.getInstance().getApplicationContext());

			if (referrer != null) {
				referrer = URLDecoder.decode(referrer);
			} 
			tracker.setReferrer(referrer);
			tracker.setCustomVar(1, "Medium", "Mobile App");
			tracker.trackPageView("Playup");
			tracker.setDebug(true);
			tracker.dispatch();

			
			
		} else {
			Tracker mTracker	=	new Tracker();
			
			mTracker.onReceive(context,intent);
		}
	}

}

