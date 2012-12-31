package com.playup.android.fragment;

import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.os.Bundle;
import android.os.Message;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.PrivateLobbyInviteFriendsAdapter;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;

import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Util;

public class PrivateLobbyInviteFriendFragment extends MainFragment {

	private ListView listView;

	private String searchString = "";


	private PrivateLobbyInviteFriendsAdapter inviteFriendsAdapter;

	private String vConversationId = null;
	private boolean isAgainActivated = false;
	
	private TimerTask liveFriendsTask;
	private Timer liveFriendsTimer ;


	private TimerTask searchFriendsTimerTask;
	private Timer searchFriendsTimer = new Timer();

	private boolean inSearch = false;
	private LinearLayout progressFriends;

	public boolean isDownloaded = false;

	private static int currentPosition = 0;
	public static boolean loadingData = true;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;


	RelativeLayout content_layout;
	
	
	Hashtable<String,List<String>> allFriendsData;

	private TimerTask friendsTimerTask;

	private Timer friendsTimer;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		
		content_layout = (RelativeLayout) inflater.inflate( R.layout.invite_friends, null);

		// get from bundle
		if (!isAgainActivated) {
			setConversationId(getArguments());
		}
		

		return content_layout;
	}

	
	@Override
	public void onStop () {
		super.onStop();

		searchString = null;
		vConversationId = null;
		
		if ( allFriendsData != null ) {
			allFriendsData.clear();
			allFriendsData = null;
		}
		if ( searchFriendsTimerTask != null ) {
		searchFriendsTimerTask.cancel();
		searchFriendsTimerTask = null;
	}
	
	if ( searchFriendsTimer != null ) {
		searchFriendsTimer.cancel();
		searchFriendsTimer = null;
	}
	
	if ( friendsTimerTask != null ) {
		friendsTimerTask.cancel();
		friendsTimerTask = null;
	}
	
	if ( friendsTimer != null ) {
		friendsTimer.cancel();
		friendsTimer = null;
	}
	
	

	if ( liveFriendsTask != null ) {
		liveFriendsTask.cancel();
		liveFriendsTask = null;
	}

	if (liveFriendsTimer != null ) {
		liveFriendsTimer.cancel();
		liveFriendsTimer = null;
	}

		
		
	}

	@Override
	public void onResume () {
		super.onResume();

		inviteFriendsAdapter = null;
		initialize( content_layout );
		
		searchString = null;


		getFriendsData();
		
		setTopBar();
		setValues ();
		
		

	}
	
	/**
	 * refreshing all the friends data
	 */
	
	public void refreshAllFriends() {
		if (friendsTimer == null) {

			friendsTimer = new Timer();
		}

		if ( friendsTimerTask ==  null ) {
			
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			final String friendsUrl= dbUtil.getFriendsUrl();
			
			friendsTimerTask = new TimerTask() {
				@Override
				public void run() {
					try {
						if(runnableList!=null && !runnableList.containsKey(Constants.GET_FREINDS_DATA)){
							if (Util.isInternetAvailable()) {
								runnableList.put(Constants.GET_FREINDS_DATA,new Util().getFriendsData(runnableList, true));
							}
						}
					} catch (Exception e) {
						
					//	Logs.show ( e );
					}
				}
			};
			
			
			
			
			int friendsCacheTime = Integer.parseInt(dbUtil.getCacheTime(friendsUrl));
			
			

			if(friendsCacheTime > 0){
				friendsTimer.schedule(friendsTimerTask, (friendsCacheTime * 1000), (friendsCacheTime * 1000));
			}else{
				friendsTimerTask = null;
				friendsTimer = null;
			}

		}
		
	}

	
	/**
	 * making api call to fetch the list of friends
	 */

	private void getFriendsData(){
		try{
		if(runnableList != null && !runnableList.containsKey(Constants.GET_FREINDS_DATA)){
			if (Util.isInternetAvailable()) {

				
				runnableList.put(Constants.GET_FREINDS_DATA, new Util().getFriendsData(runnableList,false));
			}
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	Logs.show ( e );
		}

	}

	// setting the conversation id
	private void setConversationId(Bundle bundle) {
		
		vMainColor = null;
		vMainTitleColor = null;
		

		vSecColor = null;
		vSecTitleColor = null;

		if (bundle != null && bundle.containsKey("vConversationId")) {
			vConversationId = bundle.getString("vConversationId");
		} else {
			vConversationId = null;
		}if (bundle != null &&bundle.containsKey("vMainColor")) {
			vMainColor = bundle.getString("vMainColor");
		}if (bundle != null && bundle.containsKey("vMainTitleColor")) {
			vMainTitleColor = bundle.getString("vMainTitleColor");
		}if (bundle != null && bundle.containsKey("vSecColor")) {
			vSecColor = bundle.getString("vSecColor");
		}if (bundle != null && bundle.containsKey("vSecTitleColor")) {
			vSecTitleColor = bundle.getString("vSecTitleColor");
		}
	}

	@Override
	public void onAgainActivated(Bundle args) {

		isAgainActivated = true;
		setConversationId(args);

	}
	
	/**
	 * setting the title and color of topbar
	 */
	
	private void setTopBar () {
		
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					
					String friendsUrl= dbUtil.getFriendsUrl();
					
					
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
					
					PlayupLiveApplication.callUpdateTopBarFragments(msg);
				} catch (Exception e) {
					//Logs.show(e);
				}
				
			}
		});
		
		
	}

	/**
	 * initializing the views
	 * @param conten_layout
	 */

	private void initialize (RelativeLayout conten_layout){
		listView = ( ListView ) conten_layout.findViewById( R.id.inviteFriendList );
		progressFriends = (LinearLayout) conten_layout.findViewById(R.id.progressFriends);

	}
	
	/**
	 * setting the values
	 */

	private void setValues () {
		try{
		if ( inviteFriendsAdapter == null ) {
			inviteFriendsAdapter = new PrivateLobbyInviteFriendsAdapter( vConversationId ,listView, this ,
					vMainColor,vMainTitleColor,vSecColor,vSecTitleColor);
			listView.setAdapter( inviteFriendsAdapter);
			listView.setSelection(currentPosition );
		} else {
			inviteFriendsAdapter.setData ( vConversationId,listView, this,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor );
		}
		}catch(Exception e){
			
		}
	}
	
	
	/**
	 * scheduling the refresh of only live friends data
	 */
	
	
	public void refreshLiveFriends() {

		try{
		if (liveFriendsTimer == null) {
			
		liveFriendsTimer = new Timer();
		}

		if ( liveFriendsTask ==  null ) {
			

			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			

			final Hashtable<String, Object> result = dbUtil.getLiveFriendsUrl();
			final String liveFriendsUrl = (String) result.get("url");
			final boolean isHrefUrl		= (Boolean) result.get("isHref");

			int liveFriendscacheTime = Integer.parseInt(dbUtil.getCacheTime(liveFriendsUrl));
			
			
		liveFriendsTask = new TimerTask() {



			@Override
			public void run() {

				try {
					
					
					if(runnableList!=null && !runnableList.containsKey(Constants.GET_LIVE_FRIENDS) ){
						if (Util.isInternetAvailable()) {

							runnableList.put(Constants.GET_LIVE_FRIENDS, new Util().getLiveFriends(liveFriendsUrl,isHrefUrl, false, runnableList));
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
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
			//Logs.show ( e );
		}

	}




	@Override
	public void onUpdate(final Message msg) {
		
		try {
			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("inSearch") ) {

				searchString = "";

			}

			

			if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
				
				//Praveen : Following are the APIs getting called from on Resume
				getFriendsData();
				setTopBar();
				setValues ();
				
				
			}
			
			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("handleBackButton") ) {


				if(searchString != null){

					
					searchString =  null;
					
					inSearch = false;
					Message msg1 = new Message();
					msg1.obj = "Show Friends";
					PlayupLiveApplication.callUpdateTopBarFragments(msg1);
					setValues();

					//	return;
				}

				else{
					String  topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
					PlayupLiveApplication.getFragmentManagerUtil().popBackStack(topFragmentName);

					Message msg1 = new Message();
					msg1.obj = "SHOW_GALLERY";

					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar( msg1 );
					return;
				}

			}



			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("SearchFriendsString") ) {
				/**
				 * making api call to get the search data
				 * 
				 */
				if ( msg.getData() != null ) {

					Bundle bundle = msg.getData();
					if ( bundle.containsKey( "search_value") ) {
						searchString = bundle.getString( "search_value" ); 

						
						if ( searchString.trim().length() == 0 ) {

							if ( searchFriendsTimer != null ) {
								searchFriendsTimer.cancel();
								searchFriendsTimer = null;
							}
							searchFriendsTimer = new Timer();

							if ( searchFriendsTimerTask != null ) {
								searchFriendsTimerTask.cancel();
								searchFriendsTimerTask = null;
							}
							if ( PlayUpActivity.handler != null ) {
								PlayUpActivity.handler.post( new Runnable() {

									@Override
									public void run() {
										
										try {
											inSearch = false;
											Message msg = new Message();
											
											msg.obj = "Dismiss progress";
											PlayupLiveApplication.callUpdateTopBarFragments(msg);

											if ( !isVisible() ) {
												return;
											}
											setValues();
										} catch (Exception e) {
											// TODO Auto-generated catch block
										//	Logs.show ( e );
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

											if ( vFriendSearchUrl != null && vFriendSearchUrl.trim().length() > 0 ) {

												UUID ramdomString = UUID.randomUUID();
												if ( searchString != null ) {
													boolean isHref = ((Boolean) result.get("isHref")).booleanValue();
													if (  runnableList != null && Util.isInternetAvailable() ) {
														runnableList.put(ramdomString.toString(),new Util().searchFriends( vFriendSearchUrl, searchString,isHref ));
													}
												}

											}
											dBUtil = null;

										}
									} catch (Exception e) {
										// TODO Auto-generated catch block
									//	Logs.show ( e );
									}
								}
							};

							searchFriendsTimer.schedule(searchFriendsTimerTask, 1000);
							return;

						}
					}
				}

				return;
			}
			if ( PlayUpActivity.handler != null ) {

				PlayUpActivity.handler.post( new Runnable( ) {



					@Override
					public void run() {

						try {
							if ( !isVisible() ) {
								return;
							}
							if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "SearchFriendsData" ) ) {
								
								/**
								 * setting the search friend data
								 */
								if(searchString != null &&  searchString.trim().length() > 0)
								filterText();
							} else if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "invitationSent" ) ) {
								/**
								 * updating the UI to indicate invitation is sent
								 */
								
								if(inSearch)
									filterText();
								else
									setValues();
							} 



							else if( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "FriendsData")){
								/**
								 * setting the values of friends data
								 */
								if(msg.arg1 == 1) {
									//PlayupLiveApplication.showToast(R.string.notification_err);
								}
								isDownloaded = true;
								if(!inSearch){
									setTopBar();
									setValues();
								}
								
								refreshAllFriends();
								refreshLiveFriends();
								
								
							}if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "liveFriendsData" ) ) {
								
								/**
								 * setting the values of live friends data
								 */
								
								
								if(msg.arg1 == 1){								
								if ( !inSearch ) {								
									setValues();										
								}
							}
								refreshLiveFriends();
							}
							if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("refreshAllFriends")){
								
								/**
								 * refreshing all the friends data
								 */
								
								if ( !inSearch ) {
									setValues();
								}
								refreshAllFriends();
							}
							
							else if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("EmptyFriendsData")){
								
								/**
								 * incase of absence of friends url(i.e,the user profile data is in the process of downloading)
								 *  for the primary user,we keep hitting the function
								 * 'getFriendsData() again and again
								 */
								
								getFriendsData();

							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
						//	Logs.show ( e );
						}
					}
				});
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
		//Logs.show(e);
		}

	}

	
	/**
	 * setting the search friends data
	 */

	private void filterText() {

		try{
		inSearch = true;

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		
		allFriendsData = dbUtil.searchConversationAllFriendsData(vConversationId);

		if ( inviteFriendsAdapter == null ) {
			inviteFriendsAdapter = new PrivateLobbyInviteFriendsAdapter( vConversationId ,listView, this ,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor);
			listView.setAdapter( inviteFriendsAdapter);
		}


		if ( allFriendsData == null ) {
			inviteFriendsAdapter.setDummyData( 6, this );
		} else if ( allFriendsData.size() == 0  ) {
			inviteFriendsAdapter.setDummyData( 6, this );
		} else {
			inviteFriendsAdapter.setData( allFriendsData, listView, this );
		}


		listView.setSelection( 0 );
		Message msg = new Message();
		
		msg.obj = "Dismiss progress";
		PlayupLiveApplication.callUpdateTopBarFragments(msg);


		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	Logs.show ( e );
		}
	}



	/**
	 * showing the progress dialog
	 * @param show
	 */
	public void showHideProgress( boolean show) {
		if( !Util.isInternetAvailable() ) 
			show = false;

		if( show ) {
			loadingData = true;
			listView.setVisibility(View.INVISIBLE);
			progressFriends.setVisibility(View.VISIBLE);
		} else {
			loadingData = false;
			listView.setVisibility(View.VISIBLE);
			progressFriends.setVisibility(View.GONE);
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		currentPosition = listView.getFirstVisiblePosition();
	}
}
