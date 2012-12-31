package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONException;

import org.json.JSONObject;

import android.content.ContentValues;

import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Types;
import com.playup.android.util.Util;


public class UserNotificationJsonUtil {

	private final String UID_KEY           	=  ":uid";
	private final String SELF_KEY          	=  ":self";
	private final String HREF_URL_KEY          	=  ":href";
	private final String TYPE_KEY          	=  ":type";


	private final String ITEMS_KEY          =  "items";
	private final String READ_KEY          	=  "read";
	private final String TIMESTAMP_KEY      =  "timestamp";
	private final String SUBJECT_KEY        =  "subject";
	private final String STATUS_KEY         =  "status";

	private final String FROM_KEY          	=  "from";
	private final String DISPLAY_NAME_KEY   =  "display_name";
	private final String AVATAR_KEY         =  "avatar";

	private final String DETAILS_KEY        =  "details";
	private final String TITLE_KEY          =  "title";
	private final String SUBTITLE_KEY       =  "subtitle";
	private final String MESSAGE_KEY        =  "message";

	private final String CONVERSATION_KEY   =  "conversation";

	private final String TOTAL_KEY          =  "total_count";
	private final String UNREAD_KEY         =  "unread_count";

	private final String SIZE_KEY           =  "size";
	private final String CONTENT_KEY        =  "contents";


	


	private boolean clearData = false;

	private boolean inTransaction = false;

	public UserNotificationJsonUtil ( String str, boolean clearData, boolean inTransaction  ) {

		this.clearData = clearData;
		this.inTransaction = inTransaction;
		if ( str != null && str.trim().length() > 0 ) {
			parseData ( str );
		}
	}

