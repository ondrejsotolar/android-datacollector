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

public class BackgroundDetectedActivitiesService extends Service {
    private static final String TAG = BackgroundDetectedActivitiesService.class.getSimpleName();

    private Intent mIntentService;
    private PendingIntent mPendingIntent;
    private ActivityRecognitionClient mActivityRecognitionClient;
    private int delayMilis;

    public BackgroundDetectedActivitiesService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        mIntentService = new Intent(this, DetectedActivitiesIntentService.class);
        mPendingIntent = PendingIntent.getService(this, 1, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
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
        requestActivityUpdates();
        return START_STICKY;
    }

    public void requestActivityUpdates() {
        Task<Void> task = mActivityRecognitionClient
                .requestActivityUpdates(delayMilis, mPendingIntent);

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

    public void removeActivityUpdates() {
        Task<Void> task = mActivityRecognitionClient.removeActivityUpdates(
                mPendingIntent);

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
        removeActivityUpdates();
    }
}
