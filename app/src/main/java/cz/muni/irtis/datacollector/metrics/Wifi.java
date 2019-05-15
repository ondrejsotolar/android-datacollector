package cz.muni.irtis.datacollector.metrics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import cz.muni.irtis.datacollector.database.Query;
import cz.muni.irtis.datacollector.metrics.condition.IsLocationOn;
import cz.muni.irtis.datacollector.metrics.condition.IsWifiOn;
import cz.muni.irtis.datacollector.schedule.Metric;

/**
 * Capture available WiFi SSIDs
 */
public class Wifi extends Metric {
    private final String TAG = this.getClass().getSimpleName();

    private WifiManager wifiManager;
    private WifiScanReceiver wifiScanReceiver;
    private List<String> ssidList;
    private String connectedSsid;

    public Wifi(Context context, Object... params) {
        super(context, params);
        wifiScanReceiver = new WifiScanReceiver();
        wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);

        getContext().registerReceiver(wifiScanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        addPrerequisity(new IsWifiOn());
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            addPrerequisity(new IsLocationOn());
        }
    }

    @Override
    public void run() {
        if (!isPrerequisitiesSatisfied())
            return;

        wifiManager.startScan();
        setRunning(true);
    }

    @Override
    public void save(DateTime dateTime, Object... params) {
        super.save(dateTime, params);
        Query.saveMetric(this, params);
    }

    private boolean isValidSsid(String ssid) {
        boolean result =
                ssid != null &&
                !"".equals(ssid) &&
                !"<unknown ssid>".equals(ssid);
        return result;
    }

    /**
     * Capture unique SSIDs
     */
    private class WifiScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                List<ScanResult> scanResults = wifiManager.getScanResults();
                List<String> available = new ArrayList<>(scanResults.size());
                for (int i = 0; i < scanResults.size(); i++) {
                    if (!available.contains(scanResults.get(i).SSID)) {
                        if (isValidSsid(scanResults.get(i).SSID)) {
                            available.add(scanResults.get(i).SSID);
                        }
                    }
                }
                // TODO: UTF8 has double quot marks. recognize or remove.
                String connected = wifiManager.getConnectionInfo().getSSID().replace("\"", "");
                if (isValidSsid(connected)) {
                    save(DateTime.now(), available, connected);
                } else {
                    save(DateTime.now(), available);
                }
            }
        }
    }
}
