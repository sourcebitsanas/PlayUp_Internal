
package com.playup.android.util.json;

import java.util.UUID;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;


/**
 * 
 * Parse data for conversations
 * 
 *
 */

public class ConversationsMessagesJsonUtil {

	private final String SELF_KEY =":self";
	private final String HREF_URL_KEY =":href";
	private final String UID_KEY =":uid";
	private final String MESSAGE_KEY ="message";
	private final String HREF_LINK_KEY = ":href";
	private final String MARKER_KEY ="marker";

	private final String ITEMS_KEY = "items";
	
	private final String TYPE_KEY =":type";
	private final String TYPE_SIZE ="size";
	private final String CREATED_KEY ="created";
	private final String FROM_KEY ="from";
	private final String DISPLAY_NAME_KEY = "display_name";
	private final String AVATAR_KEY = "avatar";
	
	
	private final String ADDITION_KEY = "additions";
	private final String CONTENT_KEY = "contents";
	private final String SIZE_KEY = "size";
	
	private String vConversationId = null;
	

	
	
	public int messageSize	=	0;
	private boolean isFromPolling =	 false;
	private boolean isFromContestLobby	=	false;
	
	private boolean inTransaction = false; 
	public ConversationsMessagesJsonUtil ( JSONObject jsonObj, String vConversationId,
			boolean isFromPolling,boolean isFromContestLobby , boolean inTransaction ) {
		this.vConversationId = vConversationId;
		this.inTransaction = inTransaction;
		
		this.isFromPolling	=	isFromPolling;
		this.isFromContestLobby	=	isFromContestLobby;
		if ( jsonObj != null) {
			parseData ( jsonObj );
		}
	}


	/**
	 * parse data 
	 * @param str
	 * @throws JSONException 
	 */
	private void parseData ( JSONObject jsonObj )  {
				DatabaseUtil dbUtil = DatabaseUtil.getInstance();
				/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
				} else {*/
				
				if ( !inTransaction ) {
				
					
					dbUtil.getWritabeDatabase().beginTransaction();
					
				}
				//}
				try{
		
					//JSONObject jsonObj = new JSONObject( str );
		
					String vConversationMessageId = jsonObj.getString( UID_KEY );
					String selfUrl = jsonObj.optString( SELF_KEY );

					String vHrefUrl = jsonObj.optString( HREF_LINK_KEY );
					
					if(!(jsonObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)))
						return;
					dbUtil.setHeader ( vHrefUrl,selfUrl, jsonObj.getString( TYPE_KEY ), false );
					String vNextUrl = null,vNextHref = "";

					String gap_uid	=	"";
					int gapSize		=	0;
		
					// fetching marker 

					String vMarkerUrl = "",vMarkerHrefUrl = "";
					JSONObject markerObj = jsonObj.getJSONObject( MARKER_KEY );
					if(markerObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.MARKER_DATA_TYPE)){
					 vMarkerUrl = markerObj.optString( SELF_KEY );
					 vMarkerHrefUrl = markerObj.optString( HREF_LINK_KEY );

					dbUtil.setHeader ( vMarkerHrefUrl,vMarkerUrl, markerObj.getString( TYPE_KEY ), false  );
					}
		
					// fetching additions

					String vAdditionUrl = "",vAdditionHrefUrl = "";
					JSONObject additionjObj = jsonObj.getJSONObject( ADDITION_KEY );
					if(additionjObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
					 vAdditionUrl = additionjObj.optString( SELF_KEY );
					 vAdditionHrefUrl = additionjObj.optString( HREF_LINK_KEY );

					dbUtil.setHeader (vAdditionHrefUrl, vAdditionUrl, additionjObj.getString( TYPE_KEY ), false  );
					}
								
					
					JSONArray itemsMsg= jsonObj.getJSONArray( ITEMS_KEY );
		
					int itemSize= itemsMsg.length();
					String lastMessageId	=	"";
					String lastMessageTimeStamp	=	"";
					boolean setNextUrl		=	true;
					
