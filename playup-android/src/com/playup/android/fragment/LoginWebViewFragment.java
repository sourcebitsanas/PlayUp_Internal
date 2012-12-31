package com.playup.android.fragment;


import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.FragmentManagerUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.MacCreation;
import com.playup.android.util.Util;


/***
 * Login Webview Fragment. 
 */
public class LoginWebViewFragment extends MainFragment {


	private WebView webView;
	private ProgressBar _progress;
	private String prev_url = null;

	private String login_url;
	private String success_url;
	private String failure_url;

	private String vMainColor = null;
	private String vMainTitleColor = null;

	// private int IS_DIALOG_HIDE=0;

	private boolean isSet = false;
	private String fromFragment = "MenuFragment";
	private TextView loading_text;
	private ProgressDialog progressDialog;

	private String vProviderName = null;
	private String vContestId;


	private boolean callFollowUp = false;
	private boolean inSuccess = false;

	private boolean isForSharing = false;
	private String vConversationId;
	private String vFriendName;

	public void onStop () {
		super.onStop();

		if ( inSuccess ) {
			callFollowUp = true;
		}
		
		if ( progressDialog !=null && progressDialog .isShowing() ) {
			progressDialog.dismiss();
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		RelativeLayout li = null;
		try {
			li   =  ( RelativeLayout) inflater.inflate( R.layout.login_webview, null);

			_progress = (ProgressBar) li.findViewById(R.id.progressbar );
			loading_text = (TextView)li.findViewById(R.id.loading_text);


			webView  =  ( WebView) li.findViewById( R.id.webview );
			webView.requestFocus( View.FOCUS_DOWN );

			// setting a few properties on wewbview
			webView.getSettings().setJavaScriptEnabled(true);
			webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
			webView.getSettings().setPluginsEnabled(false);
			webView.getSettings().setSupportMultipleWindows(false);
			webView.getSettings().setSupportZoom(false);
			webView.setVerticalScrollBarEnabled(true);
			webView.setHorizontalScrollBarEnabled(true);
			webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
			webView.clearHistory();
			webView.clearFormData();
			webView.clearCache(true);

			webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

			// clearing the cache
			CookieSyncManager.createInstance(  PlayUpActivity.context  );
			CookieManager.getInstance().removeAllCookie();

			initialize();

		} catch ( Exception e ) {
//			Logs.show ( e );
		}

		return li;
	}



	private void deleteFiles ( File file )  {
		try{
		if ( file.isDirectory() ) {

			File [ ] files = file.listFiles();

			for (File e : files) {
				deleteFiles( e );
				e = null;
			}
			files = null;
		} else {
			file.delete();
			file = null;
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
	}

	/**
	 * display progress dialog
	 */
	private void showProgressDialog () {

		if ( PlayUpActivity.context != null && Constants.isCurrent && LoginWebViewFragment.this.getActivity() != null ) {

			if(!isVisible()) {
				return;
			}
			if ( progressDialog == null ) {
				progressDialog = new ProgressDialog(  PlayUpActivity.context  );
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setMessage( PlayUpActivity.context.getResources().getString(R.string.loading));
				progressDialog.setCancelable(true);
				progressDialog.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
					}
				});
			} 
			progressDialog.show();

		}
	}

	@Override
	public void onAgainActivated(Bundle args) { 
		isSet = true;
		setUrls( args );
		setFromFragment( args );
	}


