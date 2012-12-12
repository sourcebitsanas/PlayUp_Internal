package com.playup.android.util.json;

import org.json.JSONArray;
import org.json.JSONObject;

import com.playup.android.util.Constants;
import com.playup.android.util.DatabaseUtil;
import com.playup.android.util.Logs;
import com.playup.android.util.Types;
import com.playup.android.util.Util;

public class TeamJsonUtil {

	private final String UID_KEY           	=  ":uid";
	private final String SELF_KEY          	=  ":self";
	
	private final String NAME_KEY        	=  "name";
	private final String SHORT_NAME_KEY     =  "short_name";
	private final String LOGO_KEY        	=  "logos";
	private final String DISPLAY_NAME_KEY   =  "display_name";
	private final String HREF_LINK_KEY = ":href";
	private final String HEADER_KEY         = "header";
	private final String CALENDAR_KEY         = "calendar";


	private final String DENSITY_KEY        	=  "density";
	private final String HREF_KEY        	=  "href";
	private final String TYPE_KEY        	=  ":type";
	private final String COMPETITION_KEY        	=  "competitions";

	private boolean inTransaction = false;
	public TeamJsonUtil( boolean inTransaction ) {

		this.inTransaction = inTransaction;
	}


	public String parseData ( JSONObject jObj ) {

		DatabaseUtil dbUtil = DatabaseUtil.getInstance();
		
		if ( !inTransaction ) {
//			Logs.show ( "begin ------------------------------------TeamJsonUtil ");
			dbUtil.getWritabeDatabase().beginTransaction();
		}
		
		try {

			// fetching the data
			String uid = jObj.getString( UID_KEY );
			String selfUrl = jObj.optString( SELF_KEY );

			String hrefUrl = jObj.optString( HREF_LINK_KEY );
			
			if(!(jObj.getString( TYPE_KEY ).equalsIgnoreCase(Types.TEAM_DATA_TYPE)))
				return "";
			dbUtil.setHeader ( hrefUrl,selfUrl, jObj.getString( TYPE_KEY )  , false  );


			
			String name = jObj.optString( NAME_KEY );
			String shortName = jObj.optString( SHORT_NAME_KEY );
			String displayName = null;
			if( jObj.has(DISPLAY_NAME_KEY) ) {
				displayName = jObj.getString(DISPLAY_NAME_KEY);
			}
			// getting athe json array for logo
			
			String vHeaderUrl = null, vCalendarUrl = null;
			if( jObj.has( LOGO_KEY ) ) {
				JSONObject logo_jObj = jObj.getJSONObject( LOGO_KEY );

				if( logo_jObj.has( HEADER_KEY ) ) {
					JSONArray header_jArr = logo_jObj.getJSONArray( HEADER_KEY );
					int header_len = header_jArr.length();

					for ( int i = 0; i < header_len; i++ ) {

						JSONObject header_jObj = header_jArr.getJSONObject( i );

						if ( Constants.DENSITY.equalsIgnoreCase( header_jObj.getString( DENSITY_KEY ) ) ) {
							if( header_jObj.has( HREF_KEY ) )
							 vHeaderUrl = header_jObj.getString( HREF_KEY );
						}

					}
				}
				
				if( logo_jObj.has( CALENDAR_KEY ) ) {
					JSONArray calendar_jArr = logo_jObj.getJSONArray( CALENDAR_KEY );
					int calendar_len = calendar_jArr.length();

					for ( int i = 0; i < calendar_len; i++ ) {

						JSONObject calendar_jObj = calendar_jArr.getJSONObject( i );

						if ( Constants.DENSITY.equalsIgnoreCase( calendar_jObj.getString( DENSITY_KEY ) ) ) {
							if(  calendar_jObj.has( HREF_KEY ) )
								vCalendarUrl = calendar_jObj.getString( HREF_KEY );
						}

					}
				}
				
				
			}
			
			
			if( jObj.has(COMPETITION_KEY) ) {
				
				dbUtil.removeCompetitionDataOfTeam( uid );
				JSONArray competitionArray = jObj.getJSONArray( COMPETITION_KEY );
				JSONObject competitionJobj= null;
				for( int k=0; k < competitionArray.length(); k++ ) {
					competitionJobj = competitionArray.getJSONObject( k );					
					if( competitionJobj.has( UID_KEY ) ) {
						dbUtil.setTeamCompetitionData( uid, competitionJobj.getString( UID_KEY) );
						new LeagueJsonUtil(competitionJobj, null, false);						
					}
					
				}
				
			}

			// inserting into the database

			dbUtil.setTeam( uid, selfUrl, name, shortName,displayName, vHeaderUrl, vCalendarUrl,hrefUrl );


			



			// return back to conversation subject json util
			return uid; 
		} catch (  Exception e ) {
			Logs.show(e);
		} finally {
			/**
			 * Cleaning Memory
			 */
			new Util().releaseMemory(jObj);
			
			if ( !inTransaction ) {

				dbUtil.getWritabeDatabase().setTransactionSuccessful();
				dbUtil.getWritabeDatabase().endTransaction();
//				Logs.show ( "end ------------------------------------TeamJsonUtil ");
			}
		}
		return null;
	}
}
