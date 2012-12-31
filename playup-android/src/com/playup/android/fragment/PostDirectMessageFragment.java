package com.playup.android.fragment;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.connection.HttpRequest;
import com.playup.android.dialog.InternetConnectivityDialog;
import com.playup.android.exception.RequestRepeatException;
import com.playup.android.util.Constants;

import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;
import com.playup.android.util.ImageDownloader;

import com.playup.android.util.Types;
import com.playup.android.util.Util;


/**
 * Post Direct Message Fragment. 
 */
public class PostDirectMessageFragment extends MainFragment {

	private String friendName = null;
	private LinearLayout friendsMessageView;
	private LinearLayout userMessageView;
	private RelativeLayout content_layout;
	private TextView characterCount;
	private EditText userTextMessage;
	private Button postButton;
	private ScrollView scrollView;
	boolean isButtonSelected;
	private ImageView blank_avatar_img;
	public static boolean  isHomeTapped = false;
	private InternetConnectivityDialog internetConnectivityDialog ;
	// related to previous latest post.
	private TextView userName;
	private TextView userMsg;
	private ImageView userAvatar;
	private TextView userTimestamp;
	private RelativeLayout mainLayout;

	private int checkValue=140;
	private boolean isAgainActivated = false;

	private ImageDownloader imageDownloader = null ;
	private String requestData;
	private ProgressDialog progressDialog;


	private String vDirectMessageUrl = null; 
	private String vDirectConversationUrl = null;
	private String vUserSelfUrl = null;
	private String fromFragment = null;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;

	private ImageView sendMessage;
	Hashtable < String , List < String > > data;

	boolean isvDirectMessageHrefUrl = false;
	
	boolean isvDirectConversationUrlHref = false;
	boolean isUserSelfUrlHref = false;
	@Override
	public void onDestroy () {
		super.onDestroy();

		vDirectMessageUrl = null;  
	}

	@Override
	public void onStop () {
		super.onStop();

		if ( userTextMessage != null ) {
			InputMethodManager imm = (InputMethodManager)PlayUpActivity.context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(userTextMessage.getWindowToken(), 0);
		}

		requestData  = null ;

		if ( progressDialog != null && progressDialog.isShowing() ) {
			progressDialog.dismiss();
		}

		if ( data != null) {
			data.clear();
		}
		data  = null ;

		if ( internetConnectivityDialog != null && internetConnectivityDialog.isShowing() ) {
			internetConnectivityDialog.dismiss();
		}
		//		internetConnectivityDialog = null;
		//		progressDialog = null ;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


		content_layout = (RelativeLayout) inflater.inflate(R.layout.post_direct_message, null);

		// get from bundle 
		if ( !isAgainActivated ) {
			setDirectMessageUrl ( getArguments() );
		}


		return content_layout;
	}


