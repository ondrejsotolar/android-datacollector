package cz.muni.irtis.datacollector.metrics;

import android.content.Context;

import cz.muni.irtis.datacollector.schedule.Metric;

public class ForegroundApplication extends Metric {
    public ForegroundApplication(Context context, Object... params) {
        super(context, params);
    }

    @Override
    public void run() {

    }
}
