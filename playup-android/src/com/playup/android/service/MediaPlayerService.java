package com.playup.android.service;

import java.util.Timer;
import java.util.TimerTask;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.Logs;

import android.app.Notification;

import android.app.PendingIntent;
import android.app.Service;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;


import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class MediaPlayerService extends Service implements OnPreparedListener,
OnCompletionListener,OnErrorListener,OnBufferingUpdateListener,OnInfoListener{
//	private final IBinder mBinder = new ServiceBinder();
	private  MediaPlayer mediaPlayer;
	private  String vRadioName = "";
	private  Timer playerTimer;
	private  TimerTask playerTimerTask;
	private  boolean isPreparing;
	public  boolean isServiceStarted = false;
	public  boolean isPaused;
	
	
//	public class ServiceBinder extends Binder {
//   	 public MediaPlayerService getService()
//  	 {
//  		return MediaPlayerService.this;
//  	 }
//  }
	
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.e("123","inside on create of service >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  ");
		PlayUpActivity.mediaPlayerService = this;
		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnCompletionListener(this);
		mediaPlayer.setOnErrorListener(this);
		mediaPlayer.setOnInfoListener(this);
		mediaPlayer.setOnBufferingUpdateListener(this);
		isServiceStarted = false;
		isPaused = false;
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
//		return mBinder;
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		try {
			
			
			
			if(mediaPlayer != null && intent != null && intent.getStringExtra("vRadioUrl") != null
					&& intent.getStringExtra("vRadioUrl").trim().length() > 0){
				
				
				Log.e("123","inside intent.getStringExtra(vRadioUrl) of service >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+intent.getStringExtra("vRadioUrl"));
				

				
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				
				if(intent.getBooleanExtra("isDefault", true)){
		
				

					
//				AssetFileDescriptor afd = PlayupLiveApplication.getInstance().getAssets().openFd("default.mp3");
//				mediaPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
				    
				}else{
					mediaPlayer.setDataSource(intent.getStringExtra("vRadioUrl"));
					
				}
				
				
			    
				
				mediaPlayer.prepareAsync();
				isPreparing = true;
				
				
				
				
				Message m = new Message();
				m.obj = "ShowBuffering";
				PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(m);
				
			}
			
			isServiceStarted = true;
			
		} catch (Exception e) {
			Logs.show(e);
		} 
		
		
		return START_NOT_STICKY;
	}
	
	
	public void resetPlayer(String vRadioUrl){
		
		isPaused = false;
		try {
			if(vRadioUrl != null && vRadioUrl.trim().length() > 0){
				
				
				if(mediaPlayer != null)
					mediaPlayer.reset();
				
				
				
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
				
				
				mediaPlayer.setDataSource(vRadioUrl);
				
				mediaPlayer.prepareAsync();
				
				isPreparing = true;
				
				Message m = new Message();
				m.obj = "ShowBuffering";
				PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(m);
				
			}
		} catch (Exception e) {
			Logs.show(e);
		} 
		
		
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		
		mediaPlayer.stop();
		mediaPlayer.release();
		stopSelf();
		PlayUpActivity.mediaPlayerService = null;
		isServiceStarted = false;
		stopTimer();
		
		
		
		
	}
	
	
	@Override
	public void onDestroy() {
		Log.e("123","onDestroy >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+isPreparing);
		super.onDestroy();
		mediaPlayer.release();
		stopTimer();
	}
	
	public  void pause(){
		isPaused = true;
		Log.e("123","isPreparing pause >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+isPreparing);
		if(!isPreparing){
			mediaPlayer.pause();
			stopTimer();
		stopForeground(true);

		}
		
		
		
	}
	
	

	public void play(){
		isPaused = false;
		Log.e("123","isPreparing play >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+isPreparing);
		if(!isPreparing){
			mediaPlayer.start();
			createNotification();
			startTimer();
		}
			
	}

	
	
	
	private  void createNotification() {
		
		Log.e("123","inside createNotification >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
		
		if(PlayUpActivity.context != null){
			
			
			
			Log.e("123","inside createNotification PlayUpActivity.context != null >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
		
			

		PendingIntent pi = PendingIntent.getActivity(PlayUpActivity.context, 0,null, PendingIntent.FLAG_CANCEL_CURRENT);
		Notification notification = new Notification();
		notification.tickerText = vRadioName;
		notification.icon = R.drawable.notification_green_dot;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.setLatestEventInfo(PlayUpActivity.context, "RadioPlaying",
		                "Playing" , pi);
		startForeground(Constants.NOTIFICATION_ID, notification);
		}
		
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		isPreparing = false;
		
		Log.e("123","inside onPrepared >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
		
		
		
		if(!isPaused){
			mediaPlayer.start();	
			startTimer();
			createNotification();
		}
		
	}
	
	public void stopTimer() {
		
		Log.e("123","stopTimer >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  ");
		
		if(playerTimer != null){
			
			Log.e("123","playerTimer != null >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  ");
			
			playerTimer.cancel();
			playerTimer = null;
		}
		
		if(playerTimerTask != null){
			
			Log.e("123","playerTimerTask != null >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  ");
			
			playerTimerTask.cancel();
			playerTimerTask = null;
		}
		
	}


	public void startTimer() {
		try {
			if(playerTimer == null)
				playerTimer = new Timer();
			
			
			if(playerTimerTask == null){
					 playerTimerTask = new TimerTask() {
						
						@Override
						public void run() {
							
							long tempMiliSec = mediaPlayer.getCurrentPosition();
							StringBuffer buf = new StringBuffer();
			
						    int hours = (int) (tempMiliSec / (1000 * 60 * 60));
						    int minutes = (int) ((tempMiliSec % (1000 * 60 * 60)) / (1000 * 60));
						    int seconds = (int) (((tempMiliSec % (1000 * 60 * 60)) % (1000 * 60)) / 1000);
			
						    buf.append(String.format("%02d", hours)).append(":")
						        .append(String.format("%02d", minutes)).append(":")
						        .append(String.format("%02d", seconds));
//						    Log.e("123","time >>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+buf);
						    
						    
						    Message m = new Message();
						    m.obj = "UpdateTime";
						    Bundle b = new Bundle();
						    b.putString("time", ""+buf);
						    m.setData(b);
						    PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(m);
						}
					};
					
					
					playerTimer.scheduleAtFixedRate(playerTimerTask, 0, 500);
			}
		} catch (Exception e) {
			Logs.show(e);
		}
	    
		
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e("123","inside errrrrrrrrrrrrrrrrrrrorrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr what "+what);
		Log.e("123","inside errrrrrrrrrrrrrrrrrrrorrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr extra "+extra);		
		mp.reset();
		stopTimer();
		return true;
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		
		
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		
		Log.e("123","inside infoooooooooooooooooooooooooooooooooooooooooooooooo what "+what);
		Log.e("123","inside infoooooooooooooooooooooooooooooooooooooooooooooooo extra "+extra);		
		
		if(what == MediaPlayer.MEDIA_INFO_BUFFERING_START){
			
			stopTimer();
			
			Message m = new Message();
			m.obj = "ShowBuffering";
			PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(m);
			
			return true;
			
		}else if(what == MediaPlayer.MEDIA_INFO_BUFFERING_END){
			
			startTimer();			
			
			return true;
		}
		
		
		return false;
	}

	public boolean isPlaying() {
		// TODO Auto-generated method stub
		return mediaPlayer.isPlaying();
	}
	
	
	

}
