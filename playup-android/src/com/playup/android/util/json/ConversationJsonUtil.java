package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.util.Log;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;

/**
 * 
 *Parse Subject of Conversation
 *
 */

public class ConversationJsonUtil {

	private final String UID_KEY = ":uid";
	private final String SELF_KEY = ":self";
	//private final String TYPE_KEY = ":type";
	private final String NAME_KEY = "name";
	private final String HREF_LINK_KEY = ":href";

	// contest keys 
	private final String CONTEST_KEY = "contest";

	// subject keys 
	private final String SUBJECT_KEY = "subject";

	// inivtation keys
	private final String INVITATION_KEY = "invitations";

	// message key
	private final String MESSAGE_KEY = "messages";

	// access keys 
	private final String ACCESS_KEY = "access";

	// presence keys 
	private final String PRESENCE_KEY = "presence";
	private final String TOTAL_PRESENCES = "total_presences";
	private final String FRIENDS_KEY = "friends";
	private final String ANCESTORS_KEY = "ancestors";






	private int iOrder = -1;
	private boolean isOrderSet = false;

	private final String TYPE_KEY = ":type";
	private String vPushId = "";

	private boolean inTransaction = false;



	public ConversationJsonUtil(JSONObject jsonObj, int iOrder, boolean inTransaction  ) {
		this.vPushId = "";

		this.inTransaction = inTransaction;
		this.iOrder = iOrder;
		this.isOrderSet = true; 
		if ( jsonObj != null ) {
			parseData ( jsonObj );
		}

	}


	public ConversationJsonUtil(JSONObject jsonObj, boolean inTransaction ) {
		this.vPushId = "";
		this.inTransaction = inTransaction ;
		this.isOrderSet = false; 
		if ( jsonObj != null ) {
			parseData ( jsonObj );
		}

	}


	public ConversationJsonUtil(JSONObject jsonObj,String vPushId, boolean  inTransaction ) {

		this.inTransaction = inTransaction;
		this.vPushId  = vPushId;
		if ( jsonObj != null ) {
			parseData ( jsonObj );
		}
	}

	private String vConversationId = null;


	private void parseData ( JSONObject  jsonObj) {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();

		if ( !inTransaction ) {
			dbUtil.getWritabeDatabase().beginTransaction();
			

		}
		try {

			vConversationId = jsonObj.getString( UID_KEY );
			if(!(jsonObj.getString(TYPE_KEY).equalsIgnoreCase(Types.CONTEST_LOBBY_CONVERSATION_DATA_TYPE)))
				return;
			
			dbUtil.setHeader ( jsonObj.optString( HREF_LINK_KEY ),jsonObj.optString( SELF_KEY ), jsonObj.getString( TYPE_KEY ), false );
			if(vPushId != null && vPushId.trim().length() > 0){
				dbUtil.updatePushNotification(vConversationId,vPushId );

				Constants.pushNotificationDownload.put(vPushId, false);


			}
			
			new Util().setColor(jsonObj);



			JSONArray ancestors = jsonObj.getJSONArray(ANCESTORS_KEY);



			String roundUid = "",roundHrefUrl = "";
			String roundUrl = "";
			String leaguesUid = "";
			String leaguesUrl = "",leaguesHrefUrl = "";
			String sportsUid = "";
			String sportsUrl = "",sportsHrefUrl = "";
			String contestLobbyUid = "";
			String contestLobbyUrl = "",contestLobbyHrefUrl = "";
			for ( int k = 0; k < ancestors.length(); k++ ) {

				JSONObject ancestor_jArr_jObj = ancestors.getJSONObject( k );

				if ( ancestor_jArr_jObj.getString( TYPE_KEY).equalsIgnoreCase( Types.ROUND_DATA_TYPE ) ) {
					roundUid = ancestor_jArr_jObj.getString( UID_KEY );
					roundUrl = ancestor_jArr_jObj.optString( SELF_KEY );
					roundHrefUrl = ancestor_jArr_jObj.optString( HREF_LINK_KEY );
				}

				if ( ancestor_jArr_jObj.getString( TYPE_KEY).equalsIgnoreCase( Types.COMPETITION_DATA_TYPE ) ) {
					leaguesUid = ancestor_jArr_jObj.getString( UID_KEY );
					leaguesUrl = ancestor_jArr_jObj.optString( SELF_KEY );
					leaguesHrefUrl = ancestor_jArr_jObj.optString( HREF_LINK_KEY );
				}

				if ( ancestor_jArr_jObj.getString( TYPE_KEY).equalsIgnoreCase( Types.SPORTS_DATA_TYPE ) ) {
					sportsUid = ancestor_jArr_jObj.getString( UID_KEY );
					sportsUrl = ancestor_jArr_jObj.optString( SELF_KEY );
					sportsHrefUrl = ancestor_jArr_jObj.optString( HREF_LINK_KEY );
				}

				if ( ancestor_jArr_jObj.getString( TYPE_KEY).equalsIgnoreCase( Types.CONTEST_LOBBY_DATA_TYPE ) ) {
					contestLobbyUid = ancestor_jArr_jObj.getString( UID_KEY );
					contestLobbyUrl = ancestor_jArr_jObj.optString( SELF_KEY );
					contestLobbyHrefUrl = ancestor_jArr_jObj.optString( HREF_LINK_KEY );

				}
			}
		






			JSONObject subject = jsonObj.getJSONObject(SUBJECT_KEY);		
			String vSubjectId = subject.getString( UID_KEY ); 
			String vSubjectSelfUrl   = subject.optString( SELF_KEY ); 
			String vSubjectSelfHrefUrl   = subject.optString( HREF_LINK_KEY ); 

			ContentValues values = new ContentValues();
			values.put( "vContestId", vSubjectId );
			values.put( "vContestUrl", vSubjectSelfUrl );
			values.put( "vContestHref", vSubjectSelfHrefUrl );
			
			dbUtil.setContestData ( values, vSubjectId );

			dbUtil.setHeader ( vSubjectSelfHrefUrl,vSubjectSelfUrl, subject.getString( TYPE_KEY ), false );
			
			
	
			 
			
			dbUtil.setContestLobbyData(contestLobbyUid, contestLobbyUrl, vSubjectId,contestLobbyHrefUrl);
			//dbUtil.setRoundData(roundUid,roundUrl);
			//dbUtil.setCompetition(leaguesUid,leaguesUrl);
			//dbUtil.setSports(sportsUid,sportsUrl);



			JSONObject invitationJSONObject = jsonObj.getJSONObject( INVITATION_KEY );			
			String invitationUrl = invitationJSONObject.optString(SELF_KEY);
			String invitationHrefUrl = invitationJSONObject.optString(HREF_LINK_KEY);
			dbUtil.setHeader( invitationHrefUrl,invitationUrl, "application/vnd.playup.conversation.invitation+json", false );

			JSONObject presenceJSONObject = jsonObj.getJSONObject( PRESENCE_KEY );
			String presenceUrl = "", totalPresences = "",presenceHrefUrl = "";
			if(presenceJSONObject.getString(TYPE_KEY).equalsIgnoreCase(Types.PRESENCE_DATA_TYPE)){
				 presenceUrl = presenceJSONObject.optString(SELF_KEY);
				 presenceHrefUrl = presenceJSONObject.optString(HREF_LINK_KEY);
				 totalPresences = presenceJSONObject.getString(TOTAL_PRESENCES);
			}
			

			String friendsUrl  = "",friendsUid = "",friendsHrefUrl = "";
			JSONObject friendsJSONObject = jsonObj.getJSONObject( FRIENDS_KEY );
			if(friendsJSONObject.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
			 friendsUrl = friendsJSONObject.optString(SELF_KEY);
			 friendsHrefUrl = friendsJSONObject.optString(HREF_LINK_KEY);
			 friendsUid = friendsJSONObject.getString(UID_KEY);
			}



			String name = jsonObj.getString( NAME_KEY );
			String access = jsonObj.getString( ACCESS_KEY );
			String vSelfUrl = jsonObj.optString( SELF_KEY );
			String vHrefUrl = jsonObj.optString( HREF_LINK_KEY  );




			dbUtil.setConversation (contestLobbyUid ,vConversationId, vSelfUrl,name,access,invitationUrl,presenceUrl,
					totalPresences,friendsUrl,friendsUid, vSubjectId, iOrder, isOrderSet,
					vHrefUrl, invitationHrefUrl , presenceHrefUrl,friendsHrefUrl);


			// getting the conversation message data  and storing in database
			JSONObject messageJSONObject = jsonObj.getJSONObject( MESSAGE_KEY );

			JsonUtil json = new JsonUtil();
			json.setConversationId( vConversationId );
			json.setBooleanFlag( false );
			json.setBooleanFlag2( true );
			json.parse( new StringBuffer( messageJSONObject.toString() ) , Constants.TYPE_CONVERSATION_MESSAGES_JSON, true );

			//new Util().getContestData(vSubjectSelfUrl);

			


		} catch (JSONException e) {
			Logs.show(e);
		} finally {

			/**
			 * Cleaning Memory
			 */
			new Util().releaseMemory(jsonObj);
			if ( !inTransaction ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
			

			}
		}

	}


