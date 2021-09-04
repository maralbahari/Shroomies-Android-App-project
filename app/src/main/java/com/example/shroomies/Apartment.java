package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Apartment implements Parcelable {
    private String userID,apartmentID, description, id ;

    protected Apartment(Parcel in) {
        userID = in.readString();
        apartmentID = in.readString();
        description = in.readString();
        id = in.readString();
        time_stamp = in.readString();
        numberOfRoommates = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        image_url = in.createStringArrayList();
        preferences = in.createStringArrayList();
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

    public String  getTime_stamp() {
        return time_stamp;
    }
    private String time_stamp;

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



    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userID);
        dest.writeString(apartmentID);
        dest.writeString(description);
        dest.writeString(id);
        dest.writeString(time_stamp);
        dest.writeInt(numberOfRoommates);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeStringList(image_url);
        dest.writeStringList(preferences);
        dest.writeInt(price);
    }
}





