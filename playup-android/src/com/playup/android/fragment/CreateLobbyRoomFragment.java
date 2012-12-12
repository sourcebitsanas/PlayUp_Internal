package com.playup.android.fragment;

import java.util.HashMap;


import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

/**
 * This fragment is for creating or editing Rooms  <br>
 * Will be called from PrivateLobby and PrivateLobbyRoomfragments.
 */
public class CreateLobbyRoomFragment extends MainFragment implements OnClickListener {
	private EditText hangoutEdittext;
	private Button doneButton;
	private TextView characterCount;
	private TextView privateDesc;
	public static boolean  isHome = false;

	private boolean isAgain = false;
	private ProgressDialog dialog ;

	private final static int ROOMNAME_LENGTH = 30;
	Handler mHandler;

	RelativeLayout content_layout;
	private boolean isEdit = false;
	private String vConversationId = null;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;
	private String fromFragment = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		
		content_layout = (RelativeLayout) inflater.inflate(R.layout.create_private_lobby, null);
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
	 * Getting the values from previous fragment
	 * @param bundle
	 */
	private void setContestId(Bundle bundle){
		try{
		vMainColor = null;
		vMainTitleColor = null;
		vSecColor = null;
		vSecTitleColor = null;
		
		fromFragment  = null;
		
		isEdit = false;
		if( bundle!=null && bundle.containsKey("editRoom") ) {
			isEdit = true;
		}
		if( bundle!=null && bundle.containsKey("vConversationId") ) {
			vConversationId = bundle.getString("vConversationId");
		}if (bundle != null &&bundle.containsKey("vMainColor")) {
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
			Logs.show(e);
		}
	}

	/**
	 * 
	 * initializes views
	 */
	private void initializeViews(RelativeLayout contentLayout) {	//verfied
		hangoutEdittext = (EditText) contentLayout
		.findViewById(R.id.hangoutEdittext);

		PlayUpActivity.mBinder	=	hangoutEdittext.getWindowToken();
		doneButton = (Button) contentLayout.findViewById(R.id.doneButton);
		characterCount = (TextView) contentLayout
		.findViewById(R.id.characterCount);

		privateDesc = (TextView) contentLayout.findViewById(R.id.privateDesc);
		setTypeFaces();
		setListeners();
		setTopBar();

		if ( !isAgain ) {
			// set values
			setValues();
		}

	}


	/**
	 * setting topbar data
	 */
	private void setTopBar () {
		try{
		Bundle b = new Bundle();
		b.putString("vMainColor",vMainColor );
		b.putString("vMainTitleColor",vMainTitleColor );
		Message msg = new Message ();
		msg.setData(b);
		HashMap<String, String> map = new HashMap<String, String>();

		if(isEdit)
			map.put("title",PlayupLiveApplication.getInstance().getString(R.string.edit_room));
		else
			map.put("title",PlayupLiveApplication.getInstance().getString(R.string.create_private_room));

		msg.obj = map;

		PlayupLiveApplication.getFragmentManagerUtil().updateTopBarFragment( msg );
		
		}catch(Exception e){
			Logs.show(e);
		}

	}



	/**
	 * setting typefaces
	 * 
	 */
	private void setTypeFaces() {
		hangoutEdittext.setTypeface(Constants.OPEN_SANS_REGULAR);
		characterCount.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		privateDesc.setTypeface(Constants.OPEN_SANS_LIGHT);
	}

	/**
	 * setting values
	 * 
	 */
	private void setValues() {	//verfeid
		try{
		hangoutEdittext.setHintTextColor(Color.parseColor("#A5A5A5"));
		if( isEdit ) {
			privateDesc.setText(R.string.editLobbyRoomDesc);
			setRoomName();
		} else {
			privateDesc.setText(R.string.privateLobbyRoomDesc);
			hangoutEdittext.setText("");
		}
		}catch(Exception e){
			Logs.show(e);
		}

	}

	/**
	 * setting the listeners
	 * 
	 */
	private void setListeners() {
		hangoutEdittext.addTextChangedListener(txwatcher);
		doneButton.setOnClickListener(this);
	}

	/**
	 * setting the editable room name
	 */
	private void setRoomName() {	//verfied
		try{
		if( vConversationId != null) {
			Cursor c = PlayupLiveApplication
			.getDatabaseWrapper()
			.selectQuery(
					" SELECT vConversationName FROM friendConversation WHERE vConversationId = \""
					+ vConversationId + "\" ");
			if (c != null) {

				if (c.getCount() > 0) {
					c.moveToFirst();
					hangoutEdittext.setText(c.getString(c.getColumnIndex("vConversationName")));
				}
				c.close();
				c = null;
			}
		}
		}catch(Exception e){
			Logs.show(e);
		}
	}

	@Override
	public void onClick(View v) {
		try{
		switch (v.getId()) {
		case R.id.doneButton:
			createRoom();				
			break;

		default:
			break;
		}
		}catch(Exception e){
			Logs.show(e);
		}

	}


	@Override 
	public void onUpdate ( final Message msg ) {	//verfied

		/**
		 *  Checking for the round id and the page to match to update the UI.
		 */

		try{
		if ( msg == null ) {
			return;
		}
		if ( msg.obj != null && msg.obj.toString().equalsIgnoreCase( "CreateLobbyHangout" ) ) {

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
								
								try {

									PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );

									String vConversationId = msg.getData().getString( "vConversationId" );
									Bundle bundle = new Bundle ();
									bundle.putString( "vConversationId", vConversationId );
									bundle.putString("vMainColor",vMainColor );									
									bundle.putString("fromFragment",fromFragment );
									bundle.putString("vMainTitleColor",vMainTitleColor );
									bundle.putString( "vSecColor",vSecColor );			
									bundle.putString( "vSecTitleColor",vSecTitleColor );
									PlayupLiveApplication.getFragmentManagerUtil().setFragment("PrivateLobbyRoomFragment", bundle,R.id.main );
	
								} catch ( Exception e ) {
									Logs.show ( e );
								} catch ( Error e ) {
									Logs.show ( e );
								}
								
								return;
							}  else {
								//PlayupLiveApplication.showToast(R.string.createRoomFailedMsg);
							}
						} catch (Exception e) {
							Logs.show ( e );
						}
					}

				});

			}
		}else if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
			
			if(dialog.isShowing())
				dialog.dismiss();
			
		}
		}catch(Exception e){
			Logs.show(e);
		}
	}

	
	/**
	 * Creating room, using the room name filled by user
	 */
	private void createRoom(){	//verfied
		try{

		InputMethodManager imm = (InputMethodManager)PlayUpActivity.context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(hangoutEdittext.getWindowToken(), 0);
		imm = null;

		
		if ( PlayUpActivity.context != null && Constants.isCurrent && CreateLobbyRoomFragment.this.getActivity() != null ) {
			dialog = ProgressDialog.show(PlayUpActivity.context, "",getString(R.string.pleasewait), false);
			
		}

		String hangoutName = hangoutEdittext.getText().toString();

		// if the mode is Edit, then do room edit operations
		if ( isEdit && vConversationId != null) {
			
			
			
			String vConversationUrl = null;
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(" SELECT vConversationUrl,vConversationHrefUrl FROM friendConversation WHERE vConversationId = \""
					+ vConversationId + "\" ");
			
			
			Boolean isHrefUrl = false;
			if (c != null) {

				if (c.getCount() > 0) {
					c.moveToFirst();
					if(c.getString(c.getColumnIndex("vConversationHrefUrl"))!=null && c.getString(c.getColumnIndex("vConversationHrefUrl")).trim().length()> 0){
						vConversationUrl = c.getString(c.getColumnIndex("vConversationHrefUrl"));
						isHrefUrl	= true;
					}
					else
					{
						vConversationUrl = c.getString(c.getColumnIndex("vConversationUrl"));
						isHrefUrl	= false;
					}
					
										
				}
				c.close();
				c = null;
			}
			if( vConversationUrl != null ){
				
				//Praveen Changes
				/*new Util().editLobbyHangout( hangoutName,vConversationUrl );*/
				new Util().editLobbyHangout( hangoutName,vConversationUrl,isHrefUrl );
				
				
			}
		} else {
			//Praveen : Changes done
			//  if the mode is create, do room create operations
			new Util().createLobbyHangout ( hangoutName );
		}
		
		}catch(Exception e){
			Logs.show(e);
		}


	}
	/**
	 * Checking for valid text in the name
	 */
	private TextWatcher txwatcher = new TextWatcher() {

		public void beforeTextChanged(CharSequence s, int start, int count,int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,int count) {

			try{
				String text = hangoutEdittext.getText().toString();

				// not allowing user to enter extra characters
				text = text.replace("&", "");
				text = text.replace("'", "");
				text = text.replace("/", "");
				text = text.replace(":", "");
				text = text.replace("<", "");
				text = text.replace(">", "");
				text = text.replace("@", "");
				text = text.replace("\"", "");

				int result = text.length();
				
				/**
				 * checking the lenght of the room name.
				 * Setting count color and Done button status
				 */
				if (result > 0) {
					doneButton.setBackgroundResource(R.drawable.done_selector);
					doneButton.setEnabled(true);
				} else {
					characterCount.setText( ROOMNAME_LENGTH + "" );
					doneButton.setBackgroundResource(R.drawable.generic_inactive_button);
					doneButton.setEnabled(false);
				}
				
				if (result > ROOMNAME_LENGTH) {
					characterCount.setTextColor(Color.parseColor("#FF4754"));
				} else {
					characterCount.setTextColor(Color.parseColor("#ABA8AB"));
				}


				characterCount.setText("" + (ROOMNAME_LENGTH - result));

			}catch (Exception e) {
				// TODO: handle exception
				Logs.show(e);
			}
		}

		public void afterTextChanged(Editable s) {

		}
	};

	
	@Override
	public void onDestroy () {
		super.onDestroy();

	}

	@Override
	public void onStop () {
		super.onStop();
		mHandler = null;
		if ( dialog != null && dialog.isShowing() )  {
			dialog.dismiss();
		}

	}


	@Override
	public void onResume() {	//verfied
		super.onResume();
		try{
		mHandler = new Handler ();

		initializeViews(content_layout);

		hangoutEdittext.requestFocus();
		PlayUpActivity.mBinder	=	hangoutEdittext.getWindowToken();
		hangoutEdittext.setHintTextColor(Color.parseColor("#A5A5A5"));
		hangoutEdittext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

		
		// Open the soft keypad
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
					Logs.show ( e );
				}
			}
		}, 500);



		if ( isAgain  ) {
			refresh ();
		}
		
		}catch(Exception e){
			Logs.show(e);
		}

	}


	/**
	 * called whenever the fragment opens
	 */
	private void refresh() {	//verfied
		try{
		if ( !isHome ) {
			hangoutEdittext.setText("");
		}
		setValues();
		}catch(Exception e){
			Logs.show(e);
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
