package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

public class Log implements Parcelable {
    String logID,actor,action,cardTitle,cardType,receivedBy,removedUser,cardID;
    String actorPic,actorName;
    long when;

    public Log(String logID, String actor, String action, long when, String cardTitle, String cardType, String receivedBy, String removedUser,String cardID) {
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
    public Log(){

    }

    public String getCardID() {
        return cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    protected Log(Parcel in) {
        logID = in.readString();
        actor = in.readString();
        action = in.readString();
        when=in.readLong();
        cardTitle = in.readString();
        cardType = in.readString();
        receivedBy = in.readString();
        removedUser = in.readString();
        actorName=in.readString();
        actorPic=in.readString();
        cardID=in.readString();
    }

    public String getActorPic() {
        return actorPic;
    }

    public void setActorPic(String actorPic) {
        this.actorPic = actorPic;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public static final Creator<Log> CREATOR = new Creator<Log>() {
        @Override
        public Log createFromParcel(Parcel in) {
            return new Log(in);
        }

        @Override
        public Log[] newArray(int size) {
            return new Log[size];
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

    public long getWhen() {
        return when;
    }

    public void setWhen(long when) {
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
        parcel.writeLong(when);
        parcel.writeString(cardTitle);
        parcel.writeString(cardType);
        parcel.writeString(receivedBy);
        parcel.writeString(removedUser);
        parcel.writeString(actorName);
        parcel.writeString(cardID);
        parcel.writeString(actorPic);
    }
}
