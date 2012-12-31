package com.playup.android.fragment;

import java.util.Hashtable;
import java.util.List;

import android.os.Bundle;
import android.os.Message;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.DirectConversationAdapter;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

/**
 * Called from menu screen. <br>
 * Shows All the friends with recent messages
 */
public class DirectConversationFragment extends MainFragment  implements OnClickListener {


	private ListView directConversationListView;
	private View noDirectConversation;
	private View postButton;
	private RelativeLayout contentLayout;

	private DirectConversationAdapter adapter;
	private LinearLayout loadingView;
	private boolean isDownloaded = false;
	
	private boolean isAgainActivated = false;
	private String fromFragment = null;
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentLayout =  (RelativeLayout) inflater.inflate( R.layout.direct_conversation, null);
		
		if( !isAgainActivated ) {
			setFromFragment (getArguments());
		}
		return contentLayout;

	}
	
	/**
	 * getting the values sent from previous fragment
	 * @param bundle
	 */
	private void setFromFragment ( Bundle bundle ) {
		vMainColor = null;
		vMainTitleColor = null;
		
		vSecColor = null;
		 vSecTitleColor = null;		
		
		if(  bundle!=null && bundle.containsKey("fromFragment")) {
			fromFragment = bundle.getString("fromFragment");
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
	

	@Override
	public void onAgainActivated(Bundle args) {
		super.onAgainActivated(args);
		isAgainActivated = true;
		setFromFragment (args);
	}


	@Override
	public void onStop () {
		super.onStop();
	}

	@Override
	public void onResume () {
		super.onResume();
		
		adapter = null;
		isDownloaded = false;
		 
		// calling conversation API
		new Util().getDirectConversationData();
		
		initialise();
	}


	private void initialise() {
		setTopBarFragment();
		initialiseViews();
	}

	/**
	 * initializing views
	 */
	private void initialiseViews () {

		directConversationListView = (ListView) contentLayout.findViewById( R.id.friendMessageList );
		noDirectConversation = contentLayout.findViewById( R.id.no_direct_conversastion_li );
		postButton = contentLayout.findViewById( R.id.sendMessage );
		loadingView = (LinearLayout) contentLayout.findViewById(R.id.loadingView);
		noDirectConversation.setVisibility( View.VISIBLE );
		directConversationListView.setVisibility( View.GONE );
		
		postButton.setOnClickListener( this );
		showHideLoading( true );
		setValues();
	}

	
	/**
	 * setting topbar data
	 */
	private void setTopBarFragment() {
		
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					Hashtable<String, Object> result = dbUtil.getDirectConversationUrl ();
					String vDirectConversationUrl = null;
					if(result!=null && result.containsKey("url")&& result.get("url").toString().trim().length()>0){
						vDirectConversationUrl = (String) result.get("url");
					}else{
						vDirectConversationUrl = "";
					}
					
				
					
					
					String vChildColor = dbUtil.getSectionMainColor("", vDirectConversationUrl);
					String vChildTitleColor = dbUtil.getSectionMainTitleColor("", vDirectConversationUrl);
					
					if(vChildColor != null && vChildColor.trim().length() > 0 )
						vMainColor = vChildColor;
					
					if(vChildTitleColor != null && vChildTitleColor.trim().length() > 0 )
						vMainTitleColor = vChildTitleColor;
					
					String vChildSecondaryColor = dbUtil.getSectionSecondaryColor ("", vDirectConversationUrl );
					 String vChildSecondaryTitleColor = dbUtil.getSectionSecondaryTitleColor ( "", vDirectConversationUrl );
					
					 
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
//					Logs.show(e);
				}
			}
			
		}).start();
		
		
		
	}
	
	/**
	 * show/hide loading view
	 * @param show boolean
	 */
	private void showHideLoading( boolean show ) {
		try{
		if ( !Util.isInternetAvailable() )
			show = false;
		
		if( show ) {
			if( loadingView!=null ) {
				loadingView.setVisibility( View.VISIBLE );
			}
			if( directConversationListView!=null ) {
				directConversationListView.setVisibility(View.GONE);
			}
			if( noDirectConversation!=null ) {
				noDirectConversation.setVisibility(View.GONE);
			}	
				
		} else {
			
			if( loadingView!=null ) {
				loadingView.setVisibility( View.GONE );
			}
		}
		
		}catch(Exception e){
//			Logs.show(e);
		}
	}
	
	/**
	 * fetching data from database, and setting the on UI
	 */

	private void setValues () {

		new Thread( new Runnable() {
			
			@Override
			public void run() {
				
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					final Hashtable<String, List<String>> data = dbUtil.getDirectConversation ();
				
					if ( PlayUpActivity.handler != null ) {
						PlayUpActivity.handler.post( new Runnable( ) {
							
							@Override
							public void run() {
								
								try {
									if ( !isVisible() ) {
										return;
									}

									if ( data != null && data.get( "vDirectMessageId" ).size() > 0 ) {

										if ( adapter == null ) {
											adapter = new DirectConversationAdapter(data,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor);
											directConversationListView.setAdapter(adapter);
										} else {
											adapter.setData(data,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor);
										}
										showHideLoading( false );
										noDirectConversation.setVisibility( View.GONE );
										directConversationListView.setVisibility( View.VISIBLE );

									} else {
										
										 // If there is no data from server
										 // show no conversation view.
										 // otherwise show loading										
										
										if( isDownloaded ) {
											showHideLoading( false );
											noDirectConversation.setVisibility( View.VISIBLE );
											directConversationListView.setVisibility( View.GONE );
										} else {
											showHideLoading( true );
										}
												
									}
								} catch (Exception e) {
									// TODO Auto-generated catch block
//									Logs.show(e);
								}
								
							}
						});
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}
				
				
			}
		}).start();
		

	}


	@Override
	public void onClick(View v) {
		
		//Navigate to PlayupFriendsFragment
		Bundle bundle = new Bundle ();		
		String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();	
		bundle.putString("fromFragment", topFragmentName);
		bundle.putString( "vMainColor",vMainColor );							
		bundle.putString( "vMainTitleColor",vMainTitleColor );
		bundle.putString( "vSecColor",vSecColor );			
		bundle.putString( "vSecTitleColor",vSecTitleColor );
		PlayupLiveApplication.getFragmentManagerUtil().setFragment( "PlayupFriendsFragment",bundle );
	}


	@Override
	public void onUpdate(Message msg) {
		
		try {

			String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
			
			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "handleBackButton" ) ) {
				// handling back button pressed state
				if ( fromFragment != null ) {
					PlayupLiveApplication.getFragmentManagerUtil().popBackStack( topFragmentName );
				} else {
					PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( "MenuFragment" );
				}
				return;
			}
			
			/**
			 * Called after completion of DirectConversation  API call.
			 * Setting the conversation data and topbar
			 */
			if ( msg != null && msg.obj !=  null && 
					( msg.obj.toString().equalsIgnoreCase( "DirectConversation" ) || 
							msg.obj.toString().equalsIgnoreCase( "DirectConversation_Downloaded" ))  ) {
				isDownloaded = true;
				if ( PlayUpActivity.handler != null ) {
					PlayUpActivity.handler.post( new Runnable () {

						@Override
						public void run() {
							try {
								setTopBarFragment();
								setValues();
							} catch (Exception e) {
								// TODO Auto-generated catch block
//								Logs.show ( e );
							}
						}

					});
				}
			}
			
			 if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
				
				new Util().getDirectConversationData();
				
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
	}



}
