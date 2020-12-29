package com.example.shroomies;

public class Model_personal {

     String date, description, latitude, longitude, preferences, price, userId;


    //blank constructor
    public Model_personal() {
    }

    public Model_personal(String date, String description, String latitude, String longitude,
                          String preferences, String price, String userId)
    {
        this.date = date;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.preferences = preferences;
        this.price = price;
        this.userId = userId;
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

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPreferences() {
        return preferences;
    }

    public void setPreferences(String preferences) {
        this.preferences = preferences;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
