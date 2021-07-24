package com.example.shroomies;

public class Config {
    public static final String apartmentID="apartmentID";
    public static final String adminID="admin";
    public static final String taskCards="taskCard";
    public static final String expensesCards="expensesCards";
    public static final String apartmentMembers="apartmentMembers";
    public static final String logs="logs";

    //FUNCTIONS NAMES
    public static final String URL_ADD_EXPENSES_CARDS = "https://us-central1-shroomies-e34d3.cloudfunctions.net/addExpensesCard";
    public static final String URL_ADD_TASK_CARDS = "https://us-central1-shroomies-e34d3.cloudfunctions.net/addTasksCards";
    public static final String URL_GET_APARTMENT_DETAILS = "https://us-central1-shroomies-e34d3.cloudfunctions.net/getApartmentDetails";
    public static final String FUNCTION_GET_USER_DETAIL = "getUserDetails";
    public static final String FUNCTION_LEAVE_APARTMENT = "leaveApartment";
    public static final String FUNCTION_DELETE_EXPENSE_CARD = "deleteExpensesCard";
    public static final String FUNCTION_DELETE_TASK_CARD = "deleteTasksCard";
    public static final String FUNCTION_ARCHIVE_TASKS_CARD = "archiveTasksCard";
    public static final String FUNCTION_ARCHIVE_EXPENSES_CARD = "archiveExpensesCard";
    public static final String FUNCTION_MARK_EXPENSES_CARD = "markingExpensesCard";
    public static final String FUNCTION_MARK_TASK_CARD = "markingTaskCard";
    public static final String FUNCTION_GET_ARCHIVE = "markingTaskCard";
    public static final String FUNCTION_SEARCH_USERS = "searchUserName";


}
