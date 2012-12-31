package com.playup.android.fragment;


import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.NotificationAdapter;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Keys;

import com.playup.android.util.PreferenceManagerUtil;


/**
 * Notification Fragment. 
 * 
 */
public class NotificationFragment extends MainFragment implements OnClickListener {
	
	
	private RelativeLayout anonymous_notification;
	private LinearLayout noNotification;

	private ListView notificationListView;
	private NotificationAdapter notificationAdapter;

	private SharedPreferences notificationSharedPreferences;
	private ImageView blueDot;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;

	private static int currentPosition = 0;
	
	RelativeLayout content_layout;
	Hashtable<String, List< String > >  notificationData;
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		
		content_layout = (RelativeLayout) inflater.inflate( R.layout.notification, null);
		
		
		return content_layout;
	}
	
	
	
	@Override
	public void onResume () {
		super.onResume();

		initialize( content_layout );
		notificationAdapter = null;

		
		setTopBar();
		// set values 
		setValues();
	}

	
	@Override
	public void onPause() {
		super.onPause();
		
		//maintaining the current scrollposition of listview
		
		currentPosition = notificationListView.getFirstVisiblePosition();
	}
	
	
	/**
	 * setting  all the content in the views 
	 */
	private void initialize ( final  RelativeLayout content_layout ) {
		
		// initialize views
		initializeViews(content_layout);
		
		// set listeners
		setListeners();

	}
	/**
	 * initializing the views
	 * @param content_layout
	 */
	
	private void initializeViews ( RelativeLayout content_layout) {
		
		notificationListView = ( ListView )content_layout.findViewById( R.id.notificationList);
		anonymous_notification = (RelativeLayout) content_layout.findViewById( R.id.anonymous_notification );
		noNotification = (LinearLayout) content_layout.findViewById( R.id.nonotification );
		blueDot = (ImageView) content_layout.findViewById(R.id.blueDot);
		noNotification.setVisibility( View.GONE );
		anonymous_notification.setVisibility( View.GONE );
		notificationSharedPreferences = PlayupLiveApplication.getInstance().getSharedPreferences( Keys.PREFERENCE_NAME , Context.MODE_PRIVATE);
		
		if( notificationSharedPreferences.getBoolean(Constants.IS_ANONYMOUS_NOTIFICATIONS_VIEWED, false) ) {
			blueDot.setVisibility(View.GONE);
		} else {
			blueDot.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * setting the listeners
	 */
	
	private void setListeners () {
		
		anonymous_notification.setOnClickListener( this );
	}
	
	
	/**
	 * setting the color and title in the topbar
	 */
	private void setTopBar() {

		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result = dbUtil.getUserNotificationUrl();
					String notificationUrl = (String) result.get("url");
					//Boolean isHref = (Boolean) result.get("isHref");
					
					
					String vChildColor = dbUtil.getSectionMainColor("", notificationUrl);
					String vChildTitleColor = dbUtil.getSectionMainTitleColor("", notificationUrl);
					
					if(vChildColor != null && vChildColor.trim().length() > 0 )
						vMainColor = vChildColor;
					
					if(vChildTitleColor != null && vChildTitleColor.trim().length() > 0 )
						vMainTitleColor = vChildTitleColor;
					
					

					String vChildSecondaryColor = dbUtil.getSectionSecondaryColor ("", notificationUrl);
					String vChildSecondaryTitleColor = dbUtil.getSectionSecondaryTitleColor ( "", notificationUrl );
					
					 
					 if(vChildSecondaryColor != null && vChildSecondaryColor.trim().length() > 0)
						 vSecColor = vChildSecondaryColor;
					 
					 if(vChildSecondaryTitleColor != null && vChildSecondaryTitleColor.trim().length() > 0)
						 vSecTitleColor = vChildSecondaryTitleColor;
					
						
					
					Bundle b = new Bundle();
					b.putString("vMainColor",vMainColor );
					b.putString("vMainTitleColor",vMainTitleColor );
					Message msg = new Message ();
					msg.setData(b);
					
					
					PlayupLiveApplication.callUpdateTopBarFragments(msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
				
			}
		}).start();
					
	}

			
	/**
	 * calling the respective functions to fetch and display data on the various parts of the screen
	 */
	
	private void setValues () {
		
		try {
			
			new Thread( new Runnable() {
				
				@Override
				public void run() {
					
					try {
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						

						notificationData = dbUtil.getUserNotificationData ();
						
						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable() {
								
								@Override
								public void run() {
									
									try {
										if ( !isVisible() ) {
											return;
										}
										
										if ( notificationData == null ) {
											showNoNotification();
										} else if ( notificationData.get( "vNotificationId" ).size() == 0 ) {
										
											showNoNotification();
										} else {
											showNotificationList ( notificationData );
										}
									} catch (Exception e) {
										// TODO Auto-generated catch block
										//Logs.show(e);
									}
									
								}
							});
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//Logs.show ( e );
					}
					
					
				}
			}).start();

		} catch ( Exception e ){
			//Logs.show( e );
		}
		
	}
	
	/**
	 *  setting the visibility of the views depending on whether the user is logged in/anonymous
	 *  
	 *  
	 */
	private void showNoNotification () {
		
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					if ( dbUtil.isUserAnnonymous() ) {
						
						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable() {
								
								@Override
								public void run() {
									
									try {
										if ( !isVisible() ) {
											return;
										}
										anonymous_notification.setVisibility( View.VISIBLE );
										
										noNotification.setVisibility( View.GONE );
										notificationListView.setVisibility( View.GONE );
									} catch (Exception e) {
										// TODO Auto-generated catch block
									//	Logs.show(e);
									}
									
								}
							});
						}
						
					} else {
						
						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable() {
								
								@Override
								public void run() {
									
									try {
										if ( !isVisible() ) {
											return;
										}
										anonymous_notification.setVisibility( View.GONE );
										noNotification.setVisibility( View.VISIBLE );
										notificationListView.setVisibility( View.GONE );
									} catch (Exception e) {
										// TODO Auto-generated catch block
									//	Logs.show(e);
									}
								}
							});
						}
						
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}
				
			}
		}).start();
		
	}
	/**
	 * displaying the notifications
	 * @param data
	 */
	private void showNotificationList ( Hashtable<String, List< String > >  data ) {
		
		anonymous_notification.setVisibility( View.GONE );
		noNotification.setVisibility( View.GONE );
		notificationListView.setVisibility( View.VISIBLE );
		
		if ( notificationAdapter == null ) {
			notificationAdapter = new NotificationAdapter( data,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor  );
			notificationListView.setAdapter ( notificationAdapter);
			notificationListView.setSelection(currentPosition);
		} else {
			notificationAdapter.setData ( data ,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor);
		}
	}
	
	
	/**
	 * handling data if any change in database has occured. 
	 */
	@Override 
	public void onUpdate ( final Message msg ) {

		
		if ( PlayUpActivity.handler != null ) {
			
			PlayUpActivity.handler.post( new Runnable () {

				@Override
				public void run() {
					try {
						if ( !isVisible() ) {
							 return;
						 }
						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "NotificationError" ) ) {
							
							//PlayupLiveApplication.showToast( R.string.notification_err );
						}
						
						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "GapNotifications") && msg.arg1 == 1 ) {
							
							//PlayupLiveApplication.showToast( R.string.notification_err );
						}
						if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
							
							showNoNotification();
						}
						
						
						// refresh the values .
						setValues();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//Logs.show ( e );
					}
				}
				
			});
		}
	}


	@Override
	public void onClick(View v) {
		PreferenceManagerUtil preferenceManagerUtil = new PreferenceManagerUtil();
		preferenceManagerUtil.set( Constants.IS_ANONYMOUS_NOTIFICATIONS_VIEWED,true);
		preferenceManagerUtil = null;
		
		PlayupLiveApplication.getFragmentManagerUtil().setFragment( "MyProfileFragment" );
			
	}
	
}
