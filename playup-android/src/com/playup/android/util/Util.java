package com.playup.android.util;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.bool;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;




import android.widget.TextView;
import android.widget.Toast;


import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.activity.VideoActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.connection.HttpRequest;
import com.playup.android.connection.methods.Crypto;
import com.playup.android.exception.RequestRepeatException;
import com.playup.android.fragment.PlayupFriendsFragment;
import com.playup.android.util.json.JsonUtil;
import com.playup.android.util.json.PostMessage;
import com.urbanairship.push.PushManager;
import com.urbanairship.push.PushPreferences;

public class Util {

	/**
	 * copies the data from inputstream into outputstream
	 * 
	 * @param InputStream
	 *            -- the inputstream from which data needs to be copied.
	 * @param OutputStream
	 *            -- the outputstream to which the data from inputstream needsto
	 *            be copied.
	 * @throws IOException
	 *             -- In case if error occured during copying of streams or
	 *             accessibility.
	 */
	public static void copyStreams(InputStream is, OutputStream out) throws IOException {

		// the byte array with the size of byte which determines how much data
		try {
			// needs to be copied in 1 round.
			byte[] buffer = new byte[1024];

			int length;
			while ((length = is.read(buffer)) > 0) {

				// copying into outputstream
				out.write(buffer, 0, length);
			}


			// closing the output and input streams.
			out.flush();
			out.close();
			is.close();

			out = null;
			is = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show(e);
		}

	}

	/**
	 * To check the connection before starting download
	 */
	public static boolean isInternetAvailable() {

		try {
			// getting the instance of connectivity manager
			ConnectivityManager cm = (ConnectivityManager) PlayupLiveApplication
			.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);

			// checking for active network info
			NetworkInfo networkInfo = cm.getActiveNetworkInfo();
			if (networkInfo == null) {
				return false;
			} else {
				return networkInfo.isConnectedOrConnecting();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
		//Logs.show(e);
		}
		
		return false;
	}

	/**
	 * handling server not found.
	 */
	public boolean handleServerNotFound(final int statusCode) {

		try {
			// case where 404 err have to e handled.
			if (statusCode == 404) {
				//PlayupLiveApplication.showToast(R.string.server_not_found);
				FragmentManagerUtil fragmentManagerUtil = PlayupLiveApplication
				.getFragmentManagerUtil();

				if (fragmentManagerUtil != null) {

					fragmentManagerUtil.popBackStack(fragmentManagerUtil
							.getTopFragmentName());
				}
				return true;
			} else {
				if ( statusCode == 500 || statusCode == 501 || statusCode == 502 
						|| statusCode == 503 || statusCode == 504 || statusCode == 505 ) {
					PlayupLiveApplication.showToast(R.string.server_not_found);
					return true;
				}
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show(e);
		}
		return false;
	}

	/**
	 * calling the base url to get all the start up urls.
	 */
	public void getDataFromServer(final Boolean isCredentialsExpirationSecanrio) {		

		
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				if(Constants.isFetchingCredentials){
					
					return;
					
				}
				
				

				try {
					// Request to Base URL
					HttpRequest request = new HttpRequest ( Keys.ROOT_URL, null, null, Constants.GET_METHOD, null );
					
					// getting the response
					StringBuffer strBuffer = (StringBuffer) request.send();
					
					if(request.getStatusCode() == 401){
						callTheCredentialsUrl(strBuffer);
						return;
					}else
					
					// checking for server not found
					if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0 ) {
						
						JsonUtil json = new JsonUtil();
						json.setIsCredentialExpirationScenario(isCredentialsExpirationSecanrio);
						json.parse (strBuffer, Constants.TYPE_BASE_URL, false );
						json = null;
						if(isCredentialsExpirationSecanrio){
							
							
							if(PlayUpActivity.handler!=null){
								PlayUpActivity.handler.post(new Runnable() {
									
									@Override
									public void run() {
									
										try {
											new Util().callLaunchScreen();
										} catch (Exception e) {
											// TODO Auto-generated catch block
											//Logs.show(e);
										}
									}
								});
							}
						}
						
						if(strBuffer!=null) {
							strBuffer.setLength(0);
							
						}
					}
					

					request = null;
				} catch (RequestRepeatException e) {
				//	Logs.show ( e );
				}

				PlayupLiveApplication.callUpdateOnFragments(null);

			}

			//	}

		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

	}




	/**
	 * registering as an annonymous user
	 * */
	public void getProfileData() {
		
		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				
				
				try {
					
					if(Constants.isFetchingCredentials){
						return;
						
					}
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result = dbUtil.getProfileURLFromRootResource();
					String profileUrl = (String) result.get("url");
					Boolean isHrefURL = (Boolean) result.get("isHref");
					
					// request to get the token
					//HttpRequest request = new HttpRequest( profileUrl, Constants.GET_METHOD );
					HttpRequest request = null;
					String encodedHrefURL	= null;
					String vTokenValue		= 	null;
					//boolean isHref = checkForToken(vSectionUrl);
					
					try {
						if(isHrefURL){
							
							//encode the url and make a http request with both encoded and unencoded urls
						
							//encode the url and make a http request with both encoded and unencoded urls
							int tokenType = new Util().checkForHrefTokenType(profileUrl);
							 vTokenValue = new Util().checkForHrefTokenParam(profileUrl); 
							encodedHrefURL = new Util().getPersonalizedEnocodedURL(profileUrl,vTokenValue,tokenType);
							
							//String encodedurl = encode(vSectionUrl)
							//String unEncodedurl = vSectionUrl;
							
							request = new HttpRequest( /*unEncodedUrl,*/profileUrl,encodedHrefURL,isHrefURL,Constants.GET_METHOD );
							
						}else{
							
							 request = new HttpRequest( profileUrl,Constants.GET_METHOD );
							
						}
					}catch(Exception e){
						//Logs.show(e);
					}
					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						if(request.getStatusCode() == 401){
							callTheCredentialsUrl(strBuffer);
							
							return;
						}
						// handling 404
						else if (!handleServerNotFound(request.getStatusCode())
								&& strBuffer != null && strBuffer.toString().trim().length() > 0) {
							
							
							if(!Constants.isLoggedIn)
								DatabaseUtil.getInstance().setUserToken("",false);

							// parse and save in database.
							JsonUtil json = new JsonUtil();
							json.parse ( strBuffer, Constants.TYPE_PRIMARY_PROFILE_URL, false );
							
							if(strBuffer!=null)
								strBuffer.setLength(0);
						}
						
					} catch (RequestRepeatException e) {
						//Logs.show(e);
					}

					PlayupLiveApplication.callUpdateOnFragments(null);
				} catch (Exception e) {
					//Logs.show ( e );
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}


	/**
	 * registering as an annonymous user
	 * */
	public void getProfileData( final String profileUrl ) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				try {
					
					// request to get the token
					HttpRequest request = new HttpRequest( profileUrl, Constants.GET_METHOD );
					
					StringBuffer strBuffer = (StringBuffer) request.send();
					// handling 404
					if (!handleServerNotFound(request.getStatusCode())
							&& strBuffer != null && strBuffer.toString().trim().length() > 0) {

						// parse and save in database.
						JsonUtil json = new JsonUtil();
						json.parse ( strBuffer, Constants.TYPE_PRIMARY_PROFILE_URL, false );
					}
					if(strBuffer!=null)
						strBuffer.setLength(0);
				} catch (RequestRepeatException e) {

				}

				PlayupLiveApplication.callUpdateOnFragments(null);
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}






	/**
	 * registering as an annonymous user
	 * */
	public void signOut() {

		Runnable runnable = new Runnable() {
			int status = 0;
			@Override
			public void run() {

				try {
//					
//					if(Constants.isFetchingCredentials){
//						
//						Log.e("123", "Constants.isFetchingCredentials------"+Constants.isFetchingCredentials);
//						return;
//						
//					}
						
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result = dbUtil.getsignOutUrl();
					String signOutUrl =(String) result.get("url");
					Boolean isHrefURL =(Boolean) result.get("isHref");

					
					
					HttpRequest request = null;
					String encodedHrefURL	= null;
					String vTokenValue		= 	null;
					
					
					try {
						if(isHrefURL){
							
							
							//encode the url and make a http request with both encoded and unencoded urls
							int tokenType = new Util().checkForHrefTokenType(signOutUrl);
							 vTokenValue = new Util().checkForHrefTokenParam(signOutUrl); 
							 encodedHrefURL = new Util().getPersonalizedEnocodedURL(signOutUrl,vTokenValue,tokenType);
							
							request = new HttpRequest( signOutUrl,encodedHrefURL,isHrefURL,Constants.GET_METHOD );
							
						}else{
							
							 request = new HttpRequest( signOutUrl, Constants.GET_METHOD );
							
						}
					}catch(Exception e){
						//Logs.show(e);
					}
					Message msg = new Message ();
					if(signOutUrl!=null && signOutUrl.length()>0){


						// request to get the token
						//HttpRequest request = new HttpRequest( signOutUrl, Constants.GET_METHOD );
						try {
							StringBuffer strBuffer = (StringBuffer) request.send();
							if(request.getStatusCode()==401){
								callTheCredentialsUrl(strBuffer);
							
								return;
							}
							// handling 404
							if (!handleServerNotFound(request.getStatusCode())
									&& strBuffer != null && strBuffer.toString().trim().length() > 0) {


								try{
									// parse 
									JSONObject jsonObj 			= 	new JSONObject( strBuffer.toString() );
									String status 	=	jsonObj.optString("status");
									String message	=	jsonObj.optString("message");
									if(status!=null&&Integer.parseInt(status)==200){
										//Log.e("123","Inside Signout");
										//Call DB flush
										new Util().logout();
										
										msg.obj = "SignOut";


									}else{
										//Log.e("123", "Signout failure-------");
										msg.obj = "SignOutFailure";

									}

								}catch (Exception e) {
									// TODO: handle exception
									msg.obj = "SignOutFailure";

								}



							} else {
								if ( request.getStatusCode() == 404 ) {
									PlayupLiveApplication.showToast(R.string.internet_connectivity_dialog);
								}
								status = 1;
								msg.obj = "ConnectionFailure";
								msg.arg1 = status;
							}
							if(strBuffer!=null)
								strBuffer.setLength(0);
						} catch (RequestRepeatException e) {

							if ( request.getStatusCode() == 404 ) {
								PlayupLiveApplication.showToast(R.string.internet_connectivity_dialog);
							}
							msg.obj = "ConnectionFailure";
							status = 1;
							msg.arg1 = status;
						}

					}else{
						//Anonymous user No Network Call. Only Db flush is involved.
						msg.obj = "AnonymousSignOut";

					}

					// updating the UI
					PlayupLiveApplication.callUpdateOnFragments(msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}






	/**Chked
	 *  getting the conversation friends
	 * */
	public Runnable getConversationFriends( final String vConversationId,final Hashtable<String, Runnable> runnableList) {
		
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(Constants.GET_CONVERSATION_FRIENDS)){
							runnableList.remove(Constants.GET_CONVERSATION_FRIENDS);
						}
						
						return;
						
					}
						
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String vConversationFriendsUrl = null;
					HttpRequest request = null;
					Hashtable<String, Object> result = dbUtil.getConversationFriendsUrl ( vConversationId );
					
					if(result != null && result.containsKey("isHref")){
						
						vConversationFriendsUrl = (String) result.get("url");
						
						if((Boolean) result.get("isHref")){
							int tokenType = checkForHrefTokenType(vConversationFriendsUrl);
							String vTokenValue = checkForHrefTokenParam(vConversationFriendsUrl);
							
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								String encodedUrl = getPersonalizedEnocodedURL(vConversationFriendsUrl, vTokenValue,tokenType);
								request = new HttpRequest( vConversationFriendsUrl,encodedUrl,true, Constants.GET_METHOD );
								
							}else{
								request = new HttpRequest( vConversationFriendsUrl, Constants.GET_METHOD );
							}
							
						}else{
							request = new HttpRequest( vConversationFriendsUrl, Constants.GET_METHOD );
							
						}
						
					}else{
						return;
					}

					// request to get the token

					try {
						StringBuffer strBuffer = (StringBuffer) request.send();

						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);
								if(runnableList != null && runnableList.containsKey(Constants.GET_CONVERSATION_FRIENDS)){
							runnableList.remove(Constants.GET_CONVERSATION_FRIENDS);
						}
							return;
						}
						// handling 404
						else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

							JsonUtil json = new JsonUtil();
							json.setConversationId( vConversationId );
							json.parse(strBuffer, Constants.TYPE_CONVERSATION_FRIENDS_JSON, false );
							
							if(strBuffer!=null)
								strBuffer.setLength(0);

						}
						
					} catch (RequestRepeatException e) {
						//Logs.show(e);
					}

					if(runnableList!=null&&runnableList.containsKey(Constants.GET_CONVERSATION_FRIENDS)){
						runnableList.remove(Constants.GET_CONVERSATION_FRIENDS);

					}
					// updating the UI
					Message msg = new Message ();
					msg.obj = "MatchHomeFragment_friends";
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		return runnable;
	}

/** 
 * Chcked
 * @param vSectionUrl
 * @param isHrefURL
 * @param vCompetitionId
 * @param runnableList
 * @return
 */
	public Runnable getLeagueLobby( final String vSectionUrl,final boolean isHrefURL, final String vCompetitionId,final Hashtable<String, Runnable> runnableList ) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {


				try {
					if(Constants.isFetchingCredentials){
						if(runnableList != null&& runnableList.containsKey(vSectionUrl)){
							runnableList.remove(vSectionUrl);

						}
						
						return;
						
					}
					
					boolean isUpdateAvailable = false;
					
					HttpRequest request = null;
					String encodedHrefURL	= null;
					String vTokenValue		= 	null;
					//boolean isHref = checkForToken(vSectionUrl);
					
					try {
						if(isHrefURL){
							
							//encode the url and make a http request with both encoded and unencoded urls
							
							//encode the url and make a http request with both encoded and unencoded urls
							int tokenType = checkForHrefTokenType(vSectionUrl);
							 vTokenValue = new Util().checkForHrefTokenParam(vSectionUrl); 
							 encodedHrefURL = new Util().getPersonalizedEnocodedURL(vSectionUrl,vTokenValue,tokenType);
							//String encodedurl = encode(vSectionUrl)
							//String unEncodedurl = vSectionUrl;
							
							request = new HttpRequest( /*unEncodedUrl,*/vSectionUrl,encodedHrefURL,isHrefURL,Constants.GET_METHOD );
							
						}else{
							
							 request = new HttpRequest( vSectionUrl,Constants.GET_METHOD );
							
						}
						StringBuffer strBuffer = (StringBuffer) request.send();
						//Log.e("123", "League lobby response------"+strBuffer);
						
						if(request.getStatusCode() == 401){
							callTheCredentialsUrl(strBuffer);
							if(runnableList != null && runnableList.containsKey(vSectionUrl)){
								runnableList.remove(vSectionUrl);

							}
							
							return;
							
						}else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && 
								strBuffer.toString().trim().length() > 0) {

							isUpdateAvailable = true;
							JsonUtil json = new JsonUtil();
							json.setSectionCompetitionId(vCompetitionId);
							json.setIsFromGeoTag(false);
							json.parse(strBuffer, Constants.TYPE_SECTION, false );
							
							if(strBuffer!=null)
								strBuffer.setLength(0);


						}
						
					} catch (RequestRepeatException e) {
						//Logs.show(e);
					}
					if(runnableList != null && runnableList.containsKey(vSectionUrl)){
						runnableList.remove(vSectionUrl);

					}
						
					Message msg = new Message ();
					msg.obj = "Sectiondata";
					if(isUpdateAvailable)
						msg.arg1 = 1;
					else
						msg.arg1 = 0;
					
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}

			}

			
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		return runnable;
	}
	

	

/** Checked
 * 
 * @param vSectionUrl
 * @param isHrefURL
 * @param vCompetitionId
 * @param runnableList
 * @return
 */
	public Runnable getNewsData( final String vSectionUrl,final Boolean isHrefURL,final String vCompetitionId,final Hashtable<String, Runnable> runnableList ) {
		
		//Log.e("123", "getNewsData-----vSectionUrl"+vSectionUrl);
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(vSectionUrl)){
							runnableList.remove(vSectionUrl);
						}
						
						return;
					}
					
					boolean isUpdateAvailable = false;
					DatabaseUtil.getInstance().removeEtag(vSectionUrl);
					HttpRequest request = null;
					String encodedHrefURL	= null;
					String vTokenValue		= 	null;
					
					//boolean isHref = checkForToken(vSectionUrl);
					
					try {
						
						if(isHrefURL){
							
							//encode the url and make a http request with both encoded and unencoded urls
							int tokenType = checkForHrefTokenType(vSectionUrl);
							vTokenValue	=	new Util().checkForHrefTokenParam(vSectionUrl); 
							encodedHrefURL	=	new Util().getPersonalizedEnocodedURL(vSectionUrl,vTokenValue,tokenType);
							
							request = new HttpRequest( /*unEncodedUrl,encoded url*/vSectionUrl,encodedHrefURL,isHrefURL,Constants.GET_METHOD );
							
						}else{
							
							 request = new HttpRequest( vSectionUrl,Constants.GET_METHOD );
						}
						StringBuffer strBuffer = (StringBuffer) request.send();

						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);
							if(runnableList != null && runnableList.containsKey(vSectionUrl)){
								runnableList.remove(vSectionUrl);
							}
							return;
						}
						// handling 404
						else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

							isUpdateAvailable 	= 	true;
							JsonUtil json 		= 	new JsonUtil();
							
							json.setSectionCompetitionId(vCompetitionId);
							json.setIsFromGeoTag(false);
							
							json.parse(strBuffer, Constants.TYPE_SECTION, false );

							if(strBuffer!=null)
								strBuffer.setLength(0);
						}
						
					} catch (RequestRepeatException e) {
						//Logs.show(e);
					}
					if(runnableList!=null&&runnableList.containsKey(vSectionUrl)){
						runnableList.remove(vSectionUrl);

					}
					
					Message msg = new Message ();
					msg.obj = "Sectiondata";
					if(isUpdateAvailable)
						msg.arg1 = 1;
					else
						msg.arg1 = 0;
					
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		return runnable;
	}

/**
 * Chcked
 * @param vConversationId
 * @param runnableList
 * @return
 */

	public Runnable getPrivateLobbyConversationFriends( final String vConversationId,final Hashtable<String, Runnable> runnableList) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(Constants.GET_PRIVATE_CONVERSATION_FRIENDS)){
							runnableList.remove(Constants.GET_PRIVATE_CONVERSATION_FRIENDS);
						}
						
						return;
						
					}
						
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result = dbUtil.getPrivateLobbyFriendsUrl ( vConversationId );
					String vConversationFriendsUrl = (String) result.get("url");
					HttpRequest request = null;
					
					if(((Boolean) result.get("isHref")).booleanValue()){
						String vTokenValue = checkForHrefTokenParam(vConversationFriendsUrl);
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							int tokenType = checkForHrefTokenType(vConversationFriendsUrl);
							String encodedUrl = getPersonalizedEnocodedURL(vConversationFriendsUrl, vTokenValue,tokenType);
							request = new HttpRequest( vConversationFriendsUrl,encodedUrl,true, Constants.GET_METHOD );
						}else{
							request = new HttpRequest( vConversationFriendsUrl, Constants.GET_METHOD );
						}
					}else{
						request = new HttpRequest( vConversationFriendsUrl, Constants.GET_METHOD );
					}

					// request to get the token
					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();

						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);
							if(runnableList != null && runnableList.containsKey(Constants.GET_PRIVATE_CONVERSATION_FRIENDS)){
								runnableList.remove(Constants.GET_PRIVATE_CONVERSATION_FRIENDS);
							}
							return;
						}
						// handling 404
						else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {


							JsonUtil json = new JsonUtil();
							json.setConversationId( vConversationId );
							json.parse(strBuffer, Constants.TYPE_CONVERSATION_FRIENDS_JSON, false );
							
							if(strBuffer!=null)
								strBuffer.setLength(0);

						}
					
					} catch (RequestRepeatException e) {

					}

					if(runnableList!=null&&runnableList.containsKey(Constants.GET_PRIVATE_CONVERSATION_FRIENDS)){
						runnableList.remove(Constants.GET_PRIVATE_CONVERSATION_FRIENDS);

					}
					// updating the UI
					Message msg = new Message ();
					msg.obj = "PrivateLobbyFriends";
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		return runnable;
	}


	/**Checked
	 * Praveen : as per the href
	 */
	/**
	 * fetch welcome data
	 * @param vSectionUrl
	 * @param runnableList
	 * @return
	 */
	public Runnable getWelComeData ( final String vSectionUrl,final boolean isHref, final String vCompetitionId, final Hashtable < String, Runnable > runnableList, final boolean fromGeoTag ) {

		
		
		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				try {
					if(Constants.isFetchingCredentials){

						if ( runnableList != null && runnableList.containsKey ( vSectionUrl ) ) {
							runnableList.remove ( vSectionUrl );
						}
						
						return;
					}
					
					boolean isUpdateAvailable = false;
					HttpRequest request = null;
					
					if(isHref){

						//Log.e("123", "Welcomed data util in if");
						int tokenType = checkForHrefTokenType(vSectionUrl);

						String vTokenValue = checkForHrefTokenParam(vSectionUrl);
						
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							
							String encodedUrl  =getPersonalizedEnocodedURL(vSectionUrl, vTokenValue,tokenType);
							request = new HttpRequest( vSectionUrl,encodedUrl,true,Constants.GET_METHOD );
						}else{
							request = new HttpRequest( vSectionUrl,null,Constants.GET_METHOD );
						}
						
					}else{
						 request = new HttpRequest( vSectionUrl, Constants.GET_METHOD );
					}
					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();

						if(request.getStatusCode() == 401){
							
							callTheCredentialsUrl(strBuffer);
							if ( runnableList != null && runnableList.containsKey ( vSectionUrl ) ) {
								runnableList.remove ( vSectionUrl );
							}
							
							return;
	
						}else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && 
								strBuffer.toString().trim().length() > 0) {

							isUpdateAvailable = true;
							JsonUtil json = new JsonUtil();
							json.setSectionCompetitionId(vCompetitionId);
							json.setIsFromGeoTag(fromGeoTag);
							json.parse(strBuffer, Constants.TYPE_SECTION, false );
							
							
							if(strBuffer!=null)
								strBuffer.setLength(0);
						}
						
					} catch (RequestRepeatException e) {
						//Logs.show(e);

					}

					if ( runnableList != null && runnableList.containsKey ( vSectionUrl ) ) {
						runnableList.remove ( vSectionUrl );
					}


					Message msg = new Message ();
					msg.obj = "Sectiondata";
					if(isUpdateAvailable)
						msg.arg1 = 1;
					else
						msg.arg1 = 0;
					
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		return runnable;
	}


	/**
	 * Checked
	 * registering as an annonymous user
	 * */
	public void getNotificationData ( ) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
													
						return;
					}
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String notificationUrl = dbUtil.getNotificationURLFromRootResource();
					
					dbUtil = null;
					
					// request to get the token
					HttpRequest request = new HttpRequest( notificationUrl, Constants.GET_METHOD );
					try {
						notificationUrl	=	null;
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
							callTheCredentialsUrl(strBuffer);
							return;

						}
						// handling 404
						//notificationUrl = null;
						else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {
							// parse and save in database.
							JsonUtil json = new JsonUtil();
							json.parse ( strBuffer, Constants.TYPE_NOTIFICATION_URL, false );
						

						if ( strBuffer != null ) {
							strBuffer.setLength( 0 );
							strBuffer = null;
						}
					  }
					} catch (RequestRepeatException e) {

					}
					request = null;

					PlayupLiveApplication.callUpdateOnFragments(null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}
	
	/**
	 * Checked
	 */
	public void getRecentActivityData ( ) {
		

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					if(Constants.isFetchingCredentials){
						return;
					}
					
					boolean isUpdateAvailable = false;
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					
					
					Hashtable<String, Object> result = dbUtil.getRecentActivityUrl();
					String recentActivityUrl = (String) result.get("url");
					Boolean isHrefURL = (Boolean) result.get("isHref");

					// request to get the token	
					if(recentActivityUrl != null && recentActivityUrl.trim().length() > 0){

						if(PlayUpActivity.runnableList != null && !PlayUpActivity.runnableList.containsKey(Constants.GET_RECENT_ACTIVITY_DATA)){
							PlayUpActivity.runnableList.put(Constants.GET_RECENT_ACTIVITY_DATA,true);
						}
						HttpRequest request = null;
						String encodedHrefURL	= null;
						String vTokenValue		= 	null;
						
						if(isHrefURL){
							
							int tokenType = checkForHrefTokenType(recentActivityUrl);
							 vTokenValue = new Util().checkForHrefTokenParam(recentActivityUrl); 
							 encodedHrefURL = new Util().getPersonalizedEnocodedURL(recentActivityUrl,vTokenValue,tokenType);
							
							
							request = new HttpRequest( recentActivityUrl,encodedHrefURL,isHrefURL,Constants.GET_METHOD );
							
						}else{
							
							 request = new HttpRequest( recentActivityUrl,Constants.GET_METHOD );
							
						}
					
						try {

							StringBuffer strBuffer = (StringBuffer) request.send();
							recentActivityUrl = null;
							if(request.getStatusCode()==401){
								callTheCredentialsUrl(strBuffer);								
								return;
							}
							
							// handling 404
							else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {
								// parse and save in database.
								if(request.isAnyChangeInContent != Constants.NO_NEW_CONTENT_AVAIL){
									String iUserId = dbUtil.getPrimaryUserId();
									isUpdateAvailable = true;

									JsonUtil json = new JsonUtil();
									json.setUserId( iUserId ) ;
									json.parse(strBuffer, Constants.TYPE_RECENT_ACTIVITY_JSON, false );

								}
								
								if ( strBuffer != null ) {
									strBuffer.setLength(0);
									strBuffer = null;
								}
							}
							
						} catch (Exception e) {
							//Logs.show(e);
						}
						dbUtil = null;
						request = null;
						// updating the UI
						if(isUpdateAvailable)
							PlayupLiveApplication.callUpdateOnFragments(null);

						PlayUpActivity.refreshRecentActivity();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}



/**
 * Checked
 */
	public void getDirectConversationData ( ) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					
					
					if(Constants.isFetchingCredentials)
						return;
					boolean isUpdateAvailable = false;
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					
					Hashtable<String, Object> result = dbUtil.getDirectConversationUrl ();
					String vDirectConversationUrl ="";
					Boolean isHrefURL =false;
					if(result!=null && result.containsKey("isHref") && result.containsKey("url")){
						
						 vDirectConversationUrl = (String) result.get("url");
						 isHrefURL = (Boolean) result.get("isHref");
						
					}else{
						//Log.e("123","i am in else util getDirectConversationData");
						
					}
					
					
					//Log.e("123", "getDirectConversationData---->>>>"+vDirectConversationUrl);
					// request to get the token
					if(vDirectConversationUrl != null && vDirectConversationUrl.trim().length() > 0){

						if(PlayUpActivity.runnableList != null && !PlayUpActivity.runnableList.containsKey(Constants.GET_DIRECT_CONVERSATION_DATA)){

							PlayUpActivity.runnableList.put(Constants.GET_DIRECT_CONVERSATION_DATA,true);
						}

						
						HttpRequest request = null;
						String encodedHrefURL	= null;
						String vTokenValue		= 	null;
					
						
						try {
							if(isHrefURL){
								
								int tokenType = checkForHrefTokenType(vDirectConversationUrl);
								 vTokenValue = new Util().checkForHrefTokenParam(vDirectConversationUrl); 
								 encodedHrefURL = new Util().getPersonalizedEnocodedURL(vDirectConversationUrl,vTokenValue,tokenType);
								request = new HttpRequest( vDirectConversationUrl,encodedHrefURL,isHrefURL,Constants.GET_METHOD );
								
							}else{
								
								 request = new HttpRequest( vDirectConversationUrl,Constants.GET_METHOD );
								
							}
						
						try {

							StringBuffer strBuffer = (StringBuffer) request.send();
							vDirectConversationUrl = null;
							
							
							if(request.getStatusCode() == 401){
							
								callTheCredentialsUrl(strBuffer);
								return;
								
								
								
							}else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && 
									strBuffer.toString().trim().length() > 0) {
								// parse and save in database.
								if(request.isAnyChangeInContent != Constants.NO_NEW_CONTENT_AVAIL){
									String iUserId = dbUtil.getPrimaryUserId();
									isUpdateAvailable = true;
									
									// Praveen : Changed
									JsonUtil json = new JsonUtil();
									json.setUserId( iUserId  );
									json.parse(strBuffer, Constants.TYPE_DIRECT_CONVERSATION_JSON, false );
									json = null;
									
									
									
								}
								
								
								if ( strBuffer != null ) {
									strBuffer.setLength(0);
									strBuffer = null;
								}
							}
							
						} catch (Exception e) {
							//Logs.show( e );
						}
						dbUtil = null;
						request = null;

						// updating the UI
						if(isUpdateAvailable) {
							Message msg = new Message ();
							msg.obj = "DirectConversation";
							PlayupLiveApplication.callUpdateOnFragments( msg );
						} else {
							String topbarFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
							if ( topbarFragmentName != null && topbarFragmentName.contains( "DirectConversationFragment" ) ) {
								Message msg = new Message ();
								msg.obj = "DirectConversation_Downloaded";
								PlayupLiveApplication.callUpdateOnFragments( msg );
							}
						} 
						}catch(Exception e ){
							//Logs.show(e);
						}
						PlayUpActivity.refreshDirectConversation();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}


	/** Checked
	 * for getting the region code
	 */
	public void getRegionData (final String url ) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					// request to get the token
					HttpRequest request = new HttpRequest( url, Constants.GET_METHOD );
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						if ( strBuffer != null ) {
							strBuffer.setLength( 0 );
							strBuffer = null;
						}
					} catch (RequestRepeatException e) {
						//Logs.show(e);
					}
					request = null;

					// updating the UI
					Message msg = new Message ();
					msg.obj = "refreshPage";
					PlayupLiveApplication.callUpdateOnFragments( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}

	
	
	/**
	 * Checked
	 */
	
	public void getPlayUpFriendsData ( ) {



		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				try {

					if(Constants.isFetchingCredentials){
						
						
						return;
						
					}
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();


					if ( dbUtil.isUserAnnonymous() ) {
						return;
					}
					
					
					
					Hashtable<String, Object> result = dbUtil.getPlayUpFriendsUrlForApiCall ();
					HttpRequest request = null;
					String vPlayUpFriendsUrl = "";
					if(result != null && result.containsKey("isHref")){
						 vPlayUpFriendsUrl = (String) result.get("url");
							if(vPlayUpFriendsUrl != null && vPlayUpFriendsUrl.trim().length() > 0){
								if(((Boolean) result.get("isHref")).booleanValue()){
									
									String vTokenValue = checkForHrefTokenParam(vPlayUpFriendsUrl);
									if(vTokenValue != null && vTokenValue.trim().length() > 0){
										int tokenType = checkForHrefTokenType(vPlayUpFriendsUrl);
										String encodedUrl = getPersonalizedEnocodedURL(vPlayUpFriendsUrl, vTokenValue,tokenType);
										request = new HttpRequest( vPlayUpFriendsUrl,encodedUrl,true, Constants.GET_METHOD );
										
									}else{
										request = new HttpRequest( vPlayUpFriendsUrl, Constants.GET_METHOD );
									}
									
								}else{
									request = new HttpRequest( vPlayUpFriendsUrl, Constants.GET_METHOD );
								}
							
						}
					}else{
						return;
					}

					// request to get the token
					if(vPlayUpFriendsUrl != null && vPlayUpFriendsUrl.trim().length() > 0){

						if(PlayUpActivity.runnableList != null && !PlayUpActivity.runnableList.containsKey(Constants.GET_PLAYUP_FREINDS_DATA)){

							PlayUpActivity.runnableList.put(Constants.GET_PLAYUP_FREINDS_DATA,true);
						}

						StringBuffer strBuffer = (StringBuffer) request.send();
						
						try {
							vPlayUpFriendsUrl = null;

							if(request.getStatusCode() == 401){
								callTheCredentialsUrl(strBuffer);
								return;
						
							}else
							

							if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && 
									strBuffer.toString().trim().length() > 0) {
								// parse and save in database.
								if(request.isAnyChangeInContent != Constants.NO_NEW_CONTENT_AVAIL){

									JsonUtil json = new JsonUtil() ;
									json.parse(strBuffer, Constants.TYPE_PLAYUP_FRIENDS_JSON, false );

								}
								if ( strBuffer != null ) {
									strBuffer.setLength(0);
									strBuffer = null;
								}
							}
							
						} catch (Exception e) {
							//Logs.show( e );
						}
						dbUtil = null;
						request = null;

						// updating the UI
						Message msg = new Message ();
						msg.obj = "PlayUpFriendsData";
						PlayupLiveApplication.callUpdateOnFragments( msg );

						PlayUpActivity.refreshPlayUpFriends();
					}
				} catch (Exception e) {
					//Logs.show ( e );
				}


			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}

