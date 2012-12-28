package com.playup.android.adapters;

import java.util.Hashtable;
import java.util.List;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.util.ImageDownloader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RadioStationListAdapter extends BaseAdapter{
	
	
	Hashtable<String, List<String>> stationList	=	null;
	LayoutInflater inflater;
	ViewHolder holder;
	private ImageDownloader imageDownloader = null;

	public RadioStationListAdapter(Hashtable<String, List<String>> stationList) {
		// TODO Auto-generated constructor stub
		
		this.stationList	=	stationList;
		inflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (imageDownloader == null) {
			imageDownloader = new ImageDownloader();
		}
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(stationList.get("vContentId").size()>0){
			return stationList.get("vContentId").size();
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
		// TODO Auto-generated method stub
		if ( inflater == null ) {
			LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return  layoutInflater.inflate( R.layout.leader_board_item, null);
		}

		if (convertView == null) {
			convertView  = inflater.inflate( R.layout.radiolist , null );
			holder = new ViewHolder();
			holder.radioTitle = (TextView) convertView.findViewById(R.id.radioTitle);
			holder.radioSubTitle = (TextView) convertView.findViewById(R.id.radioSubTitle);
			holder.logo = (ImageView) convertView.findViewById(R.id.logo);
			holder.soundPlay = (ImageView) convertView.findViewById(R.id.speaker);
			
			
			
			convertView.setTag(holder);
		} else  {
			holder		=	(ViewHolder) convertView.getTag();
		}
		
		if(stationList != null && stationList.containsKey("vRadioTitle") && stationList.get("vRadioTitle").get(position) != null && stationList.get("vRadioTitle").get(position).trim().length() >0 ){
			holder.radioTitle.setText(stationList.get("vRadioTitle").get(position));
		}
		
		if(stationList != null && stationList.containsKey("vRadioSubTitle") && stationList.get("vRadioSubTitle").get(position) != null&& stationList.get("vRadioSubTitle").get(position).trim().length() >0 ){
			holder.radioSubTitle.setText(stationList.get("vRadioSubTitle").get(position));
		}
		
		if(stationList != null && stationList.get("vRadioIcon").get(position) != null && stationList.get("vRadioIcon").get(position).trim().length() > 0)
			imageDownloader.download(stationList.get("vRadioIcon").get(position), holder.logo, false, this);
		
		holder.soundPlay.setImageResource(R.drawable.speakerlow);
		
		return convertView;
	}
	
	static class ViewHolder{
		TextView radioTitle;
		TextView radioSubTitle;
		ImageView logo;
		ImageView soundPlay;
		
		
	}

}