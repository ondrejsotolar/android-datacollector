package cz.muni.irtis.datacollector.metrics;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import cz.muni.irtis.datacollector.database.Query;
import cz.muni.irtis.datacollector.metrics.util.callhistory.CallRecord;
import cz.muni.irtis.datacollector.schedule.Metric;

/**
 * Record call history
 */
public class CallHistory extends Metric {
    private List<CallRecord> records;

    public CallHistory(Context context, Object... params) {
        super(context, params);
    }

    @Override
    public void run() {
        long latestSaved = Query.getMaxCallDate();
        Uri allCalls = Uri.parse("content://call_log/calls");
        Cursor c = getContext().getContentResolver().query(
                allCalls, null, "DATE > " + latestSaved, null, null);

        if (c != null) {
            if (c.getCount() > 0) {
                records = new ArrayList<>();
                while (c.moveToNext()) {
                    CallRecord r = new CallRecord();
                    r.setPhoneNumber(c.getString(c.getColumnIndex(CallLog.Calls.NUMBER)));
                    r.setName(c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME)));
                    r.setDuration(c.getLong(c.getColumnIndex(CallLog.Calls.DURATION)));
                    r.setType(c.getInt(c.getColumnIndex(CallLog.Calls.TYPE)));
                    r.setCallDate(c.getLong(c.getColumnIndex(CallLog.Calls.DATE)));
                    records.add(r);
                }
                save(LocalDateTime.now());
            }
            c.close();
        }
    }

    @Override
    public void save(LocalDateTime dateTime, Object... params) {
        super.save(dateTime, params);
        Query.saveMetric(this);
    }

    public List<CallRecord> getRecords() {
        return records;
    }
}
