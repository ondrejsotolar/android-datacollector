package cz.muni.irtis.datacollector.metrics.condition;

import android.content.Context;

import cz.muni.irtis.datacollector.metrics.util.ScreenState;

public class IsScreenOn implements Condition {
    @Override
    public boolean check(Context context) {
        return ScreenState.isScreenOn(context);
    }
}
