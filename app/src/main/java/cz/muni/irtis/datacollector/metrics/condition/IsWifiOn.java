package cz.muni.irtis.datacollector.metrics.condition;

import android.content.Context;
import android.net.wifi.WifiManager;

public class IsWifiOn implements Condition {
    @Override
    public boolean check(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }
}
