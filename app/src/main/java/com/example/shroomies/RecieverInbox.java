package com.example.shroomies;

import java.util.HashMap;

public class RecieverInbox {

    private String receiverID, lastMessageTime, from;
    private HashMap<String, Object> lastMessage;
    int unSeenMessageCount;

    RecieverInbox() {

    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public HashMap<String, Object> getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(HashMap<String, Object> lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnSeenMessageCount() {
        return unSeenMessageCount;
    }

    public void setUnSeenMessageCount(int unSeenMessageCount) {
        this.unSeenMessageCount = unSeenMessageCount;
    }
}
