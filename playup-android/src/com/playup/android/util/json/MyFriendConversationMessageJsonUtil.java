package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;


public class MyFriendConversationMessageJsonUtil {

	private final String UID_KEY           	=  ":uid";
	private final String SELF_KEY          	=  ":self";
	private final String HREF_KEY          	=  ":href";
	private final String TYPE_KEY          	=  ":type";

	private final String SUBJECT_KEY = "subject";


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
	private String vConversationId = null;
	private String vConversationUrl = null;
	private String vConversationHrefUrl = null;
	private String SUBJECT_TITLE_KEY = "subject_title";

	private final String TYPE_GAP = "application/vnd.playup.collection.gap+json";
	private final String TYPE_MESSAGE = "application/vnd.playup.message.text+json";

	private final String SIZE_KEY = "size";
	private final String CONTENTS_KEY = "contents";


	private boolean shouldDelete = false ;
	private boolean insertGap = true;
	private boolean isConversationUrlHref = false;




	public MyFriendConversationMessageJsonUtil ( String str, boolean inTransaction, String vConversationId, String vConversationUrl, boolean shouldDelete , boolean insertGap,boolean isConversationUrlHref ) {

//	public MyFriendConversationMessageJsonUtil ( String str, boolean inTransaction, String vConversationId, String vConversationUrl, boolean shouldDelete , 
//			boolean insertGap, boolean isConversationUrlHref ) {


		this.insertGap = insertGap;
		this.shouldDelete = shouldDelete;
		this.inTransaction = inTransaction;
		this.vConversationId = vConversationId;
		this.vConversationUrl = vConversationUrl;

		

		this.isConversationUrlHref  = isConversationUrlHref;


		if ( str != null && str.trim().length() > 0 ) {
			parseData ( str );
		}
	}

