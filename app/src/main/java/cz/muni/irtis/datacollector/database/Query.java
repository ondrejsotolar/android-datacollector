package cz.muni.irtis.datacollector.database;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import cz.muni.irtis.datacollector.metrics.BatteryState;

public class Query {
    private static DatabaseHelper db = DatabaseHelper.getInstance();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Save metric to database.
     * @param batteryState battery state
     * @return
     */
    public static long saveMetric(BatteryState batteryState) {
        long dateTimeId = saveNewTimeEntry(batteryState.getDateTime());

        ContentValues cv = new ContentValues();
        cv.put(Const.COLUMN_DATETIME_ID, dateTimeId);
        cv.put(Const.COLUMN_STATE_PERCENT, batteryState.getLevel());

        long result = db.getWritableDatabase().insert(Const.TABLE_BATTERY_STATE, null, cv);
        logCount();
        return result;
    }

    /**
     * For testing only
     */
    public static void logCount() {
        long dCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_DATETIME);
        long bCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_BATTERY_STATE);

        Log.d("DB Record count: ", dCount + "," + bCount);
    }

    private static long saveNewTimeEntry(LocalDateTime dateTime) {
        ContentValues cv = new ContentValues();
        cv.put(Const.COLUMN_TIME_STAMP, dateTime.format(formatter));

        long result = db.getWritableDatabase().insert(Const.TABLE_DATETIME, null, cv);
        return result;
    }
}
