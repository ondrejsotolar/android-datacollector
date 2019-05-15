package cz.muni.irtis.datacollector.metrics;

import android.content.Context;
import android.os.BatteryManager;

import org.joda.time.DateTime;

import cz.muni.irtis.datacollector.database.Query;
import cz.muni.irtis.datacollector.metrics.condition.IsScreenOn;
import cz.muni.irtis.datacollector.schedule.Metric;

import static android.content.Context.BATTERY_SERVICE;

/**
 * Capture battery state
 */
public class BatteryState extends Metric {
    private int currentLevel = -1;

    public BatteryState(Context context, Object... params) {
        super(context, params);
    }

    /**
     * Retrieve the battery state.
     */
    @Override
    public void run() {
        BatteryManager bm = (BatteryManager) getContext().getSystemService(BATTERY_SERVICE);
        currentLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        save(DateTime.now());
    }

    /**
     * Save recent metric to DB
     */
    @Override
    public void save(DateTime dateTime, Object... params) {
        super.save(dateTime, params);
        Query.saveMetric(this);
    }

    public int getLevel() {
        return currentLevel;
    }
}