					for ( int i=0; i<itemSize ; i++ ) {
				
						JSONObject items =itemsMsg.getJSONObject(i);
						
						String type = items.getString( TYPE_KEY );
						if ( type != null && type.equalsIgnoreCase( Types.MESSAGE_DATA_TYPE) ) {
		
							messageSize++;
							ContentValues cValues = new ContentValues();
		
							cValues.put("message_id_pk", 		items.optString( SELF_KEY ) );

							cValues.put("vMessageHrefUrl", 		items.optString( HREF_LINK_KEY ) );
							
							
							dbUtil.setHeader ( items.optString( HREF_LINK_KEY ) ,items.optString( SELF_KEY ), items.getString( TYPE_KEY ) , false );

							cValues.put("vConversationMessageId", 		vConversationMessageId );
							lastMessageTimeStamp	=	items.getString( CREATED_KEY );
							cValues.put("message", 				items.getString( MESSAGE_KEY ) );
							cValues.put("message_timestamp", 	lastMessageTimeStamp);
							cValues.put("message_uid",			items.getString( UID_KEY ) );
							JSONObject fromObj = items.getJSONObject( FROM_KEY );
							if(fromObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.FAN_DATA_TYPE)){
							String iFanSelfUrl = fromObj.optString( SELF_KEY );

							String iFanHrefUrl = fromObj.optString( HREF_LINK_KEY );
							

							
							
							String iFanId = fromObj.getString( UID_KEY );
							
							/*if ( iFanSelfUrl != null && iFanSelfUrl.trim().length() > 0 ) {
								if ( iFanSelfUrl.contains( "me" ) ) {
									String iUserId = iFanId.replace("profile-", "");
									iFanSelfUrl = iFanSelfUrl.replace( "me", iUserId );
								}
							}*/
							dbUtil.setHeader ( iFanHrefUrl,iFanSelfUrl, fromObj.getString( TYPE_KEY ), false  );	
							cValues.put("posted_by", 			fromObj.getString( DISPLAY_NAME_KEY) );
							cValues.put("fan_profile_url", 		iFanSelfUrl );
							cValues.put("fan_profile_href", 		iFanHrefUrl );
							cValues.put("fan_thumb_url",		fromObj.getString( AVATAR_KEY ) );
							cValues.put("fan_profile_uid",		iFanId );
							}
							if(isFromContestLobby)
								cValues.put("isFromContestLobby",1 );
							else
								cValues.put("isFromContestLobby",0 );
							lastMessageId	=	cValues.getAsString("message_id_pk");
							cValues.put("vNextUrl","" );
							cValues.put("vNextHref","" );
							
							/*if(dbUtil.doesExistsMessage(lastMessageId))//No Need to set vNext URL
							{
								//if(!Constants.firstTimePolling)//GAP Issue
									setNextUrl	=	false;
							}*/
							//cValues.put("vNextUrl","");
							
						/*	dbUtil.setUserConversation( cValues );*/
							dbUtil.setUserConversation( cValues );
							
		
						} else if ( type != null && type.equalsIgnoreCase( Types.GAP_DATA_TYPE ) ) {
							
							
							gap_uid	=	items.getString(UID_KEY);
							gapSize	=	Integer.parseInt(items.getString(SIZE_KEY));
							
							JSONObject contents = items.getJSONObject( CONTENT_KEY );
							int size = items.getInt ( SIZE_KEY );
							
							
							// TO DO
							// have to write the logic for total mesage post
							if(isFromContestLobby)
							dbUtil.setConversationSize ( vConversationId, size + messageSize );
							vNextUrl = contents.optString( SELF_KEY );

							vNextHref = contents.optString( HREF_LINK_KEY );
							
							dbUtil.setHeader ( vNextHref,vNextUrl, contents.getString( TYPE_KEY ) , false  );

						}
						
					}
					
					if(((vNextUrl !=null && vNextUrl.trim().length() > 0)|| (vNextHref != null && vNextHref.trim().length() > 0)) && setNextUrl){ //Set the Next URL
						/**
						 * GAP Info will get inserted only once for GetConveration call. Very first time. Other wise a
						 */
					
						if(this.isFromPolling){
								ContentValues cValues = new ContentValues();
								UUID idOne = UUID.randomUUID();
	
								cValues.put("message_id_pk",""+idOne );
								cValues.put("vMessageHrefUrl",""+idOne );
								cValues.put("vConversationMessageId", vConversationMessageId);
								cValues.put("message","" );
								cValues.put("message_timestamp", lastMessageTimeStamp);
								cValues.put("message_uid","");
								cValues.put("posted_by","" );
								cValues.put("fan_profile_url", 		"" );
								cValues.put("fan_thumb_url",		"" );
								cValues.put("fan_profile_uid",		"");
								if(isFromContestLobby)
									cValues.put("isFromContestLobby",		1 );
								else
									cValues.put("isFromContestLobby",		0 );
								//cValues.put("marker_id",			"");
								cValues.put( "vNextUrl", vNextUrl);

								cValues.put( "vNextHref", vNextHref);
								dbUtil.setNextUrl(cValues,vNextUrl,vNextHref);
								dbUtil.setGapSize(gap_uid, vNextUrl,vNextHref, gapSize);

								
						}					
					}
					

					dbUtil.setConversationMessageData ( vConversationId, vConversationMessageId, selfUrl, 
							vMarkerUrl, vAdditionUrl,vHrefUrl,vMarkerHrefUrl, vAdditionHrefUrl );

		
					
					
		
				}catch (JSONException e) {
					 Logs.show(e);
				} finally {
					
					/**
					 * Cleaning Memory
					 */
					new Util().releaseMemory(jsonObj);
					jsonObj	= null;
					
					/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
					} else {*/
					
					if ( !inTransaction ) {
						dbUtil.getWritabeDatabase().setTransactionSuccessful();
						dbUtil.getWritabeDatabase().endTransaction();
						
						
					}
					// }
				}

		}

	
	
}