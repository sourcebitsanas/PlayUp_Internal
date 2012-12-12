package com.playup.android.adapters;




import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.playup.android.fragment.PlayupFriendsFragment;
import com.playup.android.fragment.PostDirectMessageFragment;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

public class PlayUpFriendsAdapter extends BaseAdapter implements OnClickListener {

	private LayoutInflater mInflater;
	private boolean hasZeroFriends = false;
	private Hashtable<String, List<String>> data;
	private int HEADINGS = 0;
	private boolean isListViewScrolling = false;
	private ImageDownloader imageDownloader ;
	private int totalCount = 0;
	private boolean isDummy = false;
	private boolean search = false;
	private boolean isGapLoading = false;
	private String gapId = "";
	private boolean isTouched = false;


	public PlayUpFriendsAdapter () {}

	private ListView playupListView ;
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;


	public PlayUpFriendsAdapter(Hashtable<String, List<String>> data, ListView playupListView,
			String vMainColor, String vMainTitleColor  , String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		this.data = data;
		isListViewScrolling = false;
		if ( imageDownloader == null ) {
			imageDownloader = new ImageDownloader();
		}

		this.vMainColor = vMainColor;
		this.vMainTitleColor  = vMainTitleColor;
		
		hasZeroFriends = false;
		isDummy = false;
		search = false;

		this.playupListView = playupListView;
		this.playupListView.setOnScrollListener(scrollListener);

		HEADINGS = 1;

		if ( mInflater == null ) {
			mInflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		if( data != null && data.get("vFriendId") != null && data.get("vFriendId").size() > 0 )
			totalCount = data.get("vFriendId").size()+HEADINGS;


	}

	public void setData ( Hashtable<String, List<String>> data,  ListView playupListView , 
			String vMainColor, String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;

		this.vMainColor = vMainColor;
		this.vMainTitleColor  = vMainTitleColor;
		
		search = false;
		isDummy = false;
		hasZeroFriends = false;
		isListViewScrolling = false;

		if ( imageDownloader == null ) {
			imageDownloader = new ImageDownloader();
		}
		
		Log.e("234", "data===========setData===============>>"+data);
		this.data = data;

		HEADINGS = 1;

		if(data != null && data.get("vFriendId") != null && data.get("vFriendId").size() > 0 )
			totalCount = data.get("vFriendId").size()+HEADINGS;

		if ( mInflater == null ) {
			mInflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}


		this.playupListView = playupListView;
		this.playupListView.setOnScrollListener(scrollListener);


		notifyDataSetChanged();



	}

	public void setData(Hashtable<String, List<String>> data, ListView playupListView,  
			boolean inSearch , String vMainColor, String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;

		this.vMainColor = vMainColor;
		this.vMainTitleColor  = vMainTitleColor;
		
		hasZeroFriends = false;
		isDummy = false;
		search = true;
		isListViewScrolling = false;

		if ( this.data !=  null ) {
			this.data.clear();
			this.data = null;
		}
		Log.e("234", "data===========setData2===============>>"+data);
		this.data = data;		
		HEADINGS = 1;
		if (data != null && data.get("vFriendId") != null ) {
			totalCount = data.get("vFriendId").size() + HEADINGS;

		}
		if ( imageDownloader == null ) {
			imageDownloader = new ImageDownloader();
		}

		if ( mInflater == null ) {
			mInflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		this.playupListView = playupListView;
		this.playupListView.setOnScrollListener(scrollListener);


		notifyDataSetChanged();

	}


	public void setDummyData(int count) {

		isListViewScrolling = false;
		totalCount = 20;
		isDummy = true;
		hasZeroFriends = false;
		if ( mInflater == null ) {
			mInflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		notifyDataSetChanged();

	}


	public void showzeroFriends () {
		isDummy = false;
		hasZeroFriends = true;
		search = false;
		totalCount = 1;
		if ( mInflater == null ) {
			mInflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
	}


	@Override
	public void notifyDataSetChanged() {
		if ( !isListViewScrolling && !isTouched ){
			super.notifyDataSetChanged();
		}
	}


	@Override
	public int getCount() {
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

		public RelativeLayout playupFriendsMainLayout;
		public RelativeLayout friend_view;
		public TextView pfriends_no;
		public TextView  pfriends_text;

		public TextView noMatches;
		public ImageView userImage;
		public ImageView firstOnlineIndicator;
		public ImageView secondOnlineIndicator;
		public TextView userName;
		public TextView userFullName;
		private RelativeLayout avatarView;
		private RelativeLayout playupFriendGapLayout;
		private LinearLayout gapTextLayout;
		private TextView gapNumber;
		private ProgressBar gapLoadingProgress;



		//	public TextView noMatches;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if ( mInflater == null ) {
			LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return  layoutInflater.inflate( R.layout.playupfriends_view, null);
		}

		ViewHolder vh = null;
		if ( convertView == null){

			vh = new ViewHolder();
			convertView = mInflater.inflate(R.layout.playupfriends_view, null);

			vh.playupFriendsMainLayout = (RelativeLayout)convertView.findViewById( R.id.playupFriendsMainLayout );
			vh.playupFriendGapLayout = (RelativeLayout)convertView.findViewById(R.id.friendGapLinearView);
			vh.pfriends_no = ( TextView ) convertView.findViewById( R.id.pfriends_no );
			vh.pfriends_text = ( TextView ) convertView.findViewById( R.id.pfriends_text );
			vh.friend_view = ( RelativeLayout ) convertView.findViewById( R.id.friend_view);
			vh.userName = (TextView) convertView.findViewById(R.id.userNameText);
			vh.userFullName = (TextView) convertView.findViewById(R.id.userNameSubText);
			vh.noMatches = (TextView) convertView.findViewById(R.id.noFriends);
			vh.userImage = (ImageView) convertView.findViewById(R.id.imageViewpostAvatar);
			vh.firstOnlineIndicator = (ImageView) convertView.findViewById(R.id.providerImage);
			vh.secondOnlineIndicator = (ImageView) convertView.findViewById(R.id.greenDot);
			vh.avatarView = (RelativeLayout) convertView.findViewById(R.id.avatarView);
			vh.gapTextLayout = (LinearLayout) convertView.findViewById(R.id.gapTextLayout);
			vh.gapNumber = (TextView) convertView.findViewById(R.id.friendGapTextView);
			vh.gapLoadingProgress = (ProgressBar) convertView.findViewById(R.id.gapProgress);

			convertView.setTag(vh);
			vh.friend_view.setOnClickListener(this);
			setTypefaces(convertView);

		}else {

			vh = (ViewHolder) convertView.getTag();
		}

		convertView.setPadding( 0, 0, 0, 0 );
		if( isDummy ) {

			if( position == 0 ) {
				vh.playupFriendsMainLayout.setVisibility(View.GONE);
				vh.friend_view.setVisibility(View.VISIBLE);	
				vh.friend_view.setBackgroundResource(R.drawable.list_base_top);	
				vh.pfriends_text.setVisibility(View.GONE);
				vh.noMatches.setVisibility(View.VISIBLE);			
				vh.userName.setVisibility(View.GONE); 
				vh.userFullName.setVisibility(View.GONE);

				vh.userImage .setVisibility(View.GONE);

				vh.firstOnlineIndicator.setVisibility(View.GONE);
				vh.secondOnlineIndicator.setVisibility(View.GONE);
				vh.avatarView.setVisibility(View.GONE); 
				vh.playupFriendGapLayout.setVisibility(View.GONE);
				return convertView;

			} else {

				if( position == totalCount-1 )
					vh.friend_view.setBackgroundResource(R.drawable.list_base_bottom);	
				else
					vh.friend_view.setBackgroundResource(R.drawable.list_base_mid);	

				vh.playupFriendsMainLayout.setVisibility(View.GONE);
				vh.friend_view.setVisibility(View.VISIBLE);	
				vh.noMatches.setVisibility(View.GONE);			
				vh.userName.setVisibility(View.GONE); 
				vh.userFullName.setVisibility(View.GONE);
				vh.pfriends_text.setVisibility(View.GONE);
				vh.userImage .setVisibility(View.GONE);
				vh.firstOnlineIndicator.setVisibility(View.GONE);
				vh.secondOnlineIndicator.setVisibility(View.GONE);
				vh.avatarView.setVisibility(View.GONE); 
				vh.playupFriendGapLayout.setVisibility(View.GONE);

				return convertView;

			}

		}


		if ( hasZeroFriends ) {

			vh.playupFriendsMainLayout.setVisibility(View.VISIBLE);
			vh.pfriends_text.setVisibility(View.VISIBLE);
			vh.pfriends_no.setText("0");
			vh.pfriends_text.setText( R.string.friend);			
			vh.noMatches.setVisibility(View.GONE);			
			vh.friend_view.setVisibility(View.GONE);
			vh.playupFriendGapLayout.setVisibility(View.GONE);

			return convertView;
		} 


		if( position == 0 ) {

			vh.noMatches.setVisibility(View.GONE);			
			vh.playupFriendsMainLayout.setVisibility(View.VISIBLE);
			vh.friend_view.setVisibility(View.GONE);
			vh.pfriends_text.setVisibility(View.VISIBLE);
			vh.pfriends_no.setVisibility(View.VISIBLE);
			vh.pfriends_text.setText(R.string.friend);		
			vh.playupFriendGapLayout.setVisibility(View.GONE);
			if( search ){
				vh.pfriends_text.setVisibility(View.GONE);
				vh.pfriends_no.setVisibility(View.GONE);
				convertView.setPadding( 0, 10, 0, 0 );
			

			} else {

				vh.pfriends_no.setText("" +DatabaseUtil.getInstance().getPlayupFriendsCount() );

			}
		
			return convertView;					

		} else {

			final String gapUid = data.get("vGapId").get(position - 1);
			if ( gapUid != null && gapUid.trim().length() > 0 ) {
				final LinearLayout gapTextLayout = vh.gapTextLayout;
				final ProgressBar gapLoadingProgress = vh.gapLoadingProgress;
				vh.playupFriendGapLayout.setVisibility(View.VISIBLE);
				vh.playupFriendGapLayout.setClickable(true);
				vh.gapLoadingProgress.setVisibility(View.GONE);
				vh.gapTextLayout.setVisibility(View.VISIBLE);
				vh.playupFriendsMainLayout.setVisibility(View.GONE);
				vh.friend_view.setVisibility(View.GONE);	

				DatabaseUtil dbUtil = DatabaseUtil.getInstance();

				final Hashtable<String, List<String>> gapData = dbUtil.getGapUrl(gapUid);

				if ( gapData != null && ( gapData.get("gap_url").size() > 0 ||  gapData.get("gap_href_url").size() > 0)) {
					if( ( gapId != null && !gapId.equalsIgnoreCase( gapUid ) )) {
						isGapLoading = false;
						vh.gapNumber.setText(""+ gapData.get("gap_size").get(0));
					}
					else {
						vh.gapTextLayout.setVisibility(View.GONE);
						vh.gapLoadingProgress.setVisibility(View.VISIBLE);	
					}	
				}

				vh.playupFriendGapLayout.setTag( gapUid );
				vh.gapNumber.setFocusable(true);

				if( search ) {

					if ( isGapLoading || Constants.isPlayupFriendsSearchGapDownloading) {

						vh.gapTextLayout.setVisibility(View.GONE);
						vh.gapLoadingProgress.setVisibility(View.VISIBLE);

					}	
				}

				else if ( isGapLoading || Constants.isPlayupFriendsGapDownloading) {

					vh.gapTextLayout.setVisibility(View.GONE);
					vh.gapLoadingProgress.setVisibility(View.VISIBLE);

				}


				vh.playupFriendGapLayout.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {

						if ( !isGapLoading ) {
							String gapUid = v.getTag().toString();
							gapId = gapUid;
							if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
								if (gapUid != null && gapUid.trim().length() > 0) {
									isTouched = true;
									gapId =gapUid;
								}
							} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
								if ( gapUid != null && gapUid.trim().length() > 0 ) {
									gapTextLayout.setVisibility(View.GONE);
									gapLoadingProgress.setVisibility(View.VISIBLE);
									isGapLoading = true;
									isTouched = false;
									
									
									String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
									
									if ( PlayupLiveApplication.getFragmentManagerUtil().checkIfFragmentExists( topFragmentName ) ) {
										if ( PlayupLiveApplication.getFragmentManagerUtil().fragmentMap != null && PlayupLiveApplication.getFragmentManagerUtil().fragmentMap.containsKey( topFragmentName ) )  {

											PlayupFriendsFragment fragment = ( PlayupFriendsFragment ) PlayupLiveApplication.getFragmentManagerUtil().fragmentMap.get( topFragmentName ); 
											if ( fragment != null ) {
												
												 if (gapData.get("gap_href_url").get(0) != null &&
														 gapData.get("gap_href_url").get(0).trim().length() > 0 && 
														 fragment.runnableList != null && 
														!fragment.runnableList.containsKey(gapData.get("gap_href_url").get(0))  && 
														Util.isInternetAvailable() ) {  	
													if(search) {
														fragment.runnableList.put(gapData.get("gap_href_url").get(0), new Util().callNextPlayupFriendsSearchUrl(gapData.get("gap_href_url").get(0), gapUid,fragment.runnableList));
													}
													else{
														fragment.runnableList.put(gapData.get("gap_href_url").get(0), new Util().callNextPlaupFriendsUrl(gapData.get("gap_href_url").get(0), gapUid,fragment.runnableList));
													}
												}else if ( fragment.runnableList != null && 
														!fragment.runnableList.containsKey(gapData.get("gap_url").get(0))&& 
														Util.isInternetAvailable() ) {  	
													if(search) {
														fragment.runnableList.put(gapData.get("gap_url").get(0), new Util().callNextPlayupFriendsSearchUrl(gapData.get("gap_url").get(0), gapUid,fragment.runnableList));
													}
													else{
														fragment.runnableList.put(gapData.get("gap_url").get(0), new Util().callNextPlaupFriendsUrl(gapData.get("gap_url").get(0), gapUid,fragment.runnableList));
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

				return convertView;

			} else {

				vh.playupFriendGapLayout.setVisibility(View.GONE);

				if(data.get("vFriendId").size() == 1)
					vh.friend_view.setBackgroundResource(R.drawable.list_base_single);
				else if(position == 1)
					vh.friend_view.setBackgroundResource(R.drawable.list_base_top);
				else if(position == totalCount-1)
					vh.friend_view.setBackgroundResource(R.drawable.list_base_bottom);	
				else
					vh.friend_view.setBackgroundResource(R.drawable.list_base_mid);	

				vh.playupFriendsMainLayout.setVisibility(View.GONE);

				vh.noMatches.setVisibility(View.GONE);			
				vh.friend_view.setVisibility(View.VISIBLE);
				vh.userName.setVisibility(View.VISIBLE); 
				vh.userFullName.setVisibility(View.VISIBLE);		
				vh.userImage .setVisibility(View.VISIBLE);		
				vh.firstOnlineIndicator.setVisibility(View.VISIBLE);
				vh.secondOnlineIndicator.setVisibility(View.VISIBLE);
				vh.avatarView.setVisibility(View.VISIBLE);			
				
				
				
				
				
				if(data.get ( "vUserHrefUrl" ).get( position - 1 ) != null && 
					data.get ( "vUserHrefUrl" ).get( position - 1 ).trim().length() > 0){
					vh.friend_view.setTag( data.get ( "vUserHrefUrl" ).get( position - 1 ) );
					vh.friend_view.setTag(R.id.avatarView,true);
					
				}else{
					
					vh.friend_view.setTag( data.get ( "vUserSelfUrl" ).get( position - 1 ) );
					vh.friend_view.setTag(R.id.avatarView,false);
				}
				
				if(data.get ( "vDirectConversationHrefUrl" ).get( position - 1 ) != null 
						&& data.get ( "vDirectConversationHrefUrl" ).get( position - 1 ).trim().length() > 0){
					
					vh.friend_view.setTag ( R.id.about_txtview, data.get ( "vDirectConversationHrefUrl" ).get( position - 1 ) );
					vh.friend_view.setTag( R.id.avtarGreenBase,true );
					
					vh.userImage.setTag(data.get("vDirectConversationUrl").get(position - 1));
					vh.userImage.setTag( R.id.avtarGreenBase,true );
					
				}else{
					vh.friend_view.setTag ( R.id.about_txtview, data.get ( "vDirectConversationUrl" ).get( position - 1 ) );
					vh.friend_view.setTag( R.id.avtarGreenBase,false );
					
					vh.userImage.setTag(data.get("vDirectConversationUrl").get(position - 1));
					vh.userImage.setTag( R.id.avtarGreenBase,false );
				}
				
				
				vh.friend_view.setTag ( R.id.aboutText, data.get ( "vFriendUserName" ).get( position - 1 ) );
				
				vh.userName.setText(data.get("vFriendUserName").get(position - 1));
				vh.userFullName.setText(data.get("vFriendName").get(position - 1));
				vh.userImage.setImageResource(R.drawable.head);
				vh.firstOnlineIndicator.setImageBitmap(null);
				vh.userImage.setBackgroundColor(Color.WHITE);

				imageDownloader.download(data.get("vFriendAvatar").get(position - 1), vh.userImage, true, this);
				imageDownloader.download(data.get("vSourceIconHref").get(position - 1), vh.firstOnlineIndicator, true,this);

				

				if (data.get("isOnline").get(position - 1).equals("0")) {
					vh.secondOnlineIndicator.setVisibility(View.INVISIBLE);
				} else {
					vh.secondOnlineIndicator.setVisibility(View.VISIBLE);
				}
				return convertView; 
			}
		}

	}




	public void setTypefaces( View convertView) {

		ViewHolder vh = (ViewHolder) convertView.getTag();
		vh.userName.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		vh.userFullName.setTypeface(Constants.OPEN_SANS_REGULAR);
		vh.noMatches.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		vh.pfriends_no.setTypeface( Constants.OPEN_SANS_BOLD );
		vh.pfriends_text.setTypeface( Constants.OPEN_SANS_BOLD );

	}
	@Override
	public void onClick( View v ) {

		try {
			if ( v != null && v.getTag() != null ) {

			
				String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
				
				String vUserSelfUrl = v.getTag().toString();
				boolean isUserSelfUrlHref = ((Boolean) v.getTag(R.id.avatarView)).booleanValue();
				String vDirectConversationUrl = v.getTag( R.id.about_txtview ).toString();
				boolean isvDirectConversationUrlHref = ((Boolean) v.getTag(R.id.avtarGreenBase)).booleanValue();
				String vFriendName = v.getTag( R.id.aboutText ).toString();
				Bundle bundle =new Bundle();
				bundle.putString("fromFragment", topFragmentName);
				bundle.putString( "vFriendName", vFriendName );
				bundle.putString( "vUserSelfUrl", vUserSelfUrl);
				bundle.putString( "vMainColor",vMainColor );							
				bundle.putString( "vMainTitleColor",vMainTitleColor );
				bundle.putString( "vSecColor",vSecColor );			
				bundle.putString( "vSecTitleColor",vSecTitleColor );
				bundle.putString( "vDirectConversationUrl", vDirectConversationUrl );
				
				Log.e("234", "vUserSelfUrl=========!!==="+vUserSelfUrl);
				
				Hashtable<String, Object> result = DatabaseUtil.getInstance().getDirectMessageUrl( vUserSelfUrl );
				
				
				
				if(result != null && result.containsKey("url") && result.containsKey("isHref") && result.get("url").toString().trim().length() > 0){
					String url = (String) result.get("url");
					Boolean isDirectMessageHrefUrl = (Boolean) result.get("isHref");
					bundle.putString("vDirectMessageUrl", url );
					bundle.putBoolean("isvDirectMessageHrefUrl",isDirectMessageHrefUrl  );
					
					
					Log.e("234", "result=========!!==="+result);
					Log.e("234", "url================!!!====="+url);
					Log.e("234", "isDirectMessageHrefUrl==!!===="+isDirectMessageHrefUrl);
				}
				
				
				
				
				
				
				bundle.putBoolean("isUserSelfUrlHref",isUserSelfUrlHref  );
				bundle.putBoolean("isvDirectConversationUrlHref",isvDirectConversationUrlHref  );
				PostDirectMessageFragment.isHomeTapped = false;
				PlayupLiveApplication.getFragmentManagerUtil().setFragment( "PostDirectMessageFragment", bundle );
				


			}
		} catch (Exception e) {
			Logs.show(e);
		}
	}

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


