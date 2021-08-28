package com.example.shroomies;

public class RecieverInbox {
    String receiverID , lastMessage  , lastMessageTime;
    int unSeenmessageCount;

    RecieverInbox(){

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

    public int getUnSeenmessageCount() {
        return unSeenmessageCount;
    }

    public void setUnSeenmessageCount(int unSeenmessageCount) {
        this.unSeenmessageCount = unSeenmessageCount;
    }
}
