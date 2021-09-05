package com.example.shroomies;

public class RecieverInbox {

    private String receiverID, lastMessage, lastMessageTime,  from;
    int unSeenMessageCount;
    RecieverInbox(){

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

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
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
