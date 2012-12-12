package com.playup.android.adapters;

import java.util.ArrayList;
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
import com.playup.android.fragment.PrivateLobbyMessageFragment;
import com.playup.android.fragment.PrivateLobbyRoomFragment;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;
import com.playup.android.util.FragmentManagerUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;


public class PrivateMessagesAdapter extends BaseAdapter implements OnClickListener,OnTouchListener {


	private LayoutInflater inflater  = null;
	private Hashtable<String, List<String>>  data;
	private ImageDownloader imageDownloader = new ImageDownloader();
	private DateUtil dateutil = new DateUtil();
	private String vConversationId;
	LayoutParams params;
	private boolean isListViewScrolling	=	false;
	ListView mlListView;
	private String vConversationName = null;
	private List < String > gapDownloadingUrls = new ArrayList < String >();
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;

	public PrivateMessagesAdapter ( Hashtable<String, List<String>>  data, String vConversationId,
			ListView mlListView, String vConversationName,String vMainColor,String vMainTitleColor, 
			String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;

		
		this.vMainColor  = vMainColor;
		this.vMainTitleColor = vMainTitleColor;

		this.vConversationId = vConversationId;

		this.vConversationName = vConversationName;
		this.data = data;
		this.mlListView	= mlListView;
		this.mlListView.setOnScrollListener(scrollListener);
		isListViewScrolling	=	false;
		inflater = LayoutInflater.from(  PlayUpActivity.context );
	}

