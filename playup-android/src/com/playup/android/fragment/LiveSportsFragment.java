package com.playup.android.fragment;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.MatchHeaderGenerator;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

/**
 * Shows all the live contests of a selected sport <br>
 * The contests/matches are grouped by region and league
 */
public class LiveSportsFragment extends MainFragment implements  OnClickListener {

	private LinearLayout leagueBase;


	
	private ImageView zeroGraphic;

	private String fromFragment = null; 

	private String vSportsId = null;
	private boolean isAgain = false;


	
	
	private String vMainColor = null;
	private String vMainTitleColor = null;


	private Timer refreshMatchesTimer;
	private TimerTask refreshMatchesTask;
	public static boolean isInLiveRoom = false;

	private ImageDownloader imageDownloader = new ImageDownloader();

	public android.widget.LinearLayout.LayoutParams headerParams = null;
	private Hashtable< String , List < String > > liveCompetitionData;
	Hashtable< String , List < String > > contestData ;
	

	private int iLiveNowCount;

	RelativeLayout content_layout ;

	ImageView bottomShadow;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;

	@Override
	public void onDestroy () {

		super.onDestroy();

		fromFragment = null; 
		vSportsId = null;

	}
	@Override
	public void onStop () {
		super.onStop();


		if ( liveCompetitionData != null ) {
			liveCompetitionData.clear();
			liveCompetitionData = null;
		}
		

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		content_layout = (RelativeLayout) inflater.inflate( R.layout.live_sports, null);

		if ( !isAgain ) {
			setSportsId ( getArguments() );
		}
		return content_layout;
	}


