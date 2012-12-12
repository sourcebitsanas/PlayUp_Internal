package com.playup.android.fragment;


import java.util.HashMap;




import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.util.Constants;
import com.playup.android.util.Logs;


/**
 * Displays, when user taps on <b>About Us</b> from Menu
 */
public class AboutFragment extends MainFragment implements OnTouchListener{

	private LinearLayout feedbackView;
    private TextView feedBackText,aboutText;
    private TextView versionName;
	boolean outSide=false;
	private RelativeLayout content_layout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		content_layout = (RelativeLayout) inflater.inflate( R.layout.about, null);
		return content_layout;
	}
	
	
	
	@Override
	public void onResume () {
		super.onResume();
		
		initialize( content_layout );
	}

	/**
	 * setting  all the content in the views 
	 */
	private void initialize ( final  RelativeLayout content_layout ) {
		
		// initialize views
		initializeViews(content_layout);
		
		
		//setting topbar
		setTopBar();
		
		// set listeners
		setListeners();
		
		// set values 
		setValues();
		
		setTypeFaces();
	}
	
	/**
	 * setting typefaces
	 */
	private void setTypeFaces() {
		feedBackText.setTypeface(Constants.OPEN_SANS_SEMIBOLD);
		aboutText.setTypeface(Constants.OPEN_SANS_LIGHT);
		versionName.setTypeface(Constants.OPEN_SANS_LIGHT);
	}

	
	/**
	 * intializing individual vies
	 * @param content_layout
	 */
	private void initializeViews (RelativeLayout content_layout) {
		//DatabaseUtil dbDatabaseUtil	=	DatabaseUtil.getInstance();
		
		feedbackView=(LinearLayout)content_layout.findViewById(R.id.feedbackView);
		/*if(dbDatabaseUtil.isUserAnnonymous()){
			feedbackView.setVisibility(View.INVISIBLE);
		}*/
		feedBackText= (TextView )content_layout.findViewById(R.id.feedbackText);
		aboutText = (TextView )content_layout.findViewById(R.id.aboutText);
		versionName = (TextView) content_layout.findViewById(R.id.versionName);
		content_layout.findViewById(R.id.aboutScrollView).setOnTouchListener(this); 

	}
	
	/**
	 * setting listeners
	 */
	private void setListeners () {
		feedbackView.setOnTouchListener(this);
	}
	
	/**
	 * Checking the project version, and displaying at the bottom of the screen
	 */
	private void setValues () {
				
		String version = null;
		try {
			PackageInfo pInfo = PlayUpActivity.context.getPackageManager().getPackageInfo(PlayUpActivity.context.getPackageName(), 0);
			version = pInfo.versionName;
		} catch (Exception e) {
			// TODO: handle exception
		}
		if( version!= null )
			versionName.setText(version);
	}
	
	
	
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		
		
		if(view.getId()==R.id.aboutScrollView){
			selectDeSelectState(view, false);
			return false;
		}
		
		int coordinates[] = new int[2];
		view.getLocationOnScreen(coordinates);
		int viewX = coordinates[0];
		int viewY = coordinates[1];


		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			outSide=false;
			selectDeSelectState(view, true);
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			selectDeSelectState(view, false);
			if ((event.getRawX() > viewX && event.getRawX() < (viewX + view
					.getWidth()))
					&& (event.getRawY() > viewY && event.getRawY() < (viewY + view
							.getHeight()))) {
				if(!outSide)
				{
					callFeedback();
				}
			}
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if (!((event.getRawX() > viewX && event.getRawX() < (viewX + view
					.getWidth())) && (event.getRawY() > viewY && event
							.getRawY() < (viewY + view.getHeight())))) {
				outSide=true;
				selectDeSelectState(view, false);
			}
		}
			
		
		return true;
		
	}
	
	
	/**
	 * selecting and deselecting views
	 * @param view
	 * @param isSelected
	 */
	private void selectDeSelectState( View view, boolean isSelected ) {

		if( isSelected ) {
			feedbackView.setBackgroundResource(R.drawable.menu_pressed);
			((ImageView)view.findViewById(R.id.feedbackImage)).setImageResource(R.drawable.feedback_d_icon);
			((ImageView)view.findViewById(R.id.redLine)).setImageResource(R.drawable.white_line);
			((TextView)view.findViewById(R.id.feedbackText)).setTextColor(Color.parseColor("#FFFFFF"));
		} else {
			feedbackView.setBackgroundResource(R.drawable.feedback_base);
			((ImageView)view.findViewById(R.id.feedbackImage)).setImageResource(R.drawable.feedback_icon);
			((ImageView)view.findViewById(R.id.redLine)).setImageResource(R.drawable.red_line);
			((TextView)view.findViewById(R.id.feedbackText)).setTextColor(Color.parseColor("#4B4B4B"));
		}
	}
	
	/**
	 * opening mail to send feedback
	 */
	private void callFeedback() {
		String version = null;
		try {
			PackageInfo pInfo = PlayUpActivity.context.getPackageManager().getPackageInfo(PlayUpActivity.context.getPackageName(), 0);
			version = pInfo.versionName;
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		if( version!= null && version.trim().length()!=0) 
			version = " ("+version+")";
		else
			version = "";
		
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);               
        emailIntent.setType("plain/text");  
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[]{ PlayUpActivity.context.getResources().getString(R.string.feedback_mail_id )});       
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, PlayUpActivity.context.getResources().getString(R.string.feedback_subject)+version);
        
        try 
        {
       	 	startActivity(Intent.createChooser(emailIntent, PlayUpActivity.context.getResources().getString(R.string.sendFeedback)));
        } catch (android.content.ActivityNotFoundException ex) {
        	//Toast.makeText(PlayUpActivity.context, R.string.mailError, Toast.LENGTH_LONG).show();
       	    
        }
	}
	
	
	

	/**
	 *  setting the sports name in the top bar fragment.
	 **/
	private void setTopBar () {
				HashMap< String, String > map = new HashMap<String, String>();
				map.put( "title", PlayUpActivity.context.getResources().getString(R.string.about));
				Bundle b = new Bundle();
				b.putString("vMainColor",null );
				b.putString("vMainTitleColor",null );
				Message msg = new Message ();
				msg.setData(b);
				msg.obj = map;
				PlayupLiveApplication.callUpdateTopBarFragments(msg);

	}

}
