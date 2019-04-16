package cz.muni.irtis.datacollector.database;

import android.content.ContentValues;
import android.database.DatabaseUtils;
import android.util.Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import cz.muni.irtis.datacollector.metrics.BatteryState;
import cz.muni.irtis.datacollector.metrics.Location;
import cz.muni.irtis.datacollector.metrics.PhysicalActivity;
import cz.muni.irtis.datacollector.metrics.Screenshot;

public class Query {
    private static DatabaseHelper db = DatabaseHelper.getInstance();
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Save BatteryState metric to database.
     * @param metric battery state
     * @return
     */
    public static long saveMetric(BatteryState metric) {
        long dateTimeId = saveNewTimeEntry(metric.getDateTime());

        ContentValues cv = new ContentValues();
        cv.put(Const.COLUMN_DATETIME_ID, dateTimeId);
        cv.put(Const.COLUMN_STATE_PERCENT, metric.getLevel());

        long result = db.getWritableDatabase().insert(Const.TABLE_BATTERY_STATE, null, cv);
        logCount();
        return result;
    }

    /**
     * Save Screenshot metric to database.
     * @param metric Screenshot
     * @return
     */
    public static long saveMetric(Screenshot metric) {
        long dateTimeId = saveNewTimeEntry(metric.getDateTime());

        ContentValues cv = new ContentValues();
        cv.put(Const.COLUMN_DATETIME_ID, dateTimeId);
        cv.put(Const.COLUMN_URL, metric.getUrl());

        long result = db.getWritableDatabase().insert(Const.TABLE_SCREENSHOTS, null, cv);
        return result;
    }

    /**
     * Save Location metric to database.
     * @param metric Location
     * @return
     */
    public static long saveMetric(Location metric) {
        long dateTimeId = saveNewTimeEntry(metric.getDateTime());

        ContentValues cv = new ContentValues();
        cv.put(Const.COLUMN_DATETIME_ID, dateTimeId);
        cv.put(Const.COLUMN_LATITUDE, metric.getRoundedLat());
        cv.put(Const.COLUMN_LONGITUDE, metric.getRoundedLon());

        long result = db.getWritableDatabase().insert(Const.TABLE_GPS_LOCATION, null, cv);
        return result;
    }

    /**
     * Save Location metric to database.
     * @param metric Location
     * @return
     */
    public static long saveMetric(PhysicalActivity metric) {
        long dateTimeId = saveNewTimeEntry(metric.getDateTime());

        ContentValues cv = new ContentValues();
        cv.put(Const.COLUMN_DATETIME_ID, dateTimeId);
        cv.put(Const.COLUMN_ACTIVITY, metric.getActivity());
        cv.put(Const.COLUMN_CONFIDENCE, metric.getConfidence());

        long result = db.getWritableDatabase().insert(Const.TABLE_ACTIVITY_RECOGNITION, null, cv);
        return result;
    }

    /**
     * For testing only
     */
    private static void logCount() {
        long dCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_DATETIME);
        long bCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_BATTERY_STATE);
        long sCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_SCREENSHOTS);
        long lCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_GPS_LOCATION);
        long aCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_ACTIVITY_RECOGNITION);

        Log.d("DB Record count: ", dCount + "," + bCount+ "," + sCount+ "," + lCount + "," + aCount);
    }

    private static long saveNewTimeEntry(LocalDateTime dateTime) {
        ContentValues cv = new ContentValues();
        cv.put(Const.COLUMN_TIME_STAMP, dateTime.format(formatter));

        long result = db.getWritableDatabase().insert(Const.TABLE_DATETIME, null, cv);
        return result;
    }
}
