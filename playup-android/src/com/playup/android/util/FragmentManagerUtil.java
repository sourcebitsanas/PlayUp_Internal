package com.playup.android.util;


/***
 * @author vikky 

 * 
 * FragmentManager.java 
 *  For management of all the fragment in the application.
 *  
 *  Creation --- Created from the main activity and stored its instance on application class 
 *
 */
import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.BackStackEntry;
import android.support.v4.app.FragmentTransaction;


import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.fragment.MainFragment;


public class FragmentManagerUtil {

	public Hashtable< String , MainFragment > fragmentMap;

	private boolean waitForConfirmation = false;
	private FragmentTransaction ft;

	public FragmentManagerUtil () {
		fragmentMap = new  Hashtable < String , MainFragment > ();
	}


	/*
	 * For storing the fragment and also to determine if the fragment is already there in the stack or not. 
	 */
	public class FragmentHolder  {

		/**
		 * The fragment instance.
		 */
		public MainFragment fragment ;

		/**
		 * true - fragment is already there on back stack.
		 * false - not on back stack. 
		 */
		public boolean isActivated;

		/**
		 * 
		 */
		public String fragmentName;
	}

	
	/**
	 * check for the availibility of fragment in our application.
	 * @param fragmentName --- The class name of the fragment need to be checked
	 * @return -- If fragment Class exists in our application then instance of that fragment class else NULL 
	 * */ 
	private synchronized FragmentHolder checkForFragment ( String fragmentName ) {

		FragmentHolder fragmentHolder = new FragmentHolder();

		
		if ( fragmentMap != null && fragmentName != null && fragmentName.equalsIgnoreCase( "TopBarFragment" ) && fragmentMap.containsKey( fragmentName ) ) {
			fragmentHolder.fragment =  fragmentMap.get(fragmentName);
			fragmentHolder.isActivated = true;
			fragmentHolder.fragmentName = fragmentName;
			
			return fragmentHolder;
		}
		
/*		if ( fragmentMap.containsKey( fragmentName ) ) {
			
			fragmentHolder.fragment =  fragmentMap.get(fragmentName);
			fragmentHolder.isActivated = true;
			fragmentHolder.fragmentName = fragmentName;

		} else {
		*/
			try {

				// checking class if exists or not
				Class className = Class.forName( Constants.FRAGMENT_URI + fragmentName );
				if ( className != null ) {
					
					// getting the class's constructor to start class's instance.
					Constructor[] c = className.getDeclaredConstructors();
					if ( c != null && c.length > 0 ) {
						
						// getting the class's instance 
						fragmentHolder.fragment = (MainFragment) c[0].newInstance();
						
						if(fragmentName != null && 
								((fragmentName.equalsIgnoreCase("NewsFragment") && fragmentMap.containsKey("NewsFragment"))
								|| (fragmentName.equalsIgnoreCase("LeagueLobbyFragment") && fragmentMap.containsKey("LeagueLobbyFragment"))
								|| (fragmentName.equalsIgnoreCase("PublicProfileFragment") && fragmentMap.containsKey("PublicProfileFragment"))
								|| (fragmentName.equalsIgnoreCase("AllSportsFragment") && fragmentMap.containsKey("AllSportsFragment"))
								|| (fragmentName.equalsIgnoreCase("MatchRoomFragment") && fragmentMap.containsKey("MatchRoomFragment"))
								|| (fragmentName.equalsIgnoreCase("MatchHomeFragment") && fragmentMap.containsKey("MatchHomeFragment"))
								|| (fragmentName.equalsIgnoreCase("DirectConversationFragment") && fragmentMap.containsKey("DirectConversationFragment"))
								|| (fragmentName.equalsIgnoreCase("DirectMessageFragment") && fragmentMap.containsKey("DirectConversationFragment"))
								|| (fragmentName.equalsIgnoreCase("PlayupFriendsFragment") && fragmentMap.containsKey("PlayupFriendsFragment"))
								|| (fragmentName.equalsIgnoreCase("PrivateLobbyFragment") && fragmentMap.containsKey("PrivateLobbyFragment"))
								|| (fragmentName.equalsIgnoreCase("PrivateLobbyRoomFragment") && fragmentMap.containsKey("PrivateLobbyRoomFragment"))
								|| (fragmentName.equalsIgnoreCase("PrivateLobbyInviteFriendFragment") && fragmentMap.containsKey("PrivateLobbyInviteFriendFragment"))
								|| (fragmentName.equalsIgnoreCase("InviteFriendFragment") && fragmentMap.containsKey("InviteFriendFragment"))
								|| (fragmentName.equalsIgnoreCase("FriendsFragment") && fragmentMap.containsKey("FriendsFragment"))
								|| (fragmentName.equalsIgnoreCase("LeagueSelectionFragment") && fragmentMap.containsKey("LeagueSelectionFragment"))
								|| (fragmentName.equalsIgnoreCase("FixturesAndResultsFragment") && fragmentMap.containsKey("FixturesAndResultsFragment"))
								|| (fragmentName.equalsIgnoreCase("LiveSportsFragment") && fragmentMap.containsKey("LiveSportsFragment"))
								|| (fragmentName.equalsIgnoreCase("TeamScheduleFragment") && fragmentMap.containsKey("TeamScheduleFragment")))){
							
							
							UUID idOne = UUID.randomUUID();
							fragmentName = fragmentName + "%" + idOne;
						}
						
						


						// adding the fragment in hashmap for later management.
						fragmentMap.put ( fragmentName , fragmentHolder.fragment);
						fragmentHolder.isActivated = false;
						fragmentHolder.fragmentName = fragmentName;
					}
					c = null;
					className = null;
				}
				
			} catch ( Exception e ) {
				//  Logs.show(e);
			}
		// }
		
		return fragmentHolder;
	}

