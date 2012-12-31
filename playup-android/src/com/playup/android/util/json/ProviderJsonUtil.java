package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.ImageDownloader;

import com.playup.android.util.Util;

public class ProviderJsonUtil {

	private ImageDownloader imageDownloader = new ImageDownloader();
	//private final String ITEMS_KEY 				=  "items";
	private final String PROVIDERS_KEY           =  "providers";
	private final String PROVIDERS_NAME          =  "name";
	private final String LOGIN_URL_KEY          =  "login";
	private final String SUCCESS_URL_KEY        =  "success_location";//"success_base_url";
	private final String FAILURE_URL_KEY        =  "failure_location";//"failure_base_url";
	private final String ICON_URL_KEY           =  "icon";


	private final String  ICON_LOGIN_KEY = "login";
	private final String ICON_LOGIN_DISABLED_KEY = "login_disabled";
	private final String ICON_LOGIN_HIGHLIGHT_KEY = "login_highlighted";


	private final String  ICON_BROADCAST_KEY = "broadcast";
	private final String ICON_BROADCAST_DISABLED_KEY = "broadcast_disabled";
	private final String ICON_BROADCAST_HIGHLIGHT_KEY = "broadcast_highlighted";

	private final String IDENTITY_KEY      		=  "identity";
	private final String NAME_KEY               =  "name";
	private final String AVATAR_KEY             =  "avatar";


	private final String DENSITY_KEY            =  "density";
	private final String HREF_URL_KEY           =  "href";


	//Facility is not comming properly from server "facilities":["broadcast","friends"], Not in Key Value Pair Form
	private final String FACILITIES				=	"facilities";

	private boolean inTransaction = false;

	public ProviderJsonUtil ( JSONObject jsonObj, boolean inTransaction ) {

		this.inTransaction = inTransaction ;
		if ( jsonObj != null ) {
			parseData (  jsonObj );
		}
	}

	private void parseData ( JSONObject jsonObj) {
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
		if ( !inTransaction ) {
//			Logs.show ( "begin ------------------------------------ProviderJsonUtil ");
			dbUtil.getWritabeDatabase().beginTransaction();
		}
		// }
		
		try {

			//JSONObject jsonObj = new JSONObject( str );

			JSONArray jArr = jsonObj.getJSONArray( PROVIDERS_KEY );

			int count = jArr.length();

			if ( count > 0 ) {
				dbUtil.emptyProvider();
			}
			for ( int i = 0; i < count ; i++ ) {

				JSONObject jObj = jArr.getJSONObject( i );

				// get the provider data .
				String providerName = jObj.getString( PROVIDERS_NAME );
				String loginUrl = jObj.getString( LOGIN_URL_KEY );
				String successUrl = jObj.getString( SUCCESS_URL_KEY );
				String failureUrl = jObj.getString( FAILURE_URL_KEY );

				JSONObject icon_jObj = jObj.getJSONObject( ICON_URL_KEY );

				// login icon
				JSONArray icon_login_jArr = icon_jObj.getJSONArray( ICON_LOGIN_KEY );
				String vIconLoginUrl = getUrl ( icon_login_jArr );

				JSONArray icon_login_disabled_jArr = icon_jObj.getJSONArray( ICON_LOGIN_DISABLED_KEY );
				String vIconLoginDisabledUrl = getUrl ( icon_login_disabled_jArr );

				JSONArray icon_login_highlight_jArr = icon_jObj.getJSONArray( ICON_LOGIN_HIGHLIGHT_KEY );
				String vIconLoginHighLightUrl = getUrl ( icon_login_highlight_jArr );

				
				// broadcast icon
				JSONArray icon_broadcast_jArr = icon_jObj.getJSONArray( ICON_BROADCAST_KEY );
				String vIconBroadcastUrl = getUrl ( icon_broadcast_jArr );

				JSONArray icon_broadcast_disable_jArr = icon_jObj.getJSONArray( ICON_BROADCAST_DISABLED_KEY );
				String vIconBroadcastDisableUrl = getUrl ( icon_broadcast_disable_jArr );

				JSONArray icon_broadcast_highlight_jArr = icon_jObj.getJSONArray( ICON_BROADCAST_HIGHLIGHT_KEY );
				String vIconBroadcastHighLightUrl = getUrl ( icon_broadcast_highlight_jArr );

				/*imageDownloader.download( vIconLoginUrl,  null, false,null  );
				imageDownloader.download( vIconLoginDisabledUrl,  null, false,null  );
				imageDownloader.download(  vIconLoginHighLightUrl,  null, false,null  );

				imageDownloader.download( vIconBroadcastDisableUrl,  null, false,null  );
				imageDownloader.download( vIconBroadcastUrl,  null, false,null  );
				imageDownloader.download( vIconBroadcastHighLightUrl,  null, false,null  );*/
				
				// check if it contains identity
				if ( jObj.has( IDENTITY_KEY ) ) {
					JSONObject identityOObj =  jObj.getJSONObject( IDENTITY_KEY );
					String name = identityOObj.getString( NAME_KEY );
					String avatarName = identityOObj.getString( AVATAR_KEY );

					// store identity in data base 
					dbUtil.setProvider ( providerName, loginUrl, successUrl, failureUrl, vIconLoginUrl, vIconLoginDisabledUrl, vIconLoginHighLightUrl, vIconBroadcastUrl, vIconBroadcastDisableUrl, vIconBroadcastHighLightUrl, name, avatarName, 1 );
				} else {
					// store in database.
					dbUtil.setProvider ( providerName, loginUrl, successUrl, failureUrl, vIconLoginUrl, vIconLoginDisabledUrl, vIconLoginHighLightUrl, vIconBroadcastUrl, vIconBroadcastDisableUrl, vIconBroadcastHighLightUrl, "", "", 0 );
				}
			}
			
			
			
			
		} catch (JSONException e) {
			//  Logs.show(e);
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
//				Logs.show ( "end ------------------------------------ProviderJsonUtil ");
			}
				// }
		}
	}


	private String getUrl ( JSONArray jArr  ) {

		String url = null;

		if ( jArr != null ) {
			int len = jArr.length();
			for ( int i = 0; i < len; i++ ) {

				JSONObject iconObj;
				try {
					iconObj = jArr.getJSONObject( i );

					if ( iconObj != null ) {

						String density = iconObj.getString( DENSITY_KEY );
						if( density != null && density.trim().length() > 0 && density.equalsIgnoreCase( Constants.DENSITY  )) {

							url = iconObj.getString( HREF_URL_KEY);
						}
					}
					
				} catch (JSONException e) {
					  
				}
			}
		}
		return url;
	}
}



