package cz.muni.irtis.datacollector.metrics;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.List;

import cz.muni.irtis.datacollector.schedule.Metric;

/**
 * Work in progress...
 */
public class InstalledApplication extends Metric {
    private PackageManager packageManager;

    public InstalledApplication(Context context, Object... params) {
        super(context, params);
    }

    @Override
    public void run() {
        packageManager = getContext().getPackageManager();

        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.FLAG_SYSTEM != 1) {
                Log.d("InstalledApplication", "Installed package :" + packageInfo.packageName);
            }
        }
    }
}
