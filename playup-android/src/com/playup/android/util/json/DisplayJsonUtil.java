package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Util;

public class DisplayJsonUtil {

	
	private final String DENSITY_KEY = "density";
	private final String HREF_KEY = "href";
	private  final String ACCESSORY_KEY = "accessory";	
	private  final String SUBTITLE_KEY = "subtitle";
	private  final String HIGHLIGHT_KEY = "hilight";
	private  final String SUMMARY_KEY = "summary";
	private  final String SOURCE_ICON_KEY = "source_icon";
	private  final String SOCIAL_ICON_KEY = "social_icon";
	private  final String TIMESTAMP_KEY = "time_stamp";
	
	private  final String BACKGROUND_COLOR_KEY = "background_color";
	private  final String BACKGROUND_IMAGE_KEY = "background_image";
	private  final String SOURCE_KEY = "source";
	private  final String LIVE_KEY = "live";
	
	
	private  final String FOOTER_TITLE_KEY = "footer_title";
	private  final String FOOTER_SUBTITLE_KEY = "footer_subtitle";
	private  final String IMAGE_KEY = "image";
	private  final String DISPLAY_KEY = ":display";

	private  final String LINK_KEY = "link";
	
	private  final String TITLE_KEY = "title";
	private final String SELF_KEY = ":self";
	private final String HREF_URL_KEY = ":href";
	
	private final String TYPE_KEY = ":type";
	private final String UID_KEY = ":uid";


	private final String NAME_KEY = "name";
	
	private boolean inTransaction = false;
	
	private String vBlockContentId = null;
	private String vContentId = ""; 
	
	public DisplayJsonUtil(String str, boolean inTransaction,
			String vBlockContentId,String vContentId) {
	
		this.inTransaction = inTransaction;
		
		this.vBlockContentId  = vBlockContentId;
		this.vContentId = vContentId;
		
		if(str != null &&  str.trim().length() > 0){
			parseData(str);
			
		}
		
	}

