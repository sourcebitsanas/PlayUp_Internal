
package com.playup.android.util.json;

import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;



import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Types;
import com.playup.android.util.Util;


public class ContestLobbyJsonUtil {

	private final String UID_KEY           =  ":uid";
	private final String SELF_KEY          =  ":self";
	private final String HREF_URL_KEY          =  ":href";
	private final String TYPE_KEY 		   = ":type";
	private final String TITLE_KEY 		   = "title";
	private final String CONVERSATIONS_KEY  	   = "conversations";
	private final String ITEM_KEY 	   = "items";
	private final String SUBJECT_KEY = "subject";
	private final String TILES_KEY = "tiles";
	private final String STYLE_KEY = "style";


	private boolean inTransaction = false;
	
	private String vContestId = null;
	private String vLinkUrl = null;
	
	public ContestLobbyJsonUtil ( JSONObject jsonObj, boolean inTransaction, String vLinkUrl  ) {

		this.vLinkUrl = vLinkUrl;
		this. inTransaction = inTransaction;
		if ( jsonObj != null  ) {
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
			
			String contestLobbyUid = jsonObj.getString( UID_KEY ); 
			if(!(jsonObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.CONTEST_LOBBY_DATA_TYPE)))
				return;
			if ( vLinkUrl != null ) {
				dbUtil.updateBlockContentLinkUrl ( vLinkUrl, jsonObj.optString( SELF_KEY ), jsonObj.optString( HREF_URL_KEY ) );
			}
			String title = jsonObj.getString( TITLE_KEY ); 				
			dbUtil.setHeader( jsonObj.optString( HREF_URL_KEY ), jsonObj.optString( SELF_KEY ), jsonObj.getString( TYPE_KEY ), false );
			
		
			new Util().setColor(jsonObj);
			
			JSONObject subjectJSONObject = jsonObj.getJSONObject( SUBJECT_KEY );			
			vContestId = subjectJSONObject.getString( UID_KEY ); 
			String vSubjectSelfUrl   = subjectJSONObject.optString( SELF_KEY );
			String vSubjectSelfHrefUrl   = subjectJSONObject.optString( HREF_URL_KEY ); 
			

			JSONObject tilesJSONObject =  jsonObj.getJSONObject( TILES_KEY );
		
			
			
			
			dbUtil.setHeader (vSubjectSelfHrefUrl, vSubjectSelfUrl, subjectJSONObject.getString( TYPE_KEY ), false );	
			
			
			

			JSONObject conversationsJSONObject =  jsonObj.getJSONObject( CONVERSATIONS_KEY );
			if(!(conversationsJSONObject.getString(TYPE_KEY)).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE))
				return;
			String vConversationUid =  conversationsJSONObject.getString( UID_KEY ); 
			String vConversationSelfUrl   = conversationsJSONObject.optString( SELF_KEY );
			String vConversationHrefUrl   = conversationsJSONObject.optString( HREF_URL_KEY ); 

			dbUtil.setHeader (vConversationHrefUrl, vConversationSelfUrl, conversationsJSONObject.getString( TYPE_KEY ) , false);
		/*	dbUtil.setContestLobbyConversationData(vConversationUid,vConversationSelfUrl,contestLobbyUid );*/
			dbUtil.setContestLobbyConversationData(vConversationUid,vConversationSelfUrl,vConversationHrefUrl,contestLobbyUid );
			
			
			
			 

			JSONArray arr = conversationsJSONObject.getJSONArray(ITEM_KEY);
			

			for ( int i=0; i < arr.length(); i++ ) {
				
				JsonUtil json = new JsonUtil();
				json.setOrder( ( arr.length() - i ) );
				json.parse( new StringBuffer( arr.getJSONObject(i).toString() ) , Constants.TYPE_CONVERSATION_JSON, true );
			
			}
			
			if(tilesJSONObject != null && tilesJSONObject.getString(STYLE_KEY).equalsIgnoreCase("tile")){
				if(tilesJSONObject.getString(TYPE_KEY).equalsIgnoreCase(Types.BLOCK_CONTENT_DATA_TYPE)){
				
				String tilesUid =  tilesJSONObject.getString( UID_KEY ); 
				
				dbUtil.updateContestLobbyData(title, vContestId, tilesUid );
				
				dbUtil.deleteContestTilesData(tilesUid);
			JSONArray tilesArr = tilesJSONObject.getJSONArray(ITEM_KEY);
			
			for(int  i  = 0;i<tilesArr.length() ;i++){
				
				JsonUtil json = new JsonUtil();
				json.setBlockTileId( tilesUid );
				json.setBlockOrderId( i );
				json.parse( new StringBuffer( tilesArr.getJSONObject(i).toString() ) , Constants.TYPE_TILE_DISPLAY_JSON, true );
				
			}
			}
			}
			
		

		} catch (JSONException e) {
			  
		//	Logs.show(e);
			
		}finally{
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			new Util().releaseMemory(jsonObj);
			if ( !inTransaction ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
			
				
			}
				
			
		}
	}

	
	public String getContestId () {
		return vContestId;
	}
}

