package com.example.gossipmessenger;

public class msgmodelClass {
    String message;
    String senderid;
    long timeStamp;

    public msgmodelClass() {
    }

    public msgmodelClass(String senderid, String message, long timeStamp) {
        this.senderid = senderid;
        this.message = message;
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderid() {
        return senderid;
    }

    public void setSenderid(String senderid) {
        this.senderid = senderid;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
