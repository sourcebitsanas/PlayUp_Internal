package com.playup.android.adapters;

import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import android.content.res.Resources.NotFoundException;
import android.graphics.Color;


import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;

import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;


public class MatchHeaderGenerator {
	
	//Object declaration
	private Random randomGenerator = new Random();
	private DatabaseUtil dbUtil = null;
	public LeaderBoardAdapter leaderBoardAdapter = null;
	public android.view.ViewGroup.LayoutParams headerParams = null;
	private ImageDownloader imageDownloader = null;
	private LayoutInflater inflater = LayoutInflater.from( PlayUpActivity.context ) ;
	
	//Views declaration
	private View matchHeaderBase;
	public TextView matchTime;
	public TextView team1Score;
	public TextView team2Score;
	public TextView team1Name;
	public TextView team2Name;
	public TextView liveText;
	public TextView matchSummary;
	public LinearLayout cricketSummary;
	public LinearLayout baseballSummary;
	public ImageView icon1,icon2;
	public TextView player1,player2,subplayer1,subplayer2;
	private TextView upcomingText;
	private TextView matchStartingTime;
	private LinearLayout lastEventView;
	private TextView lastEventName;
	private TextView lastEventDesc;
	private ListView leaderBoardList;
	private LinearLayout events_score_base;
	private LinearLayout setView;
	private TextView name1 ;
	private TextView name2 ;
	private ImageView status1 ;
	private ImageView status2 ;
	private ImageView teamFlag1;
	private ImageView teamFlag2; 
	private ImageView sportImage;
	private RelativeLayout header;
	private LinearLayout leaderBoradView;
	
	
	//Datatype values declaration
	private String contestBackground = null;
	private Hashtable<String, List<String>> data = null;
	public int position = 0;
	private boolean fromAdapter = false;
	private String homeTeamName = null;
	private String awayTeamName = null;
	private String sportType = null;
	private String vContestId = null;
	private boolean isUpcomingMatch = false;
	private boolean isLiveMatch = false;
	private String output = null;
	boolean fromPublicRoom = false;
	

	/**
	 * This constructor is called from matchroom, matchhome, privatelobby and privatelobbyroom fragments
	 */
	public MatchHeaderGenerator( Hashtable<String, List<String>> headerData,View matchHeaderBase,  boolean fromAdapter ,  boolean fromPublicRoom  ) {
		
		
		inflater = LayoutInflater.from( PlayUpActivity.context );	
		this.fromAdapter = fromAdapter;
		this.fromPublicRoom = fromPublicRoom;
		this.data = headerData;
		this.position = 0;
		dbUtil = DatabaseUtil.getInstance();
		this.matchHeaderBase = matchHeaderBase;
		imageDownloader = new ImageDownloader();
		leaderBoardAdapter = null;
		initializeViews();

	}

	/**
	 * this constructor getting called from headerGalleryAdapter
	 */
	public MatchHeaderGenerator(Hashtable<String, List<String>> headerData,View matchHeaderBase, boolean fromAdapter ) {
		
		
		
		inflater = LayoutInflater.from( PlayUpActivity.context );	
		this.fromAdapter = fromAdapter;
		this.fromPublicRoom = false;
		this.data = headerData;
		this.position = 0;
		dbUtil = DatabaseUtil.getInstance();
		this.matchHeaderBase = matchHeaderBase;
		imageDownloader = new ImageDownloader();
		leaderBoardAdapter = null;
		initializeViews();
	}


	/**
	 * this constructor getting called from livesports fragment and gridAdapter
	 */
	public MatchHeaderGenerator(Hashtable<String, List<String>> headerData,View matchHeaderBase, int position,boolean fromAdapter ) {

		inflater = LayoutInflater.from( PlayUpActivity.context );	
		this.fromAdapter = fromAdapter;
		this.fromPublicRoom = false;
		this.data = headerData;
		this.position = position;
		dbUtil = DatabaseUtil.getInstance();
		this.matchHeaderBase = matchHeaderBase;
		imageDownloader = new ImageDownloader();
		leaderBoardAdapter = null;
		initializeViews();
	}


	/**
	 * setting data
	 */
	public void setData( Hashtable<String, List<String>> headerData ) {		
		this.data = headerData;
		this.position = 0;
		setHeaderData();
	}

	/**
	 * getting sport type of the contest
	 * @return	Sport type: cricket,baseball,leaderboard,setbased etc..
	 */
	public String getSportType() {
		if( data!=null && data.get( "vSportType" )!=null )
			return data.get( "vSportType" ).get( position );	
		else
			return null;
	}