	/**
	 * start the fragment and display on UI screen
	 * @param fragmenr --- The fragment that need to be shown
	 */
	private void startFragment ( final FragmentHolder fragmentHolder, final Bundle args, int resourceId, boolean animate ) {

		
		InputMethodManager inputManager = (InputMethodManager) PlayupLiveApplication.getInstance() .getSystemService(Context.INPUT_METHOD_SERVICE);
		
		inputManager.hideSoftInputFromWindow(  PlayUpActivity.mBinder, InputMethodManager.HIDE_NOT_ALWAYS );
		
		
		if ( Constants.isCurrent ) {
			if ( ft == null ) {
				ft  = PlayupLiveApplication.getFragmentManager().beginTransaction();
			}
			if ( resourceId == -1 )
			{
				resourceId = R.id.main;
			}

		
			
			if ( animate ) { 

				Animation anim = AnimationUtils.loadAnimation(PlayUpActivity.context,R.anim.enter);
				// showing the league selection 
				if  ( resourceId == R.id.main ) {

					LinearLayout v = (LinearLayout) PlayUpActivity.context.findViewById( resourceId );
					v.bringToFront();
					v.setAnimation(anim);
					

				} 
				
				ft.setCustomAnimations(R.anim.enter, R.anim.exit );
			} 

			
			
			if ( fragmentHolder.isActivated ) {
				fragmentHolder.fragment.onAgainActivated ( args );
			} else if ( args != null ) {
				fragmentHolder.fragment.setArguments(args);
			}
		
			
			if ( PlayUpActivity.context != null && resourceId != -1 ) {
				LinearLayout li = (LinearLayout) PlayUpActivity.context.findViewById( resourceId );
				if ( li != null ) {
					li.removeAllViews();
				}
			}
			ft.replace ( resourceId , fragmentHolder.fragment );

			ft.addToBackStack( fragmentHolder.fragmentName );
			
			
			if ( !waitForConfirmation ) {
				
				ft.commit();
				updateTopBarFragment ( null );
				ft = null;
			}
		}

	}

