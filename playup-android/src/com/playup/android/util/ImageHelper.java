package com.playup.android.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 
 * 
 *
 *  Class used to to make an image round Corner.
 *
 * 
 */
public class ImageHelper {

	/**
	 * 
	 * 
	 * @param bitmap : to which round corner need to apply
	 * @return round corener bitm map bitmap
	 */
	public Bitmap getRoundedCornerImage(Bitmap bitmap) {
		Bitmap resizedBitmap = bitmap;
		Bitmap targetBitmap = null;
		Bitmap output = null;
		try{


			int original_width = bitmap.getWidth();
			int original_height = bitmap.getHeight();




			if ( original_width > original_height ) {

				Paint paint = new Paint();
				paint.setFilterBitmap(true);

				targetBitmap = Bitmap.createBitmap( original_height, original_height, Bitmap.Config.ARGB_8888);

				RectF rectf = new RectF(0, 0, original_height, original_height );

				Canvas canvas = new Canvas(targetBitmap);
				Path path = new Path();

				path.addRect(rectf, Path.Direction.CW);
				canvas.clipPath(path);




				canvas.drawBitmap( bitmap, new Rect( original_width/2  - (  original_height )/ 2, 0, original_width/2  + (  original_height )/ 2, original_height),
						new Rect(0, 0, original_height, original_height ), paint);

				Matrix matrix = new Matrix();
				matrix.postScale(1f, 1f);
				resizedBitmap = Bitmap.createBitmap(targetBitmap, 0, 0, original_height, original_height, matrix, true);

			} else if ( original_height > original_width ) {

				Paint paint = new Paint();
				paint.setFilterBitmap(true);


				targetBitmap = Bitmap.createBitmap(  original_width,  original_width, Bitmap.Config.ARGB_8888);

				RectF rectf = new RectF(0, 0, original_width, original_width );

				Canvas canvas = new Canvas(targetBitmap);
				Path path = new Path();

				path.addRect(rectf, Path.Direction.CW);
				canvas.clipPath(path);


				canvas.drawBitmap( bitmap, new Rect( 0, original_height/2  - (  original_width )/ 2, original_width, original_height/2  + (  original_width )/ 2 ),
						new Rect(0, 0, original_width, original_width ), paint);

				Matrix matrix = new Matrix();
				matrix.postScale(1f, 1f);
				resizedBitmap = Bitmap.createBitmap(targetBitmap, 0, 0, original_width, original_width, matrix, true);

			}

			bitmap = resizedBitmap;

			output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
					.getHeight(), Config.ARGB_8888);
			Canvas canvas = new Canvas(output);




			final int color = 0xff424242;
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
			final RectF rectF = new RectF(rect);
			final float roundPx = 4;

			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

			paint.setXfermode(new PorterDuffXfermode( android.graphics.PorterDuff.Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rect, paint);
			
			
			try {
				if ( bitmap != null ) {
					bitmap.recycle();
					bitmap = null;
				}
			} catch ( Exception e) {}
			
		
			
			return output;
		}catch (Exception e) {
		} catch (Error e) {
			// as this error is only related  to bitmap 
			// even if out of memory occurs it will occur only for this and this wont hinder our application.
		} finally {
			try {
				if ( resizedBitmap != null ) {
					resizedBitmap.recycle();
					resizedBitmap = null;
				}
				if ( targetBitmap != null ) {
					targetBitmap.recycle();
					targetBitmap = null;
				}
			} catch( Exception e ) {
				
			}
			
		}
		
		try {
			if ( output != null ) {
				output.recycle();
				output = null;
			}
		} catch ( Exception e) {}
		return bitmap;


	}

}
