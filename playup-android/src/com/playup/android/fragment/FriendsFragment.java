package com.playup.android.fragment;


import java.util.Hashtable;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.os.Bundle;
import android.os.Message;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.FriendsAdapter;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;

import com.playup.android.util.Util;


/**
 * Friends Fragment. 
 */
public class FriendsFragment extends MainFragment {


	private LinearLayout progressLinear;
	
	private LinearLayout onlineView;
	private LinearLayout offLineView;
	private LinearLayout provider_layout;

	private TimerTask refreshLiveFriendsTask;
	private Timer refreshLiveFriendsTimer;

	private TimerTask searchFriendsTimerTask;
	private Timer searchFriendsTimer = new Timer();

	private TimerTask refreshOtherFriendsTask;
	private Timer refreshOtherFriendsTimer;

	private TimerTask refreshAllFriendsTask;
	private Timer refreshAllFriendsTimer ;


	private ListView listView;
	private FriendsAdapter friendsAdapter = new FriendsAdapter();
	private String searchString;

	
	


	private String vSecColor = null;
	private String vSecTitleColor = null;
	
	
	private boolean inSearch;


	private boolean isDownloaded = false;


	

	private ImageDownloader imageDownloader = new ImageDownloader();
	private Hashtable< String , List < String > > providerData ;
	private boolean outSide=false;
	

	private static int currentPosition = 0;
	public static boolean loadingData = true;

	RelativeLayout content_layout;


	Hashtable<String,List<String>> onlineFriendsData;
	Hashtable<String,List<String>> allFriendsData;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


		content_layout = (RelativeLayout) inflater.inflate( R.layout.friends, null);
		friendsAdapter = null;
		
		

