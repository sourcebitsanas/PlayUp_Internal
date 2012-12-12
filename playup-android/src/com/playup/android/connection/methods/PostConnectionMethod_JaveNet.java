package com.playup.android.connection.methods;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.httpclient.HttpException;


import android.provider.Settings;
import android.util.Log;


import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.MacCreation;
import com.playup.android.util.PreferenceManagerUtil;

public class PostConnectionMethod_JaveNet {

	private String url;

	private String requestData;
	private int STATUS_CODE = -1;

	

	private String host;
	private String port;

	private String setContentType = "";

	private boolean isHttps = false;

	private String unEncodedUrl = null;




	public PostConnectionMethod_JaveNet(String url, String requestData,String setContentType,boolean isHttps,String unEncodedUrl ) {

		this.url = url;
		this.requestData = requestData;
		this.setContentType = setContentType;
		this.isHttps  = isHttps;
		this.unEncodedUrl  = unEncodedUrl;
		
	
	}


	/**
	 * setting the headers 
	 */
	private void setHeaders (URLConnection urlConnection) {

			Log.w("111", "SetHeaders----PostConnectionMethod-----");
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		
		urlConnection.addRequestProperty(  Constants.API_VERSION_KEY, Constants.API_VERSION );
		urlConnection.addRequestProperty(  Constants.ACCEPT_LANGUAGE_KEY, Constants.ACCEPT_LANGUAGE );
		//urlConnection.addRequestProperty(  Constants.PLAYUP_API_KEY_KEY, Constants.PLAYUP_API_KEY );
		urlConnection.addRequestProperty(  Constants.ACCEPT_ENCODING_KEY, Constants.ACCEPT_ENCODING );
		urlConnection.addRequestProperty(  Constants.CONNECTION_KEY, Constants.CONNECTION );
		
			
		String header  = null;
		
		
		if(unEncodedUrl != null && unEncodedUrl.trim().length() > 0)
			header = dbUtil.getHeader ( unEncodedUrl);
		else
			header = dbUtil.getHeader ( url );
		
		


		if ( header != null && header.trim().length() > 0 ) {

			urlConnection.addRequestProperty(  "Accept", header + ",*/*" );
		} 
			
		


		

		Crypto mCrypto	=	new Crypto();


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
			MacCreation mac = new MacCreation(vId,vSecret,url,Constants.POST, isHttps);
			String authorizationHeader 	=	mac.getMacTokens();	
			
			//Add the MAC id as AUTHORIZATION_KEY
			if(authorizationHeader != null && authorizationHeader.trim().length() > 0)
				urlConnection.addRequestProperty(  Constants.AUTHORIZATION_KEY,authorizationHeader);



		}

		String apiKeySignature = mCrypto.createSignature(Constants.PLAYUP_API_KEY ,  url.toString());

		String apiKeyHeader = Constants.PLAYUP_API_KEY+ " " + apiKeySignature;
		urlConnection.addRequestProperty(Constants.PLAYUP_API_KEY_KEY,apiKeyHeader);

		
		mCrypto = null;
		
		dbUtil = null;

