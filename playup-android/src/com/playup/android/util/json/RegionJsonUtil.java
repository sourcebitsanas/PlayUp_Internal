package com.playup.android.util.json;

import org.json.JSONArray;

import org.json.JSONObject;




import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;


public class RegionJsonUtil {

	private final String SELF_KEY = ":self";
	private final String HREF_URL_KEY = ":href";
	private final String TYPE_KEY = ":type";
	
	private final String ITEM_KEY = "items";
	private boolean inTransaction = false;

	private String currentRegion = "";
	

	public RegionJsonUtil(String str, String currentRegion, boolean inTransaction ){
		this.currentRegion  = currentRegion;
		this.inTransaction = inTransaction;
		

		if (str  != null) {
			parseData(str);
		}
	}

	private void parseData(String str) {

		JSONObject jsonObj = null;
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		if ( !inTransaction ) {
			dbUtil.getWritabeDatabase().beginTransaction();
		}
		try {


			

			jsonObj = new JSONObject(str);

			

			if(!(jsonObj.getString ( TYPE_KEY ).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE)))
				return;

			dbUtil.setHeader ( jsonObj.optString ( HREF_URL_KEY ), jsonObj.optString ( SELF_KEY ), jsonObj.getString ( TYPE_KEY ), false );
			
			new Util().setColor(jsonObj);

			dbUtil.emptyRegions();
			


			JSONArray regions = jsonObj.getJSONArray(ITEM_KEY);

			for ( int i = 0; i < regions.length ( ) ; i++ ) {



				JSONObject itemObj = regions.getJSONObject(i) ;

				if(!itemObj.getString(TYPE_KEY).equalsIgnoreCase(Types.REGIONS_DATA_TYPE))
					continue;
				
				
				new CountriesJsonUtil(itemObj.toString(),currentRegion, true);
				
				

			}
		}catch(Exception e){
			Logs.show(e);

		}finally{
			

			/**
			 * Cleaning Memory
			 */
			new Util().releaseMemory(jsonObj);

			
			if ( !inTransaction ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( "end ------------------------------------RegionJsonUtil ");

			}
		}
	}

}