package cz.muni.irtis.datacollector.database;

/**
 * SQL queries
 */
class SQL {
    static final String SELECT_WIFI_SSID =
            "SELECT "+ Const.ID +
                    " FROM "+ Const.TABLE_WIFI_SSID +
                    " WHERE "+ Const.COLUMN_SSID + " = ?";

    static final String SELECT_ALL_WIFI_SSID =
            "SELECT "+ Const.COLUMN_SSID +
                    " FROM "+ Const.TABLE_WIFI_SSID;

    static final String SELECT_MAX_CALL_DATE =
            "SELECT max("+ Const.COLUMN_CALL_DATE +
                    ") FROM "+ Const.TABLE_CALL_HISTORY;

    static final String SELECT_MAX_MESSAGE_DATE =
            "SELECT max("+ Const.COLUMN_MESSAGE_DATE +
                    ") FROM "+ Const.TABLE_SMS_CONVERSATION;
}
