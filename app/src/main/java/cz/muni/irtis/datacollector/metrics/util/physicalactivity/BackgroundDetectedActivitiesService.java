package cz.muni.irtis.datacollector.metrics.util.physicalactivity;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * Register listeners to ActivityRecognitionClient with intent service.
 */
public class BackgroundDetectedActivitiesService extends Service {
    private static final String TAG = BackgroundDetectedActivitiesService.class.getSimpleName();

    private Intent intentService;
    private PendingIntent pendingIntent;
    private ActivityRecognitionClient activityRecognitionClient;
    private int delayMilis;

    public BackgroundDetectedActivitiesService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        activityRecognitionClient = new ActivityRecognitionClient(this);
        intentService = new Intent(this, DetectedActivitiesIntentService.class);
        pendingIntent = PendingIntent
                .getService(this, 1, intentService, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        throw new IllegalStateException("Non-bindable service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        delayMilis = intent.getIntExtra("delayMilis", -1);
        if (delayMilis < 0) {
            throw new IllegalStateException("Delay is less than 0!");
        }

        requestActivityUpdates();
        return START_STICKY;
    }

    /**
     * Start listening to activity changes
     */
    public void requestActivityUpdates() {
        Task<Void> task = activityRecognitionClient
                .requestActivityUpdates(delayMilis, pendingIntent);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Successfully requested activity updates");
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Requesting activity updates failed to start");
            }
        });
    }

    /**
     * Stop listening to activity changes
     */
    public void removeActivityUpdates() {
        Task<Void> task = activityRecognitionClient.removeActivityUpdates(
                pendingIntent);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "Removed activity updates successfully!");
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed to remove activity updates!");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "service exiting.");
        removeActivityUpdates();
    }
}