	/**
	 * initializing views of the passed header
	 */
	private void initializeViews() {
		try {
			if( matchHeaderBase == null ) 
				return;
			

			// common views for all the sports
			header = (RelativeLayout) matchHeaderBase.findViewById(R.id.header);
			sportImage = (ImageView) matchHeaderBase.findViewById(R.id.sportImage);	
			matchTime = (TextView) matchHeaderBase.findViewById(R.id.matchTime);
			liveText  = (TextView) matchHeaderBase.findViewById(R.id.liveText);
			matchSummary = (TextView) matchHeaderBase.findViewById(R.id.matchSummary);
			matchStartingTime =(TextView) matchHeaderBase.findViewById(R.id.matchStartingTime);
			upcomingText =(TextView) matchHeaderBase.findViewById(R.id.upcomingText);

			sportType = getSportType();	
			
			

			// initializing views based on sport type
			if(sportType != null && sportType.equalsIgnoreCase(Constants.LEADERBOARD) ) {
				
				
				leaderBoradView = (LinearLayout) matchHeaderBase.findViewById(R.id.leaderBoradView);
				leaderBoardList = (ListView) matchHeaderBase.findViewById(R.id.leaderBoardList);
				events_score_base= (LinearLayout) matchHeaderBase.findViewById(R.id.events_score_base);	
			} else if( sportType!=null && sportType.equalsIgnoreCase(Constants.SET_BASED_DATA)  ) {

				setView = (LinearLayout) matchHeaderBase.findViewById(R.id.pointsView);	
				name1 = (TextView) matchHeaderBase.findViewById(R.id.teamName1);
				name2 = (TextView) matchHeaderBase.findViewById(R.id.teamName2);
				
				status1 = (ImageView) matchHeaderBase.findViewById(R.id.status1);
				status2 = (ImageView) matchHeaderBase.findViewById(R.id.status2);
				teamFlag1 = (ImageView) matchHeaderBase.findViewById(R.id.teamFlag1);
				teamFlag2 = (ImageView) matchHeaderBase.findViewById(R.id.teamFlag2);
				
				name1.setTypeface(Constants.BEBAS_NEUE);
				name2.setTypeface(Constants.BEBAS_NEUE);
				

			} else {
				team1Score = (TextView) matchHeaderBase.findViewById(R.id.team1Score);
				team2Score = (TextView) matchHeaderBase.findViewById(R.id.team2Score);
				team1Name = (TextView) matchHeaderBase.findViewById(R.id.team1Name);
				team2Name = (TextView) matchHeaderBase.findViewById(R.id.team2Name);		
				cricketSummary = (LinearLayout) matchHeaderBase.findViewById(R.id.cricketSummary);
				baseballSummary = (LinearLayout) matchHeaderBase.findViewById(R.id.baseballSummary);	
				icon1 = (ImageView) matchHeaderBase.findViewById(R.id.icon1);
				icon2 = (ImageView) matchHeaderBase.findViewById(R.id.icon2);
				player1 = (TextView) matchHeaderBase.findViewById(R.id.player1);		
				player2 = (TextView) matchHeaderBase.findViewById(R.id.player2);		
				subplayer1 = (TextView) matchHeaderBase.findViewById(R.id.subplayer1);		
				subplayer2 = (TextView) matchHeaderBase.findViewById(R.id.subplayer2);
				lastEventView = (LinearLayout) matchHeaderBase.findViewById(R.id.lastEventView);
				lastEventDesc = (TextView) matchHeaderBase.findViewById(R.id.lastEventDesc);
				lastEventName = (TextView) matchHeaderBase.findViewById(R.id.lastEventName);
				
				player1.setTypeface(Constants.OPEN_SANS_REGULAR);
				player2.setTypeface(Constants.OPEN_SANS_REGULAR);
				subplayer1.setTypeface(Constants.OPEN_SANS_REGULAR);
				subplayer2.setTypeface(Constants.OPEN_SANS_REGULAR);
				team1Name.setTypeface(Constants.BEBAS_NEUE);
				team2Name.setTypeface(Constants.BEBAS_NEUE);
				team1Score.setTypeface(Constants.BEBAS_NEUE);
				team2Score.setTypeface(Constants.BEBAS_NEUE);
				lastEventName.setTypeface(Constants.OPEN_SANS_REGULAR);
				lastEventDesc.setTypeface(Constants.OPEN_SANS_REGULAR);
			}
			
				upcomingText.setTypeface( Constants.OPEN_SANS_REGULAR );
				matchStartingTime.setTypeface( Constants.BEBAS_NEUE );
				matchSummary.setTypeface(Constants.OPEN_SANS_REGULAR);
				matchTime.setTypeface( Constants.OPEN_SANS_REGULAR );
				
			//setting the header height based on device density
				
			if( !fromPublicRoom ) {
				headerParams = (android.view.ViewGroup.LayoutParams) header.getLayoutParams();
				if( Constants.DENSITY.equalsIgnoreCase("low") ) {
					headerParams.height = 90;	
				} else if( Constants.DENSITY.equalsIgnoreCase("medium") ) {
					headerParams.height = 100;	
				} else if( Constants.DENSITY.equalsIgnoreCase("high") ) {
					headerParams.height = 170;	
				}

				if( PlayUpActivity.isXhdpi ) {
					headerParams.height = 210;		
				}
				header.setLayoutParams( headerParams );
			}
				setHeaderData();
		} catch (Exception e) {
			Logs.show(e);
		}	
	}

	
	
