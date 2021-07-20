package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

public class TasksCard implements Parcelable {

    String actor ,description, title,dueDate,
            importance, members, cardID,
            done,mention;

    long  date;

    public TasksCard() {
    }

    public TasksCard(String description, String taskTitle, String taskDueDate, String taskImportance, long date, String taskCardId, String members, String done, String mention) {
        this.cardID = taskCardId;
        this.date = date;
        this.description = description;
        this.importance = taskImportance;
        this.members = members;
        this.title = taskTitle;
        this.dueDate = taskDueDate;
        this.done = done;
        this.mention = mention;
    }

    protected TasksCard(Parcel in) {
        description = in.readString();
        title = in.readString();
        dueDate = in.readString();
        importance = in.readString();
        members = in.readString();
        date = in.readLong();
        cardID = in.readString();
        done = in.readString();
        mention = in.readString();
    }

    public static final Creator<TasksCard> CREATOR = new Creator<TasksCard>() {
        @Override
        public TasksCard createFromParcel(Parcel in) {
            return new TasksCard(in);
        }

        @Override
        public TasksCard[] newArray(int size) {
            return new TasksCard[size];
        }
    };

    public String getMention() {
        return mention;
    }
    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public void setMention(String mention) {
        this.mention = mention;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public void setDone(String done) {
        this.done = done;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getImportance() {
        return importance;
    }

    public String getMembers() {
        return members;
    }

    public long getDate() {
        return date;
    }

    public String getCardID() {
        return cardID;
    }

    public String getDone() {
        return done;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(description);
        parcel.writeString(title);
        parcel.writeString(dueDate);
        parcel.writeString(importance);
        parcel.writeString(members);
        parcel.writeLong(date);
        parcel.writeString(cardID);
        parcel.writeString(done);
        parcel.writeString(mention);
    }
}