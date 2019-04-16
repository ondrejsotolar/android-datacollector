package cz.muni.irtis.datacollector.metrics.util.physicalactivity;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 * Used as pending intent for ActivityRecognitionClient listener.
 * Results are sent back with a local broadcast.
 */
public class DetectedActivitiesIntentService extends IntentService {
    private static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();

    public DetectedActivitiesIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Get most probable activity & broadcast it
     * @param intent
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        DetectedActivity activity = result.getMostProbableActivity();
        broadcastActivity(activity);
    }

    /**
     * Send local broadcast with result.
     * @param activity
     */
    private void broadcastActivity(DetectedActivity activity) {
        Intent intent = new Intent("activity_intent");
        intent.putExtra("type", activity.getType());
        intent.putExtra("confidence", activity.getConfidence());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