	/**
	 * getting direct conversation url from bundle
	 * @param bundle
	 */
	private void setDirectMessageUrl ( Bundle bundle ) {
		
		vMainColor = null;
		vMainTitleColor = null;
		
		vSecColor = null;
		vSecTitleColor = null;
		isvDirectMessageHrefUrl = false;
		
		 isvDirectConversationUrlHref = false;
		 isUserSelfUrlHref = false;
		
		
		if ( bundle != null && bundle.containsKey( "isvDirectMessageHrefUrl")) {
			 isvDirectMessageHrefUrl = bundle.getBoolean( "isvDirectMessageHrefUrl" );
		}
		if (bundle != null && bundle.containsKey("isvDirectConversationUrlHref")) {
			 isvDirectConversationUrlHref = bundle.getBoolean("isvDirectConversationUrlHref");
		}

		if (bundle != null && bundle.containsKey("isUserSelfUrlHref")) {
			 isUserSelfUrlHref = bundle.getBoolean("isUserSelfUrlHref");
		}


		if ( bundle != null && bundle.containsKey( "vDirectConversationUrl")) {
			vDirectConversationUrl = bundle.getString( "vDirectConversationUrl" );
		}
		if (bundle != null && bundle.containsKey("vDirectMessageUrl")) {
			vDirectMessageUrl = bundle.getString("vDirectMessageUrl");
		}

		if (bundle != null && bundle.containsKey("vUserSelfUrl")) {
			vUserSelfUrl = bundle.getString("vUserSelfUrl");
		}
		if (bundle != null && bundle.containsKey("fromFragment")) {
			fromFragment = bundle.getString("fromFragment");
		} else {
			fromFragment = null;
		}

		if( bundle != null && bundle.containsKey("vFriendName") ) {
			friendName = bundle.getString("vFriendName");
		} else {
			friendName = null;
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

	@Override
	public void onAgainActivated(Bundle args) {

		isAgainActivated = true;
		setDirectMessageUrl(args);

	}


	/**
	 *  initialise the views
	 * @param content_layout
	 */
	private void initialize ( RelativeLayout content_layout ) {

		if (imageDownloader  == null ) {
			imageDownloader = new ImageDownloader ();
		}

		friendsMessageView = (LinearLayout) content_layout.findViewById(R.id.friendMessageView);
		userMessageView = (LinearLayout) content_layout.findViewById(R.id.userMessageView);



		content_layout.findViewById( R.id.linearLayout1 ).setBackgroundResource( R.drawable.post_base_alpha );

		mainLayout  = (RelativeLayout) content_layout.findViewById( R.id.mainLayout );

		userTextMessage=(EditText)content_layout.findViewById(R.id.userTextMessage);

		characterCount= (TextView)content_layout.findViewById(R.id.characterCount);

		postButton = (Button) content_layout.findViewById(R.id.postButton);
		scrollView = (ScrollView)content_layout.findViewById(R.id.scrollView);

		blank_avatar_img = (ImageView) content_layout.findViewById(R.id.blank_avatar_img );

		userTextMessage.setTypeface(Constants.OPEN_SANS_REGULAR );
		userTextMessage.setSingleLine(false);
		characterCount.setTypeface( Constants.OPEN_SANS_REGULAR );



		// set listeners
		setListeners();

		if ( !isAgainActivated )
		{
			// set values
			setValues();
		}


	}

	/**
	 *  showing the  progress dialog
	 */
	private void showProgressDialog () {

		if ( PlayUpActivity.context != null && Constants.isCurrent && PostDirectMessageFragment.this.getActivity() != null ) {
			if ( progressDialog == null && Constants.isCurrent ) {
				progressDialog = new ProgressDialog( PlayUpActivity.context );
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setCancelable(true);
			}
			progressDialog.setMessage(getString(R.string.posting));
			progressDialog.show();
			
		}
	}

	/**
	 *  cancellnig the progress dialog 
	 */
	private void closeProgressDialog () {

		if ( progressDialog != null ) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}


	/**
	 *  showing keyboard on its focus
	 */
	public void onResume () {
		super.onResume();
		try{
		if ( imageDownloader == null ){
			imageDownloader = new ImageDownloader();
		}
		setTopBar();
		initialize(content_layout);
		refresh ();


		PlayUpActivity.mBinder	=	userTextMessage.getWindowToken();	
		PlayUpActivity.handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					if ( userTextMessage != null ) {
						InputMethodManager imm = (InputMethodManager)PlayUpActivity.context.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(userTextMessage,0);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}
		}, 1000);

		userTextMessage.requestFocus();
		PlayUpActivity.handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				if ( userTextMessage != null ) {
					userTextMessage.performClick();		
				}
			}
		}, 1000);



		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	Logs.show ( e );
		}

		

	}

	

	public void onUpdate ( final Message msg ) {

		try {
			if ( msg != null && msg.obj != null &&msg.obj.toString().equalsIgnoreCase("handleBackButton") ) {

				PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );
			}

			
			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "credentials stored" ) ) {
				
				//enabling/disabling the post button

				if ( PlayUpActivity.handler != null ) {
					PlayUpActivity.handler.post( new Runnable( ) {

						@Override
						public void run() {

							try {
								if ( !isVisible() ) {
									return;
								}

								if ( postButton != null ) {
									postButton.setEnabled( true );
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								//Logs.show ( e );
							}

						}
					});
				}
			}
			


			if ( msg  != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "Posted" ) ) {

				if ( PlayUpActivity.handler != null ) {
					PlayUpActivity.handler.post(new Runnable() {
						public void run() {

							try {
								if ( !isVisible() ) {
									return;
								}
								closeProgressDialog();

								
								
								if( fromFragment != null && fromFragment.contains("PlayupFriendsFragment") ) {

									Bundle bundle = new Bundle ();
									bundle.putString( "fromFragment", fromFragment);
									bundle.putBoolean( "isvDirectConversationUrlHref", isvDirectConversationUrlHref);
									bundle.putBoolean( "isUserSelfUrlHref", isUserSelfUrlHref);
									
									bundle.putString( "vUserSelfUrl", vUserSelfUrl);
									bundle.putString( "vDirectConversationUrl", vDirectConversationUrl );
									bundle.putString("vMainColor",vMainColor );
									bundle.putString("vMainTitleColor",vMainTitleColor );
									bundle.putString( "vSecColor",vSecColor );			
									bundle.putString( "vSecTitleColor",vSecTitleColor );
									
									PlayupLiveApplication.getFragmentManagerUtil().setFragment("DirectMessageFragment", bundle );
								} else {
									PlayupLiveApplication.getFragmentManagerUtil().popBackStack("PostDirectMessageFragment");
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
							//	Logs.show ( e );
							}


						}
					});

				}
			}

			if ( msg  != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "Error" ) ) {

				if ( PlayUpActivity.handler != null ) {
					PlayUpActivity.handler.post(new Runnable() {
						public void run() {

							try {
								if ( !isVisible() ) {
									return;
								}
								
								postButton.setEnabled( true );
								closeProgressDialog ();
								PlayupLiveApplication.showToast( R.string.error_message_posted );
							} catch (Exception e) {
								// TODO Auto-generated catch block
								//Logs.show ( e );
							}


						}
					});

				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
	//	Logs.show(e);
		}

	}

	/**
	 * called when resumed from background
	 */
	private void refresh () {
		
		setValues();

		if (!isHomeTapped ) {
			if( friendName!=null ) {
				userTextMessage.setText("@"+friendName+" ");
				userTextMessage.setSelection(userTextMessage.getText().toString().length());

			}	else
				userTextMessage.setText("");
			
		}else if( userTextMessage.getText().toString().length() > 0 &&
				userTextMessage.getText().toString().length() <= 140  ){

			postButton.setBackgroundResource(R.drawable.generic_active_button);
		}

	}


	// hiding keyboard on focus gone of this fragment.
	public void onPause () {
		super.onPause();
		userTextMessage = null;
		closeProgressDialog ();

		
		isHomeTapped = true;

	}


	// setting the listeners
	private void setListeners() {

		postButton.setEnabled( true );
		postButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if ( userTextMessage == null ) {
					return true;
				}
				int result = userTextMessage.getText().toString().trim().length();
				int result1 = userTextMessage.getText().toString().length();

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (result!=0 && result1 > 0 && result1 <= 140) {
					
						postButton.setBackgroundResource(R.drawable.generic_down_button);
					}
				} else if (event.getAction() == MotionEvent.ACTION_CANCEL
						|| event.getAction() == MotionEvent.ACTION_OUTSIDE
						|| event.getAction() == MotionEvent.ACTION_UP) {

					if (result!=0 && result1 > 0 && result1 <= 140) {
						postButton.setBackgroundResource(R.drawable.generic_active_button);
					} else {
						postButton.setBackgroundResource(R.drawable.generic_inactive_button);
					}
				}

				if(event.getAction() == MotionEvent.ACTION_UP && result!=0 && result1 > 0 && result1 <= 140 ){//POST THE MESSAGE

					try {

						if ( userTextMessage == null ) {
							return true;
						} 

						showProgressDialog();

						JSONObject mJsonObject = new JSONObject();

						mJsonObject.put(":type", "application/vnd.playup.message.text+json");
						mJsonObject.put("message", userTextMessage.getText().toString() );
						requestData = mJsonObject.toString();

						if ( ! Util.isInternetAvailable() && Constants.isCurrent ) {
							closeProgressDialog();

							return true;
						} else {
							if( vDirectMessageUrl!=null)
								postButton.setEnabled( false );

							if( vDirectMessageUrl == null ) {
								getConversation();
							} else {
								
								FlurryAgent.onEvent("direct-messages");
								// MIGHT REQUIRE TO INCLUDE THE isHref FOR THE BELOW JSON CALL , SINCE IT IS BEING USED IN THE URIL LATER IN THE FLOW, PLSSS CHK
								new Util().postMessage( vDirectMessageUrl, requestData,isvDirectMessageHrefUrl );
							}
						}


					}catch (JSONException e) {
						//Logs.show( e ); 
					}	
				}
				return true;
			}

		});

	}

	// get the values from database and set them in views
	private void setValues() {

		try{
		setPreviousMessage();

		postButton.setBackgroundResource(R.drawable.generic_inactive_button);


		// getting the image for the avatar
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		imageDownloader.download( dbUtil.getUserId()   , dbUtil.getUserAvatarUrl(), blank_avatar_img,true,null );
		dbUtil = null;


		userTextMessage.addTextChangedListener(txwatcher);
		userTextMessage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if ( PlayUpActivity.handler != null ) {
					PlayUpActivity.handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							try {
								if ( scrollView != null ) {
									scrollView.fullScroll(ScrollView.FOCUS_DOWN);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								//Logs.show ( e );
							}
						}
					}, 500);

				}

			}
		});

		userTextMessage.setOnFocusChangeListener( new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				//	if (hasFocus) {

				if ( PlayUpActivity.handler != null ) {
					PlayUpActivity.handler.postDelayed(new Runnable() {

						@Override
						public void run() {

							try {
								if ( scrollView != null ) {
									scrollView.fullScroll(ScrollView.FOCUS_DOWN);
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								//Logs.show ( e );
							}
						}
					}, 2000);
				}
			}
		});
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	Logs.show ( e );
		}
	}

	/**
	 * setting topbar with the selected friend name
	 */
	public void setTopBar() {	
		
		try{
			String vFriendName = "";
			
			Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT vFriendUserName FROM playup_friends WHERE  " +
					"vUserSelfUrl = \"" + vUserSelfUrl + "\" OR vUserHrefUrl = '"+vUserSelfUrl+"'");
			if ( c != null ) {

				if ( c.getCount() > 0)  {
					c.moveToFirst();
					vFriendName = c.getString(c.getColumnIndex("vFriendUserName"));
				}
				c.close();
				c = null;
			}
			HashMap< String, String > map = new HashMap<String, String>();
			map.put( "vFriendName", vFriendName );
			Bundle b = new Bundle();
			b.putString("vMainColor",vMainColor );
			b.putString("vMainTitleColor",vMainTitleColor );
			Message msg = new Message ();
			msg.setData(b);
			msg.obj = map;
			PlayupLiveApplication.callUpdateTopBarFragments(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show ( e );
		}
		}
	/**
	 * setting recent previous message
	 */

	public void setPreviousMessage() {
		// setting the previos latest post data 
		try{
		String friendUserId = null;
		int myId=-1;
		int friendId = -1;

		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT iId FROM user WHERE isPrimaryUser = \"" + 1 + "\" ");
		if ( c != null ) {

			if ( c.getCount() > 0 ) {
				c.moveToFirst();
				myId = c.getInt( c.getColumnIndex( "iId" ) );
			}
			c.close();
			c = null;
		}

		data =  PlayupLiveApplication.getDatabaseWrapper().select( "select vUserId,vMessage,dCreatedDate,vDisplayName," +
				"vAvatarUrl from direct_message_items dmi " +
				"LEFT JOIN direct_messages dm ON dmi.vDMessageId= dm.VDMessageId WHERE " +
				"dm.vDMessageUrl = \"" + vDirectMessageUrl + "\" OR dm.vDMessageHrefUrl = '"+vDirectMessageUrl+"' " +
				"ORDER BY dCreatedDate DESC LIMIT 0, 1;");
		if ( data != null && data.get("vMessage").size() > 0 ) {
			if( data.get("vUserId").get(0)!=null)
				friendUserId =data.get("vUserId").get(0);
		}



		if( friendUserId != null) {
			Cursor c1 = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT iId " +
					"FROM user WHERE vSelfUrl = \"" + friendUserId + "\" OR vHrefUrl = '"+friendUserId+"'");
			if ( c1 != null ) {
				if ( c1.getCount() > 0 ) {
					c1.moveToFirst();
					friendId = c1.getInt( c1.getColumnIndex( "iId" ) );
				}
				c1.close();
				c1 = null;
			}

		}

		if( myId != -1 && friendId != -1 && myId == friendId) {		   
			userMessageView.setVisibility(View.VISIBLE);
			friendsMessageView.setVisibility(View.GONE);
			userName = (TextView) userMessageView.findViewById( R.id.userName );
			userMsg = (TextView) userMessageView.findViewById( R.id.userMsg );
			userTimestamp = (TextView) userMessageView.findViewById( R.id.userTimestamp );
			userAvatar = ( ImageView ) userMessageView.findViewById( R.id.imageViewpostAvatar );
			userMessageView.findViewById( R.id.linearLayout1 ).setBackgroundResource( R.drawable.post_base_alpha );

		} else {
			userMessageView.setVisibility(View.GONE);
			friendsMessageView.setVisibility(View.VISIBLE);
			userName = (TextView) friendsMessageView.findViewById( R.id.userName );
			userMsg = (TextView) friendsMessageView.findViewById( R.id.userMsg );
			userTimestamp = (TextView) friendsMessageView.findViewById( R.id.userTimestamp );
			userAvatar = ( ImageView ) friendsMessageView.findViewById( R.id.imageViewpostAvatar );
			friendsMessageView.findViewById( R.id.linearLayout1 ).setBackgroundResource( R.drawable.post_base_alpha );

		}


		if ( data != null && data.get("vMessage").size() > 0 ) {
			mainLayout.setVisibility( View.VISIBLE );
			userName.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
			userName.setText( new Util().getSmiledText( data.get( "vDisplayName" ).get(0)));
			userMsg.setText( new Util().getSmiledText( data.get( "vMessage" ).get(0 ).replaceAll("(\\r|\\n|\\t)", " ")));
			userTimestamp.setText( new DateUtil().gmt_to_local_timezone( data.get("dCreatedDate").get( 0 ) )  );
			imageDownloader.download( data.get("vAvatarUrl").get( 0 ), userAvatar,true,null );
		} else {
			mainLayout.setVisibility( View.GONE );
		}

		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	Logs.show ( e );
		}
	}


	
	/**
	 * textwatcher for counting characters
	 */
	final TextWatcher txwatcher = new TextWatcher() {
		int result = 0, result1 =0;

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

			try{
				result = userTextMessage.getText().toString().trim().length();
				result1 = userTextMessage.getText().toString().length();

				if (result != 0 && result1 > 0 && result1 <= 140) {
					postButton.setBackgroundResource(R.drawable.generic_active_button); 

				} else {
					postButton.setBackgroundResource(R.drawable.generic_inactive_button);
				}


				if (result1 > checkValue) {
					characterCount.setTextColor(Color.parseColor("#DB2136"));
				} else {
					characterCount.setTextColor(Color.parseColor("#736E73"));
				}

				characterCount.setText("" + (checkValue - result1));
			}catch (Exception e) {
				// TODO: handle exception
			}

		}

		public void afterTextChanged(Editable s) {
		}
	};


	/**
	 * downloading the directmessage url
	 */
	private void getConversation () {
		// MIGHT REQUIRE TO INCLUDE THE isHref FOR THE BELOW JSON CALL , SINCE IT IS BEING USED IN THE URIL LATER IN THE FLOW, PLSSS CHK
		new Util().getDirectCoversationPostMsg( vDirectConversationUrl, requestData,isvDirectConversationUrlHref );
	}



}