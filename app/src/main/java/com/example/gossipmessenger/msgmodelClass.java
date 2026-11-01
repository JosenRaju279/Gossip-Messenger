package com.example.gossipmessenger;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class msgmodelClass {
    private String message;
    private String senderid;
    private long timeStamp;

    public msgmodelClass() {
        // Required empty constructor for Firebase
    }

    public msgmodelClass(String senderid, String message, long timeStamp) {
        this.senderid = senderid;
        this.message = message;
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderid() {
        return senderid != null ? senderid : "";
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
