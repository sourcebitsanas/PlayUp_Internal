package com.playup.android.adapters;

import java.util.Hashtable;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;

import android.content.res.Resources.Theme;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Layout;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;

import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;




import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.service.MediaPlayerService;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;


public class RadioListPopUp extends DialogFragment implements OnClickListener{

	
	
	private String vContentId;
	LayoutInflater layoutInflater = null;
	View popupView = null;
//	public PopupWindow popupWindow = null;
	private ListView radioListView;
	private ImageView close;
	private ImageView pauseButton;
	private TextView radioName;
	private TextView radiodesc;
	private RelativeLayout bufferingLayout;
	private RelativeLayout timeLayout;
	private Hashtable<String, List<String>> stationList;
	private TextView timeTextView;
	private Hashtable<String, List<String>> currentlyPlaying	=	null;
	private TextView bufferingText;
	
	public RadioListPopUp() {
			
	}

	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		
	AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(PlayUpActivity.context);
//		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(PlayUpActivity.context,android.R.style.Theme_Black_NoTitleBar_Fullscreen));
		
		//Dialog dialog = new Dialog(PlayUpActivity.context);
		layoutInflater    = (LayoutInflater)PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		popupView = layoutInflater.inflate(R.layout.popup_window,null);  
		
		
		
	
	//	dialogBuilder.setView(popupView);
		
		//dialog.setContentView(popupView);
		
		
		
//		initializeMediaPlayer();
		
		Log.e("123","set ONcreateDialog >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  ");
		
		AlertDialog dialog = dialogBuilder.create();
		
