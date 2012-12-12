package com.playup.android.database;

import android.content.ContentValues;

import com.playup.android.interfaces.QueryInterface;

/**
 * Update Query 
 */
public class UpdateQuery extends Query {

	private String whereClause;
	private ContentValues values;

	public UpdateQuery ( String tableName, String whereClause, ContentValues values,  QueryInterface queryInterface, int id ) {
		super( tableName, queryInterface, id );
		this.whereClause = whereClause;
		this.values = values;
	}
	
	// returning the where clause
	public String getWhereClause () {
		return whereClause;
	}

	// returning the content values
	public ContentValues getContentValues () {
		return values;
	}
	
}
