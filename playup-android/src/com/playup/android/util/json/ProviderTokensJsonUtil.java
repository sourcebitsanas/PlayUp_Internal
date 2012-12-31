package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONObject;



import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;

import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class ProviderTokensJsonUtil {

	private final String UID_KEY           			=  ":uid";
	private final String SELF_KEY          			=  ":self";
	private final String TYPE_KEY         	 		=  ":type";
	private final String ID_KEY         	 		=  "id";
	private final String TOKEN_KEY         	 	=  "token";
	private final String ITEMS_KEY         	 		=  "items";
	
	
	private boolean inTransaction;
	
	public ProviderTokensJsonUtil(String str, boolean inTransaction) {
		
		this.inTransaction = inTransaction;
		if ( str != null && str.trim().length() > 0) {
			parseData ( str );
		}
		
	}
	
	public void parseData ( String str ) {
		JSONObject jsonObject = null;
		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		if ( !inTransaction ) {
			dbUtil.getWritabeDatabase().beginTransaction();
			
		}
		

		try {
			
			jsonObject = new JSONObject(str);
			
			String vProviderTokensType = jsonObject.getString( TYPE_KEY );
			
			if( vProviderTokensType == null || !vProviderTokensType.equalsIgnoreCase(Types.COLLECTIONS_DATA_TYPE) )
				return;
	
			
			
				
				dbUtil.emptyProviderTokens();
				
					JSONArray tokensArray = jsonObject.getJSONArray( ITEMS_KEY );
					
					if(tokensArray != null && tokensArray.length() > 0){
						
						for(int i = 0;i<tokensArray.length();i++){
							
							JSONObject tokenObject = tokensArray.getJSONObject(i);
							
							if(!(tokenObject.getString(TYPE_KEY).equalsIgnoreCase(Types.TOKENS_TYPE)))
								return;
							
							String vType = tokenObject.getString(TYPE_KEY);
							String vUid = tokenObject.getString(UID_KEY);
							String vId = tokenObject.getString(ID_KEY);
							String vToken = tokenObject.getString(TOKEN_KEY);
							
							
							
							
							dbUtil.setProviderToken(vType,vUid,vId,vToken);
							
						}
						
					}
					
					
				
			
		}catch (Exception e) {
		//	Logs.show(e);
		} finally {	
			new Util().releaseMemory(jsonObject);
				if ( !inTransaction ) {
					dbUtil.getWritabeDatabase().setTransactionSuccessful();
					dbUtil.getWritabeDatabase().endTransaction();
				}
			}
		}


}
