package com.playup.android.fragment;

import java.util.Hashtable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.FragmentManagerUtil;
import com.playup.android.util.Keys;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;


/**
 *  Splash Fragment
 */
public class SplashFragment extends MainFragment {

	private TimerTask splashTimerTask;
	private Timer splashTimer;



	@Override
	public void onStop () {
		super.onStop();

		if ( splashTimer != null ) {
			splashTimer.cancel();
			splashTimer = null;
		}

		if ( splashTimerTask != null) {
			splashTimerTask.cancel();
			splashTimerTask = null;
		}



	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate( R.layout.splash, container,false);
		LinearLayout layout =(LinearLayout) view.findViewById(R.id.splashMain);
		layout.setBackgroundColor(Color.BLACK);
		return view;
		

	}


	@Override
	public void onResume () {
		super.onResume();


		initialize ();

	}

	// setting the timer to run after 3 secs ( calling  the all sports fragment.)
	private void initialize () {
		
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		
		if(dbUtil.isCredentialAvailable()){
			
			splashTimerTask = new TimerTask() {
			public void run() {

				try {
					if ( PlayUpActivity.handler != null ) {
						PlayUpActivity.handler.post( new Runnable () {

							@Override
							public void run() {

								// calling the next fragment.
								moveToNextUI ();
							}

						});
					}
				} catch (Exception e) {
					Logs.show ( e );
				}


			}

		};
		if ( splashTimer == null ) {
			splashTimer = new Timer();
		} else {
			splashTimer.cancel();
			splashTimer = new Timer();
		}
		splashTimer.schedule( splashTimerTask , 2500 ); 
			
		}
			
		else
			return;



	}


	@Override
	public void onPause () {
		super.onPause();

		if ( splashTimerTask != null ) {
			splashTimerTask.cancel();
			splashTimerTask = null;
		}

		if ( splashTimer != null ) {
			splashTimer.cancel();
			splashTimer = null;	
		}

	}


	private void moveToNextUI () {

		try {
			
			Constants.isCurrent  = true;
			if ( splashTimerTask != null ) {
				splashTimerTask.cancel();
				splashTimerTask = null;
			}

			if ( splashTimer != null ) {
				splashTimer.cancel();
				splashTimer = null;	
			}


			// as two fragments need to be started at the same time we are calling startTransaction 
			// and endTransaction  to create fragments in just one transaction.

			// popping the fragment stack so as to remove this SPLASH fragment from the fragment to handle back keys.
			PlayupLiveApplication.getFragmentManagerUtil().popBackStack();



			PlayupLiveApplication.getFragmentManagerUtil().startTransaction();

			PlayupLiveApplication.getFragmentManagerUtil().setFragment ( "TopBarFragment", R.id.topbar  );

			new Thread(  new Runnable () {

				@Override
				public void run() {
					
					
					
					final Hashtable<String, List<String>> hotItemData = DatabaseUtil.getInstance().getStartingScreenData();
					
					//if what's hot data is available then show default fragment
					//else all sports fragment
					
					if ( PlayUpActivity.handler != null ) {
						PlayUpActivity.handler.post( new Runnable () {

							@Override
							public void run() {
								
								try {
								
										if(hotItemData != null && 
												((hotItemData.get("resource_url") != null && hotItemData.get("resource_url").size() > 0 
														&& hotItemData.get("resource_url").get(0).trim().length() > 0) || 
														(hotItemData.get("resource_href") != null && hotItemData.get("resource_href").size() > 0 
																&& hotItemData.get("resource_href").get(0).trim().length() > 0))){
											
										Bundle bundle = new Bundle();
										if(hotItemData.get("resource_href") != null && hotItemData.get("resource_href").size() > 0 && hotItemData.get("resource_href").get(0) !=   null && hotItemData.get("resource_href").get(0).trim().length() > 0){
											
											bundle.putString("vSectionUrl", hotItemData.get("resource_href").get(0) );
											bundle.putBoolean("isHref",true);
										}else if(hotItemData.get("resource_url") != null && hotItemData.get("resource_url").size() > 0 && hotItemData.get("resource_url").get(0) !=   null && hotItemData.get("resource_url").get(0).trim().length() > 0){
											
											bundle.putString("vSectionUrl", hotItemData.get("resource_url").get(0) );
											bundle.putBoolean("isHref",false);
										}

										
										
										PlayupLiveApplication.getFragmentManagerUtil().setFragment( "DefaultFragment",bundle);
									}else{
										
										PlayupLiveApplication.getFragmentManagerUtil().setFragment ( "AllSportsFragment" );
									}
									
									PlayupLiveApplication.getFragmentManagerUtil().endTransaction();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									Logs.show(e);
								}		
							}
							
						});
					}
					

				}

			}).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
		


	}
	
	@Override
	public void onUpdate(Message msg) {
		try {
			// TODO Auto-generated method stub
			super.onUpdate(msg);
			
			if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
				
				new Util().callRootApi();	
				
				
				
				
			}
			
			if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("moveToNextFragment")){
				if(PlayUpActivity.handler != null){
					
					PlayUpActivity.handler.post(new Runnable() {
						
						@Override
						public void run() {
							moveToNextUI();
							
						}
					});
					
				}
				
				
			}
		
		} catch (Exception e) {
			Logs.show(e);
		}
	}

	
}
