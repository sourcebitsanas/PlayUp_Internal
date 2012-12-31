package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONObject;



import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Types;
import com.playup.android.util.Util;


public class SectionJsonUtil {


	
	private  final String BLOCK_KEY = "blocks";
	private  final String TOTAL_KEY = "total_count";
	private  final String TITLE_KEY = "title";
	private final String SELF_KEY = ":self";

	private final String HREF_LINK = ":href";
	private final String TYPE_KEY = ":type";
	private final String UID_KEY = ":uid";
	private final String ITEM_KEY = "items";
	private boolean inTransaction = false;
	private final String STYLE_KEY = "style";
	private boolean isFromGeoTag = false;
	private String vCompetitionId = null; 

	
	

	public SectionJsonUtil(String str, boolean inTransaction ,boolean isFromGeoTag,String vCompetitionId) {

		this.inTransaction = inTransaction;
		this.isFromGeoTag  = isFromGeoTag;

		this.vCompetitionId = vCompetitionId;
		
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


			if(isFromGeoTag) {
				dbUtil.dropTables();
			}

			jsonObj = new JSONObject(str);


			
			
			String vSectionId 		= jsonObj.getString(UID_KEY);

			String vSectionUrl = jsonObj.optString(SELF_KEY);
			String vSectionHref = jsonObj.optString(HREF_LINK);


			if(!(jsonObj.getString ( TYPE_KEY ).equalsIgnoreCase(Types.SECTION_DATA_TYPE)))
				return;

			dbUtil.setHeader ( vSectionHref,vSectionUrl, jsonObj.getString ( TYPE_KEY ), false );

			if ( vCompetitionId != null && vCompetitionId.trim ( ) . length ( ) > 0 ) {
				dbUtil.updateSectionId(vCompetitionId ,vSectionId);
			}

			String title = jsonObj.getString(TITLE_KEY);
			
			
			new Util().setColor(jsonObj);
			


			JSONObject blocks = jsonObj.getJSONObject(BLOCK_KEY);
			if(!blocks.getString(TYPE_KEY).equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE))
				return;

			String vBlockId = blocks.getString(UID_KEY);
			int iBlockCount = blocks.getInt(TOTAL_KEY);


			dbUtil.deleteSectionBlockContent ( vSectionId );	

			dbUtil.setSectionData ( vSectionId, vSectionUrl,vSectionHref, vBlockId, iBlockCount, title );


			JSONArray blockItems = blocks.getJSONArray(ITEM_KEY);

			for ( int i = 0; i < blockItems.length ( ) ; i++ ) {



				JSONObject itemObj = blockItems.getJSONObject(i) ;

				if(!itemObj.getString(TYPE_KEY).equalsIgnoreCase(Types.BLOCK_CONTENT_DATA_TYPE))
					continue;
 
				String style = itemObj.getString(STYLE_KEY );
				String vBlockItemId = "";

				if(style!=null &&
						(  style.equalsIgnoreCase ( Types.SECTION_FEATURE ) || style.equalsIgnoreCase ( Types.SECTION_TILE) || style.equalsIgnoreCase (Types.SECTION_STACKED))   ) {
					
					int iBlockItemCount = 0;
					vBlockItemId = itemObj.getString(UID_KEY);
					iBlockItemCount = itemObj.getInt(TOTAL_KEY);

					dbUtil.setBlockItems( vBlockId, vBlockItemId, iBlockItemCount, style, i );
					
				}
				 
				
				JSONArray contentArray = itemObj.getJSONArray(ITEM_KEY);					

				dbUtil.deleteBockContent( vBlockItemId );

				for ( int j = contentArray.length ( ) - 1; j >= 0 ; j-- ) {

					// Modififed as per the HREF link 
					new BlockContentJsonUtil ( contentArray.getJSONObject(j).toString(), true, vBlockItemId, j );

				}

			}
		}catch(Exception e){
			//Logs.show(e);

		}finally{

			new Util().releaseMemory(jsonObj);
			if ( !inTransaction ) {
				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( "end ------------------------------------SectionJsonUtil ");

			}
		}
	}

}