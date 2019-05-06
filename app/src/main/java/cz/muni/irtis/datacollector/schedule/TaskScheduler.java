package cz.muni.irtis.datacollector.schedule;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskScheduler {
    private static final String TAG = TaskScheduler.class.getSimpleName();

    private final Handler delayHandler;
    private final int delay1 = 1;
    private final int delay10;
    private List<Metric> every1Second;
    private List<Metric> every10Seconds;

    public TaskScheduler(int testDelay) {
        delay10 = testDelay;
        delayHandler = new Handler();
        every1Second = new ArrayList<>();
        every10Seconds = new ArrayList<>();
    }

    /**
     * Add a runnable metric.
     * @param metric
     */
    public void addMetric(Metric metric, int interval) {
        switch (interval) {
            case 10: every10Seconds.add(metric); break;
            case 1 : every1Second.add(metric); break;
            default: throw new IllegalStateException(TAG + "Undefined interval: " + metric.getClass().getSimpleName());
        }

    }

    /**
     * Starts repeating action with delay.
     */
    public void startCapturingPeriodicaly() {
        delayHandler.postDelayed(new Runnable() {
            public void run() {
                startCapture10();
                delayHandler.postDelayed(this, delay10);
            }
        }, delay10);
        delayHandler.postDelayed(new Runnable() {
            public void run() {
                startCapture1();
                delayHandler.postDelayed(this, delay1);
            }
        }, delay1);
    }

    /**
     * Stop all running tasks and clean the callbacks.
     */
    public void onDestroy() {
        delayHandler.removeCallbacksAndMessages(null);
        for (int i = 0; i < every10Seconds.size(); i++) {
            every10Seconds.get(i).stop();
        }
        for (int i = 0; i < every1Second.size(); i++) {
            every1Second.get(i).stop();
        }
    }

    public List<String> get10sMetricNames() {
        List<String> names = new ArrayList<>(every10Seconds.size());
        for (int i = 0; i < every10Seconds.size(); i++) {
            names.add(every10Seconds.get(i).getClass().getSimpleName());
        }
        return names;
    }

    private void startCapture10() {
        for (int i = 0; i < every10Seconds.size(); i++) {
            every10Seconds.get(i).run();
        }
    }

    private void startCapture1() {
        for (int i = 0; i < every1Second.size(); i++) {
            every1Second.get(i).run();
        }
    }

    // TODO: remove.
    private ScheduledExecutorService scheduleTaskExecutor;
    private int counter = 0;
    public void startScheduledTask(final Context context) {
        scheduleTaskExecutor = Executors.newScheduledThreadPool(5);

        //Schedule a task to run every 5 seconds (or however long you want)
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // Do only background stuff  here!
                Log.d("TaskScheduler: ", "call count: " + (++counter));

                // UI thread
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Its been 5 seconds", Toast.LENGTH_SHORT).show();
                    }
                };
                action.run();
            }
        }, 0, 5, TimeUnit.SECONDS);
    }
}
