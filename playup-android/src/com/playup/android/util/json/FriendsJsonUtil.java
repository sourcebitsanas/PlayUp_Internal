package com.playup.android.util.json;

import org.json.JSONException;
import org.json.JSONObject;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;


public class FriendsJsonUtil {

	private final String UID_KEY           =  ":uid";
	private final String SELF_KEY          =  ":self";
	private final String HREF_URL_KEY          =  ":href";
	private final String TYPE_KEY          =  ":type";

	
	private final String FRIENDS_KEY 		= "friends";
	
	

	private final String iUserId;
private boolean inTransaction = false;
	public FriendsJsonUtil ( JSONObject jsonObj, String iUserId, boolean inTransaction ) {

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
		
		Logs.show ( "begin ------------------------------------FriendsJsonUtil "+jsonObj);
		if ( !inTransaction ) {
			
	/*	Logs.show ( "begin ------------------------------------FriendsJsonUtil ");*/
			dbUtil.getWritabeDatabase().beginTransaction();
		}
			
	
		try {
			
			//JSONObject jsonObj = new JSONObject( str );

			jsonObj = jsonObj.getJSONObject( FRIENDS_KEY );
			if(!(jsonObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.PLAYUP_FRIENDS_DATA_TYPE)))
				return;
			String friend_url = jsonObj.optString( SELF_KEY );
			String friend_href_url = jsonObj.optString( HREF_URL_KEY );
			dbUtil.setHeader ( friend_href_url,friend_url, jsonObj.getString( TYPE_KEY ) , false   );
		/*	dbUtil.setFriendsUrl ( friend_url, iUserId );*/
			dbUtil.setFriendsUrl ( friend_url,friend_href_url, iUserId );
				
			
		} catch (JSONException e) {
			Logs.show(e);
		} finally {
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			
			/**
			 * Cleaning Memory
			 */
			new Util().releaseMemory(jsonObj);
			if ( !inTransaction ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( "end ------------------------------------FriendsJsonUtil ");
				
			}
			// }
		}
	}
}
