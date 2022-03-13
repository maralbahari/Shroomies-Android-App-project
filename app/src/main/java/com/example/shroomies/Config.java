package com.example.shroomies;

public class Config {
    //myShroomies Activity and its fragments
    public static final String apartmentID="apartmentID";
    public static final String adminID="adminID";
    public static final String userID="userID";
    public static final String membersID="membersID";
    public static final String taskCards="taskCard";
    public static final String expensesCards="expensesCard";
    public static final String apartmentMembers="apartmentMembers";
    public static final String data="data";
    public static final String name="name";
    public static final String currentUser="currentUser";
    public static final String id="ID";
    public static final String success="success";
    public static final String apartment="apartment";
    public static final String members="members";
    public static final String message = "message";
    public static final String cards = "cards";
    public static final String requests = "requests";
    public static final String user="user";
    public static final String receiverID = "receiverID" ;
    public static final String role = "role";
    public static final String receiverApartmentID = "receiverApartmentID";
    public static final String senderID = "senderID";
    public static final String cardDetails = "cardDetails";
    public static final String cardID= "cardID";
    public static final String result="result";
    public static final String logs="logs";
    public static final String task = "task";
    public static final String expenses  = "expenses";
    public static final String privateMessage="privateMessage";
    public static final String groupMessage="groupMessage";
    //log actions
    public static final String deletingCard="deletingCard";
    public static final String addingCard = "addingCard";
    public static final String archivingCard = "archivingCard";
    public static final String deletingArchivedCard = "deletingArchivedCard";
    public static final String markingCard  = "markingCard";
    public static final String unMarkingCard = "unMarkingCard";
    public static final String left = "left";
    public static final String joined = "joined";
    public static final String removed = "removed";
    public static final String token = "token";
    public static final String expenseImages ="expenseImages" ;
    public static final String messages="messages";
    public static final String users = "users";
    public static final String inboxes="inboxes";
    public static final String groupMessages = "groupMessages";
    public static final String image = "image";
    public static final String PREFERENCES = "PREFERENCES";
    //chat
    public static final String privateChatImages = "privateChatImages";



    public static String usersnames = "usernames";









