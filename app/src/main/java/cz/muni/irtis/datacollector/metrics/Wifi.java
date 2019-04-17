package cz.muni.irtis.datacollector.metrics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.List;

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

    public Wifi(Context context, Object... params) {
        super(context, params);
        wifiScanReceiver = new WifiScanReceiver();
        wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        getContext().registerReceiver(wifiScanReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        addPrerequisity(new IsWifiOn());
    }

    @Override
    public void run() {
        if (!isPrerequisitiesSatisfied())
            return;

        wifiManager.startScan();
        setRunning(true);
    }

    public List<String> getSsidList() {
        return ssidList;
    }

    /**
     * Capture unique SSIDs
     */
    private class WifiScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                List<ScanResult> scanResults = wifiManager.getScanResults();
                ssidList = new ArrayList<>(scanResults.size());

                for (int i = 0; i < scanResults.size(); i++) {
                    if (!ssidList.contains(scanResults.get(i).SSID)) {
                        ssidList.add(scanResults.get(i).SSID);
                    }
                }
            }
        }
    }
}
