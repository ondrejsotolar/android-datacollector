package cz.muni.irtis.datacollector.schedule;


import android.content.Context;

public abstract class Metric implements Runnable {
    private Context context;
    private Object[] params;

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
}