package cz.muni.irtis.datacollector.metrics.condition;

import android.content.Context;

public interface Condition {
    boolean check(Context context);
}
