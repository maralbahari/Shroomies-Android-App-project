package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Group implements Parcelable {
    String groupName;
    String groupImage;
    String groupID;
    List<String> groupMembers;

    public String date,time,type,message,from;

    public String seenBy;
    Group(){

    }

    protected Group(Parcel in) {
        groupName = in.readString();
        groupImage = in.readString();
        groupID = in.readString();
        groupMembers = in.createStringArrayList();
        date = in.readString();
        time = in.readString();
        type = in.readString();
        message = in.readString();
        from = in.readString();
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel in) {
            return new Group(in);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

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

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(groupName);
        dest.writeString(groupImage);
        dest.writeString(groupID);
        dest.writeStringList(groupMembers);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(type);
        dest.writeString(message);
        dest.writeString(from);
    }
}
