package cz.muni.irtis.datacollector.metrics.util.physicalactivity;

import com.google.android.gms.location.DetectedActivity;

public class RecognizedActivity {
    /**
     * Convert activity code to String
     * @param i
     * @return
     */
    public static String toString(int i) {
        switch (i) {
            case DetectedActivity.IN_VEHICLE:
                return "IN_VEHICLE";
            case DetectedActivity.ON_BICYCLE:
                return "ON_BICYCLE";
            case DetectedActivity.ON_FOOT:
                return "ON_FOOT";
            case DetectedActivity.RUNNING:
                return "RUNNING";
            case DetectedActivity.STILL:
                return "STILL";
            case DetectedActivity.TILTING:
                return "TILTING";
            case DetectedActivity.WALKING:
                return "WALKING";
            default:
                return "UNKNOWN";
        }
    }
}
