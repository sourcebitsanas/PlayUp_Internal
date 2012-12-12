package com.playup.android.util.json;




import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class CurrentSeasonJsonUtil {

	private  final String ROUNDS_KEY = "rounds";
	private final String UID_KEY           	=  ":uid";
	private final String SELF_KEY          	=  ":self";
	private final String TYPE_KEY          	=  ":type";
	private final String ANSECTOR_KEY            = "ancestors";
	
	private final String CURRENT_ROUND_KEY     = "current_round";
	
	private final String HREF_LINK_KEY = ":href";
	private final String TEAMS_KEY = "teams";
	private final String NAME_KEY = "name";
	
	private boolean inTransaction = false;;
	
	public CurrentSeasonJsonUtil(String str, boolean inTransaction) throws JSONException {
		
		this.inTransaction  = inTransaction;
		if(str != null)
		parseData(new JSONObject(str));
	}
	private void parseData ( JSONObject jsonObj) {

		DatabaseUtil mDatabaseUtil	=	DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
		
		if ( !inTransaction ) {
		
			mDatabaseUtil.getWritabeDatabase().beginTransaction();
		}
		try{
			
			
			
			String self = jsonObj.optString(SELF_KEY);
			String vHref = jsonObj.optString(HREF_LINK_KEY);
			
			
			String type = jsonObj.getString(TYPE_KEY);
			
			if(!(type.equalsIgnoreCase(Types.CURRENT_SEASON_DATA_TYPE)))
				return;
			String uid = jsonObj.getString(UID_KEY);
			String vCompetitionName = jsonObj.getString(NAME_KEY);
			
			new Util().setColor(jsonObj);
			
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			
			dbUtil.setHeader(vHref,self, type, false);
			
			
			JSONArray ancestor_jArr = jsonObj.getJSONArray ( ANSECTOR_KEY );
			String vCompetitionId = "",vCompetitionUrl = "",vCompetitionHrefUrl = "";
			for ( int k = 0, ancestor_jArr_len = ancestor_jArr.length(); k < ancestor_jArr_len; k++ ) {

				JSONObject ancestor_jArr_jObj = ancestor_jArr.getJSONObject( k );

				 if ( ancestor_jArr_jObj.getString( TYPE_KEY).equalsIgnoreCase( Types.COMPETITION_DATA_TYPE ) ) {
					vCompetitionId = ancestor_jArr_jObj.getString( UID_KEY );
					vCompetitionUrl = ancestor_jArr_jObj.optString( SELF_KEY );
					vCompetitionHrefUrl = ancestor_jArr_jObj.optString( HREF_LINK_KEY );
				} 
				}
			
			JSONObject currentRound = jsonObj.getJSONObject ( CURRENT_ROUND_KEY );
			String currentRoundId = "";
			if(currentRound != null){
				
				
				if(currentRound.getString( TYPE_KEY ).equalsIgnoreCase(Types.ROUND_DATA_TYPE)){
					currentRoundId = currentRound.getString( UID_KEY );
					dbUtil.setHeader(currentRound.optString( HREF_LINK_KEY ),currentRound.optString( SELF_KEY ), currentRound.getString( TYPE_KEY ), false);
				}
					
				
				
				
			}
			
			JSONObject teams = jsonObj.getJSONObject ( TEAMS_KEY );
			String teamId = "",teamUrl = "",teamHrefUrl = "";
			if(currentRound != null && teams.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
				
				teamId = teams.getString( UID_KEY );
				teamUrl = teams.optString( SELF_KEY );
				teamHrefUrl = teams.optString( HREF_LINK_KEY );
				dbUtil.setHeader(teamHrefUrl,teamUrl, teams.getString( TYPE_KEY ), false);
				
				dbUtil.setTeamData(teamId,teamUrl,vCompetitionId,teamHrefUrl);
				
				
				
			}
			dbUtil.setCurrentSeasonData(vCompetitionId,uid,self,vHref);
			dbUtil.setCompetition( vCompetitionId,vCompetitionUrl ,vCompetitionName,currentRoundId,teamId,vCompetitionHrefUrl,false );
			
			JSONObject rounds = jsonObj.getJSONObject(ROUNDS_KEY);
			if(rounds.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
			JsonUtil json = new JsonUtil() ;
			json.setCompetionIdForRound(vCompetitionId);
			json.parse(new StringBuffer(rounds.toString()), Constants.TYPE_ALL_ROUND_JSON, true );
			
			}
			
			
		}catch (Exception e) {
			Logs.show(e);
			  
		} finally {
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			/**
			 * Cleaning Memory
			 */
			new Util().releaseMemory(jsonObj);
			jsonObj	= null;
			
			if ( !inTransaction ) {
				
				mDatabaseUtil.getWritabeDatabase().setTransactionSuccessful();
				mDatabaseUtil.getWritabeDatabase().endTransaction();
			
			 }
		}
		
	}
}