/**
 * Checked
 */
	public void getUserLobbyData () {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					
					if(Constants.isFetchingCredentials){
						return;
						
					}
						
					Constants.isDownloadingFriendLobbyConversation = true;
					// updating the UI

					HttpRequest request = null;
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result = dbUtil.getUserLobbyUrlForApiCall ();
					String vUserLobbyUrl = "";
					if(result != null && result.containsKey("isHref")){
						
						vUserLobbyUrl = (String) result.get("url");
						if(vUserLobbyUrl != null && vUserLobbyUrl.trim().length() > 0){
						if(((Boolean) result.get("isHref")).booleanValue()){
						
						int tokenType = checkForHrefTokenType(vUserLobbyUrl);
						String vTokenValue = checkForHrefTokenParam(vUserLobbyUrl);
						
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							String encodedUrl = getPersonalizedEnocodedURL(vUserLobbyUrl, vTokenValue,tokenType);
							request = new HttpRequest( vUserLobbyUrl,encodedUrl,true, Constants.GET_METHOD );
							
						}else{
							
							request = new HttpRequest( vUserLobbyUrl, Constants.GET_METHOD );
							
						}
						
						}else{
							
							request = new HttpRequest( vUserLobbyUrl, Constants.GET_METHOD );
							
						}
						
						}
						
					}else{
						return;
					}

					// request to get the token
					if(vUserLobbyUrl != null && vUserLobbyUrl.trim().length() > 0){

						
						try {
							StringBuffer strBuffer = (StringBuffer) request.send();
							if(request.getStatusCode()==401){
								callTheCredentialsUrl(strBuffer);
								return;
							}
							// handling 404
							else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {
								// parse and save in database.
								if(request.isAnyChangeInContent != Constants.NO_NEW_CONTENT_AVAIL){

									JsonUtil json = new JsonUtil() ;
									json.setLobbyId ( dbUtil.getUserLobbyId() );
									json.setLobbyUrl ( vUserLobbyUrl );
									json.parse(strBuffer, Constants.TYPE_MY_LOBBY_JSON, false );
								
								}
								if ( strBuffer != null ) {
									strBuffer.setLength(0);
									strBuffer = null;
								}
							}
							vUserLobbyUrl = null;

							
						} catch (Exception e) {
							//Logs.show( e );
						}
						dbUtil = null;
						request = null;

						Constants.isDownloadingFriendLobbyConversation = false; 
						// updating the UI
						Message msg1 = new Message ();
						msg1.obj = "PlayUpLobby";
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg1);

						PlayUpActivity.refreshPlayUpFriends();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}


			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}
	/**
	 * Checked
	 * registering as an annonymous user
	 * */
	public void getUserNotificationData ( final boolean clearData ) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					if(!Constants.gapNotificationDownloading && !Constants.isFetchingCredentials){
					
						boolean isUpdateAvailable = false;
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						
						String notificationUrl = null;
						Boolean isHrefURL= false;
						
						Hashtable<String, Object> result = dbUtil.getUserNotificationUrl();
						if(result!=null && result.containsKey("url") && result.get("url")!=null && result.get("url").toString().trim().length()> 0 && result.containsKey("isHref") && result.get("isHref")!=null){
							 notificationUrl =(String) result.get("url");
							 isHrefURL =(Boolean) result.get("isHref");
						}
						
						dbUtil = null;
						if(notificationUrl != null && notificationUrl.trim().length() > 0){

							if(PlayUpActivity.runnableList != null && !PlayUpActivity.runnableList.containsKey(Constants.GET_USER_NOTIFICATION_DATA)){

								PlayUpActivity.runnableList.put(Constants.GET_USER_NOTIFICATION_DATA,true);
							}
							
							HttpRequest request = null;
							String encodedHrefURL	= null;
							String vTokenValue		= 	null;
							
							try {
								if(isHrefURL){
									
									int tokenType = checkForHrefTokenType(notificationUrl);
									//encode the url and make a http request with both encoded and unencoded urls
									 vTokenValue = new Util().checkForHrefTokenParam(notificationUrl); 
									 encodedHrefURL = new Util().getPersonalizedEnocodedURL(notificationUrl,vTokenValue,tokenType);
									
									
									request = new HttpRequest( /*unEncodedUrl,*/notificationUrl,encodedHrefURL,isHrefURL,Constants.GET_METHOD );
									
								}else{
									
									 request = new HttpRequest( notificationUrl,Constants.GET_METHOD );
									
								}
							}catch(Exception e ){
								//Logs.show(e);
							}
							try {
							
							
							StringBuffer strBuffer = (StringBuffer) request.send();
							
							
							if(request.getStatusCode() == 401){
								callTheCredentialsUrl(strBuffer);
								return;
								
							}else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {
									// parse and save in database.
									if(request.isAnyChangeInContent != Constants.NO_NEW_CONTENT_AVAIL){
										isUpdateAvailable = true;
										JsonUtil jsonUtil = new JsonUtil();
										jsonUtil.setBooleanFlag( clearData );
										jsonUtil.parse( strBuffer, Constants.TYPE_USER_NOTIFICATION_JSON, false );
									}


								}
							} catch (RequestRepeatException e) {
								//Logs.show ( e );
							}

							request = null;
							if(isUpdateAvailable)
								PlayupLiveApplication.callUpdateOnFragments(null);

							PlayUpActivity.refreshNotification();
							
//							PlayUpActivity.showDialog();
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					///Logs.show ( e );
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}

/**
 * Checked
 * @param vGapId
 * @param vGapUrl
 * @param isHrefURL
 * @param runnableList
 * @return
 */
	public Runnable getGapNotification ( final String vGapId, final String vGapUrl,final Boolean isHrefURL,final Hashtable<String, Runnable> runnableList  ) {

		Runnable runnable = new Runnable() {
			int status = 0;
			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){

						
						if(runnableList != null && runnableList.containsKey(vGapUrl)){
							runnableList.remove(vGapUrl);
						}
						
						return;

					}
					
					Constants.gapNotificationDownloading  = true;
					// request to get the token
					
					HttpRequest request = null;
					String encodedHrefURL	= null;
					String vTokenValue		= 	null;
					
					try {
						if(isHrefURL){
							
							
							int tokenType = checkForHrefTokenType(vGapUrl);
							vTokenValue = new Util().checkForHrefTokenParam(vGapUrl); 
							encodedHrefURL = new Util().getPersonalizedEnocodedURL(vGapUrl,vTokenValue,tokenType);
							
							request = new HttpRequest( /*unEncodedUrl,*/vGapUrl,encodedHrefURL,isHrefURL,Constants.GET_METHOD );
							
						}else{
							
							 request = new HttpRequest( vGapUrl,Constants.GET_METHOD );
							
						}
					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
							callTheCredentialsUrl(strBuffer);
							Constants.gapNotificationDownloading  = false;
							if(runnableList != null && runnableList.containsKey(vGapUrl)){
								runnableList.remove(vGapUrl);
							}
							return;
							
						}
						
						// handling 404

						else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

							JsonUtil jsonUtil = new JsonUtil();
							jsonUtil.setBooleanFlag( false );
							jsonUtil.parse( strBuffer, Constants.TYPE_USER_NOTIFICATION_JSON, false );

							JsonUtil json = new JsonUtil();
							json.queryMethod1 ( Constants.QUERY_DELETE, null, "notification", null, " vGapId = \"" + vGapId + "\" ", null, false, false  );
							Constants.gapNotificationDownloading  = false;
							status = 0;
							
							
							
						} else {
							status = 1;
						}

					} catch (RequestRepeatException e) {

						status = 1;
					}
					if(runnableList!=null&&runnableList.containsKey(vGapUrl)){
						runnableList.remove(vGapUrl);

					}

					Message msg = new Message ();
					msg.obj = "GapNotifications";
					msg.arg1 = status;
					PlayupLiveApplication.callUpdateOnFragments(msg);
					}catch(Exception e ){
						//Logs.show(e);
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		return runnable;
	}

	


	/**
	 * Checked
	 * @param vFriendUrl
	 * @param isHerf
	 * @param status
	 */
	public void addFriend  ( final String vFriendUrl,final Boolean isHerf,final String status ) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					
					if(Constants.isFetchingCredentials){
						return;
						
					}
						
					int statusCode = 0;
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String vFriendshipStatusUrl ;
					Boolean isHerf;
					Hashtable<String, Object> result = dbUtil.getFriendshipStatusUrl ( vFriendUrl );
					if(result!=null ){
						 vFriendshipStatusUrl = (String) result.get("url");
						 isHerf = (Boolean) result.get("isHref");
					}else{
						return ;
					}
					
					
				//	Log.e("123","vFriendshipStatusUrl >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+vFriendshipStatusUrl);
					
					String vFriendshipStatusType = dbUtil.getHeader( vFriendshipStatusUrl );
					
					//Log.e("123","vFriendshipStatusType >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+vFriendshipStatusType);
					JSONObject jObj = null;
					try {

						jObj = new JSONObject();
						jObj.put( ":type",  vFriendshipStatusType );
						jObj.put( "status",  status );

					} catch ( Exception e ) {
						//Logs.show(e);
					}

					// request to get the token
					HttpRequest request = null;
					String encodedHrefURL	= null;
					String vTokenValue		= 	null;
					
					
					try {
						if(isHerf){
							
							
							int tokenType = checkForHrefTokenType(vFriendshipStatusUrl);
							
							vTokenValue = new Util().checkForHrefTokenParam(vFriendshipStatusUrl); 
								
							//Log.e("234","tokenType================>>>>"+tokenType);
							encodedHrefURL = new Util().getPersonalizedEnocodedURL(vFriendshipStatusUrl,vTokenValue,tokenType);
							
							request = new HttpRequest( /*unEncodedUrl,*/vFriendshipStatusUrl,encodedHrefURL,isHerf, jObj.toString(),Constants.PUT_METHOD,null );
							
						}else{
							
							 request = new HttpRequest( vFriendshipStatusUrl, jObj.toString(),Constants.PUT_METHOD );
							
						}
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);
							return;
						}
						// handling 404
						else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {
							// parse and save in database.
							JsonUtil json = new JsonUtil();
							json.parse(strBuffer,  Constants.TYPE_ADD_FRIEND_JSON,  false );
						}

						statusCode = request.getStatusCode();


					} catch (RequestRepeatException e) {
						//Logs.show(e);
					}
					Message msg = new Message ();
					msg.obj = "AddFriend";
					//Log.e("123", "STSTUS CODE--->>>"+statusCode);
					if ( statusCode != 200 ) {
						msg.arg1 =1;
						PlayupLiveApplication.callUpdateOnFragments(msg);
					} else {
						// updating the UI

						if(status.equalsIgnoreCase("friends"))
							new Util().getFanProfileData(vFriendUrl,isHerf);
						msg.arg1 =0;
						PlayupLiveApplication.callUpdateOnFragments(msg);
					}
					}catch(Exception e){
					//Logs.show(e);	
					}
					
				}catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

	}


	/**
	 * Praveen :as per the href
	 */
	public void setNotificationConfirm  ( final String vNotificationUrl,final Boolean isHrefURL, final String requestData, final String status ) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				if(Constants.isFetchingCredentials){
					return;
				}
				
				

				int statusCode = 0;


				
				HttpRequest request = null;
				String encodedHrefURL	= null;
				String vTokenValue		= 	null;
				



				
				try {
					if(isHrefURL){
						


						int tokenType = checkForHrefTokenType(vNotificationUrl);
						vTokenValue = new Util().checkForHrefTokenParam(vNotificationUrl); 
						encodedHrefURL = new Util().getPersonalizedEnocodedURL(vNotificationUrl,vTokenValue,tokenType);


						
						
						request = new HttpRequest(vNotificationUrl,encodedHrefURL,isHrefURL,requestData,Constants.PUT_METHOD,null );
						
					}else{
						
						 request =  new HttpRequest( vNotificationUrl, requestData, Constants.PUT_METHOD );
						
					}
				
				try {
					StringBuffer strBuffer = (StringBuffer) request.send();
					
					if(request.getStatusCode() == 401){
						callTheCredentialsUrl(strBuffer);
						return;
					}else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null) {
						// parse and save in database.

						if ( strBuffer.toString().trim().length() > 0 ) {
							ContentValues values = new ContentValues ();
							values.put( "isRead", 1 );

							if ( status != null && status.trim().length() > 0 ) {
								values.put( "vStatus", status );
							}
							JsonUtil json = new JsonUtil();
							//json.queryMethod1( Constants.QUERY_UPDATE, null, "notification", values, " vNotificationUrl = \"" + vNotificationUrl + "\" ", null, false, false );
							if(isHrefURL)
							{
								json.queryMethod1( Constants.QUERY_UPDATE, null, "notification", values, " vNotificationHrefUrl = \"" + vNotificationUrl + "\" ", null, false, false );
							}else{
								json.queryMethod1( Constants.QUERY_UPDATE, null, "notification", values, " vNotificationUrl = \"" + vNotificationUrl + "\" ", null, false, false );
							}
							
							DatabaseUtil dbUtil = DatabaseUtil.getInstance();
							int notificationCount = dbUtil.getUnReadNotificationCount ();

							if(notificationCount > 0){
								values  =new ContentValues();
								values.put( "iNotificationUnReadCount", notificationCount - 1 );

								json = new JsonUtil();
								json.queryMethod1( Constants.QUERY_UPDATE, null, "user", values, " isPrimaryUser = '1' ", null, false, false  );
							}
						}

					}
					statusCode = request.getStatusCode();
				} catch (RequestRepeatException e) {
					//Logs.show(e);
				}

				if ( statusCode != 202 ) {
					Message msg = new Message ();
					msg.obj = "NotificationError";
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
				} else {
					
					PlayupLiveApplication.callUpdateOnFragments(null);
				}
				}catch(Exception e ){
					//Logs.show(e);
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

	}

	
	/**
	 * Hitting the base context url for fetching the context data.
	 */
	public void getDataFromContext() {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				
				try {
					// request to fetch the context data
					HttpRequest request = new HttpRequest(Constants.CONTEXTS_URL, Constants.GET_METHOD );
					// getting the response
					StringBuffer strBuffer = (StringBuffer) request.send();

					// checking for 404
					if (!handleServerNotFound(request.getStatusCode())
							&& strBuffer != null) {
						// parse and save in database.
						JsonUtil json = new JsonUtil();
						json.parse (strBuffer, Constants.TYPE_CONTEXT_URL, false );
					}
					if(strBuffer!=null && strBuffer.toString().trim().length() > 0)
						strBuffer.setLength(0);
				} catch (RequestRepeatException e) {
					//Logs.show(e);
				}


				PlayupLiveApplication.callUpdateOnFragments(null);
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}


	/**
	 * get the user token after authorization of the user.
	 */
	public void sendConversationInvitation ( final String vFriendId, final String vConversationId ) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				


				try {
					

					if(Constants.isFetchingCredentials){
						return;
					}
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String vLeagueName = dbUtil.getCompetitionNameFromConversation ( vConversationId );

					String vFriendType = dbUtil.getFriendType ( vFriendId );
					String vConversationName = dbUtil.getConversationName ( vConversationId );

					String message = "Invitation ---- ";
					
					JSONObject mainjObj = new JSONObject();
					try {
					JSONObject frnd_jsonObject = new JSONObject();

					frnd_jsonObject.put( ":type", vFriendType );
					frnd_jsonObject.put( ":uid", vFriendId );

					JSONObject detail_jsonObject = new JSONObject();

					detail_jsonObject.put( "title", vConversationName );
					detail_jsonObject.put( "subtitle",  vLeagueName );
					detail_jsonObject.put( ":type", "application/vnd.playup.textual.summary+json" );
					detail_jsonObject.put( "message", message );

					
					mainjObj.put( "to", frnd_jsonObject );
					mainjObj.put( "details", detail_jsonObject );
					mainjObj.put( ":type",  "application/vnd.playup.conversation.invitation.request+json" );
					}catch (JSONException e) {

					}

					HttpRequest request = null;

					Hashtable<String, Object> result = dbUtil.getConversationInvitationUrl ( vConversationId );
					
					if(result != null && result.containsKey("isHref")){
						
						String vConversationInvitationUrl = (String) result.get("url");
					//	Log.e("234","sendConversationInvitation=====isHref================>>>>"+result.get("isHref"));
						if(((Boolean) result.get("isHref")).booleanValue()){
						
							int tokenType = new Util().checkForHrefTokenType(vConversationInvitationUrl);
						//	Log.e("234","vConversationInvitationUrl================>>>>"+vConversationInvitationUrl);
							String vTokenValue = new Util().checkForHrefTokenParam(vConversationInvitationUrl); 
								
							
							
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
						//	String encodedUrl = getPersonalizedEnocodedURL(vConversationInvitationUrl, vTokenValue);
						//	Log.e("234","tokenType================>>>>"+tokenType);
							String	encodedHrefURL = new Util().getPersonalizedEnocodedURL(vConversationInvitationUrl,vTokenValue,tokenType);
							request = new HttpRequest ( encodedHrefURL,vConversationInvitationUrl,true, mainjObj.toString(), Constants.POST_METHOD,null );
							
						}else{
							
							request = new HttpRequest ( vConversationInvitationUrl, mainjObj.toString(), Constants.POST_METHOD );
							
						}
						
						}else{
							
							request = new HttpRequest ( vConversationInvitationUrl, mainjObj.toString(), Constants.POST_METHOD );
							
						}
						
						
					}else{
						return;
					}

					try {
							StringBuffer strBuffer = (StringBuffer) request.send();
							if(request.getStatusCode() == 401){

								callTheCredentialsUrl(strBuffer);								
								return;
							}else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null) {


								int statusCode = request.getStatusCode();


								if ( statusCode == 202 ) {
									dbUtil.setRecentInvite ( vFriendId, vConversationId, 2, true, false );
								} else {
									dbUtil.setRecentInvite ( vFriendId, vConversationId, 0 , true, false );
								}
								
								if(strBuffer!=null)
									strBuffer.setLength(0);
							}
							

						} catch (RequestRepeatException e) {
							///Logs.show(e);
						}

					


					Message msg =  new Message();
					msg.obj = "invitationSent";
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}

	
	
	public void sendPrivateLobbyInvitation ( final String vFriendId, final String vConversationId ) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				try {
					
					if(Constants.isFetchingCredentials){
						
						return;
						
					}
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();

					String vFriendType = dbUtil.getFriendType ( vFriendId );
					
					JSONObject frnd_jsonObject = new JSONObject();

					frnd_jsonObject.put( ":type", vFriendType );
					frnd_jsonObject.put( ":uid", vFriendId );



					JSONObject mainjObj = new JSONObject();
					mainjObj.put( "to", frnd_jsonObject );				
					mainjObj.put( ":type",  "application/vnd.playup.conversation.invitation.request+json" );
					HttpRequest request = null;
					Hashtable<String, Object> result = dbUtil.getPrivateLobbyInvitationUrl ( vConversationId );
					
					if(result != null && result.containsKey("isHref")){
						
						String vConversationInvitationUrl = (String) result.get("url");
						
						if(((Boolean) result.get("isHref")).booleanValue()){
							int tokenType = checkForHrefTokenType(vConversationInvitationUrl);
							String vTokenValue = checkForHrefTokenParam(vConversationInvitationUrl);
						
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							String encodedUrl = getPersonalizedEnocodedURL(vConversationInvitationUrl, vTokenValue,tokenType);
							request = new HttpRequest ( encodedUrl,vConversationInvitationUrl,true, mainjObj.toString(), Constants.POST_METHOD,null );
							
						}else{
							
							request = new HttpRequest ( vConversationInvitationUrl, mainjObj.toString(), Constants.POST_METHOD );
							
						}
						
						}else{
							
							request = new HttpRequest ( vConversationInvitationUrl, mainjObj.toString(), Constants.POST_METHOD );
							
						}
						
						
					}else{
						return;
					}

	

						try {
							StringBuffer strBuffer = (StringBuffer) request.send();
							
							if(request.getStatusCode() == 401){
								callTheCredentialsUrl(strBuffer);
								return;
								

							}else
							
							if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null) {

								int statusCode = request.getStatusCode();


								if ( statusCode == 202 ) {
									dbUtil.setRecentInvite ( vFriendId, vConversationId, 2, true, false );
								} else {
									dbUtil.setRecentInvite ( vFriendId, vConversationId, 0 , true, false );
								}
								
								if(strBuffer!=null)
									strBuffer.setLength(0);
							}
							

						} catch (RequestRepeatException e) {
							//Logs.show(e);
						}

					


					Message msg =  new Message();
					msg.obj = "invitationSent";
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}
	/**
	 * get the user token after authorization of the user.
	 */
	public void getUserToken(final String url, final String success_url) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				// DatabaseUtil dbutil = DatabaseUtil.getInstance();

				// String token = dbutil.getUserToken();
				// List < NameValuePair > paramList = null;

				// if ( token != null && token.trim().length() > 0 ) {
				//					
				// paramList = new ArrayList < NameValuePair > ();
				// //headerList.add( new NameValuePair ("Authorization PUToken",
				// token ) );
				// paramList.add( new NameValuePair ("token", token ) );
				// }

				HttpRequest request = new HttpRequest(url, Constants.GET_METHOD );

				try {
					StringBuffer strBuffer = (StringBuffer) request.send();
					if (!handleServerNotFound(request.getStatusCode())
							&& strBuffer != null && strBuffer.toString().trim().length() > 0) {

						// //////////////// CHANGE
						// for time being
						JsonUtil json = new JsonUtil();
						json.parse ( strBuffer, Constants.TYPE_STORE_TOKEN, false );
					}
					if(strBuffer!=null)
						strBuffer.setLength(0);

				} catch (RequestRepeatException e) {

				}
				
				
				PlayupLiveApplication.callUpdateOnFragments(null);

			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}



	/**
	 * Getting the context 's detailed information.
	 */
	public void getContextDetailedData(final String url) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				if(Constants.isFetchingCredentials){
					
					return;
					
				}

				HttpRequest request = new HttpRequest(url, Constants.GET_METHOD );
				try {
					StringBuffer strBuffer = (StringBuffer) request.send();
					
					if(request.getStatusCode() == 401){
						callTheCredentialsUrl(strBuffer);
						return;
						
						

					}else if (!handleServerNotFound(request.getStatusCode())
							&& strBuffer != null) {
						// parse and save in database.
						JsonUtil json = new JsonUtil();
						json.parse (strBuffer, Constants.TYPE_CONTEXT_DETAILED_URL, false );
						
						if(strBuffer != null && strBuffer.toString().trim().length() > 0)
							strBuffer.setLength(0);
					}
					
				} catch (RequestRepeatException e) {
					//Logs.show(e);
				}
				PlayupLiveApplication.callUpdateOnFragments(null);
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}

	/**
	 * call the next url to fetchthe messages 
	 */
	public Runnable callNextUrl ( final String vNextUrl, final String vConversationId,
			final Hashtable<String, Runnable> runnableList,final boolean isHref) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(vNextUrl)){
							runnableList.remove(vNextUrl);
						}
						return;
						
					}

					HttpRequest request = null;
					
					if(isHref){
						int tokenType = checkForHrefTokenType(vNextUrl);
						String vTokenValue = checkForHrefTokenParam(vNextUrl);

						
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							String encodedUrl = getPersonalizedEnocodedURL(vNextUrl, vTokenValue,tokenType);
							
							request = new HttpRequest( vNextUrl,encodedUrl,true, Constants.GET_METHOD );
							
						}else{
							request = new HttpRequest( vNextUrl, Constants.GET_METHOD );
						}
						

					}else{
						request = new HttpRequest( vNextUrl, Constants.GET_METHOD );
					}

					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						JsonUtil jsonUtil = new JsonUtil();
						if(request.getStatusCode() == 401){
							callTheCredentialsUrl(strBuffer);
							if(runnableList != null && runnableList.containsKey(vNextUrl)){
								runnableList.remove(vNextUrl);
							}
							return;
						
						} 
						

						ContentValues cValues = new ContentValues();
						cValues.put("isFromContestLobby",		1 );
						
						if(isHref){
							jsonUtil.queryMethod1( Constants.QUERY_UPDATE, null, "message", cValues, " vNextHref = \"" + vNextUrl + "\" ", null, false, false );
						}else{
							
							jsonUtil.queryMethod1( Constants.QUERY_UPDATE, null, "message", cValues, " vNextUrl = \"" + vNextUrl + "\" ", null, false, false );
						}

						

						if ( !handleServerNotFound ( request.getStatusCode() ) && strBuffer != null && strBuffer.toString().trim().length() > 0 ) {
							JsonUtil json = new JsonUtil();
							
							if(isHref){
								json.queryMethod1( Constants.QUERY_DELETE, null, "message", null, " vNextHref = \"" + vNextUrl + "\" ", null, false, false );
							}else{
								
								json.queryMethod1( Constants.QUERY_DELETE, null, "message", null, " vNextUrl = \"" + vNextUrl + "\" ", null, false, false );
							}

							
							json = new JsonUtil();
							json.setConversationId( vConversationId );

							json.setBooleanFlag( true );
							json.setBooleanFlag2 ( false );
							json.parse( strBuffer, Constants.TYPE_CONVERSATION_MESSAGES_JSON, false );

							DatabaseUtil mDatabaseUtil	=	DatabaseUtil.getInstance();
							mDatabaseUtil.removeGapSizeEntry(vNextUrl);

							if(strBuffer!=null)
								strBuffer.setLength(0);
						}
					} catch (RequestRepeatException e) {

						//Logs.show(e);

					}
					if(runnableList != null && runnableList.containsKey(vNextUrl)){
						runnableList.remove(vNextUrl);
					}
					Message msg = new Message ();				
					msg.obj = "MatchHomeFragment_Gap_Messages";
					PlayupLiveApplication.callUpdateOnFragments( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		return runnable;

	}


	/**
	 * 
	 * get conversations messages for match home UI
	 * 
	 */
	public synchronized Runnable getConversationMessages ( final String str, final String vConversationId ,final Hashtable<String, Runnable> runnableList, final boolean fromRefresh ) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				if(Constants.isFetchingCredentials){
					
					if(runnableList != null && runnableList.containsKey(Constants.GET_CONVERSATION_MESSAGES)){
						runnableList.remove(Constants.GET_CONVERSATION_MESSAGES);
					}
					
					return;
					
				}
					
				
				HttpRequest request = new HttpRequest( str, Constants.GET_METHOD );
				try {
					boolean isUpdated 	=	false;
					StringBuffer strBuffer = (StringBuffer) request.send();
					// not checking for 404 error in order to overcome multiple toast messages
					if ( strBuffer != null ) {
						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);
							if(runnableList!=null&&runnableList.containsKey(Constants.GET_CONVERSATION_MESSAGES)){
								runnableList.remove(Constants.GET_CONVERSATION_MESSAGES);
							}

							return;
						}
						// parse and save in database.
						else if( request.isAnyChangeInContent != Constants.NO_NEW_CONTENT_AVAIL ) {//If 304 then dont update

							if ( strBuffer != null && strBuffer.toString().trim().length() > 0 ) {

								JsonUtil json = new JsonUtil();
								json.setConversationId( vConversationId );
								json.setBooleanFlag( true );
								json.setBooleanFlag2( false );
								json.parse(strBuffer, Constants.TYPE_CONVERSATION_MESSAGES_JSON, false );

								isUpdated	=	true;
							}

							if(strBuffer!=null && strBuffer.toString().trim().length() > 0)
								strBuffer.setLength(0);

						}
					}

					if(runnableList!=null&&runnableList.containsKey(Constants.GET_CONVERSATION_MESSAGES)){
						runnableList.remove(Constants.GET_CONVERSATION_MESSAGES);
					}



					if ( fromRefresh ) {
						if ( isUpdated ) {
							Message msg = new Message ();
							msg.obj = "MatchHomeFragment_refresh";
							PlayupLiveApplication.callUpdateOnFragments( msg );
						}
					} else {
						Message msg = new Message ();
						msg.obj = "MatchHomeFragment_refresh";
						PlayupLiveApplication.callUpdateOnFragments( msg );
					}

				} catch (RequestRepeatException e) {
					//Logs.show(e);
				}

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}

	/**
	 * 
	 * get conversations messages afte posting 
	 * 
	 */
	public void getConversationMessages_ProcPosting ( final String url, final String vConversationId  ) {

		HttpRequest request = new HttpRequest( url, Constants.GET_METHOD );
		try {

			if(Constants.isFetchingCredentials){
			
				return;
				
			}
			
			DatabaseUtil mDatabaseUtil	= DatabaseUtil.getInstance();	
			if(mDatabaseUtil.getETag(url)==null){
				// Constants.firstTimePolling	=	true;
			}
			StringBuffer strBuffer = (StringBuffer) request.send();
			if(request.getStatusCode() == 401){
				callTheCredentialsUrl(strBuffer);
				return;
			}
			else {
				// not checking for 404 error in order to overcome multiple toast messages
				if ( strBuffer != null  && strBuffer.toString().trim().length() > 0) {

					// parse and save in database.
					if( request.isAnyChangeInContent != Constants.NO_NEW_CONTENT_AVAIL ) {//If 304 then dont update


						JsonUtil json = new JsonUtil();
						json.setConversationId( vConversationId );
						json.setBooleanFlag( true );
						json.setBooleanFlag2( false );
						json.parse(strBuffer, Constants.TYPE_CONVERSATION_MESSAGES_JSON, false );



						if(strBuffer!=null)
							strBuffer.setLength(0);
						Message msg = new Message ();
						msg.obj = "MatchHomeFragment";
						PlayupLiveApplication.callUpdateOnFragments( msg );

					}
				}
			}
		
		} catch (RequestRepeatException e) {
			//Logs.show(e);
		}



	}




	/**
	 * making a presence call 
	 * @param   - presenceUrl -- Presence or delete call url
	 * @param   - isPresenceCall -- true ( for presence call ) / false ( for delete call )
	 */
	public Runnable makePresenceDeleteCall ( final String presenceUrl, final boolean isPresenceCall ,final boolean isHref) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				try {
					if(Constants.isFetchingCredentials){
						return;
					}
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String token = dbUtil.getUserToken();

					if ( token != null && token.trim().length() > 0) {

						HttpRequest request = null;
						
						if(isHref){
							
							String vTokenValue = checkForHrefTokenParam(presenceUrl);
							int tokenType = checkForHrefTokenType(presenceUrl);
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								
								String encodedUrl = getPersonalizedEnocodedURL(presenceUrl, vTokenValue,tokenType);
								
								if (isPresenceCall ) {
									request = new HttpRequest( presenceUrl,encodedUrl,true, Constants.PUT_METHOD );
								} else {
									request = new HttpRequest( presenceUrl,encodedUrl,true, Constants.DELETE_METHOD );
								}
								
							}else{
								
								if (isPresenceCall ) {
									request = new HttpRequest( presenceUrl, Constants.PUT_METHOD );
								} else {
									request = new HttpRequest( presenceUrl, Constants.DELETE_METHOD );
								}
								
							}
							
						}else{
							
							if (isPresenceCall ) {
								request = new HttpRequest( presenceUrl, Constants.PUT_METHOD );
							} else {
								request = new HttpRequest( presenceUrl, Constants.DELETE_METHOD );
							}
							
						}

						

						if ( request != null ) {

							try {

								// getting the response
								StringBuffer strBuffer = (StringBuffer) request.send();
								
								if(request.getStatusCode()==401){
									callTheCredentialsUrl(strBuffer);
									return;
								}
							} catch (RequestRepeatException e) {

							}
						}


					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}

			}

		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}



	

	/**
	 * get conversation data
	 * @param url
	 */

	public Runnable getConversation ( final String url ,final Hashtable<String, Runnable> runnableList,final boolean isHref) {


	//	Log.e("234", "getConversation============url===============>>>>"+url);
		
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(Constants.GET_CONVERSATION)){
							runnableList.remove(Constants.GET_CONVERSATION);
						}
						
						return;
						
					}
						
					int status =0;
					
					HttpRequest request = null;
					if(isHref){
						
						String vTokenValue = checkForHrefTokenParam(url);
						int tokenType = checkForHrefTokenType(url);
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							String encodedUrl = getPersonalizedEnocodedURL(url, vTokenValue,tokenType);
							request = new HttpRequest(url,encodedUrl,true, Constants.GET_METHOD );
							
						}else{
							request = new HttpRequest(url, Constants.GET_METHOD );
						}
						
					}else{
						request = new HttpRequest(url, Constants.GET_METHOD );
					}
					
					 
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);
							if(runnableList != null && runnableList.containsKey(Constants.GET_CONVERSATION)){
								runnableList.remove(Constants.GET_CONVERSATION);
							}
						
							return;
						}
						else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null) {
							// parse and save in database.

							JsonUtil json = new JsonUtil();
							json.parse(strBuffer, Constants.TYPE_CONVERSATION_JSON, false );


							if(strBuffer!=null && strBuffer.toString().trim().length() > 0)
								strBuffer.setLength(0);
						} else {
							if ( request.getStatusCode() == 404 ) {
								PlayupLiveApplication.showToast(R.string.internet_connectivity_dialog);
							}
							status = 1;
						}
					} catch (RequestRepeatException e) {
					//	Logs.show(e);
					}

					if(runnableList!=null&&runnableList.containsKey(Constants.GET_CONVERSATION)){
						runnableList.remove(Constants.GET_CONVERSATION);
					}

					Message msg = new Message ();
					msg.obj = "MatchHomeFragment_getMessages";
					msg.arg1 = status;
					PlayupLiveApplication.callUpdateOnFragments( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}


	public Runnable getPrivateLobbyConversation ( final String url ,final Hashtable<String, Runnable> runnableList,
			final boolean fromRefresh,final boolean isHref) {


		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(Constants.GET_PRIVATE_LOBBY_CONVERSATION)){
							runnableList.remove(Constants.GET_PRIVATE_LOBBY_CONVERSATION);
						}
						
						return;
						
					}
						
					
					int status =0;
					boolean isUpdate = false;
					HttpRequest request = new HttpRequest(url, Constants.GET_METHOD );
					
					if(isHref){
						
						String vTokenValue = checkForHrefTokenParam(url);
						int tokenType = checkForHrefTokenType(url);
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							
							String encodedUrl = getPersonalizedEnocodedURL(url, vTokenValue,tokenType);
							request = new HttpRequest(url,encodedUrl,true, Constants.GET_METHOD );
						}else{
						
							request = new HttpRequest(url, Constants.GET_METHOD );
							
						}
						
					}else{
						
						request = new HttpRequest(url, Constants.GET_METHOD );
					}
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);

							if(runnableList != null && runnableList.containsKey(Constants.GET_PRIVATE_LOBBY_CONVERSATION)){
								runnableList.remove(Constants.GET_PRIVATE_LOBBY_CONVERSATION);
							}
							return;
						}
						else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0 ) {
							// parse and save in database.

							JsonUtil json = new JsonUtil();
							json.setAcceptableType("application/vnd.playup.friend.conversation+json");
							json.setPrivateLobbyOrderId(-1);
							json.parse(strBuffer, Constants.TYPE_FRIEND_CONVERSATION_JSON, false );
							isUpdate = true;

							if(strBuffer!=null && strBuffer.toString().trim().length() > 0)
								strBuffer.setLength(0);
						} else {

							if ( request.getStatusCode() == 404 ) {
								PlayupLiveApplication.showToast(R.string.internet_connectivity_dialog);
							}
							status = 1;
						}
					} catch (RequestRepeatException e) {
						//Logs.show(e);
					}


					if(runnableList!=null && runnableList.containsKey(Constants.GET_PRIVATE_LOBBY_CONVERSATION)){
						runnableList.remove(Constants.GET_PRIVATE_LOBBY_CONVERSATION);
					}

					if(fromRefresh ){
						if(isUpdate){
							Message msg = new Message ();
							msg.obj = "PrivateLobbyMessages";
							msg.arg1 = status;
							PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
						}

					}else{

						Message msg = new Message ();
						msg.obj = "PrivateLobbyMessages";
						msg.arg1 = status;
						PlayupLiveApplication.callUpdateOnFragments( msg );


					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}


			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}


	public Runnable getLinkData ( final String vLinkUrl ) {

		Runnable runnable = new Runnable( ) {

			@Override
			public void run() {

				
				try {
					HttpRequest request = new HttpRequest( vLinkUrl , Constants.GET_METHOD );
					StringBuffer strBuffer = (StringBuffer) request.send();

					if ( strBuffer != null ) {
						strBuffer = null;
					}
				} catch (RequestRepeatException e) {
					//Logs.show ( e );
				}



			}
		}; 
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}

	public Runnable getConversationForPushNotification ( final String url ,final String vPushId, final Intent intent) {


		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				if(Constants.isFetchingCredentials){
					
					return;
					
				}

				try {
					if(Constants.pushNotificationDownload  == null)
						Constants.pushNotificationDownload = new HashMap < String, Boolean  > ();


					Constants.pushNotificationDownload.put(vPushId, true);
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					int status =0;
					URL secondURL = null;
					try{
						URL url1 = new URL(url);
						HttpURLConnection urlConnection = (HttpURLConnection)url1.openConnection();
						urlConnection.setInstanceFollowRedirects(false);
						secondURL = new URL(urlConnection.getHeaderField("Location"));



					}catch(Exception e){
						Constants.pushNotificationDownload.put(vPushId, false);
						//Logs.show ( e );
					}


					HttpRequest request = new HttpRequest(""+secondURL, Constants.GET_METHOD );
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
							callTheCredentialsUrl(strBuffer);
							
							return;
							
							

						}else

						if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.length() > 0) {
							// parse and save in database.
							JSONObject jsonObj = new JSONObject(strBuffer.toString());



							if(jsonObj.getString(":type").equalsIgnoreCase(Constants.ACCEPT_TYPE_DIRECT_CONVERSATION)){
								dbUtil.setPushType(vPushId,0);


								JsonUtil json = new JsonUtil();
								json.setIsDirectConversationUrlHref(false);
								json.setDirectConversationUrl(""+secondURL);								
								json.setDirectConvesationPushId ( vPushId );
								json.setDirectConversationId(null);	

								json.parse(strBuffer, Constants.TYPE_DIRECT_MESSAGES_JSON, false );

								//	PlayUpActivity.showNotification(intent);

							}else if(jsonObj.getString(":type").equalsIgnoreCase( "application/vnd.playup.conversation+json" )) {

								dbUtil.setPushType(vPushId,1);
								JsonUtil json = new JsonUtil();
								json.setPushId ( vPushId );
								json.parse(strBuffer, Constants.TYPE_CONVERSATION_JSON, false );

							} else if(jsonObj.getString(":type").equalsIgnoreCase( "application/vnd.playup.friend.conversation+json")){


								dbUtil.setPushType(vPushId,3);
								JsonUtil json = new JsonUtil();
								json.setAcceptableType("application/vnd.playup.friend.conversation+json");
								json.setPrivateLobbyOrderId(-1);
								json.setPrivateMessagePushId ( vPushId );
								json.parse(strBuffer, Constants.TYPE_FRIEND_CONVERSATION_JSON, false );

							} else{

								Constants.pushNotificationDownload.put(vPushId, false);
								dbUtil.removePushNotification(vPushId);
								PlayUpActivity.mNotificationManager.cancel(intent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID,-1));
							}

							if(strBuffer!=null && strBuffer.toString().trim().length() > 0)
								strBuffer.setLength(0);
						} else if(strBuffer == null && secondURL != null ) {
							DatabaseUtil.getInstance().setConversationId(secondURL.toString(),vPushId);
							status = 1;
						} 
						else if(strBuffer != null && strBuffer.length() == 0 && secondURL != null ) {						

							DatabaseUtil.getInstance().setConversationId(secondURL.toString(),vPushId);
							status = 1;
						}
					} catch (RequestRepeatException e) {
						Constants.pushNotificationDownload.put(vPushId, false);
						//Logs.show( e );
					}catch (JSONException e) {
						Constants.pushNotificationDownload.put(vPushId, false);
						//.show( e );
					}




					Constants.pushNotificationDownload.put(vPushId, false);

					Message msg = new Message ();
					msg.obj = "MatchHomeFragment_getMessages";
					msg.arg1 = status;
					PlayupLiveApplication.callUpdateOnFragments( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}


	public void putFollowMessage ( final String vConversationUrl, 
			final String data ,final ArrayList<String> follow,final String token,final boolean isHref) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				if(Constants.isFetchingCredentials){
					return;
					
				}
				HttpRequest request = null;
					
				if(isHref){
					
					String vTokenValue = checkForHrefTokenParam(vConversationUrl);
					int tokenType = checkForHrefTokenType(vConversationUrl);
					if(vTokenValue != null && vTokenValue.trim().length() > 0){
						
						String encodedUrl = getPersonalizedEnocodedURL(vConversationUrl, vTokenValue,tokenType);
						request = new HttpRequest(encodedUrl, vConversationUrl ,true, data, Constants.PUT_METHOD,null );
						
					}else{
						request = new HttpRequest( vConversationUrl , data, Constants.PUT_METHOD );
					}
					
				}else{
				
					request = new HttpRequest( vConversationUrl , data, Constants.PUT_METHOD );
				}
				

				try {

					StringBuffer strBuffer = (StringBuffer) request.send();
					
					if(request.getStatusCode()==401){
						callTheCredentialsUrl(strBuffer);
						return;
					}
					else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

						JsonUtil json = new JsonUtil();
						json.setAcceptableType("application/vnd.playup.friend.conversation+json");
						json.setPrivateLobbyOrderId(-1);
						json.parse(strBuffer, Constants.TYPE_FRIEND_CONVERSATION_JSON, false );
						strBuffer.setLength(0);

					}
				} catch (RequestRepeatException e) {
					//Logs.show( e );
				}catch (Exception e) {
					//Logs.show( e );
				}
				if(follow != null && follow.size() > 0){
					follow.remove(0);					
				}

				Message msg = new Message ();
				msg.obj = "PrivateLobbyPutFollowMessage";
				msg.arg1 = 0;
				PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

	}

	private String host;
	private String port;




	/**
	 * checking for proxy settings  
	 */
	public boolean checkForProxy(){

		try {
			PreferenceManagerUtil preferenceManagerUtil = new PreferenceManagerUtil();
			if ( preferenceManagerUtil != null ) {
				host =	preferenceManagerUtil.get("Host", "");
				port =	preferenceManagerUtil.get("Port", "");

				if ( host.length() == 0 ) {
					return false;
				} else {
					return true;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show ( e );
		}
		return false;
	}



	public Runnable searchFriends( final String url, final String searchString,final boolean isHref) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						return;
						
					}
						
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					dbUtil.removeEtag(  url.replace( "{prefix}", searchString ) );
					HttpRequest request = new HttpRequest( url.replace( "{prefix}", searchString ), Constants.GET_METHOD );
					
					if(isHref){
						int tokenType = checkForHrefTokenType(url);
						String vTokenValue = checkForHrefTokenParam(url);
						
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							String encodedUrl = getPersonalizedEnocodedURL(url, vTokenValue,tokenType);
							
							request = new HttpRequest( url.replace( "{prefix}", searchString ),encodedUrl,true ,Constants.GET_METHOD );
							
							
							
						}else{
							
							request = new HttpRequest( url.replace( "{prefix}", searchString ), Constants.GET_METHOD );
						}
						
					}else{
					
						
						request = new HttpRequest( url.replace( "{prefix}", searchString ), Constants.GET_METHOD );
					}

					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);
							return;
						}
						else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {



							dbUtil.emptySearchFriends();


							JsonUtil json = new JsonUtil();
							json.setGapId( "" );
							json.parse(strBuffer, Constants.TYPE_SEARCH_FRIEND_JSON, false );



							if(strBuffer!=null)
								strBuffer.setLength(0);
						}
					} catch (RequestRepeatException e) {
						//Logs.show ( e );
					}catch (Exception e) {
						// TODO: handle exception
						//Logs.show ( e );


					}




					Message msg = new Message ();
					msg.obj = "SearchFriendsData";
					Bundle bundle = new Bundle();
					bundle.putString("search_value", searchString );
					msg.setData(bundle);
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		return runnable;
	}

	public void searchConversationFriends ( final String vConversationId, final String url ) {


		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				
				try {
					HttpRequest request = new HttpRequest(url, Constants.GET_METHOD );
					StringBuffer strBuffer = (StringBuffer) request.send();
					if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						dbUtil.removeEtag(url);
						dbUtil.emptySearchFriends();

						JsonUtil json = new JsonUtil();
						json.setBooleanFlag( true );
						json.setConversationId( vConversationId );
						json.parse( strBuffer, Constants.TYPE_CONVERSATION_FRIENDS_JSON, false );

						if(strBuffer!=null)
							strBuffer.setLength(0);
					}
				} catch (RequestRepeatException e) {
					//Logs.show ( e );
				}catch (Exception e) {

					//Logs.show ( e );
				}

				Message msg = new Message ();
				msg.obj = "SearchFriendsData";
				PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

	}



	public void createConversationOrRoom(final String hangoutName,final String searchText,final boolean privateStatus) {
		try {
			Runnable runnable = new Runnable() {
				int status = 0;
				@Override
				public void run() {

					String vConversationId = null;
					try {
						
						if(Constants.isFetchingCredentials)
							return;

						JSONObject mJsonObject = new JSONObject();
						mJsonObject.put(":type",
						"application/vnd.playup.conversation+json");
						mJsonObject.put("name",hangoutName);
						if(privateStatus)
							mJsonObject.put("access", "private");
						else
							mJsonObject.put("access", "public");
						String requestData = mJsonObject.toString();          


						
						

						

						
						String url = Keys.BASE_URL+"contests/"+searchText+"/conversations";



						
						
						
						HttpRequest req = new HttpRequest(url, requestData,Constants.POST_METHOD,Types.CONTEST_LOBBY_CONVERSATION_DATA_TYPE);

						try {
							StringBuffer strBuffer = (StringBuffer) req.send();
							
							if(req.getStatusCode() == 401){
								callTheCredentialsUrl(strBuffer);
								requestData = null;
								releaseMemory( mJsonObject );
								return;
								
								
								
							}else if (!handleServerNotFound(req.getStatusCode()) && strBuffer != null) {

								
								
								requestData = null;
								releaseMemory( mJsonObject );

								JsonUtil json = new JsonUtil();
								json.parse( strBuffer, Constants.TYPE_CONVERSATION_JSON, false );

								vConversationId = json.getConversationId ();

								if(strBuffer!=null)
									strBuffer.setLength(0);
							} else {
								status = 1;
							}
						} catch (RequestRepeatException e) {
							//Logs.show ( e ) ;
						}
					} catch (Exception e) {
						//Logs.show ( e );
					}


					Message msg = new Message ();
					Bundle bundle = new Bundle();
					bundle.putString( "vConversationId", vConversationId );
					msg.setData(bundle);
					msg.obj = "CreateRoom";
					msg.arg1 = status;
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
				}

			};
			PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	Logs.show(e);
		}
	}



	public void createLobbyHangout ( final String hangoutName ) {
		Runnable runnable = new Runnable() {
			int status = 0;
			@Override
			public void run() {
				
				String vConversationId = null;

				
				try {
					
					if(Constants.isFetchingCredentials)
						return;

					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result = dbUtil.getUserLobbyConversationUrl();
					HttpRequest req = null;
					String vLobbyConversationUrl = "";
					
					JSONObject mJsonObject = new JSONObject();
					mJsonObject.put(":type", "application/vnd.playup.friend.conversation+json" );
					mJsonObject.put("name", hangoutName );
					String requestData = mJsonObject.toString();   
					
					if(result != null && result.containsKey("isHref")){
						

					
						
						vLobbyConversationUrl = (String)result.get("url");
						if(((Boolean) result.get("isHref")).booleanValue()){
							
							String vTokenValue = checkForHrefTokenParam(vLobbyConversationUrl);
							int tokenType = checkForHrefTokenType(vLobbyConversationUrl);
							if(vTokenValue != null && vTokenValue.trim().length() >0 ){
								
								String encodedUrl = getPersonalizedEnocodedURL(vLobbyConversationUrl, vTokenValue,tokenType);
								
								req = new HttpRequest( encodedUrl,vLobbyConversationUrl ,true, requestData, Constants.POST_METHOD,Types.PRIVATE_CONVERSATION_DATA_TYPE );
								
							}else{
								
								req = new HttpRequest( vLobbyConversationUrl , requestData, Constants.POST_METHOD,Types.PRIVATE_CONVERSATION_DATA_TYPE );
								
							}
							
						}else{
							
							req = new HttpRequest( vLobbyConversationUrl , requestData, Constants.POST_METHOD,Types.PRIVATE_CONVERSATION_DATA_TYPE );
							
						}
					
						
						
						
					}else{
						return;
					}
					

       

					

					try {
						StringBuffer strBuffer = (StringBuffer) req.send();
						
						if(req.getStatusCode() == 401){
							
							callTheCredentialsUrl(strBuffer);
							requestData = null;
							releaseMemory( mJsonObject );
							return;
							
						}else if (!handleServerNotFound(req.getStatusCode()) && strBuffer != null) {

							requestData = null;
							releaseMemory( mJsonObject );

							JsonUtil json = new JsonUtil();
							json.setAcceptableType ( "application/vnd.playup.friend.conversation+json" );
							json.setPrivateLobbyOrderId(-1);
							json.parse( strBuffer , Constants.TYPE_FRIEND_CONVERSATION_JSON, true );
							vConversationId = json.getConversationId();

							if(strBuffer!=null)
								strBuffer.setLength(0);
						} else {
							status = 1;
						}
					} catch (RequestRepeatException e) {

					}
				} catch (Exception e) {
					//Logs.show ( e );
				}


				Message msg = new Message ();
				Bundle bundle = new Bundle();
				bundle.putString( "vConversationId", vConversationId );
				msg.setData(bundle);
				msg.obj = "CreateLobbyHangout";
				msg.arg1 = status;
				PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}


	public void editLobbyHangout ( final String hangoutName, final String vConversationUrl , final Boolean isHrefURL ) {
		Runnable runnable = new Runnable() {
			int status = 0;
			@Override
			public void run() {

				String vConversationId = null;

				try {
					
					if(Constants.isFetchingCredentials)
						return;


					JSONObject mJsonObject = new JSONObject();
					mJsonObject.put(":type", "application/vnd.playup.friend.conversation+json" );
					mJsonObject.put("name", hangoutName );
					String requestData = mJsonObject.toString();          

					
					
					HttpRequest req = null;
					String encodedHrefURL	= null;
					String vTokenValue		= 	null;
					
					
					try {
						if(isHrefURL){
							
							int tokenType = checkForHrefTokenType(vConversationUrl);
							 vTokenValue = new Util().checkForHrefTokenParam(vConversationUrl); 
							 
							 if(vTokenValue != null && vTokenValue.trim().length() > 0){
								 encodedHrefURL = new Util().getPersonalizedEnocodedURL(vConversationUrl,vTokenValue,tokenType);
								 req = new HttpRequest( vConversationUrl,encodedHrefURL,isHrefURL,requestData,Constants.PUT_METHOD,null );
								 
							 }else{
								 
								 req = new HttpRequest( vConversationUrl , requestData, Constants.PUT_METHOD );
								 
							 }
							
						}else{
							
							req = new HttpRequest( vConversationUrl , requestData, Constants.PUT_METHOD );
							
						}
					}catch(Exception e ){
						//Logs.show(e);
					}

					try {
						StringBuffer strBuffer = (StringBuffer) req.send();
						
						if(req.getStatusCode() == 401){
							callTheCredentialsUrl(strBuffer);
							requestData = null;
							releaseMemory( mJsonObject );
							return;
							
							

						}else if(!handleServerNotFound(req.getStatusCode()) && strBuffer != null) {

							requestData = null;
							releaseMemory( mJsonObject );

							JsonUtil json = new JsonUtil();
							json.setAcceptableType ( "application/vnd.playup.friend.conversation+json" );
							json.setPrivateLobbyOrderId(-1);
							json.parse( strBuffer , Constants.TYPE_FRIEND_CONVERSATION_JSON, true );
							vConversationId = json.getConversationId();

							if(strBuffer!=null)
								strBuffer.setLength(0);
						} else {
							status = 1;
						}
					} catch (RequestRepeatException e) {
						//Logs.show(e);
					}
				} catch (Exception e) {
					//Logs.show ( e );
				}


				

				Message msg = new Message ();
				Bundle bundle = new Bundle();
				bundle.putString( "vConversationId", vConversationId );
				msg.setData(bundle);
				msg.obj = "CreateLobbyHangout";
				msg.arg1 = status;
				PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}

	/**
	 * setting the username ot the server 
	 */
	public void editProfile ( final String vSelectedUrl, final String iUserId, final String name,final String providerId ) {

		if ( isInternetAvailable() ) {

			Runnable runnable = new Runnable () {

				@Override
				public void run() {



					if(Constants.isFetchingCredentials)
						return;
					
					try {
						String vAvatarUrl = vSelectedUrl;
						String prev_ImageUrl;

						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						
						Hashtable<String, Object> result = dbUtil.getProfileURLFromRootResource();
						
						if(result  == null || (result != null && !result.containsKey("isHref"))){
							return;
						}
						
						String vSelfUrl = (String) result.get("url");
						Boolean isHrefURL = (Boolean) result.get("isHref");
						
						prev_ImageUrl	=	dbUtil.getUserAvatarUrl();
						if ( vAvatarUrl == null ) {
							vAvatarUrl = dbUtil.getUserAvatarUrl();
						}


						if(providerId!=null&&vSelectedUrl.compareToIgnoreCase(prev_ImageUrl)!=0){


							ImageDownloader mDownloader	=	new ImageDownloader();
							mDownloader.removeImageFromSoftCache(dbUtil.getUserId());
							mDownloader.removeImageFromSoftCache(providerId);
							mDownloader.copyFileContent(providerId, dbUtil.getUserId());


						}

						// update to the servers
						JSONObject mJsonObject = new JSONObject();

						try {
							mJsonObject.put("avatar", vAvatarUrl );	
							mJsonObject.put("name", name );	
							mJsonObject.put(":type", "application/vnd.playup.profile+json");

						} catch (JSONException e1) {
						}
						String requestData = mJsonObject.toString();
						//HttpRequest request = new HttpRequest( vSelfUrl, requestData, Constants.PUT_METHOD );
						HttpRequest request = null;
						String encodedHrefURL	= null;
						String vTokenValue		= 	null;
						//boolean isHref = checkForToken(vSectionUrl);
						
						try {
							if(isHrefURL){
								
								
								 vTokenValue = new Util().checkForHrefTokenParam(vSelfUrl); 
								 int tokenType = checkForHrefTokenType(vSelfUrl);
								 
								 if(vTokenValue != null && vTokenValue.trim().length() > 0){
									 encodedHrefURL = new Util().getPersonalizedEnocodedURL(vSelfUrl,vTokenValue,tokenType);
									 request = new HttpRequest(vSelfUrl,encodedHrefURL,isHrefURL,requestData,Constants.PUT_METHOD,null );
									 
								 }else{
									 
									 request = new HttpRequest( vSelfUrl, requestData, Constants.PUT_METHOD );
									 
								 }
								 
								
								
								
								
								
							}else{
								
								 request = new HttpRequest( vSelfUrl, requestData, Constants.PUT_METHOD );
								
							}
						}catch(Exception e){
							//Logs.show(e);
						}
						try {
							StringBuffer strBuffer = (StringBuffer) request.send();
							

							if(request.getStatusCode() == 401){
								callTheCredentialsUrl(strBuffer);
								String message	=	"editProfileError";
								PlayUpActivity.showErrorDialog(message);
								return;
								
							}else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null ) {


								// update to the database
								ContentValues values = new ContentValues ();
								values.put( "vName", name );
								values.put( "vUserAvatarUrl", vAvatarUrl );

								JsonUtil json = new JsonUtil();
								json.queryMethod1( Constants.QUERY_UPDATE, null, "user", values, " iUserId = \"" + iUserId + "\" ", null, false, false );

								if(strBuffer!=null)
									strBuffer.setLength(0);


							}
						} catch (RequestRepeatException e) {
							//Logs.show ( e );
						}



						Message msg = new Message ();
						msg.obj = "EditProfileFragment";
						PlayupLiveApplication.callUpdateOnFragments( msg );
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//Logs.show ( e );
					}

				}

			};
			PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		} else {
			PlayupLiveApplication.showToast( R.string.no_network );
		}
	}



	public void logout () {

		Runnable runnable = new Runnable () {

			@Override
			public void run() {
				try {
					
					Constants.name	=	null;
					Constants.userName	= null;
					Constants.isAnonymous	=	true;

					
					JsonUtil json = new JsonUtil();



					json.dropTables();


					CacheUtil cache = new CacheUtil();
					cache.clearCache();
					Constants.isLoggedIn	=	false;

					PlayUpActivity.runnableList.clear();

					PlayUpActivity.runnableList = new Hashtable<String, Boolean>();
					PlayUpActivity.cancelTimers();


					new Util().getDataFromServer(false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}

			}

		};



		
		try {
			if ( !PlayupLiveApplication.getDatabaseWrapper().inProcess() ) {
				PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

			} else {
				Thread th = new Thread ( runnable );
				th.start();
			}

			NotificationManager nMgr = (NotificationManager) PlayUpActivity.context.getSystemService( Context.NOTIFICATION_SERVICE );
			nMgr.cancelAll();
		} catch (Exception e) {
			//Logs.show(e);
		}




	}

	/**
	 * Getting the all sports data from the server
	 **/
	public Runnable getAllSports (final Hashtable<String, Runnable> runnableList) {

		Constants.isAllSportsDownloading = true;
		
		
		Runnable runnable = new Runnable() {
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			@Override
			public void run() {

				try {

					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(Constants.GET_ALL_SPORTS)){
							runnableList.remove(Constants.GET_ALL_SPORTS);
						}
						
						return;
						
					}
						

					boolean isUpdateAvailable = false;
					
					try {
					String allSports;
					HttpRequest request = null;
					Hashtable<String, Object> result = dbUtil.getAllSportsURLFromRootResource(); 
					
					if(result == null)
						return;
					if(result != null && result.containsKey("isHref")){
						
						allSports = (String) result.get("url");
						
						if((Boolean) result.get("isHref")){							
							 int tokenType = checkForHrefTokenType(allSports);
							String vTokenValue = checkForHrefTokenParam(allSports);
							
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								
								String allSportsEncoded = getPersonalizedEnocodedURL(allSports, vTokenValue,tokenType);
								
								request = new HttpRequest( allSports,allSportsEncoded,true, Constants.GET_METHOD );
							}else{
								
								request = new HttpRequest( allSports, Constants.GET_METHOD );
							}
							
							
							
						}else{
							
							request = new HttpRequest( allSports, Constants.GET_METHOD );
						}
						
					}else{
						return;
					}

					dbUtil = null;
					
					
					StringBuffer strBuffer = (StringBuffer) request.send();
					if(request.getStatusCode() == 401){
						callTheCredentialsUrl(strBuffer);
						Constants.isAllSportsDownloading = false;
						if(runnableList != null && runnableList.containsKey(Constants.GET_ALL_SPORTS)){
							runnableList.remove(Constants.GET_ALL_SPORTS);
						}
						return;
						
						

					}else if( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null &&
								strBuffer.toString().trim().length() > 0) {


							if( request.isAnyChangeInContent != Constants.NO_NEW_CONTENT_AVAIL ) {

								JsonUtil json = new JsonUtil();
								json.parse(strBuffer, Constants.TYPE_ALL_SPORTS_JSON , false );
								json = null;
								isUpdateAvailable = true;

							}
							if(strBuffer!=null) {
								strBuffer.setLength(0);
								strBuffer = null;
							}
							request = null;

						}
					} catch (RequestRepeatException e) {
						//Logs.show ( e );
					}catch (Exception e) {
						
						//Logs.show(e);

					}finally{
						
						Constants.isAllSportsDownloading = false;
					}


					if(runnableList !=null && runnableList.containsKey(Constants.GET_ALL_SPORTS)){
						runnableList.remove(Constants.GET_ALL_SPORTS);
					}

					
					
					if(isUpdateAvailable){

						Message msg = new Message ();
						msg.obj = "AllSportsResults";
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );

					}
					Constants.isAllSportsDownloading = false;		
					PlayUpActivity.refreshAllSports();
				} catch (Exception e) {
					//Logs.show ( e );
				}finally{
					if(runnableList !=null && runnableList.containsKey(Constants.GET_ALL_SPORTS)){
						runnableList.remove(Constants.GET_ALL_SPORTS);
					}
					
					Constants.isAllSportsDownloading = false;
				}

			}
		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}
	
	
	public static synchronized void callTheCredentialsUrl(StringBuffer strBuffer){
		
		try {
			//Log.e("123", "Call the credentials Url ( 401 took place)--&&&&&&&&&&&&&&&&&&&&&&&&&->>>");
			
			if(PlayUpActivity.runnableList != null && PlayUpActivity.runnableList.containsKey(Constants.GET_CREDENTIALS)
					&& PlayUpActivity.runnableList.get(Constants.GET_CREDENTIALS))
				return;
			
			if(strBuffer != null && strBuffer.toString().trim().length() > 0){
				
				
				//Log.e("123","call the creedentilas url strBuffer != null >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+strBuffer.toString());
				
				Constants.isFetchingCredentials = true;
				
				PlayUpActivity.runnableList.put(Constants.GET_CREDENTIALS, true);
				
				
				new Runnable() {
					
					@Override
					public void run() {
						// Drop onlu necessary tabless
						try {
							DatabaseUtil.getInstance().dropTables();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							//Logs.show ( e );
						}
					}
				};
				
				
				new Util().getCredentials(strBuffer);
			}else{
				
				if(Util.isInternetAvailable()){
					String message = "errorFethchingCredentials";
					PlayUpActivity.showErrorDialog(message);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show ( e );
		}
		
		
	}

	public Runnable getLiveMatchesCount ( final String vCompetionUrl,
			final Hashtable<String, Runnable> runnableList,final boolean isHref) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						if(runnableList != null && runnableList.containsKey(vCompetionUrl)){

							runnableList.remove(vCompetionUrl);
						}
						
						return;
						
					}
					HttpRequest request = null;
					
					if(isHref){
						
						String vTokenvalue = checkForHrefTokenParam(vCompetionUrl);
						 int tokenType = checkForHrefTokenType(vCompetionUrl);
						if(vTokenvalue != null && vTokenvalue.trim().length() > 0){
							
							String encodedUrl = getPersonalizedEnocodedURL(vCompetionUrl, vTokenvalue,tokenType);
							
							request = new HttpRequest( vCompetionUrl,encodedUrl,true, Constants.GET_METHOD );
							
						}else{
							
							request = new HttpRequest( vCompetionUrl, Constants.GET_METHOD );
							
						}
						
					}else{
						
						request = new HttpRequest( vCompetionUrl, Constants.GET_METHOD );
						
					}
					
					boolean isUpdated	=	true;
					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401)
							{
								callTheCredentialsUrl(strBuffer);
								if(runnableList != null && runnableList.containsKey(vCompetionUrl)){

									runnableList.remove(vCompetionUrl);
								}
								
								return;
							}
						
						else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

							//parse json for leagues

							JsonUtil jsonUtil= new JsonUtil();

							jsonUtil.parse(strBuffer, Constants.TYPE_COMPETITON_LIVE, false );
							jsonUtil = null;
							
							if(strBuffer!=null) {
								strBuffer.setLength(0);
								strBuffer = null;
							}

						}
						



						request = null;
					} catch (RequestRepeatException e) {

						isUpdated	=	false;
					}

					if(runnableList!=null&&runnableList.containsKey(vCompetionUrl)){

						runnableList.remove(vCompetionUrl);
					}

					if(isUpdated){
						Message msg = new Message ();
						msg.obj = "LiveMatchesCount";
						Bundle b = new Bundle();
						b.putString("vCompetionUrl", vCompetionUrl);
						msg.setData(b);
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}
			}
		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}


	public Runnable getLiveMatches(final String vCompetionLiveUrl,final Hashtable<String, Runnable> runnableList) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					boolean isUpdated	=	false;
					
					if(Constants.isFetchingCredentials){
						if(runnableList != null && runnableList.containsKey(vCompetionLiveUrl)){

							runnableList.remove(vCompetionLiveUrl);
						}
						
						return;
						
					}
						

					HttpRequest request = new HttpRequest( vCompetionLiveUrl, Constants.GET_METHOD );
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401)
							{
								callTheCredentialsUrl(strBuffer);
								if(runnableList!= null && runnableList.containsKey(vCompetionLiveUrl)){

									runnableList.remove(vCompetionLiveUrl);
								}
								return;
							}
						
						if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && 
								strBuffer != null && strBuffer.toString().trim().length() > 0) {

							//parse json for leagues


							isUpdated	=	true;
							JsonUtil jsonUtil= new JsonUtil();

							jsonUtil.parse(strBuffer, Constants.TYPE_COMPETITON_LIVE, false );
							jsonUtil = null;
							
							if(strBuffer!=null) {
								strBuffer.setLength(0);
								strBuffer = null;
							}

						}
						



						request = null;
					} catch (RequestRepeatException e) {

						isUpdated	=	false;
					}

					if(runnableList != null && runnableList.containsKey(vCompetionLiveUrl)){

						runnableList.remove(vCompetionLiveUrl);
					}



					if(isUpdated){
						Message msg = new Message ();
						msg.obj = "LiveMatches";
						Bundle b = new Bundle();
						b.putString("vCompetionLiveUrl", vCompetionLiveUrl);
						msg.setData(b);					
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );

					}else{

						Message msg = new Message ();
						msg.obj = "RefreshMatches";
						Bundle b = new Bundle();
						b.putString("vCompetionLiveUrl", vCompetionLiveUrl);
						msg.setData(b);					
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}
		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}


	public Runnable getLiveContests(final String vContestLiveUrl,final Hashtable<String, Runnable> runnableList,
			final boolean isHref) {
		
	//	Log.e("234", "vContestLiveUrl==============>>>>"+vContestLiveUrl);
		
		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				
				try {
					if(Constants.isFetchingCredentials){
						


						
						if(runnableList != null && runnableList.containsKey(vContestLiveUrl)){



							runnableList.remove(vContestLiveUrl);
						}
						
						return;
						
					}
					
					boolean isUpdated	=	false;

					HttpRequest request = null;
					if(isHref){
						int tokenType = checkForHrefTokenType(vContestLiveUrl);
						String vTokenValue = checkForHrefTokenParam(vContestLiveUrl);
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							String encodedUrl = getPersonalizedEnocodedURL(vContestLiveUrl, vTokenValue,tokenType);
							
							request = new HttpRequest( vContestLiveUrl,encodedUrl,true, Constants.GET_METHOD );
							
						}else{
							request = new HttpRequest( vContestLiveUrl, Constants.GET_METHOD );
						}
						
					}else{
						
						request = new HttpRequest( vContestLiveUrl, Constants.GET_METHOD );
						
					}
					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();


						
						if(request.getStatusCode() == 401){
							
							callTheCredentialsUrl(strBuffer);
							
							if(runnableList != null && runnableList.containsKey(vContestLiveUrl)){



								runnableList.remove(vContestLiveUrl);
							}
							
							return;


							
							
						}else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null && strBuffer.toString().trim().length() > 0) {



							//parse json for leagues

							isUpdated	=	true;
							JsonUtil jsonUtil= new JsonUtil();
							jsonUtil.setCompetitionLiveId(null);
							jsonUtil.setRoundContestId(null);
							jsonUtil.parse(strBuffer, Constants.TYPE_CONTEST_JSON, false );
							jsonUtil = null;
							
							if(strBuffer!=null) {
								strBuffer.setLength(0);
								strBuffer = null;
							}

						}
						



						request = null;
					} catch (RequestRepeatException e) {

						isUpdated	=	false;
					}

					if(runnableList != null && runnableList.containsKey(vContestLiveUrl)){

						runnableList.remove(vContestLiveUrl);
					}
					if( isUpdated ){
						Message msg = new Message ();
						msg.obj = "LiveMatches";
						Bundle b = new Bundle();
						b.putString("vContestLiveUrl", vContestLiveUrl);
						b.putBoolean("isHref", isHref);
						msg.setData(b);					
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );

					}else{

						Message msg = new Message ();
						msg.obj = "RefreshMatches";
						Bundle b = new Bundle();
						b.putString("vContestLiveUrl", vContestLiveUrl);
						b.putBoolean("isHref", isHref);
						msg.setData(b);					
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}
			}
		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}


	
	
	/**
	 * getting the league data
	 */
	public Runnable getLeagueData(final String vCompetitionUrl,final Hashtable<String, Runnable> runnableList,final boolean isHref) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(vCompetitionUrl)){
							runnableList.remove(vCompetitionUrl);
						}
						
						return;
						
					}
						
					boolean isUpdated	=	false;

					HttpRequest request = null;
					
					
					if(isHref) {
						int tokenType = checkForHrefTokenType(vCompetitionUrl);
						String vTokenValue = checkForHrefTokenParam(vCompetitionUrl);
						
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							String encodedUrl = getPersonalizedEnocodedURL(vCompetitionUrl, vTokenValue,tokenType);
							
							request = new HttpRequest( vCompetitionUrl,encodedUrl,true, Constants.GET_METHOD );
						}else{
							
							request = new HttpRequest( vCompetitionUrl, Constants.GET_METHOD );
							
						}
						
					}else{
						
						request = new HttpRequest( vCompetitionUrl, Constants.GET_METHOD );
						
					}
					
					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);
							if(runnableList != null && runnableList.containsKey(vCompetitionUrl)){
								runnableList.remove(vCompetitionUrl);
							}
							
							return;
						}
						else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

							//parse json for leagues

							isUpdated	=	true;
							JsonUtil jsonUtil= new JsonUtil();
							jsonUtil.parse(strBuffer, Constants.TYPE_LEAGUE_ITEM, false );
							jsonUtil = null;
							
							if(strBuffer!=null) {
								strBuffer.setLength(0);
								strBuffer = null;
							}

						}
						



						request = null;
					} catch (RequestRepeatException e) {


						isUpdated	=	false;
					}

					if(runnableList != null && runnableList.containsKey(vCompetitionUrl)){


						runnableList.remove( vCompetitionUrl );
					}
					if(isUpdated){
						Message msg = new Message ();
						msg.obj = "refreshFavouriteTiles";							
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}
		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}
	
	
	
	
	
	/**
	 * Getting the leagues data from the server
	 **/
	public Runnable getLeagues ( final String vSportsId,final Hashtable<String, Runnable> runnableList) {
		
		
		//Log.e("123", "INSIDE GERT LEAGUESSSSSSSSSSSSS");
		
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					boolean isUpdated	=	false;
					
					
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && (runnableList.containsKey(Constants.GET_LEAGUES) || 
								runnableList.containsKey(vSportsId + "fetchLeagues"))){

							runnableList.remove( vSportsId + "fetchLeagues" );
							runnableList.remove(Constants.GET_LEAGUES);
						}
						
						
						return;
					}
					
					
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result  = dbUtil.getLeagueUrlFromSportsId ( vSportsId );
					String vSportsCompetitionUrl = null;
					HttpRequest request = null;
					
					
					
					
					if(result  != null && result.containsKey("isHref")){
						
						
						
						vSportsCompetitionUrl = (String) result.get("url");
						
						if((Boolean) result.get("isHref")){
							
							
							int tokenType = checkForHrefTokenType(vSportsCompetitionUrl);
							String vTokenValue = checkForHrefTokenParam(vSportsCompetitionUrl);
							
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								
								String encodedUrl = getPersonalizedEnocodedURL(vSportsCompetitionUrl, vTokenValue,tokenType);
								
								new HttpRequest( vSportsCompetitionUrl,encodedUrl,true, Constants.GET_METHOD );
								
							}else{
								
								 request = new HttpRequest( vSportsCompetitionUrl, Constants.GET_METHOD );
								
							}
							
							
						}else{
							 request = new HttpRequest( vSportsCompetitionUrl, Constants.GET_METHOD );
						}
						
						
					}else{
						return;
					}

					

					
					dbUtil = null;
					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
						
							callTheCredentialsUrl(strBuffer);
							
							if(runnableList!=null && (runnableList.containsKey(Constants.GET_LEAGUES) || 
									runnableList.containsKey(vSportsId + "fetchLeagues"))){

								runnableList.remove( vSportsId + "fetchLeagues" );
								runnableList.remove(Constants.GET_LEAGUES);
							}
							
							return;
							
							
						}else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

							
							isUpdated	=	true;
							//parse json for leagues
							//						DatabaseUtil.getInstance().clearCompetitionData(vSportsId);
							JsonUtil jsonUtil= new JsonUtil();
							jsonUtil.setSportsId( vSportsId );
							jsonUtil.parse(strBuffer, Constants.TYPE_LEAGUE_JSON, false );
							jsonUtil = null;
							
							if(strBuffer!=null) {
								strBuffer.setLength(0);
								strBuffer = null;
							}

						}
						

						vSportsCompetitionUrl = null;
						request = null;
					} catch (RequestRepeatException e) {

						isUpdated	=	false;
					}

					if(runnableList!=null && (runnableList.containsKey(Constants.GET_LEAGUES) || 
							runnableList.containsKey(vSportsId + "fetchLeagues"))){

						runnableList.remove( vSportsId + "fetchLeagues" );
						runnableList.remove(Constants.GET_LEAGUES);
					}
					
					Message msg = new Message ();
					msg.obj = "LeagueSelectionFragment";
						
					if(isUpdated){
						msg.arg1 = 1;
					}else{					
						msg.arg1 = 0;					
					}
					
					PlayupLiveApplication.callUpdateOnFragments( msg );
				} catch (Exception e) {
					//Logs.show(e);
				}finally{
					
					if(runnableList!=null && (runnableList.containsKey(Constants.GET_LEAGUES) || 
							runnableList.containsKey(vSportsId + "fetchLeagues"))){

						runnableList.remove( vSportsId + "fetchLeagues" );
						runnableList.remove(Constants.GET_LEAGUES);
					}
					
				}
				
				
			}
		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}




	public Runnable getRoundDataForFixtures ( final String vRoundId,final ArrayList<String> rounds) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						return;
					}
						
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					boolean doUpdate = true;
					
					Hashtable<String, Object> result = dbUtil.getRoundUrl ( vRoundId );
					String vRoundUrl = null;
					HttpRequest request = null;
					
					if(result != null && result.containsKey("isHref")){
						
						
						vRoundUrl = (String) result.get("url");
						if((Boolean) result.get("isHref")){
							int tokenType = checkForHrefTokenType(vRoundUrl);
							String vTokenValue = checkForHrefTokenParam(vRoundUrl);
							
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								
								String encodedUrl = getPersonalizedEnocodedURL(vRoundUrl, vTokenValue,tokenType);
								
								request = new HttpRequest( vRoundUrl,encodedUrl,true, Constants.GET_METHOD );
								
								
							}else{
								
								request = new HttpRequest( vRoundUrl, Constants.GET_METHOD );
								
							}
							
						}else{
							
							request = new HttpRequest( vRoundUrl, Constants.GET_METHOD );
						}
						
						
						
						
					}else{
						return;
					}
					dbUtil = null;


					 

					try {

						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
							callTheCredentialsUrl(strBuffer);
							return;
						}else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null ) {

							if ( strBuffer.toString().trim().length() > 0 ) {


								//parse json for leagues
								JsonUtil json = new JsonUtil();
								json.setCompetitionRoundId( null );
								json.parse(strBuffer, Constants.TYPE_ROUND_JSON, false );

								String newRoundId = json.getRoundId();
								getContestsDataForFixtures ( json.getRoundContestId ( ), newRoundId,null,false);
								doUpdate = false;
								if(strBuffer!=null) {
									strBuffer.setLength(0);
									strBuffer = null;
								}

							}


						}

						request = null;
					} catch (RequestRepeatException e) {
						//Logs.show ( e );
					}catch (Exception e) {
						//Logs.show(e);
					}finally{
						if(rounds != null && rounds.size() > 0){
							rounds.remove(0);					
						}

					}


					
					{
						Message msg = new Message ();
						
						Bundle data = new Bundle();
						
						data.putString("RoundUpdate","RoundUpdate");
						
						if(doUpdate)
							data.putString("ContestsUpdate","ContestsUpdate");					
						
						
						data.putString("vRoundId",vRoundId);
						msg.obj = "RoundDataForFixtures";
						
						msg.setData(data);
						
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );

					}
				} catch (Exception e) {
					//Logs.show(e);
				}finally{
					
					
					
				}

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		return runnable;
	}







	/**
	 * Getting the current round data from the server
	 **/
	public Runnable getContestsData ( final String vRoundContestId, final String vRoundId,final Hashtable<String, Runnable> runnableList ) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					
					if(Constants.isFetchingCredentials){
						
						if(runnableList!=null&&runnableList.containsKey(Constants.GET_CONTEST_DATA)){
							runnableList.remove(Constants.GET_CONTEST_DATA);
						}
						
						return;
					}
					
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String vRoundContestUrl = null;
					HttpRequest request = null;
					Hashtable<String, Object> result = dbUtil.getRoundcontestUrl ( vRoundContestId );
					
					if(result != null && result.containsKey("isHref")){
						vRoundContestUrl = (String) result.get("url");
						if((Boolean) result.get("isHref")){
							int tokenType = checkForHrefTokenType(vRoundContestUrl);
							String vTokenValue = checkForHrefTokenParam(vRoundContestUrl);
							
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								
								String encodedUrl = getPersonalizedEnocodedURL(vRoundContestUrl, vTokenValue,tokenType);
								
								request = new HttpRequest( vRoundContestUrl,encodedUrl,true, Constants.GET_METHOD );
								
							}else{
								request = new HttpRequest( vRoundContestUrl, Constants.GET_METHOD );
							}
							
						}else{
						
							request = new HttpRequest( vRoundContestUrl, Constants.GET_METHOD );
						}
						
					}else{
						return;
					}



					dbUtil = null;
					
					 
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
							callTheCredentialsUrl(strBuffer);
							if(runnableList!=null&&runnableList.containsKey(Constants.GET_CONTEST_DATA)){
								runnableList.remove(Constants.GET_CONTEST_DATA);
							}
							return;
						}else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && 
								strBuffer != null && strBuffer.toString().trim().length() > 0 ) {

							//parse json for contests
							JsonUtil json  =new JsonUtil();
							json.parse(strBuffer, Constants.TYPE_ROUND_CONTEST_JSON, false );
							json = null;
							

							if(strBuffer!=null) {
								strBuffer.setLength(0);
								strBuffer = null;
							}

						}
						vRoundContestUrl = null;
						request = null;
					} catch (RequestRepeatException e) {

					//	Logs.show( e );
						if(runnableList!=null&&runnableList.containsKey(Constants.GET_CONTEST_DATA)){
							runnableList.remove(Constants.GET_CONTEST_DATA);
						}
						

					}
					Message msg = new Message ();
					msg.obj = "RoundUpdate";
					PlayupLiveApplication.callUpdateOnFragments( msg );
				} catch (Exception e) {
					//Logs.show(e);
				}finally{
					if(runnableList!=null&&runnableList.containsKey(Constants.GET_CONTEST_DATA)){
						runnableList.remove(Constants.GET_CONTEST_DATA);
					}
				}

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}

	public Runnable getContestsDataForFixtures ( final String vRoundContestId, final String vRoundId,
			final Hashtable<String, Runnable> runnableList ,final boolean fromRefresh) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				

				try {
					if(Constants.isFetchingCredentials){
						if(runnableList != null && runnableList.containsKey(vRoundContestId))
							runnableList.remove(vRoundContestId);
						

					}
					
					boolean isUpdated	=	false;
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					
					Hashtable<String, Object> result = dbUtil.getRoundcontestUrl ( vRoundContestId );
					String vRoundContestUrl = null;
					HttpRequest request = null;
					
					if(result != null && result.containsKey("isHref")){
						
						vRoundContestUrl = (String) result.get("url");
						
						if((Boolean) result.get("isHref")){
							String vTokenValue = checkForHrefTokenParam(vRoundContestUrl);
							int tokenType = checkForHrefTokenType(vRoundContestUrl);
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								
								String encodedUrl = getPersonalizedEnocodedURL(vRoundContestUrl, vTokenValue,tokenType);
								request = new HttpRequest( vRoundContestUrl,encodedUrl,true, Constants.GET_METHOD );
								
							}else{
								request = new HttpRequest( vRoundContestUrl, Constants.GET_METHOD );
								
							}
						}else{
							
							request = new HttpRequest( vRoundContestUrl, Constants.GET_METHOD );
							
						}
						
						
					}else{
						return;
					}
					
					

					dbUtil = null;
					
					 request = new HttpRequest( vRoundContestUrl, Constants.GET_METHOD );
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
							
							callTheCredentialsUrl(strBuffer);
							if(runnableList != null && runnableList.containsKey(vRoundContestId))
								runnableList.remove(vRoundContestId);
							
							return;
							
						}else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && 
								strBuffer != null && strBuffer.toString().trim().length() > 0 ) {

							//parse json for contests
							JsonUtil json  =new JsonUtil();
							json.parse(strBuffer, Constants.TYPE_ROUND_CONTEST_JSON, false );
							json = null;
							isUpdated	=	true;

							if(strBuffer!=null) {
								strBuffer.setLength(0);
								strBuffer = null;
							}

						}
						vRoundContestUrl = null;
						request = null;
					} catch (RequestRepeatException e) {

					//	Logs.show( e );

						isUpdated	=	false;

					}

					if(runnableList != null && runnableList.containsKey(vRoundContestId))
						runnableList.remove(vRoundContestId);
					isUpdated = true;
					if(fromRefresh){
						if(isUpdated){
							Message msg = new Message ();
							Bundle data = new Bundle();						
							data.putString("vRoundId",vRoundId);
							data.putString("ContestsUpdate","ContestsUpdate");
							msg.obj = "RoundDataForFixtures";
							msg.setData(data);
							PlayupLiveApplication.callUpdateOnFragments( msg );
						}
					}else{
						Message msg = new Message ();
						Bundle data = new Bundle();		
						data.putString("vRoundId",vRoundId);
						data.putString("ContestsUpdate","ContestsUpdate");
						msg.obj = "RoundDataForFixtures";
						msg.setData(data);
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
					}
				} catch (Exception e) {
					//Logs.show(e);
				}finally{
					
					if(runnableList != null && runnableList.containsKey(vRoundContestId))
						runnableList.remove(vRoundContestId);
					
				}



			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}

	/**
	 * Getting the current round data from the server
	 **/
	public Runnable getContestsData ( final String vContestUrl ,final Hashtable<String, Runnable> runnableList,
			final boolean isHref) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				if(Constants.isFetchingCredentials){
					
					if(runnableList != null && runnableList.containsKey(Constants.GET_CONTEST_CONVERSATION_MESSAGES)){
						runnableList.remove(Constants.GET_CONTEST_CONVERSATION_MESSAGES);
					}
					
					return;
					
				}
				
				HttpRequest request = null;
					
				if(isHref){
					int tokenType = checkForHrefTokenType(vContestUrl);
					String vTokenValue = checkForHrefTokenParam(vContestUrl);
					
					if(vTokenValue != null && vTokenValue.trim().length() > 0){
						
						String encodedUrl = getPersonalizedEnocodedURL(vContestUrl, vTokenValue,tokenType);
						request = new HttpRequest( vContestUrl,encodedUrl,true, Constants.GET_METHOD );
						
					}else{
						request = new HttpRequest( vContestUrl, Constants.GET_METHOD );
					}
				}else{
					request = new HttpRequest( vContestUrl, Constants.GET_METHOD );
				}
				
				
				try {
					StringBuffer strBuffer = (StringBuffer) request.send();

					if(request.getStatusCode()==401){
						callTheCredentialsUrl(strBuffer);
							if(runnableList != null && runnableList.containsKey(Constants.GET_CONTEST_CONVERSATION_MESSAGES)){
						runnableList.remove(Constants.GET_CONTEST_CONVERSATION_MESSAGES);
					}
						return;
					}
					else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null && strBuffer.toString().trim().length() > 0 ) {

						//parse json for contests
						JsonUtil json = new JsonUtil() ;
						json.shouldParseLaegue(true);
						json.parse(strBuffer, Constants.TYPE_CONTEST_JSON, false );



						if(strBuffer!=null)
							strBuffer.setLength(0);

					}
				} catch (RequestRepeatException e) {

					//Logs.show ( e );

				}

				if(runnableList!=null&&runnableList.containsKey(Constants.GET_CONTEST_CONVERSATION_MESSAGES)){
					runnableList.remove(Constants.GET_CONTEST_CONVERSATION_MESSAGES);
				}

				Message msg = new Message ();
				msg.obj = "MatchHomeFragment_getScores";
				Bundle bundle = new Bundle();
				bundle.putString( "vContestUrl", vContestUrl );
				msg.setData( bundle );
				PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}



	
	/**CHKED

	 * Praveen : as per the href
	 */
	public Runnable getContestsDataForSections ( final String vContestUrl ,final Boolean isHrefURL) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				try {
					if(Constants.isFetchingCredentials){


						
						return;
						
					}
					
					HttpRequest request = null;

					if(isHrefURL){
						
						String vTokenValue = checkForHrefTokenParam(vContestUrl);
						int tokenType = checkForHrefTokenType(vContestUrl);
						
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							
							String encodeUrl = getPersonalizedEnocodedURL(vContestUrl, vTokenValue,tokenType);
							
							request = new HttpRequest( vContestUrl,encodeUrl,true, Constants.GET_METHOD );
							
						}else{
							request = new HttpRequest( vContestUrl, Constants.GET_METHOD );
						}
						
					}else{
					
						
						request = new HttpRequest( vContestUrl, Constants.GET_METHOD );
						
					}
					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
							callTheCredentialsUrl(strBuffer);

							return;
						
						}else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null && strBuffer.toString().trim().length() > 0 ) {

							//parse json for contests
							JsonUtil json = new JsonUtil() ;
							json.shouldParseLaegue(false);
							json.parse(strBuffer, Constants.TYPE_CONTEST_JSON, false );


							if(strBuffer!=null)
								strBuffer.setLength(0);

						}
					} catch (RequestRepeatException e) {

						//Logs.show ( e );

					}




					Message msg = new Message ();
					msg.obj = "MatchHomeFragment_getScores";
					Bundle bundle = new Bundle();
					bundle.putString( "vContestUrl", vContestUrl );
