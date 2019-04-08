package cz.muni.irtis.datacollector.metrics;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.time.LocalDateTime;

import cz.muni.irtis.datacollector.database.Query;
import cz.muni.irtis.datacollector.schedule.Metric;

import static android.content.Context.LOCATION_SERVICE;

public class Location extends Metric implements LocationListener{

    LocationManager locationManager;
    private boolean isRunning = false;
    private double roundedLat;
    private double roundedLon;

    public Location(Context context, Object... params) {
        super(context, params);
        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
    }

    /**
     * Register location updates per 10 seconds
     */
    @Override
    @SuppressWarnings({"MissingPermission"})
    public void run() {
        if (!isRunning) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000 * 10,
                    0,
                    this);
            isRunning = true;
        }
    }

    /**
     * Stop receiving location updates
     */
    @Override
    public void stop() {
        locationManager.removeUpdates(this);
        isRunning = false;
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        if (location != null) {
            roundedLat = (double) Math.round(location.getLatitude() * 10000d) / 10000d;
            roundedLon = (double) Math.round(location.getLongitude() * 10000d) / 10000d;
            save(LocalDateTime.now());
        }
    }

    @Override
    public void save(LocalDateTime dateTime, Object... params) {
        super.save(dateTime, params);
        Query.saveMetric(this);
    }

    public double getRoundedLat() {
        return roundedLat;
    }

    public double getRoundedLon() {
        return roundedLon;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}
}
