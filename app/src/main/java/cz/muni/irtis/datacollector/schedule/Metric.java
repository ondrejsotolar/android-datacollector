package cz.muni.irtis.datacollector.schedule;


import android.content.Context;

import java.time.LocalDateTime;

public abstract class Metric implements Runnable, Persistent {
    private Context context;
    private Object[] params;
    private LocalDateTime dateTimeCollected;

    public Metric(Context context, Object... params) {
        this.context = context;
        this.params = params;
    }

    public Context getContext() {
        return context;
    }

    public Object[] getParams() {
        return params;
    }

    public LocalDateTime getDateTime() {
        return dateTimeCollected;
    }

    public void setDateTime(LocalDateTime dateTimeCollected) {
        this.dateTimeCollected = dateTimeCollected;
    }
}