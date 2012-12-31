package com.playup.android.connection.methods;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Keys;
import com.playup.android.util.Logs;

import com.playup.android.util.MacCreation;
import com.playup.android.util.PreferenceManagerUtil;
import com.playup.android.util.Util;

public class GetConnectionMethod_JavaNet {


	private String url;
	private List < NameValuePair > paramNameValuePair; 

	private int STATUS_CODE	=	-1;

	private int isContentChanged	=	Constants.NO_NEW_CONTENT_AVAIL;//304 means no change in content
	private HttpURLConnection urlConnection;
	private String host;
	private String port;

	private String unEncodedUrl = null;

	public GetConnectionMethod_JavaNet ( String url, List< Header > header, String authenticator, List < NameValuePair > paramNameValuePair ,String unEncodedUrl) {

		String locale = PlayupLiveApplication.getInstance().getResources().getConfiguration().locale.getLanguage();

		if ( Constants.ACCEPT_LANGUAGE != null && Constants.ACCEPT_LANGUAGE.trim().length() > 0 && !locale.equalsIgnoreCase(Constants.ACCEPT_LANGUAGE ) ) {

			// remove whats hot and euro data
			DatabaseUtil.getInstance().dropTables();
			new Util().getDataFromServer(false);

		}
		Constants.ACCEPT_LANGUAGE	=	locale;

		PreferenceManagerUtil preferenceManagerUtil = new PreferenceManagerUtil();
		preferenceManagerUtil.set( "locale" , Constants.ACCEPT_LANGUAGE );

		this.url = url;
		this.paramNameValuePair = paramNameValuePair;
		
		
		this.unEncodedUrl  = unEncodedUrl;

	}
	
	
	public GetConnectionMethod_JavaNet ( String url, List< Header > header, String authenticator, List < NameValuePair > paramNameValuePair,String fromFragment, Bundle refreshData,String unEncodedUrl ) {

		String locale = PlayupLiveApplication.getInstance().getResources().getConfiguration().locale.getLanguage();

		if ( Constants.ACCEPT_LANGUAGE != null && Constants.ACCEPT_LANGUAGE.trim().length() > 0 && !locale.equalsIgnoreCase(Constants.ACCEPT_LANGUAGE ) ) {

			// remove whats hot and euro data
			DatabaseUtil.getInstance().dropTables();
			new Util().getDataFromServer(false);

		}
		Constants.ACCEPT_LANGUAGE	=	locale;

		PreferenceManagerUtil preferenceManagerUtil = new PreferenceManagerUtil();
		preferenceManagerUtil.set( "locale" , Constants.ACCEPT_LANGUAGE );

		this.url = url;
		this.paramNameValuePair = paramNameValuePair;
		
		
		this.unEncodedUrl  = unEncodedUrl;

	}

	
	
	public GetConnectionMethod_JavaNet ( String url, List< Header > header, String authenticator, List < NameValuePair > paramNameValuePair,String fromFragment, int parseType,String unEncodedUrl) {

		String locale = PlayupLiveApplication.getInstance().getResources().getConfiguration().locale.getLanguage();

		if ( Constants.ACCEPT_LANGUAGE != null && Constants.ACCEPT_LANGUAGE.trim().length() > 0 && !locale.equalsIgnoreCase(Constants.ACCEPT_LANGUAGE ) ) {

			// remove whats hot and euro data
			DatabaseUtil.getInstance().dropTables();
			new Util().getDataFromServer(false);

		}
		Constants.ACCEPT_LANGUAGE	=	locale;

		PreferenceManagerUtil preferenceManagerUtil = new PreferenceManagerUtil();
		preferenceManagerUtil.set( "locale" , Constants.ACCEPT_LANGUAGE );

		this.url = url;
		this.paramNameValuePair = paramNameValuePair;
		
		
		this.unEncodedUrl  = unEncodedUrl;

	}

