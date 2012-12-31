package com.playup.android.util.json;

import org.json.JSONException;
import org.json.JSONObject;



import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class AssociatedContestJson {

	private final String SELF_KEY          	=  ":self";
	private final String HREF_URL_KEY          	=  ":href";
	private final String TYPE_KEY 			= ":type";
	private final String UID_KEY         	=  ":uid";

	private final String ITEMS_KEY          =  "items";

	private boolean inTransaction = false;

	public AssociatedContestJson ( JSONObject str, boolean inTransaction ) {

		this.inTransaction = inTransaction;
		if ( str != null ) {
			parseData ( str );
		}
	}

	private void parseData ( JSONObject jsonObj ) {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();

		if ( !inTransaction ) {
		
			dbUtil.getWritabeDatabase().beginTransaction();
		}


		try {
			
			
			String associatedContestUrl = jsonObj.optString( SELF_KEY );
			String associatedContestHrefUrl = jsonObj.optString( HREF_URL_KEY );

			if(!jsonObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE))
				return;

		
			dbUtil.setHeader ( associatedContestHrefUrl,associatedContestUrl, jsonObj.getString( TYPE_KEY ), false  );
 
			String associatedContestId  = jsonObj.getString( UID_KEY );
			dbUtil.removeAssociatedContests(associatedContestId);

			for ( int i = 0, len = jsonObj.getJSONArray( ITEMS_KEY ).length(); i  < len ; i++ ) {

				JSONObject jObj = jsonObj.getJSONArray( ITEMS_KEY ).getJSONObject( i );		

				String contestId      = jObj.getString( UID_KEY );
			/*	dbUtil.setAssociatedContestData(associatedContestId,associatedContestUrl,contestId);*/
				dbUtil.setAssociatedContestData(associatedContestId,associatedContestUrl,associatedContestHrefUrl,contestId);

				new AssoiciatedContestsJsonUtil(jObj, "", true, false);
				
			}




		} catch (JSONException e) {
			//Logs.show ( e );

		}catch (Exception e) {
		//	Logs.show ( e );

		}catch (Error e) {
			//Logs.show ( e );

		}

		finally{

			new Util().releaseMemory(jsonObj);

			if ( !inTransaction ){

				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
				
			}	
		}
	}




}
