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

    final Handler delayHandler;
    final int delay10;
    private List<Metric> every10Seconds;

    public TaskScheduler(int testDelay) {
        delay10 = testDelay;
        delayHandler = new Handler();
        every10Seconds = new ArrayList<>();
    }

    public void addMetric(Metric metric) {
        every10Seconds.add(metric);
    }

    public void startCapturingPeriodicaly() {
        delayHandler.postDelayed(new Runnable() {
            public void run() {
                startCapture();
                delayHandler.postDelayed(this, delay10);
            }
        }, delay10);
    }

    public void onDestroy() {
        delayHandler.removeCallbacksAndMessages(null);
    }

    private void startCapture() {
        for (int i = 0; i< every10Seconds.size(); i++) {
            every10Seconds.get(i).run();
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
