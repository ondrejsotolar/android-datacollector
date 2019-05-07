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
import cz.muni.irtis.datacollector.metrics.CallHistory;
import cz.muni.irtis.datacollector.metrics.ForegroundApplication;
import cz.muni.irtis.datacollector.metrics.Location;
import cz.muni.irtis.datacollector.metrics.PhysicalActivity;
import cz.muni.irtis.datacollector.metrics.Screenshot;
import cz.muni.irtis.datacollector.metrics.SmsConversation;
import cz.muni.irtis.datacollector.metrics.Wifi;

/**
 * Make queries to the local database
 */
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
    public static long saveMetric(Wifi metric, Object... params) {
        long dateTimeId = saveNewTimeEntry(metric.getDateTime());

        List<String> available = (List<String>) params[0];
        String connected = null;
        if (params.length > 1) {
            connected = (String) params[1];
        }

        // save new & available networks
        for (int i = 0; i < available.size(); i++) {
            long ssidId = getWifiSsid(available.get(i));
            if (ssidId <= 0) {
                ssidId = saveNewWifiSsid(available.get(i));
            }
            saveAvailableWifi(dateTimeId, ssidId);
        }

        // save connected wifi
        if (connected != null) {
            long connectedId = getWifiSsid(connected);
            if (connectedId <= 0) {
                outputWifis();
                throw new IllegalStateException(TAG + ": connected wifi not yet saved to DB!");
            }
            saveConnectedWifi(dateTimeId, connectedId);
        }
        outputWifis(); // TODO: remove. for debugging only.
        return 0;
    }

    /**
     * Save CallHistory metric to database.
     * @param metric CallHistory
     * @return
     */
    public static long saveMetric(CallHistory metric) {
        long dateTimeId = saveNewTimeEntry(metric.getDateTime());

        for (int i = 0; i < metric.getRecords().size(); i++) {
            ContentValues cv = new ContentValues();
            cv.put(Const.COLUMN_DATETIME_ID, dateTimeId);
            cv.put(Const.COLUMN_NAME, metric.getRecords().get(i).getName());
            cv.put(Const.COLUMN_PHONE_NUMBER, metric.getRecords().get(i).getPhoneNumber());
            cv.put(Const.COLUMN_TYPE, metric.getRecords().get(i).getType());
            cv.put(Const.COLUMN_DURATION, metric.getRecords().get(i).getDuration());
            cv.put(Const.COLUMN_CALL_DATE, metric.getRecords().get(i).getCallDate());
            db.getWritableDatabase().insert(Const.TABLE_CALL_HISTORY, null, cv);
        }
        return 0;
    }

    /**
     * Save SmsConversation metric to database.
     * @param metric SmsConversation
     * @return
     */
    public static long saveMetric(SmsConversation metric) {
        long dateTimeId = saveNewTimeEntry(metric.getDateTime());

        for (int i = 0; i < metric.getRecords().size(); i++) {
            ContentValues cv = new ContentValues();
            cv.put(Const.COLUMN_DATETIME_ID, dateTimeId);
            cv.put(Const.COLUMN_PHONE_NUMBER, metric.getRecords().get(i).getPhoneNumber());
            cv.put(Const.COLUMN_TYPE, metric.getRecords().get(i).getType());
            cv.put(Const.COLUMN_CONTENT, metric.getRecords().get(i).getContent());
            cv.put(Const.COLUMN_MESSAGE_DATE, metric.getRecords().get(i).getMessageDate());
            db.getWritableDatabase().insert(Const.TABLE_SMS_CONVERSATION, null, cv);
        }
        return 0;
    }

    /**
     * Save ForegroundApplication metric to database: new, current
     *
     * @param metric ForegroundApplication
     * @return 0 (nothing to return)
     */
    public static long saveMetric(ForegroundApplication metric, Object... params) {
        long dateTimeId = saveNewTimeEntry(metric.getDateTime());
        String current = metric.getCurrent();

        // save new apps
        long appId = getAppId(current);
        if (appId <= 0) {
            appId = saveNewAppName(current);
        }

        // save foreground app
        saveForegroundApp(dateTimeId, appId);
        return 0;
    }

    /**
     * Get latest call date
     * @return long (INTEGER) milliseconds since epoch
     */
    public static long getMaxCallDate() {
        Cursor result = db.getReadableDatabase().rawQuery(SQL.SELECT_MAX_CALL_DATE, null);
        long date = -1;
        if (result.moveToFirst()) {
            date = result.getLong(0);
        }
        result.close();
        return date;
    }

    /**
     * Get latest message date
     * @return long (INTEGER) milliseconds since epoch
     */
    public static long getMaxMessageDate() {
        Cursor result = db.getReadableDatabase().rawQuery(SQL.SELECT_MAX_MESSAGE_DATE, null);
        long date = -1;
        if (result.moveToFirst()) {
            date = result.getLong(0);
        }
        result.close();
        return date;
    }

    private static long saveNewTimeEntry(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalStateException("Datetime is null!");
        }
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

    private static long getAppId(String name) {
        Cursor result = db.getReadableDatabase().rawQuery(SQL.SELECT_APP_ID, new String[]{name});
        long id = -1;
        if (result.moveToFirst()) {
            id = result.getLong(0);
        }
        result.close();
        return id;
    }

    private static long saveNewAppName(String name) {
        ContentValues cv = new ContentValues();
        cv.put(Const.COLUMN_NAME, name);

        long result = db.getWritableDatabase().insert(Const.TABLE_APP_NAME, null, cv);
        return result;
    }

    private static long saveForegroundApp(long datatimeId,  long appId) {
        ContentValues cv = new ContentValues();
        cv.put(Const.COLUMN_DATETIME_ID, datatimeId);
        cv.put(Const.COLUMN_APP_NAME_ID, appId);

        long result = db.getWritableDatabase().insert(Const.TABLE_FOREGROUND_APP, null, cv);
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
        long wCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_WIFI_SSID);
        long vCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_AVAILABLE_WIFI);
        long cCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_CONNECTED_WIFI);
        long hCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_CALL_HISTORY);
        long smsCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_SMS_CONVERSATION);
        long appNameCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_APP_NAME);
        long fAppCount = DatabaseUtils.queryNumEntries(db.getReadableDatabase(), Const.TABLE_FOREGROUND_APP);

        Log.d(TAG, dCount + "," + bCount+ "," + sCount+ "," + lCount + "," + aCount
                + "," + wCount + "," + vCount + "," + cCount + "," + hCount + "," + smsCount
                + "," + appNameCount + "," + fAppCount);
    }

    /**
     * For testing only
     */
    private static void outputWifis() {
        List<String> names = getSavedWifis();
        String out = "";
        for (String s : names) {
            out += s + ",";
        }
        Log.d(TAG, out);
    }
}
