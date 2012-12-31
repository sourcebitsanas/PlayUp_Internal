package com.playup.android.util.json;

import java.util.Iterator;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Util;

public class ContestsConversationsJsonUtil {

	

//	private final String UID			 		=	":uid";
	private final String SELF					=	":self";

	private final String SUBJECY_KEY			=  	"subject";
	private final String CONVERSATIONS			=	"conversations";
	private final String CONVERSATIONS_ITEMS	=	"items";

	private final String INVITATIONS			=	"invitations";
	
	private final String PRESENCE				=	"presence";
//	private final String TOTAL_PRESENCE			=	"total_presences";	
	
	
	private final String TYPE_KEY =  ":type";
	
	
	
	
	
	
	private String subjectSelf					=	"";
	
	

	private String conversationSelf				=	"";
	
	private boolean inTransaction = false;
	public ContestsConversationsJsonUtil(JSONObject str, boolean inTransaction ) {

		this.inTransaction  = inTransaction ;
		if ( str != null  ) {

			parseData ( str );
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
			Iterator jsonObjectIterator	=		jsonObj.keys();
			
			
			while(jsonObjectIterator.hasNext()){
				String KEY	=	(String)jsonObjectIterator.next();
				if(KEY.equals(SUBJECY_KEY)){
					
					JSONObject jsonObject		=	jsonObj.optJSONObject(SUBJECY_KEY);
//					subjectTitile				=	jsonObject.optString(TITLE_KEY);
//					subjectUid					=	jsonObject.optString(UID);
					subjectSelf					=	jsonObject.optString(SELF);
					dbUtil.setHeader ( subjectSelf, jsonObject.getString( TYPE_KEY ) , false );
				}else if(KEY.equals(CONVERSATIONS)){
					JSONObject jsonObject		=	jsonObj.optJSONObject(CONVERSATIONS);
//					conversationUid				=	jsonObject.optString(UID);
					conversationSelf			=	jsonObject.optString(SELF);
					dbUtil.setHeader ( conversationSelf, jsonObject.getString( TYPE_KEY ), false );
					
					JSONArray	 conversations	=	jsonObject.optJSONArray(CONVERSATIONS_ITEMS);
					if(conversations!=null){
						
						/**
						 * One Conversation Contains
						 * name.contest,subject,invitations,messages,access,presence
						 * 
						 */
						int numItems	=	conversations.length();
						int iterator	=	0;
						
						while(iterator<numItems){
							
							
							
							
							JSONObject Object	=	conversations.getJSONObject(iterator);
							
							//Conversation Self
//							String conversationUid	=	Object.optString(UID);
							String conversationSelf	=	Object.optString(SELF);
							dbUtil.setHeader ( conversationSelf, Object.optString( TYPE_KEY ), false  );
							
							
							//Getting Invitations
							JSONObject invitationObject 	=	Object.optJSONObject(INVITATIONS);
							String invitationSelf			=	invitationObject.optString(SELF);
							dbUtil.setHeader ( invitationSelf, invitationObject.optString( TYPE_KEY ), false  );
							
							//Acessibility Public or Private
//							String accesibility 				=	Object.optString("access");
							
							//Presence 
							JSONObject presenceObject 	=	Object.optJSONObject(PRESENCE);
							String presenceSelf			=	presenceObject.optString(SELF);
							dbUtil.setHeader ( presenceSelf, presenceObject.optString( TYPE_KEY ) , false );
//							String totalPresence		=	presenceObject.optString(TOTAL_PRESENCE);
							iterator++;
						}
						
								
						
						
						
					}
					
				}
				

				
			}
			
			
			
			
		} catch (JSONException e) {
			  //Logs.show(e);
		}finally{
			
			/**
			 * Releasing Memory
			 */
			new Util().releaseMemory(jsonObj);
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			
			if ( !inTransaction ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
			//}
				
				
			}
				
		}
	}
	

}
	
	
	

