package com.example.shroomies;

public class Messages {

    public String date;
    public String time;
    public String type;
    public String message;
    public String from;

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

    public void setMessage(String message) {
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public String getFrom() {
        return from;
    }
}
