package com.playup.android.adapters;


import java.util.Hashtable;


import java.util.List;

import android.content.Context;
import android.graphics.Color;

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
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.ImageDownloader;

import com.playup.android.util.PreferenceManagerUtil;

public class CountriesAdapter extends BaseAdapter implements OnTouchListener {

	private LayoutInflater inflater;
	private Hashtable<String, List<String>> regions;
	private ImageDownloader imageDownloader = null;
	private DatabaseUtil dbUtil = null;
	private String vRegionId;
	LayoutParams params;

	class ViewHolder {

		ImageView map;
		View divider ;
		TextView regionName;
		ImageView isSelected;
		RelativeLayout base;
	}

	public CountriesAdapter(String vRegionId) {

		inflater = (LayoutInflater) PlayUpActivity.context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.vRegionId = vRegionId;
		if (imageDownloader == null) {
			imageDownloader = new ImageDownloader();
		}

		dbUtil = DatabaseUtil.getInstance();

		setValues();


	}

	private void setValues(){
		new Thread ( new Runnable () {

			@Override
			public void run() {
				
				try {
					regions = dbUtil.getCountries(vRegionId);
					if ( regions != null && PlayUpActivity.handler != null ) {
						PlayUpActivity.handler.post( new Runnable() {
							
							@Override
							public void run() {
								try {
									notifyDataSetChanged();
								} catch ( Exception e ) {
									//Logs.show ( e ) ;
								}
							}
						});
					}
				} catch (Exception e) {
					//Logs.show(e);
				}
			}
			
		}).start();
		

	}

	public void setData ( final String vRegionId ) {
		
		new Thread ( new Runnable () {

			@Override
			public void run() {
				
				try {
					regions = dbUtil.getCountries ( vRegionId );
					if ( regions != null && PlayUpActivity.handler != null ) {
						PlayUpActivity.handler.post( new Runnable() {
							
							@Override
							public void run() {
								try {
									notifyDataSetChanged();
								} catch ( Exception e ) {
									//Logs.show ( e ) ;
								}
							}
						});
					}
				} catch (Exception e) {
				//	Logs.show(e);
				}
			}
			
		}).start();
	}

