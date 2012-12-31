package com.playup.android.adapters;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

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
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.fragment.NotificationFragment;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;
import com.playup.android.util.ImageDownloader;

import com.playup.android.util.Util;

public class NotificationAdapter extends BaseAdapter  implements OnTouchListener, OnClickListener {

	private LayoutInflater inflater;
	private ImageDownloader imageDownloader = new ImageDownloader();
	private DateUtil dateutil = new DateUtil();

	private Hashtable<String, List< String > > data; 


	private final String TYPE_FRIEND_NOTIFICATION = "application/vnd.playup.friend.invitation+json";
	private final String TYPE_CONVERSATION_NOTIFICATION = "application/vnd.playup.conversation.invitation+json";
	private final String TYPE_GAP = "application/vnd.playup.collection.gap+json";
	
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;




	public NotificationAdapter( Hashtable<String, List< String > > data,String vMainColor, String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		this.vMainColor = vMainColor;
		this.vMainTitleColor = vMainTitleColor;


		this.data = data;
		inflater = (LayoutInflater) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}


	public void setData ( Hashtable<String, List< String > >  data, String vMainColor, String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		this.vMainColor = vMainColor;
		this.vMainTitleColor = vMainTitleColor;


		this.data = data;
		notifyDataSetChanged();
	}