	/**
	 * setting the contest data on the header
	 */
	private void setHeaderData() {
		try {
			
			
			if ( data != null && data.get( "vContestId" ) != null  && data.get( "vContestId" ).size() > 0 ) {

				String output_data = null;
				boolean matchTimingsAvailable = false;
				try {

					if( (data.get( "dStartTime" ).get( position) != null &&  data.get( "dStartTime" ).get( position).trim().length() > 0) ||  
							(data.get( "dEndTime" ).get( position) != null &&   data.get( "dEndTime" ).get( position).trim().length() > 0) || 
							(data.get( "dScheduledStartTime" ).get( position) != null  &&  data.get( "dScheduledStartTime" ).get( position).trim().length() > 0)) {
						matchTimingsAvailable = true;

					}

					output_data = new DateUtil().Match_TimeRoomFragment( data.get( "dStartTime" ).get( position), data.get( "dEndTime" ).get( position ), data.get( "dScheduledStartTime" ).get( position ) );


				} catch ( Exception e ) {
					Logs.show ( e );
				}
			   
				if( output_data != null )
					output = output_data.trim();
				else
					output = output_data;
				
				if( output!=null &&  output.length() > 0 && !output.equalsIgnoreCase("Completed")  ) {
					isLiveMatch = false;
					isUpcomingMatch = true;
				} else if( output == null || output.length() == 0 ) {
					isLiveMatch = true;
					isUpcomingMatch = false;
				} else {
					isLiveMatch = false;
					isUpcomingMatch = false;
				}
							
				
				sportType =data.get( "vSportType" ).get( position );	
				vContestId =data.get( "vContestId" ).get( position );

				//removing the visibility of the views
				
				upcomingText.setVisibility(View.GONE);
				matchStartingTime.setVisibility(View.GONE);

				
				//Setting the match summary/ scheduled time on the header
				if( matchTimingsAvailable ) {
					if ( isLiveMatch ) {
						liveText.setVisibility(View.VISIBLE);
						matchSummary.setVisibility(View.VISIBLE);
						matchTime.setVisibility(View.GONE);
						matchSummary.setText( data.get( "vSummary" ).get( position ) );
					} else {
						liveText.setVisibility(View.GONE);
						matchSummary.setVisibility(View.GONE);
						matchTime.setVisibility(View.VISIBLE);
						if( !isUpcomingMatch ) {
							matchTime.setText( R.string.completed);
							matchTime.setTextColor(Color.parseColor("#696B6E"));
						}
					} 
				}


				//setting the background image of the contest based on type
				
				setContestBackground();
				
				//setting the header data based on sport type
				if( sportType!=null ) {
					if( sportType.equalsIgnoreCase(Constants.LEADERBOARD)){							
						setLeaderBoardData();
						
					} else if (sportType.equalsIgnoreCase(Constants.SET_BASED_DATA)) {
						setSetBasedData();
					
					} else {
						icon1.setImageBitmap(null);
						icon2.setImageBitmap(null);
						icon1.setVisibility(View.INVISIBLE);
						icon2.setVisibility(View.INVISIBLE);
						player1.setText("");
						player2.setText("");
						subplayer1.setText("");
						subplayer2.setText("");
						subplayer1.setVisibility(View.GONE);
						subplayer2.setVisibility(View.GONE);
						lastEventView.setVisibility(View.GONE);
						
						baseballSummary.setVisibility(View.GONE);
						cricketSummary.setVisibility( View.GONE );
						
						team1Name.setText( data.get( "vHomeDisplayName") .get( position )  );
						team2Name.setText( data.get( "vAwayDisplayName") .get( position )  );
						
						if( !isUpcomingMatch ) {
							team1Score.setText( data.get( "vSummary1") .get( position ));
							team2Score.setText( data.get( "vSummary2") .get( position ));
						}		
						
						if (sportType.equalsIgnoreCase(Constants.AFL))  {
							if( !isUpcomingMatch ) {
								team1Score.setText( data.get( "iTotal1") .get( position ));
								team2Score.setText( data.get( "iTotal2") .get( position ));
							}
							setAflData();
							
						} else if ( sportType.equalsIgnoreCase(Constants.BASEBALL)) {
							
							setBaseBallData();
							
						} else if ( sportType.equalsIgnoreCase(Constants.CRICKET) || sportType.equalsIgnoreCase(Constants.TEST_CRICKET) ) {
							
							setCricketData();
						
						} else if ( sportType.equalsIgnoreCase(Constants.FOOTBALL) || sportType.equalsIgnoreCase(Constants.SOCCER) ) {
						
							if( isLiveMatch ) 
								setFootballData( );						
						}
						
					
						if( isUpcomingMatch ) {
							team1Score.setText("");
							team2Score.setText("");
						}
						
					}
					
				} else {
					
					team1Name.setText( data.get( "vHomeDisplayName") .get( position )  );
					team2Name.setText( data.get( "vAwayDisplayName") .get( position )  );
					if( !isUpcomingMatch ) {
						team1Score.setText( data.get( "vSummary1") .get( position ));
						team2Score.setText( data.get( "vSummary2") .get( position ));
					}	
	
				}
				

				//setting starting time of the contest
				if( isUpcomingMatch) {
					matchTime.setVisibility(View.GONE);
					matchSummary.setVisibility(View.GONE);
					matchStartingTime.setVisibility(View.VISIBLE);
					upcomingText.setVisibility(View.VISIBLE);
					upcomingText.setText(R.string.upcoming);
					matchStartingTime.setVisibility(View.VISIBLE);
					matchStartingTime.setText( output);	
				}

				
				// checking and setting the data whether the contest dont have live updates
				if(data.get("iHasLiveUpdates").get( position ) != null  && Integer.parseInt( data.get("iHasLiveUpdates").get( position ) ) == 0 ){

					boolean value = new DateUtil().isNotLiveUpdatesMatch(data.get("dStartTime").get(position), data.get("dEndTime").get(position), data.get("dScheduledStartTime").get(position));
					if( value ) {
						
						if( sportType == null || ! ( sportType.equalsIgnoreCase(Constants.LEADERBOARD) || sportType.equalsIgnoreCase(Constants.SET_BASED_DATA) )) {
							baseballSummary.setVisibility( View.GONE );
							cricketSummary.setVisibility( View.GONE );
							player1.setText("");
							player2.setText("");
							subplayer1.setText("");
							subplayer2.setText("");
							icon1.setVisibility(View.GONE);
							icon2.setVisibility(View.GONE);
							team1Score.setText("");
							team2Score.setText("");
						}
						
						matchTime.setVisibility(View.GONE);
						liveText.setVisibility(View.GONE);
						matchSummary.setVisibility(View.GONE);

						upcomingText.setVisibility(View.VISIBLE);
						upcomingText.setText(R.string.postGameScoresMsg);
						matchStartingTime.setVisibility(View.VISIBLE);
						matchStartingTime.setText(R.string.awaiting);
						
					}

				}




			} 


		} catch (Exception e) {

			Logs.show( e );
		}


	}
	
	
	/*
	 * setting the contest data if the sport type is FOOTBALL
	 */
	private void setFootballData( ) {

			try {
				// setting last event details	
					String vLastEventName = data.get("vLastEventName").get(position);
					String vLastEventDesc = data.get("vShortMessage").get(position);
					if( vLastEventName != null && vLastEventName.trim().length() != 0) {
						lastEventView.setVisibility(View.VISIBLE);
						lastEventName.setText(vLastEventName);
						lastEventDesc.setText(vLastEventDesc);
					}
			} catch (Exception e) {
				Logs.show(e);
			}

	}

