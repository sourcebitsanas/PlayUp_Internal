package com.playup.android.util.json;

import java.util.Hashtable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.CacheUtil;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

public class JsonUtil {


	private boolean flag = false;
	private boolean flag2 = false;
	private String vConversationId = null;

	private String vCompetitionLiveId = null;
	private String vCompetitionId = null;

	private String vPushId = null;
	private int iOrder = -1;
	private String vUserId = null;
	private String vGapId = null;
	private String vSportsId = null;
	private String vCompetitionRoundId = null;
	private String vRoundId = null;
	private String vRoundContestId = null;
	private String vTeamId = null;
	private boolean friendsRefresh;

	private String vDirectConversationId = null;
	private String vDirectConversationUrl = null;
	private String vDMessageId = null;
	private String vDirectMessageId = null;
	private boolean  playupFriends;
	private String vDirectConversationPushId = null;
	private boolean fromPush;
	private boolean shouldDelete;
	private boolean fromGap;
	private String vLobbyId;
	private String vAcceptableType;
	private String vConversationUrl;
	private String vConversationHrefUrl;
	private String vPrivateMessagePushId;
	private int vPrivateLobbyOrderId;
	
	
	private String vContestId = null;
	
	
	private Hashtable<String, Object> vDirectMessageUrl = null;
	
	private String vLobbyUrl =  null;
	private String vBlockContentId;
	private int iOrderId = -1;
	private boolean isFromGeoTag = false;
	private String vSectionCompetitionId = "";
	private boolean shouldParseLeague;
	private String vCompetitionIdForRound = "";
	private String vContentId = "";
	
	
	private String vLinkUrl = null;
	private String currentRegion = null;
	
	public String vTeamScheduleUrl = null;
	private boolean isConversationUrlHref = false;
	private Boolean isCredentialsExpirationSecanrio = false;
	private boolean isDirectConversationUrlHref = false;
	
	
	
	public String getContestId ( ) {
		return vContestId;
	}
	
	
	public void setLinkUrl ( String vLinkUrl ) {
		
		this.vLinkUrl = vLinkUrl;
	}
	public void setAcceptableType ( String vAcceptableType ) {
		this.vAcceptableType = vAcceptableType;
	}
	
	public void setSectionCompetitionId ( String vSectionCompetitionId ) {
		this.vSectionCompetitionId = vSectionCompetitionId;
	}
	
	
	public void setPrivateLobbyOrderId ( int iOrderId ) {
		this.vPrivateLobbyOrderId = iOrderId;
	}
 	
	public void setLobbyId ( String vLobbyId ) {
		this.vLobbyId = vLobbyId;
	}
	
	public void setLobbyUrl ( String vLobbyUrl ) {
		this.vLobbyUrl = vLobbyUrl;
	}
	
	
	
	public void setFromGap ( boolean fromGap ) {
		this.fromGap = fromGap;
	}
	public void setShouldDelete ( boolean shouldDelete ) {
		this.shouldDelete = shouldDelete;
	}
	
	public void setDirectMessageId ( String vDirectMessageId ) {
		this.vDirectMessageId = vDirectMessageId;
	}
	public void setPlayupFriends ( boolean playupFriends ) {
		this.playupFriends = playupFriends;
	}

	public void setCompetitionLiveId ( String vCompetitionLiveId ) {
		this.vCompetitionLiveId = vCompetitionLiveId;
	}
	public void setRoundContestId ( String vRoundContestId  ) {
		this.vRoundContestId = vRoundContestId;
	}
	public void setCompetitionRoundId ( String vCompetitionRoundId ) {
		this.vCompetitionRoundId = vCompetitionRoundId;
	}

	public void setPushId ( String vPushId ) {

		this.vPushId = vPushId;
	}
	
	public void setDirectConvesationPushId ( String vPushId ) {

		this.vDirectConversationPushId  = vPushId;
	}
	
	public void setPrivateMessagePushId ( String vPushId ) {

		this.vPrivateMessagePushId  = vPushId;
	}
	
	
	
	
	public void setSportsId ( String vSportsId ) {

		this.vSportsId = vSportsId;

	}
	public void setGapId ( String vGapId ) {
		this.vGapId = vGapId;
	}
	public void setFriendsRefresh ( boolean fromRefresh ) {
		this.friendsRefresh = fromRefresh;
	}
	public void setBooleanFlag ( boolean flag ) {

		this.flag = flag;
	}

