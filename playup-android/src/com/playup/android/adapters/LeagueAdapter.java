package com.playup.android.adapters;

import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.fragment.LeagueSelectionFragment;
import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;

public class LeagueAdapter extends BaseAdapter implements OnTouchListener, OnClickListener  {

	private LayoutInflater inflater;
	private  Hashtable< String , List < String > > data;

	private String vSportsId = null;
	private int liveMatches;
	private String vSportsName = null;
	//	ArrayList<String> array_sort;
	boolean showLiveNow = false;
	private int items = 0;


	private String fromFragment = null;
	//int isLive ;
	private boolean isClicked	=	false;
	
	private String vMainColor = null;
	private String vMainTitleColor = null;


	private LeagueSelectionFragment leagueSelectionFragment;
	int selectedPosition =-1;
	private boolean isHighLightOn	=	false;
	
	
	private String vSecColor = null;
	private String vSecTitleColor = null;


	public LeagueAdapter( Hashtable< String , List < String > > data, String vSportsId, boolean showLiveNow, 
			String fromFragment, LeagueSelectionFragment leagueSelectionFragment, int selectedPosition ,
			String vMainColor,String vMainTitleColor, String vSecColor, String vSecTitleColor ) {
		
		

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		
		this.vMainColor  = vMainColor;
		this.vMainTitleColor = vMainTitleColor;


		this.leagueSelectionFragment = leagueSelectionFragment;
		this.selectedPosition = selectedPosition;
		this.fromFragment = fromFragment;
		this.vSportsId = vSportsId;
		this.data = data;
		this.showLiveNow = showLiveNow;

		inflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		isHighLightOn	=	false;
		if ( showLiveNow ) {
			setLiveMatches ();
		}

	}

//	public LeagueAdapter( Hashtable< String , List < String > > data, int count, String vSportsId, String fromFragment, int selectedPosition 
//			,String vMainColor,String vMainTitleColor ) {
//
//		
//		this.vMainColor  = vMainColor;
//		this.vMainTitleColor = vMainTitleColor;
//
//
//		this.fromFragment = fromFragment;
//		this.vSportsId = vSportsId;
//		this.data = data;
//		items = count;
//		this.selectedPosition = selectedPosition;
//
//		inflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		isHighLightOn	=	false;
//		if ( showLiveNow ) {
//			setLiveMatches ();
//		}
//
//	}


//	public LeagueAdapter( int items,String vMainColor,String vMainTitleColor ){
//		
//		this.vMainColor  = vMainColor;
//		this.vMainTitleColor = vMainTitleColor;
//
//		this.items = items;
//		isHighLightOn	=	false;
//		inflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//
//	}

	private void setLiveMatches () {


		try {
			DatabaseUtil dbUtil = DatabaseUtil.getInstance();
			liveMatches = dbUtil.getLiveMatches ( vSportsId  );
			vSportsName = dbUtil.getSportsName ( vSportsId );

			dbUtil = null;
		} catch (Exception e) {
			Logs.show(e);
		}

	}



	public void setData ( Hashtable< String , List < String > > data, String vSportsId,
			boolean showLiveNow, String fromFragment, int selectedPosition ,String vMainColor,String vMainTitleColor, String vSecColor, String vSecTitleColor ) {

		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;
		
		
		if ( this.data != null ) {
			this.data.clear();
			this.data = null;
		}
		
		this.vMainColor  = vMainColor;
		this.vMainTitleColor = vMainTitleColor;

		this.data = data;
		this.selectedPosition = selectedPosition;
		this.fromFragment = fromFragment;
		this.vSportsId = vSportsId;
		this.showLiveNow = showLiveNow;

		if ( showLiveNow ) {
			setLiveMatches ();
		}
		isHighLightOn	=	false;
		notifyDataSetChanged();
	}

//	public void setData ( Hashtable< String , List < String > > data, 
//			String vSportsId, int count, String fromFragment, int selectedPosition,String vMainColor,String vMainTitleColor  ) {
//		if ( this.data != null ) {
//			this.data.clear();
//			this.data = null;
//		}
//		
//		this.vMainColor  = vMainColor;
//		this.vMainTitleColor = vMainTitleColor;
//
//		this.data = data;
//		this.items = count;
//		this.fromFragment = fromFragment;
//		this.vSportsId = vSportsId;
//		this.selectedPosition = selectedPosition;
//		if ( showLiveNow ) {
//			setLiveMatches ();
//		}
//		isHighLightOn	=	false;
//		notifyDataSetChanged();
//
//	}

