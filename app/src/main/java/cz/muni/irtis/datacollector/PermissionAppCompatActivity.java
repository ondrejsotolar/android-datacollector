package cz.muni.irtis.datacollector;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public abstract class PermissionAppCompatActivity extends AppCompatActivity {

    abstract protected String[] getDesiredDangerousPermissions();
    abstract protected void onPermissionDenied();
    abstract protected void onReady(Bundle state);

    protected static final int REQUEST_PERMISSION = 61125;
    protected static final String STATE_IN_PERMISSION = "inPermission";
    protected boolean isInPermission = false;
    protected Bundle state;

    /**
     * Check app permissions & request dangerous ones.
     * Save permission state.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.state = savedInstanceState;
        if (state != null) {
            isInPermission = state.getBoolean(STATE_IN_PERMISSION, false);
        }

        if (hasAllPermissions(getDesiredDangerousPermissions())) {
            onReady(state);
        }
        else if (!isInPermission) {
            isInPermission = true;
            ActivityCompat
                    .requestPermissions(this,
                            netPermissions(getDesiredDangerousPermissions()),
                            REQUEST_PERMISSION);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_IN_PERMISSION, isInPermission);
    }

    /**
     * Check for permission
     * @param perm wanted
     * @return bool
     */
    protected boolean hasPermission(String perm) {
        return ContextCompat.checkSelfPermission(this, perm) ==
                PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if app has all given permissions
     * @param perms wanted
     * @return bool
     */
    protected boolean hasAllPermissions(String[] perms) {
        for (String perm : perms) {
            if (!hasPermission(perm)) {
                return false;
            }
        }
        return true;
    }

    protected boolean isPackageUsagePermissionGranted(Context context) {
        boolean granted = false;
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted =
                    context.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS)
                            == PackageManager.PERMISSION_GRANTED;
        } else {
            granted = mode == AppOpsManager.MODE_ALLOWED;
        }
        return granted;
    }

    private String[] netPermissions(String[] wanted) {
        ArrayList<String> result = new ArrayList<>();
        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result.toArray(new String[result.size()]);
    }
}
