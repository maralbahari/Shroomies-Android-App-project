package com.example.shroomies;

import java.util.List;

public class PersonalPostModel {

    String date, description,  userID, id;
    int price;
    long latitude, longitude;
    List<Boolean> preferences;

    public PersonalPostModel() {

    }

    public PersonalPostModel(String date, String description, String userID, String id, int price, long latitude, long longitude, List<Boolean> preferences) {
        this.date = date;
        this.description = description;
        this.userID = userID;
        this.id = id;
        this.price = price;
        this.latitude = latitude;
        this.longitude = longitude;
        this.preferences = preferences;
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

    public long getLatitude() {
        return latitude;
    }

    public void setLatitude(long latitude) {
        this.latitude = latitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public void setLongitude(long longitude) {
        this.longitude = longitude;
    }

    public List<Boolean> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<Boolean> preferences) {
        this.preferences = preferences;
    }
}
