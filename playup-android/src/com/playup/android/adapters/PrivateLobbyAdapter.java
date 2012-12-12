package com.playup.android.adapters;

import java.lang.annotation.ElementType;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils.TruncateAt;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.fragment.TopBarFragment;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

public class PrivateLobbyAdapter extends BaseAdapter implements OnTouchListener , OnClickListener {

	private String sportBackground = null;
	private LayoutInflater inflater;
	private Hashtable<String, List<String>> data;
	private boolean isUserAnonymous = false;
	private ImageDownloader imageDownloader = new ImageDownloader();
	private DateUtil dateUtil = new DateUtil();
	private String vContestId =null;
	private Hashtable<String, List<String>> favouriteData;
	private int favNum = 0;
	DatabaseUtil dbUtil = DatabaseUtil.getInstance();
	
	private String vMainColor = null;
	private String vMainTitleColor = null;

	private android.view.ViewGroup.LayoutParams viewLayoutParams;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;
	private GridView gridView;
	boolean isListScrolling = false;

	public PrivateLobbyAdapter (GridView gridView, Hashtable<String, List<String>> data , boolean isUserAnonymous ,
			String vMainColor,String vMainTitleColor, String vSecColor, String vSecTitleColor ) {
		
		isListScrolling = false;
		this.gridView = gridView;

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		

		this.vMainColor  = vMainColor;
		this.vMainTitleColor = vMainTitleColor;

		this.data = data;
		if (  PlayUpActivity.context != null ) {
			inflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		getFavouriteData();
		
		this.gridView.setOnScrollListener( scrollListener );
		this.isUserAnonymous = isUserAnonymous;
	}

	public void setData ( Hashtable<String, List<String>> data, boolean isUserAnonymous,
			String vMainColor,String vMainTitleColor  , String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		

		this.vMainColor  = vMainColor;
		this.vMainTitleColor = vMainTitleColor;

		this.data = data;
		this.isUserAnonymous = isUserAnonymous;
		getFavouriteData();
		notifyDataSetChanged();
	}

	
	public void refreshFavouriteTiles () {
		getFavouriteData();
		notifyDataSetChanged();
	}
	
	public void getFavouriteData() {
		new Thread( new Runnable() {

			@Override
			public void run() {

				try {
					favouriteData = DatabaseUtil.getInstance().getFavouriteSports();
					if( favouriteData!= null && favouriteData.get("vCompetitionId")!= null && favouriteData.get( "vCompetitionId").size() > 0  ) {
						favNum =  favouriteData.get( "vCompetitionId").size();
					} else {
						favNum = 0;
					} 

					if ( PlayUpActivity.handler != null ) {
						PlayUpActivity.handler.post( new Runnable() {

							@Override
							public void run() {
								
									try {
										notifyDataSetChanged();
									} catch ( Exception e ) {
										Logs.show ( e );
									}
								
							}
						});
					}
				} catch (Exception e) {
					Logs.show(e);
				}
			}
		}).start();

	}

	@Override
	public int getCount() {

		if ( isUserAnonymous ) {
			return 2 + favNum/2;

		} else {

			if ( data != null && data.get( "vConversationId") != null && data.get( "vConversationId").size() > 0 ) {
				if ( ( data.get( "vConversationId").size() + 1 ) % 2 == 0 ) {
					return ( data.get( "vConversationId").size() +1 +favNum ) / 2 + 1 ;
				} else {
					return ( data.get( "vConversationId").size() +1 +favNum) / 2 + 1 ;
				}
			} else {
				return 2;
			}
		}
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

		RelativeLayout item_placeholder_1;
		RelativeLayout item_placeholder_2;
		LinearLayout item_others_1;
		LinearLayout item_others_2;

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
		TextView hangoutText_1;
		TextView hangoutText_2;

		LinearLayout favouriteTile1;
		LinearLayout favouriteTile2;
		LinearLayout favouriteView1;
		LinearLayout favouriteView2;
		RelativeLayout favouriteImage1;
		RelativeLayout favouriteImage2;
		TextView leagueName1;
		TextView leagueName2;
		TextView liveImage1;
		TextView liveImage2;

		RelativeLayout social_sports_1;
		RelativeLayout social_sports_2;

	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		try {
			if ( inflater == null ) {
				LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				return  layoutInflater.inflate( R.layout.private_lobby_item_main, null);
			}
			ViewHolder vh = null;

			if(convertView == null ) {

				vh = new ViewHolder() ;

				convertView = inflater.inflate( R.layout.private_lobby_item_main, null); 		 



				vh.item_first = (LinearLayout) convertView.findViewById( R.id.item_first );
				vh.item_others = (LinearLayout) convertView.findViewById( R.id.item_others );
				vh.userAvatarHolder	=	(ImageView)convertView.findViewById(R.id.userAvatarHolder);

				vh.social_sports_1 = ( RelativeLayout ) vh.item_others.findViewById( R.id.private_social_sports_1 );
				vh.social_sports_2 = ( RelativeLayout ) vh.item_others.findViewById( R.id.private_social_sports_2 );

				vh.item_others_1 = (LinearLayout) vh.item_others.findViewById( R.id.item_others_1 );
				vh.item_others_2 = (LinearLayout) vh.item_others.findViewById( R.id.item_others_2 );
				vh.item_placeholder_1 = (RelativeLayout) vh.item_others.findViewById( R.id.item_placeholder_1 );
				vh.item_placeholder_2 = (RelativeLayout) vh.item_others.findViewById( R.id.item_placeholder_2 );
				vh.favouriteTile1 = (LinearLayout) vh.item_others.findViewById( R.id.favouriteTile1 );
				vh.favouriteTile2 = (LinearLayout) vh.item_others.findViewById( R.id.favouriteTile2 );

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

				vh. hangoutText_1 = (TextView) vh.item_placeholder_1.findViewById(R.id.hangoutText);	
				vh. hangoutText_2 = (TextView) vh.item_placeholder_2.findViewById(R.id.hangoutText);	

				vh.favouriteImage1 = (RelativeLayout) vh.favouriteTile1.findViewById(R.id.favouriteImage1);
				vh.favouriteImage2 = (RelativeLayout) vh.favouriteTile2.findViewById(R.id.favouriteImage2);
				vh.favouriteView1 = (LinearLayout) vh.favouriteTile1.findViewById(R.id.favouriteView1);
				vh.favouriteView2 = (LinearLayout) vh.favouriteTile2.findViewById(R.id.favouriteView2);
				vh.leagueName1 = (TextView) vh.favouriteTile1.findViewById(R.id.leagueName1);
				vh.leagueName2 = (TextView) vh.favouriteTile2.findViewById(R.id.leagueName2);
				vh.liveImage1 = (TextView) vh.favouriteTile1.findViewById(R.id.liveImage1);
				vh.liveImage2 = (TextView) vh.favouriteTile2.findViewById(R.id.liveImage2);



				vh.leagueName1.setTypeface(Constants.OPEN_SANS_BOLD);
				vh.leagueName2.setTypeface(Constants.OPEN_SANS_BOLD);
				vh.leagueName1.setTextColor(Color.WHITE);
				vh.leagueName2.setTextColor(Color.WHITE);

				setTypeFaces ( vh.first_itemTitle, vh.first_commentingNumber, vh.first_uName,vh. first_comment_desc ,  vh.first_commentTime );
				setTypeFaces ( vh.other_1_itemTitle, vh.other_1_commentingNumber, vh.other_1_uName,vh. other_1_comment_desc ,  vh.other_1_commentTime );
				setTypeFaces ( vh.other_2_itemTitle, vh.other_2_commentingNumber, vh.other_2_uName, vh. other_1_comment_desc , vh.other_2_commentTime );
				vh. hangoutText_1.setTypeface(Constants.OPEN_SANS_BOLD);
				vh. hangoutText_2.setTypeface(Constants.OPEN_SANS_BOLD);
				vh.first_comment_desc.setTypeface(Constants.OPEN_SANS_LIGHT);
				convertView.setTag( vh );
			} else {
				vh = (ViewHolder) convertView.getTag();
			}


			vh.social_sports_1.setVisibility( View.GONE );
			vh.social_sports_2.setVisibility( View.GONE );

			if( position == 0)
				convertView.setPadding(8, 8, 8, 0);
			else if(position == (getCount()-1)) 
				convertView.setPadding(8, 0, 8, 8);
			else
				convertView.setPadding(8, 0, 8, 0);

			vh.other_1_comment_desc.setMaxLines(2);
			vh.other_2_comment_desc.setMaxLines(2);
			vh.other_1_uName.setVisibility(View.VISIBLE);
			vh.other_2_uName.setVisibility(View.VISIBLE);
			vh.other_1_commentTime.setVisibility(View.VISIBLE);
			vh.other_2_commentTime.setVisibility(View.VISIBLE);
			vh.other_1_commentingNumber.setVisibility(View.VISIBLE);
			vh.other_2_commentingNumber.setVisibility(View.VISIBLE);

			vh.item_others.setVisibility(View.GONE);
			vh.item_first.setVisibility( View.GONE );
			vh.item_placeholder_1.setVisibility(View.GONE);
			vh.item_placeholder_2.setVisibility(View.GONE);
			vh.item_others_1.setVisibility(View.GONE);
			vh.item_others_2.setVisibility(View.GONE);
			vh.favouriteTile1.setVisibility(View.GONE);
			vh.favouriteTile2.setVisibility(View.GONE);

			vh.favouriteTile1.setOnClickListener( null );
			vh.favouriteTile2.setOnClickListener( null );
			vh.favouriteView1.setOnClickListener( null );
			vh.favouriteView2.setOnClickListener( null );

			vh.item_first.setOnTouchListener( null );


			if ( isUserAnonymous ) {

				// for anonymous user
				if ( position == 0 ) {
					vh.item_first.setVisibility( View.VISIBLE );

					vh.first_comment_desc.setText( R.string.private_lobby_default_text );
					if( vh.first_comment_desc.getText().toString().length() > 45 ) {
						vh.first_comment_desc.setTextSize(17);
					} else {
						vh.first_comment_desc.setTextSize(21);
					}
					vh.first_uName.setText( "PlayUp" );
					vh.first_itemTitle.setText( R.string.name_this_gp );

					vh. first_commentingNumber.setVisibility( View.INVISIBLE );
					vh.first_commentTime.setVisibility( View.GONE );
					vh.item_first.setTag(1);
					vh.item_first.setTag( R.id.about_txtview, "-1" );
					vh.item_first.setOnTouchListener( this );
				} else {



					vh.item_others.setVisibility( View.VISIBLE );
					vh.item_others_1.setVisibility( View.GONE );

					vh.social_sports_2.setVisibility( View.VISIBLE );
					vh.favouriteTile2.setVisibility( View.GONE );

					vh.item_placeholder_1.setVisibility( View.VISIBLE );
					vh.item_placeholder_1.setBackgroundResource( R.drawable.create_hangout_02 );
					vh.hangoutText_1.setText( R.string.createNewGroup );

					vh.item_placeholder_2.setVisibility( View.GONE );
					vh.item_placeholder_1.setTag(2);
					vh.item_placeholder_1.setTag( R.id.about_txtview, "-1" );
					vh.item_placeholder_1.setOnTouchListener( this );

				}


			} else {

				// for logged in user 
				if ( position == 0 ) {

					vh.item_first.setVisibility( View.VISIBLE );
					vh.first_comment_desc.setText( new Util().getSmiledText( data.get( "vMessage" ).get( position ) ) );
					vh.first_uName.setText(  new Util().getSmiledText( data.get( "vDisplayName" ).get( position ) ) );
					vh.first_itemTitle.setText( new Util().getSmiledText( data.get( "vConversationName" ).get( position ).toUpperCase() ) );

					imageDownloader.download( data.get( "vAvatarUrl").get( position ), vh.first_userImage , true, this );

					if ( Integer.parseInt( data.get( "iTotalMessages" ).get( position ) ) >= 0 ) {
						vh.first_commentingNumber.setText( data.get( "iTotalMessages" ).get( position ) );
						vh.first_commentTime.setText( dateUtil. gmt_to_local_timezone( data.get( "vCreatedDate" ).get( position ) ) );

						vh. first_commentingNumber.setVisibility( View.VISIBLE );
						vh.first_commentTime.setVisibility( View.VISIBLE );

					} else {

						vh. first_commentingNumber.setVisibility( View.INVISIBLE );
						vh.first_commentTime.setVisibility( View.GONE );
					}

					if(  data.get( "vDisplayName" ).get( position ) == null ) {					
						vh.first_uName.setText(  R.string.privateLobbyMessage11 );
						vh.first_comment_desc.setText( R.string.privateLobbyMessage12 );
					}

					vh.item_first.setOnTouchListener( this );
					vh.item_first.setTag(1);
					vh.item_first.setTag( R.id.about_txtview, data.get( "vConversationId" ).get( position ) );

				} else {

					if( position ==1 ) {
						vh.item_others.setVisibility( View.VISIBLE );
						vh.item_others_1.setVisibility( View.GONE );

						vh.social_sports_2.setVisibility( View.VISIBLE );
						vh.favouriteTile2.setVisibility( View.GONE );

						vh.item_placeholder_1.setVisibility( View.VISIBLE );
						vh.item_placeholder_1.setBackgroundResource( R.drawable.create_hangout_02 );
						vh.hangoutText_1.setText( R.string.createNewGroup );

						vh.item_placeholder_2.setVisibility( View.GONE );
						vh.item_placeholder_1.setTag(2);
						vh.item_placeholder_1.setTag( R.id.about_txtview, "-1" );
						vh.item_placeholder_1.setOnTouchListener( this );
					}

					if ( ( ( (position * 2 ) -2 -favNum) > 0 )&&(( (position * 2 ) -2 -favNum)) < data.get( "vConversationId" ).size() ) {

						selectDeSelectState ( vh.item_others_1, false, false );

						vh.item_others.setVisibility( View.VISIBLE );
						vh.item_others_1.setVisibility( View.VISIBLE );
						vh.favouriteTile2.setVisibility( View.GONE );
						if( ( position * 2 ) -2 -favNum == (data.get( "vConversationId" ).size() -1) ) {
							vh.item_others_2.setVisibility(View.GONE);
							vh.social_sports_2.setVisibility(View.VISIBLE);

						}

						vh.other_1_comment_desc.setText( new Util().getSmiledText( data.get( "vMessage" ).get( ( position * 2 ) -2 -favNum) ) );
						vh.other_1_uName.setText(  new Util().getSmiledText( data.get( "vDisplayName" ).get( ( position * 2 ) -2 -favNum) ) );
						vh.other_1_itemTitle.setText( new Util().getSmiledText( data.get( "vConversationName" ).get( ( position * 2 ) -2 -favNum ).toUpperCase() ) );
						vh.other_1_commentingNumber.setText( data.get( "iTotalMessages" ).get( ( position * 2 ) -2 -favNum  ) );
						vh.other_1_commentTime.setText( dateUtil. gmt_to_local_timezone( data.get( "vCreatedDate" ).get( ( position * 2 ) -2 -favNum )) );

						if ( Integer.parseInt( data.get( "iTotalMessages" ).get(  ( position * 2 ) -2 -favNum) ) >= 0 ) {
							vh.other_1_commentingNumber.setText( data.get( "iTotalMessages" ).get(  ( position * 2 ) -2 -favNum ) );
							vh.other_1_commentTime.setText( dateUtil. gmt_to_local_timezone( data.get( "vCreatedDate" ).get(  ( position * 2 ) -2 -favNum ) ) );

							vh. other_1_commentingNumber.setVisibility( View.VISIBLE );
							vh.other_1_commentTime.setVisibility( View.VISIBLE );

						} else {
							vh. other_1_commentingNumber.setVisibility( View.INVISIBLE );
							vh.other_1_commentTime.setVisibility( View.GONE );
						}

						if(  data.get( "vDisplayName" ).get( ( position * 2 ) -2 -favNum   ) == null ) {					
							vh.other_1_uName.setText(  R.string.privateLobbyMessage21 );
							vh.other_1_comment_desc.setText( R.string.privateLobbyMessage22 );
						}


						vh.item_others_1.setOnTouchListener( this );
						vh.item_others_1.setTag(2);
						vh.item_others_1.setTag( R.id.about_txtview, data.get( "vConversationId" ).get( ( position * 2 ) -2 -favNum ) );

					}

					if ( ((position * 2) -1-favNum>0 )&&( (position * 2) -1-favNum )  < data.get( "vConversationId" ).size() ) {

						selectDeSelectState ( vh.item_others_2, false, false );
						if( position ==1 && ( position * 2 ) -1 -favNum == 1 ) {
							vh.item_placeholder_1.setVisibility(View.VISIBLE);
						}
						if( position >1 && ( position * 2 ) -1 -favNum == 1 ) {
							vh.item_placeholder_1.setVisibility(View.GONE);
							vh.favouriteTile1.setVisibility(View.VISIBLE);
						}

						vh.social_sports_1.setVisibility( View.GONE );
						vh.social_sports_2.setVisibility( View.GONE );

						vh.item_others.setVisibility( View.VISIBLE );
						vh.item_others_2.setVisibility( View.VISIBLE );
						vh.favouriteTile2.setVisibility( View.GONE );


						vh.other_2_comment_desc.setText( new Util().getSmiledText( data.get( "vMessage" ).get( (position * 2) -1-favNum ) ) );
						vh.other_2_uName.setText(  new Util().getSmiledText( data.get( "vDisplayName" ).get( (position * 2) -1-favNum ) ) );
						vh.other_2_itemTitle.setText( new Util().getSmiledText( data.get( "vConversationName" ).get( (position * 2) -1-favNum ).toUpperCase() ) );
						vh.other_2_commentingNumber.setText( data.get( "iTotalMessages" ).get( (position * 2) -1-favNum) );
						vh.other_2_commentTime.setText( dateUtil. gmt_to_local_timezone( data.get( "vCreatedDate" ).get( (position * 2) -1-favNum ) ) );

						if ( Integer.parseInt( data.get( "iTotalMessages" ).get((position * 2) -1-favNum) ) >= 0 ) {
							vh.other_2_commentingNumber.setText( data.get( "iTotalMessages" ).get( (position * 2) -1-favNum) );
							vh.other_2_commentTime.setText( dateUtil. gmt_to_local_timezone( data.get( "vCreatedDate" ).get( (position * 2) -1-favNum ) ) );

							vh. other_2_commentingNumber.setVisibility( View.VISIBLE );
							vh.other_2_commentTime.setVisibility( View.VISIBLE );

						} else {
							vh. other_2_commentingNumber.setVisibility( View.INVISIBLE );
							vh.other_2_commentTime.setVisibility( View.GONE );
						}

						if(  data.get( "vDisplayName" ).get( ((position * 2) -1-favNum)   ) == null ) {					
							vh.other_2_uName.setText(  R.string.privateLobbyMessage21 );
							vh.other_2_comment_desc.setText( R.string.privateLobbyMessage22 );
						}

						vh.item_others_2.setOnTouchListener( this );
						vh.item_others_2.setTag(2);
						vh.item_others_2.setTag( R.id.about_txtview, data.get( "vConversationId" ).get( ( (position * 2) -1-favNum)  ) );
					}


				}
			}

			/**
			 * setting my sports / favourite leagues as tiles
			 * these tiles will come between recent tile and other tiles followed by create group tile
			 */
			if( position!=0 && favouriteData!=null && favouriteData.get( "vCompetitionId")!= null ) {
				if ((( position * 2 ) -3)>= 0 && (( position * 2 ) -3) < favouriteData.get( "vCompetitionId" ).size() ) {

					vh.item_others.setVisibility( View.VISIBLE );
					vh.favouriteTile1.setVisibility(View.VISIBLE);
					vh.favouriteTile2.setVisibility(View.GONE);

					if( (( position * 2 ) -3)  == (favNum -1) ) {

						if(isUserAnonymous || (data == null || data.get("vConversationId").size() <= 1) ) {
							vh.favouriteTile2.setVisibility(View.GONE);
							vh.social_sports_2.setVisibility( View.VISIBLE );

							vh.item_others_2.setVisibility(View.GONE);
							vh.item_others_1.setVisibility(View.GONE);
							vh.item_placeholder_1.setVisibility(View.GONE);
						}  else {
							vh.item_others_2.setVisibility(View.VISIBLE);
						}

					}




					ImageView favouriteStar = (ImageView) vh.favouriteTile1.findViewById(R.id.favouriteStar);
					int favouriteStatus = 0;
					if( favouriteData.get("isFavourite").get(( position * 2 ) -3) != null )
						favouriteStatus = Integer.parseInt( favouriteData.get("isFavourite").get(( position * 2 ) -3));

					if (favouriteStatus == 0) {
						favouriteStar.setAlpha(255);
						favouriteStar.setImageResource(R.drawable.allsports_grey_star);
					} else {
						favouriteStar.setImageResource(R.drawable.allsports_green_star);
						favouriteStar.setAlpha(200);
					}

					int liveCount = Integer.parseInt( favouriteData.get("iLiveNum").get(( position * 2 ) -3));
					if (liveCount > 0)
						vh.liveImage1.setVisibility(View.VISIBLE);
					else
						vh.liveImage1.setVisibility(View.INVISIBLE);


					if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
						vh.leagueName1.setMaxLines(2);
					} else {
						vh.leagueName1.setMaxLines(2);
						vh.leagueName1.setEllipsize( TruncateAt.END );
					}
					
					vh.leagueName1.setText( (favouriteData.get("vCompetitonName").get(( position * 2 ) -3)).toUpperCase());


					setFavouriteSportBackground(vh.favouriteImage1,  favouriteData.get("vSportType").get(( position * 2 ) -3),  favouriteData.get("vCompetitionId").get(( position * 2 ) -3)  );


					vh.favouriteView1.setTag( R.id.favouriteStar,  (position * 2  -3));
					vh.favouriteTile1.setTag( R.id.favouriteStar,(position * 2 -3) );


					vh.favouriteView1.setOnClickListener( this );
					vh.favouriteTile1.setOnClickListener( this );
				}

				if ( (( position * 2 ) -2) >= 0 && (( position * 2 ) -2 < favouriteData.get( "vCompetitionId" ).size()) ) {

					vh.social_sports_2.setVisibility( View.GONE );

					vh.favouriteTile2.setVisibility(View.VISIBLE);
					vh.item_others.setVisibility( View.VISIBLE );
					if(  position == 1  ) {
						vh.item_placeholder_1.setVisibility(View.VISIBLE);
					} else {
						vh.item_placeholder_1.setVisibility(View.GONE);
					}

					ImageView favouriteStar = (ImageView) vh.favouriteTile2.findViewById(R.id.favouriteStar);
					int favouriteStatus = 0;
					if( favouriteData.get("isFavourite").get(( position * 2 ) -2) != null)
						favouriteStatus =Integer.parseInt( favouriteData.get("isFavourite").get(( position * 2 ) -2));
					if (favouriteStatus == 0) {
						favouriteStar.setAlpha(255);
						favouriteStar.setImageResource(R.drawable.allsports_grey_star);
					} else {
						favouriteStar.setImageResource(R.drawable.allsports_green_star);
						favouriteStar.setAlpha(200);
					}

					int liveCount = Integer.parseInt( favouriteData.get("iLiveNum").get(( position * 2 ) -2));
					if (liveCount > 0)
						vh.liveImage2.setVisibility(View.VISIBLE);
					else
						vh.liveImage2.setVisibility(View.INVISIBLE);


					if( Constants.DENSITY.equalsIgnoreCase("low") || Constants.DENSITY.equalsIgnoreCase("medium") ) {
						vh.leagueName2.setMaxLines(2);
					} else {
						vh.leagueName2.setMaxLines(2);
						vh.leagueName2.setEllipsize( TruncateAt.END );
					}
					
					vh.leagueName2.setText(favouriteData.get("vCompetitonName").get(( position * 2 ) -2).toUpperCase());

					setFavouriteSportBackground( vh.favouriteImage2 , favouriteData.get("vSportType").get(( position * 2 ) -2), favouriteData.get("vCompetitionId").get(( position * 2 ) -2)  );



					vh.favouriteView2.setTag( R.id.favouriteStar, ( position * 2  -2));
					vh.favouriteTile2.setTag( R.id.favouriteStar,( position * 2 -2)) ;
					vh.favouriteView2.setOnClickListener( this );
					vh.favouriteTile2.setOnClickListener( this );

				}


			}


			return convertView;
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
		return convertView;
	}

	/**
	 * Setting the type faces 
	 */


	private void setTypeFaces  ( TextView itemTitle, TextView commentingNumber, TextView uName,TextView  comment_desc,TextView commentTime ) {

		itemTitle.setTypeface(Constants.OPEN_SANS_BOLD);
		commentingNumber.setTypeface(Constants.OPEN_SANS_REGULAR);

		uName.setTypeface(Constants.OPEN_SANS_BOLD);
		comment_desc.setTypeface(Constants.OPEN_SANS_REGULAR);
		commentTime.setTypeface(Constants.OPEN_SANS_REGULAR);


	}



	/**
	 * setting the favourite tile background image
	 */

	public void setFavouriteSportBackground( final RelativeLayout favImage, final String sportType, final String vCompetiotiinId ) {

		try {
			if( sportType == null ) {
				favImage.setBackgroundColor(Color.parseColor("#67D77B"));
				return;
			}
			final ImageView  favouriteImage = (ImageView)favImage.findViewById(R.id.favImage);
			sportBackground = null;


			sportBackground = dbUtil.getSportBackground( vCompetiotiinId );

					Random randomGenerator = new Random();
					int randomIndex = randomGenerator.nextInt( 3 );

					if( sportBackground == null  ) {
						if( sportType.equalsIgnoreCase(Constants.SPORT_AFL) ) {
							sportBackground = afl_images [ randomIndex ];
						} else if( sportType.equalsIgnoreCase(Constants.SPORT_CRICKET) ) {
							sportBackground = cricket_images [ randomIndex ];
						} else if( sportType.equalsIgnoreCase(Constants.SPORT_BASEBALL) ) {
							sportBackground = baseball_images [ randomIndex ];
						} else if( sportType.equalsIgnoreCase(Constants.SPORT_BASKETBALL) ) {
							sportBackground = basketball_images [ randomIndex ];
						} else if( sportType.equalsIgnoreCase(Constants.SPORT_HOCKEY) || sportType.equalsIgnoreCase(Constants.SPORT_ICE_HOCKEY) ) {
							sportBackground = hockey_images [ randomIndex ];
						}  else if( sportType.equalsIgnoreCase(Constants.SPORT_RUGBY_LEAGUE) ) {
							sportBackground = rugby_league_images [ randomIndex ];
						} else if( sportType.equalsIgnoreCase(Constants.SPORT_RUGBY_UNION) ) {
							sportBackground = rugby_union_images[ randomIndex ];
						} else if( sportType.equalsIgnoreCase(Constants.SPORT_NFL) ) {
							sportBackground = nfl_images [ randomIndex ];
						}else if( sportType.equalsIgnoreCase(Constants.SPORT_FOOTBALL) || sportType.equalsIgnoreCase(Constants.SPORT_SOCCER) ) {
							sportBackground = football_images [ randomIndex ];
						} else if(sportType.equalsIgnoreCase(Constants.SPORT_MOTOR_RACING) ) {
							sportBackground = "graphic_formula1";
						}


						if( sportBackground != null ) {
							dbUtil.setSportBackground(vCompetiotiinId, sportBackground);
						}

					}


					
					int imageResourceId = -1;

								if( sportBackground != null ) {
									imageResourceId = PlayUpActivity.context.getResources().getIdentifier(sportBackground , "drawable", PlayUpActivity.context.getPackageName());	
								}

								favImage.setBackgroundColor(Color.parseColor("#73DA85"));

								if( imageResourceId != -1 ) {
									favouriteImage.setImageResource( imageResourceId );
									Drawable d = favouriteImage.getDrawable();
									if(d != null){
										d.setAlpha( 20 );
										d.setColorFilter(Color.parseColor("#67D77B"), Mode.DST_OVER);	
										favouriteImage.setImageDrawable(d);
									}
								} else {
									favouriteImage.setImageDrawable(null);
									favouriteImage.setBackgroundColor(Color.parseColor("#67D77B"));
								}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}	

					

	}




	/**
	 *  showing selected / de selected state of the sports item 
	 */
	private void selectDeSelectState ( View v, boolean isSelected, boolean isFirst  ){ 

		if ( v == null ) {
			return;
		}

		try {
	
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
			// TODO: handle exception
		}
	}


	private float rawX = 0;
	private float rawY = 0;
	long downTime = 0;

	@Override
	public boolean onTouch(final View v, MotionEvent event) {


		if ( event.getAction() == MotionEvent.ACTION_DOWN ) {

			downTime 	=	event.getDownTime();
			rawX			=	event.getRawX();
			rawY			=	event.getRawY();
			// highlight blue color
			startUpdating(v);
		} 
		if ( event.getAction() == MotionEvent.ACTION_UP ) {

			stopUpdating();


			try {
				final int selectedId = Integer.parseInt ( v.getTag().toString() ) ;

				//						if(selectedId == 1)
				//							selectDeSelectState ( v, true, true );
				//						else if(selectedId == 2)
				//							selectDeSelectState ( v, true, false );

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
									Logs.show ( e );
								}
							}
						}, 100);

					}
				}	
				// handle click 
				handleClick ( v );
			}catch (Exception e) {
				// TODO: handle exception
			}

		} 
		else if (event.getAction() == MotionEvent.ACTION_CANCEL
				|| event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			try {
				stopUpdating();
				switch ( Integer.parseInt ( v.getTag().toString() ) ) {

				case 1 : 
					selectDeSelectState( v, false, true );
					break;

				case 2 :
					selectDeSelectState ( v, false, false );
					break;

				}
			} catch (Exception e) {
				// TODO: handle exception
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
	}

	private void handleClick ( View v ) {


		String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
		if ( v != null && v.getTag( R.id.about_txtview ) != null ) {
			if ( v.getTag( R.id.about_txtview ).toString().equalsIgnoreCase( "-1" ) ) {
				

				if ( isUserAnonymous ) {
					// show provider
					Bundle bundle = new Bundle ();
					bundle.putString( "fromFragment", topFragmentName );
					PlayupLiveApplication.getFragmentManagerUtil().setFragment( "ProviderFragment", bundle );
				} else {					
					// re direct it to create room page
					Bundle bundle = new Bundle ();
					bundle.putString("vMainColor",vMainColor );
					bundle.putString("vMainTitleColor",vMainTitleColor );
					bundle.putString("vMainColor",vMainColor );
					bundle.putString("vMainTitleColor",vMainTitleColor );
					bundle.putString( "fromFragment", topFragmentName );
					bundle.putString( "vSecColor",vSecColor );			
					bundle.putString( "vSecTitleColor",vSecTitleColor );

					PlayupLiveApplication.getFragmentManagerUtil().setFragment("CreateLobbyRoomFragment", bundle,-1, false );
				}
			} else {

				// redirect it to private conversation fragment 
				String vConversationId = v.getTag( R.id.about_txtview ).toString();
				Bundle bundle = new Bundle ();
				bundle.putString( "vConversationId", vConversationId );
				bundle.putString( "vContestId", vContestId );
				bundle.putString( "fromFragment", topFragmentName );
				bundle.putString("vMainColor",vMainColor );
				bundle.putString("vMainTitleColor",vMainTitleColor );
				
				bundle.putString( "vSecColor",vSecColor );			
				bundle.putString( "vSecTitleColor",vSecTitleColor );
				PlayupLiveApplication.getFragmentManagerUtil().setFragment("PrivateLobbyRoomFragment", bundle,R.id.main );
			}
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
				Logs.show ( e );
			}
		}
	}


	@Override
	public void onClick(View view) {
		try {
			int position = -1;
			int isFavourite = -1;
			String vCompetitionId = null;

			if ( view.getTag( R.id.favouriteStar) != null)  {
				position = Integer.parseInt(view.getTag( R.id.favouriteStar).toString());
			}

			if( position!=-1 ) {
				vCompetitionId = favouriteData.get ( "vCompetitionId" ).get( position ) ;
				if( favouriteData.get ( "isFavourite" ).get( position ) != null )
					isFavourite = Integer.parseInt( favouriteData.get ( "isFavourite" ).get( position ) );
				else
					isFavourite =0;
			} else{
				return;
			}

			String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
			switch (view.getId()) {
			case R.id.favouriteView1:
			case R.id.favouriteView2:

				try {
					ImageView imageView = (ImageView) view.findViewById(R.id.favouriteStar);
					if ( isFavourite == 1 ) {

						imageView.setImageResource(R.drawable.allsports_grey_star);
						imageView.setAlpha(255);
						favouriteData.get("isFavourite").set(position, "0");

						final String vCompetitionId_temp = vCompetitionId;
						new Thread ( new Runnable() {

							@Override
							public void run() {
								try {
									dbUtil.deSelectMySports ( vCompetitionId_temp );
									dbUtil.setCompetitionFavourite ( vCompetitionId_temp, 0 );
								} catch (Exception e) {
									Logs.show(e);
								} 
							}
						}).start();

					} else { 
						imageView.setImageResource(R.drawable.allsports_green_star);
						imageView.setAlpha(200);

						favouriteData.get("isFavourite").set(position, "1");

						final String vCompetitionId_temp = vCompetitionId;
						new  Thread ( new Runnable() {

							@Override
							public void run() {
								try {
									dbUtil.setSelectedMySports ( vCompetitionId_temp );
									dbUtil.setCompetitionFavourite ( vCompetitionId_temp, 1 );
								} catch (Exception e) {
									Logs.show(e);
								} 
							}
						}).start();
					}
					Message m = new Message();
					m.obj = "RefreshRedTicket";
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(m);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show(e);
				}

				//notifyDataSetChanged();

				break;

			case R.id.favouriteTile1:
			case R.id.favouriteTile2:


				try {
//					Logs.show(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> topFragmentName"+topFragmentName);
					
					Bundle b = new Bundle();

					b.putString("vCompetitionId",vCompetitionId);
					b.putString("fromFragment",topFragmentName);
					b.putString("vMainColor",vMainColor );
					b.putString("vMainTitleColor",vMainTitleColor );
					b.putString( "vSecColor",vSecColor );			
					b.putString( "vSecTitleColor",vSecTitleColor );
					Constants.isGrayBar = true;

					PlayupLiveApplication.getFragmentManagerUtil().setFragment("LeagueLobbyFragment",b);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show(e);
				}


				break;

			default:
				break;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}


	}
	
	
	/**
	 * 
	 * Srolling of list view needs to be monitored, because if list view is
	 * scrolling dont notify the data set. It will slow down the scrolling
	 * effect
	 */

	
	 GridView.OnScrollListener scrollListener = new GridView.OnScrollListener() {
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

				isListScrolling = false;

				notifyDataSetChanged();

				break;

			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:

				isListScrolling = true;

				break;

			case OnScrollListener.SCROLL_STATE_FLING:

				isListScrolling = true;

				break;
			}

		}

	};

	
	
	@Override
	public void notifyDataSetChanged() {
		try {
			if (isListScrolling == false ) {
				super.notifyDataSetChanged();
			}
		} catch ( Exception e ) {
		}

	}
	
	
	
