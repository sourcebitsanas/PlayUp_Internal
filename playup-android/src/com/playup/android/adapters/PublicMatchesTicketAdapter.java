package com.playup.android.adapters;

import java.util.Hashtable;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Color;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;

import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.DateUtil;
import com.playup.android.util.Logs;


public class PublicMatchesTicketAdapter extends BaseAdapter implements OnTouchListener {
	Hashtable<String, Hashtable<String, List<String>>> setBasedHomeScores;
	private Hashtable<String, Hashtable<String, List<String>>> setBasedAwayScores;

	private LayoutInflater inflater;
	private Hashtable<String, List<String>> data;

	private boolean isLoadingData = false;
	private String vContestId = null;
	private DateUtil dateUtil = new DateUtil();
	private boolean noMathes = false;
	// Hashtable<String, List<String>> leaderBoardData = null;

	Hashtable<String, Hashtable<String, List<String>>> leaderBoardData = null;

	private int selectedPosition;
	private Gallery matchGallery;
	protected boolean isGalleryScrolling;

	/**
	 * this will be called from match room and match home
	 * 
	 * @param vContestid
	 */
	public PublicMatchesTicketAdapter(String vContestId,Gallery matchGallery) {

		inflater = (LayoutInflater) PlayUpActivity.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.vContestId = vContestId;
		this.matchGallery = matchGallery;
		isLoadingData = true;

		selectedPosition = -1;
		matchGallery.setOnTouchListener(this);
		isGalleryScrolling = false;
		
		
		
		setValues();
	}

	
	
//	@Override
//	public void notifyDataSetChanged() {
//		try {
//
//			Log.e("123","isGalleryScrolling ----------------- > "+isGalleryScrolling);
//			
//			if (!isGalleryScrolling) {
//				super.notifyDataSetChanged();
//			}
//		} catch ( Exception e ) {
//
//
//		}
//
//	}


		
		
		
	
