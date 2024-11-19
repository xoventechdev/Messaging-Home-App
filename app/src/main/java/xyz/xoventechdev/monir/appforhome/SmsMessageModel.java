package xyz.xoventechdev.monir.appforhome;

public class SmsMessageModel {
    private String phoneNumber;
    private String messageBody;
    private String timestamp;
    private String mobileModel;

    public SmsMessageModel() {
        // Default constructor required for calls to DataSnapshot.getValue(SmsMessageModel.class)
    }

    public SmsMessageModel(String phoneNumber, String messageBody, String timestamp, String mobileModel) {
        this.phoneNumber = phoneNumber;
        this.messageBody = messageBody;
        this.timestamp = timestamp;
        this.mobileModel = mobileModel;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMobileModel() {
        return mobileModel;
    }

    public void setMobileModel(String mobileModel) {
        this.mobileModel = mobileModel;
    }
}
