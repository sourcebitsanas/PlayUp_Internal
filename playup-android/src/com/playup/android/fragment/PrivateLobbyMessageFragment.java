package com.playup.android.fragment;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.json.JSONArray;
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
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.connection.HttpRequest;
import com.playup.android.exception.RequestRepeatException;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;
import com.playup.android.util.json.PostMessage;


/**
 * PrivateLobbyMessageFragment Fragment. 
 */
public class PrivateLobbyMessageFragment extends MainFragment  {

	private String vFriendName;
	private RelativeLayout content_layout;
	private TextView characterCount;
	private EditText userTextMessage;
	private Button postButton;

	private ScrollView scrollView;
	boolean isButtonSelected;
	private ImageView blank_avatar_img;
	public static boolean  isHomeTapped = false;

	// related to previous latest post.
	private TextView userName;
	private TextView userMsg;
	private ImageView userAvatar;
	private TextView userTimestamp;
	private RelativeLayout mainLayout;

	private LinearLayout postprovider;

	private int checkValue=140;


	private String vMainColor = null;
	private String vMainTitleColor = null;

	
	private boolean isAgainActivated = false;

	private ImageDownloader imageDownloader = null ;
	private String requestData;
	private ProgressDialog progressDialog;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;


	private String vConversationId = null; 

	//private  String conversationURL	=	null;

	Hashtable < String , List < String > > data;
	private String fromFragment = null;



	@Override
	public void onDestroy () {
		super.onDestroy();

		vConversationId = null;  
	}

	@Override
	public void onStop () {
		super.onStop();

		requestData  = null ;

		if ( progressDialog != null && progressDialog.isShowing() ) {
			progressDialog.dismiss();
		}


		if ( data != null) {
			data.clear();
		}
		data  = null ;

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


		content_layout = (RelativeLayout) inflater.inflate(R.layout.post_message, null);

		// get from bundle 
		if ( !isAgainActivated ) {
			setConversationId ( getArguments() );
		}


		return content_layout;
	}

	/**
	 * seting the parameters passed on from the previous fragments
	 * @param bundle
	 */

	private void setConversationId ( Bundle bundle ) {
		vMainColor = null;
		vMainTitleColor = null;
		
		vSecColor = null;
		vSecTitleColor = null;
		
		fromFragment  = null;
		
		if (bundle != null && bundle.containsKey("vConversationId")) {
			vConversationId = bundle.getString("vConversationId");
		}
		if (bundle != null && bundle.containsKey("vFriendName")) {
			vFriendName = bundle.getString("vFriendName");
		} else {
			vFriendName = null;
		}if (bundle != null && bundle.containsKey("vMainColor")) {
			vMainColor = bundle.getString("vMainColor");
		}if (bundle != null && bundle.containsKey("vMainTitleColor")) {
			vMainTitleColor = bundle.getString("vMainTitleColor");
		}if (bundle != null && bundle.containsKey("vSecColor")) {
			vSecColor = bundle.getString("vSecColor");
		}if (bundle != null && bundle.containsKey("vSecTitleColor")) {
			vSecTitleColor = bundle.getString("vSecTitleColor");
		}if (bundle != null && bundle.containsKey("fromFragment")) {
			fromFragment = bundle.getString("fromFragment");
		}

	}

	@Override
	public void onAgainActivated(Bundle args) {

		isAgainActivated = true;
		setConversationId(args);

	}


	// initialise the views
	private void initialize ( RelativeLayout content_layout ) {

		if (imageDownloader  == null ) {
			imageDownloader = new ImageDownloader ();
		}

		userName = (TextView) content_layout.findViewById( R.id.userName );
		userMsg = (TextView) content_layout.findViewById( R.id.userMsg );
		userTimestamp = (TextView) content_layout.findViewById( R.id.userTimestamp );

		userAvatar = ( ImageView ) content_layout.findViewById( R.id.imageViewpostAvatar );

		content_layout.findViewById( R.id.linearLayout1 ).setBackgroundResource( R.drawable.post_base_alpha );

		mainLayout  = (RelativeLayout) content_layout.findViewById( R.id.mainLayout );



		userTextMessage=(EditText)content_layout.findViewById(R.id.userTextMessage);

		characterCount= (TextView)content_layout.findViewById(R.id.characterCount);

		postprovider = (LinearLayout) content_layout.findViewById( R.id.postprovider );

		postButton = (Button) content_layout.findViewById(R.id.postButton);
		scrollView = (ScrollView)content_layout.findViewById(R.id.scrollView);

		blank_avatar_img = (ImageView) content_layout.findViewById(R.id.blank_avatar_img );

		userTextMessage.setTypeface(Constants.OPEN_SANS_REGULAR );
		userTextMessage.setSingleLine(false);
		characterCount.setTypeface( Constants.OPEN_SANS_REGULAR );



		// set listeners
		setListeners();
		setTopBar();
		if ( !isAgainActivated ) {
			// set values
			setValues();
		}


	}

