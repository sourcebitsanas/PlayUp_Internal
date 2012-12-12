package com.playup.android.fragment;

import java.util.Hashtable;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.dialog.NoProviderDialog;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;


/**
 * 
 * Provider Fragment 
 */
public class ProviderFragment extends MainFragment implements OnClickListener {

	// layout where the provider images are shown.
	private LinearLayout provider_layout;


	// for storing the provider data.
	private Hashtable<String, List<String>> data;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;
	



	private boolean isSet = false;
	 private String fromFragment = "MenuFragment";

	private ImageDownloader imageDownloader = new ImageDownloader();
	private String vConversationId = "";
	private String vFriendName = "";
	
	RelativeLayout content_layout;
	boolean isForSharing = true;


	private boolean isAgain = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// inflating the layout for the UI
		
		content_layout = ( RelativeLayout ) inflater.inflate(R.layout.provider, container, false);

		
		
		if(!isAgain )
			setFromFragment( getArguments() );
		

		return content_layout;
	}


	@Override
	public void onResume () {
		super.onResume();

		
		if ( imageDownloader == null ) {
			imageDownloader = new ImageDownloader();
		}
		

		
		if ( fromFragment == null ) {
			fromFragment = "MenuFragment";
		}
		
		content_layout.setId( 0 );
		content_layout.setOnTouchListener( new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return false;
			}
		});
		initialize ( content_layout );
		
	}


	@Override
	public void onAgainActivated(Bundle arguments) {

		
		isAgain = true;
		setFromFragment(arguments);

	}
	
	/**
	 * setting the params passed on from the previous fragment
	 * @param args
	 */
	
	 public void setFromFragment(Bundle args){
		 
		 vMainColor = null;
			vMainTitleColor = null;
			
			
			
		 
		 if (args != null) {
				if ( args.containsKey( "sharing") ) {
					isForSharing = args.getBoolean( "sharing" );
				} else {
					isForSharing = false;
				}
				if (args.containsKey("fromFragment") && args.getString("fromFragment") != null) {
					fromFragment = args.getString("fromFragment");
					isSet = true;
				}
				if (args.containsKey("vConversationId") && args.getString("vConversationId") != null) {
					vConversationId = args.getString("vConversationId");
					
				}else{
					vConversationId = null;
					
				}if (args.containsKey("vFriendName") && args.getString("vFriendName") != null) {
					vFriendName = args.getString("vFriendName");
					
				}else{
					vFriendName = null;
					
				}if (args != null && args.containsKey("vMainColor")) {
					vMainColor = args.getString("vMainColor");
				}if (args != null && args.containsKey("vMainTitleColor")) {
					vMainTitleColor = args.getString("vMainTitleColor");
				}
				
				
				
		}
		
		
	}

	/**
	 * setting all the content in the views
	 */
	private void initialize(final RelativeLayout content_layout) {

		
		if (!isSet) {
			Bundle bundle = getArguments();
			if ( bundle != null &&  bundle.containsKey( "sharing") ) {
				isForSharing = bundle.getBoolean( "sharing" );
			} else {
				isForSharing = false;
			}
			if (bundle != null && bundle.containsKey("fromFragment")) {
				fromFragment = bundle.getString("fromFragment");
				
			}if (bundle != null && bundle.containsKey("vMainColor")) {
				vMainColor = bundle.getString("vMainColor");
			}if (bundle != null && bundle.containsKey("vMainTitleColor")) {
				vMainTitleColor = bundle.getString("vMainTitleColor");
			}
			
			
			
		}
		
		
		// initialize views
		initializeViews(content_layout);

		// set values
		setValues();
	}
	
	/**
	 * initializing views
	 * @param content_layout
	 */

	private void initializeViews(final RelativeLayout content_layout) {
		

		provider_layout = (LinearLayout) content_layout.findViewById(R.id.provider_layout);

		((TextView) content_layout.findViewById(R.id.provider_text1)).setTypeface(Constants.OPEN_SANS_REGULAR);
		((TextView) content_layout.findViewById(R.id.provider_text2)).setTypeface(Constants.OPEN_SANS_REGULAR);
		((TextView) content_layout.findViewById(R.id.provider_text3)).setTypeface(Constants.OPEN_SANS_REGULAR);

	}
	/**
	 * setting the list of the providers
	 */
	
	private void setValues() {

		try{
		
		LayoutParams params = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);
		provider_layout.removeAllViews();

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		data = dbUtil.getProviders();
		dbUtil = null;
		

		if (data != null && data.get("vProviderName").size() > 0) {

			int len = data.get("vProviderName").size();

			// initializing the ImageDownloader class to download images.
			for (int i = 0; i < len; i++) {

				ImageView imgView = new ImageView(  PlayUpActivity.context  );

				String icon_url = data.get("vIconLoginUrl").get(i);
				String sel_icon_url = data.get("vIconHightLightUrl").get(i);

				// setting the tag for identification and setting the
				// listeners.
				imgView.setTag(i);
				imgView.setOnClickListener(this);

				imgView.setPadding(12, 0, 12, 0);


				// calling download method to download the image and set in

				imageDownloader.download(  icon_url, imgView, false,null );
				imageDownloader.download( sel_icon_url, null, false,null );


				// adding the imageview in the layout
				provider_layout.addView(imgView,params);
			}
		} else {

			if ( Constants.isCurrent ) {

				PlayupLiveApplication.showToast( R.string.internet_connectivity_dialog );
				

			}
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show ( e );
		}
	}

	/**
	 * handling data if any change in database has occured.
	 */
	@Override
	public void onUpdate(Message msg) {


		try {
			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "UpdateImage" ) ) {
				setValues();
			} else{
				if ( PlayUpActivity.handler != null) {
					PlayUpActivity.handler.post(new Runnable() {

						@Override
						public void run() {
							try {
								if ( !isVisible() ) {
									 return;
								 }
								// refresh the values .
								setValues();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								Logs.show ( e );
							}
						}
					});
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}

	/**
	 * on click of the providers,redirecting to loginWebViewFragment with necessary params
	 */
	@Override
	public void onClick(View v) {
		
		if ( v == null ) {
			return;
		}

		if ( v.getId() == 0 ) {
			return;
		}
		int position = Integer.parseInt(v.getTag().toString());


		imageDownloader.download(   data.get("vIconHightLightUrl").get( position ) ,  ( ImageView ) v, false,null );

		Bundle bundle = new Bundle();

		bundle.putString("vLoginUrl", data.get("vLoginUrl").get(position) );
		bundle.putString("vSuccessUrl", data.get("vSuccessUrl").get(position));
		bundle.putString("vFailureUrl", data.get("vFailureUrl").get(position));
		
		
		
		bundle.putString("vConversationId", vConversationId);
		bundle.putString("vFriendName", vFriendName);
		
		bundle.putBoolean( "sharing", isForSharing );
		bundle.putString("fromFragment", fromFragment);
		
		bundle.putString("vMainColor",vMainColor );
		bundle.putString("vMainTitleColor",vMainTitleColor );
		
		

		PlayupLiveApplication.getFragmentManagerUtil().setFragment ( "LoginWebViewFragment", bundle );
	}

}
