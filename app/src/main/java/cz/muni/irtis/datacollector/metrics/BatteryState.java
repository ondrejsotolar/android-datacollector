package cz.muni.irtis.datacollector.metrics;

import android.content.Context;
import android.widget.Toast;

import cz.muni.irtis.datacollector.schedule.Metric;

public class BatteryState extends Metric {

    public BatteryState(Context context, Object... params) {
        super(context, params);
    }

    @Override
    public void run() {
        int delay = (int) getParams()[0];
        Toast.makeText(getContext(), "Its been "+ delay + " seconds", Toast.LENGTH_SHORT).show();
    }
}
