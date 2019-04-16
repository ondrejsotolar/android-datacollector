package cz.muni.irtis.datacollector.database;

class Const {
    static final String DATABASE_NAME = "metrics.db";
    static final String TITLE = "title";
    static final String VALUE = "value";
    static final String ID = "id";

    static final String TABLE_DATETIME = "datetime";
    static final String COLUMN_TIME_STAMP = "time_stamp";

    static final String TABLE_BATTERY_STATE = "battery_state";
    static final String COLUMN_DATETIME_ID = "datetime_id";
    static final String COLUMN_STATE_PERCENT = "state_percent";

    static final String TABLE_SCREENSHOTS = "screenshots";
    static final String COLUMN_URL = "device_url";

    static final String TABLE_GPS_LOCATION = "gps_location";
    static final String COLUMN_LATITUDE = "latitude";
    static final String COLUMN_LONGITUDE = "longitude";

    static final String TABLE_ACTIVITY_RECOGNITION = "activity_recognition";
    static final String COLUMN_ACTIVITY = "activity";
    static final String COLUMN_CONFIDENCE = "confidence";
}
