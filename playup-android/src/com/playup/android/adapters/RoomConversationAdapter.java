package com.playup.android.adapters;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.database.Cursor;
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
import android.view.ViewGroup.LayoutParams;
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
import com.playup.android.fragment.MatchHomeFragment;
import com.playup.android.fragment.PostMessageFragment;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;
import com.playup.android.util.FragmentManagerUtil;
import com.playup.android.util.ImageDownloader;

import com.playup.android.util.Util;

/**
 * match room adapter. 
 */
public class RoomConversationAdapter extends BaseAdapter implements OnClickListener,OnTouchListener {


	// private Cursor matchHomeCursor = null;

	private  Hashtable < String, List < String > > matchHomeData = null;
	private LayoutInflater inflater  = null;
	private ImageDownloader imageDownloader = new ImageDownloader();
	private DateUtil dateutil = new DateUtil();
	private String vConversationId;
	private String vContestId;
	private String fromFragment;
	private boolean isListViewScrolling	=	false;
	DatabaseUtil mDatabaseUtil = DatabaseUtil.getInstance();
	ListView mlListView;
	private LayoutParams params;
	ViewHolder holder;
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;
	private String topFragmentName = null;



	public RoomConversationAdapter ( Hashtable < String, List < String > > matchHomeData, String vConversationId, 
			String vContestId, String fromFragment,ListView mlListView, 
			String vMainColor, String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		this.vMainColor = vMainColor;
		this.vMainTitleColor = vMainTitleColor;
		this.vConversationId = vConversationId;
		this.vContestId = vContestId;
		this.fromFragment = fromFragment;
		this.mlListView	= mlListView;
		this.mlListView.setOnScrollListener(scrollListener);
		isListViewScrolling	=	false;
		this.matchHomeData = matchHomeData;
		inflater = LayoutInflater.from(  PlayUpActivity.context );
		
		this.topFragmentName  = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
	}

