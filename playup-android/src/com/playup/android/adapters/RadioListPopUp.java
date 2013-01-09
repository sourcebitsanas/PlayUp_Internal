package com.playup.android.adapters;

import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
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

public class RadioListPopUp implements OnClickListener{

	
	
	private String vContentId;
	LayoutInflater layoutInflater = null;
	View popupView = null;
	public PopupWindow popupWindow = null;
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


	public RadioListPopUp(String vContentId) {
		
		Log.e("123","vContentdId >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+vContentId);
		
		this.vContentId = vContentId;
		popupView = null;
		layoutInflater    = (LayoutInflater)PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		initializeViews();
		
		setListeners();
		
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
				
				if(PlayUpActivity.mediaPlayerService.isPaused){
					
					PlayUpActivity.mediaPlayerService.play();
					
				}else{
					PlayUpActivity.mediaPlayerService.resetPlayer(stationList.get("vRadioStationUrl").get(0));
				}
				
				
				
				
				
				
			}
			
			dbUtil.setIsPlaying(stationList.get("vRadioId").get(0));
			
			
		}
		
	}

	
	
	private void initializeViews() {
		popupView = layoutInflater.inflate(R.layout.popup_window, null);  
		
 		popupWindow = new PopupWindow(popupView);
 		popupWindow.setWidth(LayoutParams.WRAP_CONTENT);
 		popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
 		popupWindow.setBackgroundDrawable(new BitmapDrawable());
	 	popupWindow.setFocusable(true);
	 	popupWindow.setOutsideTouchable(false);
	 	
	 	//popupWindow = new PopupWindow(popupView,200,275);
		//popupWindow.setFocusable(true);
		
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

	public void show(){
		
		
		new Thread(new Runnable() {
			
			

			@Override
			public void run() {
				
				
				
	             DatabaseUtil dbUtil = DatabaseUtil.getInstance();
	          
	             stationList	=	dbUtil.getRadioStaionsData(vContentId);
	            
	             
	             
	             	
	             	if(PlayUpActivity.handler != null){
	             		
	             		PlayUpActivity.handler.post(new Runnable() {
							
							@Override
							public void run() {
								
			    	             
			    	             
								initializeMediaPlayer();
			    	            
			    	             
			    	         	if(stationList	!= null && stationList.get("vRadioId") != null && 
			    	         			stationList.get("vRadioId").size() > 0 ){
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
				if(popupWindow != null && popupWindow.isShowing()){
					popupWindow.dismiss();
					PlayUpActivity.popUp = null;
					PlayUpActivity.mediaPlayerService.stopTimer();
				}
				
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
