package com.playup.android.adapters;

import java.util.Hashtable;


import java.util.List;


import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;



public class TeamScheduleGenerator {
	
	private Hashtable<String, List<String>> data = null;
	private DatabaseUtil dbUtil = null;
	public int position = 0;

	
	public RelativeLayout headerSummaryView;
	public View gapView; 
	public TextView team1Score;
	public TextView team2Score;
	public TextView team1Name;
	public TextView team2Name;
	public TextView liveText;
	public TextView matchSummary;
	public ImageView icon1,icon2;
	public TextView player1,player2,subplayer1,subplayer2;
	private TextView competitionName;
	private ImageView ticket_divider;
	private ImageView headerShadow;
	
	private TextView lastEventName;
	private TextView lastEventDesc;

	String sportType = null;
	String vContestId = null;
	boolean isUpcomingMatch = false;
	boolean isLiveMatch = false;
	String output = null;
	private View matchHeaderBase ;
	private String headerColor = null;
	private String headerTitleColor = null;
	
	public TeamScheduleGenerator(Hashtable<String, List<String>> headerData, View matchHeaderBase, int position, String headerColor, String headerTitleColor  ) {
		this.matchHeaderBase = matchHeaderBase;
		this.data = headerData;
		this.position = position;
		this.headerColor = headerColor;
		this.headerTitleColor = headerTitleColor;
		dbUtil = DatabaseUtil.getInstance();
		initializeViews();
	}


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
		if( data!=null && data.get( "vSportType" )!=null && data.get( "vSportType" ).get( position ) != null)
			return data.get( "vSportType" ).get( position );	
		else
			return null;
	}

	/**
	 * initializing views of the passed header
	 */
	private void initializeViews() {
		if( matchHeaderBase == null ) 
			return;
		
		    headerSummaryView = (RelativeLayout) matchHeaderBase.findViewById(R.id.headerSummaryView);
		    gapView = (View) matchHeaderBase.findViewById(R.id.gapView);

		    headerShadow= (ImageView)matchHeaderBase.findViewById(R.id.headerShadow);
		    ticket_divider = (ImageView)matchHeaderBase.findViewById(R.id.ticket_divider);
			liveText  = (TextView) matchHeaderBase.findViewById(R.id.liveText);
			matchSummary = (TextView) matchHeaderBase.findViewById(R.id.matchSummary);			
			competitionName =(TextView) matchHeaderBase.findViewById(R.id.competitionName);
		
			team1Score = (TextView) matchHeaderBase.findViewById(R.id.team1Score);
			team2Score = (TextView) matchHeaderBase.findViewById(R.id.team2Score);
			team1Name = (TextView) matchHeaderBase.findViewById(R.id.team1Name);
			team2Name = (TextView) matchHeaderBase.findViewById(R.id.team2Name);		
			icon1 = (ImageView) matchHeaderBase.findViewById(R.id.icon1);
			icon2 = (ImageView) matchHeaderBase.findViewById(R.id.icon2);
			player1 = (TextView) matchHeaderBase.findViewById(R.id.player1);		
			player2 = (TextView) matchHeaderBase.findViewById(R.id.player2);		
			subplayer1 = (TextView) matchHeaderBase.findViewById(R.id.subplayer1);		
			subplayer2 = (TextView) matchHeaderBase.findViewById(R.id.subplayer2);
			

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
			
		
			competitionName.setTypeface( Constants.OPEN_SANS_REGULAR );
			matchSummary.setTypeface(Constants.OPEN_SANS_REGULAR);
			
		
			setHeaderData();	
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
					//Logs.show ( e );
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
				
				// removing visibility to the views, to avoid showing unnecessary data while scrolling 
				liveText.setVisibility( View.GONE );
				matchSummary.setVisibility( View.GONE );
				competitionName.setVisibility( View.GONE );
				lastEventDesc.setVisibility( View.GONE );
				lastEventName.setVisibility( View.GONE );
				
				ticket_divider.setVisibility( View.GONE );
				headerShadow.setVisibility( View.GONE );
				// setting header and header items color 
				
				try {	
					if( headerColor != null && headerColor.trim().length() > 0 ) {
						if( headerColor.contains("0x") )
							headerColor = headerColor.replace("0x", "");
						headerSummaryView.setBackgroundColor( Color.parseColor( "#" +headerColor) );	
					} else {
						headerShadow.setVisibility( View.VISIBLE );
						ticket_divider.setVisibility( View.VISIBLE );
					}
					
					if( headerTitleColor != null && headerTitleColor.trim().length() > 0 ) {
						if( headerTitleColor.contains("0x") )
							headerTitleColor = headerTitleColor.replace("0x", "");
						
						matchSummary.setTextColor( Color.parseColor("#"+headerTitleColor));
						competitionName.setTextColor( Color.parseColor("#"+headerTitleColor));
					} else {
						if( isUpcomingMatch )
							matchSummary.setTextColor( Color.parseColor("#FF4754"));
						else
							matchSummary.setTextColor( Color.parseColor("#696B6E"));					
						competitionName.setTextColor( Color.parseColor("#696B6E"));
					}
					
				} catch (Exception e) {
					//Logs.show(e);
				}
				
				
				sportType =data.get( "vSportType" ).get( position );	
				vContestId =data.get( "vContestId" ).get( position );

				
				//Setting the match summary/ scheduled time on the header
				if( matchTimingsAvailable ) {
					if ( isLiveMatch ) {
						liveText.setVisibility(View.VISIBLE);
						matchSummary.setVisibility(View.VISIBLE);
						matchSummary.setText( data.get( "vSummary" ).get( position ) );
					} else {
						liveText.setVisibility(View.GONE);
						if( !isUpcomingMatch ) {
							matchSummary.setVisibility(View.VISIBLE);
							matchSummary.setText( R.string.completed);
						}
					} 
				}


				// setting competition name
				competitionName.setVisibility( View.VISIBLE );
				competitionName.setText(data.get( "vCompetitionName" ).get( position ) );
				
				// setting the stadium name
				
				lastEventDesc.setVisibility( View.VISIBLE );
				lastEventDesc.setTypeface( Constants.OPEN_SANS_SEMIBOLD );
				lastEventDesc.setText(data.get( "vStadiumName" ).get( position )  );
				
				
				
				//setting the header data based on sport type
				if( sportType!=null ) {
				
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
						}
						
							
						
						if( isLiveMatch ) {
							lastEventDesc.setVisibility( View.GONE );
							if (sportType.equalsIgnoreCase(Constants.AFL))  {
								setAflData();
							
							} else if ( sportType.equalsIgnoreCase(Constants.BASEBALL)) {								
								setBaseBallData();
								
							} else if ( sportType.equalsIgnoreCase(Constants.CRICKET) || sportType.equalsIgnoreCase(Constants.TEST_CRICKET) ) {								
								setCricketData();
							
							} else if ( sportType.equalsIgnoreCase(Constants.FOOTBALL) || sportType.equalsIgnoreCase(Constants.SOCCER) ) {
								setFootballData( );						
							}
							
						}
						
					
						if( isUpcomingMatch ) {
							team1Score.setText("");
							team2Score.setText("");
						}
						
					}
				
					else {

					team1Name.setText( data.get( "vHomeDisplayName") .get( position )  );
					team2Name.setText( data.get( "vAwayDisplayName") .get( position )  );
					if( !isUpcomingMatch ) {
						team1Score.setText( data.get( "vSummary1") .get( position ));
						team2Score.setText( data.get( "vSummary2") .get( position ));
					}	
	
				}

				//setting starting time of the contest
				if( isUpcomingMatch) {
					matchSummary.setVisibility(View.VISIBLE);
					matchSummary.setText (R.string.upcoming);
					team1Score.setText("");
					team2Score.setText("");
					try {
						team1Score.setText( output.split("\n")[0] );
						team2Score.setText( output.split("\n")[1] );
					} catch (Exception e) {
						// TODO: handle exception
					}
				}

				
				if(data.get("iHasLiveUpdates").get( position ) != null  && Integer.parseInt( data.get("iHasLiveUpdates").get( position ) ) == 0 ){

					boolean value = new DateUtil().isNotLiveUpdatesMatch(data.get("dStartTime").get(position), data.get("dEndTime").get(position), data.get("dScheduledStartTime").get(position));
					if( value ) {
						
							player1.setText("");
							player2.setText("");
							subplayer1.setText("");
							subplayer2.setText("");
							icon1.setVisibility(View.GONE);
							icon2.setVisibility(View.GONE);
							team1Score.setText("");
							team2Score.setText("");
						
							liveText.setVisibility(View.GONE);
							matchSummary.setVisibility(View.VISIBLE);
							matchSummary.setText(R.string.postGameScoresMsg);

							String matchTime = PlayUpActivity.context.getResources().getString(R.string.awaiting );
							
							team1Score.setText( matchTime.split("\n")[0]);
							team2Score.setText(matchTime.split("\n")[1]);
					
					}

				}


					
				} 
				

				// checking and setting the data whether the contest dont have live updates
				


		} catch (Exception e) {

			//Logs.show( e );
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
						lastEventDesc.setTypeface(Constants.OPEN_SANS_REGULAR);
						lastEventDesc.setVisibility( View.VISIBLE );
						lastEventName.setVisibility( View.VISIBLE);
						lastEventName.setText(vLastEventName);
						lastEventDesc.setText(vLastEventDesc);
					}
			} catch (Exception e) {
				//Logs.show(e);
			}
	}

	/*
	 * setting the contest data if the sport type is CRICKET
	 */
	private void setCricketData() {

		
				try {
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
					//Logs.show(e);
				} 

		
	}

	/*
	 * setting the contest data if the sport type is BASEBALL
	 */
	private void setBaseBallData() {
		
		try {
			String firstName1 ,firstName2,lastName1,lastName2,playerDetails1= "",playerDetails2 ="",role1,role2;
			
			firstName1 = data.get( "vPlayerFirstName1" ).get( position ) ;
			firstName2 = data.get( "vPlayerFirstName2" ).get( position );
			lastName1 = data.get( "vPlayerLastName1" ).get( position ) ;
			lastName2 = data.get( "vPlayerLastName2" ).get( position ) ;
			role1 =  data.get( "vRole1" ).get( position ) ;
			role2 =  data.get( "vRole2" ).get( position ) ;
			
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
		//	Logs.show(e);
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
		} catch (NumberFormatException e) {
			//Logs.show(e);
		}

		
	}

	
	
	
	
}
