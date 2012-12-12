package com.playup.android.util.json;


import org.json.JSONObject;



import android.util.Log;

import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;

import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class UserJsonUtil {

	private static final String PROVIDER_TOKENS_KEY =  "provider_tokens";
	private static final String HREF_KEY = ":href";
	private final String SELF_KEY               =  ":self";
	private final String HREF_URL_KEY               =  ":href";
	private final String UID_KEY                =  ":uid";
	private final String ID_KEY                 =  "id";
	private final String NAME_KEY 				=  "name";
	private final String USER_NAME_KEY 			=  "username";
	private final String IS_ANNONYMOUS_KEY      =  "anonymous";
	private final String AVATAR_URL_KEY 		=  "avatar";

	private final String TYPE_KEY        	=  ":type";

	private final String FAN_KEY = "fan";
	private final String PUSH_REGISTRATION_KEY = "push_registrations";

	private final String FRIENDSHIP_STATUS_KEY      = "friendship_status";
	private final String STATUS_KEY      = "status";

	private final String DIRECT_CONVERSATIONS_KEY  = "direct_conversations";
	private final String DIRECT_CONVERSATION_KEY  = "direct_conversation";
	
	private final String PLAYUP_FRIENDS_KEY = "playup_friends";
	private final String SIGNOUT_KEY       	=  "sign_out";
	
	private final String LOBBY_KEY       	=  "lobby";
	
	//private final String IDENTITY_KEY 		=  "identities";

	private boolean isPrimary = false;
	private boolean inTransaction = false;
	
	public UserJsonUtil ( String str, boolean isPrimary, boolean inTransaction ) {
		
		this.inTransaction = inTransaction;
		this.isPrimary = isPrimary;
		if ( str != null && str.trim().length() > 0 ) {
			
		
			parseData ( str ); 
		}
	}

	/**
	 * 
	 * 
	 * @param str
	 * Has Two Section One Section to 
	 * Parse Profile Content
	 * Parse Provider Content
	 */
	private void parseData ( String str ) {
		JSONObject jsonObj  = null;
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
		
		
		

		
		if ( !inTransaction ) {
//			Logs.show ( "begin ------------------------------------UserJsonUtil ");
			dbUtil.getWritabeDatabase().beginTransaction();
	
		}
				// }

		try {
			/*
			 * 
			 * Parsing Profile Content
			 */
			jsonObj = new JSONObject( str );
			
			if(!(jsonObj.getString(TYPE_KEY).equalsIgnoreCase(Types.PROFILE_DATA_TYPE) ||
					jsonObj.getString(TYPE_KEY).equalsIgnoreCase(Types.FAN_PROFILE_DATA_TYPE)))
				return;
			
			
			// getting the ids
			String uid = jsonObj.getString( UID_KEY );
			
			new Util().setColor(jsonObj);
			
		
			if ( jsonObj.has( FAN_KEY ) ) {
				
				JSONObject fan_jObj = jsonObj.getJSONObject( FAN_KEY );
				if(fan_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.FAN_DATA_TYPE))
					uid = fan_jObj.getString( UID_KEY );
			}
			int id = -1;
			if ( jsonObj.has( ID_KEY )  ) {
				id = jsonObj.getInt( ID_KEY );
			}



			// checking for annonymous user
			String annonymous = null;
			if ( jsonObj.has( IS_ANNONYMOUS_KEY ) ) {
				annonymous = jsonObj.getString( IS_ANNONYMOUS_KEY );
			}
			boolean isAnonymous = true;
			if ( isPrimary ) {
				Constants.isAnonymous = true;
			}
			

			if ( annonymous != null && annonymous.trim().length() > 0 ) {
				if ( annonymous.trim().equalsIgnoreCase("false")) {
					
					isAnonymous = false;
					if ( isPrimary ) {
						Constants.isAnonymous = false;
					}
				}
			}
			// getting the user name 
			
			String name = jsonObj.getString( NAME_KEY );
			String userName = name;
			if ( isPrimary ) {
				Constants.userName = name;
				Constants.name = name;
			}
			
			if ( jsonObj.has( USER_NAME_KEY )) {
				
				userName = jsonObj.getString( USER_NAME_KEY );
				
				if ( isPrimary ) {
					Constants.userName = userName;
				}
				
				PlayupLiveApplication.callUpdateOnFragments(null);
				

				
				
			}	
			
			String avatar_url = jsonObj.getString( AVATAR_URL_KEY );
			String selfUrl = jsonObj.optString( SELF_KEY );
			String hrefUrl = jsonObj.optString( HREF_URL_KEY );

			dbUtil.setHeader ( hrefUrl,selfUrl, jsonObj.getString( TYPE_KEY ), false  );

			//	dbUtil.updaeRootResource( "profile" , selfUrl  );





			/**
			 * 
			 *Signout URL 
			 */

			JSONObject  signOut		=  jsonObj.optJSONObject(SIGNOUT_KEY);
			String signOutURL		=	"";
			String signOutURLtype	=	"",signOutHrefURL="";
			if(signOut!=null){
				if(signOut.optString(TYPE_KEY).equalsIgnoreCase(Types.SHARE_DATA_TYPE)){
				signOutURL				=	signOut.optString(SELF_KEY);
				signOutHrefURL				=	signOut.optString(HREF_URL_KEY);
			
				
				signOutURLtype			=	signOut.optString(TYPE_KEY);
				dbUtil.setHeader (signOutHrefURL, signOutURL, signOutURLtype, false   );
				}

			}

			String vLobbyId = null;
			String vLobbyUrl = null,vLobbyHrefUrl=null;
			
			if (  jsonObj.has( LOBBY_KEY ) ) {
				
				JSONObject lobby_jObj = jsonObj.getJSONObject( LOBBY_KEY );
				if(lobby_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.MY_LOBBY_DATA_TYPE)){
				vLobbyId = lobby_jObj.getString( UID_KEY );
				vLobbyUrl = lobby_jObj.optString( SELF_KEY );
				vLobbyHrefUrl = lobby_jObj.optString( HREF_URL_KEY );
				
				dbUtil.setHeader(vLobbyHrefUrl, vLobbyUrl, lobby_jObj.getString( TYPE_KEY ), false );
				}
				
			}


			String vFriendshipStatusUrl = null;
			String vFriendshipStatusId = null;
			String status = null,vFriendshipStatusHrefUrl=null;

			// parse friendship status
			if ( jsonObj.has( FRIENDSHIP_STATUS_KEY ) ) {
				JSONObject friendShip_jObj = jsonObj.getJSONObject( FRIENDSHIP_STATUS_KEY );
				if(friendShip_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.FRIENDSHIP_STATUS_DATA_TYPE)){
				vFriendshipStatusUrl = friendShip_jObj.optString( SELF_KEY );
				vFriendshipStatusHrefUrl = friendShip_jObj.optString( HREF_URL_KEY );
				vFriendshipStatusId = friendShip_jObj.getString( UID_KEY );
				status  = friendShip_jObj.getString( STATUS_KEY );

				dbUtil.setHeader( vFriendshipStatusHrefUrl,vFriendshipStatusUrl, friendShip_jObj.getString( TYPE_KEY ) , false  );
				}
			}

			String vPushNotificationUrl = null,vPushNotificationHrefUrl=null;
			if ( jsonObj.has( PUSH_REGISTRATION_KEY ) ) {
				JSONObject push_jObj = jsonObj.getJSONObject( PUSH_REGISTRATION_KEY );
				
				if( push_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.SHARE_DATA_TYPE)){

				vPushNotificationUrl = push_jObj.optString( SELF_KEY );
				vPushNotificationHrefUrl = push_jObj.optString( HREF_URL_KEY  );

				dbUtil.setHeader(vPushNotificationHrefUrl, vPushNotificationUrl , push_jObj.getString( TYPE_KEY ), false   );
				}
			}

			
			
			String vDirectConversationUrl = "",vDirectConversationId = "",vDirectConversationHrefUrl="";
			if( jsonObj.has(  DIRECT_CONVERSATION_KEY )){
				
				JSONObject direct_jObj = jsonObj.getJSONObject( DIRECT_CONVERSATION_KEY );
				if(direct_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.DIRECT_CONVERSATION_DATA_TYPE) || direct_jObj.getString( TYPE_KEY ).equalsIgnoreCase( Types.DIRECT_CONVERSATION_LOBBY_DATA_TYPE ) ){
				
				vDirectConversationUrl = direct_jObj.optString( SELF_KEY );
				vDirectConversationHrefUrl = direct_jObj.optString( HREF_URL_KEY );
				if ( direct_jObj.has( UID_KEY )) {
					vDirectConversationId = direct_jObj.getString( UID_KEY );
				}
				
				dbUtil.setHeader( vDirectConversationHrefUrl,vDirectConversationUrl, direct_jObj.getString( TYPE_KEY ), false );
				/*dbUtil.setUserDirectConversation (vDirectConversationId, vDirectConversationUrl, uid );*/
				//Log.e("123", "vDirectConversationUrl-------if---------->>>>"+vDirectConversationUrl);
				//Log.e("123", "vDirectConversationHrefUrl--------if--------->>>>"+vDirectConversationHrefUrl);
				dbUtil.setUserDirectConversation (vDirectConversationId, vDirectConversationUrl,vDirectConversationHrefUrl,true, uid );
				
				}
				new Util().releaseMemory ( direct_jObj );
				direct_jObj = null;
			} else if( jsonObj.has(  DIRECT_CONVERSATIONS_KEY )){
				JSONObject direct_jObj = jsonObj.getJSONObject( DIRECT_CONVERSATIONS_KEY );
				if(direct_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.DIRECT_CONVERSATION_DATA_TYPE)  || direct_jObj.getString( TYPE_KEY ).equalsIgnoreCase( Types.DIRECT_CONVERSATION_LOBBY_DATA_TYPE ) ) {
				vDirectConversationUrl = direct_jObj.optString( SELF_KEY );
				vDirectConversationHrefUrl = direct_jObj.optString( HREF_URL_KEY);
				if ( direct_jObj.has( UID_KEY )) {
					vDirectConversationId = direct_jObj.getString( UID_KEY );
				}
				//Log.e("123", "vDirectConversationUrl-----------ee------>>>>"+vDirectConversationUrl);
				//Log.e("123", "vDirectConversationHrefUrl-------ee---------->>>>"+vDirectConversationHrefUrl);
				dbUtil.setHeader(vDirectConversationHrefUrl, vDirectConversationUrl, direct_jObj.getString( TYPE_KEY ), false );
		/*		dbUtil.setUserDirectConversation (vDirectConversationId, vDirectConversationUrl, uid );*/
				
				
				dbUtil.setUserDirectConversation (vDirectConversationId, vDirectConversationUrl,vDirectConversationHrefUrl,true, uid );
				}
				
				new Util().releaseMemory ( direct_jObj );
				direct_jObj = null;
			}
			
			
			String vPlayUpFriendUrl = null,vPlayUpFriendHrefUrl=null;
			if ( jsonObj.has( PLAYUP_FRIENDS_KEY ) ) {
				
				JSONObject playup_friends_jObj = jsonObj.getJSONObject( PLAYUP_FRIENDS_KEY );
				if(playup_friends_jObj.getString( TYPE_KEY).equalsIgnoreCase(Types.PLAYUP_FRIENDS_DATA_TYPE)){
				vPlayUpFriendUrl = playup_friends_jObj.optString( SELF_KEY );
				vPlayUpFriendHrefUrl = playup_friends_jObj.optString( HREF_URL_KEY );
				
				dbUtil.setHeader(vPlayUpFriendHrefUrl,  vPlayUpFriendUrl, playup_friends_jObj.getString( TYPE_KEY) , false );
				}
			}


		/*	dbUtil.setUserData ( name, userName, avatar_url, uid, id, isAnonymous, selfUrl, isPrimary,signOutURL, vFriendshipStatusId, vFriendshipStatusUrl, status, vPushNotificationUrl, vPlayUpFriendUrl, vLobbyId, vLobbyUrl );*/
			dbUtil.setUserData ( name, userName, avatar_url, uid, id, isAnonymous, selfUrl,hrefUrl, isPrimary,signOutURL,signOutHrefURL, vFriendshipStatusId, vFriendshipStatusUrl,vFriendshipStatusHrefUrl, status, vPushNotificationUrl,vPushNotificationHrefUrl, vPlayUpFriendUrl,vPlayUpFriendHrefUrl, vLobbyId, vLobbyUrl,vLobbyHrefUrl );

			
			
			
			if(jsonObj.has("providers")) {
				JsonUtil json = new JsonUtil();
				json.parse( new StringBuffer( new JSONObject(str).toString() ) , Constants.TYPE_PROVIDER,  true );
			}

			JsonUtil json = new JsonUtil();
			json.setUserId( uid );
			json.parse( new StringBuffer( new JSONObject(str).toString() ) , Constants.TYPE_RECENT_ACTIVITY_JSON,  true );

			json = new JsonUtil();
			json.setUserId( uid );
			json.parse( new StringBuffer( new JSONObject(str).toString() ) , Constants.TYPE_NOTIFICATION_JSON,  true );


			// parsing friends 
			json = new JsonUtil();
			json.setUserId( uid );
			json.parse( new StringBuffer( new JSONObject(str).toString() ) , Constants.TYPE_FRIEND_JSON,  true );
			
			if(isPrimary){
			JSONObject providerTokens = jsonObj.getJSONObject(PROVIDER_TOKENS_KEY);
			String href = "";
			String self = "";
			
			if(providerTokens.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
				
				
			if(providerTokens.has(HREF_KEY)){
				
				
				
				href = providerTokens.getString(HREF_KEY);
				
				
				
				
				
			}else if(providerTokens.has(SELF_KEY)){
				
				
				
				self = providerTokens.getString(SELF_KEY);
				
			}
			
			dbUtil.setHeader(href,self,providerTokens.getString(TYPE_KEY),false);
				
			dbUtil.setProviderTokenUrl(href,self,uid);
			}
		}
			
			

			if(isPrimary){
			
//				if(! (PlayUpActivity.runnableList.containsKey(Constants.GET_PROVIDER_TOKENS) && 
//						PlayUpActivity.runnableList.get(Constants.GET_PROVIDER_TOKENS))){
//				PlayUpActivity.runnableList.put(Constants.GET_PROVIDER_TOKENS, true);
//				new Util().getProviderTokens();
					
//			}
				
				new Util().getProviderTokens();
			
			PlayUpActivity.registerC2DM();
			if(! (PlayUpActivity.runnableList.containsKey(Constants.GET_RECENT_ACTIVITY_DATA) && 
					PlayUpActivity.runnableList.get(Constants.GET_RECENT_ACTIVITY_DATA))){
				
				PlayUpActivity.runnableList.put(Constants.GET_RECENT_ACTIVITY_DATA,true);
				new Util().getRecentActivityData();
				
			}
				
			if(! (PlayUpActivity.runnableList.containsKey(Constants.GET_DIRECT_CONVERSATION_DATA) && 
					PlayUpActivity.runnableList.get(Constants.GET_DIRECT_CONVERSATION_DATA))){
				
				PlayUpActivity.runnableList.put(Constants.GET_DIRECT_CONVERSATION_DATA,true);
				
				new Util().getDirectConversationData ();
			}
			
			
			
			if(! (PlayUpActivity.runnableList.containsKey(Constants.GET_USER_NOTIFICATION_DATA) && 
					PlayUpActivity.runnableList.get(Constants.GET_USER_NOTIFICATION_DATA))){				
				PlayUpActivity.runnableList.put(Constants.GET_USER_NOTIFICATION_DATA,true);
				new Util().getUserNotificationData( true );
				
			}
			
			
			if(! (PlayUpActivity.runnableList.containsKey(Constants.GET_PLAYUP_FREINDS_DATA) && 
					PlayUpActivity.runnableList.get(Constants.GET_PLAYUP_FREINDS_DATA))){				
				PlayUpActivity.runnableList.put(Constants.GET_PLAYUP_FREINDS_DATA,true);
				new Util().getPlayUpFriendsData();
				
			}
			
			
			
			
			
			new Util().getUserLobbyData ();
			}

		} catch ( Exception e) {
			Logs.show(e);
		}  finally {
			
			new Util().releaseMemory(jsonObj);
			
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			
//			new Util().releaseMemory(jsonObj);
			if ( !inTransaction ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( "end ------------------------------------UserJsonUtil ");
			 }
		}
	}
}