//				bundle.putBoolean( "isHref", isHref );
					msg.setData( bundle );
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}

	/** CHECKED
	 * Getting the current round data from the server
	 * @param isHrefURL 
	 **/

	public Runnable getPrivateContestsData ( final String vContestUrl ,final boolean isHrefURL, final Hashtable<String, Runnable> runnableList,final boolean fromrefresh) {

//	public Runnable getPrivateContestsData ( final String vContestUrl ,final Hashtable<String, Runnable> runnableList,
//			final boolean fromrefresh,final boolean isHref) {


		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(vContestUrl)){
							runnableList.remove(vContestUrl);
						}
						
						return;
						
					}
					
					boolean isUpdated = false;
					
					HttpRequest request = null;

					String encodedHrefURL	= null;
					String vTokenValue		= 	null;
					if(isHrefURL){
						
						int tokenType = checkForHrefTokenType(vContestUrl);
						 vTokenValue = new Util().checkForHrefTokenParam(vContestUrl); 
						 encodedHrefURL = new Util().getPersonalizedEnocodedURL(vContestUrl,vTokenValue,tokenType);
						
						request = new HttpRequest(vContestUrl,encodedHrefURL,isHrefURL,Constants.GET_METHOD );
						
					}else{
						
						 request = new HttpRequest( vContestUrl,Constants.GET_METHOD );
						
					}
					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);

							if(runnableList != null && runnableList.containsKey(vContestUrl)){
								runnableList.remove(vContestUrl);
							}
							return;
							
						}else{
							
							
							if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null && strBuffer.toString().trim().length() > 0 ) {

								//parse json for contests
								JsonUtil json = new JsonUtil() ;
								json.parse(strBuffer, Constants.TYPE_CONTEST_JSON, false );

								isUpdated	=	true;

								if(strBuffer!=null)
									strBuffer.setLength(0);

							}
						}
						
						
					} catch (RequestRepeatException e) {
						//Logs.show ( e );


					}

					if(runnableList!=null&&runnableList.containsKey(vContestUrl)){
						runnableList.remove(vContestUrl);
					}
					if(fromrefresh){
						if ( isUpdated ) {
							Message msg = new Message ();
							msg.obj = "MatchHomeFragment_getScores";
							Bundle bundle = new Bundle();
							bundle.putString( "vContestUrl", vContestUrl );
							msg.setData( bundle );
							PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
						}
					}else{
						Message msg = new Message ();
						msg.obj = "MatchHomeFragment_getScores";
						Bundle bundle = new Bundle();
						bundle.putString( "vContestUrl", vContestUrl );
						msg.setData( bundle );
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}


	/**
	 * CHKED
	 * Getting the current round data from the server
	 **/
	public Runnable getScoreData ( final String vContestUrl ,final Hashtable<String, Runnable> runnableList,final boolean isHref) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(Constants.GET_SCORE)){
							runnableList.remove(Constants.GET_SCORE);
						}
						
						return;
						
					}
						
					boolean isUpdated	=	true;
					
					HttpRequest request = null;
					
					if(isHref){
						int tokenType = checkForHrefTokenType(vContestUrl);
						String vTokenValue = checkForHrefTokenParam(vContestUrl);
						
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							
							String encodedUrl = getPersonalizedEnocodedURL(vContestUrl, vTokenValue,tokenType);
							
							request = new HttpRequest( vContestUrl,encodedUrl,true, Constants.GET_METHOD );
							
						}else{
							request = new HttpRequest( vContestUrl, Constants.GET_METHOD );
						}
						
					}else{
						
						request = new HttpRequest( vContestUrl, Constants.GET_METHOD );
						
					}

					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();

						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);
								if(runnableList != null && runnableList.containsKey(Constants.GET_SCORE)){
							runnableList.remove(Constants.GET_SCORE);
							}
							return;
						}
						else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null  && strBuffer.toString().trim().length() > 0) {

							//parse json for contests

							JsonUtil json = new JsonUtil() ;
							json.parse(strBuffer, Constants.TYPE_CONTEST_JSON, false );


							isUpdated	=	false;


							if(strBuffer!=null)
								strBuffer.setLength(0);

						}
					} catch (RequestRepeatException e) {


						isUpdated	=	false;
					//	Logs.show ( e );
					}


					if(runnableList!=null&&runnableList.containsKey(Constants.GET_SCORE)){
						runnableList.remove(Constants.GET_SCORE);
					}

					Message msg = new Message ();
					
					
					msg.obj = "MatchHomeFragment_getScores";

					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}



	/**chked
	 * Getting the current round data from the server
	 **/
	public Runnable getContestLobby ( final String vContestLobbyUrl , final Hashtable<String, Runnable> runnableList,
			final boolean isHref) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(Constants.GET_CONTEST_LOBBY)){
							runnableList.remove(Constants.GET_CONTEST_LOBBY);
						}
						
						return;
						
					}
						
					String vContestId = null;
					
					HttpRequest request = new HttpRequest( vContestLobbyUrl, Constants.GET_METHOD );
					
					
					if(isHref){
						String vTokenValue = checkForHrefTokenParam(vContestLobbyUrl);
						int tokenType = checkForHrefTokenType(vContestLobbyUrl);
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							
							String encodedUrl = getPersonalizedEnocodedURL(vContestLobbyUrl, vTokenValue,tokenType);
							
							request = new HttpRequest( vContestLobbyUrl,encodedUrl,true, Constants.GET_METHOD );
						}else{
							request = new HttpRequest( vContestLobbyUrl, Constants.GET_METHOD );
						}
						
					}else{
						request = new HttpRequest( vContestLobbyUrl, Constants.GET_METHOD );
					}
					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						
						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);
								if(runnableList != null && runnableList.containsKey(Constants.GET_CONTEST_LOBBY)){
							runnableList.remove(Constants.GET_CONTEST_LOBBY);
						}
							return;
						}
						else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null && strBuffer.toString().trim().length() > 0 ) {

							//parse json for contests

							
							JsonUtil json = new JsonUtil() ;
							json.parse(strBuffer, Constants.TYPE_CONTEST_LOBBY_JSON, false );
							
							vContestId = json.getContestId();



							if(strBuffer!=null)
								strBuffer.setLength(0);


						}
					} catch (RequestRepeatException e) {
						
					}

					if(runnableList != null && runnableList.containsKey(Constants.GET_CONTEST_LOBBY)){
						runnableList.remove(Constants.GET_CONTEST_LOBBY);
					}
					
					
						Message msg = new Message();
						msg.obj = "ContestLobbyData";
						
						if(vContestId != null){
						Bundle b = new Bundle();
						b.putString("vContestId", vContestId);
						msg.setData(b);
						}
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
				
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}


	/**chked
	 * Getting the current round data from the server
	 **/
	public Runnable getContestLobbyData ( final String vContestLobbyUrl ,
			final Hashtable<String, Runnable> runnableList,final boolean fromRefresh,final boolean isHref) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(Constants.GET_CONTEST_LOBBY)){
							runnableList.remove(Constants.GET_CONTEST_LOBBY);
						}
						
						return;
						
					}
						
					boolean isUpdated = false;

					String vContestId = null;
					HttpRequest request = null;
					
					if(isHref){
						int tokenType = checkForHrefTokenType(vContestLobbyUrl);
						String vTokenValue = checkForHrefTokenParam(vContestLobbyUrl);
						
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							
							String encodedUrl = getPersonalizedEnocodedURL(vContestLobbyUrl, vTokenValue,tokenType);
							request = new HttpRequest( vContestLobbyUrl,encodedUrl,true, Constants.GET_METHOD );
							
							
						}else{
							request = new HttpRequest( vContestLobbyUrl, Constants.GET_METHOD );
						}
						
					}else{
						
						request = new HttpRequest( vContestLobbyUrl, Constants.GET_METHOD );
						
					}
					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);
								if(runnableList != null && runnableList.containsKey(Constants.GET_CONTEST_LOBBY)){
							runnableList.remove(Constants.GET_CONTEST_LOBBY);
						}
							return;
						}
						else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null && strBuffer.toString().trim().length() > 0 ) {

							//parse json for contests

							isUpdated = true;
							JsonUtil json = new JsonUtil() ;
							json.setLinkUrl ( vContestLobbyUrl );
							json.parse(strBuffer, Constants.TYPE_CONTEST_LOBBY_JSON, false );
							if(!fromRefresh)
								vContestId = json.getContestId();

							if(strBuffer!=null)
								strBuffer.setLength(0);

						}
					} catch (RequestRepeatException e) {
						isUpdated = false;
						//Logs.show ( e );
					}

					if(runnableList!=null&&runnableList.containsKey(Constants.GET_CONTEST_LOBBY)){
						runnableList.remove(Constants.GET_CONTEST_LOBBY);
					}

					
					if(fromRefresh){
						
						if(isUpdated){
							
							
							Message msg = new Message();
							msg.obj = "ContestLobbyDataRefresh";				
							PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
							
							
						}
						
					}
					else{
						if(isUpdated){
							Message msg = new Message();
							msg.obj = "ContestLobbyData";

							Bundle b  = new Bundle ();
							b.putString( "vContestId", vContestId );
							msg.setData( b );
							PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}


	/**chked
	 * Getting live contest for the specific sport
	 **/
	public Runnable getLiveSports ( final String vSportsId,final Hashtable<String, Runnable> runnableList) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				
				try {
					if(Constants.isFetchingCredentials){
						if(runnableList!=null&&runnableList.containsKey(Constants.GET_LIVE_SPORTS)){
							runnableList.remove(Constants.GET_LIVE_SPORTS);
						}
						
						return;
						
					}

					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result = dbUtil.getSportsLiveUrl ( vSportsId );
					HttpRequest request = null;
					String vSportsLiveUrl = null;
					if(result != null && result.containsKey("isHref")){
						
						 vSportsLiveUrl = (String) result.get("url");
						 
						 if((Boolean) result.get("isHref")){
							 int tokenType = checkForHrefTokenType(vSportsLiveUrl);
							 String vTokenValue = checkForHrefTokenParam(vSportsLiveUrl);
							 
							 if(vTokenValue != null && vTokenValue.trim().length() > 0){
								 
								 String encodedUrl = getPersonalizedEnocodedURL(vSportsLiveUrl, vTokenValue,tokenType);
								 
								 request = new HttpRequest( vSportsLiveUrl,encodedUrl,true, Constants.GET_METHOD );
								 
							 }else{
								 
								 request = new HttpRequest( vSportsLiveUrl, Constants.GET_METHOD );
								 
							 }
							 
							 
							 
							 
						 }else{
							 
							  request = new HttpRequest( vSportsLiveUrl, Constants.GET_METHOD );
							 
						 }
						
					}else{
						
						return;
					}

					dbUtil = null;
					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
							
							callTheCredentialsUrl(strBuffer);
							if(runnableList!=null&&runnableList.containsKey(Constants.GET_LIVE_SPORTS)){
								runnableList.remove(Constants.GET_LIVE_SPORTS);
							}
							
							return;
						}else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null && strBuffer.toString().trim().length() > 0 ) {


							JsonUtil json = new JsonUtil() ;
							json.parse(strBuffer, Constants.TYPE_SPORTS_LIVE_JSON, false );


							if(strBuffer!=null) {
								strBuffer.setLength(0);
								strBuffer = null;
							}

						}
						vSportsLiveUrl = null;
						request = null;
					} catch (RequestRepeatException e) {
						//Logs.show ( e );
					}

					if(runnableList!=null&&runnableList.containsKey(Constants.GET_LIVE_SPORTS)){
						runnableList.remove(Constants.GET_LIVE_SPORTS);
					}

					Message msg = new Message ();
					msg.obj = "getLiveSports";
					PlayupLiveApplication.callUpdateOnFragments( msg );
				} catch (Exception e) {
					//Logs.show(e);
				}finally{
					
					if(runnableList!=null&&runnableList.containsKey(Constants.GET_LIVE_SPORTS)){
						runnableList.remove(Constants.GET_LIVE_SPORTS);
					}
				}
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}


	/** chked
	 * Getting competition round data
	 **/
	public Runnable getCompetitionRoundData ( final String vCompetitionId ,final Hashtable<String, Runnable> runnableList) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String vCompetitionRoundUrl = dbUtil.getCompetitionRoundUrl ( vCompetitionId );
					dbUtil = null;

					boolean isUpdateAvailable	=	false;
					HttpRequest request = new HttpRequest( vCompetitionRoundUrl, Constants.GET_METHOD );
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null && strBuffer.toString().trim().length() > 0 ) {

							//parse json for contests
							JsonUtil json = new JsonUtil() ;
							json.parse(strBuffer, Constants.TYPE_ALL_ROUND_JSON, false );

							isUpdateAvailable	=	true;
						}


						if ( strBuffer != null && strBuffer.toString().trim().length() > 0 ) {
							if(strBuffer!=null) {
								strBuffer.setLength(0);
								strBuffer = null;
							}

						}

						vCompetitionRoundUrl = null;
						request = null;
					} catch (RequestRepeatException e) {
						//Logs.show ( e );
					}

					if(runnableList!=null&&runnableList.containsKey(Constants.GET_COMPETITION_ROUND_DATA))
						runnableList.remove(Constants.GET_COMPETITION_ROUND_DATA);

					if(isUpdateAvailable){

						Message msg = new Message ();
						msg.obj = "CalendarUpdate";
						PlayupLiveApplication.callUpdateOnFragments( msg );
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;

	}


