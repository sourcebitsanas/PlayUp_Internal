package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Message;

import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;

/**
 * Parsing the competition data. 
 */
public class RoundContestJsonUtil {

	private final String UID_KEY           =  ":uid";
	private final String SELF_KEY          =  ":self";
	private final String TYPE_KEY 		   = ":type";
	private final String HREF_LINK_KEY = ":href";

	private final String ITEM_KEY 	   		= "items";

	private boolean inTransaction = false;
	public RoundContestJsonUtil ( JSONObject jsonObj, boolean inTransaction ) {

		this.inTransaction= inTransaction;
		if ( jsonObj != null  ) {
			parseData ( jsonObj );
		}
	}

	private void parseData ( JSONObject jsonObj) {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
		if ( !inTransaction ) {
//			Logs.show ( "begin ------------------------------------RoundContestJsonUtil ");
			dbUtil.getWritabeDatabase().beginTransaction();
			
		}
		// }
		try {
			//JSONObject jsonObj = new JSONObject( str );
			

			
			

		
			
			if(!jsonObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE))
				return;
			
			String vRoundContestUrl = jsonObj.optString( SELF_KEY );
			
			String vRoundContestHrefUrl = jsonObj.optString( HREF_LINK_KEY );
			String vRoundContestId  = jsonObj.getString( UID_KEY );
			
			new Util().setColor(jsonObj);
			
			// setting the header ( content type )
			dbUtil.setHeader( vRoundContestHrefUrl,vRoundContestUrl, jsonObj.getString( TYPE_KEY ), false   );
			JSONArray jArr = jsonObj.getJSONArray( ITEM_KEY );
			int len = jArr.length();
			for ( int i = 0; i < len; i++ ) {
				
				JsonUtil json = new JsonUtil();
				
				json.setRoundContestId( vRoundContestId );
				json.parse( new StringBuffer( jArr.getJSONObject( i ).toString() ), Constants.TYPE_CONTEST_JSON, true );
		
			}

			

		} catch (JSONException e) {
			 
		}finally{
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			/**
			 * Cleaning Memory
			 */
			new Util().releaseMemory(jsonObj);
			
			if ( !inTransaction ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( "end ------------------------------------RoundContestJsonUtil ");
				
			}
			// }
		}
	}

}

