package com.playup.android.adapters;

import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.util.Constants;
import com.playup.android.util.DateUtil;

public class FixtureGallery extends BaseAdapter{

	private Hashtable<String, List<String>> weekData;
	private LayoutInflater inflater;
	private DateUtil dateUtil;

	
	class ViewHolder {

		public TextView weekNumber;
		public TextView period;
		public TextView periodStart;
		public TextView periodEnd;
	
	}
	
	public FixtureGallery(Hashtable<String, List<String>> data) {
	
		this.weekData = data;
		inflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		dateUtil= new DateUtil();
		
	}

	
	public void setData(Hashtable<String, List<String>> data) {
		this.weekData = data;
		notifyDataSetChanged();
		
	}
	
	
	@Override
	public int getCount() {
		if(weekData != null && weekData.get("vPeriod") != null && weekData.get("vPeriod").size() > 0){
			return weekData.get("vPeriod").size();
		}
		
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		
		if ( inflater == null ) {
			LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return  layoutInflater.inflate( R.layout.fixture_gallery_item, null);
		}
		ViewHolder vh;
		if ( convertView == null ) {
			vh = new ViewHolder();
			convertView = inflater.inflate( R.layout.fixture_gallery_item, null); 

			vh.weekNumber = (TextView) convertView.findViewById( R.id.weekNumber );
			vh.period = (TextView) convertView.findViewById( R.id.period );
			vh.periodStart = (TextView) convertView.findViewById( R.id.weekStart );
			vh.periodEnd = (TextView) convertView.findViewById( R.id.weekEnd );
			

			setTypeFaces( vh.weekNumber, vh.period, vh.periodStart,vh.periodEnd );

			convertView.setTag( vh );

		} else  {
			vh = (ViewHolder) convertView.getTag();
		}

		convertView.setBackgroundResource(0);
		vh.period.setText( weekData.get( "vPeriod" ).get( position ) );
		vh.weekNumber.setText( weekData.get( "vName" ).get( position ) );
		
		/**
		 * Date:18/07/2012
		 * Sprint:20
		 *  */
		//String date = dateUtil.conversionToDuration( weekData.get( "dStartDate" ).get( position ), weekData.get( "dEndDate").get ( position )) ;
		String date = dateUtil.conversionToDurationFormat( weekData.get( "dStartDate" ).get( position ), weekData.get( "dEndDate").get ( position )) ;
		
		if(date.contains(" - ")){
			
			String dates[] = date.split(" - ");
			vh.periodStart.setText(dates[0]);
			vh.periodEnd.setText(dates[1]);
			
		}
		
//		if((vh.weekNumber.getText().toString()).length()>2)
//			vh.weekNumber.setTextSize(20);

		convertView.setTag( R.id.addFrnds, position );
		
	convertView.setTag( R.id.about_txtview, weekData.get( "vRoundId" ).get( position ) );

	
	
		
		return convertView;
	}


	private void setTypeFaces(TextView weekNumber, TextView period,
			TextView periodStart, TextView periodEnd) {
		weekNumber.setTypeface(Constants.OPEN_SANS_BOLD);
		period.setTypeface(Constants.OPEN_SANS_REGULAR);
		periodStart.setTypeface(Constants.OPEN_SANS_REGULAR);
		periodEnd.setTypeface(Constants.OPEN_SANS_REGULAR);
		
	}

	

}