/**
 * Chked
 * @param vCurrentSeasonUrl
 * @param runnableList
 * @param fromRefresh
 * @param isHref
 * @return
 */
	public Runnable getCurrentSeasonData ( final String vCurrentSeasonUrl,
			final Hashtable<String, Runnable> runnableList,final boolean fromRefresh,
			final boolean isHref) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				
				try {
					if(Constants.isFetchingCredentials){
						if(runnableList!=null&&runnableList.containsKey(Constants.GET_CURRENT_SEASON_DATA))
							runnableList.remove(Constants.GET_CURRENT_SEASON_DATA);
						
						return;
						
					}
						

					boolean isUpdateAvailable	=	false;
					
					HttpRequest request = null;
					
					if(isHref){
						 int tokenType = checkForHrefTokenType(vCurrentSeasonUrl);
						String vTokenValue = checkForHrefTokenParam(vCurrentSeasonUrl);
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							String encodedUrl = getPersonalizedEnocodedURL(vCurrentSeasonUrl, vTokenValue,tokenType);
							request = new HttpRequest( vCurrentSeasonUrl,encodedUrl,true,Constants.GET_METHOD );
							
						}else{
							request = new HttpRequest( vCurrentSeasonUrl,Constants.GET_METHOD );
						}
						
					}else{
						request = new HttpRequest( vCurrentSeasonUrl,Constants.GET_METHOD );
					}
					
					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
							
							callTheCredentialsUrl(strBuffer);
							
							
							if(runnableList!=null&&runnableList.containsKey(Constants.GET_CURRENT_SEASON_DATA))
								runnableList.remove(Constants.GET_CURRENT_SEASON_DATA);
							
							return;
							
							
							
						}else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null && 
								strBuffer.toString().trim().length() > 0 ) {

							JsonUtil json = new JsonUtil() ;
							json.parse(strBuffer, Constants.TYPE_CURRENT_SEASON_JSON, false );

							isUpdateAvailable	=	true;
							
							if ( strBuffer != null && strBuffer.toString().trim().length() > 0 ) {
								if(strBuffer!=null) {
									strBuffer.setLength(0);
									strBuffer = null;
								}

							}


						}

						request = null;
					} catch (RequestRepeatException e) {

					}

					if(runnableList!=null&&runnableList.containsKey(Constants.GET_CURRENT_SEASON_DATA))
						runnableList.remove(Constants.GET_CURRENT_SEASON_DATA);

					
					
					
					
					if(fromRefresh){
						if(isUpdateAvailable){
							Message msg = new Message ();
							msg.obj = "CalendarUpdateRefresh";
							PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
						
					
						}
					}else{
					
					Message msg = new Message ();
					msg.obj = "CalendarUpdate";
					
					
					if(isUpdateAvailable){
						
						msg.arg1 = 1;
						
					}else{
						
						msg.arg1 = 0;
						
					}
					
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
					
					
					}
				} catch (Exception e) {
					//Logs.show(e);
				}finally{
					if(runnableList!=null&&runnableList.containsKey(Constants.GET_CURRENT_SEASON_DATA))
						runnableList.remove(Constants.GET_CURRENT_SEASON_DATA);
				}
				
				

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;

	}

	/**
	 * 
	 * Release Memory Hold By Json Objects
	 */
	public void releaseMemory(JSONObject mJsonObject){

		try {
			JSONArray keyName	=	mJsonObject.names();
			int i				=	0;
			while(i<keyName.length()){
				try {
					mJsonObject.remove(keyName.getString(i));
				} catch (JSONException e) {
				}
				i++;
			}
			mJsonObject	=	null;
		} catch (Exception e) {
		//	Logs.show(e);
		}
		
	}