	public void setValues() {

		Runnable r = new Runnable() {

			@Override
			public void run() {
				try {
					DatabaseUtil dbUtil = DatabaseUtil.getInstance();
					setBasedHomeScores = new Hashtable<String, Hashtable<String, List<String>>>();
					setBasedAwayScores = new Hashtable<String, Hashtable<String, List<String>>>();
					data = dbUtil.getAssoicatedContestData(vContestId);

				
					

					if (PlayUpActivity.handler != null) {
						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {
								try {
									if (!Constants.isCurrent) {
										return;
									}
									isLoadingData = false;
									
									
									
									if(matchGallery != null )
										setSelectedPosition();
									PublicMatchesTicketAdapter.this.notifyDataSetChanged();
								} catch (Exception e) {
									Logs.show(e);
								}

							}

						});
					}
				} catch (Exception e) {
					Logs.show(e);
				}
			}

		};
		Thread th = new Thread(r);
		th.start();

	}

	private void getLiveMatchPosition(Hashtable<String, List<String>> data) {
		try {
			
			if ( data != null && data.containsKey("vContestId") && data.get("vContestId") != null &&
					data.get("vContestId").size() > 0 ){
				selectedPosition = 0;
				for (int position = 0; position < data.get("vContestId").size(); position++) {
					String output = dateUtil.Match_TimeRoomFragment(data.get("dStartTime").get(position),
									data.get("dEndTime").get(position),data.get("dScheduledStartTime").get(position));
					if (output == null) {
						selectedPosition = position;
						break;
					}
				}
			
			}
		}catch (Exception e) {
			Logs.show(e);
		}

	}

	/**
	 * this will be called from match room and match home
	 * 
	 * @param vContestid
	 */
	public void setData(String vContestid) {
		isLoadingData = true;
		this.vContestId = vContestid;
		
		
		setValues();
	}

	@Override
	public int getCount() {

		
		
		
		if (isLoadingData) {
			
			
			if(data == null || (data != null && data.containsKey("vContestId") && 
					data.get("vContestId") != null && data.get("vContestId").size() == 0)){
			noMathes = false;
			return 1;
			}
		}
		if (data != null && data.containsKey("vContestId")
				&& data.get("vContestId") != null) {
			if (data.get("vContestId").size() == 0) {
				noMathes = true;
				return 1;
			} else {
				noMathes = false;
				return data.get("vContestId").size();
			}

		} else {
			noMathes = true;
			return 1;
		}

	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}

	class ViewHolder {

		public TextView team1Name;
		public TextView team2Name;
		public TextView team1Score;
		public TextView team2Score;
		private ImageView leftDivider;
		private ImageView rightDivider;
		public TextView header;
		public TextView content;
		public TextView status;
		public LinearLayout baseView;
		public ImageView starImage;
		public LinearLayout scoresView;
		public LinearLayout setScoresView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder vh = null;
		if (convertView == null) {

			vh = new ViewHolder();

			if (noMathes) {
				convertView = inflater.inflate(
						R.layout.privatelobby_gallery_view, null);
				vh.header = (TextView) convertView.findViewById(R.id.header);
				vh.content = (TextView) convertView.findViewById(R.id.content);
				vh.starImage = (ImageView) convertView
						.findViewById(R.id.starImage);
			} else {
				convertView = inflater.inflate(R.layout.match_room_gallery_vew,
						null);
				vh.team1Name = (TextView) convertView
						.findViewById(R.id.team1Name);
				vh.team2Name = (TextView) convertView
						.findViewById(R.id.team2Name);
				vh.scoresView = (LinearLayout) convertView
						.findViewById(R.id.scoresView);
				vh.setScoresView = (LinearLayout) convertView
						.findViewById(R.id.setScoresView);
				vh.team1Score = (TextView) convertView
						.findViewById(R.id.team1Score);
				vh.team2Score = (TextView) convertView
						.findViewById(R.id.team2Score);
				vh.leftDivider = (ImageView) convertView
						.findViewById(R.id.leftDivider);
				vh.rightDivider = (ImageView) convertView
						.findViewById(R.id.rightDivider);
				vh.status = (TextView) convertView.findViewById(R.id.status);
				vh.status.setTextColor(Color.parseColor("#C90F26"));
				vh.team1Name.setTypeface(Constants.BEBAS_NEUE);
				vh.team2Name.setTypeface(Constants.BEBAS_NEUE);
				vh.team1Score.setTypeface(Constants.BEBAS_NEUE);
				vh.team2Score.setTypeface(Constants.BEBAS_NEUE);
				vh.status.setTypeface(Constants.BEBAS_NEUE);
				vh.baseView = (LinearLayout) convertView
						.findViewById(R.id.baseView);
			}

			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		try {

			if (isLoadingData) {

				if (vh.team1Name != null) {
					vh.team1Name.setVisibility(View.INVISIBLE);
					vh.team2Name.setVisibility(View.INVISIBLE);

					vh.scoresView.setVisibility(View.INVISIBLE);
					vh.setScoresView.setVisibility(View.INVISIBLE);
					vh.team1Score.setVisibility(View.INVISIBLE);
					vh.team2Score.setVisibility(View.INVISIBLE);
					vh.status.setVisibility(View.INVISIBLE);
					vh.rightDivider.setVisibility(View.GONE);
					vh.leftDivider.setVisibility(View.GONE);
				}

				return convertView;
			} else {

				if (noMathes) {
					vh.starImage.setVisibility(View.GONE);
					vh.header.setVisibility(View.VISIBLE);
					vh.content.setVisibility(View.GONE);
					convertView.setBackgroundColor(Color.parseColor("#8C9696"));
					vh.header.setText(R.string.noAdditionalMatches);
					vh.header.setTextColor(Color.parseColor("#FFFFFF"));
					vh.header.setTypeface(Constants.BEBAS_NEUE);
					return convertView;
				}

				if (vh.header != null) {
					vh.header.setVisibility(View.VISIBLE);
					vh.content.setVisibility(View.VISIBLE);
				}

				if (vh.team1Name != null) {
					vh.setScoresView.removeAllViews();
					vh.team1Name.setVisibility(View.VISIBLE);
					vh.team2Name.setVisibility(View.VISIBLE);
					vh.team1Score.setVisibility(View.VISIBLE);
					vh.team2Score.setVisibility(View.VISIBLE);
					vh.status.setVisibility(View.VISIBLE);
					vh.rightDivider.setVisibility(View.GONE);
					vh.leftDivider.setVisibility(View.GONE);
				}

			}

			if (position == 0) {
				vh.leftDivider.setVisibility(View.VISIBLE);
				vh.rightDivider.setVisibility(View.GONE);
				if (position == (getCount() - 1)) {
					vh.rightDivider.setVisibility(View.VISIBLE);
				}

			} else if (position == (getCount() - 1)) {
				vh.rightDivider.setVisibility(View.VISIBLE);
				vh.leftDivider.setVisibility(View.GONE);
			} else {
				vh.rightDivider.setVisibility(View.GONE);
				vh.leftDivider.setVisibility(View.GONE);
			}

			if (data != null) {

				vh.setScoresView.setVisibility(View.GONE);
				try {

					vh.team1Name.setVisibility(View.VISIBLE);
					vh.team2Name.setVisibility(View.VISIBLE);
					vh.scoresView.setVisibility(View.VISIBLE);

					vh.team1Score.setVisibility(View.VISIBLE);
					vh.team2Score.setVisibility(View.VISIBLE);
					vh.status.setVisibility(View.VISIBLE);

					vh.baseView.setBackgroundColor(Color.parseColor("#FF4754"));
					vh.status.setTextColor(Color.parseColor("#C90F26"));
					vh.team1Name.setTextColor(Color.parseColor("#FFCCCC"));
					vh.team2Name.setTextColor(Color.parseColor("#FFCCCC"));

					vh.team1Name.setText(data.get("vTeamName1").get(position));
					vh.team2Name.setText(data.get("vTeamName2").get(position));

					String output = null;
					try {
						output = dateUtil.Match_TimeRoomFragment(
								data.get("dStartTime").get(position),
								data.get("dEndTime").get(position),
								data.get("dScheduledStartTime").get(position));
					} catch (Exception e) {
						Logs.show(e);
					}

					String sportType = null;
					if (data.get("vSportType") != null
							&& data.get("vSportType").get(position) != null) {
						sportType = data.get("vSportType").get(position);
					}

					if (output == null) {

						vh.status.setText(data.get("vSummary").get(position));
					} else {
						if (output.equalsIgnoreCase("Completed")) {

							vh.status.setTextColor(Color.parseColor("#626A6A"));
							vh.baseView.setBackgroundColor(Color
									.parseColor("#8C9696"));
							vh.status.setText(PlayUpActivity.context
									.getResources().getString(
											R.string.completed));
							vh.team1Name.setTextColor(Color
									.parseColor("#FFFFFF"));
							vh.team2Name.setTextColor(Color
									.parseColor("#FFFFFF"));
						} else {
							vh.status.setText(output);
						}
					}

					if (output == null
							|| (output != null && output
									.equalsIgnoreCase("Completed"))) {

						if (sportType != null
								&& (sportType
										.equalsIgnoreCase(Constants.CRICKET) || sportType
										.equalsIgnoreCase(Constants.TEST_CRICKET))) {

							if (output == null) {
								if (data.get("vOvers").get(position) != null
										&& data.get("vOvers").get(position)
												.length() > 0) {
									vh.status.setText(R.string.over);
									vh.status.append(":"
											+ data.get("vOvers").get(position));
								} else {
									vh.status.setText(R.string.awaitingResults);
								}
							}

						}

						vh.team1Score.setText(data.get("vSummary1").get(
								position));
						vh.team2Score.setText(data.get("vSummary2").get(
								position));

						if (sportType != null
								&& (sportType.equalsIgnoreCase(Constants.AFL))) {
							vh.team1Score.setText(data.get("iTotal1").get(
									position));
							vh.team2Score.setText(data.get("iTotal2").get(
									position));
						}

						if ((data.get("vSummary1").get(position) == null || (data
								.get("vSummary1").get(position) != null && data
								.get("vSummary1").get(position).trim().length() == 0))
								&& (data.get("vSummary2").get(position) == null || (data
										.get("vSummary2").get(position) != null && data
										.get("vSummary2").get(position).trim()
										.length() == 0))) {

							try {
								String summary1 = DatabaseUtil.getInstance()
										.getContestSummaryFromSummaries(
												data.get("vHomeTeamId").get(
														position),
												data.get("vContestId").get(
														position));
								String summary2 = DatabaseUtil.getInstance()
										.getContestSummaryFromSummaries(
												data.get("vAwayTeamId").get(
														position),
												data.get("vContestId").get(
														position));
								vh.team1Score.setText(summary1);
								vh.team2Score.setText(summary2);

							} catch (Exception e) {
								Logs.show(e);
							}

						}

						if (sportType != null
								&& sportType
										.equalsIgnoreCase(Constants.SET_BASED_DATA)) {

							

							final String vContestId = data.get("vContestId")
									.get(position), vHomeTeamId = data.get(
									"vHomeTeamId").get(position), vAwayTeamId = data
									.get("vAwayTeamId").get(position);

							new Thread(new Runnable() {

								@Override
								public void run() {
									try {

										getSetBaseData(vContestId, vHomeTeamId,
												vAwayTeamId);

									} catch (Exception e) {
									}

								}
							}).start();

							
							if (setBasedHomeScores != null
									&& setBasedHomeScores
											.containsKey(vHomeTeamId)
									&& setBasedAwayScores != null
									&& setBasedAwayScores
											.containsKey(vAwayTeamId)){
					
					
					
					

								Hashtable<String, List<String>> summaryData1 = setBasedHomeScores.get(vHomeTeamId);

								Hashtable<String, List<String>> summaryData2 = setBasedAwayScores.get(vAwayTeamId);

								
								
								int vSummary_len1 = 0, vSummary_len2  = 0;
								if ( summaryData1 != null && summaryData1.get ( "vSummary" ) != null ) {
									vSummary_len1 = summaryData1.get ("vSummary").size();
								}
								if ( summaryData2 != null && summaryData2.get ( "vSummary" ) != null ) {
									vSummary_len2 = summaryData2.get ("vSummary").size();
								}
								int summary_len = ( vSummary_len1 > vSummary_len2 )?  vSummary_len1 : vSummary_len2;

								
								if(summary_len == 0){
									
									vh.scoresView.setVisibility(View.VISIBLE);
									vh.setScoresView.setVisibility(View.GONE);
									vh.team1Score.setText(data.get("vSummary1").get(position));
									vh.team2Score.setText(data.get("vSummary2").get(position));

									
								}
								
								else{
									
									vh.setScoresView.removeAllViews();
									vh.scoresView.setVisibility(View.GONE);
									vh.setScoresView.setVisibility(View.VISIBLE);
									
									for ( int i = 0; i < summary_len  ; i++ ) {
									
										LinearLayout set_item = (LinearLayout) inflater.inflate(R.layout.set_view,null);
										TextView setPoint1 = (TextView) set_item.findViewById(R.id.score1);
										TextView setPoint2 = (TextView) set_item.findViewById(R.id.score2);
										setPoint1.setTextAppearance(PlayUpActivity.context,R.style.SearchText);
										setPoint2.setTextAppearance(PlayUpActivity.context,R.style.SearchText);
										setPoint1.setTypeface(Constants.BEBAS_NEUE);
										setPoint2.setTypeface(Constants.BEBAS_NEUE);

										if (output == null) {
											//setPoint1.setTextColor(Color.parseColor("#FFCCCC"));
											//setPoint2.setTextColor(Color.parseColor("#FFCCCC"));
											setPoint1.setTextColor(Color.parseColor("#FFFFFF"));
											setPoint2.setTextColor(Color.parseColor("#FFFFFF"));

										} else if (output != null && output.equalsIgnoreCase("Completed")) {

											setPoint1.setTextColor(Color.parseColor("#FFFFFF"));
											setPoint2.setTextColor(Color.parseColor("#FFFFFF"));

										}
										
										
										if ( vSummary_len1 > 0 && vSummary_len1 > i ) {
											
											
											setPoint1.setText(summaryData1.get("vSummary").get(i));
										
										} else {
											setPoint1.setText( "" );
										}

										if ( vSummary_len2 > 0 && vSummary_len2 > i ) {

											
											
											setPoint2.setText(summaryData2.get("vSummary").get(i));
										
										} else {
											setPoint2.setText( "" );
										}

										vh.setScoresView.addView(set_item);
									}
								}
								
								



							} 
							else {
								
								vh.scoresView.setVisibility(View.VISIBLE);
								vh.setScoresView.setVisibility(View.GONE);
										
								vh.team1Score.setText(data.get("vSummary1").get(
										position));
								vh.team2Score.setText(data.get("vSummary2").get(
										position));

							}

						}

						if (sportType != null
								&& (sportType
										.equalsIgnoreCase(Constants.LEADERBOARD))) {
							try {

								final String vContestId = data
										.get("vContestId").get(position);

								new Thread(new Runnable() {

									@Override
									public void run() {
										try {
											Hashtable<String, List<String>> scoreData = DatabaseUtil
													.getInstance()
													.getLeaderBoardData(vContestId);

											if (scoreData != null
													&& scoreData.get("vSummary") != null
													&& scoreData.get("vSummary")
															.get(1) != null) {
												if( leaderBoardData == null )
													leaderBoardData = new Hashtable<String, Hashtable<String,List<String>>>();
												leaderBoardData.put(vContestId,scoreData);
												notifyDataSetChanged();

											}
										} catch (Exception e) {
											Logs.show(e);
										}

									}
								}).start();

								if (leaderBoardData != null
										&& leaderBoardData
												.containsKey(vContestId)) {

									Hashtable<String, List<String>> scoreData = leaderBoardData
											.get("vContestId");

									if (scoreData != null
											&& scoreData.get("vSummary") != null
											&& scoreData.get("vSummary").get(1) != null) {
										vh.team1Name.setText(scoreData.get(
												"vDisplayName").get(0));
										vh.team1Score.setText(scoreData.get(
												"vSummary").get(0));
										vh.team2Name.setText(scoreData.get(
												"vDisplayName").get(1));
										vh.team2Score.setText(scoreData.get(
												"vSummary").get(1));

									}

								}

							} catch (Exception e) {
								Logs.show(e);
							}

						}
					} else {

						if (data.get("vTeamName1").get(position) == null
								|| (data.get("vTeamName1").get(position) != null && data
										.get("vTeamName1").get(position).trim()
										.length() == 0)) {

							vh.team1Name.setText(data.get("vShortTitle").get(
									position));
							vh.team2Name.setText("");

						}

						if (sportType != null
								&& (sportType
										.equalsIgnoreCase(Constants.LEADERBOARD))) {
							try {

								final String vContestId = data
										.get("vContestId").get(position);

								new Thread(new Runnable() {

									@Override
									public void run() {
										try {
											Hashtable<String, List<String>> scoreData = DatabaseUtil
													.getInstance()
													.getLeaderBoardData(vContestId);

											if (scoreData != null
													&& scoreData.get("vSummary") != null && scoreData.get("vSummary").get(1) != null) {
												if( leaderBoardData == null )
													leaderBoardData = new Hashtable<String, Hashtable<String,List<String>>>();
												leaderBoardData.put(vContestId,scoreData);
												notifyDataSetChanged();

											}
										} catch (Exception e) {
											Logs.show(e);
										}

									}
								}).start();

								if (leaderBoardData != null && leaderBoardData.containsKey(vContestId)) {

									Hashtable<String, List<String>> scoreData = leaderBoardData.get("vContestId");

									if (scoreData != null && scoreData.containsKey("vContestId") && scoreData.get("vContestId").size() == 1) {
										vh.team1Name.setText(data.get("vShortTitle").get(position));
										vh.team2Name.setText("");
									} else {
										vh.team1Name.setText(scoreData.get("vDisplayName").get(0));
										vh.team2Name.setText(scoreData.get("vDisplayName").get(1));
									}
								}

							} catch (Exception e) {

							}

						}

						vh.team1Score.setText("");
						vh.team2Score.setText("");
					}

					if (data.get("iHasLiveUpdates").get(position) != null
							&& Integer.parseInt(data.get("iHasLiveUpdates")
									.get(position)) == 0) {

						boolean value = new DateUtil().isNotLiveUpdatesMatch(
								data.get("dStartTime").get(position),
								data.get("dEndTime").get(position),
								data.get("dScheduledStartTime").get(position));

						if (value) {

							try {
								vh.status.setText((PlayUpActivity.context
										.getResources()
										.getString(R.string.awaiting))
										.replaceAll("(\\r|\\n|\\t)", " "));
							} catch (Exception e) {
								Logs.show(e);
							}
						}
					}
				} catch (Exception e) {
					Logs.show(e);
				}
			}

		} catch (Exception e) {
			Logs.show(e);
		} catch (Error e) {
			Logs.show(e);
		}

		
		
		
		return convertView;
	}
	
	

	private void getSetBaseData(String vContestId, String vHomeTeamId,
			String vAwayTeamId) {
		try {
			Hashtable<String, List<String>> summaryData1 = PlayupLiveApplication
					.getDatabaseWrapper().select(
							" Select vSummary FROM summaries WHERE "
									+ " vContestId = \"" + vContestId
									+ "\" AND   vTeamId = \"" + vHomeTeamId
									+ "\" ORDER BY iPosition ASC");

			Hashtable<String, List<String>> summaryData2 = PlayupLiveApplication
					.getDatabaseWrapper().select(
							" Select vSummary FROM summaries WHERE "
									+ " vContestId = \"" + vContestId
									+ "\" AND   vTeamId = \"" + vAwayTeamId
									+ "\" ORDER BY iPosition ASC");

			if (summaryData1 != null && summaryData2 != null
					&& summaryData1.get("vSummary") != null
					&& summaryData2.get("vSummary") != null) {

				if (setBasedHomeScores != null && setBasedAwayScores != null) {

					setBasedHomeScores.put(vHomeTeamId, summaryData1);
					setBasedAwayScores.put(vAwayTeamId, summaryData2);
					if (PlayUpActivity.handler != null) {

						PlayUpActivity.handler.post(new Runnable() {

							@Override
							public void run() {

								try {
									notifyDataSetChanged();
								} catch (Exception e) {
									Logs.show(e);
								}
							}
						});
					}

				}

			}
		} catch (Exception e) {
			Logs.show(e);
		}
	}

	public void setSelectedPosition() {
		
		
		
		if(selectedPosition == -1){
			
			
			getLiveMatchPosition(data);
			matchGallery.setSelection(selectedPosition);
		}
		
	}
	
	
	

	public int getSelectedPosition() {
		if(selectedPosition == -1)
			return 0;
		else
		return selectedPosition;
	}



	
/**
 * on touch to track the gallery scrolling
 * while gallery in touch mode, gallery wont be notified,
 * after touch removed from gallery, after 2 seconds it will be notified to set the data which fetched during scrolling
 * 2 seconds is for gallery scroll time
 */

	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		if( event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE ) {	
		
			isGalleryScrolling = true;
			if( event.getAction() == MotionEvent.ACTION_DOWN ) {
				disableHandlers();
			}
			
		} else if( event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL ) {			
			cancleScroll();
		} 
		return false;
	}
	
	
	/**
	 * this handlers will handle for tracking scroll position.
	 */

	Handler mHandler;
	ScheduledExecutorService mUpdater;
	private void cancleScroll() {
		if (mUpdater != null) {
			return;
		}
		
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				isGalleryScrolling = false;
				notifyDataSetChanged();
				super.handleMessage(msg);
			}
		};
		mUpdater = Executors.newSingleThreadScheduledExecutor();
		mUpdater.schedule(new UpdateCounterTask(), 2000,TimeUnit.MILLISECONDS);
	}
	
	
	private void disableHandlers() {
		if(mUpdater!=null&&!mUpdater.isShutdown()){
			mUpdater.shutdownNow();
			mUpdater = null;
		}
	}
	
	private class UpdateCounterTask implements Runnable {
		public void run() {

			try {
				if ( mHandler != null  ) {
					mHandler.sendEmptyMessage(0);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Logs.show ( e );
			}
		}
	}
	
	
	@Override
	public void notifyDataSetChanged() {
		
		if( !isGalleryScrolling )
			super.notifyDataSetChanged();
	}
	

}