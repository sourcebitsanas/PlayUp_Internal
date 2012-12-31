package com.playup.android.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import com.playup.android.util.Logs;
import com.playup.android.util.Util;
/**
 * Used to create/Edit public rooms
 */

public class CreateRoomFragment extends MainFragment implements OnClickListener {
	private EditText hangoutEdittext;
	private ImageView switchImage;
	private Button doneButton;
	private TextView characterCount;
	private TextView privateTxt;
	private TextView publicTxt;
	private TextView privateDesc;
	private TextView publicDesc;
	private boolean privateStatus = true;
	public static boolean  isHome = false;

	private boolean isAgain = false;
	private String vContestId = null;

	private ProgressDialog dialog ;

	private final static int ROOMNAME_LENGTH = 30;
	Handler mHandler = new Handler ( ) ;

	RelativeLayout content_layout;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;
	private String fromFragment = null;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		
		content_layout = (RelativeLayout) inflater.inflate(R.layout.create_room, null);
		if ( !isAgain ) {
			setContestId ( getArguments() ); 
		}


		return content_layout;
	}

	@Override
	public void onAgainActivated(Bundle args) {

		isAgain = true;
		setContestId ( args );

	}

	/**
	 * Getting values passed from previous fragment
	 * @param bundle
	 */
	private void setContestId(Bundle bundle){
		
		try{
		vMainColor = null;
		vMainTitleColor = null;
		
		vSecColor = null;
		 vSecTitleColor = null;
		 
		 fromFragment  = null;
		
		if ( bundle != null && bundle.containsKey( "vContestId") ) {
			vContestId = bundle.getString( "vContestId" );
		} else {
			vContestId = null;
		}if (bundle != null && bundle.containsKey("vMainColor")) {
			vMainColor = bundle.getString("vMainColor");
		}if (bundle != null && bundle.containsKey("vMainTitleColor")) {
			vMainTitleColor = bundle.getString("vMainTitleColor");
		}if (bundle != null && bundle.containsKey("vSecColor")) {
			vSecColor = bundle.getString("vSecColor");
		}if (bundle != null && bundle.containsKey("vSecTitleColor")) {
			vSecTitleColor = bundle.getString("vSecTitleColor");
		}if (bundle != null && bundle.containsKey("fromFragment")) {
			fromFragment = bundle.getString("fromFragment");
		}
		}catch(Exception e){
//			Logs.show(e);
		}

	}

	/**
	 * 
	 * initializes views
	 */
	private void initializeViews(RelativeLayout contentLayout) { //verfied
		hangoutEdittext = (EditText) contentLayout
		.findViewById(R.id.hangoutEdittext);

		PlayUpActivity.mBinder	=	hangoutEdittext.getWindowToken();
		switchImage = (ImageView) contentLayout.findViewById(R.id.switchImage);
		doneButton = (Button) contentLayout.findViewById(R.id.doneButton);
		characterCount = (TextView) contentLayout
		.findViewById(R.id.characterCount);
		privateTxt = (TextView) contentLayout.findViewById(R.id.privateTxt);
		publicTxt = (TextView) contentLayout.findViewById(R.id.publicTxt);

		privateDesc = (TextView) contentLayout.findViewById(R.id.privateDesc);
		publicDesc = (TextView) contentLayout.findViewById(R.id.publicDesc);

		setTypeFaces();
		setListeners();
		setTopBar();

		if ( !isAgain ) {
			// set values
			setValues();
		}

	}



	/**
	 *  setting the topbar data
	 */
	private void setTopBar () {

		try{
		Bundle b = new Bundle();
		b.putString("vMainColor",vMainColor );
		b.putString("vMainTitleColor",vMainTitleColor );
		Message msg = new Message ();
		msg.setData(b);
		
		PlayupLiveApplication.callUpdateTopBarFragments(msg);
		
		}catch(Exception e){
//			Logs.show(e);
		}
	}



	/**
	 * setting typefaces
	 * 
	 */
	private void setTypeFaces() {
		hangoutEdittext.setTypeface(Constants.OPEN_SANS_REGULAR);
		characterCount.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		privateTxt.setTypeface(Constants.OPEN_SANS_LIGHT);
		publicTxt.setTypeface(Constants.OPEN_SANS_LIGHT);
		privateDesc.setTypeface(Constants.OPEN_SANS_REGULAR);
		publicDesc.setTypeface(Constants.OPEN_SANS_REGULAR);
	}

	/**
	 * setting values
	 * 
	 */
	private void setValues() {	//verfied
		hangoutEdittext.setHintTextColor(Color.parseColor("#A5A5A5"));
		setPrivateSwitchImage(privateStatus);
	}

	/**
	 * setting the listeners
	 * 
	 */
	private void setListeners() {
		hangoutEdittext.addTextChangedListener(txwatcher);
		switchImage.setOnClickListener(this);
		doneButton.setOnClickListener(this);
	}

	/**
	 * Setting the private and public switches base on swithdImage click
	 */

	public void setPrivateSwitchImage(boolean isPrivate) {	//verfied
		if (isPrivate) {
			switchImage.setImageResource(R.drawable.switch_private);
			privateTxt.setTextColor(Color.parseColor("#D900EB"));
			publicTxt.setTextColor(Color.parseColor("#736E73"));
		} else {
			switchImage.setImageResource(R.drawable.switch_public);
			privateTxt.setTextColor(Color.parseColor("#736E73"));
			publicTxt.setTextColor(Color.parseColor("#28A645"));
		}
	}

	@Override
	public void onClick(View v) {	//verfied
		try{
		switch (v.getId()) {
		case R.id.doneButton: // pressing done button
			createRoom();				
			break;
		case R.id.switchImage: // changing room status ( private/public)
			if (privateStatus) {
				privateStatus = false;
				setPrivateSwitchImage(privateStatus);
			} else {
				privateStatus = true;
				setPrivateSwitchImage(privateStatus);
			}
			break;

		default:
			break;
		}
		
		}catch(Exception e){
//			Logs.show(e);
		}

	}


	@Override 
	public void onUpdate ( final Message msg ) {

		/**
		 *  Checking for the round id and the page to match to update the UI.
		 */

		try{
		if ( msg == null ) {
			return;
		}
		
		//After creating room, navigate back to previous screen
		if ( msg.obj != null && msg.obj.toString().equalsIgnoreCase( "CreateRoom" ) ) {

			if ( PlayUpActivity.handler != null ) {

				PlayUpActivity.handler.post( new Runnable () {

					
					@Override
					public void run() {
						
						try {
							if ( !isVisible() ) {
								return;
							}
							
							if(dialog.isShowing())
								dialog.dismiss();

							if ( msg.arg1 == 0) {
								PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );
								String vConversationId = msg.getData().getString( "vConversationId" );
								Bundle bundle = new Bundle ();
								bundle.putString( "vConversationId", vConversationId );
								//bundle.putString( "vContestId", vContestId );
								bundle.putString( "fromFragment", fromFragment );
								
								bundle.putString("vMainColor",vMainColor );
								bundle.putString("vMainTitleColor",vMainTitleColor );
								
								bundle.putString( "vSecColor",vSecColor );			
								bundle.putString( "vSecTitleColor",vSecTitleColor );

								PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchHomeFragment", bundle,R.id.main );

								return;
							}  
						} catch (Exception e) {
							// TODO Auto-generated catch block
//							Logs.show ( e );
						}
					}

				});

			}
		}else if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
			
			if(dialog.isShowing())
				dialog.dismiss();
			
		}
		}catch(Exception e){
//			Logs.show(e);
		}
	}

	//Creating room, closing keyboard and showing dialog
	private void createRoom(){	//verfied
		
		try{

		InputMethodManager imm = (InputMethodManager)PlayUpActivity.context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(hangoutEdittext.getWindowToken(), 0);
		imm = null;

		if ( PlayUpActivity.context != null && Constants.isCurrent && CreateRoomFragment.this.getActivity() != null ) {
			dialog = ProgressDialog.show(PlayUpActivity.context, "",
					getString(R.string.pleasewait), false);
		}
		String hangoutName = hangoutEdittext.getText().toString();
		new Util().createConversationOrRoom(hangoutName,vContestId,privateStatus);
		
		}catch(Exception e){
//			Logs.show(e);
		}
	}
	
	/**
	 * Checking for valid text in the name
	 */
	private TextWatcher txwatcher = new TextWatcher() {

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

			try{
			String text = hangoutEdittext.getText().toString();

			// not allowing user to type extra characters
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
			
			//based on the room lenght changing count color and dont button status
			if (result > 0) {
				doneButton.setBackgroundResource(R.drawable.done_selector);
				doneButton.setEnabled(true);
			} else {
				characterCount.setText( ROOMNAME_LENGTH + "" );
				doneButton.setBackgroundResource(R.drawable.generic_inactive_button);
				doneButton.setEnabled(false);
			}
			}catch (Exception e) {
				// TODO: handle exception
			}
		}

		public void afterTextChanged(Editable s) {
			characterCount.setText( ( ROOMNAME_LENGTH - s.toString().length() ) + "");
		}
	};

	
	
	@Override
	public void onDestroy () {
		super.onDestroy();
		
		vContestId = null; 
	}
	@Override
	public void onStop () {
		super.onStop();
		mHandler = null;
		
		//Closing dialog before 
		if ( dialog != null && dialog.isShowing() )  {
			dialog.dismiss();
		}
				
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		try{
		mHandler = new Handler ();
		
		initializeViews(content_layout);

		hangoutEdittext.requestFocus();
		PlayUpActivity.mBinder	=	hangoutEdittext.getWindowToken();
		hangoutEdittext.setHintTextColor(Color.parseColor("#A5A5A5"));
		hangoutEdittext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

		//hangoutEdittext.setText("");
		switchImage.setImageResource(R.drawable.switch_private);
		privateTxt.setTextColor(Color.parseColor("#D900EB"));		
		publicTxt.setTextColor(Color.parseColor("#736E73"));
		
		//opening soft keypad
		hangoutEdittext.requestFocus();
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				try {
					if ( hangoutEdittext != null ) {
						InputMethodManager imm = (InputMethodManager)PlayUpActivity.context.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.showSoftInput(hangoutEdittext,InputMethodManager.SHOW_FORCED);
						imm = null;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}
			}
		}, 500);

		privateStatus = true;


		if ( isAgain  ) {
			refresh ();
		}


		//setValues();
		
		}catch(Exception e){
//			Logs.show(e);
		}

	}



	/**
	 * called, when fragment launches
	 */
	private void refresh() {
		try{
		if ( !isHome ) {
			hangoutEdittext.setText("");
		}
		setValues();
		}catch(Exception e){
//			Logs.show(e);
		}
	}


	/**
	 * Hiding the keyboard if this fragment goes out of focus.
	 */
	@Override
	public void onPause() {
		super.onPause();
		InputMethodManager imm = (InputMethodManager)PlayUpActivity.context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(PlayUpActivity.mBinder, 0);
		imm = null;
		isHome = true;

	}



}
