package com.example.shroomies;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;

public class SessionManager {
    SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    // we will keep a list of users to make the task of looking for logged in users easier.
    SharedPreferences listOfUsers;
    SharedPreferences.Editor listEditor;
    public Context context;
    int PRIVATE_MODE = 0;
    private static final String USERNAME_LIST = "USERNAME_LIST";
    private static final String LOGIN = "IS_LOGGED_IN";
    public static final String NAME = "NAME";
    public static final String EMAIL = "EMAIL";
    public static final String BIOMETRIC_ENABLED = "BIOMETRIC_ENABLED";

    //constructor gets the shared preferences or creates a new session if it doesn't exist
    // each user will have his/her own shared preferences folder
    public SessionManager(Context context, String userName) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(userName, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }


    public void createSession(String name,String email) {
        HashMap<String, String> user = getUserDetail();
        // if the session is new , then populate the new SP file with the data.
        if (user.get("NAME") == null || user.get("EMAIL") == null) {
            editor.putString(NAME, name);
            editor.putString(EMAIL, email);
            // add the user's name to the list , if the list doesn't exist then a new one will be generated.
            addUserNameToList(name);
        }
        //change the user's status to logged in.
        editor.putBoolean(LOGIN, true);
        editor.apply();
    }

    private void addUserNameToList(String name) {
        listOfUsers = context.getSharedPreferences(USERNAME_LIST ,PRIVATE_MODE);
        listEditor = listOfUsers.edit();
        listEditor.putString(NAME , name);
        listEditor.apply();
    }

    //check if the user is logged in.
    public boolean isLoggedIn() {

        return sharedPreferences.getBoolean(LOGIN, false);

    }

    // get the user details from the shared preferences file and return the data in a hashMap
    public HashMap<String, String> getUserDetail() {

        HashMap<String, String> user = new HashMap<>();
        user.put(NAME, sharedPreferences.getString(NAME, null));
        user.put(EMAIL, sharedPreferences.getString(EMAIL, null));
        user.put(BIOMETRIC_ENABLED, Boolean.toString(sharedPreferences.getBoolean(BIOMETRIC_ENABLED, false)));
        return user;

    }

    // keeps the user's data stored and changes his login status to false
    public void logout() {

        editor.putBoolean(LOGIN, false);
        editor.apply();

    }

    // allows  biometric login , use this method in registration and in settings.
    public void setBiometricEnabled(boolean allowed) {

        editor.putBoolean(BIOMETRIC_ENABLED, allowed);
        editor.apply();

    }
    //checks weather the user allowed the biometric login from this phone or not
    public boolean biometricIsEnabled() {

        return sharedPreferences.getBoolean(BIOMETRIC_ENABLED, false);

    }

    // clears all  data , use this method only when user deletes account or wants to remove all shared preferences data.
    void deleteSession() {

        editor.clear();
        editor.apply();

    }
}