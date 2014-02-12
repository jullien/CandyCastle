package candy.castle;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PositionDatabase extends SQLiteOpenHelper {
	
	public static final String TABLE_POSITION = "position";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_USER = "user";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_LAT = "lat";
	public static final String COLUMN_LNG = "lng";
	public static final String COLUMN_ALT = "alt";
	public static final String COLUMN_BSL = "bsl";
	public static final String COLUMN_ACT = "act";
	public static final String COLUMN_TIME = "time";
	public static final String COLUMN_GAME = "game";
	public static final String COLUMN_SEND = "send";

	private static final String DATABASE_NAME = "position.db";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = "create table "
			+ TABLE_POSITION + "( " + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_USER
			+ " string not null, " + COLUMN_USER_ID
			+ " integer not null, " + COLUMN_LAT
			+ " double not null, " + COLUMN_LNG
			+ " double not null, " + COLUMN_ALT
			+ " double not null, " + COLUMN_BSL
			+ " double not null, " + COLUMN_ACT
			+ " integer not null, " + COLUMN_TIME
			+ " long not null, " + COLUMN_GAME
			+ " integer not null, " + COLUMN_SEND
			+ " integer not null" + ");";

	public PositionDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(PositionDatabase.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSITION);
		onCreate(db);
	}
}
