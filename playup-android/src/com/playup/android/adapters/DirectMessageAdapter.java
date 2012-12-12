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
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.fragment.PostDirectMessageFragment;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

public class DirectMessageAdapter extends BaseAdapter implements OnClickListener,OnTouchListener {

	private LayoutInflater inflater;
	private Hashtable<String,List<String> > data =null;
	private ImageDownloader imageDownloader;
	private String primaryUserId = null;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;

	private String vUserSelfUrl = null;
	private DateUtil dateUtil;
	private LayoutParams params;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;
	private boolean isUserSelfUrlHref = false;
	

	public DirectMessageAdapter(Hashtable<String, List<String>> data, String vUserSelfUrl ,
			String vMainColor,String vMainTitleColor, String vSecColor, String vSecTitleColor,boolean isUserSelfUrlHref ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		
		this.vMainColor  = vMainColor;
		this.vMainTitleColor = vMainTitleColor;
		this.isUserSelfUrlHref  = isUserSelfUrlHref;



		this.data = data;
		dateUtil = new DateUtil();
		if ( inflater == null ) {
			inflater = (LayoutInflater) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		if ( imageDownloader == null ) {
			imageDownloader = new ImageDownloader();
		}

		this.vUserSelfUrl = vUserSelfUrl;
		primaryUserId = DatabaseUtil.getInstance().getPrimaryUserId();

	}


	public void setData(Hashtable<String, List<String>> data, String vUserSelfUrl 
			,String vMainColor,String vMainTitleColor, String vSecColor, String vSecTitleColor,boolean isUserSelfUrlHref  ) {
		
		this.isUserSelfUrlHref  = isUserSelfUrlHref;

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		
		this.vMainColor  = vMainColor;
		this.vMainTitleColor = vMainTitleColor;


		
		this.data = data;
		this.vUserSelfUrl = vUserSelfUrl;
		dateUtil = new DateUtil();
		if ( inflater == null ) {
			inflater = (LayoutInflater) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		if ( imageDownloader == null ) {
			imageDownloader = new ImageDownloader();
		}

		primaryUserId = DatabaseUtil.getInstance().getPrimaryUserId();

		notifyDataSetChanged();

	}
	
	public void setData(Hashtable<String, List<String>> data, String vUserSelfUrl,boolean isUserSelfUrlHref  ) {
		
	
		this.isUserSelfUrlHref  = isUserSelfUrlHref;

		
		this.data = data;
		this.vUserSelfUrl = vUserSelfUrl;
		dateUtil = new DateUtil();
		if ( inflater == null ) {
			inflater = (LayoutInflater) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		if ( imageDownloader == null ) {
			imageDownloader = new ImageDownloader();
		}

		primaryUserId = DatabaseUtil.getInstance().getPrimaryUserId();

		notifyDataSetChanged();

	}

	@Override
	public int getCount() {
		if  ( data != null && data.get( "vDMessageId" ) != null ) {
			return data.get( "vDMessageId" ).size();
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

		public RelativeLayout primaryUserLayout;
		public TextView pri_userMessage;
		public TextView pri_userMessageTimestamp;
		public ImageView pri_imgViewpostAvatar;

		public RelativeLayout friendLayout;
		public TextView fri_userMessage;
		public TextView fri_userMessageTimestamp;
		public ImageView fri_imgViewpostAvatar;
		
		public View gapLinearView;
		public ProgressBar gapProgress;
		public View gapTextLayout;
		public TextView gapText;
		
		public View avatar_layout_left;
		public View avatar_layout_right;
		public RelativeLayout messageView1;
		public RelativeLayout messageView2;
		
		public LinearLayout base;
		public LinearLayout base1;

	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {


		ViewHolder vh;
		
		if ( inflater == null ) {
			LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return  layoutInflater.inflate( R.layout.friends_message_view, null);
		}
		
		if( convertView == null){

			convertView = inflater.inflate(R.layout.friends_message_view, null);

			vh = new ViewHolder();
			vh.primaryUserLayout = (RelativeLayout)convertView.findViewById(R.id.primaryUserLayout);
			vh.friendLayout = (RelativeLayout)convertView.findViewById(R.id.friendLayout);

			
			vh.pri_userMessage = ( TextView ) vh.primaryUserLayout.findViewById( R.id.userMsg );
			vh.pri_userMessageTimestamp = ( TextView ) vh.primaryUserLayout.findViewById( R.id.userTimestamp );

			vh.fri_userMessage = ( TextView ) vh.friendLayout.findViewById( R.id.userMsg1 );
			vh.fri_userMessageTimestamp = ( TextView ) vh.friendLayout.findViewById( R.id.userTimestamp1 );
			
			vh.avatar_layout_right = vh.primaryUserLayout.findViewById( R.id.avatar_layout_right );
			vh.avatar_layout_left = vh.friendLayout.findViewById( R.id.avatar_layout_left );
			
			vh.pri_imgViewpostAvatar = (ImageView) vh.avatar_layout_right.findViewById( R.id.imgViewpostAvatar );
			vh.fri_imgViewpostAvatar = (ImageView) vh.avatar_layout_left.findViewById( R.id.imgViewpostAvatar );
			
			vh.base = (LinearLayout) convertView.findViewById(R.id.base);
			vh.base1 = (LinearLayout) convertView.findViewById(R.id.base1);
			
			vh.gapLinearView = convertView.findViewById( R.id.gapLinearView );
			vh.gapProgress = (ProgressBar) vh.gapLinearView.findViewById( R.id.gapProgress );
			vh.gapTextLayout = vh.gapLinearView.findViewById( R.id.gapTextLayout );
			vh.gapText = (TextView) vh.gapLinearView.findViewById( R.id.gapTextView );
			vh.messageView1 = (RelativeLayout)vh.primaryUserLayout.findViewById(R.id.messageView1);
			vh.messageView2 = (RelativeLayout)vh.friendLayout.findViewById(R.id.messageView2);
			convertView.setTag(vh);
			setTypefaces(convertView);
		} else {

			vh = (ViewHolder) convertView.getTag();
		}
		vh.fri_imgViewpostAvatar.setImageResource(R.drawable.head);
		vh.pri_imgViewpostAvatar.setImageResource(R.drawable.head);
		vh.base.setOnTouchListener( null );
		vh.base1.setOnTouchListener( null );
		if ( position == 0 ) {
			convertView.setPadding( 0, 8, 0, 0);
		} else if ( position == getCount() - 1 ) {
			convertView.setPadding( 0, 0, 0, 8 );
		} else {
			convertView.setPadding( 0, 0, 0, 0 );
		}
		
		
		if ( data !=null && data.get( "vGapId" ).get( position ) != null ) {
			
			vh.primaryUserLayout.setVisibility(View.GONE);
			vh.friendLayout.setVisibility(View.GONE);
			vh.gapLinearView.setVisibility( View.VISIBLE );
			
			if ( data.get( "isGapLoading" ).get( position ).equalsIgnoreCase( "0" ) ) {
				vh.gapTextLayout.setVisibility( View.VISIBLE );
				vh.gapProgress.setVisibility( View.GONE );
				vh.gapText.setText( data.get( "iGapSize" ).get( position ) + "" );
				if(data.get( "vGapHrefUrl" ).get( position ) != null &&
						data.get( "vGapHrefUrl" ).get( position ).trim().length() > 0){
					
					vh.gapLinearView.setTag( data.get( "vGapHrefUrl" ).get( position ) );
					vh.gapLinearView.setTag( R.id.avtarGreenBase,true );
				}else{
					vh.gapLinearView.setTag( data.get( "vGapUrl" ).get( position ) );
					vh.gapLinearView.setTag( R.id.avtarGreenBase,false );
				}
				
				vh.gapLinearView.setOnClickListener( gapClickListener );
			} else {
				
				vh.gapTextLayout.setVisibility( View.GONE );
				vh.gapProgress.setVisibility( View.VISIBLE );
				vh.gapLinearView.setOnClickListener( null );
				
			}
			
			return convertView ;
		} 
		
		vh.gapLinearView.setVisibility( View.GONE );
		
		if ( data !=null && data.get( "vUserId" ).get( position ) != null && data.get( "vUserId" ).get( position ).equalsIgnoreCase ( primaryUserId ) ) {
			
			vh.primaryUserLayout.setVisibility(View.VISIBLE);
			vh.friendLayout.setVisibility(View.GONE);

			vh.pri_userMessage.setText( new Util().getSmiledText ( data.get( "vMessage" ).get( position ) .replaceAll("(\\r|\\n|\\t)", " ")) );

			if(data.get( "vUserHrefUrl" ).get( position ) != null && 
					data.get( "vUserHrefUrl" ).get( position ).trim().length() > 0){
				vh.avatar_layout_right.setTag( data.get( "vUserHrefUrl" ).get( position ) );
				vh.avatar_layout_right.setTag(R.id.avtarGreenBase,true);
			}else{
				
				vh.avatar_layout_right.setTag( data.get( "vUserSelfUrl" ).get( position ) );
				vh.avatar_layout_right.setTag(R.id.avtarGreenBase,false);
				
			}
			
			vh.avatar_layout_right.setOnClickListener( avatarClickListener );
			imageDownloader.download( data.get( "vAvatarUrl" ).get( position ) , vh.pri_imgViewpostAvatar, true, this );

			vh.pri_userMessageTimestamp.setText ( dateUtil.gmt_to_local_timezone( data.get ( "dCreatedDate" ).get ( position  ) ) );
			
			vh.base.setOnTouchListener(this);
			
		} else if (data !=null){
			
			vh.primaryUserLayout.setVisibility(View.GONE);
			vh.friendLayout.setVisibility(View.VISIBLE);

			vh.fri_userMessage.setText( new Util().getSmiledText ( data.get( "vMessage" ).get( position ).replaceAll("(\\r|\\n|\\t)", " ") ) );

			
			if(data.get( "vUserHrefUrl" ).get( position ) != null && 
					data.get( "vUserHrefUrl" ).get( position ).trim().length() > 0){
				vh.avatar_layout_left.setTag( data.get( "vUserHrefUrl" ).get( position ) );
				vh.avatar_layout_left.setTag(R.id.avtarGreenBase,true);
			}else{
				
				vh.avatar_layout_left.setTag( data.get( "vUserSelfUrl" ).get( position ) );
				vh.avatar_layout_left.setTag(R.id.avtarGreenBase,false);
				
			}
			
			
			vh.avatar_layout_left.setOnClickListener( avatarClickListener );
			imageDownloader.download( data.get( "vAvatarUrl" ).get( position ) , vh.fri_imgViewpostAvatar, true, this );

			vh.fri_userMessageTimestamp.setText ( dateUtil.gmt_to_local_timezone( data.get ( "dCreatedDate" ).get ( position  ) ) );
			
			vh.base1.setOnTouchListener(this);
		}




		return convertView;

	}


	public void setTypefaces ( View convertView){

		ViewHolder vh = (ViewHolder) convertView.getTag();

		vh.pri_userMessage.setTypeface(com.playup.android.util.Constants.OPEN_SANS_REGULAR );
		
		vh.pri_userMessageTimestamp.setTypeface(com.playup.android.util.Constants.OPEN_SANS_REGULAR );
		vh.fri_userMessage.setTypeface(com.playup.android.util.Constants.OPEN_SANS_REGULAR );
		vh.fri_userMessageTimestamp.setTypeface(com.playup.android.util.Constants.OPEN_SANS_REGULAR );
		
		
	}


	private OnClickListener avatarClickListener = new OnClickListener () {

		@Override
		public void onClick(View v) {

			try {
				if ( v != null && v.getTag() != null ) {
					// checking if the profile is of primary user or not.
					String vSelfUrl = (String)v.getTag( );
					int myId = -1;
					if ( DatabaseUtil.getInstance().getUserId() != null ) {
						myId = Integer.parseInt( DatabaseUtil.getInstance().getUserId() );
					}

					
					String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
					
					boolean isHref = ((Boolean)v.getTag(R.id.avtarGreenBase)).booleanValue();
					
					
					Bundle bundle = new Bundle();
					bundle.putString( "vSelfUrl", vSelfUrl );
					bundle.putBoolean( "isHref", isHref );
					bundle.putInt("myId", myId );
					bundle.putString("vMainColor",vMainColor );
					bundle.putString("vMainTitleColor",vMainTitleColor );
					bundle.putString( "vSecColor",vSecColor );			
					bundle.putString( "vSecTitleColor",vSecTitleColor );
					bundle.putString( "fromfragment",topFragmentName );
					

					PlayupLiveApplication.getFragmentManagerUtil().setFragment("PublicProfileFragment", bundle );

				}
			} catch (Exception e) {
				Logs.show(e);
			}
		}

	};
	
	
	private OnClickListener gapClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			
			try {
				if ( v != null && v.getTag() != null ) {
					String vGapUrl = v.getTag().toString();
					boolean isHref = ((Boolean)v.getTag( R.id.avtarGreenBase)).booleanValue();
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					dbUtil.setDirectMessageGapLoading ( vGapUrl, 1 ); 

					new Util().callDirectGapUrl ( vGapUrl, vUserSelfUrl,isHref ); 
					
					setData( DatabaseUtil.getInstance().getDirectConversationMessages ( vUserSelfUrl ), vUserSelfUrl,isUserSelfUrlHref );
				}
			} catch (Exception e) {
				Logs.show(e);
			}
		}
	};



	@Override
	public void onClick(View v) {
		try {
			if( v.getId() == R.id.base || v.getId()==R.id.base1 ) {
				String vFriendName =null;
				Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT vFriendUserName" +
						" FROM playup_friends WHERE  (vUserSelfUrl = \"" + vUserSelfUrl + "\" ) OR " +
								"(vUserHrefUrl = '"+vUserSelfUrl+"')");
				if ( c != null ) {

					if ( c.getCount() > 0)  {
						c.moveToFirst();
						vFriendName = c.getString(c.getColumnIndex("vFriendUserName"));
					}
					c.close();
					c = null;
				}
				String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
				Bundle bundle =new Bundle();
				bundle.putString("vUserSelfUrl", vUserSelfUrl );
				Hashtable<String, Object> result = DatabaseUtil.getInstance().getDirectMessageUrl( vUserSelfUrl );
				
				String url = (String) result.get("url");
				Boolean isHrefUrl  = (Boolean) result.get("isHref");
				
				bundle.putBoolean("isUserSelfUrlHref",isUserSelfUrlHref );
				bundle.putBoolean("isvDirectMessageHrefUrl",isHrefUrl );
				bundle.putString ("vDirectMessageUrl",url );
				bundle.putString( "vFriendName", vFriendName );
				bundle.putString("vMainColor",vMainColor );
				bundle.putString("vMainTitleColor",vMainTitleColor );
				bundle.putString( "vSecColor",vSecColor );			
				bundle.putString( "vSecTitleColor",vSecTitleColor );
				
				bundle.putString( "fromfragment",topFragmentName );
				PostDirectMessageFragment.isHomeTapped = false;
				PlayupLiveApplication.getFragmentManagerUtil().setFragment( "PostDirectMessageFragment", bundle );
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
			if( v.getId() == R.id.base ) {
				( ( TextView ) v.findViewById( R.id.userTimestamp ) ).setTextColor( Color.parseColor( "#FFFFFF") );
				( ( TextView ) v.findViewById( R.id.userMsg ) ).setTextColor( Color.parseColor( "#FFFFFF") );
			} else if ( v.getId() == R.id.base1) {
				( ( TextView ) v.findViewById( R.id.userTimestamp1 ) ).setTextColor( Color.parseColor( "#FFFFFF") );
				( ( TextView ) v.findViewById( R.id.userMsg1 ) ).setTextColor( Color.parseColor( "#FFFFFF") );
			}
			params = v.getLayoutParams();
			if( v.getHeight() > 0)
				params.height=v.getHeight();
			v.setLayoutParams(params);	
			v.setBackgroundResource( R.drawable.notification_base_d );
		} else {
			if( v.getId() == R.id.base ) {
				( ( TextView ) v.findViewById( R.id.userTimestamp ) ).setTextColor( Color.parseColor( "#B9B6B9") );
			    ( ( TextView ) v.findViewById( R.id.userMsg ) ).setTextColor( Color.parseColor( "#ABABAB") );
			} else if ( v.getId() == R.id.base1) {
				( ( TextView ) v.findViewById( R.id.userTimestamp1 ) ).setTextColor( Color.parseColor( "#B9B6B9") );
			    ( ( TextView ) v.findViewById( R.id.userMsg1 ) ).setTextColor( Color.parseColor( "#ABABAB") );
			}
			v.setBackgroundResource( R.drawable.post_base );

		}
	}




}
