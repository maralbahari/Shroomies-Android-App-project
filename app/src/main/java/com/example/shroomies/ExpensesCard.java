package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class ExpensesCard implements Parcelable {

    String attachedFile, description, title, dueDate, importance,members, cardId, done, mention;
    HashMap<String, Float> membersShares=new HashMap<String, Float>();
    long date;

    public ExpensesCard() {

    }


    public ExpensesCard(String attachedFile, String description, String title, String dueDate, String importance, String members, String cardId, String done, String mention, HashMap<String, Float> membersShares) {
        this.attachedFile = attachedFile;
        this.description = description;
        this.title = title;
        this.dueDate = dueDate;
        this.importance = importance;
        this.members = members;
        this.cardId = cardId;
        this.done = done;
        this.mention = mention;
        this.membersShares=membersShares;
    }

    protected ExpensesCard(Parcel in) {
        attachedFile = in.readString();
        description = in.readString();
        title = in.readString();
        dueDate = in.readString();
        importance = in.readString();
        members = in.readString();
        date = in.readLong();
        cardId = in.readString();
        done = in.readString();
        mention = in.readString();
        membersShares=in.readHashMap(HashMap.class.getClassLoader());
    }

    public static final Creator<ExpensesCard> CREATOR = new Creator<ExpensesCard>() {
        @Override
        public ExpensesCard createFromParcel(Parcel in) {
            return new ExpensesCard(in);
        }

        @Override
        public ExpensesCard[] newArray(int size) {
            return new ExpensesCard[size];
        }
    };

    public HashMap<String, Float> getMembersShares() {
        return membersShares;
    }

    public void setMembersShares(HashMap<String, Float> membersShares) {
        this.membersShares = membersShares;
    }

    public String getMention() {
        return mention;
    }

    public void setMention(String mention) {
        this.mention = mention;
    }

    public void setAttachedFile(String attachedFile) {
        this.attachedFile = attachedFile;
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

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public void setDone(String done) {
        this.done = done;
    }

    public String getAttachedFile() {
        return attachedFile;
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
        parcel.writeString(attachedFile);
        parcel.writeString(description);
        parcel.writeString(title);
        parcel.writeString(dueDate);
        parcel.writeString(importance);
        parcel.writeString(members);
        parcel.writeLong(date);
        parcel.writeString(cardId);
        parcel.writeString(done);
        parcel.writeString(mention);
        parcel.writeMap(membersShares);
    }
}
