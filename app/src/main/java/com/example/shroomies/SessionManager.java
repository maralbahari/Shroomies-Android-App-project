package com.example.shroomies;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    public static final String ID = "ID";
    public static final String EMAIL = "EMAIL";

    public static final String VERIFIED = "VERIFIED";
    //constructor gets the shared preferences or creates a new session if it doesn't exist
    // each user will have his/her own shared preferences folder
    public SessionManager(Context context, String userID) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(userID, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }
    public SessionManager(){

    }

    public void createSession(String id,String email) {
        HashMap<String, String> user = getUserDetail();
        // if the session is new , then populate the new SP file with the data.
        if (user.get("ID") == null || user.get("EMAIL") == null) {
            editor.putString(ID, id);
            editor.putString(EMAIL, email);
            // add the user's name to the list , if the list doesn't exist then a new one will be generated.
            addUserNameToList(id);
        }
        //change the user's status to logged in.
        editor.putBoolean(LOGIN, true);
        editor.apply();
    }

    private void addUserNameToList(String id) {
        listOfUsers = context.getSharedPreferences(USERNAME_LIST ,PRIVATE_MODE);
        listEditor = listOfUsers.edit();
        listEditor.putString( id,ID );
        listEditor.apply();
    }


    //check if the user is logged in.
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(LOGIN, false);
    }

    // get the user details from the shared preferences file and return the data in a hashMap
    public HashMap<String, String> getUserDetail() {
        HashMap<String, String> user = new HashMap<>();
        user.put(ID, sharedPreferences.getString(ID, null));
        user.put(EMAIL, sharedPreferences.getString(EMAIL, null));
        return user;
    }
    //check if a user logged in and return the name of the user that is logged in
    public String checkUsersLoggedIn(Context context){
        listOfUsers = context.getSharedPreferences("USERNAME_LIST" , 0);
        Map<String , String> users = (Map<String, String>) listOfUsers.getAll();
        for(String userName : users.keySet()) {
            SessionManager user = new SessionManager(context, userName);
            if (user.isLoggedIn()) {
                return userName;
            }
        }
        return null;
    }


    // keeps the user's data stored and changes his login status to false
    public void logout() {

        editor.putBoolean(LOGIN, false);
        editor.apply();
        editor.commit();

    }


    // clears all  data , use this method only when user deletes account or wants to remove all shared preferences data.
    void deleteSession() {

        editor.clear();
        editor.apply();

    }
    //set boolean if user verified the email
    void setVerifiedEmail(boolean verified){
        editor.putBoolean(VERIFIED,verified);
        editor.apply();
    }
    //check if the email is verified
    boolean verifiedEmail(){
        return sharedPreferences.getBoolean(VERIFIED,false);
    }
    //check among all users get a particular user if they have verified their email
    public boolean checkUserEmailVerification(String userId){
        listOfUsers=context.getSharedPreferences("USERNAME_LIST",0);
        Map<String,String> users=(Map<String,String>) listOfUsers.getAll();
        for(String userID: users.keySet()){
            if(userId.equals(userID)){
                SessionManager userSession=new SessionManager(context,userId);
                return userSession.verifiedEmail();
            }
        }
        return false;
    }
}