	@Override
	public int getCount() {

		if ( data != null && data.get( "vNotificationId" ) != null ) {
			return data.get( "vNotificationId" ).size();
		}
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


	class ViewHolder {

		// main layout
		LinearLayout postBase;
		RelativeLayout avatar_layout;

		// friend request
		RelativeLayout friend_avatar_layout;
		RelativeLayout userLayout;
		ImageView friend_avatar;
		TextView userName ;
		ImageView userLayout_blueDot;
		ImageView userLayout_arrow;
		TextView userMsg;
		TextView userTimestamp;
		TextView acceptedIgnore;
		RelativeLayout buttonBaseCheck;
		RelativeLayout buttonBaseUnCheck;
		FrameLayout confirmationlayout;

		// conversation request
		RelativeLayout conversation_avatar_layout;
		ImageView conversation_avatar;
		RelativeLayout conversationLayout;
		TextView leagueId;
		TextView teamNameId;
		ImageView leagueLayout_blueDot;
		ImageView redLine;

		// gap views
		RelativeLayout gapLinearView;
		TextView gapTextView;
		TextView gapMoreTextView;

		private LinearLayout  gapTextLayout;
		private ProgressBar gapProgress;

	}




	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if ( inflater == null ) {
			LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return  layoutInflater.inflate( R.layout.notification_view, null);
		}
		ViewHolder vh = null;
		if ( convertView == null){

			vh = new ViewHolder();

			convertView = inflater.inflate ( R.layout.notification_view, null );

			vh.postBase = (LinearLayout) convertView.findViewById( R.id.postBase );
			vh.avatar_layout = (RelativeLayout) convertView.findViewById( R.id.avatar_layout );

			vh.conversation_avatar_layout = ( RelativeLayout ) convertView.findViewById( R.id.premierLeague_avatar );
			vh.friend_avatar_layout = ( RelativeLayout ) convertView.findViewById( R.id.user_avatar );

			vh.friend_avatar = (ImageView) vh.friend_avatar_layout.findViewById( R.id.imageViewpostAvatar );
			vh.conversation_avatar = (ImageView) vh.conversation_avatar_layout.findViewById( R.id.imageViewpostAvatar );


			// conversation views
			vh.conversationLayout = ( RelativeLayout ) convertView.findViewById( R.id.premierLeague );
			vh.leagueId = ( TextView ) vh.conversationLayout.findViewById( R.id.leagueId );
			vh.leagueLayout_blueDot = ( ImageView ) vh.conversationLayout.findViewById( R.id.blueDot );
			vh.teamNameId = ( TextView ) vh.conversationLayout.findViewById( R.id.teamNameId );
			vh.redLine = ( ImageView ) vh.conversationLayout.findViewById( R.id.redLine );

			// user views
			vh.userLayout = ( RelativeLayout ) convertView.findViewById( R.id.userPost );
			RelativeLayout  name_time_arrow_layout = (RelativeLayout) vh.userLayout.findViewById( R.id.name_time_arrow_layout );
			vh.userName = ( TextView ) name_time_arrow_layout.findViewById( R.id.userName);
			vh.userTimestamp =  ( TextView ) name_time_arrow_layout.findViewById( R.id.userTimestamp);

			FrameLayout dot_arrow_layout = (FrameLayout) name_time_arrow_layout.findViewById( R.id.dot_arrow_layout );
			vh.userLayout_blueDot = ( ImageView ) dot_arrow_layout.findViewById( R.id.blueDot );
			vh.userLayout_arrow  = ( ImageView ) dot_arrow_layout.findViewById( R.id.arrowChevron );

			RelativeLayout  msg_confirm_layout = (RelativeLayout) vh.userLayout.findViewById( R.id.msg_confirm_layout );
			vh.userMsg =  ( TextView ) msg_confirm_layout.findViewById( R.id.userMsg);

			vh.confirmationlayout = ( FrameLayout ) msg_confirm_layout.findViewById( R.id.confirmationlayout );
			vh.acceptedIgnore = ( TextView ) vh.confirmationlayout.findViewById( R.id.acceptedIgnore );

			LinearLayout confirmation_li_layout = (LinearLayout) vh.confirmationlayout.findViewById( R.id.confirmation_li_layout );
			vh.buttonBaseCheck = ( RelativeLayout ) confirmation_li_layout.findViewById( R.id.buttonBaseCheck );
			vh.buttonBaseUnCheck = ( RelativeLayout ) confirmation_li_layout.findViewById( R.id.buttonBaseUncheck );


			// gap views
			vh.gapLinearView = (RelativeLayout) convertView.findViewById( R.id.gapLinearView );
			vh.gapTextView   = (TextView) vh.gapLinearView.findViewById( R.id.friendGapTextView );
			vh.gapMoreTextView = (TextView) convertView.findViewById(R.id.friendGapMoreTextView);

			vh.gapTextLayout = (LinearLayout)convertView.findViewById(R.id.gapTextLayout);;
			vh.gapProgress = (ProgressBar)convertView.findViewById(R.id.gapProgress);;

			convertView.setTag( vh );
			setTypefaces(convertView);
		} else {
			vh = (ViewHolder)convertView.getTag();
		}

		vh.friend_avatar.setImageResource(R.drawable.head);
		vh.friend_avatar.setBackgroundColor(Color.WHITE);
		vh.conversation_avatar.setImageResource(R.drawable.head);
		vh.conversation_avatar.setBackgroundColor(Color.WHITE);

		if ( position == 0 ) {
			convertView.setPadding( 0, 10, 0, 0 );
		} else if ( position == getCount() - 1 ) {
			convertView.setPadding( 0, 0, 0, 10 );
		} else {
			convertView.setPadding( 0, 0, 0, 0);
		}
		vh.postBase.setVisibility( View.VISIBLE );
		vh.postBase.setBackgroundResource(R.drawable.post_base);

		vh.userName.setTextColor( Color.parseColor( "#FF4754" ) );
		vh.userMsg.setTextColor( Color.parseColor( "#ABABAB" ) );
		vh.userTimestamp.setTextColor( Color.parseColor( "#B9B6B9" ) );

		vh.avatar_layout.setVisibility( View.VISIBLE );

		vh.buttonBaseCheck.setOnTouchListener( null );
		vh.buttonBaseUnCheck.setOnTouchListener( null );


		vh.userName.setText( new Util().getSmiledText(data.get( "vDisplayName" ).get( position ) ));
		vh.userMsg.setText( R.string.sendYouFriendRequest );

		vh.userTimestamp.setText( dateutil.gmt_to_local_timezone(  data.get( "dTime" ).get( position )  ) );

		convertView.setTag( R.id.about_txtview, position );



		if ( data.get( "vSubjectType").get( position ).equalsIgnoreCase( TYPE_FRIEND_NOTIFICATION ) ) {

			vh.gapLinearView.setVisibility( View.GONE );
			vh.gapLinearView.setOnClickListener(null);
			vh.conversationLayout.setVisibility( View.GONE );
			vh.conversation_avatar_layout.setVisibility( View.GONE );
			vh.userLayout_arrow.setVisibility( View.GONE );

			vh.friend_avatar_layout.setVisibility( View.VISIBLE );
			vh.confirmationlayout.setVisibility( View.VISIBLE );

			vh.friend_avatar.setTag( vh );
			vh.friend_avatar.setTag( R.id.about_txtview, position );
			vh.friend_avatar.setOnClickListener( this );

			imageDownloader.download ( data.get( "vAvatarUrl" ).get( position ) , vh.friend_avatar, true, this );

			if ( Integer.parseInt( data.get ( "isRead" ).get( position ) ) == 0 ) {
				vh.userLayout_blueDot.setVisibility( View.VISIBLE );
			} else {
				vh.userLayout_blueDot.setVisibility( View.GONE );
			}

			String status = data.get( "vStatus" ).get( position );


			if ( status != null && status.trim().equalsIgnoreCase( "ignored" ) ) {
				vh.acceptedIgnore.setVisibility( View.VISIBLE );
				vh.buttonBaseCheck.setVisibility( View.GONE );
				vh.buttonBaseUnCheck.setVisibility( View.GONE );

				vh.acceptedIgnore.setText ( R.string.ignored  );

			} else if ( status != null && status.trim().equalsIgnoreCase( "confirmed" ) ) {
				vh.acceptedIgnore.setVisibility( View.VISIBLE );
				vh.buttonBaseCheck.setVisibility( View.GONE );
				vh.buttonBaseUnCheck.setVisibility( View.GONE );

				vh.acceptedIgnore.setText ( R.string.accepted  );
			} else {
				vh.acceptedIgnore.setVisibility( View.GONE );

				vh.buttonBaseCheck.setVisibility( View.VISIBLE );
				vh.buttonBaseUnCheck.setVisibility( View.VISIBLE );

				vh.buttonBaseCheck.setTag( vh );
				vh.buttonBaseCheck.setTag( R.id.about_txtview, position );
				vh.buttonBaseCheck.setTag( R.id.aboutScrollView, "confirmed" );

				vh.buttonBaseUnCheck.setTag( vh );
				vh.buttonBaseUnCheck.setTag( R.id.about_txtview, position );
				vh.buttonBaseUnCheck.setTag( R.id.aboutScrollView, "ignored" );

				vh.buttonBaseCheck.setOnTouchListener(this);
				vh.buttonBaseUnCheck.setOnTouchListener( this );

			}


			convertView.setOnTouchListener( null );
			return convertView;

		} else if ( data.get( "vSubjectType").get( position ).equalsIgnoreCase( TYPE_CONVERSATION_NOTIFICATION ) ) {

			vh.gapLinearView.setVisibility( View.GONE );
			vh.gapLinearView.setOnClickListener(null);
			vh.conversationLayout.setVisibility( View.VISIBLE );
			vh.conversation_avatar_layout.setVisibility( View.VISIBLE );
			vh.userLayout_arrow.setVisibility( View.VISIBLE );

			vh.userLayout_blueDot.setVisibility( View.GONE );
			vh.friend_avatar_layout.setVisibility( View.GONE );
			vh.confirmationlayout.setVisibility( View.GONE );

			vh.conversation_avatar.setTag( vh );
			vh.conversation_avatar.setTag( R.id.about_txtview, position );
			vh.conversation_avatar.setOnClickListener( this );

			imageDownloader.download ( data.get( "vAvatarUrl" ).get( position ) , vh.conversation_avatar, true, this );

			vh.leagueId.setText( data.get ( "vDetailTitle" ).get(position) );
			vh.teamNameId.setText( data.get ( "vDetailSubTitle" ).get(position) );
			vh.userMsg.setText( new Util().getSmiledText(data.get ( "vDetailMessage" ).get(position)) );

			if ( Integer.parseInt( data.get ( "isRead" ).get( position ) ) == 0 ) {
				vh.leagueLayout_blueDot.setVisibility( View.VISIBLE );
			} else {
				vh.leagueLayout_blueDot.setVisibility( View.GONE );
			}
			convertView.setOnTouchListener( this );	


			vh.leagueId.setTextColor( Color.parseColor( "#9D999D" ) );
			vh.teamNameId.setTextColor( Color.parseColor( "#5F5A5A" ) );


			vh.userLayout_arrow.setImageResource( R.drawable.chevron );
			vh.redLine.setBackgroundResource( R.drawable.notification_red_line );

			return convertView;
		} else if ( data.get( "vSubjectType" ).get( position ).equalsIgnoreCase( TYPE_GAP ) ) {

			vh.postBase.setVisibility( View.GONE );
			vh.avatar_layout.setVisibility( View.GONE );
			vh.gapLinearView.setTag(R.id.about_txtview,position);
			vh.gapLinearView.setOnClickListener(this);

			vh.gapLinearView.setVisibility( View.VISIBLE );
			vh.gapTextLayout.setVisibility(View.VISIBLE);
			vh.gapProgress.setVisibility(View.GONE);

			vh.gapTextView.setText( data.get( "iGapSize" ).get( position ) );

			return convertView;
		}
		return convertView;
	}


