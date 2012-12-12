package com.playup.android.adapters;

import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.util.Constants;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;

/**
 * match room adapter. 
 */
public class LeaderBoardAdapter extends BaseAdapter {

	private LayoutInflater inflater  = null;
	private LayoutParams params;
	private ViewHolder holder;
	private Hashtable<String, List<String>> data = null;
	private ImageDownloader imageDownloader;
	private boolean isUpcomingMatch = false;
	

	public LeaderBoardAdapter ( Hashtable<String, List<String>> data , boolean isUpcomingMatch) {
		this.data = data;
		this.isUpcomingMatch = isUpcomingMatch;
		imageDownloader = new ImageDownloader();
		inflater = LayoutInflater.from(  PlayUpActivity.context );
	}

	
	public void setData ( Hashtable<String, List<String>> data,boolean isUpcomingMatch ) {
		imageDownloader = new ImageDownloader();
		this.isUpcomingMatch = isUpcomingMatch;
		this.data = data;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		
		if( data!=null && data.get("vTeamId") != null && data.get("vTeamId").size() > 0 ) {
			
				return data.get("vTeamId").size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {

		return null;
	}


	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		

		if ( inflater == null ) {
			LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return  layoutInflater.inflate( R.layout.leader_board_item, null);
		}

		if (convertView == null) {
			convertView  = inflater.inflate( R.layout.leader_board_item , null );
			holder = new ViewHolder();
			holder.position = (TextView) convertView.findViewById(R.id.teamPosition);
			holder.name = (TextView) convertView.findViewById(R.id.teamName);
			holder.points = (TextView) convertView.findViewById(R.id.teamScore);
			holder.flag = (ImageView) convertView.findViewById(R.id.teamFlag);
			
			
			holder.position.setTypeface(Constants.BEBAS_NEUE );
			holder.name.setTypeface(Constants.BEBAS_NEUE );
			holder.points.setTypeface(Constants.BEBAS_NEUE );
			
			convertView.setTag(holder);
		} else  {
			holder		=	(ViewHolder) convertView.getTag();
		}
		

			try {	

				holder.flag.setImageDrawable(null);

				holder.name.setText(data.get("vDisplayName").get(position));
				if(data.get("vCalendarUrl")!= null &&  data.get("vCalendarUrl").get(position)!= null && data.get("vCalendarUrl").get(position).trim().length() > 0)
					imageDownloader.download(data.get("vCalendarUrl").get(position), holder.flag, false, this, true);
				holder.position.setVisibility(View.VISIBLE);
				holder.position.setText(data.get("vPositionSummary").get(position));
				if( !isUpcomingMatch ) {
					holder.points.setText(data.get("vSummary").get(position));
				} else {
					holder.points.setText("");
					
					if(data.get("vPositionSummary").get(position) == null || data.get("vPositionSummary").get(position).trim().length() == 0   ) {
						holder.position.setVisibility(View.GONE);
						if( data.get("vDisplayName").get(position)!= null && data.get("vDisplayName").get(position).length()>18  ) {
							holder.name.setText(data.get("vDisplayName").get(position).substring(0,16)+"...");
						}
					} else {
						if( data.get("vDisplayName").get(position)!= null && data.get("vDisplayName").get(position).length()>14  ) {
							holder.name.setText(data.get("vDisplayName").get(position).substring(0,12)+"...");
						}
					}
										
					
				}
			} catch (Exception e) {
				Logs.show(e);
			}

		
		convertView.setTag(R.id.about_txtview,data.get("vTeamId").get(position));
		return convertView;
	}


	static class ViewHolder {
		TextView position ;
		TextView name;
		TextView points;
		ImageView flag;
		
	}



}
