package com.playup.android.util;


import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Typeface;

import com.playup.android.application.PlayupLiveApplication;


public final class Constants {


//	public static final String API_VERSION_KEY = "X-Playup-Api-Version";
	
	public static final String API_VERSION_KEY = "X-Playup-API-Version";
	
	public static final String API_VERSION = "3";

	public static final String ACCEPT_LANGUAGE_KEY = "Accept-Language";
	public static String ACCEPT_LANGUAGE = null;
	public static final String PLAYUP_API_KEY_KEY = "X-Playup-Api-Key";
	
	//public static final String PLAYUP_API_KEY_KEY = "X-Playup-API-Key";
	public static final String PLAYUP_API_KEY = "com.playup.android.live";
	public static final String PLAYUP_API_SECRET 		= "E5mTg5OdM4NmQxGa";


	public static final String ACCEPT_ENCODING_KEY 	= "Accept-Encoding";
	public static final String ACCEPT_ENCODING 		= "gzip";

	public static final String CONNECTION_KEY = "Connection";
	public static final String CONNECTION = "keep-alive";

	public static final String AUTHORIZATION_KEY = "Authorization";
	public static final String IF_NONE_MATCH_KEY = "If-None-Match";
	public static final String AUTHORIZATION_TOKEN_KEY = "x-authorization-token";

	public static final String CONTENT_LOCATION = "content-location";
	public static final String RESPONSE_GEO_TAG = "x-playup-geoip-region-code";
	
	public static final String REQUEST_GEO_TAG = "X-PlayUp-Geoip-Region-Code";




	public static final String CACHE_CONTROL_KEY = "cache-control";
	public static final String CACHE_MAX_AGE = "max-age";
	public static final String ISNEW_CONTENT_AVAIL_KEY = "status";
	public static final int NO_NEW_CONTENT_AVAIL = 304;


	public static final String ACCEPT_TYPE_ROOT = "application/vnd.playup.root+json";
	public static final String ACCEPT_TYPE_PROFILE = "application/vnd.playup.profile+json";
	public static final String ACCEPT_TYPE_RECENT_ACTIVITY = "application/vnd.playup.recent_activity+json";


	public static final String ACCEPT_TYPE_COLLECTION = "application/vnd.playup.collection+json";
	public static final String ACCEPT_TYPE_NOTIFICATION = "application/vnd.playup.notification+json";

	public static final String ACCEPT_TYPE_FAN_PROFILE = "application/vnd.playup.fan.profile+json";

	public static final String ACCEPT_TYPE_COVERSATION = "application/vnd.playup.conversation+json";

	public static final String ACCEPT_TYPE_CONTEST = "application/vnd.playup.contest+json";

	public static final String ACCEPT_TYPE_COMPETITION = "application/vnd.playup.sport.competition+json";

	public static final String ACCEPT_TYPE_CONTEST_LOBBY_JSON = "application/vnd.playup.contest.lobby+json";

	public static final String ACCEPT_TYPE_MY_CONTEST_LOBBY_JSON = "application/vnd.playup.my.lobby+json";

	public static final String ACCEPT_TYPE_SUBJECT = "application/vnd.playup.contest.football+json";

	public static final String ACCEPT_TYPE_GAP = "application/vnd.playup.collection.gap+json";

	public static final String ACCEPT_TYPE_INVITATION = "application/vnd.playup.invitation+json";


	public static final String ACCEPT_TYPE_FRIEND_FACEBOOK = "application/vnd.playup.friend+json;provider=facebook";
	public static final String ACCEPT_TYPE_SPORTS_JSON = "application/vnd.playup.sport.sports+json";


	public static final String ACCEPT_TYPE_MESSAGES = "application/vnd.playup.collection+json";
	public static final String ACCEPT_TYPE_MESSAGES_TEXT = "application/vnd.playup.message.text+json";

	public static final String ACCEPT_TYPE_FIXTURES_JSON = "application/vnd.playup.sport.season+json";
	public static final String ACCEPT_TYPE_GROUPING_OLYMPICS = "application/vnd.playup.groupings.olympics+json";