		// checking for eTag already exists 
		/*		String eTag = dbDatabaseUtil.getETag( this.url );
		if ( eTag != null && !this.url.equalsIgnoreCase( Constants.BASE_URL ) ) {
			urlConnection.addRequestProperty( Constants.IF_NONE_MATCH_KEY , eTag );
		}
		 */


	}



	private InputStream processData ( Map < String, List < String > > map, InputStream in ) {




		DatabaseUtil dbDatabaseUtil =  DatabaseUtil.getInstance();

		InputStream inputStream = in;

		if ( map != null && map.containsKey( "content-encoding" ) ) {

			List<String> contentEncoding = map.get( "content-encoding" );

			if (contentEncoding != null && contentEncoding.get(0).equalsIgnoreCase("gzip")) {
				try {	
					if ( inputStream != null) {
						inputStream  = new GZIPInputStream  ( in );
					}

				} catch ( Exception e ) {
				}
			}	
		}



		// setting the eTags 
		if ( map != null && map.containsKey( "etag")) {

			String eTag = map.get( "etag" ).get(0);

			if ( eTag.contains("\"")) {

				eTag.replace( "\"", "\\\"");
			}
			
			if(unEncodedUrl != null && unEncodedUrl.trim().length() > 0)
				dbDatabaseUtil.setETag( this.unEncodedUrl, eTag );
			else
				dbDatabaseUtil.setETag( this.url, eTag );
		}


		// setting the user tokens
		if ( map != null && map.containsKey( Constants.AUTHORIZATION_TOKEN_KEY ) ) {

			String authorization_token = map.get( Constants.AUTHORIZATION_TOKEN_KEY ).get(0);
			dbDatabaseUtil.setUserToken(authorization_token, true );

		}





		dbDatabaseUtil = null;
		return inputStream;
	}

	public Object send() {

		
		try {



			if(isHttps)
				return callSecuredUrl();
			
			else
				return callOrdinaryUrl();
			
			
		}catch(Exception e){
		
			Logs.show(e);
		}
			
		return null;
	}
		
		private Object callSecuredUrl(){
			
			HttpsURLConnection urlConnection = null;
			
			URL url  = null;
			String proxyString  = null;
			String proxyAddress = null;
			Proxy proxy = null;
			try{
			url = new URL ( this.url );
		
			/*
			 * 
			 * Check for proxy Settings
			 */

			proxyString = Settings.Secure.getString(PlayupLiveApplication.getInstance().getContentResolver(),Settings.Secure.HTTP_PROXY);

			if (proxyString != null)

			{      
				try{
					proxyAddress = proxyString.split(":")[0];
					int proxyPort = Integer.parseInt(proxyString.split(":")[1]);

					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, proxyPort));
					urlConnection =  (HttpsURLConnection)url.openConnection(proxy);
				} catch ( Exception e ) {
					if ( checkForProxy ( ) ) {//Check for Playup Proxy
						try {
							proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, Integer.parseInt(port)));
							urlConnection = (HttpsURLConnection) url.openConnection(proxy);

						} catch ( Exception e1 ) {
							urlConnection = (HttpsURLConnection) url.openConnection();
						}
					}
				}
			} else if ( checkForProxy ( ) ) {//Check for Playup Proxy
				try {
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, Integer.parseInt(port)));
					urlConnection = (HttpsURLConnection) url.openConnection(proxy);

				} catch ( Exception e ) {
					urlConnection = (HttpsURLConnection) url.openConnection();
				}
			}
			if ( urlConnection == null ) {
				urlConnection = (HttpsURLConnection) url.openConnection();//Without Proxy
			}

			
			HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod("POST");

			setHeaders(urlConnection);
			if(setContentType != null && setContentType.trim().length() > 0){
				
				urlConnection.setRequestProperty("Content-Type",""+setContentType);
			}
			
			if(requestData != null && requestData.trim().length() > 0){
			urlConnection.setRequestProperty("Content-Length",""+requestData.toString().getBytes().length);
			urlConnection.getOutputStream().write(requestData.toString().getBytes());
			}

			InputStream inputStream = null;
			STATUS_CODE = urlConnection.getResponseCode();
			
			if(STATUS_CODE == 401){
				
				Log.e("123","inside 401 >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+url);
				
				inputStream = urlConnection.getErrorStream();
				
				
				
			}else{
				
				inputStream = urlConnection.getInputStream();
			}

			
			inputStream = processData( urlConnection.getHeaderFields(), inputStream );

			
			
			


			if (  inputStream != null ) {
				BufferedReader reader = new BufferedReader(new InputStreamReader( inputStream ),8 * 1024 );
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ( ( line = reader.readLine())  != null ) {
					sb.append(line + "\n");
				}
				reader.close();
				reader = null;
				inputStream.close();
				inputStream = null;
				
				Logs.show(url + "==================================================================================== "+sb);
				
				return new StringBuffer(sb.toString());
			}

		} catch ( UnknownHostException e ) {
			Logs.show( e );


		} catch (HttpException e) {
			Logs.show( e );

		} catch (IOException e) {
			Logs.show( e );
		} catch ( Exception e ) {
			Logs.show( e );

		} finally {
			try{
				// release the memory
				if(urlConnection!=null) {
					
					STATUS_CODE = urlConnection.getResponseCode();
				urlConnection.disconnect();
					urlConnection = null;
				}


				proxy = null;
				proxyAddress = null;
				proxyString = null;
				url = null;
			}catch (Exception e) {
				Logs.show( e );

			}
		}
		return null;
			
		}
		
		
		private Object callOrdinaryUrl(){
			
			HttpURLConnection urlConnection = null;
			
			URL url  = null;
			String proxyString  = null;
			String proxyAddress = null;
			Proxy proxy = null;
			try{
			url = new URL ( this.url );
		
			/*
			 * 
			 * Check for proxy Settings
			 */

			proxyString = Settings.Secure.getString(PlayupLiveApplication.getInstance().getContentResolver(),Settings.Secure.HTTP_PROXY);

			if (proxyString != null)

			{      
				try{
					proxyAddress = proxyString.split(":")[0];
					int proxyPort = Integer.parseInt(proxyString.split(":")[1]);

					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, proxyPort));
					urlConnection =  (HttpURLConnection)url.openConnection(proxy);
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

			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod("POST");

			setHeaders(urlConnection);
			if(setContentType != null && setContentType.trim().length() > 0){
				
				urlConnection.setRequestProperty("Content-Type",""+setContentType);
			}
			
			
			urlConnection.setRequestProperty("Content-Length",""+requestData.toString().getBytes().length);
			urlConnection.getOutputStream().write(requestData.toString().getBytes());

			
			InputStream inputStream = null;
			
			
			STATUS_CODE = urlConnection.getResponseCode();
			
			
			
			
			
			if(STATUS_CODE == 401){
				
				inputStream = urlConnection.getErrorStream();
				
				
				
			}else{
				
				inputStream = urlConnection.getInputStream();
			}

			
			inputStream = processData( urlConnection.getHeaderFields(), inputStream );

			


			if (  inputStream != null ) {
				BufferedReader reader = new BufferedReader(new InputStreamReader( inputStream ),8 * 1024 );
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ( ( line = reader.readLine())  != null ) {
					sb.append(line + "\n");
				}
				reader.close();
				reader = null;
				inputStream.close();
				inputStream = null;
				
				
				
				return new StringBuffer(sb.toString());
			}

		} catch ( UnknownHostException e ) {
			Logs.show( e );


		} catch (HttpException e) {
			Logs.show( e );

		} catch (IOException e) {
			Logs.show( e );
		} catch ( Exception e ) {
			Logs.show( e );

		} finally {
			try{
				// release the memory
				if(urlConnection!=null) {
					
					STATUS_CODE = urlConnection.getResponseCode();
				urlConnection.disconnect();
					urlConnection = null;
				}


				proxy = null;
				proxyAddress = null;
				proxyString = null;
				url = null;
			}catch (Exception e) {
				Logs.show( e );

			}
		}
		return null;
	}
	


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

	public int getStateCode() {
		return STATUS_CODE;
	}

}
