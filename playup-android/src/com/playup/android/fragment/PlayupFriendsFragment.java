package com.playup.android.fragment;

import java.util.Hashtable;


import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentManager.BackStackEntry;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.PlayUpFriendsAdapter;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;



public class PlayupFriendsFragment extends MainFragment  {

	private RelativeLayout content_layout;
	private ListView playupfrndList;

	private static int currentPosition = 0;

	private TimerTask searchFriendsTimerTask;
	private Timer searchFriendsTimer = new Timer();

	private TimerTask liveFriendsTask;
	private Timer liveFriendsTimer = new Timer();

	private TimerTask updateFriendsTask;
	private Timer updateFriendsTimer = new Timer();


	private LinearLayout progressLinear;
	private LinearLayout onlineView;

	private boolean inSearch;

	private boolean isDownloaded = false;

	public static boolean loadingData = true;
	private PlayUpFriendsAdapter playUpFriendsAdapter = new PlayUpFriendsAdapter();
	private String searchString;
	private String vMainColor = null;
	private String vMainTitleColor = null;
	private boolean isAgain = false;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;
	private String fromFragment = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		content_layout = (RelativeLayout) inflater.inflate( R.layout.playupfriends, null);
		playUpFriendsAdapter = null;
		
		if(!isAgain )
			setColors( getArguments() );

