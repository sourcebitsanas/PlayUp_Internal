package com.playup.android.adapters;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.activity.VideoActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.fragment.CreateRoomFragment;
import com.playup.android.fragment.FixturesAndResultsFragment;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.ImageDownloaderSports;

import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class MatchRoomGridAdapter extends BaseAdapter implements OnTouchListener  {

	private LayoutInflater inflater;
	private   ArrayList< MatchRoomMessage >  data;

	private DateUtil dateUtil = new DateUtil();

	private DatabaseUtil dbUtil = null;
	private ImageDownloader imageDownloader = new ImageDownloader();

	private int items = 0;
	ImageDownloaderSports imageDownloaderSports = null;

	private String vContestId = null;
	private boolean isDummy = false;
	private GridView roomBase;
	private android.view.ViewGroup.LayoutParams viewLayoutParams;
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	
	private String vSecColor = null;
	private String vSecTitleColor = null;



	public MatchRoomGridAdapter ( ArrayList<MatchRoomMessage >  data, String vContestId,GridView roomBase, 
			String vMainColor, String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		
		if ( this.data != null ) {
			this.data.clear();
			this.data = null;
		}
		
		this.vMainColor = vMainColor;		
		this.vMainTitleColor = vMainTitleColor;
		
		
		this.vContestId = vContestId;
		this.data = data;
		this.isDummy = false;
		this.roomBase=	roomBase;
		this.roomBase.setOnTouchListener(this);
		if(dbUtil == null)
			dbUtil = DatabaseUtil.getInstance();
		if(imageDownloaderSports == null)
			imageDownloaderSports = new ImageDownloaderSports();

		if (  PlayUpActivity.context != null ) {
			inflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
	}


//	public MatchRoomGridAdapter ( ArrayList<MatchRoomMessage> data , int items, String vContestId,GridView roomBase ) {
//
//		//		if ( this.data != null ) {
//		//			this.data.clear();
//		//			this.data = null;
//		//		}
//		this.vContestId = vContestId;
//		this.data = data;
//		this.items = items;
//		isDummy = true;
//		if(dbUtil == null)
//			dbUtil = DatabaseUtil.getInstance();
//		if(imageDownloaderSports == null)
//			imageDownloaderSports = new ImageDownloaderSports();
//		this.roomBase=	roomBase;
//		this.roomBase.setOnTouchListener(this);
//		if (  PlayUpActivity.context != null ) {
//			inflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		}
//	}



	public void setData ( ArrayList<MatchRoomMessage> data, String vContestId,GridView roomBase,
			String vMainColor, String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		

		//		if ( this.data != null ) {
		//			this.data.clear();
		//			this.data = null;
		//		}
		
		this.vMainColor = vMainColor;		
		this.vMainTitleColor = vMainTitleColor;
		
		this.vContestId = vContestId;
		this.data = data;
		if(dbUtil == null)
			dbUtil = DatabaseUtil.getInstance();
		if(imageDownloaderSports == null)
			imageDownloaderSports = new ImageDownloaderSports();
		this.isDummy = false;
		this.roomBase=	roomBase;
		this.roomBase.setOnTouchListener(this);
		notifyDataSetChanged();
	}


//	public void setData ( ArrayList<MatchRoomMessage> data,  int items, String vContestId ,GridView roomBase  ) {
//
//		//		if ( this.data != null ) {
//		//			this.data.clear();
//		//			this.data = null;
//		//		}
//		
//		
//		this.vContestId = vContestId;
//		this.data = data;
//		this.items = items;
//		if(dbUtil == null)
//			dbUtil = DatabaseUtil.getInstance();
//		if(imageDownloaderSports == null)
//			imageDownloaderSports = new ImageDownloaderSports();
//		isDummy = true;
//		this.roomBase	=	 roomBase;
//		this.roomBase.setOnTouchListener(this);
//		notifyDataSetChanged();
//	}



	@Override
	public int getCount() {


		if ( isDummy ) {
			return items + 1;
		}

		if ( data != null && data.size() > 0 ) {
			if ( data.size() % 2 == 0  ) {
				return ( data.size() )/2   + 1;
			} else {
				return ( data.size() + 1)/2   + 1;
			}
		}
		return 1;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}


	class ViewHolder {

		LinearLayout item_first;
		LinearLayout item_others;

		RelativeLayout item_placeholder;
		LinearLayout item_others_1;
		LinearLayout item_others_2;
		LinearLayout thirdPartyTile1;
		LinearLayout thirdPartyTile2;

		ImageView userAvatarHolder;
		TextView first_itemTitle; 
		TextView first_commentingNumber;		
		TextView first_uName; 
		TextView first_comment_desc;
		TextView first_commentTime;
		ImageView first_userImage;

		TextView other_1_itemTitle; 
		TextView other_1_commentingNumber;		
		TextView other_1_uName; 
		TextView other_1_comment_desc;
		TextView other_1_commentTime;

		TextView other_2_itemTitle; 
		TextView other_2_commentingNumber;		
		TextView other_2_uName; 
		TextView other_2_comment_desc;
		TextView other_2_commentTime;
		TextView hangoutText;



		LinearLayout matchHighLights1;
		RelativeLayout imageWithSummary1;


		TextView imageSummary1;
		ImageView playerIcon1;
		LinearLayout footerContent1;
		TextView footerTitle1;
		TextView footerSubtitle1;
		ImageView sourceIcon1;
		ImageView socialIcon1;
		ImageView onlyImage1;


		TextView tileName1;
		TextView liveImage1;
		RelativeLayout topLayout1;
		RelativeLayout titleLayout1;


		LinearLayout matchHighLights2;
		RelativeLayout imageWithSummary2;


		TextView imageSummary2;
		ImageView playerIcon2;
		LinearLayout footerContent2;
		TextView footerTitle2;
		TextView footerSubtitle2;
		ImageView sourceIcon2;
		ImageView socialIcon2;
		ImageView onlyImage2;


		TextView tileName2;
		TextView liveImage2;
		RelativeLayout topLayout2;
		RelativeLayout titleLayout2;

		RelativeLayout euro_tile_bg1;
		RelativeLayout euro_tile_bg2;

		ImageView ImageWithSummaryImage1;
		ImageView ImageWithSummaryImage2;

		LinearLayout social_sports_1;
		LinearLayout social_sports_2;
		
		View solidImageOverlay1;
		View solidImageOverlay2;



	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if ( inflater == null ) {
			LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return  layoutInflater.inflate( R.layout.match_room_item_main, null);
		}
		ViewHolder vh = null;

		if(convertView == null ) {

			vh = new ViewHolder() ;

			convertView = inflater.inflate( R.layout.match_room_item_main, null); 		 


			vh.item_first = (LinearLayout) convertView.findViewById( R.id.item_first );
			vh.item_others = (LinearLayout) convertView.findViewById( R.id.item_others );
			vh.userAvatarHolder	=	(ImageView)convertView.findViewById(R.id.userAvatarHolder);


			vh.social_sports_1 = ( LinearLayout ) vh.item_others.findViewById( R.id.social_sports_1 );
			vh.social_sports_2 = ( LinearLayout ) vh.item_others.findViewById( R.id.social_sports_2 );


			vh.item_others_1 = (LinearLayout) vh.item_others.findViewById( R.id.item_others_1 );
			vh.item_others_2 = (LinearLayout) vh.item_others.findViewById( R.id.item_others_2 );
			vh.item_placeholder = (RelativeLayout) vh.item_others.findViewById( R.id.item_placeholder );

			vh.thirdPartyTile1 = (LinearLayout) vh.item_others.findViewById( R.id.thirdPartyTile1 );
			vh.thirdPartyTile2 = (LinearLayout) vh.item_others.findViewById( R.id.thirdPartyTile2);


			vh. first_itemTitle = (TextView) vh.item_first.findViewById(R.id.itemTitle);
			vh. first_commentingNumber = (TextView) vh.item_first.findViewById(R.id.commentingNumber);		
			vh. first_uName = (TextView) vh.item_first.findViewById(R.id.uName);
			vh. first_comment_desc = (TextView) vh.item_first.findViewById(R.id.comment_desc);
			vh. first_commentTime = (TextView) vh.item_first.findViewById(R.id.commentTime);		
			vh. first_userImage = ( ImageView ) vh.item_first.findViewById(R.id.userAvatar );		

			vh. other_1_itemTitle = (TextView) vh.item_others_1.findViewById(R.id.itemTitle);
			vh. other_1_commentingNumber = (TextView) vh.item_others_1.findViewById(R.id.commentingNumber);		
			vh. other_1_uName = (TextView) vh.item_others_1.findViewById(R.id.uName);
			vh. other_1_comment_desc = (TextView) vh.item_others_1.findViewById(R.id.comment_desc);
			vh. other_1_commentTime = (TextView) vh.item_others_1.findViewById(R.id.commentTime);		

			vh. other_2_itemTitle = (TextView) vh.item_others_2.findViewById(R.id.itemTitle);
			vh. other_2_commentingNumber = (TextView) vh.item_others_2.findViewById(R.id.commentingNumber);		
			vh. other_2_uName = (TextView) vh.item_others_2.findViewById(R.id.uName);
			vh. other_2_comment_desc = (TextView) vh.item_others_2.findViewById(R.id.comment_desc);
			vh. other_2_commentTime = (TextView) vh.item_others_2.findViewById(R.id.commentTime);		

			vh. hangoutText = (TextView) vh.item_placeholder.findViewById(R.id.hangoutText);	


			vh.playerIcon1 = (ImageView) vh.thirdPartyTile1.findViewById(R.id.playerIcon);	
			vh.imageWithSummary1 = (RelativeLayout) vh.thirdPartyTile1.findViewById(R.id.ImageWithSummary);
			vh.ImageWithSummaryImage1 = ( ImageView ) vh.imageWithSummary1.findViewById(R.id.ImageWithSummaryImage);
			vh.solidImageOverlay1 = ( View ) vh.imageWithSummary1.findViewById(R.id.solidImageOverlay);
			vh.titleLayout1 = (RelativeLayout)vh.thirdPartyTile1.findViewById(R.id.titleLayout);			
			vh.topLayout1 = (RelativeLayout) vh.thirdPartyTile1.findViewById(R.id.topLayout);		
			vh.imageSummary1 = (TextView) vh.thirdPartyTile1.findViewById(R.id.ImageSummary);				
			vh.tileName1 = 	(TextView)	vh.thirdPartyTile1.findViewById(R.id.tileName);	
			vh.footerContent1 = (LinearLayout) vh.thirdPartyTile1.findViewById(R.id.footerContent);				
			vh.footerTitle1 = (TextView) vh.thirdPartyTile1.findViewById(R.id.footerTitle);				
			vh.footerSubtitle1 = (TextView) vh.thirdPartyTile1.findViewById(R.id.footerSubtitle);				
			vh.sourceIcon1 = (ImageView) vh.thirdPartyTile1.findViewById(R.id.sourceIcon);				
			vh.socialIcon1 = (ImageView) vh.thirdPartyTile1.findViewById(R.id.socialIcon);				
			vh.onlyImage1 = (ImageView) vh.thirdPartyTile1.findViewById(R.id.onlyImage);
			vh.liveImage1 = (TextView)	vh.thirdPartyTile1.findViewById(R.id.liveImage);
			vh.euro_tile_bg1 = (RelativeLayout)vh.thirdPartyTile1.findViewById(R.id.euro_tile_bg );			

			vh.playerIcon2 = (ImageView) vh.thirdPartyTile2.findViewById(R.id.playerIcon);	
			vh.imageWithSummary2 = (RelativeLayout) vh.thirdPartyTile2.findViewById(R.id.ImageWithSummary);		
			vh.ImageWithSummaryImage2 = ( ImageView ) vh.imageWithSummary2.findViewById(R.id.ImageWithSummaryImage);
			vh.solidImageOverlay2 = ( View ) vh.imageWithSummary2.findViewById(R.id.solidImageOverlay);
			vh.titleLayout2 = (RelativeLayout)vh.thirdPartyTile2.findViewById(R.id.titleLayout);			
			vh.topLayout2 = (RelativeLayout) vh.thirdPartyTile2.findViewById(R.id.topLayout);		
			vh.imageSummary2 = (TextView) vh.thirdPartyTile2.findViewById(R.id.ImageSummary);				
			vh.tileName2 = 	(TextView)	vh.thirdPartyTile2.findViewById(R.id.tileName);	
			vh.footerContent2 = (LinearLayout) vh.thirdPartyTile2.findViewById(R.id.footerContent);				
			vh.footerTitle2 = (TextView) vh.thirdPartyTile2.findViewById(R.id.footerTitle);				
			vh.footerSubtitle2 = (TextView) vh.thirdPartyTile2.findViewById(R.id.footerSubtitle);				
			vh.sourceIcon2 = (ImageView) vh.thirdPartyTile2.findViewById(R.id.sourceIcon);				
			vh.socialIcon2 = (ImageView) vh.thirdPartyTile2.findViewById(R.id.socialIcon);				
			vh.onlyImage2 = (ImageView) vh.thirdPartyTile2.findViewById(R.id.onlyImage);
			vh.liveImage2 = (TextView)	vh.thirdPartyTile2.findViewById(R.id.liveImage);
			vh.euro_tile_bg2 = (RelativeLayout)vh.thirdPartyTile2.findViewById(R.id.euro_tile_bg );	

			vh.tileName1.setTextColor(Color.parseColor("#404040"));
			vh.tileName1.setTypeface(Constants.OPEN_SANS_BOLD);

			vh.tileName2.setTextColor(Color.parseColor("#404040"));
			vh.tileName2.setTypeface(Constants.OPEN_SANS_BOLD);

			setTypeFaces ( vh.first_itemTitle, vh.first_commentingNumber, vh.first_uName,vh. first_comment_desc ,  vh.first_commentTime );
			setTypeFaces ( vh.other_1_itemTitle, vh.other_1_commentingNumber, vh.other_1_uName,vh. other_1_comment_desc ,  vh.other_1_commentTime );
			setTypeFaces ( vh.other_2_itemTitle, vh.other_2_commentingNumber, vh.other_2_uName, vh. other_1_comment_desc , vh.other_2_commentTime );
			vh. hangoutText.setTypeface(Constants.OPEN_SANS_BOLD);

			convertView.setTag( vh );
		} else {
			vh = (ViewHolder) convertView.getTag();
		}


		vh.social_sports_1.setVisibility( View.GONE );
		vh.social_sports_2.setVisibility( View.GONE );

		if( position == 0 ) 
			convertView.setPadding(8,10,8,0);
		else if (position == (getCount() -1))
			convertView.setPadding(8,0,8,8);
		else
			convertView.setPadding(8,0,8,0);


		/**
		 * Removing visibility and listeners
		 * to avoid conflict while scrolling
		 */


		vh.imageWithSummary1.setBackgroundColor( Color.TRANSPARENT );
	    vh.imageWithSummary2.setBackgroundColor( Color.TRANSPARENT );
	    
		vh.ImageWithSummaryImage1.setVisibility( View.GONE);
	    vh.ImageWithSummaryImage2.setVisibility( View.GONE);

		vh.euro_tile_bg1.setBackgroundColor(Color.TRANSPARENT);
		vh.euro_tile_bg2.setBackgroundColor(Color.TRANSPARENT);


		vh.item_placeholder.setVisibility(View.GONE);
		vh.item_others_1.setVisibility(View.GONE);
		vh.item_others_2.setVisibility(View.GONE);
		vh.item_first.setVisibility(View.GONE);

		vh.thirdPartyTile1.setOnTouchListener(null);
		vh.thirdPartyTile2.setOnTouchListener(null);
		vh.item_placeholder.setOnTouchListener(null);
		vh.item_others_1.setOnTouchListener(null);
		vh.item_others_2.setOnTouchListener(null);
		vh.item_first.setOnTouchListener(null);

		vh.footerTitle1.setTextColor(Color.parseColor("#FFFFFF"));
		vh.footerSubtitle1.setTextColor(Color.parseColor("#FFFFFF"));
		vh.imageSummary1.setTextColor(Color.parseColor("#FFFFFF"));
		vh.footerTitle1.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		vh.footerSubtitle1.setTypeface(Constants.OPEN_SANS_REGULAR);
		vh.imageSummary1.setTypeface(Constants.OPEN_SANS_REGULAR);

		vh.footerTitle2.setTextColor(Color.parseColor("#FFFFFF"));
		vh.footerSubtitle2.setTextColor(Color.parseColor("#FFFFFF"));
		vh.imageSummary2.setTextColor(Color.parseColor("#FFFFFF"));
		vh.footerTitle2.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		vh.footerSubtitle2.setTypeface(Constants.OPEN_SANS_REGULAR);
		vh.imageSummary2.setTypeface(Constants.OPEN_SANS_REGULAR);

		vh.imageSummary1.setText("");
		vh.footerTitle1.setText("");
		vh.footerSubtitle1.setText("");
		
		vh.imageSummary2.setText("");
		vh.footerTitle2.setText("");
		vh.footerSubtitle2.setText("");
		



		if ( getCount() == 1 ) {

			// show only placeholder			
			vh.item_first.setVisibility(View.GONE);
			vh.item_others.setVisibility(View.VISIBLE);			
			vh.item_others_1.setVisibility(View.GONE);
			vh.item_others_2.setVisibility(View.GONE );
			vh.social_sports_2.setVisibility(View.GONE );
			vh.item_placeholder.setVisibility(View.VISIBLE);

			vh.social_sports_2.setVisibility( View.VISIBLE );

			vh.item_placeholder.setTag(R.id.about_txtview, -1);
			vh.item_placeholder.setTag(0);
			vh.item_placeholder.setOnTouchListener(this);


			convertView.setTag(R.id.about_txtview, -1);

			return convertView;
		} else {

			if ( position == 0 ) {
				// show first
				// show place holder
				vh.item_first.setVisibility(View.VISIBLE);
				vh.item_others.setVisibility(View.GONE);

				MatchRoomMessage m = data.get(position);

				vh.first_itemTitle.setText( new Util().getSmiledText( m.conversationName ) );
				vh.first_commentTime.setText(m.postedTime);
				vh.first_uName.setText( new Util().getSmiledText(m.postedBy));
				vh.first_comment_desc.setText( new Util().getSmiledText(m.message));
				vh.item_first.setTag(R.id.about_txtview, m.conversationId);
				vh.item_first.setTag(1);
				vh.item_first.setOnTouchListener(this);

				if ( m.imageUrl != null ) {
					vh.first_userImage.setBackgroundColor(Color.WHITE);
					imageDownloader.download(m.imageUrl, vh.first_userImage, true, this);
				}
				vh.item_first.setTag(R.id.activeUserMainLayout, m.isThirdPartyTile );
				vh.first_comment_desc.setTypeface(Constants.OPEN_SANS_LIGHT);

				convertView.setTag(R.id.about_txtview, m.conversationId );


				if ( m.totalMessages == null ) {
					vh.first_uName.setText( R.string.loveSports);
					vh.first_comment_desc.setText( R.string.inviteAndHangout);

					vh.first_commentingNumber.setVisibility(View.INVISIBLE);
				} else if ( m.totalMessages.trim().length() == 0 ) {
					vh.first_uName.setText( R.string.loveSports);
					vh.first_comment_desc.setText( R.string.inviteAndHangout);

					vh.first_commentingNumber.setVisibility(View.INVISIBLE);
				} else {
					vh.first_commentingNumber.setVisibility(View.VISIBLE);
					vh.first_commentingNumber.setText(m.totalMessages);
				}


				return convertView;
			}
			if ( position == 1 ) {

				// show only placeholder
				vh.item_first.setVisibility(View.GONE);
				vh.item_others.setVisibility(View.VISIBLE);

				vh.item_placeholder.setVisibility(View.VISIBLE);

				vh.social_sports_2.setVisibility( View.VISIBLE );

				vh.item_others_1.setVisibility(View.GONE );

				vh.thirdPartyTile1.setVisibility(View.GONE);
				vh.thirdPartyTile2.setVisibility(View.GONE);
				vh.item_others_2.setVisibility(View.GONE);

				vh.item_placeholder.setTag(R.id.about_txtview, -1);
				vh.item_placeholder.setTag(0);
				vh.item_placeholder.setTag(R.id.activeUserMainLayout, false);
				vh.item_placeholder.setOnTouchListener(this);

				if ( data != null && data.size() > 1 ) {

					vh.social_sports_1.setVisibility( View.GONE );
					vh.social_sports_2.setVisibility( View.GONE );

					MatchRoomMessage m = data.get(position);

					if ( m.isThirdPartyTile ) {
						vh.item_others_1.setVisibility(View.GONE);
						vh.thirdPartyTile1.setVisibility(View.GONE );
						vh.thirdPartyTile2.setVisibility(View.VISIBLE);
						vh.thirdPartyTile2.setTag(4);
						vh.thirdPartyTile2.setOnTouchListener(this);						
						vh.thirdPartyTile2.setTag(R.id.activeUserMainLayout, m.isThirdPartyTile);
						setThirdPartyTilesData ( vh, false, m);

					}else{

						vh.item_others_1.setVisibility(View.VISIBLE);
						vh.thirdPartyTile1.setVisibility(View.GONE);

						vh.other_1_itemTitle.setText( new Util().getSmiledText( m.conversationName));
						vh.other_1_commentTime.setText(m.postedTime);
						vh.other_1_uName.setText( new Util().getSmiledText( m.postedBy ));
						vh.other_1_comment_desc.setText( new Util().getSmiledText( m.message));


						vh.item_others_1.setTag(R.id.about_txtview, m.conversationId );
						vh.item_others_1.setTag( 2 );
						vh.item_others_1.setTag(R.id.activeUserMainLayout, m.isThirdPartyTile );

						vh.item_others_1.setOnTouchListener( this );					
						convertView.setTag(R.id.about_txtview, m.conversationId );


						if ( m.totalMessages == null ) {
							vh.other_1_commentingNumber.setVisibility(View.INVISIBLE);
							vh.other_1_uName.setText( R.string.loveSports);
							vh.other_1_comment_desc.setText( R.string.inviteAndHangout);
						} else if ( m.totalMessages.trim().length() == 0 ) {
							vh.other_1_commentingNumber.setVisibility(View.INVISIBLE);
							vh.other_1_uName.setText( R.string.loveSports);
							vh.other_1_comment_desc.setText( R.string.inviteAndHangout);
						} else {
							vh.other_1_commentingNumber.setVisibility(View.VISIBLE);
							vh.other_1_commentingNumber.setText(m.totalMessages);
						}

					}



				}
				return convertView;
			} 


			vh.item_first.setVisibility( View.GONE );
			vh.item_others.setVisibility( View.VISIBLE );



			vh.item_placeholder.setVisibility( View.GONE );



			MatchRoomMessage m = data.get( position * 2 - 2 );

			if(m.isThirdPartyTile){
				
				
				
				vh.thirdPartyTile1.setVisibility(View.VISIBLE);
				vh.thirdPartyTile2.setVisibility( View.GONE );
				
				vh.item_others_1.setVisibility( View.GONE );
				vh.item_others_2.setVisibility( View.GONE );

				vh.social_sports_2.setVisibility( View.VISIBLE );
				vh.thirdPartyTile1.setTag(4);
				vh.thirdPartyTile1.setTag(R.id.activeUserMainLayout, m.isThirdPartyTile);
				vh.thirdPartyTile1.setOnTouchListener(this);

				setThirdPartyTilesData(vh,true,m);
			}
			else{
				vh.thirdPartyTile1.setVisibility(View.GONE);
				vh.thirdPartyTile2.setVisibility(View.GONE);
				
				vh.item_others_1.setVisibility( View.VISIBLE );
				vh.item_others_2.setVisibility( View.GONE );


				vh.social_sports_2.setVisibility( View.VISIBLE );

				vh.other_1_itemTitle.setText( new Util() .getSmiledText( m.conversationName ));
				vh.other_1_commentTime.setText(m.postedTime);
				vh.other_1_uName.setText( new Util().getSmiledText(m.postedBy));

				vh.other_1_comment_desc.setText( new Util().getSmiledText(m.message));
				vh.item_others_1.setTag(R.id.about_txtview, m.conversationId );
				vh.item_others_1.setTag( 2 );
				vh.item_others_1.setTag(R.id.activeUserMainLayout, m.isThirdPartyTile );
				vh.item_others_1.setOnTouchListener ( this );			
				convertView.setTag(R.id.about_txtview, m.conversationId );

				if ( m.totalMessages == null ) {
					vh.other_1_commentingNumber.setVisibility( View.INVISIBLE );
					vh.other_1_uName.setText( R.string.loveSports);
					vh.other_1_comment_desc.setText( R.string.inviteAndHangout);
				} else if ( m.totalMessages.trim().length() == 0 ) {
					vh.other_1_commentingNumber.setVisibility( View.INVISIBLE );
					vh.other_1_uName.setText( R.string.loveSports);
					vh.other_1_comment_desc.setText( R.string.inviteAndHangout);
				} else {
					vh.other_1_commentingNumber.setVisibility(View.VISIBLE);
					vh.other_1_commentingNumber.setText(m.totalMessages);
				}
			}
			if ( data.size() > position * 2 - 1 ) {

				m = data.get(position * 2 - 1);
				if(m.isThirdPartyTile){

					vh.social_sports_2.setVisibility( View.GONE );

					vh.thirdPartyTile2.setVisibility(View.VISIBLE);
					vh.thirdPartyTile2.setTag(4);
					vh.thirdPartyTile2.setTag(R.id.activeUserMainLayout, m.isThirdPartyTile);
					vh.thirdPartyTile2.setOnTouchListener(this);
					vh.item_others_2.setVisibility( View.GONE );
					setThirdPartyTilesData(vh,false,m);


				}else{
					vh.thirdPartyTile2.setVisibility(View.GONE);

					vh.social_sports_2.setVisibility( View.GONE );
					vh.item_others_2.setVisibility(View.VISIBLE);

					vh.other_2_itemTitle.setText( new Util().getSmiledText(m.conversationName));
					vh.other_2_commentTime.setText(m.postedTime);
					vh.other_2_uName.setText( new Util().getSmiledText(m.postedBy));

					vh.other_2_comment_desc.setText( new Util().getSmiledText(m.message));

					vh.item_others_2.setTag( 2 );
					vh.item_others_2.setTag(R.id.about_txtview, m.conversationId );
					vh.item_others_2.setTag(R.id.activeUserMainLayout, m.isThirdPartyTile );
					vh.item_others_2.setOnTouchListener ( this );				
					convertView.setTag(R.id.about_txtview, m.conversationId );

					if ( m.totalMessages == null ) {
						vh.other_2_commentingNumber.setVisibility( View.INVISIBLE );
						vh.other_2_uName.setText( R.string.loveSports);
						vh.other_2_comment_desc.setText( R.string.inviteAndHangout);
					} else if ( m.totalMessages.trim().length() == 0 ) {
						vh.other_2_commentingNumber.setVisibility( View.INVISIBLE );
						vh.other_2_uName.setText( R.string.loveSports);
						vh.other_2_comment_desc.setText( R.string.inviteAndHangout);
					} else {
						vh.other_2_commentingNumber.setVisibility(View.VISIBLE);
						vh.other_2_commentingNumber.setText(m.totalMessages);
					}

				}

			}

		}
		return convertView;

	}

	/**
	 * Setting the type faces 
	 */


	private void setTypeFaces  ( TextView itemTitle, TextView commentingNumber, TextView uName,TextView  comment_desc,TextView commentTime) {

		itemTitle.setTypeface(Constants.OPEN_SANS_BOLD);
		commentingNumber.setTypeface(Constants.OPEN_SANS_REGULAR);

		uName.setTypeface(Constants.OPEN_SANS_BOLD);
		comment_desc.setTypeface(Constants.OPEN_SANS_REGULAR);
		commentTime.setTypeface(Constants.OPEN_SANS_REGULAR);

	}


	private void setThirdPartyTilesData ( ViewHolder vh,boolean isLeftSided, MatchRoomMessage m) {

		try {
			if ( isLeftSided ) {
				
				vh.imageSummary1.setShadowLayer(0, 0, 0, 0);
				vh.footerTitle1.setShadowLayer(0, 0, 0, 0);
				vh.footerSubtitle1.setShadowLayer(0, 0, 0, 0);
	
				
				
				
				if(m.vDisplayType.equalsIgnoreCase(Types.TILE_TIMESTAMP)){
					

					vh.footerContent1.setVisibility(View.VISIBLE);
					
					if(m.vSource != null && m.vSource.trim().length() > 0){
						
						vh.footerTitle1.setVisibility(View.VISIBLE);
						vh.footerTitle1.setText(m.vSource);
					}
					
					if(m.vTimeStamp != null && m.vTimeStamp.trim().length() > 0){

						
						vh.footerSubtitle1.setVisibility(View.VISIBLE);
						vh.footerSubtitle1.setText(dateUtil.gmt_to_local_timezone_tiles(m.vTimeStamp));

					}
					
					if(m.vSourceIcon != null && m.vSourceIcon.trim().length() > 0 ) {
						
						vh.sourceIcon1.setVisibility(View.VISIBLE);
						imageDownloader.download( m.vSourceIcon, vh.sourceIcon1, false,null);
					}
					
					

					if(m.vSocialIcon != null && m.vSocialIcon.trim().length() > 0){
						
						vh.socialIcon1.setVisibility(View.VISIBLE);
						imageDownloader.download( m.vSocialIcon ,vh.socialIcon1, false,null );
					}
					
					
					
					if(m.vTitle != null && m.vTitle.trim().length() > 0){
						
						vh.tileName1.setVisibility(View.VISIBLE);
						
						
						String titleName = m.vTitle.toUpperCase();

						if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
							if(titleName.length() > 25 ) {
								titleName = titleName.substring(0,22) +"...";
							}
						}
						
						vh.tileName1.setText( titleName );
					}
					
					if(m.vSummary != null && 
							m.vSummary.trim().length() > 0){
						vh.imageSummary1.setVisibility(View.VISIBLE);
						vh.imageSummary1.setText(m.vSummary);		
					}
					
					setImageAndBackgroundColor(m, vh, isLeftSided );
					
					if((m.vLinkUrl != null && m.vLinkUrl.trim().length() > 0) || (m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0)){
						
						if(m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0){
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vLinkHrefUrl);
						}else{
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vLinkUrl);
						}

					//	vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vLinkUrl);
						vh.thirdPartyTile1.setTag(R.id.active_users_text,m.vLinkType);
						
					}else if(m.vContentType != null && 
							m.vContentType.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){

					/*	vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vContentUrl);*/
						if(m.vContentHrefUrl!=null && m.vContentHrefUrl.trim().length()>0){
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vContentHrefUrl);
						}else if(m.vContentUrl!=null && m.vContentUrl.trim().length()>0){
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vContentUrl);
						}
						vh.thirdPartyTile1.setTag(R.id.active_users_text,m.vContentType);
						vh.thirdPartyTile1.setTag(R.id.aboutText,m.vContentId);
						

					}
					
					

					
				}else if(m.vDisplayType.equalsIgnoreCase(Types.TILE_HEADLINE)){
					vh.socialIcon1.setVisibility(View.GONE);
					vh.sourceIcon1.setVisibility(View.GONE);
					
					if(m.vFooterTitle != null && m.vFooterTitle.trim().length() > 0){
						vh.footerContent1.setVisibility(View.VISIBLE);
						vh.footerTitle1.setVisibility(View.VISIBLE);
						vh.footerTitle1.setText(m.vFooterTitle);
					}
					
					if(m.vFooterSubTitle != null && m.vFooterSubTitle.trim().length() > 0){

						vh.footerContent1.setVisibility(View.VISIBLE);
						vh.footerSubtitle1.setVisibility(View.VISIBLE);
						vh.footerSubtitle1.setText(m.vFooterSubTitle);

					}
					
					if(m.vTitle != null && m.vTitle.trim().length() > 0){
						
						vh.tileName1.setVisibility(View.VISIBLE);
						
						
						String titleName = m.vTitle.toUpperCase();

						if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
							if(titleName.length() > 25 ) {
								titleName = titleName.substring(0,22) +"...";
							}
						}
					
						vh.tileName1.setText( titleName );
					}
					
					setFooterStyles( false, vh, isLeftSided,  m);
					
					setImageAndBackgroundColor(m, vh, isLeftSided );
					
				/*	if(m.vLinkUrl != null && 
							m.vLinkUrl.trim().length() > 0){*/
						if((m.vLinkUrl != null && m.vLinkUrl.trim().length() > 0) || 
								(m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0)){
							
							
							if(m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0){
								vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vLinkHrefUrl);
							}else{
								vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vLinkUrl);
							}
							
							
						//vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vLinkUrl);
						vh.thirdPartyTile1.setTag(R.id.active_users_text,m.vLinkType);
						
					}else if(m.vContentType != null && 
							m.vContentType.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){
						
						if(m.vContentHrefUrl!=null && m.vContentHrefUrl.trim().length()>0){
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vContentHrefUrl);
						}else if(m.vContentUrl!=null && m.vContentUrl.trim().length()>0){
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vContentUrl);
						}
						//vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vContentUrl);
						vh.thirdPartyTile1.setTag(R.id.active_users_text,m.vContentType);
						vh.thirdPartyTile1.setTag(R.id.aboutText,m.vContentId);
						

					}
					
					
					
					
				}else if(m.vDisplayType.equalsIgnoreCase(Types.TILE_PHOTO)){
					
					vh.socialIcon1.setVisibility(View.GONE);
					vh.sourceIcon1.setVisibility(View.GONE);
					
					
					if(m.vFooterTitle != null && m.vFooterTitle.trim().length() > 0){
						vh.footerContent1.setVisibility(View.VISIBLE);
						vh.footerTitle1.setVisibility(View.VISIBLE);
						vh.footerTitle1.setText(m.vFooterTitle);
					}
					
					if(m.vFooterSubTitle != null && m.vFooterSubTitle.trim().length() > 0){

						vh.footerContent1.setVisibility(View.VISIBLE);
						vh.footerSubtitle1.setVisibility(View.VISIBLE);
						vh.footerSubtitle1.setText(m.vFooterSubTitle);

					}
					
					if(m.vTitle != null && m.vTitle.trim().length() > 0){
						
						vh.tileName1.setVisibility(View.VISIBLE);
						
						
						String titleName = m.vTitle.toUpperCase();

						if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
							if(titleName.length() > 25 ) {
								titleName = titleName.substring(0,22) +"...";
							}
						}
						
						vh.tileName1.setText( titleName );
					}
					
					
					if(m.iLive != null && Integer.parseInt(m.iLive) == 1)
						vh.liveImage1.setVisibility(View.VISIBLE);
					
					setFooterStyles( false, vh, isLeftSided,  m);
					
					setImageAndBackgroundColor(m, vh, isLeftSided );
					
				//	if(m.vLinkUrl != null && 
					//		m.vLinkUrl.trim().length() > 0){
						if((m.vLinkUrl != null && m.vLinkUrl.trim().length() > 0)
								|| (m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0)){
							
							
							if(m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0){
								vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vLinkHrefUrl);
							}else{
								vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vLinkUrl);
							}
							
							

						//vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vLinkUrl);
						vh.thirdPartyTile1.setTag(R.id.active_users_text,m.vLinkType);
						
					}else if(m.vContentType != null && 
							m.vContentType.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){
						
						if(m.vContentHrefUrl!=null && m.vContentHrefUrl.trim().length()>0){
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vContentHrefUrl);
						}else if(m.vContentUrl!=null && m.vContentUrl.trim().length()>0){
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vContentUrl);
						}
					//	vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vContentUrl);
						vh.thirdPartyTile1.setTag(R.id.active_users_text,m.vContentType);
						vh.thirdPartyTile1.setTag(R.id.aboutText,m.vContentId);
						

					}
					
					
				}else if(m.vDisplayType.equalsIgnoreCase(Types.TILE_SOLID)){
					
					
					vh.socialIcon1.setVisibility(View.GONE);
					vh.sourceIcon1.setVisibility(View.GONE);
					
					
					if(m.vFooterTitle != null && m.vFooterTitle.trim().length() > 0){
						vh.footerContent1.setVisibility(View.VISIBLE);
						vh.footerTitle1.setVisibility(View.VISIBLE);
						vh.footerTitle1.setText(m.vFooterTitle);
					}
					
					if(m.vFooterSubTitle != null && m.vFooterSubTitle.trim().length() > 0){

						vh.footerContent1.setVisibility(View.VISIBLE);
						vh.footerSubtitle1.setVisibility(View.VISIBLE);
						vh.footerSubtitle1.setText(m.vFooterSubTitle);

					}
					
					if(m.vTitle != null && m.vTitle.trim().length() > 0){
						
						vh.tileName1.setVisibility(View.VISIBLE);
						
						
						String titleName = m.vTitle.toUpperCase();

						if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
							if(titleName.length() > 25 ) {
								titleName = titleName.substring(0,22) +"...";
							}
						}
						
						vh.tileName1.setText( titleName );
					}

					
					
					if(m.iLive != null && Integer.parseInt(m.iLive) == 1)
						vh.liveImage1.setVisibility(View.VISIBLE);
					
					
					setFooterStyles( false, vh, isLeftSided,  m);
					
					setImageAndBackgroundColor(m, vh, isLeftSided );
					
				//	if(m.vLinkUrl != null && 
					//		m.vLinkUrl.trim().length() > 0){

					if((m.vLinkUrl != null && m.vLinkUrl.trim().length() > 0) 
							|| (m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0)){
						
						
						if(m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0){
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vLinkHrefUrl);
						}else{
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vLinkUrl);
						}
						
						//vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vLinkUrl);
						vh.thirdPartyTile1.setTag(R.id.active_users_text,m.vLinkType);
						
					}else if(m.vContentType != null && 
							m.vContentType.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){

						if(m.vContentHrefUrl!=null && m.vContentHrefUrl.trim().length()>0){
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vContentHrefUrl);
						}else if(m.vContentUrl!=null && m.vContentUrl.trim().length()>0){
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vContentUrl);
						}
						//vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vContentUrl);
						vh.thirdPartyTile1.setTag(R.id.active_users_text,m.vContentType);
						vh.thirdPartyTile1.setTag(R.id.aboutText,m.vContentId);
						

					}
					
					
					
				}else if(m.vDisplayType.equalsIgnoreCase(Types.TILE_VIDEO)){
					
					vh.footerContent1.setVisibility(View.GONE);
					
					vh.playerIcon1.setVisibility(View.VISIBLE);
					
					if(m.vTitle != null && m.vTitle.trim().length() > 0){
						
						vh.tileName1.setVisibility(View.VISIBLE);
						
						
						String titleName = m.vTitle.toUpperCase();

						if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
							if(titleName.length() > 25 ) {
								titleName = titleName.substring(0,22) +"...";
							}
						}
						
						vh.tileName1.setText( titleName );
					}
					
					setImageAndBackgroundColor(m, vh, isLeftSided );
					
					
					//if(m.vLinkUrl != null && 
				//			m.vLinkUrl.trim().length() > 0){
					if((m.vLinkUrl != null && m.vLinkUrl.trim().length() > 0)
							|| (m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0)){
	
						
						if(m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0){
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vLinkHrefUrl);
						}else{
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vLinkUrl);
						}
						

						//vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vLinkUrl);
						vh.thirdPartyTile1.setTag(R.id.active_users_text,m.vLinkType);
						
					}else if(m.vContentType != null && 
							m.vContentType.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){
						
						if(m.vContentHrefUrl!=null && m.vContentHrefUrl.trim().length()>0){
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vContentHrefUrl);
						}else if(m.vContentUrl!=null && m.vContentUrl.trim().length()>0){
							vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vContentUrl);
						}
						//vh.thirdPartyTile1.setTag(R.id.about_txtview,m.vContentUrl);
						vh.thirdPartyTile1.setTag(R.id.active_users_text,m.vContentType);
						vh.thirdPartyTile1.setTag(R.id.aboutText,m.vContentId);
						

					}
					
					
					
				}
				
				
				
				
				
				


			} else {
				
				
				vh.imageSummary2.setShadowLayer(0, 0, 0, 0);
				vh.footerTitle2.setShadowLayer(0, 0, 0, 0);
				vh.footerSubtitle2.setShadowLayer(0, 0, 0, 0);
				
				
				
				
				if(m.vDisplayType.equalsIgnoreCase(Types.TILE_TIMESTAMP)){
					

					vh.footerContent2.setVisibility(View.VISIBLE);
					
					if(m.vSource != null && m.vSource.trim().length() > 0){
						
						vh.footerTitle2.setVisibility(View.VISIBLE);
						vh.footerTitle2.setText(m.vSource);
					}
					
					if(m.vTimeStamp != null && m.vTimeStamp.trim().length() > 0){

						
						vh.footerSubtitle2.setVisibility(View.VISIBLE);
						vh.footerSubtitle2.setText(dateUtil.gmt_to_local_timezone_tiles(m.vTimeStamp));

					}
					
					if(m.vSourceIcon != null && m.vSourceIcon.trim().length() > 0 ) {
						
						vh.sourceIcon2.setVisibility(View.VISIBLE);
						imageDownloader.download( m.vSourceIcon, vh.sourceIcon2, false,null);
					}
					
					

					if(m.vSocialIcon != null && m.vSocialIcon.trim().length() > 0){
						
						vh.socialIcon2.setVisibility(View.VISIBLE);
						imageDownloader.download( m.vSocialIcon ,vh.socialIcon2, false,null );
					}
					
					
					
					if(m.vTitle != null && m.vTitle.trim().length() > 0){
						
						vh.tileName2.setVisibility(View.VISIBLE);
						
						
						String titleName = m.vTitle.toUpperCase();

						if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
							if(titleName.length() > 25 ) {
								titleName = titleName.substring(0,22) +"...";
							}
						}
						
						vh.tileName2.setText( titleName );
					}
					
					if(m.vSummary != null && 
							m.vSummary.trim().length() > 0){
						vh.imageSummary2.setVisibility(View.VISIBLE);
						vh.imageSummary2.setText(m.vSummary);		
					}
					
					setImageAndBackgroundColor(m, vh, isLeftSided );
					
					//if(m.vLinkUrl != null && 
					//		m.vLinkUrl.trim().length() > 0){

						if((m.vLinkUrl != null && m.vLinkUrl.trim().length() > 0)
								|| (m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0)){
							
							
							if(m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0){
								vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vLinkHrefUrl);
							}else{
								vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vLinkUrl);
							}
							
					//	vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vLinkUrl);
						vh.thirdPartyTile2.setTag(R.id.active_users_text,m.vLinkType);
						
					}else if(m.vContentType != null && 
							m.vContentType.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){

						if(m.vContentHrefUrl!=null && m.vContentHrefUrl.trim().length()>0){
							vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vContentHrefUrl);
						}else if(m.vContentUrl!=null && m.vContentUrl.trim().length()>0){
							vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vContentUrl);
						}
						//vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vContentUrl);
						vh.thirdPartyTile2.setTag(R.id.active_users_text,m.vContentType);
						vh.thirdPartyTile2.setTag(R.id.aboutText,m.vContentId);
						

					}
					
					

					
				}else if(m.vDisplayType.equalsIgnoreCase(Types.TILE_HEADLINE)){
					vh.socialIcon2.setVisibility(View.GONE);
					vh.sourceIcon2.setVisibility(View.GONE);
					
					if(m.vFooterTitle != null && m.vFooterTitle.trim().length() > 0){
						vh.footerContent2.setVisibility(View.VISIBLE);
						vh.footerTitle2.setVisibility(View.VISIBLE);
						vh.footerTitle2.setText(m.vFooterTitle);
					}
					
					if(m.vFooterSubTitle != null && m.vFooterSubTitle.trim().length() > 0){

						vh.footerContent2.setVisibility(View.VISIBLE);
						vh.footerSubtitle2.setVisibility(View.VISIBLE);
						vh.footerSubtitle2.setText(m.vFooterSubTitle);

					}
					
					if(m.vTitle != null && m.vTitle.trim().length() > 0){
						
						vh.tileName2.setVisibility(View.VISIBLE);
						
						
						String titleName = m.vTitle.toUpperCase();

						if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
							if(titleName.length() > 25 ) {
								titleName = titleName.substring(0,22) +"...";
							}
						}
					
						vh.tileName2.setText( titleName );
					}
					
					setFooterStyles( false, vh, isLeftSided,  m);
					
					setImageAndBackgroundColor(m, vh, isLeftSided );
					
					//if(m.vLinkUrl != null && 
					//		m.vLinkUrl.trim().length() > 0){

					if((m.vLinkUrl != null && m.vLinkUrl.trim().length() > 0)
							|| (m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0)){
							
							
							if(m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0){
								vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vLinkHrefUrl);
							}else{
								vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vLinkUrl);
							}
							
					//	vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vLinkUrl);
						vh.thirdPartyTile2.setTag(R.id.active_users_text,m.vLinkType);
						
					}else if(m.vContentType != null && 
							m.vContentType.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){

						if(m.vContentHrefUrl!=null && m.vContentHrefUrl.trim().length()>0){
							vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vContentHrefUrl);
						}else if(m.vContentUrl!=null && m.vContentUrl.trim().length()>0){
							vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vContentUrl);
						}
						//vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vContentUrl);
						vh.thirdPartyTile2.setTag(R.id.active_users_text,m.vContentType);
						vh.thirdPartyTile2.setTag(R.id.aboutText,m.vContentId);
						

					}
					
					
					
					
				}else if(m.vDisplayType.equalsIgnoreCase(Types.TILE_PHOTO)){
					
					vh.socialIcon2.setVisibility(View.GONE);
					vh.sourceIcon2.setVisibility(View.GONE);
					
					
					if(m.vFooterTitle != null && m.vFooterTitle.trim().length() > 0){
						vh.footerContent2.setVisibility(View.VISIBLE);
						vh.footerTitle2.setVisibility(View.VISIBLE);
						vh.footerTitle2.setText(m.vFooterTitle);
					}
					
					if(m.vFooterSubTitle != null && m.vFooterSubTitle.trim().length() > 0){

						vh.footerContent2.setVisibility(View.VISIBLE);
						vh.footerSubtitle2.setVisibility(View.VISIBLE);
						vh.footerSubtitle2.setText(m.vFooterSubTitle);

					}
					
					if(m.vTitle != null && m.vTitle.trim().length() > 0){
						
						vh.tileName2.setVisibility(View.VISIBLE);
						
						
						String titleName = m.vTitle.toUpperCase();

						if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
							if(titleName.length() > 25 ) {
								titleName = titleName.substring(0,22) +"...";
							}
						}
						
						vh.tileName2.setText( titleName );
					}
					
					
					if(m.iLive != null && Integer.parseInt(m.iLive) == 1)
						vh.liveImage2.setVisibility(View.VISIBLE);
					
					setFooterStyles( false, vh, isLeftSided,  m);
					
					setImageAndBackgroundColor(m, vh, isLeftSided );
					
				//	if(m.vLinkUrl != null && 
					//		m.vLinkUrl.trim().length() > 0){

					if((m.vLinkUrl != null && m.vLinkUrl.trim().length() > 0)
							|| (m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0)){
							
							
							if(m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0){
								vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vLinkHrefUrl);
							}else{
								vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vLinkUrl);
							}

					//	vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vLinkUrl);
						vh.thirdPartyTile2.setTag(R.id.active_users_text,m.vLinkType);
						
					}else if(m.vContentType != null && 
							m.vContentType.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){

						if(m.vContentHrefUrl!=null && m.vContentHrefUrl.trim().length()>0){
							vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vContentHrefUrl);
						}else if(m.vContentUrl!=null && m.vContentUrl.trim().length()>0){
							vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vContentUrl);
						}
						//vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vContentUrl);
						vh.thirdPartyTile2.setTag(R.id.active_users_text,m.vContentType);
						vh.thirdPartyTile2.setTag(R.id.aboutText,m.vContentId);
						

					}
					
					
				}else if(m.vDisplayType.equalsIgnoreCase(Types.TILE_SOLID)){
					
					
					vh.socialIcon2.setVisibility(View.GONE);
					vh.sourceIcon2.setVisibility(View.GONE);
					
					
					if(m.vFooterTitle != null && m.vFooterTitle.trim().length() > 0){
						vh.footerContent2.setVisibility(View.VISIBLE);
						vh.footerTitle2.setVisibility(View.VISIBLE);
						vh.footerTitle2.setText(m.vFooterTitle);
					}
					
					if(m.vFooterSubTitle != null && m.vFooterSubTitle.trim().length() > 0){

						vh.footerContent2.setVisibility(View.VISIBLE);
						vh.footerSubtitle2.setVisibility(View.VISIBLE);
						vh.footerSubtitle2.setText(m.vFooterSubTitle);

					}
					
					if(m.vTitle != null && m.vTitle.trim().length() > 0){
						
						vh.tileName2.setVisibility(View.VISIBLE);
						
						
						String titleName = m.vTitle.toUpperCase();

						if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
							if(titleName.length() > 25 ) {
								titleName = titleName.substring(0,22) +"...";
							}
						}
						
						vh.tileName2.setText( titleName );
					}

					
					
					if(m.iLive != null && Integer.parseInt(m.iLive) == 1)
						vh.liveImage2.setVisibility(View.VISIBLE);
					
					
					setFooterStyles( false, vh, isLeftSided,  m);
					
					setImageAndBackgroundColor(m, vh, isLeftSided );
					
				//	if(m.vLinkUrl != null && 
					//		m.vLinkUrl.trim().length() > 0){
						if((m.vLinkUrl != null && m.vLinkUrl.trim().length() > 0)
								|| (m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0)){
							
							
							if(m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0){
								vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vLinkHrefUrl);
							}else{
								vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vLinkUrl);
							}

					//	vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vLinkUrl);
						vh.thirdPartyTile2.setTag(R.id.active_users_text,m.vLinkType);
						
					}else if(m.vContentType != null && 
							m.vContentType.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){

						if(m.vContentHrefUrl!=null && m.vContentHrefUrl.trim().length()>0){
							vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vContentHrefUrl);
						}else if(m.vContentUrl!=null && m.vContentUrl.trim().length()>0){
							vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vContentUrl);
						}
						//vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vContentUrl);
						vh.thirdPartyTile2.setTag(R.id.active_users_text,m.vContentType);
						vh.thirdPartyTile2.setTag(R.id.aboutText,m.vContentId);
						

					}
					
					
					
				}else if(m.vDisplayType.equalsIgnoreCase(Types.TILE_VIDEO)){
					
					vh.footerContent2.setVisibility(View.GONE);
					
					vh.playerIcon2.setVisibility(View.VISIBLE);
					
					if(m.vTitle != null && m.vTitle.trim().length() > 0){
						
						vh.tileName2.setVisibility(View.VISIBLE);
						
						
						String titleName = m.vTitle.toUpperCase();

						if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
							if(titleName.length() > 25 ) {
								titleName = titleName.substring(0,22) +"...";
							}
						}
						
						vh.tileName2.setText( titleName );
					}
					
					setImageAndBackgroundColor(m, vh, isLeftSided );
					
					
				//	if(m.vLinkUrl != null && 
					//		m.vLinkUrl.trim().length() > 0){
					if((m.vLinkUrl != null && m.vLinkUrl.trim().length() > 0)
							|| (m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0)){
							
							
							if(m.vLinkHrefUrl != null && m.vLinkHrefUrl.trim().length() > 0){
								vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vLinkHrefUrl);
							}else{
								vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vLinkUrl);
							}

					//	vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vLinkUrl);
						vh.thirdPartyTile2.setTag(R.id.active_users_text,m.vLinkType);
						
					}else if(m.vContentType != null && 
							m.vContentType.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){

						if(m.vContentHrefUrl!=null && m.vContentHrefUrl.trim().length()>0){
							vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vContentHrefUrl);
						}else if(m.vContentUrl!=null && m.vContentUrl.trim().length()>0){
							vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vContentUrl);
						}
						//vh.thirdPartyTile2.setTag(R.id.about_txtview,m.vContentUrl);
						vh.thirdPartyTile2.setTag(R.id.active_users_text,m.vContentType);
						vh.thirdPartyTile2.setTag(R.id.aboutText,m.vContentId);
						

					}
					
					
					
				}
				
				
				



			}
		} catch (Exception e) {
			//Logs.show(e);
		}

	}



	
	
	private void setImageAndBackgroundColor(MatchRoomMessage m, ViewHolder vh,boolean isThirdPartyTile1) {
		try {
			
			String bgColor = null, imageUrl = null;


			if (m.vBackgroundColor != null
					&& m.vBackgroundColor.trim()
					.length() > 0) {

				bgColor = m.vBackgroundColor;
			}

			if (m.vImageUrl != null
					&& m.vImageUrl.trim().length() > 0) {

				imageUrl = m.vImageUrl;
			}

			else if (m.vBackgroundImage != null
					&& m.vBackgroundImage.trim()
					.length() > 0) {

				imageUrl = m.vBackgroundImage;
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
			
			
			if(isThirdPartyTile1){
				if(vh.ImageWithSummaryImage1 != null  )
					vh.ImageWithSummaryImage1.setTag( null );
				
				
				
				
				
				


				if (bgColor != null && bgColor.trim().length() > 0) {
					
					if (m.vDisplayType != null
							&& m.vDisplayType
							.equalsIgnoreCase(Constants.ACCEPT_TYPE_SOLID)) {
						
						// removing centre cropping for solid type image
						vh.ImageWithSummaryImage1.setScaleType(ScaleType.FIT_CENTER);
						vh.ImageWithSummaryImage1.setAdjustViewBounds(false);

						if (bgColor.equalsIgnoreCase("FFFFFF")) {
							vh.tileName1.setTextColor(Color.parseColor("#404040"));
						} else {
							vh.tileName1.setTextColor(Color.parseColor("#FFFFFF"));				
						}

	
						vh.ImageWithSummaryImage1.setBackgroundColor(Color.parseColor("#FFFFFF"));
						vh.imageWithSummary1.setBackgroundColor(Color.parseColor("#FFFFFF"));
						
						vh.euro_tile_bg1.getChildAt( 0 ).setBackgroundColor(Color.parseColor("#"+bgColor));
						if(vh.euro_tile_bg1.getChildAt( 0 ).getBackground() != null)
							vh.euro_tile_bg1.getChildAt( 0 ).getBackground().setAlpha(220);
						
						vh.solidImageOverlay1.setVisibility(View.VISIBLE);
						vh.solidImageOverlay1.setBackgroundColor(Color.parseColor("#"+bgColor));
						if(vh.solidImageOverlay1.getBackground() != null)
							vh.solidImageOverlay1.getBackground().setAlpha(220);
						

						if (imageUrl != null && imageUrl.trim().length() > 0) {
							vh.ImageWithSummaryImage1.setVisibility( View.VISIBLE);
								vh.imageWithSummary1.setTag(R.id.active_users_no,bgColor);
								imageDownloaderSports.download(imageUrl, vh.imageWithSummary1,false, bgColor);
						}

					} else if (imageUrl != null && imageUrl.trim().length() > 0) {
						vh.imageWithSummary1.setBackgroundColor( Color.parseColor("#"+bgColor) );
						vh.ImageWithSummaryImage1.setVisibility( View.VISIBLE);
						
						if(!bgColor.equalsIgnoreCase("ffffff")){
							
							
							
							
							if(vh.ImageWithSummaryImage1 != null  )
								vh.ImageWithSummaryImage1.setTag("show_dark");
							vh.imageWithSummary1.setTag(R.id.active_users_no,bgColor);
							imageDownloaderSports.download(imageUrl, vh.imageWithSummary1,false, bgColor);	
							vh.imageSummary1.setShadowLayer(0, 0, 0, 0);
							vh.footerTitle1.setShadowLayer(0, 0, 0, 0);
							vh.footerSubtitle1.setShadowLayer(0, 0, 0, 0);
						} else {
							
							
							imageDownloaderSports.download(imageUrl, vh.imageWithSummary1,false);					
							vh.imageSummary1.setShadowLayer(4, 0, 0, Color.parseColor("#000000"));
							vh.footerTitle1.setShadowLayer(4, 0, 0, Color.parseColor("#000000"));
							vh.footerSubtitle1.setShadowLayer(4, 0, 0, Color.parseColor("#000000"));
						}
						
						
						
					} else {
						setFooterStyles(true,vh,isThirdPartyTile1,m);
//					if( bgColor!= null && bgColor.trim().equalsIgnoreCase("ffffff") )
//						imageWithSummary.setBackgroundColor( Color.GRAY );
//					else
						vh.imageWithSummary1.setBackgroundColor(Color.parseColor("#"+ bgColor));
					}
				} else {
					vh.imageWithSummary1.setBackgroundColor( Color.GRAY );
					if (imageUrl != null && imageUrl.trim().length() > 0) {
						vh.ImageWithSummaryImage1.setVisibility( View.VISIBLE);
						imageDownloaderSports.download(imageUrl, vh.imageWithSummary1,false);
					}

				}
				
				
			}else{
				
				if(vh.ImageWithSummaryImage2 != null  )
					vh.ImageWithSummaryImage2.setTag( null );
				
				
				
				


				if (bgColor != null && bgColor.trim().length() > 0) {
					
					if (m.vDisplayType != null
							&& m.vDisplayType
							.equalsIgnoreCase(Constants.ACCEPT_TYPE_SOLID)) {

						vh.ImageWithSummaryImage2.setScaleType(ScaleType.FIT_CENTER);
						vh.ImageWithSummaryImage2.setAdjustViewBounds(false);
						
						if (bgColor.equalsIgnoreCase("FFFFFF")) {
							vh.tileName2.setTextColor(Color.parseColor("#404040"));
						} else {
							vh.tileName2.setTextColor(Color.parseColor("#FFFFFF"));				
						}

						vh.ImageWithSummaryImage2.setBackgroundColor(Color.parseColor("#FFFFFF"));
						vh.imageWithSummary2.setBackgroundColor(Color.parseColor("#FFFFFF"));
						
						vh.euro_tile_bg2.getChildAt( 0 ).setBackgroundColor(Color.parseColor("#"+bgColor));
						if(vh.euro_tile_bg2.getChildAt( 0 ).getBackground() != null)
							vh.euro_tile_bg2.getChildAt( 0 ).getBackground().setAlpha(220);
						
						vh.solidImageOverlay2.setVisibility(View.VISIBLE);
						vh.solidImageOverlay2.setBackgroundColor(Color.parseColor("#"+bgColor));
						if(vh.solidImageOverlay2.getBackground() != null)
							vh.solidImageOverlay2.getBackground().setAlpha(220);
						

						if (imageUrl != null && imageUrl.trim().length() > 0) {

							vh.ImageWithSummaryImage2.setVisibility( View.VISIBLE);		
								vh.imageWithSummary2.setTag(R.id.active_users_no,bgColor);
								imageDownloaderSports.download(imageUrl, vh.imageWithSummary2,false, bgColor);
						}

					} else if (imageUrl != null && imageUrl.trim().length() > 0) {
						vh.imageWithSummary2.setBackgroundColor( Color.parseColor("#"+bgColor) );
						vh.ImageWithSummaryImage2.setVisibility( View.VISIBLE);
						
						if(!bgColor.equalsIgnoreCase("ffffff")){
							
							
							
							
							if(vh.ImageWithSummaryImage2 != null  )
								vh.ImageWithSummaryImage2.setTag("show_dark");
							vh.imageWithSummary2.setTag(R.id.active_users_no,bgColor);
							imageDownloaderSports.download(imageUrl, vh.imageWithSummary2,false, bgColor);	
							vh.imageSummary2.setShadowLayer(0, 0, 0, 0);
							vh.footerTitle2.setShadowLayer(0, 0, 0, 0);
							vh.footerSubtitle2.setShadowLayer(0, 0, 0, 0);
						} else {
							
							
							imageDownloaderSports.download(imageUrl, vh.imageWithSummary2,false);					
							vh.imageSummary2.setShadowLayer(4, 0, 0, Color.parseColor("#000000"));
							vh.footerTitle2.setShadowLayer(4, 0, 0, Color.parseColor("#000000"));
							vh.footerSubtitle2.setShadowLayer(4, 0, 0, Color.parseColor("#000000"));
						}
						
						
						
					} else {
						setFooterStyles(true,vh,isThirdPartyTile1,m);

							vh.imageWithSummary2.setBackgroundColor(Color.parseColor("#"+ bgColor));
					}
				} else {
					vh.imageWithSummary2.setBackgroundColor( Color.GRAY );
					if (imageUrl != null && imageUrl.trim().length() > 0) {
						vh.ImageWithSummaryImage2.setVisibility( View.VISIBLE);
						
						imageDownloaderSports.download(imageUrl, vh.imageWithSummary2,false);
					}

				}
				
			}
			
			
		} catch (Exception e) {
		//	Logs.show(e);
		}
	}
	






	/**
	 *  showing selected / de selected state of the sports item 
	 */
	private void selectDeSelectState ( View v, boolean isSelected, boolean isFirst  ){ 

		try {
			if ( v == null ) {
				return;
			}
			viewLayoutParams = v.getLayoutParams();
			if ( isSelected ) {


				(( TextView ) v.findViewById(R.id.itemTitle)).setTextColor(Color.WHITE);
				(( TextView ) v.findViewById( R.id.commentingNumber)).setTextColor(Color.WHITE);
				(( TextView ) v.findViewById( R.id.commentingNumber)).setBackgroundResource(R.drawable.comments_bubble_d);
				(( TextView ) v.findViewById( R.id.uName)).setTextColor(Color.WHITE);
				(( TextView ) v.findViewById( R.id.commentTime)).setTextColor(Color.WHITE);
				(( TextView ) v.findViewById( R.id.comment_desc )).setTextColor(Color.WHITE);


				setTypeFaces (( TextView ) v.findViewById(R.id.itemTitle), ( TextView )v.findViewById( R.id.commentingNumber), ( TextView )v.findViewById( R.id.uName), ( TextView )v.findViewById( R.id.comment_desc ) , ( TextView )v.findViewById( R.id.commentTime) );


				if ( isFirst ) {
					(( TextView ) v.findViewById( R.id.comment_desc )).setTypeface(Constants.OPEN_SANS_LIGHT);
				} 
				v.setBackgroundColor(Color.parseColor("#B0E6EE"));
				v.setLayoutParams(viewLayoutParams);


			} else {


				(( TextView ) v.findViewById(R.id.itemTitle)).setTextColor(Color.parseColor("#404040"));
				(( TextView ) v.findViewById( R.id.commentingNumber)).setTextColor(Color.parseColor("#B9B6B9"));
				(( TextView ) v.findViewById( R.id.commentingNumber)).setBackgroundResource(R.drawable.comments_bubble);
				(( TextView ) v.findViewById( R.id.uName)).setTextColor(Color.parseColor("#FF4754"));
				(( TextView ) v.findViewById( R.id.comment_desc )).setTextColor(Color.parseColor("#616161"));
				(( TextView ) v.findViewById( R.id.commentTime)).setTextColor(Color.parseColor("#B9B6B9"));
				setTypeFaces (( TextView ) v.findViewById(R.id.itemTitle), ( TextView )v.findViewById( R.id.commentingNumber), ( TextView )v.findViewById( R.id.uName), ( TextView )v.findViewById( R.id.comment_desc ) , ( TextView )v.findViewById( R.id.commentTime) );

				if ( isFirst ) {
					(( TextView ) v.findViewById( R.id.comment_desc )).setTypeface(Constants.OPEN_SANS_LIGHT);
					(( TextView ) v.findViewById( R.id.comment_desc )).setTextColor(Color.parseColor("#FF4754"));
					v.setBackgroundResource ( R.drawable.hero_base );
				} else {
					v.setBackgroundResource ( R.drawable.sport_base );
				}
				
				v.setLayoutParams(viewLayoutParams);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show(e);
		}
	}


	private float rawX = 0;
	private float rawY = 0;
	long downTime = 0;

	@Override
	public boolean onTouch(final View v, MotionEvent event) {

		try {
			if ( v == null ) {
				return false;
			}
			if( roomBase != null && v.getId() == roomBase.getId() ) {
				if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_OUTSIDE)
					this.notifyDataSetChanged();
				return false;
			}
			if (event.getAction() == MotionEvent.ACTION_DOWN) {

				// select
				downTime = event.getDownTime();
				rawX = event.getRawX();
				rawY = event.getRawY();
				startUpdating(v);

			} else if (event.getAction() == MotionEvent.ACTION_UP) {

				stopUpdating();

				final int selectedId = Integer.parseInt ( v.getTag().toString() ) ;

				if (event.getRawY() == rawY || (event.getEventTime() - downTime) < 200) {

					if(selectedId == 1)
						selectDeSelectState ( v, true, true );
					else if(selectedId == 2)
						selectDeSelectState ( v, true, false );

					if (mHandler != null) {

						mHandler.postDelayed(new Runnable() {

							@Override
							public void run() {
								try {
									if(selectedId == 1)
										selectDeSelectState ( v, false, true );
									else if(selectedId == 2)
										selectDeSelectState ( v, false, false );
								} catch ( Exception e) {
									//Logs.show ( e );
								}
							}
						}, 100);

					}
				}



				LinearLayout li  = null;
				String vConversationId = null;
				Bundle bundle = null;

				if( v.getTag(R.id.activeUserMainLayout) != null && Boolean.parseBoolean(v.getTag(R.id.activeUserMainLayout).toString()) ){
				//	Log.e("123","insie ON tOuch of mact room grid adapter >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  ");
					if(v.getTag(R.id.about_txtview) != null && v.getTag(R.id.about_txtview).toString().trim().length() > 0 ){
					//	Log.e("123","insie ON tOuch of mact room grid adapter >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+v.getTag(R.id.about_txtview).toString());
						String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
						
						Bundle b =  new Bundle();
						
						b.putString( "vMainColor",vMainColor );
						
						b.putString( "vMainTitleColor",vMainTitleColor );
						
						b.putString( "vSecColor",vSecColor );			
						b.putString( "vSecTitleColor",vSecTitleColor );
						b.putString("fromFragment",topFragmentName);
						
						String type = v.getTag(R.id.active_users_text).toString();
						
						
						//Log.e("123","insie ON tOuch of mact room grid adapter type >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+type);

						if ( type != null && type.equalsIgnoreCase(Constants.ACCEPT_TYPE_SPORTS_JSON ) ) {
							PlayupLiveApplication.getFragmentManagerUtil().setFragment("AllSportsFragment",b);						

						} else if ( type != null &&  type.equalsIgnoreCase(Constants.ACCEPT_TYPE_FIXTURES_JSON ) ) {

							
							b.putString("vCurrentSeasonUrl",v.getTag(R.id.about_txtview).toString() );
							
							PlayupLiveApplication.getFragmentManagerUtil().setFragment("FixturesAndResultsFragment",b);

						} else if ( type != null &&  type.equalsIgnoreCase(Constants.ACCEPT_TYPE_SECTION_JSON ) ) {
							
							b.putString("vSectionUrl",v.getTag(R.id.about_txtview).toString());
							PlayupLiveApplication.getFragmentManagerUtil().setFragment("NewsFragment",b);

						} else if ( type != null &&  type.equalsIgnoreCase(Constants.ACCEPT_TYPE_HTML)){
							///Log.e("123","insie ON tOuch of mact room grid adapter type.equalsIgnoreCase(Constants.ACCEPT_TYPE_HTML) >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  ");
							
							li = (LinearLayout) PlayUpActivity.context.findViewById( R.id.main );
							li.bringToFront();
							

							b.putString("url",v.getTag(R.id.about_txtview).toString());
							
							PlayupLiveApplication.getFragmentManagerUtil().setFragment("WebViewFragment",b);

						} else if ( type != null &&  (type.equalsIgnoreCase(Constants.ACCEPT_TYPE_VIDEO)
								|| type.equalsIgnoreCase(Constants.ACCEPT_TYPE_AUDIO))){

							Intent i = new Intent(PlayUpActivity.context,VideoActivity.class);
							i.putExtra("videoUrl", v.getTag(R.id.about_txtview).toString());
							PlayUpActivity.context.startActivity(i);

						} else if ( type != null &&  type.equalsIgnoreCase(Constants.ACCEPT_TYPE_COMPETITION)){

							
							b.putString("vCompetitionId",v.getTag(R.id.aboutText).toString());
							PlayupLiveApplication.getFragmentManagerUtil().setFragment("LeagueLobbyFragment",b);
						}
					}

				}else{
					
					String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
					
					switch ( Integer.parseInt ( v.getTag().toString() ) ) {

					case 0 : Message msg = new Message(); 
					CreateRoomFragment.isHome = false;
					msg.obj = "callCreateRoom";
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );

					break;

					case 1 :
						//selectDeSelectState( v, true, true );
						li = (LinearLayout) PlayUpActivity.context.findViewById( R.id.main );

						//li.removeAllViews();
						li.bringToFront();
						li = null;
						vConversationId = v.getTag(R.id.about_txtview).toString();
						bundle = new Bundle ();
						bundle.putString( "vConversationId", vConversationId );
						bundle.putString( "vContestId", vContestId );
						bundle.putString( "fromFragment", topFragmentName );						
						bundle.putString( "vMainColor",vMainColor );						
						bundle.putString( "vMainTitleColor",vMainTitleColor );
						bundle.putString( "vSecColor",vSecColor );			
						bundle.putString( "vSecTitleColor",vSecTitleColor );
						PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchHomeFragment", bundle,R.id.main );

						break;

					case 2 : 
						//selectDeSelectState ( v, true, false );
						li = (LinearLayout) PlayUpActivity.context.findViewById( R.id.main );

						//li.removeAllViews();
						li.bringToFront();
						li = null;
						vConversationId = v.getTag(R.id.about_txtview).toString();
						bundle = new Bundle ();
						bundle.putString( "vConversationId", vConversationId );
						bundle.putString( "vContestId", vContestId );
						bundle.putString( "fromFragment", topFragmentName );
						bundle.putString( "vMainColor",vMainColor );						
						bundle.putString( "vMainTitleColor",vMainTitleColor );
						bundle.putString( "vSecColor",vSecColor );			
						bundle.putString( "vSecTitleColor",vSecTitleColor );

						PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchHomeFragment", bundle,R.id.main );

						break;

					}
				}


			} else if (event.getAction() == MotionEvent.ACTION_CANCEL
					|| event.getAction() == MotionEvent.ACTION_OUTSIDE) {

				stopUpdating();
				switch ( Integer.parseInt ( v.getTag().toString() ) ) {

				case 1 : 
					selectDeSelectState( v, false, true );
					break;

				case 2 :
					selectDeSelectState ( v, false, false );
					break;

				}
			}

			if (event.getEventTime() > (downTime + Constants.highightDelay)) {


				if (event.getRawY() >= rawY - 10 && event.getRawY() <= rawY + 10) {

					stopUpdating();
					int tagId=0;
					if(v.getTag()!= null) {
						tagId =  Integer.parseInt ( v.getTag().toString() ) ;
					}
					switch ( tagId ) {

					case 1 : 
						selectDeSelectState( v, true, true );
						break;

					case 2 : 
						selectDeSelectState ( v, true, false );
						break;

					}

				}
			}

			return true;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
		//	Logs.show(e);
			return false;
		}
		catch(Exception e ){
		//	Logs.show(e);
			return false;
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

				int tagId=0;
				if( v!= null && v.getTag() != null) {
					tagId = Integer.parseInt ( v.getTag().toString() );
				}

				switch (tagId ) {

				case 1 :
					selectDeSelectState( v, true, true );
					break;

				case 2 :
					selectDeSelectState ( v, true, false );
					break;

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
			mHandler	=	null;
			mUpdater = null;
		}
	}

	private class UpdateCounterTask implements Runnable {
		public void run() {
			try {
				if(mHandler	!=	null)
					mHandler.sendEmptyMessage(0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//Logs.show ( e );
			}
		}
	}
	
	
	public void setFooterStyles(boolean isBuzz ,ViewHolder vh,boolean isLeftSided, MatchRoomMessage m) {
		
		if(isLeftSided){
			
			if(!isBuzz){
				vh.footerTitle1.setPadding(0, 0, 0, -10);
				vh.footerTitle1.setTextColor(Color.parseColor("#FFFFFF"));
				vh.footerSubtitle1.setTextColor(Color.parseColor("#FFFFFF"));
				vh.footerTitle1.setTextSize(40);
				vh.footerSubtitle1.setTextSize(18);
				vh.footerTitle1.setTypeface(Constants.OPEN_SANS_BOLD);
				vh.footerSubtitle1.setTypeface(Constants.OPEN_SANS_LIGHT);
			}else if(m.vDisplayType != null && m.vDisplayType.trim().length() > 0 && m.vDisplayType.equalsIgnoreCase(Types.TILE_PHOTO)){
				
				
			}else{
				
				vh.footerTitle1.setPadding(0, 0, 0, 0);
				  
				vh.footerTitle1.setTextSize(13);
				vh.footerSubtitle1.setTextSize(13);
				
				vh.imageSummary1.setTextColor(Color.parseColor("#404040"));
				vh.footerTitle1.setTextColor(Color.parseColor("#404040"));
				vh.footerTitle1.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
				vh.footerSubtitle1.setTextColor(Color.parseColor("#404040"));
				vh.footerSubtitle1.setTypeface(Constants.OPEN_SANS_LIGHT);
			}
			
		}else{
			
			
			
			if(!isBuzz){
				
				
				vh.footerTitle2.setPadding(0, 0, 0, -10);
				vh.footerTitle2.setTextColor(Color.parseColor("#FFFFFF"));
				vh.footerSubtitle2.setTextColor(Color.parseColor("#FFFFFF"));
				vh.footerTitle2.setTextSize(40);
				vh.footerSubtitle2.setTextSize(18);
				vh.footerTitle2.setTypeface(Constants.OPEN_SANS_BOLD);
				vh.footerSubtitle2.setTypeface(Constants.OPEN_SANS_LIGHT);
			}else if(m.vDisplayType != null && m.vDisplayType.trim().length() > 0 && m.vDisplayType.equalsIgnoreCase(Types.TILE_PHOTO)){
				
				
			}else{
				
				vh.footerTitle2.setPadding(0, 0, 0, 0);
				  
				vh.footerTitle2.setTextSize(13);
				vh.footerSubtitle2.setTextSize(13);
				
				vh.imageSummary2.setTextColor(Color.parseColor("#404040"));
				vh.footerTitle2.setTextColor(Color.parseColor("#404040"));
				vh.footerTitle2.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
				vh.footerSubtitle2.setTextColor(Color.parseColor("#404040"));
				vh.footerSubtitle2.setTypeface(Constants.OPEN_SANS_LIGHT);
			}
			
			
		}
		
		
	}

}

