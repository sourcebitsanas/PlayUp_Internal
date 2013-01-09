package com.playup.android.adapters;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.text.Layout;
import android.util.Log;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.activity.VideoActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.connection.HttpRequest;
import com.playup.android.exception.RequestRepeatException;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.ImageDownloaderSports;

import com.playup.android.util.Types;
import com.playup.android.util.Util;


public class EuroTilesGridGenerator  implements OnTouchListener{

	private String titleName = null;
	private Hashtable<String, List<String>> tilesData;
	private LayoutInflater inflater;
	private DateUtil dateUtil = new DateUtil();
	private ImageDownloader imageDownloader = new ImageDownloader();
	DatabaseUtil dbUtil;

	private LayoutParams params, params1;

	private ImageDownloaderSports imageDownloaderSports;



	private RelativeLayout imageWithSummary;
	//		public ImageView imageWithSummary;

	private TextView imageSummary;
	private ImageView playerIcon;
	private LinearLayout footerContent;
	private TextView footerTitle;
	private TextView footerSubtitle;
	private ImageView sourceIcon;
	private ImageView socialIcon;
	private View solidImageOverlay;



	private TextView tileName;
	private TextView liveImage;
	private RelativeLayout euro_tile_bg;

	private LinearLayout tilesBase;
	private String fromFragment;
	private String vMainColor = null;
	private String vMainTitleColor = null;
	private String vSecColor = null;
	private String vSecTitleColor = null;
	private String vDisplayType = null;
	private boolean pausePlay	=	false;
	private Hashtable<String , List<String>> currentlyPlaying =null;
	Hashtable <String , List<String>> stationList	=	null;



	public EuroTilesGridGenerator(Hashtable<String, List<String>> data,LinearLayout tilesBase,
			String vMainColor,String vMainTitleColor, String vSecColor, String vSecTitleColor) {
		tilesData = data;
		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		
		this.vMainColor = vMainColor;
		this.vMainTitleColor  = vMainTitleColor;

		if ( PlayUpActivity.context != null ) {
			inflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		imageDownloader = new ImageDownloader();
		imageDownloaderSports = new ImageDownloaderSports();
		dbUtil = DatabaseUtil.getInstance();

		dateUtil = new DateUtil();
		
		 

		this.tilesBase = tilesBase;

		if ( params == null ) {
			params = new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1f);
			params.setMargins(4, 4, 4, 4);
		}



		if ( params1 == null ) {
			params1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);				
			params1.gravity =Gravity.CENTER; 
			params1.setMargins(0, 10, 0, 0);
		}



