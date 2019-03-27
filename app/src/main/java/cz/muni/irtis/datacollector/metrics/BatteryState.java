package cz.muni.irtis.datacollector.metrics;

import android.content.Context;
import android.os.BatteryManager;
import android.widget.Toast;

import java.time.LocalDateTime;

import cz.muni.irtis.datacollector.database.Query;
import cz.muni.irtis.datacollector.schedule.Metric;

import static android.content.Context.BATTERY_SERVICE;

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

        Toast.makeText(getContext(), "Battery is at: " + currentLevel + "%",
                Toast.LENGTH_SHORT).show();

        save(LocalDateTime.now());
    }

    /**
     * Save recent metric to DB
     */
    @Override
    public void save(LocalDateTime dateTime, Object... params) {
        setDateTime(dateTime);
        Query.saveMetric(this);
    }

    public int getLevel() {
        return currentLevel;
    }
}
