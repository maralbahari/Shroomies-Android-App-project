package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.type.Date;

import java.sql.Timestamp;
import java.util.List;

public class Apartment implements Parcelable {
    String userID;


    String apartmentID;
    String description;
    String date;
    Date time_stamp;
    String id;
    int numberOfRoommates;
    double latitude;
    double longitude;

    List<String> image_url;
     List<String> preferences;
    int price;
    Apartment(){

    }


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

    public void setApartmentID(String apartmentID) {
        this.apartmentID = apartmentID;
    }

    protected Apartment(Parcel in) {
        userID = in.readString();
        description = in.readString();
        date = in.readString();
        id = in.readString();
        numberOfRoommates = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        image_url = in.createStringArrayList();
        price = in.readInt();

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

    public String getId() {
        return id;
    }





    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userID);
        dest.writeString(description);
        dest.writeString(date);
        dest.writeString(id);
        dest.writeInt(numberOfRoommates);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeStringList(image_url);
        dest.writeInt(price);
    }
}





