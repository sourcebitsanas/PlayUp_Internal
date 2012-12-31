package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class LeagueItemJsonUtil {
	private final String SELF_KEY = ":self";
	
	private final String TYPE_KEY = ":type";
	private final String UID_KEY = ":uid";

	private final String NAME_KEY = "name";
	private final String SHORT_KEY = "short_name";
	private final String REGION_KEY = "region";


	private  final String SECTION_KEY = "section";
	private final String LOGO_KEY = "logos";
	private final String DENSITY_KEY = "density";
	private final String HREF_KEY = "href";
	private final String HREF_LINK_KEY = ":href";

	private final String LIVE_CONTESTS_KEY = "live_contests";
	private final String LIVE_KEY = "live";
	private final String ROUND_KEY = "rounds";
	private final String CURRENT_ROUND_KEY = "current_round";
	private final String TEAM_KEY = "teams";

	
	private boolean inTransaction = false;
	public LeagueItemJsonUtil(String leagueItem, boolean inTransaction ) {

		this.inTransaction = inTransaction;
		if (leagueItem != null && leagueItem.trim().length() > 0) {
			parseData(leagueItem);

			leagueItem = null;
		}

	}

	private void parseData(String leagueItem) {
		JSONObject item_jObj  = null;
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*
		 * if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160
		 * ) { } else {
		 */
		if ( !inTransaction ) {
		//	Logs.show ( "begin ------------------------------------LeagueItemJsonUtil ");
			dbUtil.getWritabeDatabase().beginTransaction();
				
		}
		// }

		try {

			item_jObj = new JSONObject(leagueItem);

			// competition related data
			String vCompetitionUrl = item_jObj.optString(SELF_KEY);

			String vCompetitionHrefUrl = item_jObj.optString(HREF_LINK_KEY);

			String vCompetitionId = item_jObj.getString(UID_KEY);
			if(!item_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.COMPETITION_DATA_TYPE))
				return;
			dbUtil.setHeader(vCompetitionHrefUrl,vCompetitionUrl, item_jObj.getString(TYPE_KEY),
					false);

//			String name = item_jObj.getString(NAME_KEY);
//			String shortName = item_jObj.getString(SHORT_KEY);
//			String region = item_jObj.getString(REGION_KEY);
//			String vLogoUrl = null;
//
//			// getting the competition icon
//			JSONArray logos_jArr = item_jObj.getJSONArray(LOGO_KEY);
//			int logo_len = logos_jArr.length();
//
//			for (int j = 0; j < logo_len; j++) {
//
//				JSONObject logo_jObj = logos_jArr.getJSONObject(j);
//				if (Constants.DENSITY.equalsIgnoreCase(logo_jObj.getString(DENSITY_KEY))) {
//
//					vLogoUrl = logo_jObj.getString(HREF_KEY);
//				}
//
//			}
//
//			// no. of live matches in the competition
//			int iLiveNum = item_jObj.getInt(LIVE_CONTESTS_KEY);
//
//			// getting competition live data
//			JSONObject competition_livejObj = item_jObj.getJSONObject(LIVE_KEY);
//			String vCompetitionLiveId = "";
//			if(competition_livejObj.getString(TYPE_KEY).equalsIgnoreCase(Types.LIVE_DATA_TYPE)){
//			String vCompetitionLiveUrl = competition_livejObj.getString(SELF_KEY);
//			 vCompetitionLiveId = competition_livejObj.getString(UID_KEY);
//
//			dbUtil.setHeader(vCompetitionLiveUrl,competition_livejObj.getString(TYPE_KEY), false);
//			dbUtil.setCompetitionLiveData(vCompetitionId, vCompetitionLiveId,vCompetitionLiveUrl);
//			}
//			// getting rounds data
//			JSONObject round_jObj = item_jObj.getJSONObject(ROUND_KEY);
//			String vCompetitionRoundId = "";
//			if(round_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
//			String vCompetitionRoundUrl = round_jObj.getString(SELF_KEY);
//			 vCompetitionRoundId = round_jObj.getString(UID_KEY);
//
//			dbUtil.setHeader(vCompetitionRoundUrl,round_jObj.getString(TYPE_KEY), false);
//			dbUtil.setCompetitionRoundData(vCompetitionId, vCompetitionRoundId,vCompetitionRoundUrl);
//			}
//			// getting current rounds data
//			JSONObject current_round_jObj = item_jObj.getJSONObject(CURRENT_ROUND_KEY);
//			String vCompetitionCurrentRoundId = "";
//			if(current_round_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.ROUND_DATA_TYPE)){
//			String vCompetitionCurrentRoundUrl = current_round_jObj.getString(SELF_KEY);
//			 vCompetitionCurrentRoundId = current_round_jObj.getString(UID_KEY);
//
//			dbUtil.setHeader(vCompetitionCurrentRoundUrl,current_round_jObj.getString(TYPE_KEY), false);
//			dbUtil.setRoundData(vCompetitionCurrentRoundId,vCompetitionCurrentRoundUrl);
//			}
//			// getting teams data
//			JSONObject tems_jObj = item_jObj.getJSONObject(TEAM_KEY);
//			String vCompetitionTeamId = "";
//			if(tems_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
//			String vCompetitionTeamUrl = tems_jObj.getString(SELF_KEY);
//			 vCompetitionTeamId = tems_jObj.getString(UID_KEY);
//
//			dbUtil.setHeader(vCompetitionTeamUrl,tems_jObj.getString(TYPE_KEY), false);
//			dbUtil.setCompetitionTeamData(vCompetitionId, vCompetitionTeamId,vCompetitionTeamUrl);
//			}
//			JSONObject section = item_jObj.getJSONObject(SECTION_KEY);
//			String vSectionId = "",sectionUrl = "";
//			if(section.getString(TYPE_KEY).equalsIgnoreCase(Types.SECTION_DATA_TYPE)){
//			 sectionUrl = section.getString(SELF_KEY);
//			 vSectionId = section.getString(UID_KEY);
//			
//			dbUtil.setHeader(sectionUrl,section.getString(TYPE_KEY) , false);
//			}
//			// saving the competition data in db
//			dbUtil.setCompetition(vCompetitionId, vCompetitionUrl, name,
//					shortName, region, vLogoUrl, iLiveNum, vCompetitionLiveId,
//					vCompetitionRoundId, vCompetitionCurrentRoundId,
//					vCompetitionTeamId,vSectionId,sectionUrl);
//			dbUtil.setSectionData(vSectionId,sectionUrl);
			
			
			String name = item_jObj.getString(NAME_KEY);
			String shortName = item_jObj.getString(SHORT_KEY);
			String region = item_jObj.getString(REGION_KEY);
			String vLogoUrl = null;

			// getting the competition icon
			JSONArray logos_jArr = item_jObj.getJSONArray(LOGO_KEY);
			int logo_len = logos_jArr.length();

			for (int j = 0; j < logo_len; j++) {

				JSONObject logo_jObj = logos_jArr.getJSONObject(j);
				if (Constants.DENSITY.equalsIgnoreCase(logo_jObj
						.getString(DENSITY_KEY))) {

					vLogoUrl = logo_jObj.getString(HREF_KEY);
				}

			}

			// no. of live matches in the competition
			int iLiveNum = item_jObj.getInt(LIVE_CONTESTS_KEY);

			// getting competition live data
			JSONObject competition_livejObj = item_jObj
			.getJSONObject(LIVE_KEY);
			String vCompetitionLiveId = "";
			if(competition_livejObj.getString(TYPE_KEY).equalsIgnoreCase(Types.LIVE_DATA_TYPE)){

			String vCompetitionLiveUrl = competition_livejObj.optString(SELF_KEY);
			String vCompetitionLiveHrefUrl = competition_livejObj
			.optString(HREF_LINK_KEY);
			
			
			 vCompetitionLiveId = competition_livejObj.getString(UID_KEY);

			dbUtil.setHeader(vCompetitionLiveHrefUrl,vCompetitionLiveUrl, competition_livejObj
					.getString(TYPE_KEY) , false  );
			dbUtil.setCompetitionLiveData(vCompetitionId,
					vCompetitionLiveId, vCompetitionLiveUrl,vCompetitionLiveHrefUrl);

			}
			// getting rounds data
			JSONObject round_jObj = item_jObj.getJSONObject(ROUND_KEY);
			String vCompetitionRoundId = "";
			if(round_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){

			String vCompetitionRoundUrl = round_jObj.optString(SELF_KEY);
			String vCompetitionRoundHrefUrl = round_jObj.optString(HREF_LINK_KEY);
			
			 vCompetitionRoundId = round_jObj.getString(UID_KEY);

			dbUtil.setHeader(vCompetitionRoundHrefUrl,vCompetitionRoundUrl, round_jObj
					.getString(TYPE_KEY) , false  );
			dbUtil.setCompetitionRoundData(vCompetitionId,
					vCompetitionRoundId, vCompetitionRoundUrl,vCompetitionRoundHrefUrl);

			}

			// getting current rounds data
			JSONObject current_round_jObj = item_jObj.optJSONObject(CURRENT_ROUND_KEY);
			String vCompetitionCurrentRoundId = "";
			
			if(current_round_jObj != null){
			if(current_round_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.ROUND_DATA_TYPE)){

			String vCompetitionCurrentRoundUrl = current_round_jObj
			.optString(SELF_KEY);
			
			String vCompetitionCurrentRoundHrefUrl = current_round_jObj
			.optString(HREF_LINK_KEY);
			 vCompetitionCurrentRoundId = current_round_jObj
			.getString(UID_KEY);

			dbUtil.setHeader(vCompetitionCurrentRoundHrefUrl,vCompetitionCurrentRoundUrl,
					current_round_jObj.getString(TYPE_KEY) , false  );
			dbUtil.setRoundData(vCompetitionCurrentRoundId,
					vCompetitionCurrentRoundUrl,vCompetitionCurrentRoundHrefUrl);
			}
			

			}

			
			
//			JSONObject currentSeason = item_jObj.getJSONObject(CURRENT_SEASON_KEY);
//			if(currentSeason.getString(TYPE_KEY).equalsIgnoreCase(Types.CURRENT_SEASON_DATA_TYPE)){
//			String vCurrentSeasonId = currentSeason.getString(UID_KEY);
//			String vCurrentSeasonUrl = currentSeason.optString(SELF_KEY);
//			String vCurrentSeasonHrefUrl = currentSeason.optString(HREF_LINK_KEY);
//			
//			dbUtil.setHeader(vCurrentSeasonHrefUrl,vCurrentSeasonUrl, currentSeason.getString(TYPE_KEY), false);
//			
//			dbUtil.setCurrentSeasonData(vCompetitionId,vCurrentSeasonId,vCurrentSeasonUrl,vCurrentSeasonHrefUrl);
//			}
			// getting teams data
			JSONObject tems_jObj = item_jObj.getJSONObject(TEAM_KEY);
			String vCompetitionTeamId = "";
			if(tems_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
			String vCompetitionTeamUrl = tems_jObj.optString(SELF_KEY);

			
			String vCompetitionTeamHrefUrl = tems_jObj.optString(HREF_LINK_KEY);
			 vCompetitionTeamId = tems_jObj.getString(UID_KEY);

			dbUtil.setHeader(vCompetitionTeamHrefUrl,vCompetitionTeamUrl, tems_jObj
					.getString(TYPE_KEY) , false  );
			dbUtil.setCompetitionTeamData(vCompetitionId,
					vCompetitionTeamId, vCompetitionTeamUrl,vCompetitionTeamHrefUrl);

			}

			JSONObject section = item_jObj.getJSONObject(SECTION_KEY);

			String sectionId = "",sectionUrl = "",sectionHref = "";
			if(section.getString(TYPE_KEY).equalsIgnoreCase(Types.SECTION_DATA_TYPE)){
			 sectionUrl = section.optString(SELF_KEY);
			 sectionHref = section.optString(HREF_LINK_KEY);
			 
			 sectionId = section.getString(UID_KEY);
			
			dbUtil.setHeader(sectionHref,sectionUrl,section.getString(TYPE_KEY) , false);
			}
			// saving the competition data in db
			dbUtil.setCompetition(vCompetitionId, vCompetitionUrl, name,
					shortName, region, vLogoUrl, iLiveNum,
					vCompetitionLiveId, vCompetitionRoundId,
					vCompetitionCurrentRoundId, vCompetitionTeamId,
					sectionId,sectionUrl,vCompetitionHrefUrl,sectionHref);
		
			dbUtil.setSectionData(sectionId,sectionUrl,sectionHref);


			

		} catch (Exception e) {
			//Logs.show(e);
		} finally {
			
			
			/*
			 * if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <=
			 * 160 ) { } else {
			 */
			
			/**
			 * Cleaning Memory
			 */
			new Util().releaseMemory(item_jObj);
			
			if ( !inTransaction ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
			//	Logs.show ( "end ------------------------------------LeagueItemJsonUtil ");
					
			}
			// }

		}
	}
}
