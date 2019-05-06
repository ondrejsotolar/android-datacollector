package cz.muni.irtis.datacollector.database;

/**
 * Database constants
 */
class Const {
    static final String DATABASE_NAME = "datacollector.db";
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

    static final String TABLE_WIFI_SSID = "wifi_ssid";
    static final String COLUMN_SSID = "ssid";

    static final String TABLE_AVAILABLE_WIFI = "available_wifi";
    static final String COLUMN_WIFI_SSID_ID = "wifi_ssid_id";

    static final String TABLE_CONNECTED_WIFI = "connected_wifi";

    static final String TABLE_CALL_HISTORY = "call_history";
    static final String COLUMN_NAME = "name";
    static final String COLUMN_PHONE_NUMBER = "phone_number";
    static final String COLUMN_TYPE = "type";
    static final String COLUMN_DURATION = "duration";
    static final String COLUMN_CALL_DATE = "call_date";

    static final String TABLE_SMS_CONVERSATION = "sms_conversation";
    static final String COLUMN_CONTENT = "content";
    static final String COLUMN_MESSAGE_DATE = "message_date";
}
