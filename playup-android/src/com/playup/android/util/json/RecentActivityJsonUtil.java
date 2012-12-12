package com.playup.android.util.json;

import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;




import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class RecentActivityJsonUtil {

	private final String UID_KEY           			= ":uid";
	private final String RECENT_NAME_KEY 			= "name";
	private final String SUBJECT_TITLE_KEY 			= "subject_title";
	private final String SUBJECT_KEY 				= "subject";
	private final String ACCESS_KEY 				= "access";
	private final String ACCESS_PERMITTED_KEY 		= "access_permitted";
	private final String UNREAD_KEY 				= "unread";	
	private final String SELF_KEY          			= ":self";
	private final String HREF_URL_KEY          			= ":href";
	private final String TYPE_KEY          			= ":type";
	private final String ITEMS_KEY       		    = "items";

	private final String RECENT_ACTIVITY = "recent_activity";





	private final String iUserId;
	private boolean inTransaction = false;


	public RecentActivityJsonUtil ( JSONObject jsonObj, String iUserId, boolean inTransaction ) {


		this.inTransaction = inTransaction;
		this.iUserId = iUserId;
		if ( jsonObj != null ) {
			parseData ( jsonObj );
		}
	}

	private void parseData ( JSONObject jsonObj) {
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/

		if ( !inTransaction ) {
//			Logs.show ( "begin ------------------------------------RecentActivityJsonUtil ");
			dbUtil.getWritabeDatabase().beginTransaction();
		}
		// }
		try {


			//JSONObject jsonObj = new JSONObject( str );

			// get recent activity
			if(jsonObj.has( RECENT_ACTIVITY )){
				jsonObj = jsonObj.getJSONObject(RECENT_ACTIVITY);
			}

			if(!jsonObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE))
				return;

			if ( jsonObj.has( SELF_KEY ) ||  jsonObj.has( HREF_URL_KEY )  ) {
				dbUtil.setHeader( jsonObj.optString( HREF_URL_KEY ),jsonObj.optString( SELF_KEY ), jsonObj.getString( TYPE_KEY ) , false  );
				/*dbUtil.setRecentAvtivityUrl(jsonObj.getString( SELF_KEY ), iUserId);*/
				dbUtil.setRecentAvtivityUrl(jsonObj.optString( SELF_KEY ),jsonObj.optString( HREF_URL_KEY ), iUserId);
			}
			String vUserRecentId = jsonObj.getString( UID_KEY );

			dbUtil.setUserRecent ( vUserRecentId, iUserId );

			JSONArray jArr = jsonObj.getJSONArray( ITEMS_KEY );
			int len = jArr.length();

		
			if ( len > 0 ) {

				dbUtil.setClearRecentAvtivity ( vUserRecentId );
			}
			for ( int i = 0; i < len; i++ ) {

				
				JSONObject jObj = jArr.getJSONObject( i );

				if ( jObj.getString( TYPE_KEY ).equalsIgnoreCase( Types.RECENT_CONVERSATION_DATA_TYPE ) ) {


					JSONObject subjectJObj = jObj.getJSONObject( SUBJECT_KEY );

					String vSubjectId = subjectJObj.getString( UID_KEY );
					String vSubjectUrl = subjectJObj.optString( SELF_KEY );
					String vSubjectHrefUrl = subjectJObj.optString( HREF_URL_KEY );

					dbUtil.setHeader( vSubjectHrefUrl,vSubjectUrl, subjectJObj.getString( TYPE_KEY )  , false  ); 

					if ( subjectJObj.getString( TYPE_KEY ).equalsIgnoreCase( Types.CONTEST_LOBBY_CONVERSATION_DATA_TYPE) ||
							subjectJObj.getString( TYPE_KEY ).equalsIgnoreCase( Types.PRIVATE_CONVERSATION_DATA_TYPE )) {
						String vRecentId = jObj.getString( UID_KEY );

						String vRecentName = null;
						if ( jObj.has( RECENT_NAME_KEY ) ) {
							vRecentName = jObj.getString( RECENT_NAME_KEY );
						}
						String vSubjectTitle = jObj.getString( SUBJECT_TITLE_KEY );

						int iAccess = ( jObj.getString( ACCESS_KEY ).equalsIgnoreCase("public") )? 0 : 1;
						int iAccessPermitted = ( jObj.getBoolean( ACCESS_PERMITTED_KEY ) )? 1 : 0;
						int iUnRead = jObj.getInt( UNREAD_KEY );


						/*dbUtil.setRecentAvtivity ( vRecentId, vRecentName, vSubjectTitle, vSubjectId, vSubjectUrl, iAccess, iAccessPermitted, iUnRead, vUserRecentId  );*/
						dbUtil.setRecentAvtivity ( vRecentId, vRecentName, vSubjectTitle, vSubjectId, vSubjectUrl,vSubjectHrefUrl, iAccess, iAccessPermitted, iUnRead, vUserRecentId  );

					}

				}

			}


			
		} catch (JSONException e) {
			Logs.show(e);
		}  finally {
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			
			/**
			 * Cleaning Memory
			 */
			new Util().releaseMemory(jsonObj);

			
			if ( !inTransaction ) {

				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( "end ------------------------------------RecentActivityJsonUtil ");
			}
		}
	}
}