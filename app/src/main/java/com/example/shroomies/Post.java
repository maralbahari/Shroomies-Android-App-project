package com.example.shroomies;

public class Post {
    private String postid;
    private String postimage;
    private String description;
    private String userID;

    public Post(String postid, String postimage, String description, String userID) {
        this.postid = postid;
        this.postimage = postimage;
        this.description = description;
        this.userID = userID;
    }

    public Post() {
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
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
}