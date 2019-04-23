package cz.muni.irtis.datacollector.metrics.condition;

import android.content.Context;
import android.location.LocationManager;

public class IsLocationOn implements Condition {
    @Override
    public boolean check(Context context) {
        LocationManager lm = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return isGpsEnabled || isNetworkEnabled;
    }
}