    //FUNCTIONS NAMES
//    public static final String URL_ADD_EXPENSES_CARDS = "https://us-central1-shroomies-e34d3.cloudfunctions.net/addExpensesCard";
//    public static final String URL_ADD_TASK_CARDS = "https://us-central1-shroomies-e34d3.cloudfunctions.net/addTasksCards";
//    public static final String URL_GET_APARTMENT_DETAILS = "https://us-central1-shroomies-e34d3.cloudfunctions.net/getApartmentDetails";
//    public static final String FUNCTION_GET_USER_DETAIL = "getUserDetails";
//    public static final String FUNCTION_LEAVE_APARTMENT = "leaveApartment";
//    public static final String FUNCTION_DELETE_EXPENSE_CARD = "deleteExpensesCard";
//    public static final String FUNCTION_DELETE_TASK_CARD = "deleteTasksCard";
//    public static final String FUNCTION_ARCHIVE_TASKS_CARD = "archiveTasksCard";
//    public static final String FUNCTION_ARCHIVE_EXPENSES_CARD = "archiveExpensesCard";
//    public static final String FUNCTION_MARK_EXPENSES_CARD = "markingExpensesCard";
//    public static final String FUNCTION_MARK_TASK_CARD = "markingTaskCard";
//    public static final String FUNCTION_GET_ARCHIVE = "markingTaskCard";
//    public static final String FUNCTION_SEARCH_USERS = "searchUserName";

//    public static final String URL_ADD_EXPENSES_CARDS = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/addExpensesCard";
//    public static final String URL_ADD_TASK_CARDS = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/addTasksCards";
//    public static final String URL_GET_APARTMENT_DETAILS = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/getApartmentDetails";
//    public static final String FUNCTION_GET_MEMBER_DETAIL = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/getMembersDetails";
//    public static final String FUNCTION_LEAVE_APARTMENT = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/leaveApartment";
//    public static final String FUNCTION_DELETE_EXPENSE_CARD = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/deleteExpensesCard";
//    public static final String FUNCTION_DELETE_TASK_CARD = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/deleteTasksCard";
//    public static final String FUNCTION_ARCHIVE_TASKS_CARD = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/archiveTasksCard";
//    public static final String FUNCTION_ARCHIVE_EXPENSES_CARD = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/archiveExpensesCard";
//    public static final String FUNCTION_MARK_EXPENSES_CARD = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/markingExpensesCard";
//    public static final String FUNCTION_MARK_TASK_CARD = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/markingTaskCard";
//    public static final String FUNCTION_GET_ARCHIVE = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/getArchivedCard";
//    public static final String FUNCTION_SEARCH_USERS = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/searchUserName";
//    public static final String FUNCTION_DELETE_TASK_CARD_ARCHIVE = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/deleteArchiveTasksCard";
//    public static final String FUNCTION_DELETE_EXPENSE_CARD_ARCHIVE = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/deleteArchiveExpensesCard";
//    public static final String URL_SEND_REQUEST = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/sendRequest";
//    public static final String URL_GET_REQUESTS = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/getRequests";
//    public static final String URL_CANCEL_OR_REJECT_REQUEST= "http://10.0.2.2:5001/shroomies-e34d3/us-central1/cancelOrRejectRequest";
//    public static final String URL_ACCEPT_REQUEST = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/acceptRequest";
//    public static final String URL_GET_USER_DETAILS= "http://10.0.2.2:5001/shroomies-e34d3/us-central1/getUserDetails";
//    public static final String URL_REGISTER_USER = "http://10.0.2.2:5001/shroomies-e34d3/us-central1/registerUser";
//    public static final String URL_REMOVE_MEMBER= "http://10.0.2.2:5001/shroomies-e34d3/us-central1/removeMember";


    public static final String URL_ADD_EXPENSES_CARDS = "https://us-central1-shroomies-e34d3.cloudfunctions.net/addExpensesCard";
    public static final String URL_ADD_TASK_CARDS = "https://us-central1-shroomies-e34d3.cloudfunctions.net/addTasksCards";
    public static final String URL_GET_APARTMENT_DETAILS = "https://us-central1-shroomies-e34d3.cloudfunctions.net/getApartmentDetails";
    public static final String URL_GET_MEMBER_DETAIL = "https://us-central1-shroomies-e34d3.cloudfunctions.net/getMembersDetails";
    public static final String URL_LEAVE_APARTMENT = "https://us-central1-shroomies-e34d3.cloudfunctions.net/leaveApartment";
    public static final String URL_DELETE_EXPENSE_CARD = "https://us-central1-shroomies-e34d3.cloudfunctions.net/deleteExpensesCard";
    public static final String URL_DELETE_TASK_CARD = "https://us-central1-shroomies-e34d3.cloudfunctions.net/deleteTasksCard";
    public static final String URL_ARCHIVE_TASKS_CARD = "https://us-central1-shroomies-e34d3.cloudfunctions.net/archiveTasksCard";
    public static final String URL_ARCHIVE_EXPENSES_CARD = "https://us-central1-shroomies-e34d3.cloudfunctions.net/archiveExpensesCard";
    public static final String URL_MARK_EXPENSES_CARD = "https://us-central1-shroomies-e34d3.cloudfunctions.net/markingExpensesCard";
    public static final String URL_MARK_TASK_CARD = "https://us-central1-shroomies-e34d3.cloudfunctions.net/markingTaskCard";
    public static final String URL_GET_ARCHIVE = "https://us-central1-shroomies-e34d3.cloudfunctions.net/getArchivedCard";
    public static final String URL_SEARCH_USERS = "https://us-central1-shroomies-e34d3.cloudfunctions.net/searchUserName";
    public static final String URL_DELETE_TASK_CARD_ARCHIVE = "https://us-central1-shroomies-e34d3.cloudfunctions.net/deleteArchiveTasksCard";
    public static final String URL_DELETE_EXPENSE_CARD_ARCHIVE = "https://us-central1-shroomies-e34d3.cloudfunctions.net/deleteArchiveExpensesCard";
    public static final String URL_SEND_REQUEST = "https://us-central1-shroomies-e34d3.cloudfunctions.net/sendRequest";
    public static final String URL_GET_REQUESTS = "https://us-central1-shroomies-e34d3.cloudfunctions.net/getRequests";
    public static final String URL_CANCEL_OR_REJECT_REQUEST = "https://us-central1-shroomies-e34d3.cloudfunctions.net/cancelOrRejectRequest";
    public static final String URL_ACCEPT_REQUEST = "http://us-central1-shroomies-e34d3.cloudfunctions.net/acceptRequest";
    public static final String URL_GET_USER_DETAILS = "https://us-central1-shroomies-e34d3.cloudfunctions.net/getUserDetails";
    public static final String URL_REGISTER_USER = "https://us-central1-shroomies-e34d3.cloudfunctions.net/registerUser";
    public static final String URL_REMOVE_MEMBER = "https://us-central1-shroomies-e34d3.cloudfunctions.net/removeMember";
    public static final String URL_GET_LOGS = "https://us-central1-shroomies-e34d3.cloudfunctions.net/getLogs";
    public static final String URL_PUBLISH_POST = "https://us-central1-shroomies-e34d3.cloudfunctions.net/publishPost";
    public static final String URL_GET_VIRGIL_JWT = "https://us-central1-shroomies-e34d3.cloudfunctions.net/getVirgilJwt";

