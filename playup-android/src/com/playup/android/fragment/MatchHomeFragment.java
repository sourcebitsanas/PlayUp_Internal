
package com.playup.android.fragment;
import java.util.HashMap;

import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
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

import com.flurry.android.FlurryAgent;
import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.FriendGalleryAdapter;
import com.playup.android.adapters.MatchHeaderGenerator;
import com.playup.android.adapters.ProviderAdapter;
import com.playup.android.adapters.PublicMatchesTicketAdapter;
import com.playup.android.adapters.RoomConversationAdapter;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.FragmentManagerUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;
import com.playup.android.util.json.JsonUtil;



/**
 * Shows <br>
 * 1)Selected room's conversations/posts <br>
 * 2)Selected Contest <br>
 * 3)Common friends list
 * 
 */
public class MatchHomeFragment extends MainFragment implements OnClickListener, OnItemClickListener , OnTouchListener{

	private ImageView write_post;

	private LinearLayout addFriendsLayout;
	private ImageView addFriendImage;
	private ListView listView;
	private ImageView addFrnds;
	private LinearLayout noMessages;

	private RoomConversationAdapter roomConversationAdapter;
	private PublicMatchesTicketAdapter publicMatchesTicketAdapter;

	private TextView liveCountDown;
	private TextView liveCountUp;
	private TextView stripLiveCount;
	private RelativeLayout stripDown, stripUp,commonStrip,matchHeaderBottom;
	private Gallery matchGallery;

	private TimerTask pollingTask;
	private Timer pollingTimer = new Timer();
	private TimerTask refreshMessagesTask;
	private TimerTask refreshfriendsTask;
	private TimerTask refreshScoreTask;
	private TextView invite;
	private TextView messageIn;
	private boolean isDownloading = false;
	private Timer refreshMessagesTimer = new Timer();
	private Timer refreshFriendsTimer = new Timer();
	private Timer refreshSubjectTimer = new Timer();


	private RelativeLayout  noFriendsView;
	
	private String vConversationId = null;
	private String fromFragment = null;
	private Hashtable<String, List<String>> matchHomeData;
	private ImageDownloader imageDownloader = new ImageDownloader();
	private boolean isAgainActivated = false;
	static String vContestId = null;
	private Gallery friendGallery;
	private FriendGalleryAdapter friendGalleryAdapter;
	private String vContestUrl	=	null;

	private int cacheTime_refreshScore;

	private boolean  addFriendsLayoutisClicked = false;

	RelativeLayout content_layout ;

	private Dialog shareDialog;
	private float yPosition = 0;
	private boolean onclick = false;
	private Timer refreshMatchesTimer;
	private TimerTask refreshMatchesTask;

	private String vConversationUrl = null;
	public static boolean postClicked = false;
	RelativeLayout matchHeaderLayout;
	LayoutInflater inflater ;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;

	private String vSecColor = null;
	private String vSecTitleColor = null;
	
	private boolean expand = true;
	private boolean animation_completed = true;
	public  RelativeLayout header;
	private boolean isStripExpanded = false;
	private android.widget.LinearLayout.LayoutParams liParams;
	private LayoutParams params;
	private ListView leaderBoardList;



	private boolean isConversationUrlHref = false;

	private boolean isContestUrlHref = false;
	@Override
	public void onDestroy () {
		super.onDestroy();

		if( shareDialog != null && shareDialog.isShowing() ) {
			shareDialog.dismiss();
			shareDialog = null;
		}

		vConversationId = null;
		fromFragment = null;
		vContestId = null;


	}

	@Override
	public void onStop () {

		super.onStop();
		try {


			matchHeaderGenerator = null;

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
			if ( refreshScoreTask != null ) {
				refreshScoreTask.cancel();
				refreshScoreTask = null;
			}
			if ( refreshSubjectTimer != null ) {
				refreshSubjectTimer.cancel();
				refreshSubjectTimer = null;
			}



			if (refreshfriendsTask != null) {
				refreshfriendsTask.cancel();
				refreshfriendsTask = null;
			}
			if ( refreshFriendsTimer != null ) {
				refreshFriendsTimer.cancel();
				refreshFriendsTimer = null;
			}


			if ( refreshMatchesTimer != null ) {
				refreshMatchesTimer.cancel();
				refreshMatchesTimer = null;
			}
			if ( refreshMatchesTask != null ) {
				refreshMatchesTask.cancel();
				refreshMatchesTask = null;
			}

		} catch ( Exception e ) {
			Logs.show ( e );
		} catch ( Error e ) {
			Logs.show ( e ) ;
		}
	}

	


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		
		content_layout = (RelativeLayout) inflater.inflate( R.layout.matchhome, null);

		// get from bundle
		if (!isAgainActivated) {
			setConversationId ( getArguments() );
		}
		FlurryAgent.onEvent("conversation");
		return content_layout;
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
		
		Log.e("234", "MatchHomeFragment======>>>>vConversationId====>>>"+vConversationId);
		if (bundle != null && bundle.containsKey("vConversationUrl")) {
			vConversationUrl  = bundle.getString("vConversationUrl");
		} else {
			vConversationUrl = null;
		}
		
		
		if (bundle != null && bundle.containsKey("isHref")) {
			isConversationUrlHref   = bundle.getBoolean("isHref");
		} else{
			isConversationUrlHref   = false;
		}
	

