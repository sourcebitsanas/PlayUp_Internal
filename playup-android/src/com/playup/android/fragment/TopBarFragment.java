package com.playup.android.fragment;



import java.util.HashMap;


import android.content.Context;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;


import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.PreferenceManagerUtil;
import com.playup.android.util.Util;


/**
 * Top Bar Fragment
 */
public class TopBarFragment extends MainFragment implements OnClickListener	{


	private RelativeLayout searchView;

	// For searching leagues
	private EditText leagueSearchEdittext;

	// private ImageView searchIcon;
	private RelativeLayout searchRelative;

	private ProgressBar friendsSearchSpinner;
	private ImageView leagueSearch;
	private ImageView shareScores;
	private boolean outSide=false;


	private RelativeLayout backButton_rel;
	RelativeLayout content_layout;

	public boolean isPlayupFriendsFragment;
	


	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

		
		content_layout = (RelativeLayout) inflater.inflate(R.layout.topbar, null );

		return content_layout;
	}


	@Override
	public void onResume () {
		try {
			super.onResume();

			parentView = content_layout;
			initialize(content_layout);
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
	}

	private TextView title;
	private ImageView title_img;

	private ImageView friendSearch;
	private ImageView homeImg;
	private ImageView divider;
	private TextView notificationText;

	private RelativeLayout search;

	private View parentView;


	/**
	 * setting all the content in the views
	 */
	private void initialize ( RelativeLayout conteLayout ) {

		try {
			FrameLayout leftView = (FrameLayout) conteLayout.findViewById( R.id.leftView );

			title = (TextView) conteLayout.findViewById( R.id.topbar_title_text );



			LayoutParams lp = (LayoutParams) title.getLayoutParams();
			float temp = ( ( float ) Constants.DPI ) / 160;
			lp.leftMargin =  (int) (temp * 34);
			title.setLayoutParams( lp );

			lp =  (LayoutParams) leftView.getLayoutParams();
			lp.leftMargin = (int) (temp * 34);
			leftView.setLayoutParams( lp );

			title_img = (ImageView) leftView.findViewById( R.id.topbar_logo );


			friendSearch = (ImageView) conteLayout.findViewById( R.id.friendsSearch );
			divider = (ImageView) conteLayout.findViewById( R.id.topbar_divider1 );
			homeImg  = (ImageView) conteLayout.findViewById( R.id.topbar_home_imgView );

			notificationText = (TextView) conteLayout.findViewById( R.id.topbar_notification_text );

			searchView = (RelativeLayout) conteLayout.findViewById(R.id.searchView);


			notificationText.setTypeface(Constants.OPEN_SANS_SEMIBOLD);

			search = (RelativeLayout) leftView.findViewById( R.id.topbar_rel_search );

			leftView = null;
			RelativeLayout searchView = (RelativeLayout) search.findViewById( R.id.searchView );
			searchRelative = (RelativeLayout) search.findViewById( R.id.searchRelative );

			// These are used in league searching
			leagueSearchEdittext = (EditText) searchView.findViewById(R.id.leagueSearchEdittext);
			leagueSearch = (ImageView) searchRelative.findViewById(R.id.leagueSearch);
			friendsSearchSpinner = ( ProgressBar ) searchRelative.findViewById( R.id.friendsSearchSpinner);
			searchView = null;


			title.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
			notificationText.setTypeface(Constants.OPEN_SANS_SEMIBOLD);


			divider.setAlpha(100);

			backButton_rel = ( RelativeLayout )  parentView.findViewById( R.id.back_chevron_rel );
			lp = (LayoutParams) backButton_rel.getLayoutParams();
			lp.width = (int) (temp * 34);
			backButton_rel.setLayoutParams( lp );

			shareScores = (ImageView) conteLayout.findViewById(R.id.shareScores);
			// Search Details
			setListeners();

			// set values
			setValues(null);
		} catch (Exception e) {
//			Logs.show(e);
		}
	}


	private boolean isuserAnonymous = false;

	/**
	 * set listeners
	 */
	private void setListeners() {

		try {
			backButton_rel.setOnClickListener( this );
			//home_imgView.setOnClickListener(this);
			homeImg.setOnTouchListener(homeTouchListener);
			notificationText.setOnClickListener(this);
			searchRelative.setOnTouchListener(homeTouchListener);

			friendSearch.setOnTouchListener(homeTouchListener);
			shareScores.setOnTouchListener( homeTouchListener );
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
	}

	/**
	 * showing/hiding the back chevron
	 * @param visibility - true/false
	 */
	private void showHideBackChevron ( boolean visibility ) {
		backButton_rel.setVisibility( ( visibility )? View.VISIBLE : View.GONE );
	}
	
	/**
	 * setting the color ,title,chevron,share icon,search spinner and search functionalities based on the current fragment 
	 * @param msg - vTitle,vMainColor,vMainTitleColor
	 */

	private void callUpdate ( Message msg ) {

		try {
			


			if (!Constants.isCurrent) {
				return;
			}
			
			
			
			

			String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();

			/**topFragmentName - eg:'AllSportsFragment%213456'
			 * splitting the topFragmentName in order to get the current screen name
			 */
			if(topFragmentName != null){
			String[] fragmentName = topFragmentName.split("%", 2);
			
			
			
			if(fragmentName != null && fragmentName.length > 0)
				topFragmentName = fragmentName[0];
			
			}
			
			//by default backChevron is made invisible
			showHideBackChevron ( false );

			title_img.setVisibility(View.INVISIBLE);

			// getting the top fragment name in order to cange the UI accordingly

			if (topFragmentName != null && topFragmentName.trim().length() > 0) {

				if ( topFragmentName.equalsIgnoreCase("ProviderFragment") || topFragmentName.equalsIgnoreCase("LoginWebViewFragment") 
						|| topFragmentName.equalsIgnoreCase("SplashFragment") ) {
					showHideFragment(false);
					return;
				} 
				showHideFragment(true);

				showDivider(true);
				showHideHomeButton(true);
				shareScores.setVisibility( View.GONE );


				
				//setting the color of the topFragment
				
				if ( msg != null && msg.getData() != null  ) {
					try {
						Bundle data = msg.getData();
						
						if ( data != null && (data.containsKey( "vMainColor" ) || data.containsKey( "vMainTitleColor" ))) {
							
							
							
							String vMainColor = null;
							String vMainTitleColor = null;

							if ( data.containsKey( "vMainColor" ) ) {
								vMainColor  = data.getString( "vMainColor" );
							}

							if ( data.containsKey( "vMainTitleColor" ) ) {
								vMainTitleColor  = data.getString( "vMainTitleColor" );
							}
							
							/*if(topFragmentName != null && topFragmentName.contains("MenuFragment")){
								
								vMainColor = null;
								vMainTitleColor = null;
								
							}*/
							
							
							
							/**incase the color is null/empty string,
							 * setting the default color to gray/green
							 */
							
							if(Constants.isGrayBar || topFragmentName.equalsIgnoreCase("PrivateLobbyFragment") || 
									topFragmentName.equalsIgnoreCase("PrivateLobbyRoomFragment") ||
									topFragmentName.equalsIgnoreCase("PrivateLobbyMessageFragment") || 
									topFragmentName.equalsIgnoreCase("CreateLobbyRoomFragment") ||
									topFragmentName.equalsIgnoreCase("PrivateLobbyInviteFriendFragment")
									||topFragmentName.equalsIgnoreCase("MenuFragment")){
								
								setTitleColor ( vMainTitleColor, vMainColor,false );
							}else{
								setTitleColor ( vMainTitleColor, vMainColor,true );
							}
						}
					} catch ( Exception e ) {
//						Logs.show ( e );
					}

				}
				
				
				

				if (topFragmentName.equalsIgnoreCase("MenuFragment")) {
					showImageTitle();
					showHideHomeButton(true);
					divider.setVisibility(View.GONE);
					showHideHomeButton(false);
					notificationText.setVisibility(View.GONE);
					showSearchButton(false);
					return;
				}if (topFragmentName.equalsIgnoreCase("CountriesFragment")) {
					showNotificationIcon();
					if(msg != null && msg.getData() != null && msg.getData().getString("vTitle") != null){
						showTitle(msg.getData().getString("vTitle"));
					}

					showSearchButton ( false );
					return;


				}

				if ( topFragmentName.equalsIgnoreCase ( "UpdateRegionFragment" ) ) {
					showNotificationIcon();
					showTitle( PlayUpActivity.context.getResources().getString(R.string.set_region));
					showSearchButton ( false );
					return;

				} 

				if ( topFragmentName.equalsIgnoreCase ( "CreateRoomFragment" ) ) {
					showNotificationIcon();
					showTitle( PlayUpActivity.context.getResources().getString(R.string.newHangout));
					showSearchButton ( false );
					return;

				} else if ( topFragmentName.equalsIgnoreCase ( "AllSportsFragment" ) ) {
					showNotificationIcon();
					showTitle( PlayUpActivity.context.getResources().getString(R.string.menu_sports));
					showSearchButton ( false );
					return;
				} else if ( topFragmentName.equalsIgnoreCase ( "WelcomeFragment" ) ) {
					showNotificationIcon();
					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("title")) {
								showTitle(data.get("title"));
							}
						} catch (Exception e) {
						}

					}
					showSearchButton ( false );
					return;
				}else if ( topFragmentName.equalsIgnoreCase ( "DefaultFragment" ) ) {
					showNotificationIcon();
					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("title")) {
								showTitle(data.get("title"));
							}
						} catch (Exception e) {
						}

					}
					showSearchButton ( false );
					return;
				} else  if (topFragmentName.equalsIgnoreCase("PrivateLobbyFragment")) {
					showNotificationIcon();
					
					

					showTitle(PlayupLiveApplication.getInstance().getString(R.string.home));
					showHideHomeButton(true);
					divider.setVisibility(View.VISIBLE);					
					showSearchButton(false);

					if(PrivateLobbyFragment.vContestId != null){

						shareScores.setVisibility( View.VISIBLE );
					}					



					return;
				}else  if (topFragmentName.equalsIgnoreCase("PrivateLobbyMessageFragment")) {


					showHideHomeButton(false);
					divider.setVisibility(View.INVISIBLE);		
					notificationText.setVisibility(View.GONE);
					showSearchButton(false);
					return;
				}else  if (topFragmentName.equalsIgnoreCase("CreateLobbyRoomFragment")) {
					

					showHideHomeButton(false);
					divider.setVisibility(View.INVISIBLE);		
					notificationText.setVisibility(View.GONE);
					showSearchButton(false);
					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("title")) {
								showSearchButton ( false );
								showTitle(data.get("title"));
							}
						} catch (Exception e) {
						}

					}
					return;



				}


				else if ( topFragmentName.equalsIgnoreCase ( "AboutFragment" ) ) {
					showNotificationIcon();
					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("title")) {
								showSearchButton ( false );
								showTitle(data.get("title"));
							}
						} catch (Exception e) {
						}

					}
					return;

				} else if (topFragmentName.equalsIgnoreCase("FriendsFragment") ||  topFragmentName.equalsIgnoreCase("InviteFriendFragment")
						|| topFragmentName.equalsIgnoreCase("PrivateLobbyInviteFriendFragment")){
					


					try {
						friendsSearchSpinner.setVisibility(View.INVISIBLE);
					} catch ( Exception e ) {

					}
					if ( isuserAnonymous ) {
						friendSearch.setVisibility( View.GONE);
						
					} else {
						friendSearch.setVisibility( View.VISIBLE);
					}

				
					
					if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "Dismiss progress" ) ) {
						friendsSearchSpinner.setVisibility(View.GONE);

						title.setVisibility(View.GONE);
						showHideHomeButton(false);

						searchView.setVisibility(View.VISIBLE);
						searchRelative.setVisibility(View.VISIBLE);

						friendSearch.setVisibility( View.GONE );
						notificationText.setVisibility( View.GONE );

						divider.setVisibility(View.GONE);

						leagueSearch.setVisibility(View.VISIBLE);
						leagueSearchEdittext.setVisibility(View.VISIBLE);

					} else if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "Show Friends" ) ) {
						showNotificationIcon();
						isSearchPressed = false;
						searchView.setVisibility(View.GONE);
						searchRelative.setVisibility(View.GONE);
						leagueSearchEdittext.removeTextChangedListener(friendTextWatcher);
						leagueSearchEdittext.setText("");
						friendSearch.setVisibility(View.VISIBLE);

						friendsSearchSpinner.setVisibility(View.GONE );

						showTitle( PlayUpActivity.context.getResources().getString(R.string.friends));

					} else {
						showNotificationIcon();
						searchView.setVisibility(View.GONE);
						searchRelative.setVisibility(View.GONE);

						if ( isuserAnonymous ) {
							friendSearch.setVisibility(View.GONE);
						} else {
							friendSearch.setVisibility(View.VISIBLE);
						}

						showTitle( PlayUpActivity.context.getResources().getString(R.string.friends));

					}
					return;

				}else if ( topFragmentName.equalsIgnoreCase( "PlayupFriendsFragment" )){
					showHideBackChevron ( true );
					isPlayupFriendsFragment = true;

					try {
						friendsSearchSpinner.setVisibility(View.INVISIBLE);
					} catch ( Exception e ) {

					}
					if ( isuserAnonymous ) {
						friendSearch.setVisibility( View.GONE);
						
					} else {
						friendSearch.setVisibility( View.VISIBLE);
					}

					if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "Dismiss progress" ) ) {
						friendsSearchSpinner.setVisibility(View.GONE);

						title.setVisibility(View.GONE);
						showHideHomeButton(false);
						showHideBackChevron ( false );
						searchView.setVisibility(View.VISIBLE);
						searchRelative.setVisibility(View.VISIBLE);

						friendSearch.setVisibility( View.GONE );
						notificationText.setVisibility(View.GONE);

						divider.setVisibility(View.GONE);

						leagueSearch.setVisibility(View.VISIBLE);
						leagueSearchEdittext.setVisibility(View.VISIBLE);

					} else if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase ( "Show Friends" ) ) {
						showNotificationIcon();
						isSearchPressed = false;
						searchView.setVisibility(View.GONE);
						searchRelative.setVisibility(View.GONE);
						leagueSearchEdittext.removeTextChangedListener(friendTextWatcher);
						leagueSearchEdittext.setText("");
						friendSearch.setVisibility(View.VISIBLE);

						friendsSearchSpinner.setVisibility(View.GONE );

						showTitle( PlayUpActivity.context.getResources().getString(R.string.friends));

					} else {
						showNotificationIcon();
						searchView.setVisibility(View.GONE);
						searchRelative.setVisibility(View.GONE);

						if ( isuserAnonymous ) {
							friendSearch.setVisibility(View.GONE);
						} else {
							friendSearch.setVisibility(View.VISIBLE);
						}

						showTitle( PlayUpActivity.context.getResources().getString(R.string.friends));

					}
					return;


				}else if ( topFragmentName.equalsIgnoreCase ( "NotificationFragment" ) ) {
					showNotificationIcon();
					showTitle( PlayUpActivity.context.getResources().getString(R.string.notifications));
					showSearchButton ( false );
					return;


				} else if ( topFragmentName.equalsIgnoreCase ( "MyProfileFragment" ) ) {
					showNotificationIcon();
					showSearchButton ( false );
					homeImg.setVisibility(View.VISIBLE);

					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("vUserName")) {

								showTitle(data.get("vUserName"));

							}
						} catch (Exception e) {
						}

					}
					return;


				} else if ( topFragmentName.equalsIgnoreCase ( "EditProfileFragment" ) ) {
					showNotificationIcon();
					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("vUserName")) {
								showSearchButton ( false );
								showTitle(data.get("vUserName"));
							}
						} catch (Exception e) {
						}

					}
					return;


				}else if ( topFragmentName.equalsIgnoreCase ( "WebViewFragment" ) ) {
					showNotificationIcon();
					showHideBackChevron(true);

					if(msg != null && msg.obj != null){
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("vTitle")) {

								showTitle(data.get("vTitle"));

							}

							
						} catch (Exception e) {

						}
					}




					return;


				} else if ( topFragmentName.equalsIgnoreCase ( "NewsFragment" ) ) {
					
					shareScores.setVisibility(View.GONE);
					showNotificationIcon();
					showHideBackChevron(true);
					showSearchButton ( false );
					if(msg != null && msg.obj != null){
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("vTitle")) {

								showTitle(data.get("vTitle"));
								return;
							}
						} catch (Exception e) {

						}
					}


					return;


				} 
				else if ( topFragmentName.equalsIgnoreCase ( "PublicProfileFragment" ) ) {
					

					showNotificationIcon();
					isSearchPressed = false;
					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("vUserName")) {
								showSearchButton ( false );
								showTitle(data.get("vUserName"));
							}
						} catch (Exception e) {
						}

					}
					return;


				} else if (topFragmentName.equalsIgnoreCase("MatchHomeFragment")) {
					showNotificationIcon();
					showHideBackChevron ( true );

					if(MatchHomeFragment.vContestId!= null)
						shareScores.setVisibility( View.VISIBLE );
					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("vConversationName")) {
								showSearchButton ( false );
								showTitle(data.get("vConversationName"));
								return;
							}
						} catch (Exception e) {

						}

					}
					title_img.setVisibility(View.INVISIBLE);
					title.setVisibility(View.VISIBLE);
					return;
				}else if (topFragmentName.equalsIgnoreCase("PrivateLobbyRoomFragment")) {

					showNotificationIcon();

					if(PrivateLobbyRoomFragment.vContestId != null)
						shareScores.setVisibility( View.VISIBLE );

					

					
					showHideBackChevron ( true );
					if (msg != null) {

						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("vConversationName")) {
								showSearchButton ( false );
								showTitle(data.get("vConversationName"));

							}


							return;
						} catch (Exception e) {
						}

					}			

					title_img.setVisibility(View.INVISIBLE);
					title.setVisibility(View.VISIBLE);
					return;
				} 
				else if ( topFragmentName.equalsIgnoreCase("DirectMessageFragment") ) {
					showNotificationIcon();
					isSearchPressed = false;
					showSearchButton(false);
					showHideBackChevron(true);
					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;

							if (data.containsKey("vFriendName")) {

								showTitle(data.get("vFriendName"));
								return;
							}
						} catch (Exception e) {



						}

					}
					title_img.setVisibility(View.INVISIBLE);
					title.setVisibility(View.VISIBLE);
					return;
				} else if ( topFragmentName.equalsIgnoreCase ( "LeagueSelectionFragment" )) {

					notificationText.setVisibility(View.INVISIBLE);

					divider.setVisibility(View.INVISIBLE);

					showHideHomeButton(false);
					showSearchButton(true);
					showHideBackChevron(true);

					if ( leagueSearchEdittext != null ) {
						if( leagueSearchEdittext.getVisibility() == View.VISIBLE )
							return;
						leagueSearchEdittext.setHint(R.string.findLeagues);
					}

					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("vSportsName")) {
								if (topFragmentName	.equalsIgnoreCase("MyLeagueSelectionFragment")) {
									showTitle( PlayUpActivity.context.getResources().getString(R.string.my_sports));
								} else
									showTitle(data.get("vSportsName"));
							}
						} catch (Exception e) {
						}
					}
					return;

				} else if ( topFragmentName.equalsIgnoreCase ( "WeeklyDetailsFragment" ) ) {

					title_img.setVisibility(View.INVISIBLE);
					notificationText.setVisibility(View.INVISIBLE);

					divider.setVisibility(View.INVISIBLE);

					showSearchButton(true);
					showHideHomeButton(false);

					if (leagueSearchEdittext != null) {
						if( leagueSearchEdittext.getVisibility() == View.VISIBLE )
							return;
						leagueSearchEdittext.setHint(R.string.findFixtures);
					}
					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("vCompetitionName")) {
								showTitle(data.get("vCompetitionName"));

								title.setVisibility(View.VISIBLE);
							}
						} catch (Exception e) {
						}

					}

					return;

				} else if (topFragmentName.equalsIgnoreCase("LiveSportsFragment")) {
					showNotificationIcon();
					showHideBackChevron ( true  );
					showSearchButton ( false );

					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("vSportsName")) {
								showTitle(data.get("vSportsName"));
							}
						} catch (Exception e) {
						}
					}
					title_img.setVisibility(View.INVISIBLE);
					title.setVisibility(View.VISIBLE);
					return;

				} else if (topFragmentName.equalsIgnoreCase ( "FixturesAndResultsFragment" ) ) {
					showNotificationIcon();
					showHideBackChevron ( true  );
					showSearchButton ( false );

					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if ( data != null && data.containsKey ( "vCompetitionName" ) ) {
								showTitle(data.get("vCompetitionName"));
								return;
							}
						} catch (Exception e) {
						}

						title_img.setVisibility(View.INVISIBLE);
						title.setVisibility(View.VISIBLE);
						return;

					} 


				}else if (topFragmentName.equalsIgnoreCase ( "LeagueLobbyFragment" ) ) {
					showNotificationIcon();
					showHideBackChevron ( true );
					showSearchButton ( false );

					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("vCompetitionName")) {
								showTitle(data.get("vCompetitionName"));
							}

						
						
						} catch (Exception e) {
						}



						title_img.setVisibility(View.INVISIBLE);
						title.setVisibility(View.VISIBLE);
						return;

					} 
				}
				else if(topFragmentName.equalsIgnoreCase ( "FriendsDirectMessageFragment" ) ){
					showNotificationIcon();
					showHideBackChevron ( true  );
					showSearchButton ( false );


					try {
						HashMap<String, String> data = (HashMap<String, String> )  msg.obj ;

						if ( data != null && data.containsKey("vFriendName")) {

							showTitle(data.get("vFriendName"));
							data.clear();
							data = null;
							return;
						}
						if ( data != null ) {
							data.clear();
							data = null;
						}

					} catch ( Exception e) {

					}

					return;

				}else if (topFragmentName.equalsIgnoreCase("MatchRoomFragment")) {

					showNotificationIcon();

					shareScores.setVisibility( View.VISIBLE );

					showHideBackChevron ( true  );
					showSearchButton ( false );

					showTitle( PlayUpActivity.context.getResources().getString(R.string.game_centre) );

					
					title_img.setVisibility(View.INVISIBLE);
					title.setVisibility(View.VISIBLE);
					return;

				}  else if ( topFragmentName.equalsIgnoreCase( "DirectConversationFragment" ) ) {
					showNotificationIcon();
					showHideBackChevron( false );
					showSearchButton( false );

					showTitle( getString(R.string.directMessages));

					title_img.setVisibility(View.INVISIBLE);
					title.setVisibility(View.VISIBLE);
					return;
				} else if (topFragmentName.equalsIgnoreCase("PostDirectMessageFragment")) {

					isSearchPressed = false;
					showHideBackChevron(false);
					showSearchButton(false);
					notificationText.setVisibility(View.INVISIBLE);
					divider.setVisibility(View.INVISIBLE);
					homeImg.setVisibility(View.INVISIBLE);
					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("vFriendName")) {

								showTitle(data.get("vFriendName"));
								return;
							}
						} catch (Exception e) {
						}
					}

					title_img.setVisibility(View.INVISIBLE);
					title.setVisibility(View.VISIBLE);
					return;
				}
				else if (topFragmentName.equalsIgnoreCase("PostMessageFragment")) {
					showNotificationIcon();
					isSearchPressed = false;
					showHideBackChevron(false);
					showSearchButton(false);


					if (msg != null) {
						try {
							HashMap<String, String> data = (HashMap<String, String>) msg.obj;
							if (data.containsKey("vConversationName")) {
								showTitle(data.get("vConversationName"));

								return;
							}
						} catch (Exception e) {
						}

					}
					title_img.setVisibility(View.INVISIBLE);
					title.setVisibility(View.VISIBLE);

					return;

				} else if ( topFragmentName.equalsIgnoreCase ( "TeamScheduleFragment") ) {
						showNotificationIcon();
						showSearchButton ( false );
						showHideBackChevron ( true  );
						if (msg != null) {
							try {
								HashMap<String, String> data = (HashMap<String, String>) msg.obj;
								if (data.containsKey("vTitle")) {	
									showTitle(data.get("vTitle"));
									return;
								}
							} catch (Exception e) {
							}

						}
						return;
					}
				}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show(e);
		}

	}
	/**
	 * setting the values
	 * 
	 * @param msg
	 */
	private void setValues(final Message msg) {

		Runnable r = new Runnable () {

			@Override
			public void run() {
				try {
					
				
					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					boolean isuserAnonymous_temp = dbUtil.isUserAnnonymous();

					if ( isuserAnonymous_temp != isuserAnonymous ) {
						isuserAnonymous = isuserAnonymous_temp;
						if ( PlayUpActivity.handler != null ) {
							PlayUpActivity.handler.post( new Runnable() {

								@Override
								public void run() {
									
									try {
										if(!isVisible() )
											return;

										callUpdate ( msg );
									} catch (Exception e) {
										// TODO Auto-generated catch block
										//Logs.show(e);
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
		};
		Thread th = new Thread ( r );
		th.start();

		callUpdate ( msg );

	}

	/**
	 * Setting Topbar fragment notifcation
	 */
	private void showNotificationIcon() {
		try{
		if ( isuserAnonymous ) {
			PreferenceManagerUtil preferenceManagerUtil = new PreferenceManagerUtil();
			if( preferenceManagerUtil.get ( Constants.IS_ANONYMOUS_NOTIFICATIONS_VIEWED, false)) {
				notificationText.setVisibility( View.GONE );
			} else {
				notificationText.setVisibility(View.VISIBLE);
				notificationText.setText("1");	
			}
			preferenceManagerUtil = null;
		} else {

			String topbarName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName() ;
			
			
			if(topbarName != null){
				String[] fragmentName = topbarName.split("%", 2);
				
				
				
				if(fragmentName != null && fragmentName.length > 0)
					topbarName = fragmentName[0];
				
				}

			if  ( topbarName != null && !topbarName.trim().toString().equalsIgnoreCase( "MenuFragment" ) ) {

				new Thread ( new Runnable () {

					@Override
					public void run() {

						try {
							DatabaseUtil dbUtil = DatabaseUtil.getInstance();
							int notificationCount = dbUtil.getUnReadNotificationCount ();
							String vUserId = dbUtil.getPrimaryUserId();
							int directMessagesCount = dbUtil.getUnReadDirectMessagesCount (vUserId);

							final int totalCount = notificationCount + directMessagesCount;

							if ( PlayUpActivity.handler != null ) {
								PlayUpActivity.handler.post( new Runnable () {

									@Override
									public void run() {
										
										try {
											if ( !isVisible() ) {
												return;
											}
											notificationText.setVisibility(View.VISIBLE );
											if ( totalCount > 99 ) {
												notificationText.setText( 99 + "+" );
											} else if ( totalCount > 9 ) {
												notificationText.setText( totalCount + "" );
											} else if( totalCount <= 0 ) {
												notificationText.setVisibility( View.GONE );
											} else {
												notificationText.setText( totalCount + "" );
											}
										} catch (Exception e) {
											// TODO Auto-generated catch block
//											Logs.show(e);
										}

									}

								});
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
//							Logs.show ( e );
						}


					}

				}).start();


			}


		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show ( e );
		}
	}

	/**
	 * showing and hiding the fragment
	 * 
	 * @param isVisible
	 */
	private void showHideFragment(boolean isVisible) {

		if (isVisible) {
			parentView.setVisibility(View.VISIBLE);
		} else {
			parentView.setVisibility(View.GONE);
		}
	}


	

	/**
	 * show / hide home button
	 * 
	 * @param isVisible
	 */
	private void showHideHomeButton(boolean isVisible) {

		homeImg.setVisibility((isVisible) ? View.VISIBLE : View.INVISIBLE);

	}

	/**
	 * setting the title and showing the title text instead of image in the
	 * title.
	 */
	private void showTitle(final String titleStr ) {
		title_img.setVisibility(View.INVISIBLE);
		title.setVisibility(View.VISIBLE);
		//title.setText ( titleStr );
		title.setText( new Util().getSmiledText(titleStr) );
	}

	/**
	 * setting the topBarColor and topBarTextColor
	 * 
	 * incase of null values,default color is set
	 * 
	 * @param titleTextColor
	 * @param topBarColor
	 * @param isGreen
	 */
	
	private void setTitleColor ( String titleTextColor, String topBarColor, boolean isGreen ) {

		try{
		if(titleTextColor != null && titleTextColor.trim().length() > 0){
			
			titleTextColor = titleTextColor.replace("0x", "#" );
		}
		
		if(topBarColor != null && topBarColor.trim().length() > 0){
			
			topBarColor = topBarColor.replace("0x", "#" );
		}
		
		
		
		if ( titleTextColor != null && titleTextColor.trim().length() > 0) {
			
				try {
					title.setTextColor( Color.parseColor( titleTextColor ) );
				} catch (Exception e) {
					
				
					
					title.setTextColor( Color.parseColor( "#FFFFFF" ) );
					
//					Logs.show(e);
				}
			
		} else {
			title.setTextColor( Color.parseColor( "#FFFFFF" ) );
		}

		if ( topBarColor != null && topBarColor.trim().length() > 0) {
			try {
				content_layout.setBackgroundColor( Color.parseColor( topBarColor ) );
			} catch (Exception e) {
				
				if(isGreen){
					
					content_layout.setBackgroundColor( Color.parseColor( "#46FF64" ) ) ;					
					} else {
						content_layout.setBackgroundColor( Color.parseColor( "#575757" ) ) ;
						

					}
				
//				Logs.show(e);
			}
		}
		else{
			
			if(isGreen){
				
				content_layout.setBackgroundColor( Color.parseColor( "#46FF64" ) ) ;					
				} else {
					content_layout.setBackgroundColor( Color.parseColor( "#575757" ) ) ;
					

				}
			
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show ( e );
		}

	}

	/**
	 * setting the image in the title
	 */
	private void showImageTitle() {

		title_img.setVisibility(View.VISIBLE);
		title.setVisibility(View.INVISIBLE);

	}

	/**
	 * Showing vertical divider
	 * 
	 * @param isVisible
	 */
	private void showDivider(boolean isVisible) {

		if (isVisible) {
			divider.setVisibility(View.VISIBLE);
		} else {
			divider.setVisibility(View.INVISIBLE);
		}

	}

	/**
	 * handling data if any change in database has occured.
	 */
	@Override
	public void onUpdate(final Message msg) {

		if ( PlayUpActivity.handler != null) {
			PlayUpActivity.handler.post(new Runnable() {

				@Override
				public void run() {
					try {
						
						if ( !isVisible()) {

							return;
						}
						
						

						if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("resetSearchValue")) {
							if (leagueSearchEdittext != null) {
								leagueSearchEdittext.setText("");
							}
							return;
						}

						if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("ShowFriendsSearch")) {
							friendSearch.setVisibility(View.VISIBLE);
							return;
						}

						// This is to avoid the Bounce effect when user returns from
						// provider screen.
						if (msg != null && msg.getData().getBoolean("ShowTopbar")) {
							showHideFragment(true);
							showHideHomeButton(false);
						}



						if ( notificationText != null ) {


							String topbarName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
							
							
							if(topbarName != null){
								String[] fragmentName = topbarName.split("%", 2);
								
								
								
								if(fragmentName != null && fragmentName.length > 0)
									topbarName = fragmentName[0];
								
								}


							if ( topbarName != null && topbarName.trim().length() > 0 && ( topbarName.equalsIgnoreCase( "InviteFriendFragment" ) || topbarName.equalsIgnoreCase( "FriendsFragment" ) ||
									topbarName.equalsIgnoreCase( "PlayupFriendsFragment" ) ||  topbarName.equalsIgnoreCase("PrivateLobbyInviteFriendFragment")) ) {


								if ( isSearchPressed ) {
									//setValues(msg);
									if ( msg != null && msg.obj != null && ( msg.obj.toString().equalsIgnoreCase( "Dismiss progress" ) || msg.obj.toString().equalsIgnoreCase( "Show Friends" ) ) ) {
										setValues(msg);
									}
								} else {
									setValues(msg);
								}
							} else {
								setValues(msg);
							}
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
	public void onClick(View v) {

		String topbarName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
		
		
		if(topbarName != null){
			String[] fragmentName = topbarName.split("%", 2);
			
			
			
			if(fragmentName != null && fragmentName.length > 0)
				topbarName = fragmentName[0];
			
			}
		
		
		
		Message msg;
		switch (v.getId()) {

		case R.id.topbar_home_imgView:
		case R.id.topbar_notification_text:

			if ( topbarName != null && topbarName.equalsIgnoreCase( "MenuFragment" ) ) {
			} else {
				handleHome();
			}
			break;
			

		case R.id.back_chevron_rel :
			msg = new Message();
			if ( topbarName != null && topbarName.equalsIgnoreCase( "MatchHomeFragment" )) {
				msg.obj = "callMatchRoomFragment";
				PlayupLiveApplication.callUpdateOnFragments( msg );
			} else if ( topbarName != null && topbarName.equalsIgnoreCase( "MatchRoomFragment" )) {
				msg.obj = "callPreviousFragment";
				PlayupLiveApplication.callUpdateOnFragments( msg );
			} else if ( topbarName != null && ( topbarName != null && ( topbarName.equalsIgnoreCase( "LiveSportsFragment" ) )
					) ) {
				msg.obj = "callChevron";
				PlayupLiveApplication.callUpdateOnFragments( msg );
			}  else if ( topbarName != null && topbarName.equalsIgnoreCase( "PlayupFriendsFragment" ) ) {
				msg.obj = "callChevron";
				PlayupLiveApplication.callUpdateOnFragments( msg );
			}else if ( topbarName != null && topbarName.equalsIgnoreCase( "PrivateLobbyRoomFragment" ) ) {
				msg.obj = "callPrivateLobbyFragment";
				PlayupLiveApplication.callUpdateOnFragments( msg );
			} 

			else if ( topbarName != null && topbarName.equalsIgnoreCase("DirectMessageFragment") ) {
				msg.obj = "callChevron";
				PlayupLiveApplication.callUpdateOnFragments( msg );
			}else if ( topbarName != null && ( topbarName.equalsIgnoreCase("LeagueLobbyFragment")  || topbarName.equalsIgnoreCase("WebViewFragment")
					||topbarName.equalsIgnoreCase("NewsFragment")	|| topbarName.equalsIgnoreCase("UpdateRegionFragment") || topbarName.equalsIgnoreCase("FixturesAndResultsFragment")
					||  topbarName.equalsIgnoreCase("LeagueSelectionFragment")  ||  topbarName.equalsIgnoreCase("TeamScheduleFragment")) ) {
				msg.obj = "callChevron";
				PlayupLiveApplication.callUpdateOnFragments( msg );
			}
			break;
			

		case R.id.shareScores:
			msg = new Message();
			msg.obj = "shareScores";
			PlayupLiveApplication.callUpdateOnFragments( msg );
			break;
		}
	}

	/**
	 * Displaying EditText on search button
	 */
	private void searchItems() {

		try {
			String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
			
			if(topFragmentName != null){
				String[] fragmentName = topFragmentName.split("%", 2);
				
				
				
				if(fragmentName != null && fragmentName.length > 0)
					topFragmentName = fragmentName[0];
				
				}
			
			boolean status = false;


			if(topFragmentName.equalsIgnoreCase("LeagueSelectionFragment")) {
				status= false;
			}


			if ( !status ) {

				title.setVisibility( View.INVISIBLE);
				leagueSearchEdittext.setVisibility(View.VISIBLE);
				searchView.setVisibility(View.VISIBLE);

				leagueSearchEdittext.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
				leagueSearchEdittext.setHintTextColor( PlayUpActivity.context.getResources().getColor(
						R.color.white));
				leagueSearchEdittext.requestFocus();


				PlayUpActivity.mBinder = leagueSearchEdittext.getWindowToken();
				InputMethodManager softKeyboard = (InputMethodManager) PlayupLiveApplication.getInstance() .getSystemService(Context.INPUT_METHOD_SERVICE);
				softKeyboard.showSoftInput( leagueSearchEdittext, 1 );


			}
		} catch (Exception e) {
//			Logs.show(e);
		}
	}



	private boolean isSearchPressed = false;
	/**
	 * displaying the search UI 
	 * @param isPressed
	 */
	private void friendSearchItems(boolean isPressed){


		try {
			isSearchPressed = isPressed;
			String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
			if(topFragmentName != null){
				String[] fragmentName = topFragmentName.split("%", 2);
				
				
				
				if(fragmentName != null && fragmentName.length > 0)
					topFragmentName = fragmentName[0];
				
				}
			
			
			boolean loadingData = true;
			if(topFragmentName.equalsIgnoreCase("FriendsFragment") )
				loadingData = FriendsFragment.loadingData;
			if(topFragmentName.equalsIgnoreCase("InviteFriendFragment"))
				loadingData = InviteFriendFragment.loadingData;

			if(topFragmentName.equalsIgnoreCase("PrivateLobbyInviteFriendFragment"))
				loadingData = PrivateLobbyInviteFriendFragment.loadingData;

			if(topFragmentName.equalsIgnoreCase("PlayupFriendsFragment"))
				loadingData = PlayupFriendsFragment.loadingData;



			if(topFragmentName.equalsIgnoreCase("FriendsFragment") 
					|| topFragmentName.equalsIgnoreCase("InviteFriendFragment") ||  topFragmentName.equalsIgnoreCase("PrivateLobbyInviteFriendFragment")
					|| topFragmentName.equalsIgnoreCase( "PlayupFriendsFragment")){


				
				//in case the data for the particular fragment is still loading,then search UI should not be shown

				if(isPressed && !loadingData){


					showHideBackChevron(false);

					friendSearch.setVisibility( View.GONE);

					title.setVisibility(View.GONE);
					showHideHomeButton(false);
					searchView.setVisibility(View.VISIBLE);
					searchRelative.setVisibility(View.VISIBLE);
					

					leagueSearch.setVisibility(View.VISIBLE);

					notificationText.setVisibility(View.GONE);
					divider.setVisibility(View.GONE);

					leagueSearchEdittext.setVisibility(View.VISIBLE);
					leagueSearchEdittext.removeTextChangedListener(filterTextWatcher);
					leagueSearchEdittext.setHint("");


					leagueSearchEdittext.requestFocus();


					PlayUpActivity.mBinder = leagueSearchEdittext.getWindowToken();
					InputMethodManager keyboard = (InputMethodManager) PlayupLiveApplication.getInstance()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
					
					keyboard.showSoftInput( leagueSearchEdittext, 0 );
					


					leagueSearchEdittext.addTextChangedListener(friendTextWatcher);

					Message msg = new Message();
					msg.obj = "inSearch";
					PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);

				}

			}
		} catch (Exception e) {
//			Logs.show(e);
		}

	}

	/**
	 * textListener for search editText 
	 */
	private TextWatcher friendTextWatcher = new TextWatcher() {



		@Override
		public void afterTextChanged(Editable s) {

			final String searchStr = s.toString();
			searchRelative.setVisibility(View.VISIBLE);
			leagueSearch.setVisibility( View.INVISIBLE);
			friendsSearchSpinner.setVisibility( View.VISIBLE);

			if( searchStr!=null ) {
				Message msg = new Message();
				msg.obj = "SearchFriendsString";
				Bundle bundle = new Bundle();
				bundle.putString("search_value", searchStr);
				msg.setData(bundle);
				PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {


		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {


		}
	};

	private void highlightDivider(View view, boolean istouched){

		try {
			if(istouched){
				showDivider(false);

				
				view.setBackgroundColor(Color.parseColor("#B0E6EE"));
			}else{
				showDivider(true);
				view.setBackgroundColor(Color.TRANSPARENT);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}

	}
	/**
	 * open home fragment.
	 */
	private void handleHome() {
		try {
			InputMethodManager imm = (InputMethodManager)PlayUpActivity.context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(PlayUpActivity.mBinder, 0);
			imm=null;
		}catch (Exception e) {
		}

		isSearchPressed = false;


		LinearLayout li = (LinearLayout) PlayUpActivity.context.findViewById( R.id.main );
		li.removeAllViews();
		li.bringToFront();
		
		PlayupLiveApplication.getFragmentManagerUtil().popBackStack();

		// start the transaction
		PlayupLiveApplication.getFragmentManagerUtil().startTransaction();

		PlayupLiveApplication.getFragmentManagerUtil().setFragment("TopBarFragment", R.id.topbar);
		PlayupLiveApplication.getFragmentManagerUtil().setFragment("MenuFragment");

		// end the transaction
		PlayupLiveApplication.getFragmentManagerUtil().endTransaction();

	}

	

	/**
	 * show/hide search button and related views
	 * 
	 * @param isVisisble
	 */
	
	private void showSearchButton(boolean isVisisble) {
		try {
			if (isVisisble) {

				leagueSearchEdittext.addTextChangedListener(filterTextWatcher);

				

				leagueSearch.setVisibility(View.VISIBLE);
				friendsSearchSpinner.setVisibility(View.GONE);
				searchRelative.setVisibility(View.VISIBLE);

			} else {

				try {
					leagueSearchEdittext.removeTextChangedListener(filterTextWatcher);
					leagueSearchEdittext.removeTextChangedListener(friendTextWatcher);
				} catch (Exception e) {

				}


				if ( PlayUpActivity.mBinder == null ) {
					PlayUpActivity.mBinder = leagueSearchEdittext.getWindowToken();
				}


				String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
				
				
				if(topFragmentName != null){
					String[] fragmentName = topFragmentName.split("%", 2);
					
					
					
					if(fragmentName != null && fragmentName.length > 0)
						topFragmentName = fragmentName[0];
					
					}
				
				
				
				if(topFragmentName.equalsIgnoreCase("PostDirectMessageFragment") || topFragmentName.equalsIgnoreCase("PostMessageFragment") 
						|| topFragmentName.equalsIgnoreCase("CreateRoomFragment")){

				}else{
					InputMethodManager inputManager = (InputMethodManager) PlayUpActivity.context
					.getSystemService(PlayUpActivity.context.INPUT_METHOD_SERVICE);
					inputManager.hideSoftInputFromWindow(  PlayUpActivity.mBinder, InputMethodManager.HIDE_NOT_ALWAYS);
					inputManager = null;
				}

				leagueSearchEdittext.setText("");

				searchView.setVisibility(View.GONE);
				searchRelative.setVisibility(View.GONE);
				leagueSearch.setVisibility(View.GONE);
				friendsSearchSpinner.setVisibility(View.GONE);
				friendSearch.setVisibility(View.GONE);
				leagueSearchEdittext.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show ( e );
		}
	}
	
	/**
	 * adding textChangeListener
	 */

	private TextWatcher filterTextWatcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {

			
			Message msg = new Message();
			Bundle bundle = new Bundle();
			
			bundle.putString("search_value", s.toString());
			msg.setData(bundle);
			PlayupLiveApplication.callUpdateOnFragmentsNotTopBar(msg);

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {



		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	};






	private OnTouchListener homeTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent event) {



			int coordinates[] = new int[2];
			view.getLocationOnScreen(coordinates);
			int viewX = coordinates[0];
			int viewY = coordinates[1];


			if (event.getAction() == MotionEvent.ACTION_DOWN) {

				outSide=false;
				if(view.getId() == R.id.topbar_home_imgView){
					highlightDivider(view,true);  
				}else if(view.getId() == R.id.searchRelative){
					friendSearch.setImageResource(R.drawable.search_button_d);
					searchItems();

				}else if(view.getId() == R.id.friendsSearch){

					ImageView img = (ImageView) view.findViewById(R.id.friendsSearch);
					img.setImageResource(R.drawable.search_button_d); 
					friendSearchItems(true);
				} else if( view.getId() == R.id.shareScores ) {
					shareScores.setImageResource( R.drawable.share_icon_d );
				}

			} else if (event.getAction() == MotionEvent.ACTION_UP) {

				if(view.getId() == R.id.topbar_home_imgView){
					highlightDivider(view,false);  
				}else if(view.getId() == R.id.searchRelative){
					ImageView img = (ImageView) view.findViewById(R.id.leagueSearch);					
					img.setImageResource(R.drawable.search_button);
					img = null;
					
				} else if (view.getId() == R.id.friendsSearch ){

					friendSearch.setImageResource(R.drawable.search_button); 
				} else if(view.getId() == R.id.shareScores ) {
					shareScores.setImageResource( R.drawable.share_icon );
				}
				if ((event.getRawX() > viewX && event.getRawX() < (viewX + view
						.getWidth()))
						&& (event.getRawY() > viewY && event.getRawY() < (viewY + view
								.getHeight()))) {
					if(!outSide)
						
						onClick(view);
				}
			} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				if (!((event.getRawX() > viewX && event.getRawX() < (viewX + view
						.getWidth())) && (event.getRawY() > viewY && event
								.getRawY() < (viewY + view.getHeight())))) {
					outSide=true;
					if(view.getId() == R.id.topbar_home_imgView){
						highlightDivider(view,false);  
					}else if(view.getId() == R.id.searchRelative){
						ImageView img = (ImageView) view.findViewById(R.id.leagueSearch);
						img.setImageResource(R.drawable.search_button);
						

					}else if (view.getId() == R.id.friendsSearch){
						
					} else if( view.getId() == R.id.shareScores ) {
						shareScores.setImageResource( R.drawable.share_icon );
					}
				}
			}
			return true;
		}
	};


}

