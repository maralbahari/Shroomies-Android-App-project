package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShroomiesApartment implements Parcelable {
    String apartmentID,ownerID;
    HashMap<String,String> apartmentMembers=new HashMap<>();


    public ShroomiesApartment() {

    }

    public ShroomiesApartment(String apartmentID, String ownerID, HashMap<String, String> apartmentMembers) {
        this.apartmentID = apartmentID;
        this.ownerID = ownerID;
        this.apartmentMembers = apartmentMembers;
    }

    public String getApartmentID() {
        return apartmentID;
    }

    public void setApartmentID(String apartmentID) {
        this.apartmentID = apartmentID;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }

    public HashMap<String,String> getApartmentMembers() {
        return apartmentMembers;
    }

    public void setApartmentMembers(HashMap<String,String> apartmentMembers) {
        this.apartmentMembers = apartmentMembers;
    }

    protected ShroomiesApartment(Parcel in) {
        apartmentID = in.readString();
        ownerID = in.readString();
        apartmentMembers=in.readHashMap(HashMap.class.getClassLoader());



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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(apartmentID);
        parcel.writeString(ownerID);
        parcel.writeMap(apartmentMembers);

    }
}