		return content_layout;
	}
	private String vMainColor = null;

	private String vMainTitleColor = null;




	@Override
	public void onStop () {
		super.onStop();
		
		searchString = null;
		
		
		if ( providerData != null ) {
			providerData.clear();
			providerData = null;
		}
		if ( onlineFriendsData != null ) {
			onlineFriendsData.clear();
			onlineFriendsData = null;
		}
		if ( allFriendsData != null ) {
			allFriendsData.clear();
			allFriendsData = null;
		}



		if ( refreshLiveFriendsTask != null ) {
			refreshLiveFriendsTask.cancel();
			refreshLiveFriendsTask = null;
		}

		if ( refreshLiveFriendsTimer != null ) {
			refreshLiveFriendsTimer.cancel();
			refreshLiveFriendsTimer = null;
		}

		if ( searchFriendsTimerTask != null ) {
			searchFriendsTimerTask.cancel();
			searchFriendsTimerTask = null;
		}

		if ( searchFriendsTimer != null ) {
			searchFriendsTimer.cancel();
			searchFriendsTimer = null;
		}

		if ( refreshOtherFriendsTask != null ) {
			refreshOtherFriendsTask.cancel();
			refreshOtherFriendsTask = null;
		}

		if ( refreshOtherFriendsTimer != null ) {
			refreshOtherFriendsTimer.cancel();
			refreshOtherFriendsTimer = null;
		}

		if ( refreshAllFriendsTask != null ) {
			refreshAllFriendsTask.cancel();
			refreshAllFriendsTask = null;
		}

		if ( refreshAllFriendsTimer != null ) {
			refreshAllFriendsTimer.cancel();
			refreshAllFriendsTimer = null;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if ( imageDownloader == null ) {
			imageDownloader = new ImageDownloader();
		}
		Log.e("123","inside onResume of FriendsFragment >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
		initialize( content_layout );
		isDownloaded = false;
		searchString =  null;		
		inSearch =  false;	


		setValues();
		if(!(DatabaseUtil.getInstance().isUserAnnonymous())) {
			getFriendsData();
		}

	}


	/**
	 * setting  all the content in the views 
	 */
	private void initialize ( final RelativeLayout content_layout ) {


		initializeViews(content_layout);

		setTopBar();

	}


	private void setTopBar() {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					
					
					String friendsUrl = dbUtil.getFriendsUrl(); 
					
					String vChildColor = dbUtil.getSectionMainColor("", friendsUrl);
					String vChildTitleColor = dbUtil.getSectionMainTitleColor("", friendsUrl);
					
					if(vChildColor != null && vChildColor.trim().length() > 0 )
						vMainColor = vChildColor;
					
					if(vChildTitleColor != null && vChildTitleColor.trim().length() > 0 )
						vMainTitleColor = vChildTitleColor;
					

					String vChildSecondaryColor = dbUtil.getSectionSecondaryColor ("", friendsUrl);
					String vChildSecondaryTitleColor = dbUtil.getSectionSecondaryTitleColor ( "", friendsUrl );
					
					 
					 if(vChildSecondaryColor != null && vChildSecondaryColor.trim().length() > 0)
						 vSecColor = vChildSecondaryColor;
					 
					 if(vChildSecondaryTitleColor != null && vChildSecondaryTitleColor.trim().length() > 0)
						 vSecTitleColor = vChildSecondaryTitleColor;
					
						

					Bundle b = new Bundle();
					b.putString("vMainColor",vMainColor );
					b.putString("vMainTitleColor",vMainTitleColor );
					Message msg = new Message ();
					msg.setData(b);
					msg.obj = "";
					PlayupLiveApplication.callUpdateTopBarFragments(msg);
				} catch (Exception e) {
					Logs.show(e);
				}
				
			}
		}).start();
		
		

	}



	private void initializeViews ( RelativeLayout content_layout) {
		listView = (ListView) content_layout.findViewById(R.id.activeUserList);

		if ( friendsAdapter == null ) {
			friendsAdapter = new FriendsAdapter();
			listView.setAdapter( friendsAdapter);

		} else {
			friendsAdapter.notifyDataSetChanged();
		}

		progressLinear	=	(LinearLayout) content_layout.findViewById ( R.id.progressFriends);
		onlineView = (LinearLayout) content_layout.findViewById(R.id.onlineView);
		offLineView = (LinearLayout) content_layout.findViewById(R.id.offLineView);
		provider_layout = (LinearLayout) content_layout.findViewById(R.id.provider_layout);
	}

	private void setValues () {
		
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		try {

			
			if ( dbUtil.isUserAnnonymous() ) {
			
				onlineView.setVisibility( View.INVISIBLE );
				progressLinear.setVisibility(View.GONE);
				offLineView.setVisibility(View.VISIBLE);
				providerData = dbUtil.getProviders();
				if(Util.isInternetAvailable()){
					showProvider();

				}else{
					if ( Constants.isCurrent ) {

						PlayupLiveApplication.showToast( R.string.internet_connectivity_dialog );

						/*if ( noProviderDialog != null && noProviderDialog.isShowing() ) {
							return;
						}


						noProviderDialog = new NoProviderDialog(  PlayUpActivity.context  );
						noProviderDialog.show();
						noProviderDialog.setOnCancelListener(new OnCancelListener() {


							@Override
							public void onCancel(DialogInterface dialog) {
								// TODO Auto-generated method stub
								PlayupLiveApplication.getFragmentManagerUtil().popBackStack("ProviderFragment");
							}
						});*/
					}

				}


			} else {
	

				offLineView.setVisibility(View.GONE);

				progressLinear.setVisibility(View.VISIBLE);
				
				
				if ( onlineFriendsData != null ) {
					onlineFriendsData.clear();
				}
				if ( allFriendsData != null ) {
					allFriendsData.clear();
				}

				onlineFriendsData = dbUtil.getOnlineFriendsData();
				allFriendsData = dbUtil.getAllFriendsData();
				


				if ( ( allFriendsData != null && allFriendsData.get("vFriendId").size() > 0 ) || ( onlineFriendsData != null && onlineFriendsData.get("vFriendId").size() > 0 ) ) {

					dismissProgressIndicator();

					if ( friendsAdapter == null ) {
						friendsAdapter = new FriendsAdapter ( allFriendsData, onlineFriendsData, listView,vMainColor,  vMainTitleColor ,vSecColor,vSecTitleColor);
						listView.setAdapter( friendsAdapter);
						listView.setSelection(currentPosition);
					} else {
						friendsAdapter.setData ( allFriendsData, onlineFriendsData, listView,vMainColor,  vMainTitleColor ,vSecColor,vSecTitleColor);
					}
				} else {
					showProgressIndicator();
				}



				if ( isDownloaded &&  ( ( allFriendsData == null && onlineFriendsData == null ) || 
						( allFriendsData != null && allFriendsData.get( "vFriendId" ).size() == 0 ) && 
						( onlineFriendsData != null && onlineFriendsData.get( "vFriendId" ).size() == 0 ) ) ) {

					dismissProgressIndicator();
					if ( friendsAdapter == null ) {
						friendsAdapter = new FriendsAdapter(vMainColor,  vMainTitleColor,vSecColor,vSecTitleColor);
						friendsAdapter.showzeroFriends();
						listView.setAdapter( friendsAdapter);
					} else {
						friendsAdapter.showzeroFriends();
					}
				}
			}

			dbUtil = null;

		}catch ( Exception e  ) {
			dbUtil = null;
		}


		


	}


	private void showProgressIndicator(){

		if( !Util.isInternetAvailable() ) {
			dismissProgressIndicator();
			return;
		}
		loadingData = true;
		if(progressLinear != null && !progressLinear.isShown()){

			progressLinear.setVisibility(View.VISIBLE);
			onlineView.setVisibility(View.INVISIBLE);
		}
	}

	private void dismissProgressIndicator(){

		loadingData = false;
		if(listView != null && !listView.isShown()){
			onlineView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.VISIBLE);
		}
		if(progressLinear != null)
			progressLinear.setVisibility(View.GONE);
	}


	private void getFriendsData(){
		try{

		if(runnableList!=null&&!runnableList.containsKey(Constants.GET_FREINDS_DATA)){
			if (Util.isInternetAvailable()) {


				runnableList.put(Constants.GET_FREINDS_DATA, new Util().getFriendsData(runnableList,false));



			}
		
		}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}


	}




	public void onUpdate ( final Message msg ) {


		if ( msg == null && !inSearch ) {
			if ( PlayUpActivity.handler != null ) {
				PlayUpActivity.handler.post( new Runnable () {

					@Override
					public void run() {

						try {
							if ( !isVisible() ) {
								return;
							}
							if ( !DatabaseUtil.getInstance().isUserAnnonymous() ) {
								Log.e("123","inside onUpdate of FriendsFragment >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> ");
								onResume();
							} 


							// setValues();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Logs.show ( e );
						}

					}
				});
			}
			return;
		}



		if ( msg != null && msg.obj!= null && msg.obj.toString().equalsIgnoreCase("SearchFriendsString") ) {

			if ( msg.getData() != null ) {

				Bundle bundle = msg.getData();
				if ( bundle.containsKey( "search_value") ) {
					searchString = bundle.getString( "search_value" ); 

					if ( searchFriendsTimer != null ) {
						searchFriendsTimer.cancel();
						searchFriendsTimer = null;
					}
					searchFriendsTimer = new Timer();

					if ( searchFriendsTimerTask != null ) {
						searchFriendsTimerTask.cancel();
						searchFriendsTimerTask = null;
					}

					if ( searchString.trim().length() == 0 ) {

						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable() {

								@Override
								public void run() {

									try {
										inSearch = false;

										if ( !isVisible() ) {
											return;
										}

										Message msg = new Message();
										msg.obj = "Dismiss progress";
										PlayupLiveApplication.callUpdateTopBarFragments(msg);

										setValues();
									} catch (Exception e) {
										// TODO Auto-generated catch block
										Logs.show ( e );
									}
								}
							});
						}
						return;
					} else {


						if ( searchFriendsTimer != null ) {
							searchFriendsTimer.cancel();
							searchFriendsTimer = null;
						}
						searchFriendsTimer = new Timer();

						if ( searchFriendsTimerTask != null ) {
							searchFriendsTimerTask.cancel();
							searchFriendsTimerTask = null;
						}
						searchFriendsTimerTask = new TimerTask() {

							@Override
							public void run() {
								try {
									if (Util.isInternetAvailable()) {	

										DatabaseUtil dBUtil = DatabaseUtil.getInstance();
										Hashtable<String, Object> result = dBUtil.getFriendSearchUrl();
										
										if(result == null || (result != null && !result.containsKey("isHref"))){
											return;
										}
										
										String vFriendSearchUrl = (String) result.get("url");

										if ( vFriendSearchUrl != null && vFriendSearchUrl.trim().length() > 0 && searchString != null) {

											vFriendSearchUrl = vFriendSearchUrl.replace( "{prefix}", searchString );
											UUID ramdomString = UUID.randomUUID();

											if ( searchString != null ) {
												
												if (  runnableList != null && ramdomString != null && Util.isInternetAvailable()  ) {
													boolean isHref = ((Boolean) result.get("isHref")).booleanValue();
													runnableList.put(ramdomString.toString(),new Util().searchFriends( vFriendSearchUrl, searchString,isHref));
												}
											}


										}
										dBUtil = null;

									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									Logs.show ( e );
								}
							}
						};
						searchFriendsTimer.schedule(searchFriendsTimerTask, 1000);
						return;

					}
				}
			}
		}

		if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("inSearch") ) {

			searchString = "";

		}


		if ( PlayUpActivity.handler != null ) {
			PlayUpActivity.handler.post( new Runnable () {

				@Override
				public void run() {

					try {
						if ( !isVisible() ) {
							return;
						}

						if ( msg != null && msg.obj != null &&msg.obj.toString().equalsIgnoreCase("handleBackButton") ) {

							if( searchString != null ){	

								searchString =  null;
								inSearch = false;
								Message msg = new Message();
								msg.obj = "Show Friends";
								PlayupLiveApplication.callUpdateTopBarFragments(msg);
								setValues();

							} else {

								Message msg = new Message();
								msg.obj = "Show Friends";
								PlayupLiveApplication.callUpdateTopBarFragments(msg);

								PlayUpActivity.handler.postDelayed( new Runnable () {

									@Override
									public void run() {
										try {
											String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
											PlayupLiveApplication.getFragmentManagerUtil().popBackStack(topFragmentName);
										} catch (Exception e) {
											// TODO Auto-generated catch block
											Logs.show(e);
										}
									}

								},  200 );
							}

						}

						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("FriendsData")){

							if(  msg.arg1 == 1) {
								//PlayupLiveApplication.showToast( R.string.notification_err );
							}

							isDownloaded = true;
							if(!inSearch){
								setTopBar();
								setValues();
							}

							refreshAllFriends();
							refreshLiveFriends();
							refreshOtherFriends();

						}

						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("EmptyFriendsData")){

							getFriendsData();

						}


						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "SearchFriendsData" ) ) {
							if(searchString != null && searchString.trim().length() > 0)
								filterText();
						}
						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "liveFriendsData" ) ) {
							
							if(msg.arg1 == 1){
							
							if ( !inSearch ) {
								if ( Constants.friendsUpdated ) {
									setValues();
									Constants.friendsUpdated = false;
								} else {
									updateLiveFriends();
								}
							}
						}
							refreshLiveFriends();
						}

						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "updateFriendsData" ) ) {
							
							if(msg.arg1 == 1){
								
								if ( !inSearch )
									setValues();
								
							}
							refreshOtherFriends();
						}

