package com.playup.android.adapters;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.playup.android.R;
import com.playup.android.activity.PlayUpActivity;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;

public class SectionGenerator {
	
	private LinearLayout sectionBase;

	private Hashtable<Integer, Hashtable<String, List<String>>> sectionData;
	private String vBlockItemType;
	private String vBlockItemId;
	private Hashtable<String, List<String>> blockItemData;
	private LayoutInflater inflater;
	private String vMainColor = null;
	private String vMainTitleColor = null;
	private String vSecColor = null;
	private String vSecTitleColor = null;
	private Hashtable<String, LinearLayout> viewTable;
	private Hashtable<String, HeaderGalleryAdapter> gallerdyAdapterTable;
	
	LinearLayout featureBase;
	LinearLayout tileBase;
	LinearLayout stackBase;
	Gallery featureGallery;
	LinearLayout dots;
	HeaderGalleryAdapter headerAdapter;
	String fromFragment;
	int galleryPosition;
	
	public SectionGenerator ( LinearLayout sectionBase,Hashtable<Integer, Hashtable<String, List<String>>> sectionData, String vMainColor, String vMainTitleColor, String vSecColor, String vSecTitleColor ) {
		
		
		this.vSecColor = vSecColor;
		this.vSecTitleColor  = vSecTitleColor;

		this.vMainColor = vMainColor;
		this.vMainTitleColor  = vMainTitleColor;
		
		this.sectionBase = sectionBase;
		this.sectionData = sectionData;
		if ( PlayUpActivity.context != null ) {
			inflater = ( LayoutInflater ) PlayUpActivity.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		viewTable = new Hashtable<String, LinearLayout>();
		gallerdyAdapterTable = new Hashtable<String, HeaderGalleryAdapter>();
		
		setSectionData();
	}
	
	public void setData( LinearLayout sectionBase, Hashtable<Integer, Hashtable<String, List<String>>> sectionData ) {
		
		if( this.sectionBase!=null && this.sectionBase != sectionBase ) {
			this.sectionBase.removeAllViews();
			this.gallerdyAdapterTable.clear();
		}
		
		this.sectionData = sectionData;
		this.sectionBase = sectionBase;
		setSectionData();
		//removeNotUsedViews();
		
	}
	
	
	
	/**
	 * removing unnessesary views from views/adapter table
	 */
	private void removeNotUsedViews() {
		
		try {
			Hashtable<String, LinearLayout> tempViewTable = new Hashtable<String, LinearLayout>();
		    Hashtable<String, HeaderGalleryAdapter> tempGalleryAdapterTable = new Hashtable<String, HeaderGalleryAdapter>();
			for( int i=0; i< sectionData.size(); i++ ) {

				if ( sectionData.get(i)!= null && 
						sectionData.get(i).get("vBlockItemId")!=null && 
						sectionData.get(i).get("vBlockItemId").get(0)!=null ) {
					
					tempViewTable.put(sectionData.get(i).get("vBlockItemId").get(0), viewTable.get(sectionData.get(i).get("vBlockItemId").get(0)));
					
					if( sectionData.get(i).get("vBlockItemType")!=null && 
						sectionData.get(i).get("vBlockItemType").get(0)!=null && 
						sectionData.get(i).get("vBlockItemType").get(0).equalsIgnoreCase(Types.SECTION_FEATURE) ) {
						tempGalleryAdapterTable.put(sectionData.get(i).get("vBlockItemId").get(0), gallerdyAdapterTable.get(sectionData.get(i).get("vBlockItemId").get(0)));
					}
					
				}
			
			}	
			
			gallerdyAdapterTable.clear();
			viewTable.clear();
			
			gallerdyAdapterTable = tempGalleryAdapterTable;
			viewTable = tempViewTable;
			
			tempGalleryAdapterTable.clear();
			tempViewTable.clear();
			
			tempGalleryAdapterTable = null;
			tempViewTable = null;
		} catch (Exception e) {
			Logs.show(e);
		}
		
		
	}

	
	/**
	 * setting section data
	 */
	public void setSectionData() {
		
		try {
			sectionBase.removeAllViews();
			
			if( sectionData == null || sectionData.size() == 0 )
				return;
			
			for( int i=0; i< sectionData.size(); i++ ) {

					blockItemData = sectionData.get(i);
					
					if( blockItemData!=null ) {
						if( blockItemData.get("vBlockItemType")!=null )
							vBlockItemType = blockItemData.get("vBlockItemType").get(0);
						else
							vBlockItemType = null;
						
						if( blockItemData.get("vBlockItemId")!=null )
							vBlockItemId = blockItemData.get("vBlockItemId").get(0);
						else
							vBlockItemId = null;
										
						
						if(vBlockItemType!=null && vBlockItemType.equalsIgnoreCase(Types.SECTION_FEATURE) ) {
							setFeatureData( i, sectionData.size(), blockItemData );			
  
					    } else if( vBlockItemType!=null && vBlockItemType.equalsIgnoreCase(Types.SECTION_TILE) ) {
										
							setTileData( i,sectionData.size(), blockItemData );	    		
						
					    } else if( vBlockItemType!=null && vBlockItemType.equalsIgnoreCase(Types.SECTION_STACKED) ) {
							
					    	setStackedData( i,sectionData.size(), blockItemData );
					    	
					    }				
						
					}
 
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}	
		
		
	}
	
	

	
	
	/**
	 * setting feature gallery data
	 */
	private void setFeatureData (  int position, int size, Hashtable<String, List<String>> blockItemData ) {
		
		try {
			if( viewTable!=null && viewTable.get(vBlockItemId)!=null )  {
				featureBase = viewTable.get(vBlockItemId);
			} else {
				featureBase = (LinearLayout) inflater.inflate(R.layout.feature_base, null);
				if( vBlockItemId != null )
					viewTable.put( vBlockItemId, featureBase );
			}
				
			featureGallery = (Gallery) featureBase.findViewById(R.id.headerGallery);
			dots = ( LinearLayout ) featureBase.findViewById(R.id.dots);
			
			if( position == 0 )
				featureBase.setPadding(0, 0, 0, 4);
			else
				featureBase.setPadding(0, 4, 0, 4);
			
			
			
			sectionBase.addView( featureBase );	
			if( featureGallery!=null && featureGallery.getSelectedItemPosition() >= 0 )
				galleryPosition = featureGallery.getSelectedItemPosition();
			else
				galleryPosition = 0;
			
			if(gallerdyAdapterTable!=null && gallerdyAdapterTable.get( vBlockItemId )!=null ) {
				
				headerAdapter = gallerdyAdapterTable.get( vBlockItemId );
				headerAdapter.setData(blockItemData, fromFragment, vMainColor, vMainTitleColor, vSecColor, vSecTitleColor);
				
			} else {
				
				headerAdapter = new HeaderGalleryAdapter(featureGallery, dots, blockItemData,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor);
				featureGallery.setAdapter(headerAdapter);
				gallerdyAdapterTable.put( vBlockItemId, headerAdapter );
			
			}
			
			featureGallery.setSelection( galleryPosition );
			featureGallery.setOnItemClickListener( headerAdapter );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
		
	}
	
	/**
	 * setting tile data
	 */
	private void setTileData ( int position, int size, Hashtable<String, List<String>> blockItemData ) {
		
		try {
			if( viewTable!=null && viewTable.get(vBlockItemId)!=null )  {
				tileBase = viewTable.get(vBlockItemId);
			} else {
				tileBase = new LinearLayout(PlayUpActivity.context);
				LayoutParams liParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				tileBase.setOrientation(LinearLayout.VERTICAL);
				tileBase.setLayoutParams( liParams );	
				if( vBlockItemId != null )
					viewTable.put( vBlockItemId, tileBase );
			}				    	
			
			if( position == 0 ) {
				tileBase.setPadding(4, 4, 4, 0);
				if( (position == (size -1)) )
					tileBase.setPadding(4, 4, 4, 4);
			} else {
				tileBase.setPadding(4, 0, 4, 0);
				if( (position == (size -1)) )
					tileBase.setPadding(4, 0, 4, 4);
			}
			sectionBase.addView( tileBase );
			new EuroTilesGridGenerator( blockItemData, tileBase,vMainColor,vMainTitleColor,vSecColor,vSecTitleColor);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
	
	}
	
	/**
	 * settting stacked data
	 */

	private void setStackedData (  int position,int size, Hashtable<String, List<String>> blockItemData ) {
		
		try {
			if( viewTable!=null && viewTable.get(vBlockItemId)!=null )  {
				stackBase = viewTable.get(vBlockItemId);
			} else {
				stackBase = new LinearLayout(PlayUpActivity.context);
				LayoutParams liParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
				stackBase.setOrientation(LinearLayout.VERTICAL);
				stackBase.setLayoutParams( liParams );	
				if( vBlockItemId != null )
					viewTable.put( vBlockItemId, stackBase );
			}		
			
			
			
			
			if(position == 0) {
				stackBase.setPadding(0, 4, 0, 0);
				if( (position == (size -1)) )
					stackBase.setPadding(0, 4, 0, 4);
			} else	{	
				stackBase.setPadding(0, 0, 0, 0);
				if( (position == (size -1)) )
					stackBase.setPadding(0, 0, 0, 4);
			}
			
			sectionBase.addView( stackBase );
			new ListGenerator(blockItemData, stackBase,  vMainColor, vMainTitleColor, vSecColor, vSecTitleColor , (position == (size -1)) );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}

	}
	
	
	/**
	 * calling feature gallery adapter's refresh whenever refresh happens
	 */
	public void resetFeaturesData() {
		
		try {
			if( gallerdyAdapterTable != null  ) {
				List<String> headerGalleryKeys = new ArrayList<String>(gallerdyAdapterTable.keySet()) ;
				
				for( int i=0; i< headerGalleryKeys.size(); i++ ) {
					try {
						headerAdapter = gallerdyAdapterTable.get(headerGalleryKeys.get(i));
						headerAdapter.refresh();
					} catch (Exception e) {
						Logs.show(e);
					}
					
				}	
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logs.show(e);
		}
		
		
	}
	

}
