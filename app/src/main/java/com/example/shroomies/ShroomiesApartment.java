package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ShroomiesApartment implements Parcelable {
    String apartmentID,ownerID;
    ArrayList<String> membersID;

    ShroomiesApartment(){

    }

    public ShroomiesApartment(String apartmentID, String ownerID, ArrayList<String> membersID) {
        this.apartmentID = apartmentID;
        this.ownerID = ownerID;
        this.membersID = membersID;
    }

    protected ShroomiesApartment(Parcel in) {
        apartmentID = in.readString();
        ownerID = in.readString();
        membersID = in.createStringArrayList();
    }

    public static final Creator<ShroomiesApartment> CREATOR = new Creator<ShroomiesApartment>() {
        @Override
        public ShroomiesApartment createFromParcel(Parcel in) {
            return new ShroomiesApartment(in);
        }

        @Override
        public ShroomiesApartment[] newArray(int size) {
            return new ShroomiesApartment[size];
        }
    };

    public String getApartmentID() {
        return apartmentID;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public ArrayList<String> getMembersID() {
        return membersID;
    }

    public void setApartmentID(String apartmentID) {
        this.apartmentID = apartmentID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public void setMembersID(ArrayList<String> membersID) {
        this.membersID = membersID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(apartmentID);
        parcel.writeString(ownerID);
        parcel.writeStringList(membersID);
    }
}