//						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "refreshOtherFriends" ) ) {
//							if ( !inSearch )
//								setValues();
//							refreshOtherFriends();
//						}
//						if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("refreshLiveFriends")){
//							if ( !inSearch ) {
//								if ( Constants.friendsUpdated ) {
//									setValues();
//									Constants.friendsUpdated = false;
//								} else {
//									updateLiveFriends();
//								}
//							}
//							refreshLiveFriends();
//						}
						if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("refreshAllFriends")){
							if ( !inSearch ) {
								setValues();
							}
							refreshAllFriends();
						}
						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "refreshFriends" ) ) {

							if ( inSearch ) {
								filterText();
							} else {
								setValues();
							}

						}
						
						if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
							
							if(!(DatabaseUtil.getInstance().isUserAnnonymous())) {
								getFriendsData();
							}
							
							setValues();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show ( e );
					}
				}
			});
		}

	}



	private void updateLiveFriends () {
		try{
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();

		if ( onlineFriendsData != null ) {
			onlineFriendsData.clear();
			onlineFriendsData = null;
		}
		onlineFriendsData = dbUtil.getOnlineFriendsData();


		if ((onlineFriendsData != null && onlineFriendsData.get("vFriendId").size() > 0 ) ) {

			if ( friendsAdapter == null ) {
				friendsAdapter = new FriendsAdapter();
			} 
			friendsAdapter.setData(onlineFriendsData,true,vMainColor,  vMainTitleColor,vSecColor,vSecTitleColor);
		}

		dbUtil = null;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
	}



	public void filterText(){
		

		try {
			inSearch = true;
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();

			if ( allFriendsData != null ) {
				allFriendsData.clear();
				allFriendsData = null;
			}
			allFriendsData = dbUtil.searchAllFriendsData();
			dbUtil = null;

			if ( allFriendsData == null ) {
				friendsAdapter.setDummyData( 6 );
			} else if ( allFriendsData.size() == 0  ) {

				friendsAdapter.setDummyData( 6 );
			} else {
				friendsAdapter.setData(allFriendsData,vMainColor,  vMainTitleColor,vSecColor,vSecTitleColor);
			}


			listView.setSelection( 0 );

			Message msg = new Message();
			msg.obj = "Dismiss progress";
			PlayupLiveApplication.callUpdateTopBarFragments(msg);

		} catch ( Exception e) {

		}


	}




	@Override
	public void onPause() {
		super.onPause();


		currentPosition = listView.getFirstVisiblePosition();

		if (refreshLiveFriendsTimer != null) {
			refreshLiveFriendsTimer.cancel();
			refreshLiveFriendsTimer	=	null;
		}


		if ( refreshLiveFriendsTask !=  null ) {
			refreshLiveFriendsTask.cancel();
			refreshLiveFriendsTask = null;
		}

		if (refreshOtherFriendsTimer != null) {
			refreshOtherFriendsTimer.cancel();
			refreshOtherFriendsTimer	=	null;
		}


		if ( refreshOtherFriendsTask !=  null ) {
			refreshOtherFriendsTask.cancel();
			refreshOtherFriendsTask = null;
		}


		if (refreshAllFriendsTimer != null) {
			refreshAllFriendsTimer.cancel();
			refreshAllFriendsTimer	=	null;

		}


		if ( refreshAllFriendsTask !=  null ) {
			refreshAllFriendsTask.cancel();
			refreshAllFriendsTask = null;
		}


	}
	public void refreshLiveFriends() {

		
		
		try {
			if (refreshLiveFriendsTimer == null) {
				refreshLiveFriendsTimer = new Timer();
			}
			

			if ( refreshLiveFriendsTask ==  null ) {
				
				DatabaseUtil dbUtil = DatabaseUtil.getInstance();
				final Hashtable<String, Object> result = dbUtil.getLiveFriendsUrl();
				final String liveFriendsUrl = (String) result.get("url");
				final Boolean isHref = (Boolean) result.get("isHref");
				
			refreshLiveFriendsTask = new TimerTask() {



				@Override
				public void run() {

					try {
						if(runnableList!=null && !runnableList.containsKey(Constants.GET_LIVE_FRIENDS)){
							if (Util.isInternetAvailable()) {


								runnableList.put(Constants.GET_LIVE_FRIENDS, new Util().getLiveFriends(liveFriendsUrl, isHref,false, runnableList));
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show ( e );
					}
				}
			};
			
			


			int liveFriendscacheTime = Integer.parseInt(dbUtil.getCacheTime(liveFriendsUrl));
			
			

			if(liveFriendscacheTime > 0){

				refreshLiveFriendsTimer.schedule(refreshLiveFriendsTask, (liveFriendscacheTime * 1000), (liveFriendscacheTime * 1000));
			}else{
				
				refreshLiveFriendsTask = null;
				refreshLiveFriendsTimer = null;
				
			}

			
			dbUtil = null;
			
			
			}
		} catch (Exception e) {
			Logs.show(e);
		}
		
	}


	public void refreshOtherFriends() {

		
		
		

		try {
			if (refreshOtherFriendsTimer == null) {
				refreshOtherFriendsTimer = new Timer();
				
			}
			

			if ( refreshOtherFriendsTask ==  null ) {
				
				DatabaseUtil dbUtil = DatabaseUtil.getInstance();
				final String updateFriendsUrl;
				
					Hashtable<String,Object> 	result = dbUtil.getUpdateFriendsUrl();
					updateFriendsUrl = (String)result.get("url");
					final Boolean isHrefUrl	=	(Boolean) result.get("isHref");



				int updateFriendscacheTime = Integer.parseInt(dbUtil.getCacheTime(updateFriendsUrl));
				
				
				refreshOtherFriendsTask = new TimerTask() {

				@Override
				public void run() {
					try {
						if(runnableList!=null&&!runnableList.containsKey(Constants.GET_UPDATE_FRIENDS)){

							if (Util.isInternetAvailable()) {
								runnableList.put(Constants.GET_UPDATE_FRIENDS, new Util().getUpdateFriends(updateFriendsUrl,isHrefUrl, false, runnableList));
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show ( e );
					}
				}
			};

			if(updateFriendscacheTime > 0){

				refreshOtherFriendsTimer.schedule(refreshOtherFriendsTask, (updateFriendscacheTime * 1000 + 13000 ), (updateFriendscacheTime * 1000));
			}else{
				
				refreshOtherFriendsTask = null;
				refreshOtherFriendsTimer = null;
				
			}


			
			dbUtil = null;
			}
		} catch (Exception e) {
			Logs.show(e);
		}
		
	}



	public void refreshAllFriends() {

		try {
			DatabaseUtil	dbUtil = DatabaseUtil.getInstance();

			
				String allFriendsUrl = dbUtil.getFriendsUrl();


			
			
			int allFriendscacheTime = Integer.parseInt(dbUtil.getCacheTime(allFriendsUrl));
			
			
			

			if (refreshAllFriendsTimer == null) {

			
				refreshAllFriendsTimer = new Timer();
			}

			if ( refreshAllFriendsTask ==  null ) {


			

				refreshAllFriendsTask = new TimerTask() {
					@Override
					public void run() {
						try {
							if(runnableList!=null&&!runnableList.containsKey(Constants.GET_FREINDS_DATA)){
								if (Util.isInternetAvailable()) {
									runnableList.put(Constants.GET_FREINDS_DATA,new Util().getFriendsData(runnableList,true));
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Logs.show ( e );
						}
					}
				};

				if(allFriendscacheTime > 0){

					refreshAllFriendsTimer.schedule(refreshAllFriendsTask, (allFriendscacheTime * 1000), (allFriendscacheTime * 1000));
				}else{
					refreshAllFriendsTask	=	null;
					refreshAllFriendsTimer	=	null;
				}

			}
			dbUtil = null;
		} catch (NumberFormatException e) {
			Logs.show(e);
		}
	}
	/**
	 * Showing the provider screen
	 */
	private void showProvider(){

		LayoutParams params = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f);

		provider_layout.removeAllViews();

//		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
//		providerData = dbUtil.getProviders();
//		dbUtil = null;

		if (providerData != null && providerData.get("vProviderName").size() > 0) {
		
			int len = providerData.get("vProviderName").size();

			// initializing the ImageDownloader class to download images.
			for (int i = 0; i < len; i++) {
				
				ImageView imgView = new ImageView(  PlayUpActivity.context  );

				String icon_url = providerData.get("vIconLoginUrl").get(i);
				String sel_icon_url = providerData.get("vIconHightLightUrl").get(i);

				imgView.setTag(i);
				imgView.setOnTouchListener( providerTouchListener );

				imgView.setPadding( 12, 0, 12, 0);
				// adding the imageview in the layout
				provider_layout.addView(imgView,params);

				imageDownloader.download(  icon_url, imgView, false,null );
				imageDownloader.download( sel_icon_url, null, false,null );

				icon_url = null;
				sel_icon_url = null;
				imgView = null;
			}


		} else {

			if ( Constants.isCurrent ) {

				PlayupLiveApplication.showToast( R.string.no_provider_data );

				/*if ( noProviderDialog != null && noProviderDialog.isShowing() ) {
					return;
				}

				noProviderDialog = new NoProviderDialog(  PlayUpActivity.context  );
				noProviderDialog.show();
				noProviderDialog.setOnCancelListener(new OnCancelListener() {


					@Override
					public void onCancel(DialogInterface dialog) {
						PlayupLiveApplication.getFragmentManagerUtil().popBackStack("ProviderFragment");
					}
				});*/
			}
		}
	}


	private OnTouchListener providerTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent event) {

			int coordinates[] = new int[2];
			view.getLocationOnScreen(coordinates);
			int viewX = coordinates[0];
			int viewY = coordinates[1];


			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				outSide=false;
				selectDeSelectState(view, true);
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				selectDeSelectState(view, false);
				if ((event.getRawX() > viewX && event.getRawX() < (viewX + view
						.getWidth()))
						&& (event.getRawY() > viewY && event.getRawY() < (viewY + view
								.getHeight()))) {
					if(!outSide)
						callProvider(Integer.parseInt(view.getTag().toString()));
				}
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				if (!((event.getRawX() > viewX && event.getRawX() < (viewX + view
						.getWidth())) && (event.getRawY() > viewY && event
								.getRawY() < (viewY + view.getHeight())))) {
					outSide=true;
					selectDeSelectState(view, false);
				}
			}
			return true;
		}
	};

	/**
	 * Calling provider for login 
	 * @param position position of the provider
	 */
	private void callProvider ( int position ) {

		String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
		Bundle bundle = new Bundle();
		bundle.putString("vLoginUrl", providerData.get("vLoginUrl").get(position));
		bundle.putString("vSuccessUrl", providerData.get("vSuccessUrl").get(position));
		bundle.putString("vFailureUrl", providerData.get("vFailureUrl").get(position));
		bundle.putString("fromFragment", topFragmentName);

		cancelRunnable();
		PlayupLiveApplication.getFragmentManagerUtil().setFragment("LoginWebViewFragment", bundle);

	}

	/**
	 *  showing selected / de selected state of the sports item 
	 */
	private void selectDeSelectState ( View v, boolean isSelected ) {

		try {
			if ( v.getTag()!=null && providerData != null && imageDownloader != null ) {
				int position = Integer.parseInt(v.getTag().toString());
				ImageView imageView=(ImageView) v;
				if ( isSelected ) {
					imageDownloader.download( providerData.get("vIconHightLightUrl").get(position) , imageView , false,null );
				}else{	
					imageDownloader.download( providerData.get("vIconLoginUrl").get(position), imageView, false ,null);
				}


			}

		} catch ( Exception e ) {
			Logs.show ( e  );
		}
	}

}