	public static final String ACCEPT_TYPE_SECTION_JSON = "application/vnd.playup.section+json";

	public static final String ACCEPT_TYPE_PRESENCE	 = "application/vnd.playup.presence+json";

	public static final String ACCEPT_TYPE_DIRECT_CONVERSATION = "application/vnd.playup.direct.conversation+json";

	public static final String ACCEPT_TYPE_SOLID = "application/vnd.playup.display.tile.solid+json";



	public static final String ACCEPT_TYPE_HTML = "text/html";

	public static final String ACCEPT_TYPE_VIDEO = "video/mp4";
	
	public static final String ACCEPT_TYPE_AUDIO = "audio/mpeg";
	
	

	public static String searchFriendsResults = "";
	public static boolean friendsUpdated = false;



	public static boolean gapNotificationDownloading  = false;

	// life cycle 
	public static boolean isCurrent = true;
	// density
	public static String  DENSITY = "medium";
	public static int  DPI = 160;

	public static int ANIMATION_HT = 244;
	public static int Y = 0;
	public static int X = 0;


	public static String NEXT_URL_CONVERSATION_MESSAGES="";


	public static final int TYPE_BASE_URL 				= 	0;
	public static final int TYPE_PROFILE_URL 			= 	TYPE_BASE_URL + 1;
	public static final int TYPE_PRIMARY_PROFILE_URL 	= 	TYPE_PROFILE_URL + 1;

	public static final int TYPE_NOTIFICATION_URL 		= 	TYPE_PRIMARY_PROFILE_URL + 1;
	public static final int TYPE_CONTEXT_URL 			= 	TYPE_NOTIFICATION_URL + 1;
	public static final int TYPE_LOGIN_PROVIDER_URL 	= 	TYPE_CONTEXT_URL + 1;
	public static final int TYPE_CONTEXT_DETAILED_URL 	= 	TYPE_LOGIN_PROVIDER_URL + 1;
	public static final int TYPE_PROVIDER				= 	TYPE_CONTEXT_DETAILED_URL + 1 ;
	public static final int TYPE_STORE_TOKEN			=	TYPE_PROVIDER + 1;
	public static final int TYPE_CONTEST_COVERSATIONS	=	TYPE_STORE_TOKEN + 1;
	public static final int TYPE_MESSAGE_POST			=	TYPE_CONTEST_COVERSATIONS + 1;



	public static final int TYPE_ADD_FRIEND_JSON = TYPE_MESSAGE_POST + 1;
	public static final int TYPE_ALL_ROUND_JSON = TYPE_ADD_FRIEND_JSON + 1;
	public static final int TYPE_CONTEST_LOBBY_JSON = TYPE_ALL_ROUND_JSON + 1;
	public static final int TYPE_CONTEST_JSON = TYPE_CONTEST_LOBBY_JSON + 1;
	public static final int TYPE_CONVERSATION_FRIENDS_JSON = TYPE_CONTEST_JSON + 1;
	public static final int TYPE_CONVERSATION_JSON = TYPE_CONVERSATION_FRIENDS_JSON + 1;
	public static final int TYPE_CONVERSATION_MESSAGES_JSON = TYPE_CONVERSATION_JSON + 1;
	public static final int TYPE_FRIEND_JSON = TYPE_CONVERSATION_MESSAGES_JSON + 1;
	public static final int TYPE_LEAGUE_JSON = TYPE_FRIEND_JSON + 1;
	public static final int TYPE_GET_FRIENDS_JSON = TYPE_LEAGUE_JSON + 1;
	public static final int TYPE_LIVE_FRIEND_JSON = TYPE_GET_FRIENDS_JSON + 1;
	public static final int TYPE_NOTIFICATION_JSON = TYPE_LIVE_FRIEND_JSON + 1;
	public static final int TYPE_RECENT_ACTIVITY_JSON = TYPE_NOTIFICATION_JSON + 1;
	public static final int TYPE_ROUND_CONTEST_JSON = TYPE_RECENT_ACTIVITY_JSON + 1;
	public static final int TYPE_ROUND_JSON = TYPE_ROUND_CONTEST_JSON + 1;
	public static final int TYPE_SEARCH_FRIEND_JSON = TYPE_ROUND_JSON + 1;
	public static final int TYPE_SEARCH_LIVE_FRIEND_JSON = TYPE_SEARCH_FRIEND_JSON + 1;
	public static final int TYPE_SPORTS_LIVE_JSON = TYPE_SEARCH_LIVE_FRIEND_JSON + 1;
	public static final int TYPE_TEAM_JSON = TYPE_SPORTS_LIVE_JSON + 1;
	public static final int TYPE_USER_NOTIFICATION_JSON = TYPE_TEAM_JSON + 1;

