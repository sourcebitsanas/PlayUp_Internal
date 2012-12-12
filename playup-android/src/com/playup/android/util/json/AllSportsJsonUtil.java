package com.playup.android.util.json;

import org.json.JSONException;
import org.json.JSONObject;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;

/**
 * Parsing the competition data. 
 */
public class AllSportsJsonUtil {

	private final String SELF_KEY          	=  ":self";
	private final String HREF_URL_KEY       =  ":href";
	private final String TYPE_KEY 			= ":type";
	private final String UID_KEY         	=  ":uid";

	private final String ITEMS_KEY          =  "items";

	private final String LOGO_KEY           =  "logos";
	private final String DENSITY_KEY        =  "density";
	private final String HREF_KEY           =  "href";

	private final String LIVE_KEY           =  "live";

	private final String FEATURE_KEY        =  "feature";
	private final String TILE_KEY           =  "tile";
	
	private final String HREF_LINK_KEY = ":href";

	// competition_current_round

	private final String LIVE_CONTEST_KEY   = "live_contests";



	private final String NAME_KEY 			= "name";
	private final String COMPETITION_KEY 	= "competitions";

	
	


	private boolean inTransaction = false;
	
	
	public AllSportsJsonUtil ( JSONObject str, boolean inTransaction ) {

		this.inTransaction = inTransaction;
		if ( str != null ) {
			parseData ( str );
		}
	}

	private void parseData ( JSONObject jsonObj ) {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
		
		if ( !inTransaction ){
			
			dbUtil.getWritabeDatabase().beginTransaction();
		}
		//}
		
			
		try {
			

			
			
			if(!jsonObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.SPORTS_DATA_TYPE))
				return;
			
			String selfUrl = jsonObj.optString( SELF_KEY );
			
			String hrefUrl = jsonObj.optString(HREF_LINK_KEY);
			
			
			dbUtil.setHeader ( hrefUrl,selfUrl, jsonObj.getString( TYPE_KEY ), false  );

			
			new Util ().setColor ( jsonObj );

			for ( int i = 0, len = jsonObj.getJSONArray( ITEMS_KEY ).length(); i  < len ; i++ ) {

				JSONObject jObj = jsonObj.getJSONArray( ITEMS_KEY ).getJSONObject( i );

				String vSelfUrl = jObj.optString( SELF_KEY );

				String vSportsHrefUrl = jObj.optString( HREF_LINK_KEY );
				String uid      = jObj.getString( UID_KEY );
				String type 	= jObj.getString( TYPE_KEY );

				dbUtil.setHeader( vSportsHrefUrl,vSelfUrl, type, false  );


				// setting the sports data 
				String name     = jObj.getString(  NAME_KEY );
				
				//JSONArray featureLogojArr = jObj.getJSONObject( LOGO_KEY ).getJSONArray( FEATURE_KEY );
				int featureLogojArr_len = jObj.getJSONObject( LOGO_KEY ).getJSONArray( FEATURE_KEY ).length();
				String vFeatureLogoUrl = null;
				for ( int j = 0; j < featureLogojArr_len; j++ ) {

					JSONObject logoArr_jObj = jObj.getJSONObject( LOGO_KEY ).getJSONArray( FEATURE_KEY ).getJSONObject( j );

					String density = logoArr_jObj.getString( DENSITY_KEY );
					
					if ( Constants.DENSITY.equalsIgnoreCase( density ) ) {
						vFeatureLogoUrl = logoArr_jObj.getString( HREF_KEY );
					}
				}

				//JSONArray tileLogojArr = jObj.getJSONObject( LOGO_KEY ).getJSONArray( TILE_KEY );
				int tileLogojArr_len = jObj.getJSONObject( LOGO_KEY ).getJSONArray( TILE_KEY ).length();
				String vTileLogoUrl = null;
				for ( int j = 0; j < tileLogojArr_len; j++ ) {

					JSONObject logoArr_jObj = jObj.getJSONObject( LOGO_KEY ).getJSONArray( TILE_KEY ).getJSONObject( j );

					String density = logoArr_jObj.getString( DENSITY_KEY );

					if ( Constants.DENSITY.equalsIgnoreCase( density ) ) {
						vTileLogoUrl = logoArr_jObj.getString( HREF_KEY );

					}
				}


				// getting the competition data
				JSONObject competitionJObj  = jObj.getJSONObject( COMPETITION_KEY );

				String vCompetitionUrl = competitionJObj.optString( SELF_KEY );

				String vCompetitionHrefUrl = competitionJObj.optString( HREF_LINK_KEY );

				String vCompetitionUid = competitionJObj.getString( UID_KEY );
				String vCompetitionType  = competitionJObj.getString( TYPE_KEY );

				
				dbUtil.setHeader( vCompetitionHrefUrl,vCompetitionUrl, vCompetitionType, false );

				// gettting the live data
				JSONObject  livejObj = jObj.getJSONObject( LIVE_KEY );

				String vLiveUrl = livejObj.optString( SELF_KEY );

				String vSportLiveHrefUrl = livejObj.optString( HREF_LINK_KEY );
				

				String vLiveUid = livejObj.getString( UID_KEY );
				String vLiveType  = livejObj.getString( TYPE_KEY );

				dbUtil.setHeader( vSportLiveHrefUrl,vLiveUrl, vLiveType, false );

				// getting the live contest numbers
				int live_contests = jObj.getInt( LIVE_CONTEST_KEY );


				dbUtil.setSportsCompetition ( uid, vCompetitionUid, vCompetitionUrl,vCompetitionHrefUrl );
				dbUtil.setSportsLive ( uid, vLiveUid, vLiveUrl,vSportLiveHrefUrl );
				dbUtil.setSports(i+1, uid,type, vSelfUrl, name, vFeatureLogoUrl, 
						vTileLogoUrl, live_contests,  vCompetitionUid, vLiveUid,vSportsHrefUrl );

				
				
			}
			
			
		
		
		}
		catch (JSONException e) {
			  
		}catch (Exception e) {
			// TODO: handle exception
			  
		}catch (Error e) {
			// TODO: handle exception
			  
		}
		
		finally{
			
			new Util().releaseMemory(jsonObj);
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			
			if ( !inTransaction ){
				
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
			//}
				
			}	
		}
	}
	
	
	
	
}