	public String getConversationId () {

		return vConversationId;
	}





	//	private void parseData ( JSONObject  jsonObj ) {
	//
	//		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
	//
	//
	//		try {
	//			
	//
	//
	//			String vConversationId = jsonObj.getString( UID_KEY );
	//			
	//			// saving into the database
	//			
	//			JSONObject subjectJSONObject = jsonObj.getJSONObject( SUBJECT_KEY );
	//			String vSubjectId = subjectJSONObject.getString( UID_KEY ); 
	//			String vSelfUrl   = subjectJSONObject.getString( SELF_KEY ); 
	//			dbUtil.setHeader ( vSelfUrl, subjectJSONObject.getString( TYPE_KEY ) );
	//
	//			// saving the subject 
	//			dbUtil.setConversationSubject ( vSubjectId, vSelfUrl );
	//			new Util().getConversationSubjectData ( vSubjectId, vSelfUrl );
	//			
	//			JSONObject invitationJSONObject = jsonObj.getJSONObject( INVITATION_KEY );
	//			JSONObject presenceJSONObject = jsonObj.getJSONObject( PRESENCE_KEY );
	//			
	//			// saving the conversation data
	//			dbUtil.setHeader ( jsonObj.getString( SELF_KEY ), jsonObj.getString( TYPE_KEY ) );
	//			dbUtil.setConversation ( vConversationId, jsonObj.getString( SELF_KEY ), jsonObj.getString( NAME_KEY ), 
	//					jsonObj.getString( ACCESS_KEY ) , invitationJSONObject.getString( SELF_KEY ), 
	//					presenceJSONObject.getString( SELF_KEY ), presenceJSONObject.getString( TOTAL_PRESENCES ), 
	//					vSubjectId );
	//			
	//			// getting the conversation message data  and storing in database
	//			JSONObject messageJSONObject = jsonObj.getJSONObject( MESSAGE_KEY );
	//			new ConversationsMessagesJsonUtil( messageJSONObject, vConversationId, cacheTime );
	//			
	//			/**
	//			 * Cleaning Memory
	//			 */
	//			new Util().releaseMemory(jsonObj);
	//			
	//			
	//		} catch (JSONException e) {
	//			  
	//		}
	//
	//	}


}