	public static final int TYPE_ALL_SPORTS_JSON = TYPE_USER_NOTIFICATION_JSON + 1;
	public static final int TYPE_DIRECT_CONVERSATION_JSON = TYPE_ALL_SPORTS_JSON + 1;
	public static final int TYPE_DIRECT_MESSAGES_JSON = TYPE_DIRECT_CONVERSATION_JSON + 1;
	public static final int TYPE_DIRECT_MESSAGES_ITEM_JSON = TYPE_DIRECT_MESSAGES_JSON + 1;
	public static final int TYPE_PLAYUP_FRIENDS_JSON = TYPE_DIRECT_MESSAGES_ITEM_JSON + 1;
	public static final int TYPE_PLAYUP_FRIENDS_SEARCH_JSON = TYPE_PLAYUP_FRIENDS_JSON + 1;
	public static final int TYPE_DIRECT_MSGS_JSON = TYPE_PLAYUP_FRIENDS_SEARCH_JSON + 1;

	public static final int TYPE_MY_LOBBY_JSON = TYPE_DIRECT_MSGS_JSON + 1;
	public static final int TYPE_FRIEND_CONVERSATION_JSON = TYPE_MY_LOBBY_JSON + 1;
	public static final int TYPE_FRIEND_CONVERSATION__MESSAGE_JSON =TYPE_FRIEND_CONVERSATION_JSON + 1;
	public static final int TYPE_LEAGUE_ITEM = TYPE_FRIEND_CONVERSATION__MESSAGE_JSON + 1;
	public static final int TYPE_COMPETITON_LIVE =TYPE_LEAGUE_ITEM + 1;
	public static final int TYPE_SECTION = TYPE_COMPETITON_LIVE + 1;
	public static final int TYPE_CURRENT_SEASON_JSON = TYPE_SECTION + 1;

	public static final int TYPE_TILE_DISPLAY_JSON = TYPE_CURRENT_SEASON_JSON + 1;
	public static final int TYPE_DISPLAY_JSON = TYPE_TILE_DISPLAY_JSON + 1;
	public static final int TYPE_ASSOCIATED_CONTEST_JSON = TYPE_DISPLAY_JSON+1;
	public static final int TYPE_REGIONS_JSON = TYPE_ASSOCIATED_CONTEST_JSON+1 ;

	public static final int TYPE_COUNTRIES_JSON = TYPE_REGIONS_JSON+1 ;

	public static final int TYPE_ASSOCIATED_TEAM_JSON = TYPE_COUNTRIES_JSON + 1;

	public static final int TYPE_TEAM_SCHEDULE_JSON = TYPE_ASSOCIATED_TEAM_JSON + 1;
	
	
	public static final int PROVIDER_TOKEN = TYPE_TEAM_SCHEDULE_JSON +  1;



	public static int SUCCESS_POST						=	202;
	public int STATUS_CODE								=	-1;

	public static String HOME_URL;
	public static String SIGNUP_URL;
	public static String LOGIN_ANNONYMOUS_URL;
	public static String CONTEXTS_URL;


	// fragment URI 
	public static final String 	FRAGMENT_URI 			= PlayupLiveApplication.getInstance().getPackageName() + ".fragment.";

	

	// Database name
	public static final String 	DATABASE_NAME  			= "playUpDatabase";
	public static String 	DATABASE_PATH 			= "/data/data/" + PlayupLiveApplication.getInstance().getPackageName() + "/databases/" + DATABASE_NAME;
	public static final int 	DATABASE_VERSION 		= 2;


