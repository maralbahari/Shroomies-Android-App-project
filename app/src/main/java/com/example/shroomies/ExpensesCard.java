package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.loader.content.Loader;

import com.virgilsecurity.crypto.foundation.Hash;

import java.util.HashMap;

public class ExpensesCard implements Parcelable {

    String attachedFile, description, title,
            dueDate,importance,cardID,
            done, fileType,actor;
    HashMap<String, String> mention;

    HashMap<String, Float> membersShares=new HashMap<String, Float>();
    String date;


    protected ExpensesCard(Parcel in) {
        attachedFile = in.readString();
        description = in.readString();
        title = in.readString();
        dueDate = in.readString();
        importance = in.readString();
        cardID = in.readString();
        done = in.readString();
        fileType = in.readString();
        actor = in.readString();
        date = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(attachedFile);
        dest.writeString(description);
        dest.writeString(title);
        dest.writeString(dueDate);
        dest.writeString(importance);
        dest.writeString(cardID);
        dest.writeString(done);
        dest.writeString(fileType);
        dest.writeString(actor);
        dest.writeString(date);
    }

    @Override
    public int describeContents() {
        return 0;
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

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }



    public ExpensesCard() {

    }


    public ExpensesCard(String attachedFile, String description, String title, String dueDate, String importance, String members, String cardId, String done, HashMap<String,String> mention, HashMap<String, Float> membersShares) {
        this.attachedFile = attachedFile;
        this.description = description;
        this.title = title;
        this.dueDate = dueDate;
        this.importance = importance;
        this.cardID = cardId;
        this.done = done;
        this.mention = mention;
        this.membersShares=membersShares;
    }




    public HashMap<String, Float> getMembersShares() {
        return membersShares;
    }

    public void setMembersShares(HashMap<String, Float> membersShares) {
        this.membersShares = membersShares;
    }

    public HashMap<String,String> getMention() {
        return mention;
    }

    public void setMention(HashMap<String , String > mention) {
        this.mention = mention;
    }

    public String getFileType() { return fileType; }

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


    public void setDate(String date) {
        this.date = date;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
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


    public String getDate() {
        return date;
    }

    public String getCardID() {
        return cardID;
    }

    public String getDone() {
        return done;
    }



}