	public void setData ( Hashtable < String, List < String > > matchHomeData, 
			String vConversationId, String vContestId, String fromFragment,
			ListView mlListView, String vMainColor, String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		
		this.vMainColor = vMainColor;
		this.vMainTitleColor = vMainTitleColor;
		
		this.vConversationId = vConversationId;
		this.vContestId = vContestId;
		this.fromFragment = fromFragment;
		this.matchHomeData = matchHomeData;
		isListViewScrolling	=	false;
		this.mlListView	= mlListView;
		this.mlListView.setOnScrollListener(scrollListener);
		
		this.topFragmentName  = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
		
		this.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {

		try {
			if ( matchHomeData != null && matchHomeData.containsKey( "message_timestamp" ) ) {
				return matchHomeData.get( "message_timestamp" ).size();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show(e);
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {

		if ( getCount() > 0 ) {
			return 1;
		}
		return null;
	}


	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {


		if ( inflater == null ) {
			LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return  layoutInflater.inflate( R.layout.matchroomview, null);
		}

		if (convertView == null) {
			convertView  = inflater.inflate( R.layout.matchroomview , null );
			holder		=	new ViewHolder();
			holder.linearLayout1 = (LinearLayout)convertView.findViewById(R.id.linearLayout1);
			holder.userMessage = (TextView) convertView.findViewById( R.id.userMsg );
			holder.timeStamp = (TextView) convertView.findViewById( R.id.userTimestamp );
			holder.userName = (TextView) convertView.findViewById( R.id.userName );
			holder.userAvatar = (ImageView) convertView.findViewById( R.id.imageViewpostAvatar );
			holder.mRaltiveLayout	=	(RelativeLayout) convertView.findViewById( R.id.mainLayout );
			holder.gapTextView		=	(TextView) convertView.findViewById( R.id.gapTextView );
			holder.gapLinearView	=	(RelativeLayout) convertView.findViewById(R.id.gapLinearView);
			holder.gapMoreTextView	=	(TextView) convertView.findViewById( R.id.gapMoreTextView );
			holder.gapTextLayout = (LinearLayout)convertView.findViewById(R.id.gapTextLayout);
			holder.gapProgress = (ProgressBar)convertView.findViewById(R.id.gapProgress);
			holder.userPost = (RelativeLayout)convertView.findViewById(R.id.userPost);
			// settin the typefaces
			holder.userMessage.setTypeface(Constants.OPEN_SANS_REGULAR );
			holder.userName.setTypeface(Constants.OPEN_SANS_SEMIBOLD );
			holder.timeStamp.setTypeface( Constants.OPEN_SANS_REGULAR );
			holder.userAvatar.setOnClickListener ( avatarClickListener ); 
			holder.gapTextView.setTypeface(Constants.OPEN_SANS_BOLD );
			holder.gapMoreTextView.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
			convertView.setTag(holder);
		} else  {
			holder		=	(ViewHolder) convertView.getTag();
		}

		if( position == 0 ) {
			convertView.setPadding(0,10,0,0);
		} else {
			convertView.setPadding(0,0,0,0);
		}
		try {
			params = holder.linearLayout1.getLayoutParams();
			params.height=LayoutParams.WRAP_CONTENT;
			holder.linearLayout1.setLayoutParams(params);
			holder.linearLayout1.setOnTouchListener( null );
			if ( matchHomeData != null ) {
			
				//Check for Gap Item
				// set the gap 
				final String vNextUrl = matchHomeData.get( "vNextUrl" ).get( position );
				final String vNextHrefUrl = matchHomeData.get( "vNextHref" ).get( position );
				
				if ( (vNextUrl != null && vNextUrl.trim().length() > 0)  || (vNextHrefUrl != null && vNextHrefUrl.trim().length() > 0) ) {

					// setting the url inthe gap url key value.

					holder.mRaltiveLayout.setVisibility(View.GONE );
					
					
					int gapSize 	=	mDatabaseUtil.getGapSize(vNextUrl,vNextHrefUrl);
					if(gapSize == 0){
						holder.gapLinearView.setVisibility(View.GONE);
						holder.gapTextLayout.setVisibility(View.GONE);
						holder.gapProgress.setVisibility(View.GONE);
					}else{
						holder.gapLinearView.setVisibility(View.VISIBLE);
						holder.gapTextLayout.setVisibility(View.VISIBLE);
						holder.gapProgress.setVisibility(View.GONE);
						holder.gapTextView.setText (""+mDatabaseUtil.getGapSize(vNextUrl,vNextHrefUrl));
						holder.gapTextView.setFocusable( true );
						holder.gapTextView.setTag( "GAP" );
						holder.gapLinearView.setId( position );
						holder.gapLinearView.setOnClickListener( new OnClickListener() {

							@Override
							public void onClick(View v) {
								try {
									v.setOnClickListener( null );
									int pos = v.getId();
									if ( (vNextUrl != null && vNextUrl.trim().length() > 0) || (vNextHrefUrl != null && vNextHrefUrl.trim().length() > 0) ) {
										holder.gapTextLayout.setVisibility(View.GONE);
										holder.gapProgress.setVisibility(View.VISIBLE);


										FragmentManagerUtil fragmentManagerUtil = PlayupLiveApplication.getFragmentManagerUtil();
										if ( fragmentManagerUtil != null && fragmentManagerUtil.checkIfFragmentExists( topFragmentName ) ) {
											if ( fragmentManagerUtil.fragmentMap != null && fragmentManagerUtil.fragmentMap.containsKey( topFragmentName ) )  {

												MatchHomeFragment fragment = ( MatchHomeFragment ) fragmentManagerUtil.fragmentMap.get( topFragmentName ); 
												if ( fragment != null ) {
													
													if(vNextHrefUrl != null && vNextHrefUrl.trim().length() > 0){
														
														
														if ( fragment.runnableList!=null && !fragment.runnableList.containsKey(vNextHrefUrl)  && Util.isInternetAvailable() ){

															fragment.runnableList.put(vNextHrefUrl, new Util().callNextUrl( vNextHrefUrl, vConversationId ,fragment.runnableList,true));

														}
													}else if(vNextUrl != null && vNextUrl.trim().length() > 0){
														
														if ( fragment.runnableList!=null && !fragment.runnableList.containsKey(vNextUrl)  && Util.isInternetAvailable() ){

															fragment.runnableList.put(vNextUrl, new Util().callNextUrl( vNextUrl, vConversationId ,fragment.runnableList,false));

														}
														
													} 
													
													
												}
											}
										}


									}
								} catch (Exception e) {
									//Logs.show(e);
								}
							}

						});
					}
				}else{

					try {
						//Normal Message Item

						// handling the click listener 
						holder.userAvatar.setImageResource(R.drawable.head);
						holder.userAvatar.setBackgroundColor(Color.WHITE);
						holder.mRaltiveLayout.setVisibility(View.VISIBLE);
						holder.gapLinearView.setVisibility(View.GONE);
						// setting  the data








						holder.userMessage.setText( new Util().getSmiledText ( matchHomeData.get( "message" ).get( position ) .replaceAll("(\\r|\\n|\\t)", " ")) );
						holder.timeStamp.setText( dateutil.gmt_to_local_timezone( matchHomeData.get( "message_timestamp" ).get( position ) ) );
						holder.userName.setText( new Util().getSmiledText(  matchHomeData.get( "posted_by" ).get( position ) ) );

						// showing the avatar images for the user/ fans

						imageDownloader.download(  matchHomeData.get( "fan_thumb_url" ).get( position ), holder.userAvatar, true, this );
						holder.userAvatar.setTag( R.id.about_txtview,  matchHomeData.get( "fan_profile_uid" ).get( position ) );
						
						if(matchHomeData.get( "fan_profile_href" ).get( position ) != null && matchHomeData.get( "fan_profile_href" ).get( position ).trim().length() > 0){
							holder.userAvatar.setTag( R.id.activity_list_relativelayout,  matchHomeData.get( "fan_profile_href" ).get( position ) );
							holder.userAvatar.setTag( R.id.addFriends,  true);
						}else if(matchHomeData.get( "fan_profile_url" ).get( position ) != null && matchHomeData.get( "fan_profile_url" ).get( position ).trim().length() > 0){
							holder.userAvatar.setTag( R.id.activity_list_relativelayout,  matchHomeData.get( "fan_profile_url" ).get( position ) );
							holder.userAvatar.setTag( R.id.addFriends,  false);
						}


						holder.linearLayout1.setTag(  matchHomeData.get( "posted_by" ).get( position ) );
						holder.linearLayout1.setOnTouchListener( this );
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//Logs.show(e);
					}
				}
			} else {
			}
		}
		catch (Exception e) {
			//Logs.show( e );

		}catch (Error e) {
			//Logs.show( e );
		}
		highLightBlueColor(holder.linearLayout1, false);
		params = holder.linearLayout1.getLayoutParams();
		params.height=LayoutParams.WRAP_CONTENT;
		holder.linearLayout1.setLayoutParams(params);
		holder.linearLayout1.setBackgroundResource( R.drawable.post_base );
		return convertView;
	}


	static class ViewHolder {
		TextView userMessage ;
		TextView timeStamp;
		TextView userName;
		ImageView userAvatar;
		RelativeLayout mRaltiveLayout;
		RelativeLayout gapLinearView;
		TextView gapTextView;
		TextView gapMoreTextView;
		LinearLayout gapTextLayout;
		ProgressBar gapProgress;
		RelativeLayout userPost;
		LinearLayout linearLayout1;
	}




	private OnClickListener avatarClickListener = new OnClickListener( ) {

		@Override
		public void onClick(View v) {

			try {
				// checking if the profile is of primary user or not.
				String iUserId = null;
				if( v.getTag( R.id.about_txtview )!= null  )
					iUserId = v.getTag( R.id.about_txtview ).toString();

				if ( iUserId != null && iUserId.trim().length() > 0 ) {

					iUserId = null;
					int myId=-1;
					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT iId FROM user WHERE isPrimaryUser = \"" + 1 + "\" ");
					if ( c != null ) {

						if ( c.getCount() > 0 ) {
							c.moveToFirst();
							myId = c.getInt( c.getColumnIndex( "iId" ) );
						}
						c.close();
						c = null;
					}

					String vSelfUrl = (String)v.getTag( R.id.activity_list_relativelayout );
					boolean isHref = (Boolean) v.getTag(R.id.addFriends);
//					int count = PlayupLiveApplication.getDatabaseWrapper().getTotalCount( " SELECT vSelfUrl FROM user WHERE vSelfUrl = \"" + vSelfUrl + "\" ");
					//new Util().getFanProfileData ( vSelfUrl );

					Bundle bundle = new Bundle();
					bundle.putString( "vSelfUrl", vSelfUrl );
					bundle.putInt("myId", myId);
					bundle.putBoolean("isHref",isHref);
					bundle.putString("vMainColor",vMainColor );
					bundle.putString("vMainTitleColor",vMainTitleColor );
					
					bundle.putString( "vSecColor",vSecColor );			
					bundle.putString( "vSecTitleColor",vSecTitleColor );
					


					FragmentManagerUtil fragmentManagerUtil = PlayupLiveApplication.getFragmentManagerUtil();
					if ( fragmentManagerUtil != null && fragmentManagerUtil.checkIfFragmentExists( topFragmentName ) ) {
						if ( fragmentManagerUtil.fragmentMap != null && fragmentManagerUtil.fragmentMap.containsKey( topFragmentName ) )  {

							MatchHomeFragment fragment = ( MatchHomeFragment ) fragmentManagerUtil.fragmentMap.get( topFragmentName ); 
							if ( fragment != null ) {
								fragment.cancelRunnable ();
							}
						}
					}

					PlayupLiveApplication.getFragmentManagerUtil().setFragment( "PublicProfileFragment", bundle );

				}
			} catch (Exception e) {
				//Logs.show(e);
			}
		}
	};


	@Override
	public void notifyDataSetChanged(){
		try {
			if(isListViewScrolling  ==  false){
				super.notifyDataSetChanged();  

			}else{

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show(e);
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



	@Override
	public void onClick(View v) {
		try {
			if( v.getId() == R.id.linearLayout1 ) {
				
				String vFriendName = v.getTag().toString();
				DatabaseUtil dbUtil =DatabaseUtil.getInstance();
				Bundle bundle;
				if (dbUtil.isUserAnnonymous()) {
					MatchHomeFragment.postClicked = true;
					MatchHomeFragment.friendName = vFriendName;
					bundle = new Bundle();
					bundle.putString("fromFragment", topFragmentName);
					bundle.putString("vConversationId", vConversationId);
					bundle.putString("vFriendName", vFriendName);
					
					bundle.putString("vMainColor",vMainColor );
					bundle.putString("vMainTitleColor",vMainTitleColor );
					
					bundle.putString( "vSecColor",vSecColor );			
					bundle.putString( "vSecTitleColor",vSecTitleColor );

				
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("ProviderFragment", bundle);
				} else {
					bundle = new Bundle();
					bundle.putString("vConversationId", vConversationId);
					bundle.putString("vFriendName", vFriendName);
					bundle.putString("fromFragment", topFragmentName);
					bundle.putString("vMainColor",vMainColor );
					bundle.putString("vMainTitleColor",vMainTitleColor );
					
					
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("PostMessageFragment", bundle);
				}
				PostMessageFragment.isHomeTapped = false;
			}
		} catch (Exception e) {
			//Logs.show(e);
		}

	}




	private  float rawX	=	0;
	private  float rawY	=	0;
	long downTime		=	0;

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if ( event.getAction() == MotionEvent.ACTION_DOWN ) {

			downTime 	=	event.getDownTime();
			rawX			=	event.getRawX();
			rawY			=	event.getRawY();
			// highlight blue color
			startUpdating(v);
		} 
		if ( event.getAction() == MotionEvent.ACTION_UP ) {

			stopUpdating();
			if(event.getRawY()==rawY||(event.getEventTime()-downTime)<200){
				highLightBlueColor ( v, true );
			}else{
				// disable the blue color
				highLightBlueColor ( v, false );
			}
			// handle click 
			onClick ( v );
		} 
		if ( event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_OUTSIDE ) {
			stopUpdating();
			// disable the blue color
			highLightBlueColor ( v, false );
		}


		if ( event.getEventTime ( ) > ( downTime + Constants.highightDelay ) ) {

			if ( event.getRawY ( )  >= rawY -10 && event.getRawY ( ) <= rawY + 10 ) {

				stopUpdating();
				highLightBlueColor ( v, true );

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

				highLightBlueColor ( v, true );
				super.handleMessage(msg);
			}
		};
		mUpdater = Executors.newSingleThreadScheduledExecutor();
		mUpdater.schedule(new UpdateCounterTask(), 200,
				TimeUnit.MILLISECONDS);
	}

	private void stopUpdating() {
		if(mUpdater!=null&&!mUpdater.isShutdown()){
			mUpdater.shutdownNow();
			mUpdater = null;
		}
	}

	private class UpdateCounterTask implements Runnable {
		public void run() {

			try {
				if ( mHandler != null  ) {

					mHandler.sendEmptyMessage(0);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//Logs.show ( e );
			}
		}
	}


	private void highLightBlueColor ( View v, boolean isSelected ) {


		try {
			if ( v == null)  {
				return;
			}
			if ( isSelected ) {
				( ( TextView ) v.findViewById( R.id.userTimestamp ) ).setTextColor( Color.parseColor( "#FFFFFF") );
				( ( TextView ) v.findViewById( R.id.userName ) ).setTextColor( Color.parseColor( "#FFFFFF") );
				( ( TextView ) v.findViewById( R.id.userMsg ) ).setTextColor( Color.parseColor( "#FFFFFF") );
				params = v.getLayoutParams();
				if( v.getHeight() > 0)
					params.height=v.getHeight();
				v.setLayoutParams(params);	
				v.setBackgroundResource( R.drawable.notification_base_d );
			} else {

				( ( TextView ) v.findViewById( R.id.userTimestamp ) ).setTextColor( Color.parseColor( "#B9B6B9") );
				( ( TextView ) v.findViewById( R.id.userMsg ) ).setTextColor( Color.parseColor( "#ABABAB") );
				( ( TextView ) v.findViewById( R.id.userName ) ).setTextColor( Color.parseColor( "#FF4754") );
				params = v.getLayoutParams();
				if( v.getHeight() > 0)
					params.height=v.getHeight();
				v.setLayoutParams(params);	
				v.setBackgroundResource( R.drawable.post_base );

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	Logs.show(e);
		}
	}


}
