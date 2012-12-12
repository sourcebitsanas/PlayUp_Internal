package com.playup.android.exception;

/**
 * If http requests are repeated this will be thrown.
  **/
public class RequestRepeatException extends Exception {
	
	
	public RequestRepeatException ( String msg ) {
		super ( msg );
	}
}