	/**
	 * setting the headers 
	 */
	private void setHeaders () {
		
		
		DatabaseUtil dbDatabaseUtil = DatabaseUtil.getInstance();

		String userRegion = new PreferenceManagerUtil().get(Constants.REGION_TOKEN,"");

		urlConnection.addRequestProperty(  Constants.API_VERSION_KEY, Constants.API_VERSION );
		urlConnection.addRequestProperty(  Constants.ACCEPT_LANGUAGE_KEY, Constants.ACCEPT_LANGUAGE  );
		urlConnection.addRequestProperty(  Constants.ACCEPT_ENCODING_KEY, Constants.ACCEPT_ENCODING );
		urlConnection.addRequestProperty(  Constants.CONNECTION_KEY, Constants.CONNECTION );
		
		
		

		String header  = null;
		if(unEncodedUrl != null && unEncodedUrl.trim().length() > 0)
			header = dbDatabaseUtil.getHeader ( unEncodedUrl);
		else
			header = dbDatabaseUtil.getHeader ( url );
	

		if ( header != null && header.trim().length() > 0 ) {
			urlConnection.addRequestProperty(  "Accept", header + ",*/*" );
		}
		
		
		
		
		if ( userRegion != null && userRegion.trim().length() > 0 ) {	
			urlConnection.addRequestProperty(  Constants.REQUEST_GEO_TAG,userRegion);
		}
		

		Crypto mCrypto	=	new Crypto();
	

		
		// Sign the request using the MAC id if MAc credentials are availbale in the DB
		Hashtable<String, String> credentials = dbDatabaseUtil.getCredentials();
			
			String vId = "";
			String vSecret = "";
			
			if(credentials != null && credentials.size() > 0){
				if(credentials.containsKey("vId") && credentials.get("vId") != null )
					vId = credentials.get("vId");
				if(credentials.containsKey("vSecret") && credentials.get("vId") != null )
				vSecret = credentials.get("vSecret");
				
			}


			 

				
				if(vId != null && vSecret != null && vId.trim().length() > 0 && vSecret.trim().length() > 0){
					
						
					
					MacCreation mac = new MacCreation(vId,vSecret,url,Constants.GET,false);
					String authorizationHeader 	=	mac.getMacTokens();	
					
//					Add the MAC id as AUTHORIZATION_KEY
					if(authorizationHeader != null && authorizationHeader.trim().length() > 0)


						urlConnection.addRequestProperty(  Constants.AUTHORIZATION_KEY,authorizationHeader);
				
				}
				
				
				
				
	
			String apiKeySignature = mCrypto.createSignature(Constants.PLAYUP_API_KEY ,  url.toString());
			String apiKeyHeader = Constants.PLAYUP_API_KEY+ " " + apiKeySignature;
			//Add the API Signature as PLAYUP_API_KEY_KEY
			urlConnection.addRequestProperty(Constants.PLAYUP_API_KEY_KEY,apiKeyHeader);
 
		

			
		// checking for eTag already exists 
			
			String eTag = null;
			
			if(unEncodedUrl != null && unEncodedUrl.trim().length() > 0) 
				 eTag = dbDatabaseUtil.getETag( this.unEncodedUrl );
			else
				 eTag = dbDatabaseUtil.getETag( this.url );
		if ( eTag != null && !this.url.equalsIgnoreCase( Keys.ROOT_URL ) ) {
			urlConnection.addRequestProperty( Constants.IF_NONE_MATCH_KEY , eTag );
		}

		mCrypto = null;
		dbDatabaseUtil = null;
		
		
		
	}

	/**
	 * adding params at the end of the url  
	 */
	private String formUrlRequest () {
		String urlStr = "";
		if ( paramNameValuePair != null && paramNameValuePair.size() > 0 ) {
			urlStr = "?";
			int size = paramNameValuePair.size();
			for ( int i = 0; i < size; i++ ) {
				NameValuePair nameValPair = paramNameValuePair.get( i );
				urlStr += nameValPair.getName() + "=" + nameValPair.getValue()  + "&";
				nameValPair = null;
			}
			urlStr = urlStr.substring( 0 , urlStr.length() - 1);
			paramNameValuePair = null;
		}
		return urlStr;
	}