	private synchronized void parseData ( String  str ) {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		JSONObject message_jObj = null;
		
		if ( !inTransaction ) {
			
		Logs.show ( "begin ------------------------------------MyFriendConversationMessageJsonUtil ");
			dbUtil.getWritabeDatabase().beginTransaction();
		}
			
		try {
			message_jObj = new JSONObject( str );
			if(!( message_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)))
				return;
			
			String vConversationMessageId = message_jObj.getString( UID_KEY );
			String vConversationMessageUrl = message_jObj.optString( SELF_KEY );
			String vConversationMessageHrefUrl = message_jObj.optString( HREF_KEY );

			dbUtil.setHeader( vConversationMessageHrefUrl,vConversationMessageUrl, message_jObj.getString( TYPE_KEY ), false );

			// message addition 
			JSONObject addition_jObj = message_jObj.getJSONObject(ADDITIONS_KEY );
			String vAdditionUrl = "",vAdditionHrefUrl = "";
			if(addition_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
			 vAdditionUrl = addition_jObj.optString( SELF_KEY );
			 vAdditionHrefUrl = addition_jObj.optString( HREF_KEY );
			dbUtil.setHeader( vAdditionHrefUrl,vAdditionUrl, addition_jObj.getString( TYPE_KEY ), false );
			}

			// message marker 
			JSONObject marker_jObj = message_jObj.getJSONObject( MARKER_KEY );
			String vMarkerUrl = "",vMarkerHrefUrl="";
			if(addition_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
			 vMarkerUrl = marker_jObj.optString( SELF_KEY );
			 vMarkerHrefUrl = marker_jObj.optString( HREF_KEY );
			 dbUtil.setHeader( vMarkerHrefUrl,vMarkerUrl, marker_jObj.getString( TYPE_KEY ), false );
			}

			int iTotalMessages = message_jObj.getInt( TOTAL_COUNT_KEY );


			int iVersion = 0;
			if ( message_jObj.has( VERSION_KEY ) ) {
				iVersion = message_jObj.getInt( VERSION_KEY );
			}



			
			dbUtil.setFriendConversationMessage ( vConversationMessageId, vConversationMessageUrl,vConversationMessageHrefUrl, vAdditionUrl,vAdditionHrefUrl, vMarkerUrl,vMarkerHrefUrl, vConversationId, vConversationUrl, iTotalMessages, iVersion,isConversationUrlHref );

			

			// delete all the previous messages and gaps 
			
			if ( shouldDelete ) {
				PlayupLiveApplication.getDatabaseWrapper().queryMethod2( Constants.QUERY_DELETE, null, "friendMessage", null,  " vConversationMessageId = \"" + vConversationMessageId + "\" " );
			}
			// message items 
			JSONArray message_item_jArr = message_jObj.getJSONArray( ITEM_KEY );
			for ( int j = 0; j < message_item_jArr.length() ; j ++ ) {

				JSONObject msg_jObj = message_item_jArr.getJSONObject( j );

				if ( msg_jObj.getString( TYPE_KEY ).equalsIgnoreCase( Types.GAP_DATA_TYPE ) ) {

					if ( insertGap ) {
						// saving gap 
						String vGapId = msg_jObj.getString( UID_KEY );
						int iGapSize = msg_jObj.getInt( SIZE_KEY );

						JSONObject content_jObj = msg_jObj.getJSONObject( CONTENTS_KEY );
						String vContentUrl = content_jObj.optString( SELF_KEY );
						String vContentHrefUrl = content_jObj.optString( HREF_KEY );
						dbUtil.setHeader( vContentHrefUrl,vContentUrl, content_jObj.getString( TYPE_KEY ), false );

						//dbUtil.setFriendMessageGap ( vGapId, iGapSize, vContentUrl, vConversationMessageId, vConversationMessageUrl );
						dbUtil.setFriendMessageGap ( vGapId, iGapSize, vContentUrl,vContentHrefUrl, vConversationMessageId, vConversationMessageUrl,vConversationMessageHrefUrl );

					}
				} else if(msg_jObj.getString( TYPE_KEY ).equalsIgnoreCase( Types.MESSAGE_DATA_TYPE ) ||
						msg_jObj.getString( TYPE_KEY ).equalsIgnoreCase( Types.FOLLOWING_MESSAGE_DATA_TYPE)){

					// saving normal message 
					String vMessageId = msg_jObj.getString( UID_KEY ) ;
					String vMessageUrl = msg_jObj.optString( SELF_KEY );
					String vMessageHrefUrl = msg_jObj.optString( HREF_KEY );
					dbUtil.setHeader(vMessageHrefUrl, vMessageUrl,  msg_jObj.getString( TYPE_KEY ), false );

					String vMessage = msg_jObj.getString( MSG_KEY );
					String vCreatedDate = msg_jObj.getString( CREATED_KEY );

					JSONObject from_jObj = msg_jObj.getJSONObject( FROM_KEY );
					String vSelfUrl = "",vSelfHrefUrl="",vUserId = "",vDisplayName = "",vAvatarUrl = "";
					if(from_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.FAN_DATA_TYPE)){
					 vSelfUrl = from_jObj.optString( SELF_KEY );
					 vSelfHrefUrl = from_jObj.optString( HREF_KEY );
					 vUserId = from_jObj.getString( UID_KEY );
					dbUtil.setHeader( vSelfHrefUrl,vSelfUrl, from_jObj.getString( TYPE_KEY ), false );

					 vDisplayName = from_jObj.getString( DISPLAY_NAME_KEY );
					 vAvatarUrl = from_jObj.getString( AVATAR_KEY );
					}

					String subjectTitle = "",subjectId = "",subjectUrl = "",subjectHrefUrl="";
					if(msg_jObj.has(SUBJECT_TITLE_KEY)){

						subjectTitle  = msg_jObj.getString(SUBJECT_TITLE_KEY );
					}

					if(msg_jObj.has(SUBJECT_KEY)){

						JSONObject subject = msg_jObj.getJSONObject(SUBJECT_KEY);
						subjectId = subject.getString(UID_KEY);
						subjectUrl = subject.optString(SELF_KEY);
						subjectHrefUrl = subject.optString(HREF_KEY);
						dbUtil.setHeader(subjectHrefUrl,subjectUrl, subject.get(TYPE_KEY).toString(), false);

					}

					/*dbUtil.setFriendMessage ( vMessageId, vMessageUrl, vMessage, 
							vSelfUrl, vUserId, vDisplayName, vAvatarUrl, vConversationMessageId, vConversationMessageUrl, 
							vCreatedDate ,subjectId ,subjectUrl,subjectTitle );*/
					dbUtil.setFriendMessage ( vMessageId, vMessageUrl,vMessageHrefUrl, vMessage, 
							vSelfUrl,vSelfHrefUrl, vUserId, vDisplayName, vAvatarUrl, vConversationMessageId, vConversationMessageUrl, vConversationMessageHrefUrl,
							vCreatedDate ,subjectId ,subjectUrl,subjectHrefUrl,subjectTitle );
				}

			}

			
			//new Util().releaseMemory( message_jObj );
		} catch (JSONException e) {
			Logs.show ( e );
		}  finally {
			new Util().releaseMemory( message_jObj );
			if ( !inTransaction ) {
				
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( "end ------------------------------------MyFriendConversationMessageJsonUtil ");
			}
				
		}
	}


	public String getConversationId () {
		return vConversationId;
	}
}
