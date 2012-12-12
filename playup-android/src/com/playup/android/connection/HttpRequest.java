/**
 * HttpRequest


 * For handling all the fetching of data from net with proper Authentication.
 */



package com.playup.android.connection;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.NameValuePair;

import android.os.Bundle;

import com.playup.android.R;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.connection.methods.DeleteConnectionMethod;
import com.playup.android.connection.methods.GetConnectionMethod_JavaNet;
import com.playup.android.connection.methods.PostConnectionMethod_JaveNet;
import com.playup.android.connection.methods.PutConnectionMethod;
import com.playup.android.exception.RequestRepeatException;
import com.playup.android.util.Constants;
import com.playup.android.util.Util;



/**
 * @author vikky
 *
 */




public class HttpRequest  {
	/**
	 * Url to fetch data from.
	 */
	private String url;

	/**
	 * the namer value pair for the headers to be added in the request. 
	 */
	private List < NameValuePair > headerNameValuePair;


	/**
	 * 
	 * Request Data
	 */
	private String requestData;

	/**
	 * for authentication purpose.
	 */
	private String authenticator;


	/**
	 * status  code of a request   
	 */
	private int statusCode = -1;


	/**
	 * to differentiate between the type of method. 
	 */
	private int METHOD_TYPE = Constants.POST_METHOD;

	/**
	 *  For adding the name value pair in header, post, get or delete method.
	 */
	private List < NameValuePair > paramNameValuePair;


	private List < Header > headers;


	private boolean isRepeatRequest;
	
	
	public String token;

	/**
	 * 
	 * To Get the Any Change in Content 
	 */
	public int isAnyChangeInContent	=	Constants.NO_NEW_CONTENT_AVAIL;

	private String setContentType = "";
	
	/**
	 * to differentiate between http and https connections
	 */
	private boolean isHttps = false;
	

	
	private String fromFragment = null;
	private Bundle refreshData = null;
	private int parseType = -1;

	private String unEncodedUrl = null;

	/**
	 * Constructor 
	 */
	private HttpRequest () {
		headers = new ArrayList < Header > ( ); 
		unEncodedUrl = null;
		isHttps = false;
	}

	
	


	/**
	 * HttpRequest constructor.
	 * @param sURL : The url from which data needs to be fetched.
	 * @param requestList : The Name value pair that needs to used
	 * in authentication, post method, get method, delete method
	 * and multipart method.
	 */
	public HttpRequest(final String url, final  List < NameValuePair > headerNameValuePair,  final String authenticator, final int TYPE_METHOD, final List < NameValuePair > paramNameValuePair) {

		this ();
		this.url = url;
		this.headerNameValuePair = headerNameValuePair;
		this.authenticator = authenticator;
		this.METHOD_TYPE = TYPE_METHOD;
		this.paramNameValuePair = paramNameValuePair;
		

		this.parseType = -1;
		this.refreshData = null;
		setIsRepeatRequest();
	}

	public HttpRequest(final String url, final String requestData, final int TYPE_METHOD ) {

		this ();

		this.url = url;
		this.requestData = requestData;
		this.METHOD_TYPE = TYPE_METHOD;
		
		this.parseType = -1;
		this.refreshData = null;
		
		

		setIsRepeatRequest();
	}
	
	public HttpRequest(final String unEncodedUrl, final String url,boolean isHref ,final int TYPE_METHOD ) {

		this ();
		
		this.unEncodedUrl  = unEncodedUrl;

		this.url = url;
		
		this.METHOD_TYPE = TYPE_METHOD;
		
		this.parseType = -1;
		this.refreshData = null;

		setIsRepeatRequest();
	}
	
	public HttpRequest(final String url,final String unEncodedUrl,Boolean isHref, final String requestData, final int TYPE_METHOD,String setContentType ) {

		this ();
		this.url = url;
		this.unEncodedUrl  = unEncodedUrl;
		this.requestData = requestData;
		this.METHOD_TYPE = TYPE_METHOD;
		this.setContentType = setContentType;
		
		this.parseType = -1;
		this.refreshData = null;

		setIsRepeatRequest();
	}