	/**
	 *  getting the headers, etags , cache time and response data 
	 */
	private InputStream processData ( Map < String, List < String > > map, InputStream in ) {

		DatabaseUtil dbDatabaseUtil =  DatabaseUtil.getInstance();
		InputStream inputStream = in;


		if ( map != null && map.containsKey( "content-encoding" ) ) {

			List<String> contentEncoding = map.get( "content-encoding" );

			if (contentEncoding != null && contentEncoding.get(0).equalsIgnoreCase("gzip")) {
				try {
					if ( inputStream != null ) {
						inputStream  = new GZIPInputStream  ( in );
					}

				} catch ( Exception e ) {

				}
			}	
		}

		String eTag=null;
		// setting the eTags 
		if ( map != null && map.containsKey( "etag")) {
			eTag = map.get( "etag" ).get(0);
			if ( eTag.contains("\"")) {
				eTag.replace( "\"", "\\\"");
			}
		}

		
		if(unEncodedUrl != null && unEncodedUrl.trim().length() > 0)
			dbDatabaseUtil.setETag( this.unEncodedUrl, eTag );
		else
			dbDatabaseUtil.setETag( this.url, eTag );

		// setting the user tokens
		if ( map != null && map.containsKey( Constants.AUTHORIZATION_TOKEN_KEY ) ) {
			
			

			String authorization_token = map.get( Constants.AUTHORIZATION_TOKEN_KEY ).get(0);
			
			
			

			if(!Constants.isLoggedIn) {
				
				dbDatabaseUtil.setUserToken(authorization_token, true );
			}
		}

		if ( map != null && map.containsKey( Constants.CONTENT_LOCATION ) ) {

			/**
			 * Need to change this logic. Need to store it on DB
			 */
			
			if(unEncodedUrl != null && unEncodedUrl.trim().length() > 0)
				dbDatabaseUtil.setPollingUrl ( unEncodedUrl, map.get( Constants.CONTENT_LOCATION ).get(0) );
			else
				dbDatabaseUtil.setPollingUrl ( url, map.get( Constants.CONTENT_LOCATION ).get(0) );
		}


		// setting the user geo location
		if ( map != null && map.containsKey( Constants.RESPONSE_GEO_TAG ) ) {
			if(  map.get( Constants.RESPONSE_GEO_TAG ).size()>0 ) {
				String region_token = map.get( Constants.RESPONSE_GEO_TAG ).get(0);

				if( region_token!=null && region_token.length()>0){

					PreferenceManagerUtil preferenceManagerUtil = new PreferenceManagerUtil();
					if( !preferenceManagerUtil.get(Constants.IS_GEO_TAG_SET, false) || 
							(preferenceManagerUtil.get( Constants.REGION_TOKEN,null) != null && !preferenceManagerUtil.get( Constants.REGION_TOKEN,null).equalsIgnoreCase(region_token) )){
						preferenceManagerUtil.set( Constants.IS_GEO_TAG_SET,true);

						preferenceManagerUtil.set( Constants.REGION_TOKEN,region_token);

						//						dbDatabaseUtil.setPrimaryUserRegion(region_token, false );

						if(PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName() != null && 
								PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName().equalsIgnoreCase("DefaultFragment")){
							Hashtable<String, List<String>> hotItemData =DatabaseUtil.getInstance().getStartingScreenData();
							if(hotItemData != null && hotItemData.get("resource_url") != null && 
									hotItemData.get("resource_url").size() > 0){
								
								
								String url = hotItemData.get("resource_href").get(0);
								
								if(url != null && url.trim().length() > 0){
									new Util().getWelComeData ( hotItemData.get("resource_href").get(0),true, hotItemData.get("resource_href").get(0), null,  true );
									
								}else{
									url = hotItemData.get("resource_url").get(0);
									new Util().getWelComeData ( hotItemData.get("resource_url").get(0),false, hotItemData.get("resource_url").get(0), null,  true );
								}

								
							}
						} else {
							DatabaseUtil.getInstance().dropTables();
						}
					}
					preferenceManagerUtil = null;
				}
			}
		}

		int cacheTime = -1;
		if ( map != null && map.containsKey( Constants.CACHE_CONTROL_KEY ) ) {

			try {
				List < String > cacheControl = map.get( Constants.CACHE_CONTROL_KEY );

				String serverCacheStr	=	cacheControl.get( 0 );

				String split[]	=	serverCacheStr.split(",");
				int i 	=	0;
				while(i < split.length){
					if(split[i].contains(Constants.CACHE_MAX_AGE)){

						String splits[]	=	split[i].split("=");
						if(splits.length==2){

							cacheTime = Integer.parseInt(splits[1]);
							
							if(unEncodedUrl != null && unEncodedUrl.trim().length() > 0)
								dbDatabaseUtil.setETag( this.unEncodedUrl, cacheTime );
							else
								dbDatabaseUtil.setETag( this.url, cacheTime );

						}

						splits = null;
					}
					i++;
				}
				split = null;
				serverCacheStr = null;

			} catch ( Exception e) {

				//Not sure about what to do When Cache time is not present at all.
				cacheTime	=	-1;
			}

		}

//		if( cacheTime > 0 ) {
//			if( parseType!=-1 || refreshData!=null )
//				PlayupLiveApplication.getPollManager().pollUrl( this.url, fromFragment, refreshData, parseType );
//		}
//			

		if ( map != null && map.containsKey( Constants.ISNEW_CONTENT_AVAIL_KEY ) ) {

			isContentChanged		=	Integer.parseInt(map.get( Constants.ISNEW_CONTENT_AVAIL_KEY ).get(0));

		}



		dbDatabaseUtil = null;
		return inputStream;
	}


