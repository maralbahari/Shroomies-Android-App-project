package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

import com.virgilsecurity.crypto.foundation.Hash;

import java.util.HashMap;

public class TasksCard implements Parcelable {

    String actor ,description, title,dueDate,
            importance, members, cardID,
            done;
    HashMap<String , String> mention;
    String  date;

    public TasksCard() {
    }

    public TasksCard(String description, String taskTitle, String taskDueDate, String taskImportance, String date, String taskCardId, String members, String done, HashMap<String, String> mention) {
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
        actor = in.readString();
        description = in.readString();
        title = in.readString();
        dueDate = in.readString();
        importance = in.readString();
        members = in.readString();
        cardID = in.readString();
        done = in.readString();
        date = in.readString();
    }

    public HashMap<String, String> getMention() {
        return mention;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(actor);
        dest.writeString(description);
        dest.writeString(title);
        dest.writeString(dueDate);
        dest.writeString(importance);
        dest.writeString(members);
        dest.writeString(cardID);
        dest.writeString(done);
        dest.writeString(date);
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

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
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

    public String getDate() {
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


}