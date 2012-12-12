package com.playup.android.adapters;

import java.util.Hashtable;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.ImageDownloader;
import com.playup.android.util.Logs;
import com.playup.android.util.PreferenceManagerUtil;


public class RegionsAdapter extends BaseAdapter implements OnTouchListener {

	private LayoutInflater inflater;
	private Hashtable<String, List<String>> regions;
	private ImageDownloader imageDownloader = null;
	private String currentRegion = "";
	
	private LayoutParams params;

	class ViewHolder {

		ImageView map;
		View divider ;
		TextView regionName;
		ImageView isSelected;
		RelativeLayout base;
	}

	public RegionsAdapter(Hashtable<String, List<String>> data) {

		inflater = (LayoutInflater) PlayUpActivity.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.regions = data;

		if (imageDownloader == null) {
			imageDownloader = new ImageDownloader();
		}

		currentRegion  = new PreferenceManagerUtil().get(Constants.REGION_TOKEN,"");
		
	}

	public void setData(Hashtable<String, List<String>> data) {
		this.regions = data;
		currentRegion  = new PreferenceManagerUtil().get(Constants.REGION_TOKEN,"");
		notifyDataSetChanged();

	}

	@Override
	public int getCount() {
		if (regions != null && regions.get("vRegionId") != null
				&& regions.get("vRegionId").size() > 0)
			return regions.get("vRegionId").size();
		else
			return 0;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder vh = null;

		if (convertView == null) {

			vh = new ViewHolder();

			convertView = inflater.inflate(R.layout.update_region_item, null);

			vh.base = (RelativeLayout) convertView.findViewById( R.id.updateRegionItem );
			vh.divider = convertView.findViewById( R.id.divider );
			vh.map = (ImageView) convertView.findViewById(R.id.map);
			vh.isSelected = (ImageView) convertView.findViewById(R.id.tick);
			vh.regionName = (TextView) convertView
					.findViewById(R.id.regionName);
			 
			convertView.setTag(vh);
			vh.regionName.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		vh.regionName.setTextColor(Color.parseColor("#4A4A4A"));
		
		
		
		if(currentRegion != null && currentRegion.trim().length() > 0 
				&& regions.get("vRegionCode").get(position) != null && 
				regions.get("vRegionCode").get(position).equalsIgnoreCase(currentRegion))
			vh.regionName.setTextColor(Color.parseColor("#27A544"));
		
		
		vh.isSelected.setVisibility(View.VISIBLE);
		vh.isSelected.setImageResource(R.drawable.chevron);
		

		try {
			params = (LayoutParams) vh.base.getLayoutParams();
			params.setMargins(0, 0, 0, 0);
			vh.base.setLayoutParams(params);
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		convertView.setPadding(0, 0, 0, 0);
		vh.map.setPadding(20,0,0,0);
		


		if(regions.get("vRegionLogo").get(position) != null && regions.get("vRegionLogo").get(position).trim().length() > 0)
			imageDownloader.download(regions.get("vRegionLogo").get(position), vh.map, false, this);
		
		if(regions.get("vRegionName").get(position) != null && regions.get("vRegionName").get(position).trim().length() > 0)
			vh.regionName.setText(regions.get("vRegionName").get(position));
		
		
		if ( getCount() == 1 || ( getCount() - 1 == position ) )  {
			vh.divider.setVisibility( View.GONE );
		} else {
			vh.divider.setVisibility( View.VISIBLE );
		}
		
		if(position == 0){
			
			convertView.setPadding(0, 20, 0, 0);
			vh.base.setBackgroundResource(R.drawable.content_base_top);
		}else if(position == regions.get("vRegionId").size() - 1){
			vh.base.setBackgroundResource(R.drawable.content_base);
		}else{
			vh.base.setBackgroundResource(R.drawable.content_base_middle);
		}
		
		convertView.setTag(R.id.aboutScrollView,position);
		
		
		

		convertView.setTag(R.id.about_txtview,regions.get("vRegionId").get(position));
		
		convertView.setOnTouchListener(this);

		return convertView;
	}

	private void handleClick(View v) {

		try {
			if (v.getTag(R.id.about_txtview) != null 
					&& v.getTag(R.id.about_txtview).toString().trim().length() > 0) {
			
				Bundle b = new Bundle();
				b.putString("vRegionId",v.getTag(R.id.about_txtview).toString() );
				
				PlayupLiveApplication.getFragmentManagerUtil().setFragment("CountriesFragment",b);

			}
		} catch (Exception e) {
			Logs.show(e);
		}

	}

	public void highLightBlueColor(final View view, final boolean shouldDo) {

		try {
			if (view == null) {
				
				return;
			}
			int position = Integer.parseInt(view.getTag(R.id.aboutScrollView).toString());
			RelativeLayout base = ((RelativeLayout) view.findViewById(R.id.updateRegionItem));
			params = (LayoutParams) base.getLayoutParams();
			if( base.getHeight() > 0 )
				params.height = base.getHeight();
			
			
			if (shouldDo) {
				
				
				if(view.getTag(R.id.aboutScrollView) != null){
					params.setMargins(2, 0, 2, 0);

					base.setLayoutParams(params);
					base.findViewById(R.id.map).setPadding(18,0,0,0);

					if(position == 0){
						base.setBackgroundResource(R.drawable.menu_top_pressed);
					}else if(position == regions.get("vRegionName").size() -1){
						base.setBackgroundResource(R.drawable.menu_bottom_pressed);
					}else{
						base.setBackgroundResource(R.drawable.menu_middle_pressed);
					}
					
				
				((TextView) view.findViewById(R.id.regionName)).setTextColor(Color
						.parseColor("#FFFFFF"));
				
				((ImageView) view.findViewById(R.id.tick)).setImageResource(R.drawable.chevron_d);
				}

			} else { 
				
				
				
				if(view.getTag(R.id.aboutScrollView) != null){
					params.setMargins(0, 0, 0, 0);

					base.setLayoutParams(params);
					base.findViewById(R.id.map).setPadding(20,0,0,0);

					if(position == 0){
						base.setBackgroundResource(R.drawable.content_base_top);
					}else if(position == regions.get("vRegionName").size() -1){
						base.setBackgroundResource(R.drawable.content_base);
					}else{
						base.setBackgroundResource(R.drawable.content_base_middle);
					}
					
					
				
				((TextView) view.findViewById(R.id.regionName)).setTextColor(Color
						.parseColor("#4A4A4A"));
				
				((ImageView) view.findViewById(R.id.tick)).setImageResource(R.drawable.chevron);

			}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}

	private float rawX = 0;
	private float rawY = 0;
	long downTime = 0;

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		
		
			if (event.getAction() == MotionEvent.ACTION_DOWN) {

				downTime = event.getDownTime();
				rawX = event.getRawX();
				rawY = event.getRawY();

				highLightBlueColor(v, true);

			}
			if (event.getAction() == MotionEvent.ACTION_UP) {

				highLightBlueColor(v, false);

				handleClick(v);
			}
			if (event.getAction() == MotionEvent.ACTION_CANCEL
					|| event.getAction() == MotionEvent.ACTION_OUTSIDE) {
				

				highLightBlueColor(v, false);
			}

			if (event.getEventTime() > (downTime + Constants.highightDelay)) {

				if (event.getRawY() >= rawY - 10
						&& event.getRawY() <= rawY + 10) {

					highLightBlueColor(v, true);

				}
			}
		
		return true;
	}

}
