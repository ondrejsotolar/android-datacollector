package cz.muni.irtis.datacollector.metrics;

import android.content.Context;
import android.os.BatteryManager;
import android.util.Log;

import java.time.LocalDateTime;

import cz.muni.irtis.datacollector.database.Query;
import cz.muni.irtis.datacollector.metrics.condition.IsScreenOn;
import cz.muni.irtis.datacollector.schedule.Metric;

import static android.content.Context.BATTERY_SERVICE;

public class BatteryState extends Metric {
    private int currentLevel = -1;

    public BatteryState(Context context, Object... params) {
        super(context, params);
        addPrerequisity(new IsScreenOn());
    }

    /**
     * Retrieve the battery state.
     */
    @Override
    public void run() {
        if (!isConditionsSatisfied())
            return;

        BatteryManager bm = (BatteryManager) getContext().getSystemService(BATTERY_SERVICE);
        currentLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        save(LocalDateTime.now());
    }

    /**
     * Save recent metric to DB
     */
    @Override
    public void save(LocalDateTime dateTime, Object... params) {
        super.save(dateTime, params);
        Query.saveMetric(this);
    }

    public int getLevel() {
        return currentLevel;
    }

    private boolean isConditionsSatisfied() {
        if (!getPrerequisity(IsScreenOn.class).check(getContext())) {
            Log.d(getClass().getSimpleName() + " metric:","Screen is off - battery state will not checked.");
            return false;
        }
        return true;
    }
}
