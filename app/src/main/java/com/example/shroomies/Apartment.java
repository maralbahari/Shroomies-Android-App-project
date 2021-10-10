package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.maps.android.clustering.ClusterItem;

import java.util.List;

public class Apartment implements Parcelable, ClusterItem {
    private String userID, apartmentID, description, id, locality, subLocality, buildingName, buildingType;
    private List<String> buildingAddress;
    private Timestamp timeStamp;

    private String preferences;
    private int numberOfRoommates;
    private double latitude, longitude;
    private List<String> imageUrl;
    private int price;


    protected Apartment(Parcel in) {
        userID = in.readString();
        apartmentID = in.readString();
        description = in.readString();
        id = in.readString();
        locality = in.readString();
        subLocality = in.readString();
        buildingName = in.readString();
        buildingType = in.readString();
        buildingAddress = in.createStringArrayList();
        timeStamp = in.readParcelable(Timestamp.class.getClassLoader());
        preferences = in.readString();
        numberOfRoommates = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        imageUrl = in.createStringArrayList();
        price = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userID);
        dest.writeString(apartmentID);
        dest.writeString(description);
        dest.writeString(id);
        dest.writeString(locality);
        dest.writeString(subLocality);
        dest.writeString(buildingName);
        dest.writeString(buildingType);
        dest.writeStringList(buildingAddress);
        dest.writeParcelable(timeStamp, flags);
        dest.writeString(preferences);
        dest.writeInt(numberOfRoommates);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeStringList(imageUrl);
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

    public String getSubLocality() {
        return subLocality;
    }

    public List<String> getBuildingAddress() {
        return buildingAddress;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public String getBuildingType() {
        return buildingType;
    }

    public String getPreferences() {
        return preferences;
    }


    public Timestamp getTimeStamp() {
        return timeStamp;
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

    public List<String> getImageUrl() {
        return imageUrl;
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

    Apartment() {
    }


    public String getLocality() {
        return locality;
    }


    @NonNull
    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }


    @Nullable
    @Override
    public String getTitle() {
        return "";
    }


    @Nullable
    @Override
    public String getSnippet() {
        return "";
    }


}





