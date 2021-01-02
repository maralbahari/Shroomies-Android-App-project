package com.example.shroomies;

import android.os.Parcelable;

import java.util.List;

public class Group {
    List<String> groupMemberUsers;
    String groupName;
    String imageUrl;
    String groupID;
    Group(){
    }

    public List<String> getGroupMemberUsers() {
        return groupMemberUsers;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getGroupID() {
        return groupID;
    }
}