	public HttpRequest(final String url, final String requestData, final int TYPE_METHOD,String setContentType ) {

		this ();
		this.url = url;
		this.requestData = requestData;
		this.METHOD_TYPE = TYPE_METHOD;
		this.setContentType = setContentType;
		
		this.parseType = -1;
		this.refreshData = null;

		setIsRepeatRequest();
	}


	public HttpRequest(final String url, final String requestData, List<Header> header,final int TYPE_METHOD ) {

		this ();
		this.url = url;
		this.requestData = requestData;
		this.METHOD_TYPE = TYPE_METHOD;
		this.headers = header;
		
		this.parseType = -1;
		this.refreshData = null;
		setIsRepeatRequest();
	}



	public HttpRequest(final String url, final int TYPE_METHOD) {

		this ();
		this.url = url;
		this.METHOD_TYPE = TYPE_METHOD;

		this.parseType = -1;
		this.refreshData = null;
		setIsRepeatRequest();
	}
	
	

	
	public HttpRequest(final String url, final int TYPE_METHOD, String fromFragment, Bundle refreshData ) {

		this ();
		this.url = url;
		this.METHOD_TYPE = TYPE_METHOD;
		this.fromFragment = fromFragment;
		this.refreshData = refreshData;
		this.parseType = -1;
		setIsRepeatRequest();
	}

	public HttpRequest(final String url, final int TYPE_METHOD, String fromFragment, int parseType ) {

		this ();
		this.url = url;
		this.METHOD_TYPE = TYPE_METHOD;
		this.fromFragment = fromFragment;
		this.parseType = parseType;
		this.refreshData = null;
		setIsRepeatRequest();
	}


	/**
	 * Constructor related to downloading image ( time being )
	 *  @param url - the ur lof the image  that needs to be downloaded
	 */
	public HttpRequest ( final String url ) {

		this ();
		this.url = url;

		setIsRepeatRequest();

	}

	public HttpRequest(String url, int methodType, boolean isHttps) {
		
		this();
		this.url = url;
		
		this.METHOD_TYPE = methodType;
		
		this.isHttps = isHttps;
	}


	/**
	 *  for downloading the image 
	 *  @throws RequestRepeatException if the request to download the image is already present in the queue or is downloading.
	 *  @return object -- returns inputstream in form of object
	 */
	public Object getImage () throws RequestRepeatException {

		// setting the method 
		this.METHOD_TYPE = Constants.IMAGE_METHOD; 

		// getting the data.
		return send ();
	}


	/**
	 * for fetching the data  
	 * @throws RequestRepeatException if the request to download the image is already present in the queue or is downloading.
	 *  @return object -- returns string 
	 */
	public Object send ()  throws RequestRepeatException {

		if ( Util.isInternetAvailable() ) {
			isRepeatRequest = false;
			// checking again if this request is already duplicated for previous one is completed or not.
			// if not completed it will be "duplicated" request hence isRepeatRequest will be true.
			// else isRepeatRequest will become true.
			if ( isRepeatRequest ) {

				// checks for duplicate request completion
				setIsRepeatRequest();
			}


			if ( ! isRepeatRequest ) {



				// get downloaded data 
				Object obj = sendRequest ();

				// removes this instance from the list to tell the future request that its been completed.
				PlayupLiveApplication.removeHttRequest( this );

				return obj;

			} else {
				throw new RequestRepeatException( "Request already in queue");
			}
		} else {
			if ( this.METHOD_TYPE != Constants.IMAGE_METHOD ) {
				PlayupLiveApplication.showToast ( R.string.no_network );
			}
			return null;
		}

	}


	/**
	 * checks for duplicate request. 
	 */
	private boolean checkForRepeatRequest () {

		// getting all the active queues
		try{
			List < HttpRequest > list  = PlayupLiveApplication.getHttpRequestList ();

			if ( list != null && list.size() > 0 ) {

				int count = list.size();

				for ( int i = 0; i < count ; i++ ) {

					HttpRequest r = list.get( i );

					// comparing the two request
					if ( this.equals( r ) ) {
						list.clear();
						list = null;
						return true;
					}
				}
				list.clear();
			}
			list = null;
		}catch(Exception e){
			return false;
		}

		return false;
	}



