package com.example.shroomies;

public class ExpensesCard  {

    String attachedFile, description, title, dueDate, importance,members, date, cardId;

    public ExpensesCard() {

    }


    public ExpensesCard(String attachedFile, String description, String title, String dueDate, String importance, String members,String cardId) {
        this.attachedFile = attachedFile;
        this.description = description;
        this.title = title;
        this.dueDate = dueDate;
        this.importance = importance;
        this.members = members;
        this.cardId = cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getCardId() {
        return cardId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
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
}
