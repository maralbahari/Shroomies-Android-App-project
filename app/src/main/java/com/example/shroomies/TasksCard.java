package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.ParameterizedType;

public class TasksCard implements Parcelable {

    String description, title,dueDate, importance, members, date, cardId, done,mention;

    public TasksCard() {
    }

    public TasksCard(String description, String taskTitle, String taskDueDate, String taskImportance, String date, String taskCardId, String members, String done, String mention) {
        this.cardId = taskCardId;
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
        date = in.readString();
        cardId = in.readString();
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

    public void setDate(String date) {
        this.date = date;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
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

    public String getDate() {
        return date;
    }

    public String getCardId() {
        return cardId;
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
        parcel.writeString(date);
        parcel.writeString(cardId);
        parcel.writeString(done);
        parcel.writeString(mention);
    }
}