package cz.muni.irtis.datacollector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import java.util.Arrays;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.Preference;
import cz.muni.irtis.datacollector.database.DatabaseHelper;
import cz.muni.irtis.datacollector.schedule.SchedulerService;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_SMS;

public class MainActivity extends PermissionAppCompatActivity
        implements Preference.OnPreferenceChangeListener {

    private static final int SCREENSHOT_REQUEST_CODE = 59706;
    private static final int PACKAGE_USAGE_CODE = 59707;
    private MediaProjectionManager projectionMgr;
    private BroadcastReceiver broadcastReceiver;
    private boolean isReturnedFromSettings = false;

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
        initFragment();
        DatabaseHelper.getInstance(this);

        initBroadcastreceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("elapsed_time"));

        if (!SchedulerService.IS_RUNNING) {
            setOnOffState(false);
            createScreenCaptureIntent();
        }
    }

    @Override
    protected void onResume() {
        if (isReturnedFromSettings) {
            isReturnedFromSettings = false;
        }
        super.onResume();

    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof RootScreenFragment) {
            RootScreenFragment preferenceFragment = (RootScreenFragment) fragment;
            preferenceFragment.setOnPreferenceChangeListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if ("onOf".equals(preference.getKey())) {
            boolean preferenceValue = (boolean) newValue;
            if (preferenceValue) {
                restartTakingMetrics();
            } else {
                stopTakingMetrics();
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        setOnOffState(SchedulerService.IS_RUNNING);
        super.onDestroy();
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
                setOnOffState(true);
                checkUsagePermission();
            } else {
                setOnOffState(false);
            }
        }
    }

    private void checkUsagePermission() {
        if (!isPackageUsagePermissionGranted(this)) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(intent);
        }
    }

    private void initFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment == null) {
            fragment = Fragment.instantiate(this, RootScreenFragment.class.getName());
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();
        setOnOffState(SchedulerService.IS_RUNNING);
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

            setOnOffState(false);
        }
    }

    private void restartTakingMetrics() {
        if (!SchedulerService.IS_RUNNING) {
            createScreenCaptureIntent();
        }
    }

    private void setOnOffState(boolean value) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment instanceof RootScreenFragment) {
            RootScreenFragment rootFragment = (RootScreenFragment) fragment;
            rootFragment.setOnOfSwitchValue(value);
            rootFragment.setLocalFilesSize(this, DatabaseHelper.getInstance(this).getSize());
            rootFragment.setCollectedMetrics(
                    Arrays.asList("Screenshot", "Sms", "Wifi networks", "Location"));

        }
    }

    private void updateElapsedTime(String elapsed) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment instanceof RootScreenFragment) {
            RootScreenFragment rootFragment = (RootScreenFragment) fragment;
            rootFragment.setRuntime(elapsed);
        }
    }
}
