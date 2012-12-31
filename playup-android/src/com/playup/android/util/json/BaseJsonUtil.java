package com.playup.android.util.json;

import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Types;
import com.playup.android.util.Util;

// parsing the root url
public class BaseJsonUtil {


	private final String SELF_KEY 					=  ":self";
	
	private final String PROFILE					=	"profile";
	private final String HOME						=	"home";	
	private final String NOTIFICATIONS				=	"notifications";
	private final String SPORTS						=	"sports";
	private final String UID_KEY	 =  ":uid";
	private final String REGIONS_KEY	 =  "regions";
	private final String HREF_LINK_KEY = ":href";


	private final String DEFAULT_SECTION			=	"default_section";
	private final String SECTIONS			=	"sections";


	private final String TYPE_KEY                  = ":type";	



	public BaseJsonUtil ( String str ) {

		if ( str != null && str.trim().length() > 0 ) {
			parseBaseUrlData ( str );
		}
	}
	private void parseBaseUrlData ( String str ) {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		JSONObject jsonObj 	 = null;
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
	

		dbUtil.getWritabeDatabase().beginTransaction();
		//}
		try {

			jsonObj 			= 	new JSONObject( str );

			if(!jsonObj.getString(TYPE_KEY).equalsIgnoreCase(Types.ROOT_DATA_TYPE))
				return;

			Iterator jsonObjectIterator	=		jsonObj.keys();
			int index	=	0;
			boolean clearPreviousData	=	false;


			while ( jsonObjectIterator.hasNext( ) ) {
				if ( index == 0 )
					clearPreviousData	=	true;
				else
					clearPreviousData	=	false;

				String KEY	=	(String)jsonObjectIterator.next();

				if ( KEY.compareTo ( PROFILE ) == 0 ) {

					JSONObject jsonObject		=	jsonObj.optJSONObject(PROFILE);

					if(jsonObject.getString(TYPE_KEY).equalsIgnoreCase(Types.PROFILE_DATA_TYPE)){
						String resource_id = jsonObj.optString(UID_KEY);

						String userSelfUrl = jsonObject.optString(SELF_KEY);
						String userHrefUrl = jsonObject.optString(HREF_LINK_KEY);
						dbUtil.setHeader ( userHrefUrl,userSelfUrl, jsonObject.getString( TYPE_KEY ), false );
						dbUtil.setRootResources(PROFILE,userSelfUrl,clearPreviousData,resource_id, userHrefUrl);
						if(userHrefUrl != null && userHrefUrl.trim().length() > 0)
							Constants.LOGIN_ANNONYMOUS_URL = userHrefUrl;
						else
							Constants.LOGIN_ANNONYMOUS_URL = userSelfUrl;
						

					}

				} else if ( KEY.compareTo ( HOME ) == 0) {

					JSONObject jsonObject		=	jsonObj.optJSONObject(HOME);

					String vHomeSelfUrl = jsonObject.optString(SELF_KEY);
					String vHomeHrefUrl = jsonObject.optString(HREF_LINK_KEY);
					dbUtil.setHeader ( vHomeHrefUrl,vHomeSelfUrl, jsonObject.getString( TYPE_KEY ), false );
					String resource_id = jsonObj.optString(UID_KEY);
					dbUtil.setRootResources(HOME,vHomeSelfUrl,clearPreviousData,resource_id,vHomeHrefUrl);


				} else if ( KEY.compareTo ( NOTIFICATIONS ) == 0 ) {

					JSONObject jsonObject		=	jsonObj.optJSONObject(NOTIFICATIONS);
					String vNotificationSelfUrl = jsonObject.optString(SELF_KEY);
					String vNotificationHrefUrl = jsonObject.optString(HREF_LINK_KEY);
					String resource_id = jsonObj.optString(UID_KEY);

					dbUtil.setRootResources(NOTIFICATIONS,vNotificationSelfUrl,clearPreviousData,resource_id,vNotificationHrefUrl);



				} else if ( KEY.compareTo ( SPORTS ) == 0 ) {

					JSONObject jsonObject		=	jsonObj.optJSONObject(SPORTS);
					if(jsonObject.getString(TYPE_KEY).equalsIgnoreCase(Types.SPORTS_DATA_TYPE)){
						String resource_id = jsonObj.optString(UID_KEY);

						
						String vSportsSelfUrl =  jsonObject.optString(SELF_KEY);
						
						String vSportsHrefUrl =  jsonObject.optString(HREF_LINK_KEY);
						
						dbUtil.setHeader ( vSportsHrefUrl,vSportsSelfUrl, jsonObject.getString( TYPE_KEY ), false );
						dbUtil.setRootResources(SPORTS,vSportsSelfUrl,clearPreviousData,resource_id,vSportsHrefUrl);

					}

				} else if ( KEY.compareTo ( DEFAULT_SECTION ) == 0 ) {

					JSONObject jsonObject		=	jsonObj.optJSONObject(DEFAULT_SECTION);

					String resource_id = jsonObject.optString(UID_KEY);	
					String vDefaultSectionSelfUrl = jsonObject.optString(SELF_KEY);
					String vDefaultSectionHrefUrl = jsonObject.optString(HREF_LINK_KEY);
				
					dbUtil.setHeader ( vDefaultSectionHrefUrl,vDefaultSectionSelfUrl, jsonObject.getString( TYPE_KEY ), false );
					dbUtil.setRootResources(DEFAULT_SECTION,vDefaultSectionSelfUrl,clearPreviousData,resource_id,vDefaultSectionHrefUrl);



				} else if ( KEY.compareTo ( SECTIONS ) == 0 ) {

					JSONObject jsonObject		=	jsonObj.optJSONObject(SECTIONS);
					if(jsonObject.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
						String resource_id = jsonObj.optString(UID_KEY);

						
						String vSectionsUrl = jsonObject.optString(SELF_KEY);
						String vSectionHrefUrl = jsonObject.optString(HREF_LINK_KEY);
						dbUtil.setHeader ( vSectionHrefUrl,vSectionsUrl, jsonObject.getString( TYPE_KEY ), false );
						dbUtil.setRootResources(SECTIONS,vSectionsUrl,clearPreviousData,resource_id,vSectionHrefUrl);					

						new BaseSectionsJsonUtil(jsonObject.toString(), true );
					}
				}else if ( KEY.compareTo ( REGIONS_KEY ) == 0 ) {

					JSONObject jsonObject		=	jsonObj.optJSONObject(REGIONS_KEY);
					if(jsonObject.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
						String resource_id = jsonObj.optString(UID_KEY);

						String vRegionUrl = jsonObject.optString(SELF_KEY);
						String vRegionHrefUrl = jsonObject.optString(HREF_LINK_KEY);
						dbUtil.setHeader ( vRegionHrefUrl,vRegionUrl, jsonObject.getString( TYPE_KEY ), false );
						dbUtil.setRootResources(REGIONS_KEY,vRegionUrl,clearPreviousData,resource_id,vRegionHrefUrl);					


					}
				}
				index++;
			}






			
		} catch (JSONException e) {
		//	Logs.show(e);
		} finally {
			
			/**
			 * Release Memory
			 */
			new Util().releaseMemory(jsonObj);

			/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
			} else {*/
			dbUtil.getWritabeDatabase().setTransactionSuccessful();
			dbUtil.getWritabeDatabase().endTransaction();
			//}
			

		}
	}


}
