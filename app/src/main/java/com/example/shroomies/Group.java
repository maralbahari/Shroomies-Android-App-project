package com.example.shroomies;

import android.os.Parcelable;

import java.util.List;

public class Group {
    String groupName;
    String groupImage;
    String groupID;
    List<String> groupMembers;
    Group(){
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
}
