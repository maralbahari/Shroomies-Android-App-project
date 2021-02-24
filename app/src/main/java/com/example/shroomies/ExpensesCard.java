package com.example.shroomies;

import java.util.HashMap;

public class ExpensesCard  {

    String attachedFile, description, title, dueDate, importance,members, date, cardId, done, mention;
    HashMap<String,Object> membersShares=new HashMap<>();

    public ExpensesCard() {

    }


    public ExpensesCard(String attachedFile, String description, String title, String dueDate, String importance, String members,String cardId, String done, String mention,HashMap<String,Object> membersShares) {
        this.attachedFile = attachedFile;
        this.description = description;
        this.title = title;
        this.dueDate = dueDate;
        this.importance = importance;
        this.members = members;
        this.cardId = cardId;
        this.done = done;
        this.mention = mention;
        this.membersShares=membersShares;
    }

    public HashMap<String, Object> getMembersShares() {
        return membersShares;
    }

    public void setMembersShares(HashMap<String, Object> membersShares) {
        this.membersShares = membersShares;
    }

    public String getMention() {
        return mention;
    }

    public void setMention(String mention) {
        this.mention = mention;
    }

    public void setAttachedFile(String attachedFile) {
        this.attachedFile = attachedFile;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public void setDone(String done) {
        this.done = done;
    }

    public String getAttachedFile() {
        return attachedFile;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getImportance() {
        return importance;
    }

    public String getMembers() {
        return members;
    }

    public String getDate() {
        return date;
    }

    public String getCardId() {
        return cardId;
    }

    public String getDone() {
        return done;
    }
}