	/**
	 * checks for the repeat request and adds in the list if not repeated   
	 */
	private void setIsRepeatRequest () {

		// checking for duplicate request
		if ( ! checkForRepeatRequest () ) {

			isRepeatRequest = false;

			// adding this object in the list
			PlayupLiveApplication.addHttRequest( this );
		} else {

			isRepeatRequest = true;
		}
	}


	/**
	 * setting the headers
	 * @param header -- the name value pair of the header to be set. 
	 */
	private void setHeaders ( ) {

		if ( headers != null ) {
			headers.clear();
		}
		if ( headerNameValuePair != null && headerNameValuePair.size() > 0) {

			Iterator < NameValuePair > mIterator	=	headerNameValuePair.iterator();

			while ( mIterator.hasNext ( ) ) {

				NameValuePair nameValuePair = mIterator.next();
				headers.add( new Header ( nameValuePair.getName(), nameValuePair.getValue() ) ); 
				nameValuePair = null;
			}
			mIterator = null;
		}
	}
	/**
	 * Manages to which method needs to be called for sending  the request. 
	 */
	private Object sendRequest () {

		if ( url != null && url.trim().length() > 0 ) {

			switch ( METHOD_TYPE ) {

			case Constants.GET_METHOD :

				return getMethod( Constants.TYPE_STRINGBUFFER );

			case Constants.DELETE_METHOD :

				return  deleteMethod( authenticator );
			case Constants.HEAD_METHOD :
				// new HeadConnectionMethod( url, headers, authenticator, paramNameValuePair ).send();
				break;
			case Constants.OPTIONS_METHOD : 
				//	new OptionsConnectionMethod( url, headers, authenticator, paramNameValuePair ).send();
				break;
			case Constants.POST_METHOD :

				return postMethod ( authenticator,requestData,setContentType,isHttps,unEncodedUrl);

			case Constants.PUT_METHOD :
				return putMethod( requestData );//Authenticator should be userToken 
			case Constants.PUT_METHOD_REDTICKET :
				return putMethod_ForRedTicket(requestData);//Authenticator should be userToken 
			case Constants.TRACE_METHOD :
				//	new TraceConnectionMethod( url, headers, authenticator, paramNameValuePair ).send();
				break;
			case Constants.IMAGE_METHOD :

				//return postMethod(  Constants.TYPE_STREAM );

			}

		}
		return null;
	}



	private Object postMethod ( final String userToken ,String requestData,String setContentType,boolean isHttps,String unEncodedUrl) {
		setHeaders ();

		PostConnectionMethod_JaveNet postConnectionMethodJaveNet	=	new PostConnectionMethod_JaveNet(url, requestData,setContentType,isHttps,unEncodedUrl );
		Object obj = postConnectionMethodJaveNet.send();
		statusCode = postConnectionMethodJaveNet.getStateCode();
		
		postConnectionMethodJaveNet = null;

		return obj;
	}

	private Object getMethod ( final int type ) {
		GetConnectionMethod_JavaNet getConnectionMethod;
		if( refreshData != null )
			getConnectionMethod =  new GetConnectionMethod_JavaNet( url, headers, authenticator, paramNameValuePair, fromFragment, refreshData,unEncodedUrl);
		else if( parseType != -1 )
			getConnectionMethod =  new GetConnectionMethod_JavaNet( url, headers, authenticator, paramNameValuePair, fromFragment, parseType,unEncodedUrl );
		else
			getConnectionMethod =  new GetConnectionMethod_JavaNet( url, headers, authenticator, paramNameValuePair,unEncodedUrl );

			
		if ( getConnectionMethod != null ) {
			Object obj = getConnectionMethod.send( type );
			statusCode = getConnectionMethod.getStateCode();

			isAnyChangeInContent	=	getConnectionMethod.isNewContentAvaialable();
			getConnectionMethod = null;

			return obj;

		} else {
			return null;
		}
	}

