package cz.muni.irtis.datacollector.metrics;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import cz.muni.irtis.datacollector.database.Query;
import cz.muni.irtis.datacollector.metrics.util.sms.SmsRecord;
import cz.muni.irtis.datacollector.schedule.Metric;

public class SmsConversation extends Metric {
    private List<SmsRecord> records;

    public SmsConversation(Context context, Object... params) {
        super(context, params);
    }

    @Override
    public void run() {
        long latestSaved = Query.getMaxMessageDate();
        String incommingAddress = "content://sms/inbox";
        String sentAddress = "content://sms/sent";
        records = new ArrayList<>();

        addMessages(records, latestSaved, incommingAddress, SmsRecord.INCOMING);
        addMessages(records, latestSaved, sentAddress, SmsRecord.SENT);

        if (records.size() > 1) {
            save(DateTime.now());
        }
    }

    private void addMessages(List<SmsRecord> records, long latestSaved, String address, String direction) {
        Uri receivedMessages = Uri.parse(address);
        Cursor c = getContext().getContentResolver().query(
                receivedMessages, null, "DATE > " + latestSaved, null, null);

        if (c != null && c.getCount() > 0) {
            while (c.moveToNext()) {
                SmsRecord r = new SmsRecord();
                r.setPhoneNumber(c.getString(c.getColumnIndex(Telephony.Sms.ADDRESS)));
                r.setContent(c.getString(c.getColumnIndex(Telephony.Sms.BODY)));
                r.setType(direction);
                r.setMessageDate(c.getLong(c.getColumnIndex(Telephony.Sms.DATE)));
                records.add(r);
            }
            c.close();
        }
    }

    @Override
    public void save(DateTime dateTime, Object... params) {
        super.save(dateTime, params);
        Query.saveMetric(this);
    }

    public List<SmsRecord> getRecords() {
        return records;
    }
}
