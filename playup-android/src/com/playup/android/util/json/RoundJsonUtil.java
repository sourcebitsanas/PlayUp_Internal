package com.playup.android.util.json;

import org.json.JSONObject;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;

/**
 * Parsing the competition data. 
 */
public class RoundJsonUtil {

	private final String UID_KEY           = ":uid";
	private final String SELF_KEY          = ":self";
	private final String TYPE_KEY 		   = ":type";

	private final String PERIOD_KEY 	   = "period";
	private final String NAME_KEY 		   = "name";
	private final String HREF_LINK_KEY = ":href";

	private final String START_KEY 		   = "start_date";
	private final String END_KEY 		   = "end_date";
	private final String CONTEST_KEY 	   = "contests";


	private String vCompetitionRoundId = null;

	private String vRoundContestId = null;


	private String vRoundId = null;
	private boolean inTransaction = false; 
	
	public RoundJsonUtil ( JSONObject str , boolean inTransaction ) {

		this.inTransaction = inTransaction;
		if ( str != null ) {
			parseData ( str );
		}
	}

	public RoundJsonUtil ( JSONObject str, String vCompetitionRoundId, boolean inTransaction  ) {


		this.inTransaction = inTransaction;
		this.vCompetitionRoundId = vCompetitionRoundId;
		if ( str != null) {
			parseData ( str );
		}
	}

	private void parseData ( JSONObject jsonObj ) {


		DatabaseUtil dbUtil = DatabaseUtil.getInstance();

		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
		
		if ( !inTransaction ) {
//			Logs.show ( "begin ------------------------------------RoundJsonUtil ");
			dbUtil.getWritabeDatabase().beginTransaction();
			
		}
		// }

		try {




			String vRoundUrl = jsonObj.optString( SELF_KEY );
			
			String vRoundHrefUrl = jsonObj.optString( HREF_LINK_KEY );
			vRoundId  = jsonObj.getString( UID_KEY );
			if(!jsonObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.ROUND_DATA_TYPE))
				return;
			// setting the round url's header ( content type )
			dbUtil.setHeader( vRoundHrefUrl,vRoundUrl, jsonObj.getString( TYPE_KEY ), false   );
			String period = jsonObj.getString( PERIOD_KEY );
			String name = jsonObj.getString( NAME_KEY );
			String dStartDate = jsonObj.getString( START_KEY );
			String dEndDate = jsonObj.getString( END_KEY );

			
			new Util().setColor(jsonObj);

			JSONObject contests_jObj = jsonObj.getJSONObject( CONTEST_KEY );
			
			if(contests_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
			String vRoundContestUrl = contests_jObj.optString( SELF_KEY );
			
			String vRoundContestHrefUrl = contests_jObj.optString( HREF_LINK_KEY );
			vRoundContestId  = contests_jObj.getString( UID_KEY );			
			// setting the round contest url's header ( content type )
			dbUtil.setHeader( vRoundContestHrefUrl,vRoundContestUrl, contests_jObj.getString( TYPE_KEY ) , false  );

			dbUtil.setRoundContestData ( vRoundContestId, vRoundContestUrl,vRoundContestHrefUrl );
			}

			// setting the round data .
			dbUtil.setRoundData ( vRoundId, vRoundUrl, period, name, dStartDate, dEndDate, vRoundContestId, vCompetitionRoundId,vRoundHrefUrl );
			

		} catch (Exception e) {
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
//				Logs.show ( "end ------------------------------------RoundJsonUtil ");
				
			}
			// }


		}
	}

	public String getRoundContestId () {
		return vRoundContestId;
	}

	public String getRoundId () {
		return vRoundId;
	}
}