	/**
	 * refreshing the matches
	 */
	public void refreshMatches ( ) {
		
		try{

		if ( refreshMatchesTimer != null ) {
			refreshMatchesTimer.cancel();
			refreshMatchesTimer = null;
		}
		refreshMatchesTimer = new Timer ();

		if ( refreshMatchesTask != null ) {
			refreshMatchesTask.cancel();
			refreshMatchesTask = null;
		}

		refreshMatchesTask = new TimerTask() {

			@Override
			public void run() {


				try {
					if(runnableList!=null&&!runnableList.containsKey(Constants.GET_LIVE_SPORTS)){


						if ( LiveSportsFragment.isInLiveRoom  && Util.isInternetAvailable()) {


							runnableList.put(Constants.GET_LIVE_SPORTS, new Util().getLiveSports ( vSportsId,runnableList ));
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}
			}
		};


		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		String vSportsLiveUrl = dbUtil.getSportsLiveUrlForRefresh ( vSportsId );


		int cacheTime = Integer.parseInt( dbUtil.getCacheTime(  vSportsLiveUrl ) );
		dbUtil = null;
		vSportsLiveUrl = null;

		if (cacheTime > 0 ) {
			refreshMatchesTimer.schedule(refreshMatchesTask , cacheTime * 1000, cacheTime * 1000);
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
	}


	@Override
	public void onResume () {
		super.onResume();

		if ( imageDownloader == null ) {
			imageDownloader =  new ImageDownloader();
		}
		initialize ( content_layout );

		isInLiveRoom = true;
		iLiveNowCount = 0;
		refreshMatches();
	}

	@Override
	public void onPause () {
		super.onPause();
		isInLiveRoom = false;

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
	public void onAgainActivated(Bundle args) {

		isAgain = true;
		setSportsId ( args );

	}

	/**
	 * setting the competition id  
	 */
	private void setSportsId ( Bundle bundle ) {

		vMainColor = null;
		vMainTitleColor = null;
		
		vSecColor = null;
		 vSecTitleColor = null;
		
		if ( bundle != null && bundle.containsKey( "vSportsId" ) ) {
			vSportsId = bundle.getString( "vSportsId" );
		}
		if ( bundle != null && bundle.containsKey( "fromFragment" ) ) {
			fromFragment = bundle.getString( "fromFragment" );
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


	private void initialize(RelativeLayout content_layout) {
		try{
		setTopBar ();
		initializeViews(content_layout);
		// get live data 
		getLiveSports ();
		// set listeners
		setListeners();
		// set values
		setValues();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}

	}

	/**
	 * getting LiveSports
	 */

	private void getLiveSports () {	//verified
		
		try{
		if(runnableList!=null&&!runnableList.containsKey(Constants.GET_LIVE_SPORTS)){

			runnableList.put(Constants.GET_LIVE_SPORTS, new Util().getLiveSports ( vSportsId,runnableList ) );
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}

	}

	/**
	 *  initialize views
	 * @param content_layout
	 */
	private void initializeViews(RelativeLayout content_layout) {	//verified

		if ( content_layout == null ) {
			return;
		}
		bottomShadow = (ImageView) content_layout.findViewById( R.id.bottomShadow );
		bottomShadow.setVisibility(View.GONE);

		leagueBase=(LinearLayout)content_layout.findViewById(R.id.leagueBase);
		zeroGraphic=(ImageView)content_layout.findViewById(R.id.zeroGraphic);


	}

	/**
	 * Setting Listeners
	 */
	private void setListeners() {

	}


	/**
	 *  setting the room in the top bar fragment.
	 **/
	private void setTopBar () {	//verified

		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String vSportsLiveUrl = dbUtil.getSportsLiveUrlForRefresh ( vSportsId );
					

					
					String vChildColor = dbUtil.getSectionMainColor("", vSportsLiveUrl);
					String vChildTitleColor = dbUtil.getSectionMainTitleColor("", vSportsLiveUrl);
					
					if(vChildColor != null && vChildColor.trim().length() > 0 )
						vMainColor = vChildColor;
					
					if(vChildTitleColor != null && vChildTitleColor.trim().length() > 0 )
						vMainTitleColor = vChildTitleColor;
					
					 String vChildSecondaryColor = dbUtil.getSectionSecondaryColor ( "", vSportsLiveUrl );
					 String vChildSecondaryTitleColor = dbUtil.getSectionSecondaryTitleColor ( "", vSportsLiveUrl );
					
					 
					 if(vChildSecondaryColor != null && vChildSecondaryColor.trim().length() > 0)
						 vSecColor = vChildSecondaryColor;
					 
					 if(vChildSecondaryTitleColor != null && vChildSecondaryTitleColor.trim().length() > 0)
						 vSecTitleColor = vChildSecondaryTitleColor;
					 
					
					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT vName FROM sports WHERE vSportsId = \"" + vSportsId + "\"  ");
					if ( c != null ) {

						if ( c.getCount() > 0 ) {

							c.moveToFirst();

							HashMap< String, String > map = new HashMap<String, String>();
							map.put( "vSportsName", c.getString( c.getColumnIndex( "vName" ) ) );
							Bundle b = new Bundle();
							b.putString("vMainColor",vMainColor );
							b.putString("vMainTitleColor",vMainTitleColor );
							Message msg = new Message ();
							msg.setData(b);
							msg.obj = map;
							PlayupLiveApplication.callUpdateOnFragments(msg);
						}
						c.close();
						c = null;
					}
				} catch (Exception e) {
//					Logs.show(e);
				}
				
			}
		}).start();
		
		
	}

	/**
	 * Setting Values
	 */
	private void setValues() {

		//		setLiveAndDate();
		setMatches();
	}



//	private String getSportName()
//	{
//		Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT vName  FROM sports WHERE vSportsId = \"" + vSportsId + "\" ");
//		try{
//			if ( c.getCount() > 0 ) {
//				c.moveToFirst();
//				String vName =	c.getString( c.getColumnIndex( "vName" ));
//				if(c!=null&&!c.isClosed())
//					c.close();
//				return vName;
//			}
//		}finally{
//			if(c!=null&&!c.isClosed())
//				c.close();
//
//			c = null;
//		}
//		return null;
//	}
	/**
	 * showing live matches 
	 */
	private void setMatches () {	//verified
		try{
		setLiveAndDate();

		iLiveNowCount = 0;
		
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();

		leagueBase.removeAllViews();

		
		LayoutInflater inflater = LayoutInflater.from(PlayUpActivity.context);

		if ( liveCompetitionData != null ) {
			liveCompetitionData.clear();
			liveCompetitionData = null;
		}
		liveCompetitionData = dbUtil.getSportsLiveCompetition ( vSportsId );
		int count = 0;

		if ( liveCompetitionData != null && liveCompetitionData.get( "vCompetitionLiveId" ).size() > 0 ) {
			count = liveCompetitionData.get( "vCompetitionLiveId" ).size();
		}

		if( count > 0)
			bottomShadow.setVisibility(View.VISIBLE);
		else
			bottomShadow.setVisibility(View.GONE);

		// Creates leagues no. of blocks of matches
		for ( int i = 0; i < count ; i++ ) {

			if ( liveCompetitionData.get( "vCompetitionLiveId" ).get( i ) == null) {
				return;
			}


			String vCompetitionName = dbUtil.getCompetitionLiveName ( liveCompetitionData.get( "vCompetitionLiveId" ).get( i ) );
			String vRegion = dbUtil.getCompetitionLiveRegion( liveCompetitionData.get( "vCompetitionLiveId").get( i ) );

			//adding header (League name , Country name) to the view
			RelativeLayout leagueHeader  = (RelativeLayout)inflater.inflate(R.layout.league_header, null);

			TextView leagueName = (TextView) leagueHeader.findViewById(R.id.leagueName);
			TextView countryName = (TextView) leagueHeader.findViewById(R.id.countryName );

			leagueName.setText ( vCompetitionName );
			countryName.setText( vRegion );

			leagueName.setTypeface(Constants.OPEN_SANS_REGULAR);
			countryName.setTypeface(Constants.OPEN_SANS_SEMIBOLD);

			leagueBase.addView(leagueHeader);

			contestData  = dbUtil.getSportsLiveContest ( liveCompetitionData.get( "vCompetitionLiveId").get( i ) );
			//adding matches to the league
			if ( contestData == null ) {
				return;
			}
			if ( contestData != null && contestData.get( "vContestId" ) == null ) {
				return ;
			}
			int len = contestData.get ( "vContestId").size();
			iLiveNowCount = contestData.get ( "vContestId").size();



			for (int j = 0; j < len; j++ )  {
				String sportType = null;
				if( contestData!= null && contestData.get("vSportType")!=null )
					sportType = contestData.get("vSportType").get( j );
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
				leagueBase.addView( headerLayout );
				headerLayout.setTag(R.id.about_txtview,contestData.get("vContestId").get(j));
				headerLayout.setOnClickListener(this);
				if( j< (len-1))
					setMatchItem( headerLayout, j, contestData, true);
				else
					setMatchItem( headerLayout, j,contestData, false);
			}

			vCompetitionName = null;
			vRegion = null;
			leagueHeader = null;
			leagueName = null;
			countryName = null;

			if ( contestData != null ) {
				contestData.clear();
				contestData = null;
			}


		}

		dbUtil = null;
		
		inflater = null;
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
	}


	/**
	 * Setting live and date data. 
	 */	private void setLiveAndDate () {	//verified

		 try {

			
			 DatabaseUtil dbUtil = DatabaseUtil.getInstance();

			 iLiveNowCount = dbUtil.getLiveMatches( vSportsId );


			
			 dbUtil = null;

			 // handling UI for zero live matches


			 if ( iLiveNowCount > 0 ) {

				 leagueBase.setVisibility( View.VISIBLE );
				 zeroGraphic.setVisibility( View.GONE );
			 } else {

				 leagueBase.setVisibility( View.GONE );
				 zeroGraphic.setVisibility( View.VISIBLE );


			 }
		 } catch ( Exception e ) {
//			 Logs.show( e );
		 }

	 }



	 @Override 
	 public void onUpdate ( final Message msg ) {


		 try {
			if ( msg != null && msg.obj != null ) {

				// handling backbutton pressed state
				 if ( msg.obj.toString().equalsIgnoreCase( "handleBackButton" ) ) {

					 if ( fromFragment == null || fromFragment.contains("LeagueSelectionFragment")) {
						 String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();

						 cancelRunnable();
						 PlayupLiveApplication.getFragmentManagerUtil().popBackStack(topFragmentName);
						 return;

					 } else if ( fromFragment.equalsIgnoreCase( "MatchRoomFragment" ) ) {

						 boolean doesExists = PlayupLiveApplication.getFragmentManagerUtil().checkIfFragmentExists( "LeagueSelectionFragment" );
						 if ( doesExists ) {

							 PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( "LeagueSelectionFragment" );


						 } else {

							 Bundle bundle = new Bundle ();
							 bundle.putString( "vSportsId", vSportsId );
							 bundle.putInt ( "showLiveLeague", 1 );

							 PlayupLiveApplication.getFragmentManagerUtil().setFragment( "LeagueSelectionFragment", bundle );

						 }
						 cancelRunnable();

					 } else if ( fromFragment.equalsIgnoreCase( "MyProfileFragment" ) ) {

						 cancelRunnable();
						 PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( "MyProfileFragment" );
					 }
					 return;
				 }
				 if ( msg.obj.toString().equalsIgnoreCase( "callChevron" ) ) {

					 String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
					 cancelRunnable();
					 PlayupLiveApplication.getFragmentManagerUtil().popBackStack(topFragmentName);
					 return;
				 }if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
						
					 getLiveSports();
						
						
					}
			 }

			 

			 if ( PlayUpActivity.handler != null ) {
				 PlayUpActivity.handler.post( new Runnable () {

					 @Override
					 public void run() {

						 try {
							if ( !isVisible() ) {
								 return;
							 }
							 if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "AllSportsResults" ) ) {

								 setValues();
								 return;
							 }
							 
							 if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "getLiveSports" ) ) {
								 setTopBar();
								 setValues();
								 refreshMatches();
							 }
						} catch (Exception e) {
							
//							Logs.show ( e );
							
						}

					 }
				 });
			 }
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
	 }