	// connection 
	public static final int CONNECTION_TIMEOUT 			= 440000;
	public static final int POST_METHOD 				= 0;
	public static final int GET_METHOD 					= POST_METHOD 			+ 1;
	public static final int HEAD_METHOD 				= GET_METHOD 			+ 1;
	public static final int OPTIONS_METHOD 				= HEAD_METHOD + 1;
	public static final int PUT_METHOD 					= OPTIONS_METHOD 		+ 1;
	public static final int TRACE_METHOD 				= PUT_METHOD 			+ 1;
	public static final int DELETE_METHOD 				= TRACE_METHOD 			+ 1;

	// custom methods
	public static final int  		IMAGE_METHOD		= DELETE_METHOD 		+ 1;

	public static final int PUT_METHOD_REDTICKET 				= IMAGE_METHOD 		+ 1;
	
	public static final String POST 				= "POST";
	public static final String GET 					= "GET";
	public static final String PUT 					= "PUT";	
	public static final String DELETE 				= "DELETE";

	







	// return types
	public static final int  TYPE_STRINGBUFFER 			= 0;
	public static final int  TYPE_STREAM 				= 1;
	public static final int  TYPE_BYTE 					= 2;

	// query types 
	public static final int QUERY_INSERT 				= 0;
	public static final int QUERY_UPDATE 				= 1;
	public static final int QUERY_DELETE 				= 2;
	public static final int QUERY_RAW 					= 3;
	public static final int QUERY_EMPTY                 = -1;


	// C2DM
	public static final String C2DM_EMAIL_ADDRESS 		= "android.apps@iplayup.com";
	public static final String C2DM_REGISTRATION_ID     = "C2DM_registration_id";

	// cache directory path
	public static String CACHE_DIR_PATH                = "";

	// menu 
	public static HashMap < String, Integer > menuMap = new HashMap < String, Integer > ();

	// font typeface

	public static Typeface OPEN_SANS_REGULAR = Typeface.createFromAsset(PlayupLiveApplication.getInstance().getApplicationContext().getAssets(), "fonts/OpenSans-Regular.ttf");
	public static Typeface OPEN_SANS_SEMIBOLD = Typeface.createFromAsset(PlayupLiveApplication.getInstance().getApplicationContext().getAssets(), "fonts/OpenSans-Semibold.ttf");
	public static Typeface OPEN_SANS_BOLD = Typeface.createFromAsset(PlayupLiveApplication.getInstance().getApplicationContext().getAssets(), "fonts/OpenSans-Bold.ttf");
	public static Typeface OPEN_SANS_LIGHT = Typeface.createFromAsset(PlayupLiveApplication.getInstance().getApplicationContext().getAssets(), "fonts/OpenSans-Light.ttf");
	public static Typeface BEBAS_NEUE = Typeface.createFromAsset(PlayupLiveApplication.getInstance().getApplicationContext().getAssets(), "fonts/BebasNeue.otf");

	//sport type constants for contests
	public static String SET_BASED_DATA ="application/vnd.playup.sport.contest.set-based+json";
	public static String LEADERBOARD ="application/vnd.playup.sport.contest.leaderboard+json";
	public static String TEST_CRICKET ="application/vnd.playup.sport.contest.cricket.test+json";
	public static String CRICKET ="application/vnd.playup.sport.contest.cricket+json";
	public static String BASKETBALL ="application/vnd.playup.sport.contest.basketball+json";
	public static String BASEBALL ="application/vnd.playup.sport.contest.baseball+json";
	public static String FOOTBALL ="application/vnd.playup.sport.contest.football+json";
	public static String SOCCER ="application/vnd.playup.sport.contest.soccer+json";
	public static String HOCKEY ="application/vnd.playup.sport.contest.hockey+json";
	public static String NFL ="application/vnd.playup.sport.contest.american_football+json";
	public static String AFL ="application/vnd.playup.sport.contest.afl+json";
	public static String RUGBY_LEAGUE ="application/vnd.playup.sport.contest.rugby_league+json";
	public static String RUGBY_UNION ="application/vnd.playup.sport.contest.rugby_union+json";
	public static String ICE_HOCKEY ="application/vnd.playup.sport.contest.ice_hockey+json";
	public static String MOTOR_RACING ="application/vnd.playup.sport.contest.motor_racing+json";
	
	
	
	
	
	
	


