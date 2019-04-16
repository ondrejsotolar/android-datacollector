package cz.muni.irtis.datacollector.metrics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import cz.muni.irtis.datacollector.metrics.util.physicalactivity.BackgroundDetectedActivitiesService;
import cz.muni.irtis.datacollector.metrics.util.physicalactivity.RecognizedActivity;
import cz.muni.irtis.datacollector.schedule.Metric;

public class PhysicalActivity extends Metric {

    private BroadcastReceiver broadcastReceiver;
    private int delayMilis;

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

    private void initBroadcastreceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("activity_intent")) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);

                    Log.w("PhysicalActivity", "User activity: " + type +
                            ", Confidence: " + confidence);
                    Toast.makeText(getContext(), "User activity: " +
                                    RecognizedActivity.toString(type) + ", Confidence: " + confidence,
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        };
    }
}