	@Override
	public int getCount() {
		if (regions != null && regions.get("vCountryId") != null
				&& regions.get("vCountryId").size() > 0) {
			
			return regions.get("vCountryId").size();
				
		} else {
			return 0;
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder vh = null;

		if (convertView == null) {

			vh = new ViewHolder();

			convertView = inflater.inflate(R.layout.update_region_item, null);
			vh.map = (ImageView) convertView.findViewById(R.id.map);
			vh.base = (RelativeLayout) convertView.findViewById( R.id.updateRegionItem );
			vh.divider = convertView.findViewById( R.id.divider );
			vh.isSelected = (ImageView) convertView.findViewById(R.id.tick);
			vh.regionName = (TextView) convertView
			.findViewById(R.id.regionName);

			convertView.setTag(vh);
			vh.regionName.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		vh.isSelected.setImageResource(R.drawable.green_tick);
		vh.isSelected.setVisibility(View.INVISIBLE);
		vh.map.setImageBitmap(null);
		vh.regionName.setTextColor(Color.parseColor("#4A4A4A"));
		convertView.setTag(R.id.aboutScrollView,position);
		vh.map.setPadding(22,0,0,0);
		convertView.setPadding(0, 0, 0, 0);

		try {
			params = (LayoutParams) vh.base.getLayoutParams();
			params.setMargins(0, 0, 0, 0);
			vh.base.setLayoutParams(params);

		} catch (Exception e) {
			//Logs.show(e);
		}



		if(regions.get("vCountryId").get(position) != null && regions.get("vCountryId").get(position).trim().length() > 0)
			convertView.setTag(R.id.about_txtview,regions.get("vCountryId").get(position));

		if(regions.get("vCountryEffectiveCode").get(position) != null && regions.get("vCountryEffectiveCode").get(position).trim().length() > 0)
			convertView.setTag(R.id.acceptedIgnore,regions.get("vCountryEffectiveCode").get(position));
		if(regions.get("isSelected").get(position) != null && Integer.parseInt(regions.get("isSelected").get(position)) == 1){
			vh.isSelected.setVisibility(View.VISIBLE);
		}



		if(regions.get("vCountryLogo").get(position)  != null && regions.get("vCountryLogo").get(position).trim().length() > 0)
			imageDownloader.download(regions.get("vCountryLogo").get(position) , vh.map, false, this);





		if(regions.get("vCountryName").get(position) != null && regions.get("vCountryName").get(position).trim().length() > 0){
			convertView.setTag(R.id.activeUserMainLayout,regions.get("vCountryName").get(position));
			vh.regionName.setText(regions.get("vCountryName").get(position));

		}



		if ( getCount() == 1 || ( getCount() - 1 == position ) )  {
			vh.divider.setVisibility( View.GONE );
		} else {
			vh.divider.setVisibility( View.VISIBLE );
		}
		if(position == 0){

			convertView.setPadding(0, 20, 0, 0);
			vh.base.setBackgroundResource(R.drawable.content_base_top);
		}else if(position == regions.get("vCountryId").size() - 1){
			vh.base.setBackgroundResource(R.drawable.content_base);
		}else{
			vh.base.setBackgroundResource(R.drawable.content_base_middle);
		}








		convertView.setOnTouchListener(this);

		return convertView;
	}

	private void handleClick(final View v) {

		try {
			if ( v.getTag(R.id.about_txtview) != null && v.getTag(R.id.about_txtview).toString().trim().length() > 0
					&& v.getTag(R.id.acceptedIgnore) != null && v.getTag(R.id.acceptedIgnore).toString().trim().length() > 0) {


				if(v.getTag(R.id.activeUserMainLayout) != null)
					Constants.RegionName =  v.getTag(R.id.activeUserMainLayout).toString();
				
				
				if(v.getTag(R.id.acceptedIgnore) != null && v.getTag(R.id.acceptedIgnore).toString().trim().length() > 0)
					new PreferenceManagerUtil().set(Constants.REGION_TOKEN, v.getTag(R.id.acceptedIgnore).toString());	
				
				
			
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {

							if ( dbUtil != null ) {
								dbUtil.setSelectedRegion(v.getTag(R.id.about_txtview).toString());
								dbUtil.dropTables();			
							}

						}
						catch (Exception e) {
						//	Logs.show(e);
						}

					}
				}).start();


				PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill("MyProfileFragment");


			}
		} catch (Exception e) {
			//Logs.show(e);
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

					base.findViewById(R.id.map).setPadding(20,0,0,0);

					if(position == 0){
						base.setBackgroundResource(R.drawable.menu_top_pressed);
					}else if(position == regions.get("vCountryId").size() -1){
						base.setBackgroundResource(R.drawable.menu_bottom_pressed);
					}else{
						base.setBackgroundResource(R.drawable.menu_middle_pressed);
					}


					((TextView) view.findViewById(R.id.regionName)).setTextColor(Color
							.parseColor("#FFFFFF"));
				}

			} else { 



				if(view.getTag(R.id.aboutScrollView) != null){
					params.setMargins(0, 0, 0, 0);
					base.setLayoutParams(params);

					base.findViewById(R.id.map).setPadding(22,0,0,0);

					if(position == 0){
						base.setBackgroundResource(R.drawable.content_base_top);
					}else if(position == regions.get("vCountryId").size() -1){
						base.setBackgroundResource(R.drawable.content_base);
					}else{
						base.setBackgroundResource(R.drawable.content_base_middle);
					}



					((TextView) view.findViewById(R.id.regionName)).setTextColor(Color
							.parseColor("#4A4A4A"));

				}
			}
		} catch (Exception e) {
			//Logs.show(e);
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
