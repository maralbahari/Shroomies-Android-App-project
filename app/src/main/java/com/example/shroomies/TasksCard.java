package com.example.shroomies;

public class TasksCard {

    String description, title,dueDate, importance, members, date, cardId, done;

    public TasksCard() {
    }

    public TasksCard(String description, String taskTitle, String taskDueDate, String taskImportance, String date, String taskCardId, String members, String done) {
        this.cardId = taskCardId;
        this.date = date;
        this.description = description;
        this.importance = taskImportance;
        this.members = members;
        this.title = taskTitle;
        this.dueDate = taskDueDate;
        this.done = done;
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