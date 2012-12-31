package com.playup.android.adapters;

import java.util.Hashtable;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.util.Constants;
import com.playup.android.util.ImageDownloader;

import com.playup.android.util.Util;

public class ProviderAdapter extends BaseAdapter implements OnTouchListener,OnClickListener{

	private LayoutInflater inflater;
	private Hashtable<String, List<String>> data = null;

	private ImageDownloader imageDownloader = new ImageDownloader();
	private AlertDialog alert = null;
	private String vShareUrl = null;
	private Dialog dialog = null;
	private String providerName = null;
	private boolean isHref = false;
	

	public ProviderAdapter ( Hashtable<String, List<String>> data ) {

		if ( imageDownloader == null ) {
			imageDownloader = new ImageDownloader();
		}

		
		this.data = data;
		inflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	
	public void setData ( AlertDialog alert, String vShareUrl ) {
		this.alert = alert;
		this.vShareUrl = vShareUrl;
	}
	public void setData ( Dialog dialog, String vShareUrl, boolean isHref ) {
		this.dialog = dialog;
		this.vShareUrl = vShareUrl;
		this.isHref  = isHref;
	}


	@Override
	public int getCount() {

		if  ( data != null && data.get( "vProviderName" ) != null ) {

			return data.get( "vProviderName" ).size();
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

		public ImageView providerImage;
		public TextView providerName;
		public View divider;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder vh = null;
		if ( data == null ) {
			LayoutInflater layoutInflater = null;
			if ( inflater == null ) {
				layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			} else {
				layoutInflater = inflater;
			}

			return  layoutInflater.inflate( R.layout.share_dialog_item, null);

		}

		if (convertView == null ) {

			vh = new ViewHolder();

			convertView = inflater.inflate(R.layout.share_dialog_item, null);

			vh.providerImage = (ImageView) convertView.findViewById(R.id.providerImage);
			vh.providerName = (TextView) convertView.findViewById(R.id.providerName);
			vh.divider = (View) convertView.findViewById(R.id.divider);
			vh.providerName.setTypeface( Constants.OPEN_SANS_REGULAR );
			
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		convertView.setBackgroundColor(Color.parseColor("#FFFFFF"));
		if( position == ( getCount()-1) )
			vh.divider.setVisibility( View.GONE );
		else
			vh.divider.setVisibility( View.VISIBLE );
		
		vh.providerImage.setImageBitmap( null );	
		
		providerName =  data.get( "vProviderName" ).get( position );
		if( providerName!= null  ) {
			providerName = providerName.substring(0,1).toUpperCase()+providerName.substring(1);
		}
		
		vh.providerName.setText( providerName );
		imageDownloader.download(  data.get( "vIconBroadcastUrl" ).get( position ),  ( ImageView ) vh.providerImage, false, this  );
		
		convertView.setTag( R.id.about_txtview, data.get( "vProviderName" ).get( position ) );
		convertView.setOnTouchListener( this );
		return convertView;
	}




	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
			selectDeSelectItems ( v, true );
		} else if ( event.getAction() == MotionEvent.ACTION_UP ) { 
			selectDeSelectItems ( v, false );
			onClick( v );
			return false;
		} else if ( event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_CANCEL ) {
			selectDeSelectItems ( v, false );		
		}
		return true;
	}
	
	/**
	 * selector for the twitter, facebook and logout 
	 */
	private void selectDeSelectItems(View v, boolean isSelected) {
		if ( isSelected ) {
			v.setBackgroundColor(Color.parseColor("#B0E6EE") );
			((TextView) v.findViewById(R.id.providerName)).setTextColor(Color.parseColor("#FFFFFF"));

		} else {
			v.setBackgroundColor(Color.parseColor("#FFFFFF"));
			((TextView) v.findViewById(R.id.providerName)).setTextColor(Color.parseColor("#4B4B4B"));
		}
	}



	@Override
	public void onClick( View v ) {
		String providerName = v.getTag(R.id.about_txtview).toString();
		if( dialog != null ) 
			dialog.dismiss();
		try {
			JSONObject jObj = new JSONObject();
			jObj.put(":type", "application/vnd.playup.share+json");
			JSONArray provider_array = new JSONArray();
			provider_array.put( providerName );
			jObj.put("providers", provider_array);
			new Util().shareTheScores(providerName, vShareUrl, jObj.toString(),isHref);

		} catch (JSONException e) {
		//	Logs.show(e);
		}
		
		
	}
}