	//sport type constants for sports
	public static String SPORT_TEST_CRICKET ="application/vnd.playup.sport.sport.cricket.test+json";
	public static String SPORT_CRICKET ="application/vnd.playup.sport.sport.cricket+json";
	public static String SPORT_BASKETBALL ="application/vnd.playup.sport.sport.basketball+json";
	public static String SPORT_BASEBALL ="application/vnd.playup.sport.sport.baseball+json";
	public static String SPORT_FOOTBALL ="application/vnd.playup.sport.sport.football+json";
	public static String SPORT_SOCCER ="application/vnd.playup.sport.sport.soccer+json";
	public static String SPORT_HOCKEY ="application/vnd.playup.sport.sport.hockey+json";
	public static String SPORT_NFL ="application/vnd.playup.sport.sport.american_football+json";
	public static String SPORT_AFL ="application/vnd.playup.sport.sport.afl+json";
	public static String SPORT_RUGBY_LEAGUE ="application/vnd.playup.sport.sport.rugby_league+json";
	public static String SPORT_RUGBY_UNION ="application/vnd.playup.sport.sport.rugby_union+json";
	public static String SPORT_ICE_HOCKEY ="application/vnd.playup.sport.sport.ice_hockey+json";	
	public static String SPORT_MOTOR_RACING ="application/vnd.playup.sport.sport.motor_racing+json";

	public static boolean isAllSportsDownloading = false;

	//public static Typeface OPEN_SANS_SEMIBOLD ;
	//public static Typeface OPEN_SANS_REGULAR ;

	public static final int highightDelay			=	150;
	public static final int threadPoolSize			=	5;
	//public static boolean firstTimePolling		=	true;

	//This implementation needs to be removed later
	//public static HashMap<String, String> MESSAGE_POLL_URLS			=	new HashMap<String, String>();
	//public static String MESSAGE_POLL_URL								=	null;

	/**
	 * 
	 * Runnable Names
	 */

	public static final String GET_ROUND_DATA				=	"GET_ROUND_DATA";
	public static final String GET_CONTEST_DATA				=	"GET_CONTEST_DATA";

	public static final String GET_SCORE                    = "GET_SCORE";
	
	public static final String GET_LIVE_FRIENDS			=	"GET_LIVE_FRIENDS";
	public static String GET_UPDATE_FRIENDS = "GET_UPDATE_FRIENDS";	

	public static final String REFRESH_LIVE_FRIENDS			=	"REFRESH_LIVE_FRIENDS";
	public static final String REFRESH_OTHER_FRIENDS		=	"REFRESH_OTHER_FRIENDS";
	public static final String REFRESH_ALL_FRIENDS			=	"REFRESH_ALL_FRIENDS";
	public static final String SEARCH_FRIENDS				=	"SEARCH_FRIENDS";

	public static final String GET_LEAGUES					=	"GET_LEAGUES";
	public static final String GET_LIVE_SPORTS				=	"GET_LIVE_SPORTS";


	public static final String GET_CONVERSATION_MESSAGES	=	"GET_CONVERSATION_MESSAGES";
	public static final String GET_CONTEST_CONVERSATION_MESSAGES	=	"GET_CONTEST_CONVERSATION_MESSAGES";
	public static final String GET_CONVERSATION						=	"GET_CONVERSATION";
	public static final String GET_CONVERSATION_FRIENDS				=	"GET_CONVERSATION_FRIENDS";
	public static final String GET_PRIVATE_CONVERSATION_FRIENDS				=	"GET_PRIVATE_CONVERSATION_FRIENDS";
	public static final String GET_CONTEST_LOBBY					=	"GET_CONTEST_LOBBY";

