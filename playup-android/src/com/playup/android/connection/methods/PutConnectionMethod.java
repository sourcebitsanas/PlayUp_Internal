package com.playup.android.connection.methods;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;



import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.MacCreation;

public class PutConnectionMethod {


	private String url;
	private HttpURLConnection urlConnection;
	private int STATUS_CODE	=	-1;
	private String requestData;
	private String host;
	private String port;
	private String token;
	private String unEncodedUrl = null;

	public PutConnectionMethod ( String url, String requestData ,String unEncodedUrl ) {

		this.url = url;
		this.requestData = requestData;
		this.unEncodedUrl  = unEncodedUrl;


	}
	
	public PutConnectionMethod ( String url, String requestData) {

		this.url = url;
		this.requestData = requestData;
		
		

	}


	/**
	 * setting the headers 
	 */
	private void setHeaders () {


		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		//if(token==null)
		 

		
//		Logs.show("url>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + Constants.API_VERSION_KEY +","+ Constants.API_VERSION  );
//		Logs.show("url>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + Constants.ACCEPT_LANGUAGE_KEY +","+ Constants.ACCEPT_LANGUAGE  );
//		Logs.show("url>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + Constants.ACCEPT_ENCODING_KEY +","+ Constants.ACCEPT_ENCODING  );
//		Logs.show("url>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + Constants.CONNECTION_KEY +","+ Constants.CONNECTION  );
//		Logs.show("url>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Accept,"+ dbUtil.getHeader ( url )  );

		
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
			MacCreation mac = new MacCreation(vId,vSecret,url,Constants.PUT,false);
			String authorizationHeader 	=	mac.getMacTokens();	
			
			//Add the MAC id as AUTHORIZATION_KEY
			if(authorizationHeader != null && authorizationHeader.trim().length() > 0)
				urlConnection.addRequestProperty(  Constants.AUTHORIZATION_KEY,authorizationHeader);
		}
		
		token = null;
		
		String apiKeySignature = mCrypto.createSignature(Constants.PLAYUP_API_KEY ,  url.toString());
        String apiKeyHeader = Constants.PLAYUP_API_KEY+ " " + apiKeySignature;
        
//        Logs.show("url>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + Constants.PLAYUP_API_KEY_KEY +","+ apiKeyHeader  );
		
        urlConnection.addRequestProperty(Constants.PLAYUP_API_KEY_KEY,apiKeyHeader);
        
        mCrypto = null;
        
        dbUtil = null;
	}



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
					//Logs.show(e);
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

		URL url  = null;
		String proxyString = null;
		String proxyAddress = null;
		Proxy proxy  = null;
		
		try{
			//This dummy field is to solve the 411 error thrown from server.

			url = new URL(this.url);

			/*
			 * 
			 * Check for proxy Settings
			 */

			//proxyString = Settings.Secure.getString(PlayupLiveApplication.getInstance().getContentResolver(),Settings.Secure.HTTP_PROXY);


			if ( proxyString != null ) {      
				try {

					proxyAddress = proxyString.split(":")[0];
					int proxyPort = Integer.parseInt(proxyString.split(":")[1]);
					//HttpHost proxy = new HttpHost(proxyAddress,proxyPort);
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, proxyPort));
					urlConnection = (HttpURLConnection) url.openConnection(proxy);

				} catch ( Exception e ) {
					//Logs.show(e);

					if ( checkForProxy ( ) ) {//Check for Playup Proxy
						try {

							proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, Integer.parseInt(port)));
							urlConnection = (HttpURLConnection) url.openConnection(proxy);

						} catch ( Exception e1 ) {
						//	Logs.show(e1);

							urlConnection = (HttpURLConnection) url.openConnection();
						}
					}
				}

			} else if ( checkForProxy ( ) ) {//Check for Playup Proxy
				try {
					
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, Integer.parseInt(port)));
					urlConnection = (HttpURLConnection) url.openConnection(proxy);

				} catch ( Exception e ) {
					//Logs.show(e);

					urlConnection = (HttpURLConnection) url.openConnection();
				}
			} 
			
			if ( urlConnection == null ) {
				urlConnection = (HttpURLConnection) url.openConnection();//Without Proxy
			}

			urlConnection.setDoOutput(true);

			setHeaders();

			urlConnection.setRequestMethod("PUT");
			if ( requestData == null ) {
				requestData = " Resource content ";
			}
			urlConnection.setRequestProperty("Content-Length",""+requestData.length());
			urlConnection.getOutputStream().write(requestData.getBytes() );

			InputStream inputStream = urlConnection.getInputStream();

			try {
				STATUS_CODE	=	urlConnection.getResponseCode();
				} catch (Exception e) {
				if (e.getMessage().contains("authentication challenge")) {
				STATUS_CODE = HttpURLConnection.HTTP_UNAUTHORIZED;

				} 
				}
			inputStream = processData( urlConnection.getHeaderFields(), inputStream );

			

			if ( inputStream != null)  {
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream),8 * 1024);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}

				reader.close();
				reader = null;
				inputStream .close();
				inputStream = null;
				return new StringBuffer( sb.toString() );
			}

		} catch ( Exception e ) {

			//Logs.show ( e );

		} finally {
			try{
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

				//Logs.show ( e );

			}
		}
		return null;
	}


	/**
	 * checknig for proxy 
	 */
	public boolean checkForProxy(){

		/*
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
		}*/
		return false;
	}

	// getting the status code
	public int getStateCode(){
		return STATUS_CODE;
	}
}
