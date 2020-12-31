package com.example.shroomies;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String name;
    private String email;

    private String bio;
    private String image;
    private String ID;

    public User() {

    }
    public User(String name, String email, String username, String bio, String imageurl, String id) {
        this.name = name;
        this.email = email;

        this.bio = bio;
        this.image = imageurl;
        this.ID = id;
    }

    protected User(Parcel in) {
        name = in.readString();
        email = in.readString();

        bio = in.readString();
        image = in.readString();
        ID = in.readString();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public  String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);

        dest.writeString(bio);
        dest.writeString(image);
        dest.writeString(ID);
    }
}