	public static final String GET_COMPETITION_ROUND_DATA			=	"GET_COMPETITION_ROUND_DATA";

	public static final String GET_CURRENT_SEASON_DATA			=	"GET_CURRENT_SEASON_DATA";

	public static final String GET_ALL_SPORTS			=	"GET_ALL_SPORTS";
	public static final String GET_FREINDS_DATA 		=   "GET_FREINDS";

	public static final String GET_DIRECT_CONVERSATION 		=  "GET_DIRECT_CONVERSATION";
	public static final String GET_DIRECT_MESSAGES 		=  "GET_DIRECT_MESSAGES";

	public static final String GET_LIVE_MATCHES 		=  "GET_LIVE_MATCHES";



	public static final String GET_PLAYUP_FREINDS_DATA		=   "GET_PLAYUP_FREINDS_DATA";


	public static final String IS_ANONYMOUS_NOTIFICATIONS_VIEWED =   "IS_ANONYMOUS_NOTIFICATIONS_VIEWED";

	public static final String IS_GEO_TAG_SET =   "IS_GEO_TAG_SET";

	public static final String REGION_TOKEN =   "REGION_TOKEN";

	public static final String GET_PRIVATE_LOBBY_CONVERSATION =   "GET_PRIVATE_LOBBY_CONVERSATION";
	public static final String GET_RECENT_ACTIVITY_DATA = "GET_RECENT_ACTIVITY_DATA";
	public static final String GET_USER_NOTIFICATION_DATA = "GET_USER_NOTIFICATION_DATA";
	public static final String GET_DIRECT_CONVERSATION_DATA = "GET_DIRECT_CONVERSATION_DATA";
	public static final String PUT_FOLLOW_MESSAGE = "PUT_FOLLOW_MESSAGE";
	public static final String IS_FIRST_TIME = "IS_FIRST_TIME";
	public static final String GET_REGIONS = "GET_REGIONS";

	public static final String GET_COUNTRIES_DATA = "GET_COUNTRIES_DATA";

	public static final String GET_ASSOCIATED_CONTEST_DATA = "GET_ASSOCIATED_CONTEST_DATA";

	public static boolean isLoggedIn					=	false;
	public static boolean isConversationDownloading 	= false;
	public static boolean isFriendsGapDownloading 		= false;
	public static boolean isSearchGapDownloading 		= false;



	public static boolean isPlayupFriendsGapDownloading = false;
	public static boolean isPlayupFriendsSearchGapDownloading = false;
	public static int SearchPlayupFriendsCount = 0;

	public static HashMap < String, Boolean  > pushNotificationDownload = new HashMap < String, Boolean  > ();
	public static boolean inDirectMessageFragment = false;
	public static boolean isDownloadingFriendLobbyConversation = false;

	public static String name = null;
	public static String userName = null;
	public static boolean isAnonymous = true;

	public static ArrayList<String> follow = new ArrayList<String>();
	public static boolean isGrayBar = false;
	public static String RegionName = "";
	public static boolean isFetchingCredentials = false;	
	
	
	public final static String STYLE_KEY = "style";
	public final static String MAIN_COLOR_KEY = "main_colour";
	public final static String SEC_COLOR_KEY = "secondary_colour";
	public final static String MAIN_TITLE_COLOR_KEY = "main_text_colour";
	public final static String SECONDARY_TITLE_COLOR_KEY = "secondary_text_colour";
	public final static String HEADER_IMG_KEY = "header_image";
	public final static String DENSITY_KEY = "density";
	public final static String HREF_KEY = "href";
	
	public final static String SELF_KEY          	=  ":self";
	public final static String TYPE_KEY 			= ":type";
	public final static String UID_KEY         	=  ":uid";


	public static final String GET_PROVIDER_TOKENS = "GET_PROVIDER_TOKENS";

	public static final String GET_CREDENTIALS = "GET_CREDENTIALS";

	public static final String TOKEN_APP_IDENTIFIER_VALUE = "live";
	
	public static String PlayUpKey_For_Credentials = null;
	public static final int NOTIFICATION_ID = 10;
	
	

}
