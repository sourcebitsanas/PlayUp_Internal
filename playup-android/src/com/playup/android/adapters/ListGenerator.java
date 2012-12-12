package com.playup.android.adapters;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils.TruncateAt;


import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class ListGenerator  implements OnTouchListener{

	private Hashtable<String, List<String>> stackData;
	private LayoutInflater inflater;
	DatabaseUtil dbUtil;
	private LinearLayout listBase;
	private String fromFragment;
	private ImageDownloader imageDownloader;
	private DateUtil dateUtil;
	private String vMainColor = null;
	private String vMainTitleColor = null;
	private String vSecColor = null;;
	private String vSecTitleColor = null;;
	
	
	private LayoutParams liParams;
	
	private int newsBaseMargin = 8;
	private boolean isLastBlock = false;
 
//	private Hashtable<String, List<String>> contestData;
	//private String vSectionId = null;


	public ListGenerator( Hashtable<String, List<String>> data,LinearLayout listBase,String vMainColor,
			String vMainTitleColor,String vSecColor,String vSecTitleColor, boolean isLastBlock ) {
	
		this.isLastBlock = isLastBlock;
		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;

		stackData = data;
		this.vMainColor = vMainColor;
		this.vMainTitleColor  = vMainTitleColor;
		
		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;

		if ( PlayUpActivity.context != null ) {
			inflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		dbUtil = DatabaseUtil.getInstance();
		imageDownloader = new ImageDownloader();
		dateUtil = new DateUtil();

		this.listBase = listBase;
		
		if( Constants.DPI > 0 ) {
			newsBaseMargin = (int) ( 8 * ( (float) Constants.DPI/160 )); 
		}
	

		showList();
	


	}

	
	public void setData ( Hashtable<String, List<String>> data  ) {

		stackData = data;
		showList();
	}





	public int getCount() {
		if(stackData != null && stackData.get("vContentId") != null ){			

			return stackData.get("vContentId").size() ;
		}

		else 
			return 0;
	}




	public View getView(int position, String vContentType , String vDisplayType ) {

			View lin = null;



			if( vDisplayType == null || !isSupportedDisplayType(vDisplayType) ) {
//				lin = setUpdateStackView( position);
				return null;
			}
	
			if ( vDisplayType.trim().equalsIgnoreCase( Types.STACKED_TYPE )) {
				lin =  setNormalStackData( position );
			} else if ( vDisplayType.trim().equalsIgnoreCase( Types.STACKED_TIMESTAMP_TYPE ) || 
					vDisplayType.trim().equalsIgnoreCase( Types.STACKED_VIDEO_TYPE ) )  {
				lin = setTimeStampData ( position, vDisplayType );
			} else if( vDisplayType.trim().equalsIgnoreCase( Types.STACKED_IMAGE_TYPE )  ) {
				lin = setPhotoStackTile( position ); 	
			} 


			/*if(stackData.get("vLinkUrl").get(position) != null && stackData.get("vLinkUrl").get(position).trim().length() > 0){

				lin.setTag(R.id.about_txtview,stackData.get("vLinkUrl").get(position));
				lin.setTag(R.id.active_users_text,stackData.get("vLinkType").get(position));
				lin.setTag( vDisplayType );
				lin.setOnTouchListener(this);

			}if(stackData.get("vContentType").get(position) != null && 
					stackData.get("vContentType").get(position).equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){

				lin.setTag(R.id.about_txtview,stackData.get("vContentUrl").get(position));
				lin.setTag(R.id.active_users_text,stackData.get("vContentType").get(position));
				lin.setTag(R.id.aboutText,stackData.get("vContentId").get(position));
				lin.setTag( vDisplayType );
				
				lin.setOnTouchListener(this);

			}*/
		//Praveen : modified based on the href element 

			
			if(stackData.get("vContentType").get(position) != null && 	stackData.get("vContentType").get(position).equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){
				
				if(stackData.get("vContentHrefUrl").get(position)!=null && stackData.get("vContentHrefUrl").get(position).length()>0){
					lin.setTag(R.id.avtarImage2,true);
					lin.setTag(R.id.about_txtview,stackData.get("vContentHrefUrl").get(position));
				}
				else{
					lin.setTag(R.id.avtarImage2,false);
					lin.setTag(R.id.about_txtview,stackData.get("vContentUrl").get(position));
				}
				lin.setTag(R.id.active_users_text,stackData.get("vContentType").get(position));
				lin.setTag(R.id.aboutText,stackData.get("vContentId").get(position));
				lin.setTag( vDisplayType );
				
				lin.setOnTouchListener(this);

			}
			else if((stackData.get("vLinkUrl").get(position) != null && stackData.get("vLinkUrl").get(position).trim().length() > 0)||(stackData.get("vLinkHrefUrl").get(position) !=null && (stackData.get("vLinkHrefUrl").get(position) .length()>0))){
				
				if((stackData.get("vLinkHrefUrl").get(position) )!=null && (stackData.get("vLinkHrefUrl").get(position) .length()>0)){
					lin.setTag(R.id.avtarImage2,true);
					lin.setTag(R.id.about_txtview,stackData.get("vLinkHrefUrl").get(position));
				}
				else{
					lin.setTag(R.id.avtarImage2,false);
					lin.setTag(R.id.about_txtview,stackData.get("vLinkUrl").get(position));
				}
				lin.setTag(R.id.active_users_text,stackData.get("vLinkType").get(position));
				lin.setTag( vDisplayType );
				lin.setOnTouchListener(this);

			}
		

		return lin;
		
	}
	
	
	/**
	 * setting the data for normal stacks
	 * data need to set ( title, subtitle, live icon, vAccessory)
	 * 
	 */
	private View setNormalStackData ( int position ) {
		LinearLayout lin 	=  ( LinearLayout ) inflater.inflate(R.layout.stack_list, null);	
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		lin.setLayoutParams(params);

		lin.setPadding(8, 0, 8, 0);		
		RelativeLayout listBase;
		ImageView bottomShadow;
		TextView liveNumber;
		TextView leagueName;
		TextView leagueSubName;
		TextView liveText;
		LinearLayout middleView;
		ImageView chevron;
		ImageView listDividerFirst;
		 
		 listBase           = (RelativeLayout) lin .findViewById(R.id.listBase);
		 bottomShadow 		= (ImageView) 	lin .findViewById(R.id.bottomShadow);
		
		 liveNumber 		= (TextView) 	lin .findViewById(R.id.liveCount);
		 leagueName 		= (TextView) 	lin .findViewById(R.id.leagueTitle);
		 leagueSubName		= (TextView)    lin .findViewById(R.id.leagueSubTitle);
		 liveText 			= (TextView)	lin .findViewById(R.id.liveText);
		 
		 middleView 	= (LinearLayout) lin.findViewById(R.id.middleView);
		 chevron 			= (ImageView)	lin .findViewById(R.id.chevron);
		 listDividerFirst = (ImageView) lin .findViewById(R.id.listDividerFirst);
		 
		 leagueSubName.setTypeface(Constants.OPEN_SANS_REGULAR);
		 leagueName.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		 liveNumber.setTypeface(Constants.BEBAS_NEUE);
		 
		 if( position == 0 && position == (getCount()-1) )
				listDividerFirst.setVisibility(View.GONE);
			else
				listDividerFirst.setVisibility(View.VISIBLE);

			if( position == ( getCount()-1) && isLastBlock ) {
				bottomShadow.setVisibility( View.VISIBLE );
			} else {
				bottomShadow.setVisibility( View.GONE );
			}
			
			listBase.setBackgroundColor(Color.parseColor("#F7F7F4"));					
			middleView.setVisibility(View.VISIBLE);
			
			

			if( stackData != null && stackData.get("vTitle") != null && stackData.get("vTitle").get(position) != null && stackData.get("vTitle").get(position).trim().length() > 0)
				leagueName.setText(stackData.get("vTitle").get(position));

			if( stackData != null && stackData.get("vSubtitle") != null && stackData.get("vSubtitle").get(position) != null && stackData.get("vSubtitle").get(position).trim().length() > 0){
				leagueSubName.setVisibility( View.VISIBLE );
				leagueSubName.setText(stackData.get("vSubtitle").get(position));
			} else {
				leagueSubName.setVisibility( View.GONE );
			}

			int isLive = Integer.parseInt( stackData.get ( "iLive" ).get( position ) );	

				if (isLive > 0) {
					liveText.setVisibility(View.VISIBLE);
			}

			if(stackData.get("vAccessory")!= null && stackData.get("vAccessory").get(position) != null && stackData.get("vAccessory").get(position).trim().length() > 0){
				liveNumber.setVisibility(View.VISIBLE);
				liveNumber.setText(stackData.get("vAccessory").get(position));
			}
		
		return lin;
	}

	/**
	 * setting the data for news/video stacks
	 */
	private View setTimeStampData ( int position, String tileType ) {
		
		
		LinearLayout lin 	=  ( LinearLayout ) inflater.inflate(R.layout.news_stacked_tile, null);	
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		lin.setLayoutParams(params);
		
		RelativeLayout stackImageLayout;
		LinearLayout newsBase;
		LinearLayout footerContent;
		TextView footerTitle;
		TextView footerSubtitle;
		ImageView sourceIcon;
		ImageView socialIcon;
		TextView stackSummary;
		TextView stackTitle;
		ImageView stackImage;
		ImageView playerIcon;
		View listDivider;

		newsBase = (LinearLayout) lin.findViewById(R.id.newsBase);
		stackImageLayout = (RelativeLayout) lin.findViewById(R.id.stackImageLayout);
		footerContent = (LinearLayout) lin.findViewById(R.id.footerContent);
		footerTitle = (TextView) lin.findViewById(R.id.footerTitle);
		footerSubtitle = (TextView) lin.findViewById(R.id.footerSubtitle);
		sourceIcon = (ImageView) lin.findViewById(R.id.sourceIcon);
		socialIcon = (ImageView) lin.findViewById(R.id.socialIcon);
		stackSummary = (TextView) lin.findViewById(R.id.stackSummary);
		stackTitle = (TextView) lin.findViewById(R.id.stackTitle);
		stackImage = (ImageView) lin.findViewById(R.id.stackImage);
		playerIcon = (ImageView) lin.findViewById(R.id.playerIcon);
		listDivider = (View) lin.findViewById( R.id.listDivider );
		
		
		footerTitle.setTextColor( Color.parseColor("#696B6E") );
		newsBase.setBackgroundColor(Color.parseColor("#FFFFFF"));
		
		
		
		stackSummary.setTypeface(Constants.OPEN_SANS_REGULAR);
		stackTitle.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		footerTitle.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		footerSubtitle.setTypeface(Constants.OPEN_SANS_LIGHT);

		try {
			footerContent.setVisibility(View.GONE);		
			stackTitle.setVisibility(View.GONE);
			stackSummary.setVisibility(View.GONE);	
			stackImageLayout.setVisibility(View.GONE);
			stackImage.setVisibility(View.GONE);
			playerIcon.setVisibility( View.GONE );
			sourceIcon.setVisibility(View.INVISIBLE);
			
			if ( tileType!= null && tileType.equalsIgnoreCase(Types.STACKED_TIMESTAMP_TYPE)  )  {
				stackTitle.setSingleLine();
				stackTitle.setEllipsize(TruncateAt.END);			
				if( Constants.DENSITY.equalsIgnoreCase("medium") )
					stackSummary.setEllipsize(null);
			} else  {
	
				stackTitle.setLines( 3 );
				stackTitle.setEllipsize(TruncateAt.END);
				if( Constants.DENSITY.equalsIgnoreCase("medium") )
					stackTitle.setEllipsize(null);
				
				stackTitle.setLineSpacing(-7f, 1f);
				stackTitle.getLayoutParams().height = LayoutParams.FILL_PARENT;
				stackTitle.requestFocus();
			}
			
			
			if ( position == 0  ) {
				
				lin.setPadding(8, 4 , 8, 0);			
				
				listDivider.setVisibility(View.VISIBLE);
				if( position == getCount() -1  ) {
					listDivider.setVisibility(View.GONE);
				}
					
			} else if( position == getCount() -1 ) {
				lin.setPadding(8, 0, 8, 8);
				 listDivider.setVisibility(View.GONE);
			} else {
				lin.setPadding(8, 0, 8, 0);
				 listDivider.setVisibility(View.VISIBLE);
			}
			
			
			
			 
			if(stackData.get("vBackgroundColor").get(position) != null && 
					stackData.get("vBackgroundColor").get(position).trim().length() > 0){
				
				
				try {
						String vBackgroundColor = stackData.get("vBackgroundColor").get(position);
						if (vBackgroundColor.substring(0, 2).equalsIgnoreCase("0x")) {
							vBackgroundColor = vBackgroundColor.substring(2);
						}			
						
						lin.setTag(R.id.acceptedIgnore,vBackgroundColor);
						newsBase.setBackgroundColor(Color.parseColor("#"+vBackgroundColor));
				} catch (Exception e) {
					Logs.show(e);
				}
			}
			
			
	
			if(stackData.get("vSource").get(position) != null && stackData.get("vSource").get(position).trim().length() > 0){
				footerContent.setVisibility(View.VISIBLE);
				footerTitle.setVisibility(View.VISIBLE);
				footerTitle.setText(stackData.get("vSource").get(position));
			}
			if(stackData.get("vTimeStamp").get(position) != null && stackData.get("vTimeStamp").get(position).trim().length() > 0){

				footerContent.setVisibility(View.VISIBLE);
				footerSubtitle.setVisibility(View.VISIBLE);
				footerSubtitle.setText(dateUtil.gmt_to_local_timezone_tiles(stackData.get("vTimeStamp").get(position)));

			}
		

			if(stackData.get("vSourceIcon").get(position) != null && stackData.get("vSourceIcon").get(position).trim().length() > 0 ) {
				footerContent.setVisibility(View.VISIBLE);
				sourceIcon.setVisibility(View.VISIBLE);
				imageDownloader.download( stackData.get("vSourceIcon").get(position) , sourceIcon, false,null);
			}


			
			if (stackData.get("vImageUrl").get(position) != null  && stackData.get("vImageUrl").get(position).trim().length() > 0) {

				stackImageLayout.setVisibility(View.VISIBLE);	
				stackImage.setVisibility( View.VISIBLE );
				imageDownloader.download(stackData.get("vImageUrl").get(position), stackImage,false,null);
				// setting the player icon for video tile
				if ( tileType.equalsIgnoreCase(Types.STACKED_VIDEO_TYPE)  ) {
					playerIcon.setVisibility( View.VISIBLE );
				}
			}
			
			
			if(stackData.get("vTitle").get(position) != null && stackData.get("vTitle").get(position).trim().length() > 0){
				 String title = stackData.get("vTitle").get(position);
				 stackTitle.setVisibility(View.VISIBLE);
				 stackTitle.setText( title );
			}
			
			if ( tileType!= null && tileType.equalsIgnoreCase(Types.STACKED_TIMESTAMP_TYPE)  ) {
				
				if(stackData.get("vSummary").get(position) != null && 
						stackData.get("vSummary").get(position).trim().length() > 0){
					stackSummary.setVisibility(View.VISIBLE);
					stackSummary.setText(stackData.get("vSummary").get(position));		
				}
				
			} else {
				stackSummary.setVisibility( View.GONE );
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
		
		
		return lin;
	}
	
	
	/**
	 * setting the data for photo stacks
	 */
	private View setPhotoStackTile ( int position ) {
		

		LinearLayout lin 	=  ( LinearLayout ) inflater.inflate(R.layout.photo_stacked_tile, null);	
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(8, 0, 8, 0);
		lin.setLayoutParams(params);


		try {

			lin.setPadding(0, 0, 0, 0);				
			
			ImageView stackedImageView = (ImageView) lin.findViewById( R.id.stackedImageView);
			RelativeLayout stackedImageTile = (RelativeLayout)lin.findViewById( R.id.stackedImageTile);
			try {
				if(stackData.get("vBackgroundColor").get(position) != null && 
						stackData.get("vBackgroundColor").get(position).trim().length() > 0){
					String bgColor = stackData.get("vBackgroundColor").get(position).trim();
					if (bgColor.substring(0, 2).equalsIgnoreCase("0x")) {
						bgColor = bgColor.substring(2);
					}
					stackedImageTile.setBackgroundColor(Color.parseColor("#"+bgColor));	
				}
			} catch (Exception e) {
				Logs.show(e);
			}
			
			stackedImageView.setTag("fullImage");
			if (stackData.get("vImageUrl").get(position) != null
					&& stackData.get("vImageUrl").get(position).trim().length() > 0) {
				stackedImageView.setVisibility(View.VISIBLE);
				imageDownloader.download(stackData.get("vImageUrl").get(position), stackedImageView,false,null);		
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
		Logs.show(e);
		}
		
		
		return lin;
	}
	
	 
	
	private View setUpdateStackView ( int position ) {
		RelativeLayout lin 	=  ( RelativeLayout ) inflater.inflate(R.layout.update_stack, null);	
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		lin.setLayoutParams(params);
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
			highLightBlueColor ( v, true );
			startUpdating(v);
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {

			stopUpdating();
			if (event.getRawY() == rawY
					|| (event.getEventTime() - downTime) < 200) {

				highLightBlueColor(v, false);



			}
			
			Bundle b =  new Bundle();
			
			b.putString( "vMainColor",vMainColor );
			
			b.putString( "vMainTitleColor",vMainTitleColor );
			
			b.putString( "vSecColor",vSecColor );
			
			b.putString( "vSecTitleColor",vSecTitleColor );
			
			
			try {
				if(v.getTag(R.id.about_txtview) != null && v.getTag(R.id.about_txtview).toString().trim().length() > 0 ){
					
					fromFragment = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
					
					

					String type = v.getTag(R.id.active_users_text).toString();


					
					
					
					

					if (  type != null && type.equalsIgnoreCase(Constants.ACCEPT_TYPE_COVERSATION)){

						b.putBoolean("isHref", (Boolean) v.getTag(R.id.avtarImage2));
						b.putString("vConversationUrl",v.getTag(R.id.about_txtview).toString());
						b.putString("fromFragment",fromFragment);
						PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchHomeFragment",b);
						
					}else if( type != null && type.equalsIgnoreCase(Constants.ACCEPT_TYPE_SPORTS_JSON)){
						b.putBoolean("isHref", (Boolean) v.getTag(R.id.avtarImage2));
						b.putString( "fromFragment",fromFragment );
						PlayupLiveApplication.getFragmentManagerUtil().setFragment("AllSportsFragment",b);
						
					}else if( type != null && type.equalsIgnoreCase(Constants.ACCEPT_TYPE_FIXTURES_JSON)){
						b.putBoolean("isHref", (Boolean) v.getTag(R.id.avtarImage2));
						b.putString( "fromFragment",fromFragment );
						b.putString("vCurrentSeasonUrl",v.getTag(R.id.about_txtview).toString() );
						PlayupLiveApplication.getFragmentManagerUtil().setFragment("FixturesAndResultsFragment",b);

					}else if( type != null && ( type.equalsIgnoreCase(Constants.ACCEPT_TYPE_SECTION_JSON) || type.equalsIgnoreCase( Constants.ACCEPT_TYPE_GROUPING_OLYMPICS ) ) ){
						
						if ( type.equalsIgnoreCase( Constants.ACCEPT_TYPE_GROUPING_OLYMPICS ) ) {
							
						
							b.putBoolean("isHref", (Boolean) v.getTag(R.id.avtarImage2));
							b.putString("vContestLobbyUrl",v.getTag(R.id.about_txtview).toString() );
							b.putString("fromFragment",fromFragment );
							PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchRoomFragment",b);
								
						} else {
							b.putBoolean("isHref", (Boolean) v.getTag(R.id.avtarImage2));
							b.putString( "fromFragment",fromFragment );
							b.putString("vSectionUrl",v.getTag(R.id.about_txtview).toString());
							PlayupLiveApplication.getFragmentManagerUtil().setFragment("NewsFragment",b);
						}
						

					}else if( type != null && type.equalsIgnoreCase(Constants.ACCEPT_TYPE_HTML)){
						
						b.putBoolean("isHref", (Boolean) v.getTag(R.id.avtarImage2));
						b.putString("url",v.getTag(R.id.about_txtview).toString());
						b.putString("fromFragment",fromFragment);
						PlayupLiveApplication.getFragmentManagerUtil().setFragment("WebViewFragment",b);

					}else if( type != null && (type.equalsIgnoreCase(Constants.ACCEPT_TYPE_VIDEO)
							|| type.equalsIgnoreCase(Constants.ACCEPT_TYPE_AUDIO))){

						Intent i = new Intent(PlayUpActivity.context,VideoActivity.class);
						i.putExtra("videoUrl", v.getTag(R.id.about_txtview).toString());
						i.putExtra("isHref", (Boolean) v.getTag(R.id.avtarImage2));
						PlayUpActivity.context.startActivity(i);
						
					}else if( type != null && type.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){
						if(v.getTag(R.id.aboutText) != null && v.getTag(R.id.aboutText).toString().trim().length() > 0){
						b.putBoolean("isHref", (Boolean) v.getTag(R.id.avtarImage2));
						b.putString("vCompetitionId",v.getTag(R.id.aboutText ).toString());
						b.putString("fromFragment",fromFragment);
						PlayupLiveApplication.getFragmentManagerUtil().setFragment("LeagueLobbyFragment",b);
						}

					} else if (  type != null && type.equalsIgnoreCase( Constants.ACCEPT_TYPE_MY_CONTEST_LOBBY_JSON ) ) {
						b.putString( "fromFragment",fromFragment );
						b.putBoolean("isHref", (Boolean) v.getTag(R.id.avtarImage2));
						PlayupLiveApplication.getFragmentManagerUtil().setFragment("PrivateLobbyFragment",b);

					} else if ( type != null && type.equalsIgnoreCase( Constants.ACCEPT_TYPE_COVERSATION ) ) {

						b.putBoolean("isHref", (Boolean) v.getTag(R.id.avtarImage2));
						b.putString("vConversationUrl",v.getTag(R.id.about_txtview).toString());
						b.putString("fromFragment",fromFragment);
						PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchHomeFragment",b);

					} else if ( type != null && type.equalsIgnoreCase( Constants.ACCEPT_TYPE_CONTEST_LOBBY_JSON ) ) {
						
					/*	Log.e("------","-------->>>>>>>>"+v.getTag(R.id.about_txtview).toString() );
						
						if(v.getTag(R.id.about_txtview).toString().contains("tiles"))
							v.getTag(R.id.about_txtview).toString().replace("tiles","staging");
						
						Log.e("------","--------<<<<<<<<"+v.getTag(R.id.about_txtview).toString() );*/
						b.putBoolean("isHref", (Boolean) v.getTag(R.id.avtarImage2));
						b.putString("vContestLobbyUrl",v.getTag(R.id.about_txtview).toString());
						b.putString("fromFragment",fromFragment );
						PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchRoomFragment",b);

					} else if ( type != null && type.equalsIgnoreCase( Types.LEADERBOARD_TYPE )  && !Constants.isFetchingCredentials) {
						///NEED TO DO HREF RELATED WORK HERE , LIKE ABOVE


						HttpRequest request =	null;
						if((Boolean) v.getTag(R.id.avtarImage2)){
							String encodedHrefURL	= null;
							String vTokenValue		= 	null;
							
							int tokenType = 	new Util().checkForHrefTokenType( v.getTag(R.id.about_txtview).toString());
							vTokenValue		=	new Util().checkForHrefTokenParam( v.getTag(R.id.about_txtview).toString()); 
							encodedHrefURL	=	new Util().getPersonalizedEnocodedURL(v.getTag(R.id.about_txtview).toString(),vTokenValue,tokenType);
							
							request 		= new HttpRequest(  v.getTag(R.id.about_txtview).toString(),encodedHrefURL,true, Constants.GET_METHOD );
						}
						else{
							 request 		= new HttpRequest(  v.getTag(R.id.about_txtview).toString(), Constants.GET_METHOD );	
						}
					//	HttpRequest request = new HttpRequest(  v.getTag(R.id.about_txtview).toString(), Constants.GET_METHOD );
						try {
							StringBuffer strBuffer	=	(StringBuffer) request.send();

							if(request.getStatusCode() == 401){
								Util.callTheCredentialsUrl(strBuffer);
								

							}
						} catch (RequestRepeatException e) {
							Logs.show ( e );
						}

					} else if ( type != null && type.equalsIgnoreCase( Types.CONTEST_SET_TYPE ) && !Constants.isFetchingCredentials) {
						
						///NEED TO DO HREF RELATED WORK HERE , LIKE ABOVE
						HttpRequest request =null;
						if((Boolean) v.getTag(R.id.avtarImage2)){
							String encodedHrefURL	= null;
							String vTokenValue		= 	null;
						
							int tokenType = 	new Util().checkForHrefTokenType(v.getTag(R.id.about_txtview).toString());
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
							Logs.show ( e );
						}
					} else if ( type  != null && type.equalsIgnoreCase( Types.TEAM_SCHEDULE_TYPE ) ) { 

						b.putBoolean("isHref", (Boolean) v.getTag(R.id.avtarImage2));
						b.putString( "vTeamScheduleUrl",v.getTag(R.id.about_txtview).toString() );
						b.putString( "fromFragment",fromFragment );
						PlayupLiveApplication.getFragmentManagerUtil().setFragment("TeamScheduleFragment",b);
						
					} 
				}
			} catch (Exception e) {
				Logs.show(e);
			} 
		}
		if (event.getAction() == MotionEvent.ACTION_CANCEL
				|| event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			stopUpdating();
			// disable the blue color
			highLightBlueColor(v, false);

		}

		if (event.getEventTime() > (downTime + Constants.highightDelay)) {

			if (event.getRawY() >= rawY - 10 && event.getRawY() <= rawY + 10) {

				stopUpdating();

			}

		}

		return true;
	}


	public void highLightBlueColor ( final View view, final boolean shouldDo ) {

		try {
			if ( view == null ) {
				return;
			}

			// performs highlighting blue color for normal ties only
			if( view.getTag() != null && ( view.getTag().toString().equalsIgnoreCase( Types.STACKED_TIMESTAMP_TYPE ) 
					|| view.getTag().toString().equalsIgnoreCase( Types.STACKED_VIDEO_TYPE ) ) ) {
				
				if ( shouldDo ) {

					
					((LinearLayout)view.findViewById(R.id.newsBase)).setBackgroundResource(R.drawable.allsports_list_d);
					((LinearLayout)view.findViewById(R.id.newsBase)).setPadding(0, 0, 0, 0);
					((TextView)view.findViewById(R.id.stackTitle)).setTextColor(Color.parseColor("#FFFFFF"));
					((TextView)view.findViewById(R.id.stackSummary)).setTextColor(Color.parseColor("#FFFFFF"));
					((TextView)view.findViewById(R.id.footerTitle)).setTextColor(Color.parseColor("#FFFFFF"));
					((TextView)view.findViewById(R.id.footerSubtitle)).setTextColor(Color.parseColor("#FFFFFF"));

				} else { // remove the highlight
					
					String vBackgroundColor  = "FFFFFF";
					
					if(view.getTag(R.id.acceptedIgnore) != null)
						 vBackgroundColor =  view.getTag(R.id.acceptedIgnore).toString();
					
					
				
					
					try {
						((LinearLayout)view.findViewById(R.id.newsBase)).setBackgroundColor(Color.parseColor("#"+vBackgroundColor));
					} catch (Exception e) {
						Logs.show(e);
					}
					((TextView)view.findViewById(R.id.stackTitle)).setTextColor(Color.parseColor("#000000"));
					((TextView)view.findViewById(R.id.stackSummary)).setTextColor(Color.parseColor("#696B6E"));
					((TextView)view.findViewById(R.id.footerTitle)).setTextColor(Color.parseColor("#696B6E"));
					((TextView)view.findViewById(R.id.footerSubtitle)).setTextColor(Color.parseColor("#B8B6B8"));
				}
				
				
			} else if( view.getTag() != null && view.getTag().toString().equalsIgnoreCase( Types.STACKED_TYPE ) )  {			
				if ( shouldDo ) {

					((RelativeLayout)view.findViewById(R.id.listBase)).setBackgroundColor(Color.parseColor("#B0E6FF"));
					((TextView)view.findViewById(R.id.leagueTitle)).setTextColor(Color.parseColor("#FFFFFF"));
					((TextView)view.findViewById(R.id.leagueSubTitle)).setTextColor(Color.parseColor("#FFFFFF"));
					((ImageView)view.findViewById(R.id.chevron)).setImageResource(R.drawable.chevron_d);
					((TextView)view.findViewById(R.id.liveNowTextView)).setTextColor(Color.parseColor("#FFFFFF"));

				} else { // remove the highlight
					
					String vBackgroundColor  = "F7F7F4";
					
					if(view.getTag(R.id.acceptedIgnore) != null)
						 vBackgroundColor =  view.getTag(R.id.acceptedIgnore).toString();

					try {
						((RelativeLayout)view.findViewById(R.id.listBase)).setBackgroundColor(Color.parseColor("#"+vBackgroundColor));
					} catch (Exception e) {
						Logs.show(e);
					}
					((TextView)view.findViewById(R.id.leagueTitle)).setTextColor(Color.parseColor("#565656"));
					((TextView)view.findViewById(R.id.leagueSubTitle)).setTextColor(Color.parseColor("#B8B6B8"));
					((ImageView)view.findViewById(R.id.chevron)).setImageResource(R.drawable.chevron);
					((TextView)view.findViewById(R.id.liveNowTextView)).setTextColor(Color.parseColor("#27A544"));
				}		
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
			
		}


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

				if (v !=null && v.getId() >= 0 && v.getId() < getCount()) {


				}
				super.handleMessage(msg);
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
				// TODO Auto-generated catch block
				Logs.show ( e );
			}
		}
	}

	private void showList(){

		try {

			
			if ( stackData != null && stackData.get("vContentId") != null && stackData.get("vContentId").size() > 0) {

				  listBase.removeAllViews();

				  
				final int count = stackData.get("vContentId").size();

				
				for (int i = 0; i < count; i++) {
					
					String vContentType = null;
					String vDisplayType = null;
					
					if( stackData!= null && stackData.get("vContentType") != null  )
						vContentType = stackData.get("vContentType").get(i);
					
					if( stackData!= null && stackData.get("vDisplayType") != null  )
						vDisplayType = stackData.get("vDisplayType").get(i);

						// row linear layout
						LinearLayout tileItemLayout = new LinearLayout( PlayUpActivity.context);
						LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
						tileItemLayout.setLayoutParams(params);
						
						tileItemLayout.addView(getView(i, vContentType, vDisplayType ));
						listBase.addView(tileItemLayout);	
					
				}
				

			}
		}catch(Exception e){

			Logs.show(e);

		}
	}



	
	
   


    private boolean isSupportedDisplayType( String displayType ) {
		
		if( displayType.equalsIgnoreCase(Types.STACKED_IMAGE_TYPE) ||
				displayType.equalsIgnoreCase(Types.STACKED_TIMESTAMP_TYPE) ||
				displayType.equalsIgnoreCase(Types.STACKED_TYPE) ||
				displayType.equalsIgnoreCase(Types.STACKED_VIDEO_TYPE)  ) {
			return true;
		}
		
			return false;
	}




}