//	/**
//	 * setting ellipsize for textview which has multilines
//	 */
//	public void setEllipsizeText( final TextView textView, final int lines) {
//		textView.setVisibility(View.GONE);
//		try {
//			
//			ViewTreeObserver vto = textView.getViewTreeObserver();
//		     vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//
//		         @Override
//		         public void onGlobalLayout() {
//		             ViewTreeObserver obs = textView.getViewTreeObserver();
//		             obs.removeGlobalOnLayoutListener(this);
//		             if( textView.getLineCount() > lines ) {
//		            	 String textViewText = textView.getText().toString();
//		            	 String text ="";
//		            	 for( int i = 0; i< ( lines-1 ) ; i++ ) {
//		            		 text = text + textViewText.subSequence(textView.getLayout().getLineStart( i ), textView.getLayout().getLineEnd( i ))+"\n";
//		            	 }
//		                 text = text + textViewText.subSequence( textView.getLayout().getLineStart( lines -1 ),  textView.getLayout().getLineEnd( lines -1 )-3)+"...";
//		                 textView.setText(text);
//		             }
//
//		         }
//		     });
//			
//		} catch (Exception e) {
//			Logs.show(e);
//		}
//	     textView.setVisibility( View.VISIBLE );
//		
//	}


	public String cricket_images[] = { "graphic_cricket1", "graphic_cricket2", "graphic_cricket3" };
	public String afl_images[] = { "graphic_afl1", "graphic_afl2", "graphic_afl3" };
	public String baseball_images[] = { "graphic_baseball1", "graphic_baseball2",  "graphic_baseball3"};
	public String football_images[] = { "graphic_football1",  "graphic_football2", "graphic_football3"};
	public String basketball_images[] = { "graphic_basketball1", "graphic_basketball2","graphic_basketball3"};
	public String rugby_union_images[] = { "graphic_rugbyunion1","graphic_rugbyunion2","graphic_rugbyunion3" };
	public String rugby_league_images[] = { "graphic_rugbyleague1", "graphic_rugbyleague2","graphic_rugbyleague3" };
	public String nfl_images[] = { "graphic_americanfootball1",  "graphic_americanfootball2",  "graphic_americanfootball3" };
	public String hockey_images[] = { "graphic_hockey1", "graphic_hockey2", "graphic_hockey3" };

}

