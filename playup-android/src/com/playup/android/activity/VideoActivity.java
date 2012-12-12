package com.playup.android.activity;

import com.playup.android.R;
import com.playup.android.application.PlayupLiveApplication;

import com.playup.android.util.Logs;
import com.playup.android.util.Util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources.NotFoundException;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;


import android.view.KeyEvent;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends Activity {
	private ProgressDialog progressDialog;
	private VideoView videoView;
	public static VideoActivity context;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.video_view);
		videoView = (VideoView) findViewById( R.id.videoView);
		initialize () ;
		
	
			
	}		
				
		
			
	
	
	
			
		@Override
		protected void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
			
			context = this;
			
			
		}
		
	
		private void initialize () {
			try{

				

				
				
				if ( !Util.isInternetAvailable() ) {

					PlayupLiveApplication.showToast( R.string.no_network );
					
					return;
				}
				
				
			 
			 
			 showProgressDialog();
			 
			 Intent i = getIntent();
				
				if(i.getExtras() != null && i.getExtras().getString("videoUrl") != null){
					
					String url = i.getExtras().getString("videoUrl");
					
					
			
				
					
				
				    final Uri uri=Uri.parse(url);

				   
				    videoView.setVideoURI(uri);
				    
				    videoView.setOnPreparedListener(new OnPreparedListener() {
						
						@Override
						public void onPrepared(MediaPlayer mp) {
							cancelProgress();
						
							
						}
					});
				   
					
					
				
				videoView.setOnErrorListener(new OnErrorListener() {
					
					@Override
					public boolean onError(MediaPlayer mp, int what, int extra) {
						
						

						
						if ( progressDialog != null && progressDialog.isShowing()) {
							progressDialog.dismiss();
							progressDialog = null;
						}	
						return false;
					}
				});
			

		        videoView.setMediaController(new MediaController(VideoActivity.this));
		         videoView.requestFocus();
		         videoView.start();
		         
		         
		       
		         
					}
			

			}

					
					

				
			catch(Exception e){
				Logs.show(e);
			}


	}
	
	private void showProgressDialog () {

		try {
			if( !Util.isInternetAvailable() ) {
				return;
			}
			
			if ( progressDialog == null ) {
				progressDialog = new ProgressDialog(  VideoActivity.this  );
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setMessage( getResources().getString(R.string.loading));
				progressDialog.setCancelable(true);
				progressDialog.setOnCancelListener(new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
					}
				});
			} 
			progressDialog.show();
		} catch (Exception e) {
			
			Logs.show(e);
			
		}
	}

	
	private void cancelProgress() {
		try {
			
			

				if ( progressDialog != null ) {
					progressDialog.cancel();
					progressDialog = null;
				}	


		}catch (Exception e) {
			Logs.show ( e );
		}
	}

	
	@Override
	protected void onPause() {
		
		super.onPause();
		try {
			
			if(progressDialog!=null && progressDialog.isShowing()){
				progressDialog.dismiss();
			}
//			videoView.pause();
			
			
		} catch (Exception e) {
			Logs.show(e);
		}
	}
	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		try {
			if ( videoView != null ) {
				if(videoView.isPlaying()){
					
					videoView.stopPlayback();
				}
			}
		} catch (Exception e) {
			Logs.show(e);
		}
	}
	
	/**
	 * handling the back button especially to handle the life cycle of the
	 * fragment.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {


		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			finish();
			return true;
		} else {
			super.onKeyDown(keyCode, event );
			return false;
		}
	}
	
}