/**
 * chked
 * @param gapUrl
 * @param gapUid
 * @param runnableList
 * @return
 */
	public Runnable callNextFriendsUrl(final String gapUrl,
			final String gapUid,final Hashtable<String, Runnable> runnableList,final boolean isHref ) {

		Runnable runnable = new Runnable() {
			int status = 0;
			@Override
			public void run() {
				
				if(Constants.isFetchingCredentials){
					if(runnableList != null && runnableList.containsKey(gapUrl)){
						runnableList.remove(gapUrl);
					}
					return;
					
				}
				
				Constants.isFriendsGapDownloading = true;

				HttpRequest request = null;
				
				
			//	Log.e("123","gapUrl >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   "+gapUrl);
				
				if(isHref){
					String vTokenValue = checkForHrefTokenParam(gapUrl);
					 int tokenType = checkForHrefTokenType(gapUrl);
					if(vTokenValue != null && vTokenValue.trim().length() > 0){
						String encodedUrl = getPersonalizedEnocodedURL(gapUrl, vTokenValue,tokenType);
						request = new HttpRequest(gapUrl,encodedUrl,true, Constants.GET_METHOD );
					}else{
						request = new HttpRequest(gapUrl, Constants.GET_METHOD );
					}
					
					
				}else{
					request = new HttpRequest(gapUrl, Constants.GET_METHOD );
					
				}

				try {
					StringBuffer strBuffer = (StringBuffer) request.send();
					
					if(request.getStatusCode() == 401){
						callTheCredentialsUrl(strBuffer);
						if(runnableList != null && runnableList.containsKey(gapUrl)){
							runnableList.remove(gapUrl);
						}
						return;


					}else
					
					if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {


						JsonUtil json = new JsonUtil() ;
						json.setGapId( gapUid );
						json.setFriendsRefresh(false);
						json.parse(strBuffer, Constants.TYPE_GET_FRIENDS_JSON, false );

						if(strBuffer!=null)
							strBuffer.setLength(0);
						status = 0;
					} else {
						status = 1;
					}
				} catch (RequestRepeatException e) {
					Constants.isFriendsGapDownloading = false;
					status = 1;
					//Logs.show ( e );

				}catch (Exception e) {

					//Logs.show ( e );
					Constants.isFriendsGapDownloading = false;
					status = 1;

				}



				if(runnableList!=null&&runnableList.containsKey(gapUrl)){
					runnableList.remove(gapUrl);
				}
				Constants.isFriendsGapDownloading = false;
				Message msg = new Message ();
				msg.obj = "FriendsData";
				msg.arg1 = status;
				PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		return runnable;

	}

	/**
	 * CHKED
	 * @param gapUrl
	 * @param gapUid
	 * @param runnableList
	 * @return
	 */
	public Runnable callNextSearchFriendsUrl (final String gapUrl,
			final String gapUid,final Hashtable<String, Runnable> runnableList,final boolean isHref ) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						if(runnableList != null && runnableList.containsKey(gapUrl)){
							runnableList.remove(gapUrl);
						}
						return;
						
					}
					
					Constants.isSearchGapDownloading = true;
					HttpRequest request = null;
					if(isHref){
						
						String vTokenValue = checkForHrefTokenParam(gapUrl);
						 int tokenType = checkForHrefTokenType(gapUrl);
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							String encodedUrl = getPersonalizedEnocodedURL(gapUrl, vTokenValue,tokenType);
							request = new HttpRequest(gapUrl,encodedUrl,true, Constants.GET_METHOD );
							
						}else{
							request = new HttpRequest(gapUrl, Constants.GET_METHOD );
						}
						
					}else{
						request = new HttpRequest(gapUrl, Constants.GET_METHOD );
					}


					
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
							callTheCredentialsUrl(strBuffer);
							Constants.isSearchGapDownloading = false;
							if(runnableList != null && runnableList.containsKey(gapUrl)){
								runnableList.remove(gapUrl);
							}
							return;
							
							

						}else
						
						if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

							JsonUtil json = new JsonUtil() ;
							json.setGapId( gapUid );
							json.parse(strBuffer, Constants.TYPE_SEARCH_FRIEND_JSON, false );


							if(strBuffer!=null)
								strBuffer.setLength(0);
						}
					} catch (RequestRepeatException e) {
						Constants.isSearchGapDownloading = false;
						//Logs.show ( e );
					}catch (Exception e) {
						Constants.isSearchGapDownloading = false;
						//Logs.show ( e );


					}


					if(runnableList!=null&&runnableList.containsKey(gapUrl)){
						runnableList.remove(gapUrl);

						Constants.isSearchGapDownloading = false;
						Message msg = new Message ();
						msg.obj = "SearchFriendsData";
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}

			}


		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return  runnable;

	}
	
	
	/**
	 * Chked
	 * @param updateFriendsUrl
	 * @param isHrefURL
	 * @param playupfriends
	 * @param runnableList
	 * @return
	 */
	public Runnable getUpdateFriends(final String updateFriendsUrl,final boolean isHrefURL,final boolean playupfriends,
			final Hashtable<String, Runnable> runnableList) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(Constants.GET_UPDATE_FRIENDS)){
							runnableList.remove(Constants.GET_UPDATE_FRIENDS);
						}
						
						return;
						
					}
					
					HttpRequest request = null;
					String encodedHrefURL	= null;
					String vTokenValue		= 	null;

					boolean isUpdateAvailable	=	false;
					try {
					if(isHrefURL){
						
						 int tokenType = checkForHrefTokenType(updateFriendsUrl);
						 vTokenValue = new Util().checkForHrefTokenParam(updateFriendsUrl); 
						 if(vTokenValue != null && vTokenValue.trim().length() >0 ){
							 encodedHrefURL = new Util().getPersonalizedEnocodedURL(updateFriendsUrl,vTokenValue,tokenType);
							 request = new HttpRequest( updateFriendsUrl,encodedHrefURL,isHrefURL,Constants.GET_METHOD );
						 }else{
							 request = new HttpRequest( updateFriendsUrl,Constants.GET_METHOD );
						 }
						
						
					}else{
						
						 request = new HttpRequest( updateFriendsUrl,Constants.GET_METHOD );
						
					}

					StringBuffer strBuffer = (StringBuffer) request.send();
						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);
							if(runnableList != null && runnableList.containsKey(Constants.GET_UPDATE_FRIENDS)){
								runnableList.remove(Constants.GET_UPDATE_FRIENDS);
							}
							
							return;
						}
						else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {


							JsonUtil json = new JsonUtil() ;
							json.setPlayupFriends(playupfriends);
							json.parse(strBuffer, Constants.TYPE_LIVE_FRIEND_JSON, false );

							isUpdateAvailable	=	true;
							if(strBuffer!=null)
								strBuffer.setLength(0);

						}
					} catch (RequestRepeatException e) {
						isUpdateAvailable	=	false;
						//Logs.show ( e );
					}catch (Exception e) {
						isUpdateAvailable	=	false;
						//Logs.show ( e );
					}
					
					
					if(runnableList != null && runnableList.containsKey(Constants.GET_UPDATE_FRIENDS)){
						
						runnableList.remove(Constants.GET_UPDATE_FRIENDS);
						
					}
					
					Message msg = new Message ();
					msg.obj = "updateFriendsData";
					if(isUpdateAvailable){
						msg.arg1 = 1;
					}else{
						msg.arg1 = 0;
					}
					
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		
		return runnable;

	}


/**
 * Cked
 * @param liveFriendsUrl
 * @param isHrefURL
 * @param playupFriends
 * @param runnableList
 * @return
 */
	public Runnable getLiveFriends(final String liveFriendsUrl,final boolean isHrefURL,final boolean playupFriends, 
			final Hashtable<String, Runnable> runnableList) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				
				
				if(Constants.isFetchingCredentials){
					
					if(runnableList != null && runnableList.containsKey(Constants.GET_LIVE_FRIENDS)){
						runnableList.remove(Constants.GET_LIVE_FRIENDS);
					}
					
					return;
					
				}
					
			
				boolean isUpdateAvailable	=	false;
				HttpRequest request = null;
				String encodedHrefURL	= null;
				String vTokenValue		= 	null;
				
				
				try {
					
					if(isHrefURL){
						
					
						 vTokenValue = new Util().checkForHrefTokenParam(liveFriendsUrl); 
						 int tokenType = checkForHrefTokenType(liveFriendsUrl);
						 if(vTokenValue != null && vTokenValue.trim().length() > 0){
							 encodedHrefURL = new Util().getPersonalizedEnocodedURL(liveFriendsUrl,vTokenValue,tokenType);
							 request = new HttpRequest( liveFriendsUrl,encodedHrefURL,isHrefURL,Constants.GET_METHOD );
						 }else{
							 
							 request = new HttpRequest( liveFriendsUrl,Constants.GET_METHOD );
							 
						 }
						 
						
						
						
						
						
					}else{
						
						 request = new HttpRequest( liveFriendsUrl,Constants.GET_METHOD );
						
					}
					StringBuffer strBuffer = (StringBuffer) request.send();
					if(request.getStatusCode()==401){
						callTheCredentialsUrl(strBuffer);
						if(runnableList != null && runnableList.containsKey(Constants.GET_LIVE_FRIENDS)){
							runnableList.remove(Constants.GET_LIVE_FRIENDS);
						}
						return;
					}
					else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

						JsonUtil json = new JsonUtil() ;
						json.setPlayupFriends(playupFriends);
						json.parse(strBuffer, Constants.TYPE_LIVE_FRIEND_JSON, false );

						isUpdateAvailable	=	true;
						if(strBuffer!=null)
							strBuffer.setLength(0);




					}
				} catch (RequestRepeatException e) {
					//Logs.show ( e );
				}catch (Exception e) {
					// TODO: handle exception
					//Logs.show ( e );

				}
				
			
				if(runnableList != null && runnableList.containsKey(Constants.GET_LIVE_FRIENDS)){
					
					
					
					runnableList.remove(Constants.GET_LIVE_FRIENDS);
				}
				
				Message msg = new Message ();
				msg.obj = "liveFriendsData";
				if(isUpdateAvailable){
					msg.arg1 = 1;
				}else{
					msg.arg1 = 0;
				}
				
				PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);


			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		
		return runnable;

	}



	/**
	 * Putting and deleting the marker for the count number  ( recent activity ) 
	 */
	public void putDeleteMarker ( final boolean isPut, final String vConversationId ) {


		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				if(Constants.isFetchingCredentials){
					
					return;
					
				}

				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result = dbUtil.getMarkerUrl ( vConversationId );
					
					String vMessageId = null,vMessageUrl = null;
					JSONObject jObj = new JSONObject ();
					
					
					HttpRequest request = null;
					
					String vMarkerUrl = null;
					if(result != null && result.containsKey("isHref")){
						vMarkerUrl = (String) result.get("url");
						if ( vMarkerUrl != null && vMarkerUrl.trim().length() > 0 ){
							
							
							if ( isPut ) {
								vMessageId = dbUtil.getLatestMessageId ( vConversationId );
								vMessageUrl = dbUtil.getLatestMessageUrl ( vConversationId );

								try {
									jObj.put( ":type", "application/vnd.playup.collection.marker+json" );

									JSONObject pos = new JSONObject();
									pos.put( ":uid", vMessageId );
									pos.put( ":self", vMessageUrl );
									pos.put( ":type", "application/vnd.playup.message.text+json" );

									jObj.put( "position", pos );
								} catch ( Exception e ) {

								}
							}
							
						if((Boolean) result.get("isHref")){
							 int tokenType = checkForHrefTokenType(vMarkerUrl);
							String vTokenValue = checkForHrefTokenParam(vMarkerUrl);
							
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								
								String encodedUrl = getPersonalizedEnocodedURL(vMarkerUrl, vTokenValue,tokenType);
								
								if ( isPut ) {
									request = new HttpRequest( encodedUrl,vMarkerUrl ,true, jObj.toString(), Constants.PUT_METHOD ,null);
								} else {
									request = new HttpRequest ( vMarkerUrl ,encodedUrl,true, Constants.DELETE_METHOD );
								}
								
							}else{
									if ( isPut ) {
										request = new HttpRequest( vMarkerUrl , jObj.toString(), Constants.PUT_METHOD );
									} else {
										request = new HttpRequest ( vMarkerUrl , Constants.DELETE_METHOD );
									}
							}
							
						}else{							
								if ( isPut ) {
									request = new HttpRequest( vMarkerUrl , jObj.toString(), Constants.PUT_METHOD );
								} else {
									request = new HttpRequest ( vMarkerUrl , Constants.DELETE_METHOD );
								}
					}
						}else{
							return;
						}
					}else{
						return;
					}

						try {
							if(request != null){
							StringBuffer strBuffer = (StringBuffer) request.send();
							
							if(request.getStatusCode() == 401){
								callTheCredentialsUrl(strBuffer);
								return;
								
								

							}else
							
							if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null) {


								if(strBuffer!=null)
									strBuffer.setLength(0);
							}
							}
						} catch (RequestRepeatException e) {
						//	Logs.show ( e );
						}catch (Exception e) {

						//	Logs.show ( e );
						}

					
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}finally{
					
				}

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}


	public void privateLobbyPutDeleteMarker ( final boolean isPut, final String vConversationId ) {


		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result = dbUtil.getPrivateLobbyMarkerUrl ( vConversationId );

					String vMessageId = null;
					String vMessageUrl = null;

					
					JSONObject jObj = new JSONObject ();
					if ( isPut ) {
						vMessageId = dbUtil.getLatestPrivateLobbyMessageId ( vConversationId );
						vMessageUrl = dbUtil.getLatestPrivateLobbyMessageUrl ( vConversationId );

						try {
							jObj.put( ":type", "application/vnd.playup.collection.marker+json" );

							JSONObject pos = new JSONObject();
							pos.put( ":uid", vMessageId );
							pos.put( ":self", vMessageUrl );
							pos.put( ":type", "application/vnd.playup.message.text+json" );

							jObj.put( "position", pos );
						} catch ( Exception e ) {
							//Logs.show ( e );
						}
					}
					
					String vMarkerUrl = "";
					HttpRequest request = null;
					if(result != null && result.containsKey("isHref")){
						vMarkerUrl = (String) result.get("url");
						if(vMarkerUrl != null && vMarkerUrl.trim().length() > 0){
						if(((Boolean)result.get("isHref")).booleanValue()){
							String vTokenValue = checkForHrefTokenParam(vMarkerUrl);
							 int tokenType = checkForHrefTokenType(vMarkerUrl);
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								String encodedUrl = getPersonalizedEnocodedURL(vMarkerUrl, vTokenValue,tokenType);
								
								if ( isPut ) {
									request = new HttpRequest(encodedUrl, vMarkerUrl ,true, jObj.toString(), Constants.PUT_METHOD,null );
								} else {
									request = new HttpRequest (encodedUrl,  vMarkerUrl ,true, Constants.DELETE_METHOD );
								}
								
							}else{
								
								if ( isPut ) {
									request = new HttpRequest( vMarkerUrl , jObj.toString(), Constants.PUT_METHOD );
								} else {
									request = new HttpRequest ( vMarkerUrl , Constants.DELETE_METHOD );
								}
							}
							
						}else{
							
							if ( isPut ) {
								request = new HttpRequest( vMarkerUrl , jObj.toString(), Constants.PUT_METHOD );
							} else {
								request = new HttpRequest ( vMarkerUrl , Constants.DELETE_METHOD );
							}
							
						}
						}
						
					}else{
						return;
					}

					if ( vMarkerUrl != null && vMarkerUrl.trim().length() > 0 ) {

						

						if(Constants.isFetchingCredentials){
							return ;
						}
						
						
						try {
							StringBuffer strBuffer = (StringBuffer) request.send();
							
							if(request.getStatusCode()==401){
								callTheCredentialsUrl(strBuffer);
								
								return;
							}
							else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null) {


								if(strBuffer!=null)
									strBuffer.setLength(0);
							}
						} catch (RequestRepeatException e) {
							//Logs.show ( e );
						}catch (Exception e) {

							//Logs.show ( e );
						}

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}

	/**
	 * Putting and deleting the marker for the count number  ( recent activity ) 
	 */
	public void putDirectDeleteMarker ( final boolean isPut, final String vMarkerUrl, final Boolean isHrefURL  ) {


		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				
				try {
					if(Constants.isFetchingCredentials){
						
						return;
					}
					
					//Log.e("123","putDirectDeleteMarker >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   ");

					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String vMessageId = null;
					String vMessageUrl = null;

					JSONObject jObj = new JSONObject ();
					if ( isPut ) {
						vMessageId = dbUtil.getLatestDirectMessageId ( vMarkerUrl );
						vMessageUrl = dbUtil.getLatestDirectMessageUrl ( vMarkerUrl );



						try {
							jObj.put( ":type", "application/vnd.playup.collection.marker+json" );

							JSONObject pos = new JSONObject();
							pos.put( ":uid", vMessageId );
							pos.put( ":self", vMessageUrl );
							pos.put( ":type", "application/vnd.playup.message.text+json" );

							jObj.put( "position", pos );
						} catch ( Exception e ) {
							//Logs.show( e );

						}
					}

					if ( vMarkerUrl != null && vMarkerUrl.trim().length() > 0 ) {

					
						HttpRequest request = null;
						String encodedHrefURL	= null;
						String vTokenValue		= 	null;
						//boolean isHref = checkForToken(vSectionUrl);
						
						try {
							if(isHrefURL){
								
								 int tokenType = checkForHrefTokenType(vMarkerUrl);
								 vTokenValue = new Util().checkForHrefTokenParam(vMarkerUrl); 
								 encodedHrefURL = new Util().getPersonalizedEnocodedURL(vMarkerUrl,vTokenValue,tokenType);
								
								 if ( isPut ) {
										request = new HttpRequest( encodedHrefURL,vMarkerUrl ,isHrefURL, jObj.toString(), Constants.PUT_METHOD,null );
									} else {
										request = new HttpRequest (encodedHrefURL, vMarkerUrl ,isHrefURL, Constants.DELETE_METHOD );
									}
								
							}else{
								
								if ( isPut ) {
									request = new HttpRequest( vMarkerUrl , jObj.toString(), Constants.PUT_METHOD );
								} else {
									request = new HttpRequest ( vMarkerUrl , Constants.DELETE_METHOD );
								}
								
							}
						}catch(Exception e ){
							//Logs.show(e);
						}
						
						try {
							StringBuffer strBuffer = (StringBuffer) request.send();
							
						//	Log.e("123","putDirectDeleteMarker >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   "+strBuffer);
							
							if(request.getStatusCode() == 401){
								
								callTheCredentialsUrl(strBuffer);
								return;
								
							}else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null) {


								if(strBuffer!=null)
									strBuffer.setLength(0);
							}
						} catch (RequestRepeatException e) {
							//Logs.show( e );
						}catch (Exception e) {
							//Logs.show( e );
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

	}

/**
 * Chked
 * @param liveFriendsUrl
 * @param runnableList
 * @param playupfriends
 * @return
 */
	public Runnable refreshLiveFriends(final String liveFriendsUrl,final Hashtable<String, Runnable> runnableList,final boolean playupfriends ) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					int cacheTime = -1;
					
					
					HttpRequest request = new HttpRequest(liveFriendsUrl, Constants.GET_METHOD );
					boolean isUpdateAvailable	=	false;
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {
							if( request.isAnyChangeInContent != Constants.NO_NEW_CONTENT_AVAIL ) {//If 304 then dont update

								JsonUtil json = new JsonUtil() ;
								json.setPlayupFriends(playupfriends);
								json.parse(strBuffer, Constants.TYPE_LIVE_FRIEND_JSON, false );

								isUpdateAvailable	=	true;
								if(strBuffer!=null)
									strBuffer.setLength(0);
							}
						}
					} catch (RequestRepeatException e) {
						///Logs.show ( e );
					}catch (Exception e) {
						// TODO: handle exception
						//Logs.show ( e );

					}
					if(runnableList!=null&&runnableList.containsKey(Constants.REFRESH_LIVE_FRIENDS)){
						runnableList.remove(Constants.REFRESH_LIVE_FRIENDS);
					}

					if(isUpdateAvailable){
						Message msg = new Message ();
						msg.obj = "refreshLiveFriends";
						msg.arg1 = cacheTime;
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		return runnable;
	}

/**
 * Chked
 * @param allFriendsUrl
 * @param runnableList
 * @return
 */
	public Runnable refreshAllFriends(final String allFriendsUrl,final Hashtable<String, Runnable> runnableList) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				int cacheTime = -1;
				
				
				HttpRequest request = new HttpRequest(allFriendsUrl, Constants.GET_METHOD );	
				boolean isUpdateAvailable	=	false;
				try {
					StringBuffer strBuffer = (StringBuffer) request.send();
					if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

						if( request.isAnyChangeInContent != Constants.NO_NEW_CONTENT_AVAIL ) {//If 304 then dont update
							JsonUtil json = new JsonUtil() ;
							json.setGapId( "" );
							json.setFriendsRefresh(true);
							json.parse(strBuffer, Constants.TYPE_GET_FRIENDS_JSON, false );

							isUpdateAvailable	=	true;
							if(strBuffer!=null)
								strBuffer.setLength(0);
						}
					}
				} catch (RequestRepeatException e) {
					//Logs.show(e);

				}catch (Exception e) {
					// TODO: handle exception
					//Logs.show(e);

				}

				if(runnableList!=null&&runnableList.containsKey(Constants.REFRESH_ALL_FRIENDS)){
					runnableList.remove(Constants.REFRESH_ALL_FRIENDS);
				}

				if(isUpdateAvailable){
					Message msg = new Message ();
					msg.obj = "refreshAllFriends";
					msg.arg1 = cacheTime;
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
				}
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		return runnable;
	}
	
	/**
	 * Chked
	 * @param otherFriendsUrl
	 * @param runnableList
	 * @param playupFriends
	 * @return
	 */
	public Runnable refreshOtherFriends(final String otherFriendsUrl,final Hashtable<String, Runnable> runnableList,final boolean playupFriends) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				int cacheTime = -1;
				boolean isUpdateAvailable	=	false;
				
				try {
					HttpRequest request = new HttpRequest(otherFriendsUrl, Constants.GET_METHOD );
					
					StringBuffer strBuffer = (StringBuffer) request.send();
					if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

						if( request.isAnyChangeInContent != Constants.NO_NEW_CONTENT_AVAIL ) {//If 304 then dont update

							JsonUtil json = new JsonUtil() ;
							json.setPlayupFriends(playupFriends);
							json.parse(strBuffer, Constants.TYPE_LIVE_FRIEND_JSON, false );

							if(strBuffer!=null)
								strBuffer.setLength(0);

							isUpdateAvailable	=	true;
						}
					}
				} catch (RequestRepeatException e) {
					//Logs.show(e);
				}catch (Exception e) {
					// TODO: handle exception
					//Logs.show(e);

				}

				if(runnableList!=null&&runnableList.containsKey(Constants.REFRESH_OTHER_FRIENDS)){
					runnableList.remove(Constants.REFRESH_OTHER_FRIENDS);
				}

				if(isUpdateAvailable){
					Message msg = new Message ();
					msg.obj = "refreshOtherFriends";
					msg.arg1 = cacheTime;
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
				}

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return  runnable;

	}





