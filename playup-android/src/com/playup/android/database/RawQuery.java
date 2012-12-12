package com.playup.android.database;

import com.playup.android.interfaces.QueryInterface;

/**
 * Raw query 
 * 
 */
public class RawQuery extends Query {


	private String sqlQuery;
	
	public RawQuery ( String sqlQuery, QueryInterface queryInterface, int id  ) {
		super( queryInterface, id );
		
		this.sqlQuery = sqlQuery;
	}
	
	
	// returning the query 
	public String getSQLQuery () {
		return sqlQuery;
	}
	
}
