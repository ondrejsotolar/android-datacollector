package cz.muni.irtis.datacollector;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import cz.muni.irtis.datacollector.database.DatabaseHelper;
import cz.muni.irtis.datacollector.schedule.SchedulerService;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends PermissionAppCompatActivity {
    private static final int SCREENSHOT_REQUEST_CODE = 59706;
    private MediaProjectionManager projectionMgr;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        isInPermission = false;
        if (requestCode == REQUEST_PERMISSION) {
            if (hasAllPermissions(getDesiredDangerousPermissions())) {
                onReady(state);
            }
            else {
                onPermissionDenied();
            }
        }
    }

    @Override
    protected String[] getDesiredDangerousPermissions() {
        return new String[] {
                ACCESS_FINE_LOCATION
        };
    }

    @Override
    protected void onPermissionDenied() {
        Toast.makeText(this, getString(R.string.permissions_denied), Toast.LENGTH_LONG)
                .show();
        finish();
    }

    /**
     * Run the scheduler service only if it's not already running.
     */
    @Override
    protected void onReady(Bundle state) {
        setContentView(R.layout.activity_main);
        initButtons();
        DatabaseHelper.getInstance(this);
        if (!SchedulerService.IS_RUNNING) {
            createScreenCaptureIntent();
        }
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
                SchedulerService.startRunning(this, i);
            }
        }
    }

    private void initButtons() {
        Button start = findViewById(R.id.startButton);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartTakingMetrics();
            }
        });

        Button stop = findViewById(R.id.stopButton);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTakingMetrics();
            }
        });
    }

    /**
     * Request user permission for taking screenshots
     */
    private void createScreenCaptureIntent() {
        projectionMgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionMgr.createScreenCaptureIntent(), SCREENSHOT_REQUEST_CODE);
    }

    private void stopTakingMetrics() {
        if (SchedulerService.IS_RUNNING) {
            Intent stopIntent = new Intent(this, SchedulerService.class);
            SchedulerService.stopRunning(this, stopIntent);
            Toast.makeText(this, getString(R.string.metrics_taking_stopped), Toast.LENGTH_SHORT)
                    .show();
        } else {
            Toast.makeText(this, getString(R.string.no_metrics_are_running), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void restartTakingMetrics() {
        if (!SchedulerService.IS_RUNNING) {
            createScreenCaptureIntent();
        } else {
            Toast.makeText(this, getString(R.string.already_running), Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
