package com.playup.android.connection.methods;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import android.provider.Settings;
import android.util.Log;

import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.MacCreation;
import com.playup.android.util.PreferenceManagerUtil;

public class DeleteConnectionMethod {


	private String url;

	private int STATUS_CODE	=	-1;
	private HttpURLConnection urlConnection; 
	private String host;
	private String port;

	private String unEncodedUrl = null;


	public DeleteConnectionMethod ( String url,String unEncodedUrl ) {

		this.url = url;
		this.unEncodedUrl  = unEncodedUrl;

	}

	/**
	 * setting the headers 
	 */
	private void setHeaders () {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		
		
		urlConnection.addRequestProperty(  Constants.API_VERSION_KEY, Constants.API_VERSION );
		urlConnection.addRequestProperty(  Constants.ACCEPT_LANGUAGE_KEY, Constants.ACCEPT_LANGUAGE );
		//urlConnection.addRequestProperty(  Constants.PLAYUP_API_KEY_KEY, Constants.PLAYUP_API_KEY );
		urlConnection.addRequestProperty(  Constants.ACCEPT_ENCODING_KEY, Constants.ACCEPT_ENCODING );
		urlConnection.addRequestProperty(  Constants.CONNECTION_KEY, Constants.CONNECTION );

		if(unEncodedUrl != null && unEncodedUrl.trim().length() > 0)
			urlConnection.addRequestProperty(  "Accept", dbUtil.getHeader ( unEncodedUrl )  );
		else
			urlConnection.addRequestProperty(  "Accept", dbUtil.getHeader ( url )  );

		
		
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
			MacCreation mac = new MacCreation(vId,vSecret,url,Constants.DELETE,false);
			String authorizationHeader 	=	mac.getMacTokens();	
			
			//Add the MAC id as AUTHORIZATION_KEY
			if(authorizationHeader != null && authorizationHeader.trim().length() > 0)
				urlConnection.addRequestProperty(  Constants.AUTHORIZATION_KEY,authorizationHeader);
		}

		
		String apiKeySignature = mCrypto.createSignature(Constants.PLAYUP_API_KEY ,  url.toString());
        String apiKeyHeader = Constants.PLAYUP_API_KEY+ " " + apiKeySignature;
        urlConnection.addRequestProperty(Constants.PLAYUP_API_KEY_KEY,apiKeyHeader);

        
        dbUtil = null;
        
	}

	/**
	 * 
	 * @param map 
	 * @param in
	 * @return
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

		URL url = null;
		String proxyString = null;
		String proxyAddress = null;
		Proxy proxy = null;
		
		try{
			//This dummy field is to solve the 411 error thrown from server.
			String dummy 	=	"Resource content";

			url = new URL(this.url);
			/*
			 * 
			 * Check for proxy Settings
			 */

			proxyString = Settings.Secure.getString(PlayupLiveApplication.getInstance().getContentResolver(),Settings.Secure.HTTP_PROXY);

			if (proxyString != null) {      
				try {
					proxyAddress = proxyString.split(":")[0];
					int proxyPort = Integer.parseInt(proxyString.split(":")[1]);
					//HttpHost proxy = new HttpHost(proxyAddress,proxyPort);
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, proxyPort));
					urlConnection = (HttpURLConnection) url.openConnection(proxy);

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
				} catch (Exception e) {
					urlConnection = (HttpURLConnection) url.openConnection();
				}
			}
			
			if ( urlConnection == null ) {
				urlConnection = (HttpURLConnection) url.openConnection();//Without Proxy
			}
			//urlConnection.setDoOutput(true);
			
			urlConnection.setRequestMethod("DELETE");
			setHeaders();

			InputStream inputStream	=	urlConnection.getInputStream();
			inputStream = processData( urlConnection.getHeaderFields(), inputStream );

			STATUS_CODE	=	urlConnection.getResponseCode();
			
			if ( inputStream != null ) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				reader.close();
				reader = null;
				inputStream.close();
				inputStream = null;
				
				return	new StringBuffer ( sb.toString() );
			}
		} catch ( Exception e ) {
		} finally {
			try{
				// release the memory
				if(urlConnection!=null) {
					urlConnection.disconnect();
					urlConnection = null;
				}

				url = null;
				proxyString = null;
				proxyAddress = null;
				proxy = null;
				
			} catch (Exception e ) {
			}
		}
		return null;
	}

	/**
	 * checking for proxy 
	 */
	public boolean checkForProxy ( ) {

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

}
