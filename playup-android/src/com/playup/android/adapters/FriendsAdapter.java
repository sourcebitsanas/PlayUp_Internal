package com.playup.android.adapters;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

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
import com.playup.android.fragment.FriendsFragment;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.ImageDownloader;

import com.playup.android.util.Util;

public class FriendsAdapter extends BaseAdapter implements OnTouchListener, OnClickListener {

	public static boolean emptyData = true;
	private LayoutInflater inflater;

	private boolean isDummy = false;
	private Hashtable<String, List<String>> onlineFriends;
	private Hashtable<String, List<String>> otherFriends;

	private int totalOnlineFriends = 0, totalotherFriends = 0;
	private int totalCount = 0;

	private ImageDownloader imageDownloader = new ImageDownloader();

	private boolean search;

	private ListView mListView;
	private boolean isListViewScrolling = false;

	private int HEADINGS = 0;

	private boolean roomAvail = false;

	private String gapId = "";
	private boolean isTouched = false;
	private boolean isGapLoading = false;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;



	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public FriendsAdapter(){
		
		try {
			inflater = (LayoutInflater) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	Logs.show(e);
		}
		
	}
	
	public FriendsAdapter(String vMainColor, String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		this.vMainColor = vMainColor;
		this.vMainTitleColor = vMainTitleColor;
		
		inflater = (LayoutInflater) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public FriendsAdapter(Hashtable<String, List<String>> otherFriendsData,
			Hashtable<String, List<String>> onlineFriendsData,
			ListView mListView, String vMainColor, String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;

		this.vMainColor = vMainColor;
		this.vMainTitleColor  = vMainTitleColor;
		

		hasZeroFriends = false;
		search = false;
		isDummy = false;
		isListViewScrolling = false;

		inflater = (LayoutInflater) PlayUpActivity.context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.otherFriends = otherFriendsData;
		this.onlineFriends = onlineFriendsData;
		this.mListView = mListView;
		this.mListView.setOnScrollListener(scrollListener);

		totalOnlineFriends = 0;
		totalotherFriends = 0;
		HEADINGS = 0;

		if (otherFriendsData != null && otherFriendsData.get("vFriendId") != null ) {
			totalotherFriends = otherFriendsData.get("vFriendId").size();
			HEADINGS = HEADINGS + 1;
		}

		if (onlineFriendsData != null && onlineFriendsData.get("vFriendId") != null ) {
			totalOnlineFriends = onlineFriendsData.get("vFriendId").size();
			if (totalOnlineFriends > 0) {
				HEADINGS = HEADINGS + 1;
			}
		}
		totalCount = totalOnlineFriends + totalotherFriends + HEADINGS;

		if ( (totalOnlineFriends + totalotherFriends ) > 0 ) {

			emptyData = false ;


		} else {
			emptyData = true ;
		}
	}

	public FriendsAdapter(Hashtable<String, List<String>> allFriendsData,
			ListView mListView, String vMainColor, String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;

		this.vMainColor = vMainColor;
		this.vMainTitleColor  = vMainTitleColor;
		


		inflater = (LayoutInflater) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		hasZeroFriends = false;
		isDummy = false;
		search = true;
		isListViewScrolling = false;

		totalOnlineFriends = 0;
		HEADINGS = 0;

		this.otherFriends = allFriendsData;
		this.onlineFriends = null;
		this.mListView = mListView;
		this.mListView.setOnScrollListener(scrollListener);

		if (allFriendsData != null && allFriendsData.get("vFriendId") != null ) {
			totalotherFriends = allFriendsData.get("vFriendId").size();
			HEADINGS = HEADINGS + 1;
		}

		totalCount = totalOnlineFriends + totalotherFriends + HEADINGS;
	}

	private boolean hasZeroFriends = false;
	private String vMainColor = null;
	private String vMainTitleColor = null;
	public void showzeroFriends () {

		hasZeroFriends = true;
		totalCount = 1;
	}

	@Override
	public int getCount() {
		return totalCount;
	}

	public void setData(Hashtable<String, List<String>> otherFriendsData,
			Hashtable<String, List<String>> onlineFriendsData,
			ListView mListView, String vMainColor, String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;

		this.vMainColor = vMainColor;
		this.vMainTitleColor  = vMainTitleColor;
		


		hasZeroFriends = false;
		isDummy = false;
		search = false;
		isListViewScrolling = false;
		gapId = "";
		totalOnlineFriends = 0;
		totalotherFriends = 0;
		HEADINGS = 0;

		if ( this.otherFriends !=  null ) {
			this.otherFriends.clear();
			this.otherFriends = null;
		}
		if ( this.onlineFriends !=  null ) {
			this.onlineFriends.clear();
			this.onlineFriends = null;
		}

		this.otherFriends = otherFriendsData;
		this.onlineFriends = onlineFriendsData;
		this.mListView = mListView;
		this.mListView.setOnScrollListener(scrollListener);

		if (otherFriendsData != null && otherFriendsData.get("vFriendId") != null ) {
			totalotherFriends = otherFriendsData.get("vFriendId").size();
			HEADINGS = HEADINGS + 1;
		}

		if (onlineFriendsData != null &&  onlineFriendsData.get("vFriendId") != null ) {
			totalOnlineFriends = onlineFriendsData.get("vFriendId").size();
			if (totalOnlineFriends > 0) {
				HEADINGS = HEADINGS + 1;
			}
		}

		totalCount = totalOnlineFriends + totalotherFriends + HEADINGS;

		if ( (totalOnlineFriends + totalotherFriends ) > 0 ) {
			emptyData = false ;
		} else {
			emptyData = true ;
		}
		notifyDataSetChanged();
	}

	public void setData(Hashtable<String, List<String>> friendsData,
			boolean refreshLive, String vMainColor, String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;

		this.vMainColor = vMainColor;
		this.vMainTitleColor  = vMainTitleColor;
		

		hasZeroFriends = false;
		search = false;
		isDummy = false;

		HEADINGS = 0;
		if (refreshLive) {
			totalOnlineFriends = 0;

			if ( this.onlineFriends !=  null ) {
				this.onlineFriends.clear();
				this.onlineFriends = null;
			}
			this.onlineFriends = friendsData;

			if (friendsData != null && friendsData.get("vFriendId") != null )
				totalOnlineFriends = friendsData.get("vFriendId").size();
			if (totalOnlineFriends > 0) {
				HEADINGS = HEADINGS + 1;
			}

			if (otherFriends != null && otherFriends.get("vFriendId") != null ) {
				totalotherFriends = otherFriends.get("vFriendId").size();
				HEADINGS = HEADINGS + 1;
			}
		} else {

			if ( this.otherFriends !=  null ) {
				this.otherFriends.clear();
				this.otherFriends = null;
			}

			this.otherFriends = friendsData;

			if (friendsData != null && friendsData.get("vFriendId") != null ) {
				totalotherFriends = friendsData.get("vFriendId").size();
				HEADINGS = HEADINGS + 1;
			}

			if (onlineFriends != null && onlineFriends.get("vFriendId") != null ) {
				totalOnlineFriends = onlineFriends.get("vFriendId").size();
				if (totalOnlineFriends > 0) {
					HEADINGS = HEADINGS + 1;
				}
			}
		}

		totalCount = totalOnlineFriends + totalotherFriends + HEADINGS;

		if ( (totalOnlineFriends + totalotherFriends ) > 0 ) {
			emptyData = false ;
		} else {
			emptyData = true ;
		}
		notifyDataSetChanged();
	}

	public void setData(Hashtable<String, List<String>> allFriendsData, String vMainColor, String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;

		this.vMainColor = vMainColor;
		this.vMainTitleColor  = vMainTitleColor;
		

		hasZeroFriends = false;
		isDummy = false;
		search = true;


		if ( this.onlineFriends !=  null ) {
			this.onlineFriends.clear();
			this.onlineFriends = null;
		}

		totalOnlineFriends = 0;
		totalotherFriends = 0;
		HEADINGS = 0;

		if ( this.otherFriends !=  null ) {
			this.otherFriends.clear();
			this.otherFriends = null;
		}

		this.otherFriends = allFriendsData;

		if (allFriendsData != null && allFriendsData.get("vFriendId") != null ) {
			totalotherFriends = allFriendsData.get("vFriendId").size();
			HEADINGS = HEADINGS + 1;
		}

		if (onlineFriends != null && onlineFriends.get("vFriendId") != null ) {
			totalOnlineFriends = onlineFriends.get("vFriendId").size();
			if (totalOnlineFriends > 0) {
				HEADINGS = HEADINGS + 1;
			}
		}

		totalCount = totalOnlineFriends + totalotherFriends + HEADINGS;
		notifyDataSetChanged();

	}

	public void setDummyData(int count) {

		totalCount = 20;
		isDummy = true;
		notifyDataSetChanged();

	}

	class ViewHolder {

		public TextView activeUsers;
		public TextView activerUserNum;
		public ImageView userImage;
		public ImageView firstOnlineIndicator;
		public ImageView secondOnlineIndicator;
		public TextView userName;
		public TextView userFullName;
		public TextView dividerLeftText;
		public TextView dividerRightText;
		public RelativeLayout activeUserMainLayout;
		public RelativeLayout footerView;
		public ImageView inviteImage;
		public TextView noMatches;
		public ProgressBar friendProgress;
		private RelativeLayout avatarView;
		private RelativeLayout friendView;
		private RelativeLayout gapView;
		private TextView gapTextView;
		private TextView gapMoreTextView;
		private RelativeLayout middleView;
		private LinearLayout  gapTextLayout;
		private ProgressBar gapProgress;
		private LinearLayout friendViewRoot;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		try {
			if ( inflater == null ) {
				LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				return  layoutInflater.inflate( R.layout.friends_view, null);
			}
			ViewHolder vh = null;
			if (convertView == null) {

				vh = new ViewHolder();

				convertView = inflater.inflate(R.layout.friends_view, null);

				vh.activeUsers = (TextView) convertView
				.findViewById(R.id.active_users_text);
				vh.activerUserNum = (TextView) convertView
				.findViewById(R.id.active_users_no);
				vh.activeUserMainLayout = (RelativeLayout) convertView
				.findViewById(R.id.activeUserMainLayout);
				vh.footerView = (RelativeLayout) convertView
				.findViewById(R.id.footerView);

				vh.userName = (TextView) convertView
				.findViewById(R.id.userNameText);
				vh.userFullName = (TextView) convertView
				.findViewById(R.id.userNameSubText);
				vh.noMatches = (TextView) convertView
				.findViewById(R.id.nomatches);
				vh.userImage = (ImageView) convertView
				.findViewById(R.id.imageViewpostAvatar);
				vh.dividerLeftText = (TextView) convertView
				.findViewById(R.id.midDivider_leftText);
				vh.dividerRightText = (TextView) convertView
				.findViewById(R.id.midDivider_rightText);
				vh.firstOnlineIndicator = (ImageView) convertView
				.findViewById(R.id.providerImage);
				vh.secondOnlineIndicator = (ImageView) convertView
				.findViewById(R.id.greenDot);
				vh.avatarView = (RelativeLayout) convertView
				.findViewById(R.id.avatarView);
				vh.inviteImage = (ImageView) convertView
				.findViewById(R.id.inviteImage);

				vh.friendProgress = (ProgressBar) convertView
				.findViewById(R.id.friendProgress);

				vh.activeUserMainLayout = (RelativeLayout) convertView
				.findViewById(R.id.activeUserMainLayout);
				vh.friendView = (RelativeLayout) convertView
				.findViewById(R.id.friend_view);
				vh.friendViewRoot = (LinearLayout) convertView
				.findViewById(R.id.friend_view_root);
				vh.gapView = (RelativeLayout) convertView
				.findViewById(R.id.friendGapLinearView);
				vh.gapMoreTextView = (TextView) convertView
				.findViewById(R.id.friendGapMoreTextView);
				vh.gapTextView = (TextView) convertView.findViewById(R.id.friendGapTextView);
				vh.middleView=(RelativeLayout)convertView.findViewById(R.id.middleView);

				vh.gapTextLayout = (LinearLayout)convertView.findViewById(R.id.gapTextLayout);;
				vh.gapProgress = (ProgressBar)convertView.findViewById(R.id.gapProgress);;

				vh.gapTextView.setTypeface(Constants.OPEN_SANS_BOLD);
				vh.gapMoreTextView.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
				convertView.setTag(vh);

				setTypefaces(convertView);

			} else {

				vh = (ViewHolder) convertView.getTag();
			}



			vh.friendViewRoot.setOnTouchListener(null);
			vh.noMatches.setVisibility(View.GONE);
			convertView.setPadding( 5, 0, 5, 0 );
			//			vh.footerView.setOnClickListener( null );
			//			vh.middleView.setOnClickListener(null);
			//			convertView.setOnTouchListener(null);
			if ( hasZeroFriends ) {

				vh.activeUserMainLayout.setVisibility(View.VISIBLE);
				vh.activerUserNum.setText("" + totalOnlineFriends);
				vh.activeUsers.setText( R.string.friend);
				vh.activeUsers.setVisibility(View.VISIBLE);
				vh.noMatches.setVisibility(View.GONE);
				vh.gapView.setVisibility(View.GONE);
				vh.friendViewRoot.setVisibility(View.GONE);
				vh.friendView.setVisibility(View.GONE);
				vh.footerView.setVisibility(View.GONE);
				return convertView;
			} 





			if (isDummy) {
				vh.activeUserMainLayout.setVisibility(View.GONE);
				vh.avatarView.setVisibility(View.GONE);
				vh.userImage.setImageResource(R.drawable.head);
				vh.userImage.setVisibility(View.GONE);
				vh.firstOnlineIndicator.setVisibility(View.GONE);
				vh.secondOnlineIndicator.setVisibility(View.GONE);
				vh.userFullName.setVisibility(View.GONE);
				vh.userName.setVisibility(View.GONE);
				vh.inviteImage.setVisibility(View.GONE);
				vh.gapView.setVisibility(View.GONE);
				vh.footerView.setVisibility(View.GONE);
				vh.friendViewRoot.setVisibility(View.VISIBLE);
				vh.friendView.setVisibility(View.VISIBLE);


				if (position == 0) {
					vh.noMatches.setVisibility(View.VISIBLE);					
					vh.friendView.setBackgroundResource(R.drawable.list_base_top);					
					return convertView;
				} else {
					if (position == totalCount - 1) {
						vh.friendView.setBackgroundResource(R.drawable.list_base_bottom);
						convertView.setPadding(5, 0, 5, 5);
					} else {
						vh.friendView.setBackgroundResource(R.drawable.list_base_mid);
					}
					vh.noMatches.setVisibility(View.GONE);

					return convertView;
				}



			}

			if(position == 0){
				vh.activeUserMainLayout.setVisibility(View.VISIBLE);
				vh.activeUsers.setVisibility(View.VISIBLE);
				vh.noMatches.setVisibility(View.GONE);
				vh.gapView.setVisibility(View.GONE);
				vh.friendViewRoot.setVisibility(View.GONE);
				vh.friendView.setVisibility(View.GONE);
				vh.footerView.setVisibility(View.GONE);
				vh.activerUserNum.setVisibility(View.VISIBLE);
				if(search){			
					vh.activeUserMainLayout.setVisibility(View.GONE);
					vh.activerUserNum.setVisibility(View.GONE);
					vh.activeUsers.setVisibility(View.GONE);
//					vh.activerUserNum.setText(""+ Constants.searchFriendsResults);
//					vh.activeUsers.setText(R.string.matches);
					convertView.setPadding( 5, 10, 5, 0 );
					return convertView;
				}
				else if(totalOnlineFriends > 0){					
					vh.activerUserNum.setText("" + totalOnlineFriends);
					vh.activeUsers.setText(R.string.active);					
					return convertView;					
				}				

			}
			if (position == totalOnlineFriends + (HEADINGS - 1) && !search) {



				DatabaseUtil dbUtil = DatabaseUtil.getInstance();
				vh.activeUserMainLayout.setVisibility(View.VISIBLE);
				String totalCount = dbUtil.getTotalFriendsCount();
				if(totalCount != null && totalCount.trim().length() > 0){
					
					vh.activerUserNum.setText(""+ dbUtil.getTotalFriendsCount());
					vh.activeUsers.setText(R.string.friend);
				}else{
					vh.activerUserNum.setText("");
					vh.activeUsers.setText("");
				}
				
				
				vh.noMatches.setVisibility(View.GONE);

				vh.gapView.setVisibility(View.GONE);
				vh.friendViewRoot.setVisibility(View.GONE);
				vh.friendView.setVisibility(View.GONE);
				vh.footerView.setVisibility(View.GONE);
				
				
				
				
				
				
				
				dbUtil = null;
				//				vh.footerView.setOnClickListener(null);
				//				vh.middleView.setOnClickListener(null);

				return convertView;

			} 
			if ( position > 0 && position <= totalOnlineFriends ) {


				vh.avatarView.setVisibility(View.VISIBLE);
				vh.userImage.setImageResource(R.drawable.head);
				vh.firstOnlineIndicator.setImageBitmap(null);
				vh.userName.setText("");
				vh.userFullName.setText("");
				vh.userImage.setVisibility(View.VISIBLE);				
				vh.firstOnlineIndicator.setVisibility(View.VISIBLE);
				vh.userFullName.setVisibility(View.VISIBLE);
				vh.friendViewRoot.setVisibility(View.VISIBLE);
				vh.friendView.setVisibility(View.VISIBLE);
				vh.footerView.setVisibility(View.GONE);
				vh.userName.setVisibility(View.VISIBLE);

				vh.inviteImage.setVisibility(View.GONE);
				vh.gapView.setVisibility(View.GONE);
				vh.noMatches.setVisibility(View.GONE);
				vh.activeUserMainLayout.setVisibility(View.GONE);



				//					vh.inviteImage.setOnClickListener(null);
				//					vh.footerView.setOnClickListener(null);
				//					vh.middleView.setOnClickListener(null);

				vh.userImage.setOnClickListener(this);

				roomAvail = false;

				if( totalOnlineFriends == 1) {
					vh.friendView.setBackgroundResource(R.drawable.friend_list_base_single);
					vh.friendView.setTag(4);
				} else if (position == 1) {

					vh.friendView.setBackgroundResource(R.drawable.friend_list_base_top);
					//vh.friendView.setBackgroundResource(R.drawable.friend_list_base_single);
					vh.friendView.setTag(1);

				} else if (position == totalOnlineFriends) {
					vh.friendView.setBackgroundResource(R.drawable.friend_list_base_bottom);
					convertView.setPadding(5, 0, 5, 5);
					vh.friendView.setTag(3);

				} else {
					vh.friendView.setBackgroundResource(R.drawable.friend_list_base_mid);
					vh.friendView.setTag(2);

				}


				if (onlineFriends.get("vRoomName").get(position - 1) != null && onlineFriends.get("vRoomName").get(position - 1).trim().length() > 0) {
					vh.footerView.setVisibility(View.VISIBLE);
					vh.dividerLeftText.setText(onlineFriends.get("vRoomName").get(position - 1));
					roomAvail = true;
					vh.footerView.setTag(onlineFriends.get("vSubjectId").get(position - 1));
					vh.footerView.setTag(R.id.about_txtview,onlineFriends.get("iAccessPermitted").get(position - 1));
					vh.middleView.setTag(onlineFriends.get("vSubjectId").get(position - 1));
					vh.middleView.setTag(R.id.about_txtview,onlineFriends.get("iAccessPermitted").get(position - 1));
					
					if(onlineFriends.get("vSubjectHrefUrl").get(position - 1) != null && 
							onlineFriends.get("vSubjectHrefUrl").get(position - 1).trim().length() > 0){
						vh.middleView.setTag(R.id.aboutText,onlineFriends.get("vSubjectHrefUrl").get(position - 1));
						vh.middleView.setTag(R.id.avatarView,true);
					}else{
						vh.middleView.setTag(R.id.aboutText,onlineFriends.get("vSubjectUrl").get(position - 1));
						vh.middleView.setTag(R.id.avatarView,false);
					}
					
					vh.friendViewRoot.setOnTouchListener(this);

					//						vh.footerView.setOnClickListener(this);
					//						vh.middleView.setOnClickListener(this);

				}
				if (onlineFriends.get("vSubjectTitle").get(position - 1) != null && onlineFriends.get("vSubjectTitle").get(position - 1).trim().length() > 0) {
					vh.footerView.setVisibility(View.VISIBLE);
					vh.dividerRightText.setText(onlineFriends.get("vSubjectTitle").get(position - 1));
					roomAvail = true;
				}
				if (roomAvail) {
					roomAvail = false;

					if( totalOnlineFriends == 1) {
						vh.friendView.setBackgroundResource(R.drawable.friend_list_base_single);
					} else if (position == 1) {

						vh.friendView.setBackgroundResource(R.drawable.friend_list_base_top);
						//vh.friendView.setBackgroundResource(R.drawable.friend_list_base_single);

					} else if (position == totalOnlineFriends) {
						vh.friendView.setBackgroundResource(R.drawable.friend_list_base_bottom);

					} else {
						vh.friendView.setBackgroundResource(R.drawable.friend_list_base_mid);

					}

				}

				String activeUname = onlineFriends.get ( "vFriendUserName").get( position - 1 ); 
				if ( activeUname.equalsIgnoreCase("null") ){

					vh.userName.setText( new Util().getSmiledText(onlineFriends.get("vFriendName").get(position - 1)) );
					vh.userName.setTextColor(Color.parseColor("#FF4754"));
					vh.userFullName.setVisibility(View.VISIBLE);
					vh.userFullName.setText( new Util().getSmiledText(onlineFriends.get("vFriendName").get(position - 1)));

				}else{

					vh.userName.setText( new Util() .getSmiledText(onlineFriends.get("vFriendUserName").get(position - 1)));
					vh.userName.setTextColor(Color.parseColor("#FF4754"));
					vh.userFullName.setVisibility(View.VISIBLE);
					vh.userFullName.setText( new Util().getSmiledText(onlineFriends.get("vFriendName").get(position - 1)));
				}



				vh.userImage.setImageResource(R.drawable.head);
				vh.firstOnlineIndicator.setImageBitmap(null);
				vh.userImage.setBackgroundColor(Color.WHITE);
				imageDownloader.download(onlineFriends.get("vFriendAvatar").get(position - 1), vh.userImage, true, this);
				imageDownloader.download(onlineFriends.get("vSourceIcon").get(position - 1), vh.firstOnlineIndicator, true,this);
				vh.userImage.setTag(onlineFriends.get("vFriendId").get(position - 1));





				if (onlineFriends.get("isOnline").get(position - 1).equals("0")) {
					vh.secondOnlineIndicator.setVisibility(View.INVISIBLE);
				} else {
					vh.secondOnlineIndicator.setVisibility(View.VISIBLE);
				}


				//					vh.friendView.setOnTouchListener(this);
				return convertView;

			} if(position-HEADINGS-totalOnlineFriends <= totalotherFriends) {

				if (position == totalOnlineFriends + HEADINGS) {	
					vh.friendView.setBackgroundResource(R.drawable.list_base_top);
				} else if (position == totalCount - 1) {
					vh.friendView.setBackgroundResource(R.drawable.list_base_bottom);
					convertView.setPadding(5, 0, 5, 5);
				} else {
					vh.friendView.setBackgroundResource(R.drawable.list_base_mid);
				}

				if ( search && totalotherFriends == 1 && totalotherFriends == position ) {
					vh.friendView.setBackgroundResource(R.drawable.list_base_single);
				}

				roomAvail = false;

				vh.avatarView.setVisibility(View.VISIBLE);
				vh.friendViewRoot.setVisibility(View.VISIBLE);
				vh.friendView.setVisibility(View.VISIBLE);
				vh.userImage.setImageResource(R.drawable.head);
				vh.userImage.setVisibility(View.VISIBLE);
				vh.firstOnlineIndicator.setVisibility(View.VISIBLE);
				vh.userFullName.setVisibility(View.VISIBLE);
				vh.userName.setVisibility(View.VISIBLE);
				vh.noMatches.setVisibility(View.GONE);
				vh.gapView.setVisibility(View.GONE);
				vh.footerView.setVisibility(View.GONE);

				vh.activeUserMainLayout.setVisibility(View.GONE);
				vh.secondOnlineIndicator.setVisibility(View.INVISIBLE);

				//					vh.footerView.setOnClickListener( null );
				//					vh.middleView.setOnClickListener(null);


				if(otherFriends.get("vSourceName").get(position-HEADINGS-totalOnlineFriends).equalsIgnoreCase("playup")){

					String uName = otherFriends.get("vFriendUserName").get(position-HEADINGS-totalOnlineFriends);

					if( uName.equalsIgnoreCase("null")){

						vh.userName.setText( new Util().getSmiledText( otherFriends.get("vFriendName")
								.get(position - HEADINGS - totalOnlineFriends)) );

						vh.userName.setTextColor(Color.parseColor("#FF4754"));
						vh.userFullName.setVisibility(View.VISIBLE);
						vh.userFullName.setText( new Util().getSmiledText(otherFriends.get("vFriendName")
								.get(position - HEADINGS - totalOnlineFriends)));

					}else{

						vh.userName.setText( new Util().getSmiledText(otherFriends.get("vFriendUserName").get(position-HEADINGS-totalOnlineFriends)));

						vh.userName.setTextColor(Color.parseColor("#FF4754"));
						vh.userFullName.setVisibility(View.VISIBLE);
						vh.userFullName.setText( new Util().getSmiledText(otherFriends.get("vFriendName").get(position - HEADINGS - totalOnlineFriends)));

					}

				} else {
					vh.userName.setText( new Util().getSmiledText(otherFriends.get("vFriendName")
							.get(position - HEADINGS - totalOnlineFriends)));
					vh.userName.setTextColor(Color.parseColor("#736E73"));
					vh.userFullName.setVisibility(View.GONE);

				}

				vh.userImage.setTag(otherFriends.get("vFriendId").get(
						position - HEADINGS - totalOnlineFriends));
				vh.userImage.setOnClickListener(this);

				vh.userImage.setImageResource(R.drawable.head);
				vh.firstOnlineIndicator.setImageBitmap(null);
				vh.userImage.setBackgroundColor(Color.WHITE);

				imageDownloader.download(otherFriends.get("vSourceIconHref").get(position - HEADINGS - totalOnlineFriends),vh.firstOnlineIndicator, true, this);
				imageDownloader.download(otherFriends.get("vFriendAvatar").get(position - HEADINGS - totalOnlineFriends),vh.userImage, true, this);

				if (otherFriends.get("isOnline").get(
						position - HEADINGS - totalOnlineFriends).equals(
						"0")) {
					vh.secondOnlineIndicator.setVisibility(View.INVISIBLE);
				} else {
					vh.secondOnlineIndicator.setVisibility(View.VISIBLE);
				}

				
				//Log.e("position==============>>", ""+position);
			//	Log.e("HEADINGS==============>>",""+ HEADINGS);
			//	Log.e("totalOnlineFriends==============>>", ""+totalOnlineFriends);
				
				
				
			/*	if (otherFriends.get("vAppInvitationUrl").get(position - HEADINGS - totalOnlineFriends) != null 
						&& otherFriends.get("vAppInvitationUrl").get(position - HEADINGS - totalOnlineFriends).trim().length() != 0) {*/
				if ((otherFriends.get("vAppInvitationUrl").get(position - HEADINGS - totalOnlineFriends) != null 
						&& otherFriends.get("vAppInvitationUrl").get(position - HEADINGS - totalOnlineFriends).trim().length() != 0)||
					(otherFriends.get("vAppInvitationHrefUrl").get(position - HEADINGS - totalOnlineFriends) != null 
						&& otherFriends.get("vAppInvitationHrefUrl").get(position - HEADINGS - totalOnlineFriends).trim().length() != 0))
				{
					
				//	Log.e("INSIDE IFFFFF==============>>","");
					
					
					

					vh.inviteImage.setVisibility(View.VISIBLE);
				//	vh.inviteImage.setVisibility(View.GONE);


					if (otherFriends.get("isAlreadyInvited").get(position - HEADINGS - totalOnlineFriends) == null) {
						otherFriends.get("isAlreadyInvited").set(position - HEADINGS - totalOnlineFriends,"0");
					}
					int isAlreadyInvited = Integer.parseInt(otherFriends.get("isAlreadyInvited").get(position - HEADINGS- totalOnlineFriends));
					
					
				//	Log.e("position - HEADINGS - totalOnlineFriends==============>>", ""+(position - HEADINGS - totalOnlineFriends));
				//	Log.e("isAlreadyInvited==============>>", ""+isAlreadyInvited);
					
					if (isAlreadyInvited == 0) {

						vh.friendProgress.setVisibility(View.GONE);
						vh.inviteImage.setTag(otherFriends.get("vFriendId").get(position - HEADINGS- totalOnlineFriends));
						vh.inviteImage.setTag( R.id.aboutScrollView, otherFriends.get("vSourceName").get(position - HEADINGS- totalOnlineFriends));
						vh.inviteImage.setImageResource(R.drawable.invite);
						vh.inviteImage.setOnClickListener(this);

					} else if (isAlreadyInvited == 1) {

						/*if ( !Util.isInternetAvailable() ) {

								PlayupLiveApplication.showToast( R.string.no_network );
								vh.friendProgress.setVisibility(View.GONE);
							}else{*/

						vh.inviteImage.setVisibility(View.GONE);
						vh.inviteImage.setOnClickListener(null);
						vh.friendProgress.setVisibility(View.VISIBLE);

						//	}




					} else {
						vh.friendProgress.setVisibility(View.GONE);
						vh.inviteImage.setTag(otherFriends.get("vFriendId").get(position - HEADINGS- totalOnlineFriends));
						vh.inviteImage.setTag( R.id.aboutScrollView, otherFriends.get("vSourceName").get(position - HEADINGS- totalOnlineFriends));
						vh.inviteImage.setImageResource(R.drawable.invite_sent);
						vh.inviteImage.setOnClickListener(null);
					}

				} else {
					

					
					vh.inviteImage.setVisibility(View.GONE);
					vh.friendProgress.setVisibility(View.GONE);
					vh.inviteImage.setOnClickListener(null);
				}



				final String gapUid = otherFriends.get("vGapId").get(position - HEADINGS - totalOnlineFriends);
				if (gapUid != null && gapUid.trim().length() > 0) {
					final LinearLayout gapTextLayout = vh.gapTextLayout;
					final ProgressBar gapProgress = vh.gapProgress;
					convertView.setPadding(5, 0, 5, 5);
					vh.gapView.setVisibility(View.VISIBLE);
					vh.gapView.setClickable(true);
					vh.gapProgress.setVisibility(View.GONE);
					vh.gapTextLayout.setVisibility(View.VISIBLE);
					vh.activeUserMainLayout.setVisibility(View.GONE);
					vh.friendViewRoot.setVisibility(View.GONE);
					vh.friendView.setVisibility(View.GONE);
					vh.footerView.setVisibility(View.GONE);

					DatabaseUtil dbUtil = DatabaseUtil.getInstance();

					final Hashtable<String, List<String>> gapData = dbUtil.getGapUrl(gapUid);
					dbUtil = null;
					if ( ((gapData != null && gapData.get("gap_url").size() > 0) ||(gapData != null && gapData.get("gap_href_furl").size() > 0) )&& ( gapId != null && !gapId.equalsIgnoreCase( gapUid ) )) {
						isGapLoading = false;
						vh.gapTextView.setText(""+ gapData.get("gap_size").get(0));
					}
					else {
						vh.gapTextLayout.setVisibility(View.GONE);
						vh.gapProgress.setVisibility(View.VISIBLE);	
					}

					vh.gapTextView.setFocusable(true);
					vh.gapView.setTag(gapUid);
					if(search){
						if ( isGapLoading || Constants.isSearchGapDownloading) {


							vh.gapTextLayout.setVisibility(View.GONE);
							vh.gapProgress.setVisibility(View.VISIBLE);
						}


					}
					else if ( isGapLoading || Constants.isFriendsGapDownloading ) {

						vh.gapTextLayout.setVisibility(View.GONE);
						vh.gapProgress.setVisibility(View.VISIBLE);
					}




					vh.gapView.setOnTouchListener(new OnTouchListener() {

						@Override
						public boolean onTouch(View v, MotionEvent event) {
							//							isTouched = true;

							if ( !isGapLoading ) {
								String gapUid = v.getTag().toString();
								gapId = gapUid;
								if (event.getAction() == MotionEvent.ACTION_DOWN) {
									if (gapUid != null && gapUid.trim().length() > 0) {

										isTouched = true;
										gapId =gapUid;
									}
								} 
								if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
									if (gapUid != null && gapUid.trim().length() > 0) {
										gapTextLayout.setVisibility(View.GONE);
										gapProgress.setVisibility(View.VISIBLE);
										isGapLoading = true;
										isTouched = false;

										String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
										FriendsFragment fragment = null;
										if ( PlayupLiveApplication.getFragmentManagerUtil().fragmentMap != null && PlayupLiveApplication.getFragmentManagerUtil().fragmentMap.containsKey( topFragmentName ) ) {
											fragment = (FriendsFragment) PlayupLiveApplication.getFragmentManagerUtil().fragmentMap.get( topFragmentName ); 
										}



										if ( fragment != null ) {
											 if ( fragment.runnableList!=null && 
													! fragment.runnableList.containsKey(gapData.get("gap_href_url").get(0))  
													&& Util.isInternetAvailable() ){

													if(search){
														fragment.runnableList.put(gapData.get("gap_href_url").get(0), 
																new Util().callNextSearchFriendsUrl(gapData.get("gap_href_url").get(0), gapUid,fragment.runnableList,true));
													}
													else{
														fragment.runnableList.put(gapData.get("gap_href_url").get(0), 
																new Util().callNextFriendsUrl(gapData.get("gap_href_url").get(0), gapUid,fragment.runnableList,true));
													}

												}else if ( fragment.runnableList!=null && 
														! fragment.runnableList.containsKey(gapData.get("gap_url").get(0)) 
														&& Util.isInternetAvailable() ){

													if(search){
														fragment.runnableList.put(gapData.get("gap_url").get(0), 
																new Util().callNextSearchFriendsUrl(gapData.get("gap_url").get(0), gapUid,fragment.runnableList,false));
													}
													else{
														fragment.runnableList.put(gapData.get("gap_url").get(0),
																new Util().callNextFriendsUrl(gapData.get("gap_url").get(0), gapUid,fragment.runnableList,false));
													}

												}
											
										}






									}

								} 
							}
							return true;		
						}
					});


				}
				return convertView;

			}



		} catch (Exception e) {
			// TODO: handle exception
		//	Logs.show(e);
		}
		return convertView;
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
				if (mHandler != null) {

					mHandler.post(new Runnable() {

						@Override
						public void run() {

							try {
								highLightBlueColor(v, false);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								//Logs.show ( e );
							}
						}
					});


				}

			}
			if ( (v.findViewById( R.id.footerView ) != null) || (v.findViewById(R.id.middleView) !=  null)) {

				if (v.findViewById( R.id.middleView).getTag() == null) {
					return true;
				}
				else{
					if( Boolean.parseBoolean(v.findViewById( R.id.middleView).getTag(R.id.about_txtview).toString()) == true ){						
						final Bundle bundle = new Bundle ();
						bundle.putString( "vConversationId",v.findViewById( R.id.middleView).getTag().toString());
						bundle.putString( "vMainColor",vMainColor );							
						bundle.putString( "vMainTitleColor",vMainTitleColor );
						bundle.putString( "vSecColor",vSecColor );			
						bundle.putString( "vSecTitleColor",vSecTitleColor );
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						String type = dbUtil.getHeader(v.findViewById( R.id.middleView).getTag(R.id.aboutText).toString());
						if(type != null){
							if(type.equalsIgnoreCase(Constants.ACCEPT_TYPE_COVERSATION)){
								if  ( PlayUpActivity.handler != null ) {
									PlayUpActivity.handler.post( new Runnable( ) {
										
										@Override
										public void run() {
											
											try {
												if ( !Constants.isCurrent ) {
													return;
												}
												PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchHomeFragment",bundle);
											} catch (Exception e) {
												// TODO Auto-generated catch block
												//Logs.show ( e );
											}
										}
									});
								}
								

							}else{
								if  ( PlayUpActivity.handler != null ) {
									PlayUpActivity.handler.post( new Runnable( ) {
										
										@Override
										public void run() {
											try {
												if ( !Constants.isCurrent ) {
													return;
												}
												PlayupLiveApplication.getFragmentManagerUtil().setFragment("PrivateLobbyRoomFragment",bundle);
											} catch (Exception e) {
												// TODO Auto-generated catch block
												//Logs.show ( e );
											}
										}
									});
								}
								
								

							}
						}
					}else{
						PlayupLiveApplication.showToast(R.string.privateRoomMessage);
					}



				}
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
				highLightBlueColor(v, true);
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

				highLightBlueColor(v, true);
				super.handleMessage(msg);
			}
		};
		mUpdater = Executors.newSingleThreadScheduledExecutor();
		mUpdater.schedule(new UpdateCounterTask(), 10, TimeUnit.MILLISECONDS);
	}

	private void stopUpdating() {
		if (mUpdater != null && !mUpdater.isShutdown()) {
			mUpdater.shutdownNow();
			mUpdater = null;
		}
	}

	public void highLightBlueColor(final View view, final boolean shouldDo) {
		// highlight with blue color
		if ( view == null ) {
			return;
		}
		int id = Integer.parseInt(view.findViewById(R.id.friend_view).getTag().toString());
		if (shouldDo) {

			if(id == 1){
				view.findViewById(R.id.friend_view).setBackgroundResource(R.drawable.friend_list_base_top_d);
				//	view.setBackgroundResource(R.drawable.friend_list_base_single_d);
			} else if(id==4){
				view.findViewById(R.id.friend_view).setBackgroundResource(R.drawable.friend_list_base_single_d);
			}

			else if(id == 2){

				view.findViewById(R.id.friend_view).setBackgroundResource(R.drawable.friend_list_base_mid_d);
			}

			else if(id ==3){

				view.findViewById(R.id.friend_view).setBackgroundResource(R.drawable.friend_list_base_bottom_d);
			}

			((TextView) view.findViewById(R.id.userNameText))
			.setTextColor(Color.parseColor("#FFFFFF"));

			((TextView) view.findViewById(R.id.userNameSubText))
			.setTextColor(Color.parseColor("#FFFFFF"));

		} else { // remove the highlight

			if(id == 1){
				view.findViewById(R.id.friend_view).setBackgroundResource(R.drawable.friend_list_base_top);
				//view.setBackgroundResource(R.drawable.friend_list_base_single);
			}
			else if( id==4){
				view.findViewById(R.id.friend_view).setBackgroundResource(R.drawable.friend_list_base_single);
			}

			else if(id == 2)
				view.findViewById(R.id.friend_view).setBackgroundResource(R.drawable.friend_list_base_mid);
			else if(id ==3)
				view.findViewById(R.id.friend_view).setBackgroundResource(R.drawable.friend_list_base_bottom);


			((TextView) view.findViewById(R.id.userNameText))
			.setTextColor(Color.parseColor("#FF4754"));

			((TextView) view.findViewById(R.id.userNameSubText))
			.setTextColor(Color.parseColor("#9A9A9A"));

		}

	}

	private class UpdateCounterTask implements Runnable {
		public void run() {

			try {
				mHandler.sendEmptyMessage(0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
			//	Logs.show ( e );
			}
		}
	}

	@Override
	public void onClick(View v) {

		try {
			if ( v == null ) {
				return;
			}
			
			String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
			
			
		//	Log.e("123","INSIED THE ONCLICK*****************");
			if (v.getId() == R.id.inviteImage && !isListViewScrolling) {
				//Log.e("123","INSIED THE IF*****************");
				String vFriendId = v.getTag().toString();

				DatabaseUtil dbUtil = DatabaseUtil.getInstance();

				if ( v.getTag( R.id.aboutScrollView ) != null ) {
					String vSourceName =  v.getTag( R.id.aboutScrollView ).toString();

					if ( !dbUtil.isIdentifierProviderName( vSourceName ) ) {

						Hashtable<String, List<String>> data = dbUtil.getProviderUrls ( vSourceName );

						if ( data != null && data.get( "vLoginUrl" ) != null && data.get( "vLoginUrl" ).size() > 0 ) {
							Bundle bundle = new Bundle();

							bundle.putString("vLoginUrl", data.get("vLoginUrl").get( 0 ) );
							bundle.putString("vSuccessUrl", data.get("vSuccessUrl").get( 0 ));
							bundle.putString("vFailureUrl", data.get("vFailureUrl").get( 0 )); 
							bundle.putString("fromFragment", topFragmentName );

							PlayupLiveApplication.getFragmentManagerUtil().setFragment ( "LoginWebViewFragment", bundle );
							return;
						}
					}

				}


				//			v.setVisibility(View.INVISIBLE);
				//			try {
				//				((RelativeLayout) v.getParent()).findViewById(R.id.friendProgress).setVisibility(View.VISIBLE);	
				//			} catch (NullPointerException e) {
				//				
				//			}

				dbUtil.setAlreadyInvited(vFriendId, 1);

				if (search) {
					otherFriends = dbUtil.searchAllFriendsData();

					totalOnlineFriends = 0;
					totalotherFriends = 0;

					if (otherFriends != null) {
						totalotherFriends = otherFriends.get("vFriendId").size();
					}

					totalCount = totalOnlineFriends + totalotherFriends + 1;
					notifyDataSetChanged();

				} else {
					otherFriends = dbUtil.getAllFriendsData();

					//totalOnlineFriends = 0;
					totalotherFriends = 0;

					if (otherFriends != null) {
						totalotherFriends = otherFriends.get("vFriendId").size();
					}

					totalCount = totalOnlineFriends + totalotherFriends + ( totalOnlineFriends > 0 ? 2 : 1 );
					notifyDataSetChanged();

				}


				new Util().sendInvite ( vFriendId );
			//	Log.e("123","INSIED THE IVITE SENDDDD*****************");
				dbUtil = null;

			} else if (v.getId() == R.id.imageViewpostAvatar) {
			//	Log.e("123","INSIED THE ELSE *****************");
				if (v.getTag() == null) {
					return;
				}
				String vFriendId = v.getTag().toString();

				DatabaseUtil dbUtil = DatabaseUtil.getInstance();
				Hashtable<String, Object> result = dbUtil.getProfileUrlFromFriendId(vFriendId);
				String vSelfUrl ="";
				boolean isHref = false;
				if(result!=null && result.contains("url")&& result.contains("isHref")){
				vSelfUrl =(String) result.get("url");
				isHref = (Boolean) result.get("isHref");
				}
				if (vSelfUrl == null || vSelfUrl.trim().length() == 0) {
					
					return;
				}
				
			

				Bundle bundle = new Bundle();
				bundle.putString("vSelfUrl", vSelfUrl);
				bundle.putBoolean("isHref", isHref);
				bundle.putInt("myId", -1);
				bundle.putString( "vMainColor",vMainColor );							
				bundle.putString( "vMainTitleColor",vMainTitleColor );
				bundle.putString( "vSecColor",vSecColor );			
				bundle.putString( "vSecTitleColor",vSecTitleColor );
				PlayupLiveApplication.getFragmentManagerUtil().setFragment(
						"PublicProfileFragment", bundle);

				dbUtil = null;
			}
		} catch (Exception e) {
		//	Logs.show(e);
		} 

	}

	

	public void setTypefaces(View converView) {

		ViewHolder vh = (ViewHolder) converView.getTag();
		vh.activerUserNum.setTypeface(Constants.OPEN_SANS_BOLD);
		vh.activeUsers.setTypeface(Constants.OPEN_SANS_BOLD);
		vh.userName.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		vh.userFullName.setTypeface(Constants.OPEN_SANS_REGULAR);
		vh.dividerLeftText.setTypeface(Constants.OPEN_SANS_REGULAR);
		vh.dividerRightText.setTypeface(Constants.OPEN_SANS_REGULAR);
		vh.noMatches.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
	}



	@Override
	public void notifyDataSetChanged() {


		try {

			if (isListViewScrolling == false && isTouched == false) {
				super.notifyDataSetChanged();
			}
		} catch ( Exception e ) {


		}

	}



	/**
	 * 
	 * Srolling of list view needs to be monitored, because if list view is
	 * scrolling dont notify the data set. It will slow down the scrolling
	 * effect
	 */

	ListView.OnScrollListener scrollListener = new ListView.OnScrollListener() {
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

				isListViewScrolling = false;

				notifyDataSetChanged();

				break;

			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:

				isListViewScrolling = true;

				break;

			case OnScrollListener.SCROLL_STATE_FLING:

				isListViewScrolling = true;

				break;
			}

		}

	};


}
