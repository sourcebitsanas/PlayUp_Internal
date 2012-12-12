package com.playup.android.fragment;

import java.util.HashMap;

import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;


import android.util.Log;
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

import com.flurry.android.FlurryAgent;
import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

public class MyProfileFragment extends MainFragment  implements OnClickListener, OnTouchListener {

	

	// user views
	private TextView userNameTextView ;
	private ImageView avatarImageView;
	private TextView userIdTextView;

	private TextView connectionMsg;
	private TextView socialTitle;

	private LinearLayout myProviders;
	private RelativeLayout logoutRelativeLayout;
	private RelativeLayout  updateRegionRelativeLayout;
	private RelativeLayout connected_userview;
	private RelativeLayout disconnected_userview;


	private boolean isUserAnonymous = true;

	private boolean isSet = false;
	private ImageDownloader imageDownloader = new ImageDownloader();


	RelativeLayout content_layout;
	private TextView regionCode;
	

	LayoutParams params = new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT );
	android.widget.RelativeLayout.LayoutParams relParams;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


		content_layout = (RelativeLayout) inflater.inflate( R.layout.myprofile, null );



		return content_layout;
	}

	@Override
	public void onResume () {
		try {
			super.onResume();

			if( imageDownloader == null ) {
				imageDownloader = new ImageDownloader();
			}



			initializeViews ( content_layout );
			isSet = false;

			isProviderHandlerSet = false;
			isProviderSet = false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}

	@Override
	public void onPause () {
		super.onPause();
		isSet = false;


		isProviderHandlerSet = false;
		isProviderSet = false;
	}
	/**
	 * setting all the content in the views
	 */
	private void initializeViews ( RelativeLayout content_layout ) {

		try {
			// initialize views


			avatarImageView = ( ImageView ) content_layout.findViewById( R.id.profile_image );
			userNameTextView = ( TextView ) content_layout.findViewById( R.id.user_name );
			userIdTextView           = ( TextView ) content_layout.findViewById( R.id.user_id );

			// anonymous/ registered views
			connected_userview = (RelativeLayout) content_layout.findViewById(R.id.connected_userview);
			disconnected_userview = (RelativeLayout) content_layout.findViewById(R.id.disconnected_userview);

			connectionMsg = (TextView) content_layout.findViewById(R.id.connectionMsg);
			socialTitle = (TextView) content_layout.findViewById(R.id.mysocial_title);


			regionCode = (TextView) content_layout.findViewById(R.id.regionCode);
		


			myProviders = (LinearLayout) content_layout.findViewById ( R.id.myprovider );
			// logout
			logoutRelativeLayout = (RelativeLayout) content_layout.findViewById ( R.id.logout_view );
			updateRegionRelativeLayout = (RelativeLayout) content_layout.findViewById ( R.id.updateRegionView );

			if( Constants.DENSITY.equalsIgnoreCase("medium")  ) {

				LayoutParams relParams =  (LayoutParams) logoutRelativeLayout.getLayoutParams();
				relParams.height = 65;
				logoutRelativeLayout.setLayoutParams(relParams);

				relParams =  (LayoutParams)  updateRegionRelativeLayout.getLayoutParams();
				relParams.height = 65;
				updateRegionRelativeLayout.setLayoutParams(relParams);

			} else if ( Constants.DENSITY.equalsIgnoreCase("low") ) {

				LayoutParams relParams =  (LayoutParams) logoutRelativeLayout.getLayoutParams();
				relParams.height = 50;
				logoutRelativeLayout.setLayoutParams(relParams);

				relParams =  (LayoutParams)  updateRegionRelativeLayout.getLayoutParams();
				relParams.height = 50;
				updateRegionRelativeLayout.setLayoutParams(relParams);

			}

			getRegions();

			// setting the typefaces
			setTypeFaces ();

			// set listeners
			setListeners();

			// set values
			setValues( );
			
			refreshMyProfile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}


	}
	/**
	 * making api call to get the list of regions to set the user region
	 */
	private void getRegions() {


		try {
			//Praveen : Changedd
			if(runnableList != null && !runnableList.containsKey(Constants.GET_REGIONS))
				runnableList.put(Constants.GET_REGIONS,new Util().getRegionData(runnableList));;
		} catch (Exception e) {

			Logs.show ( e );
		}


	}


	/**
	 * Setting Typeface values
	 */
	private void setTypeFaces ()  {

		try {
			regionCode.setTextColor(Color.parseColor("#27A544"));
			regionCode.setTypeface(Constants.OPEN_SANS_REGULAR);
			connectionMsg.setTypeface(Constants.OPEN_SANS_REGULAR);
			socialTitle.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
			
			( ( TextView ) updateRegionRelativeLayout.findViewById( R.id.updateRegionTitile ) ).setTypeface(Constants.OPEN_SANS_SEMIBOLD);
			( ( TextView ) logoutRelativeLayout.findViewById( R.id.title ) ).setTypeface(Constants.OPEN_SANS_SEMIBOLD);

			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}

	/**
	 * Setting Listeners
	 */
	private void setListeners() {
		logoutRelativeLayout.setOnTouchListener( this );
		updateRegionRelativeLayout.setOnTouchListener( this );
	}

	/**
	 * Setting Values
	 */
	private void setValues( ) {
		try{
			if( !Util.isInternetAvailable() && isVisible()) {
				PlayupLiveApplication.showToast( R.string.no_network );
			}
		} catch (Exception e) {

		}

		if ( !Constants.isCurrent ) {
			return;
		}


		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					isUserAnonymous = dbUtil.isUserAnnonymous();

					dbUtil = null;	

					if(PlayUpActivity.handler != null){

						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {
								try {
									if(!isVisible())
										return;

									setUserRegion();
									if ( isUserAnonymous ) {
										Constants.menuMap.put("MyProfileFragment", -1 );
										logoutRelativeLayout.setVisibility( View.INVISIBLE );
									} else {
										Constants.menuMap.put("MyProfileFragment", R.menu.my_profile);
										logoutRelativeLayout.setVisibility( View.VISIBLE );
									}

									checkAnnonymousUser();


									if ( !isUserAnonymous ) {
										setUserData ();

									} else {
										setUserData ();
										setTopBarFragment (  getString( R.string.my_profile) );
									}

									if ( !isProviderSet ) {
										isProviderSet = true;
										setProviders ();
									} else {

										if ( !isProviderHandlerSet ) {
											isProviderHandlerSet = true;
											setProviders ();
										}
									}


								} catch (Exception e) {
									Logs.show(e);
								}

							}
						});

					}

				} catch (Exception e) {
					Logs.show(e);
				}	
			}
		}).start();



	}

	
	/**
	 * setting the user region 
	 */

	private void setUserRegion() {

		try {
			if ( Constants.RegionName != null ) {
				regionCode.setText( Constants.RegionName );
			}
				
			new Thread ( new Runnable( ) {

				@Override
				public void run() {

					try {
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						Constants.RegionName  = dbUtil.getSelectedRegionName();
						if(PlayUpActivity.handler != null){

							PlayUpActivity.handler.post(new Runnable() {

								@Override
								public void run() {
									try {
										if(!isVisible())
											return;

										if ( regionCode != null ) {
											regionCode.setText(Constants.RegionName);
										}
									} catch (Exception e) {
										Logs.show(e);
									}

								}
							});

						}

					} catch (Exception e) {

						Logs.show(e);
					}
				}
			}).start();
		} catch ( Exception e ) {
			Logs.show ( e ); 
		}





	}




	private boolean isProviderSet = false;
	private boolean isProviderHandlerSet = false;
	private Timer myProfileTimer;
	private TimerTask myProfileTimerTask;

	/**
	 * Setting the providers 
	 */
	private void setProviders() {
		
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					final DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					final Hashtable< String , List < String > > data = dbUtil.getProviders();

					if(PlayUpActivity.handler != null){

						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {
								try {

									if(!isVisible())
										return;
									myProviders.removeAllViews();
									myProviders.setVisibility(View.VISIBLE);



									if ( data != null && data.get( "vProviderName" ).size() > 0 ) {
										int len = data.get( "vProviderName" ).size();

										LayoutInflater inflater = LayoutInflater.from( PlayUpActivity.context );
										for ( int i = 0; i < len ; i++ ) {
											final RelativeLayout provider = ( RelativeLayout ) inflater.inflate( R.layout.my_profile_provider, null);

											if( Constants.DENSITY.equalsIgnoreCase("medium") ) {
												relParams = new RelativeLayout.LayoutParams( LayoutParams.FILL_PARENT, 65 );
												provider.setLayoutParams(relParams);
											} else if ( Constants.DENSITY.equalsIgnoreCase("low") ) {
												relParams = new RelativeLayout.LayoutParams( LayoutParams.FILL_PARENT, 50 );
												provider.setLayoutParams(relParams);
											}

											if ( i!=0 && ( i +1 ) < len ) {
												ImageView imgView = new ImageView( PlayUpActivity.context );
												imgView.setBackgroundResource( R.drawable.content_divider1 );
												params = new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT );
												params.setMargins( 3, 0, 3, 0 );
												imgView.setLayoutParams( params );
												myProviders.addView ( imgView );

												imgView = null;
											}

											if ( (i+1) == len ) {

												ImageView imgView = new ImageView( PlayUpActivity.context );
												imgView.setBackgroundResource( R.drawable.content_divider1 );
												params = new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT );
												params.setMargins( 3, 0, 3, 0 );
												imgView.setLayoutParams( params );

												myProviders.addView ( imgView );
												provider.setBackgroundResource( R.drawable.content_base );
												provider.setId( -2 );
												myProviders.addView ( provider );

												LinearLayout shadowLayout = new LinearLayout(PlayUpActivity.context);
												params = new LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT );
												params.setMargins( 20, 0, 20, 0 );
												shadowLayout.setLayoutParams( params );
												shadowLayout.setOrientation(1);		
												shadowLayout.setPadding( -15, -10, -15, 0 );
												shadowLayout.setBackgroundResource(R.drawable.drop_shadow);

												LinearLayout li = new LinearLayout( PlayUpActivity.context );
												li.setLayoutParams( params );
												li.setPadding( 3, 0, 3, 0);
												li.addView ( shadowLayout );
												( ( ImageView ) provider.findViewById( R.id.vertical_divider ) ).setPadding(0, 0,0,4);
												// myProviders.addView ( li );

											} else {
												myProviders.addView ( provider );
												provider.setId( i );
											}

											final TextView title = (TextView) provider.findViewById( R.id.title );
											title.setText( data.get( "vProviderName" ).get( i ) );

											title.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
											( ( TextView ) provider.findViewById( R.id.connectText ) ).setTypeface(Constants.OPEN_SANS_REGULAR);

											provider.setTag( data.get("vProviderName").get( i ) );
											// provider.setOnClickListener( providerClickListener );
											provider.setOnTouchListener( MyProfileFragment.this );

											final int i_temp = i;

											
											if ( dbUtil.isIdentifierProviderName( data.get( "vProviderName" ).get( i_temp ) ) ) {


															try {

																title.setText( data.get("vProviderName").get( i_temp ) );
																title.setTextColor(Color.parseColor("#28A645"));
																
																
																imageDownloader.download(  data.get( "vIconBroadcastUrl" ).get( i_temp ),  ( ImageView ) provider.findViewById( R.id.logo ), false,null  );
																imageDownloader.download( data.get( "vIconBroadcastHighLightUrl" ).get( i_temp ), null, false ,null );
																provider.findViewById( R.id.connectPlus ).setVisibility( View.GONE );
																provider.setTag(R.id.about_txtview,data.get( "vIconBroadcastUrl" ).get( i_temp ));

																( ( TextView ) provider.findViewById( R.id.connectText ) ).setText( getString( R.string.connected ) );

															} catch ( Exception e ) {
																Logs.show ( e );
															}


											}else{
													try {
															
																imageDownloader.download(  data.get( "vIconBroadcastUrl" ).get( i_temp ),null, false,null  );
																imageDownloader.download( data.get( "vIconBroadcastHighLightUrl" ).get( i_temp ),   ( ImageView ) provider.findViewById( R.id.logo ), false ,null );
																provider.setTag(R.id.about_txtview,data.get( "vIconBroadcastHighLightUrl" ).get( i_temp ));

															} catch ( Exception e ) {
																Logs.show ( e );
															}
											}

											
//											new Thread( new Runnable () {
//
//												@Override
//												public void run() {
//
//													if ( dbUtil.isIdentifierProviderName( data.get( "vProviderName" ).get( i_temp ) ) ) {
//
//														if ( PlayUpActivity.handler  != null ) {
//															PlayUpActivity.handler.post( new Runnable () {
//
//																@Override
//																public void run() {
//
//																	try {
//
//																		title.setText( data.get("vProviderName").get( i_temp ) );
//																		title.setTextColor(Color.parseColor("#28A645"));
//																		
//																		
//																		imageDownloader.download(  data.get( "vIconBroadcastUrl" ).get( i_temp ),  ( ImageView ) provider.findViewById( R.id.logo ), false,null  );
//																		imageDownloader.download( data.get( "vIconBroadcastHighLightUrl" ).get( i_temp ), null, false ,null );
//																		provider.findViewById( R.id.connectPlus ).setVisibility( View.GONE );
//																		provider.setTag(R.id.about_txtview,data.get( "vIconBroadcastUrl" ).get( i_temp ));
//
//																		( ( TextView ) provider.findViewById( R.id.connectText ) ).setText( getString( R.string.connected ) );
//
//																	} catch ( Exception e ) {
//																		Logs.show ( e );
//																	}
//
//																}
//
//															}); 
//														}
//
//													}else{
//														if ( PlayUpActivity.handler != null ) {
//															PlayUpActivity.handler.post( new  Runnable () {
//
//																@Override
//																public void run() {
//
//																	try {
//																		
//																		
//																		
//																		imageDownloader.download(  data.get( "vIconBroadcastUrl" ).get( i_temp ),null, false,null  );
//																		imageDownloader.download( data.get( "vIconBroadcastHighLightUrl" ).get( i_temp ),   ( ImageView ) provider.findViewById( R.id.logo ), false ,null );
//																		provider.setTag(R.id.about_txtview,data.get( "vIconBroadcastHighLightUrl" ).get( i_temp ));
//
//																	} catch ( Exception e ) {
//																		Logs.show ( e );
//																	}
//
//																}
//
//															});
//														}
//
//													}
//												}
//
//											}).start();
										}

										inflater = null;
									}
								} catch (Exception e) {
									Logs.show(e);
								}

							}
						});

					}
				} catch (Exception e) {
					Logs.show(e);
				}

			}
		}).start();





	}



	/**
	 * handling data if any change in database has occured.
	 */
	@Override
	public void onUpdate(final Message msg) {


		

		// refresh the values .
		if ( PlayUpActivity.handler != null ) {
			PlayUpActivity.handler.post( new Runnable () {

				@Override
				public void run() {
					try{

						if ( !isVisible() ) {
							return;
						}
						
						if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
							
							getRegions();
							
						}
						if( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "Regions" )){

							try{

								if(PlayUpActivity.handler != null){

									PlayUpActivity.handler.post(new Runnable() {

										@Override
										public void run() {
											try {
												if(!isVisible())
													return;

												setUserRegion();
												return;
											} catch (Exception e) {
												Logs.show ( e );
											}
										}
									});

								}

							}catch (Exception e) {
								// TODO: handle exception
								Logs.show(e);
							}



						}


						if( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "SignOut" )){
							//Log.e("123", "SignOut");
							try {
								if( PlayUpActivity.handler!= null ) {
									PlayUpActivity.handler.post( new Runnable() {

										@Override
										public void run() {

											try {

												if(!isVisible())
													return;
												
												
												logoutRelativeLayout.setVisibility(View.GONE);										
												myProviders.setVisibility(View.GONE);
												myProviders.removeAllViews();
												avatarImageView.setImageBitmap( null );

											} catch ( Exception e ) {
												Logs.show ( e );
											}
										}
									});
								}


							} catch (Exception e) {
								Logs.show ( e );
							}



							isProviderSet = false;

							
						} else if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "AnonymousSignOut" )){
							//Log.e("123", "AnonymousSignOut");
							new Util().logout();
						}else if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "SignOutFailure" )){
							//Log.e("123", "SignoutFailure");
							logoutRelativeLayout.setVisibility(View.VISIBLE);
							logoutRelativeLayout.setEnabled( true );
							
						} else if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "ConnectionFailure") && msg.arg1==1) {
							//Log.e("123", "ConnectionFailure");
							logoutRelativeLayout.setVisibility(View.VISIBLE);
							logoutRelativeLayout.setEnabled( true );
							
						} else {
							//Log.e("123", "UpdateAnyways");

							isProviderSet = false;
							setValues();
							
							refreshMyProfile();


						}
					}catch (Exception e) {
						Logs.show ( e );
					}



				}

			} );
		}




	}
	
	/**
	 * scheduling the refresh for primary user profile
	 */

	private void refreshMyProfile() {
		if(myProfileTimer == null)
			myProfileTimer = new Timer();
		
		
		if(myProfileTimerTask == null){
			
			myProfileTimerTask = new TimerTask() {
				
				@Override
				public void run() {
					//Praveen : Changed
					new Util().getProfileData();
				}
			};
			
			new Thread(new  Runnable() {
				
				@Override
				public void run() {
					try {
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						Hashtable<String, Object> result = dbUtil.getProfileURLFromRootResource();

						if( result!=null	&&	result.get("url")!=null &&  result.get("url").toString().trim().length()> 0 ){
													String myProfileUrl = (String) result.get("url");
													//Boolean isHref = (Boolean) result.get("isHref");
													
													int cacheTime = Integer.parseInt(dbUtil.getCacheTime(myProfileUrl));
													
													
													
													if(cacheTime > 0){
														
														myProfileTimer.schedule(myProfileTimerTask, cacheTime * 1000,cacheTime * 1000);
													}else{
														
														myProfileTimer = null;
														myProfileTimerTask = null;
													}
												}
					} catch (Exception e) {
						Logs.show(e);
					}
					
				}
			}).start();
			
			
			
		}
		
	}
	
	@Override
	public void onStop() {

		super.onStop();
		
		if(myProfileTimer != null)
			myProfileTimer.cancel();
		
		
		if(myProfileTimerTask != null)			
			myProfileTimerTask.cancel();
		
		
		myProfileTimer = null;
		myProfileTimerTask = null;
	}

	/**
	 * selector for the twitter, facebook and logout 
	 */
	private void selectDeSelectItems  ( final View v, boolean isSelected ) {

		try {
			if ( v.getId() == R.id.logout_view ) {

				if ( isSelected ) {
					v.setBackgroundResource(R.drawable.menu_pressed);
					( ( ImageView ) v.findViewById( R.id.logo ) ).setImageResource(R.drawable.log_out_d);
					( ( ImageView ) v.findViewById( R.id.vertical_divider ) ).setImageResource(R.drawable.white_line);
					( ( TextView ) v.findViewById( R.id.title ) ).setTextColor(Color.parseColor("#FFFFFF"));
					( ( ImageView ) v.findViewById( R.id.vertical_divider ) ).setPadding(0, 0,0,0);

				} else {
					v.setBackgroundResource(R.drawable.log_out_base);

					( ( ImageView ) v.findViewById( R.id.logo ) ).setImageResource(R.drawable.log_out);
					( ( ImageView ) v.findViewById( R.id.vertical_divider ) ).setImageResource(R.drawable.red_line);
					( ( TextView ) v.findViewById( R.id.title ) ).setTextColor(Color.parseColor("#4B4B4B"));
					( ( ImageView ) v.findViewById( R.id.vertical_divider ) ).setPadding(0, 4,0,4);

				}

			}
			else if ( v.getId() == R.id.updateRegionView ) {

				if ( isSelected ) {
					v.setBackgroundResource(R.drawable.menu_pressed);


					( ( ImageView ) v.findViewById( R.id.vertical_divider ) ).setImageResource(R.drawable.white_line);
					( ( TextView ) v.findViewById( R.id.updateRegionTitile ) ).setTextColor(Color.parseColor("#FFFFFF"));
					( ( ImageView ) v.findViewById( R.id.vertical_divider ) ).setPadding(0, 0,0,0);
					( ( ImageView ) v.findViewById( R.id.chevron ) ).setImageResource(R.drawable.chevron_d);
				} else {
					v.setBackgroundResource(R.drawable.log_out_base);
					( ( ImageView ) v.findViewById( R.id.vertical_divider ) ).setImageResource(R.drawable.red_line);
					( ( TextView ) v.findViewById( R.id.updateRegionTitile ) ).setTextColor(Color.parseColor("#4B4B4B"));
					( ( ImageView ) v.findViewById( R.id.vertical_divider ) ).setPadding(0,4,0,4);
					( ( ImageView ) v.findViewById( R.id.chevron ) ).setImageResource(R.drawable.chevron);
				}




			}


			else if ( v.getTag() != null ) {

				LayoutParams params = (LayoutParams) v.getLayoutParams();
				if( v.getHeight() > 0 )
					params.height = v.getHeight();


				final String vProviderName = v.getTag().toString();
				if ( vProviderName != null && vProviderName.trim().length() > 0 ) {
					if ( isSelected ) {
						params.setMargins(2, 0, 2, 0);
						v.setLayoutParams(params);
						relParams = (android.widget.RelativeLayout.LayoutParams) v.findViewById( R.id.logoBase ).getLayoutParams();
						relParams.leftMargin = -2;
						v.findViewById( R.id.logoBase ).setLayoutParams(relParams);

						if ( v.getId() == -2 ) {

							//v.setBackgroundResource(R.drawable.menu_middle_pressed);

							v.setBackgroundResource(R.drawable.menu_bottom_pressed);
							( ( ImageView ) v.findViewById( R.id.vertical_divider ) ).setPadding(0, 0,0,0);

						} else {
							v.setBackgroundResource(R.drawable.menu_middle_pressed);
						}

						new Thread ( new Runnable () {

							@Override
							public void run() {
								
								DatabaseUtil dbUtil = DatabaseUtil.getInstance();
								final String sel_icon_url = dbUtil.getProviderSelIconBroadcastUrl( vProviderName );
								
								if ( PlayUpActivity.handler != null )  {
									PlayUpActivity.handler.post( new  Runnable () {

										@Override
										public void run() {
											
											try {
												imageDownloader.download( sel_icon_url,  ( ImageView ) v.findViewById( R.id.logo ), false,null );
											} catch (Exception e) {
												// TODO Auto-generated catch block
												Logs.show(e);
											}

										}
										
									});
								}
								
							}
							
						}).start();
						
						( ( ImageView ) v.findViewById( R.id.vertical_divider ) ).setImageResource(R.drawable.white_line);
						( ( ImageView ) v.findViewById( R.id.connectPlus ) ).setImageResource(R.drawable.connect_plus_d);

						( ( TextView ) v.findViewById( R.id.title ) ).setTextColor(Color.parseColor("#FFFFFF"));
						( ( TextView ) v.findViewById( R.id.connectText ) ).setTextColor(Color.parseColor("#FFFFFF"));


					} else {
						//					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						//					String icon_url = dbUtil.getProviderIconBroadcastUrl( vProviderName );
						//					dbUtil = null;

						params.setMargins(0, 0, 0, 0);
						v.setLayoutParams(params);
						relParams = (android.widget.RelativeLayout.LayoutParams) v.findViewById( R.id.logoBase ).getLayoutParams();
						relParams.leftMargin = 0;
						v.findViewById( R.id.logoBase ).setLayoutParams(relParams);

						String icon_url  = "";
						if(v.getTag(R.id.about_txtview) != null && v.getTag(R.id.about_txtview).toString().trim().length() > 0)
							icon_url = v.getTag(R.id.about_txtview).toString();

						imageDownloader.download(  icon_url,  ( ImageView ) v.findViewById( R.id.logo ), false,null  );

						if ( v.getId() == -2 ) {
							v.setBackgroundResource(R.drawable.content_base );
							( ( ImageView ) v.findViewById( R.id.vertical_divider ) ).setPadding(0, 0,0,4);
						} else {
							v.setBackgroundResource(R.drawable.content_base_middle );
						}

						( ( ImageView ) v.findViewById( R.id.vertical_divider ) ).setImageResource(R.drawable.red_line);
						( ( ImageView ) v.findViewById( R.id.connectPlus ) ).setImageResource(R.drawable.connect_plus );

						TextView title = ( TextView ) v.findViewById( R.id.title );

						if( !( ( TextView ) v.findViewById( R.id.connectText ) ).getText().toString().contains( getString( R.string.connected ) ) ) {
							title.setTextColor(Color.parseColor("#4B4B4B"));
						} else {
							title.setTextColor(Color.parseColor("#28A645"));
						}
						title = null;

						( ( TextView ) v.findViewById( R.id.connectText ) ).setTextColor(Color.parseColor("#ABABAB"));
					}
				} 
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {

		try {
			if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
				selectDeSelectItems ( v, true );
			} else if ( event.getAction() == MotionEvent.ACTION_UP ) { 

				selectDeSelectItems ( v, false );

				// handle click
				onClick( v );
				return false;
			} else if ( event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_CANCEL ) {
				selectDeSelectItems ( v, false );		
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
		return true;
	}


	/**
	 * sets the user data  
	 */
	private void setUserData () {
		// TO DO 
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT iUserId, vUserName, vName, vUserAvatarUrl, isAnonymousUser, vUserToken, vSelfUrl,vHrefUrl, iId FROM user WHERE isPrimaryUser = \"1\" " );
					if ( c != null  ) {

						if ( c.getCount() > 0 ) {

							c.moveToFirst();

							String userName = c.getString( c.getColumnIndex( "vUserName" ) );
							final String name = c.getString( c.getColumnIndex( "vName" ) );
							String avatarUrl = c.getString( c.getColumnIndex( "vUserAvatarUrl" ) );


							final String iId = c.getString( c.getColumnIndex( "iId" ) );


							if( ! DatabaseUtil.getInstance().isUserAnnonymous() ) 
								setTopBarFragment ( userName );

							if(PlayUpActivity.handler != null){

								PlayUpActivity.handler.post(new Runnable() {



									@Override
									public void run() {

										try {
											if(!isVisible())
												return;
											setUserName ( name );
											setUserId ( iId );
										} catch (Exception e) {
											// TODO Auto-generated catch block
											Logs.show(e);
										}


									}
								});

							}

							setUserAvatar( iId, avatarUrl);

						}
						c.close();
						c = null;

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show(e);
				}

			}
		}).start();

		// fetch data from db 

		// set relevant data on corresponding fields

	}

	/**
	 * Checking if user is anonymous or logged in 
	 */
	private void checkAnnonymousUser() {

		try {
			if ( !isUserAnonymous ) {
				connected_userview.setVisibility(View.VISIBLE);
				disconnected_userview.setVisibility(View.GONE);
			} else {
				connected_userview.setVisibility(View.GONE);
				disconnected_userview.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}


	private String prev_userName;
	/**
	 *  setting the room in the top bar fragment.
	 **/
	private void setTopBarFragment ( String userName ) {

		try {
			if ( userName == null ) {
				userName = "";
			}
			if ( prev_userName != null && !userName.equalsIgnoreCase( prev_userName ) ) {
				isSet = false;
			}
			if ( !isSet ) {
				isSet = true;
				HashMap< String, String > map = new HashMap<String, String>();
				prev_userName = userName;
				map.put( "vUserName", userName );
				Bundle b = new Bundle();
				b.putString("vMainColor",null );
				b.putString("vMainTitleColor",null );
				Message msg = new Message ();
				msg.setData(b);
				msg.obj = map;
				PlayupLiveApplication.callUpdateTopBarFragments(msg) ;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
	}


	/**
	 * Setting User Name
	 * @param userName
	 */
	private void setUserName ( final String userName ) {

		userNameTextView.setText( new Util().getSmiledText(userName) );

	}

	/**
	 * Setting User id
	 * @param userId
	 */
	private void setUserId ( final String userId ) {
		userIdTextView.setText ( userId );
	}

	/**
	 * setting the avatar of the user 
	 */
	private void setUserAvatar( final String iId,  final String imageUrl ) {



		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String url = imageUrl;
					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT vProviderName,vAvatarUrl FROM providers WHERE isSet = \"1\" " );

					if(c != null){

						if(c.getCount() == 1){
							c.moveToFirst();			
							if(c.getString(c.getColumnIndex("vAvatarUrl")) != null && c.getString(c.getColumnIndex("vAvatarUrl")).trim().length() > 0){
								if(c.getString(c.getColumnIndex("vProviderName")).equalsIgnoreCase("facebook")  ){						
									url = c.getString(c.getColumnIndex("vAvatarUrl"));						
								}
								else if(c.getString(c.getColumnIndex("vProviderName")).equalsIgnoreCase("twitter")  ){
									url = c.getString(c.getColumnIndex("vAvatarUrl"));					

								}

							}
						}
						c.close();
						c = null;

					}
					final String avatarUrl = url;
					if(PlayUpActivity.handler != null){

						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {
								try {
									if(!isVisible())
										return;


									if ( imageDownloader != null && avatarUrl != null && avatarImageView != null ) {
										imageDownloader.download( iId , avatarUrl.replace("square", "large"),  avatarImageView, true,null );
									}
								} catch (Exception e) {
									Logs.show(e);
								}

							}
						});

					}


				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show(e);
				}

			}
		}).start();





		

	}

	@Override
	public void onClick(View view ) {

		switch ( view.getId() ) {


		case R.id.updateRegionView :

			
			PlayupLiveApplication.getFragmentManagerUtil().setFragment("UpdateRegionFragment");
			
			
			
			break;

		case R.id.logout_view :
			logoutRelativeLayout.setEnabled( false );
			final View logoutView 		=	 view;
			final AlertDialog alertDialog = new AlertDialog.Builder( PlayUpActivity.context ).create();
			alertDialog.setTitle( R.string.logoutText );

			String confirm = getString( R.string.confirm );
			String cancel = getString( R.string.cancel );

			alertDialog.setButton( confirm, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					logoutRelativeLayout.setVisibility(View.GONE);
					selectDeSelectItems ( logoutView, false );

					FlurryAgent.onEvent("signout");
					Util util = new Util ();
					util.signOut();
					//util.logout();
				}

			});
			alertDialog.setButton2( cancel, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					logoutRelativeLayout.setEnabled( true );
					selectDeSelectItems ( logoutView, false );
					alertDialog.cancel();
				}
			});

			alertDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					// TODO Auto-generated method stub
					try{
						logoutRelativeLayout.setEnabled( true );
					}catch (Exception e) {
						// TODO: handle exception
					}
				}
			});
			alertDialog.show();

			return;
		case R.id.disconnected_userview :
			Bundle bundle = new Bundle ();	
			bundle.putString( "fromFragment", "MyProfileFragment");
			PlayupLiveApplication.getFragmentManagerUtil().setFragment( "ProviderFragment", bundle );
			return;

		}

		if ( view.getTag() != null ) {
			final String vProviderName = (String) view.getTag();
			if ( isUserAnonymous ) {
				callProvider ( vProviderName );
			} else {
				
				new Thread ( new Runnable () {

					@Override
					public void run() {
						if ( !DatabaseUtil.getInstance().isIdentifierProviderName( vProviderName ) && PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable () {
								@Override
								public void run() {
									try {
										
										callProvider( vProviderName );
												
									} catch ( Exception e ) {
										Logs.show ( e );
									}
								}
							});
						}
					}
					
				}).start(); 
				
			}

		}

	}

	/**
	 * Calling Provider for fb or twitter login
	 * @param vProviderName
	 */
	private void callProvider ( String vProviderName ) {


		try {
			
			
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			Hashtable < String, List < String > >  data = dbUtil.getProviderByName( vProviderName );
			dbUtil = null;

			if ( data != null && data.get( "vLoginUrl").size() > 0 ) {

				Bundle bundle = new Bundle();

				bundle.putString("vLoginUrl", data.get("vLoginUrl").get( 0 ) );
				bundle.putString("vSuccessUrl", data.get("vSuccessUrl").get( 0 ) );
				bundle.putString("vFailureUrl", data.get("vFailureUrl").get( 0 ) );
				bundle.putString("fromFragment", "MyProfileFragment" );

				PlayupLiveApplication.getFragmentManagerUtil().setFragment( "LoginWebViewFragment", bundle);
			}
			data = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}


	@Override
	public void onConnectionChanged(boolean isConnectionActive) {
		super.onConnectionChanged(isConnectionActive);

//Praveen : Changed
		new Util().getRecentActivityData();
	}
}