	@Override
	public int getCount() {

		if ( data != null && data.get( "vCompetitionId") != null && data.get( "vCompetitionId").size() > 0 ) {

			if ( showLiveNow ) {
				return data.get( "vCompetitionId").size() + 1;

			} else {
				return data.get( "vCompetitionId").size() ;
			}
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

		public ImageView listDevider;
		public ImageView favouriteStar;
		public ImageView chevron;

		public TextView liveNowTextView;
		public TextView liveNumber;
		public TextView liveText;
		public TextView leagueName;
		public TextView leagueSubName;

		public LinearLayout middleView;
		public LinearLayout favouriteStarView;

		public ImageView bottomShadow;

	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {


		if ( inflater == null ) {
			LayoutInflater layoutInflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			return  layoutInflater.inflate( R.layout.league_selection_listitem, null);
		}

		ViewHolder vh	= new ViewHolder() ;

		if(convertView == null ) {

			convertView = inflater.inflate( R.layout.league_selection_listitem, null); 

			vh.bottomShadow 	= (ImageView) 	convertView .findViewById(R.id.bottomShadow);
			vh.listDevider 	= (ImageView) 	convertView .findViewById(R.id.listDivider);
			vh.liveNumber 	= (TextView) 	convertView .findViewById(R.id.liveCount);
			vh.leagueName 	= (TextView) 	convertView .findViewById(R.id.leagueTitle);
			vh.leagueSubName= (TextView)    convertView .findViewById(R.id.leagueSubTitle);
			vh.liveText 	= (TextView)	convertView .findViewById(R.id.liveText);
			vh.favouriteStar 	= (ImageView)	convertView .findViewById(R.id.favouriteStar);
			vh.chevron 	= (ImageView)	convertView .findViewById(R.id.chevron);
			vh.liveNowTextView 	= (TextView)	convertView .findViewById(R.id.liveNowTextView);
			vh.middleView =(LinearLayout) convertView.findViewById(R.id.middleView);
			vh.favouriteStarView = (LinearLayout) convertView.findViewById(R.id.favouriteStarView);
			setTypeFaces( vh );

			convertView.setTag( vh );
		} else {
			vh = (ViewHolder) convertView.getTag();
		}


		if( position == ( getCount()-1) ) {
			vh.listDevider.setVisibility(View.GONE);
			vh.bottomShadow.setVisibility( View.VISIBLE );
		} else {
			vh.listDevider.setVisibility(View.VISIBLE);
			vh.bottomShadow.setVisibility( View.GONE );
		}

		convertView.setOnTouchListener( null );
		vh.favouriteStarView.setOnClickListener( null );

		convertView.setOnTouchListener( this );
		highLightBlueColor ( convertView, false );
		vh.chevron.setVisibility(View.VISIBLE);
		if ( showLiveNow ) {

			if ( position == 0 ) {
				vh.liveNowTextView.setVisibility(View.VISIBLE);
				vh.liveNowTextView.setText( (PlayUpActivity.context.getResources().getString(R.string.liveNow)).toUpperCase());
				vh.middleView.setVisibility(View.GONE);
				vh.favouriteStarView.setVisibility(View.INVISIBLE);
				vh.liveNumber.setVisibility(View.VISIBLE);
				vh.liveText.setVisibility(View.VISIBLE);
				if ( liveMatches > 0 ) {					
					vh.liveNumber.setText(""+liveMatches);
					vh.liveNowTextView.setTextColor(Color.parseColor("#27A544"));
					convertView.setOnTouchListener( this );
				} else {
					vh.liveNumber.setText(""+0);
					vh.liveText.setVisibility(View.GONE);
					vh.liveNowTextView.setTextColor(Color.parseColor("#B8B6B8"));
					vh.chevron.setVisibility(View.GONE);
					convertView.setOnTouchListener( null );
				}
				vh.favouriteStarView.setOnClickListener( null );
				convertView.setTag( R.id.about_txtview, null );
			} else {

				vh.middleView.setVisibility(View.VISIBLE);
				vh.liveNowTextView.setVisibility(View.GONE);
				vh.favouriteStarView.setVisibility(View.VISIBLE);

				vh.leagueName.setVisibility(View.VISIBLE);
				vh.leagueSubName.setVisibility(View.VISIBLE);

				int isLive = 0;
				
				if(data.get ( "iLiveNum" ).get( position -1 ) != null && data.get ( "iLiveNum" ).get( position -1 ).trim().length() > 0)
					 isLive = Integer.parseInt( data.get ( "iLiveNum" ).get( position -1 ) );	


				if ( isLive > 0  && showLiveNow) {
					vh.liveText.setVisibility(View.VISIBLE);
					vh.liveNumber.setVisibility(View.VISIBLE);
					vh.liveNumber.setText(""+isLive);
				} else {
					vh.liveText.setVisibility(View.GONE);
					vh.liveNumber.setVisibility(View.GONE);
				}


				vh.leagueName.setText( data.get( "vCompetitonName" ) .get ( position - 1 ) );
				vh.leagueSubName.setText( data.get( "vRegion" ) .get ( position - 1 )  );

				if( data.get ( "isFavourite" ).get( position -1 ) != null ) 
					setFavouriteHighlight ( vh, ( Integer.parseInt( data.get ( "isFavourite" ).get( position -1 ) ) == 1 )? true : false  ) ;
				else
					setFavouriteHighlight ( vh,  false  ) ;	

				vh.favouriteStarView.setTag(R.id.favouriteStarView,( position -1) );
				vh.favouriteStarView.setOnClickListener( this );
				convertView.setTag( R.id.activity_list_relativelayout, data.get ( "isFavourite" ).get( position -1 ) );
				convertView.setTag( R.id.about_txtview, data.get ( "vCompetitionId" ).get( position -1 ) );
			}

		} else {

			vh.middleView.setVisibility(View.VISIBLE);
			vh.liveNowTextView.setVisibility(View.GONE);
			vh.favouriteStarView.setVisibility(View.VISIBLE);

			vh.leagueName.setVisibility(View.VISIBLE);
			vh.leagueSubName.setVisibility(View.VISIBLE);

			int isLive = Integer.parseInt( data.get ( "iLiveNum" ).get( position ) );	


			if (isLive > 0) {
				vh.liveText.setVisibility(View.VISIBLE);
				vh.liveNumber.setVisibility(View.VISIBLE);
				vh.liveNumber.setText("" + isLive);
			} else {
				vh.liveText.setVisibility(View.GONE);
				vh.liveNumber.setVisibility(View.GONE);
			}


			vh.leagueName.setText( data.get( "vCompetitonName" ) .get ( position  ) );
			vh.leagueSubName.setText( data.get( "vRegion" ) .get ( position )  );

			if( data.get ( "isFavourite" ).get( position  ) != null ) 
				setFavouriteHighlight ( vh, ( Integer.parseInt( data.get ( "isFavourite" ).get( position ) ) == 1 )? true : false  ) ;
			else
				setFavouriteHighlight ( vh, false  ) ;

			vh.favouriteStarView.setTag(R.id.favouriteStarView,position );
			vh.favouriteStarView.setOnClickListener( this );
			convertView.setTag( R.id.activity_list_relativelayout, data.get ( "isFavourite" ).get( position ) );
			convertView.setTag( R.id.about_txtview, data.get ( "vCompetitionId" ).get( position  ) );
		}




		return convertView;


	}

	//for setting typeFaces
	private void setTypeFaces (ViewHolder vh) {
		vh.leagueSubName.setTypeface(Constants.OPEN_SANS_REGULAR);
		vh.leagueName.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		vh.liveNumber.setTypeface(Constants.BEBAS_NEUE);
		vh.liveNowTextView.setTypeface(Constants.OPEN_SANS_BOLD);
	}


	//setting current leagues properties with green highlight
	public void setFavouriteHighlight( ViewHolder vh, boolean isSelected ) {

		if ( isSelected ) {
			vh.favouriteStar.setImageResource(R.drawable.allsports_green_star);
		} else {
			vh.favouriteStar.setImageResource(R.drawable.allsports_grey_star);
		}

	}

	private  float rawX	=	0;
	private  float rawY	=	0;
	long downTime		=	0;

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if ( event.getAction() == MotionEvent.ACTION_DOWN ) {

			downTime 	=	event.getDownTime();
			rawX			=	event.getRawX();
			rawY			=	event.getRawY();
			// highlight blue color
			startUpdating(v);
		} 
		if ( event.getAction() == MotionEvent.ACTION_UP ) {

			stopUpdating();
			if(event.getRawY()==rawY||(event.getEventTime()-downTime)<200){

				isHighLightOn = false;
				highLightBlueColor ( v, true );
			}else{
				// disable the blue color
				highLightBlueColor ( v, false );
			}
			// handle click 
			handleClick ( v );
		} 
		if ( event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_OUTSIDE ) {
			stopUpdating();
			// disable the blue color
			highLightBlueColor ( v, false );
		}


		if(event.getEventTime()>(downTime+Constants.highightDelay)){

			if(event.getRawY()>=rawY-10&&event.getRawY()<=rawY+10){

				stopUpdating();
				isHighLightOn = false;
				highLightBlueColor ( v, true );
			}

		}


		return true;
	}


	/**
	 * 
	 * This is to handle custom selection. 
	 */
	Handler mHandler;

	ScheduledExecutorService mUpdater;

	private void startUpdating(final View v) {
		if (mUpdater != null) {

			return;
		}
		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				isHighLightOn = false;
				highLightBlueColor ( v, true );
				super.handleMessage(msg);
			}
		};
		mUpdater = Executors.newSingleThreadScheduledExecutor();
		mUpdater.schedule(new UpdateCounterTask(), 200,
				TimeUnit.MILLISECONDS);
	}

	private void stopUpdating() {
		if(mUpdater!=null&&!mUpdater.isShutdown()){
			mUpdater.shutdownNow();
			mUpdater = null;
		}
	}

	private class UpdateCounterTask implements Runnable {
		public void run() {

			try {
				if ( mHandler != null  ) {

					mHandler.sendEmptyMessage(0);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Logs.show ( e );
			}
		}
	}





	/**
	 * handling the click events 
	 */
	private void handleClick ( View view ) {

		try {
			if ( view == null ) {
				return;
			}

			if ( view.getTag( R.id.about_txtview ) != null ) {

				
				
				String vCompetitionId = (String) view.getTag( R.id.about_txtview );

				/*LinearLayout kli = (LinearLayout) PlayUpActivity.context.findViewById( R.id.main );
				kli.bringToFront();*/

				DatabaseUtil  dbUtil = DatabaseUtil.getInstance();


				Bundle b = new Bundle();			
				b.putString("vCompetitionId",vCompetitionId);
				b.putString("vMainColor",vMainColor );
				b.putString("vMainTitleColor",vMainTitleColor );
				b.putString( "vSecColor",vSecColor );
				
				b.putString( "vSecTitleColor",vSecTitleColor );
				

				PlayupLiveApplication.getFragmentManagerUtil().setFragment("LeagueLobbyFragment", b );


			} else { // for header items

				String topFragmentName = PlayupLiveApplication.getFragmentManagerUtil().getTopFragmentName();
				Bundle bundle = new Bundle ();
				bundle.putString( "vSportsId", vSportsId );
				bundle.putString("vMainColor",vMainColor );
				bundle.putString("vMainTitleColor",vMainTitleColor );
				bundle.putString( "vSecColor",vSecColor );			
				bundle.putString( "vSecTitleColor",vSecTitleColor );
				bundle.putString("fromFragment", topFragmentName);
				PlayupLiveApplication.getFragmentManagerUtil().setFragment("LiveSportsFragment", bundle);
			}
		} catch (Exception e) {
			Logs.show(e);
		}
	}


	/**
	 * Highligting the blue color. 
	 */

	public void highLightBlueColor ( final View view, final boolean shouldDo ) {

		if ( view == null ) {
			return;
		}

		if( view.getTag( R.id.about_txtview ) != null ) {

			if ( shouldDo ) {

				((RelativeLayout)view.findViewById(R.id.listBase)).setBackgroundColor(Color.parseColor("#B0E6FF"));
				((TextView)view.findViewById(R.id.leagueTitle)).setTextColor(Color.parseColor("#FFFFFF"));
				((TextView)view.findViewById(R.id.leagueSubTitle)).setTextColor(Color.parseColor("#FFFFFF"));
				((ImageView)view.findViewById(R.id.chevron)).setImageResource(R.drawable.chevron_d);

			} else { // remove the highlight

				((RelativeLayout)view.findViewById(R.id.listBase)).setBackgroundColor(Color.parseColor("#F7F7F4"));
				((TextView)view.findViewById(R.id.leagueTitle)).setTextColor(Color.parseColor("#565656"));
				((TextView)view.findViewById(R.id.leagueSubTitle)).setTextColor(Color.parseColor("#B8B6B8"));
				((ImageView)view.findViewById(R.id.chevron)).setImageResource(R.drawable.chevron);
			}

		} else {
			if ( shouldDo ) {

				((RelativeLayout)view.findViewById(R.id.listBase)).setBackgroundColor(Color.parseColor("#B0E6FF"));
				((TextView)view.findViewById(R.id.liveNowTextView)).setTextColor(Color.parseColor("#FFFFFF"));
				((ImageView)view.findViewById(R.id.chevron)).setImageResource(R.drawable.chevron_d);

			} else { // remove the highlight

				((RelativeLayout)view.findViewById(R.id.listBase)).setBackgroundColor(Color.parseColor("#F7F7F4"));
				((TextView)view.findViewById(R.id.liveNowTextView)).setTextColor(Color.parseColor("#27A544"));
				((ImageView)view.findViewById(R.id.chevron)).setImageResource(R.drawable.chevron);
			}
		}

	}

	@Override
	public void onClick(View view) {
		try {
			int position = -1;
			int isFavourite = -1;
			String vCompetitionId = null;
			if( view.getId() ==R.id.favouriteStarView ) {

				if ( view.getTag( R.id.favouriteStarView) != null)  {
					position = Integer.parseInt(view.getTag( R.id.favouriteStarView).toString());
				}

				if( position!=-1 ) {
					vCompetitionId = data.get ( "vCompetitionId" ).get( position ) ;
					if( data.get ( "isFavourite" ).get( position ) != null  )
						isFavourite = Integer.parseInt( data.get ( "isFavourite" ).get( position ) );
					else
						isFavourite = 0;
				} else{
					return;
				}

				DatabaseUtil dbUtil = DatabaseUtil.getInstance();
				ImageView imageView = (ImageView) view.findViewById(R.id.favouriteStar);
				if ( isFavourite == 1 ) {

					FlurryAgent.onEvent("favorite.removed");
					imageView.setImageResource(R.drawable.allsports_grey_star);
					data.get("isFavourite").set(position, "0");
					dbUtil.deSelectMySports ( vCompetitionId );
					dbUtil.setCompetitionFavourite ( vCompetitionId, 0 ); 

				} else { 
					FlurryAgent.onEvent("favorite.added");
					imageView.setImageResource(R.drawable.allsports_green_star);
					data.get("isFavourite").set(position, "1");
					dbUtil.setSelectedMySports ( vCompetitionId );
					dbUtil.setCompetitionFavourite ( vCompetitionId, 1 ); 
				}

				notifyDataSetChanged();

			}
		} catch (NumberFormatException e) {
			Logs.show(e);
		}

	}







}