	/**
	 * getting data sent from previous fragment
	 * @param bundle
	 */
	private void setFromFragment ( Bundle bundle ) {
		try{
			if ( bundle != null && bundle.containsKey( "fromFragment") ) {
				fromFragment = bundle.getString( "fromFragment" );
			}

			if ( bundle != null && bundle.containsKey( "sharing") ) {
				isForSharing = bundle.getBoolean( "sharing" );
			} 

			if(fromFragment != null && fromFragment.trim().length() > 0 && fromFragment.contains("MatchRoomFragment")){
				vContestId = bundle.getString("vContestId");
			}if (bundle.containsKey("vConversationId") && bundle.getString("vConversationId") != null) {
				vConversationId = bundle.getString("vConversationId");

			}else{
				vConversationId = null;

			}if (bundle.containsKey("vFriendName") && bundle.getString("vFriendName") != null) {
				vFriendName = bundle.getString("vFriendName");

			}else{
				vFriendName = null;

			}
			
			vMainColor = null;
			 vMainTitleColor = null;
			
			if (bundle != null && bundle.containsKey("vMainColor")) {
				vMainColor = bundle.getString("vMainColor");
			}if (bundle != null && bundle.containsKey("vMainTitleColor")) {
				vMainTitleColor = bundle.getString("vMainTitleColor");
			}
			
			
		}catch(Exception e){

//			Logs.show(e);

		}
	}
	/**
	 * getting the urls from the bundle and setting it  
	 */
	private void setUrls ( Bundle bundle ) {
		// getting the login url from the bundle ( provider screen fragment)
		try{
		
		if ( bundle != null && bundle.containsKey( "vLoginUrl" ) ) {
			String vLoginUrl = bundle.getString("vLoginUrl");

			if ( vLoginUrl != null && vLoginUrl.trim().length() > 0 ) {
				login_url = vLoginUrl;
				prev_url  = vLoginUrl;
			}
			if ( bundle.containsKey( "vProviderName" ) ) {
				vProviderName = bundle.getString( "vProviderName");
			}
			success_url = bundle.getString( "vSuccessUrl" );
			failure_url = bundle.getString( "vFailureUrl" );
		} else {
			success_url = null;
			failure_url = null;
			prev_url = null;
		}
		
		bundle = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
	}

	
	/**
	 * initialize views
	 */
	private void initialize () {
		try{
			// checking for the net connectivity
			if ( !Util.isInternetAvailable() ) {

				PlayupLiveApplication.showToast( R.string.no_network );
				PlayupLiveApplication.getFragmentManagerUtil().popBackStackNotImmediate( "LoginWebViewFragment");
				return;
			}

			// getting the login url from the bundle ( provider screen fragment)
			if ( !isSet ) {
				setUrls(getArguments()); 
				setFromFragment ( getArguments() );
			}

			if ( prev_url != null ) {

				webView.getSettings().setJavaScriptEnabled(true);

				webView.setWebViewClient(new LoginWebViewClient());

				webView.setWebChromeClient(new WebChromeClient() {

					public void onProgressChanged(WebView view, int progress) {

						try{
							if ( isVisible() ) {
								if ( progress < 100 ) {
									showProgressDialog ();
								} else {
									cancelProgress();

									if(webView != null && webView.getVisibility() != View.VISIBLE)
										webView.setVisibility(View.VISIBLE);
								}

							}
						}catch (Exception e) {

						}
					}
				});



				HashMap< String , String > map = new HashMap (); 
				DatabaseUtil dbUtil = DatabaseUtil.getInstance();
//				String token = dbUtil.getUserToken();
//				dbUtil = null;
//
//				if ( token != null && token.trim().length() > 0 ) {
//					map.put(  Constants.AUTHORIZATION_KEY, "PUToken " + token );
//
//				}
				
				Hashtable<String, String> credentials = dbUtil.getCredentials();
				
				String vId = "";
				String vSecret = "";
				
				if(credentials != null && credentials.size() > 0){
					if(credentials.containsKey("vId") && credentials.get("vId") != null )
						vId = credentials.get("vId");
					if(credentials.containsKey("vSecret") && credentials.get("vId") != null )
					vSecret = credentials.get("vSecret");
					
				}
				
				if(vId != null && vSecret != null && vId.trim().length() > 0 && vSecret.trim().length() > 0){
					MacCreation mac = new MacCreation(vId,vSecret,prev_url,Constants.GET,false);
					String authorizationHeader 	=	mac.getMacTokens();	
					
					//Add the MAC id as AUTHORIZATION_KEY
					if(authorizationHeader != null && authorizationHeader.trim().length() > 0)
						map.put(Constants.AUTHORIZATION_KEY,authorizationHeader);
				}
				
	 
				
				webView.loadUrl(prev_url, map);

			}
		}catch(Exception e){
//			Logs.show(e);
		}



	}


/**
 * cancel progress dialog
 */
	private void cancelProgress() {
		try {
			if ( isVisible() ) {
				if (_progress != null )
				{
					_progress.setVisibility(View.GONE);
					loading_text.setVisibility(View.GONE);
				}

				if ( progressDialog != null ) {
					progressDialog.cancel();
					progressDialog = null;
				}	
			}

		}catch (Exception e) {
//			Logs.show ( e );
		}
	}



	@Override
	public void onResume () {
		super.onResume();


		if ( callFollowUp ) {
			followUp();
		}

	}
	@Override
	public void onPause() {

		if(progressDialog!=null){
			progressDialog.dismiss();
		}

		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if ( webView != null ) {
			webView.stopLoading();
		}

		callFollowUp = false;
		inSuccess = false;

		/*Logs.show ( "getDatabasePath() ==============================================="+ webView.getSettings().getDatabasePath() );
		boolean webview = PlayupLiveApplication.getInstance().deleteDatabase( "webview.db" );
		boolean webviewCache = PlayupLiveApplication.getInstance().deleteDatabase( "webviewCache.db" );
		Logs.show ( "webview =============================================="+ webview );
		Logs.show ( "webviewCache =============================================="+ webviewCache );

		webView.clearCache(true);
		try {
			deleteFiles( new File ( Constants.DATABASE_PATH.replace( Constants.DATABASE_NAME, "webview.db")) );
			deleteFiles( new File ( Constants.DATABASE_PATH.replace( Constants.DATABASE_NAME, "webviewCache.db")) );

		} catch ( Exception e ) {
			Logs.show ( e );
		}*/
	}




/**
 * called from onresume, to resume the login process if its been interrupted
 */
	private void followUp () {
		
		try{

		if ( fromFragment != null && fromFragment.equalsIgnoreCase( "PostMessageFragment" ) ) {
			PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( "PostMessageFragment" );

		} else if ( fromFragment != null && fromFragment.contains( "PrivateLobbyMessageFragment" ) ) {
			PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );
		} else if ( fromFragment != null &&  fromFragment.contains( "PublicProfileFragment" ) ) { 
			PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );
		} else if ( fromFragment != null &&  fromFragment.contains( "MatchHomeFragment" ) ) {

			FragmentManagerUtil fragmentManagerUtil = PlayupLiveApplication.getFragmentManagerUtil();
			if ( fragmentManagerUtil.fragmentMap != null && fragmentManagerUtil.fragmentMap.containsKey( fromFragment ) ) {

				MatchHomeFragment fragment = (MatchHomeFragment) fragmentManagerUtil.fragmentMap.get( fromFragment );
				if ( fragment  != null ) {
					Message msg = new Message ();
					msg.obj = "share";
					fragment.onUpdate(msg);
				}
			}

			PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );
		}  else if ( fromFragment != null &&  fromFragment.equalsIgnoreCase( "MyProfileFragment" ) ) {
			PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill ( "MyProfileFragment");
		} else if ( fromFragment != null && fromFragment.contains( "MatchRoomFragment" ) ) {
			PlayupLiveApplication.getFragmentManagerUtil().removeFragment( "LoginWebViewFragment" );
			PlayupLiveApplication.getFragmentManagerUtil().removeFragment( "ProviderFragment" );
			PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );


