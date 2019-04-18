package cz.muni.irtis.datacollector.database;

class SQL {
    static final String SELECT_WIFI_SSID =
            "SELECT "+ Const.ID +
                    " FROM "+ Const.TABLE_WIFI_SSID +
                    " WHERE "+ Const.COLUMN_SSID + " = ?";

    static final String SELECT_ALL_WIFI_SSID =
            "SELECT "+ Const.COLUMN_SSID +
                    " FROM "+ Const.TABLE_WIFI_SSID;
}
