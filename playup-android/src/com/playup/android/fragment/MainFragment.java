package com.playup.android.fragment;

import java.util.Hashtable;

import android.R;
import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;

import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.interfaces.FragmentInterface;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

public class MainFragment extends Fragment implements  FragmentInterface {


	public Hashtable<String,  Runnable > runnableList = new Hashtable <String, Runnable > ( );

	/**
	 * When creating, retrieve this instance's number from its arguments.
	 */
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	
	public void onAttach (Activity activity) {
		super.onAttach( PlayUpActivity.context );
	}

	/**
	 * to update the top bar fragment . 
	 */
	public void onDestroy () {
		super.onDestroy();
		
		
		
		PlayupLiveApplication.callUpdateOnFragments( null ); 
	}

	@Override
	public void onAgainActivated(Bundle args) {


	}

	@Override
	public void onUpdate( Message msg ) {

	}

	
/**
 * For HMAC Purpose call the root API to get the Credentials
 */
	@Override
	public void onConnectionChanged(boolean isConnectionActive) {
		/**
		 * Called Automatically when device internet connection is on/off
		 */
		if ( isConnectionActive ) {

			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			
			if(!dbUtil.isCredentialAvailable()){
				
				new Util().callRootApi();
				dbUtil = null;
				return;
				
			}
			
			String iId = dbUtil.getUserId();

			// means no primary user available
			if ( iId == null ) {
				new Util().getDataFromServer(false);
			}
			
			dbUtil = null;
			iId = null;
		}else{
			
			PlayupLiveApplication.showToast(com.playup.android.R.string.no_network);
		}




	}


	public void cancelRunnable () {
	}


	@Override
	public void onResume(){
		super.onResume();
		
		
	}

	@Override
	public void onPause() {
		super.onPause();

		cancelRunnable();
	}
}