	/**
	 * commiting the fragment transaction  
	 */
	private void commitFragmentTransaction () {


		if ( ft != null  && Constants.isCurrent  ) {

			int type = ft.commit();
			ft = null;
		}
	}



	/**
	 * removing the fragment from te stack .
	 * @param - Name of the fragment that needs to be removed. 
	 */
	public boolean removeFragment ( String fragmentName ) {

		FragmentTransaction ft  = PlayupLiveApplication.getFragmentManager().beginTransaction();
		

		if ( fragmentName != null && fragmentName.trim().length() > 0 ) {

			// checking if fragmentMap contains that fragment or not.
			if ( fragmentMap.containsKey( fragmentName ) ) {

				// if it contains then remove from the fragment stack and the map too.
				
				// ft.remove( fragmentMap.get( fragmentName ) );
				fragmentMap.remove( fragmentName );
				return true;
			}
		}
		fragmentName = null;
		ft.commit();
		ft = null;
		return false;
		// updateTopBarFragment ( null );

	}


	public void startTransaction () {
		if ( Constants.isCurrent  ) { 
			waitForConfirmation = true;
		}
	} 

	public void endTransaction () { 

		try {
			if ( Constants.isCurrent  ) {
				waitForConfirmation = false;
				commitFragmentTransaction ();
				updateTopBarFragment ( null );

			}
		} catch (Error e) {
			// TODO Auto-generated catch block
			
		}catch (Exception e) {
			// TODO: handle exception
			//Logs.show(e);
		}
	}


	

	public void popBackStack () {
		if ( Constants.isCurrent ) {
			FragmentManager fragmentManager = PlayupLiveApplication.getFragmentManager();
			fragmentManager.popBackStackImmediate( null, FragmentManager.POP_BACK_STACK_INCLUSIVE );
			updateTopBarFragment ( null );
			fragmentManager = null;
			
			Set< String > keys = fragmentMap.keySet();
			Iterator itr = keys.iterator();
			while ( itr.hasNext() ) {
				String key = (String) itr.next();
				MainFragment mf = fragmentMap.get( key );
				mf = null;
			}
			itr = null;
			fragmentMap.clear();
			
		}
	}

