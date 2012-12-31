package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONObject;

import com.playup.android.activity.PlayUpActivity;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class BaseSectionsJsonUtil {

	private final String ICON			=	"icon";
	private final String TARGET			=	"target";
	
	private final String DENSITY_KEY = "density";
	private final String HREF_KEY = "href";
	private  final String TITLE_KEY = "title";
	private final String SELF_KEY = ":self";
	private final String HREF_URL_KEY = ":href";
	private final String UID_KEY = ":uid";
	private final String ITEM_KEY = "items";
	
	private final String HREF_LINK_KEY = ":href";
	

	private boolean inTransaction = false;
	private final String TYPE_KEY                  = ":type";	
	
	public BaseSectionsJsonUtil( String str, boolean inTransaction ) {

		this.inTransaction = inTransaction;
		if (str  != null) {
			parseData(str);
		}

	}

	private void parseData(String str) {
		JSONObject jsonObj = null;
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		/*if ( Constants.X <= 320 && Constants.Y <= 480 && Constants.DPI <= 160 ) {
		} else {*/
		
		if ( !inTransaction ) {
			
			dbUtil.getWritabeDatabase().beginTransaction();
			
		}
		
		// }
		try{
			
			jsonObj = new JSONObject(str);
			
			JSONArray jsonArray = jsonObj.getJSONArray(ITEM_KEY);
			
			for( int i = 0; i< jsonArray.length(); i++ ) {
				int iOrderId = i;
				String vBaseSectionTitle = null;
				String vBaseSectionImageUrl = null;

				String vBaseSectionTargetUrl = null,vBaseSectionTargetHrefUrl=null;

				String vBaseSectionId = null;
				String vBaseSectionTargetId = null;
				JSONObject itemJsonObject = jsonArray.getJSONObject( i );
				
				if(!((itemJsonObject.getString(TYPE_KEY).equalsIgnoreCase(Types.BASE_SECTION_LINK_DATA_TYPE)) ||
						(itemJsonObject.getString(TYPE_KEY).equalsIgnoreCase(Types.SECTION_DATA_TYPE))))
					continue;
						
				
				vBaseSectionTitle = itemJsonObject.getString( TITLE_KEY );
				vBaseSectionId = itemJsonObject.getString( UID_KEY );
				
				JSONArray image_jArr = itemJsonObject.optJSONArray( ICON );
				String imageType = "low";
				
				if( PlayUpActivity.isXhdpi ) {
					imageType = "high";
				} else {
					if( Constants.DENSITY.equalsIgnoreCase("high") )
						imageType ="medium";
					else if ( Constants.DENSITY.equalsIgnoreCase("medium") )
						imageType ="low";
				}
				if(image_jArr != null ){

					for (int k = 0; k < image_jArr.length(); k++) {
	
						JSONObject logo_jObj = image_jArr.getJSONObject(k);
						
						if (imageType.equalsIgnoreCase(logo_jObj
								.getString(DENSITY_KEY))) {
	
							vBaseSectionImageUrl = logo_jObj.getString(HREF_KEY);
						}
	
					}
				
				}
				JSONObject targetObject = itemJsonObject.getJSONObject( TARGET );
				
				vBaseSectionTargetId = targetObject.getString( UID_KEY );
				vBaseSectionTargetUrl = targetObject.optString( SELF_KEY );

				vBaseSectionTargetHrefUrl = targetObject.optString(HREF_URL_KEY );
				
			/*	dbUtil.setBaseSectionData( vBaseSectionId, vBaseSectionTargetId, 
						vBaseSectionTitle, vBaseSectionImageUrl, vBaseSectionTargetUrl,iOrderId );
				dbUtil.setSectionData(vBaseSectionTargetId, vBaseSectionTargetUrl);*/
				
				dbUtil.setBaseSectionData( vBaseSectionId, vBaseSectionTargetId, 
						vBaseSectionTitle, vBaseSectionImageUrl, vBaseSectionTargetUrl,vBaseSectionTargetHrefUrl,iOrderId );

				dbUtil.setSectionData(vBaseSectionTargetId, vBaseSectionTargetUrl,vBaseSectionTargetHrefUrl);
								
			}
			
			
			
	
		}catch(Exception e){
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