/**
 * Chked
 * @param liveFriendsUrl
 */
	public void searchLiveFriends(final String liveFriendsUrl) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {

			
				try {
					HttpRequest request = new HttpRequest(liveFriendsUrl, Constants.GET_METHOD );
					StringBuffer strBuffer = (StringBuffer) request.send();
					if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

						JsonUtil json = new JsonUtil() ;
						json.parse(strBuffer, Constants.TYPE_SEARCH_LIVE_FRIEND_JSON, false );

						if(strBuffer!=null)
							strBuffer.setLength(0);
					}
				} catch (RequestRepeatException e) {
					//Logs.show(e);
				}catch (Exception e) {
					//Logs.show(e);

				}

				Message msg = new Message ();
				msg.obj = "SearchFriendsData";
				PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

	}


/**
 * Chked
 * @param vGapUrl
 * @param vUserSelfUrl
 */
	public void callDirectGapUrl ( final String vGapUrl, final String vUserSelfUrl,final boolean isHref ) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				try {
					if(Constants.isFetchingCredentials){
						
						return;
						
					}

					HttpRequest request = new HttpRequest( vGapUrl, Constants.GET_METHOD );
					if(isHref){
						String vTokenValue = checkForHrefTokenParam(vGapUrl);
						 int tokenType = checkForHrefTokenType(vGapUrl);
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							String encodedUrl = getPersonalizedEnocodedURL(vGapUrl, vTokenValue,tokenType);
							request = new HttpRequest( vGapUrl,encodedUrl,true, Constants.GET_METHOD );
							
						}else{
							
							request = new HttpRequest( vGapUrl, Constants.GET_METHOD );
							
						}
						
					}else{
						request = new HttpRequest( vGapUrl, Constants.GET_METHOD );
					}

					
					try {
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();

						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
							callTheCredentialsUrl(strBuffer);
							return;

						}else
						
						if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

							dbUtil.setDirectMessageGapDelete ( vGapUrl ); 

							
							Hashtable<String, Object> result = DatabaseUtil.getInstance().getDirectMessageUrl( vUserSelfUrl );
						
							String url =(String) result.get("url") ;
							String vDirectMessageId = dbUtil.getDirectMessageId ( url); 
							/*String vDirectMessageId = dbUtil.getDirectMessageId ( DatabaseUtil.getInstance().getDirectMessageUrl( vUserSelfUrl ) ); */
							JsonUtil json = new JsonUtil();
							json.setDirectMessageId ( vDirectMessageId );
							json.setFromGap ( true );
							json.parse( strBuffer, Constants.TYPE_DIRECT_MSGS_JSON, false );
							json = null;

							if ( strBuffer != null ) {
								strBuffer.setLength(0);
								strBuffer = null;		
							}
						} else {
							dbUtil.setDirectMessageGapLoading ( vGapUrl, 0 ); 
						}
						request = null;
					} catch (RequestRepeatException e) {
					//	Logs.show(e);
					}catch (Exception e) {
						//Logs.show(e);
					}

					Message msg = new Message ();
					msg.obj = "DirectConversation";
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

	}

/**
 * Chked
 * @param runnableList
 * @param fromRefresh
 * @return
 */
	public Runnable getFriendsData (final Hashtable<String, Runnable> runnableList, final boolean fromRefresh ) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {


				try {
					
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(Constants.GET_FREINDS_DATA)){
							runnableList.remove(Constants.GET_FREINDS_DATA);
						}
						
						return;
						
					}
						
					boolean isUpdated = false;
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result = dbUtil.getFriendsUrlForApiCall();
					String friendsUrl = "";
					HttpRequest  request  = null;
					
					if(result != null && result.containsKey("isHref")){
						
						friendsUrl = (String) result.get("url");
						if(friendsUrl != null && friendsUrl.trim().length() > 0){
						
						
						
						if((Boolean) result.get("isHref")){							
							
							String vTokenValue = checkForHrefTokenParam(friendsUrl);
							 int tokenType = checkForHrefTokenType(friendsUrl);
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								
								String encodedUrl = getPersonalizedEnocodedURL(friendsUrl, vTokenValue,tokenType);
								
								request = new HttpRequest( friendsUrl,encodedUrl,true, Constants.GET_METHOD );
							}else{
								
								request = new HttpRequest( friendsUrl, Constants.GET_METHOD );
							}
							
							
							
						}else{
							
							request = new HttpRequest( friendsUrl, Constants.GET_METHOD );
						}
						}
						
					}else{
						return;
					}

					int cacheTime =  -1;
					if(friendsUrl != null && friendsUrl.trim().length() > 0){

						
						try {

							//Log.e("123","friendUrl >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+friendsUrl);
							StringBuffer strBuffer = (StringBuffer) request.send();
							
							if(request.getStatusCode()==401){
								callTheCredentialsUrl(strBuffer);

								if(runnableList != null && runnableList.containsKey(Constants.GET_FREINDS_DATA)){
									runnableList.remove(Constants.GET_FREINDS_DATA);
								}
								return;
							}
							else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {
								isUpdated = true;
								JsonUtil json = new JsonUtil() ;
								json.setGapId( "" );
								json.setFriendsRefresh(false);
								json.parse(strBuffer, Constants.TYPE_GET_FRIENDS_JSON, false );


								if(strBuffer!=null)
									strBuffer.setLength(0);

							}
						} catch (RequestRepeatException e) {
							isUpdated = false;
						}catch (Exception e) {
							isUpdated = false;


						}

						if(runnableList!=null&&runnableList.containsKey(Constants.GET_FREINDS_DATA)){
							runnableList.remove(Constants.GET_FREINDS_DATA);
						}

						isUpdated = false;
						if(fromRefresh){
							if(isUpdated){
							Message msg = new Message ();
							msg.obj = "refreshAllFriends";
							msg.arg1 = cacheTime;
							PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
							}
						}else{
							
							Message msg = new Message ();
							msg.obj = "FriendsData";
							
							if(isUpdated)
								msg.arg1 = 1;
							
							else
								msg.arg1 = 0;
							msg.arg1 = cacheTime;
							PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
							
						}
					}
					else{
						if(runnableList!=null&&runnableList.containsKey(Constants.GET_FREINDS_DATA)){
							runnableList.remove(Constants.GET_FREINDS_DATA);
						}

						Message msg = new Message ();
						msg.obj = "EmptyFriendsData";
						msg.arg1 = cacheTime;
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}finally{
					if(runnableList!=null&&runnableList.containsKey(Constants.GET_FREINDS_DATA)){
						runnableList.remove(Constants.GET_FREINDS_DATA);
					}
				}

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return  runnable;


	}


/**
 * Chked
 * @param runnableList
 * @return
 */
	public Runnable getPlayUpFriendsData (final Hashtable<String, Runnable> runnableList ) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {


				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String friendsUrl= dbUtil.getPlayupFriendsUrl();



					int cacheTime =  -1;
					if(friendsUrl != null && friendsUrl.trim().length() > 0){

						HttpRequest request = new HttpRequest( friendsUrl, Constants.GET_METHOD );
						try {


							StringBuffer strBuffer = (StringBuffer) request.send();

							if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

								JsonUtil json = new JsonUtil() ;
								json.setGapId( "" );
								json.setFriendsRefresh(false);
								json.parse(strBuffer, Constants.TYPE_PLAYUP_FRIENDS_JSON, false );


								if(strBuffer!=null)
									strBuffer.setLength(0);


							}
						} catch (RequestRepeatException e) {
							//Logs.show(e);
						}catch (Exception e) {
							// TODO: handle exception
							//Logs.show(e);

						}



						if(runnableList!=null&&runnableList.containsKey(Constants.GET_PLAYUP_FREINDS_DATA)){
							runnableList.remove(Constants.GET_PLAYUP_FREINDS_DATA);
						}

						Message msg = new Message ();
						msg.obj = "PlayUpFriendsData";
						msg.arg1 = cacheTime;
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
					}
					else{


						Message msg = new Message ();
						msg.obj = "EmptyFriendsData";
						msg.arg1 = cacheTime;
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}

			}


		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return  runnable;


	}

/**
 * Chked
 * @param url
 * @param searchString
 * @return
 */
	public Runnable searchPlayUpFriendsData ( final String url, final String searchString) {

		Runnable runnable = new Runnable() {
			int status=1;
			@Override
			public void run() {

				try {
					if(Constants.isFetchingCredentials){
						
						
						return;
						
					}
						
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					dbUtil.emptySearchPlayupFriends();

					int cacheTime =  -1;
					if(url != null && url.trim().length() > 0){
						dbUtil.removeEtag(  url.replace( "{prefix}", searchString ) );
						HttpRequest request = new HttpRequest( url, Constants.GET_METHOD );
						try {


							StringBuffer strBuffer = (StringBuffer) request.send();

							if(request.getStatusCode()==401){
								callTheCredentialsUrl(strBuffer);
								
								return;
							}else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

								JsonUtil json = new JsonUtil() ;
								json.setGapId( "" );
								json.parse(strBuffer, Constants.TYPE_PLAYUP_FRIENDS_SEARCH_JSON, false );


								if(strBuffer!=null)
									strBuffer.setLength(0);
								status = 0;

							}
							if ( request.getStatusCode() == 404 ) {
								PlayupLiveApplication.showToast(R.string.internet_connectivity_dialog);
							}
						} catch (RequestRepeatException e) {
							status = 1;
						}catch (Exception e) {
							status = 1;
						}
						Message msg = new Message ();
						msg.obj = "SearchPlayUpFriendsData";
						msg.arg1=status;
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}


			}


		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return  runnable;


	}


	/**Chked
	 * called on pressing of gap for playup friends
	 * @param gapUrl
	 * @param gapUid
	 * @param runnableList
	 * @return
	 */
	public Runnable callNextPlaupFriendsUrl(final String gapUrl,final String gapUid,final Hashtable<String, Runnable> runnableList ) {

		Runnable runnable = new Runnable() {
			int status = 0;
			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						if(runnableList!=null&&runnableList.containsKey(gapUrl)){
							runnableList.remove(gapUrl);
						}
						return;
						
					}
					
					Constants.isPlayupFriendsGapDownloading = true;
					HttpRequest request = new HttpRequest(gapUrl, Constants.GET_METHOD );
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
							callTheCredentialsUrl(strBuffer);
							
							if(runnableList!=null&&runnableList.containsKey(gapUrl)){
								runnableList.remove(gapUrl);
							}
							return;
							
							

						}else
						
						if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {


							JsonUtil json = new JsonUtil() ;
							json.setGapId( gapUid );
							json.setFriendsRefresh(false);
							json.parse(strBuffer, Constants.TYPE_PLAYUP_FRIENDS_JSON, false );

							if(strBuffer!=null)
								strBuffer.setLength(0);
							status = 0;
						} else {
							status = 1;
						}
					} catch (RequestRepeatException e) {
						Constants.isPlayupFriendsGapDownloading = false;
						status = 1;

					}catch (Exception e) {


						Constants.isPlayupFriendsGapDownloading = false;
						status = 1;

					}



					if(runnableList!=null&&runnableList.containsKey(gapUrl)){
						runnableList.remove(gapUrl);
					}
					Constants.isPlayupFriendsGapDownloading = false;
					Message msg = new Message ();
					msg.obj = "PlayUpFriendsData";
					msg.arg1 = status;
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		return runnable;

	}

	/**Chked
	 * called when gap button of playup search friends is pressed
	 * @param gapUrl
	 * @param gapUid
	 * @param runnableList
	 * @return
	 */
	public Runnable callNextPlayupFriendsSearchUrl (final String gapUrl,final String gapUid,final Hashtable<String, Runnable> runnableList ) {

		Runnable runnable = new Runnable() {
			int status = 0;
			@Override
			public void run() {
				Constants.isPlayupFriendsSearchGapDownloading = true;

				HttpRequest request = new HttpRequest(gapUrl, Constants.GET_METHOD );
				try {
					StringBuffer strBuffer = (StringBuffer) request.send();
					if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

						JsonUtil json = new JsonUtil() ;
						json.setGapId( gapUid );
						json.parse(strBuffer, Constants.TYPE_PLAYUP_FRIENDS_SEARCH_JSON, false );


						if(strBuffer!=null)
							strBuffer.setLength(0);
						status = 0;
					}
					if ( request.getStatusCode() == 404 ) {
						PlayupLiveApplication.showToast(R.string.internet_connectivity_dialog);
					}
				} catch (RequestRepeatException e) {
					status = 1;
					Constants.isPlayupFriendsSearchGapDownloading = false;
				}catch (Exception e) {
					status = 1;
					Constants.isPlayupFriendsSearchGapDownloading = false;
				}


				if(runnableList!=null&&runnableList.containsKey(gapUrl)){
					runnableList.remove(gapUrl);

					Constants.isPlayupFriendsSearchGapDownloading = false;
					Message msg = new Message ();
					msg.obj = "SearchPlayUpFriendsData";
					msg.arg1 = status;
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
				}

			}


		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return  runnable;

	}


	/**
	 * Chked
	 * @param vPostUrl
	 * @param data
	 */
	public void postMessage ( final String vPostUrl, final String data,final boolean isHref ) {



		Runnable r = new Runnable() {

			@Override
			public void run() {


				try {
					

					if(Constants.isFetchingCredentials){
						return;
					}
					// Request to Base URL
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					/**
					 * 
					 * Check To See Whether already User Exists In Local DB
					 * 
					 * 
					 */
					String token = dbUtil.getUserToken();
					if (token != null || token.length() > 1 ) {
						HttpRequest request = null;

						if(isHref){
							String vTokenValue = checkForHrefTokenParam(vPostUrl);
							 int tokenType = checkForHrefTokenType(vPostUrl);
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								String encodedUrl = getPersonalizedEnocodedURL(vPostUrl, vTokenValue,tokenType);
								request = new HttpRequest(encodedUrl,vPostUrl,true,data,Constants.POST_METHOD,Types.MESSAGE_POST_TYPE );
								
							}else{
								request = new HttpRequest( vPostUrl, data, Constants.POST_METHOD,Types.MESSAGE_POST_TYPE );
							}
							
						}else{
							request = new HttpRequest( vPostUrl, data, Constants.POST_METHOD,Types.MESSAGE_POST_TYPE );
						}

						

						try {
							
							// getting the response
							StringBuffer strBuffer = (StringBuffer) request.send();
							
							if ( request.getStatusCode() == 401 ) {
								
								JSONObject json = new JSONObject(strBuffer.toString());
								if(json.getString(":type") != null && json.getString(":type").trim().length() > 0 && 
										json.getString(":type").equalsIgnoreCase(Types.CREDENTIAL_TYPE)){
								
									callTheCredentialsUrl(strBuffer);
								}
								
								
								
								
								return;
								
								
							} else {
								if ( strBuffer != null ) {
									
									PostMessage mPostMessage = new PostMessage(strBuffer.toString());
									if ( mPostMessage.getStatusCode() == Constants.SUCCESS_POST ) {
										Message msg = new Message ();
										msg.obj = "Posted";
										PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
									}
								} else {
									Message msg = new Message ();
									msg.obj = "Error";
									PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
								}

							}

							request = null;
						} catch (RequestRepeatException e) {
						//	Logs.show(e);
						}	


					} else {


					}
					dbUtil = null;
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}

			}
		};


		PlayupLiveApplication.getThreadPoolExecutor().execute( r );
	}

	/**Chked
	 * getting conversations with the specific friend
	 * @param runnableList
	 * @return
	 */
	public Runnable getDirectCoversation ( final String vDirectConversationUrl ,
			final Hashtable<String, Runnable> runnableList ,final boolean isHref) {


		Runnable runnable = new Runnable () {

			@Override
			public void run() {
				
				
				try {
					if(Constants.isFetchingCredentials){
						
						if ( runnableList != null && runnableList.containsKey ( Constants.GET_DIRECT_CONVERSATION ) ) {
							runnableList.remove(Constants.GET_DIRECT_CONVERSATION );
						}
						
						return;
					}

					if( vDirectConversationUrl  != null && vDirectConversationUrl.trim().length() > 0){

						HttpRequest request = new HttpRequest( vDirectConversationUrl, Constants.GET_METHOD );
						try {
							StringBuffer strBuffer = (StringBuffer) request.send();
							
							if(request.getStatusCode() == 401){
								
								
								callTheCredentialsUrl(strBuffer);
								if ( runnableList != null && runnableList.containsKey ( Constants.GET_DIRECT_CONVERSATION ) ) {
									runnableList.remove(Constants.GET_DIRECT_CONVERSATION );
								}
								
								return;
								
							}else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && 
									strBuffer.toString().trim().length() > 0) {
								DatabaseUtil dbUtil = DatabaseUtil.getInstance();
								String vDirectConversationId = dbUtil.getDirectConversationIdFromUrl ( vDirectConversationUrl ); 
								String vUserId = dbUtil.getDirectConversationUserIdFromUrl ( vDirectConversationUrl ); 
								dbUtil = null;

								JsonUtil json = new JsonUtil();
								
									json.setIsDirectConversationUrlHref ( isHref );
								
									json.setDirectConversationUrl ( vDirectConversationUrl );
								
								
								json.setDirectConversationId ( vDirectConversationId ) ; 
								json.setDirectConvesationPushId(null);
								json.setUserId( vUserId );
								json.parse( strBuffer, Constants.TYPE_DIRECT_MESSAGES_JSON, false );
								json = null;


								if ( strBuffer != null ) {
									strBuffer.setLength(0);
									strBuffer = null;
								}

							}
						} catch (RequestRepeatException e) {
						//	Logs.show( e); 

						}catch (Exception e) {
							//Logs.show( e); 
						}
						if ( runnableList != null && runnableList.containsKey ( Constants.GET_DIRECT_CONVERSATION ) ) {
							runnableList.remove(Constants.GET_DIRECT_CONVERSATION );
						}

						Message msg = new Message ();
						msg.obj = "DirectConversation";
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}


			}

		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;

	}

	/**Chked
	 * getting conversations with the specific friend
	 * @param runnableList
	 * @return
	 */
	public void getDirectCoversationPostMsg ( final String vDirectConversationUrl, final String data,final boolean isHref ) {



		Runnable runnable = new Runnable () {

			@Override
			public void run() {
					

				try {
					if(Constants.isFetchingCredentials){
						return;
					}
					
					Hashtable<String, Object> vDirectMessageUrl = null;
					if( vDirectConversationUrl  != null && vDirectConversationUrl.trim().length() > 0){
						HttpRequest request = null;
						if(isHref){
							 int tokenType = checkForHrefTokenType(vDirectConversationUrl);
							String vTokenValue = checkForHrefTokenParam(vDirectConversationUrl);
							
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								String encodedUrl = getPersonalizedEnocodedURL(vDirectConversationUrl, vTokenValue,tokenType);
								request = new HttpRequest( vDirectConversationUrl,encodedUrl,true, Constants.GET_METHOD );
								
							}else{
								request = new HttpRequest( vDirectConversationUrl, Constants.GET_METHOD );
							}
							
							
						}else{
							
							request = new HttpRequest( vDirectConversationUrl, Constants.GET_METHOD );
							
						}
						
						
						try {
							StringBuffer strBuffer = (StringBuffer) request.send();
							if(request.getStatusCode() == 401){
								callTheCredentialsUrl(strBuffer);
								return;
							}
							else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {
									try {
										DatabaseUtil dbUtil = DatabaseUtil.getInstance();
										String vDirectConversationId = dbUtil.getDirectConversationIdFromUrl ( vDirectConversationUrl ); 
										String vUserId = dbUtil.getDirectConversationUserIdFromUrl ( vDirectConversationUrl ); 
										dbUtil = null;

										JsonUtil json = new JsonUtil();
										
											json.setIsDirectConversationUrlHref ( isHref );
										
											json.setDirectConversationUrl ( vDirectConversationUrl );
										
										json.setDirectConversationId ( vDirectConversationId ) ; 
										json.setDirectConvesationPushId(null);
										json.setUserId( vUserId );
										json.parse( strBuffer, Constants.TYPE_DIRECT_MESSAGES_JSON, false );
										vDirectMessageUrl = json.getDirectMessageUrl ();
										json = null;


										if ( strBuffer != null ) {
											strBuffer.setLength(0);
											strBuffer = null;
										}
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								}
						} catch (Exception e) {
							//Logs.show( e); 
						}


					}

					if( vDirectMessageUrl != null && vDirectMessageUrl.containsKey("url") && vDirectMessageUrl.containsKey("isHref") )

					postMessage( vDirectMessageUrl.get("url").toString(), data,((Boolean)vDirectMessageUrl.get("isHref")).booleanValue() );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
				
			}
		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}



	
	/**Chked
	 * Praveen: as per the Href
	 * @param isHref 
	 */
	public Runnable getPrivateLobbyConversationMessages(  final String vConversationMessagesUrl, 
			final Boolean isHrefUrl, final String vConversationId, final String vConversationUrl, 
			final boolean insertGap,final Hashtable<String, Runnable> runnableList,final boolean isConversationUrlHref   ) {

		Runnable runnable = new Runnable () {

			@Override
			public void run() {
				
				if(Constants.isFetchingCredentials){
					
					if(runnableList != null && runnableList.containsKey(Constants.GET_ALL_SPORTS)){
						runnableList.remove(Constants.GET_ALL_SPORTS);
					}
					
					return;
					
				}
					

				boolean update = false;
				if( vConversationMessagesUrl  != null && vConversationMessagesUrl.trim().length() > 0){

					//HttpRequest request = new HttpRequest( vConversationMessagesUrl, Constants.GET_METHOD );
					HttpRequest request = null;
					String encodedHrefURL	= null;
					String vTokenValue		= 	null;
					//boolean isHref = checkForToken(vSectionUrl);
					
					
					try {
						
						if(isHrefUrl){
							
							
							 int tokenType = checkForHrefTokenType(vConversationMessagesUrl);
							//encode the url and make a http request with both encoded and unencoded urls
							 vTokenValue = new Util().checkForHrefTokenParam(vConversationMessagesUrl); 
							 encodedHrefURL = new Util().getPersonalizedEnocodedURL(vConversationMessagesUrl,vTokenValue,tokenType);
							
							
							request = new HttpRequest( /*unEncodedUrl,*/vConversationMessagesUrl,encodedHrefURL,isHrefUrl,Constants.GET_METHOD );
							
						}else{
							
							 request = new HttpRequest( vConversationMessagesUrl,Constants.GET_METHOD );
							
						}
						
						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);
							if(runnableList != null && runnableList.containsKey(Constants.GET_ALL_SPORTS)){
								runnableList.remove(Constants.GET_ALL_SPORTS);
							}
							return;
						}
						else if ( !handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null && strBuffer.toString().trim().length() > 0 ) {
							update = true;

							JsonUtil json = new JsonUtil();
							json.setConversationId( vConversationId );
							json.setConversationUrl ( vConversationUrl );
							json.setIsConversationUrlHref ( isConversationUrlHref );
							json.setBooleanFlag( insertGap );
							json.parse(  strBuffer, Constants.TYPE_FRIEND_CONVERSATION__MESSAGE_JSON, false );
							json = null;

							if ( strBuffer != null ) {
								strBuffer.setLength(0);
								strBuffer = null;
							}

						}
					} catch (RequestRepeatException e) {
					//	Logs.show( e); 

					}catch (Exception e) {
						//Logs.show( e); 
					}
				}

				if(runnableList!=null&&runnableList.containsKey(vConversationMessagesUrl)){
					runnableList.remove(vConversationMessagesUrl);
				}

				if ( update ) {


					Message msg = new Message ();
					msg.obj = "PrivateLobbyMessages_refresh";
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
				}
			}
		};


		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		return runnable;

	}
	/**Chked
	 * getting conversations with the specific friend
	 * @param runnableList
	 * @return
	 */
	public Runnable getDirectMessages ( final String vDirectMessageUrl,final Boolean isHrefURL, final Hashtable<String, Runnable> runnableList ) {


		Runnable runnable = new Runnable () {

			@Override
			public void run() {

				
				try {
					if(Constants.isFetchingCredentials){
						if ( runnableList != null && runnableList.containsKey ( Constants.GET_DIRECT_MESSAGES ) ) {
							runnableList.remove(Constants.GET_DIRECT_MESSAGES );
						}
						
						return;
						
					}
					boolean shouldUpdate = false;
					if( vDirectMessageUrl  != null && vDirectMessageUrl.trim().length() > 0){

					//	HttpRequest request = new HttpRequest( vDirectMessageUrl, Constants.GET_METHOD );
						HttpRequest request = null;
						String encodedHrefURL	= null;
						String vTokenValue		= 	null;
						//boolean isHref = checkForToken(vSectionUrl);
						
						try {
							if(isHrefURL){
								
								//encode the url and make a http request with both encoded and unencoded urls
								 int tokenType = checkForHrefTokenType(vDirectMessageUrl);
								//encode the url and make a http request with both encoded and unencoded urls
								 vTokenValue = new Util().checkForHrefTokenParam(vDirectMessageUrl); 
								 encodedHrefURL = new Util().getPersonalizedEnocodedURL(vDirectMessageUrl,vTokenValue,tokenType);
								//String encodedurl = encode(vSectionUrl)
								//String unEncodedurl = vSectionUrl;
								
								request = new HttpRequest( /*unEncodedUrl,*/vDirectMessageUrl,encodedHrefURL,isHrefURL,Constants.GET_METHOD );
								
							}else{
								
								 request = new HttpRequest( vDirectMessageUrl,Constants.GET_METHOD );
								
							}
						}catch(Exception e ){
							//Logs.show(e);
						}
						try {
							StringBuffer strBuffer = (StringBuffer) request.send();
							
							
							if(request.getStatusCode() == 401){
								
								callTheCredentialsUrl(strBuffer);
								
								if ( runnableList != null && runnableList.containsKey ( Constants.GET_DIRECT_MESSAGES ) ) {
									runnableList.remove(Constants.GET_DIRECT_MESSAGES );
								}
								
								return;
								
							}else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && 
									strBuffer.toString().trim().length() > 0) {

								shouldUpdate = true; 
								DatabaseUtil dbUtil = DatabaseUtil.getInstance();
								String vDirectMessageId = dbUtil.getDirectMessageId ( vDirectMessageUrl ); 
								dbUtil = null;

								JsonUtil json = new JsonUtil();
								json.setDirectMessageId ( vDirectMessageId );
								json.parse( strBuffer, Constants.TYPE_DIRECT_MSGS_JSON, false );
								json = null;


								if ( strBuffer != null ) {
									strBuffer.setLength(0);
									strBuffer = null;
								}

							}
						} catch (RequestRepeatException e) {
							//Logs.show( e); 

						}catch (Exception e) {
							//Logs.show( e); 
						}
						if ( runnableList != null && runnableList.containsKey ( Constants.GET_DIRECT_MESSAGES ) ) {
							runnableList.remove(Constants.GET_DIRECT_MESSAGES );
						}


						if ( shouldUpdate ) {
							Message msg = new Message ();
							msg.obj = "DirectConversation";
							PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}


			}

		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;

	}

	/**
	 * Chked
	 * @param vGapId
	 * @param vContentUrl
	 * @param isHref
	 * @return
	 */
	public Runnable getPrivateLobbyConversationGap ( final String vGapId, final String vContentUrl,final boolean isHref ) {

		Runnable runnable = new Runnable () {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						return;
						
					}

					if( vContentUrl  != null && vContentUrl.trim().length() > 0){
						HttpRequest request = null;
						if(isHref){
							
							String vTokenValue = checkForHrefTokenParam(vContentUrl);
							int tokenType = checkForHrefTokenType(vContentUrl);
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								String vEncodedUrl = getPersonalizedEnocodedURL(vContentUrl, vTokenValue,tokenType);
								
								request = new HttpRequest( vContentUrl, vEncodedUrl,true,Constants.GET_METHOD );
							}else{
								request = new HttpRequest( vContentUrl, Constants.GET_METHOD );
							}
							
						}else{
							request = new HttpRequest( vContentUrl, Constants.GET_METHOD );
						}
						try {
							StringBuffer strBuffer = (StringBuffer) request.send();
							
							if(request.getStatusCode() == 401){
								callTheCredentialsUrl(strBuffer);
								return;

							}else

							if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {


								DatabaseUtil dbUtil = DatabaseUtil.getInstance();
								String vConversationId = dbUtil.getConversationIdFromPrivateLobbyGapId ( vGapId );
								String vConversationUrl = dbUtil.getConversationUrlFromPrivateLobbyGapId ( vGapId );

								dbUtil.deletePrivateLobbyGap ( vGapId ) ;

								// messages 
								JsonUtil json = new JsonUtil();
								json.setConversationId( vConversationId );
								json.setConversationUrl ( vConversationUrl );
								json.setBooleanFlag( true );
								json.parse( strBuffer, Constants.TYPE_FRIEND_CONVERSATION__MESSAGE_JSON, false );

								if ( strBuffer != null ) {
									strBuffer.setLength(0);
									strBuffer = null;
								}

							}
						} catch (RequestRepeatException e) {
							//Logs.show( e); 

						}catch (Exception e) {
							//Logs.show( e); 
						}
						Message msg = new Message ();
						msg.obj = "PrivateLobbyMessages_gap";
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}


			}

		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;

	}

/**
 * Chked
 */
	public void postC2DM ( ) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						return;
						
					}
					
					

					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Boolean isHref = false;
					String vPushNotificationUrl = ""; 
					
					
					Hashtable<String, Object> result = dbUtil.getPushNotificationUrl ();
					
					if(result!=null && result.containsKey("url") && result.containsKey("isHref") ){
						 vPushNotificationUrl = (String) result.get("url");
						isHref = (Boolean) result.get("isHref");
					}
					

					PushPreferences prefs = PushManager.shared().getPreferences();
					String registrationId = prefs.getPushId();
					
					

					if ( registrationId == null ) {
						return;
					} 
					if ( registrationId.trim().length() == 0 )  {
						return;
					}



					JSONObject jObj = new JSONObject();

					try {
						jObj.put( "app_id",  "com.playup.android" );
						jObj.put( ":type",  "application/vnd.playup.push.registration+json" );
						jObj.put( "push_token",  registrationId );
					} catch ( Exception e  ) {
					//	Logs.show(e);
					}
					
					if(vPushNotificationUrl!=null && vPushNotificationUrl.trim().length()>0)
					{
						dbUtil.setHeader( vPushNotificationUrl, "application/vnd.playup.push.registration+json" , false );

						
						HttpRequest request;
						if(isHref){
							
							
							String vTokenValue = checkForHrefTokenParam(vPushNotificationUrl);
							 int tokenType = checkForHrefTokenType(vPushNotificationUrl);
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								String encodedUrl = getPersonalizedEnocodedURL(vPushNotificationUrl, vTokenValue,tokenType);
								request = new HttpRequest( encodedUrl,vPushNotificationUrl , true,jObj.toString(), Constants.POST_METHOD,null );
								
							}else{
								request = new HttpRequest( vPushNotificationUrl , jObj.toString(), Constants.POST_METHOD );
							}
							
						}
						else{
							request = new HttpRequest( vPushNotificationUrl , jObj.toString(), Constants.POST_METHOD );
						}
						
						

						try {


							StringBuffer strBuffer = (StringBuffer) request.send();
							
							if(request.getStatusCode() == 401){
								callTheCredentialsUrl(strBuffer);
								return;
								
								

							}else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null) {

								int statusCode = request.getStatusCode();

								if(strBuffer!=null)
									strBuffer.setLength(0);

							}
						} catch (RequestRepeatException e) {
						//	Logs.show(e);
						}catch (Exception e) {
							//Logs.show(e);
						}
					}

					
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//Logs.show(e);
				}
				
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

	}
	
	/**
	 * Chked
	 * @param vFriendId
	 */
	public void sendInvite ( final String vFriendId ) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						return;
						
					}



					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result = dbUtil.getAppInvitationUrl ( vFriendId );
					JSONObject jsonObject = null;
					
					try {

						jsonObject = new JSONObject();
						jsonObject.put( ":type", "application/vnd.playup.application.invitation.request+json" );
						jsonObject.put( "message", "Application invitation message." );
					} catch (JSONException e1) {
					}

					HttpRequest request = null;
					
					if(result != null && result.containsKey("isHref")){
						String vAppInvitationUrl = (String) result.get("url");
						
						if(((Boolean) result.get("isHref")).booleanValue()){
							
							 int tokenType = checkForHrefTokenType(vAppInvitationUrl);
							String vTokenValue = checkForHrefTokenParam(vAppInvitationUrl);
							
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								String encodedUrl = getPersonalizedEnocodedURL(vAppInvitationUrl, vTokenValue,tokenType);
								request = new HttpRequest( encodedUrl,vAppInvitationUrl , true,jsonObject.toString(), Constants.POST_METHOD,null );
								
							}else{
								request = new HttpRequest( vAppInvitationUrl , jsonObject.toString(), Constants.POST_METHOD );
							}
							
						}else{
							
							request = new HttpRequest( vAppInvitationUrl , jsonObject.toString(), Constants.POST_METHOD );
							
							
						}
						
						
					}else{
						return;
					}

					
					
					try {


						StringBuffer strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
							callTheCredentialsUrl(strBuffer);
							return;
							
							

						}else
						
						if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null) {

							int statusCode = request.getStatusCode();
							if ( statusCode == 202 ) {

								dbUtil.setAlreadyInvited( vFriendId, 2 );
							} else {
								dbUtil.setAlreadyInvited( vFriendId, 0 );
							}

							if(strBuffer!=null)
								strBuffer.setLength(0);

						}
					} catch (RequestRepeatException e) {
						//Logs.show(e);
					}catch (Exception e) {
						//Logs.show(e);
					}
					Message msg = new Message ();
					msg.obj = "refreshFriends";
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

	}



	// emoticons and smileys

	private static final HashMap<String, Integer> emoticons = new HashMap();
	static {

		emoticons.put(":-)", R.drawable.emo_im_happy);
		emoticons.put(";-)", R.drawable.emo_im_winking);
		emoticons.put(":-(", R.drawable.emo_im_sad);
		emoticons.put(":�-(", R.drawable.emo_im_crying);

		emoticons.put("B-)", R.drawable.emo_im_cool);
		emoticons.put(":-*", R.drawable.emo_im_kissing);
		emoticons.put(":-P", R.drawable.emo_im_tongue_sticking_out);
		emoticons.put(":O", R.drawable.emo_im_yelling);

		emoticons.put("O:-)", R.drawable.emo_im_angel);
		emoticons.put(":-O", R.drawable.emo_im_surprised);
		emoticons.put(":-$", R.drawable.emo_im_money_mouth);
		emoticons.put(":-!", R.drawable.emo_im_foot_in_mouth);

		emoticons.put(":-/", R.drawable.emo_im_undecided);
		emoticons.put(":-D ", R.drawable.emo_im_laughing);
		emoticons.put("o_O", R.drawable.emo_im_wtf);
		emoticons.put(":-X", R.drawable.emo_im_lips_are_sealed);


	}

	/**
	 * Chked
	 * @param text
	 * @return
	 */
	public Spannable getSmiledText(String text) {

		if ( text  != null && text .trim().length() > 0 ) {
			SpannableStringBuilder builder = new SpannableStringBuilder(text);
			int index;
			for (index = 0; index < builder.length(); index++) {
				for (Entry<String, Integer> entry : emoticons.entrySet()) {
					int length = entry.getKey().length();
					if (index + length > builder.length())
						continue;
					if (builder.subSequence(index, index + length).toString().equals(entry.getKey())) {
						builder.setSpan(new ImageSpan(PlayUpActivity.context, entry.getValue()), index, index + length,
								Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						index += length - 1;
						break;
					}
				}
			}
			return builder;
		} else {
			return new SpannableStringBuilder( "" );
		}

	}

	/**Chked
	 * 
	 * @param text
	 * @param textView
	 */
	public void getSmiledText(final String text, final TextView textView ) {


		Runnable r = new Runnable () {

			@Override
			public void run() {

				if ( text  != null && text .trim().length() > 0 ) {
					final SpannableStringBuilder builder = new SpannableStringBuilder(text);
					int index;for (index = 0; index < builder.length(); index++) {
						for (Entry<String, Integer> entry : emoticons.entrySet()) {
							int length = entry.getKey().length();
							if (index + length > builder.length())
								continue;
							if (builder.subSequence(index, index + length).toString().equals(entry.getKey())) {
								builder.setSpan(new ImageSpan(PlayUpActivity.context, entry.getValue()), index, index + length,
										Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
								index += length - 1;
								break;
							}
						}
					}

					if ( PlayUpActivity.handler != null ) {
						PlayUpActivity.handler.post( new Runnable () {

							@Override
							public void run() {
								textView.setText( builder );
							}

						});
					}
					return ;
				} else {
					textView.setText( "" );
				}

			}

		};
		Thread th = new Thread ( r );
		th.start();



	}

	/**
	 * 
	 * Chked
	 * @param providerName
	 * @param vShareUrl
	 * @param data
	 * @param isHref
	 */
	public void shareTheScores ( final String providerName, final String vShareUrl, final String data,final boolean isHref ) {
		Runnable runnable = new Runnable() {
			int status =0;
			@Override
			public void run() {
				try {
					StringBuffer strBuffer;
					
					if(Constants.isFetchingCredentials){
						return;
					}
					HttpRequest request = null;
					if(isHref){
						
						String vTokenValue = checkForHrefTokenParam(vShareUrl);
						 int tokenType = checkForHrefTokenType(vShareUrl);
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							String encodedUrl = getPersonalizedEnocodedURL(vShareUrl, vTokenValue,tokenType);
							request = new HttpRequest( encodedUrl,vShareUrl ,isHref, data, Constants.POST_METHOD,null );
							
						}else{
							request = new HttpRequest( vShareUrl , data, Constants.POST_METHOD );
						}
						
					}else{
						request = new HttpRequest( vShareUrl , data, Constants.POST_METHOD );
					}
					
					try {


						strBuffer = (StringBuffer) request.send();
						
						if(request.getStatusCode() == 401){
							callTheCredentialsUrl(strBuffer);
							return;
							
						}else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null) {

							int statusCode = request.getStatusCode();
							PostMessage mPostMessage = new PostMessage(strBuffer.toString());
							if ( mPostMessage.getStatusCode() == Constants.SUCCESS_POST ) 			
								status = 1;	

							if(strBuffer!=null)
								strBuffer.setLength(0);

						}
					} catch (RequestRepeatException e) {
						//Logs.show( e );

					}catch (Exception e) {
						//Logs.show( e );
					}

					Message msg = new Message ();
					msg.obj = "share_response:"+providerName;
					msg.arg1 = status;

					PlayupLiveApplication.callUpdateOnFragments( msg );
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

	}

	public void getRegions(String regionUrl) {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();

					dbUtil.setRegions("Europe",0,"","EU");
					dbUtil.setRegions("Africa",0,"","AF");
					dbUtil.setRegions("South America",0,"","SA");
					dbUtil.setRegions("Oceania",0,"","OC");
					dbUtil.setRegions("Asia",0,"","AS");
					dbUtil.setRegions("North America",0,"","NA");

					//				

					Message m =  new Message();
					m.obj = "regionsData";
					PlayupLiveApplication.callUpdateOnFragments(m);
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show(e);
				}

			}
		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(r);

	}
	/**
	 * Chked
	 */
	public Runnable getDisplayUpdateForTiles(final String vDisplayUrl,final boolean ishref,
			final String vContentId, final String vBlockContentId, final Hashtable<String, Runnable> runnableList) {

		Runnable runnable = new Runnable () {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(vDisplayUrl)){
							runnableList.remove(vDisplayUrl);
						}
						
						return;
						
					}
						
					boolean isUpdated = false;

					if( vDisplayUrl  != null && vDisplayUrl.trim().length() > 0){


						HttpRequest request = new HttpRequest( vDisplayUrl, Constants.GET_METHOD );
						try {
							StringBuffer strBuffer = (StringBuffer) request.send();
							
							if(request.getStatusCode()==401){
								callTheCredentialsUrl(strBuffer);
								if(runnableList != null && runnableList.containsKey	(vDisplayUrl)){
									runnableList.remove(vDisplayUrl);
								}
								return;
							}
							else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {


								isUpdated = true;
								JsonUtil json = new JsonUtil();
								json.setContentId( vContentId );
								json.setBlockTileId ( vBlockContentId );

								json.parse( strBuffer, Constants.TYPE_DISPLAY_JSON, false );

								if ( strBuffer != null ) {
									strBuffer.setLength(0);
									strBuffer = null;
								}

							}
						} catch (RequestRepeatException e) {
						//	Logs.show( e); 

						}catch (Exception e) {
						//	Logs.show( e); 
						}finally{

							if(runnableList != null && runnableList.containsKey(vDisplayUrl))
								runnableList.remove(vDisplayUrl);

						} 
						
						
						
						Message msg = new Message ();
						msg.obj = "UpdateTiles";
						Bundle b = new Bundle();
						b.putString("vDisplayUrl", vDisplayUrl);
						b.putString("vContentId", vContentId);
						b.putString("vBlockContentId", vBlockContentId);
						msg.setData(b);
						
						
						if(isUpdated){
							
							msg.arg1 = 1;
							
						}else{
							
							msg.arg1 = 0;
						}
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show(e);
				}


			}

		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}


