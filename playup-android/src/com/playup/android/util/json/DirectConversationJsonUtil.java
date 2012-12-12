package com.playup.android.util.json;

import org.json.JSONArray;


import org.json.JSONException;

import org.json.JSONObject;



import android.util.Log;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;


public class DirectConversationJsonUtil {

	private final String UID_KEY           	=  ":uid";
	private final String SELF_KEY          	=  ":self";
	private final String HREF_URL_KEY          	=  ":href";
	private final String TYPE_KEY          	=  ":type";

	
	private final String ITEM_KEY = "items";
	private final String TOTAL_COUNT_KEY    = "total_count";
	private final String UNREAD_COUNT    = "unread_message_count";
	
	private String vUserId = null;
	private String directConversationPushId = null;

	private boolean inTransaction = false; 
	
	public DirectConversationJsonUtil ( String str, String vUserId, boolean inTransaction ) {
	
		this.inTransaction = inTransaction;
		this.vUserId = vUserId;
		if ( str != null && str.trim().length() > 0 ) {

			parseData ( str );
			
			str = null;
		}
	}

	private void parseData ( String  str ) {
		JSONObject jObj = null;
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
			
		
		
		if ( !inTransaction ) {
		
			
			dbUtil.getWritabeDatabase().beginTransaction();
			
		}
			// }
		try {


			jObj = new JSONObject( str );
			if(!(jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.DIRECT_CONVERSATION_LOBBY_DATA_TYPE)))
				return;
			String vDirectConversationUrl = jObj.optString( SELF_KEY );
			String vDirectConversationHrefUrl = jObj.optString( HREF_URL_KEY );
			String vDirectConversationId  = jObj.getString( UID_KEY );
			dbUtil.setHeader(vDirectConversationHrefUrl, vDirectConversationUrl , jObj.getString( TYPE_KEY ), false  );
			new Util().setColor(jObj);
			

			Log.e("123","DirectConversationJsonUtil >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");

			int total_count = jObj.getInt( TOTAL_COUNT_KEY );
			int unread_count = jObj.getInt( UNREAD_COUNT );

			
			// update the usertable in order to show the latest count
			/*dbUtil.setUserDirectConversation ( vDirectConversationId, vDirectConversationUrl,total_count, unread_count, vUserId );*/
			dbUtil.setUserDirectConversation ( vDirectConversationId, vDirectConversationUrl,vDirectConversationHrefUrl,total_count, unread_count, vUserId );

			
			
			JSONArray item_arr = jObj.getJSONArray( ITEM_KEY );
			
			int len = item_arr.length();
			for ( int i = 0; i < len; i++) {
				
				JsonUtil json = new JsonUtil();
				json.setDirectConversationId ( vDirectConversationId ) ; 
				
					
				
					json.setDirectConversationUrl ( null );
					json.setIsDirectConversationUrlHref ( false );
					
				
				json.setDirectConversationUrl ( null ) ; 
				json.setUserId( vUserId );
				json.setDirectConvesationPushId(null);
				
				json.parse( new StringBuffer( item_arr.getString( i ) ), Constants.TYPE_DIRECT_MESSAGES_JSON, true );
				
				
			}
			item_arr = null;
			
		} catch (JSONException e) {
			
			Logs.show(e);

		}  finally {
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			/**
			 * Cleaning Memory
			 */
			new Util().releaseMemory(jObj);
			jObj	= null;
			if ( !inTransaction ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
				
				
			}
			// }
		}
	}
}
