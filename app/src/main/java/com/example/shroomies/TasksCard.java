package com.example.shroomies;

public class TasksCard {

    String description, title,dueDate, importance, members, date, cardId;

    public TasksCard() {
    }

    public TasksCard(String description, String taskTitle, String taskDueDate, String taskImportance, String date, String taskCardId, String members) {
        this.cardId = taskCardId;
        this.date = date;
        this.description = description;
        this.importance = taskImportance;
        this.members = members;
        this.title = taskTitle;
        this.dueDate = taskDueDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(String importance) {
        this.importance = importance;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }
}