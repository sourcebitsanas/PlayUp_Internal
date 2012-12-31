package com.playup.android.fragment;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentManager.BackStackEntry;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.adapters.DirectMessageAdapter;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;
import com.playup.android.util.json.JsonUtil;


/**
 * Shows the conversation between user and his/her friend <br>
 * called from DirectConversationFragment, PostDirectMessageFragment ( After posting message )
 */
public class DirectMessageFragment extends MainFragment implements OnClickListener {

	private TimerTask refreshDirectMessagesTask;
	private Timer refreshDirectMessagesTimer;

	private RelativeLayout content_layout ;
	private ImageView sendMessage ; 
	private boolean isAgainActivated = false;
	public static  String vDirectConversationUrl = null;
	private String vUserSelfUrl = null;

	private DirectMessageAdapter messageAdapter = null;
	private ListView friendMessageList ;

	private RelativeLayout noConversationsView;
	private String fromFragment;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;

	private String vSecColor = null;
	private String vSecTitleColor = null;
	private boolean isvDirectConversationUrlHref; 
	boolean isUserSelfUrlHref ;
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		content_layout = (RelativeLayout) inflater.inflate( R.layout.friend_message, null);

		if( !isAgainActivated ) {
			setDirectConversationUrl (getArguments());
		}
		return content_layout;
	}



	@Override
	public void onResume() {
		super.onResume();

		Constants.inDirectMessageFragment = true;
		messageAdapter = null;

		PlayUpActivity.stopDirectConversation();


		
		makeApiCall();

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		if(vUserSelfUrl != null && vUserSelfUrl.trim().length() > 0){
			dbUtil.updateConversationUnreadCount2(vUserSelfUrl);

		}
		else if(vDirectConversationUrl != null && vDirectConversationUrl.trim().length() > 0){

			dbUtil.updateConversationUnreadCount(vDirectConversationUrl);

		}
		dbUtil.updateUserUnreadCount();
		dbUtil = null;

		refreshDirectMessages();

		initialise( );
		
		setValues();

//		deleteMarker();

	}
	
	private void makeApiCall(){
		try{
		
		Hashtable<String, Object> result = DatabaseUtil.getInstance().getDirectMessageUrl( vUserSelfUrl );
		if(result!=null){
			String vDirectMessageUrl = (String) result.get("url");

			if ( vDirectMessageUrl == null ) {
				getConversation();
			} else {
				getMessages();
			}

		}
		
		
		deleteMarker();
		}catch(Exception e){
//			Logs.show(e);
		}
		
	}


	@Override
	public void onPause() {

		super.onPause();

		/*InputMethodManager imm = (InputMethodManager)PlayUpActivity.context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(PlayUpActivity.mBinder, InputMethodManager.HIDE_NOT_ALWAYS);
		imm = null;*/
	}

	/**
	 * intializing the views
	 */
	private void  initialise (  ) {

		sendMessage = (ImageView) content_layout.findViewById(R.id.sendMessage);
		friendMessageList = ( ListView ) content_layout.findViewById(R.id.friendMessageList);
		noConversationsView =(RelativeLayout) content_layout.findViewById(R.id.noConversationsView);

		setListeners();
		
	}


	/**
	 * setting conversation url from bundle
	 * @param args
	 */
	private void setDirectConversationUrl(Bundle args) {
		try{
		vMainColor = null;
		vMainTitleColor = null;
		
		vSecColor = null;
		vSecTitleColor = null;
		
		isvDirectConversationUrlHref = false;
		isUserSelfUrlHref = false;
		

		if( args != null && args.containsKey("vDirectConversationUrl")) {
			vDirectConversationUrl = args.getString("vDirectConversationUrl");
		}
		
		
		if( args !=null && args .containsKey("vUserSelfUrl")) {
			vUserSelfUrl = args.getString("vUserSelfUrl");
		}
		
		if( args != null && args.containsKey("isvDirectConversationUrlHref")) {
			 isvDirectConversationUrlHref = args.getBoolean("isvDirectConversationUrlHref");
		}

		if( args !=null && args .containsKey("isUserSelfUrlHref")) {
			 isUserSelfUrlHref = args.getBoolean("isUserSelfUrlHref");
		}
		
		if( args !=null && args .containsKey("fromFragment")) {
			fromFragment = args.getString("fromFragment");
		}else{
			fromFragment = null;
		}if (args != null && args.containsKey("vMainColor")) {
			vMainColor = args.getString("vMainColor");
		}if (args != null && args.containsKey("vMainTitleColor")) {
			vMainTitleColor = args.getString("vMainTitleColor");
		}if (args != null && args.containsKey("vSecColor")) {
			vSecColor = args.getString("vSecColor");
		}if (args != null && args.containsKey("vSecTitleColor")) {
			vSecTitleColor = args.getString("vSecTitleColor");
		}
		
		
//		Log.e("234", "setDirectConversationUrl======>>>>vUserSelfUrl====>>>"+vUserSelfUrl);
		
//		Log.e("234", "setDirectConversationUrl======>>>>vDirectConversationUrl====>>>"+vDirectConversationUrl);
		
		
		}catch(Exception e){
//			Logs.show(e);
		}

	}



	/**
	 * Setting Topbar data
	 */
	private void setTopBarFragment (  ) {	
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					String vFriendName = "";		
					Cursor c = PlayupLiveApplication.getDatabaseWrapper().selectQuery( " SELECT vFriendUserName FROM playup_friends WHERE  vUserSelfUrl = \"" + vUserSelfUrl + "\" ");
					if ( c != null ) {

						if ( c.getCount() > 0)  {
							c.moveToFirst();
							vFriendName = c.getString(c.getColumnIndex("vFriendUserName"));
						}
						c.close();
						c = null;
					}

					
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					String vSelfUrl = "";
					if(vUserSelfUrl != null && vUserSelfUrl.trim().length() > 0){
						Hashtable<String, Object> result = dbUtil.getDirectMessageUrl(vUserSelfUrl);
						vSelfUrl = (String) result.get("url");
						
					}
					else if(vDirectConversationUrl != null && vDirectConversationUrl.trim().length() > 0){
						vSelfUrl = vDirectConversationUrl;
					}
					
	
					String vChildColor = dbUtil.getSectionMainColor("", vSelfUrl);
					String vChildTitleColor = dbUtil.getSectionMainTitleColor("", vSelfUrl);
					
					if(vChildColor != null && vChildColor.trim().length() > 0 )
						vMainColor = vChildColor;
					
					if(vChildTitleColor != null && vChildTitleColor.trim().length() > 0 )
						vMainTitleColor = vChildTitleColor;
					
					String vChildSecondaryColor = dbUtil.getSectionSecondaryColor ("", vSelfUrl);
					String vChildSecondaryTitleColor = dbUtil.getSectionSecondaryTitleColor ( "", vSelfUrl );
					
					 
					 if(vChildSecondaryColor != null && vChildSecondaryColor.trim().length() > 0)
						 vSecColor = vChildSecondaryColor;
					 
					 if(vChildSecondaryTitleColor != null && vChildSecondaryTitleColor.trim().length() > 0)
						 vSecTitleColor = vChildSecondaryTitleColor;
						
					
					HashMap< String, String > map = new HashMap<String, String>();
					map.put( "vFriendName", vFriendName );
					Bundle b = new Bundle();
					b.putString("vMainColor",vMainColor );
					b.putString("vMainTitleColor",vMainTitleColor );
					Message msg = new Message ();
					msg.setData(b);
					msg.obj = map;
					PlayupLiveApplication.callUpdateTopBarFragments(msg);
				} catch (Exception e) {
//					Logs.show(e);
				}

				
			}
		}).start();
		
		
	}


	@Override
	public void onAgainActivated(Bundle args) {
		super.onAgainActivated(args);
		isAgainActivated = true;
		setDirectConversationUrl (args);
	}




	/**
	 * setting listeners
	 */
	private void setListeners() {

		sendMessage.setOnClickListener(this);
	}


	/**
	 * setting data on UI.
	 */
	private void setValues() {
		try{
		
		if ( PlayUpActivity.handler != null ) {
			PlayUpActivity.handler.postDelayed( new Runnable() {


				@Override
				public void run() {
					try {
						setTopBarFragment();
					} catch (Exception e) {
						// TODO Auto-generated catch block
//						Logs.show ( e );
					}
				}
				
			}, 1000 );
		}
		setTopBarFragment();

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		Hashtable<String, List<String>> data = dbUtil.getDirectConversationMessages ( vUserSelfUrl );

		if ( data != null && data.get( "vDMessageId" ).size() > 0 ) {

			noConversationsView.setVisibility(View.GONE);
			friendMessageList.setVisibility(View.VISIBLE);

			if ( messageAdapter == null ) {

				messageAdapter = new  DirectMessageAdapter(data, vUserSelfUrl,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor,isUserSelfUrlHref );
				friendMessageList.setAdapter(messageAdapter);
			} else {
				messageAdapter.setData(data, vUserSelfUrl,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor,isUserSelfUrlHref  );  
			}

			return;
		} 
		if(data == null ||(data != null && data.get( "vDMessageId" ).size()  == 0) || (data != null && data.size()  == 0) ){

			noConversationsView.setVisibility(View.VISIBLE);
			friendMessageList.setVisibility(View.GONE);

			return;
		}
		
		}catch(Exception e){
//			Logs.show(e);
		}

	}


	private void getMessages() {
		try{

		if ( runnableList != null && !runnableList.containsKey(Constants.GET_DIRECT_MESSAGES ) ) {

			if (Util.isInternetAvailable()) {
				//Praveen : Cahnged
				Hashtable<String, Object> result 	=  DatabaseUtil.getInstance().getDirectMessageUrl( vUserSelfUrl );
				if(result!=null){
					String url = 	(String) result.get("url");
					Boolean isHref 	= (Boolean) result.get("isHref");
					runnableList.put(Constants.GET_DIRECT_MESSAGES, new Util().getDirectMessages(url,isHref, runnableList));
				}
				
			}
		}
		}catch(Exception e){
//			Logs.show(e);
		}
	}

	private void getConversation () {
		try{
		//Praveen : Changed
		if ( Util.isInternetAvailable()  ) {
			new Util().getDirectCoversation(vDirectConversationUrl, runnableList ,isvDirectConversationUrlHref);
		}
		}catch(Exception e){
//			Logs.show(e);
		}

	}


	@Override
	public void onUpdate ( final Message msg ) {

		super.onUpdate(msg);
		
		String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
		
		try {
			if ( msg != null && msg.obj != null ) {

				if( msg.obj.toString().equalsIgnoreCase( "callChevron" ) ) {

					int backStackCount = PlayupLiveApplication.getFragmentManager().getBackStackEntryCount();
					BackStackEntry entry1 = null;
					BackStackEntry entry2 = null;

					if ( backStackCount - 2 > -1 ) {
						entry1 = PlayupLiveApplication.getFragmentManager().getBackStackEntryAt( backStackCount - 2 );
					}
					if ( backStackCount - 3 > -1 ) {
						entry2 = PlayupLiveApplication.getFragmentManager().getBackStackEntryAt( backStackCount - 3 );
					}

					if ( entry1 != null && entry1.getName().contains( "DirectConversationFragment" ) ) {
						
						PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( entry1.getName() );
						entry1 = null;
						entry2 = null;
					} else {
						entry1 = null;
						if ( entry2 != null && entry2.getName().contains( "DirectConversationFragment" ) ) {
							
							PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( entry2.getName() );
							entry2 = null;
						} else {
							entry1 = null;
							entry2 = null;
							Bundle bundle = new Bundle ();
							bundle.putString( "fromFragment",topFragmentName  );
							bundle.putString( "vMainColor",vMainColor );							
							bundle.putString( "vMainTitleColor",vMainTitleColor );
							bundle.putString( "vSecColor",vSecColor );			
							bundle.putString( "vSecTitleColor",vSecTitleColor );
							PlayupLiveApplication.getFragmentManagerUtil().setFragment( "DirectConversationFragment", bundle );
						}
					}

					return;
				}
				if( msg.obj.toString().equalsIgnoreCase( "handleBackButton" ) ) {
				if( fromFragment!=null && fromFragment.contains("PlayupFriendsFragment") ) {
					PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( fromFragment );
					}else{

					PlayupLiveApplication.getFragmentManagerUtil().popBackStack( topFragmentName );
					}
				}if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
					
					makeApiCall();
					
				}


				
				if ( PlayUpActivity.handler != null ) {

					PlayUpActivity.handler.post ( new Runnable() {

						@Override
						public void run() {
							try {
								if ( msg.obj.equals ( "DirectConversation" ) ) {
									refreshDirectMessages();
									setValues();
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
//								Logs.show ( e );
							} 
						}
					});
				}

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			Logs.show(e);
		}
	}

	@Override
	public void onClick(View v) {
		try{
		String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
		
		switch (v.getId()) {
		
		
		
		case R.id.sendMessage:

			Bundle bundle =new Bundle();
			bundle.putString("vUserSelfUrl", vUserSelfUrl );
			bundle.putBoolean("isUserSelfUrlHref", isUserSelfUrlHref );
			Hashtable<String, Object> result = DatabaseUtil.getInstance().getDirectMessageUrl( vUserSelfUrl );
			if(result != null && result.containsKey("url") && result.containsKey("isHref")){
				bundle.putString("vDirectMessageUrl",(String) result.get("url") );
				bundle.putBoolean("isHrefUrl", (Boolean) result.get("isHref") );
			}
			
			bundle.putString( "fromFragment",topFragmentName  );
			bundle.putString("vMainColor",vMainColor );
			bundle.putString("vMainTitleColor",vMainTitleColor );
			
			bundle.putString( "vSecColor",vSecColor );			
			bundle.putString( "vSecTitleColor",vSecTitleColor );
			
			PostDirectMessageFragment.isHomeTapped = false;
			PlayupLiveApplication.getFragmentManagerUtil().setFragment( "PostDirectMessageFragment", bundle );
			break;
		}
		}catch(Exception e){
//			Logs.show(e);
		}
	}

	private  void putMarker () {
		try{

			
//			Log.e("123","put marker >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   ");
			
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		Hashtable<String, Object> result = dbUtil.getDirectMessageUrl( vUserSelfUrl );
		
//		Log.e("123","put marker result >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   "+result);
		 
		String vDirectMessageId = dbUtil.getDirectMessageId ((String) (result.get("url")) );
		
//		Log.e("123","put marker vDirectMessageId >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   "+vDirectMessageId);

		ContentValues values = new ContentValues();
		values.put( "iUnreadCount",  0 );
		JsonUtil json  = new JsonUtil();

		json.queryMethod1( Constants.QUERY_UPDATE, null, "direct_conversation", values, " vDirectMessageId = \"" + vDirectMessageId + "\" ", null, false, false );


		Hashtable<String, Object> result1 = DatabaseUtil.getInstance().getDirectMarkerUrl ( vDirectConversationUrl, vUserSelfUrl );
		String vDirectMarkerUrl =(String) result1.get("url");
		Boolean isHref  =(Boolean) result1.get("isHref");
		
		
//		Log.e("123","put marker vDirectMarkerUrl >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>   "+vDirectMarkerUrl);

		new Util().putDirectDeleteMarker ( true , vDirectMarkerUrl,isHref );
		}catch(Exception e){
//			Logs.show(e);
		}
	}


	private void deleteMarker () {
		try{
		// update the count in recent activity.

		Hashtable<String, Object> result = DatabaseUtil.getInstance().getDirectMarkerUrl ( vDirectConversationUrl, vUserSelfUrl );
		String vDirectMarkerUrl = (String) result.get("url");
		Boolean isHrefUrl = (Boolean) result.get("isHref");
		new Util().putDirectDeleteMarker ( false , vDirectMarkerUrl,isHrefUrl );
		
		}catch(Exception e){
//			Logs.show(e);
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		
		if ( refreshDirectMessagesTask != null ) {
		refreshDirectMessagesTask.cancel();
		refreshDirectMessagesTask = null;
	}

	if ( refreshDirectMessagesTimer != null ) {
		refreshDirectMessagesTimer.cancel();
		refreshDirectMessagesTimer = null;
	}
		
		Constants.inDirectMessageFragment = false;
		putMarker();
		PlayUpActivity.refreshDirectConversation();


		//		friendMessageList = null;
//		if ( messageAdapter != null )  {
//			messageAdapter.close();
//			messageAdapter = null;
//		}
//



		

	}

	/**
	 * refreshing direct messages
	 */
	private void refreshDirectMessages() {

		try{
		Hashtable<String, Object>  result = DatabaseUtil.getInstance().getDirectMessageUrl( vUserSelfUrl );
		final String url = (String )result.get("url");
		final Boolean isHref = (Boolean)result.get("isHref");
		int cacheTime = Integer.parseInt( DatabaseUtil.getInstance().getCacheTime(url));

		
		
		
		if ( refreshDirectMessagesTimer != null ) {
			refreshDirectMessagesTimer.cancel();
			refreshDirectMessagesTimer = null;
		}
		refreshDirectMessagesTimer = new Timer();

		if ( refreshDirectMessagesTask != null ) {
			refreshDirectMessagesTask.cancel();
			refreshDirectMessagesTask = null;
		}

		refreshDirectMessagesTask = new TimerTask() {
			@Override
			public void run() {

				try {
					if( runnableList != null && !runnableList.containsKey(Constants.GET_DIRECT_MESSAGES )  ){

						if ( Util.isInternetAvailable() ) {
							/*Hashtable<String, Object>  result = DatabaseUtil.getInstance().getDirectMessageUrl( vUserSelfUrl );
							String url = (String )result.get("url");
							Boolean isHref = (Boolean)result.get("isHref");*/
							//Praveen : Changed
							runnableList.put(Constants.GET_DIRECT_MESSAGES, new Util().getDirectMessages(url, isHref , runnableList));

						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}
			}
		};

		
		if ( cacheTime > 0 ) {

			refreshDirectMessagesTimer.schedule(refreshDirectMessagesTask, ( cacheTime * 1000), ( cacheTime * 1000));
		} else {

			if ( refreshDirectMessagesTimer != null ) {
				refreshDirectMessagesTimer.cancel();
				refreshDirectMessagesTimer = null;
			}

			if ( refreshDirectMessagesTask != null ) {
				refreshDirectMessagesTask.cancel();
				refreshDirectMessagesTask = null;
			}

		}
		}catch(Exception e){
//			Logs.show(e);
		}

	}

}
