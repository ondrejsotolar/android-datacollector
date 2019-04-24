package cz.muni.irtis.datacollector.metrics;

import android.annotation.SuppressLint;
import android.content.Context;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.time.LocalDateTime;

import cz.muni.irtis.datacollector.database.Query;
import cz.muni.irtis.datacollector.metrics.condition.IsLocationOn;
import cz.muni.irtis.datacollector.schedule.Metric;

/**
 * Capture location
 */
public class Location extends Metric {
    private FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;

    private int minTimeMilis;
    private int minDistance;
    double roundedLat;
    double roundedLon;

    public Location(Context context, Object... params) {
        super(context, params);

        addPrerequisity(new IsLocationOn());

        minTimeMilis = (int) params[0];
        minDistance = (int) params[1];

        initLocationCallback(context);
    }

    @SuppressLint("MissingPermission")
    private void initLocationCallback(Context context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (android.location.Location location : locationResult.getLocations()) {
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                }
            }
        };
    }

    /**
     * Register location updates delay from starting intent
     */
    @Override
    @SuppressWarnings({"MissingPermission"})
    public void run() {
        if (!isPrerequisitiesSatisfied()) {
            return;
        }
        if (!isRunning()) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(minTimeMilis);
            locationRequest.setFastestInterval(minTimeMilis);
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback,
                    getContext().getMainLooper());
            setRunning(true);
        }
    }

    /**
     * Stop receiving location updates
     */
    @Override
    public void stop() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        setRunning(false);
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

    private void onLocationChanged(android.location.Location location) {
        roundedLat = (double) Math.round(location.getLatitude() * 10000d) / 10000d;
        roundedLon = (double) Math.round(location.getLongitude() * 10000d) / 10000d;
        save(LocalDateTime.now());
    }


}