	public void setData ( Hashtable<String, List<String>>  data, String vConversationId,ListView mlListView, 
			String vConversationName ,String vMainColor,String vMainTitleColor, String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		
		
		this.vMainColor  = vMainColor;
		this.vMainTitleColor = vMainTitleColor;
		
		
		this.vConversationId = vConversationId;

		this.vConversationName = vConversationName;
		this.data = data;
		isListViewScrolling	=	false;
		this.mlListView	= mlListView;
		this.mlListView.setOnScrollListener(scrollListener);
		this.notifyDataSetChanged();


	}
	@Override
	public int getCount() {

		if ( data != null && data.get( "vMessageId" ) != null && data.get( "vMessageId" ).size() > 0 ) {
			return data.get( "vMessageId" ).size();
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

		ViewHolder holder = null;
		if ( inflater == null ) {
			LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return  layoutInflater.inflate( R.layout.matchroomview, null);
		}

		if (convertView == null) {
			convertView  = inflater.inflate( R.layout.matchroomview , null );
			holder		=	new ViewHolder();

			holder.linearLayout1 = (LinearLayout) convertView.findViewById(R.id.linearLayout1);
			holder.userMessage = (TextView) convertView.findViewById( R.id.userMsg );
			holder.timeStamp = (TextView) convertView.findViewById( R.id.userTimestamp );
			holder.userName = (TextView) convertView.findViewById( R.id.userName );
			holder.postAvatar = (ImageView) convertView.findViewById( R.id.imageView1 );
			holder.userAvatar = (ImageView) convertView.findViewById( R.id.imageViewpostAvatar );
			holder.mRaltiveLayout	=	(RelativeLayout) convertView.findViewById( R.id.mainLayout );			
			holder.userPost = (RelativeLayout)convertView.findViewById(R.id.userPost);

			holder.gapLinearView	=	(RelativeLayout) convertView.findViewById(R.id.gapLinearView);
			holder.gapTextLayout = (LinearLayout)convertView.findViewById(R.id.gapTextLayout);;
			holder.gapProgress = (ProgressBar)convertView.findViewById(R.id.gapProgress);;
			holder.gapTextView = (TextView) holder.gapTextLayout.findViewById( R.id.gapTextView );
			holder.gapMoreTextView = (TextView) holder.gapTextLayout.findViewById( R.id.gapMoreTextView );
			holder.remoteIcon = (ImageView) convertView.findViewById(R.id.remoteIcon);

			// settin the typefaces
			holder.userMessage.setTypeface(Constants.OPEN_SANS_REGULAR );
			holder.userName.setTypeface(Constants.OPEN_SANS_SEMIBOLD );
			holder.timeStamp.setTypeface( Constants.OPEN_SANS_REGULAR );

			holder.gapTextView.setTypeface(Constants.OPEN_SANS_BOLD );
			holder.gapMoreTextView.setTypeface(Constants.OPEN_SANS_SEMIBOLD);

			convertView.setTag(holder);
		} else  {
			holder		=	(ViewHolder) convertView.getTag();
		}

		highLightBlueColor ( holder.linearLayout1, false );
		try {


			if ( data.get( "vGapId" ).get( position ) != null ) {

				holder.mRaltiveLayout.setVisibility( View.GONE );
				holder.gapLinearView.setVisibility( View.VISIBLE );

				if ( gapDownloadingUrls != null && gapDownloadingUrls.contains( data.get( "vGapId" ).get( position ) ) || data.get( "isGapLoading" ).get( position ).equalsIgnoreCase( "1" ) ) {
					holder.gapTextLayout.setVisibility( View.GONE );
					holder.gapProgress.setVisibility( View.VISIBLE );

					holder.gapLinearView.setOnClickListener( null );
				} else {

					holder.gapTextLayout.setVisibility( View.VISIBLE );
					holder.gapProgress.setVisibility( View.GONE );

					holder.gapTextView.setText( data.get( "iGapSize" ).get( position ) );
					
					if(data.get( "vContentUrl" ).get( position ) != null && data.get( "vContentUrl" ).get( position ).trim().length() > 0){
						holder.gapLinearView.setTag( data.get( "vContentUrl" ).get( position ) );
						holder.gapLinearView.setTag( R.id.anonymous_notification,false );
						
					}else if( data.get( "vContentHrefUrl" ).get( position ) != null &&  data.get( "vContentHrefUrl" ).get( position ).trim().length() > 0){
						holder.gapLinearView.setTag( data.get( "vContentHrefUrl" ).get( position ) );
						holder.gapLinearView.setTag( R.id.anonymous_notification,true );
					}
					
					holder.gapLinearView.setTag( R.id.about_txtview, data.get( "vGapId" ).get( position ) );

					holder.gapLinearView.setOnClickListener( this );
				}
				return convertView;
			} else {
				holder.mRaltiveLayout.setVisibility( View.VISIBLE );
				holder.gapLinearView.setVisibility( View.GONE );
			}
			holder.linearLayout1.setOnTouchListener( null );
			holder.userAvatar.setImageResource(R.drawable.head);
			holder.userAvatar.setBackgroundColor(Color.WHITE);
			holder.mRaltiveLayout.setVisibility(View.VISIBLE);

			// setting  the data



			holder.userMessage.setText( new Util().getSmiledText( data.get( "vMessage" ).get( position ).replaceAll("(\\r|\\n|\\t)", " ") ) );
			holder.timeStamp.setText( dateutil.gmt_to_local_timezone( data.get( "vCreatedDate" ).get( position ) ) );
			holder.userName.setText( new Util().getSmiledText( data.get( "vDisplayName" ).get( position ) ) );

			// showing the avatar images for the user/ fans

			if( data.get( "vSubjectId" ).get( position ) != null && data.get( "vSubjectId" ).get( position ).trim().length() > 0 ) { 
				holder.remoteIcon.setVisibility(View.VISIBLE);
				holder.postAvatar.setVisibility(View.GONE);
				holder.userAvatar.setVisibility(View.GONE);
				holder.userAvatar.setOnClickListener ( null ); 
				holder.userName.setTag(0);
				//				holder.linearLayout1.setTag( data.get( "vDisplayName" ).get( position ) );
				holder.linearLayout1.setOnTouchListener( this );
				holder.remoteIcon.setOnClickListener ( this ); 
				holder.userName.setTextColor(Color.parseColor("#81A9A9"));
				holder.userMessage.setTextColor(Color.parseColor("#ABABAB"));
				holder.timeStamp.setTextColor(Color.parseColor("#B9B6B9"));
				holder.userName.setTextSize(14);
				if(data.get( "vSubjectUrl" ).get( position ) != null && data.get( "vSubjectUrl" ).get( position ).trim().length() > 0){
					holder.remoteIcon.setTag(R.id.about_txtview,data.get( "vSubjectUrl" ).get( position ));						
					holder.remoteIcon.setTag(R.id.avtarGreenBase,false);
					holder.linearLayout1.setTag( R.id.about_txtview,data.get( "vSubjectUrl" ).get( position ) );
					holder.linearLayout1.setTag(R.id.avtarGreenBase,false);
				}else if(data.get( "subjectHrefUrl" ).get( position ) != null && data.get( "subjectHrefUrl" ).get( position ).trim().length() > 0){
					holder.remoteIcon.setTag(R.id.about_txtview,data.get( "subjectHrefUrl" ).get( position ));			
					
					holder.remoteIcon.setTag(R.id.avtarGreenBase,true);
					holder.linearLayout1.setTag( R.id.about_txtview,data.get( "subjectHrefUrl" ).get( position ) );
					holder.linearLayout1.setTag(R.id.avtarGreenBase,true);
				}
				
				holder.remoteIcon.setTag(R.id.activity_list_relativelayout,data.get( "vSubjectId" ).get( position ) );
				holder.linearLayout1.setTag("");
				holder.linearLayout1.setTag(R.id.activity_list_relativelayout,data.get( "vSubjectId" ).get( position ) );
				
				
				
				
				


			} else {
				holder.remoteIcon.setVisibility(View.INVISIBLE);
				holder.postAvatar.setVisibility(View.VISIBLE);
				holder.userAvatar.setVisibility(View.VISIBLE);

				holder.userAvatar.setTag( R.id.about_txtview, data.get( "vUserId" ).get( position ) );
				if(data.get( "vSelfUrl").get( position ) != null && data.get( "vSelfUrl").get( position ).trim().length() > 0){
					
					holder.userAvatar.setTag( R.id.activity_list_relativelayout, data.get( "vSelfUrl").get( position ) );
					holder.userAvatar.setTag( R.id.avtarTounge, false);
				}else if(data.get( "vHrefUrl").get( position ) != null && data.get( "vHrefUrl").get( position ).trim().length() > 0) {
				
					holder.userAvatar.setTag( R.id.activity_list_relativelayout, data.get( "vHrefUrl").get( position ) );
					
					holder.userAvatar.setTag( R.id.avtarTounge, true);
				}
				
				holder.userAvatar.setOnClickListener ( avatarClickListener ); 

				imageDownloader.download( data.get( "vAvatarUrl").get( position ), holder.userAvatar, true, this );


				holder.userName.setTag(1);
				holder.linearLayout1.setTag( data.get( "vDisplayName" ).get( position ) );

				holder.linearLayout1.setOnTouchListener( this );
				holder.userName.setTextColor(Color.parseColor("#FF4754"));
				holder.userMessage.setTextColor(Color.parseColor("#ABABAB"));
				holder.timeStamp.setTextColor(Color.parseColor("#B9B6B9"));
				holder.userName.setTextSize(16);

			}

			params = holder.linearLayout1.getLayoutParams();
			params.height=LayoutParams.WRAP_CONTENT;
			holder.linearLayout1.setLayoutParams(params);

			holder.linearLayout1.setBackgroundResource( R.drawable.post_base );


		} catch ( Exception e ) {
			Logs.show(e);
		}
		return convertView;
	}


	class ViewHolder {

		ImageView postAvatar;
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
		ImageView remoteIcon;
		LinearLayout linearLayout1;

	}


	private OnClickListener avatarClickListener = new OnClickListener( ) {

		@Override
		public void onClick(View v) {

			try {
				// checking if the profile is of primary user or not.
				String iUserId = v.getTag( R.id.about_txtview ).toString();
				String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
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
					boolean isHref = ((Boolean) v.getTag( R.id.avtarTounge )).booleanValue();
					Bundle bundle = new Bundle();
					bundle.putString( "vSelfUrl", vSelfUrl );
					bundle.putInt("myId", myId);
					bundle.putBoolean("isHref", isHref);
					bundle.putString("fromFragment", topFragmentName);
					bundle.putString("vMainColor",vMainColor );
					bundle.putString("vMainTitleColor",vMainTitleColor );
					bundle.putString( "vSecColor",vSecColor );			
					bundle.putString( "vSecTitleColor",vSecTitleColor );
					
					FragmentManagerUtil fragmentManagerUtil = PlayupLiveApplication.getFragmentManagerUtil();
					if ( fragmentManagerUtil != null && fragmentManagerUtil.checkIfFragmentExists( topFragmentName ) ) {
						if ( fragmentManagerUtil.fragmentMap != null && fragmentManagerUtil.fragmentMap.containsKey( topFragmentName ) )  {

							PrivateLobbyRoomFragment fragment = ( PrivateLobbyRoomFragment ) fragmentManagerUtil.fragmentMap.get( topFragmentName ); 
							if ( fragment != null ) {
								fragment.cancelRunnable ();
							}
						}
					}

					PlayupLiveApplication.getFragmentManagerUtil().setFragment( "PublicProfileFragment", bundle );

				}
			} catch (Exception e) {
				Logs.show(e);
			}
		}
	};