		return content_layout;
	}



	@Override
	public void onAgainActivated(Bundle args) {
		super.onAgainActivated(args);
		
		isAgain = true;
		setColors( args );
	}
	/**
	 * set the params that are passed onto the fragment
	 * @param bundle
	 */

	private void setColors(Bundle bundle) {
		
		vMainColor = null;
		vMainTitleColor = null;
		

		vSecColor = null;
		vSecTitleColor = null;
		
		
		fromFragment = null;

		if (bundle != null && bundle.containsKey("vMainColor")) {
			vMainColor = bundle.getString("vMainColor");
		}if (bundle != null && bundle.containsKey("vMainTitleColor")) {
			vMainTitleColor = bundle.getString("vMainTitleColor");
		}if (bundle != null && bundle.containsKey("vSecColor")) {
			vSecColor = bundle.getString("vSecColor");
		}if (bundle != null && bundle.containsKey("vSecTitleColor")) {
			vSecTitleColor = bundle.getString("vSecTitleColor");
		}if (bundle != null && bundle.containsKey("fromFragment")) {
			fromFragment  = bundle.getString("fromFragment");
		}

		
	}



	@Override
	public void onResume() {

		super.onResume();
		isDialogShowing = false;

		initialize( content_layout );
		isDownloaded = false;
		searchString =  null;		
		inSearch =  false;	
		getPlayupFriendsData();
		
		setValues();

	}



	/**
	 * making api call to fetch the playupfriends data
	 * 
	 */


	private void getPlayupFriendsData() {
		try{
		new Util().getPlayUpFriendsData();
		
		

		DatabaseUtil dbUtil  = DatabaseUtil.getInstance();
		Hashtable<String, Object> result = dbUtil.getPlayupLiveFriendsUrl();
		String liveFriendsUrl = (String) result.get("url");
		Boolean isHref = (Boolean) result.get("isHref");
		


		
		if(liveFriendsUrl != null && liveFriendsUrl.trim().length() > 0 && runnableList != null 
				&& !runnableList.contains(Constants.GET_LIVE_FRIENDS))
			runnableList.put(Constants.GET_LIVE_FRIENDS,new Util().getLiveFriends(liveFriendsUrl,isHref,true,runnableList));
			
				

		Hashtable<String, Object> result1 = dbUtil.getPlayupUpdateFriendsUrl();
		String updateFriendsUrl =(String) result1.get("url");
		Boolean isHref1 =(Boolean) result1.get("isHref");
		
		if(updateFriendsUrl != null && updateFriendsUrl.trim().length() > 0 && runnableList != null 
				&& !runnableList.contains(Constants.GET_UPDATE_FRIENDS))
			runnableList.put(Constants.GET_UPDATE_FRIENDS,new Util().getUpdateFriends(updateFriendsUrl,isHref1,true,runnableList));
			

		

		dbUtil = null;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show ( e );
		}
	}



	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		currentPosition = playupfrndList.getFirstVisiblePosition();

		InputMethodManager imm = (InputMethodManager)PlayUpActivity.context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(PlayUpActivity.mBinder, InputMethodManager.HIDE_NOT_ALWAYS);

		
	}
	
	/**
	 * initializing
	 * @param content_layout
	 */
	
	private void initialize ( final RelativeLayout content_layout ) {


		initializeViews(content_layout);

		setTopBar();

	}
	
	
	/**
	 * setting the name and the color of the topBar
	 */
	private void setTopBar() {
		
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					
					
					String playupFriendsUrl = dbUtil.getPlayupFriendsUrl(); 
					
					String vChildColor = dbUtil.getSectionMainColor("", playupFriendsUrl);
					String vChildTitleColor = dbUtil.getSectionMainTitleColor("", playupFriendsUrl);
					
					if(vChildColor != null && vChildColor.trim().length() > 0 )
						vMainColor = vChildColor;
					
					if(vChildTitleColor != null && vChildTitleColor.trim().length() > 0 )
						vMainTitleColor = vChildTitleColor;
					
					String vChildSecondaryColor = dbUtil.getSectionSecondaryColor ("", playupFriendsUrl);
					String vChildSecondaryTitleColor = dbUtil.getSectionSecondaryTitleColor ( "", playupFriendsUrl);
					
					 
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
	
	/**
	 * initializing the views
	 * @param content_layout
	 */

	private void initializeViews ( RelativeLayout content_layout) {
		try{
		playupfrndList = ( ListView )content_layout.findViewById( R.id.playupfrndList );

		if ( playUpFriendsAdapter == null ) {
			playUpFriendsAdapter = new PlayUpFriendsAdapter();
			playupfrndList.setAdapter( playUpFriendsAdapter);

		} else {
			playUpFriendsAdapter.notifyDataSetChanged();
		}

		progressLinear	=	(LinearLayout) content_layout.findViewById ( R.id.progressFriends);
		onlineView = (LinearLayout) content_layout.findViewById(R.id.onlineView);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show ( e );
		}

	}





	/**
	 * showing the progress indicator
	 */

	private void showProgressIndicator(){

		loadingData = true;
		if(progressLinear!=null && !progressLinear.isShown()){

			progressLinear.setVisibility(View.VISIBLE);
			onlineView.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * dismissing the progress indicator
	 */

	private void dismissProgressIndicator(){

		loadingData = false;
		if(playupfrndList != null && !playupfrndList.isShown()){

			onlineView.setVisibility(View.VISIBLE);
			playupfrndList.setVisibility(View.VISIBLE);
		}
		if(progressLinear != null)
			progressLinear.setVisibility(View.GONE);
	}

	private boolean isDialogShowing = false;
	
	/**
	 * calling the respective functions to display data on the screen
	 */

	private void setValues(){
		
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		try {

			progressLinear.setVisibility(View.VISIBLE);

			Hashtable<String, List<String>> playupFriendsData = dbUtil.getPlayupFriendsData();

			if ( ( playupFriendsData != null && playupFriendsData.get("vFriendId").size() > 0 )) {

				dismissProgressIndicator();

				if ( playUpFriendsAdapter == null ) {
					playUpFriendsAdapter = new PlayUpFriendsAdapter ( playupFriendsData, playupfrndList,
							 vMainColor,  vMainTitleColor,vSecColor,vSecTitleColor);
					playupfrndList.setAdapter( playUpFriendsAdapter);
					playupfrndList.setSelection(currentPosition);
				} else {
					playUpFriendsAdapter.setData ( playupFriendsData, playupfrndList,vMainColor,  vMainTitleColor ,vSecColor,vSecTitleColor);
				}

			} else {
				showProgressIndicator();
			}

			if ( isDownloaded &&  ( ( playupFriendsData == null  ) || ( playupFriendsData != null && playupFriendsData.get( "vFriendId" ).size() == 0 )  )) {



				// redirecting to friends fragment 
				// also showing the dialog


				if ( isDialogShowing ) {
					dbUtil = null;
					return;
				}
				PlayupLiveApplication.getFragmentManagerUtil().setFragment( "FriendsFragment" );


				AlertDialog.Builder alertDialog = new AlertDialog.Builder( PlayUpActivity.context );
				alertDialog.setMessage( R.string.no_playup_friend_str );
				alertDialog.setNeutralButton( R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				alertDialog.setNegativeButton( R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {



						LinearLayout li = (LinearLayout) PlayUpActivity.context.findViewById( R.id.main );
						li.removeAllViews();
						li.bringToFront();
						

						// remove all the fragment from the stack;
						PlayupLiveApplication.getFragmentManagerUtil().popBackStack();

						// start the transaction
						PlayupLiveApplication.getFragmentManagerUtil().startTransaction();

						PlayupLiveApplication.getFragmentManagerUtil().setFragment(
								"TopBarFragment", R.id.topbar);
						PlayupLiveApplication.getFragmentManagerUtil().setFragment("MenuFragment");

						// end the transaction
						PlayupLiveApplication.getFragmentManagerUtil().endTransaction();
					}
				});

				alertDialog.create();
				alertDialog.show();
				isDialogShowing = true;
				
			}

			dbUtil = null;


		}catch ( Exception e  ) {
			dbUtil = null;
		}





	}


	@Override
	public void onStop() {

		super.onStop();

		playUpFriendsAdapter = null;

		if ( liveFriendsTask != null ) {
			liveFriendsTask.cancel();
			liveFriendsTask = null;
		}

		if ( liveFriendsTimer != null ) {
			liveFriendsTimer.cancel();
			liveFriendsTimer = null;
		}

		if ( updateFriendsTask != null ) {
			updateFriendsTask.cancel();
			updateFriendsTask = null;
		}

		if ( updateFriendsTimer != null ) {
			updateFriendsTimer.cancel();
			updateFriendsTimer = null;
		}

		if ( searchFriendsTimerTask != null ) {
			searchFriendsTimerTask.cancel();
			searchFriendsTimerTask = null;
		}

		if ( searchFriendsTimer != null ) {
			searchFriendsTimer.cancel();
			searchFriendsTimer = null;
		}
		
		
		
		

	}


	@Override
	public void onUpdate(final Message msg) {

		
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
								onResume();
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							Logs.show ( e );
						} 



					}
				});
			}
			return;
		}
		
		
		if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
			
			getPlayupFriendsData();
			
		}



		if ( msg != null && msg.obj!= null && msg.obj.toString().equalsIgnoreCase("SearchFriendsString") ) {
			/**
			 * making api call to search
			 */
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


										String vFriendSearchUrl= dBUtil.getPlayupFriendsSearchUrl();




										if ( vFriendSearchUrl != null && vFriendSearchUrl.trim().length() > 0 && searchString != null) {

											vFriendSearchUrl = vFriendSearchUrl.replace( "{prefix}", searchString );
											UUID ramdomString = UUID.randomUUID();

											if ( searchString != null ) {
												
												if ( runnableList != null  && Util.isInternetAvailable() ) {
													runnableList.put(ramdomString.toString(),new Util().searchPlayUpFriendsData(vFriendSearchUrl, searchString));
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
			PlayUpActivity.handler.post(new Runnable() {

				@Override
				public void run() {

					try {
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

								if ( PlayUpActivity.handler != null ) {
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

						}


						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("PlayUpFriendsData") ) {
							
							/**
							 * setting the playupFriendsData
							 */
							isDownloaded = true;
							if(!inSearch){
								setTopBar();
								setValues();
								
							}
						}

						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "liveFriendsData" ) ) {
							
							/**
							 * updating the  data of live friends(i.e show/hide availability)
							 */
							
							if(!inSearch && msg.arg1 == 1)
								setValues();

							
							refreshLiveFriends();
						}

						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "updateFriendsData" ) ) {
							
							/**
							 * as of now its similar to the live friends data.
							 */
							
							if(!inSearch && msg.arg1 == 1)
								setValues();
							
							refreshOtherFriends();
						}


						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "SearchPlayUpFriendsData" ) ) {

							/**
							 * setting the playup friends data
							 */
							if(searchString != null  && searchString.trim().length() > 0) {
								if( msg.arg1 !=1 ) {
									filterText();
								}
							}
						}

//						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "refreshOtherFriends" ) ) {
//							if ( !inSearch )
//								setValues();
//							refreshOtherFriends();
//						}
//						if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("refreshLiveFriends")){
//							if ( !inSearch ) {							
//								setValues();								
//							}
//							refreshLiveFriends();
//						}

						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "callChevron" ) ) {

							int backStackCount = PlayupLiveApplication.getFragmentManager().getBackStackEntryCount();
							BackStackEntry entry = null;
							if ( backStackCount - 2 > -1 ) {
								entry = PlayupLiveApplication.getFragmentManager().getBackStackEntryAt( backStackCount - 2 );
							}
							if ( entry != null && entry.getName().contains( "DirectConversationFragment" ) ) {
								
								PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( entry.getName() );
								entry = null;
								
							} else {
								Bundle bundle = new Bundle ();
								bundle.putString( "fromFragment", fromFragment );
								PlayupLiveApplication.getFragmentManagerUtil().setFragment( "DirectConversationFragment" , bundle );
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show ( e );
					}
				}
			});



		}




	}

	
	/**
	 * schedule for the refresh of live friends
	 */

	public void refreshLiveFriends() {

		try{
		
		if (liveFriendsTimer == null) {
			
		liveFriendsTimer = new Timer();
		}

		if ( liveFriendsTask ==  null ) {
			

			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			

			final Hashtable<String, Object> result = dbUtil.getPlayupLiveFriendsUrl();
			final String liveFriendsUrl =	(String) result.get("url");
			final Boolean isHref		=	(Boolean) result.get("isHref");

			int liveFriendscacheTime = Integer.parseInt(dbUtil.getCacheTime(liveFriendsUrl));
			
			
		liveFriendsTask = new TimerTask() {



			@Override
			public void run() {

				try {
					if(runnableList!=null&&!runnableList.containsKey(Constants.GET_LIVE_FRIENDS) ){
						if (Util.isInternetAvailable()) {


							runnableList.put(Constants.GET_LIVE_FRIENDS, new Util().getLiveFriends(liveFriendsUrl,isHref, true, runnableList));
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show ( e );
				}
			}
		};

		if(liveFriendscacheTime > 0){

			liveFriendsTimer.schedule(liveFriendsTask, (liveFriendscacheTime * 1000), (liveFriendscacheTime * 1000));
		}else{
			
			liveFriendsTimer = null;
			liveFriendsTask = null;
		}
		
		dbUtil = null;
	}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show ( e );
		}

	}


	/**
	 * schedule for the refresh of 'update friends url'
	 */
	
	public void refreshOtherFriends() {

		try{
		

		if (updateFriendsTimer == null) {
			
			updateFriendsTimer = new Timer();
			
		}
		

		if ( updateFriendsTask ==  null ) {
			
			
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			

			final Hashtable<String, Object> result = dbUtil.getPlayupUpdateFriendsUrl();
			final String updateFriendsUrl = (String) result.get("url");
			final Boolean isHref  = (Boolean) result.get("isHref");


			int updateFriendscacheTime = Integer.parseInt(dbUtil.getCacheTime(updateFriendsUrl));
			
			updateFriendsTask = new TimerTask() {

			@Override
			public void run() {
				try {
					if(runnableList!=null&&!runnableList.containsKey(Constants.GET_UPDATE_FRIENDS)){

						if (Util.isInternetAvailable()) {
							runnableList.put(Constants.GET_UPDATE_FRIENDS, new Util().getUpdateFriends(updateFriendsUrl,isHref, true, runnableList));
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show ( e );
				}
			}
		};



		if(updateFriendscacheTime > 0){

			updateFriendsTimer.schedule(updateFriendsTask, (updateFriendscacheTime * 1000 + 13000 ), (updateFriendscacheTime * 1000));
		}else{
			
			updateFriendsTimer = null;
			updateFriendsTask = null;
			
			
			
		}
			dbUtil = null;
		}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show ( e );
		}
		
	}

	/**
	 * fetching and displaying the search friends data
	 */

	public void filterText(){


		try {
			inSearch = true;
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();

			Hashtable<String, List<String>> playupFriendsData = dbUtil.searchPlayupFriendsData();
			dbUtil = null;

			if ( playupFriendsData == null || (playupFriendsData != null && playupFriendsData.size() == 0) || (playupFriendsData != null && playupFriendsData.get("vFriendId").size() == 0)) {
				playUpFriendsAdapter.setDummyData( 6 );
			}  else {
				playUpFriendsAdapter.setData(playupFriendsData, playupfrndList,  true,vMainColor,  vMainTitleColor ,vSecColor,vSecTitleColor);
			}

			playupfrndList.setSelection( 0 );

			Message msg = new Message();
			msg.obj = "Dismiss progress";
			PlayupLiveApplication.callUpdateTopBarFragments(msg);

		} catch ( Exception e) {

		}


	}

}
