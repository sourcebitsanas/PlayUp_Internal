package com.playup.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Util;

/**
 * the recent activity adapter for public profile. 
 */
public class PublicProfileAdapter extends BaseAdapter implements OnTouchListener, OnClickListener {

	private LayoutInflater inflater;
	private Cursor c;
	private LayoutParams params;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;
	
	private boolean isPrimaryUser = false;
	
	private String vSecColor = null;
	private String vSecTitleColor = null;
	
	public void close () {
//		inflater = null;
		c = null;
		
	}

	public PublicProfileAdapter( Cursor c, boolean isPrimaryUser ,String vMainColor,String vMainTitleColor, String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		
		this.vMainColor  = vMainColor;
		this.vMainTitleColor = vMainTitleColor;

		this.isPrimaryUser = isPrimaryUser;

		this.c = c;

		inflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * setting the data  and refreshing the content. 
	 */
	public void setData ( Cursor c, String fromFragment, boolean isPrimaryUser,String vMainColor,String vMainTitleColor, String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		
		this.vMainColor  = vMainColor;
		this.vMainTitleColor = vMainTitleColor;

		this.isPrimaryUser = isPrimaryUser;
		this.c = c;

		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if ( c != null ) {
			return c.getCount();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * recent activity textview 
	 */
	class ViewHolder {
		public TextView roomName;
		public ImageView contentDevider;
		public ImageView chevron;
		public TextView matchName;
		public RelativeLayout base;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if ( inflater == null ) {
			LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return  layoutInflater.inflate( R.layout.public_profile_listitems, null);
		}
		ViewHolder vh = null;

		// checking if convertView is null or not
		if(convertView == null ) {

			vh = new ViewHolder();
			convertView = inflater.inflate( R.layout.public_profile_listitems, null); 

			vh.roomName  = ( TextView ) convertView.findViewById ( R.id.roomName );
			vh.roomName.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
			vh.matchName  = ( TextView ) convertView.findViewById ( R.id.matchName );
			
			vh.chevron  = ( ImageView ) convertView.findViewById ( R.id.chevron );
			vh.contentDevider= (ImageView)convertView.findViewById(R.id.contentDivider);
			vh.base = ((RelativeLayout) convertView.findViewById(R.id.itemView));
			convertView.setTag( vh );
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		
		// checking for valid cursor 
		if ( c != null && !c.isClosed() ) {
			c.moveToPosition ( position );
			vh.roomName.setText ( new Util().getSmiledText( c.getString( c.getColumnIndex( "vRecentName" ) )) );
			vh.matchName.setText ( new Util().getSmiledText(c.getString( c.getColumnIndex( "vSubjectTitle" )) ) );
			
			int accessType =  c.getInt( c.getColumnIndex( "iAccess"));
			

			if(accessType == 1&&!isPrimaryUser) {

				
				vh.chevron.setImageResource(R.drawable.lock);
				if ( c.getInt( c.getColumnIndex( "iAccessPermitted")) == 0 ) {
					convertView.setTag( R.id.chevron , "private" );
				} else {
					vh.chevron.setImageResource(R.drawable.chevron);
					convertView.setTag( R.id.chevron , "public");
				}
			} else {
				vh.chevron.setImageResource(R.drawable.chevron);
				convertView.setTag( R.id.chevron , "public");
			}

			try {
				params = (LayoutParams) vh.base.getLayoutParams();
				params.setMargins(0, 0, 0, 0);
				vh.base.setLayoutParams(params);
				
			} catch (Exception e) {
				// TODO: handle exception
			}
			vh.base.findViewById(R.id.redLine).setPadding(50,0,0,0);
			if ( position == getCount() - 1 ) {
				vh.base.setBackgroundResource ( R.drawable.content_base );
				vh.contentDevider.setVisibility(View.GONE);
			} else {
				vh.base.setBackgroundResource ( R.drawable.content_base_middle );
				vh.contentDevider.setVisibility(View.VISIBLE);
			}
			
			
			
			// convertView.setTag( R.id.addFriendIcon , position );
			if(c.getString(c.getColumnIndex( "vSubjectHref" )) != null 
					&& c.getString(c.getColumnIndex( "vSubjectHref" )).trim().length() > 0){
				
				convertView.setTag( R.id.aboutText, c.getString( c.getColumnIndex( "vSubjectHref" ) ) );
				
			}else if(c.getString(c.getColumnIndex( "vSubjectUrl" )) != null 
					&& c.getString(c.getColumnIndex( "vSubjectUrl" )).trim().length() > 0){
				
				convertView.setTag( R.id.aboutText, c.getString( c.getColumnIndex( "vSubjectUrl" ) ) );
				
			}
				
			
			
			
			convertView.setTag( R.id.about_txtview, c.getString( c.getColumnIndex( "vSubjectId" ) ) );
			//	convertView.setOnClickListener( this );
		}

		if ( position + 1 >= getCount() ) {
			convertView.setId( -2 );
		} else {
			convertView.setId( -1 );
		}
		convertView.setOnTouchListener( this );

		return convertView;
	}



	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if ( event.getAction() == MotionEvent.ACTION_DOWN ) {
			selectDeSelectItems ( v, true );
		} else if ( event.getAction() == MotionEvent.ACTION_UP ) { 

			selectDeSelectItems ( v, false );

			// handle click
			if(v.getTag(R.id.chevron).toString().equalsIgnoreCase("private") && !isPrimaryUser ) {
				PlayupLiveApplication.showToast(R.string.privateRoomMessage);
			} else {
				onClick( v );
			}
		
			return false;
		} else if ( event.getAction() == MotionEvent.ACTION_OUTSIDE || event.getAction() == MotionEvent.ACTION_CANCEL ) {
			selectDeSelectItems ( v, false );		
		}
		return true;
	}


	@Override
	public void onClick(View v) {
		try {
			if ( v == null ) {
				return;
			}
//		// handling click events 
//		int pos = Integer.parseInt( v.getTag( R.id.addFriendIcon ).toString() ); 
//		if( pos == ( getCount() - 1 ) ) {
//			v.setBackgroundResource( R.drawable.content_base_d);
//		} else {
//			v.setBackgroundResource(R.drawable.content_base_middle_d);
//		}

				// TO DO 
			// when room screen is made use the later code starting another room.

			//PlayupLiveApplication.getFragmentManagerUtil().popBackStackTill( "MatchHomeFragment" ); 

			
			
			
			Bundle bundle = new Bundle ();
			bundle.putString( "vConversationId", (String) v.getTag ( R.id.about_txtview ) );
			bundle.putString("vMainColor",vMainColor );
			 bundle.putString("vMainTitleColor",vMainTitleColor );
			
			 bundle.putString( "vSecColor",vSecColor );			
				bundle.putString( "vSecTitleColor",vSecTitleColor );
			
			String type = DatabaseUtil.getInstance().getHeader(v.getTag (R.id.aboutText).toString());
			if( type != null ){
				
				//Log.e("123","PublicProfileAdpater >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> "+v.getTag ( R.id.about_txtview ));
				
				if( type.equalsIgnoreCase(Constants.ACCEPT_TYPE_COVERSATION )){
					PlayupLiveApplication.getFragmentManagerUtil().setFragment( "MatchHomeFragment", bundle );
				} else {
					PlayupLiveApplication.getFragmentManagerUtil().setFragment( "PrivateLobbyRoomFragment", bundle );
				}
			}
			
			//PlayupLiveApplication.getFragmentManagerUtil().setFragment( "MatchHomeFragment", bundle );
		} catch (Exception e) {
			//Logs.show(e);
		}

	}



	/**
	 * selector for the twitter, facebook and logout 
	 */
	private void selectDeSelectItems  ( View v, boolean isSelected ) {


		try {
			if ( v == null ) {
				return;
			}

			RelativeLayout base = ((RelativeLayout) v.findViewById(R.id.itemView));
			params = (LayoutParams) base.getLayoutParams();
			if( base.getHeight() > 0 )
				params.height = base.getHeight();

			if ( isSelected ) {
				params.setMargins(2, 0, 2, 0);
				base.setLayoutParams(params);
				base.findViewById(R.id.redLine).setPadding(48,0,0,0);

				
				if ( v.getId() == -2 ) {
					base.setBackgroundResource(R.drawable.menu_bottom_pressed);
				} else {
					base.setBackgroundResource(R.drawable.menu_middle_pressed);
				}

				( ( ImageView ) v.findViewById( R.id.redLine ) ).setImageResource(R.drawable.white_line);
				
				if(v.getTag(R.id.chevron).toString().equalsIgnoreCase("private"))
				{
					( ( ImageView ) v.findViewById( R.id.chevron ) ).setImageResource(R.drawable.lock);
				} else {
					
					( ( ImageView ) v.findViewById( R.id.chevron ) ).setImageResource(R.drawable.chevron_d);
				}
					
					

				TextView roomName  = ( TextView ) v.findViewById ( R.id.roomName );
				TextView matchName  = ( TextView ) v.findViewById ( R.id.matchName );
				roomName.setTextColor(Color.parseColor("#FFFFFF"));
				matchName.setTextColor(Color.parseColor("#FFFFFF"));
				
				roomName = null;
				matchName = null;

			} else {
				
				params.setMargins(0, 0, 0, 0);
				base.setLayoutParams(params);
				base.findViewById(R.id.redLine).setPadding(50,0,0,0);
				
				if ( v.getId() == -2 ) {
					base.setBackgroundResource(R.drawable.content_base );
				} else {
					base.setBackgroundResource(R.drawable.content_base_middle );
				}

				TextView roomName  = ( TextView ) v.findViewById ( R.id.roomName );
				TextView matchName  = ( TextView ) v.findViewById ( R.id.matchName );
				roomName.setTextColor(Color.parseColor("#4B4B4B"));
				matchName.setTextColor(Color.parseColor("#9A9A9A"));

				roomName = null;
				matchName = null;
				
				( ( ImageView ) v.findViewById( R.id.redLine ) ).setImageResource(R.drawable.red_line);
				
				if(v.getTag(R.id.chevron).toString().equalsIgnoreCase("private"))
				{
					( ( ImageView ) v.findViewById( R.id.chevron ) ).setImageResource(R.drawable.lock);
				} else {
					
					( ( ImageView ) v.findViewById( R.id.chevron ) ).setImageResource(R.drawable.chevron);
				}
				
				

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//Logs.show(e);
		}
	}

}