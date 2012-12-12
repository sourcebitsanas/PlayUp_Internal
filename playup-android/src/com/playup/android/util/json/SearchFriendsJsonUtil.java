package com.playup.android.util.json;


import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class SearchFriendsJsonUtil {
	private final String TOTAL_COUNT_KEY     =  "total_count";

	private static final String LAST_ACTIVITY_KEY = "last_activity";
	private final String ONLINE_SINCE            =   "online_since";
	private final String SUBJECT_TITLE_KEY       =  "subject_title";
	private final String SUBJECT_KEY             =  "subject";
	private final String ACCESS_KEY              =  "access";
	private final String ACCESS_PERMITTED_KEY    =  "access_permitted";
	private final String LAST_ACTIVITY_SINCE_KEY =  "in_last_activity_since";
	private final String SELF_KEY               =  ":self";
	private final String HREF_URL_KEY               =  ":href";
	
	private final String UID_KEY                =  ":uid";
	private final String ID_KEY                 =  "id";
	private final String NAME_KEY 				=  "name";
	private final String USER_NAME_KEY 			=  "username";
	private final String AVATAR_URL_KEY 		=  "avatar";
	private final String TYPE_KEY        		=  ":type";
	private final String SOURCE_KEY				=	"source";
	private final String ICON_KEY				=	"icon";
	private final String DENSITY_KEY			=	"density";
	private final String HREF_KEY				=	"href";
	private final String APP_INVITATION_KEY		=	"app_invitations";
	private final String SIZE_KEY				=	"size";
	private final String CONTENTS_KEY			=	"contents";
	private final String ALREADY_INVITED_KEY	=	"already_invited";
	private final String ITEMS_KEY				=	"items";
	private final String ONLINE_KEY				=	"online";
	private final String PROFILE_KEY			=	"profile";
	private final String DIRECT_CONVERSATION_KEY   =   "direct_conversation";


	private boolean inTransaction = false;


	public SearchFriendsJsonUtil (  JSONObject jsonObj,String gapUid, boolean inTransaction ) {

		this.inTransaction = inTransaction ;
		if ( jsonObj != null) {
			parseData ( jsonObj ,gapUid);
		}
	}

	private void parseData ( JSONObject jsonObj,String gapUid) {

		DatabaseUtil mDatabaseUtil	=	DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
		
		if ( !inTransaction ) {
//			Logs.show ( "begin ------------------------------------SearchFriendsJsonUtil ");
			mDatabaseUtil.getWritabeDatabase().beginTransaction();
		}
		// }

		try {

			/**
			 * 
			 * parse general info like 
			 */

			try {

				String self = jsonObj.optString(SELF_KEY);
				String href = jsonObj.optString(HREF_URL_KEY);
				String type = jsonObj.getString(TYPE_KEY);
				//String uid = jsonObj.getString(UID_KEY);
				if(!type.equalsIgnoreCase(Types.PLAYUP_FRIENDS_DATA_TYPE))
					return;
				mDatabaseUtil.setHeader ( href,self, type , false  );		
				Constants.searchFriendsResults = jsonObj.getString(TOTAL_COUNT_KEY);
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
				if(gapUid != null && gapUid.length() > 0)
					mDatabaseUtil.removeSearchFriendGapEntry(gapUid);

				int i =0;
				while(i<friendsArray.length()){


					/**
					 * Friend ITEM ONLY
					 */
					String friendType		=	friendsArray.getJSONObject(i).getString(TYPE_KEY);

					if(friendType.compareToIgnoreCase(Types.FRIEND_DATA_TYPE)==0){

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

						JSONObject	app_invitation		=	friendsArray.getJSONObject(i).optJSONObject(APP_INVITATION_KEY);
						String	invitation_self="",invitation_href="";
						String invitation_type="";
						if(app_invitation!=null){
							if(app_invitation.getString(TYPE_KEY).equalsIgnoreCase(Types.SHARE_DATA_TYPE)){
							invitation_self			=	app_invitation.optString(SELF_KEY);
							invitation_href			=	app_invitation.optString(HREF_URL_KEY);
							invitation_type			=	app_invitation.getString(TYPE_KEY);
							}
						}										
						boolean already_Invited	=		friendsArray.getJSONObject(i).optBoolean(ALREADY_INVITED_KEY);
						boolean isOnline		=		friendsArray.getJSONObject(i).optBoolean(ONLINE_KEY);
						String onlineSince = friendsArray.getJSONObject(i).optString(ONLINE_SINCE);
						

						String vProfileId = null,vProfileUrl = "",vProfileHrefUrl="";
						if ( friendsArray.getJSONObject( i ).has( PROFILE_KEY ) ) {
							JSONObject profile_jObj = friendsArray.getJSONObject( i ).getJSONObject( PROFILE_KEY );
							if(profile_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.FAN_PROFILE_DATA_TYPE)){
							 vProfileUrl = profile_jObj.optString( SELF_KEY );
							 vProfileHrefUrl = profile_jObj.optString( HREF_URL_KEY );
							vProfileId  = profile_jObj.getString( UID_KEY );

							mDatabaseUtil.setHeader(vProfileHrefUrl,vProfileUrl, profile_jObj.getString( TYPE_KEY ), false   );

							ContentValues values1 = new ContentValues ();
							values1.put( "iUserId", vProfileId );
							values1.put( "vSelfUrl", vProfileUrl );
							values1.put( "vHrefUrl", vProfileHrefUrl );

						/*	mDatabaseUtil.setUserData ( values1, vProfileId );*/
							mDatabaseUtil.setUserData ( values1, vProfileId );
							}
						}


						int access =  0;
						int access_permitted = 0;
						String lastActivityUid = "",lastActivityType= "",roomName= "",subjectTitle= "",subjectUid = "",subjectType= ""
							,subjectUrl = "",subjectHrefUrl="";
						String lastActivitySince = "";
						JSONObject lastActivity = friendsArray.getJSONObject(i).optJSONObject(LAST_ACTIVITY_KEY);
						if(lastActivity != null){
							if(lastActivity.getString(TYPE_KEY).equalsIgnoreCase(Types.RECENT_CONVERSATION_DATA_TYPE)){
							lastActivityUid = lastActivity.getString(UID_KEY);
							lastActivityType = lastActivity.getString(TYPE_KEY);
							roomName = lastActivity.getString(NAME_KEY);
							subjectTitle = lastActivity.getString(SUBJECT_TITLE_KEY);

							JSONObject subject = lastActivity.getJSONObject(SUBJECT_KEY);
							if(subject.getString(TYPE_KEY).equalsIgnoreCase(Types.PRIVATE_CONVERSATION_DATA_TYPE) ||
									subject.getString(TYPE_KEY).equalsIgnoreCase(Types.CONTEST_LOBBY_CONVERSATION_DATA_TYPE)){
							subjectUid = subject.getString(UID_KEY);
							subjectType = subject.getString(TYPE_KEY);
							subjectUrl = subject.optString(SELF_KEY);
							subjectHrefUrl = subject.optString(HREF_URL_KEY);

							}
							mDatabaseUtil.setHeader(subjectHrefUrl,subjectUrl, subjectType , false  );
							 lastActivitySince = friendsArray.getJSONObject(i).optString(LAST_ACTIVITY_SINCE_KEY);
							access = ( lastActivity.getString(ACCESS_KEY).equalsIgnoreCase("public") )? 1 : 0;
							access_permitted = ( lastActivity.getString(ACCESS_PERMITTED_KEY).equalsIgnoreCase("public") )? 1 : 0;
							}
						}


						/**
						 * Insertion Into Friends Table
						 * 			
						 */
						int iAlreadyInvited = 0;
						if ( already_Invited ) {
							iAlreadyInvited = 2;
						}
						
						String directConversationUrl = "",directConversationHrefUrl="";
						if(friendsArray.getJSONObject( i ).has( DIRECT_CONVERSATION_KEY )){
							JSONObject direct_conversation_obj = friendsArray.getJSONObject( i ).getJSONObject( DIRECT_CONVERSATION_KEY );
							if(direct_conversation_obj.getString(TYPE_KEY).equalsIgnoreCase(Types.DIRECT_CONVERSATION_DATA_TYPE)){
							directConversationUrl = direct_conversation_obj.optString(SELF_KEY);
							directConversationHrefUrl = direct_conversation_obj.optString(HREF_URL_KEY);
							mDatabaseUtil.setHeader(directConversationHrefUrl,directConversationUrl, direct_conversation_obj.getString(TYPE_KEY), false);						
							/*mDatabaseUtil.setUserDirectConversation ( null, directConversationUrl, vProfileId );*/

							mDatabaseUtil.setUserDirectConversation ( null, directConversationUrl,directConversationHrefUrl,true, vProfileId );}

							
						}
						
					/*	mDatabaseUtil.setSearchFriends(friendUID, friendName, friendAvatar, sourceName, 
								sourceIconHref, invitation_self,invitation_type, iAlreadyInvited,
								isOnline, vProfileId, userName,
								onlineSince, lastActivityUid ,roomName,subjectTitle,subjectUid ,subjectUrl 
								,access, access_permitted,lastActivitySince,vProfileUrl,directConversationUrl,friendType);	*/
						mDatabaseUtil.setSearchFriends(friendUID, friendName, friendAvatar, sourceName, 
								sourceIconHref, invitation_self,invitation_href,invitation_type, iAlreadyInvited,
								isOnline, vProfileId, userName,
								onlineSince, lastActivityUid ,roomName,subjectTitle,subjectUid ,subjectUrl ,subjectHrefUrl
								,access, access_permitted,lastActivitySince,vProfileUrl,vProfileHrefUrl,directConversationUrl,directConversationHrefUrl,friendType);	
						
						/*mDatabaseUtil.setFriends(friendUID, friendName, friendAvatar, sourceName, 
								sourceIconHref, invitation_self,invitation_type, iAlreadyInvited, isOnline, 
								vProfileId, userName, friendType,vProfileUrl,directConversationUrl,true);	*/
						mDatabaseUtil.setFriends(friendUID, friendName, friendAvatar, sourceName, 
								sourceIconHref, invitation_self,invitation_href,invitation_type, iAlreadyInvited, isOnline, 
								vProfileId, userName, friendType,vProfileUrl,vProfileHrefUrl,directConversationUrl,directConversationHrefUrl,true);	
						

					}else if(friendType.compareToIgnoreCase(Types.GAP_DATA_TYPE )==0){ //GAP Object
						JSONObject contentObject 	=	friendsArray.getJSONObject(i).getJSONObject(CONTENTS_KEY);

						if(contentObject.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
						/**
						 * If Its a gap object then need to have an entry in my_friends table and gap_url table
						 */
						gapUid	=	friendsArray.getJSONObject(i).getString(UID_KEY);


						mDatabaseUtil.setSearchFriendsGap(gapUid);	
						int gapSize	=	friendsArray.getJSONObject(i).getInt(SIZE_KEY);
						
						String contentType	=	contentObject.getString(TYPE_KEY);
						String gap_url 	=	contentObject.optString(SELF_KEY);
						String gap_href_url 	=	contentObject.optString(HREF_URL_KEY);
						/**
						 * Insert into database the Gap Info
						 */

						mDatabaseUtil.updateGapInfo(gapUid, gap_url,gap_href_url, gapSize);
						/**
						 * Setting Header 		
						 */
						mDatabaseUtil.setHeader(gap_href_url,gap_url, contentType ,false  );
						}

					}


					i++;
				}

				//							JSONObject liveFriends = jsonObj.getJSONObject("live");
				//							
				//							//String liveFriendsuid = liveFriends.getString(UID_KEY);
				//							String liveFriendsUrl = liveFriends.getString(SELF_KEY);
				//							String liveFriendsType = liveFriends.getString(TYPE_KEY);
				//							
				//							mDatabaseUtil.setHeader(liveFriendsUrl, liveFriendsType);
				//							
				//					
				//							
				//							new Util().searchLiveFriends(liveFriendsUrl);

			}
			finally{
				new Util().releaseMemory(jsonObj);
			}
		}catch (Exception e) {
			Logs.show(e);
			  
		} finally {
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			
			if ( !inTransaction ) {
				
				mDatabaseUtil.getWritabeDatabase().setTransactionSuccessful();
				mDatabaseUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( "end ------------------------------------SearchFriendsJsonUtil ");
			 }
		}



	}


}