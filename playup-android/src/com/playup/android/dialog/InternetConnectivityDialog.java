package com.playup.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;

/**
 * Showing the No network dialog 
 */
public class InternetConnectivityDialog extends Dialog implements android.view.View.OnClickListener {

	public boolean exit = true;
	public InternetConnectivityDialog(Context context) {
		super(context);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(  R.layout.internet_dialog );
		
		findViewById(R.id.internet_dialog_ok).setOnClickListener(  this );
		
	}
	
	public InternetConnectivityDialog(Context context, boolean exit) {
		super(context);
		this.exit = exit;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(  R.layout.internet_dialog );
		findViewById(R.id.internet_dialog_ok).setOnClickListener(  this );	
	}
	
	/**
	 * Handling the on click listeners 
	 */
	@Override
	public void onClick(View v) {
		
		if ( PlayUpActivity.context != null ) {
			this.cancel();
		//	if(exit)
			//  PlayUpActivity.context.finish();
		}
	
	}

}