	public void setBooleanFlag2 ( boolean flag2 ) {

		this.flag2 = flag2;
	}

	public void setUserId ( String vUserId ) {
		this.vUserId = vUserId;
	}

	public void setConversationId ( String vConversationId ) {

		this.vConversationId = vConversationId;
	}

	public void setConversationUrl ( String vConversationUrl ) {

		this.vConversationUrl = vConversationUrl;
	}
	
	
	public void setConversationHrefUrl ( String vConversationHrefUrl ) {

		this.vConversationHrefUrl = vConversationHrefUrl;
	}

	public void setOrder  ( int iOrder ) {

		this.iOrder = iOrder;
	}
	
	public void setBlockTileId(String tilesUid) {
	this.vBlockContentId = tilesUid;
		
	}
	public void setBlockOrderId(int iOrderId) {
	this.iOrderId  = iOrderId;
		
	}
	


	public void setDirectConversationId ( String vDirectConversationId ) {
		this.vDirectConversationId = vDirectConversationId;
	}


	public void setDirectConversationUrl ( String vDirectConversationUrl ) {
		this.vDirectConversationUrl = vDirectConversationUrl;
	}
	public void setDMessageId ( String vDMessageId ) {
		this.vDMessageId = vDMessageId;
	}


	synchronized public void parse ( StringBuffer strBuffer, int type, boolean inTransaction ) {


		json_method ( strBuffer.toString(), type, inTransaction, -1, null, null, null, null );




	}

	synchronized public void dropTables ( ) {

		//Log.e("123", "Inside dropTables");
		json_method ( null, -200, false, -1, null, null, null, null );




	}

