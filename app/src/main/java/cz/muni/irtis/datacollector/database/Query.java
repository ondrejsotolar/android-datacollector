package cz.muni.irtis.datacollector.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import cz.muni.irtis.datacollector.metrics.BatteryState;
import cz.muni.irtis.datacollector.metrics.Location;
import cz.muni.irtis.datacollector.metrics.PhysicalActivity;
import cz.muni.irtis.datacollector.metrics.Screenshot;
import cz.muni.irtis.datacollector.metrics.Wifi;

public class Query {
    private static final String TAG = Query.class.getSimpleName();

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
     * Save Wifi metric to database: new, available & connected
     *
     * @param metric Wifi
     * @return 0 (nothing to return)
     */
    public static long saveMetric(Wifi metric) {
        long dateTimeId = saveNewTimeEntry(metric.getDateTime());

        // save new & available networks
        for (int i = 0; i < metric.getSsidList().size(); i++) {
            long ssidId = getWifiSsid(metric.getSsidList().get(i));
            if (ssidId <= 0) {
                ssidId = saveNewWifiSsid(metric.getSsidList().get(i));
            }
            saveAvailableWifi(dateTimeId, ssidId);
        }

        // save connected wifi
        long connectedId = getWifiSsid(metric.getConnectedSsid());
        if (connectedId <= 0) {
            outputWifis();
            throw new IllegalStateException(TAG + ": connected wifi not yet saved to DB!");
        }
        saveConnectedWifi(dateTimeId, connectedId);
        outputWifis(); // TODO: remove. for debugging only.
        return 0;
    }

    private static long saveNewTimeEntry(LocalDateTime dateTime) {
        ContentValues cv = new ContentValues();
        cv.put(Const.COLUMN_TIME_STAMP, dateTime.format(formatter));

        long result = db.getWritableDatabase().insert(Const.TABLE_DATETIME, null, cv);
        return result;
    }

    private static long getWifiSsid(String ssid) {
        Cursor result = db.getReadableDatabase().rawQuery(SQL.SELECT_WIFI_SSID, new String[]{ssid});
        long id = -1;
        if (result.moveToFirst()) {
            id = result.getLong(0);
        }
        result.close();
        return id;
    }

    private static List<String> getSavedWifis() {
        Cursor result = db.getReadableDatabase().rawQuery(SQL.SELECT_ALL_WIFI_SSID, null);
        List<String> names = new ArrayList<>();
        if (result.moveToFirst()){
            do {
                names.add(result.getString(0));
            } while(result.moveToNext());
        }
        result.close();
        return names;
    }

    private static long saveNewWifiSsid(String ssid) {
        ContentValues cv = new ContentValues();
        cv.put(Const.COLUMN_SSID, ssid);

        long result = db.getWritableDatabase().insert(Const.TABLE_WIFI_SSID, null, cv);
        return result;
    }

    private static void saveAvailableWifi(long datatimeId,  long ssidId) {
        ContentValues cv = new ContentValues();
        cv.put(Const.COLUMN_DATETIME_ID, datatimeId);
        cv.put(Const.COLUMN_WIFI_SSID_ID, ssidId);

        db.getWritableDatabase().insert(Const.TABLE_AVAILABLE_WIFI, null, cv);
    }

    private static void saveConnectedWifi(long datatimeId, long ssidId) {
        ContentValues cv = new ContentValues();
        cv.put(Const.COLUMN_DATETIME_ID, datatimeId);
        cv.put(Const.COLUMN_WIFI_SSID_ID, ssidId);

        db.getWritableDatabase().insert(Const.TABLE_CONNECTED_WIFI, null, cv);
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
        long wCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_WIFI_SSID);
        long vCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_AVAILABLE_WIFI);
        long cCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_CONNECTED_WIFI);

        Log.d(TAG, dCount + "," + bCount+ "," + sCount+ "," + lCount + "," + aCount
                + "," + wCount + "," + vCount + "," + cCount);
    }

    private static void outputWifis() {
        List<String> names = getSavedWifis();
        String out = "";
        for (String s : names) {
            out += s + ",";
        }
        Log.d(TAG, out);
    }
}
