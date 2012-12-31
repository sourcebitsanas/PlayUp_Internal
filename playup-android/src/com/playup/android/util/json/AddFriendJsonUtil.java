package com.playup.android.util.json;




import org.json.JSONObject;

import android.content.ContentValues;


import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Types;
import com.playup.android.util.Util;

/**
 * Parsing the competition data. 
 */
public class AddFriendJsonUtil {

	private final String UID_KEY           =  ":uid";
	private final String SELF_KEY          =  ":self";
	private final String HREF_URL_KEY          =  ":href";
	private final String TYPE_KEY 		   = ":type";
	private final String STATUS_KEY 		   = "status";
	
	
	private boolean inTransaction = false;
	public AddFriendJsonUtil ( String str , boolean inTransaction ) {
	///	Log.e("234","AddFriendJsonUtil==============>>>"+str);
		this.inTransaction = inTransaction;
		if ( str != null && str.trim().length() > 0 ) {
			parseData ( str );
		}
	}

	private void parseData ( String str ) {

		
		JSONObject jsonObj  = null;
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y < 480 && Constants.DPI <= 160 ) {
		} else {
		*/	
		if ( !inTransaction ) {
			dbUtil.getWritabeDatabase().beginTransaction();
		}
		//}
		try {

			jsonObj = new JSONObject( str );
			if(!jsonObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.FRIENDSHIP_STATUS_DATA_TYPE))
				return;

			String vFriendshipStatusUrl = jsonObj.optString( SELF_KEY );
			String vFriendshipStatusHrefUrl = jsonObj.optString( HREF_URL_KEY );
			String vFriendshipStatusId  = jsonObj.getString( UID_KEY );

			dbUtil.setHeader(vFriendshipStatusUrl,  jsonObj.getString( TYPE_KEY ), false );
			
			String status = jsonObj.getString(  STATUS_KEY );
			
			ContentValues values = new ContentValues ();
			values.put( "vFriendshipStatusId", vFriendshipStatusId );
			values.put( "vFriendshipStatusUrl", vFriendshipStatusUrl );
			values.put( "vFriendshipStatusHrefUrl",  vFriendshipStatusHrefUrl);
			values.put( "status", status );
			
			/*PlayupLiveApplication.getDatabaseWrapper().queryMethod2( Constants.QUERY_UPDATE, null, "user", values, " vFriendshipStatusId = \"" + vFriendshipStatusId + "\" " );*/
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2( Constants.QUERY_UPDATE, null, "user", values, " vFriendshipStatusId = \"" + vFriendshipStatusId + "\" " );
			
			
		} catch (Exception e) {
			  
		} finally {
			/*if ( Constants.X <= 320 && Constants.Y < 480 && Constants.DPI <= 160 ) {
			} else {*/
			new Util().releaseMemory(jsonObj);
			if ( !inTransaction ) {
				
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
			//}
				
				
			}
				
		}
	}

}
