package com.example.shroomies;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class UserFavourites {

    private final SharedPreferences sharedPreferences;
    private static volatile UserFavourites userFavourites = null;


    private static Set<String> apartmentFavouritesSet, personalPostFavouritesSet;

    //constructor gets the shared preferences or creates a new session if it doesn't exist
    // each user will have his/her own shared preferences folder
    private UserFavourites(Context context, String userID) {
        sharedPreferences = context.getSharedPreferences(userID, Activity.MODE_PRIVATE);
        apartmentFavouritesSet = sharedPreferences.getStringSet(Config.APARTMENT_POST, null);
        personalPostFavouritesSet = sharedPreferences.getStringSet(Config.PERSONAL_POST, null);
    }

    //singleton pattern to avoid having problem
    // with different threads accessing the same
    //preference file
    public static UserFavourites getInstance(Context context, String userID) {
        if (userFavourites == null) {
            synchronized (UserFavourites.class) {
                if (userFavourites == null) {
                    userFavourites = new UserFavourites(context, userID);
                }
            }
        }
        return userFavourites;
    }

    synchronized void clearInstance() {
        userFavourites = null;
    }

    void addPersonalPostToFavourites(String postID) {
        if (personalPostFavouritesSet == null) {
            personalPostFavouritesSet = new HashSet<>();
        }
        personalPostFavouritesSet.add(postID);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(Config.PERSONAL_POST, personalPostFavouritesSet).apply();
    }

    void removePersonalPostFavourites(String postID) {
        if (personalPostFavouritesSet != null) {
            //if the post has been removed from the set
            //add the set to shared preferences
            personalPostFavouritesSet.remove(postID);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet(Config.PERSONAL_POST, personalPostFavouritesSet).apply();

        }
    }

    void addApartmentPostToFavourites(String postID) {
        if (apartmentFavouritesSet == null) {
            apartmentFavouritesSet = new HashSet<>();
        }
        apartmentFavouritesSet.add(postID);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(Config.APARTMENT_POST, apartmentFavouritesSet).apply();
    }

    void removeApartmentPostFavourites(String postID) {

        //create a new set since this set cannot be modified
        if (apartmentFavouritesSet != null) {
            //if the post has been removed from the set
            //add the set to shared preferences
            apartmentFavouritesSet.remove(postID);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet(Config.APARTMENT_POST, apartmentFavouritesSet).apply();

        }
    }

    Set<String> getPersonalPostFavourites() {
        return personalPostFavouritesSet;
    }

    Set<String> getApartmentPostFavourites() {
        return apartmentFavouritesSet;
    }


}
