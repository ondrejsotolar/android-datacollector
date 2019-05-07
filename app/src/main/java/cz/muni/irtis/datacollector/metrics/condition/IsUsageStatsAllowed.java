package cz.muni.irtis.datacollector.metrics.condition;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;


public class IsUsageStatsAllowed implements Condition {
    @Override
    public boolean check(Context context) {
        return isPackageUsagePermissionGranted(context);
    }

    public static boolean isPackageUsagePermissionGranted(Context context) {
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
}
