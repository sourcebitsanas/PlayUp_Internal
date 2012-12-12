package com.playup.android.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.playup.android.application.PlayupLiveApplication;

public class PreferenceManagerUtil {

	private SharedPreferences preferences;
	
	public PreferenceManagerUtil () {
		preferences = PlayupLiveApplication.getInstance().getSharedPreferences( Keys.PREFERENCE_NAME , Context.MODE_PRIVATE);
	}
	
	
	
	/**
	 * @return  Instance of SharedPreferences.Editor from SharedPreferences.
	 */
	private SharedPreferences.Editor getEditor () {
		
		return (  preferences != null )? preferences.edit() : null;
		
	}
	
	private void commitPreferences (  SharedPreferences.Editor editor ) {
			
		if ( editor != null ) {
			
			editor .commit();
			editor = null;
			
		}
	}
	
	// all float, long, String, integer, boolean setter methods
	public void set ( String key, long value ) {

		SharedPreferences.Editor editor = getEditor();
		
		editor.putLong( key, value );
		
		commitPreferences ( editor ) ;
	
	}
	
	public void set ( String key, boolean value ) {

		SharedPreferences.Editor editor = getEditor();
		
		editor.putBoolean( key, value  );
		
		commitPreferences ( editor ) ;
	
	}
	
	public void set ( String key, float value ) {

		SharedPreferences.Editor editor = getEditor();
		
		editor.putFloat( key, value  );
		
		commitPreferences ( editor ) ;
	
	}
	
	public void set ( String key, int value ) {

		SharedPreferences.Editor editor = getEditor();
		
		editor.putInt ( key, value  );
		
		commitPreferences ( editor ) ;
	
	}
	
	public void set ( String key, String value ) {

		SharedPreferences.Editor editor = getEditor();
		
		editor.putString ( key, value  );
		
		commitPreferences ( editor ) ;
	
	}
	
	
	// all long, int, flaot, String , boolean getter methods
	public long get ( String key, long defValue ) {
		return preferences.getLong(key, defValue);
	}
	
	public boolean get ( String key, boolean defValue ) {
		return preferences.getBoolean(key, defValue);
	}
	
	public float get ( String key, float defValue ) {
		return preferences.getFloat(key, defValue);
	}
	
	public int get ( String key, int defValue ) {
		return preferences.getInt(key, defValue);
	}
	
	public String get ( String key, String defValue ) {
		return preferences.getString(key, defValue);
	}

	
}
