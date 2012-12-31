package com.playup.android.util.json;

import org.json.JSONArray;


import org.json.JSONObject;







import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Util;

import com.playup.android.util.Types;


public class CountriesJsonUtil {

	private final String DENSITY_KEY       =   "density";
	private final String HREF_KEY          =   "href";


	private final String LOGO_KEY          =   "logo";
	private  final String TOTAL_KEY = "total_count";
	private  final String NAME_KEY = "name";
	private final String SELF_KEY = ":self";
	private final String HREF_URL_KEY = ":href";
	private final String TYPE_KEY = ":type";
	private final String UID_KEY = ":uid";
	private final String ITEM_KEY = "items";
	private boolean inTransaction = false;
	private final String CODE_KEY = "code";
	private final String EFFECTIVE_CODE_KEY = "effective_code";
	private String currentRegion = null;
	

	public CountriesJsonUtil(String str, String currentRegion, boolean inTransaction ){
		this.inTransaction = inTransaction;
		this.currentRegion  = currentRegion;

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

			

			if(!(jsonObj.getString ( TYPE_KEY ).equalsIgnoreCase(Types.REGIONS_DATA_TYPE)))
				return;
			
			
			
			
			
			
			boolean isSelected = false;

			dbUtil.setHeader ( jsonObj.optString ( HREF_URL_KEY ), jsonObj.optString ( SELF_KEY ), jsonObj.getString ( TYPE_KEY ), false );
				String vRegionUrl = jsonObj.optString ( SELF_KEY );
				String vRegionHrefUrl = jsonObj.optString ( HREF_URL_KEY );

				dbUtil.setHeader(vRegionHrefUrl,vRegionUrl, jsonObj.getString ( TYPE_KEY ), false);
				
				
				String vRegionId = jsonObj.getString ( UID_KEY );
				String vRegionName = jsonObj.getString ( NAME_KEY );
				String vRegionCode = jsonObj.getString ( CODE_KEY );
				int iCountryCount = jsonObj.getInt ( TOTAL_KEY );
				
				
				JSONArray logo_jArr = jsonObj.getJSONArray( LOGO_KEY );
				//Log.e("123", "logo length------------"+logo_jArr);
				String vRegionLogo = null;
				for ( int k = 0, len_j = logo_jArr.length(); k < len_j; k++ ) {
					
					JSONObject logo_jObj = logo_jArr.getJSONObject( k );
					if ( logo_jObj.getString( DENSITY_KEY ).equalsIgnoreCase( Constants.DENSITY ) ) {
						
						vRegionLogo = logo_jObj.getString( HREF_KEY );
					}
					
				}
				
				/*dbUtil.setRegionData(vRegionName,vRegionCode,vRegionUrl,vRegionId,iCountryCount,vRegionLogo);*/
				//Log.e("123", "Region url in countries json----------------------"+vRegionLogo);
				dbUtil.setRegionData(vRegionName,vRegionCode,vRegionUrl,vRegionHrefUrl,vRegionId,iCountryCount,vRegionLogo);
				
				dbUtil.emptyCountries(vRegionId);
				
				JSONArray countriesArray = jsonObj.getJSONArray(ITEM_KEY);
			

				for ( int j = 0; j <countriesArray.length ( ) ; j++ ) {
					JSONObject country = countriesArray.getJSONObject(j);
					

					if(!country.getString(TYPE_KEY).equalsIgnoreCase(Types.COUNTRIES_DATA_TYPE))
						continue;
					
					isSelected = false;
					
					String vCountryUrl = country.optString ( SELF_KEY );
					String vCountryHrefUrl = country.optString ( HREF_URL_KEY );

					dbUtil.setHeader(vCountryHrefUrl,vCountryUrl, country.getString ( TYPE_KEY ), false);
					
					
					String vCountryId = country.getString ( UID_KEY );
					String vCountryName = country.getString ( NAME_KEY );
					String vCountryCode = country.getString ( CODE_KEY );
					String vCountryEffectiveCode = country.getString(EFFECTIVE_CODE_KEY);
					
					
					 logo_jArr = country.getJSONArray( LOGO_KEY );
					String vCountryLogo = null;
					for ( int k = 0, len_j = logo_jArr.length(); k < len_j; k++ ) {
						
						JSONObject logo_jObj = logo_jArr.getJSONObject( k );
						if ( logo_jObj.getString( DENSITY_KEY ).equalsIgnoreCase( Constants.DENSITY ) ) {
							
							vCountryLogo = logo_jObj.getString( HREF_KEY );
						}
						
					}
					if(vCountryId!= null && vCountryId.trim().length() > 0 &&
							currentRegion!= null && currentRegion.trim().length() > 0 &&
							vCountryId.equalsIgnoreCase(currentRegion))
						isSelected = true;
						
					
					
					/*dbUtil.setCountryData(vCountryUrl,vCountryId,vCountryName,vCountryCode,vCountryEffectiveCode,
							vCountryLogo,vRegionId,isSelected);*/
					dbUtil.setCountryData(vCountryUrl,vCountryHrefUrl,vCountryId,vCountryName,vCountryCode,vCountryEffectiveCode,
							vCountryLogo,vRegionId,isSelected);

					

				}
				
				

			
		}catch(Exception e){
			//Logs.show(e);

		}finally{
			
			/**
			 * Cleaning Memory
			 */
			new Util().releaseMemory(jsonObj);
			jsonObj	= null;

			if ( !inTransaction ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
				

			}
		}
	}

}