		try {
			
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setView(popupView, 0, 0,0 ,0 );
			
			/*LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			dialog.getWindow().getContainer().setAttributes(params);*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return dialog;
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.e("123","onResume >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+vContentId);
		
		
		
		
		initializeViews();
		
		setListeners();
		setValues();
		
	}

	public RadioListPopUp(String vContentId) {
		
		Log.e("123","vContentdId >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+vContentId);
		
		this.vContentId = vContentId;
//		popupView = null;
//		layoutInflater    = (LayoutInflater)PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		initializeViews();
//		
//		setListeners();
		
	}

	private void setListeners() {
		close.setOnClickListener(this);
		pauseButton.setOnClickListener(this);
		
	}

	private void initializeMediaPlayer() {
		
		
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		
		if(PlayUpActivity.mediaPlayerService != null && PlayUpActivity.mediaPlayerService.isPlaying()){
			
			PlayUpActivity.mediaPlayerService.startTimer();
			
			Hashtable<String, List<String>> currentRadio = dbUtil.getCurrentRadioForLeague(vContentId);
			
			Log.e("123","currentRadio >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+currentRadio);
			
			if(currentRadio != null && currentRadio.get("vRadioId") != null && currentRadio.get("vRadioId").size() > 0){
				radioName.setText(currentRadio.get("vRadioTitle").get(0));
				radiodesc.setText(currentRadio.get("vRadioSubTitle").get(0));
				pauseButton.setTag(R.id.aboutScrollView, false);
				pauseButton.setTag(R.id.about_txtview, currentRadio.get("vRadioStationUrl").get(0));				
				pauseButton.setTag(R.id.acceptedIgnore, currentRadio.get("vRadioId").get(0));
				
				
				
			}else{
				radioName.setText("Welcome To Playup");
				radiodesc.setText("About Us");
				pauseButton.setTag(R.id.aboutScrollView, true);
				pauseButton.setTag(R.id.about_txtview, "dummyUrl");
			}
			
			
		}else{
			if(PlayUpActivity.mediaPlayerService == null){
			//mediaPlayerService is not yet started
				
				radioName.setText(stationList.get("vRadioTitle").get(0));
				radiodesc.setText(stationList.get("vRadioSubTitle").get(0));
				pauseButton.setTag(R.id.aboutScrollView, false);
				pauseButton.setTag(R.id.about_txtview, stationList.get("vRadioStationUrl").get(0));
				pauseButton.setTag(R.id.acceptedIgnore, stationList.get("vRadioId").get(0));
				
				Intent startMediaPlayer = new Intent(PlayUpActivity.context,MediaPlayerService.class);
				startMediaPlayer.putExtra("vRadioUrl", stationList.get("vRadioStationUrl").get(0));
				startMediaPlayer.putExtra("isDefault", false);
				PlayUpActivity.context.startService(startMediaPlayer);
				
				
				
			}else if(PlayUpActivity.mediaPlayerService != null && PlayUpActivity.mediaPlayerService.isServiceStarted){
				
				
				
				radioName.setText(stationList.get("vRadioTitle").get(0));
				radiodesc.setText(stationList.get("vRadioSubTitle").get(0));
				pauseButton.setTag(R.id.aboutScrollView, false);
				pauseButton.setTag(R.id.about_txtview, stationList.get("vRadioStationUrl").get(0)); 
				pauseButton.setTag(R.id.acceptedIgnore, stationList.get("vRadioId").get(0));
				
//				if(PlayUpActivity.mediaPlayerService.isPaused){
//					
//					PlayUpActivity.mediaPlayerService.play();
//					
//				}else
				{
					PlayUpActivity.mediaPlayerService.resetPlayer(stationList.get("vRadioStationUrl").get(0));
				}
				
				
				
				
				
				
			}
			
			dbUtil.setIsPlaying(stationList.get("vRadioId").get(0));
			
			
		}
		
	}

	
	
	private void initializeViews() {

		
          
// 		popupWindow = new PopupWindow(popupView,200,275);
// 		popupWindow.setBackgroundDrawable(new BitmapDrawable());
//		popupWindow.setFocusable(true);
//		popupWindow.setOutsideTouchable(false);
//		popupWindow.setTouchInterceptor(new OnTouchListener() {
			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				if(event.getAction() ==  MotionEvent.ACTION_DOWN){
//					return true;
//					
//					
//					
//				}
//				
//				if(event.getAction() ==  MotionEvent.ACTION_UP){
//					return true;
//					
//					
//					
//				}
				
				
				
//					popupWindow.dismiss();
//				 AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//			 case KeyEvent.KEYCODE_VOLUME_UP:
//		            if (action == KeyEvent.ACTION_DOWN) {
//		                audio.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
//		            }
//		            return true;
//		        case KeyEvent.KEYCODE_VOLUME_DOWN:
//		            if (action == KeyEvent.ACTION_DOWN) {
//		                audio.adjustStreamVolume(AudioManager.STREAM_VOICE_CALL, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
//		            }
//		            return true;
//					
////				return false;
//			}
//		});
		 radioListView	=	(ListView) popupView.findViewById(R.id.radioList);
		 close	=	(ImageView) popupView.findViewById(R.id.close);
		 pauseButton = (ImageView)popupView.findViewById(R.id.pause);
			radioName = (TextView) popupView.findViewById(R.id.radioName);
			radiodesc = (TextView) popupView.findViewById(R.id.radioDesc);
			radioName.setTypeface(Constants.OPEN_SANS_REGULAR);
			radiodesc.setTypeface(Constants.OPEN_SANS_LIGHT);
			bufferingLayout = (RelativeLayout) popupView.findViewById(R.id.bufferingLayout);
			timeLayout = (RelativeLayout) popupView.findViewById(R.id.timeMain);
			
			timeTextView = (TextView)popupView.findViewById(R.id.time);
			timeTextView.setTypeface(Constants.OPEN_SANS_LIGHT);
			
			bufferingText	=	(TextView) popupView.findViewById(R.id.bufferingText);
			bufferingText.setTypeface(Constants.OPEN_SANS_LIGHT);
	}
	

	public void setValues(){
		
		
		new Thread(new Runnable() {
			
			

			@Override
			public void run() {
				
				
				
	             DatabaseUtil dbUtil = DatabaseUtil.getInstance();
	          
	             stationList	=	dbUtil.getRadioStaionsData(vContentId);
	             final String vCurrentRadioId = dbUtil.getCurrentRadioId();
	             
	             
	             	
	             	if(PlayUpActivity.handler != null){
	             		
	             		PlayUpActivity.handler.post(new Runnable() {
							
							@Override
							public void run() {
								
			    	             
			    	             
								initializeMediaPlayer();
			    	            
			    	             
			    	         	if(stationList	!= null && stationList.get("vRadioId") != null && 

			    	/*         			stationList.get("vRadioId").size() > 0 ){
				             		radioListView.setAdapter(new RadioStationListAdapter(stationList));
				             	}
							if (Constants.DENSITY.equalsIgnoreCase("high")) {
								
								popupWindow.showAtLocation(popupView, 0, 0, 0);
								popupWindow.update(25, 75, 430, 690);
								Log.e("123", "density of view----------------"+ Constants.DENSITY);
								
							} else if (Constants.DENSITY.equalsIgnoreCase("medium")) {
								
								popupWindow.showAtLocation(popupView, 0, 0, 0);
								popupWindow.update(25, 75, 430, 690);
								Log.e("123", "density of view----------------"+ Constants.DENSITY);
								
							} else if (Constants.DENSITY.equalsIgnoreCase("low")) {
								
								popupWindow.showAtLocation(popupView, 0, 0, 0);
								popupWindow.update(25, 75, 200, 275);
								Log.e("123", "density of view----------------"+ Constants.DENSITY);
								
							} else if (PlayUpActivity.isXhdpi) {
								
								popupWindow.showAtLocation(popupView, 0, 0, 0);
								popupWindow.update(25, 75, 430, 720);
								Log.e("123", "density of view----------------"+ Constants.DENSITY);
								
							}
*/
			    	         			stationList.get("vRadioId").size() > 0){
			    	         		if(vCurrentRadioId != null && vCurrentRadioId.trim().length() > 0)
			    	         			radioListView.setAdapter(new RadioStationListAdapter(stationList,vCurrentRadioId));
			    	         		else
			    	         			radioListView.setAdapter(new RadioStationListAdapter(stationList));
				             	}
//			    	             popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

								
							}
						});
	             		
	             	
	             	}
	             
	            
				
			}
		}).start();
		
	     		
		
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
		
			case R.id.close:
				dismiss();
				
