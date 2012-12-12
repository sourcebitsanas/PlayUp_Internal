package com.playup.android.adapters;

import java.util.Hashtable;

import java.util.List;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;

import com.playup.android.application.PlayupLiveApplication;


import com.playup.android.util.Logs;

import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

public class TeamScheduleAdapter extends BaseAdapter implements OnClickListener {

	private LayoutInflater inflater;
	private Hashtable<String, List<String>> data;

	boolean 	isListViewScrolling = false;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;
	private String vSecColor = null;
	private String vSecTitleColor = null;

	
	public TeamScheduleAdapter(Hashtable<String, List<String>> data, String vMainColor,String vMainTitleColor, String vSecColor, String vSecTitleColor ) {
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		
		this.vMainColor  = vMainColor;
		this.vMainTitleColor = vMainTitleColor;
		this.data = data;
		inflater = (LayoutInflater) PlayUpActivity.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	
	public void setData(Hashtable<String, List<String>> data, String vMainColor,String vMainTitleColor, String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;

		this.vMainColor  = vMainColor;
		this.vMainTitleColor = vMainTitleColor;
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


	}
	
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder vh = null;
		try {
			if ( inflater == null ) {
				LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
				return  layoutInflater.inflate( R.layout.team_schedule_header, null);
			}
			if (convertView == null) {

				vh = new ViewHolder();
				convertView = inflater.inflate(R.layout.team_schedule_header, null);
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}

				convertView.setId(position);
				convertView.setTag(R.id.about_txtview, data.get("vContestId")
						.get(position));
				convertView.setOnClickListener( this );
				showHeader( convertView , position);
				
			
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

				new TeamScheduleGenerator(data, matchHeader, position, vSecColor, vSecTitleColor );
		
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





	@Override
	public void onClick(View v) {
		try {
			String vContestId = v.getTag(R.id.about_txtview).toString();
			if( vContestId != null  ) {
				String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
				Bundle bundle = new Bundle();
				bundle.putString("vContestId", vContestId);
				bundle.putString("vMainColor",vMainColor );
				bundle.putString("vMainTitleColor",vMainTitleColor );
				bundle.putString("fromFragment", topFragmentName );
				bundle.putString( "vSecColor",vSecColor );			
				bundle.putString( "vSecTitleColor",vSecTitleColor );
				
				PlayupLiveApplication.getFragmentManagerUtil().setFragment("MatchRoomFragment", bundle);
			}
		} catch (Exception e) {
			Logs.show(e);
		}
	}

}