    //publish post
    public static final String LOCALITY = "locality";
    public static final String SUB_LOCALITY = "subLocality";
    public static final String SELECTED_LAT_LNG = "selectedLatLng";
    public static final String DESCRIPTION = "description";

    public static final String POST_TYPE = "postType";
    public static final String PERSONAL_POST = "personalPost";
    public static final String APARTMENT_POST = "apartmentPost";

    public static final String BUDGET = "budget";

    public static final String NUMBER_OF_ROOMMATES = "numberOfRoommates";
    public static final String PREFERENCE = "preferences";
    public static final String MALE = "male";
    public static final String FEMALE = "female";
    public static final String NON_SMOKING = "nonSmoking";
    public static final String PET_FRIENDLY = "pet";

    public static final String TYPE_HOUSE = "house";
    public static final String TYPE_APARTMENT = "apartment";
    public static final String TYPE_FLAT = "flat";
    public static final String TYPE_CONDO = "condo";
    public static final String BUILDING_TYPE = "buildingType";
    public static final String BUILDING_NAME = "buildingName";
    public static final String BUILDING_ADDRESS = "buildingAddress";
    public static final String PRICE = "price";
    public static final String IMAGE_URL = "imageUrl";
    public static final String GEO_HASH = "geoHash";
    public static final String TIME_STAMP = "timeStamp";
    public static final String POST_DETAILS = "postDetails";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";


    public static final String USERNAME_PATTERN =
            "^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]$";
    public static final String APARTMENT_POST_IMAGE = "apartmentPostImage";
    public static final String BUILDING_TYPES = "buildingTypes";
    public static final String PERSONAL_POST_LIST = "PERSONAL_POST_LIST";
    public static final String KEY_WORDS = "keyWords";
    public static final String SHOW_POSTS_NEAR_ME = "show_posts_near_me";
    public static final String STATE_PREFERENCE = "state_preference";

    public static final String NONE = "None";
    public static final String LOW_VALUE = "lowValue";
    public static final String HIGH_VALUE = "highValue";
    public static final String FILTER_PRICE = "filter_price";
    public static final String PROPERTIES = "properties";
    public static final String FILTER_PREFERENCE = "filter_preferences";
    public static final String APARTMENT_LIST = "APARTMENT_LIST";
    public static final String FAVOURITES = "FAVOURITES";
    public static final String IMAGE_FOLDER_PATH = "imageFolderPath";

    public static String username = "username";


}
