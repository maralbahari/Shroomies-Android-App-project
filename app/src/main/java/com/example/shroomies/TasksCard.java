package com.example.shroomies;

public class TasksCard {

    String taskDescription, taskTitle, taskDueDate, taskImportance, members, date, taskCardId;

    public TasksCard() {
    }

    public TasksCard(String description, String taskTitle, String taskDueDate, String taskImportance, String date, String taskCardId, String members) {
        this.taskCardId = taskCardId;
        this.date = date;
        this.taskDescription = description;
        this.taskImportance = taskImportance;
        this.members = members;
        this.taskTitle = taskTitle;
        this.taskDueDate = taskDueDate;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public void setTaskDueDate(String taskDueDate) {
        this.taskDueDate = taskDueDate;
    }

    public void setTaskImportance(String taskImportance) {
        this.taskImportance = taskImportance;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTaskCardId(String taskCardId) {
        this.taskCardId = taskCardId;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public String getTaskDueDate() {
        return taskDueDate;
    }

    public String getTaskImportance() {
        return taskImportance;
    }

    public String getMembers() {
        return members;
    }

    public String getDate() {
        return date;
    }

    public String getTaskCardId() {
        return taskCardId;
    }
}