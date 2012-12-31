package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Util;

/**
 * Context data parsing  
 */
public class ContextJsonUtil {
	
	
	
	private final String ITEMS_KEY 	=  "items";
	private final String SELF_KEY 	=  ":self";
	
	
	private final String TYPE_KEY 	= ":type";
	
private boolean inTransaction= false;
	
	
	public ContextJsonUtil ( JSONObject jsonObj, boolean inTransaction) {
		
		this.inTransaction = inTransaction;
		if ( jsonObj != null) {
			parseData ( jsonObj );
		}
	}
	
	private void parseData ( JSONObject jsonObj ) {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
		
		if ( !inTransaction ) {
			
			dbUtil.getWritabeDatabase().beginTransaction();
			
		}
		//}
		try {
			
			//JSONObject jsonObj = new JSONObject( str );
			// get items
			JSONArray jArr = jsonObj.getJSONArray( ITEMS_KEY );
			
			int count = jArr.length();
			
			for ( int i = 0; i < count ; i++ ) {
				JSONObject jObj = jArr.getJSONObject( i );
				String context_url = jObj.getString( SELF_KEY );
				dbUtil.setHeader ( context_url, jObj.getString( TYPE_KEY ), false  );
				// save context url in database.
				dbUtil.setContext ( context_url );
				
			}
			
			
			
		} catch (JSONException e) {
			 // Logs.show(e);
		} finally {
			
			/**
			 * Cleaning Memory
			 */
			new Util().releaseMemory(jsonObj);
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			if ( !inTransaction ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
				
				
			}
			//}
		}
	}
}
