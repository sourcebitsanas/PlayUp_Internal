package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONObject;


import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class TeamScheduleJsonUtil {

	private final String UID_KEY           			=  ":uid";
	private final String SELF_KEY          			=  ":self";
	private final String HREF_URL_KEY          			=  ":href";
	private final String TYPE_KEY         	 		=  ":type";
	private final String TEAM_KEY         	 		=  "team";
	private final String CONTESTS_KEY         	 	=  "contests";
	private final String ITEMS_KEY         	 		=  "items";
	private final String TITLE_KEY         	 		=  "title";
	
	private boolean inTransaction;
	private String vLinkUrl = null; 
	public TeamScheduleJsonUtil(String str, String vLinkUrl, boolean inTransaction) {
		this.vLinkUrl = vLinkUrl;
		this.inTransaction = inTransaction;
		if ( str != null && str.trim().length() > 0) {
			parseData ( str );
		}
		
	}
	
	public void parseData ( String str ) {
		JSONObject jsonObject = null;
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		if ( !inTransaction ) {
			dbUtil.getWritabeDatabase().beginTransaction();
			
		}
		

		try {
			
			jsonObject = new JSONObject(str);
			String vTeamScheduleId = jsonObject.optString( UID_KEY );
			String vTeamScheduleUrl = jsonObject.optString( SELF_KEY );
			String vTeamScheduleHrefUrl = jsonObject.optString( HREF_URL_KEY );
			String vTeamScheduleType = jsonObject.getString( TYPE_KEY );
			
			if( vTeamScheduleType == null || !vTeamScheduleType.equalsIgnoreCase(Types.TEAM_SCHEDULE_TYPE) )
				return;
	
			String vTitle = null;
			
				vTitle = jsonObject.optString(TITLE_KEY);
			
				
//				Logs.show("vTitle>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> in parsing");
			String vTeamId = null;
			dbUtil.setHeader ( vTeamScheduleHrefUrl,vTeamScheduleUrl,vTeamScheduleType, false );
			dbUtil.setlinkId( vLinkUrl, vTeamScheduleId );
			
			String vTeamContestId = null;
			String vTeamContestUrl = null,vTeamContestHrefUrl= null;
			
			String vTeamContestType = null;
			
			// fetching team data
			JsonUtil json = new JsonUtil();
			
			if ( jsonObject.has( TEAM_KEY ) ) {
				
				if(!(jsonObject.getJSONObject( TEAM_KEY ).getString( TYPE_KEY ).equalsIgnoreCase(Types.TEAM_DATA_TYPE)))
					vTeamId = "";
				else
					json.parse(  new StringBuffer( jsonObject.getJSONObject( TEAM_KEY ).toString() ) , Constants.TYPE_TEAM_JSON, true );
			}
			
			vTeamId = json.getTeamId();
				
			// fetching contest data
			
			
			
			if ( jsonObject.has( CONTESTS_KEY ) ) {
				
				JSONObject contestJsonObj = jsonObject.getJSONObject( CONTESTS_KEY );
				
				if(contestJsonObj.optString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
				
				if( contestJsonObj.has(UID_KEY) )
					vTeamContestId = contestJsonObj.getString(UID_KEY);
				if( contestJsonObj.has(TYPE_KEY) )
					vTeamContestType = contestJsonObj.getString(TYPE_KEY);
				if( contestJsonObj.has(SELF_KEY) )
					vTeamContestUrl = contestJsonObj.optString(SELF_KEY);
				if( contestJsonObj.has(HREF_URL_KEY) )
					vTeamContestHrefUrl = contestJsonObj.optString(HREF_URL_KEY);
				
				// fetching individual contests
				if( contestJsonObj.has( ITEMS_KEY ) ) {
					JSONArray contestArray = contestJsonObj.getJSONArray( ITEMS_KEY );
					
					String vContestId= null;
					dbUtil.removeTeamScheduledContests (vTeamScheduleId  );
					for( int i=0; i<contestArray.length(); i++ ) {
						
						JSONObject contestItemJobj = contestArray.getJSONObject( i );
						
						vContestId = contestItemJobj.getString( UID_KEY );						
						dbUtil.setTeamScheduleContests( vTeamScheduleId, vContestId, i+1 );
						new ContestsJsonUtil( contestItemJobj, "", true, false);
					
					}
					
				}
				
			}
		}
			
			
		/*	dbUtil.setTeamScheduleData( vTeamScheduleId, vTeamScheduleUrl, vTeamScheduleType, vTitle, vTeamId, vTeamContestId, vTeamContestType, vTeamContestUrl );
*/
			dbUtil.setTeamScheduleData( vTeamScheduleId, vTeamScheduleUrl,vTeamScheduleHrefUrl, vTeamScheduleType, vTitle, vTeamId, vTeamContestId, vTeamContestType, vTeamContestUrl,vTeamContestHrefUrl );


			
			
			
		} catch (Exception e) {
			//Logs.show(e);
		} finally {	
			new Util().releaseMemory(jsonObject);
				if ( !inTransaction ) {
					dbUtil.getWritabeDatabase().setTransactionSuccessful();
					dbUtil.getWritabeDatabase().endTransaction();
				}
			}
		}


}
