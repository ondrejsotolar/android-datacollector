package cz.muni.irtis.datacollector.schedule;

import org.joda.time.DateTime;

public interface Persistent {
    void save(DateTime dateTime, Object... params);
}
