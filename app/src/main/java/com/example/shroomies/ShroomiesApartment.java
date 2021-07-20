package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShroomiesApartment implements Parcelable {
    String apartmentID, adminID;
    HashMap<String,String> apartmentMembers;
    Map<String , ExpensesCard > expensesCard;
    Map<String , TasksCard> taskCard;

    public Map<String, apartmentLogs> getLogs() {
        return logs;
    }

    public void setLogs(Map<String, apartmentLogs> logs) {
        this.logs = logs;
    }

    Map<String , apartmentLogs> logs;

    public Map<String ,ExpensesCard> getExpensesCard() {
        return expensesCard;
    }

    public void setExpensesCard(Map<String,ExpensesCard> expensesCard) {
        this.expensesCard = expensesCard;
    }

    public Map<String,TasksCard> getTaskCard() {
        return taskCard;
    }

    public void setTaskCard(Map<String,TasksCard> taskCard) {
        this.taskCard = taskCard;
    }

    public ShroomiesApartment() {

    }

    public ShroomiesApartment(String apartmentID, String adminID, HashMap<String, String> apartmentMembers, Map<String ,TasksCard> taskCard, Map<String,ExpensesCard> expensesCard) {
        this.apartmentID = apartmentID;
        this.adminID = adminID;
        this.apartmentMembers = apartmentMembers;
        this.expensesCard = expensesCard;
        this.taskCard = taskCard;
    }


    public String getApartmentID() {
        return apartmentID;
    }

    public void setApartmentID(String apartmentID) {
        this.apartmentID = apartmentID;
    }

    public String getAdminID() {
        return adminID;
    }

    public void setAdminID(String adminID) {
        this.adminID = adminID;
    }

    public HashMap<String,String> getApartmentMembers() {
        return apartmentMembers;
    }

    public void setApartmentMembers(HashMap<String,String> apartmentMembers) {
        this.apartmentMembers = apartmentMembers;
    }

    protected ShroomiesApartment(Parcel in) {
        apartmentID = in.readString();
        adminID = in.readString();
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
        parcel.writeString(adminID);
        parcel.writeMap(apartmentMembers);

    }
}
