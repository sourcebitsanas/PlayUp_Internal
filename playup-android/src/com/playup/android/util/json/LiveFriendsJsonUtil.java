package com.playup.android.util.json;




import org.json.JSONArray;
import org.json.JSONObject;

import android.R.bool;
import android.content.ContentValues;
import android.util.Log;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class LiveFriendsJsonUtil {
	private static final String LAST_ACTIVITY_KEY= "last_activity";
	private final String NAME_KEY                =  "name";
	private final String SUBJECT_TITLE_KEY       =  "subject_title";
	private final String SUBJECT_KEY             =  "subject";
	private final String ACCESS_KEY              =  "access";
	private final String ACCESS_PERMITTED_KEY    =  "access_permitted";
	private final String LAST_ACTIVITY_SINCE_KEY =  "in_last_activity_since";


	private final String SELF_KEY               =  ":self";
	private final String HREF_URL_KEY               =  ":href";
	private final String UID_KEY                =  ":uid";


	private final String USER_NAME_KEY 			=  "username";
	private final String AVATAR_URL_KEY 		=  "avatar";
	private final String TYPE_KEY        		=  ":type";
	private final String SOURCE_KEY				=	"source";
	private final String ICON_KEY				=	"icon";
	private final String DENSITY_KEY			=	"density";
	private final String HREF_KEY				=	"href";

	private final String ITEMS_KEY				=	"items";
	private final String ONLINE_KEY				=	"online";
	private final String PROFILE_KEY			=	"profile";
	private final String DIRECT_CONVERSATION_KEY   =   "direct_conversation";
	private final String ONLINE_SINCE            =   "online_since";
	

	private final String UNREAD_KEY              =   "unread";
	private boolean playUpFriends = false;
private boolean inTransaction = false;

	public LiveFriendsJsonUtil (  JSONObject jsonObj,boolean playupfriends, boolean inTransaction ) {
		
		
		this.inTransaction = inTransaction;
		this.playUpFriends  = playupfriends;

		if ( jsonObj != null) {
			parseData ( jsonObj );
		}
	}

	private void parseData ( JSONObject jsonObj) {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
		
		if ( !inTransaction ) {
			Logs.show ( "begin ------------------------------------LiveFriendsJsonUtil ");
			dbUtil.getWritabeDatabase().beginTransaction();
			
		}
		// }
		try {

			/**
			 * 
			 * parse general info like 
			 */

			if(!(jsonObj.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)))
				return;
		
			/*String self = jsonObj.getString(SELF_KEY);
				String type = jsonObj.getString(TYPE_KEY);
				String uid = jsonObj.getString(UID_KEY);

				mDatabaseUtil.setHeader ( self, type );	*/	

			/**
			 * ITEMS parse . Friends LIST 
			 * 
			 */
			JSONArray friendsArray	=	jsonObj.getJSONArray(ITEMS_KEY);

			/**
			 * Iterate through Friends ITEMS 
			 */
			if(friendsArray!=null){
			}
			dbUtil.emptyLiveFriends();
			int i =0;
			
			
				if(playUpFriends){
					
					dbUtil.updatePlayupFriends();
					
				}
				
				
				
			
			while(i<friendsArray.length()){


				/**
				 * Friend ITEM ONLY
				 */
				String friendType		=	friendsArray.getJSONObject(i).getString(TYPE_KEY);
				if(!(friendType.equalsIgnoreCase(Types.FRIEND_DATA_TYPE)))
					continue;
				String friendUID		=	friendsArray.getJSONObject(i).getString(UID_KEY);
				String friendName		=	friendsArray.getJSONObject(i).getString(NAME_KEY);
				String friendAvatar		=	friendsArray.getJSONObject(i).getString(AVATAR_URL_KEY);
				String userName			=	friendsArray.getJSONObject(i).optString(USER_NAME_KEY); //Only for playup User
				JSONObject friendSource	=	friendsArray.getJSONObject(i).getJSONObject(SOURCE_KEY);
				String sourceIconHref	="";
				String sourceName		=	friendSource.getString(NAME_KEY);
				JSONArray friendIcon	=	friendSource.getJSONArray(ICON_KEY);
				/**
				 * Iterate through Icons
				 */
				int j	=	0;
				while(j<friendIcon.length()){
					String density			=	friendIcon.getJSONObject(j).getString(DENSITY_KEY);
					if(density.compareToIgnoreCase(Constants.DENSITY)==0){//Store
						sourceIconHref		=	friendIcon.getJSONObject(j).getString(HREF_KEY);
					}

					j++;
				}

				boolean isOnline		=		friendsArray.getJSONObject(i).optBoolean(ONLINE_KEY);
				String onlineSince = friendsArray.getJSONObject(i).optString(ONLINE_SINCE);
				

				String vProfileId = null;
				String vProfileUrl = "",vProfileHrefUrl="";
				if ( friendsArray.getJSONObject( i ).has( PROFILE_KEY ) ) {
					JSONObject profile_jObj = friendsArray.getJSONObject( i ).getJSONObject( PROFILE_KEY );
					if(profile_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.FAN_PROFILE_DATA_TYPE)){
					 vProfileUrl = profile_jObj.optString( SELF_KEY );
					 vProfileHrefUrl = profile_jObj.optString( HREF_URL_KEY );
					vProfileId  = profile_jObj.getString( UID_KEY );

					dbUtil.setHeader( vProfileHrefUrl,vProfileUrl, profile_jObj.getString( TYPE_KEY ) , false  );
					
					ContentValues values1 = new ContentValues ();
					values1.put( "iUserId", vProfileId );
					values1.put( "vSelfUrl", vProfileUrl );
					values1.put( "vHrefUrl", vProfileHrefUrl );
					//dbUtil.setUserData ( values1, vProfileId );
					dbUtil.setUserData ( values1, vProfileId );
					}
				}
				int access =  0,unread = 0;
				String lastActivityUid = "",lastActivityType= "",roomName= "",subjectTitle= "",subjectUid = "",subjectType= ""
					,subjectUrl = "",subjectHrefUrl="", access_permitted = "";
				String lastActivitySince = "";
				JSONObject lastActivity = friendsArray.getJSONObject(i).optJSONObject(LAST_ACTIVITY_KEY);
				if(lastActivity != null){
					if(lastActivity.getString(TYPE_KEY).equalsIgnoreCase(Types.RECENT_CONVERSATION_DATA_TYPE)){
					lastActivityUid = lastActivity.getString(UID_KEY);
					lastActivityType = lastActivity.getString(TYPE_KEY);
					roomName = lastActivity.getString(NAME_KEY);
					subjectTitle = lastActivity.getString(SUBJECT_TITLE_KEY);

					JSONObject subject = lastActivity.getJSONObject(SUBJECT_KEY);
					subjectUid = subject.getString(UID_KEY);
					if(subject.getString(TYPE_KEY).equalsIgnoreCase(Types.PRIVATE_CONVERSATION_DATA_TYPE) ||
							subject.getString(TYPE_KEY).equalsIgnoreCase(Types.CONTEST_LOBBY_CONVERSATION_DATA_TYPE)){
					subjectType = subject.getString(TYPE_KEY);
					subjectUrl = subject.optString(SELF_KEY);
					subjectHrefUrl = subject.optString(HREF_URL_KEY);


					dbUtil.setHeader(subjectHrefUrl,subjectUrl, subjectType , false );
					}

					access = ( lastActivity.getString(ACCESS_KEY).equalsIgnoreCase("public") )? 1 : 0;
					access_permitted = lastActivity.getString(ACCESS_PERMITTED_KEY);
					
					unread = lastActivity.getInt(UNREAD_KEY);
					 lastActivitySince = friendsArray.getJSONObject(i).optString(LAST_ACTIVITY_SINCE_KEY);
					}

				}
				JSONObject direct_conversation_obj = friendsArray.getJSONObject( i ).getJSONObject( DIRECT_CONVERSATION_KEY );
				String directConversationUrl = "",directConversationHrefUrl="";
				if(direct_conversation_obj.getString(TYPE_KEY).equalsIgnoreCase(Types.DIRECT_CONVERSATION_DATA_TYPE)){
				 directConversationUrl = direct_conversation_obj.optString(SELF_KEY);
				 directConversationHrefUrl = direct_conversation_obj.optString(HREF_URL_KEY);
				dbUtil.setHeader(directConversationHrefUrl,directConversationUrl, direct_conversation_obj.getString(TYPE_KEY), false);
				
				//dbUtil.setUserDirectConversation ( null, directConversationUrl, vProfileId );
				dbUtil.setUserDirectConversation ( null, directConversationUrl,directConversationHrefUrl,true, vProfileId );
				}

				/**
				 * Insertion Into Live Friends Table
				 * 			
				 */

				
				
				if(playUpFriends){
					/*dbUtil.setPlayupFriendsData(friendUID,friendName,friendAvatar,userName,
							sourceName,sourceIconHref,vProfileId,isOnline,onlineSince,lastActivitySince,
							access,unread,lastActivityUid ,roomName,subjectTitle,subjectUid,subjectUrl, access_permitted,directConversationUrl, vProfileUrl );*/
					dbUtil.setPlayupFriendsData(friendUID,friendName,friendAvatar,userName,
							sourceName,sourceIconHref,vProfileId,isOnline,onlineSince,lastActivitySince,
							access,unread,lastActivityUid ,roomName,subjectTitle,subjectUid,subjectUrl,subjectHrefUrl, access_permitted,directConversationUrl,directConversationHrefUrl, vProfileUrl,vProfileHrefUrl);
					
				}else{
					
					/*dbUtil.setLiveFriends(friendUID, friendName, friendAvatar, sourceName, 
							sourceIconHref,isOnline, vProfileId, userName,onlineSince, lastActivityUid ,
							roomName,subjectTitle,subjectUid ,subjectUrl ,access, access_permitted,
							lastActivitySince,vProfileUrl,directConversationUrl);
*/
					dbUtil.setLiveFriends(friendUID, friendName, friendAvatar, sourceName, 
							sourceIconHref,isOnline, vProfileId, userName,onlineSince, lastActivityUid ,
							roomName,subjectTitle,subjectUid ,subjectUrl ,subjectHrefUrl,access, access_permitted,
							lastActivitySince,vProfileUrl,vProfileHrefUrl,directConversationUrl,directConversationHrefUrl);

					dbUtil.updateFriends(friendUID, friendName, friendAvatar, sourceName, sourceIconHref,
							isOnline, vProfileId, userName);
					
				}

				
				i++;			
			}




			



		}catch (Exception e) {
			Logs.show(e);
		} finally {
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			new Util().releaseMemory(jsonObj);
			if ( !inTransaction ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( "end ------------------------------------LiveFriendsJsonUtil ");
			}
			// }
		}



	}


}