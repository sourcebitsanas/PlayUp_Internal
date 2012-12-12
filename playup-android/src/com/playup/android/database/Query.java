package com.playup.android.database;

import com.playup.android.interfaces.QueryInterface;

public class Query {

	private String tableName;
	private QueryInterface queryInterface;
	private int id;

	// getting the required data for the query
	public Query ( final String tableName, final QueryInterface queryInterface, final int id ) {
		this.tableName = tableName;
		this.queryInterface = queryInterface;
		this.id = id;
	}	
	
	// handling the QueryInterface
	public Query ( final QueryInterface queryInterface, final int id ) {
		this.queryInterface = queryInterface;
		this.id = id;
	}
	
	// returning table name
	public String getTableName () {
		return tableName;
	}
	
	// returning the id 
	public int getId () {
		return id;
	}
	
	// returning the query interface.
	public QueryInterface getInterface () {
		return queryInterface;
	}
	
}
