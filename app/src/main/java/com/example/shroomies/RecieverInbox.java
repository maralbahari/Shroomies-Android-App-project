package com.example.shroomies;

public class RecieverInbox {
    String receiverID , lastMessage  , lastMessageTime;
    int unseenMessageCount;

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

    public int getUnseenMessageCount() {
        return unseenMessageCount;
    }

    public void setUnseenMessageCount(int unseenMessageCount) {
        this.unseenMessageCount = unseenMessageCount;
    }
}