	 @Override
	 public void onClick ( View v ) {
		 Bundle bundle;
		 switch ( v.getId() ) {
		
		 default:
			 String vContestId = v.getTag( R.id.about_txtview ).toString();
			 if( vContestId!= null ) {
				

				 
				 String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
				 bundle = new Bundle();
				 bundle.putString("vContestId", vContestId);
				 bundle.putString("vMainColor",vMainColor );
				 bundle.putString("vMainTitleColor",vMainTitleColor );
				 bundle.putString( "vSecColor",vSecColor );									
				 bundle.putString( "vSecTitleColor",vSecTitleColor );
				 bundle.putString("fromFragment", topFragmentName);

				 cancelRunnable();

				 PlayupLiveApplication.getFragmentManagerUtil().setFragment( "MatchRoomFragment", bundle, R.id.main);
			 }

			 break;
		 }
	 }


	 /**
	  * setting the match item view
	  * @param lin
	  * @param position
	  */
	 private void setMatchItem( View header, int position,  Hashtable<String, List<String>> data, boolean showDivider ) {
		 try {

			 new MatchHeaderGenerator(data, header, position, true);
			 ImageView fixtureDivider = (ImageView) header.findViewById(R.id.fixtureDivider);
			 if( showDivider )
				 fixtureDivider.setVisibility(View.VISIBLE);
			 else
				 fixtureDivider.setVisibility(View.GONE);
			 
		 } catch (Exception e) {
			// TODO: handle exception
		}

	 }



}
