package cz.muni.irtis.datacollector.metrics.report;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import cz.muni.irtis.datacollector.metrics.condition.IsScreenOn;
import cz.muni.irtis.datacollector.schedule.Metric;

public class Runtime extends Metric {
    private long tStart = -1;

    public Runtime(Context context, Object... params) {
        super(context, params);
        addPrerequisity(new IsScreenOn());
    }

    @Override
    public void run() {
        if (!isPrerequisitiesSatisfied())
            return;

        if (tStart < 0) {
            tStart = System.currentTimeMillis();
        } else {
            broadcastElapsedTime();
        }
    }

    @Override
    public void stop() {
        super.stop();
        tStart = -1;
    }

    private void broadcastElapsedTime() {
        String elapsed = calculateElapsedTime();
        Intent intent = new Intent("elapsed_time");
        intent.putExtra("elapsed", elapsed);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
    }

    private String calculateElapsedTime() {
        long tEnd = System.currentTimeMillis();
        long elapsedMilis = tEnd - tStart;

        int seconds = (int) (elapsedMilis / 1000);
        int minutes = (int) (seconds / 60);
        int hours = (int) (minutes / 60);

        return String.format("%s:%s:%s",
                longify(hours),
                longify(minutes % 59),
                longify(seconds % 59));
    }

    private String longify(int time) {
        String t = String.valueOf(time);
        return t.length() < 2 ? "0" + t : t;
    }
}