		showTiles();

	}




	public int getCount() {
		if(tilesData != null && tilesData.get("vContentId") != null ){			

			return tilesData.get("vContentId").size() ;
		}

		else 
			return 0;
	}




	public View getView(int position) {

		if( tilesData != null && tilesData.get("vDisplayType")!= null  ){
			vDisplayType = tilesData.get("vDisplayType").get( position );
			
		}
		else
			vDisplayType = null;

		
		if( vDisplayType == null || !isSupportedDisplayType(  ) ) {
			
			RelativeLayout lin =  ( RelativeLayout ) inflater.inflate(R.layout.update_tile, null);
			lin.setLayoutParams(params);
			return lin;
		}
		
		
		RelativeLayout lin =  ( RelativeLayout ) inflater.inflate(R.layout.euro_tiles, null);
		lin.setLayoutParams(params);

		euro_tile_bg 		= (RelativeLayout)lin.findViewById(R.id.euro_tile_bg );			

		playerIcon 			= (ImageView)lin.findViewById(R.id.playerIcon);	
		imageWithSummary 	= (RelativeLayout) lin.findViewById(R.id.ImageWithSummary);		

		imageSummary 		= (TextView) lin.findViewById(R.id.ImageSummary);				
		tileName 			= (TextView)	lin.findViewById(R.id.tileName);	
		footerContent 		= (LinearLayout) lin.findViewById(R.id.footerContent);				
		footerTitle 		= (TextView) lin.findViewById(R.id.footerTitle);				
		footerSubtitle 		= (TextView) lin.findViewById(R.id.footerSubtitle);				
		sourceIcon 			= (ImageView) lin.findViewById(R.id.sourceIcon);				
		socialIcon 			= (ImageView) lin.findViewById(R.id.socialIcon);		
		solidImageOverlay 	= (View) lin.findViewById(R.id.solidImageOverlay);

		liveImage 			= (TextView)	lin.findViewById(R.id.liveImage);
		
		tileName.setTextColor(Color.parseColor("#404040"));
		tileName.setTypeface(Constants.OPEN_SANS_BOLD);

		lin.setId(position );
		lin.setOnTouchListener(null);


		// setting title and footer 
		footerContent.setVisibility(View.GONE);

		footerTitle.setTextColor(Color.parseColor("#FFFFFF"));
		footerSubtitle.setTextColor(Color.parseColor("#FFFFFF"));
		imageSummary.setTextColor(Color.parseColor("#FFFFFF"));
		footerTitle.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		footerSubtitle.setTypeface(Constants.OPEN_SANS_REGULAR);
		imageSummary.setTypeface(Constants.OPEN_SANS_REGULAR);

		imageSummary.setShadowLayer(0, 0, 0, 0);
		footerTitle.setShadowLayer(0, 0, 0, 0);
		footerSubtitle.setShadowLayer(0, 0, 0, 0);
		
		
		
		if(vDisplayType.equalsIgnoreCase(Types.TILE_TIMESTAMP)){
			

			footerContent.setVisibility(View.VISIBLE);
			
			if(tilesData.get("vSource").get(position) != null && tilesData.get("vSource").get(position).trim().length() > 0){
				
				footerTitle.setVisibility(View.VISIBLE);
				footerTitle.setText(tilesData.get("vSource").get(position));
			}
			
			if(tilesData.get("vTimeStamp").get(position) != null && tilesData.get("vTimeStamp").get(position).trim().length() > 0){

				
				footerSubtitle.setVisibility(View.VISIBLE);
				footerSubtitle.setText(dateUtil.gmt_to_local_timezone_tiles(tilesData.get("vTimeStamp").get(position)));

			}
			
			if(tilesData.get("vSourceIcon").get(position) != null && tilesData.get("vSourceIcon").get(position).trim().length() > 0 ) {
				
				sourceIcon.setVisibility(View.VISIBLE);
				imageDownloader.download( tilesData.get("vSourceIcon").get(position) , sourceIcon, false,null);
			}
			
			

			if(tilesData.get("vSocialIcon").get(position) != null && tilesData.get("vSocialIcon").get(position).trim().length() > 0){
				
				socialIcon.setVisibility(View.VISIBLE);
				imageDownloader.download( tilesData.get("vSocialIcon").get(position) ,socialIcon, false,null );
			}
			
			
			
			if(tilesData.get("vTitle").get(position) != null && tilesData.get("vTitle").get(position).trim().length() > 0){
				
				lin.findViewById(R.id.titleLayout).setVisibility(View.VISIBLE);
				tileName.setVisibility(View.VISIBLE);
				
				titleName = tilesData.get("vTitle").get(position).toUpperCase();

				if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
					if(titleName.length() > 25 ) {
						titleName = titleName.substring(0,22) +"...";
					}
				}
				
				tileName.setText( titleName );
			}
			
			if(tilesData.get("vSummary").get(position) != null && 
					tilesData.get("vSummary").get(position).trim().length() > 0){
				imageSummary.setVisibility(View.VISIBLE);
				imageSummary.setText(tilesData.get("vSummary").get(position));		
			}
			
			setImageAndBackgroundColor(position,lin);
			
			
			
			if(tilesData.get("vContentType").get(position) != null && 	tilesData.get("vContentType").get(position).equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){
				
				if(tilesData.get("vContentHrefUrl").get(position)!=null && tilesData.get("vContentHrefUrl").get(position).trim().length()>0){
					// Must implement the encoding logic here....
					lin.setTag(R.id.avtarImage2,true);
					lin.setTag(R.id.about_txtview,tilesData.get("vContentHrefUrl").get(position));
				}
				else
				{
					lin.setTag(R.id.avtarImage2,false);
					lin.setTag(R.id.about_txtview,tilesData.get("vContentUrl").get(position));
				}
				lin.setTag(R.id.active_users_text,tilesData.get("vContentType").get(position));
				lin.setTag(R.id.aboutText,tilesData.get("vContentId").get(position));
				lin.setOnTouchListener(this);

			}	
			else if((tilesData.get("vLinkUrl").get(position) != null && tilesData.get("vLinkUrl").get(position).trim().length() > 0) ||
					(tilesData.get("vLinkHrefUrl").get(position) != null && tilesData.get("vLinkHrefUrl").get(position).trim().length() > 0) ){
				
				if(tilesData.get("vLinkHrefUrl").get(position) != null && tilesData.get("vLinkHrefUrl").get(position).trim().length() > 0) {
					// Must implement the encoding logic here....
					lin.setTag(R.id.avtarImage2,true);
					lin.setTag(R.id.about_txtview,tilesData.get("vLinkHrefUrl").get(position) );
				}
				else
				{
					lin.setTag(R.id.avtarImage2,false);
					lin.setTag(R.id.about_txtview,tilesData.get("vLinkUrl").get(position));
				}
				lin.setTag(R.id.active_users_text,tilesData.get("vLinkType").get(position));
				lin.setOnTouchListener(this);
			}
			
		
			return lin;

			
		}else if(vDisplayType.equalsIgnoreCase(Types.TILE_HEADLINE)){
			socialIcon.setVisibility(View.GONE);
			sourceIcon.setVisibility(View.GONE);
			
			if(tilesData.get("vFooterTitle").get(position) != null && tilesData.get("vFooterTitle").get(position).trim().length() > 0){
				footerContent.setVisibility(View.VISIBLE);
				footerTitle.setVisibility(View.VISIBLE);
				footerTitle.setText(tilesData.get("vFooterTitle").get(position));
			}
			
			if(tilesData.get("vFooterSubTitle").get(position) != null && tilesData.get("vFooterSubTitle").get(position).trim().length() > 0){

				footerContent.setVisibility(View.VISIBLE);
				footerSubtitle.setVisibility(View.VISIBLE);
				footerSubtitle.setText(tilesData.get("vFooterSubTitle").get(position));

			}
			
			if(tilesData.get("vTitle").get(position) != null && tilesData.get("vTitle").get(position).trim().length() > 0){
				
				lin.findViewById(R.id.titleLayout).setVisibility(View.VISIBLE);
				tileName.setVisibility(View.VISIBLE);
				
				titleName = tilesData.get("vTitle").get(position).toUpperCase();

				if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
					if(titleName.length() > 25 ) {
						titleName = titleName.substring(0,22) +"...";
					}
				}
				tileName.setVisibility(View.VISIBLE);
				tileName.setText( titleName );
			}
			
			setFooterStyles( false);
			
			setImageAndBackgroundColor(position,lin);
			
			
			if(tilesData.get("vContentType").get(position) != null && 	tilesData.get("vContentType").get(position).equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){
			 
						 if(tilesData.get("vContentHrefUrl").get(position)!=null && tilesData.get("vContentHrefUrl").get(position).trim().length()>0){
							// Must implement the encoding logic here....
							 lin.setTag(R.id.avtarImage2,true);
							 lin.setTag(R.id.about_txtview,tilesData.get("vContentHrefUrl").get(position));
						 }
						 else{
							 lin.setTag(R.id.avtarImage2,false);
							 lin.setTag(R.id.about_txtview,tilesData.get("vContentUrl").get(position));
						 }
							
							lin.setTag(R.id.active_users_text,tilesData.get("vContentType").get(position));
							lin.setTag(R.id.aboutText,tilesData.get("vContentId").get(position));
							lin.setOnTouchListener(this);

			}
			else if((tilesData.get("vLinkUrl").get(position) != null && tilesData.get("vLinkUrl").get(position).trim().length() > 0)||(tilesData.get("vLinkHrefUrl").get(position)!=null && tilesData.get("vLinkHrefUrl").get(position).length()>0)){
						
						if((tilesData.get("vLinkHrefUrl").get(position)!=null) && (tilesData.get("vLinkHrefUrl").get(position).trim().length()>0)){
							// Must implement the encoding logic here....
							lin.setTag(R.id.avtarImage2,true);
							lin.setTag(R.id.about_txtview, (tilesData.get("vLinkHrefUrl").get(position)));
						}else{
							lin.setTag(R.id.avtarImage2,false);
							lin.setTag(R.id.about_txtview,tilesData.get("vLinkUrl").get(position));
						}
						
						lin.setTag(R.id.active_users_text,tilesData.get("vLinkType").get(position));
						lin.setOnTouchListener(this);
			}
			
			return lin;
			
			
		}else if(vDisplayType.equalsIgnoreCase(Types.TILE_PHOTO)){
			
			socialIcon.setVisibility(View.GONE);
			sourceIcon.setVisibility(View.GONE);
			
			
			if(tilesData.get("vFooterTitle").get(position) != null && tilesData.get("vFooterTitle").get(position).trim().length() > 0){
				footerContent.setVisibility(View.VISIBLE);
				footerTitle.setVisibility(View.VISIBLE);
				footerTitle.setText(tilesData.get("vFooterTitle").get(position));
			}
			
			if(tilesData.get("vFooterSubTitle").get(position) != null && tilesData.get("vFooterSubTitle").get(position).trim().length() > 0){

				footerContent.setVisibility(View.VISIBLE);
				footerSubtitle.setVisibility(View.VISIBLE);
				footerSubtitle.setText(tilesData.get("vFooterSubTitle").get(position));

			}
			
			if(tilesData.get("vTitle").get(position) != null && tilesData.get("vTitle").get(position).trim().length() > 0){
				
				lin.findViewById(R.id.titleLayout).setVisibility(View.VISIBLE);
				tileName.setVisibility(View.VISIBLE);
				
				titleName = tilesData.get("vTitle").get(position).toUpperCase();

				if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
					if(titleName.length() > 25 ) {
						titleName = titleName.substring(0,22) +"...";
					}
				}
				tileName.setVisibility(View.VISIBLE);
				tileName.setText( titleName );
			}
			
			
			if(tilesData.get("iLive").get(position) != null && Integer.parseInt(tilesData.get("iLive").get(position)) == 1)
				liveImage.setVisibility(View.VISIBLE);
			
			setFooterStyles( false );
			
			setImageAndBackgroundColor(position,lin);
			
	
			
			 if(tilesData.get("vContentType").get(position) != null && 	tilesData.get("vContentType").get(position).equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){

				 	if(tilesData.get("vContentHrefUrl").get(position)!=null && tilesData.get("vContentHrefUrl").get(position).length()>0){
				 	// Must implement the encoding logic here....
				 		lin.setTag(R.id.avtarImage2,true);
				 		lin.setTag(R.id.about_txtview,tilesData.get("vContentHrefUrl").get(position));
				 	}else{
				 		lin.setTag(R.id.avtarImage2,false);
				 		lin.setTag(R.id.about_txtview,tilesData.get("vContentUrl").get(position));
				 	}
					
					lin.setTag(R.id.active_users_text,tilesData.get("vContentType").get(position));
					lin.setTag(R.id.aboutText,tilesData.get("vContentId").get(position));
					lin.setOnTouchListener(this);

				}else if((tilesData.get("vLinkUrl").get(position) != null && tilesData.get("vLinkUrl").get(position).trim().length() > 0)||((tilesData.get("vLinkHrefUrl").get(position)!=null) && (tilesData.get("vLinkHrefUrl").get(position).length()>0))){

					if((tilesData.get("vLinkHrefUrl").get(position)!=null && (tilesData.get("vLinkHrefUrl").get(position).length()>0))){
						// Must implement the encoding logic here....
						lin.setTag(R.id.avtarImage2,true);
						lin.setTag(R.id.about_txtview,(tilesData.get("vLinkHrefUrl").get(position)));
					}
					else{
						lin.setTag(R.id.avtarImage2,false);
						lin.setTag(R.id.about_txtview,tilesData.get("vLinkUrl").get(position));
					}
					
					lin.setTag(R.id.active_users_text,tilesData.get("vLinkType").get(position));
					lin.setOnTouchListener(this);
				}
				
			
			return lin;
		}else if(vDisplayType.equalsIgnoreCase(Types.TILE_SOLID)){
			
			
			socialIcon.setVisibility(View.GONE);
			sourceIcon.setVisibility(View.GONE);
			
			
			if(tilesData.get("vFooterTitle").get(position) != null && tilesData.get("vFooterTitle").get(position).trim().length() > 0){
				footerContent.setVisibility(View.VISIBLE);
				footerTitle.setVisibility(View.VISIBLE);
				footerTitle.setText(tilesData.get("vFooterTitle").get(position));
			}
			
			if(tilesData.get("vFooterSubTitle").get(position) != null && tilesData.get("vFooterSubTitle").get(position).trim().length() > 0){

				footerContent.setVisibility(View.VISIBLE);
				footerSubtitle.setVisibility(View.VISIBLE);
				footerSubtitle.setText(tilesData.get("vFooterSubTitle").get(position));

			}
			
			if(tilesData.get("vTitle").get(position) != null && tilesData.get("vTitle").get(position).trim().length() > 0){
				
				lin.findViewById(R.id.titleLayout).setVisibility(View.VISIBLE);
				tileName.setVisibility(View.VISIBLE);
				
				titleName = tilesData.get("vTitle").get(position).toUpperCase();

				if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
					if(titleName.length() > 25 ) {
						titleName = titleName.substring(0,22) +"...";
					}
				}
				tileName.setVisibility(View.VISIBLE);
				tileName.setText( titleName );
			}

			
			
			if(tilesData.get("iLive").get(position) != null && Integer.parseInt(tilesData.get("iLive").get(position)) == 1)
				liveImage.setVisibility(View.VISIBLE);
			
			
			setFooterStyles( false );
			
			setImageAndBackgroundColor(position,lin);
			
			
			if(tilesData.get("vContentType").get(position) != null && tilesData.get("vContentType").get(position).equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){
					
					if(tilesData.get("vContentHrefUrl").get(position)!=null && tilesData.get("vContentHrefUrl").get(position).trim().length()>0){
						
						// Must implement the encoding logic here....
						//Log.e("123","Praveeen here....");
						lin.setTag(R.id.avtarImage2,true);
						lin.setTag(R.id.about_txtview,tilesData.get("vContentHrefUrl").get(position));
					}
					else{
						lin.setTag(R.id.avtarImage2,false);
						lin.setTag(R.id.about_txtview,tilesData.get("vContentUrl").get(position));
					}
					lin.setTag(R.id.active_users_text,tilesData.get("vContentType").get(position));
					lin.setTag(R.id.aboutText,tilesData.get("vContentId").get(position));
					lin.setOnTouchListener(this);

			}else if((tilesData.get("vLinkUrl").get(position) != null && tilesData.get("vLinkUrl").get(position).trim().length() > 0)||
					(tilesData.get("vLinkHrefUrl").get(position) !=null &&tilesData.get("vLinkHrefUrl").get(position) .length()>0 )){

					
					if((tilesData.get("vLinkHrefUrl").get(position) !=null) && (tilesData.get("vLinkHrefUrl").get(position).trim().length()>0)){
						// Must implement the encoding logic here....
						//Log.e("123","Praveeen here....");
						lin.setTag(R.id.avtarImage2,true);
						lin.setTag(R.id.about_txtview,tilesData.get("vLinkHrefUrl").get(position));
						
					}else if (tilesData.get("vLinkUrl").get(position) != null && tilesData.get("vLinkUrl").get(position).trim().length() > 0) {
						lin.setTag(R.id.avtarImage2,false);
						lin.setTag(R.id.about_txtview,tilesData.get("vLinkUrl").get(position));
						
					}
					lin.setTag(R.id.active_users_text,tilesData.get("vLinkType").get(position));
					lin.setOnTouchListener(this);
					
					
				}
			return lin;
			
		}else if(vDisplayType.equalsIgnoreCase(Types.TILE_VIDEO)){
			
			footerContent.setVisibility(View.GONE);
			
			playerIcon.setVisibility(View.VISIBLE);
			
			if(tilesData!= null && tilesData.get("vTitle").get(position) != null && tilesData.get("vTitle").get(position).trim().length() > 0){
				
				lin.findViewById(R.id.titleLayout).setVisibility(View.VISIBLE);
				tileName.setVisibility(View.VISIBLE);
				
				titleName = tilesData.get("vTitle").get(position).toUpperCase();

				if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
					if(titleName.length() > 25 ) {
						titleName = titleName.substring(0,22) +"...";
					}
				}
				tileName.setVisibility(View.VISIBLE);
				tileName.setText( titleName );
			}
			
			setImageAndBackgroundColor(position,lin);
			
			
		
			
			if(tilesData!=null && tilesData.get("vContentType").get(position) != null && 	tilesData.get("vContentType").get(position).equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){
				if(tilesData.get("vContentHrefUrl").get(position)!=null && tilesData.get("vContentHrefUrl").get(position).trim().length()>0){
					// Must implement the encoding logic here....
					lin.setTag(R.id.avtarImage2,true);
					lin.setTag(R.id.about_txtview,tilesData.get("vContentHrefUrl").get(position));
				}
				else{
					lin.setTag(R.id.avtarImage2,false);
					lin.setTag(R.id.about_txtview,tilesData.get("vContentUrl").get(position));
				}
				
				lin.setTag(R.id.active_users_text,tilesData.get("vContentType").get(position));
				lin.setTag(R.id.aboutText,tilesData.get("vContentId").get(position));
				lin.setOnTouchListener(this);

			}
			else if(tilesData!=null && (tilesData.get("vLinkUrl").get(position) != null && tilesData.get("vLinkUrl").get(position).trim().length() > 0) ||
					((tilesData.get("vLinkHrefUrl").get(position) != null && tilesData.get("vLinkHrefUrl").get(position).trim().length() > 0))){
				

				if(tilesData!=null && (tilesData.get("vLinkHrefUrl").get(position) != null && tilesData.get("vLinkHrefUrl").get(position).trim().length() > 0)){
					// Must implement the encoding logic here....
					lin.setTag(R.id.avtarImage2,true);
					lin.setTag(R.id.about_txtview,(tilesData.get("vLinkHrefUrl").get(position)));
				}
				else{
					lin.setTag(R.id.avtarImage2,false);
					lin.setTag(R.id.about_txtview,tilesData.get("vLinkUrl").get(position));
				}
				
				lin.setTag(R.id.active_users_text,tilesData.get("vLinkType").get(position));
				lin.setOnTouchListener(this);
			} 
			
			return lin;
			
		}
		
		else if(vDisplayType.equalsIgnoreCase(Types.TILE_AUDIO_LIST) || vDisplayType.equalsIgnoreCase(Types.TILE_AUDIO)){
			
			
			

			

			footerContent.setVisibility(View.VISIBLE);
			playerIcon.setVisibility(View.VISIBLE);
			playerIcon.setImageResource(R.drawable.pause_icon);
			
			
			sourceIcon.setVisibility(View.VISIBLE);
			sourceIcon.setImageResource(R.drawable.mic_png);

			if(vDisplayType.equalsIgnoreCase(Types.TILE_AUDIO_LIST)){
				if(tilesData.get("vDisplayCount") != null && tilesData.get("vDisplayCount").get(position).trim().length() > 0){
				
					footerSubtitle.setVisibility(View.VISIBLE);
					footerSubtitle.setText(tilesData.get("vDisplayCount").get(position));
				}
			}else if(vDisplayType.equalsIgnoreCase(Types.TILE_AUDIO)){
				
					
					footerSubtitle.setVisibility(View.GONE);
					
				
			}

			
			if(tilesData.get("vTitle").get(position) != null && tilesData.get("vTitle").get(position).trim().length() > 0){
				
				lin.findViewById(R.id.titleLayout).setVisibility(View.VISIBLE);
				tileName.setVisibility(View.VISIBLE);
				
				titleName = tilesData.get("vTitle").get(position).toUpperCase();

				if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
					if(titleName.length() > 25 ) {
						titleName = titleName.substring(0,22) +"...";
					}
				}
				
				tileName.setText( titleName );
			}else if(tilesData.get("vContentTitle").get(position) != null && tilesData.get("vContentTitle").get(position).trim().length() > 0){
				lin.findViewById(R.id.titleLayout).setVisibility(View.VISIBLE);
				tileName.setVisibility(View.VISIBLE);
				
				titleName = tilesData.get("vContentTitle").get(position).toUpperCase();

				if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
					if(titleName.length() > 25 ) {
						titleName = titleName.substring(0,22) +"...";
					}
				}
				
				tileName.setText( titleName );
			}
			

			
			setImageAndBackgroundColor(position,lin);
			
			
				if(vDisplayType.equalsIgnoreCase(Types.TILE_AUDIO_LIST)){
					
					lin.setTag(R.id.aboutText,tilesData.get("vContentId").get(position));
					lin.setTag(R.id.about_txtview,"dummyUrl");
					
//					if(tilesData.get("vLinkHrefUrl").get(position) != null && tilesData.get("vLinkHrefUrl").get(position).trim().length() > 0){
//						
//						lin.setTag(R.id.about_txtview,tilesData.get("vLinkHrefUrl").get(position));
//						
//					}else if(tilesData.get("vLinkUrl").get(position) != null && tilesData.get("vLinkUrl").get(position).trim().length() > 0){
//						
//						lin.setTag(R.id.about_txtview,tilesData.get("vLinkUrl").get(position));
//						
//					}
							
					
					lin.setTag(R.id.active_users_text,tilesData.get("vContentType").get(position));
					lin.setOnTouchListener(this);
					
				}else if(vDisplayType.equalsIgnoreCase(Types.TILE_AUDIO)){
					
					if((tilesData.get("vLinkUrl").get(position) != null && 
							tilesData.get("vLinkUrl").get(position).trim().length() > 0) ||
							(tilesData.get("vLinkHrefUrl").get(position) != null && 
									tilesData.get("vLinkHrefUrl").get(position).trim().length() > 0)){
						
						
						lin.setTag(R.id.aboutText,tilesData.get("vContentId").get(position));
						
						if(tilesData.get("vLinkHrefUrl").get(position) != null && 
									tilesData.get("vLinkHrefUrl").get(position).trim().length() > 0)
							lin.setTag(R.id.about_txtview,tilesData.get("vLinkHrefUrl").get(position));
						else
							lin.setTag(R.id.about_txtview,tilesData.get("vLinkUrl").get(position));
						
						lin.setTag(R.id.active_users_text,tilesData.get("vContentType").get(position));
						lin.setOnTouchListener(this);
						
					}
					
					
					
				}
			
			// if this does not work then getRadioStationData and get RadioId from that and match with currentlyPlaying.
				currentlyPlaying	=	dbUtil.getRadioStationsDataToPass(tilesData.get("vContentId").get(position));
				Log.e("123", "currentlyPlaying---------------"+currentlyPlaying);
			
