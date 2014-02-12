package candy.castle;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class PositionDatasource {
	// Database fields
	private SQLiteDatabase database;
	private PositionDatabase dbHelper;
	private List<Position> positions = new ArrayList<Position>();
	
	public PositionDatasource(Context context) {
		dbHelper = new PositionDatabase(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}
	
	public void createPosition(String user, int user_id, Double lat, Double lng, Double alt, Double bsl, int act, long time, int game, int send) {
		ContentValues values = new ContentValues();
		values.put(PositionDatabase.COLUMN_USER, user);
		values.put(PositionDatabase.COLUMN_USER_ID, user_id);
		values.put(PositionDatabase.COLUMN_LAT, lat);
		values.put(PositionDatabase.COLUMN_LNG, lng);
		values.put(PositionDatabase.COLUMN_ALT, alt);
		values.put(PositionDatabase.COLUMN_BSL, bsl);
		values.put(PositionDatabase.COLUMN_ACT, act);
		values.put(PositionDatabase.COLUMN_TIME, time);
		values.put(PositionDatabase.COLUMN_GAME, game);
		values.put(PositionDatabase.COLUMN_SEND, send);
		database.insert(PositionDatabase.TABLE_POSITION, null, values);
	}
	
	public void updatePositionBSL(String user, int id, Double bsl, long time) {
		ContentValues values = new ContentValues();
		values.put(PositionDatabase.COLUMN_BSL, bsl);
		values.put(PositionDatabase.COLUMN_TIME, time);
		database.update(PositionDatabase.TABLE_POSITION, values, PositionDatabase.COLUMN_USER_ID+"="+id+" AND "+PositionDatabase.COLUMN_USER+"='"+user+"'", null);
	}
	
	public void updateActivedDatabase(String user, int act) {
		ContentValues values = new ContentValues();
		values.put(PositionDatabase.COLUMN_ACT, act);
		database.update(PositionDatabase.TABLE_POSITION, values, PositionDatabase.COLUMN_ACT+"="+1+" AND "+PositionDatabase.COLUMN_USER+"='"+user+"'", null);
	}
	
	public void updateSendedDatabase(String user, int send) {
		ContentValues values = new ContentValues();
		values.put(PositionDatabase.COLUMN_SEND, send);
		database.update(PositionDatabase.TABLE_POSITION, values, PositionDatabase.COLUMN_SEND+"="+1+" AND "+PositionDatabase.COLUMN_USER+"='"+user+"'", null);
	}
	
	public void updatePosition(Position pos) {
		ContentValues values = new ContentValues();
		values.put(PositionDatabase.COLUMN_LAT, pos.getLat());
		values.put(PositionDatabase.COLUMN_LNG, pos.getLng());
		values.put(PositionDatabase.COLUMN_ALT, pos.getAlt());
		values.put(PositionDatabase.COLUMN_BSL, pos.getBsl());
		values.put(PositionDatabase.COLUMN_ACT, 1);
		values.put(PositionDatabase.COLUMN_TIME, pos.getTime());
		values.put(PositionDatabase.COLUMN_GAME, pos.getGame());
		values.put(PositionDatabase.COLUMN_SEND, pos.getSend());
		database.update(PositionDatabase.TABLE_POSITION, values, PositionDatabase.COLUMN_USER_ID+"="+pos.getUserId()+" AND "+PositionDatabase.COLUMN_USER+"='"+pos.getUser()+"'", null);
	}
	
	public int lastGameId(String user) {
		Cursor cursor = database.query(PositionDatabase.TABLE_POSITION,
									   new String[] {PositionDatabase.COLUMN_GAME},
									   PositionDatabase.COLUMN_ACT+"="+0+" AND "+PositionDatabase.COLUMN_USER+"='"+user+"'",
									   null,
									   null,
									   null,
									   null);
		
		if (cursor.moveToLast()) {
		   int i = cursor.getInt(0);
		
		   // Make sure to close the cursor
		   cursor.close();
		
		   return i;
		}
		else {
		   // Make sure to close the cursor	
		   cursor.close();
		   return 0;
		}
	}
	
	public int lastUserId(String user) {
		Cursor cursor = database.query(PositionDatabase.TABLE_POSITION,
									   new String[] {PositionDatabase.COLUMN_USER_ID},
									   PositionDatabase.COLUMN_USER+"='"+user+"'",
									   null,
									   null,
									   null,
									   null);
		
		if (cursor.moveToLast()) {
		   int i = cursor.getInt(0);
		
		   // Make sure to close the cursor
		   cursor.close();
		
		   return i;
		}
		else {
		   // Make sure to close the cursor
		   cursor.close();
		   return 0;
		}
	}
	
	public int checkUserId(String user, int userid) {
		Cursor cursor = database.query(PositionDatabase.TABLE_POSITION,
									   null,
									   PositionDatabase.COLUMN_USER_ID+"="+userid+" AND "+PositionDatabase.COLUMN_USER+"='"+user+"'",
									   null,
									   null,
									   null,
									   null);
		
		if (!cursor.moveToFirst()) {
		   // Make sure to close the cursor
		   cursor.close();
		   return 0;
		}
		else {
		   // Make sure to close the cursor			
		   cursor.close();
		   return 1;
		}
		
	}
	
	public List<Position> getAllPositions(String user) {
		positions.clear();
		
		if (database.isOpen()) {
		   Cursor cursor = database.query(PositionDatabase.TABLE_POSITION,
				   						  new String[] {PositionDatabase.COLUMN_ID, PositionDatabase.COLUMN_USER, PositionDatabase.COLUMN_USER_ID, PositionDatabase.COLUMN_LAT, PositionDatabase.COLUMN_LNG, PositionDatabase.COLUMN_BSL, PositionDatabase.COLUMN_ACT, PositionDatabase.COLUMN_TIME, PositionDatabase.COLUMN_GAME, PositionDatabase.COLUMN_ALT, PositionDatabase.COLUMN_SEND},
				   						  PositionDatabase.COLUMN_ACT+"="+1+" AND "+PositionDatabase.COLUMN_USER+"='"+user+"'",
				   						  null,
				   						  null,
				   						  null,
				   						  PositionDatabase.COLUMN_USER_ID+" ASC");
		
		   cursor.moveToFirst();
		   while (!cursor.isAfterLast()) {
				 Position position = cursorToPosition(cursor);
				 positions.add(position);
				 cursor.moveToNext();
		   }
		   
		   // Make sure to close the cursor
		   cursor.close(); 
		}
		
		return positions;
	}
	
	public List<Position> getAllPositionsNotSended(String user) {
		positions.clear();
		
		if (database.isOpen()) {
		   Cursor cursor = database.query(PositionDatabase.TABLE_POSITION,
				   						  new String[] {PositionDatabase.COLUMN_ID, PositionDatabase.COLUMN_USER, PositionDatabase.COLUMN_USER_ID, PositionDatabase.COLUMN_LAT, PositionDatabase.COLUMN_LNG, PositionDatabase.COLUMN_BSL, PositionDatabase.COLUMN_ACT, PositionDatabase.COLUMN_TIME, PositionDatabase.COLUMN_GAME, PositionDatabase.COLUMN_ALT, PositionDatabase.COLUMN_SEND},
				   						  PositionDatabase.COLUMN_SEND+"="+1+" AND "+PositionDatabase.COLUMN_USER+"='"+user+"'",
				   						  null,
				   						  null,
				   						  null,
				   						  PositionDatabase.COLUMN_USER_ID+" ASC");
		   
		   cursor.moveToFirst();
		   while (!cursor.isAfterLast()) {
				 Position position = cursorToPosition(cursor);
				 positions.add(position);
				 cursor.moveToNext();
		   }
		   
		   // Make sure to close the cursor
		   cursor.close(); 
		}
		
		return positions;
	}
	
	private Position cursorToPosition(Cursor cursor) {
		Position position = new Position();
		position.setId(cursor.getLong(0));
		position.setUser(cursor.getString(1));
		position.setUserId(cursor.getInt(2));
		position.setLat(cursor.getDouble(3));
		position.setLng(cursor.getDouble(4));
		position.setBsl(cursor.getDouble(5));
		position.setAct(cursor.getInt(6));
		position.setTime(cursor.getLong(7));
		position.setGame(cursor.getInt(8));
		position.setAlt(cursor.getDouble(9));
		position.setSend(cursor.getInt(10));
		
		return position;
	}
	
	public boolean isOpen() {
		return database.isOpen();
	}
}