/**
 * Chked
 * @param vDisplayUrl
 * @param vContentId
 * @param vBlockContentId
 * @param runnableList
 * @return
 */
	public Runnable getDisplayUpdateForStack(final String vDisplayUrl,
			final String vContentId, final String vBlockContentId, final Hashtable<String, Runnable> runnableList) {

		Runnable runnable = new Runnable () {

			@Override
			public void run() {
				
				
				boolean isUpdated = false;

				if( vDisplayUrl  != null && vDisplayUrl.trim().length() > 0){

					HttpRequest request = new HttpRequest( vDisplayUrl, Constants.GET_METHOD );
					try {
						StringBuffer strBuffer = (StringBuffer) request.send();

						if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {


							isUpdated = true;
							JsonUtil json = new JsonUtil();
							json.setContentId( vContentId );
							json.setBlockTileId ( vBlockContentId );

							json.parse( strBuffer, Constants.TYPE_DISPLAY_JSON, false );

							if ( strBuffer != null ) {
								strBuffer.setLength(0);
								strBuffer = null;
							}

						}
					} catch (RequestRepeatException e) {
					//	Logs.show( e); 

					}catch (Exception e) {
						//Logs.show( e); 
					}finally{

						if(runnableList != null && runnableList.containsKey(vDisplayUrl))
							runnableList.remove(vDisplayUrl);

					}
					
					Message msg = new Message ();
					msg.obj = "UpdateStack";
					Bundle b = new Bundle();
					b.putString("vDisplayUrl", vDisplayUrl);
					b.putString("vContentId", vContentId);
					b.putString("vBlockContentId", vBlockContentId);
					msg.setData(b);
					
					
					if(isUpdated){
						
						msg.arg1 = 1;
						
					}else{
						
						msg.arg1 = 0;
					}
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
				}


			}

		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}


	

	
	/**
	 * Chked
	 */
	public Runnable getDisplayUpdateForSectionItems(final String vDisplayUrl,final boolean isHref,
			final String vContentId, final String vBlockContentId, final Hashtable<String, Runnable> runnableList) {

		Runnable runnable = new Runnable () {

			@Override
			public void run() {

				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(vDisplayUrl)){
							runnableList.remove(vDisplayUrl);
						}
						
						return;
						
					}
						

					
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(vDisplayUrl))
							runnableList.remove(vDisplayUrl);
						
						return;

						
					}
						

					
					boolean isUpdated = false;

					if( vDisplayUrl  != null && vDisplayUrl.trim().length() > 0){


						HttpRequest request = new HttpRequest( vDisplayUrl, Constants.GET_METHOD );
						try {
							
							
							StringBuffer strBuffer = (StringBuffer) request.send();
							if(request.getStatusCode()==401){
								callTheCredentialsUrl(strBuffer);
								if(runnableList != null && runnableList.containsKey	(vDisplayUrl)){
									runnableList.remove(vDisplayUrl);
								}
								return;
							}


							
							if(request.getStatusCode() == 401){
								
								callTheCredentialsUrl(strBuffer);
								if(runnableList != null && runnableList.containsKey(vDisplayUrl))
									runnableList.remove(vDisplayUrl);
								
								return;

								
								
								
								
							}else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && 
									strBuffer.toString().trim().length() > 0) {




								isUpdated = true;
								JsonUtil json = new JsonUtil();
								json.setContentId( vContentId );
								json.setBlockTileId ( vBlockContentId );

								json.parse( strBuffer, Constants.TYPE_DISPLAY_JSON, false );

								if ( strBuffer != null ) {
									strBuffer.setLength(0);
									strBuffer = null;
								}

							}
						} catch (RequestRepeatException e) {
						//	Logs.show( e); 

						}catch (Exception e) {
						//	Logs.show( e); 
						}finally{

							if(runnableList != null && runnableList.containsKey(vDisplayUrl))
								runnableList.remove(vDisplayUrl);

						}
						if(isUpdated){
							Message msg = new Message ();
							msg.obj = "updateSectionItems";
							PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}


			}

		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}


	/**
	 * Chked
	 * @param upgradeUrl
	 */
	public void getAppUpdateDetails(final String upgradeUrl) {

		Runnable runnable = new Runnable() {
			String app_version = null;
			String apk_path = null;

			@Override
			public void run() {

				
				if (upgradeUrl != null && upgradeUrl.trim().length() > 0) {
					try {
						URL url = new URL(upgradeUrl);
						HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();
						InputStream inputStream = urlConnection
						.getInputStream();
						if (inputStream != null) {
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(inputStream),
									8 * 1024);
							StringBuilder sb = new StringBuilder();
							String line = null;
							while ((line = reader.readLine()) != null) {
								sb.append(line + "\n");
							}
							inputStream.close();
							inputStream = null;
							reader.close();
							reader = null;

							if (sb.toString() != null) {
								JSONObject upgradeJson = new JSONObject(sb.toString());

								apk_path = upgradeJson.getString("apk_path");
								app_version = upgradeJson.getString("version");

								upgradeJson = null;
							}
							sb = null;
						}

					} catch (Exception e) {
					//	Logs.show(e);
					}


					if (app_version != null && apk_path != null) {
						app_version = app_version.replace( ".", "" );

						String current_version = null;
						try {
							PackageInfo pInfo = PlayUpActivity.context.getPackageManager()
							.getPackageInfo(
									PlayUpActivity.context
									.getPackageName(),
									0);
							current_version = pInfo.versionName;
							current_version = current_version.replace( ".", "" );
							pInfo = null;
						} catch (Exception e) {
							//Logs.show(e);
						}

						try {

							if ( current_version != null && app_version != null && current_version.trim().length() != app_version.trim().length() ) {
								if ( current_version.trim().length() > app_version.trim().length() ) {

									int diff = current_version.trim().length() - app_version.trim().length();
									for ( int i = 0; i < diff; i++ ) {
										app_version = app_version.trim() + "0";
									}

								} else if ( app_version.trim().length() > current_version.trim().length()  ) {
									int diff = app_version.trim().length() - current_version.trim().length();
									for ( int i = 0; i < diff; i++ ) {
										current_version = current_version.trim() + "0";
									}
								}
							}


							if (current_version != null
									&& !current_version.trim().equalsIgnoreCase(
											app_version.trim()) && Integer.parseInt( current_version ) < Integer.parseInt( app_version ) ) {
								PlayupLiveApplication.showUpgradeDialog(apk_path);
							}

						} catch ( Exception e ) {
							//Logs.show ( e );
						} catch ( Error e ) {
							//Logs.show ( e );
						}


					}

				}

			}

		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}

	
	/**
	 * Chked
	 * @param runnableList
	 * @return
	 */
	public Runnable getRegionData(final Hashtable<String, Runnable> runnableList) {

		Runnable r = new Runnable() {

			@Override
			public void run() {
				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(Constants.GET_REGIONS)){
							runnableList.remove(Constants.GET_REGIONS);
						}
						
						return;
						
					}
						
					
					boolean isUpdated = false;
					
					Hashtable<String, Object> result = DatabaseUtil.getInstance().getRegionUrlFromRoot();
					String regionUrl = (String) result.get("url");
					Boolean isHrefURL = (Boolean) result.get("isHref");




					HttpRequest request;
					if(regionUrl != null && regionUrl.trim().length() > 0){

						if(isHrefURL){
							
							//encode the url and make a http request with both encoded and unencoded urls
							
							//encode the url and make a http request with both encoded and unencoded urls
							int tokenType = checkForHrefTokenType(regionUrl);
							 String vTokenValue = new Util().checkForHrefTokenParam(regionUrl); 
							 String encodedHrefURL = new Util().getPersonalizedEnocodedURL(regionUrl,vTokenValue,tokenType);
							
							request = new HttpRequest( /*unEncodedUrl,*/regionUrl,encodedHrefURL,isHrefURL,Constants.GET_METHOD );
							
						}else{
							
							 request = new HttpRequest( regionUrl,Constants.GET_METHOD );
							
						}	
						/*HttpRequest request = new HttpRequest( regionUrl, Constants.GET_METHOD );*/

						try {
							StringBuffer strBuffer = (StringBuffer) request.send();
							
							if(request.getStatusCode()==401){
								callTheCredentialsUrl(strBuffer);
								if(runnableList != null && runnableList.containsKey(Constants.GET_REGIONS)){
									runnableList.remove(Constants.GET_REGIONS);
								}
								
								return;
							}
							else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

								isUpdated = true;
								String currentRegion = DatabaseUtil.getInstance().getSelectedRegionId();
								JsonUtil json = new JsonUtil();
								json.setCurrentRegion(currentRegion);

								json.parse( strBuffer, Constants.TYPE_REGIONS_JSON, false );

								if ( strBuffer != null ) {
									strBuffer.setLength(0);
									strBuffer = null;
								}

							}
						} catch (RequestRepeatException e) {
							isUpdated = false;
							//Logs.show( e); 

						}catch (Exception e) {
							isUpdated = false;
							//Logs.show( e); 
						}finally{

							if(runnableList != null && runnableList.containsKey(Constants.GET_REGIONS))
								runnableList.remove(Constants.GET_REGIONS);

						}

						
						Message m =  new Message();
						m.obj = "Regions";
						
						if(isUpdated)
							m.arg1 = 1;
						else
							m.arg1 = 0;
						
						PlayupLiveApplication.callUpdateOnFragments(m);



					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}
			}

		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(r);
		return r;

	}

	
	/**
	 * Chked
	 * @return
	 */
	public Runnable getRegionsForCacheTime() {

		Runnable r = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						
						return;
						
					}
						
					
//	String regionUrl = DatabaseUtil.getInstance().getRegionUrlFromRoot();
					Hashtable<String, Object> result = DatabaseUtil.getInstance().getRegionUrlFromRoot();
					String regionUrl = (String) result.get("url");
					Boolean isHrefURL = (Boolean) result.get("isHref");

					HttpRequest request;


					if(regionUrl != null && regionUrl.trim().length() > 0){
						if(isHrefURL){
							
							//encode the url and make a http request with both encoded and unencoded urls
							
							//encode the url and make a http request with both encoded and unencoded urls
							 int tokenType = checkForHrefTokenType(regionUrl);
							 String vTokenValue = new Util().checkForHrefTokenParam(regionUrl); 
							 String encodedHrefURL = new Util().getPersonalizedEnocodedURL(regionUrl,vTokenValue,tokenType);
							
							request = new HttpRequest( /*unEncodedUrl,*/regionUrl,encodedHrefURL,isHrefURL,Constants.GET_METHOD );
							
						}else{
							
							 request = new HttpRequest( regionUrl,Constants.GET_METHOD );
							
						}	
						/*HttpRequest request = new HttpRequest( regionUrl, Constants.GET_METHOD );*/

						try {
							request.send();
							StringBuffer strBuffer = (StringBuffer) request.send();
							
								if(request.getStatusCode()==401){
									callTheCredentialsUrl(strBuffer);
									
									return;
								}
					

						} catch (RequestRepeatException e) {
							
						//	Logs.show( e); 

						}catch (Exception e) {
							
						//	Logs.show( e); 
						}

						
						Message m =  new Message();
						m.obj = "Regions";
						
						
						
						PlayupLiveApplication.callUpdateOnFragments(m);



					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show(e);
				}
			}

		};

		PlayupLiveApplication.getThreadPoolExecutor().execute(r);
		return r;

	}

/**
 * Chked
 * @param associatedUrl
 */
	public void getAssociatedContests (final  String associatedUrl) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				StringBuffer strBuffer;
				HttpRequest request = new HttpRequest( associatedUrl ,  Constants.GET_METHOD );
				try {


					strBuffer = (StringBuffer) request.send();

				} catch (RequestRepeatException e) {
					//Logs.show( e );

				}catch (Exception e) {
					//Logs.show( e );
				}


			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

	}




/**
 * Checked
 * @param assoicatedContestUrl
 * @param runnableList
 * @param isHref
 * @return
 */
	public Runnable getAssoiciatedContestsData ( final String assoicatedContestUrl,
			final Hashtable<String, Runnable> runnableList,final boolean isHref) {
		
		

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					if(Constants.isFetchingCredentials){
						
						if(runnableList != null && runnableList.containsKey(Constants.GET_ASSOCIATED_CONTEST_DATA)){
							runnableList.remove(Constants.GET_ASSOCIATED_CONTEST_DATA);
						}
						
						return;
						
					}
						
					boolean isUpdated	=	false;
					
					HttpRequest request = null;
					
					if(isHref){
						int tokenType = checkForHrefTokenType(assoicatedContestUrl);
						String vTokenValue = checkForHrefTokenParam(assoicatedContestUrl);
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							
							String encodedUrl = getPersonalizedEnocodedURL(assoicatedContestUrl, vTokenValue,tokenType);
							request = new HttpRequest( assoicatedContestUrl,encodedUrl,true, Constants.GET_METHOD );
						}else{
							request = new HttpRequest( assoicatedContestUrl, Constants.GET_METHOD );
						}
						
					}else{
						request = new HttpRequest( assoicatedContestUrl, Constants.GET_METHOD );
					}



					try {
						StringBuffer strBuffer = (StringBuffer) request.send();
						if(request.getStatusCode()==401){
									callTheCredentialsUrl(strBuffer);
										if(runnableList != null && runnableList.containsKey(Constants.GET_ASSOCIATED_CONTEST_DATA)){
									runnableList.remove(Constants.GET_ASSOCIATED_CONTEST_DATA);
								}
									return;
						}
						
						else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && 
								strBuffer != null && strBuffer.toString().trim().length() > 0 ) {

							
							
							//parse json for contests
							JsonUtil json  =new JsonUtil();
							json.parse(strBuffer, Constants.TYPE_ASSOCIATED_CONTEST_JSON, false );
							json = null;
							isUpdated	=	true;

							if(strBuffer!=null) {
								strBuffer.setLength(0);
								strBuffer = null;
							}

						}

						request = null;
					} catch (RequestRepeatException e) {

						//Logs.show( e );




						if(runnableList!=null&&runnableList.containsKey(Constants.GET_ASSOCIATED_CONTEST_DATA)){
							runnableList.remove(Constants.GET_ASSOCIATED_CONTEST_DATA);
						}
						isUpdated	=	false;
					}

					isUpdated  = true;
					
					
						
					
					if( isUpdated ) {
						Message msg = new Message ();
						msg.obj = "assoicated_contest_update";
						
						
						PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg );
					}
				} catch (Exception e) {
					//Logs.show(e);
				}finally{
					if(runnableList!=null&&runnableList.containsKey(Constants.GET_ASSOCIATED_CONTEST_DATA)){
						runnableList.remove(Constants.GET_ASSOCIATED_CONTEST_DATA);
					}
				}

			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}


	public Runnable getCountriesDataForCacheTime(final String vRegionId) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				try {
					if(Constants.isFetchingCredentials){
						
						return;
					}

					HttpRequest request = null;
					
					Hashtable<String, Object> result = DatabaseUtil.getInstance().getRegionUrl(vRegionId);
					
					if(result != null && result.containsKey("isHref")){
						
						String regionUrl = (String) result.get("url");
						
						if(((Boolean) result.get("isHref")).booleanValue()){
							
							String vTokenvalue = checkForHrefTokenParam(regionUrl);
							int tokenType = checkForHrefTokenType(regionUrl);
							if(vTokenvalue != null && vTokenvalue.trim().length() > 0){
								String encodedUrl = getPersonalizedEnocodedURL(regionUrl, vTokenvalue,tokenType);
								request = new HttpRequest( regionUrl,encodedUrl,true, Constants.GET_METHOD );
							}else{
								request = new HttpRequest( regionUrl, Constants.GET_METHOD );
							}
							
						}else{
							request = new HttpRequest( regionUrl, Constants.GET_METHOD );
						}
						
					}else{
						return;
					}

					
					
					
					
					
						try {
							StringBuffer strBuffer = (StringBuffer) request.send();
							
							
							
							if(request.getStatusCode() == 401){
								callTheCredentialsUrl(strBuffer);
							}else{
								
								Message m =  new Message();
								m.obj = "Countries";				
								PlayupLiveApplication.callUpdateOnFragments(m);
							}
							
							
						} catch (RequestRepeatException e) {
							//Logs.show(e);
						}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show(e);
				}

					

				



			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}

	
	/**
	 * Checked
	 * @param vRegionId
	 * @param runnableList
	 * @return
	 */
	public Runnable getCountriesData(final String vRegionId,
			final Hashtable<String, Runnable> runnableList) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {

				

				if(Constants.isFetchingCredentials){
					if(runnableList != null && runnableList.containsKey(Constants.GET_COUNTRIES_DATA)){
						runnableList.remove(Constants.GET_COUNTRIES_DATA);
					}
					return;
					
				}


				try {
					boolean isUpdated = false;
					Hashtable<String, Object> result = DatabaseUtil.getInstance().getRegionUrl(vRegionId);
					HttpRequest request = null;
					if(result != null && result.containsKey("isHref")){
						String regionUrl = (String) result.get("url");
						if(((Boolean) result.get("isHref")).booleanValue()){
							
							String vTokenValue = checkForHrefTokenParam(regionUrl);
							int tokenType = checkForHrefTokenType(regionUrl);
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								
								String encodedUrl = getPersonalizedEnocodedURL(regionUrl, vTokenValue,tokenType);
								
								request = new HttpRequest( regionUrl,encodedUrl,true, Constants.GET_METHOD );
								
							}else{
								request = new HttpRequest( regionUrl, Constants.GET_METHOD );
							}
							
						}else{
							request = new HttpRequest( regionUrl, Constants.GET_METHOD );
						}
					}else{
						return;
					}
						

				
			
				
				try {
					StringBuffer strBuffer = (StringBuffer) request.send();
					
					if(request.getStatusCode() == 401){
						callTheCredentialsUrl(strBuffer);
						if(runnableList != null && runnableList.containsKey(Constants.GET_COUNTRIES_DATA)){
							runnableList.remove(Constants.GET_COUNTRIES_DATA);
						}
						return;
						
						

					}else if ( ! handleServerNotFound ( request.getStatusCode ( ) ) && strBuffer != null && strBuffer.toString().trim().length() > 0 ) {
							
							isUpdated = true;
							String currentRegion = DatabaseUtil.getInstance().getSelectedRegionId();
							//parse json for contests
							JsonUtil json  =new JsonUtil();
							json.setCurrentRegion(currentRegion);
							json.parse(strBuffer, Constants.TYPE_COUNTRIES_JSON, false );
							json = null;


							if(strBuffer!=null) {
								strBuffer.setLength(0);
								strBuffer = null;
							}

						}

						request = null;
					} catch (RequestRepeatException e) {
						isUpdated = false;
						//Logs.show( e );


					}finally{

						if(runnableList!=null&&runnableList.containsKey(Constants.GET_COUNTRIES_DATA)){
							runnableList.remove(Constants.GET_COUNTRIES_DATA);
						}
					}

					Message m =  new Message();
					m.obj = "Countries";
					
					if(isUpdated)
						m.arg1 = 1;
					else
						m.arg1 = 0;
					
					PlayupLiveApplication.callUpdateOnFragments(m);
				} catch (Exception e) {
				//	Logs.show(e);
				}finally{
					

					if(runnableList!=null&&runnableList.containsKey(Constants.GET_COUNTRIES_DATA)){
						runnableList.remove(Constants.GET_COUNTRIES_DATA);
					}
					
				}



			}

		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);

		return runnable;
	}


	/**
	 * Checked
	 * @param vTeamScheduleUrl
	 * @param isHrefURL
	 * @param runnableList
	 * @return
	 */
	public Runnable getTeamScheduleData ( final String vTeamScheduleUrl,final boolean isHrefURL, final Hashtable < String, Runnable > runnableList ) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				if(Constants.isFetchingCredentials){
					if ( runnableList != null && runnableList.containsKey ( vTeamScheduleUrl ) ) {
						runnableList.remove ( vTeamScheduleUrl );
					}
					return;
					
				}

				boolean isUpdateAvailable = false;
				String encodedHrefURL	= null;
				String vTokenValue		= 	null;

				HttpRequest request ;
				try {
					
					if(isHrefURL){
						
						//encode the url and make a http request with both encoded and unencoded urls
						int tokenType = checkForHrefTokenType(vTeamScheduleUrl);
						vTokenValue	=	new Util().checkForHrefTokenParam(vTeamScheduleUrl); 
						encodedHrefURL	=	new Util().getPersonalizedEnocodedURL(vTeamScheduleUrl,vTokenValue,tokenType);
					
						
						request = new HttpRequest( /*unEncodedUrl,encoded url*/vTeamScheduleUrl,encodedHrefURL,isHrefURL,Constants.GET_METHOD );
						
					}else{
						
						 request = new HttpRequest( vTeamScheduleUrl,Constants.GET_METHOD );
						
					}
					
				
					
					StringBuffer strBuffer = (StringBuffer) request.send();
					if(request.getStatusCode() == 401){
						callTheCredentialsUrl(strBuffer);
						if ( runnableList != null && runnableList.containsKey ( vTeamScheduleUrl ) ) {
							runnableList.remove ( vTeamScheduleUrl );
						}

						return;
						
						

					}else
					
					// handling 404
					if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {

						isUpdateAvailable = true;
						JsonUtil json = new JsonUtil();
						json.vTeamScheduleUrl =vTeamScheduleUrl;
						json.parse( strBuffer, Constants.TYPE_TEAM_SCHEDULE_JSON, false );
					}
					strBuffer = null;
					
//					if(strBuffer!=null)
//						strBuffer.setLength(0);
				} catch (RequestRepeatException e) {
					
					//Logs.show(e);

				}catch(Exception e){
					
					//Logs.show(e);
					
					
				}

				if ( runnableList != null && runnableList.containsKey ( vTeamScheduleUrl ) ) {
					runnableList.remove ( vTeamScheduleUrl );
				}

				
				
					Message msg = new Message ();
					if( isUpdateAvailable )
						msg.arg1 = 1;
					else
						msg.arg1 = 0;
					msg.obj = "refreshTeamSchedule";
					PlayupLiveApplication.callUpdateOnFragments( msg );					
				
					

			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
		return runnable;
	}
	
	
