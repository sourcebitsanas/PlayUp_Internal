package com.playup.android.database;

/**
 * DatabaseWrapper class 
 * @author vikky mewada
 * 
 *  To perform any task related to database , it should happen through this wrapper class.
 *  
 *  1) All the select methods are direct and not synchronized with other queries for better user performance.
 *  
 *  2) All the queries made by services or any other background thread not related to UI should happen through queryMethod()
 *     eg queryMethod() 
 *  
 *  3) For all the queries that known to change only the local database and not remote server database should happen direct 
 *     eg directInsert(), directUpdate(), directDelete() and directRaw()
 */
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.playup.android.application.PlayupLiveApplication;
import com.playup.android.exception.NotApplicationContextException;
import com.playup.android.interfaces.QueryInterface;
import com.playup.android.util.Constants;


public class DatabaseWrapper {

	private static SQLiteDatabase writable_db;
	public static DatabaseWrapper databaseWrapper;
	private static SQLiteDatabase readOnly_db;
	private static List < Query > queryList = new ArrayList  < Query > ();

	private static DatabaseControl databaseControl;
	private DatabaseWrapper (  ) {

		databaseControl = new DatabaseControl ();

		readOnly_db = databaseControl.getReadableDatabase();
		writable_db = databaseControl.getWritableDatabase();

		databaseWrapper = this;
	}

	public void setWritableDatabase () {

		if ( databaseControl == null ) {
			databaseControl = new DatabaseControl();
		}
		writable_db = databaseControl.getWritableDatabase();
	}

	public static DatabaseWrapper getInstance () {


		if ( databaseWrapper == null ) {

			return new DatabaseWrapper ();
		} else {
			return databaseWrapper;
		}
	}

	public static SQLiteDatabase getWritableSQLiteDatabase () {

		if ( writable_db == null) {
			getInstance();
		}
		if ( !writable_db.isOpen() ) {
			getInstance();
		}
		return writable_db;
	}

	public static SQLiteDatabase getReadOnlySQLiteDatabase () {

		if ( readOnly_db == null) {
			getInstance();
		}
		if ( !readOnly_db.isOpen() ) {
			getInstance();
		}
		return readOnly_db;

	}



	/**
	 * getTotalCount --- gets the total number of records for a specific SQlite statement
	 *  @params sqlQuery is name of query
	 *  returns the total number of rows
	 */
	public int getTotalCount ( String sqlQuery ) {

		Cursor c = null;

		try {

			c = selectQuery ( sqlQuery );
			return c.getCount();

		} catch ( Exception e ) {
			
			return 0;

		} finally {

			if ( c != null ) {
				c.close();
			}
			c = null;
			// closing the read only database object 
		}

	}

	/** 
	 * dropTable   - Delete the whole table from the database
	 * @params tableName  - Name of table 
	 */
	public void dropTable ( String tableName ) {

		
		try {

			getWritableSQLiteDatabase().execSQL( " DROP TABLE " + tableName );
		} catch ( Exception e ) {
			//Logs.show ( e );
		} catch ( Error e ) {
			//Logs.show ( e ); 
		} finally {
		}
	}

