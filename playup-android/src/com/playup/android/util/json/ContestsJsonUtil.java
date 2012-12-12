package com.playup.android.util.json;

import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.util.Log;

import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class ContestsJsonUtil {

	private  final String BACKGROUND_IMAGE_KEY = "background_images";
	private  final String DENSITY_KEY = "density";
	
	private final String HREF_LINK_KEY = ":href";
	
	private  final String HREF_KEY = "href";
	private final String UID_KEY           			=  ":uid";
	private final String SELF_KEY          			=  ":self";
	private final String TYPE_KEY         	 		=  ":type";

	private final String SCHEDULED_START_TIME_KEY   =  "scheduled_start_time";
	private final String START_KEY          		=  "start_time";
	private final String END_KEY          			=  "end_time";
	private final String LAST_MODIFIED_KEY          =  "last_modified";
	private final String SHORT_KEY          		=  "short_title";
	private final String TITLE_KEY          		=  "title";
	private final String ANNOTATION_KEY             = "annotation";

	private final String SCORE_KEY          		=  "scores";
	private final String CLOCK_KEY          		=  "clock";
	private final String LOBBY_KEY          		=  "contest_lobby";

	private final String EVENTS_EXTENSION_LINK_KEY          		=  "events_extension_link";

	private final String EVENTS_KEY       		=  "events";
	





	private final String TOTAL_KEY                  = "total";
	private final String SUMMARY_KEY                = "summary";
	private final String TEAM_KEY                   = "team";
	private final String MINUTES_KEY                = "minutes";
	private final String SECONDS_KEY                = "seconds";
	private final String PERIOD_KEY                 = "period";

	private final String INNINGS_KEY                = "innings";
	private final String INNINGS_HALF_KEY           = "inningsHalf";


	private final String OVERS_KEY                  = "overs";
	private final String RUNRATE_KEY                = "run_rate";
	private final String LAST_BALL_KEY              = "last_ball";

	private final String ROUND_NAME_KEY              = "round_name";
	private final String COMPETITION_NAME_KEY              = "competition_name";
	private final String SPORT_NAME_KEY              = "sport_name";


	private final String WICKET_KEY                   = "wickets";
	private final String PLAYER_KEY                   = "player";
	private final String FIRSTNAME_KEY                   = "firstName";
	private final String LASTNAME_KEY                   = "lastName";
	private final String ROLE_KEY                   = "role";
	private final String STATS_KEY                   = "stats";


	private final String STRIKER_KEY				="striker";
	private final String NON_STRIKER_KEY				="non_striker";
	private final String STRIKER_FIRSTNAME_KEY                   = "first_name";
	private final String STRIKER_LASTNAME_KEY                   = "last_name";

	private final String RUNS_KEY = "runs";
	private final String OUT_KEY = "out";
	private final String BALLS_KEY = "balls";
	private final String STRIKES_KEY = "strikes";
	private final String BASES_KEY = "bases";

	private final String SUPER_GOALS_KEY = "super_goals";
	private final String GOALS_KEY = "goals";
	private final String BEHINDS_KEY = "behinds";


	private final String ANSECTOR_KEY            = "ancestors";

	private final String SHARE_KEY            = "share";
	private final String LIVE_UPDATES			= "has_live_updates";

	private final String LAST_EVENT_KEY            = "last_event";
	private final String SHORT_MESSAGE_KEY			= "short_message";
	private final String NAME_KEY					=		"name";
	private final String  LONG_MESSAGE_KEY           = "long_message";

	private final String  POSITION_KEY           = "position";
	private final String  POSITION_SUMMARY_KEY     = "position_summary";
	private final String  ACTIVE_KEY     = "active";
	private final String SUMMARIES_KEY               = "summaries";
	private final String ASSOCIATED_CONTESTS_KEY =  "associated_contests";
	private final String STADIUM_NAME_KEY =  "stadium_name";

	private String vRoundContestId = null;
	private String vCompetitionLiveId = null;

	private String vCompetitionId = null;
	private boolean inTransaction = false;
	private boolean leagues = false;;


	public ContestsJsonUtil ( JSONObject jsonObj, String vRoundContestId , boolean inTransaction,boolean leagues  ) {

		this.inTransaction = inTransaction;
		this.vRoundContestId = vRoundContestId;
		this.leagues  = leagues;
		if ( jsonObj != null ) {
			parseData ( jsonObj );
		}
	}

	public ContestsJsonUtil ( JSONObject jsonObj, String vRoundContestId, String vCompetitionLiveId, boolean inTransaction,boolean leagues ) {

		this.inTransaction = inTransaction;
		this.vCompetitionLiveId = vCompetitionLiveId;

		this.vRoundContestId = vRoundContestId;

		this.leagues  = leagues;
		if ( jsonObj != null) {
			parseData ( jsonObj );
		}
	}

	private void parseData ( JSONObject jsonObj) {
		
	
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();


		if ( !inTransaction ) {
			

			dbUtil.getWritabeDatabase().beginTransaction();
		}
		

		try {
			String vContestId = jsonObj.getString( UID_KEY );
			String vContestUrl = jsonObj.optString( SELF_KEY );
			
			String vContestHrefUrl = jsonObj.optString( HREF_LINK_KEY );
			
			

			dbUtil.setHeader( vContestHrefUrl,vContestUrl, jsonObj.getString( TYPE_KEY ), false  );

			String scheduled_start_time = null;
			if ( jsonObj.has( SCHEDULED_START_TIME_KEY ) ) {
				scheduled_start_time = jsonObj.getString( SCHEDULED_START_TIME_KEY );
			}

			String competitionName = jsonObj.optString(COMPETITION_NAME_KEY);
			String roundName = jsonObj.getString(ROUND_NAME_KEY);
			String sportsName = jsonObj.getString(SPORT_NAME_KEY);
			String vSportType  = null;
			if ( jsonObj.has( TYPE_KEY ) ) 
				vSportType = jsonObj.getString( TYPE_KEY );

			String annotation = null;
			if ( jsonObj.has( ANNOTATION_KEY ) ) {
				annotation = jsonObj.getString(ANNOTATION_KEY);
			}

			String startTime =	null;
			String endTime = null;

			if ( jsonObj.has( START_KEY ) ) {
				startTime = jsonObj.getString( START_KEY );
			}

			if ( jsonObj.has( END_KEY ) ) {
				endTime = jsonObj.getString( END_KEY );
			}

			String last_modified = null;
			if ( jsonObj.has( LAST_MODIFIED_KEY ) ) {
				last_modified = jsonObj.getString( LAST_MODIFIED_KEY );
			}

			int iHasLiveUpdates = 0;
			if ( jsonObj.has( LIVE_UPDATES ) ) {
				if( jsonObj.getBoolean( LIVE_UPDATES ) )
					iHasLiveUpdates = 1;
				else
					iHasLiveUpdates = 0;
			}

			String shortTitle = null;
			if ( jsonObj.has( SHORT_KEY ) ) {
				shortTitle = jsonObj.getString( SHORT_KEY );
			}

			String title = null;
			if ( jsonObj.has( TITLE_KEY ) ) {
				title = jsonObj.getString( TITLE_KEY );
			}

			
			String stadiumName = null;
			if( jsonObj.has(STADIUM_NAME_KEY) ) {
				stadiumName = jsonObj.getString( STADIUM_NAME_KEY );
			}
			
			String vLastEventName = null;
			String vShortMessage = null;
			String vLongMessage = null;
			if( jsonObj.has(LAST_EVENT_KEY)) {
				JSONObject lastEventObj = jsonObj.getJSONObject(LAST_EVENT_KEY);
				vLastEventName = lastEventObj.optString(NAME_KEY);
				vShortMessage = lastEventObj.optString(SHORT_MESSAGE_KEY);
				vLongMessage = lastEventObj.optString(LONG_MESSAGE_KEY);			
			}
			String assoiciatedContestUrl = "", associatedContestId = "",associatedContestHref = "";

			
			if( jsonObj.has(ASSOCIATED_CONTESTS_KEY) ) {

				JSONObject associatedJsonObj = jsonObj.getJSONObject(ASSOCIATED_CONTESTS_KEY);
				if( associatedJsonObj.getString(TYPE_KEY) != null && associatedJsonObj.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)) {
					assoiciatedContestUrl = associatedJsonObj.optString(SELF_KEY);
					associatedContestHref = associatedJsonObj.optString(HREF_LINK_KEY);
					associatedContestId =  associatedJsonObj.optString(UID_KEY);
					dbUtil.setHeader(associatedContestHref,assoiciatedContestUrl, associatedJsonObj.getString(TYPE_KEY), false);

				}
				//				new Util().getAssociatedContests(assoiciatedMatchUrl);
			}

			
			
			String vBackgroundImageUrl= null;

			JSONArray backgroundImage_jArr = jsonObj.optJSONArray(BACKGROUND_IMAGE_KEY);
					if(backgroundImage_jArr != null ){
						
					for (int k = 0; k < backgroundImage_jArr.length(); k++) {

						JSONObject logo_jObj = backgroundImage_jArr.getJSONObject(k);
						if (Constants.DENSITY.equalsIgnoreCase(logo_jObj.getString(DENSITY_KEY))) {

							vBackgroundImageUrl = logo_jObj.getString(HREF_KEY);
						}

					}
					}
					
			

			String vRoundId 	  = null;

			JSONArray ancestor_jArr = jsonObj.getJSONArray ( ANSECTOR_KEY );
			for ( int k = 0, ancestor_jArr_len = ancestor_jArr.length(); k < ancestor_jArr_len; k++ ) {

				JSONObject ancestor_jArr_jObj = ancestor_jArr.getJSONObject( k );

				if ( ancestor_jArr_jObj.getString( TYPE_KEY).equalsIgnoreCase( Types.ROUND_DATA_TYPE ) ) {
					vRoundId = ancestor_jArr_jObj.getString( UID_KEY );


					ContentValues values = new ContentValues ();
					values.put( "vRoundId", vRoundId );
					values.put( "vRoundUrl", ancestor_jArr_jObj.optString( SELF_KEY ) );
					values.put( "vRoundHref", ancestor_jArr_jObj.optString( HREF_LINK_KEY ) );
					
					dbUtil.setRoundData(vRoundId, values );
				} else if ( ancestor_jArr_jObj.getString( TYPE_KEY).equalsIgnoreCase( Types.COMPETITION_DATA_TYPE ) ) {
					vCompetitionId = ancestor_jArr_jObj.getString( UID_KEY );
					dbUtil.setCompetition( vCompetitionId, ancestor_jArr_jObj.optString( SELF_KEY ),ancestor_jArr_jObj.optString( HREF_LINK_KEY ) );
				} else if ( ancestor_jArr_jObj.getString( TYPE_KEY).equalsIgnoreCase( Types.SPORTS_DATA_TYPE ) ) {

				} else if ( ancestor_jArr_jObj.getString( TYPE_KEY).equalsIgnoreCase( Types.GROUPINGS_DATA_TYPE ) ) {

					vRoundId = ancestor_jArr_jObj.getString( UID_KEY );


					ContentValues values = new ContentValues ();
					values.put( "vRoundId", vRoundId );
					values.put( "vRoundUrl", ancestor_jArr_jObj.optString( SELF_KEY ) );
					values.put( "vRoundHref", ancestor_jArr_jObj.optString( HREF_LINK_KEY ) );
					
					dbUtil.setRoundData(vRoundId, values );

				}else {

					boolean exists = dbUtil.checkForLeague ( ancestor_jArr_jObj.getString( UID_KEY ) );
					if ( !exists && leagues) {
						Hashtable<String,  Runnable > runnableList = new Hashtable <String, Runnable > ( );
						runnableList.put(Constants.GET_LEAGUES, new Util().getLeagues( ancestor_jArr_jObj.getString( UID_KEY ) ,runnableList) );

					}
				}
			}

			String vPlayerFirstName1 = null;
			String vPlayerLastName1 = null;
			String vRole1 = null;
			String vStats1 = null;
			// for test cricket 
			String vStrikerFirstName =null;
			String vStrikerLastName =null;
			String vStrikerStats =null;

			String vNonStrikerFirstName =null;
			String vNonStrikerLastName =null;
			String vNonStrikerStats =null;
			String vPlayerFirstName2 = null;
			String vPlayerLastName2 = null;
			String vRole2 = null;
			String vStats2 = null;
			int runs1=0,out1=0,balls1=0,strikes1=0;
			String base1="";
			int runs2=0,out2=0,balls2=0,strikes2=0;
			String base2="";
			int super_goals1=0, goals1=0,behinds1=0;
			int super_goals2=0, goals2=0,behinds2=0;
			int iTotal1 =0; 
			int iTotal2 =0;
			int iWickets1 = 0;
			int iWickets2 = 0;
			String vHomeTeamId = null,vSummary1 = null;
			String vAwayTeamId =null, vSummary2 = null;
			int active1 = 0;
			int active2 = 0;

			// reomving the previous team data 
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2 ( Constants.QUERY_DELETE, null, "summaries", null, "  vContestId = \"" + vContestId + "\" " );
			PlayupLiveApplication.getDatabaseWrapper().queryMethod2 ( Constants.QUERY_DELETE, null, "leaderBoard", null, "  vContestId = \"" + vContestId + "\" " );
			
			

			JSONArray score_jArr = jsonObj.getJSONArray( SCORE_KEY );

			// for non leader board matches
			if( vSportType!= null && !vSportType.equalsIgnoreCase(Constants.LEADERBOARD)) {

				if( score_jArr != null && score_jArr.length() > 0 ) {
					JSONObject team1_jObj = score_jArr.getJSONObject( 0 );

					if ( team1_jObj.has( ACTIVE_KEY ) ) {
						active1 = team1_jObj.getBoolean( ACTIVE_KEY ) ? 1:0 ;
					}

					if ( team1_jObj.has( RUNS_KEY ) ) {
						runs1 = team1_jObj.getInt( RUNS_KEY );
					}
					if ( team1_jObj.has( OUT_KEY ) ) {
						out1 = team1_jObj.getInt( OUT_KEY );
					}
					if ( team1_jObj.has( BALLS_KEY ) ) {
						balls1 = team1_jObj.getInt( BALLS_KEY );
					}
					if ( team1_jObj.has( STRIKES_KEY ) ) {
						strikes1 = team1_jObj.getInt( STRIKES_KEY );
					}
					if ( team1_jObj.has( BASES_KEY ) ) {
						JSONArray baseArray = team1_jObj.getJSONArray(BASES_KEY);
						if( baseArray.length() ==3 ) {
							for ( int i=0; i<3; i++) {
								base1 += baseArray.getBoolean(i) ? "1" :"0";
							}
						}
					}


					if ( team1_jObj.has( SUPER_GOALS_KEY ) ) {
						super_goals1 = team1_jObj.getInt( SUPER_GOALS_KEY );
					}
					if ( team1_jObj.has( GOALS_KEY ) ) {
						goals1 = team1_jObj.getInt( GOALS_KEY );
					}
					if ( team1_jObj.has( BEHINDS_KEY ) ) {
						behinds1 = team1_jObj.getInt( BEHINDS_KEY );
					}


					iTotal1 = team1_jObj.optInt( TOTAL_KEY );

					iWickets1 = 0;
					if ( team1_jObj.has( WICKET_KEY ) ) {
						iWickets1 = team1_jObj.getInt( WICKET_KEY );
					}



					if ( team1_jObj.has( PLAYER_KEY ) ) {

						JSONObject player1_jObj = team1_jObj.getJSONObject( PLAYER_KEY );
						vPlayerFirstName1 = player1_jObj.getString( FIRSTNAME_KEY );
						vPlayerLastName1 = player1_jObj.getString( LASTNAME_KEY );
						vRole1 = player1_jObj.getString( ROLE_KEY );

						if ( player1_jObj.has( STATS_KEY ) ) {
							vStats1 = player1_jObj.getString( STATS_KEY );
						}
					}

					if ( team1_jObj.has( STRIKER_KEY ) ) {

						JSONObject strkier_jObj = team1_jObj.getJSONObject( STRIKER_KEY );
						vStrikerFirstName = strkier_jObj.getString( STRIKER_FIRSTNAME_KEY );
						vStrikerLastName = strkier_jObj.getString( STRIKER_LASTNAME_KEY );
						vStrikerStats = strkier_jObj.getString( STATS_KEY );

					}

					if ( team1_jObj.has( NON_STRIKER_KEY ) ) {

						JSONObject nonStrkier_jObj = team1_jObj.getJSONObject( NON_STRIKER_KEY );
						vNonStrikerFirstName = nonStrkier_jObj.getString( STRIKER_FIRSTNAME_KEY );
						vNonStrikerLastName = nonStrkier_jObj.getString( STRIKER_LASTNAME_KEY );
						vNonStrikerStats = nonStrkier_jObj.getString( STATS_KEY );

					}

					if ( team1_jObj.has( SUMMARY_KEY ) && !team1_jObj.isNull( SUMMARY_KEY ) ) {
						vSummary1 = team1_jObj.getString( SUMMARY_KEY );
					}
					
					
					// parse team and get the team id
					JsonUtil json = new JsonUtil();
					json.parse(  new StringBuffer( team1_jObj.getJSONObject( TEAM_KEY ).toString() ) , Constants.TYPE_TEAM_JSON, true );
					vHomeTeamId = json.getTeamId();


					if ( team1_jObj.has( SUMMARIES_KEY ) ) {

						int summaries_pos = 0;
						JSONArray summariesArray = team1_jObj.getJSONArray( SUMMARIES_KEY );
						for ( int j=0;j < summariesArray.length(); j++) {

							String summaries_data = summariesArray.get(j).toString();
							if ( summaries_data != null && summaries_data.trim().length() > 0 ) {
								summaries_pos = summaries_pos + 1;
								dbUtil.setSummariesData(vHomeTeamId, vContestId, summariesArray.get(j).toString(), summaries_pos );
							}
						}
					}

				}

				if( score_jArr != null && score_jArr.length() > 1 ) {
					JSONObject team2_jObj = score_jArr.getJSONObject( 1 );

					if ( team2_jObj.has( ACTIVE_KEY ) ) {
						active2 = team2_jObj.getBoolean( ACTIVE_KEY ) ? 1:0 ;
					}

					if ( team2_jObj.has( RUNS_KEY ) ) {
						runs2 = team2_jObj.getInt( RUNS_KEY );
					}
					if ( team2_jObj.has( OUT_KEY ) ) {
						out2 = team2_jObj.getInt( OUT_KEY );
					}
					if ( team2_jObj.has( BALLS_KEY ) ) {
						balls2 = team2_jObj.getInt( BALLS_KEY );
					}
					if ( team2_jObj.has( STRIKES_KEY ) ) {
						strikes2 = team2_jObj.getInt( STRIKES_KEY );
					}
					if ( team2_jObj.has( BASES_KEY ) ) {
						JSONArray baseArray = team2_jObj.getJSONArray(BASES_KEY);
						if( baseArray.length() ==3 ) {
							for ( int i=0; i<3; i++) {
								base2 += baseArray.getBoolean(i) ? "1" :"0";
							}
						}
					}

					if ( team2_jObj.has( SUPER_GOALS_KEY ) ) {
						super_goals2 = team2_jObj.getInt( SUPER_GOALS_KEY );
					}
					if ( team2_jObj.has( GOALS_KEY ) ) {
						goals2 = team2_jObj.getInt( GOALS_KEY );
					}
					if ( team2_jObj.has( BEHINDS_KEY ) ) {
						behinds2 = team2_jObj.getInt( BEHINDS_KEY );
					}


					iTotal2 = team2_jObj.optInt( TOTAL_KEY );

					if ( team2_jObj.has( SUMMARY_KEY ) && !team2_jObj.isNull( SUMMARY_KEY ) ) {
						vSummary2 = team2_jObj.getString( SUMMARY_KEY );
					}


					iWickets2 = 0;
					if ( team2_jObj.has( WICKET_KEY ) ) {
						iWickets2 = team2_jObj.getInt( WICKET_KEY );
					}



					if ( team2_jObj.has( PLAYER_KEY ) ) {

						JSONObject player2_jObj = team2_jObj.getJSONObject( PLAYER_KEY );
						vPlayerFirstName2 = player2_jObj.getString( FIRSTNAME_KEY );
						vPlayerLastName2 = player2_jObj.getString( LASTNAME_KEY );
						vRole2 = player2_jObj.getString( ROLE_KEY );

						if ( player2_jObj.has( STATS_KEY ) ) {
							vStats2 = player2_jObj.getString( STATS_KEY );
						}

					}

					if ( team2_jObj.has( STRIKER_KEY ) ) {
						JSONObject strkier_jObj = team2_jObj.getJSONObject( STRIKER_KEY );
						vStrikerFirstName = strkier_jObj.getString( STRIKER_FIRSTNAME_KEY );
						vStrikerLastName = strkier_jObj.getString( STRIKER_LASTNAME_KEY );

						vStrikerStats = strkier_jObj.getString( STATS_KEY );

					}

					if ( team2_jObj.has( NON_STRIKER_KEY ) ) {

						JSONObject nonStrkier_jObj = team2_jObj.getJSONObject( NON_STRIKER_KEY );
						vNonStrikerFirstName = nonStrkier_jObj.getString( STRIKER_FIRSTNAME_KEY );
						vNonStrikerLastName = nonStrkier_jObj.getString( STRIKER_LASTNAME_KEY );
						vNonStrikerStats = nonStrkier_jObj.getString( STATS_KEY );	
					}

					JsonUtil json = new JsonUtil();

					if ( team2_jObj.has( TEAM_KEY ) ) {
						json.parse(  new StringBuffer( team2_jObj.getJSONObject( TEAM_KEY ).toString() ) , Constants.TYPE_TEAM_JSON, true );
					}
					vAwayTeamId = json.getTeamId();

					if ( team2_jObj.has( SUMMARIES_KEY ) ) {
						JSONArray summariesArray = team2_jObj.getJSONArray( SUMMARIES_KEY );
						int summaries_pos = 0;
						for ( int j=0;j < summariesArray.length(); j++) {

							String summaries_data = summariesArray.get(j).toString();

							if ( summaries_data != null && summaries_data.trim().length() > 0 ) {
								summaries_pos = summaries_pos + 1;
								dbUtil.setSummariesData(vAwayTeamId, vContestId, summaries_data, summaries_pos );

							}

						}
					}
				}


			} else if(  vSportType!= null && vSportType.equalsIgnoreCase(Constants.LEADERBOARD) ){

				
				for ( int i =0; i< score_jArr.length(); i++ ) {

					String vSummary =null;					
					String vPositionSummary = null;
					int iPosition = 0;
					String vTeamId = null;

					JSONObject team = score_jArr.getJSONObject( i );						


					if ( team.has( SUMMARY_KEY ) && !team.isNull( SUMMARY_KEY ) ) {
						vSummary = team.getString( SUMMARY_KEY );
					}
					iPosition = team.optInt( POSITION_KEY );
					
					if ( iPosition == 0 ) {
						iPosition = score_jArr.length() + 1;
					}
					vPositionSummary = team.optString( POSITION_SUMMARY_KEY );


					JsonUtil json = new JsonUtil();
					json = new JsonUtil();

					if ( team.has( TEAM_KEY ) ) {
						
						// call directly 
						json.parse(  new StringBuffer( team.getJSONObject( TEAM_KEY ).toString() ) , Constants.TYPE_TEAM_JSON, true );
					}

					vTeamId = json.getTeamId();


					if ( team.has( SUMMARIES_KEY ) ) {
						JSONArray summariesArray = team.getJSONArray( SUMMARIES_KEY );
						int summaries_pos = 0; 
						for (int  j=0;j < summariesArray.length(); j++) {

							String summaries_data = summariesArray.get(j).toString();

							if ( summaries_data != null && summaries_data.trim().length() > 0 ) {
								summaries_pos = summaries_pos + 1;
								dbUtil.setSummariesData(vTeamId, vContestId, summaries_data, summaries_pos );
							}
						}
					}


					dbUtil.setLeaderBoardData( vContestId, vTeamId, vSummary, iPosition, vPositionSummary );
				}

			}


			// end of parsing the scores data

			JSONObject clock_jObj = jsonObj.getJSONObject( CLOCK_KEY );

			int mins = 0;
			int secs = 0;
			int period = 0;

			int innings = 0;
			String inningsHalf = null;

			String overs = ""; 
			String run_rate = "";
			String last_ball = "";
			String annotation2 =null;
			if ( clock_jObj.has( MINUTES_KEY ) ) {

				mins = clock_jObj.getInt( MINUTES_KEY );
				secs = clock_jObj.getInt( SECONDS_KEY );
				period = clock_jObj.getInt( PERIOD_KEY );

			} else if ( clock_jObj.has( INNINGS_KEY ) ) {

				try {
					if(clock_jObj.get( INNINGS_KEY ).toString().trim().length() > 0)					
						innings = Integer.parseInt( clock_jObj.get( INNINGS_KEY ).toString() );
				} catch ( Exception e ) {
					Logs.show( e );
				}
				inningsHalf = clock_jObj.getString( INNINGS_HALF_KEY );

			}  else if ( clock_jObj.has( OVERS_KEY ) ) { 

				overs = clock_jObj.getString( OVERS_KEY );
				run_rate = clock_jObj.getString( RUNRATE_KEY );
				last_ball = clock_jObj.getString( LAST_BALL_KEY );
				if ( clock_jObj.has( ANNOTATION_KEY ) ) { 
					annotation2 = clock_jObj.getString( ANNOTATION_KEY );
				}

			} else  {

				mins = clock_jObj.optInt( MINUTES_KEY );
				secs = clock_jObj.optInt( SECONDS_KEY );
				period = clock_jObj.optInt( PERIOD_KEY );
			}

			String summary = null;
			if ( clock_jObj.has( SUMMARY_KEY ) && !clock_jObj.isNull( SUMMARY_KEY ) ) {
				summary = clock_jObj.getString( SUMMARY_KEY );
			}

			JSONObject contest_lobby_jObj = jsonObj.getJSONObject( LOBBY_KEY );
			String vContestLobbyUid = "",vContestLobbyUrl = "",vContestLobbyHrefUrl = "";
			if(contest_lobby_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.CONTEST_LOBBY_DATA_TYPE)){
				vContestLobbyUid = contest_lobby_jObj.getString( UID_KEY );
				vContestLobbyUrl = contest_lobby_jObj.optString( SELF_KEY );
				vContestLobbyHrefUrl = contest_lobby_jObj.optString( HREF_LINK_KEY );
				
				
				
				dbUtil.setContestLobbyData ( vContestLobbyUid, vContestLobbyUrl,  vContestId ,vContestLobbyHrefUrl);
				dbUtil.setHeader(vContestLobbyHrefUrl, vContestLobbyUrl, contest_lobby_jObj.getString( TYPE_KEY ), false  );
			}

			String vShareUrl = null,vShareHrefUrl = "";
			if( jsonObj.has( SHARE_KEY ) ) {
				JSONObject share_jObj = jsonObj.getJSONObject( SHARE_KEY );
				if(share_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.SHARE_DATA_TYPE)){
					
					vShareUrl = share_jObj.optString( SELF_KEY );
					vShareHrefUrl = share_jObj.optString( HREF_LINK_KEY );
					dbUtil.setHeader(vShareHrefUrl,vShareUrl, share_jObj.getString(TYPE_KEY), false);
				}
			}


			String vEventsExtensionUrl = null,vEventsExtensionHrefUrl = null;
			if( jsonObj.has( EVENTS_EXTENSION_LINK_KEY ) ) {
				JSONObject events_extension_jObj = jsonObj.getJSONObject( EVENTS_EXTENSION_LINK_KEY );

				if(events_extension_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.HTML_DATA_TYPE)){
					vEventsExtensionUrl = events_extension_jObj.optString( SELF_KEY );
					vEventsExtensionHrefUrl = events_extension_jObj.optString( HREF_LINK_KEY );
					
				}
			}

			String vEventsUrl = null,vEventsId = null,vEventsHrefUrl = null;
			if( jsonObj.has( EVENTS_KEY ) ) {
				JSONObject events_jObj = jsonObj.getJSONObject( EVENTS_KEY );

				if(events_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
					vEventsUrl = events_jObj.optString( SELF_KEY );
					vEventsHrefUrl = events_jObj.optString( HREF_LINK_KEY );
					vEventsId = events_jObj.getString(UID_KEY);
					dbUtil.setHeader(vEventsHrefUrl,vEventsUrl, events_jObj.getString(TYPE_KEY), false);

				}
			}




			// saving the contest data in the DB
			dbUtil.setContestData ( vContestId, vContestUrl, scheduled_start_time, startTime, endTime, last_modified, shortTitle, title, iTotal1, vSummary1, vHomeTeamId, 
					iTotal2, vSummary2, vAwayTeamId, mins, secs, period, innings,
					inningsHalf, overs, run_rate, last_ball, summary,
					vRoundContestId, vCompetitionId, vCompetitionLiveId,competitionName,roundName,sportsName,annotation,
					iWickets1, vPlayerFirstName1, vPlayerLastName1, vRole1, vStats1, iWickets2, vPlayerFirstName2, vPlayerLastName2, vRole2, vStats2,
					vStrikerFirstName,vStrikerLastName,vStrikerStats,vNonStrikerFirstName,vNonStrikerLastName,vNonStrikerStats,annotation2, 
					runs1,out1,balls1,strikes1,base1,runs2,out2,balls2,strikes2,base2,
					super_goals1, goals1,behinds1,super_goals2, goals2,behinds2,
					vRoundId,vShareUrl,iHasLiveUpdates , vLastEventName, vShortMessage, vLongMessage, vSportType, active1, active2,
					associatedContestId, assoiciatedContestUrl,vEventsExtensionUrl,vEventsUrl,vEventsId, 
					stadiumName,vBackgroundImageUrl ,associatedContestHref,vContestHrefUrl,vEventsExtensionHrefUrl,vEventsHrefUrl,vShareHrefUrl);
			 
			//			,vEventsUrl 

			/*// saving  the sports live contest data into the database
			if ( vCompetitionLiveId != null && vCompetitionLiveId.trim().length() > 0 ) {
				dbUtil.setSportsLiveContest ( vCompetitionLiveId, vContestId );
			}*/



			

		} catch (JSONException e) {
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
			}
			//}
		}
	}





	public String getCompetitionId () {

		return vCompetitionId;

	}
}