	private void parseData ( String  str ) {
		JSONObject jObj = null;
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/


		if ( !inTransaction ) {
			dbUtil.getWritabeDatabase().beginTransaction();
//			Logs.show ( "begin ------------------------------------UserNotificationJsonUtil ");
		}
		// }
		try {


			jObj = new JSONObject( str );

			String vUserNotificationUrl = jObj.optString( SELF_KEY );
			String vUserNotificationHrefUrl = jObj.optString( HREF_URL_KEY );
			String vUserNotificationId  = jObj.getString( UID_KEY );
			
			
			new Util().setColor(jObj);
			
			if(!jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.NOTIFICATIONS_DATA_TYPE))
				return;
			dbUtil.setHeader( vUserNotificationHrefUrl,vUserNotificationUrl , jObj.getString( TYPE_KEY ), false  );

			int iNotificationTotalCount = jObj.getInt( TOTAL_KEY );
			int iNotificationUnReadCount = jObj.getInt( UNREAD_KEY );

			

			ContentValues values = new ContentValues ();
			values.put( "iNotificationTotalCount", iNotificationTotalCount );
			values.put( "iNotificationUnReadCount",iNotificationUnReadCount  );

			PlayupLiveApplication.getDatabaseWrapper().queryMethod2( Constants.QUERY_UPDATE, null, "user", values, " isPrimaryUser = '1' " );

			// String vUserId = dbUtil.getUserId();

			JSONArray jArr = jObj.getJSONArray( ITEMS_KEY );
			for ( int i = 0, len = jArr.length(); i < len; i++ ) {


				if ( i == 0 && clearData ) {
					PlayupLiveApplication.getDatabaseWrapper().queryMethod2( Constants.QUERY_DELETE, null, "notification", null, null );
				}
				JSONObject itemjObj = jArr.getJSONObject( i );
				


				boolean isGap = false;


				String vNotificationUrl = null,vNotificationHrefUrl=null;
				String vNotificationId  = null;
				int isRead = 0;
				String dDate = null;
				String vStatus = null;
				String vSubjectType = null;
				String vUserSelfUrl = null,vUserHrefUrl=null;
				String vUserId      = null;
				String vUserType    = null;
				String vUserName    = null;
				String vAvatarUrl   = null;

				int iGapSize = 0;
				String vGapId = null;
				String vGapUrl = null,vGapHrefUrl=null;
				

				String vDetailTitle = null;
				String vDetailSubTitle = null;
				String vDetailType = null;
				String vDetailMessage = null;

				String vConversationId = null;
				String vConversationUrl = null,vConversationHrefUrl=null;


				if ( itemjObj.has( CONTENT_KEY ) ) {
					isGap = true;
					vSubjectType = itemjObj.getString( TYPE_KEY );
					iGapSize     = itemjObj.getInt( SIZE_KEY );
					vGapId       = itemjObj.getString( UID_KEY );

					isRead = 2;
					vNotificationId = vGapId;

					JSONObject content_jObj = itemjObj.getJSONObject( CONTENT_KEY );
					vGapUrl = content_jObj.optString( SELF_KEY );
					vGapHrefUrl = content_jObj.optString( HREF_URL_KEY );

					dbUtil.setHeader( vGapHrefUrl,vGapUrl, content_jObj.getString( TYPE_KEY ), false  );

				/*	dbUtil.setUserNotification ( vNotificationId, vNotificationUrl, isRead, dDate, 
							vSubjectType, vUserSelfUrl, vUserId, vUserType, vUserName, vAvatarUrl, 
							vDetailTitle, vDetailSubTitle, vDetailType, vDetailMessage, 
							vConversationId, vConversationUrl, vStatus, iGapSize, vGapId, vGapUrl );
*/
					dbUtil.setUserNotification ( vNotificationId, vNotificationUrl,vNotificationHrefUrl, isRead, dDate, 
							vSubjectType, vUserSelfUrl,vUserHrefUrl, vUserId, vUserType, vUserName, vAvatarUrl, 
							vDetailTitle, vDetailSubTitle, vDetailType, vDetailMessage, 
							vConversationId, vConversationUrl,vConversationHrefUrl, vStatus, iGapSize, vGapId, vGapUrl,vGapHrefUrl );


				} else {
					if(!(itemjObj.getString(TYPE_KEY).equalsIgnoreCase(Types.CONFIRMABLE_NOTIFICATION_DATA_TYPE) ||
							itemjObj.getString(TYPE_KEY).equalsIgnoreCase(Types.INVITATION_NOTIFICATIONS_DATA_TYPE)))
						continue;
					JSONObject sub_jObj = itemjObj.getJSONObject( SUBJECT_KEY );
					vSubjectType = sub_jObj.getString( TYPE_KEY );


					vNotificationUrl = itemjObj.optString( SELF_KEY );
					vNotificationHrefUrl = itemjObj.optString( HREF_URL_KEY );
					vNotificationId  = itemjObj.getString( UID_KEY );

					dbUtil.setHeader ( vNotificationHrefUrl,vNotificationUrl, itemjObj.getString( TYPE_KEY ), false  );

					if ( vSubjectType.equalsIgnoreCase( Types.FRIEND_INVITATION_DATA_TYPE ) || vSubjectType.equalsIgnoreCase(Types.CONVERSATION_INVITATION_DATA_TYPE) ) {

						isRead = ( itemjObj.getBoolean( READ_KEY ) )? 1 : 0 ;
						dDate = itemjObj.getString( TIMESTAMP_KEY );

						vStatus = itemjObj.optString( STATUS_KEY );


						JSONObject from_jObj = sub_jObj.getJSONObject( FROM_KEY );
						if(from_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.FAN_DATA_TYPE)){
						vUserSelfUrl = from_jObj.optString( SELF_KEY );
						vUserHrefUrl = from_jObj.optString( HREF_URL_KEY );
						vUserId      = from_jObj.getString( UID_KEY );
						vUserType    = from_jObj.getString( TYPE_KEY );
						vUserName    = from_jObj.getString( DISPLAY_NAME_KEY );
						vAvatarUrl   = from_jObj.getString( AVATAR_KEY );


						values = new ContentValues();
						values.put( "iUserId", vUserId );
						values.put( "vUserName", vUserName );
						values.put( "vUserAvatarUrl", vAvatarUrl );
						values.put( "vSelfUrl", vUserSelfUrl );
						values.put( "vHrefUrl", vUserHrefUrl );

						/*dbUtil.setUserData( values, vUserId );*/
						dbUtil.setUserData( values, vUserId );
						}

						if ( sub_jObj.has( DETAILS_KEY ) ) {

							JSONObject detail_jObj = sub_jObj.getJSONObject( DETAILS_KEY );
							if(detail_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.DETAILS_DATA_TYPE)){
							vDetailTitle = detail_jObj.getString( TITLE_KEY );
							vDetailSubTitle = detail_jObj.optString( SUBTITLE_KEY);
							vDetailType = detail_jObj.getString( TYPE_KEY );
							vDetailMessage = detail_jObj.getString( MESSAGE_KEY );
							}

						}

						if ( sub_jObj.has( CONVERSATION_KEY ) ) {

							JSONObject conversation_jObj = sub_jObj.getJSONObject( CONVERSATION_KEY );
							if(conversation_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.CONTEST_LOBBY_CONVERSATION_DATA_TYPE)||
									conversation_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.PRIVATE_CONVERSATION_DATA_TYPE)){
							vConversationId = conversation_jObj.getString( UID_KEY );
							vConversationUrl = conversation_jObj.optString( SELF_KEY );
							vConversationHrefUrl = conversation_jObj.optString( HREF_URL_KEY );

							values = new ContentValues();
							values.put( "vConversationId", vConversationId );
							values.put( "vSelfUrl", vConversationUrl );
							values.put( "vHref",  vConversationHrefUrl);
							/*dbUtil.setConversation ( values, vConversationId );*/
							dbUtil.setConversation ( values, vConversationId );

							dbUtil.setHeader( vConversationHrefUrl,vConversationUrl,  conversation_jObj.getString( TYPE_KEY ) , false );
							}
						}

						/*dbUtil.setUserNotification ( vNotificationId, vNotificationUrl, isRead, dDate, 
								vSubjectType, vUserSelfUrl, vUserId, vUserType, vUserName, vAvatarUrl, 
								vDetailTitle, vDetailSubTitle, vDetailType, vDetailMessage, 
								vConversationId, vConversationUrl, vStatus, iGapSize, vGapId, vGapUrl );*/
						
							dbUtil.setUserNotification ( vNotificationId, vNotificationUrl,vNotificationHrefUrl, isRead, dDate, 
								vSubjectType, vUserSelfUrl,vUserHrefUrl, vUserId, vUserType, vUserName, vAvatarUrl, 
								vDetailTitle, vDetailSubTitle, vDetailType, vDetailMessage, 
								vConversationId, vConversationUrl,vConversationHrefUrl, vStatus, iGapSize, vGapId, vGapUrl,vGapHrefUrl );

					}



				}




			}



			
		} catch (JSONException e) {
			//Logs.show(e);
		}  finally {
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			new Util().releaseMemory( jObj );
			if ( !inTransaction ) {

				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( "end ------------------------------------UserNotificationJsonUtil ");
			}
			// }
		}
	}
}
