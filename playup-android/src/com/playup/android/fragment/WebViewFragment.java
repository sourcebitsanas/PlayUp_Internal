package com.playup.android.fragment;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;




import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.connection.methods.Crypto;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.MacCreation;
import com.playup.android.util.PreferenceManagerUtil;
import com.playup.android.util.Util;



public class WebViewFragment extends MainFragment {

	
	private WebView webView;
	
	
	private String url = null;

	private boolean isAgain = false;
	private String vMainColor = null;
	private String vMainTitleColor = null;

	
	private ProgressDialog progressDialog;
	private Boolean isHrefURL	= false;







	public void onStop () {
		super.onStop();


		try {

			if ( progressDialog !=null && progressDialog .isShowing() ) {

				progressDialog.dismiss();
			}
			
		//	Class.forName("android.webkit.WebView").getMethod("onDestroy", (Class[]) null).invoke(webView, (Object[]) null);
			if(webView!=null){
				//webView.destroy();
			}
		} catch  ( Exception e ) {
			Logs.show(e);
		} catch ( Error r ) {
			Logs.show ( r );
		}

	}





	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		
		RelativeLayout li = null;
		try {
			Log.e("123","IN ON CREATIVE VIEWWW============");
			li   =  ( RelativeLayout) inflater.inflate( R.layout.login_webview, null);

			//webView = new WebView( PlayUpActivity.context );
			//li.addView( webView );


			webView  =  ( WebView) li.findViewById( R.id.webview );

			webView.getSettings().setJavaScriptEnabled(true);
			webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
			
			
			try {
				webView.getSettings().setPluginState(PluginState.ON_DEMAND);
				
			//	webView.getSettings().setPluginsEnabled(true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e("234","setPluginState try catch"+e.getMessage());
				
			//	webView.getSettings().setPluginsEnabled(true);
			}
			
			
			
		
			
			
			
			
			webView.getSettings().setSupportMultipleWindows(true);			
			webView.setVerticalScrollBarEnabled(true);
			webView.setHorizontalScrollBarEnabled(true);
			webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
			webView.clearHistory();
			webView.clearFormData();
			webView.clearCache(true);
			
		

			webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
			

			if(!isAgain)
				setUrl(getArguments());
			
			
			setTopBar("");
			initialize();


		} catch ( Exception e ) {
			Logs.show ( e );
		}

