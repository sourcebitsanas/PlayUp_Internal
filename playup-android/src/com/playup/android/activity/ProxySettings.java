package com.playup.android.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.playup.android.R;
import com.playup.android.util.PreferenceManagerUtil;

/**
 * Setting the paramters for proxy. 
 */
public class ProxySettings extends Activity {


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.proxy_configuration );



		final EditText hostEditText	=	(EditText) findViewById(R.id.login_emailedittext);
		final EditText portEditText	=	(EditText) findViewById(R.id.login_passwordEditText);

		PreferenceManagerUtil preferenceUtil = new PreferenceManagerUtil();
		hostEditText.setText( preferenceUtil.get( "Host", "" ) );
		portEditText.setText(preferenceUtil.get("Port", ""));
		preferenceUtil = null;
		findViewById(R.id.btn_save).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				PreferenceManagerUtil preferenceUtil = new PreferenceManagerUtil();
				preferenceUtil.set("Host", hostEditText.getText().toString());
				preferenceUtil.set("Port", portEditText.getText().toString());
				
				preferenceUtil = null;
				Intent intent = new Intent ( ProxySettings.this, PlayUpActivity.class );
				startActivity( intent );
				
				finish();
				
			}
		});

		findViewById(R.id.btn_clear).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				PreferenceManagerUtil preferenceUtil = new PreferenceManagerUtil();
				
				preferenceUtil.set("Host", "");
				preferenceUtil.set("Port", "");
				preferenceUtil = null;
				Intent intent = new Intent ( ProxySettings.this, PlayUpActivity.class );
				startActivity( intent );
				finish();
				
			}
		});

	}



}