	private Object putMethod (String requestData ) {

		setHeaders ();

		PutConnectionMethod putConnectionMethod =  new PutConnectionMethod( url, requestData,unEncodedUrl );

		Object obj =  putConnectionMethod.send();
		statusCode = putConnectionMethod.getStateCode();
		putConnectionMethod = null;
		return obj;
	}
	
	private Object putMethod_ForRedTicket (String requestData) {

		setHeaders ();

		PutConnectionMethod putConnectionMethod =  new PutConnectionMethod( url, requestData,token );

		Object obj =  putConnectionMethod.send();
		statusCode = putConnectionMethod.getStateCode();
		putConnectionMethod = null;
		return obj;
	}




	private Object deleteMethod ( String userToken ) {
		setHeaders ();

		DeleteConnectionMethod deleteConnectionMethod =  new DeleteConnectionMethod( url,unEncodedUrl );

		Object obj =  deleteConnectionMethod.send();
		statusCode = deleteConnectionMethod.getStateCode();
		deleteConnectionMethod = null;
		return obj;
	}



	// all the methods written down are for checking the duplication 
	/**
	 *  @return string - url 
	 */
	private String getUrl () {
		if ( url == null) {
			return "";
		} else {
			return url;
		}
	}


	/**
	 * @return  List < NameValuePair > -- header name value pair
	 */
	private List < NameValuePair >  getHeaderNameValuePair () {
		return headerNameValuePair;
	}

	/**
	 * @return String - authorizing string 
	 */
	private String getAuthenticator () {
		if ( authenticator == null) {
			return "";
		} else {
			return authenticator;
		}
	}

	/**
	 * @return int - the method type 
	 */
	private int getMethodType () {
		return METHOD_TYPE;
	}

	/**
	 * @return  List < NameValuePair > -- the params that needs to be passed.
	 */
	private List < NameValuePair > getParams () {
		return paramNameValuePair;
	}


	public int getStatusCode () {
		return statusCode;
	}





	/**
	 * comparing the two name value pairs. ( header / param name value pairs)
	 * @return boolean - true if their contents are same. 
	 */
	private boolean compareNameValuePairs ( List < NameValuePair > pair1, List < NameValuePair > pair2 ) {

		if ( pair1 != null && pair2 != null ) {

			if ( pair1 == null || pair2 == null ) {
				return false;
			}
			
			if ( pair1.size() != pair2.size() ) {
				return false;
			}

			Iterator pair1_itr =  pair1.iterator();
			Iterator pair2_itr =  pair2.iterator();

			while( pair1_itr.hasNext() ) {

				NameValuePair pair1_namevaluepair = (NameValuePair) pair1_itr.next();
				NameValuePair pair2_namevaluepair = (NameValuePair) pair2_itr.next();

				if ( pair1_namevaluepair.getName().equals( pair2_namevaluepair.getName() ) && pair1_namevaluepair.getValue().equals( pair2_namevaluepair.getValue() ) )  { 

				} else {
					pair1_itr = null;
					pair2_itr = null;
					return false;
				}
			}
			pair1_itr = null;
			pair2_itr = null;
			return true;

		} else {
			return true;
		}
	}


	/**
	 * comparing the two instances of HttpRequest on the basis of url, header values, authenticator, params and various other factors.
	 * @param HttpRequest -- the HttpRequest that has to be compared with.
	 * @return boolean - if HttpRequest is duplicate else false.
	 */
	public boolean equals( HttpRequest  httpRequestObj ) {

		//null instanceof Object will always return false
		if ( this.getUrl().equals( httpRequestObj.getUrl() ) 
				&&  compareNameValuePairs(this.getHeaderNameValuePair(),  httpRequestObj.getHeaderNameValuePair() ) 
				&&  this.getAuthenticator().equals( httpRequestObj.getAuthenticator() ) 
				&&  this.getMethodType() == httpRequestObj.getMethodType() 
				&&  compareNameValuePairs(getParams(), httpRequestObj.getParams() ) ) {

			return true;

		}

		return false;
	}


}

