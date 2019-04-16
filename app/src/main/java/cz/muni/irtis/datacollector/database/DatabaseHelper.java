package cz.muni.irtis.datacollector.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int SCHEMA = 1;
    private static DatabaseHelper singleton = null;

    // In memory
    private DatabaseHelper(Context context) {
        super(context, null /*Const.DATABASE_NAME*/, null, SCHEMA);
    }

    /**
     * Get or create instance, if first call.
     * @param context App context. Should be MainActivity.
     * @return instance
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (singleton == null) {
            singleton = new DatabaseHelper(context.getApplicationContext());
        }
        return singleton;
    }

    /**
     * Get instance. Throws if previously not initialized with context.
     * @return instance
     */
    public static synchronized DatabaseHelper getInstance() {
        if (singleton == null) {
            throw new RuntimeException("Database not initialized!");
        }
        return singleton;
    }

    /**
     * Create tables (on first run).
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " +
                Const.TABLE_DATETIME + "(" +
                Const.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Const.COLUMN_TIME_STAMP + " DATETIME);"
        );
        db.execSQL("CREATE TABLE " +
                Const.TABLE_BATTERY_STATE + "( " +
                Const.COLUMN_STATE_PERCENT + " INTEGER, " +
                Const.COLUMN_DATETIME_ID + " INTEGER, " +
                "FOREIGN KEY (" + Const.COLUMN_DATETIME_ID + ") " +
                "REFERENCES " + Const.TABLE_DATETIME + "(" + Const.ID + "));"
        );
        db.execSQL("CREATE TABLE " +
                Const.TABLE_SCREENSHOTS + "( " +
                Const.COLUMN_URL + " TEXT, " +
                Const.COLUMN_DATETIME_ID + " INTEGER, " +
                "FOREIGN KEY (" + Const.COLUMN_DATETIME_ID + ") " +
                "REFERENCES " + Const.TABLE_DATETIME + "(" + Const.ID + "));"
        );
        db.execSQL("CREATE TABLE " +
                Const.TABLE_GPS_LOCATION + "( " +
                Const.COLUMN_LATITUDE + " REAL, " +
                Const.COLUMN_LONGITUDE + " REAL, " +
                Const.COLUMN_DATETIME_ID + " INTEGER, " +
                "FOREIGN KEY (" + Const.COLUMN_DATETIME_ID + ") " +
                "REFERENCES " + Const.TABLE_DATETIME + "(" + Const.ID + "));"
        );
        db.execSQL("CREATE TABLE " +
                Const.TABLE_ACTIVITY_RECOGNITION + "( " +
                Const.COLUMN_ACTIVITY + " TEXT, " +
                Const.COLUMN_CONFIDENCE + " INTEGER, " +
                Const.COLUMN_DATETIME_ID + " INTEGER, " +
                "FOREIGN KEY (" + Const.COLUMN_DATETIME_ID + ") " +
                "REFERENCES " + Const.TABLE_DATETIME + "(" + Const.ID + "));"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new RuntimeException("Not implemented!");
    }
}