package com.playup.android.adapters;

import java.util.Hashtable;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.util.ImageDownloader;

public class FriendGalleryAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private Hashtable<String, List<String>> data = null;

	private ImageDownloader imageDownloader = new ImageDownloader();

	public FriendGalleryAdapter ( Hashtable<String, List<String>> data ) {

		if ( imageDownloader == null ) {
			imageDownloader = new ImageDownloader();
		}
		this.data = data;
		inflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	

	public void setData ( Hashtable<String, List<String>> data  ) {
		if ( this.data != null ) {
			this.data.clear();
			this.data = null;
		}

		if ( inflater == null ) {
			inflater = (LayoutInflater) PlayUpActivity.context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		if ( imageDownloader == null ) {
			imageDownloader = new ImageDownloader();
		}
		this.data = data;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {

		if  ( data != null && data.get( "vFriendId" ) != null ) {

			return data.get( "vFriendId" ).size();
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

		public ImageView friendImage;
		public ImageView friendStatus;
		public ImageView friendType;
		

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

			return  layoutInflater.inflate( R.layout.addfriendview, null);

		}

		if (convertView == null ) {

			vh = new ViewHolder();

			convertView = inflater.inflate(R.layout.addfriendview, null);

			vh.friendImage = (ImageView) convertView.findViewById(R.id.friendImage);
			vh.friendStatus = (ImageView) convertView.findViewById(R.id.friendStatus);
			vh.friendType = (ImageView) convertView.findViewById(R.id.friendType);
			
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}


		if ( data.get( "isOnline").get( position ) == null ) {
			data.get( "isOnline").set( position, "0" );
		}

		int isOnline = Integer.parseInt ( data.get( "isOnline").get( position ) );
		if ( isOnline == 0 ) {
			vh.friendStatus.setVisibility( View.GONE );
		} else {
			vh.friendStatus.setVisibility( View.VISIBLE );
		}
		
		if(data.get("vSourceName").get(position) != null){
			if(data.get("vSourceName").get(position).toString().equalsIgnoreCase("playup")){
				vh.friendType.setVisibility( View.GONE );
			}else{
				vh.friendType.setVisibility( View.VISIBLE );
				vh.friendImage.setAlpha(70);
				imageDownloader.download(data.get("vSourceIconHref").get(position),vh.friendType, true, this);
			}
			
		}
		if ( data.get( "vFriendAvatar" ).get( position ) != null  ) {

			imageDownloader.download( data.get( "vFriendAvatar" ).get( position ) , vh.friendImage, true,  this );
		}
		convertView.setTag( R.id.activity_list_relativelayout, data.get( "vProfileId" ).get( position ) );
		return convertView;
	}

}
