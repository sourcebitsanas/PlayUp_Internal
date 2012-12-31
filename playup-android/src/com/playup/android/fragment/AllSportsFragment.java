package com.playup.android.fragment;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.ImageDownloaderSports;
import com.playup.android.util.Logs;
import com.playup.android.util.Util;

/**
 * <P>
 * All Sports Fragment. <br>
 * Which shows all the sports as a squired tiles
 * </P>
 */
public class AllSportsFragment extends MainFragment implements  OnTouchListener {

	// user views
	private LinearLayout sportsBase;
	private LinearLayout progress_li;

	private ImageDownloaderSports imageDownloaderSports = null;


	private String vSecColor = null;
	private String vSecTitleColor = null;

	private boolean isAgain = false;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;

	private LayoutParams params, params1;
	private LayoutInflater inflater;
	private  Hashtable<String, List<String>> allSportsData;

	RelativeLayout content_layout ;
	android.view.ViewGroup.LayoutParams viewLayoutParams;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		content_layout = (RelativeLayout) inflater.inflate( R.layout.allsports, null);

		// setting the inflater

		if ( !isAgain ) {
			setFromFragment ( getArguments() );
		}
		return content_layout;
	}

	@Override
	public void onAgainActivated(Bundle args) {
		isAgain = true;
		setFromFragment( args );
	}

	
	/**
	 * Getting the values sent from previous fragment.
	 * @param bundle contains the individual items
	 */
	private void setFromFragment ( Bundle bundle ) {

		vMainColor = null;
		vMainTitleColor = null;
		
		vSecColor = null;
		 vSecTitleColor = null;
		
		 if (bundle != null &&bundle.containsKey("vMainColor")) {
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
	public void onDestroy () {
		super.onDestroy();

	}
	@Override
	public void onStop () {
		super.onStop();
		isAgain = false;
		
		

	}


	@Override
	public void onConnectionChanged(boolean isConnectionActive) {
		super.onConnectionChanged(isConnectionActive);

		if ( isConnectionActive ) {
			onResume();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if( !Util.isInternetAvailable() ) {
			PlayupLiveApplication.showToast( R.string.no_network );
		}
		
		if(!isVisible())
			return;
	
			try {

				if ( imageDownloaderSports == null ) {
					imageDownloaderSports = new ImageDownloaderSports ();
				}
				
				if ( inflater == null ) {
					this.inflater = LayoutInflater.from(PlayUpActivity.context);
				}
				
				if ( params == null ) {
					params = new LinearLayout.LayoutParams( LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1f);
					params.setMargins(4, 4, 4, 4);
				}
				
				if ( params1 == null ) {
					params1 = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);				
					params1.gravity =Gravity.CENTER; 
					params1.setMargins(0, 10, 0, 0);
				}

				initialize();

				// set values
				setValues();


			} catch (Exception e) {
				// TODO Auto-generated catch block
//				Logs.show(e);
			}


	}



	/**
	 * setting Topbar for Allsports Fragment
	 */
	private void setTopBar() {
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {

				try {

					Bundle b = new Bundle();
					b.putString("vMainColor", vMainColor);
					b.putString("vMainTitleColor", vMainTitleColor);
					Message msg = new Message();
					msg.setData(b);

					PlayupLiveApplication.callUpdateTopBarFragments(msg);
	
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}
				
			}
		}).start();
		
	}

	/**
	 * setting all the content in the views
	 */
	private void initialize() {

		// initialize views
		initializeViews();
	}

	/**
	 * Initialising views
	 * 
	 * @param content_layout
	 */
	private void initializeViews( ) {

		fetchAllSports();

		if ( content_layout == null ) {
			return;
		}
	
		LinearLayout sportsBase_progress_li = (LinearLayout) content_layout.findViewById( R.id.sportsBase_progress_li );
		progress_li = (LinearLayout) sportsBase_progress_li.findViewById( R.id.progress_li );

		sportsBase = (LinearLayout) sportsBase_progress_li.findViewById(R.id.sportsBase);
		sportsBase_progress_li = null;

	}



	/**
	 * setting the values
	 */
	private void setValues() {
		try{

			if(!isVisible()) {
				return;
			}
			
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
				
					

					try {

					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					 Hashtable<String, Object> result = dbUtil.getAllSportsURL(); 
					 if(result != null && result.containsKey("url") &&  result.containsKey("isHref") ){
							String allSports = (String) result.get("url");
							
							
							
							

							String vChildColor = dbUtil.getSectionMainColor("", allSports);
							String vChildTitleColor = dbUtil.getSectionMainTitleColor("", allSports);
							
							if(vChildColor != null && vChildColor.trim().length() > 0 )
								vMainColor = vChildColor;
							
							if(vChildTitleColor != null && vChildTitleColor.trim().length() > 0 )
								vMainTitleColor = vChildTitleColor;
								
							
							 String vChildSecondaryColor = dbUtil.getSectionSecondaryColor ( "", allSports );
							 String vChildSecondaryTitleColor = dbUtil.getSectionSecondaryTitleColor ( "", allSports );
							
							 
							 if(vChildSecondaryColor != null && vChildSecondaryColor.trim().length() > 0)
								 vSecColor = vChildSecondaryColor;
							 
							 if(vChildSecondaryTitleColor != null && vChildSecondaryTitleColor.trim().length() > 0)
								 vSecTitleColor = vChildSecondaryTitleColor;
							 if(PlayUpActivity.handler != null){
								 
								 PlayUpActivity.handler.post(new Runnable() {
									
									@Override
									public void run() {
										
										setTopBar();
									}
								});
								 
							 }

						}else{
							///////////////////////
						}
					
					} catch (Exception e) {
						// TODO Auto-generated catch block
//						Logs.show(e);
					}

					
				}
			}).start();
			
			
			showAvailableSports();
				
			
		}catch(Exception e){
//			Logs.show(e);
		}

	}


	/**
	 * Fetching Sports data from Database <br>
	 * And displaying as a squired tiles
	 */
	private void showAvailableSports() {

		new Thread( new Runnable() {

			@Override
			public void run() {

				try {
					// fetching the sports data from database.
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();

					if ( allSportsData != null ) {
						allSportsData.clear();
						allSportsData = null;
					}
					allSportsData = dbUtil.getAllSports();

					if ( PlayUpActivity.handler != null ) {
						PlayUpActivity.handler.post( new Runnable() {

							@Override
							public void run() {

								if ( !isVisible() ) {
									return;
								}
								try {
									
									if ( allSportsData != null && allSportsData.get("vSportsId") != null && allSportsData.get("vSportsId").size() > 0) {

										
										// remove all previously added sports

										sportsBase.removeAllViews();
										progress_li.setVisibility( View.GONE );
										sportsBase.setVisibility(View.VISIBLE );


										final int count = allSportsData.get("vSportsId").size();

										int len = count;
										
										
										if ( count % 2 != 0 ) {
											// getting the rows as it will be for each two sports
											len = (count +1 ) / 2 + (count +1 ) % 2;
										} else {
											// getting the rows as it will be for each two sports
											len = (count  ) / 2 + (count ) % 2;
										}


										for (int i = 0; i < len; i++) {

											// row linear layout
											LinearLayout sportsItemLayout = new LinearLayout( PlayUpActivity.context);

											for (int j = 0; j < 2; j++) {

												if (i * 2 + j < count) {

													// sports item layout
													LinearLayout lin = (LinearLayout) inflater.inflate( R.layout.mysports_item, null);

													lin.setId(i * 2 + j+1 );
													lin.setLayoutParams(params);

													if (allSportsData.get("iLiveNum") != null && allSportsData.get("iLiveNum").get(i * 2 + j) != null && Integer.parseInt( allSportsData.get("iLiveNum").get(i * 2 + j)) > 0)
														lin.setTag(-1,"live");
													else
														lin.setTag(-1,"notlive");

													// setting the sports name
													TextView sportName = (TextView) lin.findViewById(R.id.mysportsItemName);
													sportName.setText( allSportsData.get("vName").get(i * 2 + j).toUpperCase());
													sportName.setTypeface(Constants.OPEN_SANS_BOLD);

													sportName = null;

													// setting the sports Image
													ImageView sportsImage = (ImageView) lin	.findViewById(R.id.mysportsItemImage);
													sportsImage.setTag("0");
													sportsImage = null;

													imageDownloaderSports.download(  allSportsData.get("vLogoUrl").get( i * 2 + j), lin, false);

													lin.setTag( allSportsData.get("vSportsId").get(i * 2 + j));
													lin.setOnTouchListener ( AllSportsFragment.this );

													// adding sports item into the row layout
													sportsItemLayout.addView(lin);

													lin = null;
													// selectDeSelectState ( lin, false );
												} else {
													// Displaying tagline image
													LinearLayout lin = (LinearLayout) inflater.inflate( R.layout.tagline_view, null);
													lin.setLayoutParams(params);
													sportsItemLayout.addView(lin);
													lin = null;			

												}
											}

											// adding row layout on main layout
											sportsBase.addView(sportsItemLayout);
											sportsItemLayout = null;

										}

									} else {
									
										if( Util.isInternetAvailable() ) 
											progress_li.setVisibility( View.VISIBLE );
										else
											progress_li.setVisibility( View.GONE );
										sportsBase.setVisibility(View.GONE );
									}
								}catch(Exception e){

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

	/**
	 * handling data if any change in database has occured.
	 */
	@Override
	public void onUpdate(Message msg) {
		super.onUpdate(msg);
		try {
			
			//Handling back button pressed state
			if ( msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase( "handleBackButton" ) ) {
				PlayupLiveApplication.getFragmentManagerUtil().popBackStackImmediate();
			}
			
			//Setting the All sports data, after completion of Allsports API call
			if (msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("AllSportsResults")) {

				if ( PlayUpActivity.handler != null) {
					PlayUpActivity.handler.post(new Runnable() {

						@Override
						public void run() {

							try {

								if ( !isVisible() ) {
									return;
								}
								
								setValues();
							} catch (Exception e) {
//								Logs.show ( e );
							}
						}
					});
				}

			}
			
			if(msg != null && msg.obj != null && msg.obj.toString().equalsIgnoreCase("credentials stored")){
				
				fetchAllSports();
				
			}
		} catch (Exception e) {
//			Logs.show(e);
		}

	}

	/**
	 * showing selected / de selected state of the sports item
	 */
	private void selectDeSelectState(View v, boolean isSelected) {
		try{
		ImageView sportsImage = (ImageView) v.findViewById(R.id.mysportsItemImage);
		viewLayoutParams = v.findViewById(R.id.topLayout).getLayoutParams();
		if (sportsImage.getTag().toString().equalsIgnoreCase("1")) {
			if (isSelected) {
				v.findViewById(R.id.topLayout).setBackgroundColor(Color.parseColor("#B0E6EE"));
				((TextView) v.findViewById(R.id.mysportsItemName))
				.setTextColor(Color.WHITE);
			} else {
				v.findViewById(R.id.topLayout).setBackgroundResource(R.drawable.sport_base);
				((TextView) v.findViewById(R.id.mysportsItemName))
				.setTextColor(Color.parseColor("#404040"));
			}
			v.findViewById(R.id.topLayout).setLayoutParams(viewLayoutParams);
		}
		sportsImage = null;
		}catch(Exception e){
//			Logs.show(e);
		}
	}


	/**
	 * getting the all sports data
	 */
	private void fetchAllSports() {
		try{
		new Thread( new Runnable() {

			@Override
			public void run() {

				boolean credentialAvailable = DatabaseUtil.getInstance().isCredentialAvailable();
				if ( credentialAvailable 
						&& !Constants.isAllSportsDownloading  
						&& runnableList != null && !runnableList.containsKey ( Constants.GET_ALL_SPORTS ) 
						&& Util.isInternetAvailable() ){

					runnableList.put(Constants.GET_ALL_SPORTS,new Util().getAllSports(runnableList));

				}
			}
		}).start();
		
		}catch(Exception e){
//			Logs.show(e);
		}

	}

	private float rawX = 0;
	private float rawY = 0;
	long downTime = 0;

	@Override
	public boolean onTouch(final View v, MotionEvent event) {
		
		try{

		if (event.getAction() == MotionEvent.ACTION_DOWN) {

			downTime = event.getDownTime();
			rawX = event.getRawX();
			rawY = event.getRawY();
			// select
			startUpdating(v);
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			stopUpdating();
			if (event.getRawY() == rawY
					|| (event.getEventTime() - downTime) < 200) {
				selectDeSelectState(v, true);
			} else {
				// de select
				selectDeSelectState(v, false);
			}
			// clicked


			if (mHandler != null) {

				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						try {
							selectDeSelectState(v, false);
						} catch ( Exception e) {
//							Logs.show ( e );
						}
					}
				}, 100);
			}


			final String vSportsId = (String) v.getTag();
			clickOnSport(vSportsId );

		} else if (event.getAction() == MotionEvent.ACTION_CANCEL
				|| event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			stopUpdating();
			// de select
			selectDeSelectState(v, false);
		}

		if (event.getEventTime() > (downTime + Constants.highightDelay)) {

			if (event.getRawY() >= rawY - 10 && event.getRawY() <= rawY + 10) {

				stopUpdating();
				selectDeSelectState(v, true);
			}

		}
		}catch(Exception e){
//			Logs.show(e);
		}
		return true;
	}

	
	/**
	 * Tapping on any sport navigates to league selection fragment
	 * @param sportId tapped sport id
	 */
	private void clickOnSport( final String sportId ) {

		Runnable r = new Runnable() {

			@Override
			public void run() {

				try {
					FlurryAgent.onEvent("sport");

					if ( PlayUpActivity.handler != null ) {
						PlayUpActivity.handler.post( new Runnable() {

							@Override
							public void run() {

								if ( !isVisible() ) {
									// return;
								}

								Bundle bundle = new Bundle();
								bundle.putString("vSportsId", sportId);
								bundle.putString("fromFragment", "AllSportsFragment");									
								bundle.putString("vMainColor",vMainColor );
								bundle.putString("vMainTitleColor",vMainTitleColor );
								bundle.putString( "vSecColor",vSecColor );									
								bundle.putString( "vSecTitleColor",vSecTitleColor );

								cancelRunnable();
								PlayupLiveApplication.getFragmentManagerUtil().setFragment(
										"LeagueSelectionFragment", bundle);
							}
						});
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
//					Logs.show ( e );
				}

			}
		};
		Thread th = new Thread ( r );
		th.start();
	}
	
	/**
	 * 
	 * This is to handle custom selection.
	 */
	Handler mHandler;

	ScheduledExecutorService mUpdater;

	private void startUpdating(final View v) {
		try{
		if (mUpdater != null) {

			return;
		}
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				selectDeSelectState(v, true);
				super.handleMessage(msg);
			}
		};
		mUpdater = Executors.newSingleThreadScheduledExecutor();
		mUpdater.schedule(new UpdateCounterTask(), 200, TimeUnit.MILLISECONDS);
		}catch(Exception e){
//			Logs.show(e);
		}
	}

	
	/**
	 * stopping schedule executor service
	 */
	private void stopUpdating() {
		if (mUpdater != null && !mUpdater.isShutdown()) {
			mUpdater.shutdownNow();
			mUpdater = null;
		}
	}

	private class UpdateCounterTask implements Runnable {
		public void run() {
			try {
				mHandler.sendEmptyMessage(0);
			} catch (Exception e) {
				// TODO Auto-generated catch block
//				Logs.show ( e );
			}
		}
	}

	
}
