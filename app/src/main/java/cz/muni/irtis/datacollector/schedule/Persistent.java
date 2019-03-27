package cz.muni.irtis.datacollector.schedule;

import java.time.LocalDateTime;

public interface Persistent {
    void save(LocalDateTime dateTime, Object... params);
}