			FragmentManagerUtil fragmentManagerUtil = PlayupLiveApplication.getFragmentManagerUtil();
			if ( fragmentManagerUtil.fragmentMap != null && fragmentManagerUtil.fragmentMap.containsKey( fromFragment ) ) {

				MatchRoomFragment fragment = (MatchRoomFragment) fragmentManagerUtil.fragmentMap.get( fromFragment );
				if ( fragment  != null ) {
					Message msg = new Message ();
					if( isForSharing )
						msg.obj = "share";
					else
						msg.obj = "callDirectCreateRoom";
					fragment.onUpdate(msg);
				}
			}


		} else if ( fromFragment != null && fromFragment.contains( "FriendsFragment" ) ) {
			PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );
		}  else if ( fromFragment != null && fromFragment.contains( "PrivateLobbyFragment" ) ) {
			PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );

			if ( PlayUpActivity.handler != null ) {
				PlayUpActivity.handler.postDelayed( new Runnable () {

					@Override
					public void run() {

						try {
							if ( Constants.isCurrent ) {
								Message msg = new Message ();
								if( isForSharing )
									msg.obj = "share";
								else
									msg.obj = "PlayUpLobby";
								PlayupLiveApplication.callUpdateOnFragments( msg );
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
//							Logs.show ( e );
						}
					}

				}, 500 );
			}
		} else if ( fromFragment != null && fromFragment.contains( "DirectConversationFragment" ) ) {
			PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( "MenuFragment" );
			PlayupLiveApplication.getFragmentManagerUtil().setFragment( fromFragment );
		} else if ( fromFragment != null && fromFragment.contains( "PrivateLobbyRoomFragment" ) ) {
			PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );


			FragmentManagerUtil fragmentManagerUtil = PlayupLiveApplication.getFragmentManagerUtil();
			if ( fragmentManagerUtil.fragmentMap != null && fragmentManagerUtil.fragmentMap.containsKey( fromFragment ) ) {

				PrivateLobbyRoomFragment fragment = ( PrivateLobbyRoomFragment ) fragmentManagerUtil.fragmentMap.get( fromFragment );
				if ( fragment  != null ) {
					Message msg = new Message ();
					msg.obj = "share";
					fragment.onUpdate(msg);
				}
			}

		} else {
			PlayupLiveApplication.getFragmentManagerUtil().popBackStack();

			// start the transaction 
			PlayupLiveApplication.getFragmentManagerUtil().startTransaction();

			PlayupLiveApplication.getFragmentManagerUtil().setFragment( "TopBarFragment", R.id.topbar  );
			PlayupLiveApplication.getFragmentManagerUtil().setFragment( "MenuFragment" );

			// end the transaction
			PlayupLiveApplication.getFragmentManagerUtil().endTransaction();
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
	}


	private class LoginWebViewClient extends WebViewClient {

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon){

			

			if(!isVisible()) {
				return;
			}
			//	url = "http://uat.fanbase.playupdev.com/auth/success?token=eAocszsLjx8mToY76T7z";
			if ( prev_url != null && prev_url.equalsIgnoreCase( url )) {
			} else {


				if ( success_url != null && url.contains(success_url)) {

					FlurryAgent.onEvent("signin");
					PlayUpActivity.runnableList.clear();
					PlayUpActivity.cancelTimers();

					PlayUpActivity.runnableList = new Hashtable<String, Boolean>();

					DatabaseUtil.getInstance().removeLobbySubject ();
					inSuccess = true;

					// download data from this url and stop webview

					// remove all the fragment from the stack;

					if ( fromFragment != null && fromFragment.equalsIgnoreCase( "PostMessageFragment" ) ) {
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						// call to update the profile to get the latest identifiers
						if ( vProviderName != null && vProviderName.trim().length() > 0 ) {
							dbUtil.setIdentifierEnabledProviderName( vProviderName,  1 );
						}
						/**
						 * this we have made as per mike feedback 
						 */
						Uri uri = Uri.parse(url);
						String token = uri. getQueryParameter("token"); 
						dbUtil.setUserToken(token, false );
						Constants.isLoggedIn	=	true;
						new Util().getProfileData();

						uri = null;
						dbUtil = null;

						if ( !Constants.isCurrent ) {
							return;
						}
						PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( "PostMessageFragment" );

					} else if ( fromFragment != null && fromFragment.contains( "PrivateLobbyMessageFragment" ) ) {
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						// call to update the profile to get the latest identifiers
						if ( vProviderName != null && vProviderName.trim().length() > 0 ) {
							dbUtil.setIdentifierEnabledProviderName( vProviderName,  1 );
						}
						/**
						 * this we have made as per mike feedback 
						 */
						Uri uri = Uri.parse(url);
						String token = uri. getQueryParameter("token"); 
						dbUtil.setUserToken(token, false );
						Constants.isLoggedIn	=	true;
						new Util().getProfileData();

						uri = null;
						dbUtil = null;
						if ( !Constants.isCurrent ) {
							return;
						}
						PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );

					} else if ( fromFragment != null &&  fromFragment.contains( "PublicProfileFragment" ) ) { 
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						/**
						 * this we have made as per mike feedback 
						 */
						Uri uri = Uri.parse(url);
						String token = uri. getQueryParameter("token"); 
						dbUtil.setUserToken(token, false );
						Constants.isLoggedIn	=	true;
						new Util().getProfileData();
						if ( !Constants.isCurrent ) {
							return;
						}
						PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );

						dbUtil = null;
						uri = null;

					} else if ( fromFragment != null &&  fromFragment.contains( "MatchHomeFragment" ) ) {
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						/**
						 * this we have made as per mike feedback 
						 */
						Uri uri = Uri.parse(url);
						String token = uri. getQueryParameter("token"); 
						dbUtil.setUserToken(token, false );

						Constants.isLoggedIn	=	true;
						new Util().getProfileData();

						if ( !Constants.isCurrent ) {
							return;
						}


						FragmentManagerUtil fragmentManagerUtil = PlayupLiveApplication.getFragmentManagerUtil();
						if ( fragmentManagerUtil.fragmentMap != null && fragmentManagerUtil.fragmentMap.containsKey( fromFragment ) ) {

							MatchHomeFragment fragment = ( MatchHomeFragment ) fragmentManagerUtil.fragmentMap.get( fromFragment );
							if ( fragment  != null ) {
								if(isForSharing){
									Message msg = new Message ();
									msg.obj = "share";
									fragment.onUpdate(msg);
									PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment ); 
									
									
									
								}else if(vConversationId != null && vConversationId.trim().length() > 0){
									Bundle b = new Bundle();
									b.putString("vConversationId",vConversationId);

									if(vFriendName != null && vFriendName.trim().length() > 0)
										b.putString("vFriendName",vFriendName);
									
									b.putString("fromFragment", fromFragment);
									b.putString("vMainColor",vMainColor );
									b.putString("vMainTitleColor",vMainTitleColor );
									
									
									

									PlayupLiveApplication.getFragmentManagerUtil().setFragment("PostMessageFragment", b);


								}else{
									PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment ); 

								}
							}
						}





						dbUtil = null;
						uri = null;

					}  else if ( fromFragment != null &&  fromFragment.equalsIgnoreCase( "MyProfileFragment" ) ) {
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();


						if ( dbUtil.isUserAnnonymous() ) {


							/**
							 * this we have made as per mike feedback 
							 */

							Uri uri = Uri.parse(url);
							String token = uri. getQueryParameter("token"); 

							uri = null;
							dbUtil.setUserToken(token, false );
							Constants.isLoggedIn	=	true;
							new Util().getProfileData();

							//util.getUserToken ( url, success_url );
						} else {


							new Util().getProfileData();
						}

						dbUtil = null;
						if ( !Constants.isCurrent ) {
							return;
						}
						PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill ( "MyProfileFragment");
					}

					else if ( fromFragment != null && fromFragment.contains( "MatchRoomFragment" ) ) {

						DatabaseUtil dbUtil = DatabaseUtil.getInstance();

						/**
						 * this we have made as per mike feedback 
						 */
						Uri uri = Uri.parse(url);
						String token = uri. getQueryParameter("token"); 

						dbUtil.setUserToken(token, false );
						Constants.isLoggedIn	=	true;
						new Util().getProfileData();

						if ( !Constants.isCurrent ) {
							return;
						}

						PlayupLiveApplication.getFragmentManagerUtil().removeFragment( "LoginWebViewFragment" );
						PlayupLiveApplication.getFragmentManagerUtil().removeFragment( "ProviderFragment" );
						PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );


						FragmentManagerUtil fragmentManagerUtil = PlayupLiveApplication.getFragmentManagerUtil();
						if ( fragmentManagerUtil.fragmentMap != null && fragmentManagerUtil.fragmentMap.containsKey( fromFragment ) ) {

							MatchRoomFragment fragment = ( MatchRoomFragment ) fragmentManagerUtil.fragmentMap.get( fromFragment );
							if ( fragment  != null ) {
								Message msg = new Message ();
								if( isForSharing )
									msg.obj = "share";
								else
									msg.obj = "callDirectCreateRoom";
								fragment.onUpdate(msg);
							}
						}

						dbUtil = null;
						uri = null;

					} else if ( fromFragment != null && fromFragment.contains( "FriendsFragment" ) ) {

						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						
						// url = url.replace( , "");
						Uri uri = Uri.parse(url);
						String token = uri. getQueryParameter("token"); 
						//dbUtil.setUserTokenLogin(token);
						dbUtil.setUserToken(token, false );
						Constants.isLoggedIn	=	true;
						new Util().getProfileData();
						if ( !Constants.isCurrent ) {
							return;
						}
						PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );

						dbUtil = null;
						uri = null;

					}  else if ( fromFragment != null && fromFragment.contains( "InviteFriendFragment" ) ) {

						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						
						// url = url.replace( , "");
						Uri uri = Uri.parse(url);
						String token = uri. getQueryParameter("token"); 
						//dbUtil.setUserTokenLogin(token);
						dbUtil.setUserToken(token, false );
						Constants.isLoggedIn	=	true;
						new Util().getProfileData();
						if ( !Constants.isCurrent ) {
							return;
						}
						PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );

						dbUtil = null;
						uri = null;

					}  else if ( fromFragment != null && fromFragment.contains( "PrivateLobbyInviteFriendFragment" ) ) {

						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						
						// url = url.replace( , "");
						Uri uri = Uri.parse(url);
						String token = uri. getQueryParameter("token"); 
						//dbUtil.setUserTokenLogin(token);
						dbUtil.setUserToken(token, false );
						Constants.isLoggedIn	=	true;
						new Util().getProfileData();
						if ( !Constants.isCurrent ) {
							return;
						}
						PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );

						dbUtil = null;
						uri = null;

					} else if ( fromFragment != null && fromFragment.contains( "PrivateLobbyFragment" ) ) {

						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						
						// url = url.replace( , "");
						Uri uri = Uri.parse(url);
						String token = uri. getQueryParameter("token"); 
						//dbUtil.setUserTokenLogin(token);
						dbUtil.setUserToken(token, false );
						Constants.isLoggedIn	=	true;
						new Util().getProfileData();
						if ( !Constants.isCurrent ) {
							return;
						}
						PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );

						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.postDelayed( new Runnable () {

								@Override
								public void run() {

									try {
										if ( Constants.isCurrent ) {
											Message msg = new Message ();
											if( isForSharing )
												msg.obj = "share";
											else
												msg.obj = "PlayUpLobby";
											PlayupLiveApplication.callUpdateOnFragments( msg );
										}
									} catch (Exception e) {
										// TODO Auto-generated catch block
//										Logs.show ( e );
									}
								}

							}, 500 );
						}
						dbUtil = null;
						uri = null;

					}  else if ( fromFragment != null && fromFragment.contains( "DirectConversationFragment" ) ) {

						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						
						// url = url.replace( , "");
						Uri uri = Uri.parse(url);
						String token = uri. getQueryParameter("token"); 
						//dbUtil.setUserTokenLogin(token);
						dbUtil.setUserToken(token, false );
						Constants.isLoggedIn	=	true;
						new Util().getProfileData();

						if ( !Constants.isCurrent ) {
							return;
						}
						PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( "MenuFragment" );
						PlayupLiveApplication.getFragmentManagerUtil().setFragment( fromFragment );
						dbUtil = null;
						uri = null;

					} else if ( fromFragment != null && fromFragment.contains( "PrivateLobbyRoomFragment" ) ) {

						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						
						// url = url.replace( , "");
						Uri uri = Uri.parse(url);
						String token = uri. getQueryParameter("token"); 
						//dbUtil.setUserTokenLogin(token);
						dbUtil.setUserToken(token, false );
						Constants.isLoggedIn	=	true;
						new Util().getProfileData();
						if ( !Constants.isCurrent ) {
							return;
						}

						FragmentManagerUtil fragmentManagerUtil = PlayupLiveApplication.getFragmentManagerUtil();
						if ( fragmentManagerUtil.fragmentMap != null && fragmentManagerUtil.fragmentMap.containsKey( fromFragment ) ) {

							PrivateLobbyRoomFragment fragment = ( PrivateLobbyRoomFragment ) fragmentManagerUtil.fragmentMap.get( fromFragment );
							if ( fragment  != null ) {
								Message msg = new Message ();
								msg.obj = "share";
								fragment.onUpdate(msg);
							}
						}

						PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );
						dbUtil = null;
						uri = null;

					} else {
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						/**
						 * this we have made as per mike feedback 
						 */
						Uri uri = Uri.parse(url);
						String token = uri. getQueryParameter("token"); 
						dbUtil.setUserToken(token, false );
						Constants.isLoggedIn	=	true;
						new Util().getProfileData();
						if ( !Constants.isCurrent ) {
							return;
						}
						PlayupLiveApplication.getFragmentManagerUtil().popBackStack();

						// start the transaction 
						PlayupLiveApplication.getFragmentManagerUtil().startTransaction();

						PlayupLiveApplication.getFragmentManagerUtil().setFragment( "TopBarFragment", R.id.topbar  );
						PlayupLiveApplication.getFragmentManagerUtil().setFragment( "MenuFragment" );

						// end the transaction
						PlayupLiveApplication.getFragmentManagerUtil().endTransaction();

						


						dbUtil = null;
						uri = null;
					}



				} else if ( failure_url != null &&  url.equalsIgnoreCase( failure_url ) ) {

					inSuccess = true;

					if ( !Constants.isCurrent ) {
						return;
					}
					//PlayupLiveApplication.showToast(R.string.loginErrMsg);
					if ( fromFragment != null ) {
						PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill ( fromFragment );
					} else {

						PlayupLiveApplication.getFragmentManagerUtil().popBackStack();

						// start the transaction 
						PlayupLiveApplication.getFragmentManagerUtil().startTransaction();

						PlayupLiveApplication.getFragmentManagerUtil().setFragment( "TopBarFragment", R.id.topbar  );
						PlayupLiveApplication.getFragmentManagerUtil().setFragment( "MenuFragment" );

						// end the transaction
						PlayupLiveApplication.getFragmentManagerUtil().endTransaction();

					}
				}
			}
			super.onPageStarted(view, url, favicon);
		}


		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);


		}

		@Override 
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);

			
			
			PlayupLiveApplication.showToast( description );



			inSuccess = true;

			if ( !Constants.isCurrent ) {
				return;
			}
			//PlayupLiveApplication.showToast(R.string.loginErrMsg);
			if ( fromFragment != null ) {
				PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill ( fromFragment );
			} else {

				PlayupLiveApplication.getFragmentManagerUtil().popBackStack();

				// start the transaction 
				PlayupLiveApplication.getFragmentManagerUtil().startTransaction();

				PlayupLiveApplication.getFragmentManagerUtil().setFragment( "TopBarFragment", R.id.topbar  );
				PlayupLiveApplication.getFragmentManagerUtil().setFragment( "MenuFragment" );

				// end the transaction
				PlayupLiveApplication.getFragmentManagerUtil().endTransaction();

			}


		}


		@Override
		public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
			handler.proceed();

			inSuccess = true;


			if ( !Constants.isCurrent ) {
				return;
			}
			//PlayupLiveApplication.showToast(R.string.loginErrMsg);
			if ( fromFragment != null ) {
				PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill ( fromFragment );
			} else {

				PlayupLiveApplication.getFragmentManagerUtil().popBackStack();

				// start the transaction 
				PlayupLiveApplication.getFragmentManagerUtil().startTransaction();

				PlayupLiveApplication.getFragmentManagerUtil().setFragment( "TopBarFragment", R.id.topbar  );
				PlayupLiveApplication.getFragmentManagerUtil().setFragment( "MenuFragment" );

				// end the transaction
				PlayupLiveApplication.getFragmentManagerUtil().endTransaction();

			}

		}
	}


}