package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Apartment implements Parcelable {
    String userID;
    String description;
    String date;

    public String getId() {
        return id;
    }

    String id;
    int numberOfRoommates;
    double latitude;
    double longitude;
    List<String> image_url;
    List<Boolean> preferences;
    int price;
    String userName;

    public String getUserID() {
        return userID;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
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

    public List<Boolean> getPreferences() {
        return preferences;
    }

    public int getPrice() {
        return price;
    }

    public String getUserName() {
        return userName;
    }

    public Apartment() {

    }


    protected Apartment(Parcel in) {
        userID = in.readString();
        description = in.readString();
        date = in.readString();
        numberOfRoommates = in.readInt();
        latitude = in.readDouble();
        longitude = in.readDouble();
        image_url = in.createStringArrayList();
        price = in.readInt();
        userName = in.readString();
        id= in.readString();
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

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
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
        dest.writeString(description);
        dest.writeString(date);
        dest.writeInt(numberOfRoommates);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeStringList(image_url);
        dest.writeInt(price);
        dest.writeString(userName);
        dest.writeString(id);
    }
}





