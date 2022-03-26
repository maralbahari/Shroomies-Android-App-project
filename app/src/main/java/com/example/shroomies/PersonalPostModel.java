package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.maps.android.clustering.ClusterItem;

import java.util.ArrayList;

public class PersonalPostModel implements ClusterItem, Parcelable {

    private String date, description, userID, id, geoHash, locality, subLocality;
    private ArrayList<String> buildingTypes;
    private int price;
    private double latitude, longitude;
    private String preferences, postID;
    private Timestamp timeStamp;

    public PersonalPostModel() {
    }

    protected PersonalPostModel(Parcel in) {
        date = in.readString();
        description = in.readString();
        userID = in.readString();
        id = in.readString();
        geoHash = in.readString();
        locality = in.readString();
        subLocality = in.readString();
        buildingTypes = in.createStringArrayList();
        price = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        preferences = in.readString();
        postID = in.readString();
        timeStamp = in.readParcelable(Timestamp.class.getClassLoader());
    }

    public static final Creator<PersonalPostModel> CREATOR = new Creator<PersonalPostModel>() {
        @Override
        public PersonalPostModel createFromParcel(Parcel in) {
            return new PersonalPostModel(in);
        }

        @Override
        public PersonalPostModel[] newArray(int size) {
            return new PersonalPostModel[size];
        }
    };

    public ArrayList<String> getBuildingTypes() {
        return buildingTypes;
    }

    public void setBuildingTypes(ArrayList<String> buildingTypes) {
        this.buildingTypes = buildingTypes;
    }


    public PersonalPostModel(String date, String description, String userID, String id, int price, double latitude, double longitude, String preferences) {
        this.date = date;
        this.description = description;
        this.userID = userID;
        this.id = id;
        this.price = price;
        this.latitude = latitude;
        this.longitude = longitude;
        this.preferences = preferences;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    public String getLocality() {
        return locality;
    }

    public String getSubLocality() {
        return subLocality;
    }

    public static Creator<PersonalPostModel> getCREATOR() {
        return CREATOR;
    }

    public String getPostID() {
        return id;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    /**
     * The position of this marker. This must always return the same value.
     */
    @NonNull
    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return null;
    }


    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeString(description);
        dest.writeString(userID);
        dest.writeString(id);
        dest.writeString(geoHash);
        dest.writeString(locality);
        dest.writeString(subLocality);
        dest.writeStringList(buildingTypes);
        dest.writeInt(price);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(preferences);
        dest.writeString(postID);
        dest.writeParcelable(timeStamp, flags);
    }
}
