package cz.muni.irtis.datacollector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import cz.muni.irtis.datacollector.database.DatabaseHelper;
import cz.muni.irtis.datacollector.schedule.SchedulerService;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_SMS;

public class MainActivity extends PermissionAppCompatActivity {
    private static final int SCREENSHOT_REQUEST_CODE = 59706;
    private MediaProjectionManager projectionMgr;
    private BroadcastReceiver broadcastReceiver;

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

    /**
     * All required dangerous permissions must be declared here in addition to manifest.
     * Only dangerous permissions must be declared here. They will be asked for.
     * @return list of required dangerous permissions
     */
    @Override
    protected String[] getDesiredDangerousPermissions() {
        return new String[] {
                ACCESS_FINE_LOCATION,
                READ_CONTACTS,
                READ_CALL_LOG,
                READ_SMS

        };
    }

    /**
     * Used didn't grant required permissions. The app will close.
     */
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

        initBroadcastreceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("elapsed_time"));

        Switch onOf = findViewById(R.id.toggle_onof);
        if (!SchedulerService.IS_RUNNING) {
            setOffWithText();
            createScreenCaptureIntent();
        } else {
            if (!onOf.isChecked()) {
                setOnWithText();
            }
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
                setOnWithText();
            }
            else {
                setOffWithText();
            }
        }
    }

    private void initButtons() {
        final Switch onOf = findViewById(R.id.toggle_onof);
        onOf.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    restartTakingMetrics();
                } else {
                    stopTakingMetrics();
                }
            }
        });
    }

    private void initBroadcastreceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("elapsed_time".equals(intent.getAction())) {
                    updateElapsedTime(intent.getStringExtra("elapsed"));
                }
            }
        };
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

            setTextToOff();
            TextView elapsedText = findViewById(R.id.runningTimeText);
            elapsedText.setText(getString(R.string.last_runtime));
            clearElapsedTime();
        }
    }

    private void restartTakingMetrics() {
        if (!SchedulerService.IS_RUNNING) {
            createScreenCaptureIntent();

            TextView elapsedText = findViewById(R.id.runningTimeText);
            elapsedText.setText(R.string.runtime_info);
        }
    }

    private void setOnWithText() {
        Switch onOf = findViewById(R.id.toggle_onof);
        onOf.setChecked(true);
        onOf.setText(R.string.stopButton_text);
    }

    private void setOffWithText() {
        Switch onOf = findViewById(R.id.toggle_onof);
        onOf.setChecked(false);
        onOf.setText(R.string.startButton_text);
    }

    private void setTextToOff() {
        Switch onOf = findViewById(R.id.toggle_onof);
        onOf.setText(R.string.startButton_text);
    }

    private void updateElapsedTime(String elapsed) {
        TextView elapsedText = findViewById(R.id.runningTimeValue);
        elapsedText.setText(elapsed);
    }

    private void clearElapsedTime() {
        TextView elapsedText = findViewById(R.id.runningTimeValue);
        elapsedText.setText("");
    }
}