	public void popBackStack ( String fragmentName ) {

		try {
			if ( fragmentName != null && Constants.isCurrent ) {
				FragmentManager fragmentManager = PlayupLiveApplication.getFragmentManager();
				fragmentManager.popBackStackImmediate ( fragmentName, FragmentManager.POP_BACK_STACK_INCLUSIVE );
				
				
				int count = fragmentManager.getBackStackEntryCount();
				for ( int i = count - 1; i >= 0; i-- ) {
					BackStackEntry bEntry = fragmentManager.getBackStackEntryAt( i );
					
					
					
					
				}
				
				updateTopBarFragment ( null );
				fragmentManager = null;
				
				if ( fragmentMap != null && fragmentMap.containsKey( fragmentName ) ) {
//					MainFragment mf = fragmentMap.get( fragmentName );
////					mf = null;
					 fragmentMap.remove( fragmentName );
				}
				
			}

			fragmentName = null;
		} catch ( Exception e ) {
			//  Logs.show ( e ); 
		}
	}
	
	
	public void popBackStackImmediate () {

		try {
			
			String fragmentName = getTopFragmentName();
			
			if ( fragmentName != null && Constants.isCurrent ) {
				FragmentManager fragmentManager = PlayupLiveApplication.getFragmentManager();
				fragmentManager.popBackStackImmediate ();
				
				
				
				if ( fragmentMap != null && fragmentMap.containsKey( fragmentName ) ) {
					fragmentMap.remove(fragmentName);
				}
				
				
				updateTopBarFragment ( null );
				fragmentManager = null;
				
				
				
			}
			
			fragmentName = null;
		} catch ( Exception e ) {
			 /// Logs.show ( e ); 
		}
	}
	
	
	public void popBackStackImmediate (String fragmentName) {

		try {
			
			
			if ( fragmentName != null && Constants.isCurrent ) {
				FragmentManager fragmentManager = PlayupLiveApplication.getFragmentManager();
				fragmentManager.popBackStackImmediate (fragmentName,FragmentManager.POP_BACK_STACK_INCLUSIVE);
				
				
				if ( fragmentMap != null && fragmentMap.containsKey( fragmentName ) ) {
					fragmentMap.remove(fragmentName);
				}
				
				
				
				
				
				
				updateTopBarFragment ( null );
				fragmentManager = null;
				
				
				
			}

			fragmentName = null;
		} catch ( Exception e ) {
			 // Logs.show ( e ); 
		}
	}
	
	
	public void popBackStackTill ( String fragmentName ) {
		FragmentManager fragmentManager = PlayupLiveApplication.getFragmentManager();
		int count = fragmentManager.getBackStackEntryCount();

		int ID = -1;
		int pos = -1;
		for ( int i = count - 1; i >= 0; i-- ) {
			BackStackEntry bEntry = fragmentManager.getBackStackEntryAt( i );
			
			
			
			if ( bEntry.getName() != null && ID == -1 && pos == -1 ) {

				if ( bEntry.getName().equalsIgnoreCase( fragmentName ) ) {
					ID = bEntry.getId();
					pos = i;
				}
			}
			bEntry = null;
		}
		if ( ID == -1 ) {
			popBackStack ( fragmentName );
		} else {
			for ( int i = count - 1 ; i > pos; i-- ) {
				BackStackEntry bEntry = fragmentManager.getBackStackEntryAt( i );
				
				
//				MainFragment mf = fragmentMap.get( bEntry.getName() );
//				mf = null;
				
				
				
				 fragmentMap.remove(  bEntry.getName()  );	
				 
				bEntry = null;
			}
			
			fragmentManager.popBackStackImmediate( ID , 0 );
			
			updateTopBarFragment ( null );
		}
		fragmentManager = null;
	}

	public void popBackStackNotImmediate ( String fragmentName ) {

		if ( fragmentName != null && Constants.isCurrent ) {
			FragmentManager fragmentManager = PlayupLiveApplication.getFragmentManager();
			fragmentManager.popBackStack( fragmentName, 0 );
			
			if ( fragmentMap != null && fragmentMap.containsKey( fragmentName ) ) {
				MainFragment mf = fragmentMap.get( fragmentName );
				mf = null;
			//	fragmentMap.remove( fragmentName );
			}
			
			updateTopBarFragment ( null );
			fragmentManager = null;
			fragmentName = null;
		}
	}

	/**
	 * start the fragment with the given name.
	 * @param fragmentName -- the class name of fragment which needs to be started.
	 * @param args -- bundle that if needs to pass to the fragment
	 * @return boolean -- true if successfully started the fragment else false
	 */
	public boolean setFragment ( String fragmentName, Bundle args ) {
		
		FragmentHolder fragmentHolder = checkForFragment ( fragmentName );

		if ( fragmentHolder.fragment != null  ) {
			
			startFragment( fragmentHolder, args, -1, false );
			fragmentHolder = null;
			return true;

		}
		fragmentHolder = null;
		return false;
	}

	public boolean setFragment ( String fragmentName, boolean animate ) {

		FragmentHolder fragmentHolder = checkForFragment( fragmentName );

		if ( fragmentHolder.fragment != null  ) {

			startFragment( fragmentHolder, null, -1, animate );
			fragmentHolder = null;
			return true;

		}
		fragmentHolder = null;
		return false;
	}