	public boolean inProcess () {

		return getWritableSQLiteDatabase().inTransaction();
	}
	public void dropTables () {
	
	
		//getWritableSQLiteDatabase().beginTransaction();
		
		try {
			//Log.e("123","Deleting provider tokens");
			getWritableSQLiteDatabase().delete("providerTokens", null, null);
			//Log.e("123","After Deleting provider tokens");
			getWritableSQLiteDatabase().delete( "user", null, null);
			getWritableSQLiteDatabase().delete( "recent", null, null);
			getWritableSQLiteDatabase().delete( "user_notification", null, null);
			getWritableSQLiteDatabase().delete( "user_recent", null, null);
			getWritableSQLiteDatabase().delete( "conversation_friends", null, null);
			getWritableSQLiteDatabase().delete( "conversation_message", null, null);
			getWritableSQLiteDatabase().delete( "direct_conversation", null, null);
			getWritableSQLiteDatabase().delete( "direct_message_items", null, null);
			getWritableSQLiteDatabase().delete( "direct_messages", null, null);
			getWritableSQLiteDatabase().delete( "eTag", null, null);
			
			getWritableSQLiteDatabase().delete( "friendConversation", null, null);
			getWritableSQLiteDatabase().delete( "friendConversationMessage", null, null);
			getWritableSQLiteDatabase().delete( "friendLobbyConversation", null, null);
			getWritableSQLiteDatabase().delete( "friendMessage", null, null);
			
			getWritableSQLiteDatabase().delete( "gap_info", null, null);
			getWritableSQLiteDatabase().delete( "headers", null, null);
			getWritableSQLiteDatabase().delete( "match_conversation_node", null, null);
			getWritableSQLiteDatabase().delete( "message", null, null);
			getWritableSQLiteDatabase().delete( "my_friends", null, null);
			getWritableSQLiteDatabase().delete( "my_friends_live", null, null);
			getWritableSQLiteDatabase().delete( "notification", null, null);
			getWritableSQLiteDatabase().delete( "poll", null, null);
			getWritableSQLiteDatabase().delete( "providers", null, null);
			getWritableSQLiteDatabase().delete( "push_notifications", null, null);
			
			getWritableSQLiteDatabase().delete( "recent_invite", null, null);
			getWritableSQLiteDatabase().delete( "root_resource", null, null);
			getWritableSQLiteDatabase().delete( "round_contest", null, null);
			getWritableSQLiteDatabase().delete( "round_matches_node", null, null);
			getWritableSQLiteDatabase().delete( "rounds", null, null);
			getWritableSQLiteDatabase().delete( "search_friends", null, null);
			getWritableSQLiteDatabase().delete( "teams", null, null);
			
			getWritableSQLiteDatabase().delete( "user_direct_conversation", null, null);
		
			getWritableSQLiteDatabase().delete( "playup_friends", null, null);
			getWritableSQLiteDatabase().delete( "friendConversationHeaders", null, null);
			
			getWritableSQLiteDatabase().delete( "associatedContestsData", null, null);
			getWritableSQLiteDatabase().delete( "associatedContest", null, null);
			getWritableSQLiteDatabase().delete( "associatedTeams", null, null);
			getWritableSQLiteDatabase().delete( "sportBackground", null, null);
			getWritableSQLiteDatabase().delete( "summaries", null, null);
			getWritableSQLiteDatabase().delete( "leaderBoard", null, null);
			
			
			
			getWritableSQLiteDatabase().delete( "competition", null, null);
			getWritableSQLiteDatabase().delete( "competition_live", null, null);
			getWritableSQLiteDatabase().delete( "competition_round", null, null);
			getWritableSQLiteDatabase().delete( "competition_team", null, null);
			getWritableSQLiteDatabase().delete( "contest_lobby", null, null);
			getWritableSQLiteDatabase().delete( "contest_lobby_conversation", null, null);
			getWritableSQLiteDatabase().delete( "contests", null, null);
			getWritableSQLiteDatabase().delete( "context", null, null);
			
			//drop provider tokens:
			
			
			
			
		} catch ( Exception e ) {
		//	Logs.show ( e );
		} catch ( Error e ) { 
			//Logs.show ( e );
		} finally {
			
		
			/*if ( getWritableSQLiteDatabase().inTransaction() ) {
				getWritableSQLiteDatabase().setTransactionSuccessful();
				getWritableSQLiteDatabase().endTransaction();
			}*/
		}
	}

