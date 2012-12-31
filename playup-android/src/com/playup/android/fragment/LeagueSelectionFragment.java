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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.LeagueAdapter;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;
/**
 * Called from all sports fragment,
 * Shows leagues of a selected sport
 */
public class LeagueSelectionFragment extends MainFragment implements OnItemSelectedListener {

	private ListView leaguesListView;
	private LeagueAdapter leagueAdapter;
	private LinearLayout progressLinear;

	

	private Timer refreshLeaguesTimer;
	private TimerTask refreshLeaguesTask;
	
	private boolean isAgain = false;
	private String vSportsId = null;

	private String fromFragment = null;

	private int selectedPosition = -1;

	private String searchString = "";
	
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;


	private RelativeLayout content_layout;
	private Hashtable< String , List < String > > leagueData  = null;
	private Hashtable<String, List<String>> data_for_search;
	private boolean inSearch = false ;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		content_layout = (RelativeLayout) inflater.inflate( R.layout.league_selection, null);

		if ( !isAgain ) {
			setSportsId ( getArguments() ); 
		}

		return content_layout;
	}

	
	@Override
	public void onResume () {
		super.onResume();

		if ( leagueData != null ) {
			leagueData.clear();
			leagueData = null;
		}


		LiveSportsFragment.isInLiveRoom=false;

		initialize( content_layout );
		setTopBar();

		leagueAdapter = null;
		
		inSearch = false;

		if ( searchString != null && searchString.trim().length() > 0 ) {
			filterText( searchString );
		} else {
			
			setValues();
		}

	}

	/**
	 * setting the sports id  
	 */
	private void setSportsId ( Bundle bundle ) {
		
		vMainColor = null;
		vMainTitleColor = null;
		
		vSecColor = null;
		 vSecTitleColor = null;

		if ( bundle != null && bundle.containsKey( "vSportsId") ) {
			vSportsId = bundle.getString( "vSportsId" );
		}if ( bundle != null && bundle.containsKey( "fromFragment") ) {
			fromFragment = bundle.getString( "fromFragment" );
		}
		if ( bundle != null && bundle.containsKey( "vCompetitionId") ) {
			bundle.getString( "vCompetitionId" );
		}
		if ( bundle != null && bundle.containsKey( "vRoundId") ) {
			bundle.getString( "vRoundId" );
		}

		if ( bundle != null && bundle.containsKey( "showLeagueHome" ) ) {
			bundle.getInt( "showLeagueHome" );
		} else {
		}
		if ( bundle != null && bundle.containsKey( "showLiveLeague" ) ) {
			bundle.getInt( "showLiveLeague" );
		} else {
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
	 * Hiding the keyboard if this fragment goes out of focus. 
	 */
	@Override
	public void onPause () {
		super.onPause();


		if ( leaguesListView != null ) {
			InputMethodManager inputManager = (InputMethodManager)PlayUpActivity.context.getSystemService( Context.INPUT_METHOD_SERVICE); 
			inputManager.hideSoftInputFromWindow( leaguesListView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			inputManager = null;
			selectedPosition = leaguesListView.getFirstVisiblePosition();
		}


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
					
					Hashtable<String, Object> result = dbUtil.getSportsCompetitionUrl(vSportsId);
					String sportsCompetitionUrl = (String) result.get("url");
				//	Boolean isHref = (Boolean) result.get("isHref");
					
					String vChildColor = dbUtil.getSectionMainColor("", sportsCompetitionUrl);
					String vChildTitleColor = dbUtil.getSectionMainTitleColor("", sportsCompetitionUrl);
					
					if(vChildColor != null && vChildColor.trim().length() > 0 )
						vMainColor = vChildColor;
					
					if(vChildTitleColor != null && vChildTitleColor.trim().length() > 0 )
						vMainTitleColor = vChildTitleColor;
					

					 String vChildSecondaryColor = dbUtil.getSectionSecondaryColor ( "", sportsCompetitionUrl );
					 String vChildSecondaryTitleColor = dbUtil.getSectionSecondaryTitleColor ( "", sportsCompetitionUrl );
					
					 
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
	 * handling data if any change in database has occured. 
	 */
	@Override 
	public void onUpdate ( final Message msg ) {


		try {
			// handling chevron
			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "callChevron" ) ) {
				
				PlayupLiveApplication.getFragmentManager().popBackStack();
				return;
			}

			
			//handling backbutton 
			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "handleBackButton" ) ) {

				PlayupLiveApplication.getFragmentManager().popBackStack();
				return;

			}if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
				
				getCompetitionData();
				
			}
			
			//handling search functionality ( from topbar )
			if ( msg != null && msg.getData() != null ) {

				Bundle bundle = msg.getData();
				if ( bundle.containsKey( "search_value") ) {
					inSearch  = true;					 
					filterText(msg.getData().get("search_value").toString() );
					return;
				}
			}



			if ( PlayUpActivity.handler != null ) {
				PlayUpActivity.handler.post( new Runnable () {

					@Override
					public void run() {

						try {
							if(msg != null && msg.obj != null ) {



								if ( isRemoving() || isDetached() || !isVisible()) {
									return;
								}


								if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "AllSportsResults" ) ) {

									setValues();
									return;
								}
								
								// called after leagueSelecionUrl API call
								if ( msg != null && msg.obj != null && 
										msg.obj.toString().equalsIgnoreCase( "LeagueSelectionFragment" ) ) {

									if(msg.arg1 == 1 && !inSearch){
										setTopBar();
										setValues();
									}									
																		
									refreshLeagues();		
									
									return;
								}
								
								if(!inSearch)
									setValues();
								
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
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


	/**
	 * refreshing leagues
	 */
	private void refreshLeagues() {
		
		try{
		if ( refreshLeaguesTimer == null ) {
			refreshLeaguesTimer = new Timer ();
		}

		if ( refreshLeaguesTask  == null) {

			refreshLeaguesTask = new TimerTask() {

				@Override
				public void run() {

					try {
						
						getCompetitionData();
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
//						Logs.show ( e );
					}
				}
			};
			
			
			
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			String vSportsCompetitionUrl = dbUtil.getLeagueUrlFromSportsIdForRefresh ( vSportsId );
			
			int cacheTime = Integer.parseInt( dbUtil.getCacheTime( vSportsCompetitionUrl ) );
	
			if (cacheTime > 0 ) {
				refreshLeaguesTimer.schedule(refreshLeaguesTask , cacheTime * 1000, cacheTime * 1000);
			}else{
				refreshLeaguesTask=null;
				refreshLeaguesTimer = null;
			}
			
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
	}



			@Override
			public void onStop() {
				// TODO Auto-generated method stub
				super.onStop();
				
				if(refreshLeaguesTask != null){
					
					refreshLeaguesTask.cancel();
					refreshLeaguesTask = null;
					
				}
				
				
				if(refreshLeaguesTimer != null){
					
					refreshLeaguesTimer.cancel();
					refreshLeaguesTimer = null;
					
				}	
				
			}



	@Override
	public void onAgainActivated(Bundle args) {

		isAgain = true;
		setSportsId ( args );

	}


	private void initialize(RelativeLayout content_layout) {
		initializeViews(content_layout);
		// get competition data 
		getCompetitionData ();
		setListeners () ;

		leagueAdapter = null;

	}

	
	/**
	 * Displaying progress view
	 */
	private void showProgressIndicator(){

		if(!isVisible())
			return;

		try {
			progressLinear.setVisibility(View.VISIBLE);
			leaguesListView.setVisibility(View.GONE);
		} catch ( Exception e ) {
//			Logs.show ( e );
		}
	}

	
	/**
	 * dismissing progress view
	 */
	private void dismissProgressIndicator(){


		leaguesListView.setVisibility(View.VISIBLE);
		progressLinear.setVisibility(View.GONE);
	}

	/**
	 * getting the competition data from the servere 
	 */
	private void getCompetitionData () {	//verfiried
		//showProgressIndicator();
		
		try{
		
		if(runnableList!=null && !runnableList.containsKey( vSportsId + "fetchLeagues" ) && Util.isInternetAvailable() ){
			
			runnableList.put(vSportsId + "fetchLeagues", new Util().getLeagues( vSportsId ,runnableList) );
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
	}

	
	/**
	 * setting listeners
	 */
	private void setListeners () {
		leaguesListView.setOnItemSelectedListener( this );
	}



	/**
	 * getting data from DB 
	 */
	private void setValues() {	//verified
		try{
		
		try{
			
			// if network is not available, show toast
		if( !Util.isInternetAvailable() && isVisible()) {
			
			progressLinear.setVisibility(View.GONE);
				PlayupLiveApplication.showToast( R.string.no_network );
		}

		} catch (Exception e) {
	
		}
	

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		//	Hashtable< String , List < String > > data = null;


		leagueData = dbUtil.getLeagues ( vSportsId );

		if( leagueData ==null || leagueData .size()==0){
			if ( leagueData != null ) {
			}
			if( Util.isInternetAvailable() )
				showProgressIndicator();
			else
				dismissProgressIndicator();
		} else {
			dismissProgressIndicator();

		}
		String vCompId = dbUtil.getRecentCompetitionId(vSportsId);
		int selectedPosition = dbUtil.getLeagueSelectedPosition(vCompId, leagueData ) ;

		vCompId = null;
		dbUtil = null;

		if ( leagueAdapter == null ) {

			leagueAdapter = new LeagueAdapter( leagueData, vSportsId, true, fromFragment, this, selectedPosition,
					vMainColor,vMainTitleColor,vSecColor,vSecTitleColor);
			leaguesListView.setAdapter(leagueAdapter);
		} else {
			leagueAdapter.setData( leagueData, vSportsId, true, fromFragment,selectedPosition,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor );
		}

		if ( selectedPosition != -1 ) {

			leaguesListView.setSelection( selectedPosition );
			selectedPosition = -1;
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}


	}

	/**
	 *  initialize views
	 * @param contentLayout
	 */
	private void initializeViews(RelativeLayout contentLayout) {	//verified

		leaguesListView = ( ListView ) contentLayout.findViewById ( R.id.leaguesListView );
		progressLinear	=	(LinearLayout) contentLayout.findViewById ( R.id.progressLeagues );

	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id ) {

		/*if ( adapterView != null ) {
			int count = adapterView.getCount();
			for ( int i = 0; i < count ; i++ ) {

				if ( i == position ) {
					if ( leagueAdapter != null ) {
						leagueAdapter.highLightBlueColor ( view, true );
					}
				} else {
					if ( leagueAdapter != null ) {
						leagueAdapter.highLightBlueColor ( view, false );
					}
				}
			}
		}	*/
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView ) {

	}
	/**
	 * Filtering Search items from Hashtable and send to adapter	
	 * @param s
	 * 
		 * Date:18/07/2012
		 * Sprint:20
		
	 */

	private void filterText(String s){	//verfied

		try {	
			
			if ( s == null ) {
				s = "";
			}
			searchString = s;
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			//LinearLayout v = (LinearLayout) PlayUpActivity.context.findViewById( R.id.main1);
			//v.removeAllViews();	
			if ( data_for_search != null ) {
				data_for_search.clear();
				data_for_search = null;
			}
			data_for_search = dbUtil.getLeagueNames( s, vSportsId );
			dbUtil = null;
			Animation anim1 = AnimationUtils.loadAnimation( PlayUpActivity.context, R.anim.enter );


			boolean showLiveNow = false;

			if ( s == null ||  s.trim().length() == 0 ) {
				showLiveNow = true;
			} 


			leagueAdapter = new LeagueAdapter( data_for_search, vSportsId, showLiveNow, fromFragment, this
					, selectedPosition,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor );
			
			if(leaguesListView!=null)
			{
				leaguesListView.setAdapter(leagueAdapter);
				anim1.setDuration( 300 );
				leaguesListView.startAnimation( anim1 );
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
		}


	


	}



}






