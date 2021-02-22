package com.example.shroomies.notifications;

public class Data {
    private String user,body,title,sent , groupID , cardNotification;
    private Integer icon;
    private String requestNoti;
   private String acceptReqNoti;
    public Data(String user, String body, String title, String sent, Integer icon) {
        this.user = user;
        this.body = body;
        this.title = title;
        this.sent = sent;
        this.icon = icon;
    }
    public Data(String groupID , String user, String body, String title, String sent, Integer icon) {
        this.groupID = groupID;
        this.user = user;
        this.body = body;
        this.title = title;
        this.sent = sent;
        this.icon = icon;
    }
    public Data(String user, String body, String title, String sent, Integer icon , String cardNotification) {
        this.user = user;
        this.body = body;
        this.title = title;
        this.sent = sent;
        this.icon = icon;
        this.cardNotification = cardNotification;
    }

    public Data(String user, String body, String title, String sent, Integer icon, String cardNotification, String requestNoti) {
        this.user = user;
        this.body = body;
        this.title = title;
        this.sent = sent;
        this.cardNotification = cardNotification;
        this.icon = icon;
        this.requestNoti = requestNoti;
    }
    public Data(String user, String body, String title, String sent, Integer icon, String cardNotification, String requestNoti,String acceptReqNoti) {
        this.user = user;
        this.body = body;
        this.title = title;
        this.sent = sent;
        this.cardNotification = cardNotification;
        this.icon = icon;
        this.requestNoti = requestNoti;
        this.acceptReqNoti=acceptReqNoti;
    }


    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getGroupID() {
        return groupID;
    }

    public Data() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }
}
