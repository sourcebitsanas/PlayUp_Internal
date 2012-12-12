package com.playup.android.util.json;

import org.json.JSONException;
import org.json.JSONObject;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;


public class NotificationJsonUtil {

	private final String UID_KEY           =  ":uid";
	private final String SELF_KEY          =  ":self";
	private final String HREF_URL_KEY          =  ":href";
	private final String TYPE_KEY          =  ":type";

	
	private final String NOTIFICATION_KEY = "notifications";
	

	private boolean inTransaction = false;
	private final String iUserId;

	public NotificationJsonUtil ( JSONObject jsonObj, String iUserId, boolean inTransaction ) {
		this.inTransaction = inTransaction;

		this.iUserId = iUserId;
		if ( jsonObj != null ) {
			parseData ( jsonObj );
		}
	}

	private void parseData ( JSONObject jsonObj) {
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
		Logs.show ( "begin ------------------------------------NotificationJsonUtil "+jsonObj);
		if ( !inTransaction ) {
			
		
//		Logs.show ( "begin ------------------------------------NotificationJsonUtil ");
			dbUtil.getWritabeDatabase().beginTransaction();
		}
		
		try {
			
			//JSONObject jsonObj = new JSONObject( str );

			jsonObj = jsonObj.getJSONObject( NOTIFICATION_KEY );
			if(!jsonObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.NOTIFICATIONS_DATA_TYPE))
				return;
			String recent_url = jsonObj.optString( SELF_KEY );
			String recent_href_url = jsonObj.optString( HREF_URL_KEY );
			dbUtil.setHeader ( recent_href_url,recent_url, jsonObj.getString( TYPE_KEY ) , false   );
			
			/*dbUtil.setNotificationUrl ( recent_url, iUserId );*/
			dbUtil.setNotificationUrl ( recent_url,recent_href_url, iUserId );
				
			
		} catch (JSONException e) {
			Logs.show(e);
		}   finally {
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			/**
			 * Cleaning Memory
			 */
			new Util().releaseMemory(jsonObj);
			
			if ( !inTransaction ) {
				
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( "end ------------------------------------NotificationJsonUtil ");
			 }
		}
	}
}