	/** 
	 * emptyTable   - Deletes all the data in the table leaving table structure intact
	 * @params tableName  - Name of table 
	 */
	public void emptyTable ( String tableName) {

	
		try {
			
			getWritableSQLiteDatabase().execSQL(  "DELETE FROM  " + tableName  );
		} catch  ( Exception e ) {
			//Logs.show ( e );
		} catch ( Error e ) {
			//Logs.show ( e );
		} finally {
			
		}
	}
	/**
	 * normally developers have  to use this method and not direct methods directly.
	 * 
	 *  its  the queries so that the database remains synced 
	 *  
	 *   @param queryType -- the type of query to perform insert/ update/ delete / other direct queries.
	 *   @param tableName -- name of the table  used only for insert/ update/ delete not for raw
	 *   @param values    -- ContentValues used in update/ insert not in delete and raw query.
	 *   @param whereClause-- used in update/ delete not in insert or raw queries.
	 *   @param sqlQuery   -- the whole sql query ony for raw query not for insert/ update / delete.
	 */
	public synchronized Object queryMethod2 ( int queryType, String sqlQuery, String  tableName, ContentValues values, String whereClause )  {


		
			try {
				
				
				while ( queryList.size() > 0 ) {

					Query query = queryList.get( 0 );

					if ( query instanceof InsertQuery ) {
						long id = insertQuery ( query.getTableName(), ((InsertQuery) query).getContentValues() );
						
						QueryInterface  queryInterface = query.getInterface();
						if ( queryInterface != null ) {
							queryInterface.onResult( query.getId() , id);
						}

					} else if ( query instanceof UpdateQuery ) {
						int updateRowCount = updateQuery( query.getTableName(), ((UpdateQuery) query).getContentValues(), ((UpdateQuery) query).getWhereClause()) ;
						
						QueryInterface  queryInterface = query.getInterface();
						if ( queryInterface != null ) {
							queryInterface.onResult( query.getId() , updateRowCount );
						}

					} else if ( query instanceof DeleteQuery ) {

						int deleteRowCount = deleteQuery(  query.getTableName(), ((DeleteQuery) query).getWhereClause());
						
						QueryInterface  queryInterface = query.getInterface();
						if ( queryInterface != null ) {
							queryInterface.onResult( query.getId() , deleteRowCount);
						}

					} else if ( query instanceof RawQuery ) {
						Cursor rawCursor = rawQuery( ((RawQuery) query).getSQLQuery() );
						QueryInterface  queryInterface = query.getInterface();
						if ( queryInterface != null ) {
							queryInterface.onResult( query.getId() , rawCursor);
						}

					}
					queryList.remove( 0 );
				}

				switch ( queryType ) {


				case Constants.QUERY_INSERT :  return insertQuery ( tableName, values );
				case Constants.QUERY_UPDATE :  return updateQuery(tableName, values, whereClause) ;
				case Constants.QUERY_DELETE :  return deleteQuery(tableName, whereClause) ;
				case Constants.QUERY_RAW    :  return rawQuery ( sqlQuery );

				}
			} catch ( Exception e ) {
				
				//Logs.show(e);
			} catch ( Error e ) {
				//Logs.show ( e );
			}
		
			

		return null;
	}

	

	public void queryMethod3 ( int queryType, String sqlQuery, String  tableName, ContentValues values, String whereClause )  {

		
		try {
				
				
				while ( queryList.size() > 0 ) {

					Query query = queryList.get( 0 );

					if ( query instanceof InsertQuery ) {
						long id = insertQuery ( query.getTableName(), ((InsertQuery) query).getContentValues() );
						QueryInterface  queryInterface = query.getInterface();
						if ( queryInterface != null ) {
							queryInterface.onResult( query.getId() , id);
						}

					} else if ( query instanceof UpdateQuery ) {
						int updateRowCount = updateQuery( query.getTableName(), ((UpdateQuery) query).getContentValues(), ((UpdateQuery) query).getWhereClause()) ;
						QueryInterface  queryInterface = query.getInterface();
						if ( queryInterface != null ) {
							queryInterface.onResult( query.getId() , updateRowCount );
						}

					} else if ( query instanceof DeleteQuery ) {

						int deleteRowCount = deleteQuery(  query.getTableName(), ((DeleteQuery) query).getWhereClause());
						QueryInterface  queryInterface = query.getInterface();
						if ( queryInterface != null ) {
							queryInterface.onResult( query.getId() , deleteRowCount);
						}

					} else if ( query instanceof RawQuery ) {
						Cursor rawCursor = rawQuery( ((RawQuery) query).getSQLQuery() );
						QueryInterface  queryInterface = query.getInterface();
						if ( queryInterface != null ) {
							queryInterface.onResult( query.getId() , rawCursor);
						}

					}
					queryList.remove( 0 );
				}

				switch ( queryType ) {


				case Constants.QUERY_INSERT :  insertQuery ( tableName, values ); break;
				case Constants.QUERY_UPDATE :  updateQuery(tableName, values, whereClause) ;break;
				case Constants.QUERY_DELETE :  deleteQuery(tableName, whereClause) ;break;
				case Constants.QUERY_RAW    :  rawQuery ( sqlQuery );break;

				}
				
			} catch ( Exception e ) {
				//Logs.show  ( e );
			} catch ( Error e ) {
				//Logs.show ( e );
			}

		
		
			

		/*	}

		};

		Thread th = new Thread( runnable );
		th.start();*/

	}




	/**
	 * DirectInsert, directUpdate and directDelete are the methods to perform queries immediately without waiting in queue.
	 * 
	 *  @param tableName -- name of the table to insert /update/ delete
	 *  @param values    -- contentvalues to be inserted / updated.
	 *  @param whereClause -- where clause of the query.
	 *  @return long  for directInsert -- returns the inserted row id
	 *  @return int  for  directUpdate and directDelete -- number  of rows that got affected.me
	 *  
	 */
	public void directInsert ( final String tableName, final ContentValues values,  QueryInterface queryInterface , int id ) {
		queryList.add( new InsertQuery ( tableName, values, queryInterface , id) );
		queryMethod2( Constants.QUERY_EMPTY, null, null, null, null);

	}

