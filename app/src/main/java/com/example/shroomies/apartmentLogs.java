package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

public class apartmentLogs implements Parcelable {
    String logID,actor, action,cardTitle,cardType,receivedBy,removedUser,cardID , when;

    public apartmentLogs(String logID, String actor, String action, String when, String cardTitle, String cardType, String receivedBy, String removedUser, String cardID) {
        this.logID = logID;
        this.actor = actor;
        this.action = action;
        this.when = when;
        this.cardTitle = cardTitle;
        this.cardType = cardType;
        this.receivedBy = receivedBy;
        this.removedUser = removedUser;
        this.cardID=cardID;
    }
    public apartmentLogs(){

    }

    public String getCardID() {
        return cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    protected apartmentLogs(Parcel in) {
        logID = in.readString();
        actor = in.readString();
        action = in.readString();
        when=in.readString();
        cardTitle = in.readString();
        cardType = in.readString();
        receivedBy = in.readString();
        removedUser = in.readString();
        cardID=in.readString();
    }


    public static final Creator<apartmentLogs> CREATOR = new Creator<apartmentLogs>() {
        @Override
        public apartmentLogs createFromParcel(Parcel in) {
            return new apartmentLogs(in);
        }

        @Override
        public apartmentLogs[] newArray(int size) {
            return new apartmentLogs[size];
        }
    };

    public String getLogID() {
        return logID;
    }

    public void setLogID(String logID) {
        this.logID = logID;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getCardTitle() {
        return cardTitle;
    }

    public void setCardTitle(String cardTitle) {
        this.cardTitle = cardTitle;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getReceivedBy() {
        return receivedBy;
    }

    public void setReceivedBy(String receivedBy) {
        this.receivedBy = receivedBy;
    }

    public String getRemovedUser() {
        return removedUser;
    }

    public void setRemovedUser(String removedUser) {
        this.removedUser = removedUser;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(logID);
        parcel.writeString(actor);
        parcel.writeString(action);
        parcel.writeString(when);
        parcel.writeString(cardTitle);
        parcel.writeString(cardType);
        parcel.writeString(receivedBy);
        parcel.writeString(removedUser);
        parcel.writeString(cardID);

    }
}
