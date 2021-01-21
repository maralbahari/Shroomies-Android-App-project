package com.example.shroomies;

import java.util.List;

public class Model_personal {


    String date, description,  userID;
    int price;
    long latitude, longitude;

    List<Boolean> preferences;

    public Model_personal() {
    }

    public Model_personal(String date, String description, String userID, long latitude, long longitude, int price, List<Boolean> preferences) {
        this.date = date;
        this.description = description;
        this.userID = userID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.price = price;
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

    public String getUserId() {
        return userID;
    }

    public void setUserId(String userId) {
        this.userID = userID;
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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public List<Boolean> getPreferences() {
        return preferences;
    }

    public void setPreferences(List<Boolean> preferences) {
        this.preferences = preferences;
    }
}
