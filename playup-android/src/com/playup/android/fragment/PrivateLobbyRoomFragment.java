package com.playup.android.fragment;



import java.util.ArrayList;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.util.DisplayMetrics;


import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.FriendGalleryAdapter;
import com.playup.android.adapters.MatchGalleryAdapter;
import com.playup.android.adapters.MatchHeaderGenerator;
import com.playup.android.adapters.PrivateMessagesAdapter;
import com.playup.android.adapters.ProviderAdapter;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;

import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;

import com.playup.android.util.Util;

import com.playup.android.util.json.JsonUtil;




/**
 * Match Home Fragment
 */
public class PrivateLobbyRoomFragment extends MainFragment implements OnClickListener, OnItemClickListener, OnTouchListener {

	private ImageView editRoom;
	private ImageView write_post;
	private TextView lobbyMessage;
	private RelativeLayout stripDown,stripUp;
	private TextView liveCountUp,liveCountDown;
	private Gallery matchGallery;
	private String vConversationUrl;
	public static boolean isSubjectDownloading;
	boolean isConversationUrlHref = false;
	static String vContestId = null;

	private LinearLayout addFriendsLayout;
	private ImageView addFriendImage;
	private ListView listView;
	private ImageView addFrnds;
	private LinearLayout noMessages;

	private PrivateMessagesAdapter adapter;

	private boolean onclick = false;

	private  Hashtable<String,TimerTask> refreshMatchesTask = new Hashtable<String,TimerTask>();
	private  Hashtable<String,Timer> refreshMatchesTimer = new Hashtable<String,Timer>();

	private TimerTask pollingTask;
	private TimerTask refreshMessagesTask;
	private TimerTask refreshConversationTask;
	private TimerTask refreshfriendsTask;
	private TimerTask contestTask = null;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;


	private Timer contestTimer = null;
	private Timer pollingTimer = new Timer();
	private Timer refreshMessagesTimer = null;
	private Timer refreshConversationTimer = null;
	private Timer refreshFriendsTimer = null;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;


	private TextView messageIn;
	private boolean isDownloading = false;
	private ArrayList<String> follow ;

	private RelativeLayout  noFriendsView;

	private String vConversationId = null;
	private String fromFragment = null;

	private boolean isAgainActivated = false;

	private Gallery friendGallery;
	private FriendGalleryAdapter friendGalleryAdapter;

	private boolean  addFriendsLayoutisClicked = false;
	private LinearLayout content_layout ;

	private String vConversationName = "";

	private MatchGalleryAdapter matchGalleryAdapter;
	private Dialog shareDialog;
	String token;

	private float  yPosition = 0;
	RelativeLayout matchHeaderLayout;
	LayoutInflater inflater ;

	private String prevSportType = null;
	@Override
	public void onStop () {

		super.onStop();


		if ( pollingTask != null ) {
			pollingTask.cancel();
			pollingTask = null;
		}

		if ( pollingTimer != null ) {
			pollingTimer.cancel();
			pollingTimer = null;
		}

		if ( refreshMessagesTask != null ) {
			refreshMessagesTask.cancel();
			refreshMessagesTask = null;
		}

		if ( refreshMessagesTimer != null ) {
			refreshMessagesTimer.cancel();
			refreshMessagesTimer = null;
		}

		if ( refreshConversationTask != null ) {
			refreshConversationTask.cancel();
			refreshConversationTask = null;
		}

		if ( refreshConversationTimer != null ) {
			refreshConversationTimer.cancel();
			refreshConversationTimer = null;
		}


		if (refreshfriendsTask != null) {
			refreshfriendsTask.cancel();
			refreshfriendsTask = null;
		}
		if ( refreshFriendsTimer != null ) {
			refreshFriendsTimer.cancel();
			refreshFriendsTimer = null;
		}

		if (contestTask != null) {
			contestTask.cancel();
			contestTask = null;
		}

		if ( contestTimer != null ) {
			contestTimer.cancel();
			contestTimer = null;

		}


		Iterator it = refreshMatchesTask.values().iterator();
		while(it.hasNext()){
			((TimerTask) it.next()).cancel();
		}


		it = refreshMatchesTimer.values().iterator();
		while(it.hasNext()){
			((Timer) it.next()).cancel();
		}
		refreshMatchesTask.clear();
		refreshMatchesTimer.clear();


	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		try {

			content_layout = (LinearLayout) inflater.inflate( R.layout.private_lobby_room, null);

			// get from bundle
			if (!isAgainActivated) {
				setConversationId(getArguments());
			}
			DatabaseUtil mDatabaseUtil	=	DatabaseUtil.getInstance();
			token = mDatabaseUtil.getUserToken();

			return content_layout;

		} catch  ( Exception e ) {
			//Logs.show ( e );
			return content_layout;
		} catch ( Error e ) {
			//Logs.show ( e );
			return content_layout;
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
		}



		if (bundle != null && bundle.containsKey("fromFragment")) {
			fromFragment = bundle.getString("fromFragment");
		} else {
			fromFragment = null;
		}if (bundle != null && bundle.containsKey("vMainColor")) {
			vMainColor = bundle.getString("vMainColor");
		}if (bundle != null && bundle.containsKey("vMainTitleColor")) {
			vMainTitleColor = bundle.getString("vMainTitleColor");
		}if (bundle != null && bundle.containsKey("vSecColor")) {
			vSecColor = bundle.getString("vSecColor");
		}if (bundle != null && bundle.containsKey("vSecTitleColor")) {
			vSecTitleColor = bundle.getString("vSecTitleColor");
		}

	}
	/**
	 * setting the information about the last followed /currently following contest of the user
	 */



	
	/**
	 * send the information of upto which the messages were viewed (to handle gaps)
	 */