/**
 * Checked
 * @param jsonObj
 */
	public void setColor ( JSONObject jsonObj ) {

		try {
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			 final String SELF_KEY          =  ":self";
			 final String UID_KEY         	=  ":uid";
			 final String HREF_LINK			= 	":href";
			
			String vMainColor = null;
			String vSecColor = null;
			String vMainTitleColor = null;
			String vSecTitleColor = null;
			String vHeaderImg = null;

			if ( jsonObj.has( Constants.STYLE_KEY ) ) {

				JSONObject style_jObj = jsonObj.getJSONObject( Constants.STYLE_KEY );

				vMainColor = style_jObj.optString( Constants.MAIN_COLOR_KEY );
				vSecColor = style_jObj.optString( Constants.SEC_COLOR_KEY );

				vMainTitleColor = style_jObj.optString( Constants.MAIN_TITLE_COLOR_KEY );
				vSecTitleColor = style_jObj.optString( Constants.SECONDARY_TITLE_COLOR_KEY );

				JSONArray headerImgArr = style_jObj.optJSONArray( Constants.HEADER_IMG_KEY );

				if ( headerImgArr != null && headerImgArr.length() > 0 ) {
					int headerImgArr_len = headerImgArr.length();

					for ( int j = 0; j < headerImgArr_len; j++ ) {

						JSONObject headerImgArr_jObj = headerImgArr.getJSONObject( j );

						String density = headerImgArr_jObj.getString( Constants.DENSITY_KEY );

						if ( Constants.DENSITY.equalsIgnoreCase( density ) ) {
							vHeaderImg = headerImgArr_jObj.getString( Constants.HREF_KEY );
						}
					}
				}
			}
			
			
			//dbUtil.setColor ( jsonObj.optString( SELF_KEY ),jsonObj.optString(HREF_LINK), jsonObj.optString( UID_KEY ), vMainColor, vSecColor, vMainTitleColor, vSecTitleColor, vHeaderImg );
			//modified as per HREF
			dbUtil.setColor ( jsonObj.optString( SELF_KEY ),jsonObj.optString(HREF_LINK), jsonObj.optString( UID_KEY ), vMainColor, vSecColor, vMainTitleColor, vSecTitleColor, vHeaderImg );
		} catch (JSONException e) {
		//	Logs.show(e);
		}


	}

	
	/**
	 * Call the Crentials Root API to get the URL to get the credentials and POSt body
	 */
	
	public void callRootApi() {
		
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				
				try {
					HttpRequest req = new HttpRequest(Keys.ROOT_URL,null, null, Constants.GET_METHOD, null);
					StringBuffer response = (StringBuffer) req.send();
					
					
					if(req.getStatusCode() == 401){
						
						if(response != null && response.toString().trim().length() > 0){
							
							Constants.isFetchingCredentials = true;
							
							PlayUpActivity.runnableList.put(Constants.GET_CREDENTIALS, true);
							new Util().getCredentials(response);
						}else{
							
							//handle the error condition
							if(Util.isInternetAvailable()){
							String message = "errorFethchingCredentials";
							PlayUpActivity.showErrorDialog(message);
							}

						}
						
					}else if(!(handleServerNotFound(req.getStatusCode())) && response != null && response.toString().trim().length() > 0){
						JsonUtil json = new JsonUtil();
						json.parse (response, Constants.TYPE_BASE_URL, false );
						json = null;
						
						Message m = new Message();
						m.obj = "moveToNextFragment";
						PlayupLiveApplication.callUpdateOnFragments(m);
						
						
						
						
					}
					
//					if(response != null){
//						response.setLength(0);
//						response = null;
//					}
					
					req = null;
					
				} catch (Exception e) {
					//Logs.show(e);
				}
				
				
				
			}
		};
		
		PlayupLiveApplication.getThreadPoolExecutor().execute(r);
		
		
	}
	
	/**
	 * Parse the root doc and get the MAC credentials URL and call it in HTTPS to get the MAC Id and Seceret
	 * @param strBuffer
	 */
	public  void getCredentials(final StringBuffer strBuffer) {	
		
		
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				
				
				
				try {
					
					
					JSONObject json = new JSONObject(strBuffer.toString());
					if(json.getString(":type") != null && json.getString(":type").trim().length() > 0 && 
							json.getString(":type").equalsIgnoreCase(Types.CREDENTIAL_TYPE)){
						String vCredentialUrl = json.getString(":self");
							
						if(vCredentialUrl != null && vCredentialUrl.trim().length() > 0){
							HttpRequest req = null;
							if(vCredentialUrl.startsWith("https"))
								 req = new HttpRequest(vCredentialUrl,Constants.POST_METHOD,true);
							else
								req = new HttpRequest(vCredentialUrl,Constants.POST_METHOD,false);
							
							if(!Util.isInternetAvailable()){
								
								return;
							}
							
							StringBuffer response = (StringBuffer) req.send();
							
							
							
								
							
							if(!handleServerNotFound(req.getStatusCode()) && response != null && response.toString().trim().length() > 0){
								
								//parse the MAC credentials, encrypt and store in the db.
								
								
								JSONObject credentials = new JSONObject(response.toString());
								String mac_Id = credentials.getString("id");
								String mac_Secret = credentials.getString("secret");
								
								DatabaseUtil.getInstance().setCredentials(mac_Id,mac_Secret);
								
								Constants.isFetchingCredentials = false;
								PlayUpActivity.runnableList.put(Constants.GET_CREDENTIALS, false);
								
								
								
								
								if(response != null){
									response.setLength(0);
									response = null;
									
								}
								if(PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName()!=null && PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName().equalsIgnoreCase("SplashFragment"))
								{
									 Message m = new Message();
									 m.obj = "credentials stored";
									 PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(m);
								
									 
								}
								else{
									
									
									getDataFromServer(true);
									
									/*if(PlayUpActivity.handler!=null)
										{
											PlayUpActivity.handler.post(new Runnable() {
												
												@Override
												public void run() {
													// TODO Auto-generated method stub
													getDataFromServer();
												}
											});
										
										}*/
								}
							
								
								
							}else{
								if(Util.isInternetAvailable()){
								
								String message = "errorFethchingCredentials";
								PlayUpActivity.showErrorDialog(message);
								}
							}
							
						}else{
							//handle error condition
							if(Util.isInternetAvailable()){
							String message = "errorFethchingCredentials";
							PlayUpActivity.showErrorDialog(message);
							}
						}
						
					
					}else{
						
						//handle error condition
						if(Util.isInternetAvailable()){
						String message = "errorFethchingCredentials";
						PlayUpActivity.showErrorDialog(message);
						}
					}
					
					
					
					
				} catch (Exception e) {
					
				//	Logs.show(e);
					//Log.e("123", "Inside handle the error condition!!!!!!!!!!!!catch!!!!!!!!!!!!");
					if(Util.isInternetAvailable()){
					String message = "errorFethchingCredentials";
					PlayUpActivity.showErrorDialog(message);
					}
					
				}finally{
					if(strBuffer != null){
						strBuffer.setLength(0);	
					}
					
				}
				
				
				
			}
		};
		
		PlayupLiveApplication.getThreadPoolExecutor().execute(r);
		
	}
/**
 * Check if the href link contains valid provider token and if yes replace with the encoded value
 * @param vHrefUrl
 * @return
 */




	/**
	 * Praveen :
	 * Checnk whether the URL contains '{token:dfadf}' value for param token
	 * @param url
	 * @return value of param token / null
	 */
	public String checkForHrefTokenParam(String url) {

		try {
			if(url != null){
				URL aURL = new URL(url);
				String query = aURL.getQuery();
					
				
				
				if(query != null && query.trim().length() > 0){

						String[] params = query.split("&");
							
						for (String param : params) {
								if(param != null && param.trim().length() > 0){
										String value = param.split("=")[1];
										
										if(value.startsWith("{token:")){
											//	Logs.show("value------>>>>"+value);
												return value;
										}
										else if(value.startsWith("{app_identifier")){
											//Logs.show("value------>>>>"+value);
											//return Constants.TOKEN_APP_IDENTIFIER_VALUE;
											return value;
									}
								}

						}

				}
				return null;
			}
				
		} catch (Exception e) {
		///Logs.show(e);
		}
		return null;
	}
	
	public int checkForHrefTokenType(String url) {

		try {
			if(url != null){
				URL aURL = new URL(url);
				String query = aURL.getQuery();
					
				
				
				if(query != null && query.trim().length() > 0){

						String[] params = query.split("&");
							
						for (String param : params) {
								if(param != null && param.trim().length() > 0){
										String value = param.split("=")[1];
										
										if(value.startsWith("{token:")){
												//Logs.show("value------>>>>"+value);
												return 0;
										}
										else if(value.startsWith("{app_identifier")){
											//Logs.show("value------>>>>"+value);
											return 1;
									}
								}

						}

				}
				return -1;
			}
				
		} catch (Exception e) {
		//Logs.show(e);
		}
		return -1;
	}

	

	/**
	 * Checked
	 * @return
	 * @throws JSONException
	 */

	public Hashtable<String,Object> refreshDataFromServer() throws JSONException {

		if(Constants.isFetchingCredentials){
			return null;
			

		}
		
	//	Log.e("123", "Inside refreshDataFromServer >>>>>>>>>>>>>>>>>>>>>>>>>");
		
		Hashtable<String,Object> result = new Hashtable<String, Object>();
				
			
		DatabaseUtil.getInstance().removeEtag( Keys.ROOT_URL);
				// Request to Base URL
				HttpRequest request = new HttpRequest ( Keys.ROOT_URL, null, null, Constants.GET_METHOD, null );
				try {

					// getting the response
					StringBuffer strBuffer = (StringBuffer) request.send();
					
					
					//Log.e("123", "Inside refreshDataFromServer >>>>>>>>>>>>>>>>>>>>>>>>>"+strBuffer);
					
					if(request.getStatusCode() == 401){
						callTheCredentialsUrl(strBuffer);
						return null;
						
						

					}else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0 ) {
						
						JSONObject jsonObj 	 = null;
						jsonObj 			= 	new JSONObject( strBuffer.toString() );
						
						String SELF_KEY 		= ":self";
						String PROFILE			= "profile";
						String TYPE_KEY         = ":type";
						
						String HREF_URL_KEY          =  ":href";
				


					

						JSONObject jsonObject		=	jsonObj.optJSONObject(PROFILE);

						if(jsonObject.getString(TYPE_KEY).equalsIgnoreCase(Types.PROFILE_DATA_TYPE)){
							
							String url = jsonObject.optString(HREF_URL_KEY);
							if(url != null && url.trim().length() > 0){
								
								result.put("url",url);
								result.put("isHref",true);
								
							}else{
							
								url = jsonObject.optString(SELF_KEY);
								
								result.put("url",url);
								result.put("isHref",false);
							}

							
						}
						if(strBuffer!=null) {
							strBuffer.setLength(0);
							strBuffer = null;
						}
					}
					

					request = null;
				} catch (RequestRepeatException e) {
					//Logs.show ( e );
				}

				return result;



			}

			

	
	


	public Hashtable<String,List<String>> refreshProfileData() {
		
		Hashtable<String,List<String>> providerTokenUrl = new Hashtable<String,List<String>>();

			
				
				try {
					
					if(Constants.isFetchingCredentials){
						return null;
						
					}
					
					//Log.e("123", "Inside refreshProfileData >>>>>>>>>>>>>>>>>>>>>>>>>");
						

					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result = dbUtil.getProfileURLFromRootResource();
					String profileUrl = null;
					//Log.e("123", "Inside refreshProfileData result>>>>>>>>>>>>>>>>>>>>>>>>>"+result);
					HttpRequest request = null;
					if(result == null || (result.get("url").toString().trim().length() == 0)){
						
						
						
					//	Log.e("123", "Inside refreshProfileData profileUrl inside result>>>>>>>>>>>>>>>>>>>>>>>>>"+profileUrl);
						
						
							
							result = refreshDataFromServer();
						
							
						
					}
					
				//	Log.e("123", "Inside refreshProfileData after result != null>>>>>>>>>>>>>>>>>>>>>>>>>"+result);
					if(result == null || ((String) result.get("url")) == null || (result.get("url").toString().trim().length() == 0))
						return null;
					
					profileUrl = (String)result.get("url");
				
					
					if((Boolean) result.get("isHref")){
						
						
						String vTokenValue = checkForHrefTokenParam(profileUrl);
						int tokenType = checkForHrefTokenType(profileUrl);
						if(vTokenValue != null && vTokenValue.trim().length() > 0){
							String encodedUrl = getPersonalizedEnocodedURL(profileUrl, vTokenValue,tokenType);
							 request = new HttpRequest( profileUrl,encodedUrl,true, Constants.GET_METHOD );
							
						}else{
							
							 request = new HttpRequest( profileUrl, Constants.GET_METHOD );
							
						}
						
						
					}else{
						
						 request = new HttpRequest( profileUrl, Constants.GET_METHOD );
						
					}
					
					
					try {
						DatabaseUtil.getInstance().removeEtag(profileUrl);
						StringBuffer strBuffer = (StringBuffer) request.send();
						//Log.e("123", "Inside refreshProfileData strBuffer>>>>>>>>>>>>>>>>>>>>>>>>>"+strBuffer);
						
						if(request.getStatusCode()==401){
							callTheCredentialsUrl(strBuffer);
							
							return null;
						}
					
						else if (!handleServerNotFound(request.getStatusCode())
								&& strBuffer != null && strBuffer.toString().trim().length() > 0) {
							

							JSONObject jsonObj = new JSONObject( strBuffer.toString() );
							
							String PROVIDER_TOKENS_KEY =  "provider_tokens";
							String HREF_KEY = ":href";
							String SELF_KEY =  ":self";
							
							String TYPE_KEY =  ":type";
							
								JSONObject providerTokens = jsonObj.getJSONObject(PROVIDER_TOKENS_KEY);
								
								
								if(providerTokens.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)){
									
									
								
								if(providerTokens.has(HREF_KEY)){
									
									
									List<String> li = new ArrayList<String>();
									li.add(providerTokens.getString(HREF_KEY));
										
									
									providerTokenUrl.put("vProviderTokenHref",li); 
									
									
									
									
									
								}else if(providerTokens.has(SELF_KEY)){
									
									List<String> li = new ArrayList<String>();
									li.add(providerTokens.getString(SELF_KEY));
									
									providerTokenUrl .put("vProviderTokenSelf", li);
									
								}
								
								
								}
						
							
							//Log.e("123","refreshProfileData>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  providerTokenUrl =============== "+providerTokenUrl);
							
							if(strBuffer!=null)
								strBuffer.setLength(0);
						}
						
					} catch (RequestRepeatException e) {
					//	Logs.show(e);
					}

				
				} catch (Exception e) {
					//Logs.show ( e );
				}
				
				return providerTokenUrl;
			}
		
	


	

	public synchronized void refreshProviderTokens(String vProviderTokenId){
		String vProviderTokenValue = 	DatabaseUtil.getInstance().getProviderToken(vProviderTokenId);
		//Log.e("123", "Inside refresh provider tokens start");
		
		if(vProviderTokenValue == null || vProviderTokenValue.trim().length() == 0){

		
			try {
			if(Constants.isFetchingCredentials){
			

				return;
			}
				
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			Hashtable<String, List<String>> url = dbUtil.getProviderTokensUrl();
			
			if(url == null || ((url.get("vProviderTokenHref") == null || url.get("vProviderTokenHref").get(0) == null ||
					url.get("vProviderTokenHref").get(0).trim().length() == 0) &&
					(url.get("vProviderTokenSelf") == null || url.get("vProviderTokenSelf").get(0) == null ||
							url.get("vProviderTokenSelf").get(0).trim().length() == 0))){
				
			//	Log.e("123", "refreshProviderTokens()---url == null ");
				url = refreshProfileData();
				
				
				
			}
			
			
			
			String providerTokenUrl = "";
			HttpRequest req = null;
			
			
			
			if(url != null ){
				
				//Log.e("123", "Inside refresh provider tokens in 2 if");
				
				if(url.get("vProviderTokenHref") != null && url.get("vProviderTokenHref").size() > 0 && 
						url.get("vProviderTokenHref").get(0) != null && 
						url.get("vProviderTokenHref").get(0).trim().length() > 0){

					providerTokenUrl = url.get("vProviderTokenHref").get(0);
					String vTokenValue = checkForHrefTokenParam(providerTokenUrl);
					int tokenType = checkForHrefTokenType(providerTokenUrl);


					 
					if(vTokenValue != null && vTokenValue.length() >0){
						//Log.e("123", "Inside refresh provider tokens in 3 if");
						//Do the process of replacing the encoded personalized value
						String encodedUrl = new Util().getPersonalizedEnocodedURL(providerTokenUrl,vTokenValue,tokenType);
						
						req = new HttpRequest(providerTokenUrl,encodedUrl,true, Constants.GET_METHOD);
					}else{
						
						req = new HttpRequest(providerTokenUrl, Constants.GET_METHOD);
						
					}
					
					
					
				}else if(url.get("vProviderTokenSelf") != null && url.get("vProviderTokenSelf").size() > 0 && 
						url.get("vProviderTokenSelf").get(0) != null && 
						url.get("vProviderTokenSelf").get(0).trim().length() > 0){
					
					providerTokenUrl = url.get("vProviderTokenSelf").get(0);
					
					req = new HttpRequest(providerTokenUrl, Constants.GET_METHOD);
					
				}
				
				
				if(req != null){

				StringBuffer response = (StringBuffer) req.send();
				
				
				if(req.getStatusCode() == 401){

					callTheCredentialsUrl(response);
					return;
					

				}else if(!handleServerNotFound(req.getStatusCode()) && 
						response != null && response.toString().trim().length() > 0){
					

					JsonUtil json = new JsonUtil();
					json.parse (response, Constants.PROVIDER_TOKEN, false );
					json = null;
					
					if(response != null){
						
						response.setLength(0);
						response = null;
						
				}
					
			}
						
						
		}	
						
					
	}
		} catch (Exception e) {
		//	Logs.show(e);
		}
		
		
		
			
	}
		
		
		
		
}
	
	
	


	/**
	 * Checked
	 */


	public void getProviderTokens() {
		
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				
				
				
				try {
					
				
					if(Constants.isFetchingCredentials)
						return;
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, List<String>> url = dbUtil.getProviderTokensUrl();
					
					
					
					String providerTokenUrl = "";
					HttpRequest req = null;
					
					
					
					if(url != null ){
						
						
						
						if(url.get("vProviderTokenHref") != null && url.get("vProviderTokenHref").size() > 0 && 
								url.get("vProviderTokenHref").get(0) != null && url.get("vProviderTokenHref").get(0).trim().length() > 0){
							
							providerTokenUrl = url.get("vProviderTokenHref").get(0);
							
							String vTokenValue = checkForHrefTokenParam(providerTokenUrl);
							int tokenType = checkForHrefTokenType(providerTokenUrl);
							
							if(vTokenValue != null && vTokenValue.trim().length() > 0){
								
								String encodedUrl = getPersonalizedEnocodedURL(providerTokenUrl, vTokenValue,tokenType);
								req = new HttpRequest(providerTokenUrl,encodedUrl,true, Constants.GET_METHOD);
								
							}else{
								
								req = new HttpRequest(providerTokenUrl, Constants.GET_METHOD);
								
							}
							
							
							
						}else if(url.get("vProviderTokenSelf") != null && url.get("vProviderTokenSelf").size() > 0 && 
								url.get("vProviderTokenSelf").get(0) != null &&url.get("vProviderTokenSelf").get(0).trim().length() > 0){
							
							providerTokenUrl = url.get("vProviderTokenSelf").get(0);
							
							req = new HttpRequest(providerTokenUrl, Constants.GET_METHOD);
							
						}
						
						
						//Log.e("123","req in vProviderToken >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+req);
						
						DatabaseUtil.getInstance().removeEtag(providerTokenUrl);
						
						StringBuffer response = (StringBuffer) req.send();
						
						//Log.e("123","provider token >>>>>>>>>>>>>>>>>>>>>> "+response);
						if(req.getStatusCode() == 401){
							callTheCredentialsUrl(response);
							return;
							

						}else if(!handleServerNotFound(req.getStatusCode()) && response != null && response.toString().trim().length() > 0){
							
							
							JsonUtil json = new JsonUtil();
							json.parse (response, Constants.PROVIDER_TOKEN, false );
							json = null;
							
							if(response != null){
								
								response.setLength(0);
								response = null;
								
							}
							
							
							
						}
								
								
								
								
							
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				

				
				
			}

			
		};
		
		PlayupLiveApplication.getThreadPoolExecutor().execute(r);
		
		
	}


/**
 * Praveen:
 * @param url
 * @param vTokenValue
 * @param tokenType 
 * @return
 */
	public String getPersonalizedEnocodedURL(String url,String vTokenValue, int tokenType) {
		String vProviderTokenId = 	null;
		String returnString = url;
		
	//	
		
		if((url != null && url.length() > 0) && (vTokenValue != null && vTokenValue.length()>0) && tokenType != -1 ){
			try {
				vProviderTokenId 	=	 vTokenValue;
				
				switch (tokenType) {
				case 0:
					returnString=	templateToken(url,vTokenValue,vProviderTokenId);
					break;
					
				case 1:
					returnString=	templateAppIdentifier(url,vTokenValue,vProviderTokenId);
				default:
					break;
				}
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				///Logs.show(e);
			}
			
		}
		
		return returnString;
	}
	
/**
 * 
 * @param url
 * @param vTokenValue
 * @param vProviderTokenId
 * @return
 */
	private String templateAppIdentifier(String url, String vTokenValue,String vProviderTokenId) {
	// TODO Auto-generated method stub
		//Log.e("234","url================>"+url);
		//Log.e("234","vTokenValue========>"+vTokenValue);
		//Log.e("234","vProviderTokenId====>"+vProviderTokenId);
		
		if(vTokenValue != null && vTokenValue.length() > 0){
			
			url = url.replace(vTokenValue,Constants.TOKEN_APP_IDENTIFIER_VALUE );
		//	Log.e("234","RETURN url================>"+url);
			return url;
		}
	
	return url;
}

	private String templateToken(String url, String vTokenValue, String vProviderTokenId) {
		try {
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			String encodedValue		= 	null;
			
			
		//	Log.e("123","vProviderTokenValue------------->>>"+ vProviderTokenId);
			if(vProviderTokenId.endsWith("}")){
				vProviderTokenId		=	vProviderTokenId.substring(0, vProviderTokenId.length()-1); 
				vProviderTokenId		=	vProviderTokenId.substring(7);
			}
		//	Log.e("123","vProviderTokenValue------------->>>"+ vProviderTokenId);
			
			String vProviderTokenValue = 	dbUtil.getProviderToken(vProviderTokenId);
		//	Log.e("123", "getPersonalizedEnocodedURL()------"+vProviderTokenValue);
			if(vProviderTokenValue == null || vProviderTokenValue.trim().length() == 0){
				
		//		Log.e("123", "getPersonalizedEnocodedURL()---in if--"+vProviderTokenValue);
				
				refreshProviderTokens(vProviderTokenId);
				
				vProviderTokenValue = 	dbUtil.getProviderToken(vProviderTokenId);
			}
			
			Hashtable<String, String> credentials = dbUtil.getCredentials();
			
			String vMacSecret 	= 	"";

			
			if(credentials.containsKey("vSecret") && credentials.get("vSecret") != null )
				vMacSecret = credentials.get("vSecret");

			
		//	Log.e("123", "vMacSecret=============>>>>"+vMacSecret);
		//	Log.e("123", "vProviderTokenValue=============>>>>"+vProviderTokenValue);
			
			if((vProviderTokenValue != null && vProviderTokenValue.length() > 0) && (vMacSecret != null && 
					vMacSecret.length() > 0)){
				Crypto crypto = new Crypto();
				encodedValue	= 	crypto.createAES256CBCString(vProviderTokenValue, vMacSecret);
			}
			else{
				url = url.replace(vTokenValue,"" );
			}
			if(encodedValue != null && encodedValue.length() > 0){
				
					url = url.replace(vTokenValue,encodedValue );
					return url;
				}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show(e);
		}
	return url;
}

	/**
	 * cHCKED
	 *  getting the public profile data 
	 * */
	public void getFanProfileData( final String vSelfUrl, final boolean isHref ) {

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				
				if(Constants.isFetchingCredentials){
				
					return;
					
				}
					
				boolean isUpdated = false;
				
				DatabaseUtil.getInstance().removeEtag(vSelfUrl);
				// request to get the token
			
				HttpRequest request = null;
				String encodedHrefURL	= null;
				String vTokenValue		= 	null;
				
				
				try {
								if(isHref){
									
									
									
									//encode the url and make a http request with both encoded and unencoded urls
									int tokenType = checkForHrefTokenType(vSelfUrl);
									 vTokenValue = new Util().checkForHrefTokenParam(vSelfUrl); 
									 encodedHrefURL = new Util().getPersonalizedEnocodedURL(vSelfUrl,vTokenValue,tokenType);
									
									request = new HttpRequest( /*unEncodedUrl,*/vSelfUrl,encodedHrefURL,isHref,Constants.GET_METHOD );
									
								}else{
									
									 request = new HttpRequest( vSelfUrl,Constants.GET_METHOD );
									
								}
								try {
									StringBuffer strBuffer = (StringBuffer) request.send();
									
									isUpdated = true;
															if(request.getStatusCode()==401){
																callTheCredentialsUrl(strBuffer);
																return;
															}
															else if (!handleServerNotFound(request.getStatusCode()) && strBuffer != null && strBuffer.toString().trim().length() > 0) {
										
																// parse and save in database.
																JsonUtil json = new JsonUtil();
																json.parse ( strBuffer, Constants.TYPE_PROFILE_URL, false );
																if(strBuffer!=null)
																	strBuffer.setLength(0);
															}
									
								} catch (RequestRepeatException e) {
									
									isUpdated = false;
									//Logs.show(e);
				
								}

				// updating the UI
				
								Message msg = new Message ();
								msg.obj = "FAN_PROFILE_DATA";
								if(isUpdated){
								
									msg.arg1 = 1;
								
								}else{
									
									msg.arg1 = 0;
									
								}
				
				PlayupLiveApplication.callUpdateOnFragments( msg );
			}catch(Exception e){
			//	Logs.show(e);
				}
			}
		};
		PlayupLiveApplication.getThreadPoolExecutor().execute(runnable);
	}
	
	
	
	
	/**
	 * After the expiration of crednetials, new credentials are obtainded and launching screen is initiated again
	 */
	public void callLaunchScreen(){
		
		
		
		try{
			
			
			if(!Constants.isCurrent){
				
				
				VideoActivity.context.finish();
				
			}
			
			//PlayupLiveApplication.showToast("Please wait! Application is refreshing.");
		
//			Toast.makeText(PlayUpActivity.context, " Session has Expired. Please Login Again! ", Toast.LENGTH_SHORT).show();
			PlayupLiveApplication.showToast(R.string.app_refreshing);
			PlayupLiveApplication.getFragmentManagerUtil().popBackStack();



			PlayupLiveApplication.getFragmentManagerUtil().startTransaction();

			PlayupLiveApplication.getFragmentManagerUtil().setFragment ( "TopBarFragment", R.id.topbar  );

			new Thread(  new Runnable () {

				@Override
				public void run() {
					
					
					
					try {
						final Hashtable<String, List<String>> hotItemData = DatabaseUtil.getInstance().getStartingScreenData();
						

						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable () {

								@Override
								public void run() {
									
									try {
										if(hotItemData != null &&(( hotItemData.get("resource_url") != null && hotItemData.get("resource_url").size() > 0)
												|| ( hotItemData.get("resource_href") != null && hotItemData.get("resource_href").size() > 0))){

											Bundle bundle = new Bundle();
											
											 if(hotItemData.get("resource_href").get(0) !=   null && 
														hotItemData.get("resource_href").get(0).trim().length() > 0){
													
													bundle.putString("vSectionUrl", hotItemData.get("resource_href").get(0) );
													bundle.putBoolean("isHref",true);
													
												}else if(hotItemData.get("resource_url").get(0) !=   null && 
													hotItemData.get("resource_url").get(0).trim().length() > 0){
												
												bundle.putString("vSectionUrl", hotItemData.get("resource_url").get(0) );
												bundle.putBoolean("isHref",false);
											}
											
											
											PlayupLiveApplication.getFragmentManagerUtil().setFragment( "DefaultFragment",bundle);
										}else{
											PlayupLiveApplication.getFragmentManagerUtil().setFragment ( "AllSportsFragment" );
										}
										
										PlayupLiveApplication.getFragmentManagerUtil().endTransaction();
									} catch (Exception e) {
										
										//Logs.show(e);
									}		
								}
								
							});
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//Logs.show(e);
					}
					

				}

			}).start();
		}catch(Exception e ){
			//Logs.show(e);
		}
		
	}
		

	
	
	
}
