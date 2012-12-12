package com.playup.android.fragment;


import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

/**
 * Editing the user profile fragment. 
 */
public class EditProfileFragment extends MainFragment implements OnClickListener {


	private EditText userNameEditText;
	private Button doneButton;
	private TextView  nameTextView;
	private TextView userIdTextView;

	private RelativeLayout avtarTounge;
	private ImageView avatarImageView;
	private RelativeLayout toungeExpandView;

	private String profileId;
	private boolean isSet = false;
	
	private ImageDownloader imageDownloader = new ImageDownloader();


	private String iId = "";

	private String vSelectedUrl = null;

	String providerId	=	null;
	RelativeLayout content_layout ;

	
	@Override
	public void onStop () {
		super.onStop();
		
		
		profileId = null;

		
		iId = null;
		vSelectedUrl = null;
		providerId	=	null;

	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)  {
		content_layout = ( RelativeLayout ) inflater.inflate ( R.layout.editprofile, null );
		return content_layout;
	}

	/**
	 * 
	 * initializes views 
	 */
	private void initializeViews(RelativeLayout contentLayout) {

		userIdTextView = ( TextView ) contentLayout.findViewById( R.id.editprofile_userId );
		nameTextView   = ( TextView ) contentLayout.findViewById( R.id.user_name );

		userNameEditText = ( EditText ) contentLayout.findViewById ( R.id.usernameEdittext );
		doneButton = ( Button ) contentLayout.findViewById ( R.id.save );

		avtarTounge = ( RelativeLayout ) contentLayout.findViewById ( R.id.avtarTounge );
		avatarImageView = ( ImageView ) contentLayout.findViewById( R.id.profile_image );
		toungeExpandView = ( RelativeLayout ) contentLayout.findViewById ( R.id.toungeExpandView );

		providerId	=	null;
		setListeners();

		setValues ();

	}

	/** 
	 * Setting Values
	 */
	private void setValues () {

		setUserData();
	}

