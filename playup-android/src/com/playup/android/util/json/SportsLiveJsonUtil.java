package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.os.Bundle;
import android.os.Message;

import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Types;
import com.playup.android.util.Util;

/**
 * Parsing the competition data. 
 */
public class SportsLiveJsonUtil {

	private final String UID_KEY           =  ":uid";
	private final String SELF_KEY          =  ":self";
	private final String ITEM_KEY          =  "items";
	private final String TYPE_KEY          =  ":type";
	private final String HREF_LINK_KEY = ":href";

	private final String NAME_KEY          =   "name";
	private final String SHORT_NAME_KEY    =   "short_name";
	private final String REGION_KEY        =   "region";
	private final String LOGO_KEY          =   "logos";
	private final String DENSITY_KEY       =   "density";
	private final String HREF_KEY          =   "href";

	
	private boolean inTransaction = false;
	
	public SportsLiveJsonUtil ( JSONObject jObj, boolean inTransaction  ) {

		
		this.inTransaction = inTransaction;
		if ( jObj != null ) {
			parseData ( jObj );
		}
	}




	private void parseData ( JSONObject jObj ) {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
		
		if ( !inTransaction ){
//			
//			Logs.show ( "begin ------------------------------------SportsLiveJsonUtil ");
			dbUtil.getWritabeDatabase().beginTransaction();
		}
		// }
		try {
			if(!jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE))
				return;
			String vSportsLiveId = jObj.getString( UID_KEY );
			dbUtil.setHeader( jObj.optString( HREF_LINK_KEY ),jObj.optString( SELF_KEY ), jObj.getString( TYPE_KEY ), false  );
			
			
			new Util().setColor(jObj);
			
			JSONArray items = jObj.getJSONArray( ITEM_KEY );
			
			
			int len = items.length();
			
			ContentValues values = new ContentValues();
			values.put( "vSportsLiveId", "" );
			
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2( Constants.QUERY_UPDATE, null, "competition_live", values, " vSportsLiveId = \"" + vSportsLiveId + "\" ");
			

			for ( int i = 0; i < len;  i++ ) {
				
				
				
					
					
				
				JSONObject items_jObj = items.getJSONObject( i );
				if(!(items_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.LIVE_DATA_TYPE)))
					continue;
				
				String vCompetitionLiveId = items_jObj.getString( UID_KEY );
				String vCompetitionLiveUrl = items_jObj.optString( SELF_KEY );
				String vCompetitionLiveHrefUrl = items_jObj.optString(HREF_LINK_KEY );
				dbUtil.setHeader( vCompetitionLiveHrefUrl,vCompetitionLiveUrl, items_jObj.getString( TYPE_KEY ) , false  );
				
				String name = items_jObj.getString( NAME_KEY );
				String short_name = items_jObj.getString( SHORT_NAME_KEY );
				String region = items_jObj.getString( REGION_KEY );
				
				JSONArray logo_jArr = items_jObj.getJSONArray( LOGO_KEY );
				String vLogoUrl = null;
				for ( int j = 0, len_j = logo_jArr.length(); j < len_j; j++ ) {
					
					JSONObject logo_jObj = logo_jArr.getJSONObject( j );
					if ( logo_jObj.getString( DENSITY_KEY ).equalsIgnoreCase( Constants.DENSITY ) ) {
						
						vLogoUrl = logo_jObj.getString( HREF_KEY );
					}
					
				}
				
				
				
				values = new ContentValues();
				values.put( "vCompetitionLiveId", "" );
				PlayupLiveApplication.getDatabaseWrapper().queryMethod2( Constants.QUERY_UPDATE, null, "contests", values, " vCompetitionLiveId = \"" + vCompetitionLiveId + "\" ");
				
				String vCompetitionId = null;
				
				boolean isSet = false;
				JSONArray content_jArr = items_jObj.getJSONArray( ITEM_KEY );
				for ( int k = 0, len_k = content_jArr.length(); k < len_k; k++ ) {
					
					// fetching the contest data.
					JsonUtil json = new JsonUtil();
					json.setCompetitionLiveId ( vCompetitionLiveId );
					json.parse( new StringBuffer( content_jArr.getJSONObject( k ).toString() ) , Constants.TYPE_CONTEST_JSON, true );
					
					vCompetitionId = json.getCompetitionId();
					
					if ( k == 0 ) {
						// set the competition data 
						dbUtil.setCompetitionLiveData ( vSportsLiveId, vCompetitionLiveId, vCompetitionLiveUrl, vCompetitionId, name, short_name, region, vLogoUrl,vCompetitionLiveHrefUrl);
						
						isSet = true;
					}
					dbUtil.setCompetitionLiveData ( vSportsLiveId, vCompetitionLiveId, vCompetitionLiveUrl, vCompetitionId, name, short_name, region, vLogoUrl ,vCompetitionLiveHrefUrl);
					
					
				
					
					
				}
				
				if ( !isSet ) {
					// set the competition data 
					dbUtil.setCompetitionLiveData ( vSportsLiveId, vCompetitionLiveId, vCompetitionLiveUrl, vCompetitionId, name, short_name, region, vLogoUrl,vCompetitionLiveHrefUrl );
				}
				
				
				
				
			}
						
			
			
			
		} catch (JSONException e) {
		//	 Logs.show(e);
		} finally {
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			/**
			 * Cleaning Memory
			 */
			new Util().releaseMemory( jObj );

			
			if ( !inTransaction ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( "end ------------------------------------SportsLiveJsonUtil ");
				
			}
			// }
		}
	}
}