	// showing the  progress dialog
	private void showProgressDialog () {

		if ( PlayUpActivity.context != null && Constants.isCurrent && PrivateLobbyMessageFragment.this.getActivity() != null ) {
			if ( progressDialog == null && Constants.isCurrent) {
				progressDialog = new ProgressDialog( PlayUpActivity.context );
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setCancelable(true);
			}
			progressDialog.setMessage( PlayUpActivity.context.getResources().getString(R.string.posting));
			progressDialog.show();	
		}
		
	}

	// cancelling the progress dialog 
	private void closeProgressDialog () {

		if ( progressDialog != null ) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}


	// showing keyboard on its focus
	public void onResume () {
		super.onResume();

		if ( imageDownloader == null ){
			imageDownloader = new ImageDownloader();
		}

		initialize(content_layout);

		PlayUpActivity.mBinder	=	userTextMessage.getWindowToken();		

		PlayUpActivity.handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					if ( userTextMessage != null ) {
						InputMethodManager imm = (InputMethodManager)PlayUpActivity.context.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(userTextMessage,InputMethodManager.SHOW_FORCED);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show ( e );
				}
			}
		}, 500);

		userTextMessage.requestFocus();

		PlayUpActivity.handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				try {
					if ( userTextMessage != null ) {
						userTextMessage.performClick();		
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show ( e );
				}
			}
		}, 1000);


		refresh ();
	}
	
	
	


	public void onUpdate ( final Message msg ) {

		try {
			if ( msg != null && msg.obj != null &&msg.obj.toString().equalsIgnoreCase("handleBackButton") ) {

				PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );
			}
			if ( msg != null && msg.obj != null &&msg.obj.toString().equalsIgnoreCase("credentials stored") ) {

				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}


	// hiding keyboard on focus gone of this fragment.
	public void onPause () {
		super.onPause();

		closeProgressDialog ();

		InputMethodManager imm = (InputMethodManager)PlayUpActivity.context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(PlayUpActivity.mBinder, 0);
		imm = null;

		
		isHomeTapped = true;
	}


	// setting the listeners
	private void setListeners() {

		postButton.setEnabled( true );
		postButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				int result = userTextMessage.getText().toString().trim().length();
				int result1 = userTextMessage.getText().toString().length();

				if (event.getAction() == MotionEvent.ACTION_DOWN) {

					if (result != 0 && result1 > 0 && result1 <= 140) {
						postButton.setBackgroundResource(R.drawable.generic_down_button);
					}
				} else if (event.getAction() == MotionEvent.ACTION_CANCEL
						|| event.getAction() == MotionEvent.ACTION_OUTSIDE
						|| event.getAction() == MotionEvent.ACTION_UP) {

					if (result != 0 && result1 > 0 && result1 <= 140) {
						postButton.setBackgroundResource(R.drawable.generic_active_button);
					} else {
						postButton.setBackgroundResource(R.drawable.generic_inactive_button);
					}
				}

				if(event.getAction() == MotionEvent.ACTION_UP && result != 0 && result1 > 0 && result1 <= 140 ){//POST THE MESSAGE

					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					if ( dbUtil.isUserAnnonymous() ) {

						callProvider();

					} else {

						try {

							v.setEnabled( false );
							showProgressDialog();
							//							InputMethodManager inputManager = (InputMethodManager)      PlayUpActivity.context.getSystemService( PlayUpActivity.context.INPUT_METHOD_SERVICE); 
							//							inputManager.hideSoftInputFromWindow( userTextMessage.getWindowToken(),  InputMethodManager.HIDE_NOT_ALWAYS);

							JSONObject mJsonObject = new JSONObject();


							JSONArray broadcast_providers_arr = new JSONArray();

							Hashtable< String , List < String > > data = dbUtil.getProviders();
							if ( data != null &&  data.get( "vProviderName" ).size() > 0 ) {

								int len = data.get( "vProviderName" ).size();
								for ( int i= 0; i < len; i ++)  {
									if ( dbUtil.isIdentifierEnabledProviderName( data.get( "vProviderName" ).get( i ) ) ) {

										broadcast_providers_arr.put( data.get( "vProviderName" ).get( i ) );
									}

								}
							}

							if ( data != null ) {
								data.clear();
								data = null;
							}
							if ( broadcast_providers_arr.length() == 0 ) {
								mJsonObject.put(":type", "application/vnd.playup.message.text+json");
								mJsonObject.put("message",userTextMessage.getText().toString() );
								requestData = mJsonObject.toString();
								postMessage();	
							} else {

								mJsonObject.put(":type", "application/vnd.playup.message.text+json");
								mJsonObject.put("message",userTextMessage.getText().toString() );

								JSONObject jObj = new JSONObject();
								jObj.put( ":type", "application/vnd.playup.message.envelope+json" );
								jObj.put( "body", mJsonObject );

								jObj.put( "broadcast_providers", broadcast_providers_arr );

								requestData = jObj.toString();
								postMessage();
							}

						}catch (JSONException e) {

						}	

					}
					dbUtil = null;


				}
				return true;
			}

		});

	}

	// get the values of the last posted message in the room from database and set them in views
	private void setValues() {
		try{
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT vConversationName FROM " +
				"friendConversation WHERE vConversationId = \"" + vConversationId + "\" ");
		if ( c != null ) {

			if ( c.getCount() > 0 ) {


				c.moveToFirst();

				HashMap< String, String > map = new HashMap<String, String>();
				map.put( "vConversationName", c.getString( c.getColumnIndex( "vConversationName" ) ) );
				Message msg = new Message ();
				msg.obj = map;
				PlayupLiveApplication.getFragmentManagerUtil().updateTopBarFragment( msg );
			}
			c.close();
			c = null;

		}

		postButton.setBackgroundResource(R.drawable.generic_inactive_button);

		// getting the image for the avatar
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		imageDownloader.download( dbUtil.getUserId()   , dbUtil.getUserAvatarUrl(), blank_avatar_img, true, null );
		dbUtil = null;

		// setting the previos latest post data 
		data =  PlayupLiveApplication.getDatabaseWrapper().select( "SELECT vMessage, vSelfUrl,vHrefUrl, vUserId, vDisplayName, vAvatarUrl, vCreatedDate " +
				"FROM friendMessage fm " +
				"LEFT JOIN friendConversationMessage fcm ON fm.vConversationMessageId = fcm.vConversationMessageId " +
				"LEFT JOIN friendConversation fc ON fc.vConversationId = fcm.vConversationId " +
				"WHERE fc.vConversationId = \"" + vConversationId + "\" " +
		"ORDER BY vCreatedDate DESC LIMIT 0, 1");

		if ( data != null && data.get("vMessage").size() > 0 ) {
			mainLayout.setVisibility( View.VISIBLE );

			userName.setText( new Util().getSmiledText( data.get( "vDisplayName" ).get(0)));
			userMsg.setText( new Util().getSmiledText( data.get( "vMessage" ).get(0 ).replaceAll("(\\r|\\n|\\t)", " ")));
			userTimestamp.setText( new DateUtil().gmt_to_local_timezone( data.get("vCreatedDate").get( 0 ) )  );

			imageDownloader.download(  data.get("vAvatarUrl").get( 0 ), userAvatar, true, null );
		} else {
			mainLayout.setVisibility( View.GONE );
		}

		final TextWatcher txwatcher = new TextWatcher() {
			int result = 0, result1 =0;

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {

				if ( userTextMessage ==  null ) {
					return;
				}
				try{
					result = userTextMessage.getText().toString().trim().length();
					result1 = userTextMessage.getText().toString().length();
				
					if (result !=0 && result1 >0 && result1 <= 140) {
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
					Logs.show( e );
				}

			}


			public void afterTextChanged(Editable s) {
			}


		};


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
								Logs.show ( e );
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
								Logs.show ( e );
							}
						}
					}, 2000);

				}



				//	}
			}
		});

		setProviders ();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show ( e );
		}
	}

	
	/**
	 * setting the name and color in the topbar
	 */
	private void setTopBar () {

	
				Bundle b = new Bundle();
				b.putString("vMainColor",vMainColor );
				b.putString("vMainTitleColor",vMainTitleColor );
				Message msg = new Message ();
				msg.setData(b);
				
				PlayupLiveApplication.callUpdateTopBarFragments(msg);
			
	}
	

	/**
	 * Setting the providers 
	 */
	private void setProviders () {
		try{
		postprovider.removeAllViews();

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		Hashtable< String , List < String > > data = dbUtil.getProviders();
		LayoutParams params = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);

		if ( data != null && data.get( "vProviderName" ).size() > 0 ) {
			int len = data.get( "vProviderName" ).size();

			for ( int i = 0; i < len ; i++ ) {

				ImageView imgView = new ImageView( PlayUpActivity.context );

				postprovider.addView ( imgView, params );

				imgView.setPadding( 8, 8, 8, 8);
				imgView.setTag( data.get("vProviderName").get( i ) );
				imgView.setOnClickListener( providerClickListener );

				if ( dbUtil.isIdentifierEnabledProviderName( data.get( "vProviderName" ).get( i ) ) ) {
					imageDownloader.download(  data.get( "vIconBroadcastUrl" ).get( i ),  imgView, false,null  );
					imageDownloader.download( data.get( "vIconBroadcastHighLightUrl" ).get( i ),  null, false ,null  );
				} else {
					imageDownloader.download( data.get( "vIconBroadcastHighLightUrl" ).get( i ),  imgView, false ,null  );
					imageDownloader.download(  data.get( "vIconBroadcastUrl" ).get( i ),  null, false, null  );
				}
			}
		}
		if ( data != null ){
			data.clear();
			data = null;
		}
		dbUtil = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show ( e );
		}

	}

	private OnClickListener providerClickListener = new OnClickListener( ) {

		@Override
		public void onClick(View v) {
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();

			String vProviderName = (String) v.getTag();
			if ( !dbUtil.isIdentifierEnabledProviderName( vProviderName ) )  {

				if ( dbUtil.isIdentifierProviderName ( vProviderName ) ) {

					dbUtil.setIdentifierEnabledProviderName ( vProviderName, 1 );
					String vIconUrl = dbUtil.getProviderIconBroadcastUrl( vProviderName );
					imageDownloader.download(  vIconUrl,  ( ( ImageView ) v ), false,null );

					return;
				}
				// shows that user is logged in via facebook so ask user to sign in from twitter to use 
				Hashtable < String, List < String > > data = dbUtil.getProviderByName( vProviderName );

				if ( data != null && data.get("vLoginUrl").size() > 0 ) {
					String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
					Bundle bundle = new Bundle ();

					bundle.putString( "vProviderName", vProviderName );
					bundle.putString("vLoginUrl", data.get("vLoginUrl").get(0)  );
					bundle.putString("vSuccessUrl", data.get("vSuccessUrl") .get(0));
					bundle.putString("vFailureUrl", data.get("vFailureUrl") .get(0));
					bundle.putString("fromFragment", topFragmentName);

					PlayupLiveApplication.getFragmentManagerUtil().setFragment ( "LoginWebViewFragment", bundle);

				}
				if ( data != null) {
					data.clear();
					data = null;
				}
			} else {

				dbUtil.setIdentifierEnabledProviderName(vProviderName, 0 );

				String vSelIconUrl = dbUtil.getProviderSelIconBroadcastUrl( vProviderName );
				imageDownloader.download( vSelIconUrl,  ( ( ImageView ) v ), false ,null);
			}

			dbUtil = null;
		}
	};



	/**
	 * refresh on home key press
	 */


	private void refresh() {
		// userTextMessage.setText("");
		setValues();

		if (!isHomeTapped ) {
			if( vFriendName!=null && vFriendName.length()>0 ) {
				userTextMessage.setText("@"+vFriendName+" ");
				userTextMessage.setSelection(userTextMessage.getText().toString().length());
			} else {
				userTextMessage.setText("");
			}

			//PlayUpActivity.isHomePressed=false;
		}else if( userTextMessage.getText().toString().trim().length() > 0 &&
				userTextMessage.getText().toString().trim().length() <= 140){

			postButton.setBackgroundResource(R.drawable.generic_active_button);
		}




	}



	/**
	 * posting a message to the server. 
	 */
	private void postMessage() {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				try {
					
					final String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
					// Request to Base URL
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					/**
					 * 
					 * Check To See Whether already User Exists In Local DB
					 * 
					 * 
					 */
					String token = dbUtil.getUserToken();
					if (token != null || token.length() > 1) {
						
						if(Constants.isFetchingCredentials){
							
								return;
							
						}


						Hashtable<String, Object> result = dbUtil.getLobbyConversationMessagesUrl( vConversationId );
						String vConversationMessagesUrl = (String) result.get("url");
						Boolean isHref = (Boolean) result.get("isHref");

						HttpRequest request = null;
						String encodedHrefURL	= null;
						String vTokenValue		= 	null;
						
						if(isHref){
							
							int tokenType = new Util().checkForHrefTokenType(vConversationMessagesUrl);
							 vTokenValue = new Util().checkForHrefTokenParam(vConversationMessagesUrl); 
							 encodedHrefURL = new Util().getPersonalizedEnocodedURL(vConversationMessagesUrl,vTokenValue,tokenType);
							
							request = new HttpRequest( vConversationMessagesUrl,encodedHrefURL,isHref, requestData, Constants.POST_METHOD,Types.MESSAGE_POST_TYPE );
						}else{
							
							request = new HttpRequest( vConversationMessagesUrl, requestData, Constants.POST_METHOD,Types.MESSAGE_POST_TYPE );
							
						}

						


						try {

							// getting the response
							StringBuffer strBuffer = (StringBuffer) request.send();
							
							

							if ( request.getStatusCode() == 401 ) {
								
								postButton.setEnabled( true );
								
								JSONObject json = new JSONObject(strBuffer.toString());
								if(json.getString(":type") != null && json.getString(":type").trim().length() > 0 && 
										json.getString(":type").equalsIgnoreCase(Types.CREDENTIAL_TYPE)){
								
									Util.callTheCredentialsUrl(strBuffer);
								}else{
								
									callProvider ();
								}
								 
								
								
								
								
								
								return;
							} else {
								if ( strBuffer != null ) {

									// parse and save in database.

									PostMessage mPostMessage = new PostMessage(strBuffer.toString());
									if ( mPostMessage.getStatusCode() == Constants.SUCCESS_POST ) {

										/**
										 * 
										 * If Message Posting is a success. We should hit the conversationURL. To Get the latest messages.
										 * 
										 */
										if(vConversationId!=null) {
										//	getConversationMessages();
										}
										
										if ( PlayUpActivity.handler != null ) {
											PlayUpActivity.handler.post(new Runnable() {
												public void run() {

													try {
														closeProgressDialog();

														//PlayupLiveApplication.showToast( R.string.message_posted );
														PlayupLiveApplication.getFragmentManagerUtil().popBackStack(topFragmentName);
													} catch (Exception e) {
														// TODO Auto-generated catch block
														Logs.show(e);
														
													}

												}
											});

										}



									} else {

										if ( PlayUpActivity.handler != null ) {
											PlayUpActivity.handler.post(new Runnable() {
												public void run() {


													try {
														//Redirect User To Login
														postButton.setEnabled( true );
														callProvider ();
													} catch (Exception e) {
														// TODO Auto-generated catch block
														Logs.show(e);
													}

												}
											});

										}

									}

								} else {

									if ( PlayUpActivity.handler != null ) {
										PlayUpActivity.handler.post(new Runnable() {
											public void run() {


												try {
													postButton.setEnabled( true );
													closeProgressDialog ();
													// PlayupLiveApplication.showToast( R.string.error_message_posted );
												} catch (Exception e) {
													// TODO Auto-generated catch block
													Logs.show(e);
												}

											}
										});
									}


								}

							}

							request = null;
						} catch (RequestRepeatException e) {

						}	

					} else {

					}
					dbUtil = null;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show ( e );
				}
			}

		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

	}



	// calling the provider fragment .
	private void callProvider () {

		if ( PlayUpActivity.handler != null ) {
			PlayUpActivity.handler.post(new Runnable() {
				public void run() {

					try {
						closeProgressDialog();
						
						final String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
						Bundle bundle = new Bundle ();
						bundle.putString( "fromFragment", topFragmentName);
						PlayupLiveApplication.getFragmentManagerUtil().setFragment( "ProviderFragment", bundle );
					} catch (Exception e) {
						
						Logs.show ( e );
					}

				}
			});

		}

	}



	



}