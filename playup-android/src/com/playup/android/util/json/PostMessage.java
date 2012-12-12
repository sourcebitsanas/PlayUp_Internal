package com.playup.android.util.json;

import org.json.JSONException;
import org.json.JSONObject;

import com.playup.android.util.Constants;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

public class PostMessage {

	private final String STATUS		               =  "status";// "status": 401, When Success 202
	private final String ERROR	    	           =  "error";	//"error": "Please Sign In", 
	private final String SOLUTION				   =  "solution";//"solution": "http://uat.reboot.playupdev.com/fanbase/providers.json"
	private final String MESSAGE    	           =  "message";//  "message": "OK"
	
	private int STATUS_CODE							=	-1;
	public PostMessage ( String str ) {
		
		if ( str != null && str.trim().length() > 0 ) {
			parseData ( str );
		}
	}
	
	private void parseData ( String str ) {
		JSONObject jsonObj = null;
		try {

			/*
			 * 
			 * Parsing Token Content
			 */
			jsonObj = new JSONObject( str );
			// getting the ids
			String status 			= jsonObj.optString( STATUS );
			if(status!=null&&Integer.parseInt(status) == Constants.SUCCESS_POST){
				
				STATUS_CODE	=	Integer.parseInt(status);
			}else if(status!=null&&Integer.parseInt(status)!=Constants.SUCCESS_POST)
			
			{// Failure
				
				STATUS_CODE	=	Integer.parseInt(status);
			}
		
			
		
			
		} catch (JSONException e) {
			Logs.show(e);
		} finally {
			new Util().releaseMemory(jsonObj);
		}
	}
	
	
	public int getStatusCode(){
		
		return STATUS_CODE;
	}
	
}
