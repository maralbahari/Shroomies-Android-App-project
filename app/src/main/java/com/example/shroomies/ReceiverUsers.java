package com.example.shroomies;

public class ReceiverUsers {
    String receiverName;
    String receiverID;
    String receiverImage;

    public ReceiverUsers(String receiverName, String receiverID, String receiverImage) {
        this.receiverName = receiverName;
        this.receiverID = receiverID;
        this.receiverImage = receiverImage;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public void setReceiverImage(String receiverImage) {
        this.receiverImage = receiverImage;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public String getReceiverID() {
        return receiverID;
    }

    public String getReceiverImage() {
        return receiverImage;
    }
}
