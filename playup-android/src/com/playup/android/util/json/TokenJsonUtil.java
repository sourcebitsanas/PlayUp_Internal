package com.playup.android.util.json;

import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

import com.playup.android.connection.HttpRequest;
import com.playup.android.exception.RequestRepeatException;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Util;

public class TokenJsonUtil {

	private final String TOKEN		               =  "token";
	private final String USER	    	           =  "user";

	private boolean inTransaction = false;

	public TokenJsonUtil ( JSONObject jsonObj , boolean inTransaction ) {

		this.inTransaction = inTransaction;
		if ( jsonObj != null ) {
			parseData ( jsonObj );
		}
	}

	private void parseData ( JSONObject jsonObj ) {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
		
		if ( !inTransaction  ) {
//			Logs.show ( "begin ------------------------------------TokenJsonUtil ");
			dbUtil.getWritabeDatabase().beginTransaction();
			
		}
			
		// }
		try {

			/*
			 * 
			 * Parsing Token Content
			 */
			//JSONObject jsonObj = new JSONObject( str );
			// getting the ids
			//String token 			= jsonObj.getString( TOKEN );

			
			Hashtable<String, Object> result = dbUtil.getProfileURLFromRootResource();
				String user_profile_url = (String) result.get("url");
				Boolean isHrefURl = (Boolean) result.get("isHref");
			/**
			 * Once We get the token we should hit the Profile URL
			 */
			// user_profile_url = "http://uat.fanbase.playupdev.com/users/81?token=LK2UFZaymz1wX5mZyAsW";
			/*HttpRequest	request  =  new HttpRequest( user_profile_url, Constants.GET_METHOD  );
=======
			String user_profile_url = jsonObj.getString( USER );
			/*String user_profile_url = dbUtil.getProfileURLFromRootResource();
			*//**
			 * Once We get the token we should hit the Profile URL
			 *//*
			// user_profile_url = "http://staging.fanbase.playupdev.com/users/14320?token=zU6XyqA8ePfsPxLSVXVx";
						HttpRequest	request  =  new HttpRequest( user_profile_url, Constants.GET_METHOD  );
>>>>>>> Stashed changes
			try {

				// getting the response
				StringBuffer strBuffer = (StringBuffer) request.send();

				// checking for server not found
				if ( strBuffer != null && strBuffer.toString().trim().length() > 0 ) {
					JsonUtil json = new JsonUtil();
					json.parse ( strBuffer, Constants.TYPE_PRIMARY_PROFILE_URL, true );
				}

			} catch (RequestRepeatException e) {
				  
			}
			

			/**
			 * Once We get the token: We need to update the token
			 */


		//dbUtil.setUserTokenPrimary( token );

			new Util().getProfileData ();

			/**
			 * Cleaning Memory
			 */
			

		} catch (Exception e) {
			//Logs.show(e);  
		} finally {
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			new Util().releaseMemory(jsonObj);
			if ( !inTransaction  ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( "end ------------------------------------TokenJsonUtil ");
				
			}
			// }
		}
	}

}
