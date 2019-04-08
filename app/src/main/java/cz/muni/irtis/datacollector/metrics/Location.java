package cz.muni.irtis.datacollector.metrics;

import android.content.Context;
import android.location.LocationManager;

import cz.muni.irtis.datacollector.schedule.Metric;

import static android.content.Context.LOCATION_SERVICE;

public class Location extends Metric {

    LocationManager locationManager;

    public Location(Context context, Object... params) {
        super(context, params);
    }


    @Override
    public void run() {

        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);


        throw new UnsupportedOperationException("Not implemented!");
    }
}
