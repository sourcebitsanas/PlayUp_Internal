package com.playup.android.database;

import android.content.ContentValues;

import com.playup.android.interfaces.QueryInterface;

/**
 *  Insert query
 */
public class InsertQuery extends Query {


	private ContentValues values;
	
	public InsertQuery ( String tableName, ContentValues values, QueryInterface queryInterface, int id  ) {
		super( tableName, queryInterface, id );
		
		this.values = values;
	}

	/**
	 * getting the content values
	 */
	public ContentValues getContentValues () {
		return values;
	}
	
}

