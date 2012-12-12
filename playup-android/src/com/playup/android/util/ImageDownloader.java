package com.playup.android.util;

import android.graphics.drawable.Drawable;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageDownloader {

	public boolean isRoundCornerReq = false;

	private static CacheUtil cache = new CacheUtil();
	public BaseAdapter mBaseAdapter;

	private String bgColor = null;
	/**
	 * Download the specified image from the Internet and binds it to the
	 * provided ImageView. The binding is immediate if the image is found in the
	 * cache and will be done asynchronously otherwise. A null bitmap will be
	 * associated to the ImageView if an error occurs.
	 * 
	 * @param url
	 *            The URL of the image to download.
	 * @param imageView
	 *            The ImageView to bind the downloaded image to.
	 */

	public void nullViewDrawablesRecursive(View view) {
		if (view != null) {
			try {
				ViewGroup viewGroup = (ViewGroup) view;


				int childCount = viewGroup.getChildCount();
				for (int index = 0; index < childCount; index++) {
					View child = viewGroup.getChildAt(index);
					nullViewDrawablesRecursive(child);
				}
			} catch (Exception e) {
			}

			nullViewDrawable(view);
		}
	}

	public void nullViewDrawable(View view) {
		try {
			view.setBackgroundDrawable(null);
		} catch (Exception e) {
		}

		try {
			ImageView imageView = (ImageView) view;
			Drawable drawale = imageView.getDrawable();
			imageView.setImageDrawable(null);
			imageView.setBackgroundDrawable(null);
			drawale = null;

			imageView = null;
		} catch (Exception e) {
		}
	}
	
	/*public void refresh () {

		mBaseAdapter = null;


		if ( cache != null && cache.imageViews != null ) {
			int len = cache.imageViews.size();
			for ( int i = 0; i < len; i++ ) {

				nullViewDrawablesRecursive( cache.imageViews.get( i ) );

			}
			cache.imageViews.clear();
		}

		if ( cache != null ) {
			cache.clearSofCache();
		}

	}
*/




	// optimized
	public void download(String id, String url, ImageView imageView, boolean isRoundCornerReq,BaseAdapter mBaseAdapter) {

		try {
			
			if(url != null && url.trim().length() > 0){
			
			this.isRoundCornerReq = isRoundCornerReq;
			this.mBaseAdapter	=	mBaseAdapter;
			

			
			
		
			
			cache.getBitmapFromCache(id, url, imageView, this);
			}
			
		} catch ( Exception e)  {
			Logs.show( e);
		}
		

	}
	
	

	// optimized
	public void download( String url, ImageView imageView, boolean isRoundCornerReq,BaseAdapter mBaseAdapter) {
		try {

			if(url != null && url.trim().length() > 0){
			this.isRoundCornerReq = isRoundCornerReq;
			this.mBaseAdapter	=	mBaseAdapter;
			
			

			cache.getBitmapFromCache(""+url.hashCode(), url, imageView, this);
			}

			

		}catch (Exception e) {
			Logs.show(e);
		}

	}
	
	
//	public void download( String url, ImageView imageView, boolean isRoundCornerReq,BaseAdapter mBaseAdapter,String bgColor) {
//		try {
//
//			if(url != null && url.trim().length() > 0){
//			this.isRoundCornerReq = isRoundCornerReq;
//			this.mBaseAdapter	=	mBaseAdapter;
//
//			cache.getBitmapFromCache(""+url.hashCode(), url, imageView, this,bgColor);
//			}
//			
//
//		}catch (Exception e) {
//
//		}
//
//	}
	
	
	
    // optimized
	public void download( String url, ImageView imageView, boolean isRoundCornerReq,BaseAdapter mBaseAdapter, boolean notify ) {
		if(url != null && url.trim().length() > 0){
		this.isRoundCornerReq = isRoundCornerReq;
		this.mBaseAdapter	=	mBaseAdapter;

		cache.getBitmapFromCache(""+url.hashCode(), url, imageView, this, notify );
		}

	}

	public void removeImageFromLocalStorage(String iId) {

		cache.removeBitmapFromLocalStorage(iId);
	}

	public void removeImageFromSoftCache(String iId) {

		cache.removeBitmapFromSoftCache(iId);
	}

	public void interchangeName(String firstFileName, String secondFileName) {

		cache.interchangeBitmapFromLocalStorage(firstFileName, secondFileName);
	}

	/**
	 * Copy file content
	 * 
	 */

	public void copyFileContent(String source, String destination) {

		cache.copyfile(source, destination);
	}


}