	private  void putMarker () {
		try{
		new Util().privateLobbyPutDeleteMarker( true , vConversationId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	Logs.show ( e );
		}
	}

	
	/**
	 * asking the server to delete the previous marker information
	 */

	private void deleteMarker () {
		try{
		// update the count in recent activity.
		ContentValues values = new ContentValues();
		values.put( "iUnRead",  0 );
		JsonUtil json  =new JsonUtil();
		json.queryMethod1( Constants.QUERY_UPDATE, null, "recent", values, " vSubjectId = \"" + vConversationId + "\" ", null, false, true );



		new Util().privateLobbyPutDeleteMarker( false , vConversationId);
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
		//	Logs.show ( e );
		}
	}
	
	/**
	 * informing the server that the user is in a particular room (for recent activites)
	 * @param isPresence - true(user has entered/staying in the room)/false(user has left the room)
	 */


	private void doPresenceDeleteCall(final boolean isPresence){

		Runnable r =  new Runnable () {

			@Override
			public void run() {
				try {
					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT vPresenceUrl,vPresenceHrefUrl FROM friendConversation WHERE vConversationId = \"" + vConversationId + "\" ");
					if (c != null) {

						if (c.getCount() > 0) {
							c.moveToFirst();
							
							String vPresenceUrl = c.getString(c.getColumnIndex("vPresenceHrefUrl"));
							if(vPresenceUrl != null && vPresenceUrl.trim().length() > 0)
							{
								new Util().makePresenceDeleteCall(vPresenceUrl, isPresence,true);
							}else{
								vPresenceUrl = c.getString(c.getColumnIndex("vPresenceUrl"));
								new Util().makePresenceDeleteCall(vPresenceUrl, isPresence,false);
							}
						}
						c.close();
						c = null;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();
	}


	public void onResume() {
		super.onResume();
		try{
		matchHeaderGenerator= null;
		follow = new ArrayList<String>();
		isSubjectDownloading = false;

		vContestId = null;
		initialize(content_layout);

		isDownloading = false;
		matchGalleryAdapter = null;
		adapter = null;
		friendGalleryAdapter = null;
		// getting the lobby conversation data
		getConversation();

		refreshMatchesTask = new Hashtable<String, TimerTask>();
		refreshMatchesTimer = new Hashtable<String, Timer>();

		if(matchGalleryAdapter == null){
			matchGalleryAdapter = new MatchGalleryAdapter(matchGallery);
			matchGallery.setAdapter(matchGalleryAdapter);
		}

		else{
			matchGalleryAdapter.setData();
		}

		matchGallery.setOnItemClickListener( matchGalleryItemListener );





		// set values
		setValues();

		doPolling();

		deleteMarker();

		getLiveMatchesCount();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show ( e );
		}
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		if( shareDialog != null && shareDialog.isShowing() ) {
			shareDialog.dismiss();
			shareDialog = null;
		}

	}
	
	/**
	 * scheduling the presence/delete call
	 */

	public void doPolling() {
		try{
		if ( pollingTask != null ) {
			pollingTask.cancel();
			pollingTask = null;
		}

		if ( pollingTimer != null ) {
			pollingTimer.cancel();
			pollingTimer = null;
		}

		pollingTimer = new Timer();
		pollingTask = new TimerTask() {
			public void run() {

				try {
					doPresenceDeleteCall(true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}
			}

		};

		pollingTimer.schedule(pollingTask, 300, 6000);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show ( e );
		}

	}

	
	/**
	 * stopping all the timers
	 */
	public void stopPolling() {
		
		try{

		/**
		 * Timer Stopping for Messages
		 */

		if (pollingTask != null) {
			if (pollingTask.cancel()) {
				doPresenceDeleteCall(false);
			}
		}



		if ( pollingTimer != null ) {
			pollingTimer.cancel();
			pollingTimer = null;
		}

		if ( refreshMessagesTask != null ) {
			refreshMessagesTask.cancel();
			refreshMessagesTask = null;
		}

		if ( refreshMessagesTimer != null ) {
			refreshMessagesTimer.cancel();
			refreshMessagesTimer = null;
		}


		if (refreshfriendsTask != null) {
			refreshfriendsTask.cancel();
			refreshfriendsTask = null;
		}
		if ( refreshFriendsTimer != null ) {
			refreshFriendsTimer.cancel();
			refreshFriendsTimer = null;
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show ( e );
		}

	}
	@Override
	public void onAgainActivated(Bundle args) {

		isAgainActivated = true;
		setConversationId(args);

	}
	
	/**
	 * initializing the views
	 * @param content_layout
	 */

	private void initialize(LinearLayout content_layout) {


		try {
			if(content_layout!=null){
				addFriendImage = (ImageView) content_layout.findViewById(R.id.addFriends);

				noMessages = (LinearLayout) content_layout.findViewById(R.id.noMessages);

				addFriendsLayout = ( LinearLayout ) content_layout.findViewById(R.id.addFriendsLayout);
				listView = (ListView) content_layout.findViewById(R.id.userChatListView);

				addFrnds = (ImageView) content_layout.findViewById(R.id.addFrnds);

				friendGallery = (Gallery) content_layout.findViewById(R.id.friendGallery);


				stripDown = (RelativeLayout) content_layout.findViewById(R.id.stripDown);
				stripDown.setVisibility(View.VISIBLE);
				stripUp = (RelativeLayout) content_layout.findViewById(R.id.stripUp);
				matchGallery = (Gallery) content_layout.findViewById(R.id.matchGallery);

				noFriendsView = ( RelativeLayout ) content_layout.findViewById( R.id.noFriendsView );
				write_post = (ImageView)content_layout.findViewById(R.id.write_post);

				messageIn = ( TextView )content_layout.findViewById( R.id.messageIn);
				editRoom = (ImageView)content_layout.findViewById(R.id.editRoom);


				liveCountDown = (TextView)content_layout.findViewById(R.id.liveCountDown);
				liveCountUp = (TextView)content_layout.findViewById(R.id.liveCountUp);
				matchHeaderLayout = ( RelativeLayout) content_layout.findViewById(R.id.matchHeaderLayout);
				
				lobbyMessage = new TextView(PlayUpActivity.context);
				lobbyMessage.setGravity(Gravity.CENTER);
				lobbyMessage.setTextSize(20 );
				lobbyMessage.setTextColor( Color.parseColor("#696B6E"));
				lobbyMessage.setTypeface(Constants.BEBAS_NEUE);
				lobbyMessage.setText( R.string.privateLobbyRoomMessage);
				
				liveCountDown.setTypeface( Constants.BEBAS_NEUE );
				liveCountUp.setTypeface( Constants.BEBAS_NEUE );
				// set listeners
				setListeners();

				// setTypefaces
				setTypefaces();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show(e);
		}


	}
	
	/**
	 * setting the listeners
	 */

	private void setListeners() {

		addFriendImage.setOnClickListener(this);
		addFrnds.setOnClickListener(this);
		write_post.setOnClickListener( this );
		editRoom.setOnClickListener(this);


		stripDown.setOnTouchListener(this);
		stripUp.setOnTouchListener(this);
	}

	/**
	 * cancelling the progress dialog.
	 */
	private void cancelProgressDialog() {

		if (noMessages != null) {
			noMessages.setVisibility(View.GONE);
		}
	}
	
	/**
	 * showing the progress dialog
	 */

	private void showProgressDialog() {

		if( !Util.isInternetAvailable() ) {
			cancelProgressDialog();
			return;
		}

		if (noMessages != null) {
			noMessages.setVisibility(View.VISIBLE);
		}

		if(noFriendsView != null){
			noFriendsView.setVisibility(View.GONE);

		}
		if (listView != null) {
			listView.setVisibility(View.GONE);
		}

	}
	
	/**
	 * call the respective functions to display data on the various parts of the screen
	 */

	private void setValues() {

		try {


			setHeaders ();

			setEditRoomImage();

			setRoomNameInTopBar();

			// set the room messages
			setRoomMessages();			
		} catch ( Exception e ) {
		//	Logs.show ( e ); 
		}
	}





	


	
	
	/**
	 * showing lobby message, it will remove all the views from the header view
	 * and adds the lobby message textview with alignment
	 */
	public void showLobbyMessage() {
		if( matchHeaderLayout!=null ) {
			matchHeaderLayout.removeAllViews();
			
			LayoutParams params = new RelativeLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			matchHeaderLayout.setBackgroundResource(R.drawable.ticket_texture);
			matchHeaderLayout.setPadding( 0, 0, 0, 0 );
			params.addRule(RelativeLayout.CENTER_IN_PARENT);			
			matchHeaderLayout.addView( lobbyMessage ,params);
		}
	}

	/**
	 * setting the typefaces for various views
	 */
	private void setTypefaces() {
		messageIn.setTypeface( Constants.OPEN_SANS_REGULAR);
		liveCountDown.setTypeface( Constants.BEBAS_NEUE );
		liveCountUp.setTypeface( Constants.BEBAS_NEUE );
		
	}

	/**
	 * setting editroom image
	 */
	private void setEditRoomImage() {

		Runnable r = new Runnable () {

			@Override
			public void run() {

				try {
					int editName_temp = 0 ;
					if( vConversationId != null ) {

						Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT editName FROM friendConversation WHERE vConversationId = \"" + vConversationId + "\" ");
						if (c != null) {

							if (c.getCount() > 0) {
								c.moveToFirst();
								editName_temp = c.getInt(c.getColumnIndex("editName"));
							}
							c.close();
							c = null;
						}
					}

					final int editName = editName_temp;
					if ( PlayUpActivity.handler != null )  {
						PlayUpActivity.handler.post( new  Runnable () {

							@Override
							public void run() {

								if ( !isVisible() ) {
									return;
								}
								if( editName == 1 ) {
									editRoom.setVisibility(View.VISIBLE);
								} else {
									editRoom.setVisibility(View.GONE);
								}

							}

						});
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}

			}

		};

		Thread th = new Thread ( r );
		th.start();


	}

	/**
	 * setting the room in the top bar fragment.
	 **/
	private void setRoomNameInTopBar() {

		Runnable r = new Runnable () {

			@Override
			public void run() {

				try {
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					
					
					String vChildColor = dbUtil.getSectionMainColor(vConversationId, "");
					String vChildTitleColor = dbUtil.getSectionMainTitleColor(vConversationId, "");
					
					if(vChildColor != null && vChildColor.trim().length() > 0 )
						vMainColor = vChildColor;
					
					if(vChildTitleColor != null && vChildTitleColor.trim().length() > 0 )
						vMainTitleColor = vChildTitleColor;
					
					String vChildSecondaryColor = dbUtil.getSectionSecondaryColor (vConversationId,"" );
					String vChildSecondaryTitleColor = dbUtil.getSectionSecondaryTitleColor ( vConversationId,"" );
					
					 
					 if(vChildSecondaryColor != null && vChildSecondaryColor.trim().length() > 0)
						 vSecColor = vChildSecondaryColor;
					 
					 if(vChildSecondaryTitleColor != null && vChildSecondaryTitleColor.trim().length() > 0)
						 vSecTitleColor = vChildSecondaryTitleColor;
						
					
					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery("SELECT vConversationName FROM friendConversation WHERE vConversationId = \""+ vConversationId + "\" ");
					if (c != null) {

						if (c.getCount() > 0) {
							c.moveToFirst();
							HashMap<String, String> map = new HashMap<String, String>();
							vConversationName = c.getString(c.getColumnIndex("vConversationName"));
							map.put("vConversationName", vConversationName );
							Bundle b = new Bundle();
							b.putString("vMainColor",vMainColor );
							b.putString("vMainTitleColor",vMainTitleColor );
							Message msg = new Message();
							msg.obj = map;
							
							msg.setData(b);
							PlayupLiveApplication.callUpdateTopBarFragments(msg);
						}
						c.close();
						c = null;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}

			}

		};
		Thread th = new Thread ( r );
		th.start();

	}

	/**
	 * Setting the room messages
	 */
	private void setRoomMessages() {


		try {
			

			Runnable r = new Runnable () {

				@Override
				public void run() {

					try {
						final Hashtable<String, List<String>> data = PlayupLiveApplication.getDatabaseWrapper().select( "SELECT fm.vMessageId,fm.vMessage,fm.vSelfUrl ,fm.vUserId,fm.vDisplayName,fm.vAvatarUrl," +
								"fm.vConversationMessageId,fm.vSubjectId,fm.vSubjectTitle,fm.vSubjectUrl,fm.vConversationMessageHrefUrl,fm.vHrefUrl,fm.subjectHrefUrl,fm.vContentUrlHref," +
								"fm.vConversationMessageUrl,fm.vCreatedDate, fm.vGapId, fm.iGapSize, fm.vContentUrl, fm.isGapLoading FROM friendMessage fm " +
								"LEFT JOIN friendConversationMessage fcm " +
								"ON fm.vConversationMessageId  = fcm.vConversationMessageId  LEFT JOIN " +
								"friendConversation fc ON fcm.vConversationId  = fc.vConversationId WHERE " +
								"fc.vConversationId = '"+vConversationId+"' ORDER BY fm.vCreatedDate DESC");

						deleteMarker();

						if(!isDownloading && ( data== null || ( data != null && data.get( "vMessageId" ) != null && data.get( "vMessageId" ).size() == 0)) ){

							if ( PlayUpActivity.handler != null ) {
								PlayUpActivity.handler.post( new Runnable () {

									@Override
									public void run() {


										if ( !isVisible() ) {
											return;
										}


										cancelProgressDialog ();

										listView.setVisibility(View.GONE);
										noFriendsView.setVisibility(View.VISIBLE);

									}

								});
							}


						} else if( data != null && data.get( "vMessageId" ) != null && data.get( "vMessageId" ).size() > 0){	



							if ( PlayUpActivity.handler != null ) {
								PlayUpActivity.handler.post( new Runnable () {

									@Override
									public void run() {

										if ( !isVisible() ) {
											return;
										}
										//									updateRecentConversationCount();
										cancelProgressDialog();
										listView.setVisibility(View.VISIBLE);
										noFriendsView.setVisibility(View.GONE);

										if ( adapter != null) {
											adapter.setData( data, vConversationId,listView, vConversationName,
													vMainColor,vMainTitleColor,vSecColor,vSecTitleColor );
										} else {
											adapter = new PrivateMessagesAdapter( data, vConversationId,listView, 
													vConversationName,vMainColor,vMainTitleColor ,vSecColor,vSecTitleColor);
											listView.setAdapter(adapter);
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

			};
			Thread th = new Thread ( r) ;
			th.start();


		} catch ( Exception e ) {
			//Logs.show ( e );
		}
	}
	
	/**
	 * checking if the contest shown in the header is live and if so scheduling the refresh
	 */


	private void checkForRefresh(){

		if ( ( vContestId != null && vContestId.trim().length() > 0 ) ) {

			Runnable r = new Runnable () {

				@Override
				public void run() {

					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery("SELECT dScheduledStartTime, dStartTime, dEndTime FROM contests WHERE vContestId = \"" + vContestId + "\" ");
					try {

						if (c != null && c.getCount() > 0) {
							c.moveToFirst();
							String output = null;
							try {
								output = new DateUtil().Match_TimeRoomFragment(c.getString(c.getColumnIndex("dStartTime")), c.getString(c.getColumnIndex("dEndTime")), c.getString(c.getColumnIndex("dScheduledStartTime")));
								if ( output != null && output.trim().length() != 0 ) {
								} else {
									refreshContest();
								}
							} catch ( Exception e ) {
							//	Logs.show ( e );
							}
						}
					} catch ( Exception e ) {
						//Logs.show ( e );
					}
					if(c != null)
					{
						c.close();
						c = null;
					}

				}

			};
			Thread th = new Thread ( r );
			th.start();

		}
	}



	/**
	 * setting the information about the last followed /currently following contest of the user
	 */



	private void setHeaders () {

		setLiveCount( true );




		Runnable r = new Runnable () {



			@Override
			public void run() {
				try {
					
				//	Log.e("123","set Headers >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  ");
					
					Hashtable<String, List<String>> contestData = PlayupLiveApplication.getDatabaseWrapper().select( " SELECT vSubjectId, vSubjectUrl,vSubjectHrefUrl " +
							"FROM  friendConversation " +
							"WHERE vConversationId = \"" + vConversationId + "\" LIMIT 0, 1 " );

					

					if ( contestData != null && contestData.get( "vSubjectId") != null && contestData.get( "vSubjectId").size() > 0 && 
							contestData.get( "vSubjectId").get( 0 ) != null && contestData.get( "vSubjectId").get( 0 ).trim().length() > 0)  {
						vContestId = contestData.get( "vSubjectId").get( 0 );
						//Log.e("123","set Headers vContestId >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+vContestId);
						
					//	Log.e("123","set Headers vContestId ( vSubjectUrl).get( 0 )) >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+contestData.get( "vSubjectUrl").get( 0 ));
						
					//	Log.e("123","set Headers vContestId ( vSubjectUrl).get( 0 )) runnableList >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "+runnableList);
						
						
						if(contestData.get( "vSubjectHrefUrl").get( 0 ) != null && contestData.get( "vSubjectHrefUrl").get( 0 ).trim().length() > 0 && 
								runnableList != null && !runnableList.containsKey(contestData.get( "vSubjectHrefUrl").get( 0 ))  && 
								Util.isInternetAvailable()  )	{
							
							isSubjectDownloading  = true;
							if( contestData.get( "vSubjectHrefUrl").get( 0 )!=null && contestData.get( "vSubjectHrefUrl").get( 0 ).trim().length()>0){
								runnableList.put(contestData.get( "vSubjectHrefUrl").get( 0 ),	new Util().getPrivateContestsData (contestData.get( "vSubjectHrefUrl").get( 0 ),true , runnableList,false ));
							}
							
						}
						
						else if(runnableList != null && !runnableList.containsKey(contestData.get( "vSubjectUrl").get( 0 ))  && Util.isInternetAvailable()  )	{
							isSubjectDownloading  = true;
							//Log.e("123","set Headers vContestId runnableList != null && !runnableList.containsKey(contestData.get( vSubjectUrl).get( 0 )) >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  ");
							if( contestData.get( "vSubjectUrl").get( 0 )!=null && contestData.get( "vSubjectUrl").get( 0 ).trim().length()>0){
								runnableList.put(contestData.get( "vSubjectUrl").get( 0 ),	new Util().getPrivateContestsData ( contestData.get( "vSubjectUrl").get( 0 ),false, runnableList,false ));
	
						}
					}

							




						



						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable () {

								@Override
								public void run() {
									if ( !isVisible() ) {
										return;
									}
									showMatchHeader(  );
								}

							});
						}

					}else{
						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable () {

								@Override
								public void run() {
									if ( !isVisible() ) {
										return;
									}
									showLobbyMessage();
								}

							});
						}

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}



		};
		Thread th = new Thread (r );
		th.start();


	}


	@Override
	public void onUpdate(final Message msg) {
		String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
		try {
			if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
				//Praveen: Following are getting called from onResume
				
				setValues();

				doPolling();

				deleteMarker();

				getLiveMatchesCount();
			}			if( msg != null && msg.obj !=null && msg.obj.toString().contains("share")) {
				if( msg.obj.toString().equalsIgnoreCase("shareScores") ) {
					if( DatabaseUtil.getInstance().isUserAnnonymous() ) {
						Bundle bundle = new Bundle();
						bundle.putBoolean("sharing", true );
						bundle.putString("fromFragment", topFragmentName);
						PlayupLiveApplication.getFragmentManagerUtil().setFragment( "ProviderFragment", bundle);

					} else {
						if( vContestId != null )
							showShareDialog( false );
					}

				} else if( msg.obj.toString().equalsIgnoreCase("share") ) {
					//				if( vContestId != null )
					//					showShareDialog( true );

				} else if( msg.obj.toString().contains("share_response") ) {

					String provider = msg.obj.toString();
					if( provider.split(":").length > 0 )
						provider = provider.split(":")[1];
					final String toastMessage = PlayUpActivity.context.getResources().getString(R.string.shareResponse)+" "+provider;

					if( msg.arg1 == 1 ) {
						PlayupLiveApplication.showToast(toastMessage);
					} else {
						PlayupLiveApplication.showToast(R.string.shareFailure);
					}

				}
				return;
			}

			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "SHOW_GALLERY" ) ) {

				if ( isGalleryAnimating ) {

					if ( PlayUpActivity.handler != null ) {
						PlayUpActivity.handler.postDelayed( new Runnable () {

							@Override
							public void run() {
								try {
									showHideOnlineFriends( true );
								} catch (Exception e) {
									// TODO Auto-generated catch block
								//	Logs.show ( e );
								}
							}

						}, 500 );
					}
				} else {
					showHideOnlineFriends( true );
				}

				return;
			}

			if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("callPrivateLobbyFragment")) {
				Bundle bundle = new Bundle ();
				bundle.putString("fromFragment", topFragmentName);
				bundle.putString( "vMainColor",vMainColor );							
				bundle.putString( "vMainTitleColor",vMainTitleColor );
				bundle.putString( "vSecColor",vSecColor );			
				bundle.putString( "vSecTitleColor",vSecTitleColor );
				PlayupLiveApplication.getFragmentManagerUtil().setFragment("PrivateLobbyFragment",bundle);
			}
			if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("MatchHomeFragment_getScores")) {
				/**
				 * setting the match header
				 */
				isSubjectDownloading = false;
				if ( PlayUpActivity.handler != null ) {

					if ( isGalleryAnimating ) {
						PlayUpActivity.handler.postDelayed( new Runnable () {

							@Override
							public void run() {
								try {
									if ( !isVisible() ) {
										return;
									}
									showMatchHeader();
									checkForRefresh();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									//Logs.show ( e );
								}
							}

						}, 500 );
					} else {
						PlayUpActivity.handler.post( new Runnable () {

							@Override
							public void run() {
								try {
									if ( !isVisible() ) {
										return;
									}
									showMatchHeader();
									checkForRefresh();
								} catch (Exception e) {
									// TODO Auto-generated catch block
								//	Logs.show ( e );
								}
							}

						});
					}

				}

			}



			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "LiveMatchesCount" ) ) {
				
				/**
				 * updated count of list of live matches under favourited competition
				 */

				if ( PlayUpActivity.handler  != null ) {

					if ( isGalleryAnimating ) {
						PlayUpActivity.handler.postDelayed( new Runnable() {

							@Override
							public void run() {
								try {
									if ( !isVisible() ) {
										return;
									}
									if(msg != null && msg.getData() != null && msg.getData().getString("vCompetionUrl") != null){
										getLiveMatches(msg.getData().getString("vCompetionUrl"));
									}


									setLiveCount(true);
									//							setHeaders();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									//Logs.show ( e );
								}

							}
						}, 500 );

					} else {
						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {
								try {
									if ( !isVisible() ) {
										return;
									}
									if(msg != null && msg.getData() != null && msg.getData().getString("vCompetionUrl") != null)								
										getLiveMatches(msg.getData().getString("vCompetionUrl"));
									setLiveCount(true);
									//							setHeaders();
								} catch (Exception e) {
									// TODO Auto-generated catch block
								//	Logs.show ( e );
								}

							}
						});

					}
				}
			}



			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "updateHeader" ) ) {
				if(msg.getData().containsKey("vContestId")){				
					/**
					 * updating the header on clicking the red tickets
					 */

					vContestId = msg.getData().getString("vContestId");

					if(contestTask != null){
						contestTask.cancel();
						contestTask = null;
					}if(contestTimer != null){
						contestTimer.cancel();
						contestTimer = null;
					}

					if ( isGalleryAnimating ) {
						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.postDelayed( new Runnable () {

								@Override
								public void run() {
									try {
										
										putFollowMessage(msg.getData().getString("vContestUrl"),msg.getData().getBoolean("isHref"));
										checkForRefresh();
									} catch (Exception e) {
										// TODO Auto-generated catch block
								//		Logs.show ( e );
									}

								}

							}, 500 );
						}
					} else {
						//					showMatchHeader();
						putFollowMessage(msg.getData().getString("vContestUrl"),msg.getData().getBoolean("isHref"));
						checkForRefresh();
					}
				}




			}







			if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("LiveMatches")) {
				/**
				 * getting the update info of individual live contests
				 */
				if (msg.getData() != null && msg.getData().containsKey("vContestLiveUrl")) {
					refreshMatches(msg.getData().getString("vContestLiveUrl"),msg.getData().getBoolean("isHref"));
				}

				if (PlayUpActivity.handler != null) {

					if ( isGalleryAnimating ) {
						PlayUpActivity.handler.postDelayed( new Runnable() {

							@Override
							public void run() {

								try {
									if (!isVisible()) {
										return;
									}


									showMatchHeader();
									try {
										if (matchGallery.getVisibility() == View.VISIBLE) {
											matchGalleryAdapter.setValues();
										}else{
											if (matchGalleryAdapter == null) {
												matchGalleryAdapter = new MatchGalleryAdapter( matchGallery );
												matchGallery.setAdapter(matchGalleryAdapter);
											} else {
												matchGalleryAdapter.setData();
											}

										}
									} catch ( Exception e ) {
										//Logs.show ( e );
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
									//Logs.show ( e );
								}



							}

						}, 500 );
					} else {
						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {


								try {

									if (!isVisible()) {
										return;
									}


									showMatchHeader();
									if (matchGallery.getVisibility() == View.VISIBLE) {
										matchGalleryAdapter.setValues();
									}else{
										if (matchGalleryAdapter == null) {
											matchGalleryAdapter = new MatchGalleryAdapter( matchGallery );
											matchGallery.setAdapter(matchGalleryAdapter);
										} else {
											matchGalleryAdapter.setData();
										}

									}
								} catch ( Exception e ) {
									//Logs.show ( e );
								}



							}

						});
					}

				}

			}
			

			if (msg != null && msg.obj != null && msg.obj.toString().contains("PrivateLobby")) {


				if ( PlayUpActivity.handler != null) {

					if ( isGalleryAnimating ) {
						PlayUpActivity.handler.postDelayed (new Runnable() {

							@Override
							public void run() {

								try {
									isDownloading = false;

									if ( !isVisible() ) {
										return;
									}

									// cancelProgressDialog();
									setRoomNameInTopBar();

									if (msg.obj.toString().equalsIgnoreCase("PrivateLobbyMessages")) {
										/**
										 * updating the messages
										 */
										setHeaders();
										getConversationMessages ();
										refreshMessages();
										refreshConversation ();


										setRoomMessages();
										



									}

									if (msg.obj.toString().equalsIgnoreCase("PrivateLobbyPutFollowMessage")) {
										/**
										 * making api call for 'follow message'
										 */


										if(follow != null && follow.size() > 0){

											new Util().putFollowMessage(vConversationUrl, follow.get(0),follow,token,isConversationUrlHref);

										}




										showMatchHeader();
										setRoomMessages();

										//								setHeaders();
										//								if(msg.arg1 == 1) {
										//									cancelProgressDialog();
										//								} else {
										//									setRoomMessages();
										//								}



									}



									if (msg.obj.toString().equalsIgnoreCase( "PrivateLobbyMessages_refresh")) {
										/**
										 * refreshing the private lobby messages
										 */

										refreshMessages();

										cancelProgressDialog();

										setRoomMessages();


									}
									if (msg.obj.toString().equalsIgnoreCase( "PrivateLobbyMessages_gap")) {

										// call updates for msgs
										cancelProgressDialog();

										setRoomMessages();


									}
									if (msg.obj.toString().equalsIgnoreCase ( "PrivateLobbyFriends" ) ) {
										
										/**
										 * updating the conversation friends
										 */

										refreshFriends();
										showHideOnlineFriends ( true );
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
								//	Logs.show ( e );
								}
							}

						}, 500 );
					} else {
						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {

								try {
									isDownloading = false;

									if ( !isVisible() ) {
										return;
									}

									// cancelProgressDialog();
									setRoomNameInTopBar();

									if (msg.obj.toString().equalsIgnoreCase("PrivateLobbyMessages")) {


										getConversationMessages ();
										refreshMessages();
										refreshConversation ();								
										setRoomMessages();
										
									}

									if (msg.obj.toString().equalsIgnoreCase("PrivateLobbyPutFollowMessage")) {

										if(follow != null && follow.size() > 0){
											new Util().putFollowMessage(vConversationUrl, follow.get(0),follow,token,isConversationUrlHref);

										}

										showMatchHeader();
										setRoomMessages();


										


									}

									if (msg.obj.toString().equalsIgnoreCase( "PrivateLobbyMessages_refresh")) {

										refreshMessages();

										cancelProgressDialog();

										setRoomMessages();


									}
									if (msg.obj.toString().equalsIgnoreCase( "PrivateLobbyMessages_gap")) {

										// call updates for msgs
										cancelProgressDialog();

										setRoomMessages();


									}
									if (msg.obj.toString().equalsIgnoreCase ( "PrivateLobbyFriends" ) ) {

										refreshFriends();
										showHideOnlineFriends ( true );
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
								//	Logs.show ( e );
								}
							}

						});
					}

				}

			}
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
	//	Logs.show(e);
		}
	}