		if (bundle != null && bundle.containsKey("vContestId")) {
			vContestId = bundle.getString("vContestId");
		} else {
			vContestId = null;
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

	public void onResume() {
		super.onResume();

		matchHeaderGenerator= null;

		try{


			if ( imageDownloader == null ) {
				imageDownloader = new ImageDownloader();
			}
			
			expand = true;
			LiveSportsFragment.isInLiveRoom = false;
			initialize(content_layout);

			isDownloading = false;
			deleteMarker();
			isInMatchRoom = true;
			roomConversationAdapter = null;
			friendGalleryAdapter = null;
			vContestUrl = null;
			isContestUrlHref  = false;
			friendName = "";


			if (! Util.isInternetAvailable() ) {

				cancelProgressDialog();
				PlayupLiveApplication.showToast(R.string.no_network);
				refreshMessages();
				refreshScores();
				refreshMatches();

			} else {

				getConversation();
			}

			publicMatchesTicketAdapter= null;


			// set values

			setValues();

			if( vContestId == null ) {
				vContestId = DatabaseUtil.getInstance().getContestIdFromConversationId( vConversationId );
			}

			
			setLiveCount();
			getCompetitionData();
			
		} catch ( Exception e ) {

			Logs.show ( e );

		}
	}


	private void showAssociatedContests () {

		try {

			if (publicMatchesTicketAdapter == null) {
				publicMatchesTicketAdapter = new PublicMatchesTicketAdapter( vContestId,matchGallery );
				matchGallery.setAdapter( publicMatchesTicketAdapter );
				
			} else {
				publicMatchesTicketAdapter.setData( vContestId );
			}
		} catch ( Exception e ) {
			Logs.show ( e );
		} 
	}

	@Override
	public void onAgainActivated(Bundle args) {

		isAgainActivated = true;
		setConversationId(args);

	}


	/**
	 * Date:18/07/2012
	 * Sprint:20
	 *  */
	private void initialize(RelativeLayout content_layout) {//VERIFIED

		try {
			if ( content_layout == null ) {
				return ;
			}

			matchHeaderLayout = ( RelativeLayout) content_layout.findViewById(R.id.matchHeaderLayout);
			addFriendImage = (ImageView) content_layout.findViewById(R.id.addFriends);
			noMessages = (LinearLayout) content_layout.findViewById(R.id.noMessages);
			addFriendsLayout = ( LinearLayout ) content_layout.findViewById(R.id.addFriendsLayout);
			listView = (ListView) content_layout.findViewById(R.id.userChatListView);
			addFrnds = (ImageView) content_layout.findViewById(R.id.addFrnds);
			friendGallery = (Gallery) content_layout.findViewById(R.id.friendGallery);
			noFriendsView = ( RelativeLayout ) content_layout.findViewById( R.id.noFriendsView );
			invite = ( TextView ) content_layout.findViewById( R.id.invite );
			write_post = (ImageView)content_layout.findViewById(R.id.write_post);
			messageIn = ( TextView )content_layout.findViewById( R.id.messageIn);
			stripDown = (RelativeLayout) content_layout.findViewById(R.id.stripDown);
			commonStrip = (RelativeLayout) content_layout.findViewById(R.id.commonStrip);
			matchHeaderBottom = ( RelativeLayout) content_layout.findViewById(R.id.matchHeaderBottom);


			if ( stripDown != null ) {
				stripDown.setVisibility(View.VISIBLE);
			}

			stripUp = (RelativeLayout) content_layout.findViewById(R.id.stripUp);
			matchGallery = (Gallery) content_layout.findViewById(R.id.matchGallery);
			
			liveCountDown = (TextView)content_layout.findViewById(R.id.stripLiveCountDown);
			liveCountUp = (TextView)content_layout.findViewById(R.id.stripLiveCountUp);
			stripLiveCount = (TextView)content_layout.findViewById(R.id.commonStripCount);

			matchGallery.setBackgroundColor(Color.parseColor("#8C9696"));
			matchGallery.setUnselectedAlpha(1.0f);

			// set listeners
			setListeners();


			doPolling();
			setLastViewed();

			// setTypefaces
			setTypefaces();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}

	private void setLastViewed() {	//VERIFIED

		try {
			if (vConversationId != null) {

				Runnable runnable = new Runnable () {

					@Override
					public void run() {
						try {
							DatabaseUtil dbUtil = DatabaseUtil.getInstance();
							dbUtil.setLastViewed(vConversationId);	
						} catch (Exception e) {
							Logs.show ( e );
						}

					}

				};
				Thread th = new Thread ( runnable );
				th.start();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
	}

	private void setListeners() {//VERFIED

		try {
			addFriendImage.setOnClickListener(this);
			addFrnds.setOnClickListener(this);
			write_post.setOnClickListener( this );
			stripDown.setOnTouchListener(this);
			stripUp.setOnTouchListener(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}



	/**
	 * cancelling the progress dialog.
	 */
	private void cancelProgressDialog() {

		try {
			if (noMessages != null && noMessages.getVisibility() == View.VISIBLE) {
				noMessages.setVisibility(View.GONE);
			}
			//		if(noFriendsView != null){
			//			noFriendsView.setVisibility(View.GONE);
			//		}
			//		if (listView != null) {
			//			listView.setVisibility(View.VISIBLE);
			//		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}

	private void showProgressDialog() {

		try {
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}





	private void setValues() {	//verified

		try {
			setRoomNameInTopBar();

			//setEditRoomImage();
			// set the match room data
			setRoomData();

			// set the room messages
			setRoomMessages();			
		} catch ( Exception e ) {
			Logs.show ( e );
		}


	}

	private void setTypefaces() {//VERIFIED

		try {
			invite.setTypeface( Constants.OPEN_SANS_REGULAR);
			messageIn.setTypeface( Constants.OPEN_SANS_REGULAR);

			liveCountDown.setTypeface( Constants.BEBAS_NEUE );
			liveCountUp.setTypeface( Constants.BEBAS_NEUE );
			stripLiveCount.setTypeface( Constants.BEBAS_NEUE );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
	}

	/**
	 * setting the room in the top bar fragment.
	 **/
	private void setRoomNameInTopBar() {	//verified
		Runnable r =  new Runnable () {

			@Override
			public void run() {
				try {
					
					HashMap< String, String > map = new HashMap<String, String>();
					
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String vChildColor = dbUtil.getSectionMainColor(vConversationId,"");
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
					
					Bundle b = new Bundle();
					b.putString("vMainColor",vMainColor );
					b.putString("vMainTitleColor",vMainTitleColor );
					
					Cursor c = PlayupLiveApplication
					.getDatabaseWrapper()
					.selectQuery(
							" SELECT vConversationName FROM match_conversation_node WHERE vConversationId = \""
							+ vConversationId + "\" ");
					if (c != null) {

						if (c.getCount() > 0) {
							c.moveToFirst();
							
							map.put("vConversationName", c.getString(c.getColumnIndex("vConversationName")));
							
							Message msg = new Message ();
							msg.setData(b);
							msg.obj = map;
							PlayupLiveApplication.callUpdateTopBarFragments(msg);
						}else{
							c.moveToFirst();
						
							map.put("vConversationName", "");
							
							Message msg = new Message ();
							msg.setData(b);
							msg.obj = map;
							PlayupLiveApplication.callUpdateTopBarFragments(msg);


						}
						c.close();
						c = null;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Logs.show ( e );
				}

			}





		};

		Thread th = new Thread ( r );
		th.start();


	}


	/**
	 * Setting the room messages
	 */
	private void setRoomMessages() {//verified

		Runnable r = new Runnable() {

			@Override
			public void run() {

				try {

					matchHomeData = PlayupLiveApplication
					.getDatabaseWrapper()
					.select (
							"SELECT  "
							+ "message_id_pk,vMessageHrefUrl,fan_profile_href,m.vNextHref AS vNextHref, message, message_timestamp, posted_by, fan_thumb_url, fan_profile_uid, fan_profile_url, m.vNextUrl AS vNextUrl, message_uid "
							+ "FROM message m"
							+ " LEFT JOIN conversation_message cm ON m.vConversationMessageId = cm.vConversationMessageId AND   m.isFromContestLobby = 0 "
							+ "LEFT JOIN   match_conversation_node mc ON mc.vConversationId = cm.vConversationId "
							+ "WHERE cm.vConversationId = \""
							+ vConversationId
							+ "\"  "
							+ " ORDER BY message_timestamp DESC ");

					deleteMarker();


					if (!isDownloading && ( matchHomeData == null || ( matchHomeData != null && matchHomeData.get( "message_timestamp" ).size() == 0 ) ) ) {
						if (PlayUpActivity.handler != null) {

							PlayUpActivity.handler.post(new Runnable() {

								@Override
								public void run() {
									try {
										cancelProgressDialog();
										listView.setVisibility(View.GONE);
										noFriendsView.setVisibility(View.VISIBLE);
									} catch (Exception e) {
										// TODO Auto-generated catch block
										Logs.show ( e );
									}

								}
							});
						}
					} else if ( matchHomeData != null && matchHomeData.get( "message_timestamp" ).size() > 0 ) {

						updateRecentConversationCount();
						if (PlayUpActivity.handler != null) {

							PlayUpActivity.handler.post(new Runnable() {

								@Override
								public void run() {
									try {
										cancelProgressDialog();
										listView.setVisibility(View.VISIBLE);
										noFriendsView.setVisibility(View.GONE);

										if (roomConversationAdapter != null) {
											roomConversationAdapter.setData ( matchHomeData, 
													vConversationId, vContestId,fromFragment, 
													listView,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor);
										} else {
											roomConversationAdapter = new RoomConversationAdapter ( matchHomeData, 
													vConversationId, vContestId,fromFragment, 
													listView,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor);
											listView.setAdapter(roomConversationAdapter);
										}
									} catch (Exception e) {
										// TODO Auto-generated catch block
										Logs.show ( e );
									}

								}
							});

						}

					}

				} catch (Exception e) {
					Logs.show ( e );
				}

			}
		};

		Thread t = new Thread(r);
		t.start();

	}





	
	private void  updateRecentConversationCount () {	//verified

		try {
			ContentValues values = new ContentValues();
			values.put( "iUnRead", 0 );

			JsonUtil json  =new JsonUtil();
			json.queryMethod1( Constants.QUERY_UPDATE, null, "recent", values, " vSubjectId = \"" + vConversationId + "\" ", null, false, true );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}

	
	private  void putMarker () {

		new Util().putDeleteMarker( true , vConversationId);	//verified
	}


	private void deleteMarker () {	//VERIFIED

		// update the count in recent activity.
		ContentValues values = new ContentValues();
		values.put( "iUnRead",  0 );
		JsonUtil json  =new JsonUtil();
		json.queryMethod1( Constants.QUERY_UPDATE, null, "recent", values, " vSubjectId = \"" + vConversationId + "\" ", null, false, true );



		new Util().putDeleteMarker( false , vConversationId);
	}


	/**
	 * Setting the room data
	 * 
	 * @throws JSONException
	 */


	MatchHeaderGenerator matchHeaderGenerator = null;


	private void setRoomData() {	//verified

		Runnable r = new Runnable(){

			@Override
			public void run() {
				try{


					if(PlayUpActivity.handler != null){

						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {



								try {
									
									if (fromFragment != null && fromFragment.contains("MatchRoomFragment") && vContestId != null) {





										final Hashtable<String, List<String>> data = PlayupLiveApplication.getDatabaseWrapper().select("SELECT vContestId,dScheduledStartTime, dStartTime, dEndTime, iTotal1, iTotal2,vSummary1,vSummary2, " +
												"vHomeTeamId, vAwayTeamId, vSummary ,vSportsName,vCompetitionName,iWickets1, iWickets2,vOvers,vRunRate,vLastBall,vPlayerFirstName1,vPlayerFirstName2,vPlayerLastName1,vPlayerLastName2" +
												",vRole2,vRole1,vStats1,vStats2,vStrikerFirstName,vStrikerLastName,vStrikerStats,vNonStrikerFirstName,vNonStrikerLastName,vNonStrikerStats,vAnnotation,vAnnotation2,vSummary1,vSummary2,iGoals1,iBehinds1,iSuperGoals1,iGoals2,iBehinds2,iSuperGoals2,iInnnings,vInnningsHalf,iBalls1,iBalls2,iOut1,iOut2,iStrikes1,iStrikes2,vBase1,vBase2,iHasLiveUpdates,vLastEventName, vShortMessage, vLongMessage,vSportType,iActive1,iActive2, " +
												"( SELECT vDisplayName FROM teams WHERE vTeamId = vHomeTeamId  ) AS vHomeDisplayName, " +
												"( SELECT vDisplayName FROM teams WHERE vTeamId = vAwayTeamId  ) AS vAwayDisplayName," +
												"(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vHomeTeamId ) AS vHomeCalendarUrl," +
												"(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vAwayTeamId ) AS vAwayCalendarUrl     FROM contests WHERE vContestId = \""
												+ vContestId + "\"");

										String sportType = null;
										if( data!= null && data.get("vSportType")!=null )
											sportType = data.get("vSportType").get(0);
										
										if( sportType == null )
											return;
										//setting and inflating the layouts for match header based on sport type
										if( matchHeaderGenerator== null ) {
											
											View headerLayout;
											if( inflater == null )
												inflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
											
											if( sportType!= null && sportType.equalsIgnoreCase(Constants.LEADERBOARD)  ) {
												headerLayout = (View) inflater.inflate(R.layout.match_header_leaderboard, null);
												leaderBoardList = (ListView) headerLayout.findViewById(R.id.leaderBoardList);
											} else if( sportType!= null && sportType.equalsIgnoreCase(Constants.SET_BASED_DATA)  )  {
												headerLayout = (View) inflater.inflate(R.layout.match_header_setbased, null);
											} else {
												headerLayout = (View) inflater.inflate(R.layout.match_header_normal, null);
											}
											
											matchHeaderLayout.setBackgroundResource(0);
											matchHeaderLayout.removeAllViews();
											matchHeaderLayout.addView( headerLayout );
											matchHeaderGenerator = new MatchHeaderGenerator(data, matchHeaderLayout ,false, true);
											setDummyViewsHeight(sportType);
											headerLayout = null;										
										} else {
//											if( !expand )
//											setDummyViewsHeight(sportType);
											matchHeaderGenerator.setData(data);
										}


									} else if (vConversationId != null) {



										final Hashtable<String, List<String>> data = PlayupLiveApplication.getDatabaseWrapper().select("SELECT vContestId,dScheduledStartTime, dStartTime, dEndTime, iTotal1, iTotal2, vSummary1,vSummary2, vHomeTeamId, vAwayTeamId,vSummary," +
												"vSportsName,vCompetitionName,iWickets1, iWickets2,vOvers,vRunRate,vLastBall,vPlayerFirstName1,vPlayerFirstName2,vPlayerLastName1,vPlayerLastName2" +
												",vRole2,vRole1,vStats1,vStats2,vStrikerFirstName,vStrikerLastName,vStrikerStats,vNonStrikerFirstName,vNonStrikerLastName,vNonStrikerStats,vAnnotation,vAnnotation2,vSummary1,vSummary2,iGoals1,iBehinds1,iSuperGoals1,iGoals2,iBehinds2,iSuperGoals2,iInnnings,vInnningsHalf,iBalls1,iBalls2,iOut1,iOut2,iStrikes1,iStrikes2,vBase1,vBase2,iHasLiveUpdates,vLastEventName, vShortMessage, vLongMessage,vSportType,iActive1,iActive2, " +
												"( SELECT vDisplayName FROM teams WHERE vTeamId = vHomeTeamId  ) AS vHomeDisplayName, " +
												"( SELECT vDisplayName FROM teams WHERE vTeamId = vAwayTeamId  ) AS vAwayDisplayName ,   "+
												"(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vHomeTeamId ) AS vHomeCalendarUrl," +
												"(  SELECT  vCalendarUrl FROM teams WHERE vTeamId = vAwayTeamId ) AS vAwayCalendarUrl  " 
												+ "FROM contests "
												+ "WHERE vContestId = ( SELECT cl.vContestId FROM contest_lobby cl , match_conversation_node m WHERE m.vConversationId  = \""
												+ vConversationId
												+ "\" AND  m.vContestLobbyUid = cl.vContestLobbyUid ) ");
										
										String sportType = null;
										if( data!= null && data.get("vSportType")!=null )
											sportType = data.get("vSportType").get(0);
										if( sportType == null )
											return;
										
										
										//setting and inflating the layouts for match header based on sport type
										if( matchHeaderGenerator== null ) {
										
											View headerLayout;
											if( inflater == null )
												inflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
											
											if( sportType!= null && sportType.equalsIgnoreCase(Constants.LEADERBOARD)  ) {
												headerLayout = (View) inflater.inflate(R.layout.match_header_leaderboard, null);
												leaderBoardList = (ListView) headerLayout.findViewById(R.id.leaderBoardList);
											} else if( sportType!= null && sportType.equalsIgnoreCase(Constants.SET_BASED_DATA)  )  {
												headerLayout = (View) inflater.inflate(R.layout.match_header_setbased, null);
											} else {
												headerLayout = (View) inflater.inflate(R.layout.match_header_normal, null);
											}
											
											matchHeaderLayout.setBackgroundResource(0);
											matchHeaderLayout.removeAllViews();
											matchHeaderLayout.addView( headerLayout );
											matchHeaderGenerator = new MatchHeaderGenerator(data, matchHeaderLayout, false , true );
											setDummyViewsHeight(sportType);
											headerLayout = null;										
											} else {
//												if( !expand )
//													setDummyViewsHeight(sportType);
												matchHeaderGenerator.setData(data);
										}


									}
								} catch (Exception e) {
									Logs.show ( e );
								}


							}
						});




					}


				} catch (Exception e) {
					Logs.show ( e );
				}


			}




		};

		Thread t = new Thread(r);
		t.start();

	}



	@Override
	public void onUpdate ( final Message msg ) {

		final String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
		
		

		try {

			
			if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
				
				getConversation();
				
				getCompetitionData();
			}
			if ( msg != null && msg.obj != null && msg.obj.toString().contains ( "share" ) ) {

				if ( msg.obj.toString ( ).equalsIgnoreCase ( "shareScores" ) ) {

					if ( DatabaseUtil.getInstance ( ).isUserAnnonymous ( ) ) {

						Bundle bundle = new Bundle();
						bundle.putBoolean("sharing", true );
						bundle.putString("fromFragment", topFragmentName);
						PlayupLiveApplication.getFragmentManagerUtil().setFragment( "ProviderFragment", bundle);

					} else if( vContestId != null ) {
						showShareDialog( false );
					}

				} else if ( msg.obj.toString().contains ( "share_response" ) ) {

					String provider = msg.obj.toString();

					if ( provider.split ( ":" ).length > 0 ) {
						provider = provider.split(":")[1];
					}

					if( msg.arg1 == 1 ) {
						PlayupLiveApplication.showToast( PlayUpActivity.context.getResources ( ) .getString ( R.string.shareResponse ) + " " + provider );
					} else {
						PlayupLiveApplication.showToast(R.string.shareFailure);
					}

				}
				return;
			}

			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "PostMessage" ) ) {

				Bundle bundle = new Bundle();
				bundle.putString ( "vConversationId", vConversationId );
				bundle.putString("vMainColor",vMainColor );
				bundle.putString("vMainTitleColor",vMainTitleColor );
				bundle.putString( "vSecColor",vSecColor );			
				bundle.putString( "vSecTitleColor",vSecTitleColor );
				bundle.putString("fromFragment", topFragmentName);
				if ( friendName != null && friendName.trim ( ).length ( ) > 0 ) {				
					bundle.putString ( "vFriendName", friendName );
				}
				PlayupLiveApplication.getFragmentManagerUtil().setFragment ( "PostMessageFragment", bundle);

			}

			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "SHOW_GALLERY" ) ) {

				showHideOnlineFriends( true );
				return;
			}
			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "InviteFriends" ) ) {

				stopPolling();
				Bundle bundle = new Bundle ();
				bundle.putString( "vConversationId", vConversationId );
				bundle.putString("vMainColor",vMainColor );
				bundle.putString("vMainTitleColor",vMainTitleColor );
				bundle.putString( "vSecColor",vSecColor );			
				bundle.putString( "vSecTitleColor",vSecTitleColor );
				PlayupLiveApplication.getFragmentManagerUtil().setFragment( "InviteFriendFragment", bundle );
				return;
			}
			if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("handleBackButton")) {

				
				if (fromFragment == null) {
					PlayupLiveApplication.getFragmentManagerUtil().popBackStackImmediate();
				} else{
					PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill ( fromFragment );
				}

				stopPolling();
				return;
			}

			if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "callMatchRoomFragment" ) ) {

				if ( fromFragment != null && fromFragment.contains ( "MatchRoomFragment" ) ) {

					PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill ( fromFragment );

				} else {
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								final Bundle bundle = new Bundle();
								if (vContestId == null) {

									DatabaseUtil dbUtil = DatabaseUtil.getInstance();
									vContestId = dbUtil.getContestIdFromConversationId(vConversationId);
									dbUtil = null;
								}

								Cursor c = PlayupLiveApplication.getDatabaseWrapper()
								.selectQuery(
										" SELECT c.vCompetitionName AS vCompetitionName FROM contests c LEFT JOIN match_conversation_node mcn " +
										"ON c. vContestId = mcn.vContestId WHERE  mcn.vConversationId = \"" + vConversationId + "\" ");
								if (c != null) {

									if (c.getCount() > 0) {
										c.moveToFirst();
										bundle.putString("topBarName", c.getString(c
												.getColumnIndex("vCompetitionName")));
									}
									c.close();
									c = null;
								}
								bundle.putString("fromFragment", topFragmentName);
								bundle.putString("vContestId", vContestId);
								bundle.putString("vMainColor",vMainColor );
								bundle.putString("vMainTitleColor",vMainTitleColor );
								bundle.putString( "vSecColor",vSecColor );			
								bundle.putString( "vSecTitleColor",vSecTitleColor );
								
								

								if(PlayUpActivity.handler != null){

									PlayUpActivity.handler.post(new Runnable() {
										public void run() {
											try {
												PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchRoomFragment", bundle);
											} catch (Exception e) {
												Logs.show(e);
											}

										}
									});

								}
							} catch (Exception e) {
								Logs.show ( e );
							}


						}
					}).start();



				}
				stopPolling();
			}

			if ( msg != null && msg.obj != null && msg.obj.toString().contains ( "MatchHomeFragment" ) ) {

				if ( PlayUpActivity.handler != null) {

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

								// set the match room data
								setRoomData();

								//setValues();

								if (msg.obj.toString().equalsIgnoreCase("MatchHomeFragment_getMessages")) {

									if(msg.arg1 == 1) {
										cancelProgressDialog();
									} else {
										if(fromFragment != null &&  (fromFragment.equalsIgnoreCase("WelcomeFragment") ||
												fromFragment.equalsIgnoreCase("DefaultFragment")||
												fromFragment.equalsIgnoreCase("NewsFragment") ||fromFragment.equalsIgnoreCase("LeagueLobbyFragment"))){
											vConversationId = DatabaseUtil.getInstance().getConversationIdFromConversationUrl(vConversationUrl);
										}

										getConversationSubject();
										getConversationMessages( false );

										refreshMessages();
									}


								}

								if(msg.obj.toString().equalsIgnoreCase("MatchHomeFragment_getScores")){


									getAssociatedData();
									refreshScores();
								}
								if (msg.obj.toString().equalsIgnoreCase( "MatchHomeFragment_refresh")) {
									// call updates for msgs
									//							cancelProgressDialog();
									setRoomMessages();

									refreshMessages();

								}
								if (msg.obj.toString().equalsIgnoreCase ( "MatchHomeFragment_friends" ) ) {

									refreshFriends();
									showHideOnlineFriends ( true );
								}

								if (msg.obj.toString().equalsIgnoreCase ( "MatchHomeFragment_Gap_Messages" ) ) {
									setRoomMessages();

								}
							} catch (Exception e) {
								Logs.show ( e );
							}


						}

					});
				}
			}

			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "assoicated_contest_update" ) ) {

				if ( PlayUpActivity.handler != null ) {

					PlayUpActivity.handler.post( new Runnable() {

						@Override
						public void run() {
							try {
							

								if ( !isVisible() ) {
									return;
								}

								showAssociatedContests();

								refreshMatches();

								setLiveCount();
								

							} catch (Exception e) {
								Logs.show ( e );
							}

						}
					});
				}
			}
		} catch ( Exception e) {
			Logs.show(e);
		}
	}



	private void fetchConversationFriends () {

		if(runnableList != null && !runnableList.containsKey(Constants.GET_CONVERSATION_FRIENDS) && Util.isInternetAvailable() ){
			runnableList.put(Constants.GET_CONVERSATION_FRIENDS, new Util().getConversationFriends( vConversationId,runnableList ));
		}
	}

	@Override
	public void onClick(View v) {
		
		String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
		

		if ( v == null ) {
			return;
		}

		postClicked = false;
		Bundle bundle = null;
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();

		switch (v.getId()) {

		case R.id.addFriends:

			// if user is anonymous then show provider fragment.
			if (dbUtil.isUserAnnonymous()) {

				// show toast
				//PlayupLiveApplication.showToast(R.string.login_to_invite);

				bundle = new Bundle();
				bundle.putString("fromFragment", topFragmentName);
				PlayupLiveApplication.getFragmentManagerUtil().setFragment( "ProviderFragment", bundle);

				stopPolling();
			} else {

				if( addFriendsLayout.getVisibility() == 0  )
					addFriendsLayoutisClicked = true;
				else
					addFriendsLayoutisClicked = false;

				if ( addFriendsLayoutisClicked ){

					addFriendsLayoutisClicked = false;
				}else
					addFriendsLayoutisClicked = true;



				if ( Util.isInternetAvailable() ) {
					fetchConversationFriends( );
				} else {

					// remove conversation id from the friends table
					dbUtil.removeConversationFriends ( vConversationId , true );
				}
				showHideOnlineFriends(!(addFriendsLayout.getVisibility() == 0 ? true : false) );

			}



			break;

		case R.id.addFrnds:
			stopPolling();
			bundle = new Bundle ();
			bundle.putString( "vConversationId", vConversationId );
			bundle.putString("vMainColor",vMainColor );
			bundle.putString("vMainTitleColor",vMainTitleColor );
			bundle.putString( "vSecColor",vSecColor );			
			bundle.putString( "vSecTitleColor",vSecTitleColor );
			PlayupLiveApplication.getFragmentManagerUtil().setFragment( "InviteFriendFragment", bundle );
			break;

		case R.id.editRoom:
			stopPolling();
			bundle = new Bundle ();
			bundle.putString("vConversationId", vConversationId);
			bundle.putString("editRoom", "editRoom");
			bundle.putString("vMainColor",vMainColor );
			bundle.putString("vMainTitleColor",vMainTitleColor );
			bundle.putString( "vSecColor",vSecColor );			
			bundle.putString( "vSecTitleColor",vSecTitleColor );
			bundle.putString( "fromFragment", topFragmentName );
			PlayupLiveApplication.getFragmentManagerUtil().setFragment("CreateLobbyRoomFragment", bundle );
			break;

		case R.id.write_post:

			// if user is anonymous then show provider fragment
			if (dbUtil.isUserAnnonymous()) {
				// show toast
				//PlayupLiveApplication.showToast(R.string.login_to_post);
				postClicked  = true;
				bundle = new Bundle();
				bundle.putString("fromFragment", topFragmentName);
				bundle.putString("vConversationId", vConversationId);
				bundle.putString("vMainColor",vMainColor );
				bundle.putString("vMainTitleColor",vMainTitleColor );
				
				
				PlayupLiveApplication.getFragmentManagerUtil().setFragment("ProviderFragment", bundle);
			} else {
				bundle = new Bundle();
				bundle.putString("vConversationId", vConversationId);
				bundle.putString("vMainColor",vMainColor );
				bundle.putString("vMainTitleColor",vMainTitleColor );
				bundle.putString( "vSecColor",vSecColor );			
				bundle.putString( "vSecTitleColor",vSecTitleColor );
				bundle.putString("fromFragment", topFragmentName);
				PlayupLiveApplication.getFragmentManagerUtil().setFragment("PostMessageFragment", bundle);
			}
			stopPolling();
			PostMessageFragment.isHomeTapped = false;
			break;

		}
		dbUtil = null;

	}

	/**
	 * showing and hiding the online friends list
	 */
	private void showHideOnlineFriends(boolean show) {
		// clearing the online list get all refreshed data.

		try{
			if (show) {

				if ( addFriendsLayoutisClicked ) {
					addFriendsLayout.setVisibility(View.VISIBLE);	
					addFriendImage.setImageResource(R.drawable.friends_d_icon);

				}

				DatabaseUtil dbUtil = DatabaseUtil.getInstance();
				Hashtable<String, List<String>> data = dbUtil.getConversationFriendsData ( vConversationId);
				if ( friendGalleryAdapter == null ) {
					friendGalleryAdapter = new FriendGalleryAdapter( data );

					friendGallery.setAdapter(friendGalleryAdapter);

					DisplayMetrics metrics = new DisplayMetrics();
					PlayUpActivity.context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
					MarginLayoutParams mlp = (MarginLayoutParams) friendGallery.getLayoutParams();
					mlp.setMargins(-( (metrics.widthPixels/2) +mlp.leftMargin ) , mlp.topMargin, mlp.rightMargin, mlp.bottomMargin );

					friendGallery.setOnItemClickListener( this );


				} else {

					friendGalleryAdapter.setData ( data );
				}

			} else {
				addFriendsLayout.setVisibility(View.INVISIBLE);
				addFriendImage.setImageResource(R.drawable.private_friend_selector);
			}
		}catch(Exception e){
			Logs.show(e);
		}

	}

	public void doPolling() {	//VERFIED

		try {
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
						presenceDeleteCall(true);
					} catch (Exception e) {
						Logs.show ( e );
					}
				}

			};

			pollingTimer.schedule(pollingTask, 300, 6000);
		} catch (Exception e) {
			Logs.show ( e );
		}

	}

	/**
	 * refreshing the mesages
	 */
	public void refreshMessages() {

		if ( refreshMessagesTimer != null ) {
			refreshMessagesTimer.cancel();
			refreshMessagesTimer = null;
		}
		refreshMessagesTimer = new Timer ();

		if ( refreshMessagesTask != null ) {
			refreshMessagesTask.cancel();
			refreshMessagesTask = null;
		}
		new Thread(new Runnable(){

			@Override
			public void run() {

				try {
					refreshMessagesTask = new TimerTask() {

						@Override
						public void run() {

							try {
								if (MatchHomeFragment.isInMatchRoom && Util.isInternetAvailable() ) {

									
//									getConversation();
									
									getConversationMessages( true  );

								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								Logs.show(e);
							}
						}
					};
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String vMessageUrl = dbUtil.getConversationMessagePollingUrl(vConversationId);
					int cacheTime = -1;

					if(vMessageUrl != null){
						cacheTime = Integer.parseInt( dbUtil.getCacheTime( vMessageUrl ) ) ;
					}
					dbUtil = null;




					if(cacheTime == -1)
						cacheTime	=	1;
					
					
					if ( cacheTime > 0 ) {
						refreshMessagesTimer.schedule(refreshMessagesTask, 0, (cacheTime * 1000));
					}



				} catch (Exception e) {
					Logs.show ( e );
				}



			}
		}).start();


	}

	public void refreshFriends () {


		try {
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
						if ( MatchHomeFragment.isInMatchRoom && Util.isInternetAvailable ( ) ) {
							fetchConversationFriends();
						}
					} catch (Exception e) {
						Logs.show ( e );
					}
				}
			};

			Runnable r = new Runnable () {

				@Override
				public void run() {
					try {
						DatabaseUtil dbUtil = DatabaseUtil.getInstance();

						String vConversationFriendsUrl = dbUtil.getConversationFriendsUrlForRefresh ( vConversationId );

						int cacheTime = Integer.parseInt( dbUtil.getCacheTime( vConversationFriendsUrl ) );
						dbUtil = null;
						
						
						if (cacheTime > 0) {
							refreshFriendsTimer.schedule( refreshfriendsTask, cacheTime * 1000, (cacheTime * 1000));
						}
					} catch (Exception e) {
						Logs.show ( e );
					}
				}

			};
			Thread th = new  Thread ( r  );
			th.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}


	}

	/**
	 * refreshing the subject/scores
	 */
	public void refreshScores() {	//VERIFIED

		if (refreshSubjectTimer != null) {
			refreshSubjectTimer.cancel();
		}
		refreshSubjectTimer = new Timer();

		if (refreshScoreTask != null) {
			refreshScoreTask.cancel();
			refreshScoreTask = null;
		}
		Runnable r = new Runnable(){

			@Override
			public void run() {

				try {
					refreshScoreTask = new TimerTask() {

						@Override
						public void run() {


							try {
								if (MatchHomeFragment.isInMatchRoom && Util.isInternetAvailable()) {
									getConversationSubject();
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								Logs.show(e);
							}
						}
					};

					if(vContestUrl != null){

						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						String tmp 	=	dbUtil.getCacheTime(vContestUrl);
						dbUtil = null;
						if(tmp!=null)
							cacheTime_refreshScore	=	Integer.parseInt(tmp);
					}

					if (cacheTime_refreshScore > 0) {
						refreshSubjectTimer.schedule(refreshScoreTask, cacheTime_refreshScore * 1000, (cacheTime_refreshScore * 1000));
					}
				} catch (Exception e) {
					Logs.show ( e );
				}


			}



		};
		Thread th = new Thread(r);
		th.start();

	}

	/**
	 * call the server to get match room data
	 */
	private void getConversation() {	//verified

		showProgressDialog();
		isDownloading = true;
		Runnable r = new Runnable() {
			
			@Override
			public void run() {

				try {
				//	Log.e("234", "MATCHR_HOME_FRAGMENT======>>>>vConversationId====>>>"+vConversationId);
					if ( vConversationUrl != null && vConversationUrl.trim().length() > 0 ) {

						if ( runnableList != null && !runnableList.containsKey ( Constants.GET_CONVERSATION ) ) {
							runnableList.put ( Constants.GET_CONVERSATION, new Util().getConversation ( vConversationUrl, runnableList,isConversationUrlHref ) );
						}

					} else {

						Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
								" SELECT vSelfUrl,vHref FROM match_conversation_node WHERE vConversationId = \""
								+ vConversationId + "\" ");

						if (  c != null && c.getCount() > 0 ) {

							c.moveToFirst();
							String conversationURL = c.getString(c.getColumnIndex("vHref"));
							
							if(conversationURL != null && conversationURL.trim().length() > 0){
								if(runnableList!=null&&!runnableList.containsKey(Constants.GET_CONVERSATION) && Util.isInternetAvailable() ){
									runnableList.put(Constants.GET_CONVERSATION,new Util().getConversation(conversationURL,runnableList,true));
								}
							}else{
								conversationURL = c.getString(c.getColumnIndex("vSelfUrl"));
								if(runnableList!=null&&!runnableList.containsKey(Constants.GET_CONVERSATION) && Util.isInternetAvailable() ){
									runnableList.put(Constants.GET_CONVERSATION,new Util().getConversation(conversationURL,runnableList,false));
								}
							}

							
							c.close();

						} else {
							if ( c != null ) {
								c.close();

								c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
										" SELECT vSubjectUrl,vSubjectHref FROM recent WHERE vSubjectId = \""
										+ vConversationId + "\" ");

								if ( c != null && c.getCount() > 0 ) {

									c.moveToFirst();

									String conversationURL = c.getString(c.getColumnIndex("vSubjectHref"));
									
									if(conversationURL != null && conversationURL.trim().length() > 0){
										
										if(runnableList!=null&&!runnableList.containsKey(Constants.GET_CONVERSATION) && Util.isInternetAvailable() ){
											runnableList.put(Constants.GET_CONVERSATION,new Util().getConversation(conversationURL,runnableList,true));
										}
										
									}else{
										conversationURL = c.getString(c.getColumnIndex("vSubjectUrl"));
										if(runnableList!=null&&!runnableList.containsKey(Constants.GET_CONVERSATION) && Util.isInternetAvailable() ){
											runnableList.put(Constants.GET_CONVERSATION,new Util().getConversation(conversationURL,runnableList,false));
										}
									}
									
									c.close();
									return;

								} else {
									if ( c != null ) {
										c.close();
									}

									c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
											" SELECT vSubjectUrl,vSubjectHrefUrl FROM my_friends_live WHERE vSubjectId = \""
											+ vConversationId + "\" ");

									if ( c != null && c.getCount() > 0 ) {

										c.moveToFirst();

										String conversationURL = c.getString(c.getColumnIndex("vSubjectHrefUrl"));
										if(conversationURL != null && conversationURL.trim().length() > 0){
											
											if(runnableList!=null&&!runnableList.containsKey(Constants.GET_CONVERSATION) && Util.isInternetAvailable() ){
												runnableList.put(Constants.GET_CONVERSATION,new Util().getConversation(conversationURL,runnableList,true));
											}
											
										}else{
											conversationURL = c.getString(c.getColumnIndex("vSubjectUrl"));
											if(runnableList!=null&&!runnableList.containsKey(Constants.GET_CONVERSATION) && Util.isInternetAvailable() ){
												runnableList.put(Constants.GET_CONVERSATION,new Util().getConversation(conversationURL,runnableList,false));
											}
										}
										
										c.close();
										return;
									} else {
										if ( c != null ) {
											c.close();
										}

										c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
												" SELECT vConversationUrl,vConversationHrefUrl FROM notification WHERE vConversationId = \""
												+ vConversationId + "\" ");

										if ( c != null && c.getCount() > 0 ) {
											c.moveToFirst();

											String conversationURL = c.getString(c.getColumnIndex("vConversationHref"));
											if(conversationURL != null && conversationURL.trim().length() > 0){
												
												if(runnableList != null&&!runnableList.containsKey(Constants.GET_CONVERSATION) && Util.isInternetAvailable() ){
													runnableList.put(Constants.GET_CONVERSATION,new Util().getConversation(conversationURL,runnableList,true));
												}
												
											}else{
												conversationURL = c.getString(c.getColumnIndex("vConversationUrl"));
												if(runnableList != null&&!runnableList.containsKey(Constants.GET_CONVERSATION) && Util.isInternetAvailable() ){
													runnableList.put(Constants.GET_CONVERSATION,new Util().getConversation(conversationURL,runnableList,false));
												}
											}
											
											c.close();
											return;
										} else {
											if ( c != null ) {
												c.close();
											}
										}
									}

								}
							}

						}

					}
				} catch (Exception e) {
					Logs.show ( e );
				}
			}
		};

		Thread th = new Thread(r);
		th.start();


	}

	/**
	 * call the server to get match room's messages
	 */
	private void getConversationMessages( final boolean fromRefresh ) {	//VERIFIED


		new Thread(new Runnable(){



			@Override
			public void run() {
				try {
					if(runnableList != null && !runnableList.containsKey(Constants.GET_CONVERSATION_MESSAGES) && Util.isInternetAvailable() ){
						String vMessageUrl;


						DatabaseUtil dbUtil = DatabaseUtil.getInstance();
						vMessageUrl = dbUtil.getConversationMessagePollingUrl(vConversationId);
						dbUtil = null;
						if (vMessageUrl != null && vMessageUrl.trim().length() > 0) {
							runnableList.put(Constants.GET_CONVERSATION_MESSAGES, new Util().getConversationMessages(vMessageUrl, vConversationId,runnableList, fromRefresh ));
						}
					}
				} catch (Exception e) {
					Logs.show ( e );
				}

			}



		}).start();


	}



	private void getConversationSubject() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Cursor c;

					if( vContestUrl == null){
						c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(" SELECT vContestId FROM match_conversation_node	WHERE vConversationId = \""
								+ vConversationId + "\" ");
						if (c != null) {
							if (c.getCount() > 0) {
								c.moveToFirst();
								vContestId = c.getString(c.getColumnIndex("vContestId"));
								
								if ( PlayUpActivity.handler != null ) {
									PlayUpActivity.handler.post( new Runnable () {

										@Override
										public void run() {
											try {
												showAssociatedContests();
											} catch (Exception e) {
												// TODO Auto-generated catch block
												Logs.show(e);
											}
										}
										
									});
								}
								
								Cursor contest_c = PlayupLiveApplication.getDatabaseWrapper()
								.selectQuery(
										" SELECT vContestUrl,vContestHref FROM contests WHERE vContestId = \""
										+ vContestId + "\" ");
								if (contest_c != null) {

									if (contest_c.getCount() > 0) {
										contest_c.moveToFirst();
										vContestUrl = contest_c.getString(contest_c
												.getColumnIndex("vContestHref"));
										
										if(vContestUrl != null && vContestUrl.trim().length() > 0){
											isContestUrlHref = true;
											if(runnableList!=null&&!runnableList.containsKey(Constants.GET_CONTEST_CONVERSATION_MESSAGES) && Util.isInternetAvailable() ){
												runnableList.put(Constants.GET_CONTEST_CONVERSATION_MESSAGES,new Util().getContestsData(vContestUrl,runnableList,true));
											}
										}else{
											isContestUrlHref = false;
											vContestUrl = contest_c.getString(contest_c
													.getColumnIndex("vContestUrl"));
											
											if(runnableList!=null&&!runnableList.containsKey(Constants.GET_CONTEST_CONVERSATION_MESSAGES) && Util.isInternetAvailable() ){
												runnableList.put(Constants.GET_CONTEST_CONVERSATION_MESSAGES,new Util().getContestsData(vContestUrl,runnableList,false));
											}
										}
										

									}
									contest_c.close();
									contest_c = null;
								}
							}
							c.close();
							c = null;
						}

					}
					else{

						if(runnableList!=null&&!runnableList.containsKey(Constants.GET_CONTEST_CONVERSATION_MESSAGES) && Util.isInternetAvailable() ){
							runnableList.put(Constants.GET_CONTEST_CONVERSATION_MESSAGES,new Util().getContestsData(vContestUrl,runnableList,isContestUrlHref));

						}

					}
				} catch (Exception e) {
					Logs.show ( e );
				}

			}
		}).start();



	}




	/**
	 * making a presence or delete call
	 */
	private void presenceDeleteCall(final boolean isPresenceCall) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery(
							" SELECT vPresenceUrl,vPresenceHref FROM match_conversation_node WHERE vConversationId = \""
							+ vConversationId + "\" ");
					if (c != null) {

						if (c.getCount() > 0) {
							c.moveToFirst();

							String vPresenceUrl = c.getString(c.getColumnIndex("vPresenceHref"));
							if(vPresenceUrl != null && vPresenceUrl.trim().length() > 0)
							{
								new Util().makePresenceDeleteCall(vPresenceUrl, isPresenceCall,true);
							}else{
								vPresenceUrl = c.getString(c.getColumnIndex("vPresenceUrl"));
								new Util().makePresenceDeleteCall(vPresenceUrl, isPresenceCall,false);
							}

						}
						c.close();
						c = null;
					}
				} catch (Exception e) {
					Logs.show ( e );
				}
			}
		}).start();


	}

	public void stopPolling() {

		try {
			/**
			 * Timer Stopping for Messages
			 */
			if (pollingTask != null) {
				if (pollingTask.cancel()) {
					presenceDeleteCall(false);
				}
			}

			if (refreshMessagesTask != null) {
				refreshMessagesTask.cancel();
				refreshMessagesTask = null;
			}
			if (refreshMessagesTimer != null) {
				refreshMessagesTimer.cancel();
			}
			refreshMessagesTimer = null;

			/**
			 * Timer Stopping for Sores
			 */

			if (refreshScoreTask != null) {
				refreshScoreTask.cancel();
				refreshScoreTask = null;
			}
			if (refreshSubjectTimer != null) {
				refreshSubjectTimer.cancel();
			}
			refreshSubjectTimer = null;

			/**
			 * Timer Stopping for friends
			 */

			if (refreshfriendsTask != null) {
				refreshfriendsTask.cancel();
				refreshfriendsTask = null;
			}
			if (refreshFriendsTimer != null) {
				refreshFriendsTimer.cancel();
			}
			refreshFriendsTimer = null;


			if ( refreshMatchesTimer != null ) {
				refreshMatchesTimer.cancel();
				refreshMatchesTimer = null;
			}
			if ( refreshMatchesTask != null ) {
				refreshMatchesTask.cancel();
				refreshMatchesTask = null;
			}
		} catch (Exception e) {
			Logs.show(e);
		}
	}

	public static boolean isInMatchRoom = false;
	public static String friendName;

	@Override
	public void onPause() {
		super.onPause();

		matchHeaderGenerator = null;
		putMarker();
		stopPolling();

		isInMatchRoom = false;
		cancelProgressDialog();

		if ( refreshMatchesTimer != null ) {
			refreshMatchesTimer.cancel();
			refreshMatchesTimer = null;
		}
		if ( refreshMatchesTask != null ) {
			refreshMatchesTask.cancel();
			refreshMatchesTask = null;
		}
	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long id ) {

		if ( view.getTag( R.id.activity_list_relativelayout ) == null ) {
			return;
		}
		final String vProfileId = view.getTag( R.id.activity_list_relativelayout ).toString();


		if ( vProfileId == null ) {
			return;
		}
		if ( vProfileId != null && vProfileId.trim().length() == 0 ) {
			return;
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					
					String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
					
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result = dbUtil.getProfileUrl ( vProfileId );
					String  vProfileUrl = null;
					boolean isHref = false;
					if(result != null && result.containsKey("isHref")){
						
						isHref = (Boolean) result.get("isHref");
						vProfileUrl = (String) result.get("url");
					}
					dbUtil = null;
					// new Util().getFanProfileData ( vProfileUrl );

					int myId=-1;
					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT iId FROM user WHERE isPrimaryUser = \"1\" ");
					if ( c != null ) {

						if ( c.getCount() > 0 ) {
							c.moveToFirst();
							myId = c.getInt( c.getColumnIndex( "iId" ) );
						}
						c.close();
						c = null;
					}


					final Bundle bundle = new Bundle();
					bundle.putString( "vSelfUrl", vProfileUrl );
					
					bundle.putBoolean( "isHref", isHref );
					
					
					bundle.putString( "vConversationId", vConversationId );
					bundle.putString( "vContestId", vContestId );
					bundle.putInt("myId", myId);
					
					bundle.putString("vMainColor",vMainColor );
					bundle.putString("vMainTitleColor",vMainTitleColor );
					bundle.putString( "vSecColor",vSecColor );			
					bundle.putString( "vSecTitleColor",vSecTitleColor );

					FragmentManagerUtil fragmentManagerUtil = PlayupLiveApplication.getFragmentManagerUtil();
					if ( fragmentManagerUtil.checkIfFragmentExists( topFragmentName ) ) {

						if ( fragmentManagerUtil.fragmentMap != null && fragmentManagerUtil.fragmentMap.containsKey( topFragmentName ) ) {

							MatchHomeFragment fragment = ( MatchHomeFragment ) fragmentManagerUtil.fragmentMap.get( topFragmentName );
							if ( fragment  != null ) {
								fragment.cancelRunnable ();
							}
						}

					}

					if(PlayUpActivity.handler != null){
						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {
								try {
									PlayupLiveApplication.getFragmentManagerUtil().setFragment( "PublicProfileFragment", bundle );
								} catch (Exception e) {
									// TODO Auto-generated catch block
									Logs.show(e);
								}

							}
						});



					}
				} catch (Exception e) {
					Logs.show ( e );
				}


			}
		}).start();


	}


	/**
	 * A dialog to show providers to share scores
	 */


	private void showShareDialog( boolean refresh ) {

		try {
			if ( PlayUpActivity.context != null && Constants.isCurrent ) {
				TextView dialogTitle;
				ListView providerList;
				RelativeLayout dialogRoot;
				final DatabaseUtil dbUtil = DatabaseUtil.getInstance();
				final Hashtable<String, Object> result = dbUtil.getContestShareUrl( vContestId );
				String vShareUrl = "";
				boolean isHref = false;
				if(result != null && result.containsKey("isHref")&&  result.containsKey("url")){
					vShareUrl = (String) result.get("url");
					isHref = ((Boolean) result.get("isHref")).booleanValue();
					
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
			Logs.show(e);
		}

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		try {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				event.getRawX();
				yPosition = event.getRawY();
				onclick = false;
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				if (!onclick && (event.getRawY() - yPosition) < 5
						&& (event.getRawY() - yPosition) > -5)
					onStripClick(v);
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				if (!onclick && (event.getRawY() - yPosition) > 0) {
					if (v.getId() == R.id.stripDown) {
						onStripClick(v);
						onclick = true;

					}
				} else if (!onclick && (event.getRawY() - yPosition) < 0) {
					if (v.getId() == R.id.stripUp) {
						onStripClick(v);
						onclick = true;
					}
				}
			}

	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
		return true;
	}

	/**
	 * click for animating ticket gallery
	 * @param v
	 */
	public void onStripClick(View v) {
		try {
			TranslateAnimation translateAnimation;
			if (v == null) {
				return;
			}
			switch (v.getId()) {

			case R.id.stripDown:
				isStripExpanded = true;
				if (publicMatchesTicketAdapter == null) {
					publicMatchesTicketAdapter = new PublicMatchesTicketAdapter( vContestId, matchGallery);
					matchGallery.setAdapter(publicMatchesTicketAdapter);
				} 

				
				matchGallery.setVisibility(View.VISIBLE);
				stripUp.setVisibility( View.VISIBLE );

				matchHeaderBottom.setVisibility(View.INVISIBLE);
				
				translateAnimation = new TranslateAnimation(
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, -1.0f,
						Animation.RELATIVE_TO_SELF, 0.0f);

				translateAnimation.setDuration(500);
				translateAnimation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						matchHeaderBottom.setVisibility(View.VISIBLE);
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						try {
							
							if(publicMatchesTicketAdapter != null)							
								matchGallery.setSelection(publicMatchesTicketAdapter.getSelectedPosition(), true);
						} catch (Exception e) {
							Logs.show(e);
						}

					}
				});

				matchHeaderBottom.startAnimation(	translateAnimation);



				break;

			case R.id.stripUp:
				isStripExpanded = false;
//				if( !expand ) {
//					commonStrip.setVisibility(View.VISIBLE);
//					matchHeaderBottom.setVisibility(View.GONE);
//					animateHeader();
//					break;
//				}
				
				
				translateAnimation = new TranslateAnimation(
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, 0.0f,
						Animation.RELATIVE_TO_SELF, -1.0f);

				translateAnimation.setDuration(500);

				translateAnimation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						// shadowDown.setVisibility(View.VISIBLE);
						
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onAnimationEnd(Animation animation) {
						try {
							commonStrip.setVisibility( View.GONE );
							matchHeaderBottom.setVisibility(View.GONE);
						} catch (Exception e) {
							Logs.show(e);
						}

					}
				});
				matchHeaderBottom.startAnimation(translateAnimation);

				break;
			}
		} catch (Exception e) {
			Logs.show(e);
		}

	}


	
	/**
	 * Trying to fetch the competition data from the server 
	 */
	private void getCompetitionData () {	//verified

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();

					if( vContestId == null ) {
						vContestId = dbUtil.getContestIdFromConversationId (vConversationId );
						if ( vContestId != null ) {
							showAssociatedContests();
						}
					} 
					
					
					getAssociatedData ();

					refreshMatches ();
				} catch (Exception e) {
					Logs.show ( e );
				}

			}
		}).start();

	}


	public void getAssociatedData () {	//VERIFIED

		try {
			Hashtable<String, Object> result = DatabaseUtil.getInstance().getAssociatedContestUrl( vContestId );

			if(result != null && result.containsKey("isHref")){
				
				String assoicatedContestUrl = (String) result.get("url");
				
				if((Boolean) result.get("isHref")){
					
					if ( assoicatedContestUrl != null && runnableList != null && !runnableList.containsKey( Constants.GET_ASSOCIATED_CONTEST_DATA ) && Util.isInternetAvailable() ) {
						runnableList.put(Constants.GET_ASSOCIATED_CONTEST_DATA, new Util().getAssoiciatedContestsData(assoicatedContestUrl,runnableList,true) ) ;
					}
					
				}else{
					
					
					
					if ( assoicatedContestUrl != null && runnableList != null && !runnableList.containsKey( Constants.GET_ASSOCIATED_CONTEST_DATA ) && Util.isInternetAvailable() ) {
						runnableList.put(Constants.GET_ASSOCIATED_CONTEST_DATA, new Util().getAssoiciatedContestsData(assoicatedContestUrl,runnableList,false) ) ;
					}
				}
				
				
				
			}
			
			
		} catch (Exception e) {
			Logs.show(e);
		}

	}


	/**
	 * refreshing the matches
	 */
	public void refreshMatches ( ) {	//verified


		try {

			if ( refreshMatchesTimer  != null) {
				refreshMatchesTimer.cancel();
				refreshMatchesTimer = null;
			}

			refreshMatchesTimer = new Timer ();

			if ( refreshMatchesTask != null ) {
				refreshMatchesTask.cancel();
				refreshMatchesTask = null;
			}

			new Thread( new Runnable() {

				@Override
				public void run() {

					// creating the timer task to get the contest data from the server
					refreshMatchesTask = new TimerTask() {

						@Override
						public void run() {

							try {

								getAssociatedData();
							} catch (Exception e) {
								Logs.show ( e );
							}
						}
					};

					try {
						String vAssoiciatedContestUrl = null;
						vAssoiciatedContestUrl = DatabaseUtil.getInstance().getAssociatedContestUrlForRefresh(vContestId);
						int cacheTime = Integer.parseInt( DatabaseUtil.getInstance().getCacheTime(vAssoiciatedContestUrl) );

						if (cacheTime > 0 ) {
							refreshMatchesTimer.schedule(refreshMatchesTask , cacheTime * 1000, cacheTime * 1000);
						} else {
							refreshMatchesTask = null;
						}

					} catch ( Exception e ) {
						Logs.show(e);
					}
				}
			}).start();
		} catch ( Exception e ) {
			Logs.show ( e );
		}

	}

	/**
	 * setting live favourites cout on strips
	 * @param show
	 */
	private void setLiveCount() {	//verified
		/**
		 * Date:18/07/2012
		 * Sprint:20
		 *  */
		try {
			Runnable r = new Runnable () {

				@Override
				public void run() {
					try {
						if( vContestId !=null  ) {

							final int live = PlayupLiveApplication.getDatabaseWrapper().getTotalCount("SELECT vContestId FROM   " +
									"associatedContestsData c  left join associatedContest ac on c.vContestId= ac.contestId "+
									" where ac.associatedContestId = ( select associatedContestId from contests where vContestId = '"+vContestId+"') AND ( c.dEndTime IS NULL AND c.dStartTime IS NOT NULL ) ");


							if ( PlayUpActivity.handler != null ) {
								PlayUpActivity.handler.post( new Runnable () {

									@Override
									public void run() {
										try {
											if ( !isVisible() ) {
												return;
											}

											if(liveCountDown!=null){
												liveCountDown.setVisibility(View.VISIBLE);
												liveCountDown.setText(live+"");
											}

											if(liveCountUp!=null){
												liveCountUp.setVisibility(View.VISIBLE);
												liveCountUp.setText(live+"");
											}
											
											if( stripLiveCount!=null ) {
												stripLiveCount.setVisibility(View.VISIBLE);
												stripLiveCount.setText(live+"");
											}
										} catch (Exception e) {
											// TODO Auto-generated catch block
											Logs.show(e);
										}

									}

								});
							}

						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Logs.show ( e );
					}
				}


			};
			Thread th = new Thread ( r );
			th.start();
		} catch (Exception e) {
			Logs.show ( e );
		}

	}
	

	/*
	 * this will set the dummy views height so that the red tickets and strip can alingn with respect to match header
	 */
	
	public void setDummyViewsHeight( String sportType ) {
		
	    header = (RelativeLayout) matchHeaderLayout.findViewById(R.id.header);
		LinearLayout dummyHeaderView = (LinearLayout) content_layout.findViewById(R.id.dummyHeaderView);
		int height = 0;
		
		if( Constants.DENSITY.equalsIgnoreCase("low") ) {
			height = 90;	
		} else if( Constants.DENSITY.equalsIgnoreCase("medium") ) {
			height = 100;	
		} else if( Constants.DENSITY.equalsIgnoreCase("high") ) {
			height = 170;	
		}

		if( PlayUpActivity.isXhdpi ) {
			height = 210;		
		}
		
		if(sportType!=null &&  sportType.equalsIgnoreCase(Constants.LEADERBOARD) ) {
			Hashtable<String, List<String>> leaderBoardData = DatabaseUtil.getInstance().getLeaderBoardData(  vContestId );
			if ( leaderBoardData != null && leaderBoardData.get ( "vTeamId" ) != null && leaderBoardData.get ( "vTeamId" ) .size ( ) > 2 ) {
				if( Constants.DENSITY.equalsIgnoreCase("low") ) {
					height = 110;
				} else if( Constants.DENSITY.equalsIgnoreCase("medium") ) {
					height = 130;
				} else if( Constants.DENSITY.equalsIgnoreCase("high") ) {
					height = 180;
				}

				if( PlayUpActivity.isXhdpi ) {
					height = 235;
				}  		
			}	
			
		} 
		
		    header.getLayoutParams().height = height;
		    header.requestFocus();
		    
		    params = (LayoutParams) dummyHeaderView.getLayoutParams();
		    params.height = height;
		    dummyHeaderView.setLayoutParams(params);
		    
	
		    // this part is for expanding leaderboard data
		
		if( sportType!= null && sportType.equalsIgnoreCase(Constants.LEADERBOARD ) && leaderBoardList!=null ) {
			
			leaderBoardList.setOnItemClickListener( leaderBoardListener );

		}
		
		
		
	}
	
	
	 public Animation expandTheView( final boolean shoudlExpand,final int height,final int galleryHeight,  final View v ) {
			
			Animation a = new Animation() {
				  @Override
				protected void applyTransformation(float interpolatedTime,Transformation t) {
						
			            if (shoudlExpand) {
			            	v.getLayoutParams().height = (int) ( height  + ( float) galleryHeight * interpolatedTime );
			            } else {
			            	v.getLayoutParams().height = (int) ( height  -  ( float) galleryHeight * (  interpolatedTime));
			            }
			            
			            if( interpolatedTime == 1.0 ) {
			            	animation_completed = true;
			            	setViewsAfterAnimation( shoudlExpand );
			            } else
			            	animation_completed = false;
			            v.requestLayout();
				    }
				  
				  @Override
				    public boolean willChangeBounds() {
				    	   return true;
				    }
		    };
			
		  
		    a.setDuration(100);
		    a.scaleCurrentDuration(5);
		    a.setInterpolator( new AccelerateInterpolator() );
			return a;
			
		}
		
	 
	 
	 public void setViewsAfterAnimation ( boolean isExpanded ) {
		
		 if( isExpanded ) {
			 isStripExpanded = true;
			 matchGallery.setVisibility(View.VISIBLE);
			 matchHeaderBottom.setVisibility( View.VISIBLE);
		 } else {
			
		 }
		 commonStrip.setVisibility( View.GONE );
	 }

		
	 
	OnItemClickListener leaderBoardListener = new OnItemClickListener() {

		@Override
		public void onItemClick( AdapterView<?> adapterView, View view, int position, long id) {
			//animateHeader();
		}
	};
	 
//	 public void animateHeader () {
//		 if( !animation_completed )
//			 return;
//		 
//		 if( expand ) {
//				expand = false;
//				animation_completed = false;
//				if( !isStripExpanded )
//					 commonStrip.setVisibility( View.VISIBLE );
//				header.startAnimation( expandTheView(true, header.getHeight(), matchGallery.getHeight(),  header));
//			} else {
//				expand = true;
//				animation_completed = false;						    
//				header.startAnimation( expandTheView(false, header.getHeight(),matchGallery.getHeight(),  header));
//			}
//	 }



}
