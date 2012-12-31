package com.playup.android.fragment;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;


import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.PublicProfileAdapter;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.ImageDownloader;

import com.playup.android.util.Util;

/**
 * Public profile Fragment 
 */
public class PublicProfileFragment extends MainFragment implements OnClickListener {


	private RelativeLayout friend_graphic_dmLoading,friend_graphic_frndMe,messageMe_graphic_add_view,friend_graphic_you_view,friend_graphic_reqSend;

	private ImageView avatarImageView;

	private TextView nameTextView;
	private TextView userIdTextView;


	private ListView recentActivityListView;
	private ImageView noRecentActivity;
	private String vSelfUrl;
	

	private boolean isAgainActivated = false;



	private PublicProfileAdapter adapter;
	private boolean isSet = false;
	private Cursor c;
	private ImageDownloader imageDownloader = new ImageDownloader();


	// private String fromFragment;
	private int myId=-1;
	
	private int publicUserId=-1;
	private boolean isFriendRequested = false;
	private boolean requestLogin = false;
	RelativeLayout content_layout ;
	private boolean isDownloading;

	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;

	private Timer refreshProfileTimer;
	
	private TimerTask refreshProfileTask;
	
	private Boolean isHref = 	false;



	@Override
	public void onStop () {
		super.onStop();
		if ( c!= null && !c.isClosed() ) {
			c.close();
		}
		
		if(refreshProfileTask != null)
			refreshProfileTask.cancel();
		
		if(refreshProfileTimer != null)
			refreshProfileTimer.cancel();
		
		
		refreshProfileTask = null;
		refreshProfileTimer = null;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {


		content_layout = ( RelativeLayout ) inflater.inflate ( R.layout.publicprofile, null );

		if ( !isAgainActivated ) {

			setUserId(getArguments()); 
		}


		return content_layout;

	}

	@Override
	public void onResume () {
		super.onResume();


		if ( imageDownloader == null) {
			imageDownloader= new ImageDownloader();



		}

		initializeViews ( content_layout );		
		isSet = false;
		isFriendRequested = false;
		friend_graphic_dmLoading.setVisibility(View.VISIBLE);
		friend_graphic_frndMe.setVisibility(View.GONE);
		friend_graphic_you_view.setVisibility(View.GONE);
		friend_graphic_reqSend.setVisibility(View.GONE);
		messageMe_graphic_add_view.setVisibility(View.GONE);

		if( !requestLogin ) {
			setValues();
		}
	}

	@Override
	public void onAgainActivated(Bundle args) {


		isAgainActivated= true;
		setUserId(args);



	}

	/**
	 * getting the user id from the bundle and setting it.
	 * @param bundle
	 */
	private void setUserId ( Bundle bundle ) {
		
		vMainColor = null;
		vMainTitleColor = null;
		
		vSecColor = null;
		 vSecTitleColor = null;
		
		isDownloading = false;
		
		if (bundle != null && bundle.containsKey("isHref")) {
			isHref = bundle.getBoolean("isHref");
		}
		if ( bundle != null && bundle.containsKey( "vSelfUrl" ) ) {
			vSelfUrl = bundle.getString( "vSelfUrl" );

			fetchUserData ();
		}if (bundle != null && bundle.containsKey("vMainColor")) {
			vMainColor = bundle.getString("vMainColor");
		}if (bundle != null && bundle.containsKey("vMainTitleColor")) {
			vMainTitleColor = bundle.getString("vMainTitleColor");
		}if (bundle != null && bundle.containsKey("vSecColor")) {
			vSecColor = bundle.getString("vSecColor");
		}if (bundle != null && bundle.containsKey("vSecTitleColor")) {
			vSecTitleColor = bundle.getString("vSecTitleColor");
		}
		



	}

	/**
	 * Initialising the Views
	 * @param content_layout
	 */
	private void initializeViews ( final RelativeLayout content_layout ) {

		friend_graphic_you_view=(RelativeLayout)content_layout.findViewById(R.id.friend_graphic_you_view);
		messageMe_graphic_add_view = ( RelativeLayout )content_layout.findViewById( R.id.messageMe_graphic_add_view );
		friend_graphic_reqSend = ( RelativeLayout ) content_layout.findViewById( R.id.friend_graphic_reqSend );
		friend_graphic_frndMe = ( RelativeLayout ) content_layout.findViewById( R.id.friend_graphic_frndMe );
		friend_graphic_dmLoading = ( RelativeLayout ) content_layout.findViewById( R.id.friend_graphic_dmLoading );

		
	


		avatarImageView = (ImageView) content_layout.findViewById( R.id.profile_image );

		nameTextView  = (TextView) content_layout.findViewById( R.id.user_name );
		userIdTextView = (TextView) content_layout.findViewById( R.id.user_id );
		userIdTextView.setText ("");
		recentActivityListView = ( ListView ) content_layout.findViewById( R.id. recentActivityListView );
		noRecentActivity = (ImageView) content_layout.findViewById( R.id.no_recent_activity );


		setListeners();

	}

	/**
	 * Setting Listeners
	 */

	private void setListeners(){

		friend_graphic_frndMe.setOnClickListener(this);
		messageMe_graphic_add_view.setOnClickListener( this );

	}

	@Override
	public void onUpdate( final Message msg ) { 


		if ( PlayUpActivity.handler != null ) {
			PlayUpActivity.handler.post( new Runnable () {

				@Override
				public void run() {

					try {
						if ( !isVisible() ) {
							return;
						}
						if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
							
							fetchUserData();
							
						}
						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "FAN_PROFILE_DATA" ) ) {
							
							
							//firstDisplay/refresh of user profile
							//Log.e("123","insde FAN_PROFILE_DATA >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  ");
							
							closeDialog();
							isDownloading = false;
							
							if(msg.arg1 == 1)
								setValues();
							
							refreshUserProfile();							
							return;
							
							
							
						}
						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "AddFriend" ) ) {
							//friendRequest response
							isFriendRequested = false;
							if(msg.arg1 == 1) {
								PlayupLiveApplication.showToast(R.string.error_message_addFriend);
							}
						}
						setValues();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//Logs.show ( e );
					}
				}

			});
		}

	}
	
	/**
	 * scheduling the refresh of userProfile
	 */
	
	private void refreshUserProfile(){
		
		
		if(refreshProfileTimer == null)
			refreshProfileTimer = new Timer();
		
		if(refreshProfileTask == null){
			refreshProfileTask = new TimerTask() {
				
				@Override
				public void run() {
					fetchUserData();
					
				}
			};
			
			
			int cacheTime = Integer.parseInt(DatabaseUtil.getInstance().getCacheTime(vSelfUrl));
			if(cacheTime > 0){
				
				refreshProfileTimer.schedule(refreshProfileTask, cacheTime * 1000, cacheTime * 1000);
				
			}else{
				
				refreshProfileTask = null;
				refreshProfileTimer = null;
				
			}
		
		}
		
		
	}

	/**
	 * Setting Values
	 */
	private void setValues ( ) {
		
		try{
		Cursor c=null;
		c= PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT iId FROM user WHERE isPrimaryUser = \"" + 1 + "\" ");
		if ( c != null ) {

			if ( c.getCount() > 0 ) {
				c.moveToFirst();
				myId = c.getInt( c.getColumnIndex( "iId" ) );
			}
			c.close();
			c = null;
		}

		// getting data from DB and setting it on UI
		c = null;
		c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT iUserId, vName, vUserName, vFriendsUrl,vFriendshipStatusUrl, vRecentActivityUrl, vUserAvatarUrl, vUserToken, vSelfUrl, iId FROM user WHERE ( LENGTH(vSelfUrl)> 0 AND vSelfUrl = \"" + vSelfUrl + "\" )OR ( LENGTH(vHrefUrl)> 0 AND vHrefUrl = \"" + vSelfUrl + "\" )");
		String vUserId  = "";
		if ( c != null ) {


			if ( c.getCount() > 0 ) {


				c.moveToFirst();

				String vUserName = c.getString( c.getColumnIndex( "vUserName" ) );
				
				vUserId = c.getString( c.getColumnIndex( "iUserId" ) );
				String name = c.getString( c.getColumnIndex( "vName" ) );
				String vAvatarUrl = c.getString( c.getColumnIndex( "vUserAvatarUrl" ) );
				
				publicUserId = c.getInt( c.getColumnIndex( "iId" ) );

				setUserData ( name, vUserName, vAvatarUrl );

			}
			c.close();
			c = null;
		}

		setFriendGraphic();


		// setting the recent list
		setRecentList (vUserId);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	Logs.show ( e );
		}

	}
	
	/**
	 * making api call to fetch the user profile data
	 */

	private void fetchUserData () {

		try{
	//	Log.e("123","inside fetchUserData >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+isHref);
		
		//Log.e("123","inside fetchUserData >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  vSelfUrl    "+vSelfUrl);
		
		isDownloading = true;
		//Praveen :changed
		new Util().getFanProfileData(vSelfUrl, isHref);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
	//		Logs.show ( e );
		}
	}

	private ProgressDialog pd;
	

	private void closeDialog () {

		if ( pd != null && pd.isShowing()) {
			pd.cancel();
			pd = null;
		}
	}


	private boolean isPrimaryUser = false;

	/**
	 * Setting friend graphic for setting loading, "Add as friend" or "This is you" or "We're friends.Message me"
	 */

	private void setFriendGraphic() {
		
		try{
		
	//	Log.e("123","insde   setFriendGraphic >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+isDownloading);

		if(myId !=-1 && publicUserId !=-1 && myId == publicUserId) {

			isPrimaryUser = true;
			friend_graphic_dmLoading.setVisibility(View.GONE);
			friend_graphic_frndMe.setVisibility(View.GONE);
			messageMe_graphic_add_view.setVisibility(View.GONE);
			friend_graphic_reqSend.setVisibility(View.GONE);
			friend_graphic_you_view.setVisibility(View.VISIBLE);

			TextView txt = (TextView) friend_graphic_you_view.findViewById( R.id.itsYouText );			
			txt.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
			txt.setTextColor(Color.parseColor("#9D999D"));
			txt = null;

		} else {
			if(isDownloading){
				friend_graphic_dmLoading.setVisibility(View.VISIBLE);
				friend_graphic_frndMe.setVisibility(View.GONE);
				friend_graphic_you_view.setVisibility(View.GONE);
				friend_graphic_reqSend.setVisibility(View.GONE);
				messageMe_graphic_add_view.setVisibility(View.GONE);
				return;
			}


		//	Log.e("123","insde   setFriendGraphic !isDownloading>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  ");
			
			isPrimaryUser = false;
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			//Praveen : changed
			String status = dbUtil.getFrienshipStatus ( vSelfUrl );
			
		//	Log.e("123","insde   setFriendGraphic !isDownloading>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> status   "+status);
			
		//	Log.e("123", "Inside public Profilr Fragment"+ status);
			if ( status != null && status.trim().length() > 0) {


				if ( status.equalsIgnoreCase( "none" ) ) {

					if ( !isFriendRequested ) {
						friend_graphic_dmLoading.setVisibility(View.GONE);
						friend_graphic_frndMe.setVisibility(View.VISIBLE);
						messageMe_graphic_add_view.setVisibility(View.GONE);
						friend_graphic_reqSend.setVisibility(View.GONE);
						friend_graphic_you_view.setVisibility(View.GONE);						
						TextView txt = (TextView) friend_graphic_frndMe.findViewById( R.id.addFriendText_dm );
						txt.setText(R.string.addasafriend);
						txt.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
					}


				} else if ( status.equalsIgnoreCase( "invited" )  ) { 				
					friend_graphic_dmLoading.setVisibility(View.GONE);
					friend_graphic_frndMe.setVisibility(View.GONE);
					messageMe_graphic_add_view.setVisibility(View.GONE);
					friend_graphic_reqSend.setVisibility(View.VISIBLE);
					friend_graphic_you_view.setVisibility(View.GONE);
					TextView txt = (TextView) friend_graphic_reqSend.findViewById( R.id.friendReqSendText );
					txt.setTypeface(Constants.OPEN_SANS_SEMIBOLD);

				}else if ( status.equalsIgnoreCase( "awaiting_confirmation" )){
					friend_graphic_dmLoading.setVisibility(View.GONE);
					friend_graphic_frndMe.setVisibility(View.VISIBLE);
					messageMe_graphic_add_view.setVisibility(View.GONE);
					friend_graphic_reqSend.setVisibility(View.GONE);
					friend_graphic_you_view.setVisibility(View.GONE);						
					TextView txt = (TextView) friend_graphic_frndMe.findViewById( R.id.addFriendText_dm );
					txt.setText(R.string.confirmFriend);
					txt.setTypeface(Constants.OPEN_SANS_SEMIBOLD);



				}

				else if ( status.equalsIgnoreCase( "friends" ) ) {
					
					friend_graphic_dmLoading.setVisibility(View.GONE);
					friend_graphic_frndMe.setVisibility(View.GONE);
					messageMe_graphic_add_view.setVisibility(View.VISIBLE);
					friend_graphic_reqSend.setVisibility(View.GONE);
					friend_graphic_you_view.setVisibility(View.GONE);
					TextView txt1 = (TextView) messageMe_graphic_add_view.findViewById( R.id.werFriendText_dm );
					TextView txt2 = (TextView) messageMe_graphic_add_view.findViewById( R.id.messageMeText_dm );
					txt1.setTypeface(Constants.OPEN_SANS_REGULAR);
					txt2.setTypeface(Constants.OPEN_SANS_SEMIBOLD);

				} 

			}

			dbUtil  =null;
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	Logs.show ( e );
		}
	}


	/** 
	 * fetching value for Recentlist item
	 */
	private void setRecentList (String vUserId) {
		
		try{

		if ( c != null ) {
			c.close();
			c = null;
		}

		if(vUserId.length() > 0){


			{
				c = PlayupLiveApplication.getDatabaseWrapper().selectQuery("SELECT r.vRecentName AS vRecentName," +
						"iAccess AS iAccess, iAccessPermitted AS iAccessPermitted, " +
						"r.vSubjectId AS vSubjectId , r.vSubjectTitle,r.iUnRead ," +
						"ur.vUserRecentId,r.vSubjectUrl AS vSubjectUrl,r.vSubjectHref AS vSubjectHref FROM recent r , " +
						"user_recent ur WHERE ur.vUserRecentId=r.vUserRecentId AND ur.vUserId=\""+vUserId+"\" " +
								"ORDER BY r.rowid ASC LIMIT 0,5");

			}

			if(c == null){
				noRecentActivity.setVisibility( View.VISIBLE ) ;
				recentActivityListView.setVisibility( View.GONE );
			} else {

				if ( c.getCount() == 0 ) {
					noRecentActivity.setVisibility( View.VISIBLE ) ;
					recentActivityListView.setVisibility( View.GONE );
				} else {
					noRecentActivity.setVisibility( View.GONE ) ;
					recentActivityListView.setVisibility( View.VISIBLE );
				}
			}
			adapter = new PublicProfileAdapter ( c, isPrimaryUser,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor);
			
				recentActivityListView.setAdapter ( adapter );
			
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	Logs.show ( e );
		}
		
	}

	@Override
	public void onPause () {
		super.onPause();
		isSet = false;
		if ( c != null ) {
			c.close();
			c = null;
		}
	}


	/**
	 * setting the user data 
	 * @param name
	 * @param vUserName
	 * @param vAvatarUrl
	 */
	private void setUserData ( final String name, final String vUserName, final String vAvatarUrl ) {
		try{
		if ( nameTextView == null ) {
			return;
		}
		nameTextView.setText( name );

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		//Praveen : Changed
		String iUserId = dbUtil.getUserIdFromSelfUrl( vSelfUrl );

		dbUtil = null;

		String userIdText = iUserId;

		if ( userIdText == null ) {
			userIdText = "";
		}
		userIdText= userIdText.replace("profile-", "");
		userIdText= userIdText.replace("fan", "");
		userIdText = userIdText.replace("&", "");
		userIdText = userIdText.replace("'", "");
		userIdText = userIdText.replace("/", "");
		userIdText = userIdText.replace(":", "");
		userIdText = userIdText.replace("<", "");
		userIdText = userIdText.replace(">", "");
		userIdText = userIdText.replace("@", "");
		userIdText = userIdText.replace("\"", "");
		userIdText = userIdText.replace(" ", "");
		userIdText = userIdText.replace("_", "");
		userIdText = userIdText.replace("-", "");
		userIdText = userIdText.replace("users", "");


		userIdTextView.setText( userIdText);


		if ( vAvatarUrl != null && imageDownloader != null && avatarImageView != null ) {
			imageDownloader.download( vAvatarUrl.replace("square", "large"),  avatarImageView, true,null );
		}

		setTopBarFragment ( vUserName );
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show ( e );
		}
	}


	/**
	 * Setting Topbar Fragment
	 */
	private void setTopBarFragment ( final String vUserName ) {

		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					if ( !isSet ) {
						if( vUserName!= null )
							isSet = true;
						
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						
						
						
						String vChildColor = dbUtil.getSectionMainColor("", vSelfUrl);
						String vChildTitleColor = dbUtil.getSectionMainTitleColor("", vSelfUrl);
						
						if(vChildColor != null && vChildColor.trim().length() > 0 )
							vMainColor = vChildColor;
						
						if(vChildTitleColor != null && vChildTitleColor.trim().length() > 0 )
							vMainTitleColor = vChildTitleColor;
						
						String vChildSecondaryColor = dbUtil.getSectionSecondaryColor ("", vSelfUrl);
						 String vChildSecondaryTitleColor = dbUtil.getSectionSecondaryTitleColor ( "", vSelfUrl );
						
						 
						 if(vChildSecondaryColor != null && vChildSecondaryColor.trim().length() > 0)
							 vSecColor = vChildSecondaryColor;
						 
						 if(vChildSecondaryTitleColor != null && vChildSecondaryTitleColor.trim().length() > 0)
							 vSecTitleColor = vChildSecondaryTitleColor;
						
						
						HashMap< String, String > map = new HashMap<String, String>();
						map.put( "vUserName", vUserName );
						Bundle b = new Bundle();
						b.putString("vMainColor",vMainColor );
						b.putString("vMainTitleColor",vMainTitleColor );
						Message msg = new Message ();
						msg.setData(b);
						msg.obj = map;
						PlayupLiveApplication.callUpdateOnFragments(msg);

					}
				} catch (Exception e) {
				//	Logs.show(e);
				}
				
			}
		}).start();
		
		
	}


	@Override
	public void onClick(View v) {
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();

		switch (v.getId()) 
		{
		case R.id.messageMe_graphic_add_view :
			
			//Praveen : Changed
			Hashtable<String, Object> result = dbUtil.getDirectConversationUrl( vSelfUrl ); 
			String vDirectConversationUrl = (String) result.get("url");
			Boolean isHrefUrl 	=(Boolean) result.get("isHref");

			if ( vDirectConversationUrl != null ) {
				Bundle bundle = new Bundle ();
				bundle.putString( "vDirectConversationUrl", vDirectConversationUrl);
				//Praveen : modified
				bundle.putBoolean( "isHrefUrl", isHrefUrl);
				bundle.putString( "vUserSelfUrl", vSelfUrl );
				bundle.putString("vMainColor",vMainColor );
				bundle.putString("vMainTitleColor",vMainTitleColor );
				bundle.putString( "vSecColor",vSecColor );			
				bundle.putString( "vSecTitleColor",vSecTitleColor );
				
				PlayupLiveApplication.getFragmentManagerUtil().setFragment( "DirectMessageFragment", bundle );
			}

			break;

		case R.id.friend_graphic_frndMe:
			//if the user is annonymous,then redirecting to login
			//else friendRequest is sent

			if ( dbUtil.isUserAnnonymous() ) {
				
				requestLogin = true;
				friend_graphic_frndMe.setVisibility(View.GONE);
				friend_graphic_dmLoading.setVisibility(View.VISIBLE);					
				Bundle bundle = new Bundle ();
				String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
				bundle.putString( "fromFragment", topFragmentName);
				PlayupLiveApplication.getFragmentManagerUtil().setFragment( "ProviderFragment", bundle );
			} else {
				String text = ((TextView)friend_graphic_frndMe.findViewById( R.id.addFriendText_dm )).getText().toString();
				friend_graphic_frndMe.setVisibility(View.GONE);
				friend_graphic_dmLoading.setVisibility(View.VISIBLE);
				if(text.equalsIgnoreCase(PlayUpActivity.context.getResources().getString(R.string.confirmFriend))){

					new Util().addFriend ( vSelfUrl,isHref,"friends" );

				}else{
					if( !isFriendRequested ) {
						isFriendRequested = true;
						new Util().addFriend ( vSelfUrl,isHref,"invited" );

					}

				}



			}

			break;

		default:
			break;
		}
		dbUtil = null;

	}




}