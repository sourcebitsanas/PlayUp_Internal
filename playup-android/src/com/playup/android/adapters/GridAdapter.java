package com.playup.android.adapters;

import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DateUtil;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;

public class GridAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private Hashtable<String, List<String>> data;

	ListView leagueBase;
	boolean 	isListViewScrolling = false;

	
	public android.widget.LinearLayout.LayoutParams headerParams = null;
	public String sportType = null;
	

	
	
	public GridAdapter(Hashtable<String, List<String>> data, ListView leagueBase ) {
		
		

		this.data = data;
		this.leagueBase = leagueBase;
		isListViewScrolling = false;
		this.leagueBase.setOnScrollListener(scrollListener);
		inflater = (LayoutInflater) PlayUpActivity.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	
	public void setData(Hashtable<String, List<String>> data ) {

		this.data = data;
		notifyDataSetChanged();
		
		
	}

	

	@Override
	public int getCount() {


		try {
		
			if (data != null && data.get("vContestId")!= null && data.get("vContestId").size() > 0 ) {
				return data.get("vContestId").size();
			}
		} catch (Exception e) {
			
			Logs.show(e);
		}
		
		return 0;
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

	
		private ImageView fixtureDivider;
		private ImageView bottomShadow;

	
	}
	
	public String getSportType( int position) {
		if( data!=null && data.get( "vSportType" )!=null )
			return data.get( "vSportType" ).get( position );	
		else
			return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder vh = null;
		sportType = getSportType(position);
		
		try {
			if ( inflater == null ) {
				LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				
				sportType = getSportType(position);
				if( sportType!= null && sportType.equalsIgnoreCase(Constants.LEADERBOARD)) 
					return  layoutInflater.inflate( R.layout.match_header_leaderboard, null);
				else if( sportType!= null && sportType.equalsIgnoreCase(Constants.SET_BASED_DATA)) 
					return  layoutInflater.inflate( R.layout.match_header_setbased, null);
				else	
					return  layoutInflater.inflate( R.layout.match_header_normal, null);
			}
			
			
			
			if (convertView == null) {

				vh = new ViewHolder();

				if(sportType != null){
				if( sportType.equalsIgnoreCase(Constants.LEADERBOARD)) 
					convertView = inflater.inflate( R.layout.match_header_leaderboard, null);
				else if( sportType.equalsIgnoreCase(Constants.SET_BASED_DATA)) 
					convertView = inflater.inflate( R.layout.match_header_setbased, null);
				else	
					convertView = inflater.inflate( R.layout.match_header_normal, null);
	
				
				vh.fixtureDivider = (ImageView) convertView.findViewById(R.id.fixtureDivider);
				vh.bottomShadow = (ImageView) convertView.findViewById(R.id.bottomShadow);
				
				convertView.setTag(R.id.addFriendIcon_dm,sportType);
				
				convertView.setTag(vh);
				
				}
			
			} else {
				vh = (ViewHolder) convertView.getTag();
	
				if(convertView.getTag(R.id.addFriendIcon_dm) != null && convertView.getTag(R.id.addFriendIcon_dm).toString().trim().length() > 0){
				
				String prevItemSportType = convertView.getTag(R.id.addFriendIcon_dm).toString();
				
				if(prevItemSportType != null && sportType != null && !prevItemSportType.equalsIgnoreCase(sportType)){

					if( sportType != null && sportType.equalsIgnoreCase(Constants.LEADERBOARD)) 
						convertView = inflater.inflate( R.layout.match_header_leaderboard, null);
					else if( sportType!= null && sportType.equalsIgnoreCase(Constants.SET_BASED_DATA)) 
						convertView = inflater.inflate( R.layout.match_header_setbased, null);
					else	
						convertView = inflater.inflate( R.layout.match_header_normal, null);			
					vh = new ViewHolder();
					
					vh.fixtureDivider = (ImageView) convertView.findViewById(R.id.fixtureDivider);
					vh.bottomShadow = (ImageView) convertView.findViewById(R.id.bottomShadow);
	
					convertView.setTag(R.id.addFriendIcon_dm,sportType);
					
					convertView.setTag(vh);			
					
				}
				
				}
				
				
				
			}
			
				
			
			
				convertView.setBackgroundColor(Color.parseColor("#F7F7F7"));
				showHeader( convertView , position);
				
				if( position != getCount()-1) {
					vh.fixtureDivider.setVisibility(View.VISIBLE);
					vh.bottomShadow.setVisibility(View.GONE);
					
				} else {
					vh.fixtureDivider.setVisibility(View.GONE);
					vh.bottomShadow.setVisibility(View.VISIBLE);
				}
				
				
		} catch (Exception e) {
			Logs.show(e);
		}

		return convertView;

	}

	

	
	/**
	 * showing data on match headers
	 * @param v
	 * @param position
	 */
	
	private void showHeader( View matchHeader, int position){
		
		try {

				new MatchHeaderGenerator( data, matchHeader, position, true);
		
	   } catch (Exception e) {
		
		Logs.show( e );
	  }
		



	}


	


//	@Override
//	public void notifyDataSetChanged() {
//
//		if (isListViewScrolling == false) {
//
//			super.notifyDataSetChanged();
//
//		} else {
//
//		}
//
//	}

	/**
	 * 
	 * Srolling of list view needs to be monitored, because if list view is
	 * scrolling dont notify the data set. It will slow down the scrolling
	 * effect
	 */
	ListView.OnScrollListener scrollListener = new ListView.OnScrollListener() {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,

		int visibleItemCount, int totalItemCount) {

			// TODO Auto-generated method stub

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

			// TODO Auto-generated method stub

			switch (scrollState) {

			case OnScrollListener.SCROLL_STATE_IDLE:

				isListViewScrolling = false;

				notifyDataSetChanged();

				break;

			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:

				isListViewScrolling = true;

				break;

			case OnScrollListener.SCROLL_STATE_FLING:

				isListViewScrolling = true;

				break;

			}

		}

	};





//	@Override
//	public void onClick(View v) {
//		String vContestId = v.getTag(R.id.about_txtview).toString();
//		if( vContestId != null  ) {
////			LinearLayout li = (LinearLayout) PlayUpActivity.context
////			.findViewById(R.id.main);
////
////			li.removeAllViews();
////			li.bringToFront();
////			li = null;
//			
//			Bundle bundle = new Bundle();
//			bundle.putString("vContestId", vContestId);
//			bundle.putString("vMainColor",vMainColor );
//			bundle.putString("vMainTitleColor",vMainTitleColor );
//			bundle.putString("fromFragment", "FixturesAndResultsFragment");
//			bundle.putString( "vSecColor",vSecColor );			
//			bundle.putString( "vSecTitleColor",vSecTitleColor );
//			
//			PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchRoomFragment", bundle);
//		}
//	}

}
