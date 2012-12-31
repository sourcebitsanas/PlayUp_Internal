package com.playup.android.adapters;

import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.fragment.PrivateLobbyInviteFriendFragment;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.ImageDownloader;

import com.playup.android.util.Util;

public class PrivateLobbyInviteFriendsAdapter extends BaseAdapter implements OnClickListener {

	private LayoutInflater inflater;
	private Hashtable < String, List < String > > recentdata ;
	private Hashtable < String, List < String > > alldata ;

	private int totalCount = 2;
	private int HEADING = 2;
	private int totalRecentData = 0;
	private int totalAllData = 0;
	private boolean isTouched = false;
	private int dummyCount = 0;
	private boolean isDummy = false;
	private boolean inSearch = false;
	private String vConversationId = null;
	private ImageDownloader imageDownloader = new ImageDownloader();
	private ListView listView;
	private boolean isListViewScrolling = false;
	public static boolean emptyData = true;
	private String gapId ="";
	private PrivateLobbyInviteFriendFragment inviteFriendFragment;
	DatabaseUtil dbUtil;

	private boolean isGapLoading = false;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;

	private boolean hasZeroFriends;
	
	
	private String vSecColor = null;
	private String vSecTitleColor = null;





	public PrivateLobbyInviteFriendsAdapter ( String vConversationId ,ListView listView, 
			PrivateLobbyInviteFriendFragment privateLobbyInviteFriendFragment  ,String vMainColor,String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		this.vMainColor  = vMainColor;
		this.vMainTitleColor = vMainTitleColor;


		hasZeroFriends = false;
		this.inviteFriendFragment = privateLobbyInviteFriendFragment;
		inSearch = false;
		isDummy = false;
		dummyCount = 0;

		this.listView	=	listView;
		isListViewScrolling = false;
		this.listView.setOnScrollListener(scrollListener);

		this.vConversationId = vConversationId;
		setValues ();

		inflater = (LayoutInflater) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	private void setValues () {

		try {
			hasZeroFriends = false;

			if ( inSearch ) {
				dbUtil = DatabaseUtil.getInstance();
				recentdata = null;

				totalRecentData = 0;
				totalAllData = 0;
				totalCount = 0;

				if ( alldata != null && alldata.get( "vFriendId" ) != null ) {
					totalAllData = alldata.get( "vFriendId" ).size();
					totalCount = totalCount + 1;
				}
				HEADING = totalCount;
				totalCount =  totalCount + totalAllData + totalRecentData;

			} else {

				dbUtil = DatabaseUtil.getInstance();
				recentdata = dbUtil.getRecentInviteFriends ( vConversationId );
				alldata = dbUtil.getConversationFriends ( vConversationId );

				totalRecentData = 0;
				totalAllData = 0;
				totalCount = 0;

				if ( recentdata != null && recentdata.get( "vFriendId" ) != null ) {
					totalRecentData = recentdata.get( "vFriendId" ).size();

					if ( totalRecentData > 0 ) {
						totalCount = totalCount + 1;
					}
				}
				if ( alldata != null && alldata.get( "vFriendId" ) != null ) {
					totalAllData = alldata.get( "vFriendId" ).size();
					totalCount = totalCount + 1;
				}
				HEADING = totalCount;
				totalCount =  totalCount + totalAllData + totalRecentData;

				if ( inviteFriendFragment.isDownloaded &&  ( ( alldata == null && recentdata == null ) 
						|| ( alldata != null && alldata.get( "vFriendId" ).size() == 0 ) 
						&& ( recentdata != null && recentdata.get( "vFriendId" ).size() == 0 ) ) ) {
					inviteFriendFragment.showHideProgress ( false );
					showZeroFriends();
					inviteFriendFragment.showHideProgress ( false );
				}
				else if ( totalRecentData == 0 && totalAllData == 0 ) {
					inviteFriendFragment.showHideProgress ( true );
					emptyData = true;

				} else {
					emptyData = false;

					inviteFriendFragment.showHideProgress ( false );


				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show(e);
		}




	}

	private void showZeroFriends() {
		hasZeroFriends = true;
		totalCount = 1;

	}


	public void setData ( String vConversationId ,ListView listView, PrivateLobbyInviteFriendFragment inviteFriendFragment  , 
			String vMainColor,String vMainTitleColor ,String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		
		this.vMainColor  = vMainColor;
		this.vMainTitleColor = vMainTitleColor;

		hasZeroFriends = false;
		this.inviteFriendFragment = inviteFriendFragment;
		inSearch = false;
		isDummy = false;
		dummyCount = 0;
		gapId = "";
		this.listView	=	listView;
		isListViewScrolling = false;
		this.listView.setOnScrollListener(scrollListener);

		this.vConversationId = vConversationId;
		setValues();

		this.notifyDataSetChanged();

	}

	public void setDummyData ( int count, PrivateLobbyInviteFriendFragment inviteFriendFragment  ) {
		hasZeroFriends = false;
		this.inviteFriendFragment = inviteFriendFragment;
		inSearch = false;
		isDummy = true;
		dummyCount = count;

		notifyDataSetChanged();


	}
	public void setData ( Hashtable < String, List < String > > alldata,ListView listView, PrivateLobbyInviteFriendFragment inviteFriendFragment  ) {
		hasZeroFriends = false;
		this.inviteFriendFragment = inviteFriendFragment;
		inSearch = true;
		isDummy = false;
		dummyCount = 0;

		this.listView	=	listView;
		isListViewScrolling = false;
		this.listView.setOnScrollListener(scrollListener);

		this.alldata = alldata;
		this.recentdata = null;
		setValues();

		this.notifyDataSetChanged();

	}

	@Override
	public int getCount() {

		if ( isDummy ) {
			return dummyCount;
		}

		return totalCount;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	class ViewHolder {
		public RelativeLayout activeUserMainLayout;
		public RelativeLayout friendLayout;

		public TextView active_users_text;
		public ImageView userImage;
		public ImageView firstOnlineIndicator;
		public ImageView secondOnlineIndicator;

		public View rightView;
		public RelativeLayout avatarView;

		public ImageView greenTickImage;
		public ProgressBar friendProgress;
		public ImageView connectPlusImage;
		public TextView userNameText;
		public TextView userNameSubText;
		public TextView noMatchesText;

		private RelativeLayout gapView;
		private TextView gapTextView;
		private TextView gapMoreTextView;
		private LinearLayout  gapTextLayout;
		private ProgressBar gapProgress;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if ( inflater == null ) {
			LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return  layoutInflater.inflate( R.layout.invite_friends_view, null);
		}
		ViewHolder vh = null;
		if ( convertView == null ) {
			vh = new ViewHolder();

			convertView = inflater.inflate(R.layout.invite_friends_view, null);

			vh.activeUserMainLayout = (RelativeLayout) convertView.findViewById(R.id.activeUserMainLayout); 
			vh.active_users_text = (TextView) convertView.findViewById(R.id.active_users_text);


			vh.friendLayout         = ( RelativeLayout ) convertView.findViewById( R.id.friend_view );
			vh.userNameText = ( TextView) convertView.findViewById( R.id.userNameText);
			vh.userNameSubText = ( TextView ) convertView.findViewById( R.id.userNameSubText);
			vh.userImage = (ImageView) convertView.findViewById(R.id.imageViewpostAvatar);
			vh.firstOnlineIndicator = (ImageView) convertView.findViewById(R.id.providerImage);
			vh.secondOnlineIndicator = (ImageView) convertView.findViewById(R.id.greenDot);

			vh.avatarView = ( RelativeLayout ) convertView.findViewById( R.id.avatarView );
			vh.rightView = convertView.findViewById( R.id.rightView );
			vh.greenTickImage = (ImageView) convertView.findViewById(R.id.greenTickImage);
			vh.connectPlusImage = (ImageView) convertView.findViewById(R.id.connectPlusImage);
			vh.friendProgress = (ProgressBar) convertView.findViewById(R.id.friendProgress);
			vh.noMatchesText	=	(TextView) convertView.findViewById(R.id.nomatches);

			vh.gapView = (RelativeLayout) convertView.findViewById(R.id.friendGapLinearView);
			vh.gapMoreTextView = (TextView) convertView.findViewById(R.id.friendGapMoreTextView);

			vh.gapTextLayout = (LinearLayout)convertView.findViewById(R.id.gapTextLayout);
			vh.gapProgress = (ProgressBar)convertView.findViewById(R.id.gapProgress);


			vh.gapTextView = (TextView) convertView.findViewById(R.id.friendGapTextView);

			setTypefaces( vh );
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}


		vh.noMatchesText.setVisibility(View.GONE);
		convertView.setPadding( 5, 0, 5, 0 );


		if ( hasZeroFriends ) {

			vh.noMatchesText.setVisibility(View.GONE);
			vh.gapView.setVisibility(View.GONE);		
			vh.friendLayout.setVisibility( View.GONE );
			vh.activeUserMainLayout.setVisibility(View.VISIBLE);
			vh.active_users_text.setVisibility(View.VISIBLE);
			vh.active_users_text.setText( totalAllData + " "+ PlayUpActivity.context.getResources().getString(R.string.friend) );
			vh.rightView.setOnClickListener( null );
			return convertView;

		} 

		vh.avatarView.setVisibility( View.VISIBLE );
		vh.avatarView.setBackgroundResource( R.drawable.post_avatar );



		if ( isDummy ) {

			if ( position == 0 ) {

				vh.avatarView.setVisibility(View.GONE);
				vh.activeUserMainLayout.setVisibility(View.GONE );
				vh.friendLayout.setVisibility( View.VISIBLE );
				vh.gapView.setVisibility( View.GONE);
				vh.firstOnlineIndicator.setVisibility(View.GONE);
				vh.userNameText.setVisibility( View.GONE );
				vh.noMatchesText.setVisibility(View.VISIBLE);
				vh.userNameSubText.setVisibility(View.GONE);
				vh.firstOnlineIndicator.setVisibility(View.GONE);
				vh.secondOnlineIndicator .setVisibility(View.GONE);
				vh.rightView.setVisibility(View.GONE);
				vh.greenTickImage.setVisibility(View.GONE);
				vh.connectPlusImage .setVisibility(View.GONE);
				vh.friendProgress .setVisibility(View.GONE);
				// vh.avatarView.setVisibility( View.GONE );
				vh.userImage.setImageResource( R.drawable.head );
				vh.userImage .setVisibility(View.INVISIBLE);
				vh.friendLayout .setBackgroundResource(R.drawable.list_base_top);
				//	vh.friendLayout .setBackgroundResource(R.drawable.list_base_single);

				convertView.setPadding( 5, 0, 5, 0 );

			} else {

				if(position == dummyCount - 1){
					vh.friendLayout.setBackgroundResource(R.drawable.list_base_bottom);
					convertView.setPadding( 5, 0, 5, 5 );
				}
				else{
					vh.friendLayout.setBackgroundResource(R.drawable.list_base_mid);
				}

				vh.gapView.setVisibility( View.GONE);
				vh.activeUserMainLayout.setVisibility(View.GONE );
				vh.friendLayout.setVisibility( View.VISIBLE );

				vh.firstOnlineIndicator.setVisibility(View.GONE);
				vh.userNameText.setVisibility(View.GONE);
				vh.userNameSubText.setVisibility(View.GONE);
				vh.noMatchesText.setVisibility(View.GONE);
				vh.firstOnlineIndicator.setVisibility(View.GONE);
				vh.secondOnlineIndicator .setVisibility(View.GONE);
				vh.avatarView.setVisibility(View.GONE);
				vh.rightView.setVisibility(View.GONE);
				vh.greenTickImage.setVisibility(View.GONE);
				vh.connectPlusImage .setVisibility(View.GONE);
				vh.friendProgress .setVisibility(View.GONE);
				
				vh.userImage.setImageResource( R.drawable.head );
				vh.userImage .setVisibility(View.GONE);

			}
			convertView.setOnTouchListener(null);

			return convertView;
		}
		if ( inSearch ) {


			if ( position == 0 ) {
				vh.noMatchesText.setVisibility(View.GONE);
				vh.gapView.setVisibility(View.GONE);		
				vh.friendLayout.setVisibility( View.GONE );
				vh.activeUserMainLayout.setVisibility(View.VISIBLE);
				vh.active_users_text.setVisibility(View.VISIBLE);

				vh.active_users_text.setText( Constants.searchFriendsResults + " "+PlayUpActivity.context.getResources().getString(R.string.matches) );
				vh.rightView.setOnClickListener( null );

				convertView.setPadding( 5, 5, 5, 0 ); 

				return convertView;
			} 




		}

		if (position == 0 && totalRecentData > 0 ) {
			vh.noMatchesText.setVisibility(View.GONE);
			vh.gapView.setVisibility(View.GONE);		
			vh.friendLayout.setVisibility( View.GONE );
			vh.activeUserMainLayout.setVisibility(View.VISIBLE);
			vh.active_users_text.setVisibility(View.VISIBLE);

			vh.active_users_text.setText( R.string.recentInvites );
			vh.rightView.setOnClickListener( null );

			convertView.setPadding( 5, 5, 5, 0 ); 

			return convertView;
		}  







		if ( ( position == ( HEADING -1 ) && totalRecentData == 0 ) || ( position  == totalRecentData + ( HEADING -1 ) ) ) {

			if ( position == 0 ) {
				convertView.setPadding( 5, 5, 5, 0 ); 
			}
			vh.gapView.setVisibility(View.GONE);	
			vh.friendLayout.setVisibility( View.GONE );
			vh.activeUserMainLayout.setVisibility(View.VISIBLE);
			vh.noMatchesText.setVisibility(View.GONE);

			
			
			

			String totalCount = dbUtil.getTotalFriendsCount();
			if(totalCount != null && totalCount.trim().length() > 0){
				vh.active_users_text.setVisibility(View.VISIBLE);
				vh.active_users_text.setText(totalCount+" "+PlayUpActivity.context.getResources().getString( R.string.friends ));
				
			}else{
				vh.active_users_text.setVisibility(View.GONE);
				
			}
			
			
			

			vh.rightView.setOnClickListener( null );

			return convertView;
		}

		if ( ( position - HEADING  ) < totalRecentData  && totalRecentData > 0 ) {
			vh.gapView.setVisibility(View.GONE);	
			vh.activeUserMainLayout.setVisibility(View.GONE );
			vh.friendLayout.setVisibility( View.VISIBLE );


			vh.noMatchesText.setVisibility(View.GONE);

			if ( position - 1 == 0 ) {
				vh.friendLayout.setBackgroundResource( R.drawable.list_base_top );

			}
			else if ( position == totalRecentData  ) {
				vh.friendLayout.setBackgroundResource( R.drawable.list_base_bottom);
			} else {
				vh.friendLayout.setBackgroundResource( R.drawable.list_base_mid);
			}

			if ( totalRecentData == 1){
				vh.friendLayout.setBackgroundResource( R.drawable.list_base_single );	
			} 

			vh.avatarView.setVisibility( View.VISIBLE );
			vh.userNameText.setVisibility( View.VISIBLE );
			vh.userNameSubText.setVisibility( View.VISIBLE );




			if ( recentdata.get( "vSourceName" ) !=  null && recentdata.get( "vSourceName" ).get( position - 1 ) != null && recentdata.get( "vSourceName" ).get( position - 1 ).equalsIgnoreCase( "playup" ) ) {

				String recentInviteUname = recentdata.get( "vFriendUserName" ).get( position -1 );

				if ( recentInviteUname == null || ( recentInviteUname != null  && recentInviteUname.equalsIgnoreCase("null") ) ){

					vh.userNameText.setText( new Util().getSmiledText(recentdata.get( "vFriendName" ).get( position -1 )) );
					vh.userNameText.setTextColor(Color.parseColor("#FF4754"));
					vh.userNameSubText.setText( new Util().getSmiledText(recentdata.get( "vFriendName" ).get( position -1 ) ));
				}else{

					vh.userNameText.setText( new Util().getSmiledText(recentdata.get( "vFriendUserName" ).get( position -1 )) );
					vh.userNameSubText.setText( new Util().getSmiledText(recentdata.get( "vFriendName" ).get( position -1 )) );

					vh.userNameText.setTextColor(Color.parseColor("#FF4754"));
				}


			} else {
				vh.userNameText.setText( new Util().getSmiledText(recentdata.get( "vFriendName" ).get( position -1 )) );
				vh.userNameSubText.setText( "" );
				vh.userNameText.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
				vh.userNameText.setTextColor(Color.parseColor("#736E73"));
			}

			vh.avatarView.setTag( recentdata.get( "vFriendId" ).get( position -1 ) );
			vh.avatarView.setOnClickListener( this );

			vh.userImage.setVisibility( View.VISIBLE );
			vh.userImage.setImageResource( R.drawable.head );			
			vh.firstOnlineIndicator.setImageBitmap(null);
			vh.firstOnlineIndicator.setVisibility( View.VISIBLE );

			if ( recentdata.get( "vFriendAvatar" ).get( position -1 ) != null ) {
				vh.userImage.setBackgroundColor(Color.WHITE);
				imageDownloader.download( recentdata.get( "vFriendAvatar" ).get( position -1 ),  vh.userImage, true, this );
			}

			if ( recentdata.get( "vSourceIconHref" ).get( position -1 ) != null ) {
				imageDownloader.download( recentdata.get( "vSourceIconHref" ).get( position -1 ),  vh.firstOnlineIndicator, false, this );
			}

			if ( recentdata.get( "isOnline" ).get( position -1 ) == null ) {
				recentdata.get( "isOnline" ).set( position - 1 , "0" );
			}
			if ( Integer.parseInt( recentdata.get( "isOnline" ).get( position -1 ) ) == 0 ) {
				vh.secondOnlineIndicator.setVisibility( View.INVISIBLE );
			} else {
				vh.secondOnlineIndicator.setVisibility( View.VISIBLE );
			}


			if ( recentdata.get( "iStatus" ).get( position -1 ) == null ) {
				recentdata.get( "iStatus" ).set( position - 1 , "0" );
			}
			int iStatus = Integer.parseInt( recentdata.get( "iStatus" ).get( position - 1 ) );



			
			vh.rightView.setVisibility( View.VISIBLE );
			if ( recentdata.get( "vFriendId" ).get( position -1 ) != null ) {
				vh.rightView.setTag( recentdata.get( "vFriendId" ).get( position -1 ) );
				vh.rightView.setTag( R.id.aboutScrollView, recentdata.get( "vSourceName" ).get( position -1 ) );
			}
			vh.rightView.setTag( R.id.about_txtview, iStatus );
			vh.rightView.setOnClickListener( this );

			if ( iStatus == 2  ) {

				vh.greenTickImage.setVisibility( View.VISIBLE );
				vh.friendProgress.setVisibility( View.GONE );
				vh.connectPlusImage.setVisibility( View.GONE );

				return convertView;
			} else if ( iStatus == 1  ) {

				vh.greenTickImage.setVisibility( View.GONE );
				vh.friendProgress.setVisibility( View.VISIBLE );
				vh.connectPlusImage.setVisibility( View.GONE );

				return convertView;
			} else {

				vh.greenTickImage.setVisibility( View.GONE );
				vh.friendProgress.setVisibility( View.GONE );
				vh.connectPlusImage.setVisibility( View.VISIBLE );

				return convertView;
			}

		}


		if ( ( position - totalRecentData - HEADING ) <= totalAllData ) {

			vh.noMatchesText.setVisibility(View.GONE);
			vh.gapView.setVisibility(View.GONE);
			vh.activeUserMainLayout.setVisibility(View.GONE );
			vh.friendLayout.setVisibility( View.VISIBLE );


			if ( ( position - totalRecentData - HEADING  ) == 0 ) {
				vh.friendLayout.setBackgroundResource( R.drawable.list_base_top );

			} else if ( position - totalRecentData - HEADING == totalAllData - 1 ) {
				vh.friendLayout.setBackgroundResource( R.drawable.list_base_bottom);
				convertView.setPadding( 5, 0, 5, 5 ); 
			}
			else {
				vh.friendLayout.setBackgroundResource( R.drawable.list_base_mid);
			}


			if ( totalAllData == 1){
				vh.friendLayout.setBackgroundResource( R.drawable.list_base_single );
			} 
			vh.avatarView.setVisibility( View.VISIBLE );
			vh.userNameText.setVisibility( View.VISIBLE );
			vh.userNameSubText.setVisibility( View.VISIBLE );


			if ( alldata.get( "vSourceName" ).get( position - totalRecentData - HEADING ).equalsIgnoreCase( "playup" ) ) {

				String uName = alldata.get( "vFriendUserName" ).get( position - totalRecentData - HEADING );

				if( uName.equalsIgnoreCase( "null" )){

					vh.userNameText.setText( new Util().getSmiledText(alldata.get( "vFriendName" ).get( position - totalRecentData - HEADING )) );
					vh.userNameText.setTextColor(Color.parseColor("#FF4754"));
					vh.userNameSubText.setText( new Util().getSmiledText(alldata.get( "vFriendName" ).get( position - totalRecentData - HEADING )) );
				}else{

					vh.userNameText.setText( new Util().getSmiledText(alldata.get( "vFriendUserName" ).get( position - totalRecentData - HEADING )) );
					vh.userNameSubText.setText( new Util().getSmiledText(alldata.get( "vFriendName" ).get( position - totalRecentData - HEADING )) );
					vh.userNameText.setTextColor(Color.parseColor("#FF4754"));
				}

			} else {
				vh.userNameText.setText( new Util().getSmiledText(alldata.get( "vFriendName" ).get( position - totalRecentData - HEADING ) ));
				vh.userNameText.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
				vh.userNameText.setTextColor(Color.parseColor("#736E73"));
				vh.userNameSubText.setText( "" );
			}

			vh.avatarView.setTag( alldata.get( "vFriendId" ).get( position - totalRecentData - HEADING ) );
			vh.avatarView.setOnClickListener( this );

			vh.userImage.setVisibility( View.VISIBLE );
			vh.userImage.setImageResource( R.drawable.head );
			vh.firstOnlineIndicator.setVisibility( View.VISIBLE );
			vh.userImage.setBackgroundColor(Color.WHITE);
			imageDownloader.download( alldata.get( "vFriendAvatar" ).get( position - totalRecentData - HEADING ),  vh.userImage, true, this );
			imageDownloader.download( alldata.get( "vSourceIconHref" ).get( position - totalRecentData - HEADING ),  vh.firstOnlineIndicator, false, this );

			if(alldata.get( "isOnline" ).get( position - totalRecentData - HEADING ) != null 
					&& alldata.get( "isOnline" ).get( position - totalRecentData - HEADING ).trim().length() > 0){
				if ( Integer.parseInt( alldata.get( "isOnline" ).get( position - totalRecentData - HEADING ) ) == 0 ) {
					vh.secondOnlineIndicator.setVisibility( View.INVISIBLE );
				} else {
					vh.secondOnlineIndicator.setVisibility( View.VISIBLE );
				}
			}
			if ( alldata.get( "iStatus" ).get( position - totalRecentData - HEADING ) == null )  {
				alldata.get( "iStatus" ).set( position - totalRecentData - HEADING, "0" );
			}
			int iStatus = Integer.parseInt( alldata.get( "iStatus" ).get( position - totalRecentData - HEADING ) );

			vh.rightView.setVisibility( View.VISIBLE );
			vh.rightView.setTag( alldata.get( "vFriendId" ).get( position - totalRecentData - HEADING ) );
			vh.rightView.setTag( R.id.aboutScrollView, alldata.get( "vSourceName" ).get( position - totalRecentData - HEADING ) );
			vh.rightView.setTag( R.id.about_txtview, iStatus );
			vh.rightView.setOnClickListener( this );

			if ( iStatus == 0 ) {

				vh.greenTickImage.setVisibility( View.GONE );
				vh.friendProgress.setVisibility( View.GONE );
				vh.connectPlusImage.setVisibility( View.VISIBLE );


			} else if ( iStatus == 1 ) {


				vh.greenTickImage.setVisibility( View.GONE );
				vh.friendProgress.setVisibility( View.VISIBLE );
				vh.connectPlusImage.setVisibility( View.GONE );



			} else {
				vh.greenTickImage.setVisibility( View.VISIBLE );
				vh.friendProgress.setVisibility( View.GONE );
				vh.connectPlusImage.setVisibility( View.GONE );


			}
			final String gapUid = alldata.get("vGapId").get(position - totalRecentData -  HEADING );
			if ( gapUid != null && gapUid.trim().length() > 0 ) {
				final LinearLayout gapTextLayout = vh.gapTextLayout;
				final ProgressBar gapProgress = vh.gapProgress;

				convertView.setPadding( 5, 0, 5, 5 ); 
				vh.gapView.setVisibility(View.VISIBLE);			

				vh.gapProgress.setVisibility(View.GONE);
				vh.gapTextLayout.setVisibility(View.VISIBLE);

				vh.activeUserMainLayout.setVisibility(View.GONE);
				vh.friendLayout.setVisibility(View.GONE);
				dbUtil = DatabaseUtil.getInstance();
				final Hashtable<String, List<String>> gapData = dbUtil.getGapUrl(gapUid);

				if (gapData != null && gapData.get("gap_url").size() > 0){

					if( ( gapId != null && !gapId.equalsIgnoreCase( gapUid ) )){
						isGapLoading = false;
						vh.gapTextView.setText(""+ gapData.get("gap_size").get(0));
					}
					else {
						vh.gapTextLayout.setVisibility(View.GONE);
						vh.gapProgress.setVisibility(View.VISIBLE);	
					}


				}


				vh.gapView.setTag(gapUid);

				vh.gapTextView.setFocusable(true);
				if(inSearch){
					if ( isGapLoading || Constants.isSearchGapDownloading) {


						vh.gapTextLayout.setVisibility(View.GONE);
						vh.gapProgress.setVisibility(View.VISIBLE);
					}


				}
				else if ( isGapLoading || Constants.isFriendsGapDownloading) {


					vh.gapTextLayout.setVisibility(View.GONE);
					vh.gapProgress.setVisibility(View.VISIBLE);
				}


				vh.gapView.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {

						if ( !isGapLoading ) {
							String gapUid = v.getTag().toString();
							gapId = gapUid;
							if (event.getAction() == MotionEvent.ACTION_DOWN) {
								if (gapUid != null && gapUid.trim().length() > 0) {
									isTouched = true;
									gapId =gapUid;
								}
							} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
								if (gapUid != null && gapUid.trim().length() > 0) {
									gapTextLayout.setVisibility(View.GONE);
									gapProgress.setVisibility(View.VISIBLE);
									isGapLoading = true;
									isTouched = false;

									String  topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
									if ( PlayupLiveApplication.getFragmentManagerUtil().checkIfFragmentExists( topFragmentName ) ) {
										if ( PlayupLiveApplication.getFragmentManagerUtil().fragmentMap != null && PlayupLiveApplication.getFragmentManagerUtil().fragmentMap.containsKey( topFragmentName ) )  {

											PrivateLobbyInviteFriendFragment fragment = ( PrivateLobbyInviteFriendFragment ) PlayupLiveApplication.getFragmentManagerUtil().fragmentMap.get( topFragmentName ); 
											if ( fragment != null ) {
												
												if (gapData.get("gap_href_url").get(0) != null && 
														gapData.get("gap_href_url").get(0).trim().length() > 0 && 
														fragment.runnableList != null && 
														!fragment.runnableList.containsKey(gapData.get("gap_href_url").get(0))  
														&& Util.isInternetAvailable() ){

													if(inSearch){
														fragment.runnableList.put(gapData.get("gap_href_url").get(0), 
																new Util().callNextSearchFriendsUrl(gapData.get("gap_href_url").get(0), 
																		gapUid,fragment.runnableList,true));
													}
													else{
														fragment.runnableList.put(gapData.get("gap_href_url").get(0), 
																new Util().callNextFriendsUrl(gapData.get("gap_href_url").get(0), 
																		gapUid,fragment.runnableList,true));
													}

												}else if (fragment.runnableList != null && 
														!fragment.runnableList.containsKey(gapData.get("gap_url").get(0))  
														&& Util.isInternetAvailable() ){

													if(inSearch){
														fragment.runnableList.put(gapData.get("gap_url").get(0), 
																new Util().callNextSearchFriendsUrl(gapData.get("gap_url").get(0), 
																		gapUid,fragment.runnableList,false));
													}
													else{
														fragment.runnableList.put(gapData.get("gap_url").get(0), 
																new Util().callNextFriendsUrl(gapData.get("gap_url").get(0), 
																		gapUid,fragment.runnableList,false));
													}

												}
											}
										}
									}




									v.setOnTouchListener(null);
								}

							} 
						}
						return true;		
					}
				});

			}
		}
		return convertView;
	}





	private void setTypefaces ( ViewHolder vh ) {

		vh.active_users_text.setTypeface ( Constants.OPEN_SANS_BOLD );
		vh.userNameSubText.setTypeface ( Constants.OPEN_SANS_REGULAR );
		vh.userNameText.setTypeface( Constants.OPEN_SANS_SEMIBOLD);

		vh.gapTextView.setTypeface(Constants.OPEN_SANS_BOLD);
		vh.gapMoreTextView.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		vh.noMatchesText.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
	}

	@Override
	public void onClick(View v) {

		try {
			if ( v == null  ) {
				return;
			}
			if ( v.getId() == R.id.avatarView ) {
				if ( v.getTag() == null ) {
					return;
				}
				String vFriendId = v.getTag().toString();

				dbUtil = DatabaseUtil.getInstance();
				Hashtable<String, Object> result = dbUtil.getProfileUrlFromFriendId (  vFriendId );
				String vSelfUrl = "";
				Boolean isHrefUrl = false;
				if(result!=null && result.contains("url")&& result.contains("isHref")){
					vSelfUrl = (String) result.get("url");
					isHrefUrl = (Boolean) result.get("isHref");
					}

				if ( vSelfUrl == null ) {

					return;
				}
				if ( vSelfUrl.trim().length() == 0 ) {

					return;
				}
				int myId=-1;

				Bundle bundle = new Bundle();
				bundle.putString( "vSelfUrl", vSelfUrl );
				bundle.putInt("myId", -1);
				bundle.putString("vMainColor",vMainColor );
				 bundle.putString("vMainTitleColor",vMainTitleColor );
				 
				 bundle.putString( "vSecColor",vSecColor );			
					bundle.putString( "vSecTitleColor",vSecTitleColor );
				
				PlayupLiveApplication.getFragmentManagerUtil().setFragment( "PublicProfileFragment", bundle );
				return;
			}
			if ( v.getTag() != null && v.getTag( R.id.about_txtview ) != null ) {

				int status = Integer.parseInt( v.getTag( R.id.about_txtview ).toString() );

				if ( status == 0 ) {
					String vFriendId = v.getTag().toString() ;

					if ( v.getTag( R.id.aboutScrollView ) != null ) {
						String vSourceName =  v.getTag( R.id.aboutScrollView ).toString();

						if ( !dbUtil.isIdentifierProviderName( vSourceName ) ) {

							Hashtable<String, List<String>> data = dbUtil.getProviderUrls ( vSourceName );

							if ( data != null && data.get( "vLoginUrl" ) != null && data.get( "vLoginUrl" ).size() > 0 ) {
								Bundle bundle = new Bundle();
								String  topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
								bundle.putString("vLoginUrl", data.get("vLoginUrl").get( 0 ) );
								bundle.putString("vSuccessUrl", data.get("vSuccessUrl").get( 0 ));
								bundle.putString("vFailureUrl", data.get("vFailureUrl").get( 0 )); 
								bundle.putString("fromFragment", topFragmentName );

								PlayupLiveApplication.getFragmentManagerUtil().setFragment ( "LoginWebViewFragment", bundle );
								return;
							}
						}

					}
					dbUtil = DatabaseUtil.getInstance();
					dbUtil.setRecentInvite ( vFriendId, vConversationId, 1, true , false );

					setValues();
					notifyDataSetChanged();
					new Util().sendPrivateLobbyInvitation ( vFriendId, vConversationId );
				}
			}
		} catch (Exception e) {
			//Logs.show(e);
		}
	}



	@Override
	public void notifyDataSetChanged(){
		if(isListViewScrolling  ==  false && isTouched == false  ){
			super.notifyDataSetChanged();  
		}

	}

	/**
	 *
	 * Srolling of list view needs to be monitored, because if list view is scrolling dont notify the data set. It will slow down the scrolling effect
	 */

	ListView.OnScrollListener  scrollListener = new ListView.OnScrollListener(){
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

				isListViewScrolling  =  false;
				notifyDataSetChanged();

				break;

			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:

				isListViewScrolling  =  true;

				break;

			case OnScrollListener.SCROLL_STATE_FLING:

				isListViewScrolling  =  true;

				break;
			}

		}

	};
}
