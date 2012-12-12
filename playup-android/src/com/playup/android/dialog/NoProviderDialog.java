package com.playup.android.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;

/**
 * Showing no provider dialog
 **/
public class NoProviderDialog extends Dialog implements android.view.View.OnClickListener {


	public NoProviderDialog(Context context) {
		super(context);
	
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(  R.layout.provider_dialog );
		findViewById(R.id.provider_dialog_ok).setOnClickListener(  this );
		
	}
	
	/**
	 * Handling the click listeners 
	 */
	@Override
	public void onClick(View v) {
		
		if ( PlayUpActivity.context != null ) {
			this.cancel();
		}
	
	}

}
