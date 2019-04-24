package cz.muni.irtis.datacollector.metrics.util.callhistory;

import android.provider.CallLog;
import android.util.Log;

/**
 * Data class for call log
 */
public class CallRecord {
    private String name;
    private String phoneNumber;
    private String type;
    private long duration;
    private long callDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getType() {
        return type;
    }

    /**
     * Convert from CallLog.Calls.INCOMING_TYPE int code to corresponding string
     * @param type CallLog.Calls.INCOMING_TYPE int code
     */
    public void setType(int type) {
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE:
                this.type = "INCOMING_TYPE";
                break;
            case CallLog.Calls.OUTGOING_TYPE:
                this.type = "OUTGOING_TYPE";
                break;
            case CallLog.Calls.MISSED_TYPE:
                this.type = "MISSED_TYPE";
                break;
            case CallLog.Calls.VOICEMAIL_TYPE:
                this.type = "VOICEMAIL_TYPE";
                break;
            case CallLog.Calls.REJECTED_TYPE:
                this.type = "REJECTED_TYPE";
                break;
            case CallLog.Calls.BLOCKED_TYPE:
                this.type = "BLOCKED_TYPE";
                break;
            default:
                this.type = "";
                Log.w("CallRecord", "unrecognized call type '"+type+"'");
        }
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getCallDate() {
        return callDate;
    }

    public void setCallDate(long callDate) {
        this.callDate = callDate;
    }
}
