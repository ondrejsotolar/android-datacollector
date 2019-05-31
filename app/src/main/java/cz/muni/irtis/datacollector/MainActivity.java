package cz.muni.irtis.datacollector;

import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;

import cz.muni.irtis.datacollector.database.DatabaseHelper;
import cz.muni.irtis.datacollector.fragment.MetricsScreenFragment;
import cz.muni.irtis.datacollector.fragment.RootScreenFragment;
import cz.muni.irtis.datacollector.fragment.SyncScreenFragment;
import cz.muni.irtis.datacollector.fragment.UsageStatsDialogFragment;
import cz.muni.irtis.datacollector.metrics.condition.IsUsageStatsAllowed;
import cz.muni.irtis.datacollector.schedule.SchedulerService;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_SMS;

public class MainActivity extends PermissionAppCompatActivity
        implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final int SCREENSHOT_REQUEST_CODE = 59706;
    private MediaProjectionManager projectionMgr;
    private boolean isInScreenFragment = false;

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
        initRootScreenFragment(false);
        DatabaseHelper.getInstance(this);

        if (!SchedulerService.IS_RUNNING) {
            setOnOffState(false);
            createScreenCaptureIntent();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof RootScreenFragment) {
            RootScreenFragment preferenceFragment = (RootScreenFragment) fragment;
            preferenceFragment.setOnPreferenceChangeListener(this);
            preferenceFragment.setPreferenceClickListener(this);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // konstanty, tady vubec nevim, o co jde
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
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        // konstanty, vsechny preference
        if ("lastSync".equals(key)) {
            initSyncScreenFragment();
        } else if ("metricsTaken".equals(key)) {
            initMetricsScreenFragment();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (isInScreenFragment) {
            isInScreenFragment = false;
            initRootScreenFragment(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        if (!IsUsageStatsAllowed.isPackageUsagePermissionGranted(this)) {
            UsageStatsDialogFragment dialog = new UsageStatsDialogFragment();
            dialog.show(getSupportFragmentManager(), UsageStatsDialogFragment.class.getSimpleName());
        }
    }

    private void initRootScreenFragment(boolean isReturnFromOtherFragment) {
        Fragment fragment = Fragment.instantiate(this, RootScreenFragment.class.getName());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (isReturnFromOtherFragment) {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();
    }

    private void initSyncScreenFragment() {
        Fragment fragment = Fragment.instantiate(this, SyncScreenFragment.class.getName());
        isInScreenFragment = true;
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();

        setActionBar(true);
    }

    private void initMetricsScreenFragment() {
        Fragment fragment = Fragment.instantiate(this, MetricsScreenFragment.class.getName());
        isInScreenFragment = true;
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.fragment, fragment);
        transaction.commit();

        setActionBar(true);
    }

    /**
     * Add back arrow to title
     * @param value
     */
    // To je k cemu? Proc to nenastavis jednou v onCreate(), ale volas to pokazde?
    // A k cemu je ten argument, ktery je vzdy true?
    private void setActionBar(boolean value) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(value);
        }
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

    /**
     * Set UI switch & update storage size info
     * @param value
     */
    private void setOnOffState(boolean value) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment instanceof RootScreenFragment) {
            RootScreenFragment rootFragment = (RootScreenFragment) fragment;
            rootFragment.setOnOfSwitchValue(value);
            rootFragment.setLocalFilesSize(this, DatabaseHelper.getInstance(this).getSize());
        }
    }
}
