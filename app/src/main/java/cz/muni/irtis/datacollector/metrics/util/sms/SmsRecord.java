package cz.muni.irtis.datacollector.metrics.util.sms;

/**
 * Data class for SMS messages
 */
public class SmsRecord {
    public static final String INCOMING = "INCOMING";
    public static final String SENT = "SENT";

    private String phoneNumber;
    private String type;
    private String content;
    private long messageDate;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phone_number) {
        this.phoneNumber = phone_number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(long call_date) {
        this.messageDate = call_date;
    }
}