	public boolean setFragment ( String fragmentName, Bundle args , int resourceId, boolean animate ) {

		FragmentHolder fragmentHolder = checkForFragment( fragmentName );

		if ( fragmentHolder.fragment != null  ) {

			startFragment( fragmentHolder, args, resourceId, animate );
			fragmentHolder = null;
			return true;

		}
		fragmentHolder = null;
		return false;
	}

	/**
	 * start the fragment with the given name.
	 * @param fragmentName -- the class name of fragment which needs to be started.
	 * @param args -- bundle that if needs to pass to the fragment
	 * @param resourceId -- the id of the resource view group/ view where the fragment has to be added or replaced.
	 * @return boolean -- true if successfully started the fragment else false
	 */
	public boolean setFragment ( String fragmentName, Bundle args, int resourceId ) {


		FragmentHolder fragmentHolder = checkForFragment( fragmentName );

		if ( fragmentHolder.fragment != null  ) {

			startFragment( fragmentHolder, args, resourceId, false );
			fragmentHolder = null;
			return true;

		}
		fragmentHolder = null;
		return false;
	}



	/**
	 * start the fragment with the given name.
	 * @param fragmentName -- the class name of fragment which needs to be started.
	 * @return boolean -- true if successfully started the fragment else false
	 */
	public boolean setFragment ( String fragmentName ) {

		return setFragment ( fragmentName, null, -1 );

	}


	/**
	 * start the fragment with the given name.
	 * @param fragmentName -- the class name of fragment which needs to be started.
	 * @param resourceId -- the id of the resource view group/ view where the fragment has to be added or replaced.
	 * @return boolean -- true if successfully started the fragment else false
	 */
	public boolean setFragment ( String fragmentName, int resourceId ) {

		return setFragment ( fragmentName, null, resourceId );

	}

	/**
	 * calls if refresh needs to be called due to some circumstances. 
	 */
	public void onUpdate ( Message msg ) {
		String fragmentName = getTopFragmentName();
		
		if (  fragmentMap != null && fragmentName != null ) {
		}
		
		if ( fragmentName != null && fragmentName.trim().length() > 0 && fragmentMap != null && fragmentMap.containsKey( fragmentName ) ) {
			
			
			
			// check for the fragment class if it exists in our application or not!
			MainFragment fragment = fragmentMap.get( fragmentName );

			// checks if fragment instance is created or not in FragmentHolder class. 
			if ( fragment != null  ) {
					// calls the onUpdate methods of the fragment.
				

				
					fragment.onUpdate(  msg );
					updateTopBarFragment ( msg );

			}
			fragmentName = null;
		}
	}


	/**
	 * calls if refresh needs to be called due to some circumstances. 
	 */
	public void onUpdateNotTopBar ( Message msg ) {
		String fragmentName = getTopFragmentName();
		
		if ( fragmentName != null && fragmentName.trim().length() > 0  && 
				fragmentMap != null && fragmentMap.containsKey( fragmentName ) ) {
			// check for the fragment class if it exists in our application or not!
			MainFragment fragment = fragmentMap.get( fragmentName );

			
			// checks if fragment instance is created or not in FragmentHolder class. 
			if ( fragment != null  ) {
					// calls the onUpdate methods of the fragment.
					fragment.onUpdate(  msg );
			}
		}
	}


	/**
	 * calls if device network connection is changed due to some problem. 
	 */
	public void onConnectionChanged (boolean isConnectionActive) {
		String fragmentName = getTopFragmentName();

		if ( fragmentName != null && fragmentName.trim().length() > 0 && fragmentMap != null && fragmentMap.containsKey( fragmentName )  ) {
			// check for the fragment class if it exists in our application or not!
			MainFragment fragment = fragmentMap.get( fragmentName );


			// checks if fragment instance is created or not in FragmentHolder class. 
			if ( fragment != null ) {

				// calls the onConnectionChanged methods of the fragment.
				fragment.onConnectionChanged(isConnectionActive);
			}
		}
		fragmentName = null;
	}


