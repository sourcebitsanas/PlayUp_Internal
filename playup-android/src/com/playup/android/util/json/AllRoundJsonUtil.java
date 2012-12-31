package com.playup.android.util.json;




import org.json.JSONObject;

import android.os.Message;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Types;
import com.playup.android.util.Util;

/**
 * Parsing the competition data. 
 */
public class AllRoundJsonUtil {

	private final String UID_KEY           =  ":uid";
	private final String SELF_KEY          =  ":self";
	
	private final String TYPE_KEY 		   = ":type";
	private final String ITEM_KEY 		   = "items";
	private final String HREF_LINK_KEY = ":href";


	private boolean inTransaction = false;
	private String vCompetitionId = "";
	public AllRoundJsonUtil ( String str, boolean inTransaction ,String vCompetitionId) {

		this.inTransaction = inTransaction ;
		this.vCompetitionId  = vCompetitionId;
		if ( str != null && str.trim().length() > 0 ) {
			parseData ( str );
		}
	}

	private void parseData ( String str ) {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		JSONObject jsonObj = null;
		Message msg = new Message ();
		int len=0;
		if ( !inTransaction ) {

			
			dbUtil.getWritabeDatabase().beginTransaction();
		}
		try {

			jsonObj = new JSONObject( str );


			String vCompetitionRoundUrl = jsonObj.optString( SELF_KEY );

			String vCompetitionRoundHrefUrl = jsonObj.optString( HREF_LINK_KEY );
			

			String vCompetitionRoundId  = jsonObj.getString( UID_KEY );
			if(!jsonObj.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE))
				return;
			dbUtil.setCompetitionRoundData(vCompetitionId, vCompetitionRoundId, vCompetitionRoundUrl,vCompetitionRoundHrefUrl);

			// setting the round url's header ( content type )
			dbUtil.setHeader( vCompetitionRoundHrefUrl,vCompetitionRoundUrl, jsonObj.getString( TYPE_KEY ), false );
			len = jsonObj.getJSONArray( ITEM_KEY ).length();

			/*msg.obj = "CalendarUpdate";
			msg.arg1 = len;
			PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );*/


			for ( int i = 0; i < len; i++ ) {
				// getting the round data
				JSONObject item = jsonObj.getJSONArray( ITEM_KEY ).getJSONObject( i );
				if(!(item.getString(TYPE_KEY).equalsIgnoreCase(Types.ROUND_DATA_TYPE)))
					continue;
				JsonUtil json = new JsonUtil();

				json.setCompetitionRoundId( vCompetitionRoundId );
				json.parse( new StringBuffer( jsonObj.getJSONArray( ITEM_KEY ).getJSONObject( i ).toString() ) , Constants.TYPE_ROUND_JSON, true );

			}



		}catch (Exception e) {
			//Logs.show(e);
		}finally{
			new Util().releaseMemory(jsonObj);
			if ( !inTransaction ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
				
			}


		}



	}

}
