package com.playup.android.util.json;

import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import android.util.Log;

import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;


public class DirectMessagesJsonUtil {

	private final String UID_KEY           	=  ":uid";
	private final String SELF_KEY          	=  ":self";
	private final String HREF_URL_KEY          	=  ":href";
	private final String TYPE_KEY          	=  ":type";


	private final String ITEM_KEY = "items";
	private final String UNREAD_COUNT    = "unread_message_count";
	private final String TOTAL_COUNT = "total_count";
	private final String VERSION = ":version";
	private final String ANCESTOR_KEY = "ancestors";

	private final String SUBJECT_KEY = "subject";
	private final String DISPLAY_KEY = "display_name";
	private final String AVATAR_KEY = "avatar";


	private final String MESSAGE_KEY = "messages";
	private final String ADDITION_KEY = "additions";
	private final String MARKER_KEY  = "marker";


	private String vDirectConversationUrl = null;
	private String vDirectConversationId = null;
	private String vUserId = null;
	private boolean inTransaction = false;
	private boolean fromPush;
	private String directConversationPushId = null;
	
	
	private String vDMessageUrl = null,vDMessageHrefUrl=null;
	private boolean isDirectConversationUrlHref = false;

	public DirectMessagesJsonUtil ( String str,boolean inTransaction,String vDirectConversationUrl,String directConversationPushId ) {
		
		
		this.vDirectConversationUrl = vDirectConversationUrl;
		this.inTransaction = inTransaction;
		this.directConversationPushId  = directConversationPushId;
		if ( str != null && str.trim().length() > 0 ) {
			parseData ( str );
			
			
			str = null;
		}
	}

	public DirectMessagesJsonUtil ( String str, 
			String vUserId, String vDirectConversationId, 
			boolean inTransaction, String vDirectConversationUrl,boolean isDirectConversationUrlHref ) {

		this.vUserId = vUserId;
		this.inTransaction = inTransaction;
		this.vDirectConversationId = vDirectConversationId;
		this.vDirectConversationUrl = vDirectConversationUrl;
		this.isDirectConversationUrlHref  = isDirectConversationUrlHref;
		if ( str != null && str.trim().length() > 0 ) {
			
			
			parseData ( str );

			str = null;
		}
	}

	private void parseData ( String  str ) {
		JSONObject jObj =null;
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();

		if ( !inTransaction ) {
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
		
			
				dbUtil.getWritabeDatabase().beginTransaction();
			//}
		}
		try {

			

		   jObj = new JSONObject( str );
			
			if(!(jObj.getString(TYPE_KEY)).equalsIgnoreCase(Types.DIRECT_CONVERSATION_DATA_TYPE))
				return;

			String vDirectMessageUrl = jObj.optString( SELF_KEY );
			String vDirectMessageHrefUrl = jObj.optString( HREF_URL_KEY );
			String vDirectMessageId  = jObj.getString( UID_KEY );
			
			new Util().setColor(jObj);
			
			dbUtil.setHeader( vDirectMessageHrefUrl,vDirectMessageUrl , jObj.getString( TYPE_KEY ), false  );
			 if ((directConversationPushId == null ||(directConversationPushId != null && directConversationPushId.trim().length() == 0)) && vUserId == null ) {
				vUserId = dbUtil.getPrimaryUserId();
			}
			 
			 
			 int unread_count = jObj.getInt( UNREAD_COUNT );			 
			 JSONObject subject_jObj = jObj.getJSONObject( SUBJECT_KEY );
			 String vUserSelfUrl = "",vUserHrefUrl="",vUserId2 = "",vDisplayName = "",vAvatarUrl = "";
			 if(subject_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.FAN_DATA_TYPE)){
				 vUserSelfUrl = subject_jObj.optString( SELF_KEY );
				 vUserHrefUrl = subject_jObj.optString( HREF_URL_KEY );
				 vUserId2 = subject_jObj.getString( UID_KEY );
				dbUtil.setHeader( vUserHrefUrl,vUserSelfUrl , subject_jObj.getString( TYPE_KEY ), false );

				 vDisplayName = subject_jObj.getString( DISPLAY_KEY );
				 vAvatarUrl = subject_jObj.getString( AVATAR_KEY );

				subject_jObj = null;
			 }
				
				
				if(directConversationPushId != null && directConversationPushId.trim().length() > 0)
					dbUtil.setUserDirectConversation(this.vDirectConversationUrl,vUserId2);
			
			if ( jObj.has( ANCESTOR_KEY )) {

				JSONArray ancestor_arr = jObj.getJSONArray( ANCESTOR_KEY );

				JSONObject ancestor_jObj = ancestor_arr.getJSONObject( 0 );
				if(ancestor_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.DIRECT_CONVERSATION_LOBBY_DATA_TYPE)){
				vDirectConversationId = ancestor_jObj.getString( UID_KEY );
				String vDirectConversationUrl_inner = ancestor_jObj.optString( SELF_KEY );
				String vDirectConversationHrefUrl_inner = ancestor_jObj.optString( HREF_URL_KEY );
				dbUtil.setHeader( vDirectConversationHrefUrl_inner,vDirectConversationUrl, ancestor_jObj.getString( TYPE_KEY ), false );
				
				if(directConversationPushId != null && directConversationPushId.trim().length() > 0)
					vUserId = vUserId2;
				
				if ( this.vDirectConversationUrl != null ) {
					dbUtil.setUserDirectConversation ( vDirectConversationId, this.vDirectConversationUrl, vUserId, vDirectMessageId,isDirectConversationUrlHref);
				} else {
					//dbUtil.setUserDirectConversation ( vDirectConversationId, vDirectConversationUrl_inner, vUserId, vDirectMessageId );
					dbUtil.setUserDirectConversation ( vDirectConversationId, vDirectConversationUrl_inner,vDirectConversationHrefUrl_inner, vUserId, vDirectMessageId );
				}
				}
			}
			