	/**
	 * Updating the top bar fragment for title change / notification / home ( visible/ gone ) 
	 */
	public void updateTopBarFragment ( Message msg ) {
		try {
			// call onUpdate of topbarfragment.
			
			
			if ( fragmentMap != null && fragmentMap.containsKey( "TopBarFragment" ) )  {
				MainFragment fragment = fragmentMap.get( "TopBarFragment" );
				if ( fragment != null ) {
				
					fragment.onUpdate( msg );
				}
			}
		} catch (Error e) {
			// TODO Auto-generated catch block
		//	Logs.show(e);
		}catch (Exception e) {
			// TODO: handle exception
			//Logs.show(e);
		}
		
		
	}

	/**
	 * to get the fragment whcih is at the top.
	 * @return - returns the fragment class name which is at the top of the fragment stack. 
	 */
	public String getTopFragmentName () {

		String fragmentName  = null;
		// get the number of stack for fragment.
		FragmentManager fragmentManager = PlayupLiveApplication.getFragmentManager();
		if ( fragmentManager != null ) {
			int backStackEntryCount = fragmentManager.getBackStackEntryCount();

			// if fragment stac contains atleast one fragment then call the topmost fragment's onUpdate method.
			if ( backStackEntryCount > 0 ) {

				// getting the backstackentry of the latest fragment added.
				BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt( backStackEntryCount - 1 );

				// getting the name of the fragment.
				fragmentName = backStackEntry.getName();
				backStackEntry = null;
			}
		}
		fragmentManager = null;
		return fragmentName;
	}


	/**
	 *  checking if the fragment exists in the fragment back stack or not.
	 */
	public boolean checkIfFragmentExists ( String fragmentName ) {

		FragmentManager fragmentManager = PlayupLiveApplication.getFragmentManager();
		if ( fragmentManager != null ) {
			int backStackEntryCount = fragmentManager.getBackStackEntryCount();

			// if fragment stac contains atleast one fragment then call the topmost fragment's onUpdate method.
			if ( backStackEntryCount > 0 ) {


				for ( int i = 0; i < backStackEntryCount; i++ ) {
					// getting the backstackentry of the latest fragment added.
					BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt( i );
					if ( backStackEntry.getName().equalsIgnoreCase( fragmentName ) ) {
						backStackEntry = null;
						fragmentManager = null;
						return true;
					} else {
						backStackEntry = null;
					}
					
				}
			}
		} 
		fragmentManager = null;
		return false;
	}



	public void updateFragmentList () {

		FragmentManager fragmentManager = PlayupLiveApplication.getFragmentManager();
		if ( fragmentManager != null ) {
			int backStackEntryCount = fragmentManager.getBackStackEntryCount();

			// if fragment stac contains atleast one fragment then call the topmost fragment's onUpdate method.
			if ( backStackEntryCount > 0 ) {

				for ( int i = 0; i < backStackEntryCount; i++ ) {

				}

				// getting the backstackentry of the latest fragment added.
				BackStackEntry backStackEntry = fragmentManager.getBackStackEntryAt( backStackEntryCount - 1 );

				// getting the name of the fragment.
				backStackEntry = null;

			}
		}
		fragmentManager = null;
	}
	
	
	
	/**
	 * calling refresh for polling
	 */
//	public void onRefresh ( Message msg ) {
//		String fragmentName = getTopFragmentName();
//
//		if (  fragmentMap != null && fragmentName != null ) {
//		}
//		
//		if ( fragmentName != null && fragmentName.trim().length() > 0 && fragmentMap != null && fragmentMap.containsKey( fragmentName ) ) {
//			MainFragment fragment = fragmentMap.get( fragmentName );
//			if ( fragment != null  ) {
//					fragment.onRefresh( msg );
//			}
//			fragmentName = null;
//		}
//	}



}