	private void parseData(String str){
		JSONObject content = null;
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		try{
			
			if ( !inTransaction ) {
//				Logs.show ( "begin ------------------------------------SectionJsonUtil ");
				dbUtil.getWritabeDatabase().beginTransaction();
				
			}
			
		
		JSONArray contentObj  = new JSONArray(str);
		
		if(contentObj != null && contentObj.length() > 0){
			
			ContentValues values = new ContentValues();
			content = contentObj.getJSONObject(0);
		
		
		values.put( "vContentId", content.getString(UID_KEY) );
		String vContentType = content.getString(TYPE_KEY);
		values.put( "vContentType", content.getString(TYPE_KEY) );
		
//		String vContentId = content.getString(UID_KEY);
		
		if(content.optString(SELF_KEY).trim().length() > 0)
			values.put( "vContentUrl", content.optString(SELF_KEY) );
		if(content.optString(HREF_URL_KEY).trim().length() > 0)
			values.put( "vContentHrefUrl", content.optString(HREF_URL_KEY) );
		
		if(content.getString(NAME_KEY).trim().length() > 0)
			values.put( "vName", content.getString(NAME_KEY) );
		
		JSONObject link = content.optJSONObject(LINK_KEY);
		
		if(link != null){
			if(link.optString(SELF_KEY).trim().length() > 0)
				values.put("vLinkUrl", link.optString(SELF_KEY));
			if(link.optString(HREF_URL_KEY).trim().length() > 0)
				values.put("vLinkHrefUrl", link.optString(HREF_URL_KEY));
			 if(link.optString(TYPE_KEY).trim().length() > 0)
				 values.put( "vLinkType", link.optString(TYPE_KEY) );
		}
		
		JSONObject display = content.getJSONObject(DISPLAY_KEY);	
		
		if(display.optString(SELF_KEY).trim().length() > 0)		
			values.put( "vDisplayUrl", display.optString(SELF_KEY) );
		if(display.optString(HREF_URL_KEY).trim().length() > 0)		
			values.put( "vDisplayHrefUrl", display.optString(HREF_URL_KEY) );
		if(display.getString(TYPE_KEY).trim().length() > 0)
			values.put( "vDisplayType", display.optString(TYPE_KEY) );
		
		
		
		
		if(display.optString(TITLE_KEY).trim().length() > 0)
			values.put( "vTitle", display.optString(TITLE_KEY) );
		
		
		if(display.optString(SUBTITLE_KEY).trim().length() > 0)
			values.put("vSubtitle", display.optString(SUBTITLE_KEY));
		if(display.optString(ACCESSORY_KEY).trim().length() > 0)
			values.put("vAccessory", display.optString(ACCESSORY_KEY));
		
		if(display.optString(SUMMARY_KEY).trim().length() > 0)
		values.put("vSummary", display.optString(SUMMARY_KEY));
		if(display.optString(FOOTER_TITLE_KEY).trim().length() > 0)
		values.put( "vFooterTitle", display.optString(FOOTER_TITLE_KEY) );
		
		if(display.optString(FOOTER_SUBTITLE_KEY).trim().length() > 0)
		values.put( "vFooterSubtitle", display.optString(FOOTER_SUBTITLE_KEY) );
		if(display.optString(TIMESTAMP_KEY).trim().length() > 0)
		values.put( "vTimeStamp", display.optString(TIMESTAMP_KEY) );
		
		if(display.optString(BACKGROUND_COLOR_KEY).trim().length() > 0)
		values.put( "vBackgroundColor", display.optString(BACKGROUND_COLOR_KEY) );
		
		String vBackgroundImage = "",vImage = "",vSocialIcon = "",vSourceIcon = "";
		if(display.optString(SOURCE_KEY).trim().length() > 0)
		values.put( "vSource", display.optString(SOURCE_KEY) );
		if( display.optBoolean(LIVE_KEY))
		values.put( "iLive", display.optBoolean(LIVE_KEY) ? 1 : 0 );

	
		
		
		if(display.optString(BACKGROUND_IMAGE_KEY).trim().length() > 0)
				values.put( "vBackgroundImage", display.optString(BACKGROUND_IMAGE_KEY) );
		if(display.optString(IMAGE_KEY).trim().length() > 0)
			values.put( "vImageUrl", display.optString(IMAGE_KEY) );
		if(display.optString(SOURCE_ICON_KEY).trim().length() > 0)
			values.put( "vSourceIcon", display.optString(SOURCE_ICON_KEY) );
		if(display.optString(SOCIAL_ICON_KEY).trim().length() > 0)
			values.put( "vSocialIcon", display.optString(SOCIAL_ICON_KEY) );
		
		
		JSONArray backgroundImage_jArr = display.optJSONArray(BACKGROUND_IMAGE_KEY);
		if(backgroundImage_jArr != null ){
			
		for (int k = 0; k < backgroundImage_jArr.length(); k++) {

			JSONObject logo_jObj = backgroundImage_jArr.getJSONObject(k);
			if (Constants.DENSITY.equalsIgnoreCase(logo_jObj
					.getString(DENSITY_KEY))) {

				vBackgroundImage = logo_jObj.getString(HREF_KEY);
				if(values.containsKey("vBackgroundImage")){
					values.remove("vBackgroundImage");
					values.put( "vBackgroundImage", vBackgroundImage );
				}
					
				
			}

		}
		}
		
		JSONArray image_jArr = display.optJSONArray(IMAGE_KEY);
		if(image_jArr != null ){

		for (int k = 0; k < image_jArr.length(); k++) {

			JSONObject logo_jObj = image_jArr.getJSONObject(k);
			if (Constants.DENSITY.equalsIgnoreCase(logo_jObj
					.getString(DENSITY_KEY))) {

				vImage = logo_jObj.getString(HREF_KEY);
				if(values.containsKey("vImageUrl")){
					values.remove("vImageUrl");
					values.put( "vImageUrl", vImage );
				}
				
			}

		}
		}
		
		
		JSONArray source_jArr = display.optJSONArray(SOURCE_ICON_KEY);
		if(source_jArr != null ){

		for (int k = 0; k < source_jArr.length(); k++) {

			JSONObject logo_jObj = source_jArr.getJSONObject(k);
			if (Constants.DENSITY.equalsIgnoreCase(logo_jObj
					.getString(DENSITY_KEY))) {

				vSourceIcon = logo_jObj.getString(HREF_KEY);
				if(values.containsKey("vSourceIcon")){
					values.remove("vSourceIcon");
					values.put( "vSourceIcon", vSourceIcon );
				}
			
			}

		}
		}
		
		
		JSONArray social_jArr = display.optJSONArray(SOCIAL_ICON_KEY);
		if(social_jArr != null ){

		for (int k = 0; k < social_jArr.length(); k++) {

			JSONObject logo_jObj = social_jArr.getJSONObject(k);
			if (Constants.DENSITY.equalsIgnoreCase(logo_jObj
					.getString(DENSITY_KEY))) {

				vSocialIcon = logo_jObj.getString(HREF_KEY);
				if(values.containsKey("vSocialIcon")){
					values.remove("vSocialIcon");
					values.put( "vSocialIcon", vSocialIcon );
				}
				
			}

		}
		}
		
		
		
		
		
		JSONObject highlight = display.optJSONObject(HIGHLIGHT_KEY);
		String vHighlightUrl = "";
		if(highlight != null){
			if(highlight.getString(UID_KEY).trim().length() > 0)
				values.put("vHighlightId", highlight.getString(UID_KEY));
			if(highlight.getString(TYPE_KEY).trim().length() > 0)
				values.put( "vHighlightType", highlight.getString(TYPE_KEY) );
			
			if(highlight.optString(HREF_URL_KEY).trim().length() > 0){
				values.put( "vHighlightHrefUrl", highlight.optString(HREF_URL_KEY) );
			new Util().getContestsData(vHighlightUrl, null,true);
			}
			else if(highlight.optString(SELF_KEY).trim().length() > 0){
				values.put( "vHighlightUrl", highlight.optString(SELF_KEY) );
				new Util().getContestsData(highlight.optString(SELF_KEY), null,false);				
			}
			

				

		}
		
		if(vContentType.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){
			
			JsonUtil json = new JsonUtil();										
			json.parse( new StringBuffer( content.toString() ) , Constants.TYPE_LEAGUE_ITEM, true );
			
			
		}
		
		dbUtil.setContetData(vBlockContentId,vContentId,values);
		
		
		
		}
	
	}catch(Exception e){
		
		///Logs.show(e);
	}finally{
		new Util().releaseMemory(content);
		if ( !inTransaction ) {
			dbUtil.getWritabeDatabase().setTransactionSuccessful();
			dbUtil.getWritabeDatabase().endTransaction();
//			Logs.show ( "end ------------------------------------SectionJsonUtil ");
			
		}
	}
}
}
