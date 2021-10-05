package com.example.shroomies;

import java.util.HashMap;

public class GroupMessage {
    public String messageID;
    public String date;
    public String type;
    public String message;
    public String from;
    public HashMap<String , Boolean> seenBy;
    public HashMap<String,Integer> unSeenCount;
    public String cardID;
    public String cardTitle;
    public String cardImportance;

    public GroupMessage() {
    }

    public GroupMessage(String messageID, String date, String type, String message, String from, HashMap<String, Boolean> seenBy, HashMap<String, Integer> unSeenCount, String cardID, String cardTitle, String cardImportance) {
        this.messageID = messageID;
        this.date = date;
        this.type = type;
        this.message = message;
        this.from = from;
        this.seenBy = seenBy;
        this.unSeenCount = unSeenCount;
        this.cardID = cardID;
        this.cardTitle = cardTitle;
        this.cardImportance = cardImportance;
    }

    public String getCardID() {
        return cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public String getCardTitle() {
        return cardTitle;
    }

    public void setCardTitle(String cardTitle) {
        this.cardTitle = cardTitle;
    }

    public String getCardImportance() {
        return cardImportance;
    }

    public void setCardImportance(String cardImportance) {
        this.cardImportance = cardImportance;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public HashMap<String, Boolean> getSeenBy() {
        return seenBy;
    }

    public void setSeenBy(HashMap<String, Boolean> seenBy) {
        this.seenBy = seenBy;
    }

    public HashMap<String, Integer> getUnSeenCount() {
        return unSeenCount;
    }

    public void setUnSeenCount(HashMap<String, Integer> unSeenCount) {
        this.unSeenCount = unSeenCount;
    }
}