	public void directUpdate ( final String tableName, final ContentValues values, final String whereClause, QueryInterface queryInterface, int id ) {
		queryList.add( new UpdateQuery(tableName, whereClause, values, queryInterface, id ) );
		queryMethod2( Constants.QUERY_EMPTY, null, null, null, null);

	}

	public void directDelete ( final String tableName, final String whereClause, QueryInterface queryInterface, int id ) {
		queryList.add( new DeleteQuery( tableName, whereClause, queryInterface, id ) );
		queryMethod2( Constants.QUERY_EMPTY, null, null, null, null);
	}

	public void directRaw ( final String sqlQuery, final QueryInterface queryInterface, int id ) {
		queryList.add( new RawQuery( sqlQuery, queryInterface, id ) );
		queryMethod2( Constants.QUERY_EMPTY, null, null, null, null);
	}




	private long insertQuery ( final String tableName, final ContentValues values ) {
			return getWritableSQLiteDatabase().insertOrThrow( tableName, null, values);

	}

	private int updateQuery ( final String tableName, final ContentValues values, final String whereClause) {
		
		return getWritableSQLiteDatabase().update( tableName, values, whereClause, null );
	}

	public int deleteQuery ( final String tableName, final String whereClause ) {

		return getWritableSQLiteDatabase().delete(tableName, whereClause, null);
	}

	private Cursor rawQuery ( final String sqlQuery ) {

		return getWritableSQLiteDatabase().rawQuery( sqlQuery, null );
	}





	/**
	 * select query should either be called from this method or  select method only.
	 * @param queryString -- Query string 
	 * @return cursor
	 */
	public Cursor selectQuery ( final String queryString ) {

		try {

			return getReadOnlySQLiteDatabase().rawQuery( queryString, null);
		} catch  ( Exception e ) {
			//Logs.show(e);
			return null;
		} catch ( Error e ) {
			//Logs.show(e);
			return null;
		} finally {

			
		}

	}

	/**
	 * executes the given query and returns the result in form of HashTable.
	 * @params sql_query      - The Sqlite select Query  
	 * return  HashTable<String, List<String>> where String represents column name and List<String> all the rows of that column
	 */
	public Hashtable < String, List < String > > select ( String sql_query )  {

		
		Cursor c = getReadOnlySQLiteDatabase().rawQuery(sql_query, null);
		
		try{

			// check for valid cursor 
			
			if ( c != null && c.getCount() > 0 ) {
				
				c.moveToFirst();

				int totCols = c.getColumnCount();
				int tot = c.getCount();


				Hashtable < String, List < String  > > column = new Hashtable < String, List < String > > ( totCols ); 

				for ( int i = 0; i < totCols; i++ ) { 

					List < String > val = new ArrayList < String > ( );

					for ( int j = 0; j < tot; j++ ) {

						val.add( c.getString(i) );
						c.moveToNext();
					}

					column.put ( c.getColumnName ( i ), val );
					c.moveToFirst();

				}
				
				c.close();
				return column;

			} else {
				if(c!=null&&!c.isClosed())
					c.close();
				
				c = null;
				return null;

			}

		}catch (Exception e) {
			// Logs.show ( e );
			return null;
		} catch ( Error e ) {
		//	Logs.show( e );
			return null;
		} finally {
			
			if ( c != null && !c.isClosed() ) {
				c.close();
			}
			c = null; 
				
			
		}
	}

	/**
	 * to close the object of SQLiteDatabase. 
	 * Only application context can close the database instance as we do not want this database to be closed by any activity.
	 * @param context -- Only application context and not any activity context. 
	 */
	public void close ( Context context ) throws NotApplicationContextException {

		// checking for application context
		if ( context instanceof PlayupLiveApplication ) {

			// closing the read only database object 
			if ( readOnly_db != null ) {

				readOnly_db.close();
				readOnly_db = null;
			}
			// closing the read  & write database object 
			if ( writable_db != null ) {

				writable_db.close();
				writable_db = null;
			}
		} else {
			throw new NotApplicationContextException (" Context should be PlayUpApplication context" ) ;
		}

	}

}