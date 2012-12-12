package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class BlockContentJsonUtil {

	
	private final String DENSITY_KEY = "density";
	private final String HREF_KEY = "href";
	

	
	private  final String HIGHLIGHT_KEY = "hilight";
	private  final String SUMMARY_KEY = "summary";
	private  final String SOURCE_ICON_KEY = "source_icon";
	private  final String SOCIAL_ICON_KEY = "social_icon";
	private  final String TIMESTAMP_KEY = "time_stamp";
	
	private  final String BACKGROUND_COLOR_KEY = "background_color";
	private  final String BACKGROUND_IMAGE_KEY = "background_image";
	private  final String SOURCE_KEY = "source";
	private  final String LIVE_KEY = "live";
	
	
private  final String ACCESSORY_KEY = "accessory";

	
	private  final String SUBTITLE_KEY = "subtitle";
	
	
	private  final String FOOTER_TITLE_KEY = "footer_title";
	private  final String FOOTER_SUBTITLE_KEY = "footer_subtitle";
	private  final String IMAGE_KEY = "image";
	private  final String DISPLAY_KEY = ":display";

	private  final String LINK_KEY = "link";
	
	private  final String TITLE_KEY = "title";
	private final String SELF_KEY = ":self";
	private final String HREF_LINK_KEY = ":href";
	private final String TYPE_KEY = ":type";
	private final String UID_KEY = ":uid";
	private final String LOBBY_KEY          		=  "contest_lobby";

	private final String NAME_KEY = "name";
	
	private boolean inTransaction = false;

	private String vBlockContentId = null;
	private int iOrderId = -1; 
	
	


	
	public BlockContentJsonUtil(String str, boolean inTransaction,
			String vBlockContentId,int iOrderId) {
	
		this.inTransaction = inTransaction;
		
		this.vBlockContentId  = vBlockContentId;
		this.iOrderId  = iOrderId;
		
		if(str != null &&  str.trim().length() > 0){
			parseData(str);
			
		}
		
	}

	private void parseData(String str){
		JSONObject content  = null;
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		try{
			
			if ( !inTransaction ) {
				
				dbUtil.getWritabeDatabase().beginTransaction();
				
			}
			
		
		content = new JSONObject(str);
		
		if ( ! content.has( TYPE_KEY ) ) {
			return;
		}
		
		
		
		String vName = null;
		String vDisplayUrl = null;
	
		String vDisplayType = null;
		String vLinkUrl = null;
		String vLinkHrefUrl = "";
		String vLinkType = null;
		String vImage = null;
		String vFooterTitle= null;
		String vFooterSubtitle= null;
		int iLive= 0;
		String vBackgroundColor= null;
		String vBackgroundImage= null;
		String vSummary= null;
		String vTitle= null;
		String vSource= null;
		String vSourceIcon= null;
		String vSocialIcon= null;
		String vTimeStamp= null;
		String vHighlightUrl= null;
		String vHighlightHrefUrl	= null;
		String vHighlightType= null;
		String vHighLightId= null;
		String vAccessory= null; 
		String vSubtitle =null;

		
		String vContentType = content.getString(TYPE_KEY);
		String vContentId = content.getString(UID_KEY);		
		String vContentUrl = content.optString(SELF_KEY);
		
		String vContentHrefUrl = content.optString(HREF_LINK_KEY);

		
		if( vContentType == null )
			return;

			vName = content.optString(NAME_KEY);
			JSONObject link = content.optJSONObject(LINK_KEY);
			vLinkUrl = "";
		
			vLinkType = "";
			
				
			if(link != null){		 
				 if(link != null){
						vLinkUrl = link.optString(SELF_KEY);
						vLinkHrefUrl = link.optString(HREF_LINK_KEY);
						
						vLinkType = link.optString(TYPE_KEY);

						if( (vLinkUrl.trim().length() > 0 || vLinkHrefUrl.trim().length() > 0) &&   vLinkType.trim().length() >0) {
							dbUtil.setHeader(vLinkHrefUrl,vLinkUrl,vLinkType, false );
							
							if(vLinkType.equalsIgnoreCase(Constants.ACCEPT_TYPE_SECTION_JSON) || vLinkType.equalsIgnoreCase( Constants.ACCEPT_TYPE_GROUPING_OLYMPICS )  ) {
								//Modified as per the href link element
									dbUtil.setLinkData(vLinkUrl,vLinkHrefUrl);
							}
						}
					} 
				
			}

			JSONObject display = content.optJSONObject(DISPLAY_KEY);	
			
			String vDisplayHref = null;
			
			if(display != null){
			 vSubtitle = display.optString(SUBTITLE_KEY);
			 vAccessory = display.optString(ACCESSORY_KEY);
		
			 vDisplayUrl = display.optString(SELF_KEY);
			 vDisplayHref = display.optString(HREF_LINK_KEY);
		
			 vDisplayType = display.getString(TYPE_KEY);

			if ( vDisplayType != null && ( vDisplayType.equalsIgnoreCase( Types.TILE_TIMESTAMP ) || 
					vDisplayType.equalsIgnoreCase( Types.TILE_SOLID ) || 
					vDisplayType.equalsIgnoreCase( Types.TILE_PHOTO ) || 
					vDisplayType.equalsIgnoreCase( Types.TILE_HEADLINE ) ||  
					vDisplayType.equalsIgnoreCase( Types.TILE_VIDEO ) || 
					vDisplayType.equalsIgnoreCase( Types.FEATURE_HIGHLIGHT ) || 
					vDisplayType.equalsIgnoreCase( Types.FEATURE_TIMESTAMP ) || 
					vDisplayType.equalsIgnoreCase( Types.FEATURE_PHOTO ) || 
					vDisplayType.equalsIgnoreCase( Types.FEATURE_VIDEO_TIMESTAMP ) || 
					vDisplayType.equalsIgnoreCase( Types.FEATURE_IMAGE ) || 
					vDisplayType.equalsIgnoreCase( Types.FEATURE_VIDEO ) || 
					vDisplayType.equalsIgnoreCase( Types.STACKED_TYPE )
					|| vDisplayType.equalsIgnoreCase( Types.STACKED_TIMESTAMP_TYPE )
					|| vDisplayType.equalsIgnoreCase( Types.STACKED_VIDEO_TYPE )
					|| vDisplayType.equalsIgnoreCase( Types.STACKED_IMAGE_TYPE )) ) {
			
				
			if(!((vDisplayUrl == null || vDisplayUrl.trim().length() == 0) && (vDisplayHref == null || vDisplayHref.trim().length() == 0)))
				dbUtil.setHeader(vDisplayHref,vDisplayUrl, vDisplayType, false);
			
			 vTitle = display.optString(TITLE_KEY);
			
			 vSummary = display.optString(SUMMARY_KEY);
			 vFooterTitle = display.optString(FOOTER_TITLE_KEY);
			 vFooterSubtitle = display.optString(FOOTER_SUBTITLE_KEY);
			 vTimeStamp = display.optString(TIMESTAMP_KEY);
			
			 vBackgroundColor = display.optString(BACKGROUND_COLOR_KEY);
			 vBackgroundImage = "";vImage = "";vSocialIcon = "";vSourceIcon = "";
			 vSource = display.optString(SOURCE_KEY);
			 iLive = display.optBoolean(LIVE_KEY) ? 1 : 0;
		
			
			
			vBackgroundImage = display.optString(BACKGROUND_IMAGE_KEY);
			if( display.has(IMAGE_KEY) )
				vImage = display.optString(IMAGE_KEY);
			vSourceIcon = display.optString(SOURCE_ICON_KEY);
			vSocialIcon = display.optString(SOCIAL_ICON_KEY);
			
			
			JSONArray backgroundImage_jArr = display.optJSONArray(BACKGROUND_IMAGE_KEY);
			if(backgroundImage_jArr != null ){
				
			for (int k = 0; k < backgroundImage_jArr.length(); k++) {

				JSONObject logo_jObj = backgroundImage_jArr.getJSONObject(k);
				if (Constants.DENSITY.equalsIgnoreCase(logo_jObj
						.getString(DENSITY_KEY))) {

					vBackgroundImage = logo_jObj.getString(HREF_KEY);
				}

			}
			}
			
			// Fetching background image for block tiles
			// if the there is no image which supports current device density
			// it will take the high resolution image from the provided images
			
			try {
				boolean isUrlFetched = false;
				JSONArray image_jArr = display.optJSONArray(IMAGE_KEY);
				if(image_jArr != null && image_jArr.length()>0 ){
					
					for (int k = 0; k < image_jArr.length(); k++) {
						JSONObject logo_jObj = image_jArr.getJSONObject(k);
						if (Constants.DENSITY.equalsIgnoreCase(logo_jObj.getString(DENSITY_KEY))) {
							vImage = logo_jObj.getString(HREF_KEY);
							isUrlFetched = true;
						}
					}
					
					if( !isUrlFetched ) {
						vImage = image_jArr.getJSONObject(image_jArr.length()-1).optString(HREF_KEY);
					}
				}
				
			} catch (Exception e) {
				Logs.show(e);
			}
			
			
			JSONArray source_jArr = display.optJSONArray(SOURCE_ICON_KEY);
			if(source_jArr != null ){

			for (int k = 0; k < source_jArr.length(); k++) {

				JSONObject logo_jObj = source_jArr.getJSONObject(k);
				if (Constants.DENSITY.equalsIgnoreCase(logo_jObj
						.getString(DENSITY_KEY))) {

					vSourceIcon = logo_jObj.getString(HREF_KEY);
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
				}

			}
			}
			
			JSONObject highlight = display.optJSONObject(HIGHLIGHT_KEY);
			 vHighlightUrl = "";
			 vHighlightType = "";
			 vHighLightId = "";
			 vHighlightHrefUrl= "";
			if(highlight != null){
				vHighLightId = highlight.getString(UID_KEY);
				vHighlightType = highlight.getString(TYPE_KEY);
				vHighlightUrl = highlight.optString(SELF_KEY);
				vHighlightHrefUrl = highlight.optString(HREF_LINK_KEY);
				
				if(vHighlightHrefUrl!=null && vHighlightHrefUrl.trim().length()> 0){
					

					new Util().getContestsDataForSections(vHighlightHrefUrl,true);

				}else{
					new Util().getContestsDataForSections(vHighlightUrl,false);
				}
				
			}
			
			if(vContentType.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){
				
				JsonUtil json = new JsonUtil();										
				json.parse( new StringBuffer( content.toString() ) , Constants.TYPE_LEAGUE_ITEM, true );
				
				
			}
			
			
		
		
			dbUtil.setContetData(vBlockContentId,vContentId,vContentType,vContentUrl,vContentHrefUrl,vName,vDisplayUrl,vDisplayHref,vDisplayType,vLinkUrl,vLinkType,
					vImage,vFooterTitle,vFooterSubtitle,iLive,vBackgroundColor,vBackgroundImage,vSummary,vTitle,vSource,vSourceIcon,
					vSocialIcon,vTimeStamp,vHighlightUrl,vHighlightHrefUrl,vHighlightType,vHighLightId,iOrderId,vAccessory, vSubtitle,vLinkHrefUrl);

			}
		}
			
	
		

	
		
	
	}catch(Exception e){
		
		Logs.show(e);
	}finally{
		
		new Util().releaseMemory(content);
		if ( !inTransaction ) {
			dbUtil.getWritabeDatabase().setTransactionSuccessful();
			dbUtil.getWritabeDatabase().endTransaction();
			
			
		}
	}
}
}