//				AlertDialog.Builder dialog = new AlertDialog.Builder(PlayUpActivity.context);
				
				
				
//				if(popupWindow != null && popupWindow.isShowing()){
//					popupWindow.dismiss();
//					PlayUpActivity.popUp = null;
//					PlayUpActivity.mediaPlayerService.stopTimer();
//				}
				
				break; 
				
				
			case R.id.pause:
				
				
				
				
				
				
				if(PlayUpActivity.mediaPlayerService != null)
					Log.e("123"," MediaPlayerService.isServiceStarted  >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+PlayUpActivity.mediaPlayerService.isServiceStarted);
					
				if(PlayUpActivity.mediaPlayerService != null && PlayUpActivity.mediaPlayerService.isServiceStarted){
						
						
						if(PlayUpActivity.mediaPlayerService.isPaused){	
							
							PlayUpActivity.mediaPlayerService.play();
						}else{
							
							PlayUpActivity.mediaPlayerService.pause();
							
						}
						
						
					}
					
					
				
				
				
				
				break;
			
			
		}
			
			
		
	}


	public void updateTime(String time) {
		bufferingLayout.setVisibility(View.GONE);
		timeLayout.setVisibility(View.VISIBLE);		
		timeTextView.setText(time);
		
	}


	public void showBuffering() {
		bufferingLayout.setVisibility(View.VISIBLE);
		timeLayout.setVisibility(View.GONE);
		
	}


		
	
	
//	public boolean isShowingPopUpWindow(){
//		if(popupWindow.isShowing()){
//			return true;
//		}else {
//			return false;
//		}
//		
//		
//	}
//	
//	public void dismissPopUpWindow(){
//		popupWindow.dismiss();
//	}
	
}
