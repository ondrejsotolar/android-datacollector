package cz.muni.irtis.datacollector;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cz.muni.irtis.datacollector.database.DatabaseHelper;
import cz.muni.irtis.datacollector.schedule.SchedulerService;

public class MainActivity extends AppCompatActivity {
    private static final int SCREENSHOT_REQUEST_CODE = 59706;
    private MediaProjectionManager projectionMgr;

    /**
     * Run the scheduler service only if it's not already running.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SchedulerService.IS_RUNNING) {
            createScreenCaptureIntent();
        } else {
            setContentView(R.layout.activity_main);
        }
    }

    @Override
    protected void onStart() {
        DatabaseHelper.getInstance(this);
        super.onStart();
    }

    /**
     * Start SchedulerService if user has given permission, then return to main layout
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCREENSHOT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Intent i = new Intent(this, SchedulerService.class)
                        .putExtra(SchedulerService.EXTRA_RESULT_CODE, resultCode)
                        .putExtra(SchedulerService.EXTRA_RESULT_INTENT, data);
                SchedulerService.startMeUp(this, i);
            }
        }
        setContentView(R.layout.activity_main);
    }

    /**
     * Request user permission for taking screenshots
     */
    private void createScreenCaptureIntent() {
        projectionMgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionMgr.createScreenCaptureIntent(), SCREENSHOT_REQUEST_CODE);
    }
}