	/*
	 * setting the contest data if the sport type is CRICKET
	 */
	private void setCricketData() {

		try {
			// setting match summary at top of the view.. like " ov 23 rr 4.5 "
					if (  isLiveMatch ) {
						
						String vOvers,vRunRate,vLastBall;
						vOvers = data.get( "vOvers").get( position ) ;
						vRunRate = data.get( "vRunRate").get( position );
						vLastBall = data.get( "vLastBall").get( position ) ;	
						matchSummary.setVisibility(View.GONE);
						matchTime.setVisibility(View.GONE);
						baseballSummary.setVisibility(View.GONE);
						cricketSummary.setVisibility(View.VISIBLE);
						liveText.setVisibility(View.GONE);

						((TextView)cricketSummary.findViewById(R.id.overs)).setText(PlayUpActivity.context.getResources().getString(R.string.over)+" "+vOvers);
						((TextView)cricketSummary.findViewById(R.id.runrate)).setText(PlayUpActivity.context.getResources().getString(R.string.runrate)+" "+vRunRate);
						((TextView)cricketSummary.findViewById(R.id.lastball)).setText(PlayUpActivity.context.getResources().getString(R.string.lastball)+" "+vLastBall);

						((TextView)cricketSummary.findViewById(R.id.overs)).setTypeface(Constants.OPEN_SANS_REGULAR);
						((TextView)cricketSummary.findViewById(R.id.runrate)).setTypeface(Constants.OPEN_SANS_REGULAR);
						((TextView)cricketSummary.findViewById(R.id.lastball)).setTypeface(Constants.OPEN_SANS_REGULAR);

					} 
			
					// setting player details 
					// it contains batsman and bowler
					// and batsman may contain striker and nonstriker depending on match type
			
					String firstName1 ,firstName2,lastName1,lastName2,playerDetails1= "",playerDetails2 ="",role1,role2;
					firstName1 = data.get( "vPlayerFirstName1" ).get( position ) ;
					firstName2 = data.get( "vPlayerFirstName2" ).get( position );
					lastName1 = data.get( "vPlayerLastName1" ).get( position ) ;
					lastName2 = data.get( "vPlayerLastName2" ).get(position ) ;
					role1 =  data.get( "vRole1" ).get( position ) ;
					role2 =  data.get( "vRole2" ).get( position ) ;
			
					String strikerFirstName = data.get( "vStrikerFirstName" ).get( position);
					String strikerLastName = data.get( "vStrikerLastName" ).get(position );
					String nonStrikerFirstName = data.get( "vNonStrikerFirstName" ).get( position ) ;
					String nonStrikerLastName = data.get( "vNonStrikerLastName" ).get( position ) ;
					String strikerDetails="", nonStrikerDetails="";
			
					// first player details
					if(firstName1!=null && firstName1.length() > 0) {
						if(lastName1!=null && lastName1.length() >0) {
							playerDetails1 = playerDetails1 + Character.toUpperCase(firstName1.charAt(0)) +".";
							playerDetails1 = playerDetails1 + lastName1;
						}
					} else {
						if(lastName1!=null && lastName1.length() >0) 
							playerDetails1 = playerDetails1 + lastName1;
					}
			
			
					// second player details
					if(firstName2!=null && firstName2.length() > 0) {
						if(lastName2!=null && lastName2.length() >0) {
							playerDetails2 = playerDetails2 + Character.toUpperCase(firstName2.charAt(0)) +".";
							playerDetails2 = playerDetails2 + lastName2;
						}
					} else {
						if(lastName2!=null && lastName2.length() >0) 
							playerDetails2 = playerDetails2 + lastName2;
					}
			
			
					// getting and setting striker details
					if(strikerFirstName!=null && strikerFirstName.length() > 0) {
						if(strikerLastName!=null && strikerLastName.length() >0) {
							strikerDetails = strikerDetails + Character.toUpperCase(strikerFirstName.charAt(0)) +".";
							strikerDetails = strikerDetails + strikerLastName;
						}
					} else {
						if(strikerLastName!=null && strikerLastName.length() >0) 
							strikerDetails = strikerDetails + strikerLastName;
					}
			
					
					// getting and setting non striker details
					if(nonStrikerFirstName!=null && nonStrikerFirstName.length() > 0) {
						if(nonStrikerLastName!=null && nonStrikerLastName.length() >0) {
							nonStrikerDetails = nonStrikerDetails + Character.toUpperCase(nonStrikerFirstName.charAt(0)) +".";
			
			
							if(nonStrikerLastName.length() > 15) {
			
								nonStrikerDetails = nonStrikerDetails + nonStrikerLastName.substring(0,13) + "...";
			
							} else {
								nonStrikerDetails = nonStrikerDetails + nonStrikerLastName;
							}
						}
					} else {
						if(nonStrikerLastName!=null && nonStrikerLastName.length() >0) 
							nonStrikerDetails = nonStrikerDetails + nonStrikerLastName;
			
					}
			
			
					if (playerDetails1 !=null && playerDetails1.trim().length() >0 ) {
						icon1.setVisibility(View.VISIBLE);	
						player1.setVisibility(View.VISIBLE);
						if(role1!=null && role1.equalsIgnoreCase("batsman")) {
							icon1.setPadding(0, 4, 0, 0);
							icon1.setImageResource(R.drawable.icon_cricket_icon_batsman);
						} else if(role1!=null && role1.equalsIgnoreCase("bowler")) {
							icon1.setPadding(0, 0, 0, 0);
							icon1.setImageResource(R.drawable.icon_cricket_icon_bowler);
						}
						player1.setText(playerDetails1);
					} else {
						icon1.setVisibility(View.GONE);	
						player1.setVisibility(View.GONE);
			
					}
			
			
					if (playerDetails2 !=null && playerDetails2.trim().length() >0 ) {					
						icon2.setVisibility(View.VISIBLE);	
						player2.setVisibility(View.VISIBLE);
						if(role2!=null && role2.equalsIgnoreCase("batsman")) {
							icon2.setPadding(0, 4, 0, 0);
							icon2.setImageResource(R.drawable.icon_cricket_icon_batsman);
						} else if(role2!=null && role2.equalsIgnoreCase("bowler")) {
							icon2.setPadding(0, 0, 0, 0);
							icon2.setImageResource(R.drawable.icon_cricket_icon_bowler);
						}
						player2.setText(playerDetails2);
					} else {
						icon2.setVisibility(View.GONE);	
						player2.setVisibility(View.GONE);
					}
			
			
					if(role1!=null && role1.equalsIgnoreCase("batsman")) {
			
						if( nonStrikerDetails!=null && nonStrikerDetails.trim().length() >0 ) {
							icon1.setVisibility(View.VISIBLE);
							icon1.setImageResource(R.drawable.icon_cricket_icon_batsman);
							subplayer1.setVisibility(View.VISIBLE);
							subplayer1.setText(nonStrikerDetails);
						} else {
							subplayer1.setVisibility(View.GONE);
						}
			
					} 
			
					if(role2!=null && role2.equalsIgnoreCase("batsman")) {
			
						if( nonStrikerDetails!=null && nonStrikerDetails.trim().length() >0 ) {	
							icon2.setVisibility(View.VISIBLE);
							icon2.setImageResource(R.drawable.icon_cricket_icon_batsman);
							subplayer2.setVisibility(View.VISIBLE);
							subplayer2.setText(nonStrikerDetails);
						} else {
							subplayer2.setVisibility(View.GONE);
						}
			
					}
		} catch (Exception e) {
			Logs.show(e);
		} 

		
	}

