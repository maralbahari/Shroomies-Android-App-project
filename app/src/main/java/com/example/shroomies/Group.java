package com.example.shroomies;

import java.util.List;

public class Group {
    String groupName;
    String groupImage;
    String groupID;
    List<String> groupMembers;
    public String date,time,type,message,from;
    Group(){
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
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

    public String getGroupName() {
        return groupName;
    }

    public String getImage() {
        return groupImage;
    }

    public String getGroupID() {
        return groupID;
    }

    public List<String> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setImageUrl(String groupImage) {
        this.groupImage = groupImage;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public void setGroupMembers(List<String> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public String getGroupImage() {
        return groupImage;
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