	@Override
	public void notifyDataSetChanged(){
		if(isListViewScrolling  ==  false){
			super.notifyDataSetChanged();  

		}else{

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
			String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
			
			if( v.getId() == R.id.linearLayout1 ) {



				DatabaseUtil dbUtil =DatabaseUtil.getInstance();
				Bundle bundle;
				if (dbUtil.isUserAnnonymous()) {
					bundle = new Bundle();
					bundle.putString("fromFragment", topFragmentName);
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("ProviderFragment", bundle);
				} else if(v.getTag().toString() != null && v.getTag().toString().trim().length() > 0){


					String vFriendName = v.getTag().toString();
					PrivateLobbyMessageFragment.isHomeTapped = false;
					bundle = new Bundle();
					bundle.putString("fromFragment", topFragmentName);
					bundle.putString("vConversationId", vConversationId);
					bundle.putString("vFriendName", vFriendName);
					bundle.putString("vMainColor",vMainColor );
					bundle.putString("vMainTitleColor",vMainTitleColor );
					bundle.putString( "vSecColor",vSecColor );			
					bundle.putString( "vSecTitleColor",vSecTitleColor );
					PlayupLiveApplication.getFragmentManagerUtil().setFragment("PrivateLobbyMessageFragment", bundle);
				}else{

					String vSubjectUrl = v.getTag(R.id.about_txtview).toString();
					boolean isHref = ((Boolean) v.getTag(R.id.avtarGreenBase)).booleanValue();

					if(vSubjectUrl != null && vSubjectUrl.trim().length() > 0){


						Message msg = new Message();
						Bundle b = new Bundle();
						b.putString("vContestId",v.getTag(R.id.activity_list_relativelayout).toString());
						b.putString("vContestUrl",vSubjectUrl);
						msg.setData(b);
						msg.obj = "updateHeader";
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);

						
						
						FragmentManagerUtil fragmentManagerUtil = PlayupLiveApplication.getFragmentManagerUtil();
						if ( fragmentManagerUtil != null && fragmentManagerUtil.checkIfFragmentExists( topFragmentName ) ) {
							if ( fragmentManagerUtil.fragmentMap != null && fragmentManagerUtil.fragmentMap.containsKey( topFragmentName ) )  {

								PrivateLobbyRoomFragment fragment = ( PrivateLobbyRoomFragment ) fragmentManagerUtil.fragmentMap.get( topFragmentName ); 
								if ( fragment != null ) {
									if ( fragment.runnableList!=null && ! fragment.runnableList.containsKey(vSubjectUrl)  && Util.isInternetAvailable() ){
										PrivateLobbyRoomFragment.isSubjectDownloading = true;
										fragment.runnableList.put(Constants.GET_CONTEST_CONVERSATION_MESSAGES,new Util().getContestsData(vSubjectUrl,fragment.runnableList,isHref));
									}
								}
							}
						}

						notifyDataSetChanged();
					}
				}
			}

			else if ( v.getId() == R.id.gapLinearView ) {
				String vContentUrl =  (String) v.getTag();
				boolean isHref = ((Boolean) v.getTag(R.id.anonymous_notification)).booleanValue();
				String vGapId = ( String ) v.getTag( R.id.about_txtview ); 

				// adding in the list to identify the loading gap
				if ( !gapDownloadingUrls.contains( vGapId ) ) {
					gapDownloadingUrls.add( vGapId );

					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					dbUtil.setPrivateLobbyGapLoading ( vGapId, 1 );

					new Util().getPrivateLobbyConversationGap ( vGapId, vContentUrl,isHref ); 

				}

				// show loading on the gap 
				v.findViewById( R.id.gapTextLayout ).setVisibility( View.GONE );
				v.findViewById( R.id.gapProgress ).setVisibility( View.VISIBLE ); 

				notifyDataSetChanged();
			}else if(v.getId() == R.id.remoteIcon ){
				String vSubjectUrl = v.getTag(R.id.about_txtview).toString();
				boolean isHref = ((Boolean) v.getTag(R.id.avtarGreenBase)).booleanValue();

				if(vSubjectUrl != null && vSubjectUrl.trim().length() > 0){


					Message msg = new Message();
					Bundle b = new Bundle();
					b.putString("vContestId",v.getTag(R.id.activity_list_relativelayout).toString());
					b.putString("vContestUrl",vSubjectUrl);
					b.putBoolean("isHref",isHref);
					

					msg.setData(b);
					msg.obj = "updateHeader";
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
					
					FragmentManagerUtil fragmentManagerUtil = PlayupLiveApplication.getFragmentManagerUtil();
					if ( fragmentManagerUtil != null && fragmentManagerUtil.checkIfFragmentExists( topFragmentName ) ) {
						if ( fragmentManagerUtil.fragmentMap != null && fragmentManagerUtil.fragmentMap.containsKey( topFragmentName ) )  {

							PrivateLobbyRoomFragment fragment = ( PrivateLobbyRoomFragment ) fragmentManagerUtil.fragmentMap.get( topFragmentName ); 
							if ( fragment != null ) {
								if (fragment.runnableList!=null && !fragment.runnableList.containsKey(vSubjectUrl)  && Util.isInternetAvailable() ){
									PrivateLobbyRoomFragment.isSubjectDownloading = true;

									fragment.runnableList.put(Constants.GET_CONTEST_CONVERSATION_MESSAGES,new Util().getContestsData(vSubjectUrl,fragment.runnableList,isHref));


								}
							}
						}
					}
					
					notifyDataSetChanged();
				}


			}
		} catch (Exception e) {
			Logs.show(e);
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
			highLightBlueColor ( v, true );
			//			startUpdating(v);
		} 
		if ( event.getAction() == MotionEvent.ACTION_UP ) {
			//			stopUpdating();
			//			if(event.getRawY()==rawY||(event.getEventTime()-downTime)<200){
			//				highLightBlueColor ( v, true );
			//			}else{
			//				// disable the blue color
			//				highLightBlueColor ( v, false );
			//			}

			//			stopUpdating();
			highLightBlueColor(v, false);
			//			if (event.getRawY() == rawY
			//					|| (event.getEventTime() - downTime) < 200) {
			//
			//				// highLightBlueColor(v, true);
			//				
			//						
			//
			//							highLightBlueColor(v, false);
			//						
			//
			//				
			//
			//			}

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
				Logs.show ( e );
			}
		}
	}


	private void highLightBlueColor ( View v, boolean isSelected ) {


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

			if( (( TextView ) v.findViewById( R.id.userName ) ).getTag() != null &&  ( Integer.parseInt ((( TextView ) v.findViewById( R.id.userName ) ).getTag().toString()) == 0 ) ) {
				( ( TextView ) v.findViewById( R.id.userName ) ).setTextColor( Color.parseColor( "#81A9A9") );	   
			} else {
				( ( TextView ) v.findViewById( R.id.userName ) ).setTextColor( Color.parseColor( "#FF4754") );
			}

			params = v.getLayoutParams();
			if( v.getHeight() > 0)
				params.height=v.getHeight();
			v.setLayoutParams(params);	
			v.setBackgroundResource( R.drawable.post_base );

		}
	}






}
