package com.playup.android.customview;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class ShadowView extends View {

	
	
	private Rect mRect;
	private Paint mPaint;

	public ShadowView(Context context)
	{
	    super(context);
	    mRect = new Rect();
	    mPaint = new Paint();
	    mPaint.setAntiAlias(true);
	    mPaint.setShadowLayer(2f, 1f, 1f, Color.BLACK);
	}

	@Override
	protected void onDraw(Canvas canvas) 
	{
		 Bitmap bmp = BitmapFactory.decodeResource( PlayUpActivity.context.getResources(), R.drawable.content_base);
		    Paint paint = new Paint();
		    paint.setAntiAlias(true);
		    paint.setShadowLayer(5.5f, 6.0f, 6.0f, Color.GRAY);
		    //canvas.drawColor(Color.GRAY);
		    canvas.drawRect(0,0, bmp.getWidth(),bmp.getHeight(), paint);
		    canvas.drawBitmap(bmp, 0, 0, null);  
	}



}
