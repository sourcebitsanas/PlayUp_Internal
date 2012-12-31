package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Types;
import com.playup.android.util.Util;


public class MyLobbyJsonUtil {

	private final String UID_KEY           	=  ":uid";
	private final String SELF_KEY          	=  ":self";
	private final String HREF_KEY          	=  ":href";
	private final String TYPE_KEY          	=  ":type";

	private final String CONVERSATION_KEY = "conversations";
	private final String SUBJECT_KEY = "subject";

	

	private final String ACCEPTABLE_TYPE_KEY = ":acceptable_types";
	private final String OPTIONS_KEY = ":options";
	private final String ITEM_KEY = "items";
	private final String TOTAL_COUNT_KEY = "total_count";

	private boolean inTransaction = false;
	private String vLobbyId = null;
	private String vLobbyUrl = null;
	private String vLobbyHrefUrl	= null;
	
	
	
	
	public MyLobbyJsonUtil ( String str, boolean inTransaction, String vLobbyId, String vLobbyUrl  ) {

		this.inTransaction = inTransaction;
		this.vLobbyId = vLobbyId;
		this.vLobbyUrl = vLobbyUrl;
		if ( str != null && str.trim().length() > 0 ) {
			parseData ( str );
		}
	}

	private void parseData ( String  str ) {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		JSONObject jObj = null;
		//Logs.show ( "begin ------------------------------------MyLobbyJsonUtil " + str);
			dbUtil.getWritabeDatabase().beginTransaction();
		
		try {
			jObj = new JSONObject( str );


			// checking for the type to filter out  unwanted data.
			if ( jObj.getString( TYPE_KEY ).equalsIgnoreCase( Types.MY_LOBBY_DATA_TYPE ) ) {
				
				vLobbyUrl = jObj.optString( SELF_KEY );
				vLobbyHrefUrl = jObj.optString(HREF_KEY);
			//	vLobbyUrl = jObj.getString( SELF_KEY );
				vLobbyId = jObj.getString( UID_KEY );
				
				new Util().setColor(jObj);

				if ( jObj.has( SUBJECT_KEY ) )  {
					JSONObject subjecct_jObj = jObj.getJSONObject( SUBJECT_KEY );
					
					String vLobbySubjectId = subjecct_jObj.getString( UID_KEY );
					String vLobbySubjectUrl = subjecct_jObj.optString( SELF_KEY );
					String vLobbySubjectHrefUrl = subjecct_jObj.optString( HREF_KEY );
					
					dbUtil.setHeader( vLobbySubjectHrefUrl,vLobbySubjectUrl, subjecct_jObj.getString( TYPE_KEY ), false );
					
					dbUtil.setLobbySubject ( vLobbySubjectId, vLobbySubjectUrl,vLobbySubjectHrefUrl );
				}
				
		

			//Praveen :" mofified as per the Href
			// parsing the conversation data 
			dbUtil.setHeader( vLobbyHrefUrl,vLobbyUrl, jObj.getString( TYPE_KEY), false );
			
			if ( jObj.has( CONVERSATION_KEY ) ) {
					JSONObject conJsonObject = jObj.getJSONObject( CONVERSATION_KEY );
					if(!(conJsonObject.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)))
						return;
					String vLobbyConversationId = conJsonObject.getString( UID_KEY );
					//String vLobbyConversationUrl = conJsonObject.getString( SELF_KEY );
					String vLobbyConversationUrl = conJsonObject.optString( SELF_KEY );
					String vLobbyConversationHrefUrl = conJsonObject.optString( HREF_KEY );

					dbUtil.setHeader ( vLobbyConversationHrefUrl,vLobbyConversationUrl, conJsonObject.getString( TYPE_KEY ), false );

					int iTotalCount = conJsonObject.getInt( TOTAL_COUNT_KEY );

				/*	// adding data in database
					dbUtil.setFrindLobbyconversation ( vLobbyConversationId,  vLobbyConversationUrl, iTotalCount, vLobbyId, vLobbyUrl );*/
				
				// Pravee : modified as per the href element 
				//::: adding data in database

					dbUtil.setFrindLobbyconversation ( vLobbyConversationId,  vLobbyConversationUrl,vLobbyConversationHrefUrl, iTotalCount, vLobbyId, vLobbyUrl,vLobbyHrefUrl );
					// getting the acceptable types
					String vAcceptableType = null;
					if ( conJsonObject.has( ACCEPTABLE_TYPE_KEY ) ) {
						JSONArray acceptableType_jArr = conJsonObject.getJSONArray( ACCEPTABLE_TYPE_KEY );
						int acceptableTypeCount = acceptableType_jArr.length();
						
						for ( int i = 0; i < acceptableTypeCount; i++ ) {

							if ( vAcceptableType == null ) {
								vAcceptableType  = acceptableType_jArr.getString( i ); 
							} else {
								vAcceptableType = ", "+ acceptableType_jArr.getString( i ); 
							}
						}
						acceptableType_jArr  = null;
					}
					
					// getting the options 
					if ( conJsonObject.has( OPTIONS_KEY ) ) {
						JSONArray options_jArr = conJsonObject.getJSONArray( OPTIONS_KEY );

						int optionsCount = options_jArr.length();
						String[] vOptions = new String [ optionsCount ];
						for ( int i = 0; i < optionsCount; i++ ) {
							vOptions [ i ] = options_jArr.getString( i ); 
						}
						options_jArr = null;
					}
					
					
					
					JSONArray items_jArr = conJsonObject.getJSONArray( ITEM_KEY );
					for ( int i = 0; i < items_jArr.length(); i++ ) {
						
						if ( i == 0 ) {
							dbUtil.setFriendConversationOrder( -1 );
						}
						if(!(items_jArr.getJSONObject( i ).getString(TYPE_KEY).equalsIgnoreCase(Types.PRIVATE_CONVERSATION_DATA_TYPE)))
							continue;
						JsonUtil json = new JsonUtil();
						json.setAcceptableType ( vAcceptableType );
						json.setPrivateLobbyOrderId(i);						
						json.parse( new StringBuffer( items_jArr.getJSONObject( i ).toString() ) , Constants.TYPE_FRIEND_CONVERSATION_JSON, true );
						
						//MyFriendConversationJsonUtil myFriendConversationJsonUtil= new MyFriendConversationJsonUtil ( items_jArr.getJSONObject( i ).toString(), true, vAcceptableType, i );
					}
				}

			}
			
			
		} catch (JSONException e) {
			//Logs.show ( e );
		}  finally {
		
			new Util().releaseMemory( jObj );
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( "end ------------------------------------MyLobbyJsonUtil ");
				
		}
	}
}
