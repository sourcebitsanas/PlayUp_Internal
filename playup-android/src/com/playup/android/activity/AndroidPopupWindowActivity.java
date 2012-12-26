package com.playup.android.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;

public class AndroidPopupWindowActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.popupwindowxml);
		final Button b	=	(Button) findViewById(R.id.button1pop);
		b.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LayoutInflater layoutInflater 
			     = (LayoutInflater)getBaseContext()
			      .getSystemService(LAYOUT_INFLATER_SERVICE);  
			    View popupView = layoutInflater.inflate(R.layout.playup, null);  
			             final PopupWindow popupWindow = new PopupWindow(
			               popupView, 
			               LayoutParams.WRAP_CONTENT,  
			                     LayoutParams.WRAP_CONTENT);  
			       
			             ListView lv	=	(ListView) popupView.findViewById(R.id.radioList);
			             lv.setAdapter(new AdapterDemo(AndroidPopupWindowActivity.this));
			             ImageView	close	=	(ImageView) popupView.findViewById(R.id.close);
			             close.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								popupWindow.dismiss();
							}
						});
//			             Button btnDismiss = (Button)popupView.findViewById(R.id.dismiss);
//			             btnDismiss.setOnClickListener(new View.OnClickListener() {
//							
//							@Override
//							public void onClick(View v) {
//								// TODO Auto-generated method stub
//								popupWindow.dismiss();
//							}
//						});
			             
			             popupWindow.showAtLocation(popupView, 0, 0, 0);
			             popupWindow.update(25, 65 ,430, 700);
			}
		});
	}
	
	
}
