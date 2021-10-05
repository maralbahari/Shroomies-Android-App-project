package com.example.shroomies;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;


public class User implements Parcelable{
    private String username;
    private String email;
    private String apartmentID;
    private String bio;
    private String image;
    private String userID;
    private float sharedAmount;
    private boolean requestSent;
    private String name;
    private int requestCount;

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }


    public boolean isRequestSent() {
        return requestSent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean requestSent() {
        return requestSent;
    }

    public void setRequestSent(boolean requestSent) {
        this.requestSent = requestSent;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApartmentID() {
        return apartmentID;
    }

    public void setApartmentID(String apartmentID) {
        this.apartmentID = apartmentID;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }


    public float getSharedAmount() {
        return sharedAmount;
    }

    public void setSharedAmount(float sharedAmount) {
        this.sharedAmount = sharedAmount;
    }

    public User() {

    }

    public User(String name, String email, String username, String bio, String imageurl, String id,String apartmentID,int requestCount) {
        this.username = username;
        this.name=name;
        this.email = email;
        this.bio = bio;
        this.image = imageurl;
        this.userID = id;
        this.apartmentID = apartmentID;
        this.requestCount=requestCount;
    }
    protected User(Parcel in) {
        username = in.readString();
        email = in.readString();
        bio = in.readString();
        image = in.readString();
        userID = in.readString();
    }
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(email);
        dest.writeString(bio);
        dest.writeString(image);
        dest.writeString(userID);
    }

}
