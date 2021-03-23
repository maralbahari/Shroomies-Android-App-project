package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Apartment implements Parcelable {
    private String userID,apartmentID, description, id ;


    public Timestamp getTime_stamp() {
        return time_stamp;
    }

    private Timestamp time_stamp;

    private int numberOfRoommates;
    private double latitude,longitude;
    private List<String> image_url, preferences;
    private int price;

    public String getUserID() {
        return userID;
    }
    public String getDescription() {
        return description;
    }
    public int getNumberOfRoommates() {
        return numberOfRoommates;
    }
    public double getLatitude() {
        return latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public List<String> getImage_url() {
        return image_url;
    }
    public List<String> getPreferences() {
        return preferences;
    }
    public int getPrice() {
        return price;
    }
    public String getApartmentID() {
        return apartmentID;
    }
    public String getId() {
        return id;
    }
    public void setApartmentID(String apartmentID) {
        this.apartmentID = apartmentID;
    }

    Apartment(){ }

    protected Apartment(Parcel in) {

        userID = in.readString();
        description = in.readString();
        id = in.readString();
        numberOfRoommates = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        image_url = in.createStringArrayList();
        price = in.readInt();
        preferences = in.createStringArrayList();


    }

    public static final Creator<Apartment> CREATOR = new Creator<Apartment>() {
        @Override
        public Apartment createFromParcel(Parcel in) {
            return new Apartment(in);
        }

        @Override
        public Apartment[] newArray(int size) {
            return new Apartment[size];
        }
    };



    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(userID);
        dest.writeString(description);
        dest.writeString(id);
        dest.writeInt(numberOfRoommates);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeStringList(image_url);
        dest.writeStringList(preferences);
        dest.writeInt(price);

    }


}





