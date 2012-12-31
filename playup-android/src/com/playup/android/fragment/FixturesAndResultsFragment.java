package com.playup.android.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.util.DisplayMetrics;

import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.FixtureGallery;
import com.playup.android.adapters.GridAdapter;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

/**
 * Displays Rounds and Matches information.
 * Called when tap on a league's fixure/schedule tile
 */
public class FixturesAndResultsFragment extends MainFragment implements OnGestureListener{

	private Timer refreshMatchesTimer;
	private TimerTask refreshMatchesTask;
	
	private Timer refreshRoundsTimer;
	private TimerTask refreshRoundsTask;

	private RelativeLayout content_layout;
	private boolean isAgain = false;
	private String vCurrentSeasonUrl;
	private String vCompetitionId;
	private Hashtable<String, List<String>> weekData;
	private Gallery fixtureGallery;
	private FixtureGallery fixtureAdapter;
	private Hashtable<String, List<String>> contestData;
	private GridAdapter adapter;
	private ListView leagueBase;
	private String vRoundId;
	private LinearLayout progressLinear;
	private boolean isFetchingCompetitionData;
	private View lastSelected;
	private ArrayList<String> rounds;

	private GestureDetector detector;

	private RelativeLayout round_select_base;
	private TextView roundBaseTextView;

	private int roundSelected = -1;
	private int selectPosition = -1;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;