//			if(tilesData.get("vContentId").get(position) != null && tilesData.get("vContentId").get(position).length() > 0 ){
//			
//				stationList	=	dbUtil.getRadioStaionsData(tilesData.get("vContentId").get(position));
//				if(stationList	!=	null &&stationList.containsKey("vRadioId")	&& stationList.get("vRadioId").size() >	0 ){
//					
//				}
//				currentlyPlaying	=	dbUtil.getRadioStationsDataToPass(tilesData.get("vContentId").get(position));
//			
//			}
//			
//			if(currentlyPlaying !=null && currentlyPlaying.containsKey("vRadioId") && currentlyPlaying.get("vRadioId").toString().length() > 0 ){
//				
//				playerIcon.setImageResource(R.drawable.round_play);
//				
//			}else{
//				playerIcon.setImageResource(R.drawable.pause_icon);
//			}
		
//			Hashtable<String , List<String>> currentlyPlaying	=	dbUtil.getCurrentRadio();
//			String vRadioUrl	=	null;
//			if(currentlyPlaying != null ){
//				if(currentlyPlaying.get("vRadioStationUrl").size()>0 && currentlyPlaying.get("vRadioStationUrl").toString().length() > 0) {
//					
//					vRadioUrl	=	currentlyPlaying.get("vRadioStationUrl").toString();
//					if(tilesData.get("vRadioStationUrl").contains(vRadioUrl)){
//						playerIcon.setImageResource(R.drawable.round_play);
//					}
//					
//				}else if(currentlyPlaying.get("vRadioSationHrefUrl").size()>0 && currentlyPlaying.get("vRadioSationHrefUrl").toString().length() > 0){
//					
//					vRadioUrl	=	currentlyPlaying.get("vRadioSationHrefUrl").toString();
//					if(tilesData.get("vRadioSationHrefUrl").contains(vRadioUrl)){
//						playerIcon.setImageResource(R.drawable.round_play);
//					}
//					
//				}else {
//					playerIcon.setImageResource(R.drawable.pause_icon);
//				}
//				
//			}else {
//				playerIcon.setImageResource(R.drawable.pause_icon);
//			}
		
				lin.setOnTouchListener(this);
			return lin;
			
		}


		
		return lin;

		
	}


	float rawX = 0;
	float rawY = 0;
	long downTime = 0;

	@Override
	public boolean onTouch(final View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {

			downTime = event.getDownTime();
			rawX = event.getRawX();
			rawY = event.getRawY();
			// highlight blue color

			startUpdating(v);
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {

			stopUpdating();
			if (event.getRawY() == rawY
					|| (event.getEventTime() - downTime) < 200) {

				// highLightBlueColor(v, true);



			}
			if(v.getTag(R.id.about_txtview) != null && v.getTag(R.id.about_txtview).toString().trim().length() > 0 ){

				Bundle b =  new Bundle();
				
				b.putString( "vMainColor",vMainColor );
				
				b.putString( "vMainTitleColor",vMainTitleColor );
				
				b.putString( "vSecColor",vSecColor );
				
				b.putString( "vSecTitleColor",vSecTitleColor );
				
				fromFragment = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
				
				
				String type = v.getTag(R.id.active_users_text).toString();
				Log.e("123" , "Inside onTouch of Euro Tiles Generator-----"+type);

				//Log.e("123", "Type------"+type);
				if( type  != null && type.equalsIgnoreCase(Constants.ACCEPT_TYPE_COVERSATION)){

					b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
					b.putString("vConversationUrl",v.getTag(R.id.about_txtview).toString());
					b.putString("fromFragment",fromFragment);
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchHomeFragment",b);

				}else if( type  != null && type.equalsIgnoreCase(Constants.ACCEPT_TYPE_SPORTS_JSON)){
					b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
					b.putString("fromFragment",fromFragment);
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("AllSportsFragment",b);						

				}else if( type  != null && type.equalsIgnoreCase(Constants.ACCEPT_TYPE_FIXTURES_JSON)){
					b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
					b.putString("fromFragment",fromFragment);
					b.putString("vCurrentSeasonUrl",v.getTag(R.id.about_txtview).toString() );
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("FixturesAndResultsFragment",b);

				}else if( type  != null &&  ( type.equalsIgnoreCase(Constants.ACCEPT_TYPE_SECTION_JSON) || type.equalsIgnoreCase( Constants.ACCEPT_TYPE_GROUPING_OLYMPICS ) ) ) {

					if ( type.equalsIgnoreCase( Constants.ACCEPT_TYPE_GROUPING_OLYMPICS ) ) {

						b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
						b.putString("vConversationUrl",v.getTag(R.id.about_txtview).toString());
						b.putString("fromFragment",fromFragment);
						PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchHomeFragment",b);

					} else {
						b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
						b.putString("fromFragment",fromFragment);
						b.putString("vSectionUrl",v.getTag(R.id.about_txtview).toString());
						PlayupLiveApplication.getFragmentManagerUtil().setFragment("NewsFragment",b);
					}

				}else if( type  != null && type.equalsIgnoreCase(Constants.ACCEPT_TYPE_HTML)){
					b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
					b.putString("url",v.getTag(R.id.about_txtview).toString());
					
					b.putString("fromFragment",fromFragment);
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("WebViewFragment",b);

				}else if( type  != null && (type.equalsIgnoreCase(Constants.ACCEPT_TYPE_VIDEO) || type.equalsIgnoreCase(Constants.ACCEPT_TYPE_AUDIO))){
					
					Intent i = new Intent(PlayUpActivity.context,VideoActivity.class);
					i.putExtra("isHref",(Boolean) v.getTag(R.id.avtarImage2));
					i.putExtra("videoUrl", v.getTag(R.id.about_txtview).toString());
					PlayUpActivity.context.startActivity(i);

				}else if( type  != null && type.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){

					b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
					b.putString("vCompetitionId",v.getTag(R.id.aboutText ).toString());
					b.putString("fromFragment",fromFragment);
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("LeagueLobbyFragment",b);

				} else if (  type  != null && type.equalsIgnoreCase( Constants.ACCEPT_TYPE_MY_CONTEST_LOBBY_JSON ) ) {
					b.putString("fromFragment",fromFragment);
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("PrivateLobbyFragment",b);

				}  else if (  type  != null && type.equalsIgnoreCase( Constants.ACCEPT_TYPE_CONTEST_LOBBY_JSON ) ) {

					
					String vContestLobbyUrl = v.getTag(R.id.about_txtview).toString();
					b.putString("vContestLobbyUrl",vContestLobbyUrl);
					b.putString("fromFragment",fromFragment );
					b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchRoomFragment",b);

				} else if ( type  != null && type.equalsIgnoreCase( Types.TEAM_SCHEDULE_TYPE ) ) { 
					b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
					b.putString( "vTeamScheduleUrl",v.getTag(R.id.about_txtview).toString() );
					b.putString( "fromFragment",fromFragment );
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("TeamScheduleFragment",b);
					
				} else if (  type  != null &&  type.equalsIgnoreCase( Types.LEADERBOARD_TYPE )  && !Constants.isFetchingCredentials) {
					//NEED TO DO HREF RELATED WORK HERE , LIKE ABOVE
					HttpRequest request =null;
					if((Boolean) v.getTag(R.id.avtarImage2)){
						String encodedHrefURL	= null;
						String vTokenValue		= 	null;
						
						int tokenType = new Util().checkForHrefTokenType( v.getTag(R.id.about_txtview).toString());
						vTokenValue		=	new Util().checkForHrefTokenParam( v.getTag(R.id.about_txtview).toString()); 
						encodedHrefURL	=	new Util().getPersonalizedEnocodedURL(v.getTag(R.id.about_txtview).toString(),vTokenValue,tokenType);
						
						 request = new HttpRequest(  v.getTag(R.id.about_txtview).toString(),encodedHrefURL,true, Constants.GET_METHOD );
					}
					else{
						 request = new HttpRequest(  v.getTag(R.id.about_txtview).toString(), Constants.GET_METHOD );
					}
					try {
						StringBuffer strBuffer	=	(StringBuffer) request.send();

						if(request.getStatusCode() == 401){
							Util.callTheCredentialsUrl(strBuffer);
							

						}
					} catch (RequestRepeatException e) {
						//Logs.show ( e );
					}

				} else if ( type  != null &&  type.equalsIgnoreCase( Types.CONTEST_SET_TYPE )  && !Constants.isFetchingCredentials) {
					
					HttpRequest request =null;
					if((Boolean) v.getTag(R.id.avtarImage2)){
						String encodedHrefURL	= null;
						String vTokenValue		= 	null;
						int tokenType = new Util().checkForHrefTokenType( v.getTag(R.id.about_txtview).toString());
						vTokenValue	=	new Util().checkForHrefTokenParam( v.getTag(R.id.about_txtview).toString()); 
						encodedHrefURL	=	new Util().getPersonalizedEnocodedURL(v.getTag(R.id.about_txtview).toString(),vTokenValue,tokenType);
						
						 request = new HttpRequest(  v.getTag(R.id.about_txtview).toString(),encodedHrefURL,true, Constants.GET_METHOD );
					}
					else{
						 request = new HttpRequest(  v.getTag(R.id.about_txtview).toString(), Constants.GET_METHOD );
					}
				
					try {
						StringBuffer strBuffer	=	(StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
							Util.callTheCredentialsUrl(strBuffer);
							

						}
					} catch (RequestRepeatException e) {
						//Logs.show ( e );
					}
				}else if(type != null && type.equalsIgnoreCase(Types.AUDIO_LIST_TYPE) && !Constants.isFetchingCredentials){
					// call POP UP window
					Log.e("123", "on touch of tile radio----");

					
					 if(v.getTag(R.id.aboutText) != null && v.getTag(R.id.aboutText).toString().trim().length() >0){
						 
						 PlayUpActivity.popUp = new RadioListPopUp(v.getTag(R.id.aboutText).toString());
						 PlayUpActivity.popUp.show();
				}
					

				             
					
					
				}else if(type != null && type.equalsIgnoreCase(Types.STATIONS_TYPE) && !Constants.isFetchingCredentials){
					
					Log.e("123", "on touch of tile radio- single---");
					playerIcon 			= (ImageView)v.findViewById(R.id.playerIcon);	
					if(pausePlay){
						playerIcon.setImageResource(R.drawable.pause_icon);
						pausePlay	=	false;
					}else{
						
						playerIcon.setImageResource(R.drawable.round_play);
						pausePlay	=	true;
					}
					
				}
			}
		}

		if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			stopUpdating();
			// disable the blue color

		}

		if (event.getEventTime() > (downTime + Constants.highightDelay)) {
			if (event.getRawY() >= rawY - 10 && event.getRawY() <= rawY + 10) {
				stopUpdating();
			}
		}
		return true;
	}




	/**
	 * 
	 * This is to handle custom selection.
	 */
	Handler mHandler;

	ScheduledExecutorService mUpdater;

	private void startUpdating(final View v) {
		if (mUpdater != null) {
			return;
		}
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				try {
					if (v !=null && v.getId() >= 0 && v.getId() < getCount()) {


					}
					super.handleMessage(msg);
				} catch (Exception e) {
					//Logs.show(e);
				}
			}
		};
		mUpdater = Executors.newSingleThreadScheduledExecutor();
		mUpdater.schedule(new UpdateCounterTask(), 200, TimeUnit.MILLISECONDS);
	}

	private void stopUpdating() {
		if (mUpdater != null && !mUpdater.isShutdown()) {
			mUpdater.shutdownNow();
			mUpdater = null;
		}
	}

	private class UpdateCounterTask implements Runnable {
		public void run() {

			try {
				mHandler.sendEmptyMessage(0);
			} catch (Exception e) {
				//Logs.show ( e );
			}
		}
	}

	private void showTiles(){



		try {


			if ( tilesData != null && tilesData.get("vContentId") != null && tilesData.get("vContentId").size() > 0) {




				tilesBase.removeAllViews();



				final int count = tilesData.get("vContentId").size();
				int len = 0;
				if((count % 2) == 0)
					len = count /2;
				else
					len = (count+1)/2;


				for (int i = 0; i < len; i++) {

					// row linear layout
					LinearLayout tileItemLayout = new LinearLayout( PlayUpActivity.context);
					for (int j = 0; j < 2; j++) {

						if (i * 2 + j < count) {

							
							tileItemLayout.addView(getView(i * 2 + j ));
						} else {

							LinearLayout lin = (LinearLayout) inflater.inflate( R.layout.tagline_view, null);
							lin.setLayoutParams(params);
							tileItemLayout.addView(lin);
							lin = null;			





						}
					}

					// adding row layout on main layout
					tilesBase.addView(tileItemLayout);
					tileItemLayout = null;

				}

			} 
		}catch(Exception e){

			//Logs.show(e);

		}
	}



	private void setImageAndBackgroundColor(int position, RelativeLayout lin ) {
		try {
			if(imageWithSummary.findViewById(R.id.ImageWithSummaryImage)!= null  )
				imageWithSummary.findViewById(R.id.ImageWithSummaryImage).setTag( null );
			
			String bgColor = null, imageUrl = null;

			if(tilesData != null){
			if (tilesData.containsKey("vBackgroundColor") && tilesData.get("vBackgroundColor").get(position) != null
					&& tilesData.get("vBackgroundColor").get(position).trim()
					.length() > 0) {

				bgColor = tilesData.get("vBackgroundColor").get(position);
			}

			if (tilesData.containsKey("vImageUrl") && tilesData.get("vImageUrl").get(position) != null
					&& tilesData.get("vImageUrl").get(position).trim().length() > 0) {

				imageUrl = tilesData.get("vImageUrl").get(position);
			}

			else if (tilesData.containsKey("vBackgroundImage") && tilesData.get("vBackgroundImage").get(position) != null
					&& tilesData.get("vBackgroundImage").get(position).trim()
					.length() > 0) {

				imageUrl = tilesData.get("vBackgroundImage").get(position);
			} else if(tilesData.get("vDisplayType") != null && tilesData.get("vDisplayType").get(position) != null 
					&& (tilesData.get("vDisplayType").get(position).equalsIgnoreCase(Types.TILE_AUDIO_LIST ) || 
							tilesData.get("vDisplayType").get(position).equalsIgnoreCase(Types.TILE_AUDIO ))){
				
				if(tilesData.get("vRadioBackground") != null && tilesData.get("vRadioBackground").get(position) != null
						&& tilesData.get("vRadioBackground").get(position).trim().length() > 0)
							imageUrl = tilesData.get("vRadioBackground").get(position);
				
			}
			try {
				if(bgColor != null && bgColor.trim().length() > 0){
					if (bgColor.substring(0, 2).equalsIgnoreCase("0x")) {
						
							bgColor = bgColor.substring(2);
						
					}		
				
				}
			} catch (Exception e) {
				//Logs.show(e);
				
				bgColor = null;
			}
			
			


			if (bgColor != null && bgColor.trim().length() > 0) {
				
				if (tilesData.containsKey("vDisplayType") && tilesData.get("vDisplayType").get(position) != null
						&& tilesData.get("vDisplayType").get(position)
						.equalsIgnoreCase(Constants.ACCEPT_TYPE_SOLID)) {
					
					// removing centre cropping for solid type image
					ImageView solidImage = (ImageView) imageWithSummary.findViewById(R.id.ImageWithSummaryImage);
					solidImage.setScaleType(ScaleType.FIT_CENTER);
					solidImage.setAdjustViewBounds(false);
					
					if (bgColor.equalsIgnoreCase("FFFFFF")) {
						tileName.setTextColor(Color.parseColor("#404040"));
					} else {
						tileName.setTextColor(Color.parseColor("#FFFFFF"));				
					}


					solidImage.setBackgroundColor(Color.parseColor("#FFFFFF"));
					imageWithSummary.setBackgroundColor(Color.parseColor("#FFFFFF"));
					
					euro_tile_bg.getChildAt( 0 ).setBackgroundColor(Color.parseColor("#"+bgColor));
					if(euro_tile_bg.getChildAt( 0 ).getBackground() != null)
						euro_tile_bg.getChildAt( 0 ).getBackground().setAlpha(220);
					
					solidImageOverlay.setVisibility(View.VISIBLE);
					solidImageOverlay.setBackgroundColor(Color.parseColor("#"+bgColor));
					if(solidImageOverlay.getBackground() != null)
						solidImageOverlay.getBackground().setAlpha(220);


					if (imageUrl != null && imageUrl.trim().length() > 0) {
							imageWithSummary.setTag(R.id.active_users_no,bgColor);
							imageDownloaderSports.download(imageUrl, imageWithSummary,false, bgColor);
					}
				} else if (imageUrl != null && imageUrl.trim().length() > 0) {
					
					imageWithSummary.setBackgroundColor( Color.parseColor("#"+bgColor) );
					
					
					
					
					if(!bgColor.equalsIgnoreCase("ffffff")){
						
						
						
						
						if(imageWithSummary.findViewById(R.id.ImageWithSummaryImage)!= null  )
							imageWithSummary.findViewById(R.id.ImageWithSummaryImage).setTag("show_dark");
						imageWithSummary.setTag(R.id.active_users_no,bgColor);
						imageDownloaderSports.download(imageUrl, imageWithSummary,false, bgColor);	
						imageSummary.setShadowLayer(0, 0, 0, 0);
						footerTitle.setShadowLayer(0, 0, 0, 0);
						footerSubtitle.setShadowLayer(0, 0, 0, 0);
					} else {
						
						
						imageDownloaderSports.download(imageUrl, imageWithSummary,false);					
						imageSummary.setShadowLayer(4, 0, 0, Color.parseColor("#000000"));
						footerTitle.setShadowLayer(4, 0, 0, Color.parseColor("#000000"));
						footerSubtitle.setShadowLayer(4, 0, 0, Color.parseColor("#000000"));
					}
					
					
					
				} else {
					setFooterStyles(true);

						imageWithSummary.setBackgroundColor(Color.parseColor("#"+ bgColor));
				}
			} else {
				imageWithSummary.setBackgroundColor( Color.GRAY );
				if (imageUrl != null && imageUrl.trim().length() > 0) {
					
					imageDownloaderSports.download(imageUrl, imageWithSummary,false);
				}

			}
			
		}
		} catch (Exception e) {
			//Logs.show(e);
		}
	}
	
	


	public void setFooterStyles(boolean isBuzz) {
		if(!isBuzz){
			footerTitle.setPadding(0, 0, 0, -10);
			footerTitle.setTextColor(Color.parseColor("#FFFFFF"));
			footerSubtitle.setTextColor(Color.parseColor("#FFFFFF"));
			footerTitle.setTextSize(40);
			footerSubtitle.setTextSize(18);
			footerTitle.setTypeface(Constants.OPEN_SANS_BOLD);
			footerSubtitle.setTypeface(Constants.OPEN_SANS_LIGHT);
		}else if(vDisplayType != null && vDisplayType.trim().length() > 0 && vDisplayType.equalsIgnoreCase(Types.TILE_PHOTO)){
			
			
		}else{
			imageSummary.setTextColor(Color.parseColor("#404040"));
			footerTitle.setTextColor(Color.parseColor("#404040"));
			footerTitle.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
			footerSubtitle.setTextColor(Color.parseColor("#404040"));
			footerSubtitle.setTypeface(Constants.OPEN_SANS_LIGHT);
		}
	}

	
	private boolean isSupportedDisplayType( ) {
		
		if( vDisplayType.equalsIgnoreCase(Types.TILE_HEADLINE) ||
				vDisplayType.equalsIgnoreCase(Types.TILE_PHOTO) ||
				vDisplayType.equalsIgnoreCase(Types.TILE_SOLID) ||
				vDisplayType.equalsIgnoreCase(Types.TILE_TIMESTAMP) ||
				vDisplayType.equalsIgnoreCase(Types.TILE_VIDEO) ||
				vDisplayType.equalsIgnoreCase(Types.TILE_AUDIO) || 
				vDisplayType.equalsIgnoreCase(Types.TILE_AUDIO_LIST)) {
			return true;
		}
		
			return false;
	}




}