	/*
	 * setting the contest data if the sport type is BASEBALL
	 */
	private void setBaseBallData() {
		
		try {
			String firstName1 ,firstName2,lastName1,lastName2,playerDetails1= "",playerDetails2 ="",role1,role2;
			String inningsHalf, base1,base2;
			int innings,balls1,balls2,outs1,outs2,strikes1,strikes2;

			firstName1 = data.get( "vPlayerFirstName1" ).get( position ) ;
			firstName2 = data.get( "vPlayerFirstName2" ).get( position );
			lastName1 = data.get( "vPlayerLastName1" ).get( position ) ;
			lastName2 = data.get( "vPlayerLastName2" ).get( position ) ;
			role1 =  data.get( "vRole1" ).get( position ) ;
			role2 =  data.get( "vRole2" ).get( position ) ;
			base1 =  data.get( "vBase1" ).get( position );
			base2 = data.get( "vBase2" ).get( position ) ;

			inningsHalf = data.get( "vInnningsHalf" ).get( position ) ;
			innings = Integer.parseInt( data.get( "iInnnings" ).get( position ) ) ;
			balls1 = Integer.parseInt( data.get( "iBalls1" ).get( position ) );
			balls2 = Integer.parseInt( data.get( "iBalls2" ).get( position ) );
			outs1 = Integer.parseInt( data.get( "iOut1" ).get( position ) ) ;
			outs2 = Integer.parseInt( data.get( "iOut2" ).get( position )  ) ;
			strikes1 = Integer.parseInt( data.get( "iStrikes1" ).get( position ) );
			strikes2 = Integer.parseInt( data.get( "iStrikes2" ).get( position ) );

			// setting the match summary at top of the header view
			if( isLiveMatch) {
				
				liveText.setVisibility(View.GONE);
				matchSummary.setVisibility(View.GONE);
				matchTime.setVisibility(View.GONE);
				cricketSummary.setVisibility(View.GONE);
				baseballSummary.setVisibility(View.VISIBLE);

				ImageView matchHalfImage = (ImageView)baseballSummary.findViewById(R.id.matchHalf);
				TextView matchQuarter = (TextView)baseballSummary.findViewById(R.id.matchQuarter);
				ImageView matchLoad = (ImageView)baseballSummary.findViewById(R.id.matchLoad);
				TextView strikes = (TextView)baseballSummary.findViewById(R.id.strikes);
				TextView outs = (TextView)baseballSummary.findViewById(R.id.outs);

				matchQuarter.setTypeface(Constants.OPEN_SANS_REGULAR);
				strikes.setTypeface(Constants.OPEN_SANS_REGULAR);
				outs.setTypeface(Constants.OPEN_SANS_REGULAR);

				String baseString;
				if( role1!=null && role1.equalsIgnoreCase("batter")) 
					baseString = base1.trim();
				else
					baseString = base2.trim();

				if( baseString != null) {

					matchLoad.setVisibility(View.VISIBLE);
					if( baseString.equalsIgnoreCase("100"))
						matchLoad.setImageResource(R.drawable.icon_baseball_bases_100);
					else if( baseString.equalsIgnoreCase("110"))
						matchLoad.setImageResource(R.drawable.icon_baseball_bases_110);
					else if( baseString.equalsIgnoreCase("111"))
						matchLoad.setImageResource(R.drawable.icon_baseball_bases_111);
					else if( baseString.equalsIgnoreCase("101"))
						matchLoad.setImageResource(R.drawable.icon_baseball_bases_101);
					else if( baseString.equalsIgnoreCase("001"))
						matchLoad.setImageResource(R.drawable.icon_baseball_bases_001);
					else if( baseString.equalsIgnoreCase("011"))
						matchLoad.setImageResource(R.drawable.icon_baseball_bases_011);
					else if( baseString.equalsIgnoreCase("010"))
						matchLoad.setImageResource(R.drawable.icon_baseball_bases_010);
					else if( baseString.equalsIgnoreCase("000"))
						matchLoad.setImageResource(R.drawable.icon_baseball_bases_000);
					else 
						matchLoad.setVisibility(View.INVISIBLE);

					baseString = null;
				} else {
					matchLoad.setVisibility(View.INVISIBLE);

				}


				String quarterText="";
				String strikesText="";
				String outsText="";
				if( innings !=0 ) {
					if( innings == 1)								
						quarterText+=innings+"ST";
					else if( innings == 2)								
						quarterText+=innings+"ND";
					else if( innings == 3)								
						quarterText+=innings+"RD";
					else 							
						quarterText+=innings+"TH";
				}

				if( role1!=null && role1.equalsIgnoreCase("batter")) {
					strikesText = balls1+"-"+strikes1;
					outsText  = outs1+" OUT" ;
				} else {
					strikesText = balls2+"-"+strikes2;
					outsText  = outs2+" OUT" ;
				}


				if( inningsHalf!=null && inningsHalf.equalsIgnoreCase("Top") ) {
					matchHalfImage.setImageResource(R.drawable.icon_baseball_arrow_top_innings);
				} else {
					matchHalfImage.setImageResource(R.drawable.icon_baseball_arrow_bottom_innings);
				}

				matchQuarter.setText( quarterText );
				strikes.setText( strikesText );
				outs.setText( outsText );

				quarterText = null;
				outsText = null;
				strikesText = null;

			}	

			
			// setting the player details
			
			// first player details
			if(firstName1!=null && firstName1.length() > 0) {
				if(lastName1!=null && lastName1.length() >0) {
					playerDetails1 = playerDetails1 + Character.toUpperCase(firstName1.charAt(0)) +".";

					if(lastName1.length() > 10) {

						playerDetails1 = playerDetails1 + lastName1.substring(0,9) + "...";

					} else {
						playerDetails1 = playerDetails1 + lastName1;
					}
				}
			} else {
				if(lastName1!=null && lastName1.length() >0) 
					playerDetails1 = playerDetails1 + lastName1;
			}



			// second player details
			if(firstName2!=null && firstName2.length() > 0) {
				if(lastName2!=null && lastName2.length() >0) {
					playerDetails2 = playerDetails2 + Character.toUpperCase(firstName2.charAt(0)) +".";

					if(lastName2.length() > 10) {

						playerDetails2 = playerDetails2 + lastName2.substring(0,9) + "...";

					} else {
						playerDetails2 = playerDetails2 + lastName2;
					}
				}
			} else {
				if(lastName2!=null && lastName2.length() >0) 
					playerDetails2 = playerDetails2 + lastName2;
			}


			if (playerDetails1 !=null && playerDetails1.trim().length() >0 ) {
				icon1.setVisibility(View.VISIBLE);	
				player1.setVisibility(View.VISIBLE);
				if(role1!=null && role1.equalsIgnoreCase("batter"))
					icon1.setImageResource(R.drawable.icon_baseball_icon_batter);
				else 
					icon1.setImageResource(R.drawable.icon_baseball_icon_pitcher);
				player1.setText(playerDetails1);
			} else {
				icon1.setVisibility(View.GONE);	
				player1.setVisibility(View.GONE);
			}


			if (playerDetails2 !=null && playerDetails2.trim().length() >0 ) {					
				icon2.setVisibility(View.VISIBLE);	
				player2.setVisibility(View.VISIBLE);
				if(role2!=null && role2.equalsIgnoreCase("batter"))
					icon2.setImageResource(R.drawable.icon_baseball_icon_batter);
				else 
					icon2.setImageResource(R.drawable.icon_baseball_icon_pitcher);
				player2.setText(playerDetails2);
			} else {
				icon2.setVisibility(View.GONE);	
				player2.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			Logs.show(e);
		}

	}

	/*
	 * setting the contest data if the sport type is AFL
	 * here we will show the detailed score of individual teams
	 */
	private void setAflData() {
		
		try {
			String homeTeamSummary ="",awayTeamSummary="";
			int homeTeamSuperGoals,awayTeamSuperGoals,homeTeamgoals,awayTeamgoals,homeTeambehinds,awayTeambehinds;
			

			homeTeamSuperGoals= Integer.parseInt( data.get( "iSuperGoals1" ).get( position ) ) ; 
			awayTeamSuperGoals= Integer.parseInt( data.get( "iSuperGoals2" ).get( position ) ) ; 
			homeTeamgoals = Integer.parseInt( data.get( "iGoals1" ).get( position ) ) ; 
			awayTeamgoals = Integer.parseInt( data.get( "iGoals2" ).get( position ) ) ; 
			homeTeambehinds = Integer.parseInt( data.get( "iBehinds1" ).get( position ) ) ; 
			awayTeambehinds = Integer.parseInt( data.get( "iBehinds2" ).get( position ) ) ; 

			if( Integer.parseInt( data.get( "iTotal1" ).get( position ) ) == 0) {
				homeTeamSummary="";
			} else {
				homeTeamSummary="";
				if ( homeTeamSuperGoals!=0 )
					homeTeamSummary+=homeTeamSummary+homeTeamSuperGoals+".";

				homeTeamSummary += homeTeamgoals+"."+homeTeambehinds+"("+ Integer.parseInt( data.get( "iTotal1" ).get( position ) ) +")";
			}
			
			if( Integer.parseInt( data.get( "iTotal2" ).get( position ) )  == 0) {
				awayTeamSummary="";
			} else {
				awayTeamSummary="";
				if ( awayTeamSuperGoals!=0 )
					awayTeamSummary+=awayTeamSummary+awayTeamSuperGoals+".";
				awayTeamSummary += awayTeamgoals+"."+awayTeambehinds+"("+ Integer.parseInt( data.get( "iTotal2" ).get( position ) ) +")";
			}

			if( !homeTeamSummary.equalsIgnoreCase("") || !awayTeamSummary.equalsIgnoreCase("") )  {

				icon1.setVisibility(View.INVISIBLE);
				icon2.setVisibility(View.INVISIBLE);
				player1.setVisibility(View.VISIBLE);
				player2.setVisibility(View.VISIBLE);
				subplayer1.setVisibility(View.GONE);
				subplayer2.setVisibility(View.GONE);

				player1.setPadding(10,0,0,0);
				player2.setPadding(10,0,0,0);
				player1.setText(homeTeamSummary);
				player2.setText(awayTeamSummary);
			}
		} catch (Exception e) {
			Logs.show(e);
		}

		
	}

	/*
	 * setting the contest data if the sport type is setbased
	 */
	private void setSetBasedData() {
		
		try {
			TextView setPoint1 = null;
			TextView setPoint2 = null;

			if ( !isUpcomingMatch ) {

				setView.removeAllViews();

				Hashtable < String, List < String > >  summaryData1 = PlayupLiveApplication.getDatabaseWrapper().select ( " Select vSummary FROM summaries " +
						"WHERE vContestId = \"" + data.get( "vContestId" ).get ( position ) + "\" " +
						"AND  vTeamId = \"" + data.get ( "vHomeTeamId" ).get(position ) + "\" " +
				"ORDER BY iPosition ASC" );

				Hashtable <String, List < String > >  summaryData2 = PlayupLiveApplication.getDatabaseWrapper().select ( " Select vSummary FROM summaries " +
						" WHERE vContestId = \"" + data.get ( "vContestId" ).get ( position ) + "\" " +
						"AND  vTeamId = \"" + data.get ( "vAwayTeamId" ).get ( position ) + "\" " +
				"ORDER BY iPosition ASC" );

				int vSummary_len1 = 0, vSummary_len2  = 0;
				if ( summaryData1 != null && summaryData1.get ( "vSummary" ) != null ) {
					vSummary_len1 = summaryData1.get ("vSummary").size();
				}
				if ( summaryData2 != null && summaryData2.get ( "vSummary" ) != null ) {
					vSummary_len2 = summaryData2.get ("vSummary").size();
				}
				int summary_len = ( vSummary_len1 > vSummary_len2 )?  vSummary_len1 : vSummary_len2;

				
				// if summary field is not present use summary1 and summary2 fields
				// else use summary fields for showing columnar style of score data
				if( summary_len == 0 ) {
					
					LinearLayout set_item = ( LinearLayout ) inflater.inflate( R.layout.set_view, null);
					setPoint1 = (TextView)set_item.findViewById(R.id.score1);
					setPoint2 = (TextView)set_item.findViewById(R.id.score2);

					setPoint1.setText( data.get( "vSummary1") .get( position ));
					setPoint2.setText( data.get( "vSummary2") .get( position ));

					setPoint1.setTextColor(Color.parseColor("#FF4754"));
					setPoint2.setTextColor(Color.parseColor("#FF4754"));

					setPoint1.setTypeface(Constants.BEBAS_NEUE);
					setPoint2.setTypeface(Constants.BEBAS_NEUE);
					setView.addView( set_item );
					
				} else {

					for ( int i = 0; i < summary_len  ; i++ ) {

						LinearLayout set_item = ( LinearLayout ) inflater.inflate( R.layout.set_view, null);
						setPoint1 = (TextView)set_item.findViewById(R.id.score1);
						setPoint2 = (TextView)set_item.findViewById(R.id.score2);
						
						setPoint1.setTypeface(Constants.BEBAS_NEUE);
						setPoint2.setTypeface(Constants.BEBAS_NEUE);
						
						if ( vSummary_len1 > 0 && vSummary_len1 > i ) {
							
							setPoint1.setTextColor( (  i == summary_len - 1  )? Color.parseColor("#FF4754") : Color.parseColor("#696B6E") );
							setPoint1.setText(summaryData1.get("vSummary").get(i));
						
						} else {
							setPoint1.setText( "" );
						}

						if ( vSummary_len2 > 0 && vSummary_len2 > i ) {

							setPoint2.setTextColor( (  i == summary_len - 1  )? Color.parseColor("#FF4754") : Color.parseColor("#696B6E") );
							
							setPoint2.setText(summaryData2.get("vSummary").get(i));
						
						} else {
							setPoint2.setText( "" );
						}

						setView.addView( set_item );
					}
				}
			} else if( setView != null ) {
					setView.removeAllViews();
			}

			


			teamFlag1.setImageDrawable(null);
			teamFlag2.setImageDrawable(null);

			
			homeTeamName = data.get( "vHomeDisplayName" ).get( position );
			awayTeamName = data.get( "vAwayDisplayName" ).get( position );


			if ( homeTeamName != null ) {
				name1.setText( homeTeamName );
			} else {
				name1.setText( "" );
			}
			
			if ( awayTeamName != null ) {
				name2.setText( awayTeamName );
			} else {
				name2.setText( "" );
			}
			
			if( isUpcomingMatch ) {
				
				if( homeTeamName!= null && homeTeamName.length() > 18  ) {
					name1.setText(homeTeamName.substring(0,16)+"...");
				}
				if( awayTeamName!= null && awayTeamName.length() > 18  ) {
					name2.setText(awayTeamName.substring(0,16)+"...");
				}
			}

			int iActive1 = 0, iActive2 = 0;
			if ( data.get( "iActive1" ).get( position ) != null && data.get( "iActive1" ).get( position ).trim().length() > 0 ) {
				try {
					iActive1 = Integer.parseInt( data.get( "iActive1" ).get( position) ) ;
					status1.setVisibility( ( iActive1 == 0 )? View.GONE : ( iActive1 == 1)? View.VISIBLE : View.INVISIBLE );
				} catch ( Exception e ) {
					Logs.show ( e );
				}
			}
			if ( data.get( "iActive2" ).get( position) != null && data.get( "iActive2" ).get( position ).trim().length() > 0  ) {
				try {
					iActive2 = Integer.parseInt( data.get( "iActive2" ).get( position) ) ;
					status2.setVisibility( ( iActive2 == 0 )? View.GONE : ( iActive2 == 1)? View.VISIBLE : View.INVISIBLE );
				} catch ( Exception e ) {
					Logs.show ( e );
				}
			}
			
			imageDownloader.download(data.get( "vHomeCalendarUrl" ).get( position ), teamFlag1, false, null);
			imageDownloader.download(data.get( "vAwayCalendarUrl" ).get( position ), teamFlag2, false, null);
		} catch (Exception e) {
			Logs.show(e);
		}

		
	}

	/*
	 * setting the contest data if the sport type is leaderborad
	 */
	private void setLeaderBoardData() {

		try {
			
			
			
			Hashtable<String, List<String>> leaderBoardData = dbUtil.getLeaderBoardData(  data.get( "vContestId" ).get( position ) );

				if (!fromPublicRoom && leaderBoardData != null && leaderBoardData.get ( "vTeamId" ) != null && leaderBoardData.get ( "vTeamId" ) .size ( ) > 2 ) {

						headerParams = (android.view.ViewGroup.LayoutParams) header.getLayoutParams();
						
						if( Constants.DENSITY.equalsIgnoreCase("low") ) {
							headerParams.height = 110;
						} else if( Constants.DENSITY.equalsIgnoreCase("medium") ) {
							headerParams.height = 130;
						} else if( Constants.DENSITY.equalsIgnoreCase("high") ) {
							headerParams.height = 180;
						}

						if( PlayUpActivity.isXhdpi ) {
							headerParams.height = 235;
						}  				

						header.setLayoutParams(headerParams);					
				}

			// adding the vies to the layout if its called from an adapter, otherwise will add in listview
			if ( fromAdapter ) {
				
				
				leaderBoradView.setVisibility( View.VISIBLE );
				leaderBoradView.removeAllViews();
				leaderBoardList.setVisibility( View.GONE );

				if(leaderBoardData!= null && leaderBoardData.get("vTeamId") != null && leaderBoardData.get("vTeamId").size() > 0  ) {	
					int size =  leaderBoardData.get("vTeamId").size() ;

					if( size > 3 ) {
						size = 3;
					}
					for ( int i = 0 ; i < size ; i++ ) {
						try {	


							// inflate leader board item
							LayoutInflater inflater = LayoutInflater.from( PlayUpActivity.context );
							RelativeLayout lin =  ( RelativeLayout ) inflater.inflate(R.layout.leader_board_item, null);

							// get the leader board views
							TextView positionValue = (TextView) lin.findViewById(R.id.teamPosition);
							TextView name = (TextView) lin.findViewById(R.id.teamName);
							TextView points = (TextView) lin.findViewById(R.id.teamScore);
							ImageView flag = (ImageView) lin.findViewById(R.id.teamFlag);

							// set type faces 
							positionValue.setTypeface(Constants.BEBAS_NEUE );
							name.setTypeface(Constants.BEBAS_NEUE );
							points.setTypeface(Constants.BEBAS_NEUE );

							// setting the values 
							String teamName = leaderBoardData.get("vDisplayName").get(i);
							String positionSummary = leaderBoardData.get("vPositionSummary").get(i);
							flag.setImageDrawable(null);

							if ( teamName != null ) {
								name.setText( teamName );
							} else {
								name.setText( "" );
							}

							if(leaderBoardData.get("vCalendarUrl").get(i) != null && leaderBoardData.get("vCalendarUrl").get(i).trim().length() > 0)
								imageDownloader.download(leaderBoardData.get("vCalendarUrl").get(i), flag, false, null );

							positionValue.setVisibility(View.VISIBLE);

							if ( positionSummary != null ) {
								positionValue.setText( positionSummary );
							} else {
								positionValue.setText( "" );
							}


							if( !isUpcomingMatch ) {
								points.setText(leaderBoardData.get("vSummary").get(i));

							} else {

								points.setText("");

								if ( positionSummary == null || ( positionSummary != null && positionSummary.trim().length() == 0 ) ) {

									positionValue.setVisibility(View.GONE);

									if( teamName!= null && teamName.length() > 18  ) {
										name.setText(teamName.substring(0,16)+"...");
									}
								} else {

									if( teamName!= null && teamName.length() > 14  ) {
										name.setText(teamName.substring(0,12)+"...");
									}
								}
							}

							leaderBoradView.addView(lin);
						} catch (Exception e) {
							Logs.show(e);

						}
					}
				}
			} else {
				

				leaderBoradView.setVisibility( View.GONE );
				leaderBoardList.setVisibility( View.VISIBLE );

				if( leaderBoardAdapter == null ) {
						leaderBoardAdapter = new LeaderBoardAdapter( leaderBoardData, isUpcomingMatch );
						leaderBoardList.setAdapter( leaderBoardAdapter );
				} else {
					leaderBoardAdapter.setData( leaderBoardData, isUpcomingMatch );
//					if( leaderBoardList.getSelectedView()!= null  && leaderBoardList.getSelectedView().getTag(R.id.about_txtview)!=null ) {
//						String selectedTeamId = leaderBoardList.getSelectedView().getTag(R.id.about_txtview).toString();
//						int position = findPositionOfTeam( leaderBoardData, selectedTeamId );
//						leaderBoardList.setSelection( position );
//					}
					
				}

				
			}
		} catch (Exception e) {
			Logs.show(e);
		}
		
	}

	
	
	
	private int findPositionOfTeam( Hashtable<String, List<String>> leaderBoardData, String selectedTeamId ) {
		if( leaderBoardData.get("vTeamId") != null && leaderBoardData.get("vTeamId").size() > 0 ) {
			for( int i=0; i< leaderBoardData.get("vTeamId").size(); i++ ) {
				if( leaderBoardData.get("vTeamId").get(i)!=null && leaderBoardData.get("vTeamId").get(i).toString().equalsIgnoreCase(selectedTeamId) )
					return i;
			}
		}
		return 0;
	}


	/*
	 * setting the background of the contest...
	 * for normal sports we will take random images from resources
	 * and save that image names in db.
	 * so for a perticular contest its background will be constant throuhout the appilication
	 */
	public void setContestBackground () {
		try {
			if( sportType !=null ) {
				

				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
				lp.addRule( RelativeLayout.CENTER_HORIZONTAL, 0 );
				lp.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM, 1 );
				lp.setMargins( 24, 0, 0, 0);
				sportImage.setLayoutParams( lp );
				
				sportImage.setImageResource(0);
				
				
				String vImageUrl = dbUtil.getWaterMarkImage(vContestId);
				
				if( vImageUrl!=null && vImageUrl.trim().length() > 0 ) {
					sportImage.setVisibility(View.VISIBLE);
					imageDownloader.download(vImageUrl, sportImage, false, null);
					
				} else {
					
					if( randomGenerator == null)
						randomGenerator = new Random();

					contestBackground = null;
					int randomIndex = randomGenerator.nextInt( 3 );
					contestBackground = dbUtil.getSportBackground( vContestId );
					if( contestBackground == null ) {

						if( sportType.equalsIgnoreCase(Constants.CRICKET) || sportType.equalsIgnoreCase(Constants.TEST_CRICKET)) {
							contestBackground = cricket_images[randomIndex];
						} else if( sportType.equalsIgnoreCase(Constants.BASEBALL)) {
							contestBackground = baseball_images[randomIndex];
						} else if( sportType.equalsIgnoreCase(Constants.AFL)) {
							contestBackground = afl_images[randomIndex];
						} else if( sportType.equalsIgnoreCase(Constants.BASKETBALL)) {
							contestBackground = basketball_images[randomIndex];
						} else if( sportType.equalsIgnoreCase(Constants.FOOTBALL) || sportType.equalsIgnoreCase(Constants.SOCCER)) {
							contestBackground = football_images[randomIndex];
						} else if( sportType.equalsIgnoreCase(Constants.HOCKEY) || sportType.equalsIgnoreCase(Constants.ICE_HOCKEY)) {
							contestBackground = hockey_images[randomIndex];
						} else if( sportType.equalsIgnoreCase(Constants.RUGBY_LEAGUE) ) {
							contestBackground = rugby_league_images[randomIndex];
						} else if(sportType.equalsIgnoreCase(Constants.RUGBY_UNION) ) {
							contestBackground = rugby_union_images[randomIndex];
						} else if( sportType.equalsIgnoreCase(Constants.NFL)  ) {
							contestBackground = nfl_images[randomIndex];
						}		


						if( contestBackground != null ) {
							dbUtil.setSportBackground(vContestId, contestBackground);
						}

					} 
					
					if( contestBackground != null ) {
						sportImage.setVisibility(View.VISIBLE);
						int resID = PlayUpActivity.context.getResources().getIdentifier(contestBackground , "drawable", PlayUpActivity.context.getPackageName());
						sportImage.setImageResource(resID);	
						sportImage.setLayoutParams( lp );
					}
					
				}
				

			}
		} catch (Exception e) {
			Logs.show(e);
		}catch(Error e){
			Logs.show(e);
		}
	}
	
	
	
	
	

	/**
	 * this arrays contains the image names of the diff. sports
	 * while setting background will use these image names
	 */
	public String cricket_images[] = { "cricket_1", "cricket_2", "cricket_3" };
	public String afl_images[] = { "afl_1", "afl_2", "afl_3" };
	public String baseball_images[] = { "baseball_1", "baseball_2",  "baseball_3"};
	public String football_images[] = { "football_1",  "football_2", "football_3"};
	public String basketball_images[] = { "basketball_1", "basketball_2","basketball_3"};
	public String rugby_union_images[] = { "rugby_union_1","rugby_union_2","rugby_union_3" };
	public String rugby_league_images[] = { "rugby_league_1", "rugby_league_2","rugby_league_3" };
	public String nfl_images[] = { "american_football_1",  "american_football_2",  "american_football_3" };
	public String hockey_images[] = { "hockey_1", "hockey_2", "hockey_3" };


}
