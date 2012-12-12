package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;


public class ConversationFriendsJsonUtil {


	private final String SELF_KEY               =  ":self";
	private final String HREF_URL_KEY               =  ":href";
	private final String UID_KEY                =  ":uid";
	private final String TYPE_KEY        		=  ":type";
	private final String ITEMS_KEY        		=  "items";


	private final String FRIEND_KEY             = "friend";
	private final String PROFILE_KEY            = "profile";
	private final String NAME_KEY            	= "name";
	private final String USER_NAME_KEY          = "username";
	private final String AVATAR_KEY             = "avatar";

	private final String ALREADY_INVITED_KEY   = "already_invited";

	private final String SOURCE_KEY   = "source";
	private final String ICON_KEY   = "icon";
	private final String DENSITY_KEY   = "density";
	private final String HREF_KEY   = "href";

	private final String ONLINE_KEY   = "online";
	private final String ONLINE_SINCE_KEY   = "online_since";
	private final String PRESENCE_KEY   = "presence";








	private String vConversationId = null;

	private boolean isSearch = false;
	private boolean inTransaction = false;


	public ConversationFriendsJsonUtil (  JSONObject jsonObj, String vConversationId, boolean inTransaction ) {

		this.inTransaction = inTransaction;
		this.vConversationId = vConversationId;

		if ( jsonObj != null) {
			parseData ( jsonObj );
		}
	}

	public ConversationFriendsJsonUtil (  JSONObject jsonObj, String vConversationId, boolean isSearch, boolean inTransaction  ) {

		this.inTransaction = inTransaction;
		this.vConversationId = vConversationId;
		this.isSearch = isSearch;
		if ( jsonObj != null) {
			parseData ( jsonObj );
		}
	}




	private void parseData ( JSONObject jsonObj) {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
		
		if ( !inTransaction ) {
			
			dbUtil.getWritabeDatabase().beginTransaction();
			
		}
		//}
		try {

			try {
				dbUtil.removeConversationFriends ( vConversationId, false  );


				String uid = null;
				String self = jsonObj.optString(SELF_KEY);
				String href = jsonObj.optString(HREF_URL_KEY);
                if ( jsonObj.has(UID_KEY)){
                	uid = jsonObj.getString(UID_KEY);
                	
                	if(!(jsonObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)))
                		return;

    				dbUtil.setHeader( href,self, jsonObj.getString( TYPE_KEY ), false  );
    			/*	dbUtil.setConversationFriends ( vConversationId, uid, self ); */
	
    				dbUtil.setConversationFriends ( vConversationId, uid, self,href); 
                }
				
				JSONArray friendsArray	=	jsonObj.getJSONArray ( ITEMS_KEY );

				for ( int i = 0, len = friendsArray.length(); i < len; i++ ) {

					JSONObject jObj = friendsArray.getJSONObject( i );
					
					if(!jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.CONVERSATION_FRIEND_DATA_TYPE))
						continue;

					JSONObject friend_jObj = jObj.getJSONObject( FRIEND_KEY );
					String vFriendID		=	friend_jObj.getString(UID_KEY);
					
					String vFriendType      =   friend_jObj.getString( TYPE_KEY );
					if(!vFriendType.equalsIgnoreCase(Types.FRIEND_DATA_TYPE))
						continue;
						

					String vProfileId = null;
					if ( jObj.has( PROFILE_KEY ) ) {
						JSONObject profile_jObj = jObj.getJSONObject( PROFILE_KEY );
						
						if(profile_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.FAN_PROFILE_DATA_TYPE)){
						String vProfileUrl = profile_jObj.optString( SELF_KEY );
						String vProfileHrefUrl = profile_jObj.optString( HREF_URL_KEY );
						vProfileId  = profile_jObj.getString( UID_KEY );

						dbUtil.setHeader(vProfileUrl, vProfileUrl, profile_jObj.getString( TYPE_KEY ), false  );

						ContentValues values1 = new ContentValues ();
						values1.put( "iUserId", vProfileId );
						values1.put( "vSelfUrl", vProfileUrl );
						values1.put( "vHrefUrl", vProfileHrefUrl );

				/*		dbUtil.setUserData ( values1, vProfileId );*/
						dbUtil.setUserData ( values1, vProfileId );
						}
					}

					String vName		=	jObj.getString(NAME_KEY);

					String vUserName		=	null;
					if ( jObj.has( USER_NAME_KEY ) ) {
						vUserName		=	jObj.getString(USER_NAME_KEY);
					}
					String vAvatarUrl			=	jObj.optString(AVATAR_KEY); //Only for playup User

					JSONObject friendSource	=	jObj.getJSONObject(SOURCE_KEY);
					String sourceName		=	friendSource.getString(NAME_KEY);

					String sourceIconHref	= "";
					JSONArray friendIcon	=	friendSource.getJSONArray(ICON_KEY);
					for ( int j = 0, len_j = friendIcon.length(); j < len_j ; j++ ) {

						JSONObject friendIcon_item = friendIcon.getJSONObject( j );
						if ( Constants.DENSITY.equalsIgnoreCase(  friendIcon_item.getString( DENSITY_KEY ) ) ) {
							sourceIconHref = friendIcon_item.getString( HREF_KEY );
						}
					}

					boolean isOnline		=		false;
					if ( jObj.has( ONLINE_KEY ) ) {
						isOnline		=		jObj.getBoolean ( ONLINE_KEY );
					}

					boolean presence		=		false;
					if ( jObj.has( PRESENCE_KEY ) ) {
						presence		=		jObj.getBoolean ( PRESENCE_KEY );
					}

					String dOnlineSince		= null;
					if ( jObj.has( ONLINE_KEY ) ) {
						dOnlineSince		=		jObj.getString ( ONLINE_SINCE_KEY );
					}

					if ( jObj.has( ALREADY_INVITED_KEY ) ) {
						boolean already_invited = jObj.getBoolean( ALREADY_INVITED_KEY );
						if ( already_invited ) {
							dbUtil.setRecentInvite ( vFriendID, vConversationId, 2, false, false  );
						} else {
							dbUtil.setRecentInvite ( vFriendID, vConversationId, 0, false, false  );
						}
					} else {
						dbUtil.setRecentInvite ( vFriendID, vConversationId, 0, false, false  );
					}

					
					ContentValues values = new ContentValues();
					values.put( "vFriendId",  vFriendID );
					values.put( "vFriendType",  vFriendType );
					values.put( "vFriendName",  vName );
					values.put( "vFriendAvatar",  vAvatarUrl );
					values.put( "vSourceName",  sourceName );
					values.put( "vSourceIconHref",  sourceIconHref );
					values.put( "isOnline",  isOnline );
					values.put( "vProfileId", vProfileId );
					values.put( "vFriendUserName",  vUserName );
					values.put( "dOnlineSince",  dOnlineSince );
					values.put( "isPresence",  presence );
					
					
					
					
					if ( uid == null ) {
						values.put( "vConversationFriendId", uid );
					}

					/**
					 * Insertion Into Friends Table
					 * 			
					 */

					if ( isSearch ) {
						dbUtil.setSearchFriends( values, vFriendID );	
					} else {
						dbUtil.setFriends( values, vFriendID );	
					}

				}




			}finally{

				new Util().releaseMemory(jsonObj);
			}



		}catch (Exception e) {
			  Logs.show(e);
		} finally {
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			
			if (  !inTransaction  ){
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
				
				
			}
			// }
		}



	}


}