	private static final int REL_SWIPE_MIN_DISTANCE = 120;
	private static final int REL_SWIPE_MAX_OFF_PATH = 250;
	private static final int REL_SWIPE_THRESHOLD_VELOCITY = 200;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;
	private boolean vCurrentSeasonHrefUrl;
	
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		content_layout = (RelativeLayout) inflater.inflate( R.layout.currentleagueround, null); 
		if ( !isAgain  ) {
			setCompetitionId ( getArguments() );
		}
		return content_layout;
	}

	
	/**
	 * Getting the data sent from previous fragment
	 */
	private void setCompetitionId(Bundle bundle) {
		try{
		vCurrentSeasonUrl = null;
		vCompetitionId = null;
		vRoundId = null;
		vMainColor = null;
		vMainTitleColor = null;
		
		vSecColor = null;
		vSecTitleColor = null;
		
		
		if ( bundle != null && bundle.containsKey( "isHref" ) ) {
			vCurrentSeasonHrefUrl = bundle.getBoolean( "isHref" );
		}else{
			vCurrentSeasonHrefUrl = false;
		}
		if ( bundle != null && bundle.containsKey( "vCurrentSeasonUrl" ) ) {
			vCurrentSeasonUrl = bundle.getString( "vCurrentSeasonUrl" );
		} if ( bundle != null && bundle.containsKey( "vCompetitionId" ) ) {
			vCompetitionId = bundle.getString( "vCompetitionId" );
		}if ( bundle != null && bundle.containsKey( "vRoundId" ) ) {
			vRoundId = bundle.getString( "vRoundId" );
		}if (bundle != null &&bundle.containsKey("vMainColor")) {
			vMainColor = bundle.getString("vMainColor");
		}if (bundle != null && bundle.containsKey("vMainTitleColor")) {
			vMainTitleColor = bundle.getString("vMainTitleColor");
		}if (bundle != null && bundle.containsKey("vSecColor")) {
			vSecColor = bundle.getString("vSecColor");
		}if (bundle != null && bundle.containsKey("vSecTitleColor")) {
			vSecTitleColor = bundle.getString("vSecTitleColor");
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
		
	}

	
	
	@Override
	public void onAgainActivated(Bundle args) {

		super.onAgainActivated(args);
		isAgain = true;

		setCompetitionId ( args );

	}




	@Override
	public void onResume() {

		super.onResume();


		if ( content_layout == null ) {
			return;	
		}
		
		//intilizing and nullifying the objects 
		rounds = new ArrayList<String>();
		isFetchingCompetitionData = false;
		fixtureAdapter = null;
		adapter = null;
		weekData = null;
		contestData = null;
		
		getRoundData(false);
		initializeViews();
		fixtureGallery.setCallbackDuringFling(false);
		fixtureGallery.setOnItemSelectedListener(fixtureGalleryListener);
		
		fixtureGallery.setAnimationDuration(120);

		setValues();
	}


	/**
	 * intializing the views
	 */
	private void initializeViews(){	//verfied
		try{
		if ( content_layout == null ) {
			return;
		}
		fixtureGallery = (Gallery)content_layout.findViewById(R.id.fixtureGallery);
		leagueBase = ( ListView )content_layout.findViewById ( R.id.leagueBase );
		progressLinear	= (LinearLayout)content_layout.findViewById(R.id.progressLeagues);
		round_select_base = (RelativeLayout)content_layout.findViewById(R.id.round_select_base);
		roundBaseTextView = (TextView)content_layout.findViewById(R.id.weekNumber);

		/**
		 * setting the round gallery background based on the fragment its been called
		 */
		if(Constants.isGrayBar){
			content_layout.findViewById(R.id.user_profile_view).setBackgroundResource(R.drawable.grey_header);
			round_select_base.setBackgroundResource(R.drawable.round_select_p_base);
		}else{
			content_layout.findViewById(R.id.user_profile_view).setBackgroundResource(R.drawable.profile_green_base);
			round_select_base.setBackgroundResource(R.drawable.round_select_base);
		}

		DisplayMetrics dm = PlayUpActivity.context.getResources().getDisplayMetrics();
		//	        REL_SWIPE_MIN_DISTANCE = (int)(120.0f * dm.densityDpi / 160.0f + 0.5); 
		//	        REL_SWIPE_MAX_OFF_PATH = (int)(250.0f * dm.densityDpi / 160.0f + 0.5);
		//	        REL_SWIPE_THRESHOLD_VELOCITY = (int)(200.0f * dm.densityDpi / 160.0f + 0.5);


		detector = new GestureDetector(PlayUpActivity.context, this);

		OnTouchListener gestureListener = new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {


				if (detector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		};


		
		leagueBase.setClickable( true );
		leagueBase.setOnItemClickListener( listBaseListener );

		((TextView)round_select_base.findViewById(R.id.weekNumber)).setTypeface(Constants.OPEN_SANS_BOLD);
		((TextView)round_select_base.findViewById(R.id.period)).setTypeface(Constants.OPEN_SANS_REGULAR);
		((TextView)round_select_base.findViewById(R.id.weekStart)).setTypeface(Constants.OPEN_SANS_REGULAR);
		((TextView)round_select_base.findViewById(R.id.weekEnd)).setTypeface(Constants.OPEN_SANS_REGULAR);

		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
	}



	/**
	 * Refreshing the contest data for selected Round
	 */
	public void refreshMatches ( ) {	//verfied
		
		try{

		if ( refreshMatchesTimer  != null) {
			refreshMatchesTimer.cancel();
			refreshMatchesTimer = null;
		}

		refreshMatchesTimer = new Timer ();

		if ( refreshMatchesTask != null ) {
			refreshMatchesTask.cancel();
			refreshMatchesTask = null;
		}


		// creating the timer task to get the contest data from the server
		refreshMatchesTask = new TimerTask() {
			@Override
			public void run() {
				try {
					if ( vRoundId == null ||  ( vRoundId != null && vRoundId.trim().length() == 0 ) ) {
						return;
					}

					String vRoundContestId = DatabaseUtil.getInstance().getRoundContestId ( vRoundId );
					if ( vRoundContestId != null && runnableList != null && !runnableList.containsKey ( vRoundContestId )  && Util.isInternetAvailable()  ) {
						runnableList.put(vRoundContestId, new Util().getContestsDataForFixtures( vRoundContestId, vRoundId ,runnableList,true) ) ;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}
			}
		};
		
		// getting the cache time for the round
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		String vRoundUrl = dbUtil.getRoundUrlForRefresh( vRoundId );
		int cacheTime = Integer.parseInt( dbUtil.getCacheTime( vRoundUrl ) );
		
		
		if (cacheTime > 0 ) {
			refreshMatchesTimer.schedule(refreshMatchesTask , cacheTime * 1000, cacheTime * 1000);
		}else{
			refreshMatchesTask=null;
		}
		vRoundUrl = null;
		dbUtil = null;
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}

	}

	
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
	}


	@Override
	public void onStop() {
		super.onStop();

		lastSelected = null;

		// saving gallery's selected item position
		if( fixtureGallery!= null ) {
			roundSelected = fixtureGallery.getSelectedItemPosition();
		} else {
			roundSelected = -1;
		}

		fixtureGallery = null;
		fixtureAdapter = null;
		
		// cancelling timers and timertasks
		if ( refreshMatchesTimer != null ) {
			refreshMatchesTimer.cancel();
			refreshMatchesTimer = null;
		}
		if ( refreshMatchesTask != null ) {
			refreshMatchesTask.cancel();
			refreshMatchesTask = null;
		}

		if(rounds != null && rounds.size() > 0){
			rounds.clear();
		}


		if(refreshRoundsTask != null){			
			refreshRoundsTask.cancel();
			refreshRoundsTask = null;			
		}
		
		if(refreshRoundsTimer != null){			
			refreshRoundsTimer.cancel();
			refreshRoundsTimer = null;			
		}

	}

	
	/**
	 * Fetching data from database and setting necessary fields
	 */
	private void setValues(){	//verified
		try{
		try {
			// if network is not available show Toast and close progress dialog
			if( !Util.isInternetAvailable() && isVisible()) {
				progressLinear.setVisibility(View.GONE);
				PlayupLiveApplication.showToast( R.string.no_network );
			}

		} catch (Exception e) {
//			Logs.show(e);
		}


		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		
		// getting vCompetitionId using vCurrentSeasonUrl
		if(vCompetitionId == null || vCompetitionId.trim().length() == 0)
			vCompetitionId = dbUtil.getCompetitionId(vCurrentSeasonUrl);
		
		// getting vRoundId using vCompetitionId
		if(vCompetitionId != null && vCompetitionId.trim().length() > 0 && (vRoundId == null || vRoundId.trim().length() == 0))
			vRoundId = dbUtil.getCurrentRoundId(vCompetitionId);
		
		setTopbar();
		getContests();
		setHeaders();	
		setContestData();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}

	}


	/**
	 * setting rounds data for rounds gallery
	 */
	private void setHeaders(){		//verfied
		
		try{

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					weekData = dbUtil.getWeekData ( vCompetitionId );
					if(weekData != null && weekData.get("vRoundId") != null && weekData.get("vRoundId").size() > 0){
						final int selectedPosition = dbUtil.getWeekSelectedPosition ( vRoundId, weekData  );

						if(PlayUpActivity.handler != null){
							PlayUpActivity.handler.post(new Runnable() {
								@Override
								public void run() {

									try {
										
										if(fixtureAdapter != null){
											fixtureAdapter.setData(weekData);
										} else {
											fixtureAdapter = new FixtureGallery(weekData);
											fixtureGallery.setAdapter(fixtureAdapter);
										}

										// selecting the round using selectedPosition
										if (selectedPosition != -1) {
											if (roundSelected != -1) {
												int roundSize = 0;
												if (weekData != null&& weekData.get("vPeriod") != null) {
													roundSize = weekData.get("vPeriod").size();
												}
												if (roundSelected < roundSize)
													selectPosition = roundSelected;

												roundSelected = -1;												
												fixtureGallery.setSelection( selectPosition, true );
											} else
												fixtureGallery.setSelection( selectedPosition, true );
										}
										
									} catch ( Exception e ) {
//										Logs.show( e );
									}
								}
							});

						}

					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}


			}
		}).start();

		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}

	}
	
	
	/**
	 * refreshing rounds, based on vCurrentSeasonUrl cache time
	 */
	private void refreshRounds(){
		
		try{
		
		if(refreshRoundsTimer == null)
			refreshRoundsTimer = new Timer();
			
		if(refreshRoundsTask == null){
			refreshRoundsTask = new TimerTask() {			
				@Override
				public void run() {
					getRoundData(true);
				}
			};			
			
			int cacheTime = Integer.parseInt(DatabaseUtil.getInstance().getCacheTime(vCurrentSeasonUrl));
			
			if(cacheTime > 0){				
				refreshRoundsTimer.schedule(refreshRoundsTask, cacheTime * 1000,cacheTime * 1000);
			}else{
				refreshRoundsTask = null;
				refreshRoundsTimer = null;
			}
	
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
		
	}
	
	
	/**
	 * listener to identify click for contest list ( GridAdapter)
	 */
	OnItemClickListener listBaseListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> adapter, View v, int position,long id) {
			
			String vContestId  = null;
			
			if( contestData != null  && contestData.get("vContestId") != null ) 
				vContestId = contestData.get("vContestId").get(position);
			
			//Navigate to MatchRoomFragment for selected contest
			if( vContestId != null  ) {				
				String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
				Bundle bundle = new Bundle();
				bundle.putString("vContestId", vContestId);
				bundle.putString("vMainColor",vMainColor );
				bundle.putString("vMainTitleColor",vMainTitleColor );
				bundle.putString("fromFragment", topFragmentName);
				bundle.putString( "vSecColor",vSecColor );			
				bundle.putString( "vSecTitleColor",vSecTitleColor );
				
				PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchRoomFragment", bundle);
			}
			
		}
	};


	/**
	 * listener to identify selection of round gallery
	 */
	OnItemSelectedListener fixtureGalleryListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapter, View v,
				int position, long id) {

			if(v.getTag(R.id.about_txtview) != null){
				
				// getting selected round id
				vRoundId = v.getTag(R.id.about_txtview).toString();

				// cancellling prevously selected round's RefreshMatches 
				if ( refreshMatchesTimer != null ) {
					refreshMatchesTimer.cancel();
					refreshMatchesTimer = null;
				}
				if ( refreshMatchesTask != null ) {
					refreshMatchesTask.cancel();
					refreshMatchesTask = null;
				}
				
				if(PlayUpActivity.handler != null){
					PlayUpActivity.handler.postDelayed(new Runnable() {

						@Override
						public void run() {
							try {
								//calling round's contest API
								getContests();
								
								//Setting round's contest data
								setContestData();
								leagueBase.setSelection(0);
								
							} catch (Exception e) {
								// TODO Auto-generated catch block
//								Logs.show ( e );
							}

						}
					},700);

				}


			}
			
			
			//Setting Gallery's child views with default properties
			if(lastSelected != null ){

				lastSelected.setBackgroundResource(0);
				((TextView) lastSelected.findViewById(R.id.period)).setTextColor(Color.parseColor("#FFFFFF"));
				((TextView) lastSelected.findViewById(R.id.weekNumber)).setTextColor(Color.parseColor("#FFFFFF"));
				((TextView) lastSelected.findViewById(R.id.weekStart)).setTextColor(Color.parseColor("#FFFFFF"));
				((TextView) lastSelected.findViewById(R.id.weekEnd)).setTextColor(Color.parseColor("#FFFFFF"));

			}


			//Setting Gallery's child views properties based on screen type ( Gray/Green)
			if(Constants.isGrayBar){

				((TextView) v.findViewById(R.id.period)).setTextColor(Color.parseColor("#FFFFFF"));
				((TextView) v.findViewById(R.id.weekNumber)).setTextColor(Color.parseColor("#FFFFFF"));
				((TextView) v.findViewById(R.id.weekStart)).setTextColor(Color.parseColor("#FFFFFF"));
				((TextView) v.findViewById(R.id.weekEnd)).setTextColor(Color.parseColor("#FFFFFF"));

			}else{

				((TextView) v.findViewById(R.id.period)).setTextColor(Color.parseColor("#45FF63"));
				((TextView) v.findViewById(R.id.weekNumber)).setTextColor(Color.parseColor("#45FF63"));
				((TextView) v.findViewById(R.id.weekStart)).setTextColor(Color.parseColor("#45FF63"));
				((TextView) v.findViewById(R.id.weekEnd)).setTextColor(Color.parseColor("#45FF63"));
			}

			// setting the dummy text to adjust ther round base background
			try {
				setDummyText( ((TextView) v.findViewById(R.id.weekNumber)).getText().toString() );
			} catch (Exception e) {
				// TODO: handle exception
			}


			lastSelected = v;	
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};


	
	/**
	 * setting the dummy text to align the background image
	 * @param dummyText
	 */
	private void setDummyText( String dummyText) {
		roundBaseTextView.setText(dummyText);
	}



	/**
	 * setting topbar data
	 */
	private void setTopbar(){	//verfied

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();	
					String vChildColor = dbUtil.getSectionMainColor("", vCurrentSeasonUrl);
					String vChildTitleColor = dbUtil.getSectionMainTitleColor("", vCurrentSeasonUrl);
					
					if( vChildColor != null && vChildColor.trim().length() > 0 ){						
						vMainColor = vChildColor;	
					}
					
					if(vChildTitleColor != null && vChildTitleColor.trim().length() > 0) {											
						vMainTitleColor = vChildTitleColor;						
					}
					
					String vChildSecondaryColor = dbUtil.getSectionSecondaryColor ("", vCurrentSeasonUrl);
					String vChildSecondaryTitleColor = dbUtil.getSectionSecondaryTitleColor ("", vCurrentSeasonUrl);
										 
					 if(vChildSecondaryColor != null && vChildSecondaryColor.trim().length() > 0)
						 vSecColor = vChildSecondaryColor;
					 
					 if(vChildSecondaryTitleColor != null && vChildSecondaryTitleColor.trim().length() > 0)
						 vSecTitleColor = vChildSecondaryTitleColor;				
					
					String vCompetitionName = "";
					
					HashMap< String, String > map = new HashMap<String, String>();
		
					if(vCompetitionId != null && vCompetitionId.trim().length() > 0){

						Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery("SELECT vCompetitonName FROM competition WHERE vCompetitionId = '"+vCompetitionId+"'");
						if(c != null){

							if(c.getCount() > 0){

								c.moveToFirst();

								vCompetitionName = c.getString(c.getColumnIndex("vCompetitonName"));

								c.close();
							}
						}

					}
		

					map.put( "vCompetitionName",  vCompetitionName );
					Bundle b = new Bundle();
					b.putString("vMainColor",vMainColor );
					b.putString("vMainTitleColor",vMainTitleColor );
					Message msg = new Message ();
					msg.setData(b);
					msg.obj = map;
					PlayupLiveApplication.callUpdateTopBarFragments(msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}


			}
		}).start();

	}


	/**
	 * Calling round's contest API
	 */
	private void getContests(){	 //verified
		if(vRoundId != null && rounds != null && !rounds.contains(vRoundId)){
			isFetchingCompetitionData = true;
			if(rounds.size() > 0){
				rounds.add(vRoundId);
			}else{
				rounds.add(vRoundId);
				new Util().getRoundDataForFixtures( vRoundId ,rounds);
			}

		}
		
		//			String vRoundContestId = DatabaseUtil.getInstance().getRoundContestId ( vRoundId );
		//			if ( vRoundContestId != null && runnableList != null && !runnableList.containsKey(vRoundContestId) ) {
		//				runnableList.put(vRoundContestId, new Util().getContestsDataForFixtures( vRoundContestId, vRoundId ,runnableList) ) ;
		//			}
	}

	/**
	 * Setting round's contest data
	 */

	private void setContestData(){	//verified

		if( roundSelected != -1 )
			return;

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();

					// Show loading screen, if round id is not valid
					if ( vRoundId == null ||  ( vRoundId != null && vRoundId.trim().length() == 0 ) ) {

						if(PlayUpActivity.handler != null){

							PlayUpActivity.handler.post(new Runnable() {

								@Override
								public void run() {
									try {
										showProgressIndicator();
									} catch (Exception e) {
										// TODO Auto-generated catch block
//										Logs.show(e);
									}

								}
							});

						}

						return;
					}


		
					// Fetching contestData from Database
					if(vRoundId != null && vRoundId.trim().length() > 0)
						contestData = dbUtil.getContestsFromRoundId ( vRoundId );

					
					if(PlayUpActivity.handler != null){

						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {
								try {
									
									// if contestData is not valid show progress
									if( contestData == null || (contestData != null && contestData.get("vContestId") != null && contestData.get("vContestId").size() == 0)){
										
										if ( isFetchingCompetitionData ) {
											showProgressIndicator();
										} else{
											progressLinear.setVisibility(View.INVISIBLE);
										}

										return;
									}

									// if contestData is valid show contests, and remove progress
									dismissProgressIndicator ();
									if ( adapter == null ) {
										
										adapter = new GridAdapter( contestData, leagueBase );
										leagueBase.setAdapter( adapter );
									} else {
										adapter.setData( contestData );										
									}
									
								} catch (Exception e) {
									// TODO Auto-generated catch block
//									Logs.show(e);
								} 
							}
						});

					}

					dbUtil = null;
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}

			}
		}).start();

	}

	/**
	 * displaying loading screen
	 */
	private void showProgressIndicator(){


		if( !Util.isInternetAvailable() ) {
			dismissProgressIndicator();
			return;
		}

		if(progressLinear!=null&&!progressLinear.isShown()){
			progressLinear.setVisibility(View.VISIBLE);
			leagueBase.setVisibility(View.GONE);
		}
	}

	/**
	 * dismissing loading screen
	 */
	private void dismissProgressIndicator(){
		if(leagueBase!=null&&!leagueBase.isShown()){

			leagueBase.setVisibility(View.VISIBLE);
			progressLinear.setVisibility(View.GONE);
		}
	}

	
	private void getRoundData(boolean fromRefresh){  //verfified
		
		if(vCurrentSeasonUrl != null && vCurrentSeasonUrl.trim().length() > 0){

			if(runnableList!=null&&!runnableList.containsKey(Constants.GET_CURRENT_SEASON_DATA)  && Util.isInternetAvailable()  )

				runnableList.put(Constants. GET_CURRENT_SEASON_DATA,new Util().getCurrentSeasonData ( vCurrentSeasonUrl,runnableList,fromRefresh,vCurrentSeasonHrefUrl ) );
		}

	}


	@Override
	public void onUpdate(final Message msg) {

		super.onUpdate(msg);

		if(PlayUpActivity.handler != null){

			PlayUpActivity.handler.post(new Runnable() {

				@Override
				public void run() {
					try {
						if(!isVisible()) {
							return;
						}


						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "CalendarUpdate" ) ) {
							if(msg.arg1 == 1 )
								setValues();
							
							refreshRounds();
						}
						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "CalendarUpdateRefresh" ) ) {
							setHeaders();
						}
						if ( msg != null && msg.obj != null && 
								msg.obj.toString().equalsIgnoreCase("RoundDataForFixtures") && msg.getData() != null) {
							try{
								Bundle data = msg.getData();
								
								if(data != null){
								if(data.containsKey("RoundUpdate") ){

									if(rounds != null && rounds.size() > 0){
										new Util().getRoundDataForFixtures( rounds.get(0),rounds) ;
									}
								}
								
								if(data.containsKey("ContestsUpdate") && data.containsKey("vRoundId")&& data.getString("vRoundId").equalsIgnoreCase(vRoundId)){
							//	Logs.show(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> inside update roundId        "+adapter);
									isFetchingCompetitionData = false;
									setContestData();								
									refreshMatches ();
								}
								
								}

							}catch(Exception e){
//								Logs.show(e);
							}

						} 
						
						// chevron handling
						if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "callChevron" ) ) {
							String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
							PlayupLiveApplication.getFragmentManagerUtil().popBackStack(topFragmentName);
						}if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
							
							getRoundData(false);
							
						}

						
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
//						Logs.show ( e );
					}
				}
			});

		}


	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {

		//			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
		//				return false;
		//				else
		//			if (Math.abs(e1.getY() - e2.getY()) > REL_SWIPE_MAX_OFF_PATH) 
		//                return false; 



		//			else
		{
			try { 
				// right to left swipe
				if(e1.getX() - e2.getX() > REL_SWIPE_MIN_DISTANCE && 
						Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {

					int position = fixtureGallery.getSelectedItemPosition()+1;

					if(position > weekData.get("vRoundId").size())
						return true;
					fixtureGallery.setSelection(position,true);


				} else if (e2.getX() - e1.getX() > REL_SWIPE_MIN_DISTANCE && 
						Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) {

					int position = fixtureGallery.getSelectedItemPosition()-1;

					if(position < 0)
						return true;



					fixtureGallery.setSelection(position,true);

				} 
			} catch (Exception e) {
//				Logs.show(e);
			}
			return true;
		}
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2,
			float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}




}
