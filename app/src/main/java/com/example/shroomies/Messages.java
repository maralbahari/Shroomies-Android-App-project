package com.example.shroomies;

public class Messages {

    public String date;
    public String time;
    public String type;
    public String message;
    public String from;
    public String streamKeyData;


    public String getIsSeen() {
        return isSeen;
    }

    public String isSeen;

    public Messages() {

    }

    public Messages(String date, String time, String type, String message, String from, String isSeen) {
        this.date = date;
        this.time = time;
        this.type = type;
        this.message = message;
        this.from = from;
        this.isSeen = isSeen;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setText(String text) {
        this.message = text;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return message;
    }

    public String getFrom() {
        return from;
    }

    public String getStreamKeyData() {
        return streamKeyData;
    }

    public void setStreamKeyData(String streamKeyData) {
        this.streamKeyData = streamKeyData;
    }
}
