package com.example.shroomies;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Apartment implements Parcelable {
    String userID;
    String description;
    String date;
    int numberOfRoommates;
    LatLngCustom locationLatLng;
    List<String> image_url;
    List<Boolean> preferences;
    int price;

    public Apartment() {

    }

    public Apartment(String userID, String description, String date, int numberOfRoommates, LatLngCustom locationLatLng, List<String> image_url, List<Boolean> preferences, int price) {
        this.userID = userID;
        this.description = description;
        this.date = date;
        this.numberOfRoommates = numberOfRoommates;
        this.locationLatLng = locationLatLng;
        this.image_url = image_url;
        this.preferences = preferences;
        this.price = price;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getNumberOfRoommates() {
        return numberOfRoommates;
    }

    public void setNumberOfRoommates(int numberOfRoommates) {
        this.numberOfRoommates = numberOfRoommates;
    }

    public LatLngCustom getLocationLatLng() {
        return locationLatLng;
    }

    public void setLocationLatLng(LatLngCustom locationLatLng) {
        this.locationLatLng = locationLatLng;
    }

    public List<String> getImage_url() {
        return image_url;
    }

    public void setImage_url(List<String> image_url) {
        this.image_url = image_url;
    }

    public List<Boolean> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<Boolean> preferences) {
        this.preferences = preferences;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }




    protected Apartment(Parcel in) {
        userID = in.readString();
        description = in.readString();
        date = in.readString();
        numberOfRoommates = in.readInt();
        image_url = in.createStringArrayList();
        price = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userID);
        dest.writeString(description);
        dest.writeString(date);
        dest.writeInt(numberOfRoommates);
        dest.writeStringList(image_url);
        dest.writeInt(price);
    }

    @Override
    public int describeContents() {
        return 0;
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
}

class LatLngCustom {
    double latitude;
    double longitude;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    LatLngCustom() {
    }


}
