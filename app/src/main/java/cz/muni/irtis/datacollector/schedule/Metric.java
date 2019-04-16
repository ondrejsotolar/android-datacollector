package cz.muni.irtis.datacollector.schedule;


import android.content.Context;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import cz.muni.irtis.datacollector.metrics.condition.Condition;

public abstract class Metric implements Runnable, Stoppable, Persistent {
    private Context context;
    private Object[] params;
    private LocalDateTime dateTimeCollected;
    private List<Condition> prerequisitises;
    private boolean isRunning = false;

    public Metric(Context context, Object... params) {
        this.context = context;
        this.params = params;
        prerequisitises = new ArrayList<>();
    }

    public void save(LocalDateTime dateTime, Object... params) {
        setDateTime(dateTime);
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

    public void addPrerequisity(Condition condition) {
        prerequisitises.add(condition);
    }

    public <T extends Condition> T getPrerequisity(Class<T> type) {
        for (int i = 0; i < prerequisitises.size(); i++) {
            if (type.isInstance(prerequisitises.get(i))) {
                return type.cast(prerequisitises.get(i));
            }
        }
        throw new IllegalStateException("No metric of type '" + type.toString() + "' found.");
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public void stop() {}
}