	/**
	 * Updating the UI if any change has happened in the database. 
	 */
	@Override
	public void onUpdate ( final  Message msg ) {


		if ( PlayUpActivity.handler != null ) {

			PlayUpActivity.handler.post( new Runnable () {

				@Override
				public void run() {
					try {
						if ( !isVisible() ) {
							return;
						}
						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "EditProfileFragment" ) ) {
							resetValues();
						}
						setValues();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show ( e );
					}

				}
			});
		}
	}


	/**
	 * ressetting the values 
	 */
	private void resetValues () {

		userNameEditText.setText( "" );
		doneButton.setEnabled( false );
	}

	/**
	 *  showing soft keyboard when this fragment is in focus 
	 */
	@Override
	public void onResume () {
		super.onResume();
		if ( PlayUpActivity.handler == null ) {
			PlayUpActivity.handler = new Handler ();
		}
		if ( imageDownloader != null ) {
			//imageDownloader.refresh();
			imageDownloader = null;
		}
		imageDownloader = new ImageDownloader();
		initializeViews ( content_layout );

		
		isSet = false;
		userNameEditText.requestFocus();
		PlayUpActivity.mBinder = userNameEditText.getWindowToken();


		
		if ( PlayUpActivity.handler != null  ) {
			PlayUpActivity.handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					
					try {
						if ( userNameEditText != null ) {
							InputMethodManager imm = (InputMethodManager)PlayUpActivity.context.getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.showSoftInput(userNameEditText,InputMethodManager.SHOW_FORCED);
							imm = null;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show ( e );
					}
				}
			}, 500);

		}
		

	}

	/**
	 * Hiding the keyboard if this fragment goes out of focus. 
	 */
	@Override
	public void onPause () {
		super.onPause();

		InputMethodManager imm = (InputMethodManager)PlayUpActivity.context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(PlayUpActivity.mBinder, 0);
		imm = null;
		isSet = false;
	}

	/**
	 * Checking for valid text in the name  
	 */
	private TextWatcher txwatcher = new TextWatcher() {

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		public void onTextChanged ( CharSequence s, int start, int before, int count) {

			try{
			String text = userNameEditText.getText().toString();

			//not allowing user to enter invalid characters
			text = text.replace("&", "");
			text = text.replace("'", "");
			text = text.replace("/", "");
			text = text.replace(":", "");
			text = text.replace("<", "");
			text = text.replace(">", "");
			text = text.replace("@", "");
			text = text.replace("\"", "");
			text = text.replace(" ", "");
			text = text.trim();
			int result = text.length();
			
			// based on name length setting done button status
			if ( result > 0 ) {
				doneButton.setBackgroundResource( R.drawable.done_selector );
				doneButton.setEnabled( true );
			} else {
				doneButton.setBackgroundResource( R.drawable.generic_inactive_button );
				doneButton.setEnabled( false );
			}
			}catch (Exception e) {
				// TODO: handle exception
			}
		}

		public void afterTextChanged(Editable s) {}
	};

	/**
	 * setting the listeners  
	 * 
	 */
	private void setListeners ( ) {

		userNameEditText.addTextChangedListener(txwatcher);

		userNameEditText.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				avtarTounge.setVisibility(View.VISIBLE);
				toungeExpandView.setVisibility(View.INVISIBLE);
			}
		});

		userNameEditText.setOnEditorActionListener(
				new EditText.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

						
						String text = userNameEditText.getText().toString();


						int result = text.length();
						if( result > 0 ) {
							if ( actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
								editProfile();
								text = null;
								return true;
							}
						}
						text = null;
						return false;
					}
				});

		avtarTounge.setOnClickListener( this );
		toungeExpandView .setOnClickListener( this );
		doneButton.setOnClickListener( this );
	}


	@Override
	public void onClick(View v) {

		switch ( v.getId() ) {

		case R.id.avtarTounge : 

			// hiding the keyboard
			InputMethodManager inputManager = (InputMethodManager)        PlayUpActivity.context.getSystemService( PlayUpActivity.context.INPUT_METHOD_SERVICE); 
			inputManager.hideSoftInputFromWindow( userNameEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			
			inputManager = null;
			avtarTounge.setVisibility(View.INVISIBLE);

			// showing the user images in the avatar.
			ImageView [] avatarImages = new ImageView [ 3 ];
			ImageView bottom_imgView = (ImageView) toungeExpandView.findViewById( R.id.toungeExpandView_bottom );
			avatarImages [ 0 ] = (ImageView) toungeExpandView.findViewById( R.id.avtarImage1 );
			avatarImages [ 1 ] = (ImageView) toungeExpandView.findViewById( R.id.avtarImage2 );
			avatarImages [ 2 ] = (ImageView) toungeExpandView.findViewById( R.id.avtarImage3 );

			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			Hashtable< String , List < String > > data = dbUtil.getProvidersSortOrder( "vUserName, vAvatarUrl" ,dbUtil.getUserAvatarUrl());
			dbUtil = null;
			if ( data != null && data.get( "vUserName" ).size() > 0 ) {
				int len = data.get( "vUserName" ).size();
				for ( int i = 0 ; i < len; i++ ) {
					if( data.get( "vAvatarUrl" ).get( i )!=null && !data.get( "vAvatarUrl" ).get( i ).trim().equals(""))
					{						
						avatarImages [ i ].setVisibility( View.VISIBLE );	

						avatarImages [ i ].setTag( R.id.about_txtview, data.get( "vAvatarUrl" ).get( i ) );
						avatarImages [ i ].setTag( R.id.activity_list_relativelayout, data.get( "vUserName" ).get( i )  );
						avatarImages [ i ].setOnClickListener( toungAvatarClickListner );
						imageDownloader.download( data.get( "vUserName" ).get( i ) , data.get( "vAvatarUrl" ).get( i ).replace("square", "large"),  avatarImages [ i ], true,null );
					}
				}

				if ( len > 1 ) {
					int resID = PlayUpActivity.context.getResources().getIdentifier( "avtarImage"+ ( len - 1 ) , "id", PlayUpActivity.context.getPackageName());
					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(  RelativeLayout.LayoutParams.WRAP_CONTENT,  RelativeLayout.LayoutParams.WRAP_CONTENT );
					lp.topMargin = 0;
					lp.addRule( RelativeLayout.BELOW, resID );
					bottom_imgView.setLayoutParams( lp );
					lp = null;
				}

			}
			bottom_imgView = null;
			if ( data != null ) {
				data.clear();
				data = null;
			}
			avatarImages = null;
			toungeExpandView.setVisibility(View.VISIBLE);
			break;


		case R.id.toungeExpandView :
			avtarTounge.setVisibility(View.VISIBLE);
			toungeExpandView.setVisibility(View.INVISIBLE);
			break;

		case R.id.save :

			// calling the server to update the change profile 
			// also make DB local changes
			editProfile();
			break;
		}

	}



	/**
	 *  Editing of Profile
	 */


	public void editProfile(){
		if(!Constants.isFetchingCredentials){
			new Util().editProfile ( vSelectedUrl, profileId, userNameEditText.getText().toString(),providerId );
			PlayupLiveApplication.getFragmentManagerUtil().popBackStack( "EditProfileFragment" );
		}
	}


	private OnClickListener toungAvatarClickListner = new OnClickListener( ) {

		@Override
		public void onClick(View v) {
			if ( v.getTag( R.id.about_txtview ) != null ) {
				String url = (String) v.getTag( R.id.about_txtview );

				providerId = (String) v.getTag( R.id.activity_list_relativelayout );

				vSelectedUrl = url;

				avtarTounge.setVisibility(View.VISIBLE);
				toungeExpandView.setVisibility(View.INVISIBLE);

				imageDownloader.download( providerId , vSelectedUrl.replace("square", "large"),  avatarImageView, true,null );

			}
		}
	};

	/**
	 * sets the user data  
	 */
	private void setUserData () {
		try{
		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT iUserId, vUserName, vName, vUserAvatarUrl, isAnonymousUser, vUserToken, vSelfUrl,nHrefUrl, iId FROM user WHERE isPrimaryUser = \"1\" " );
		if ( c != null  ) {

			if ( c.getCount() > 0 ) {

				c.moveToFirst();

				profileId = c.getString( c.getColumnIndex( "iUserId" ) );
				String userName = c.getString( c.getColumnIndex( "vUserName" ) );
				String name = c.getString( c.getColumnIndex( "vName" ) );
				vSelectedUrl = c.getString( c.getColumnIndex( "vUserAvatarUrl" ) );
				iId = c.getString( c.getColumnIndex( "iId" ) );

				setTopBarFragment ( userName );
				setUserName ( name );
				setUserId ( iId );
				setUserAvatar( );

			}
			c.close();
			c = null;
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
	}

	/**
	 *  setting the room in the top bar fragment.
	 **/
	private void setTopBarFragment ( String userName ) {
		try{
		if ( !isSet ) {
			isSet = true;
			HashMap< String, String > map = new HashMap<String, String>();
			map.put( "vUserName",userName );
			Bundle b = new Bundle();
			b.putString("vMainColor",null );
			b.putString("vMainTitleColor",null );
			Message msg = new Message ();
			msg.setData(b);
			msg.obj = map;
			PlayupLiveApplication.callUpdateOnFragments(msg);
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
	}


	/**
	 * setting the user name and name 
	 */
	private void setUserName ( final String userName ) {

		nameTextView.setText( new Util().getSmiledText(userName ));
		userNameEditText.setText( "" );
		userNameEditText.append( new Util().getSmiledText(userName) );

	}

	/**
	 * setting the user id
	 */
	private void setUserId ( final String userId ) {
		userIdTextView.setText ( userId );
	}

	/**
	 * setting the avatar of the user 
	 */
	private void setUserAvatar( ) {
		try{
		/*
		// for time being we have taken id for image as the user token.
		int width_dp = 87 * Constants.DPI / 160 ; 
		int height_dp = 77 * Constants.DPI / 160; 

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams( width_dp, height_dp );

		avatarImageView.setLayoutParams(  lp );
		avatarImageView.setPadding( 5,5,5,5);
		 */
		imageDownloader.download( iId , vSelectedUrl.replace("square", "large"),  avatarImageView, true,null );
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}

}