	/**
	 * fetching the list of friends invited to this room
	 */
	private void fetchConversationFriends () {
		try{
		if(runnableList!=null&&!runnableList.containsKey(Constants.GET_PRIVATE_CONVERSATION_FRIENDS)  && Util.isInternetAvailable()  ){
			runnableList.put(Constants.GET_PRIVATE_CONVERSATION_FRIENDS, new Util().getPrivateLobbyConversationFriends( vConversationId,runnableList ));
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show ( e );
		}
	}


	private boolean isGalleryAnimating = false;


	@Override
	public void onClick(View v) {

		if ( v == null ) {
			return;
		}
		
		String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
		
		Bundle bundle = null;

		switch (v.getId()) {

		case R.id.addFriends:

			if (  Util.isInternetAvailable() ) {
				fetchConversationFriends( );
			}

			if( addFriendsLayout.getVisibility() == 0  )
				addFriendsLayoutisClicked = true;
			else
				addFriendsLayoutisClicked = false;

			if ( addFriendsLayoutisClicked ){

				addFriendsLayoutisClicked = false;
			}else
				addFriendsLayoutisClicked = true;
			showHideOnlineFriends(!(addFriendsLayout.getVisibility() == 0 ? true : false) );

			break;

		case R.id.addFrnds:
			stopPolling();
			bundle = new Bundle ();
			bundle.putString( "vConversationId", vConversationId );
			bundle.putString("vMainColor",vMainColor );
			bundle.putString("vMainTitleColor",vMainTitleColor );
			bundle.putString( "vSecColor",vSecColor );			
			bundle.putString( "vSecTitleColor",vSecTitleColor );
			bundle.putString("fromFragment", topFragmentName);
			PlayupLiveApplication.getFragmentManagerUtil().setFragment( "PrivateLobbyInviteFriendFragment", bundle );
			break;

		case R.id.write_post:
			stopPolling();
			bundle = new Bundle ();
			bundle.putString("fromFragment", topFragmentName);
			bundle.putString( "vConversationId", vConversationId );
			bundle.putString("vMainColor",vMainColor );
			bundle.putString("vMainTitleColor",vMainTitleColor );
			bundle.putString( "vSecColor",vSecColor );			
			bundle.putString( "vSecTitleColor",vSecTitleColor );
			PrivateLobbyMessageFragment.isHomeTapped = false;
			PlayupLiveApplication.getFragmentManagerUtil().setFragment( "PrivateLobbyMessageFragment", bundle );
			break;

		case R.id.editRoom:
			stopPolling();
			bundle = new Bundle ();
			bundle.putString("vConversationId", vConversationId);
			bundle.putString("vMainColor",vMainColor );
			bundle.putString("vMainTitleColor",vMainTitleColor );
			bundle.putString( "vSecColor",vSecColor );			
			bundle.putString( "vSecTitleColor",vSecTitleColor );
			bundle.putString("editRoom", "editRoom");
			bundle.putString( "fromFragment", topFragmentName );
			PlayupLiveApplication.getFragmentManagerUtil().setFragment("CreateLobbyRoomFragment", bundle );
			break;

		case R.id.stripDown:

			if ( isGalleryAnimating ) {
				return;
			}
			TranslateAnimation translateAnimation = new TranslateAnimation( Animation.RELATIVE_TO_SELF, 0.0f, 
					Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f, 
					Animation.RELATIVE_TO_SELF, 0.0f );

			isGalleryAnimating = true;

			matchGallery.setVisibility(View.VISIBLE);
			content_layout.findViewById(R.id.matchHeaderBottom).setVisibility(View.INVISIBLE);
			
			translateAnimation.setDuration(500);
			translateAnimation.setAnimationListener( new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					content_layout.findViewById ( R.id.matchHeaderBottom ).setVisibility( View.VISIBLE );

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					// TODO Auto-generated method stub
					//shadowDown.setVisibility(View.GONE);
					isGalleryAnimating = false;

				}
			});
			content_layout.findViewById ( R.id.matchHeaderBottom ).startAnimation(translateAnimation);


			stripUp.setVisibility(View.VISIBLE);
			

			// stripDown.setVisibility(View.GONE );



			break;
		case R.id.stripUp:

			if ( isGalleryAnimating ) {
				return;
			}
			TranslateAnimation translateAnimation1 = new TranslateAnimation( Animation.RELATIVE_TO_SELF, 0.0f, 
					Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, 
					Animation.RELATIVE_TO_SELF, -1.0f );

			isGalleryAnimating = true;

			translateAnimation1.setDuration(500);

			translateAnimation1.setAnimationListener( new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					//shadowDown.setVisibility(View.VISIBLE);
					stripDown.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					content_layout.findViewById ( R.id.matchHeaderBottom ).setVisibility( View.GONE );
					isGalleryAnimating = false;
				}
			});
			content_layout.findViewById ( R.id.matchHeaderBottom ).startAnimation(translateAnimation1);




			break;
		}
	}

	@Override
	public void onPause() {

		super.onPause();
		matchHeaderGenerator = null;

		stopPolling();

		putMarker();
	}
	/**
	 * scheduling the refresh of the messages
	 */
	private void refreshMessages () {

		if (refreshMessagesTimer != null) {
			refreshMessagesTimer.cancel();
		}
		refreshMessagesTimer = new Timer();

		if (refreshMessagesTask != null) {
			refreshMessagesTask.cancel();
			refreshMessagesTask = null;
		}

		refreshMessagesTask = new TimerTask() {

			@Override
			public void run() {
				try {
					getConversationMessages ();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}
		};

		Runnable r = new Runnable () {

			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();

					Hashtable<String, Object> result = dbUtil.getLobbyConversationMessagesUrl( vConversationId );
					String vConversationMessagesUrl = (String) result.get("url");
				

					int cacheTime = Integer.parseInt( dbUtil.getCacheTime( vConversationMessagesUrl ) );

					dbUtil = null;



					if (cacheTime <= 1) {
						cacheTime = 5;
						try {
							refreshMessagesTimer.schedule( refreshMessagesTask, cacheTime * 1000, (cacheTime * 1000));
						} catch ( Exception e ) {
						//	Logs.show ( e );
						}
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();


	}

	/**
	 * scheduling the refresh of the entire conversation
	 */

	private void refreshConversation () {

		if (refreshConversationTimer != null) {
			refreshConversationTimer.cancel();
		}
		refreshConversationTimer = new Timer();

		if ( refreshConversationTask != null) {
			refreshConversationTask.cancel();
			refreshConversationTask = null;
		}

		refreshConversationTask = new TimerTask() {

			@Override
			public void run() {
				try {
					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(" SELECT vConversationUrl,vConversationHrefUrl FROM friendConversation WHERE vConversationId = \""+ vConversationId + "\" ");

					if (  c != null && c.getCount() > 0 ) {

						c.moveToFirst();
						String conversationURL = c.getString(c
								.getColumnIndex("vConversationHrefUrl"));
						
						if(conversationURL != null && conversationURL.trim().length() > 0){
							
							

							if(runnableList!=null&&!runnableList.containsKey(Constants.GET_PRIVATE_LOBBY_CONVERSATION)  && Util.isInternetAvailable()  ){
								runnableList.put(Constants.GET_PRIVATE_LOBBY_CONVERSATION,new Util().getPrivateLobbyConversation(conversationURL,runnableList,true,true));
							}
						}else{
							
							conversationURL = c.getString(c
									.getColumnIndex("vConversationUrl"));
							
							if(runnableList!=null&&!runnableList.containsKey(Constants.GET_PRIVATE_LOBBY_CONVERSATION)  && Util.isInternetAvailable()  ){
								runnableList.put(Constants.GET_PRIVATE_LOBBY_CONVERSATION,new Util().getPrivateLobbyConversation(conversationURL,runnableList,true,false));
							}
							
						}


						
						c.close();

					} 
					else {


						if ( c != null ) {
							c.close();

							c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
									" SELECT vSubjectUrl,vSubjectHref FROM recent WHERE vSubjectId = \""
									+ vConversationId + "\" ");

							if ( c != null && c.getCount() > 0 ) {

								c.moveToFirst();

								String conversationURL = c.getString(c.getColumnIndex("vSubjectHref"));
								if(conversationURL != null && conversationURL.trim().length() > 0){
									
									if(runnableList!=null&&!runnableList.containsKey(Constants.GET_PRIVATE_LOBBY_CONVERSATION)  && Util.isInternetAvailable()  ){
										runnableList.put(Constants.GET_PRIVATE_LOBBY_CONVERSATION,new Util().getPrivateLobbyConversation(conversationURL,runnableList,true,true));
									}
									
								}else{
									conversationURL = c.getString(c.getColumnIndex("vSubjectUrl"));
									
									if(runnableList!=null&&!runnableList.containsKey(Constants.GET_PRIVATE_LOBBY_CONVERSATION)  && Util.isInternetAvailable()  ){
										runnableList.put(Constants.GET_PRIVATE_LOBBY_CONVERSATION,new Util().getPrivateLobbyConversation(conversationURL,runnableList,true,false));
									}
								}


								
								c.close();


							}
							else {
								if ( c != null ) {
									c.close();
								}

								c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(" SELECT vSubjectUrl,vSubjectHrefUrl FROM my_friends_live WHERE " +
										"vSubjectId = \""+ vConversationId + "\" ");

								if ( c != null && c.getCount() > 0 ) {

									c.moveToFirst();


									String conversationURL = c.getString(c.getColumnIndex("vSubjectHrefUrl"));
									
									if(conversationURL != null && conversationURL.trim().length() > 0){
										
										if(runnableList!=null&&!runnableList.containsKey(Constants.GET_PRIVATE_LOBBY_CONVERSATION)  && Util.isInternetAvailable()  ){
											runnableList.put(Constants.GET_PRIVATE_LOBBY_CONVERSATION,new Util().getPrivateLobbyConversation(conversationURL,runnableList,true,true));
										}
										
									}else{
										
										conversationURL = c.getString(c.getColumnIndex("vSubjectUrl"));
										
										if(runnableList!=null&&!runnableList.containsKey(Constants.GET_PRIVATE_LOBBY_CONVERSATION)  && Util.isInternetAvailable()  ){
											runnableList.put(Constants.GET_PRIVATE_LOBBY_CONVERSATION,new Util().getPrivateLobbyConversation(conversationURL,runnableList,true,false));
										}
										
									}
									
									c.close();

								}
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}
			}

		};

		Runnable r = new Runnable () {

			@Override
			public void run() {


				try {
					String conversationURL = null;
					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(" SELECT vConversationUrl,vConversationHrefUrl FROM friendConversation WHERE vConversationId = \""+ vConversationId + "\" ");

					if (  c != null && c.getCount() > 0 ) {

						c.moveToFirst();
						if(c.getString(c.getColumnIndex("vConversationHrefUrl"))!=null && c.getString(c.getColumnIndex("vConversationHrefUrl")).trim().length()>0){
							conversationURL = c.getString(c.getColumnIndex("vConversationHrefUrl"));
						}else if (c.getString(c.getColumnIndex("vConversationUrl"))!=null && c.getString(c.getColumnIndex("vConversationUrl")).trim().length()>0){
							conversationURL = c.getString(c.getColumnIndex("vConversationUrl"));	
						}
						
						c.close();
					} else {

						if ( c != null ) {
							c.close();

							c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
									" SELECT vSubjectUrl,vSubjectHrefUrl FROM recent WHERE vSubjectId = \""
									+ vConversationId + "\" ");

							if ( c != null && c.getCount() > 0 ) {

								c.moveToFirst();
								if(c.getString(c.getColumnIndex("vSubjectHrefUrl"))!=null && c.getString(c.getColumnIndex("vSubjectHrefUrl")).trim().length()>0){
									conversationURL = c.getString(c.getColumnIndex("vSubjectHrefUrl"));
								}else if (c.getString(c.getColumnIndex("vSubjectUrl"))!=null && c.getString(c.getColumnIndex("vSubjectUrl")).trim().length()>0){
									conversationURL = c.getString(c.getColumnIndex("vSubjectUrl"));	
								}
							//	conversationURL = c.getString(c.getColumnIndex("vSubjectUrl"));
								c.close();
								return;

							} else {
								if ( c != null ) {
									c.close();
								}

								c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(" SELECT vSubjectUrl,vSubjectHrefUrl FROM my_friends_live WHERE " +
										"vSubjectId = \""+ vConversationId + "\" ");

								if ( c != null && c.getCount() > 0 ) {
									c.moveToFirst();
									//conversationURL = c.getString(c.getColumnIndex("vSubjectUrl"));
									if(c.getString(c.getColumnIndex("vSubjectHrefUrl"))!=null && c.getString(c.getColumnIndex("vSubjectHrefUrl")).trim().length()>0){
										conversationURL = c.getString(c.getColumnIndex("vSubjectHrefUrl"));
									}else if (c.getString(c.getColumnIndex("vSubjectUrl"))!=null && c.getString(c.getColumnIndex("vSubjectUrl")).trim().length()>0){
										conversationURL = c.getString(c.getColumnIndex("vSubjectUrl"));	
									}
									c.close();
									return;
								}
							}
						}
					}




					int cacheTime = Integer.parseInt( DatabaseUtil.getInstance().getCacheTime( conversationURL ) );

					if (cacheTime <= 1) {
						cacheTime = 5;

						try {
							refreshConversationTimer.schedule( refreshConversationTask, cacheTime * 1000, (cacheTime * 1000));
						} catch ( Exception e ) {
					//		Logs.show ( e );
						}
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();


	}
	 /**
	  * making api call to get the conversation messages
	  */

	private void getConversationMessages () {
		Runnable r = new Runnable () {

			@Override
			public void run() {
				try {
					if ( Util.isInternetAvailable ( ) ) {

						DatabaseUtil dbUtil = DatabaseUtil.getInstance();


						Hashtable<String, Object> result = dbUtil.getLobbyConversationMessagesUrl( vConversationId );
						String vConversationMessagesUrl = (String) result.get("url");
						Boolean isHref = (Boolean) result.get("isHref");
						
						
						Hashtable<String,Object> result2 = dbUtil.getFriendconversationUrlFromConversationId ( vConversationId );
						String vConversationUrl = (String) result2.get("url");
						boolean isConversationUrlHref = ((Boolean) result2.get("isHref")).booleanValue();
						try {
							if(runnableList != null && !runnableList.containsKey(vConversationMessagesUrl)  &&
									Util.isInternetAvailable()  ){
									
								runnableList.put(vConversationMessagesUrl, new Util().getPrivateLobbyConversationMessages ( vConversationMessagesUrl,isHref, vConversationId, vConversationUrl, false,runnableList,isConversationUrlHref));
							}


						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r ) ;
		th.start();


	}

	/**
	 * showing and hiding the online friends list
	 */
	private void showHideOnlineFriends(boolean show) {


		try{
			if (show) {

				if ( addFriendsLayoutisClicked) {
					addFriendsLayout.setVisibility(View.VISIBLE);	
					addFriendImage.setImageResource(R.drawable.friends_d_icon);

				}

				Runnable r = new  Runnable () {

					@Override
					public void run() {

						try {
							DatabaseUtil dbUtil = DatabaseUtil.getInstance();
							final Hashtable<String, List<String>> data = dbUtil.getConversationFriendsData ( vConversationId);

							if ( PlayUpActivity.handler != null ) {
								PlayUpActivity.handler.post( new  Runnable () {

									@Override
									public void run() {

										if ( !isVisible() ) {
											return;
										}
										if ( friendGalleryAdapter == null ) {
											friendGalleryAdapter = new FriendGalleryAdapter( data );

											friendGallery.setAdapter(friendGalleryAdapter);

											DisplayMetrics metrics = new DisplayMetrics();
											PlayUpActivity.context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
											MarginLayoutParams mlp = (MarginLayoutParams) friendGallery.getLayoutParams();
											mlp.setMargins(-( (metrics.widthPixels/2) +mlp.leftMargin ) , mlp.topMargin, mlp.rightMargin, mlp.bottomMargin );

											friendGallery.setOnItemClickListener( PrivateLobbyRoomFragment.this );
										} else {

											friendGalleryAdapter.setData ( data );
										}

									}

								});
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
						//	Logs.show ( e );
						}

					}


				};
				Thread th = new Thread (r );
				th.start();



			} else {
				addFriendsLayout.setVisibility(View.INVISIBLE);
				addFriendImage.setImageResource(R.drawable.private_friend_selector);
			}
		}catch(Exception e){
		//	Logs.show ( e );
		}

	}
	/**
	 * scheduling the refresh of the conversation friends
	 */

	public void refreshFriends () {


		if (refreshFriendsTimer != null) {
			refreshFriendsTimer.cancel();
		}
		refreshFriendsTimer = new Timer();

		if (refreshfriendsTask != null) {
			refreshfriendsTask.cancel();
			refreshfriendsTask = null;
		}

		refreshfriendsTask = new TimerTask() {

			@Override
			public void run() {
				try {
					if ( Util.isInternetAvailable ( ) ) {
						fetchConversationFriends();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}
			}
		};

		Runnable r = new Runnable () {

			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();

					Hashtable<String, Object> result = dbUtil.getPrivateLobbyFriendsUrl ( vConversationId );
					String vConversationFriendsUrl = (String) result.get("url");
					

					int cacheTime = Integer.parseInt( dbUtil.getCacheTime( vConversationFriendsUrl ) );
					dbUtil = null;
					try{
						if (cacheTime > 0) {
							refreshFriendsTimer.schedule( refreshfriendsTask, cacheTime * 1000, (cacheTime * 1000));
						}
					}catch(Exception e){
					//	Logs.show(e);
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}

		};
		Thread th = new  Thread ( r  );
		th.start();

	}

	/**
	 * call the server to get match room data
	 */
	private void getConversation() {

		showProgressDialog();

		isDownloading = true;

		Runnable r = new Runnable () {

			@Override
			public void run() {
				try {
					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(" SELECT vConversationUrl,vConversationHrefUrl FROM friendConversation WHERE vConversationId = \""+ vConversationId + "\" ");

					if (  c != null && c.getCount() > 0 ) {

						c.moveToFirst();
						String conversationURL = c.getString(c
								.getColumnIndex("vConversationHrefUrl"));
						
						if(conversationURL != null && conversationURL.trim().length() > 0){
							
							if(runnableList != null && !runnableList.containsKey(Constants.GET_PRIVATE_LOBBY_CONVERSATION)  && Util.isInternetAvailable()  ){
								runnableList.put(Constants.GET_PRIVATE_LOBBY_CONVERSATION,new Util().getPrivateLobbyConversation(conversationURL,runnableList,false,true));
							}
							
						}else{
							
							conversationURL = c.getString(c
									.getColumnIndex("vConversationUrl"));
							
							if(runnableList!=null && !runnableList.containsKey(Constants.GET_PRIVATE_LOBBY_CONVERSATION)  && Util.isInternetAvailable()  ){
								runnableList.put(Constants.GET_PRIVATE_LOBBY_CONVERSATION,new Util().getPrivateLobbyConversation(conversationURL,runnableList,false,false));
							}
						}


						
						c.close();

					} 
					else {


						if ( c != null ) {
							c.close();

							c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
									" SELECT vSubjectUrl,vSubjectHref FROM recent WHERE vSubjectId = \""
									+ vConversationId + "\" ");

							if ( c != null && c.getCount() > 0 ) {

								c.moveToFirst();

								String conversationURL = c.getString(c.getColumnIndex("vSubjectHref"));
								
								if(conversationURL != null && conversationURL.trim().length() > 0){
									
									if(runnableList != null && !runnableList.containsKey(Constants.GET_PRIVATE_LOBBY_CONVERSATION)  && Util.isInternetAvailable()  ){
										runnableList.put(Constants.GET_PRIVATE_LOBBY_CONVERSATION,new Util().getPrivateLobbyConversation(conversationURL,runnableList,false,true));
									}
									
								}else{
									
									conversationURL = c.getString(c.getColumnIndex("vSubjectUrl"));
									if(runnableList!=null && !runnableList.containsKey(Constants.GET_PRIVATE_LOBBY_CONVERSATION)  && Util.isInternetAvailable()  ){
										runnableList.put(Constants.GET_PRIVATE_LOBBY_CONVERSATION,new Util().getPrivateLobbyConversation(conversationURL,runnableList,false,false));
									}
									
								}


								
								c.close();
								return;

							}
							else {
								if ( c != null ) {
									c.close();
								}

								c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(" SELECT vSubjectUrl,vSubjectHrefUrl FROM my_friends_live WHERE " +
										"vSubjectId = \""+ vConversationId + "\" ");

								if ( c != null && c.getCount() > 0 ) {

									c.moveToFirst();


									String conversationURL = c.getString(c.getColumnIndex("vSubjectHrefUrl"));
									
									if(conversationURL != null && conversationURL.trim().length() > 0){
										
										if(runnableList!=null&&!runnableList.containsKey(Constants.GET_PRIVATE_LOBBY_CONVERSATION)  && Util.isInternetAvailable()  ){
											runnableList.put(Constants.GET_PRIVATE_LOBBY_CONVERSATION,new Util().getPrivateLobbyConversation(conversationURL,runnableList,false,true));
										}
										
									}else{
										conversationURL = c.getString(c.getColumnIndex("vSubjectUrl"));
										
										if(runnableList!=null&&!runnableList.containsKey(Constants.GET_PRIVATE_LOBBY_CONVERSATION)  && Util.isInternetAvailable()  ){
											runnableList.put(Constants.GET_PRIVATE_LOBBY_CONVERSATION,new Util().getPrivateLobbyConversation(conversationURL,runnableList,false,false));
										}
									}
									
									c.close();
									return;
								}
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}
			}
		};
		Thread th = new Thread ( r );
		th.start();
	}
	 /**
	  * getting the updated live matches count
	  */

	private void getLiveMatchesCount() {

		Runnable r = new Runnable () {

			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable < String, List < String > > competitionUrl = dbUtil.getCompetitionLiveUrl();

					if(competitionUrl != null && competitionUrl.get("vCompetitionLiveUrl").size() > 0){
						
						
						if(Util.isInternetAvailable()){
							for(int i =0 ;i<competitionUrl.get("vCompetitionLiveUrl").size();i++){

								
								if(competitionUrl.get("vCompetitionLiveUrl").get(i) != null && competitionUrl.get("vCompetitionLiveUrl").get(i).trim().length() > 0){
									
									if(runnableList!=null && !runnableList.containsKey(competitionUrl.get("vCompetitionLiveUrl").get(i))  && Util.isInternetAvailable()  ){
										runnableList.put(competitionUrl.get("vCompetitionLiveUrl").get(i), new Util().getLiveMatchesCount(competitionUrl.get("vCompetitionLiveUrl").get(i),
												runnableList,false));
									}
									
								}else if(competitionUrl.get("vCompetitionLiveHref").get(i) != null && competitionUrl.get("vCompetitionLiveHref").get(i).trim().length() > 0){
									
									
									if(runnableList!=null && !runnableList.containsKey(competitionUrl.get("vCompetitionLiveHref").get(i))  && Util.isInternetAvailable()  ){
										runnableList.put(competitionUrl.get("vCompetitionLiveHref").get(i), new Util().getLiveMatchesCount(competitionUrl.get("vCompetitionLiveHref").get(i),
												runnableList,true));
									}
								}
								
							}
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();

	}



	/**
	 * getting the updated info about the individual live contests under favourite competition
	 * @param vCompetitionUrl
	 */


	private void getLiveMatches(final String vCompetitionUrl) {

		Runnable r = new Runnable () {

			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, List<String>> contestsLiveUrl = dbUtil.getPrivateSelectedContestUrl(vCompetitionUrl);

					

					if (contestsLiveUrl != null && contestsLiveUrl.containsKey("vContestUrl") && contestsLiveUrl.get("vContestUrl").size() > 0 && contestsLiveUrl.containsKey("vContestHrefUrl") && contestsLiveUrl.get("vContestHrefUrl").size() > 0) {				

						if (runnableList != null && !runnableList.containsKey(contestsLiveUrl.get("vContestHrefUrl").get(0))  && Util.isInternetAvailable()  ) {
							runnableList.put(contestsLiveUrl.get("vContestHrefUrl").get(0), new Util().getLiveContests(	contestsLiveUrl.get("vContestHrefUrl").get(0), runnableList,true));
						}
						else if (runnableList != null && !runnableList.containsKey(contestsLiveUrl.get("vContestUrl").get(0))  && Util.isInternetAvailable()  ) {
							runnableList.put(contestsLiveUrl.get("vContestUrl").get(0), new Util().getLiveContests(	contestsLiveUrl.get("vContestUrl").get(0), runnableList,false));

						}

						
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}


			}

		};
		Thread th = new Thread ( r ); 
		th.start();



		


	}

	
	/**
	 * scheduling the refresh of the red tickets
	 * @param vCompetitionLiveUrl
	 */

	private void refreshMatches ( final String vCompetitionLiveUrl,final boolean isHref ) {

		Runnable r = new Runnable () {

			@Override
			public void run() {
				try {
					
					

					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					int cacheTime = Integer.parseInt ( dbUtil.getCacheTime ( vCompetitionLiveUrl ) );
					if ( refreshMatchesTask != null ) {
						if ( refreshMatchesTask.containsKey ( vCompetitionLiveUrl ) ) {
							refreshMatchesTask.get(vCompetitionLiveUrl).cancel();
						}
					}
					refreshMatchesTask.put ( vCompetitionLiveUrl, new TimerTask() {

						@Override
						public void run() {
							
						//	Log.e("123","refreshMatches TimerTask>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+vCompetitionLiveUrl);
							
						//	Log.e("123","refreshMatches isHref>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+isHref);
							
							
							try {
								if ( runnableList!=null && !runnableList.containsKey ( vCompetitionLiveUrl )  && Util.isInternetAvailable()  ) {
									runnableList.put ( vCompetitionLiveUrl, new Util().getLiveContests ( vCompetitionLiveUrl, runnableList,isHref) );
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
						//		Logs.show ( e );
							}
						}
					});

					if ( refreshMatchesTimer != null ) {
						if ( refreshMatchesTimer.containsKey ( vCompetitionLiveUrl ) ) {
							refreshMatchesTimer.get(vCompetitionLiveUrl).cancel();
						}
					}
					refreshMatchesTimer.put(vCompetitionLiveUrl, new Timer() );

					if(cacheTime > 0) {
						refreshMatchesTimer.get(vCompetitionLiveUrl).schedule(refreshMatchesTask.get(vCompetitionLiveUrl), (cacheTime * 1000), (cacheTime * 1000));
					}

				} catch ( Exception e )  {
					//Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();



	}


	
	/**
	 * 'follow message' on clicking on red tickets
	 */
	

	private OnItemClickListener matchGalleryItemListener = new OnItemClickListener() {

		@Override
		public void onItemClick ( AdapterView<?> arg0, View view, int position,long id ) {

			if ( view != null && view.getTag( R.id.about_txtview ) != null ) {
				vContestId = (String) view.getTag( R.id.about_txtview );
				final String vContestUrl = (String) view.getTag( R.id.aboutScrollView );
				

				putFollowMessage(vContestUrl,((Boolean)view.getTag( R.id.avtarGreenBase )).booleanValue());


				
			}
		}
	};
	
	/**
	 * sending the 'following info' to the server 
	 * 
	 * @param vContestUrl
	 */

	private void putFollowMessage(final String vContestUrl,final boolean isHref){
		try{
		Runnable r = new Runnable () {



			@Override
			public void run() {


				if(contestTask != null){
					contestTask.cancel();
					contestTask = null;
				}if(contestTimer != null){
					contestTimer.cancel();
					contestTimer = null;
				}

				DatabaseUtil dbUtil = DatabaseUtil.getInstance();
				boolean isFollowing = dbUtil.setPrivateSelectContest ( vContestId, vContestUrl , vConversationId ,isHref);



				if ( PlayUpActivity.handler != null ) {
					PlayUpActivity.handler.post( new Runnable () {

						@Override
						public void run() {
							try {
								if ( !isVisible() ) {
									return;
								}
								showMatchHeader();
							} catch (Exception e) {
								// TODO Auto-generated catch block
							//	Logs.show ( e );
							}
						}
					});
				}

				/**
				 * checking if the previous following match is same as the current one.If so , then no need to send
				 * info to the server.Else send it
				 * 
				 */

				if ( isFollowing ) {

					return;
				}

				// post the following message 
				try {
					final JSONObject jObj = new JSONObject();
					jObj.put( ":type", "application/vnd.playup.friend.conversation+json" );
					jObj.put( "name", vConversationName );

					JSONObject subject_jObj = new JSONObject();
					subject_jObj.put( ":self",  vContestUrl );
					subject_jObj.put( ":type",  dbUtil.getHeader( vContestUrl ) );
					subject_jObj.put( ":uid",  vContestId );

					jObj.put( "subject", subject_jObj );
					
					Hashtable<String, Object> result = dbUtil.getFriendconversationUrlFromConversationId ( vConversationId );
					if(result != null && result.containsKey("isHref")){
						vConversationUrl = (String) result.get("url");
						 isConversationUrlHref = ((Boolean) result.get("isHref")).booleanValue();
					}
					
					if(follow != null && follow.size() == 0){

						follow.add(jObj.toString());
						new Util().putFollowMessage ( vConversationUrl, jObj.toString(),follow,token,isConversationUrlHref);
					}

					else{

						follow.add(jObj.toString());


					}


				} catch (JSONException e) {
					//Logs.show ( e );
				}
			}

		};
		Thread th = new Thread ( r );
		th.start();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show ( e );
		}
	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id ) {

		if ( view.getTag( R.id.activity_list_relativelayout ) == null ) {
			//Toast.makeText( PlayUpActivity.context, R.string.not_playup_user, Toast.LENGTH_SHORT ).show();
			return;
		}
		final String vProfileId = view.getTag( R.id.activity_list_relativelayout ).toString();

		if ( vProfileId == null ) {
			//Toast.makeText( PlayUpActivity.context, R.string.not_playup_user, Toast.LENGTH_SHORT ).show();
			return;
		}
		if ( vProfileId != null && vProfileId.trim().length() == 0 ) {
			//Toast.makeText( PlayUpActivity.context, R.string.not_playup_user, Toast.LENGTH_SHORT ).show();
			return;
		}

		Runnable r = new  Runnable () {

			@Override
			public void run() {

				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					final Hashtable<String, Object> result = dbUtil.getProfileUrl ( vProfileId );
					
					
					if ( PlayUpActivity.handler != null ) {
						PlayUpActivity.handler.post( new Runnable () {

							@Override
							public void run() {

								if ( !isVisible() ) {
									return;
								}
								
								boolean isHref = false;
								String vProfileUrl = null;
								if(result != null && result.containsKey("isHref")){
									
									isHref = (Boolean) result.get("isHref");
									vProfileUrl = (String) result.get("url");
								}

								Bundle bundle = new Bundle();
								bundle.putString( "vSelfUrl", vProfileUrl );
								bundle.putBoolean( "isHref", isHref );
								bundle.putString( "vConversationId", vConversationId );
								bundle.putString( "fromFragment", fromFragment );
								
								bundle.putString("vMainColor",vMainColor );
								bundle.putString("vMainTitleColor",vMainTitleColor );
								
								cancelRunnable ();


								PlayupLiveApplication.getFragmentManagerUtil().setFragment( "PublicProfileFragment", bundle );

							}

						});
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
				//	Logs.show ( e );
				}



			}

		};
		Thread th = new Thread ( r );
		th.start();



	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
			
			yPosition = event.getRawY();
			onclick = false;
		} else if ( event.getAction() == MotionEvent.ACTION_UP ) {
			if( !onclick && (event.getRawY() - yPosition) < 5 && (event.getRawY() - yPosition) > -5 ) 
				onClick(v);			
		} else if ( event.getAction() == MotionEvent.ACTION_MOVE ) {
			if( !onclick && (event.getRawY() - yPosition) > 0 ) {
				if( v.getId() == R.id.stripDown ) {
					onClick(v);
					onclick = true;

				}
			} else if(!onclick && (event.getRawY() - yPosition) < 0  ) {
				if( v.getId() == R.id.stripUp ) {
					onClick(v);
					onclick = true;				
				}
			}
		} 



		return true;
	}
	
	/**
	 * refreshing the header
	 */

	private void refreshContest ( ) {

		if ( contestTask != null ) {
			contestTask.cancel();
		}
		if ( contestTimer != null ) {
			contestTimer.cancel();
		}

		Runnable r = new Runnable () {

			@Override
			public void run() {

				try {
					Hashtable<String, Object> result = DatabaseUtil.getInstance().getContestUrlFromContestId( vContestId );

		
					String vContestUrl = (String) result.get("url");
					
					
					int cacheTime = Integer.parseInt( DatabaseUtil.getInstance().getCacheTime(vContestUrl));

					if ( cacheTime > 0 ) {


						contestTask = new TimerTask() {

							@Override
							public void run() {

								if ( Util.isInternetAvailable() ) {


									Hashtable<String, Object> result = DatabaseUtil.getInstance().getContestUrlFromContestId( vContestId );
									String vContestUrl = (String) result.get("url");
									Boolean isHref = (Boolean) result.get("isHref");

									try {
										if(runnableList != null && !runnableList.containsKey(vContestUrl)  && Util.isInternetAvailable()  )									
											runnableList.put(vContestUrl, new Util().getPrivateContestsData ( vContestUrl,isHref, runnableList,true ));


									} catch (Exception e) {
										// TODO: handle exception
									}


								}
							}
						};
						try {
							contestTimer = new Timer();
							contestTimer.schedule( contestTask , cacheTime * 1000, cacheTime * 1000 );
						} catch ( Exception e ) {
						//	Logs.show ( e );
						}
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					//Logs.show ( e );
				}

			}

		};
		Thread th = new Thread ( r );
		th.start();


	}


	MatchHeaderGenerator matchHeaderGenerator = null;
	/**
	 * showing match header 
	 * @param vContestId
	 */
	public void showMatchHeader( ) {

		if(vContestId == null) {
			setHeaders();
			return;
		} else {
			setLiveCount(true);
			//			matchHeader.setVisibility( View.VISIBLE  );
			//
			//			lobbyMessage.setVisibility(View.GONE);
			//			sportMainView.setVisibility( View.VISIBLE );


			//			PlayupLiveApplication.callUpdateTopBarFragments(null);


		}

		try{
		Runnable r = new Runnable () {

			@Override
			public void run() {

				final Hashtable<String, List<String>> data = PlayupLiveApplication.getDatabaseWrapper().select( "SELECT vContestId,dScheduledStartTime," +
						"dStartTime,dEndTime,iTotal1,iTotal2,vHomeTeamId,vAwayTeamId,vSummary,vSportsName,vCompetitionName,iWickets1, iWickets2,vOvers,vRunRate,vLastBall,vPlayerFirstName1,vPlayerFirstName2,vPlayerLastName1,vPlayerLastName2" +
						",vRole2,vRole1,vStats1,vStats2,vStrikerFirstName,vStrikerLastName,vStrikerStats,vNonStrikerFirstName,vNonStrikerLastName,vNonStrikerStats,vAnnotation,vAnnotation2,vSummary1,vSummary2,iGoals1,iBehinds1,iSuperGoals1,iGoals2,iBehinds2,iSuperGoals2,iInnnings,vInnningsHalf,iBalls1,iBalls2,iOut1,iOut2,iStrikes1,iStrikes2,vBase1,vBase2,iHasLiveUpdates,vLastEventName, vShortMessage, vLongMessage,vSportType,iActive1,iActive2,  " +
						"( SELECT vDisplayName FROM teams WHERE vTeamId = vHomeTeamId  ) AS vHomeDisplayName, " +
						"( SELECT vDisplayName FROM teams WHERE vTeamId = vAwayTeamId  ) AS vAwayDisplayName ," +
						"(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vHomeTeamId ) AS vHomeCalendarUrl," +
						"(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vAwayTeamId ) AS vAwayCalendarUrl  "+
						" FROM contests WHERE vContestId = \""+vContestId+"\"" );


				
				
				


				try {
//					if(!isSubjectDownloading && (data == null || (data != null && data.get( "vHomeTeamId" ) != null  && data.get( "vHomeTeamId" ).size() == 0)))
					if(!isSubjectDownloading && (data != null && ((data.get( "vContestId" ) != null  && data.get( "vContestId" ).size() == 0) ||
							data.get( "vContestId" ) == null ))){
						if(PlayUpActivity.handler != null){
							PlayUpActivity.handler.post(new Runnable() {

								@Override
								public void run() {
									try {
										if ( !isVisible() ) {
											return;
										}
									 showLobbyMessage();

										return;
									} catch (Exception e) {
										// TODO Auto-generated catch block
									//	Logs.show ( e );
									}

								}
							});
						}


					}
					if(PlayUpActivity.handler != null){

						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {
								try {
									//setting and inflating the layouts for match header based on sport type
									String sportType = null;
									if( data!= null && data.get("vSportType")!=null )
										sportType = data.get("vSportType").get(0);
									
									if( sportType == null )
										 return;
									
									// adding layouts to the headr view based on sport type
									if( ( sportType!=null && prevSportType!=null && !sportType.equalsIgnoreCase(prevSportType) ) ||
											matchHeaderGenerator== null ) {
										prevSportType = sportType;
										View headerLayout;
										if( inflater == null )
											inflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
										
										if( sportType!= null && sportType.equalsIgnoreCase(Constants.LEADERBOARD)  ) {
											headerLayout = (View) inflater.inflate(R.layout.match_header_leaderboard, null);
										} else if( sportType!= null && sportType.equalsIgnoreCase(Constants.SET_BASED_DATA)  )  {
											headerLayout = (View) inflater.inflate(R.layout.match_header_setbased, null);
										} else {
											headerLayout = (View) inflater.inflate(R.layout.match_header_normal, null);
										}
										
										if(sportType != null){
										matchHeaderLayout.setBackgroundResource(0);
										matchHeaderLayout.removeAllViews();
										matchHeaderLayout.addView( headerLayout );
										matchHeaderGenerator = new MatchHeaderGenerator(data, matchHeaderLayout , false , false);
										
										headerLayout = null;
										}
									} else if (matchHeaderGenerator != null ){
										
										matchHeaderGenerator.setData(data);
									
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
								///	Logs.show ( e );
								}

							}
						});
					}






				} catch (Exception e) {

				//	Logs.show( e );
				}


			}

		};
		
		Thread th = new Thread ( r );
		th.start();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show ( e );
		}
	}

	/**
	 * setting live favourites cout on strips
	 * @param show
	 */
	private void setLiveCount( boolean show) {
		if( show ) {

			Runnable r = new Runnable () {

				@Override
				public void run() {
					try {
						final int live = PlayupLiveApplication.getDatabaseWrapper().getTotalCount("SELECT vContestId FROM contests c  LEFT JOIN competition cp ON cp.vCompetitionId = c.vCompetitionId WHERE cp.isFavourite = '1'  AND cp.iLiveNum > 0 AND ( c.dEndTime IS NULL AND c.dStartTime IS NOT NULL )");

						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable () {

								@Override
								public void run() {
									if ( !isVisible() ) {
										return;
									}
									liveCountDown.setVisibility(View.VISIBLE);
									liveCountUp.setVisibility(View.VISIBLE);
									liveCountDown.setText(""+live);
									liveCountUp.setText(""+live);
								}

							});
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
					//	Logs.show ( e );
					}

				}

			};
			Thread th = new Thread ( r );
			th.start();


		} else {
			liveCountDown.setVisibility(View.GONE);
			liveCountUp.setVisibility(View.GONE);
		}
	}


	/**
	 * A dialog to show providers to share scores
	 */
	private void showShareDialog( boolean refresh ) {
		
		try{

		if ( PlayUpActivity.context != null && Constants.isCurrent ) {
			TextView dialogTitle;
			ListView providerList;
			RelativeLayout dialogRoot;
			
			final DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			final Hashtable<String, Object> result = dbUtil.getContestShareUrl( vContestId );
			String vShareUrl = "";
			boolean isHref = false;
			
			if(result != null && result.containsKey("isHref") &&  result.containsKey("url")){
				vShareUrl = (String) result.get("url");
				isHref = ((Boolean) result.get("isHref"));
				
			}
			final Hashtable<String, List<String>> data = DatabaseUtil.getInstance().getLoginProviders();

			ProviderAdapter providerAdapter = new ProviderAdapter( data );
			
			shareDialog = new Dialog(PlayUpActivity.context, android.R.style.Theme_Translucent_NoTitleBar);
			shareDialog.setContentView( R.layout.share_dialog);

			View dialogView = shareDialog.findViewById( R.id.dialogView );
			dialogView.setBackgroundColor( Color.WHITE );
			
			dialogTitle = ( TextView ) shareDialog.findViewById( R.id.dialogTitle);
			providerList = ( ListView ) shareDialog.findViewById(R.id.providerList);
			dialogRoot = (RelativeLayout )shareDialog.findViewById(R.id.dialogRoot);
			
			providerAdapter.setData( shareDialog, vShareUrl,isHref);
			providerList.setAdapter(providerAdapter);		
			dialogTitle.setTypeface( Constants.OPEN_SANS_REGULAR );
			
			shareDialog.show();


			dialogRoot.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					shareDialog.dismiss();
					
				}
			});

		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
	//		Logs.show ( e );
		}

	}
		


}









