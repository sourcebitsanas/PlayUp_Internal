package com.playup.android.util;


import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageDownloaderSports {



	

	private static CacheUtil cache = new CacheUtil();

   
	
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

	
	// optimized
	public void download(String url, View li, boolean isRoundCornerReq) {
		
		
		
		cache.getBitmapFromCacheForSports( url.hashCode()+"", url, li, this,null);
	}
	
	
	public void download(String url, View li, boolean isRoundCornerReq,String bgColor) {
		
		
		
		cache.getBitmapFromCacheForSports( url.hashCode()+"", url, li, this,bgColor);
	}
	
	
	
	/*public void  refresh () {
		
		
		if ( liList != null)  {
			int len = liList.size();
			for ( int i = 0 ; i < len; i++)  {
				
				nullViewDrawablesRecursive ( liList.get( i ) );
				
			}
			liList.clear();
		}
		liList = null;
		if ( cache != null ) {
			cache.clearSofCache();
		}
		

	}
	
	
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
	        
	        imageView.setImageDrawable(null);
	        imageView.setBackgroundDrawable(null);
	        
		        
	        imageView = null;
	    } catch (Exception e) {
	    }
	}

	
}
