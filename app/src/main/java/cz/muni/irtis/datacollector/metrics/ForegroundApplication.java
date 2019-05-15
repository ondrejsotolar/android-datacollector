package cz.muni.irtis.datacollector.metrics;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import org.joda.time.DateTime;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import cz.muni.irtis.datacollector.database.Query;
import cz.muni.irtis.datacollector.metrics.condition.IsUsageStatsAllowed;
import cz.muni.irtis.datacollector.schedule.Metric;

public class ForegroundApplication extends Metric {
    private static final String TAG = ForegroundApplication.class.getSimpleName();
    private String current = "";

    public ForegroundApplication(Context context, Object... params) {
        super(context, params);

        addPrerequisity(new IsUsageStatsAllowed());
    }

    @Override
    public void run() {
        if (!isPrerequisitiesSatisfied()) {
            return;
        }
        String topPackageName = "none";
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);
            topPackageName = foregroundTaskInfo.topActivity.getPackageName();
        } else {
            UsageStatsManager usage = (UsageStatsManager) getContext().getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();

            List<UsageStats> stats = usage.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time);
            if (stats != null) {
                SortedMap<Long, UsageStats> runningTasks = new TreeMap<>();
                for (UsageStats usageStats : stats) {
                    runningTasks.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (!runningTasks.isEmpty()) {
                    UsageStats aa = runningTasks.get(runningTasks.lastKey());
                    if (aa != null) {
                        topPackageName = aa.getPackageName();
                    }
                }
            }
        }
        if (!"none".equals(topPackageName)) {
            current = topPackageName;
            save(DateTime.now());
        }
    }

    @Override
    public void save(DateTime dateTime, Object... params) {
        super.save(dateTime, params);
        Query.saveMetric(this);
    }

    public String getCurrent() {
        return current;
    }
}
