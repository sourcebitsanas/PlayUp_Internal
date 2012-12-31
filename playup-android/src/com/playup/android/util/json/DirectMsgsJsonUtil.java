package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Types;
import com.playup.android.util.Util;


public class DirectMsgsJsonUtil {

	private final String UID_KEY           	=  ":uid";
	private final String SELF_KEY          	=  ":self";
	private final String HREF_URL_KEY          	=  ":href";
	private final String TYPE_KEY          	=  ":type";

	private final String ITEM_KEY = "items";
	private final String TOTAL_COUNT = "total_count";
	private final String VERSION = ":version";
	
	private final String ADDITION_KEY = "additions";
	private final String MARKER_KEY  = "marker";

	private String vDirectMessageId = null;
	private boolean inTransaction = false;
	private boolean fromGap = false;

	public DirectMsgsJsonUtil ( String str, boolean inTransaction, String vDirectMessageId, boolean fromGap ) {


		this.fromGap = fromGap;
		this.vDirectMessageId = vDirectMessageId;
		this.inTransaction = inTransaction;
		if ( str != null && str.trim().length() > 0 ) {
			parseData ( str );

			str = null;
		}
	}

	private void parseData ( String  str ) {
		JSONObject jObj = null;
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();

		if ( !inTransaction ) {
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
				dbUtil.getWritabeDatabase().beginTransaction();
				
			// }
		}
		try {



			jObj = new JSONObject( str );
			if(!(jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)))
				return;
			String vDMessageUrl = jObj.optString( SELF_KEY );
			String vDMessageHrefUrl = jObj.optString( HREF_URL_KEY );
			String vDMessageId  = jObj.getString( UID_KEY );
			dbUtil.setHeader(vDMessageHrefUrl, vDMessageUrl , jObj.getString( TYPE_KEY ), false  );
			new Util().setColor(jObj);
			
			
			String vAdditionUrl = "",vMarkerUrl = "",vAdditionHrefUrl="",vMarkerHrefUrl="";
			JSONObject additition_jObj = jObj.getJSONObject( ADDITION_KEY );
			if(!(additition_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE))){
			 vAdditionUrl = additition_jObj.optString( SELF_KEY );
			 vAdditionHrefUrl = additition_jObj.optString( HREF_URL_KEY );
			dbUtil.setHeader( vAdditionHrefUrl,vAdditionUrl, additition_jObj.getString( TYPE_KEY ), false );
			additition_jObj = null;
			}


			JSONObject marker_jObj = jObj.getJSONObject( MARKER_KEY );
			if(!(marker_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE))){
			 vMarkerUrl = marker_jObj.optString( SELF_KEY );
			 vMarkerHrefUrl = marker_jObj.optString( HREF_URL_KEY );
			dbUtil.setHeader( vMarkerHrefUrl,vMarkerUrl, marker_jObj.getString( TYPE_KEY ), false );
			marker_jObj = null;
			}

			int total_count  = jObj.getInt( TOTAL_COUNT );
			
			int version = 0;
			
			if ( jObj.has( VERSION ) ) {
				version = jObj.getInt( VERSION );
			}

			/*dbUtil.setDirectMessages ( vDMessageUrl,vDMessageHrefUrl, vDMessageId, vAdditionUrl, vMarkerUrl, total_count, version, vDirectMessageId );*/
			dbUtil.setDirectMessages ( vDMessageUrl,vDMessageHrefUrl, vDMessageId, vAdditionUrl,vAdditionHrefUrl, vMarkerUrl,vMarkerHrefUrl, total_count, version, vDirectMessageId );
			
			JSONArray item_jarr = jObj.getJSONArray( ITEM_KEY );
			int len = item_jarr.length();

			for ( int i = 0; i < len; i++ ) {

				JsonUtil json = new JsonUtil();
				json.setDMessageId ( vDMessageId );
				if ( !fromGap ) {
					json.setShouldDelete ( true );
				}
				
				json.parse( new StringBuffer( item_jarr.getString( i ) ), Constants.TYPE_DIRECT_MESSAGES_ITEM_JSON, true );

			}


			


		} catch (JSONException e) {

			//Logs.show( e) ;
		}  finally {
			new Util().releaseMemory( jObj );
			if ( !inTransaction ) {
				/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
				} else {*/
					dbUtil.getWritabeDatabase().setTransactionSuccessful();
					dbUtil.getWritabeDatabase().endTransaction();
					
				// }
			}
		}
	}
}
