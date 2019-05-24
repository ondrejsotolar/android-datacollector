package cz.muni.irtis.datacollector.schedule;

import android.os.Debug;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskScheduler {
    public static final int ONE_SECOND = 1000;
    static final int ONCE = 0;
    private static final String TAG = TaskScheduler.class.getSimpleName();
    private final Handler delayHandler;
    private HashMap<Integer, List<Metric>> metrics;

    public TaskScheduler() {
        delayHandler = new Handler();
        metrics = new HashMap<>();
    }

    /**
     * Add a runnable metric.
     * @param metric
     */
    public void addMetric(Metric metric, Integer interval) {
        List<Metric> existingGroup = metrics.get(interval);
        if (existingGroup == null) {
            existingGroup = new ArrayList<>();
            existingGroup.add(metric);
            metrics.put(interval, existingGroup);
        } else {
            existingGroup.add(metric);
        }
    }

    /**
     * Starts repeating action with delay.
     */
    public void startCapturingPeriodicaly() {
        for (final Map.Entry<Integer, List<Metric>> entry: metrics.entrySet()) {
            if (entry.getKey() > 0) {
                delayHandler.postDelayed(new Runnable() {
                    public void run() {
                        startCapture(entry.getKey(), entry.getValue());
                        delayHandler.postDelayed(this, entry.getKey());
                    }
                }, entry.getKey());
            } else {
                switch (entry.getKey()) {
                    case ONCE: startCapture(entry.getKey(), entry.getValue()); break;
                    default: throw new IllegalStateException(
                            TAG + ": Can't run metrics with undefined interval: " + entry.getKey());
                }
            }
        }
    }

    /**
     * Stop all running tasks and clean the callbacks.
     */
    public void onDestroy() {
        delayHandler.removeCallbacksAndMessages(null);
        for (final Map.Entry<Integer, List<Metric>> entry: metrics.entrySet()) {
            for (Metric m: entry.getValue()) {
                m.stop();
            }
        }
    }

    public void onConfigurationChanged(Object... params) {
        // TODO: re-init ImageTransmogrifer and others..
    }

    private void startCapture(Integer interval, List<Metric> toRun) {
        for (Metric m : toRun) {
            try {
                m.run();
            } catch (Exception e) {
                Log.e(TAG, m.getClass().getSimpleName() + ": " + e.toString());
            }
        }
    }
}