	public Object send ( int returnType ) {

		Proxy proxy = null;
		String proxyAddress = null;
		String proxyString = null;
		URL url = null;
		try {

			url = new URL(this.url + formUrlRequest () );
			
			/*
			 * Check for proxy Settings
			 */
			proxyString = Settings.Secure.getString(PlayupLiveApplication.getInstance().getContentResolver(),Settings.Secure.HTTP_PROXY);

			if ( proxyString != null ) {      
				try {
					proxyAddress = proxyString.split ( ":" ) [ 0 ];
					int proxyPort = Integer.parseInt ( proxyString.split ( ":" ) [ 1 ] );

					proxy = new Proxy ( Proxy.Type.HTTP, new InetSocketAddress ( proxyAddress, proxyPort ) );
					urlConnection = ( HttpURLConnection ) url.openConnection ( proxy );

				} catch ( Exception e ) {
					if ( checkForProxy ( ) ) {//Check for Playup Proxy
						try {
							proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, Integer.parseInt(port)));
							urlConnection = (HttpURLConnection) url.openConnection(proxy);

						} catch ( Exception e1 ) {
							urlConnection = (HttpURLConnection) url.openConnection();
						}
					}
				}
			} else if ( checkForProxy ( ) ) {//Check for Playup Proxy
				try {
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, Integer.parseInt(port)));
					urlConnection = (HttpURLConnection) url.openConnection(proxy);
				} catch ( Exception e ) {
					urlConnection = (HttpURLConnection) url.openConnection();
				}
			} 

			if ( urlConnection == null ) {
				urlConnection = (HttpURLConnection) url.openConnection();//Without Proxy
			}
			
			
			setHeaders();
			
			InputStream inputStream	= null;

			
			
			

			try {
				STATUS_CODE	=	urlConnection.getResponseCode();
				} catch (Exception e) {
				if (e.getMessage().contains("authentication challenge")) {
				STATUS_CODE = HttpURLConnection.HTTP_UNAUTHORIZED;

				} 
				}
			
			

			
			//Check for the status , if 401 server has returned MAC credentials 


				
			if(STATUS_CODE == 401 || STATUS_CODE == 403){
				
				
				inputStream = urlConnection.getErrorStream();
				
			}else{
				inputStream	=	urlConnection.getInputStream();
			}
			
			

			inputStream = processData( urlConnection.getHeaderFields(), inputStream );

			

			switch ( returnType ) {

			case Constants.TYPE_BYTE : 		return	null;
			case Constants.TYPE_STREAM:		return	null;
			case Constants.TYPE_STRINGBUFFER : 	





				if ( inputStream != null) {
					/*BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream),8 * 1024);
					StringBuilder sb = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {

						sb.append(line + "\n");
						}*/

					StringBuilder sb = new StringBuilder();
					byte[] b = new byte[8192];
					String test;
					for (int i; (i = inputStream.read(b)) != -1;) {
						test	=new String(b, 0, i);	
						sb.append(test);
					}



					inputStream.close();
					inputStream = null;

					/*reader.close();
					reader = null;
//					 */

				
//					 





					Logs.show ( url +"================================================ " + sb);



					
					return	new StringBuffer ( sb );

					
					

				}
			}

			return null;
		} catch ( UnknownHostException e ) {
			STATUS_CODE = 001;
			//Logs.show( e );
		} catch (HttpException e) {
			STATUS_CODE = 001;
			//Logs.show( e );

		} catch (IOException e) {
			//Logs.show( e );

			String topbarFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();

			if ( DatabaseUtil.getInstance().isUserAnnonymous() && e != null && e.getMessage() != null && e.getMessage().equalsIgnoreCase( "Received authentication challenge is null" ) && topbarFragmentName !=  null && !topbarFragmentName.equalsIgnoreCase( "LoginWebViewFragment" ) ) {
				if ( PlayUpActivity.handler != null ) {
					PlayUpActivity.handler.post( new Runnable () {

						@Override
						public void run() {
							try {
								new Util().logout();
							} catch (Exception e) {
							//	Logs.show ( e );
							}
						}

					});
				}

			}


			 STATUS_CODE = 001;


		} catch ( Exception e ) {
			//Logs.show( e );
		}catch (Error e) {
			//Logs.show( e );
		}

		finally {

			try {
				// release the memory
				if(urlConnection!=null) {

					
					urlConnection.disconnect();
					urlConnection = null;
				}

				proxy = null;
				proxyAddress = null;
				proxyString = null;
				url = null;
			}catch (Exception e) {
				//Logs.show( e );
			}
		}

		return null;
	}


	/**
	 * checking for proxy settings  
	 */
	public boolean checkForProxy(){

		PreferenceManagerUtil preferenceManagerUtil = new PreferenceManagerUtil();
		if ( preferenceManagerUtil != null ) {
			host =	preferenceManagerUtil.get("Host", "");
			port =	preferenceManagerUtil.get("Port", "");
			preferenceManagerUtil = null;
			if ( host.length() == 0 ) {

				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	public int getStateCode(){

		return STATUS_CODE;
	}



	public int isNewContentAvaialable(){

		return isContentChanged;
	}

}


