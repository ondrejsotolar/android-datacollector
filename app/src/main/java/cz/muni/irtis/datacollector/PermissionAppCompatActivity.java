package cz.muni.irtis.datacollector;

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
