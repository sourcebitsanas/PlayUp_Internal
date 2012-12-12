package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;


public class MyFriendConversationJsonUtil {

	private final String UID_KEY           	=  ":uid";
	private final String SELF_KEY          	=  ":self";

	private final String HREF_KEY          	=  ":href";
	private final String TYPE_KEY          	=  ":type";

	private final String OPTIONS_KEY = ":options";

	private final String NAME_KEY = "name";
	private final String ANCESTOR_KEY = "ancestors";
	private final String SUBJECT_KEY = "subject";

	private final String INVITATION_KEY = "invitations";
	private final String ACCESS_KEY = "access";
	private final String ACCESS_PERMITTED_KEY = "access_permitted";
	private final String PRESENCE_KEY = "presence";
	private final String TOTAL_PRESENCE_KEY = "total_presences";


	private final String FRIENDS_KEY = "friends";
	private final String MESSAGE_KEY = "messages";
	private final String EDITABLE_FIELDS_KEY = ":editable_fields";

	private final String ADDITIONS_KEY = "additions";
	private final String MARKER_KEY = "marker";
	private final String ITEM_KEY = "items";


	private final String MSG_KEY = "message";
	private final String CREATED_KEY = "created";
	private final String FROM_KEY = "from";
	private final String DISPLAY_NAME_KEY = "display_name";
	private final String AVATAR_KEY = "avatar";
	private final String TOTAL_COUNT_KEY = "total_count";
	private final String VERSION_KEY = ":version";




	private boolean inTransaction = false;
	private String vAcceptableType = null;

	private String vConversationId = null;
	private int iOrder = -1;
	private String SUBJECT_TITLE_KEY = "subject_title";

	private String vPushId = null;

	public MyFriendConversationJsonUtil ( String str, boolean inTransaction, String vAcceptableType, int iOrder ) {

		this.inTransaction = inTransaction;
		this.vAcceptableType = vAcceptableType;
		this.iOrder = iOrder;

		if ( str != null && str.trim().length() > 0 ) {
			parseData ( str );
		}
	}

	public MyFriendConversationJsonUtil(String str, boolean inTransaction,
			String vAcceptableType, String vPushId) {
		this.inTransaction = inTransaction;
		this.vAcceptableType = vAcceptableType;
		this.vPushId = vPushId;
		if ( str != null && str.trim().length() > 0 ) {
			parseData ( str );
		}


	}



	private void parseData ( String  str ) {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		JSONObject jObj = null;

		if ( !inTransaction ) {
//			Logs.show ( "begin ------------------------------------MyFriendConversationJsonUtil ");
			dbUtil.getWritabeDatabase().beginTransaction();
		}


		try {
			jObj = new JSONObject( str );


			if ( vAcceptableType != null && vAcceptableType.toString().contains ( jObj.getString( TYPE_KEY ) )  ) {
				vConversationId = jObj.getString( UID_KEY );

				if(vPushId != null && vPushId.trim().length() > 0){
					dbUtil.updatePushNotification(vConversationId,vPushId );

					Constants.pushNotificationDownload.put(vPushId, false);


				}
				String vConversationUrl = jObj.optString( SELF_KEY );
				String vConversationHrefUrl = jObj.optString( HREF_KEY );
				
				new Util().setColor(jObj);

				dbUtil.setHeader( vConversationHrefUrl,vConversationUrl , jObj.getString( TYPE_KEY ), false );

				// getting the options 
				JSONArray conversation_options_jArr = jObj.getJSONArray( OPTIONS_KEY );
				int conversation_options_count = conversation_options_jArr.length();
				String[] conversation_options = new String [ conversation_options_count ];
				for ( int j = 0; j < conversation_options_count; j++ ) {

					conversation_options [ j ] = conversation_options_jArr.getString( j ); 
				}
				conversation_options_jArr = null;

				// getting the conversation name
				String vConversationName = jObj.getString( NAME_KEY );


				String vLobbyConversationId = null;
				String vLobbyConversationUrl = null;
				String vLobbyConversationHrefUrl	= null;

				// ancestor 
				JSONArray ancestor_jArr = jObj.getJSONArray( ANCESTOR_KEY );
				for (  int k = 0; k < ancestor_jArr.length() ; k++ ) {
					if ( k == 0 ) {
						JSONObject ancestor_jObj = ancestor_jArr.getJSONObject( k );
						if(ancestor_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.MY_LOBBY_DATA_TYPE)){
						vLobbyConversationId = ancestor_jObj.getString( UID_KEY );
						vLobbyConversationUrl = ancestor_jObj.optString(  SELF_KEY );	
						vLobbyConversationHrefUrl = ancestor_jObj.optString(  HREF_KEY );	
						dbUtil.setHeader( vLobbyConversationHrefUrl,vLobbyConversationUrl, ancestor_jObj.getString( TYPE_KEY ), false );
						ancestor_jObj = null;
						}
					}
				}

				String vSubjectId = null;
				String vSubjectUrl = null;
				String vSubjectHrefUrl= null;
				// subject 
				if ( jObj.has( SUBJECT_KEY ) ) {
					JSONObject subject_jObj = jObj.getJSONObject( SUBJECT_KEY );
					vSubjectId = subject_jObj.getString( UID_KEY );
					vSubjectUrl = subject_jObj.optString( SELF_KEY );
					vSubjectHrefUrl = subject_jObj.optString( HREF_KEY );
					dbUtil.setHeader(vSubjectHrefUrl, vSubjectUrl, subject_jObj.getString( TYPE_KEY ), false );
				}

				// invitation 
				JSONObject invitation_jObj = jObj.getJSONObject( INVITATION_KEY );
				String vInvitationUrl = "";
				String vInvitationHrefUrl = "";
				if(invitation_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.SHARE_DATA_TYPE)){
					vInvitationUrl = invitation_jObj.optString( SELF_KEY );
					vInvitationHrefUrl = invitation_jObj.optString( HREF_KEY );
					dbUtil.setHeader(vInvitationHrefUrl, vInvitationUrl, invitation_jObj.getString( TYPE_KEY ), false );
					invitation_jObj = null;
				}

				// access
				String vAccess = jObj.getString( ACCESS_KEY );
				boolean isAccessPermitted = jObj.getBoolean( ACCESS_PERMITTED_KEY );

				// presence
				JSONObject presence_jObj = jObj.getJSONObject( PRESENCE_KEY );
				String vPresenceUrl = "";
				String vPresenceHrefUrl = "";
				int iTotalPresences = 0;
				if(presence_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.PRESENCE_DATA_TYPE)){
				 vPresenceUrl = presence_jObj.optString( SELF_KEY );
				 vPresenceHrefUrl = presence_jObj.optString( HREF_KEY );
				 
				 iTotalPresences = presence_jObj.getInt( TOTAL_PRESENCE_KEY );
				dbUtil.setHeader( vPresenceHrefUrl,vPresenceUrl, presence_jObj.getString( TYPE_KEY ), false );
				presence_jObj = null;
				}

