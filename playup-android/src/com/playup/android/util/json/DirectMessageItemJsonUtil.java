package com.playup.android.util.json;

import org.json.JSONException;
import org.json.JSONObject;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;


public class DirectMessageItemJsonUtil {

	private final String UID_KEY           	=  ":uid";
	private final String SELF_KEY          	=  ":self";
	private final String HREF_URL_KEY          	=  ":href";
	private final String TYPE_KEY          	=  ":type";

	private final String MESSAGE_KEY = "message";
	private final String CREATED_KEY = "created";

	private final String FROM_KEY = "from";
	private final String DISPLAY_NAME_KEY = "display_name";
	private final String AVATAR_KEY = "avatar";

	private String vDMessageId = null;
	private boolean inTransaction = false;

	private String SIZE_KEY = "size";
	private String CONTENT_KEY = "contents";

	
	private boolean shouldDelete;

	public DirectMessageItemJsonUtil ( String str, String vDMessageId, boolean inTransaction, boolean shouldDelete ) {

		this.shouldDelete = shouldDelete;
		this.inTransaction = inTransaction;
		this.vDMessageId = vDMessageId;

		if ( str != null && str.trim().length() > 0 ) {
			parseData ( str );

			str = null;
		}
	}

	private void parseData ( String  str ) {

		JSONObject jObj = null;
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();

		if ( !inTransaction ) {
			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			
				dbUtil.getWritabeDatabase().beginTransaction();
				
				// }
		}
		try {

			jObj = new JSONObject( str );

			if ( jObj.getString( TYPE_KEY ).equalsIgnoreCase( Types.GAP_DATA_TYPE ) ) {

				if ( !shouldDelete ) {
					String vGapId = jObj.getString( UID_KEY );
					int iGapSize  = jObj.getInt( SIZE_KEY ) ;

					JSONObject content_jObj = jObj.getJSONObject( CONTENT_KEY );
					String vGapUrl = content_jObj.optString( SELF_KEY );
					String vGapHrefUrl = content_jObj.optString( HREF_URL_KEY );

					dbUtil.setHeader( vGapHrefUrl,vGapUrl, content_jObj.getString( TYPE_KEY ) , false );

				/*	dbUtil.setDirectMessageGapItem ( vGapId, iGapSize, vGapUrl, vDMessageId );*/
					dbUtil.setDirectMessageGapItem ( vGapId, iGapSize, vGapUrl,vGapHrefUrl, vDMessageId );

				}
			} else if(jObj.getString( TYPE_KEY ).equalsIgnoreCase( Types.MESSAGE_DATA_TYPE )){

				String vDMessageItemUrl = jObj.optString( SELF_KEY );
				String vDMessageItemHrefUrl = jObj.optString( HREF_URL_KEY );
				String vDMessageItemId  = jObj.getString( UID_KEY );
				dbUtil.setHeader( vDMessageItemHrefUrl,vDMessageItemUrl , jObj.getString( TYPE_KEY ), false  );

				String vMessage = jObj.getString( MESSAGE_KEY );
				String vCreatedDate = jObj.getString( CREATED_KEY );

				JSONObject from_jObj = jObj.getJSONObject( FROM_KEY );
				String vUserSelfUrl = "",vUserId = "",vDisplayName = "",vAvatarUrl = "",vUserHrefUrl="";
				if(from_jObj.getString(TYPE_KEY).equalsIgnoreCase(Types.FAN_DATA_TYPE)){
				 vUserSelfUrl = from_jObj.optString( SELF_KEY );
				 vUserHrefUrl = from_jObj.optString( HREF_URL_KEY );
				 vUserId = from_jObj.getString( UID_KEY );
				 dbUtil.setHeader( vUserHrefUrl,vUserSelfUrl, from_jObj.getString( TYPE_KEY ), false );

				 vDisplayName = from_jObj.getString( DISPLAY_NAME_KEY );
				 vAvatarUrl = from_jObj.getString( AVATAR_KEY );
				 from_jObj = null;
				}

			/*	dbUtil.setDirectMessageItem ( vDMessageId, vDMessageItemId, vDMessageItemUrl, vMessage,
						vCreatedDate, vUserSelfUrl, vUserId, vDisplayName, vAvatarUrl );
*/
				dbUtil.setDirectMessageItem ( vDMessageId, vDMessageItemId, vDMessageItemUrl,vDMessageItemHrefUrl, vMessage,
						vCreatedDate, vUserSelfUrl,vUserHrefUrl, vUserId, vDisplayName, vAvatarUrl );

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
}
