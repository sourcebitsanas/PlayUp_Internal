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
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

public class DirectConversationAdapter extends BaseAdapter implements OnTouchListener {

	private LayoutInflater inflater;
	private DateUtil dateUtil;
	private Hashtable<String, List<String>> data = null;
	private ImageDownloader imageDownloader = new ImageDownloader();
	LayoutParams params;
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;


	public DirectConversationAdapter ( Hashtable<String, List<String>> data, String vMainColor, String vMainTitleColor , 
			String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		
		this.vMainColor = vMainColor;
		this.vMainTitleColor  = vMainTitleColor;

		if ( imageDownloader == null ) {
			imageDownloader = new ImageDownloader();
		}
		dateUtil = new DateUtil();
		this.data = data;
		inflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}



	public void setData ( Hashtable<String, List<String>> data, String vMainColor, String vMainTitleColor , String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		this.vMainColor = vMainColor;
		this.vMainTitleColor  = vMainTitleColor;
		
		if ( this.data != null ) {
			this.data.clear();
			this.data = null;
		}

		if ( dateUtil == null ) {
			dateUtil = new DateUtil();
		}
		if ( inflater == null ) {
			inflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		if ( imageDownloader == null ) {
			imageDownloader = new ImageDownloader();
		}
		this.data = data;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {

		if  ( data != null && data.get( "vDirectMessageId" ) != null ) {

			return data.get( "vDirectMessageId" ).size();
		}
		return 0;
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

		public ImageView imageViewpostAvatar;
		public TextView userTimestamp;
		public TextView userName;
		public TextView userMsg;
		public TextView unreadMessageCount;
		//	public ImageView unReadMessageCount_Bg;

		public View user_avatar;
		public View postBase;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder vh = null;
		if ( inflater == null ) {
			LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return  layoutInflater.inflate( R.layout.direct_conversation_item, null);
		}

		if (convertView == null ) {

			vh = new ViewHolder();

			convertView = inflater.inflate(R.layout.direct_conversation_item, null);

			vh.imageViewpostAvatar = (ImageView) convertView.findViewById(R.id.imageViewpostAvatar);
			vh.userTimestamp = (TextView ) convertView.findViewById(R.id.userTimestamp);
			vh.userName = (TextView ) convertView.findViewById(R.id.userName);
			vh.userMsg = (TextView ) convertView.findViewById(R.id.userMsg);
			vh.unreadMessageCount = ( TextView ) convertView.findViewById(R.id.unreadMessageCount);
			//	vh.unReadMessageCount_Bg = ( ImageView ) convertView.findViewById(R.id.unReadMessageCount_Bg);
			
			vh.userName.setTypeface(com.playup.android.util.Constants.OPEN_SANS_SEMIBOLD );
			
			vh.user_avatar = convertView.findViewById( R.id.user_avatar );
			vh.postBase    = convertView.findViewById( R.id.postBase );
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		highLightBlueColor ( vh.postBase, false  );
		vh.postBase.setBackgroundResource( R.drawable.post_base );
		
		int iUnreadCount = Integer.parseInt ( data.get( "iUnreadCount" ).get( position ) );
		if ( iUnreadCount == 0 ) {
			vh.unreadMessageCount.setText( "0" );
			
			vh.unreadMessageCount.setVisibility( View.INVISIBLE );

		} else {

			vh.unreadMessageCount.setText( iUnreadCount + "" );
		
			vh.unreadMessageCount.setVisibility( View.VISIBLE );

		}

		vh.userName.setText( new Util().getSmiledText ( data.get( "vDisplayName" ).get( position ) ) );
		vh.userMsg.setText( new Util().getSmiledText ( data.get( "vMessage" ).get( position ).replaceAll("(\\r|\\n|\\t)", " ") ) );
		vh.userTimestamp.setText( dateUtil.gmt_to_local_timezone( data.get( "dCreatedDate" ).get( position ) ) );
		
		
		
		if( data.get( "vUserHrefUrl" ).get( position ) !=null &&  
				data.get( "vUserHrefUrl" ).get( position ).trim().length()>0 ){
			vh.user_avatar.setTag( data.get( "vUserHrefUrl" ).get( position ) );
			vh.user_avatar.setTag(R.id.avtarGreenBase, true);
			
			
			vh.postBase.setTag(R.id.postBase, data.get( "vUserHrefUrl" ).get( position ) );
			vh.postBase.setTag(R.id.avtarGreenBase,true );
		}else{
			vh.user_avatar.setTag( data.get( "vUserSelfUrl" ).get( position ) );
			vh.user_avatar.setTag(R.id.avtarGreenBase, false);
			
			vh.postBase.setTag(R.id.postBase, data.get( "vUserSelfUrl" ).get( position ) );
			vh.postBase.setTag(R.id.avtarGreenBase,false );
		}
		vh.user_avatar.setOnClickListener( avatarClickListener );
		
		vh.imageViewpostAvatar.setImageResource(R.drawable.head);
		imageDownloader.download( data.get( "vAvatarUrl" ).get( position ) , vh.imageViewpostAvatar, true, this );

		vh.postBase.setTag( data.get( "vDirectConversationId" ).get( position ) );
		
		
		

		vh.postBase.setOnTouchListener( this );
		if ( position == 0 ) {
			convertView.setPadding( 0, 10, 10, 0);
		} else if ( position == getCount() - 1 ) {
			convertView.setPadding( 0, 0, 10, 10);
		} else {
			convertView.setPadding( 0, 0, 10, 0);
		}
		return convertView;
	}




	private OnClickListener avatarClickListener = new OnClickListener( ) {

		@Override
		public void onClick(View v) {

			try {
				// checking if the profile is of primary user or not.
				String vSelfUrl = (String)v.getTag( );
				Bundle bundle = new Bundle();
				bundle.putString( "vSelfUrl", vSelfUrl );
				bundle.putBoolean( "isHrefUrl",((Boolean) v.getTag(R.id.avtarGreenBase)).booleanValue());
				bundle.putString("vMainColor",vMainColor );
				bundle.putString("vMainTitleColor",vMainTitleColor );
				bundle.putString( "vSecColor",vSecColor );			
				bundle.putString( "vSecTitleColor",vSecTitleColor );
				PlayupLiveApplication.getFragmentManagerUtil().setFragment( "PublicProfileFragment", bundle );
			} catch (Exception e) {
				Logs.show(e);
			}
		}
	};


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
			handleClick ( v );
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

				try {
					highLightBlueColor ( v, true );
					super.handleMessage(msg);
				} catch (Exception e) {
					Logs.show(e);
				}
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
			( ( TextView ) v.findViewById( R.id.unreadMessageCount ) ).setTextColor( Color.parseColor( "#B0E6FF") );
		

			params = v.getLayoutParams();
			if( v.getHeight() > 0)
				params.height=v.getHeight();
			v.setLayoutParams(params);	
			v.setBackgroundResource( R.drawable.notification_base_d );

			v.findViewById(R.id.unreadMessageCount).setBackgroundResource(R.drawable.post_count_d);
			( ( ImageView ) v.findViewById(R.id.rightChevron) ).setImageResource ( R.drawable.chevron_d );

		} else {

			( ( TextView ) v.findViewById( R.id.userTimestamp ) ).setTextColor( Color.parseColor( "#ABABAB") );
			( ( TextView ) v.findViewById( R.id.userName ) ).setTextColor( Color.parseColor( "#FF4754") );
			( ( TextView ) v.findViewById( R.id.userMsg ) ).setTextColor( Color.parseColor( "#ABABAB") );
			( ( TextView ) v.findViewById( R.id.unreadMessageCount ) ).setTextColor( Color.parseColor( "#FFFFFF") );
			

			v.setBackgroundResource( R.drawable.post_base );

			v.findViewById(R.id.unreadMessageCount).setBackgroundResource(R.drawable.post_count);
			( ( ImageView ) v.findViewById(R.id.rightChevron) ).setImageResource ( R.drawable.chevron );


		}
	}


	/**
	 * handling the click events 
	 */
	private void handleClick ( View view ) {

		if ( view == null ) {
			return;
		}

		
		String vUserSelfUrl_temp = null;
		Boolean isHrefUrl	= false;
		if(view.getTag(R.id.postBase)!=null) {
			vUserSelfUrl_temp = (String) view.getTag(R.id.postBase);
			isHrefUrl= (Boolean) view.getTag(R.id.avtarGreenBase) ;
		}
		
		final String vUserSelfUrl = vUserSelfUrl_temp;
		final Boolean isUserSelfUrlHref	= 	isHrefUrl;
		if ( view.getTag() != null ) {
			final String vDirectConversationId = (String) view.getTag();
			
			new Thread( new Runnable() {
				
				@Override
				public void run() {
					final String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
					try {
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						final Hashtable<String, Object> result = dbUtil.getDirectconversationUrlFromId ( vDirectConversationId );
						
						if ( result != null && result.containsKey("url") && result.get("url").toString() != null && 
								result.get("url").toString().trim().length() > 0) {
							
							if ( PlayUpActivity.handler != null ) {
								PlayUpActivity.handler.post( new Runnable() {
									
									@Override
									public void run() {
										
										try {
											if ( !Constants.isCurrent ) {
												return;
											}
											
											Bundle bundle = new Bundle ();
											bundle.putString( "vDirectConversationUrl", result.get("url").toString() );
											bundle.putBoolean( "isvDirectConversationUrlHref", ((Boolean)result.get("isHref")).booleanValue() );
											bundle.putString("vUserSelfUrl", vUserSelfUrl );
											bundle.putBoolean("isUserSelfUrlHref",isUserSelfUrlHref);
											bundle.putString( "vMainColor",vMainColor );							
											bundle.putString( "vMainTitleColor",vMainTitleColor );
											bundle.putString("fromFragment", topFragmentName);
											bundle.putString( "vSecColor",vSecColor );			
											bundle.putString( "vSecTitleColor",vSecTitleColor );
											PlayupLiveApplication.getFragmentManagerUtil().setFragment( "DirectMessageFragment", bundle );
										} catch (Exception e) {
											Logs.show(e);
										}	
									}
								});
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show ( e );
					}
					
				}
			}).start();
			
		}

	}

}
