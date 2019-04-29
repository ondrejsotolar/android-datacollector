package cz.muni.irtis.datacollector.metrics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.time.LocalDateTime;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import cz.muni.irtis.datacollector.database.Query;
import cz.muni.irtis.datacollector.metrics.util.physicalactivity.BackgroundDetectedActivitiesService;
import cz.muni.irtis.datacollector.metrics.util.physicalactivity.RecognizedActivity;
import cz.muni.irtis.datacollector.schedule.Metric;

/**
 * Capture physical activity
 */
public class PhysicalActivity extends Metric {
    private BroadcastReceiver broadcastReceiver;
    private int delayMilis;
    private int type;
    private int confidence;

    public PhysicalActivity(Context context, Object... params) {
        super(context, params);
        delayMilis = (int) params[0];
        initBroadcastreceiver();
    }

    /**
     * Register location updates per delay from starting intent
     */
    @Override
    public void run() {
        if (!isRunning()) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver,
                    new IntentFilter("activity_intent"));

            Intent intent = new Intent(getContext(), BackgroundDetectedActivitiesService.class);
            intent.putExtra("delayMilis", delayMilis);
            getContext().startService(intent);

            setRunning(true);
        }
    }

    /**
     * Stop receiving location updates
     */
    @Override
    public void stop() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);

        Intent intent = new Intent(getContext(), BackgroundDetectedActivitiesService.class);
        getContext().stopService(intent);

        setRunning(false);
    }

    public String getActivity() {
        return RecognizedActivity.toString(type);
    }

    public int getConfidence() {
        return confidence;
    }

    @Override
    public void save(LocalDateTime dateTime, Object... params) {
        super.save(dateTime, params);
        Query.saveMetric(this);
    }

    private void initBroadcastreceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("activity_intent".equals(intent.getAction())) {
                    type = intent.getIntExtra("type", -1);
                    confidence = intent.getIntExtra("confidence", 0);
                    save(LocalDateTime.now());
                }
            }
        };
    }
}