				// friends 
				JSONObject friends_jObj = jObj.getJSONObject( FRIENDS_KEY );
				String vFriendId = "", vFriendUrl = "",vFriendHrefUrl = "";
				if(friends_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
				 vFriendId = friends_jObj.getString ( UID_KEY );
				 vFriendUrl = friends_jObj.optString ( SELF_KEY );
				 vFriendHrefUrl = friends_jObj.optString ( HREF_KEY );
				dbUtil.setHeader(vFriendHrefUrl, vFriendUrl, friends_jObj.getString( TYPE_KEY ), false );
				friends_jObj = null;
				}

				// editable fields
				JSONArray editableField_jArr = jObj.getJSONArray( EDITABLE_FIELDS_KEY );
				boolean editSubject = false,editName = false;
				int editableField_count = editableField_jArr.length();

				for ( int j = 0; j < editableField_count; j++ ) {
					if(editableField_jArr.getString( j ).equalsIgnoreCase(SUBJECT_KEY))
						editSubject = true;
					else if(editableField_jArr.getString( j ).equalsIgnoreCase(NAME_KEY))
						editName = true;

				}
				editableField_jArr = null;

	//Praveen:	
				dbUtil.setFriendConversation ( vConversationId, vConversationUrl,vConversationHrefUrl, vConversationName, vLobbyConversationId, vLobbyConversationUrl,vLobbyConversationHrefUrl, 
						vInvitationUrl,vInvitationHrefUrl, vAccess, isAccessPermitted,  vPresenceUrl,vPresenceHrefUrl, iTotalPresences, vFriendId, vFriendUrl,vFriendHrefUrl, 
						vSubjectId, vSubjectUrl,vSubjectHrefUrl,  iOrder,editSubject ,editName );

				


				JsonUtil json = new JsonUtil();
				json.setConversationId( vConversationId );
				json.setConversationUrl ( vConversationUrl );
				json.setConversationHrefUrl ( vConversationHrefUrl );
				json.setShouldDelete( true );
				json.setBooleanFlag( true );
				json.parse( new StringBuffer( jObj.getJSONObject( MESSAGE_KEY ).toString() ), Constants.TYPE_FRIEND_CONVERSATION__MESSAGE_JSON, true );


				

			}





			
		} catch (JSONException e) {
			Logs.show ( e );
		}  finally {
			new Util().releaseMemory( jObj );
			if ( !inTransaction ) {

				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( " end ------------------------------------MyFriendConversationJsonUtil ");

			}

		}
	}


	public String getConversationId () {
		return vConversationId;
	}
}