	synchronized public void queryMethod1 (final  int queryType, final String sqlQuery,final  String  tableName, final ContentValues values, final  String whereClause, final String searchId, final boolean chk, boolean inThread ) {


		if ( inThread ) {
			Runnable r = new Runnable () {

				@Override
				public void run() {

					try {
						if ( chk ) {
							int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount( " SELECT " + searchId + " FROM " + tableName + " WHERE " + whereClause + " " );
							if ( count > 0 ) {
								json_method ( null, -100, false, Constants.QUERY_UPDATE, null, tableName, values, whereClause );
							} else {
								json_method ( null, -100, false, Constants.QUERY_INSERT, null, tableName, values, null );
							}
						} else {


							json_method ( null, -100, false, queryType, sqlQuery, tableName, values, whereClause );
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show ( e );
					}
				}

			};
			Thread th = new Thread ( r ) ;
			th.start();	
		} else {

			if ( chk ) {
				int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount( " SELECT " + searchId + " FROM " + tableName + " WHERE " + whereClause + " " );
				if ( count > 0 ) {
					json_method ( null, -100, false, Constants.QUERY_UPDATE, null, tableName, values, whereClause );
				} else {
					json_method ( null, -100, false, Constants.QUERY_INSERT, null, tableName, values, null );
				}
			} else {
				json_method ( null, -100, false, queryType, sqlQuery, tableName, values, whereClause );
			}

		}


	}


	synchronized public void json_method ( String str, int type, boolean inTransaction, int queryType, String sqlQuery, String  tableName, ContentValues values, String whereClause ) {

		if ( type == -200 ) {
			//Log.e("123", "Inside json_method() for drop tables"); 
			PlayupLiveApplication.getDatabaseWrapper().dropTables();
		}
		else if ( type == -100 ) {
			PlayupLiveApplication.getDatabaseWrapper().queryMethod3(queryType, sqlQuery, tableName, values, whereClause);
		} else {
			switch ( type ) {
			case Constants.TYPE_BASE_URL : 
				baseData ( str ); 
				break;

				//		case Constants.TYPE_LOGIN_ANNONYMOUS_URL : 
				//										annonymousData ( strBuffer.toString(), true ) ; 
				//										break;

			case Constants.TYPE_PROFILE_URL :
				profileData ( str, false, inTransaction  );
				break;
			case Constants.TYPE_PRIMARY_PROFILE_URL :
				profileData( str, true, inTransaction );
				break;
			case Constants.TYPE_CONTEXT_URL :
				contextData ( str, inTransaction );
				break;

			case Constants.TYPE_CONTEXT_DETAILED_URL :
				contextDetailedData ( str , inTransaction );
				break;

			case Constants.TYPE_PROVIDER : 
				providerData ( str, inTransaction );
				break;
				//		case Constants.TYPE_LOGIN_USER_URL : 
				//										annonymousData ( strBuffer.toString(),false ) ; 
				//										break;

			case Constants.TYPE_STORE_TOKEN:
				userToken( str , inTransaction);
				break;

			case Constants.TYPE_CONTEST_COVERSATIONS:
				contestConversations( str, inTransaction );
				break;
			case Constants.TYPE_MESSAGE_POST:
				contestConversations( str, inTransaction );
				break;

			case Constants.TYPE_ADD_FRIEND_JSON:
				addFriendJson ( str, inTransaction );
				break;
			case Constants.TYPE_ALL_ROUND_JSON :
				allRoundJson( str, inTransaction );
				break;
			case Constants.TYPE_CONTEST_LOBBY_JSON :
				contestLobbyJson ( str, inTransaction );
				break;

			case Constants.TYPE_CONTEST_JSON :
				
				contestJson ( str, inTransaction );
				break;

			case Constants.TYPE_CONVERSATION_FRIENDS_JSON :
				conversationFriendsJson( str, vConversationId, inTransaction );
				break;

			case Constants.TYPE_CONVERSATION_JSON :
				conversationJson ( str, inTransaction );
				break;

			case Constants.TYPE_CONVERSATION_MESSAGES_JSON :
				conversationMessageJson ( str, inTransaction);
				break;

			case Constants.TYPE_FRIEND_JSON :
				friendJson ( str, inTransaction );
				break;

			case Constants.TYPE_GET_FRIENDS_JSON :
				getfriendJson ( str, inTransaction );
				break;

			case Constants.TYPE_LEAGUE_JSON :
				leagueJson ( str , inTransaction );
				break;
			case Constants.TYPE_LIVE_FRIEND_JSON :
				liveFriendJson( str, inTransaction );
				break;

			case Constants.TYPE_NOTIFICATION_JSON :
				notificationJson ( str , inTransaction);
				break;

			case Constants.TYPE_RECENT_ACTIVITY_JSON :
				recentActivityJson( str, inTransaction );
				break;

			case Constants.TYPE_ROUND_CONTEST_JSON :
				roundContestJson( str, inTransaction );
				break;

			case Constants.TYPE_ROUND_JSON :
				roundJson( str, inTransaction );
				break;

			case Constants.TYPE_SEARCH_FRIEND_JSON :
				searchFriendJson( str, inTransaction );
				break;
			

			case Constants.TYPE_SPORTS_LIVE_JSON :
				sportsLiveJson ( str, inTransaction );
				break;

			case Constants.TYPE_TEAM_JSON :
				teamJson ( str, inTransaction );
				break;
			case Constants.TYPE_ASSOCIATED_TEAM_JSON :
				associatedTeamJson ( str, inTransaction );
				break;

			case Constants.TYPE_USER_NOTIFICATION_JSON :
				userNotificationJson ( str, inTransaction );
				break;

			case Constants.TYPE_ALL_SPORTS_JSON :
				allSportsJson ( str, inTransaction );
				break;

			case Constants.TYPE_DIRECT_CONVERSATION_JSON :
				directConversation( str, inTransaction ); 
				break;

			case Constants.TYPE_DIRECT_MESSAGES_JSON :
				directMessages ( str, inTransaction );
				break;

			case Constants.TYPE_DIRECT_MESSAGES_ITEM_JSON :
				directMessageItem ( str, inTransaction );
				break;

			case Constants.TYPE_PLAYUP_FRIENDS_JSON :
				playupFriends ( str, inTransaction);
				break;


			case Constants.TYPE_PLAYUP_FRIENDS_SEARCH_JSON:
				searchPlayupFriends ( str, inTransaction);
				break;

			case Constants.TYPE_DIRECT_MSGS_JSON :
				directMsgs ( str, inTransaction );
				break;
				

			case Constants.TYPE_MY_LOBBY_JSON :
				myLobby ( str, inTransaction ); 
				break;
				
			case Constants.TYPE_FRIEND_CONVERSATION_JSON :
				friendConversation ( str, inTransaction );
				break;


			case Constants.TYPE_LEAGUE_ITEM :
				leagueItem ( str, inTransaction);
				break;
				
			
				
			case Constants.TYPE_FRIEND_CONVERSATION__MESSAGE_JSON :
				frienConversationMessage ( str, inTransaction );
				break;
				
			case Constants.TYPE_SECTION:
				section ( str, inTransaction );
				break;
				
			case Constants.TYPE_CURRENT_SEASON_JSON:
				currentSeason ( str, inTransaction );
				break;
				
				
			case Constants.TYPE_TILE_DISPLAY_JSON:
				display ( str, inTransaction );
				break;
				
				
			case Constants.TYPE_DISPLAY_JSON:
				displayUpdate ( str, inTransaction );
				break;
				
			case Constants.TYPE_ASSOCIATED_CONTEST_JSON:
				associatedContestJson(str, inTransaction);

				break;
				
			case Constants.TYPE_REGIONS_JSON:
				regionsJson(str, inTransaction);

				break;
				
			case Constants.TYPE_COUNTRIES_JSON:
				countriesJson(str, inTransaction);

				break;
				
			case Constants.TYPE_COMPETITON_LIVE:
				competitionLiveJsonUtil(str, inTransaction);

				break;
				
			case Constants.TYPE_TEAM_SCHEDULE_JSON:
				teamScheduleJson ( str, inTransaction );
				break;
				
			case Constants.PROVIDER_TOKEN:
				providerTokensJson ( str, inTransaction );
				break;
				
				
				

			}

		}



		/*if ( dbUtil.getWritabeDatabase().isDbLockedByCurrentThread() ) {
			dbUtil.getWritabeDatabase().setLockingEnabled( false );
		}*/

	}
	
	
	
	private void teamScheduleJson ( String str, boolean inTransaction ) {
		new TeamScheduleJsonUtil ( str,vTeamScheduleUrl, inTransaction );
	}
	
	private void providerTokensJson ( String str, boolean inTransaction ) {
		new ProviderTokensJsonUtil ( str, inTransaction );
	}


	private void competitionLiveJsonUtil(String str, boolean inTransaction) {
		new CompetitionLiveJsonUtil(str, inTransaction);
		
	}


	private void regionsJson(String str, boolean inTransaction) {
		new RegionJsonUtil(str,currentRegion, false);
		
	}
	
	private void countriesJson(String str, boolean inTransaction) {
		new CountriesJsonUtil(str,currentRegion, false);
		
	}


	private void display(String str, boolean inTransaction) {
		try {
			
				
			new BlockContentJsonUtil ( str, inTransaction,vBlockContentId,iOrderId);
		} catch ( Exception e) {
			Logs.show( e );
		}
		
		
	}
	
	private void displayUpdate(String str, boolean inTransaction) {
		try {
			if(vContentId != null && vContentId.trim().length() > 0)
				new DisplayJsonUtil ( str, inTransaction,vBlockContentId,vContentId);
		} catch ( Exception e) {
			Logs.show( e );
		}
		
		
	}
	
	private void currentSeason(String str, boolean inTransaction) {
		try {
			new CurrentSeasonJsonUtil ( str, inTransaction);
		} catch ( Exception e) {
			Logs.show( e );
		}
		
		
	}

	private void frienConversationMessage ( String str, boolean inTransaction ) {
	
		try {

			
			new MyFriendConversationMessageJsonUtil ( str, inTransaction, vConversationId, vConversationUrl,shouldDelete , flag,isConversationUrlHref );



		} catch ( Exception e) {
			Logs.show( e );
		}
		
		
	}
	
	
	private void section ( String str, boolean inTransaction ) {
		
		try {
			new SectionJsonUtil ( str, inTransaction,isFromGeoTag,vSectionCompetitionId  );
		} catch ( Exception e) {
			Logs.show( e );
		}
		
		
	}

	private void friendConversation ( String str , boolean inTransaction ) {
		
		try {
			MyFriendConversationJsonUtil myFriendConversationJsonUtil = null;
			if(vPrivateMessagePushId != null && vPrivateMessagePushId.trim().length() > 0){				
				myFriendConversationJsonUtil = new MyFriendConversationJsonUtil ( str, inTransaction, vAcceptableType, vPrivateMessagePushId );				
			}
			else{
				myFriendConversationJsonUtil= new MyFriendConversationJsonUtil ( str, inTransaction, vAcceptableType,vPrivateLobbyOrderId );
			}
			
			
			vConversationId = myFriendConversationJsonUtil.getConversationId ();
		} catch ( Exception e) {
			Logs.show( e );
		}
		
	}
	
	private void myLobby ( String str , boolean inTransaction ) {
		
		try {
			new MyLobbyJsonUtil ( str, inTransaction, vLobbyId, vLobbyUrl );
		} catch ( Exception e) {
			Logs.show( e );
		}
	}

	
		
	
	private void leagueItem ( String str, boolean inTransaction ) {
		
		
		try {

			new LeagueItemJsonUtil( str, inTransaction );

		} catch ( Exception e) {
			Logs.show( e );
		}
		
	}
	
	private void directMessageItem ( String str, boolean inTransaction ) {

		try {
			new DirectMessageItemJsonUtil ( str, vDMessageId, inTransaction, shouldDelete );
		} catch ( Exception e) {
			Logs.show( e );
		}
	}

	private void playupFriends ( String str, boolean inTransaction ) {

		try {
			new PlayupFriendsJsonUtil ( str, vGapId, friendsRefresh, inTransaction );
		} catch ( Exception e) {
			Logs.show( e );
		}
	}


	private void searchPlayupFriends ( String str, boolean inTransaction ) {

		try {
			new SearchPlayupFriendsJsonUtil ( str, vGapId, inTransaction );
		} catch ( Exception e) {
			Logs.show( e );
		}
	}



	private void directMsgs ( String str, boolean inTransaction ) {
		try {
			new DirectMsgsJsonUtil(  str, inTransaction, vDirectMessageId, fromGap );
		} catch ( Exception e ) {
			Logs.show( e );
		}
	}

	
	public Hashtable<String, Object> getDirectMessageUrl () {
		return vDirectMessageUrl;
	}
	private void directMessages ( String str, boolean inTransaction ) {

		try {
			if(vDirectConversationPushId != null){
				DirectMessagesJsonUtil directMessagesJsonUtil = new DirectMessagesJsonUtil(  str,false, vDirectConversationUrl ,vDirectConversationPushId );
				vDirectMessageUrl = directMessagesJsonUtil.getDirectMessageUrl ();
			}else{
				DirectMessagesJsonUtil  directMessagesJsonUtil = new DirectMessagesJsonUtil(  str, vUserId, vDirectConversationId, inTransaction, vDirectConversationUrl,isDirectConversationUrlHref);
				vDirectMessageUrl = directMessagesJsonUtil.getDirectMessageUrl ();
			}
		
		} catch ( Exception e ) {
			Logs.show( e );
		}
	}

	private void directConversation ( String str, boolean inTransaction  ) {

		try {
			new DirectConversationJsonUtil(  str, vUserId, inTransaction );
		} catch ( Exception e ) {
			Logs.show( e ); 
		}
	}

	private void allSportsJson ( String str, boolean inTransaction ) {

		try {
			new AllSportsJsonUtil(new JSONObject( str ), inTransaction  );
		} catch (JSONException e) {

		}


	}

	private void userNotificationJson ( String str, boolean inTransaction  ) {

		new UserNotificationJsonUtil( str , flag , inTransaction );
	}

	private void teamJson ( String str, boolean inTransaction ) {

		try {
			TeamJsonUtil teamJsonUtil = new TeamJsonUtil( inTransaction );
			
			
			vTeamId = teamJsonUtil.parseData( new JSONObject( str ) );
		} catch (JSONException e) {

		}
	}

	private void associatedTeamJson ( String str, boolean inTransaction ) {

		try {
			AssociatedContestsTeamJsonUtil associatedContestsTeamJsonUtil = new AssociatedContestsTeamJsonUtil( inTransaction );
			vTeamId = associatedContestsTeamJsonUtil.parseData( new JSONObject( str ) );
		} catch (JSONException e) {

		}
	}
	

	public String getTeamId () {
		return vTeamId;
	}

	public String getCompetitionId () {
		return vCompetitionId;
	}

	private void sportsLiveJson ( String str , boolean inTransaction ) {

		try {
			new SportsLiveJsonUtil ( new JSONObject( str ) , inTransaction );
		} catch (JSONException e) {

		}
	}

	
	private void searchFriendJson ( String str, boolean inTransaction ) {

		try {
			new SearchFriendsJsonUtil(new JSONObject( str ), vGapId , inTransaction );
		} catch (JSONException e) {

		}
	}

	private void roundJson ( String str, boolean inTransaction ) {
		try {
			RoundJsonUtil roundJsonUtil = null;
			if ( vCompetitionRoundId == null ) {
				roundJsonUtil = new RoundJsonUtil( new JSONObject( str ), inTransaction  );
			} else {
				roundJsonUtil = new RoundJsonUtil( new JSONObject( str ) , vCompetitionRoundId, inTransaction  );
			}
			vRoundId = roundJsonUtil.getRoundId();
			vRoundContestId = roundJsonUtil.getRoundContestId();

		} catch (JSONException e) {

		} 
	}
	public String getRoundId () {
		return vRoundId;
	}
	public String getRoundContestId () {
		return vRoundContestId;
	}

	public String getConversationId () {
		return vConversationId;
	}
	private void roundContestJson ( String str, boolean inTransaction ) {
		try {
			new RoundContestJsonUtil ( new JSONObject( str ) , inTransaction );
		} catch (JSONException e) {

		}

	}

	private void recentActivityJson ( String str, boolean inTransaction ) {

		try {
			new RecentActivityJsonUtil (  new JSONObject(str), vUserId, inTransaction );
		} catch (JSONException e) {
			Logs.show(e);
		}

	}
	private void notificationJson ( String str, boolean inTransaction ) {


		try {
			new NotificationJsonUtil (  new JSONObject(str), vUserId, inTransaction );
		} catch (JSONException e) {
			Logs.show(e);
		}

	}

	private void liveFriendJson ( String str, boolean inTransaction  ) {

		try {
			new LiveFriendsJsonUtil(new JSONObject( str),playupFriends, inTransaction );
		} catch (JSONException e) {

		}
	}

	private void leagueJson ( String str , boolean inTransaction ) {

		try {
			new LeagueJsonUtil ( new JSONObject( str ), vSportsId, inTransaction  );
		} catch (JSONException e) {

		}

	}
	private void getfriendJson ( String str, boolean inTransaction ) {

		try {
			new GetFriendsJsonUtil(new JSONObject( str ), vGapId,friendsRefresh, inTransaction  );
		} catch (JSONException e) {

		}
	}

	private void friendJson ( String str, boolean inTransaction ) {

		try {
			new FriendsJsonUtil (  new JSONObject(str), vUserId, inTransaction );
		} catch (JSONException e) {
			Logs.show(e);
		}

	}

	private void conversationMessageJson ( String str, boolean inTransaction  ) {

		try {
			new ConversationsMessagesJsonUtil ( new JSONObject( str ) , vConversationId, flag, flag2, inTransaction  );
		} catch (JSONException e) {

		}

	}
	private void conversationJson ( String str , boolean inTransaction ) {
		try {

			ConversationJsonUtil conversationJsonUtil = null;
			if ( iOrder > -1 ) {
				conversationJsonUtil = new ConversationJsonUtil(new JSONObject( str ), iOrder , inTransaction );
			} else {

				if ( vPushId == null ) {
					conversationJsonUtil = new ConversationJsonUtil(new JSONObject( str ) , inTransaction );
				} else {
					conversationJsonUtil = new ConversationJsonUtil(new JSONObject( str ), vPushId , inTransaction );
				}
			}

			vConversationId = conversationJsonUtil.getConversationId();
		} catch (JSONException e) {

		}
	}
	private void  conversationFriendsJson ( String str, String vConversationId , boolean inTransaction )  {
		try {
			new ConversationFriendsJsonUtil( new JSONObject( str ), vConversationId, flag, inTransaction );
		} catch (JSONException e) {

		}
	}

	private void contestJson ( String str, boolean inTransaction ) {
		try {
			
			ContestsJsonUtil contestsJsonUtil = null;
			
			/*if(str.contains("\":self\"")){
				str= str.replace("\":self\"","\":self\":\"\",\":href\"");
				Log.e("234", "Changed====>>>>"+str);
			}*/
			if ( vCompetitionLiveId != null ) {
				
				contestsJsonUtil = new ContestsJsonUtil ( new JSONObject( str ), vRoundContestId, vCompetitionLiveId, inTransaction,shouldParseLeague ) ;
				vCompetitionId = contestsJsonUtil.getCompetitionId();
			} else {
				
				contestsJsonUtil = new ContestsJsonUtil ( new JSONObject( str ), vRoundContestId, inTransaction,shouldParseLeague) ;
				vCompetitionId = contestsJsonUtil.getCompetitionId();
			}
		} catch (JSONException e) {
			Logs.show(e);

		}
	}
	
	
	private void associatedContestJson(String str, boolean inTransaction) {
		try {
			Log.e("123","inside getAssoiciatedContestsData jsonUtl>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
			
			new AssociatedContestJson( new JSONObject( str ), inTransaction);
		} catch (JSONException e) {

		}
		
	}
	
	
	private void  contestLobbyJson ( String str, boolean inTransaction ) {
		try {
			ContestLobbyJsonUtil contestLobbyJsonUtil = new ContestLobbyJsonUtil ( new JSONObject( str ), inTransaction, vLinkUrl  );
			vContestId = contestLobbyJsonUtil.getContestId();
		} catch (JSONException e) {

		}
	}

	private void allRoundJson ( String str, boolean inTransaction ) {
		new AllRoundJsonUtil ( str, inTransaction,vCompetitionIdForRound );
	}

	private void addFriendJson ( String str , boolean inTransaction ) {

		new AddFriendJsonUtil( str , inTransaction );
	}


	/**
	 * getting the data from base url and setting the urls 
	 */
	private void baseData ( String str ) {

		// parsing the json response and setting corresponding urls
		//str	=	str.replace("\":self\":\"http://staging.api.playupdev.com/sections/default\"", "\":href\":\"http://staging.api.playupdev.com/sections/default\"");
		//Log.e("123", "Replaced String-------------"+str);
		new BaseJsonUtil ( str );
		

		
		if(isCredentialsExpirationSecanrio){
			
			Log.e("123","Json Util   baseData isCredentialsExpirationSecanrio>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  ");
			
					try {
						
						Constants.name	=	null;
						Constants.userName	= null;
						Constants.isAnonymous	=	true;

						
						DatabaseUtil.getInstance().dropTablesForExpirationScenario();
						
						Constants.isLoggedIn	=	false;

						PlayUpActivity.runnableList.clear();

						PlayUpActivity.runnableList = new Hashtable<String, Boolean>();
						PlayUpActivity.cancelTimers();


						
					} catch (Exception e) {
						
						Logs.show ( e );
					}

				


			
			try {
				NotificationManager nMgr = (NotificationManager) PlayUpActivity.context.getSystemService( Context.NOTIFICATION_SERVICE );
				nMgr.cancelAll();
			} catch (Exception e) {
				Logs.show(e);
			}


		}
		

		
		// TO DO 
		// check  for the user token if available in database 
		// if available then no need to fetch profile data.
		// fetch profile
		
		new Util().getProfileData();

		// also try to fetch all sports data.
		new Thread( new Runnable() {

			@Override
			public void run() {

				boolean credentialAvailable = DatabaseUtil.getInstance().isCredentialAvailable();
				if ( credentialAvailable && !Constants.isAllSportsDownloading  
						&& Util.isInternetAvailable() ) {

					new Util().getAllSports(null);

				}
			}
		}).start();
	}

	/**
	 * parsing  the user data and saving into the database. 
	 * After that it will be proceeded to next task.
	 */
	private void profileData ( String str, boolean isPrimary, boolean inTransaction ) {
		if ( isPrimary ) {
			Util util = new Util ();
			// fetch notification data 
			util.getNotificationData ();
		}
		
		
		
		// parsing and saving to the database.
		new UserJsonUtil ( str, isPrimary, inTransaction );
	

		if ( isPrimary ) {
			// also try to fetch all sports data.
			new Thread( new Runnable() {

				@Override
				public void run() {

					boolean credentialAvailable = DatabaseUtil.getInstance().isCredentialAvailable();
					if ( credentialAvailable && !Constants.isAllSportsDownloading  
							&& Util.isInternetAvailable() ) {

						new Util().getAllSports(null);

					}
				}
			}).start();
		}
		

	}

	/**
	 * 
	 * Parsing User Token: Once the login is Success  
	 */
	private void userToken ( String str, boolean inTransaction  ) {

		// parsing and saving to the database.
		try {
			new TokenJsonUtil( new JSONObject(str), inTransaction );
		} catch (JSONException e) {
			// TODO Auto-generated catch block

		}
	}


	private void contestConversations ( String str, boolean inTransaction ) {

		// parsing and saving to the database.
		try {
			new ContestsConversationsJsonUtil( new JSONObject(str), inTransaction );
		} catch (JSONException e) {
			// TODO Auto-generated catch block

		}

	}

	private void messagePost ( String str, boolean inTransaction  ) {

		// parsing and saving to the database.
		try {
			new ContestsConversationsJsonUtil( new JSONObject(str), inTransaction  );
		} catch (JSONException e) {
			// TODO Auto-generated catch block

		}

	}	

	private void signOut ( String str, boolean inTransaction ) {

		// parsing and saving to the database.
		try {
			new ContestsConversationsJsonUtil( new JSONObject(str), inTransaction );
		} catch (JSONException e) {
			// TODO Auto-generated catch block

		}

	}

	/**
	 * parsing  the user data and saving into the database. 
	 * After that it will be proceeded to next task.
	 */
	private void contextData ( final String str, boolean inTransaction ) {

		// parsing and saving to the database.
		try {
			new ContextJsonUtil( new JSONObject(str), inTransaction );
		} catch (JSONException e) {
			// TODO Auto-generated catch block

		}

		// getting the context urls from the context table and getting the details from those urls 
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		List < String > urls = dbUtil.getContextUrls ();

		// remove context url from context table.
		dbUtil.deleteContextUrls ();

		// validating the url list.
		if ( urls != null ) {

			int len = urls.size();

			for ( int i = 0; i < len ; i ++) {

				// getting the detailed content from the context url.
				Util util = new Util ();
				util.getContextDetailedData ( urls.get( i ) );
			}
		}

	}


	/**
	 * parsing  the user data and saving into the database. 
	 * After that it will be proceeded to next task.
	 */
	private void contextDetailedData ( final String str , boolean inTransaction ) {

		// parsing and saving into the database.
		try {
			new ContextDetailedJsonUtil ( new JSONObject(str), inTransaction );
		} catch (JSONException e) {
			// TODO Auto-generated catch block

		}
	}


	/**
	 *  parsing the provider detail ad saving into the database. 
	 */
	private void providerData ( final String str, boolean inTransaction ) {

		//  parse and save the provider detail into the database.
		try {
			new ProviderJsonUtil ( new JSONObject(str ), inTransaction ) ;
		} catch (JSONException e) {
			Logs.show(e);

		} 
	}

	public void setIsFromGeoTag(boolean fromGeoTag) {
		this.isFromGeoTag  = fromGeoTag;
		
	}

	public void shouldParseLaegue(boolean shouldParseLeague) {
		this.shouldParseLeague = shouldParseLeague;
		
	}

	public void setCompetionIdForRound(String vCompetitionId) {
		this.vCompetitionIdForRound  = vCompetitionId;
		
	}

	public void setContentId(String vContentId) {
		this.vContentId  = vContentId;
		
		
	}


	public void setCurrentRegion(String currentRegion) {
	this.currentRegion  = currentRegion;
	}



	public void setIsConversationUrlHref(boolean isConversationUrlHref) {
		this.isConversationUrlHref  = isConversationUrlHref;
		
	}


	public void setIsCredentialExpirationScenario(
			Boolean isCredentialsExpirationSecanrio) {
		this.isCredentialsExpirationSecanrio  = isCredentialsExpirationSecanrio;
		
	}


	public void setIsDirectConversationUrlHref(boolean isDirectConversationUrlHref) {
		this.isDirectConversationUrlHref  = isDirectConversationUrlHref;
		
	}



	

	

}