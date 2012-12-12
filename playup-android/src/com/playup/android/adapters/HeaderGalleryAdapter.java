package com.playup.android.adapters;

import java.util.Hashtable;


import java.util.List;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.activity.VideoActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.connection.HttpRequest;
import com.playup.android.exception.RequestRepeatException;
import com.playup.android.util.Constants;
import com.playup.android.util.DateUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class HeaderGalleryAdapter extends BaseAdapter implements OnItemClickListener {

	private LayoutInflater inflater;
	private Hashtable<String, List<String>> featuresData;

	private ImageDownloader imageDownloader;
	private DateUtil dateUtil;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;

	private boolean isGalleryScrolling = false;
	private String fromFragment = null;
	public LeaderBoardAdapter leaderBoardAdapter = null;
	Hashtable<String , Hashtable<String, List<String>> > contestData;
	public android.widget.LinearLayout.LayoutParams headerParams = null;
	public boolean frmNotify = false;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;
	Gallery headerGallery;
	LinearLayout dots;
	private String vDisplayType = null;
	
	class ViewHolder {

		public RelativeLayout matchHighLights;
		public RelativeLayout imageWithSummaryView;
		public ImageView imageWithSummary;
		public TextView summary;
		public TextView imageSummary;
		public ImageView playerIcon;
		public RelativeLayout footerContent;
		public TextView footerTitle;
		public TextView footerSubtitle;
		public ImageView sourceIcon;
		public ImageView socialIcon;
		public ImageView onlyImage;

		private RelativeLayout matchHeaderLayout;
		private RelativeLayout updateFeature;
		private LinearLayout onlyImageBackground;

	}


	private void getImages () {

		try {
			if(featuresData != null && featuresData.get("vContentId") != null ){

				int count = featuresData.get("vContentId").size();
				for ( int i = 0; i < count; i++ ) {

					if ( i == 0 ) {
						
						if(featuresData.get("vSourceIcon").get( i ) != null && featuresData.get("vSourceIcon").get( i ).trim().length() > 0){
							imageDownloader.download( featuresData.get("vSourceIcon").get( i ) , null, false, this , true );
						}
						if(featuresData.get("vSocialIcon").get( i ) != null && featuresData.get("vSocialIcon").get( i ).trim().length() > 0){
							imageDownloader.download( featuresData.get("vSocialIcon").get( i ) , null , false, this , true );
						}
						if( featuresData.get("vImageUrl").get( i )!= null && featuresData.get("vImageUrl").get( i ).trim().length() >0 ) { 
							
							
							imageDownloader.download(featuresData.get("vImageUrl").get( i ), null, false, this,true);
						}
						
					} else {

						if(featuresData.get("vSourceIcon").get( i ) != null && featuresData.get("vSourceIcon").get( i ).trim().length() > 0){
							imageDownloader.download( featuresData.get("vSourceIcon").get( i ) , null, false, null , false );
						}
						if(featuresData.get("vSocialIcon").get( i ) != null && featuresData.get("vSocialIcon").get( i ).trim().length() > 0){
							imageDownloader.download( featuresData.get("vSocialIcon").get( i ) , null , false, null , false );
						}
						if( featuresData.get("vImageUrl").get( i )!= null && featuresData.get("vImageUrl").get( i ).trim().length() >0 ) { 
							imageDownloader.download(featuresData.get("vImageUrl").get( i ), null, false, null, false );
						}
					}
					

				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}


	
	public HeaderGalleryAdapter (Gallery headerGallery, LinearLayout dots, Hashtable<String, List<String>> data,
			String vMainColor,String vMainTitleColor, String vSecColor, String vSecTitleColor)  {
				
		this.headerGallery = headerGallery;
		this.dots =dots;
		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;

		inflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.featuresData = data;
		
		this.vMainColor = vMainColor;
		this.vMainTitleColor  = vMainTitleColor;

		imageDownloader = new ImageDownloader();

		dateUtil = new DateUtil();

		contestData = new Hashtable<String , Hashtable<String, List<String>>>();
		
		showDots();
		headerGallery.setOnItemSelectedListener( headerGalleryItemListener );
		getImages ();

	}


	public void setData(Hashtable<String, List<String>> data,String fromFragment,String vMainColor,String vMainTitleColor, String vSecColor, String vSecTitleColor) {
		
		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;

		this.featuresData = data;
		this.fromFragment = fromFragment;
		
		this.vMainColor = vMainColor;
		this.vMainTitleColor  = vMainTitleColor;

		
		contestData = new Hashtable<String, Hashtable<String,List<String>>>();
		getImages ();
		
		showDots();
		
		notifyDataSetChanged();	
	}

	public void refresh() {
		contestData = new Hashtable<String, Hashtable<String,List<String>>>();
		notifyDataSetChanged();
	}


	@Override
	public void notifyDataSetChanged() {
		
		if( !isGalleryScrolling ) {
			frmNotify = true;
			super.notifyDataSetChanged();
		}
		frmNotify = false;
	}

	@Override
	public int getCount() {

		if(featuresData != null && featuresData.get("vContentId") != null ){


			return featuresData.get("vContentId").size() ;
		}

		else 
			return 0;
	}

	@Override
	public Object getItem(int position) {

		return null;
	}

	@Override
	public long getItemId(int position) {

		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		try {
			
			if ( inflater == null ) {
				LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				return  layoutInflater.inflate( R.layout.header, null);
			}
			ViewHolder vh = null;
			if (convertView == null ) {

				vh = new ViewHolder();

				convertView = inflater.inflate(R.layout.header, null);


				vh.matchHeaderLayout = (RelativeLayout) convertView.findViewById(R.id.matchHeaderLayout);
				
				vh.matchHighLights  = (RelativeLayout) convertView.findViewById(R.id.matchHighLights);				
				vh.imageWithSummary = (ImageView) convertView.findViewById(R.id.imageViewWithSummary);	
				vh.imageWithSummaryView = (RelativeLayout) convertView.findViewById(R.id.ImageWithSummaryView);	
				vh.summary = (TextView) convertView.findViewById(R.id.summary);				
				vh.imageSummary = (TextView) convertView.findViewById(R.id.ImageSummary);				
				vh.playerIcon = (ImageView) convertView.findViewById(R.id.playerIcon);			
				vh.footerContent = (RelativeLayout) convertView.findViewById(R.id.footerContent);				
				vh.footerTitle = (TextView) convertView.findViewById(R.id.footerTitle);				
				vh.footerSubtitle = (TextView) convertView.findViewById(R.id.footerSubtitle);				
				vh.sourceIcon = (ImageView) convertView.findViewById(R.id.sourceIcon);				
				vh.socialIcon = (ImageView) convertView.findViewById(R.id.socialIcon);				
				vh.onlyImage = (ImageView) convertView.findViewById(R.id.onlyImage); 
				vh.updateFeature = (RelativeLayout) convertView.findViewById(R.id.updateFeature);	
				vh.onlyImageBackground = (LinearLayout) convertView.findViewById(R.id.onlyImageBackground);
				vh.footerTitle.setTextColor(Color.parseColor("#000000"));
				vh.footerTitle.setTypeface( Constants.OPEN_SANS_SEMIBOLD );
				vh.footerSubtitle.setTypeface( Constants.OPEN_SANS_REGULAR );
				vh.summary.setTypeface( Constants.OPEN_SANS_LIGHT );		



				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}

			// checking the display type and showing update/proper items
			if( featuresData != null && featuresData.get("vDisplayType")!= null  )
				vDisplayType = featuresData.get("vDisplayType").get( position );
			else
				vDisplayType = null;
			
			if( vDisplayType == null || !isSupportedDisplayType( vDisplayType ) ) {
				//vh.updateFeature.setVisibility( View.VISIBLE );
				return convertView;
			}

			
			convertView.setPadding(0,0,0,0);
			
			
			vh.matchHighLights.setVisibility(View .GONE);
			vh.footerContent.setVisibility(View.GONE);
			vh.footerTitle.setVisibility(View.INVISIBLE);
			vh.footerSubtitle.setVisibility(View.INVISIBLE);
			vh.sourceIcon.setVisibility(View.INVISIBLE);
			vh.socialIcon.setVisibility(View.GONE);
			vh.updateFeature.setVisibility( View.GONE );
			vh.imageWithSummaryView.setVisibility(View.GONE);
			vh.imageSummary.setVisibility(View.GONE);
			vh.playerIcon.setVisibility(View.GONE);
			vh.onlyImage.setVisibility(View.GONE);
			vh.onlyImageBackground.setVisibility(View.GONE);
			vh.summary.setVisibility(View.GONE);
			vh.matchHeaderLayout.removeAllViews();

			
			
			/*
			if(featuresData.get("vContentType").get(position) != null && 
					featuresData.get("vContentType").get(position).equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){

				convertView.setTag(R.id.about_txtview,featuresData.get("vContentUrl").get(position));
				convertView.setTag(R.id.active_users_text,featuresData.get("vContentType").get(position));
				convertView.setTag(R.id.aboutText,featuresData.get("vContentId").get(position));
			}
			if(featuresData.get("vLinkUrl").get(position) != null && featuresData.get("vLinkUrl").get(position).trim().length() > 0){
				convertView.setTag(R.id.about_txtview,featuresData.get("vLinkUrl").get(position));
				convertView.setTag(R.id.active_users_text,featuresData.get("vLinkType").get(position));
			}*/

			//Praveen: modififed as per the HREF element

		

			if(featuresData.get("vContentType").get(position) != null && featuresData.get("vContentType").get(position).equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){
				
				if(featuresData.get("vContentHrefUrl").get(position) != null && featuresData.get("vContentHrefUrl").get(position).trim().length()> 0){
					// Must implement the encoding logic here....
					convertView.setTag(R.id.avtarImage2,true);
					convertView.setTag(R.id.about_txtview,featuresData.get("vContentHrefUrl").get(position));
				}
				else{
					convertView.setTag(R.id.avtarImage2,false);
					convertView.setTag(R.id.about_txtview,featuresData.get("vContentUrl").get(position));	
					
				}
				convertView.setTag(R.id.active_users_text,featuresData.get("vContentType").get(position));
				convertView.setTag(R.id.aboutText,featuresData.get("vContentId").get(position));
			}
			if((featuresData.get("vLinkHrefUrl").get(position) != null && featuresData.get("vLinkHrefUrl").get(position).trim().length() > 0)){
				
					convertView.setTag(R.id.avtarImage2,true);
					convertView.setTag(R.id.about_txtview,featuresData.get("vLinkHrefUrl").get(position));
					convertView.setTag(R.id.active_users_text,featuresData.get("vLinkType").get(position));
			}else if ((featuresData.get("vLinkUrl").get(position) != null && featuresData.get("vLinkUrl").get(position).trim().length() > 0)){
				
					convertView.setTag(R.id.avtarImage2,false);
					convertView.setTag(R.id.about_txtview,featuresData.get("vLinkUrl").get(position));
					convertView.setTag(R.id.active_users_text,featuresData.get("vLinkType").get(position));
			}
		
		
			
			if( vDisplayType.equalsIgnoreCase(Types.FEATURE_IMAGE) ) {
				vh.onlyImageBackground.setVisibility(View.VISIBLE);
				vh.onlyImage.setVisibility(View.VISIBLE);	
				setBackGroundColor( position, vh.onlyImageBackground ) ;
				if( featuresData.get("vImageUrl").get(position)!= null && featuresData.get("vImageUrl").get(position).trim().length() >0 ) {
					//Log.e("123", "Feature_Image==========>>>>>>>>>>>>>>>"+featuresData.get("vImageUrl").get(position));
					imageDownloader.download(featuresData.get("vImageUrl").get(position), vh.onlyImage, false, this,true);
				}
				return convertView;

			} else if ( vDisplayType.equalsIgnoreCase( Types.FEATURE_HIGHLIGHT ) ) {
				
				vh.matchHighLights.setVisibility(View .VISIBLE);
					if(featuresData.get("vHighlightHrefUrl").get(position)!=null && featuresData.get("vHighlightHrefUrl").get(position).trim().length()>0)
					{
						showHeader(featuresData.get("vHighlightHrefUrl").get(position),featuresData.get("vHighlightId").get(position),vh);
					}else if (featuresData.get("vHighlightUrl").get(position)!=null && featuresData.get("vHighlightUrl").get(position).trim().length()>0)
					{
						showHeader(featuresData.get("vHighlightUrl").get(position),featuresData.get("vHighlightId").get(position),vh);
					}
				setFooterData( position, vh, convertView, true );

				if(featuresData.get("vSummary").get(position) != null && 
						featuresData.get("vSummary").get(position).trim().length() > 0){
					vh.summary.setVisibility(View.VISIBLE);
					vh.summary.setText(featuresData.get("vSummary").get(position));
				}			
			
				convertView.setTag(R.id.aboutText,featuresData.get("vHighlightId").get(position));
				
					final String vContestId = featuresData.get("vHighlightId").get(position);
		
					if(contestData != null && !contestData.containsKey(vContestId)){
		
						new Thread(new Runnable() {
		
							@Override
							public void run() {
								try {
									getHeader(vContestId);
								} catch (Exception e) {
									Logs.show(e);
		
								}
		
							}
						}).start();
					}	
				
				return convertView;
		
			} else if ( vDisplayType.equalsIgnoreCase(Types.FEATURE_TIMESTAMP) ) {
			
				setFooterData( position, vh, convertView, true );
			
				vh.imageWithSummaryView.setVisibility(View.VISIBLE);
				vh.imageWithSummary.setImageBitmap(null);
				setBackGroundColor( position, vh.imageWithSummaryView );
				setSummary( position, vh, vDisplayType );
				if( featuresData.get("vImageUrl").get(position) != null && featuresData.get("vImageUrl").get(position).trim().length() > 0) {
					//Log.e("123", "Set featured tile url------"+featuresData.get("vImageUrl").get(position));
					imageDownloader.download(featuresData.get("vImageUrl").get(position), vh.imageWithSummary, false, this, true );
				}
				return convertView;
				
			} else if ( vDisplayType.equalsIgnoreCase(Types.FEATURE_PHOTO) ) {
				
				setSummary(position, vh, vDisplayType);
				setFooterData( position, vh, convertView, false );			
				vh.imageWithSummaryView.setVisibility(View.VISIBLE);
				vh.imageWithSummary.setImageBitmap(null);
				if( featuresData.get("vImageUrl").get(position) != null && featuresData.get("vImageUrl").get(position).trim().length() > 0) {
					//Log.e("123", "Set featured tile url------"+featuresData.get("vImageUrl").get(position));
					vh.imageWithSummaryView.setBackgroundColor(Color.GRAY);
					imageDownloader.download(featuresData.get("vImageUrl").get(position), vh.imageWithSummary, false, this, true );
				}
				
				return convertView;
				
			} else if ( vDisplayType.equalsIgnoreCase(Types.FEATURE_VIDEO_TIMESTAMP) ) {
				
				vh.imageWithSummaryView.setVisibility(View.VISIBLE);
				vh.imageWithSummary.setImageBitmap(null);
				vh.playerIcon.setVisibility(View.VISIBLE);
				setSummary(position, vh, vDisplayType);
				setFooterData( position, vh, convertView, true );
				setBackGroundColor( position, vh.imageWithSummaryView );
				if( featuresData.get("vImageUrl").get(position) != null && featuresData.get("vImageUrl").get(position).trim().length() > 0) {
					imageDownloader.download(featuresData.get("vImageUrl").get(position), vh.imageWithSummary, false, this, true );
				}
				
				return convertView;			
				
			} else if ( vDisplayType.equalsIgnoreCase(Types.FEATURE_VIDEO) ) {
				
				vh.imageWithSummaryView.setVisibility(View.VISIBLE);
				vh.imageWithSummary.setImageBitmap(null);
				vh.playerIcon.setVisibility(View.VISIBLE);
				setSummary(position, vh, vDisplayType);
				setFooterData( position, vh, convertView, false );
				setBackGroundColor( position, vh.imageWithSummaryView );
				if( featuresData.get("vImageUrl").get(position) != null && featuresData.get("vImageUrl").get(position).trim().length() > 0) {
					imageDownloader.download(featuresData.get("vImageUrl").get(position), vh.imageWithSummary, false, this, true );
				}
				
				return convertView;
			}


			return convertView;
			
		} catch (Exception e) {
			Logs.show(e);
		}
		return convertView;
		
	}


	private void getHeader(String vContestId){

		final Hashtable<String, List<String>> data = PlayupLiveApplication.getDatabaseWrapper().select( "SELECT vContestId,dScheduledStartTime," +
				"dStartTime,dEndTime,iTotal1,iTotal2,vHomeTeamId,vAwayTeamId,vSummary,vSportsName,vCompetitionName,iWickets1, iWickets2,vOvers,vRunRate,vLastBall,vPlayerFirstName1,vPlayerFirstName2,vPlayerLastName1,vPlayerLastName2" +
				",vRole2,vRole1,vStats1,vStats2,vStrikerFirstName,vStrikerLastName,vStrikerStats,vNonStrikerFirstName,vNonStrikerLastName,vNonStrikerStats,vAnnotation,vAnnotation2,vSummary1,vSummary2,iGoals1,iBehinds1,iSuperGoals1,iGoals2,iBehinds2,iSuperGoals2,iInnnings,vInnningsHalf,iBalls1,iBalls2,iOut1,iOut2,iStrikes1,iStrikes2,vBase1,vBase2,iHasLiveUpdates, vLastEventName, vShortMessage, vLongMessage,vSportType, iActive1,iActive2, " +
				"( SELECT vDisplayName FROM teams WHERE vTeamId = vHomeTeamId  ) AS vHomeDisplayName, " +
				"( SELECT vDisplayName FROM teams WHERE vTeamId = vAwayTeamId  ) AS vAwayDisplayName, " +
				"(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vHomeTeamId ) AS vHomeCalendarUrl," +
				"(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vAwayTeamId ) AS vAwayCalendarUrl  "+
				" FROM contests WHERE vContestId = \""+vContestId+"\"" );

		if(contestData != null && data != null && data.get( "vHomeTeamId" ) != null  && data.get( "vHomeTeamId" ).size() > 0 ){

			contestData.put(vContestId, data);
			if(PlayUpActivity.handler != null){

				PlayUpActivity.handler.post(new Runnable() {

					@Override
					public void run() {
						try {
							notifyDataSetChanged();
						} catch (Exception e) {
							// TODO Auto-generated catch block

						}

					}
				});

			}

		}



	}
	private void showHeader(final String vHighLightUrl,final String vContestId,final ViewHolder v){

		try {
			if(contestData != null && contestData.containsKey(vContestId)) {
				
				String sportType = null;
				View headerView = null;	

				if( !frmNotify ) {
					if( contestData.get(vContestId)!= null && contestData.get(vContestId).get("vSportType")!=null )
						sportType = contestData.get(vContestId).get("vSportType").get(0);
					
					if( sportType!=null && sportType.equalsIgnoreCase(Constants.SET_BASED_DATA) ) {	
						headerView = (View) inflater.inflate(R.layout.match_header_setbased, null);
					} else if( sportType!=null && sportType.equalsIgnoreCase(Constants.LEADERBOARD) ){
						headerView = (View) inflater.inflate(R.layout.match_header_leaderboard, null);
					} else if( sportType!= null ){
						headerView = (View) inflater.inflate(R.layout.match_header_normal, null);
					}
					v.matchHeaderLayout.setBackgroundResource(0);
					v.matchHeaderLayout.removeAllViews();
					v.matchHeaderLayout.addView( headerView );
				}
				
				new MatchHeaderGenerator(contestData.get(vContestId), v.matchHeaderLayout.getChildAt(0), true );
				headerView = null;
			}


		} catch (Exception e) {

			Logs.show( e );
		}
	}


	@Override
	public void onItemClick(AdapterView<?> arg0, final View v, int position, long id) {

		try {
			if(v.getTag(R.id.about_txtview) != null && v.getTag(R.id.about_txtview).toString().trim().length() > 0 ){

				String temp_type = null;
				if ( v.getTag(R.id.active_users_text) != null ) {
					temp_type = v.getTag(R.id.active_users_text).toString();
				}

				String temp_url = null;
				if ( v.getTag(R.id.about_txtview) != null ) {
					temp_url = v.getTag(R.id.about_txtview).toString();
				}

				String temp_vId = null;
				if ( v.getTag(R.id.aboutText) != null ) {
					temp_vId = v.getTag(R.id.aboutText).toString();
				}
				final String type = temp_type;
				final String url = temp_url;
				final String vId = temp_vId;



				if(PlayUpActivity.handler != null){

					PlayUpActivity.handler.post(new Runnable() {

						@Override
						public void run() {
							try {

								
								Bundle b =  new Bundle();
								
								b.putString( "vMainColor",vMainColor );
								
								b.putString( "vMainTitleColor",vMainTitleColor );
								
								b.putString( "vSecColor",vSecColor );
								
								b.putString( "vSecTitleColor",vSecTitleColor );
								
								fromFragment = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
								
								


								if( type != null && type.equalsIgnoreCase(Constants.ACCEPT_TYPE_COVERSATION)){

									b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2) );
									b.putString("vConversationUrl", url );
									b.putString("fromFragment",fromFragment);
									PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchHomeFragment",b);
								}else if(  type != null &&  type.equalsIgnoreCase(Constants.ACCEPT_TYPE_MY_CONTEST_LOBBY_JSON)){
									PlayupLiveApplication.getFragmentManagerUtil().setFragment("PrivateLobbyFragment");

								}else if(  type != null &&  type.equalsIgnoreCase(Constants.ACCEPT_TYPE_SPORTS_JSON)){
									b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
									b.putString("fromFragment",fromFragment);
									PlayupLiveApplication.getFragmentManagerUtil().setFragment("AllSportsFragment",b);


								} else if(  type != null &&  ( type.equalsIgnoreCase(Constants.ACCEPT_TYPE_SECTION_JSON) || type.equalsIgnoreCase( Constants.ACCEPT_TYPE_GROUPING_OLYMPICS ) ) ) {


									if ( type.equalsIgnoreCase( Constants.ACCEPT_TYPE_GROUPING_OLYMPICS ) ) {
										b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
										b.putString("vContestLobbyUrl",url );
										b.putString("fromFragment",fromFragment );
										PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchRoomFragment",b);

									} else {
										b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
										b.putString("fromFragment",fromFragment);
										b.putString("vSectionUrl", url );
										PlayupLiveApplication.getFragmentManagerUtil().setFragment("NewsFragment",b);

									}





								}else if( type != null &&  type.equalsIgnoreCase(Constants.ACCEPT_TYPE_HTML)){
									b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
									b.putString("url", url );
									b.putString("fromFragment", fromFragment);
									PlayupLiveApplication.getFragmentManagerUtil().setFragment("WebViewFragment",b);

								} else if (  type != null &&  (type.equalsIgnoreCase(Constants.ACCEPT_TYPE_VIDEO ) || 
										 type.equalsIgnoreCase(Constants.ACCEPT_TYPE_AUDIO))) {
									Intent i = new Intent(PlayUpActivity.context,VideoActivity.class);
									i.putExtra("videoUrl",  url );		
									i.putExtra("isHref",(Boolean) v.getTag(R.id.avtarImage2));
									PlayUpActivity.context.startActivity(i);

								}else if(  type != null &&  type.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){
									if(vId != null && vId.trim().length() > 0){
										b.putString("fromFragment",fromFragment);
										b.putString("vCompetitionId", vId );
										PlayupLiveApplication.getFragmentManagerUtil().setFragment("LeagueLobbyFragment",b);
									}
								}else if(  type != null &&  type.equalsIgnoreCase(Constants.ACCEPT_TYPE_CONTEST_LOBBY_JSON)){

									b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
									b.putString("vContestLobbyUrl",url);
									b.putString("fromFragment",fromFragment );
									PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchRoomFragment",b);


								} else if (  type != null &&  type.equalsIgnoreCase( Constants.ACCEPT_TYPE_FIXTURES_JSON ) ) {
									b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
									b.putString("fromFragment",fromFragment);
									b.putString("vCurrentSeasonUrl", url );
									PlayupLiveApplication.getFragmentManagerUtil().setFragment("FixturesAndResultsFragment",b);

								} else if ( type != null &&  type.equalsIgnoreCase( Types.LEADERBOARD_TYPE ) && !Constants.isFetchingCredentials) {
									///NEED TO DO HREF RELATED WORK HERE , LIKE ABOVE
									///NEED TO DO HREF RELATED WORK HERE , LIKE ABOVE
									HttpRequest request =null;
									if((Boolean) v.getTag(R.id.avtarImage2)){
										String encodedHrefURL	= null;
										String vTokenValue		= 	null;
										
										int tokenType = 	new Util().checkForHrefTokenType(url);
										vTokenValue		=	new Util().checkForHrefTokenParam(url); 
										encodedHrefURL	=	new Util().getPersonalizedEnocodedURL(url,vTokenValue,tokenType);
										
										request = new HttpRequest(  url,encodedHrefURL,true, Constants.GET_METHOD );
									}
									else{
										 request = new HttpRequest(  url , Constants.GET_METHOD );
										 
										 
										 
										 
									}
									try {
										StringBuffer strBuffer = (StringBuffer) request.send();
										
										if(request.getStatusCode() == 401){
											Util.callTheCredentialsUrl(strBuffer);
											

										}
										
									} catch (RequestRepeatException e) {
										Logs.show ( e );
									}

								} else if ( type != null &&  type.equalsIgnoreCase( Types.CONTEST_SET_TYPE )  && !Constants.isFetchingCredentials) {

									HttpRequest request =	null;
									if((Boolean) v.getTag(R.id.avtarImage2)){
										String encodedHrefURL	= null;
										String vTokenValue		= 	null;
										int tokenType = 	new Util().checkForHrefTokenType(url);
										vTokenValue		=	new Util().checkForHrefTokenParam(url); 
										encodedHrefURL	=	new Util().getPersonalizedEnocodedURL(url,vTokenValue,tokenType);
										
										request = new HttpRequest(  url,encodedHrefURL,true, Constants.GET_METHOD );
									}
									else{
										 request = new HttpRequest(  url , Constants.GET_METHOD );
									}
									try {
										StringBuffer strBuffer	=	(StringBuffer) request.send();

										if(request.getStatusCode() == 401){
											Util.callTheCredentialsUrl(strBuffer);
											

										}
									} catch (RequestRepeatException e) {
										Logs.show ( e );
									}
								}else if ( type  != null && type.equalsIgnoreCase( Types.TEAM_SCHEDULE_TYPE ) ) { 
										b.putBoolean("isHref",(Boolean) v.getTag(R.id.avtarImage2));
										b.putString( "vTeamScheduleUrl",v.getTag(R.id.about_txtview).toString() );
										b.putString( "fromFragment",fromFragment );
										PlayupLiveApplication.getFragmentManagerUtil().setFragment("TeamScheduleFragment",b);
										
									} 
								
							} catch (Exception e) {
								Logs.show ( e );
							}
						}
					});
				}
			}

		} catch (Exception e) {
			Logs.show(e);
		} catch ( Error e ) {
			Logs.show( e );
		}
	}




	/**
	 * 
	 * Srolling of list view needs to be monitored, because if list view is
	 * scrolling dont notify the data set. It will slow down the scrolling
	 * effect
	 */

	ListView.OnScrollListener scrollListener1 = new ListView.OnScrollListener() {
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

			// TODO Auto-generated method stub

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

			// TODO Auto-generated method stub

			switch (scrollState) {

			case OnScrollListener.SCROLL_STATE_IDLE:

				isGalleryScrolling = false;

				notifyDataSetChanged();

				break;

			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:

				isGalleryScrolling = true;

				break;

			case OnScrollListener.SCROLL_STATE_FLING:

				isGalleryScrolling = true;

				break;
			}

		}

	};


	
	private OnItemSelectedListener headerGalleryItemListener = new OnItemSelectedListener() {


		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {



			try {
				for(int i = 0;i<getCount();i++){

					if(i == position){

						dots.getChildAt(i).setBackgroundResource(R.drawable.white_dot);
					}

					else{

						dots.getChildAt(i).setBackgroundResource(R.drawable.grey_dot);
					}

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Logs.show(e);
			}


		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}

	};

	
	public void showDots() {
	
			dots.setVisibility( View.VISIBLE );
			dots.removeAllViews();
			int galleryPosition = 0;
			if( headerGallery!=null && headerGallery.getSelectedItemPosition() >=0 )
				galleryPosition = headerGallery.getSelectedItemPosition();
			for( int i = 0;i<getCount();i++){
				
				LayoutParams params = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				ImageView imgView = new ImageView( PlayUpActivity.context );
				params.setMargins(10, 10, 10, 10);		
				imgView.setLayoutParams(params);
				if( galleryPosition == i )
					imgView.setBackgroundResource(R.drawable.white_dot);
				else				
					imgView.setBackgroundResource(R.drawable.grey_dot);
				
				dots.addView ( imgView,i);
			}

	}
	
	
	/**
	 * setting footer data, footer tilte, sub title, time stamp, source icon, social icon ..etc
	 */
	
	private void setFooterData( int position, ViewHolder vh, View convertView, boolean showTimeStamp ) {
		
		if(featuresData.get("vFooterTitle").get(position) != null && featuresData.get("vFooterTitle").get(position).trim().length() > 0){
			vh.footerContent.setVisibility(View.VISIBLE);
			vh.footerTitle.setVisibility(View.VISIBLE);
			vh.footerTitle.setText(featuresData.get("vFooterTitle").get(position));
		}
		
//		if(featuresData.get("vSource").get(position) != null && featuresData.get("vSource").get(position).trim().length() > 0){
//			showOnlyImage = false;
//			vh.footerContent.setVisibility(View.VISIBLE);
//			vh.footerTitle.setVisibility(View.VISIBLE);
//			vh.footerTitle.setText(featuresData.get("vSource").get(position));
//		}

		if(featuresData.get("vSourceIcon").get(position) != null && featuresData.get("vSourceIcon").get(position).trim().length() > 0){
			vh.footerContent.setVisibility(View.VISIBLE);
			vh.sourceIcon.setVisibility(View.VISIBLE);
			imageDownloader.download( featuresData.get("vSourceIcon").get(position) , vh.sourceIcon, false,this, true );
			convertView.setPadding(0,0,0,10);
		}
		if(featuresData.get("vSocialIcon").get(position) != null && featuresData.get("vSocialIcon").get(position).trim().length() > 0){
			vh.footerContent.setVisibility(View.VISIBLE);
			vh.socialIcon.setVisibility(View.VISIBLE);
			imageDownloader.download( featuresData.get("vSocialIcon").get(position) ,vh.socialIcon, false,this, true );

		}
		
		if( showTimeStamp ) {
			if(featuresData.get("vTimeStamp").get(position) != null && featuresData.get("vTimeStamp").get(position).trim().length() > 0){
				vh.footerContent.setVisibility(View.VISIBLE);
				vh.footerSubtitle.setVisibility(View.VISIBLE);
				vh.footerSubtitle.setText(dateUtil.gmt_to_local_timezone(featuresData.get("vTimeStamp").get(position)));
				convertView.setPadding(0,0,0,10);
			}
		} else {
			if(featuresData.get("vFooterSubTitle").get(position) != null && featuresData.get("vFooterSubTitle").get(position).trim().length() > 0){
				vh.footerContent.setVisibility(View.VISIBLE);
				vh.footerSubtitle.setVisibility(View.VISIBLE);
				vh.footerSubtitle.setText(featuresData.get("vFooterSubTitle").get(position));
				convertView.setPadding(0,0,0,10);
			}
		}

	}
	
	/**
	 * settting summary over image
	 */
	private void setSummary ( int position, ViewHolder vh, String vDisplayType ) {
		if(featuresData.get("vSummary").get(position) != null && featuresData.get("vSummary").get(position).trim().length() > 0){

			if( vDisplayType.equalsIgnoreCase(Types.FEATURE_VIDEO) || vDisplayType.equalsIgnoreCase(Types.FEATURE_VIDEO_TIMESTAMP) ) {
				vh.imageSummary.setMaxLines(2);
			} else {
				vh.imageSummary.setMaxLines(3);
			}
			
			vh.imageSummary.setVisibility(View.VISIBLE);
			vh.imageSummary.setText(featuresData.get("vSummary").get(position));
			vh.imageSummary.setTextSize(25);
			vh.imageSummary.setTypeface(Constants.BEBAS_NEUE);
			vh.imageSummary.setShadowLayer(2, 0, 0, Color.parseColor("#000000"));
			vh.imageSummary.setTextColor(Color.parseColor("#FFFFFF"));

			if(featuresData.get("vBackgroundColor").get( position )  != null && 
					featuresData.get("vBackgroundColor").get( position ) .trim().length() > 0 ) {
				if(featuresData.get("vImageUrl").get(position) == null || featuresData.get("vImageUrl").get(position).trim().length() == 0 ) {
					vh.imageSummary.setShadowLayer(0, 0, 0, 0);
					vh.imageSummary.setTextColor(Color.parseColor("#696B6E"));
				}
			}
		}
	}
	
	
	/**
	 * setting background color for images
	 */
	private void setBackGroundColor( int position, View view) {
		try {
			if(featuresData.get("vBackgroundColor").get(position) != null && 
					featuresData.get("vBackgroundColor").get(position).trim().length() > 0)
			{
				String bgColor = featuresData.get("vBackgroundColor").get(position);
				bgColor = bgColor.replace("0x", "");
				view.setBackgroundColor(Color.parseColor("#"+bgColor));

			} else {
				view.setBackgroundColor( Color.GRAY );
			}
		} catch (Exception e) {
			Logs.show(e);
			
			view.setBackgroundColor( Color.GRAY );
		}
		
	}
	
	
	private boolean isSupportedDisplayType( String displayType ) {
		
		if( displayType.equalsIgnoreCase(Types.FEATURE_HIGHLIGHT) ||
				displayType.equalsIgnoreCase(Types.FEATURE_IMAGE) ||
				displayType.equalsIgnoreCase(Types.FEATURE_PHOTO) ||
				displayType.equalsIgnoreCase(Types.FEATURE_TIMESTAMP) ||
				displayType.equalsIgnoreCase(Types.FEATURE_VIDEO) ||
				displayType.equalsIgnoreCase(Types.FEATURE_VIDEO_TIMESTAMP) ) {
			return true;
		}
		
			return false;
	}



}