	public void setTypefaces ( View converView ) {

		ViewHolder	vh = ( ViewHolder ) converView.getTag();

		vh.userName.setTypeface( Constants.OPEN_SANS_SEMIBOLD);
		vh.userMsg.setTypeface( Constants.OPEN_SANS_REGULAR);
		vh.userTimestamp.setTypeface(Constants.OPEN_SANS_REGULAR);
		vh.teamNameId.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		vh.leagueId.setTypeface(Constants.OPEN_SANS_REGULAR);
		vh.acceptedIgnore.setTypeface( Constants.OPEN_SANS_REGULAR);

		vh.gapTextView.setTypeface(Constants.OPEN_SANS_BOLD);
		vh.gapMoreTextView.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
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

				highLightBlueColor(v, false);

			}
			handleClick(v);
			onClick(v);
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

	private void handleClick ( View v ) {

		try {
			if ( v == null ) {
				return;
			}
			final int position = Integer.parseInt( v.getTag( R.id.about_txtview ).toString() );

			String type = data.get( "vSubjectType" ).get( position );

			//		if ( type.equalsIgnoreCase( TYPE_GAP ) ) {
			//			
			//			String vGapId = data.get( "vGapId" ).get( position );
			//			String vGapUrl = data.get( "vGapUrl" ).get( position );
			//
			//			v.findViewById(R.id.gapTextLayout).setVisibility(View.GONE);
			//			v.findViewById(R.id.gapProgress).setVisibility(View.VISIBLE);
			//			
			//			
			//			FragmentHolder fh = PlayupLiveApplication.getFragmentManagerUtil().checkForFragment( "NotificationFragment" );
			//			
			//			 
			//			
			//			 if ( fh != null && fh.fragment != null ) {
			//			if (fh.fragment.runnableList!=null && !fh.fragment.runnableList.containsKey(vGapUrl)){
			//				
			//				fh.fragment.runnableList.put(vGapUrl,	new Util().getGapNotification ( vGapId, vGapUrl  ,fh.fragment.runnableList));
			//				
			//			}
			//			 }
			//			
			//			
			//
			//			vGapId = null;
			//			vGapUrl = null;
			//		} else 
			if ( type.equalsIgnoreCase( TYPE_CONVERSATION_NOTIFICATION ) ) {

				if (  Util.isInternetAvailable() ) {
					final Bundle bundle = new Bundle ();
					bundle.putString( "vConversationId", data.get( "vConversationId" ).get ( position ) );
					bundle.putString("vMainColor",vMainColor );
					bundle.putString("vMainTitleColor",vMainTitleColor );
					
					bundle.putString( "vSecColor",vSecColor );			
					bundle.putString( "vSecTitleColor",vSecTitleColor );
					new Thread( new Runnable() {

						@Override
						public void run() {
							try {
								DatabaseUtil dbUtil = DatabaseUtil.getInstance();
								String conversationType= null;
								if(dbUtil.getHeader(data.get( "vConversationHrefUrl" ).get ( position ))!=null && dbUtil.getHeader(data.get( "vConversationHrefUrl" ).get ( position )).trim().length()>0) {
									 conversationType = dbUtil.getHeader(data.get( "vConversationHrefUrl" ).get ( position ));
								}else{
									 conversationType = dbUtil.getHeader(data.get( "vConversationUrl" ).get ( position ));
								}
							
								
								
								if(conversationType != null){
									if(conversationType.equalsIgnoreCase(Constants.ACCEPT_TYPE_COVERSATION)){

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
														//Logs.show(e);
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
													//	Logs.show(e);
													}
												}
											});
										}
									}
								}
								if (  Integer.parseInt( data.get ( "isRead" ).get( position ) ) == 0 ) {
									JSONObject jObj = new JSONObject();


									/*String vNotificationUrl = data.get( "vNotificationUrl" ).get( position );*/
									String vNotificationUrl= null;
									boolean isHrefUrl 	= false;
									if(data.get( "vNotificationHrefUrl" ).get( position )!=null && data.get( "vNotificationHrefUrl" ).get( position ).trim().length()> 0 ){
										vNotificationUrl = data.get( "vNotificationHrefUrl" ).get( position );
										isHrefUrl	= true;
									}
									else{
										vNotificationUrl = data.get( "vNotificationUrl" ).get( position );
									}
									try {
										jObj.put( ":type", dbUtil.getHeader( vNotificationUrl ) );
										jObj.put( ":uid", data.get( "vNotificationId" ).get( position ) );
										jObj.put( "read", true );
									} catch ( Exception e ) {
										//Logs.show(e);
									}
									dbUtil = null;
									/*new Util().setNotificationConfirm ( vNotificationUrl, jObj.toString(), null );*/
									new Util().setNotificationConfirm ( vNotificationUrl,isHrefUrl, jObj.toString(), null );

								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								//Logs.show ( e );
							}


						}
					}).start();

				} else {

					PlayupLiveApplication.showToast( R.string.no_network );
				}
			} else {


			}
		} catch (Exception e) {
			//Logs.show(e);
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

				highLightBlueColor(v, true);
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

	public void highLightBlueColor(final View view, final boolean shouldDo) {
		// highlight with blue color

		if ( view == null || data == null ) {
			return;
		}
		int position = Integer.parseInt(  view.getTag( R.id.about_txtview ).toString() );
		String type = data.get( "vSubjectType" ).get( position );

		if(view.getId() == R.id.buttonBaseCheck) {

			if(shouldDo) {
				view.setBackgroundResource(R.drawable.button_base_d);
				((ImageView) view.findViewById(R.id.buttonCross)).setImageResource(R.drawable.button_tick_d);
			} else {
				view.setBackgroundResource(R.drawable.button_base);
				((ImageView) view.findViewById(R.id.buttonCross)).setImageResource(R.drawable.button_tick);
			}

			return;
		} else if( view.getId() == R.id.buttonBaseUncheck ) {
			if(shouldDo) {
				view.setBackgroundResource(R.drawable.button_base_d);
				((ImageView) view.findViewById(R.id.buttonCross)).setImageResource(R.drawable.button_cross_d);
			} else {
				view.setBackgroundResource(R.drawable.button_base);
				((ImageView) view.findViewById(R.id.buttonCross)).setImageResource(R.drawable.button_cross);
			}
			return;
		}




		if ( type.equalsIgnoreCase( TYPE_CONVERSATION_NOTIFICATION ) ) {

			ViewHolder vh = (ViewHolder) view.getTag();
			if (shouldDo) {

				vh.postBase.setBackgroundResource( R.drawable.notification_base_d );

				vh.leagueId.setTextColor( Color.parseColor( "#FFFFFF" ) );
				vh.teamNameId.setTextColor( Color.parseColor( "#FFFFFF" ) );
				vh.userName.setTextColor( Color.parseColor( "#FFFFFF" ) );
				vh.userMsg.setTextColor( Color.parseColor( "#FFFFFF" ) );
				vh.userTimestamp.setTextColor( Color.parseColor( "#FFFFFF" ) );

				vh.userLayout_arrow.setImageResource( R.drawable.chevron_d );
				vh.redLine.setBackgroundColor( Color.parseColor( "#FFFFFF" ) );

			} else { // remove the highlight
				vh.postBase.setBackgroundResource( R.drawable.post_base );

				vh.leagueId.setTextColor( Color.parseColor( "#9D999D" ) );
				vh.teamNameId.setTextColor( Color.parseColor( "#5F5A5A" ) );
				vh.userName.setTextColor( Color.parseColor( "#FF4754" ) );
				vh.userMsg.setTextColor( Color.parseColor( "#ABABAB" ) );
				vh.userTimestamp.setTextColor( Color.parseColor( "#B9B6B9" ) );

				vh.userLayout_arrow.setImageResource( R.drawable.chevron );
				vh.redLine.setBackgroundResource( R.drawable.notification_red_line );

			}

		} else {
			// remove the high light to be on safer side.
		}


	}

	private class UpdateCounterTask implements Runnable {
		public void run() {

			try {
				mHandler.sendEmptyMessage(0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//Logs.show ( e );
			}
		}
	}




	@Override
	public void onClick(View v) {


		try {
			if ( v == null ) {
				return;
			}
			if ( v.getId() == R.id.gapLinearView) {
				v.setOnClickListener(null);
				int position = Integer.parseInt( v.getTag ( R.id.about_txtview ).toString() );
				String vGapId = data.get( "vGapId" ).get( position );
				String vGapUrl = null;
				boolean isHref = false;
				if(data.get( "vGapHrefUrl" ).get( position )!=null && data.get( "vGapHrefUrl" ).get( position ).trim().length()> 0 ){
					 vGapUrl = data.get( "vGapHrefUrl" ).get( position );
					 isHref = true;
				}else{
					 vGapUrl = data.get( "vGapUrl" ).get( position );
				}
				//String vGapUrl = data.get( "vGapUrl" ).get( position );

				v.findViewById(R.id.gapTextLayout).setVisibility(View.GONE);
				v.findViewById(R.id.gapProgress).setVisibility(View.VISIBLE);


				if ( PlayupLiveApplication.getFragmentManagerUtil().checkIfFragmentExists( "NotificationFragment" ) ) {
					if ( PlayupLiveApplication.getFragmentManagerUtil().fragmentMap != null && PlayupLiveApplication.getFragmentManagerUtil().fragmentMap.containsKey( "NotificationFragment" ) )  {

						NotificationFragment fragment = ( NotificationFragment ) PlayupLiveApplication.getFragmentManagerUtil().fragmentMap.get( "NotificationFragment" ); 
						if ( fragment != null ) {
							if ( fragment.runnableList!=null && !fragment.runnableList.containsKey(vGapUrl)  && Util.isInternetAvailable() ){

								fragment.runnableList.put(vGapUrl,	new Util().getGapNotification ( vGapId, vGapUrl ,isHref ,fragment.runnableList));

							}
						}
					}
				}







				vGapId = null;
				vGapUrl = null;

			}
			if ( v.getId() == R.id.imageViewpostAvatar  ) {

				int position = Integer.parseInt( v.getTag ( R.id.about_txtview ).toString() );
				Boolean isHrefUrl = false;
				String vSelfUrl = null;//= data.get( "vUserSelfUrl" ).get( position );
			
				if(data.get( "vUserHrefUrl" ).get( position )!=null && data.get( "vUserHrefUrl" ).get( position ).trim().length()> 0 ){
					//Log.e("123","vUserHrefUrl >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+data.get( "vUserHrefUrl" ).get( position ));
					vSelfUrl = data.get( "vUserHrefUrl" ).get( position );
					isHrefUrl	 = true;
				}else{
					//Log.e("123","vUserSelfUrl >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+data.get( "vUserSelfUrl" ).get( position ));
					vSelfUrl = data.get( "vUserSelfUrl" ).get( position );
				}

				Bundle bundle = new Bundle ();
				bundle.putString( "vSelfUrl", vSelfUrl );
				bundle.putBoolean( "isHrefUrl", isHrefUrl);
				bundle.putString("vMainColor",vMainColor );
				bundle.putString("vMainTitleColor",vMainTitleColor );
				
				bundle.putString( "vSecColor",vSecColor );			
				bundle.putString( "vSecTitleColor",vSecTitleColor );
				PlayupLiveApplication.getFragmentManagerUtil().setFragment( "PublicProfileFragment", bundle );

			} else if( v.getId() == R.id.buttonBaseCheck || v.getId() == R.id.buttonBaseUncheck ) {
				int position = Integer.parseInt( v.getTag( R.id.about_txtview ).toString() );
				String state = v.getTag( R.id.aboutScrollView  ).toString();
				Boolean isHrefUrl = false;
				String vNotificationUrl = null;//= data.get( "vNotificationUrl" ).get( position );
				if(data.get( "vNotificationHrefUrl" ).get( position )!=null && data.get( "vNotificationHrefUrl" ).get( position ).trim().length()> 0 ){
					vNotificationUrl = data.get( "vNotificationHrefUrl" ).get( position );
					isHrefUrl	= true;
				}
				else{
					vNotificationUrl = data.get( "vNotificationUrl" ).get( position );
				}


				ViewHolder vh = (ViewHolder) v.getTag();
				if ( state.equalsIgnoreCase( "confirmed" ) ) {

					vh.acceptedIgnore.setVisibility( View.VISIBLE );
					vh.buttonBaseCheck.setVisibility( View.GONE );
					vh.buttonBaseUnCheck.setVisibility( View.GONE );

					vh.acceptedIgnore.setText ( R.string.accepted);

				} else if ( state.equalsIgnoreCase( "ignored" ) ) {

					vh.acceptedIgnore.setVisibility( View.VISIBLE );
					vh.buttonBaseCheck.setVisibility( View.GONE );
					vh.buttonBaseUnCheck.setVisibility( View.GONE );

					vh.acceptedIgnore.setText (  R.string.ignored );

				}
				vh.userLayout_blueDot.setVisibility( View.GONE );
				JSONObject jObj = new JSONObject();

				try {
					jObj.put( ":type", "application/vnd.playup.notification.confirmable+json" );
					jObj.put( "read", true );
					jObj.put( "status", state );
				} catch ( Exception e ) {

				}
				// Praveen : changed
				new Util().setNotificationConfirm ( vNotificationUrl,isHrefUrl, jObj.toString(), state );

			}
		} catch (Exception e) {
			//Logs.show(e);
		}


	}

}
