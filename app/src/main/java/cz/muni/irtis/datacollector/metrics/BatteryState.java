package cz.muni.irtis.datacollector.metrics;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.widget.Toast;

import cz.muni.irtis.datacollector.schedule.Metric;

public class BatteryState extends Metric {

    private IntentFilter ifilter;
    private Intent batteryStatus;

    public BatteryState(Context context, Object... params) {
        super(context, params);

        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = getContext().registerReceiver(null, ifilter);
    }

    /**
     * Retrieve the battery state.
     */
    @Override
    public void run() {
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = (int) (level / (float)scale) * 100;

        Toast.makeText(getContext(), "Battery is at: " + batteryPct + "%",
                Toast.LENGTH_SHORT).show();
    }
}