		return li;
	}



	/**
	 * displaying the prgress dialog untill the content is loaded
	 */
	private void showProgressDialog () {

		try {
			if ( PlayUpActivity.context != null && Constants.isCurrent && WebViewFragment.this.getActivity() != null ) {
				if( !Util.isInternetAvailable() ) {
					return;
				}
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
		} catch  ( Exception e ) {
			Logs.show(e);
		} catch ( Error r ) {
			Logs.show ( r );
		}
	}

	@Override
	public void onAgainActivated(Bundle args) { 
		isAgain = true;

		setUrl( args );
	}

	/**
	 * setting the required params 
	 * @param bundle
	 */
	private void setUrl ( Bundle bundle ) {
		try{
			
			vMainColor 		= 	null;
			vMainTitleColor = 	null;
			isHrefURL		= 	false;
			
			if ( bundle != null && bundle.containsKey( "url") ) {
				url = bundle.getString( "url" );
				//Log.e("123", "Inside Webview----->>>>"+url);
			}if (bundle != null &&bundle.containsKey("vMainColor")) {
				vMainColor = bundle.getString("vMainColor");
			}if (bundle != null && bundle.containsKey("vMainTitleColor")) {
				vMainTitleColor = bundle.getString("vMainTitleColor");
			}
			if(bundle != null && bundle.containsKey("isHref")){
				isHrefURL 	= 	bundle.getBoolean("isHref");
			}
			




		}catch(Exception e){

			Log.e("123","123"+e.getMessage());

		}
	}

	/**
	 * initializing the webview and loading the webpage with the necessary headers
	 */
	private void initialize () {
		try{

			

			if ( !Util.isInternetAvailable() ) {

				PlayupLiveApplication.showToast( R.string.no_network );
				PlayupLiveApplication.getFragmentManagerUtil().popBackStackNotImmediate( "WebViewFragment");
				return;
			}


			if ( url != null ) {


				webView.setWebViewClient(new webViewClient());
			
				webView.setWebChromeClient(new WebChromeClient() {

					public void onProgressChanged(WebView view, int progress) {

						try{
							
							Log.e("123","Inside on progress changed >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
							
							if ( isVisible() ) {
								if ( progress < 100 ) {
									
									

									showProgressDialog ();
								} else {
									cancelProgress();

									if(webView!=null && webView.getVisibility()!=View.VISIBLE){

										webView.setVisibility(View.VISIBLE);
									}

								}

							}
						}catch (Exception e) {
							Logs.show(e);
						}
					}
					
					
					
					
					
					
					
					public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {
						try {
							
							
							callback.invoke(origin, true, false);
						} catch (Exception e) {
							Logs.show(e);
						}
					};
					
					public void onReceivedTitle(WebView view, String title) {
						
						setTopBar(title);
						
						
					};
					
					
					
					
				}
				
				
				);


				HashMap< String , String > map = new HashMap< String , String > (); 
				DatabaseUtil dbUtil = DatabaseUtil.getInstance();
				String header = dbUtil.getHeader(url);

			
			

					//map.put(  Constants.AUTHORIZATION_KEY, "PUToken " + token );
					map.put( Constants.ACCEPT_LANGUAGE_KEY, Constants.ACCEPT_LANGUAGE );
					//map.put( Constants.ACCEPT_ENCODING_KEY, Constants.ACCEPT_ENCODING );
					map.put( "Accept", header );
					map.put( Constants.CONNECTION_KEY, Constants.CONNECTION );

					String userRegion = new PreferenceManagerUtil().get(Constants.REGION_TOKEN, "");


					map.put (  Constants.API_VERSION_KEY, Constants.API_VERSION );
					map.put (  Constants.ACCEPT_LANGUAGE_KEY, Constants.ACCEPT_LANGUAGE );
					map.put (  Constants.ACCEPT_ENCODING_KEY, Constants.ACCEPT_ENCODING );
					map.put (  Constants.CONNECTION_KEY, Constants.CONNECTION );
					map.put (  Constants.REQUEST_GEO_TAG,userRegion);


					// URl can be href URL of normal 
					//Check if the URl contains the token element
					
					
					String vTokenValue = null;
					int tokenType = -1 ;
					if(isHrefURL){
						 tokenType = new Util().checkForHrefTokenType(url);
						 vTokenValue = new Util().checkForHrefTokenParam(url);
					}
					
					if(vTokenValue != null && vTokenValue.length() >0){
						//Do the process of replacing the encoded personalized value
						url = new Util().getPersonalizedEnocodedURL(url,vTokenValue,tokenType);
					}
					
					Hashtable<String, String> credentials = dbUtil.getCredentials();

					
					Crypto mCrypto = new Crypto();
					String vId = "";
					String vSecret = "";
					
					if(credentials != null && credentials.size() > 0){
						if(credentials.containsKey("vId") && credentials.get("vId") != null )
							vId = credentials.get("vId");
						if(credentials.containsKey("vSecret") && credentials.get("vSecret") != null )
						vSecret = credentials.get("vSecret");
						
					}
					




					//Log.e("123","url in webview >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>    "+url);
					if(vId != null && vSecret != null && vId.trim().length() > 0 && vSecret.trim().length() > 0){
						MacCreation mac = new MacCreation(vId,vSecret,url,Constants.GET,false);
						String authorizationHeader 	=	mac.getMacTokens();	
						
						//Add the MAC id as AUTHORIZATION_KEY
						if(authorizationHeader != null && authorizationHeader.trim().length() > 0)
							map.put (  Constants.AUTHORIZATION_KEY,authorizationHeader);


					
					}
					
					
					
					String apiKeySignature = mCrypto.createSignature(Constants.PLAYUP_API_KEY ,  url.toString());
					String apiKeyHeader = Constants.PLAYUP_API_KEY+ " " + apiKeySignature;

					map.put (Constants.PLAYUP_API_KEY_KEY,apiKeyHeader);

					


				


					
					Log.e("123", "on load url------"+url);
				webView.loadUrl(url, map);



			}
		}catch(Exception e){
			Log.e("123","123"+e.getMessage());
		}





	}


	/**
	 * canceling the progress once the data is loaded
	 */

	private void cancelProgress() {
		try {
			if ( isVisible() ) {


				if ( progressDialog != null ) {
					progressDialog.cancel();
					progressDialog = null;
				}	
			}

		}catch (Exception e) {
			Log.e("123","123"+e.getMessage());
		}
	}



	@Override
	public void onResume () {
		super.onResume();
		Log.e("123","IN ON Resume VIEWWW============");
		//webView.resumeTimers();
//	
//		setTopBar("");
//		initialize();
		
		
		try {
			if(webView != null){
				Class.forName("android.webkit.WebView").getMethod("onResume", (Class[]) null).invoke(webView, (Object[]) null);
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			Log.e("123","123"+e.getMessage());
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			Log.e("123","123"+e.getMessage());
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			Log.e("123","123"+e.getMessage());
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			Log.e("123","123"+e.getMessage());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e("123","123"+e.getMessage());
		}

	}
	/**
	 * setting the color,title of the topbar
	 * @param vTitle - value of '<title>' tag of the html page being loaded
	 */
	private void setTopBar(String vTitle) {
		
		
		
		HashMap<String, String> title = new HashMap<String, String>();
		title.put("vTitle", vTitle);

		Bundle b = new Bundle();
		b.putString("vMainColor",vMainColor );
		b.putString("vMainTitleColor",vMainTitleColor );
		Message msg = new Message ();
		msg.setData(b);
		
		msg.obj = title;
		PlayupLiveApplication.callUpdateTopBarFragments(msg);

	}


	@Override
	public void onPause() {
		super.onPause();
	/*	
		if(webView!=null){
			webView.stopLoading();
			webView.destroy();
			//webView = null ;
		}
*/
		
		
		if(progressDialog!=null){
			progressDialog.dismiss();
		}
		
		try {
			Log.e("123", "onPause==============>>>>>");
//			webView.pauseTimers();
//			webView.stopLoading();
			
			//incase of video/audio content,the playback has to be stopped on navigating back
			Class.forName("android.webkit.WebView").getMethod("onPause", (Class[]) null).invoke(webView, (Object[]) null);
//			//Class.forName("android.webkit.WebView").getMethod("onDestroy", (Class[]) null).invoke(webView, (Object[]) null);
//			webView.destroy();
			
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}                                                                          

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			if ( webView != null ) {
			
			webView.stopLoading();
				//Class.forName("android.webkit.WebView").getMethod("onP", (Class[]) null).invoke(webView, (Object[]) null);
				webView.destroy();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	@Override
	public void onUpdate(final Message msg) {

		super.onUpdate(msg);

		if(PlayUpActivity.handler != null){

			PlayUpActivity.handler.post(new Runnable() {

				@Override
				public void run() {
					try {
						if(msg != null && msg.obj != null &&  msg.obj.toString().
								equalsIgnoreCase("callChevron")){
							//incase of landscape webview,the orientation has to be changed back to portrait 
							if( PlayUpActivity.context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE  ) {
								PlayUpActivity.context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
							}
							//moving to the parent screen
							PlayupLiveApplication.getFragmentManagerUtil().popBackStackImmediate();
						} else if ( msg != null && msg.obj != null &&  msg.obj.toString().equalsIgnoreCase("handleBackButton") ) {
							//incase of landscape webview,the orientation has to be changed back to portrait 
							if( PlayUpActivity.context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE  ) {
								PlayUpActivity.context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
							}
							//moving to the previous screen/webpage
							if ( webView != null && webView.canGoBack() ) {
								webView.goBack();
							} else {
								PlayupLiveApplication.getFragmentManagerUtil().popBackStackImmediate();
							}
						}
					} catch (Exception e) {
						Logs.show(e);
					}
				}

			});
		}
	}

	/**
	 * 
	 * handling the errors while loading the webpages
	 *
	 */
	
	private class webViewClient extends WebViewClient {

		
		
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {


			return false;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {


			super.onPageStarted(view, url, favicon);

		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onPageFinished(view, url);
			
			Log.e("123","Paage Dowmloaded==================");
			setTopBar(view.getTitle());
		}

		
		@Override 
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);

			
			
			
			
			
			
			PlayupLiveApplication.showToast( description );
			PlayupLiveApplication.getFragmentManager().popBackStack();

		}
		
		
		
		
		



		@Override
		public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
			handler.proceed();
			PlayupLiveApplication.getFragmentManager().popBackStack();
		}

	}


}