			/*dbUtil.setDirectConversation ( vDirectMessageUrl, vDirectMessageId, vDirectConversationId, unread_count, 
					vUserSelfUrl, vUserId2, vDisplayName, vAvatarUrl );*/
			dbUtil.setDirectConversation ( vDirectMessageUrl,vDirectMessageHrefUrl, vDirectMessageId, vDirectConversationId, unread_count, 
					vUserSelfUrl,vUserHrefUrl, vUserId2, vDisplayName, vAvatarUrl );
			
			if(directConversationPushId != null && directConversationPushId.trim().length() > 0){
				
				/*dbUtil.updateDirectConversationPushNotification(this.vDirectConversationUrl,vUserSelfUrl,directConversationPushId);				
				Constants.pushNotificationDownload.put(directConversationPushId, false);*/
				dbUtil.updateDirectConversationPushNotification(this.vDirectConversationUrl,vUserSelfUrl,vUserHrefUrl,directConversationPushId);				
				Constants.pushNotificationDownload.put(directConversationPushId, false);
			}

			JSONObject message_jObj = jObj.getJSONObject( MESSAGE_KEY );
			if(! message_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE))
				return;
			vDMessageUrl = message_jObj.optString( SELF_KEY );
			vDMessageHrefUrl = message_jObj.optString( HREF_URL_KEY );
			String vDMessageId = message_jObj.getString( UID_KEY );
			dbUtil.setHeader( vDMessageHrefUrl,vDMessageUrl,  message_jObj.getString( TYPE_KEY ), false );
			String vAdditionUrl = "",vMarkerUrl = "",vMarkerHrefUrl="",vAdditionHrefUrl="";
			JSONObject additition_jObj = message_jObj.getJSONObject( ADDITION_KEY );
			if(additition_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
			 vAdditionUrl = additition_jObj.optString( SELF_KEY );
			 vAdditionHrefUrl = additition_jObj.optString( HREF_URL_KEY );
			dbUtil.setHeader( vAdditionHrefUrl,vAdditionUrl, additition_jObj.getString( TYPE_KEY ), false );
			additition_jObj = null;
			}

			JSONObject marker_jObj = message_jObj.getJSONObject( MARKER_KEY );
			if(marker_jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.MARKER_DATA_TYPE)){
			 vMarkerUrl = marker_jObj.optString( SELF_KEY );
			 vMarkerHrefUrl = marker_jObj.optString( HREF_URL_KEY );
			 
			 Log.e("123","vMarkerUrl >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+vMarkerUrl);
			 
			 Log.e("123","vMarkerHrefUrl >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+vMarkerHrefUrl);
			 
			dbUtil.setHeader( vMarkerHrefUrl,vMarkerUrl, marker_jObj.getString( TYPE_KEY ), false );
			marker_jObj = null;
			}

			int total_count  = message_jObj.getInt( TOTAL_COUNT );
			
			int version = 0;
			
			if ( message_jObj.has( VERSION ) ) {
				version = message_jObj.getInt( VERSION );
			}
		//	dbUtil.setDirectMessages ( vDMessageUrl, vDMessageId, vAdditionUrl, vMarkerUrl, total_count, version, vDirectMessageId );
			dbUtil.setDirectMessages ( vDMessageUrl,vDMessageHrefUrl, vDMessageId, vAdditionUrl,vAdditionHrefUrl, vMarkerUrl,vMarkerHrefUrl, total_count, version, vDirectMessageId );
			JSONArray item_jarr = message_jObj.getJSONArray( ITEM_KEY );
			int len = item_jarr.length();

			for ( int i = 0; i < len; i++ ) {
				
				if ( i == 0 ) {
					PlayupLiveApplication.getDatabaseWrapper().queryMethod2( Constants.QUERY_DELETE, null, "direct_message_items", null, " vDMessageId = \"" + vDMessageId + "\" " );
				}
				JsonUtil json = new JsonUtil();
				json.setDMessageId ( vDMessageId );

				json.parse( new StringBuffer( item_jarr.getString( i ) ), Constants.TYPE_DIRECT_MESSAGES_ITEM_JSON, true );


			}


			


		} catch (JSONException e) {

			Logs.show( e) ;
		}  finally {
			new Util().releaseMemory( jObj );
			if ( !inTransaction ) {
				/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
				} else {*/
					dbUtil.getWritabeDatabase().setTransactionSuccessful();
					dbUtil.getWritabeDatabase().endTransaction();
					
				// }
			}
		}
	}
	
	
	public Hashtable<String,Object> getDirectMessageUrl () {
		Hashtable<String,Object> result = new Hashtable<String,Object>();
		if(vDMessageHrefUrl != null && vDMessageHrefUrl.trim().length() > 0){
			result.put("url",vDMessageHrefUrl);
			result.put("isHref",true);
		}else{
			result.put("url",vDMessageUrl);
			result.put("isHref",false);
		}
			
		
		return result;
	}
}
