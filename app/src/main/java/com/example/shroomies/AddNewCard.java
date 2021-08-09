package com.example.shroomies;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.text.NumberFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import com.hendraanggrian.appcompat.socialview.Mention;
import com.hendraanggrian.appcompat.widget.MentionArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;


import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

interface CardUploaded{
     void sendData(TasksCard tasksCard , ExpensesCard expensesCard);

}
public class AddNewCard extends DialogFragment implements SplitExpenses.membersShares {
    //static
    private static final long MAX_FILES_SIZE_IN_BYTES =20 ;
    private static final int CAMERA_REQUEST_CODE = 100, IMAGE_PICK_GALLERY_CODE = 400,
                     PDF_PICK_CODE =500 ,DIALOG_RESULT=100;
    //views
    private View v;
    private TextView addCardTextView;
    private RelativeLayout imageRelativeLayout , addCardRelativeLayout , rootLayout;
    private ImageView selectedImageView;
    private EditText titleEditText, descriptionEditText;
    private SocialAutoCompleteTextView mentionAutoCompleteTextView;
    private RadioGroup cardColorRadioGroup;
    private Chip dateChip,expenseChip;
    private LottieAnimationView loadingLottieAnimationView;
    //fireBase
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;
    private FirebaseStorage mStorage;
    //data structures
    private ArrayList<User> apartmentMembersArrayList =new ArrayList<>();
    private final HashMap<String, String> nameAndIdHashMap =new HashMap<>(), apartmentMembersHashMap =new HashMap<>();
    private HashMap<String, Integer> sharedAmountsHashMap;
    //variables
    private ShroomiesApartment apartment;
    private boolean  expensesCardSelected;
    private String fileExtension,captureFileName , fileType , dueDate;
    private Uri chosenImageUri = null;
    //interface
    private CardUploaded cardUploaded;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        cardUploaded = (CardUploaded) getTargetFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestQueue= Volley.newRequestQueue(getActivity());
        mAuth = FirebaseAuth.getInstance();
//        mAuth.useEmulator("10.0.2.2" , 9099);
        mStorage = FirebaseStorage.getInstance();
//        mStorage.useEmulator("10.0.2.2",9199);
        v = inflater.inflate(R.layout.fragment_add_new_card, container, false);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);

        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button attachFileButton = v.findViewById(R.id.my_shroomies_attach_button);
        addCardTextView = v.findViewById(R.id.my_shroomies_add_text_view);
        titleEditText = v.findViewById(R.id.new_card_title);
        descriptionEditText = v.findViewById(R.id.new_card_description);
        cardColorRadioGroup = v.findViewById(R.id.newcard_radio_group);
        Button dueDateButton = v.findViewById(R.id.due_date);
        mentionAutoCompleteTextView = v.findViewById(R.id.tag_shroomie);
        ImageButton closeImageButton = v.findViewById(R.id.x_button_new_card);
        Button splitExpensesButton = v.findViewById(R.id.split_expenses);
        addCardRelativeLayout = v.findViewById(R.id.my_shroomies_add_card_layout);
        selectedImageView = v.findViewById(R.id.attachment_image_view);
        imageRelativeLayout = v.findViewById(R.id.image_relative_layout);
        rootLayout = v.findViewById(R.id.add_new_card_root_layout);
        ImageButton deleteImageButton = v.findViewById(R.id.delete_attached_image);
        loadingLottieAnimationView = v.findViewById(R.id.lottie_loading_animation);
        dateChip = v.findViewById(R.id.date_chip);
        expenseChip = v.findViewById(R.id.expense_chip);

        mentionAutoCompleteTextView.setMentionEnabled(true);
        mentionAutoCompleteTextView.setMentionColor(Color.BLUE);

        mentionAutoCompleteTextView.setHint("@mention");
        mentionAutoCompleteTextView.setTag("@");

        closeImageButton.setOnClickListener(view1 -> dismiss());

        final Bundle bundle = this.getArguments();
        if (bundle != null) {
            expensesCardSelected = bundle.getBoolean("Expenses");
            apartment=bundle.getParcelable("APARTMENT_DETAILS");
            if(apartment.apartmentMembers!=null) {
                apartmentMembersHashMap.putAll(apartment.getApartmentMembers());
            }
            getMemberUserNames(apartmentMembersHashMap);
            if (!expensesCardSelected) {
                attachFileButton.setVisibility(View.GONE);
                splitExpensesButton.setVisibility(View.GONE);
            }
        }

        dateChip.setOnCloseIconClickListener(v -> {
            dateChip.setText("");
            dateChip.setVisibility(View.GONE);
            dueDate=null;
        });
        expenseChip.setOnCloseIconClickListener(v -> {
            sharedAmountsHashMap = null;
            expenseChip.setText("");
            expenseChip.setVisibility(View.GONE);
        });
        splitExpensesButton.setOnClickListener(view14 -> {
            if(!apartmentMembersArrayList.isEmpty()) {
                SplitExpenses split = new SplitExpenses();
                Bundle bundle1 = new Bundle();
                bundle1.putParcelable("APARTMENT_DETAILS", apartment);
                bundle1.putParcelableArrayList("MEMBERS", apartmentMembersArrayList);
                split.setArguments(bundle1);
                split.setTargetFragment(AddNewCard.this, DIALOG_RESULT);
                split.show(getParentFragmentManager(), "split expenses");
            }
        });

        attachFileButton.setOnClickListener(view15 -> showImagePickDialog());

        deleteImageButton.setOnClickListener(v -> {
            // clear the image uri and set the visibilty  to gone
            chosenImageUri = null;
            imageRelativeLayout.setVisibility(View.GONE);
        });

        addCardRelativeLayout.setOnClickListener(v -> {
            if (checkMentions()) {
                loadingLottieAnimationView.setVisibility(View.VISIBLE);
                addCardTextView.setText("Uploading");
                //disable click on layout to prevent uploading before the current task is done;
                addCardRelativeLayout.setClickable(false);
                String cardColor, mdescription, mtitle, mdueDate;
                mdueDate = dueDate;

                if(cardColorRadioGroup.getCheckedRadioButtonId() ==R.id.newcard_red_radio_button ){
                    cardColor = "2";
                }else if(cardColorRadioGroup.getCheckedRadioButtonId()== R.id.newcard_orange_radio_button){
                    cardColor = "1";

                }else{
                    cardColor = "0";
                }

                mdescription = descriptionEditText.getText().toString();
                mtitle = titleEditText.getText().toString();

                //get the mentions as a map with keys and values of the user
                JSONObject mMention = getMentionsJSONObject(mentionAutoCompleteTextView.getMentions());

                if (mtitle.isEmpty() || mdescription.isEmpty()) {
                    Snackbar.make(rootLayout, "Please insert Title and Description", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();
                    loadingLottieAnimationView.setVisibility(View.GONE);
                    addCardTextView.setText("Add card");
                    addCardRelativeLayout.setClickable(true);
                } else {
                    if (!expensesCardSelected) {
                        saveTaskCardToFirebase(mtitle, mdescription, mdueDate, cardColor, mMention, apartment.getApartmentID());
                    } else {
                        uploadImgToFirebaseStorage(mtitle, mdescription, mdueDate, cardColor, chosenImageUri, mMention, sharedAmountsHashMap);
                    }
                }
            }
        });

        dueDateButton.setOnClickListener(view13 -> {

            final Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(getContext(), (view12, year1, month1, dayOfMonth) -> {
                DateTimeFormatter dateformat =  DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss Z")
                        .withZone(TimeZone.getDefault().toZoneId());
                Calendar c= Calendar.getInstance();
                c.set(year1, month1,dayOfMonth);
                DateTimeFormatter displayFormat =  DateTimeFormatter.ofPattern("EEE, MMM d")
                        .withZone(TimeZone.getDefault().toZoneId());

//                        ZonedDateTime zonedDateTime  = new ZonedDateTime(new LocalDateTime());
                String sDate=dateformat.format(c.toInstant());
                //set the visibility of the due date to visible
                dateChip.setVisibility(View.VISIBLE);
                dateChip.setText(displayFormat.format(c.toInstant()));
                dueDate = dateformat.format(c.toInstant());

            }, year, month, day);
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });



        requestQueue = Volley.newRequestQueue(getActivity());
    }

    private JSONObject getMentionsJSONObject(List<String> userNames) {
        JSONObject mentionedUsers = new JSONObject();
        for(String username
        :userNames){
            String id = nameAndIdHashMap.get(username);
            if(id!=null){
                try {
                    mentionedUsers.put(nameAndIdHashMap.get(username) , nameAndIdHashMap.get(username));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return mentionedUsers;
    }



    private boolean checkMentions() {
//        if the user didn't add any mentions return true
        if(mentionAutoCompleteTextView.getText().toString().isEmpty()){
            return true;
        }
        //check if the mention edit text contains escaped named (names the are not mentioned)
        String[] mentions = mentionAutoCompleteTextView.getText().toString().split(" ");
        for(String mention
        :mentions){
            if(!mention.startsWith("@")){
                Snackbar.make(rootLayout, "Please ensure mentioned members start with @", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();
                return false;
            }
        }
        //check if the mentioned users are in the member list
        for (Iterator<String>  iterator=
            mentionAutoCompleteTextView.getMentions().iterator(); iterator.hasNext();){
            String name = iterator.next();
            if(!nameAndIdHashMap.containsKey(name)){
                Snackbar.make(rootLayout, "Member "+ name +" doesn't exist", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();
                return false;
            }
        }
        return true;
    }

    private void getMemberUserNames(final HashMap<String,String> membersHashMap) {
        ArrayList<String> members = new ArrayList<>();
        //add the the admin to the members
        if(membersHashMap!=null){
            members.addAll(membersHashMap.values());
        }
        members.add(apartment.getAdminID());
        apartmentMembersArrayList = new ArrayList<>();
        JSONObject jsonObject=new JSONObject();
        JSONObject data = new JSONObject();
        JSONArray jsonArray = new JSONArray(members);

        try {

            jsonObject.put(Config.membersID,jsonArray);
            data.put(Config.data , jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String token = task.getResult().getToken();
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.FUNCTION_GET_MEMBER_DETAIL, data, response -> {
                    final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper

                    try {
                        boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                        if(success){
                            JSONArray users = response.getJSONObject(Config.result).getJSONArray(Config.members);
                            if(users!=null) {
                                if (users.length()!=0) {
                                    ArrayAdapter<Mention> mentionAdapter = new MentionArrayAdapter<>(getActivity() , R.drawable.ic_user_profile_svgrepo_com);
                                    for (int i = 0 ; i<jsonArray.length();i++) {
                                        User user = null;
                                        try {
                                            user = mapper.readValue(users.get(i).toString(), User.class);
                                        } catch (JSONException | JsonProcessingException e) {
                                            e.printStackTrace();
                                        }
                                        if(user!=null) {
                                            apartmentMembersArrayList.add(user);
                                            nameAndIdHashMap.put(user.getName() , user.getUserID());
                                            mentionAdapter.add(new Mention(user.getName()));
                                            mentionAutoCompleteTextView.setMentionAdapter(mentionAdapter);
                                        }

                                    }
                                }
                            }
                        }else{
                            displayErrorAlert("Error" ,  null , "We encountered an unexpected error");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> displayErrorAlert("Error" ,  error , null))
                {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                        params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
                        return params;
                    }
                };
                requestQueue.add(jsonObjectRequest);
            }else{
                String title = "Authentication error";
                String message = "We encountered a problem while authenticating your account";
                displayErrorAlert(title , null ,message );
            }
        });

    }

    private void saveTaskCardToFirebase(String mtitle, String mdescription, String mdueDate, String importance, JSONObject mMention, String apartmentID) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult().getToken();
                JSONObject jsonObject = new JSONObject();
                JSONObject data = new JSONObject();
                JSONObject cardDetails = new JSONObject();
                //get the  date with timezone
                DateTimeFormatter dateformat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss Z")
                        .withZone(TimeZone.getDefault().toZoneId());
                String currentDate = dateformat.format(ZonedDateTime.now());
                try {
                    cardDetails.put("description", mdescription);
                    cardDetails.put("title", mtitle);
                    cardDetails.put("dueDate", mdueDate);
                    cardDetails.put("importance", importance);
                    cardDetails.put("date", currentDate);
                    cardDetails.put("cardID", "");
                    cardDetails.put("done", "false");
                    cardDetails.put("mention", mMention);
                    cardDetails.put("actor", mAuth.getCurrentUser().getUid());
                    jsonObject.put("cardDetails", cardDetails);
                    jsonObject.put("apartmentID", apartmentID);
                    data.put("data", jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_ADD_TASK_CARDS, data, response -> {
                    //send the card back to the list in my shroomies fragment
                    //create a task card and add the card id from the response
                    try {
                        boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                        String message = response.getJSONObject(Config.result).getString(Config.message);
                        if(success) {
                            String cardID = response.getJSONObject(Config.result).getString(Config.message);
                            if (cardID != null) {
                                TasksCard taskCard = new TasksCard(mdescription, mtitle, mdueDate, importance, currentDate, cardID, "false", new ObjectMapper().readValue(mMention.toString(), HashMap.class));
                                cardUploaded.sendData(taskCard, null);
                                dismiss();
                            }
                        }else{
                            Snackbar.make(rootLayout,message , BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();
                            addCardTextView.setText("Add card");
                            addCardRelativeLayout.setClickable(true);
                        }
                    } catch (JSONException | JsonProcessingException e) {
                        e.printStackTrace();
                    }

                }, error -> {
                    Snackbar.make(getView(), "Couldn't add card , connection error", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();
                    loadingLottieAnimationView.setVisibility(View.GONE);
                    addCardTextView.setText("Add card");
                    addCardRelativeLayout.setClickable(true);

                })
                {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                        params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
                        return params;
                    }
                };
                requestQueue.add(jsonObjectRequest);
            }else{
                loadingLottieAnimationView.setVisibility(View.GONE);
                addCardTextView.setText("Add card");
                addCardRelativeLayout.setClickable(true);
                String title = "Authentication error";
                String message = "We encountered a problem while authenticating your account";
                displayErrorAlert(title , null ,message );

            }
        });
    }

    public void addExpenseCard(String title, String description, String dueDate, String attachUrl , String fileType, String importance, JSONObject mMention, String apartmentID, HashMap<String, Integer> shareAmounts) {
       //get the authorization token
        //if authorization token is recived then proceed
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                String token = task.getResult().getToken();
                JSONObject jsonObject = new JSONObject();
                JSONObject data = new JSONObject();
                JSONObject cardDetails = new JSONObject();
                //get the  date with timezone
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss Z")
                        .withZone(TimeZone.getDefault().toZoneId());

                try {
                    cardDetails.put("description", description);
                    cardDetails.put("title", title);
                    cardDetails.put("dueDate", dueDate);
                    cardDetails.put("importance", importance);
                    cardDetails.put("attachedFile", attachUrl);
                    cardDetails.put("fileType", fileType);
                    cardDetails.put("date", dateFormat.format(ZonedDateTime.now()));
                    cardDetails.put("cardID", "");
                    cardDetails.put("done", "false");
                    if(mMention!=null){
                        if(mMention.length()!=0){
                            cardDetails.put("mention", mMention);
                        }
                    }
                    cardDetails.put("actor", mAuth.getCurrentUser().getUid());
                    if (shareAmounts != null) {
                        JSONObject sharedAmountsJson = new JSONObject(shareAmounts);
                        cardDetails.put("membersShares", sharedAmountsJson);
                    }
                    jsonObject.put("cardDetails", cardDetails);
                    jsonObject.put("apartmentID", apartmentID);
                    data.put("data", jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //TODO add progress dialog
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_ADD_EXPENSES_CARDS, data, response -> {
                   try {

                       boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                       String message = response.getJSONObject(Config.result).getString(Config.message);
                       if(success){
                           String cardID = response.getJSONObject(Config.result).getString(Config.message);
                           ExpensesCard expensesCard = new ExpensesCard(attachUrl, description, title, dueDate, importance,fileType, cardID, "false", new ObjectMapper().readValue(mMention.toString(), HashMap.class), shareAmounts);
                           cardUploaded.sendData(null, expensesCard);
                           dismiss();
                       }else{
                           Snackbar.make(rootLayout,message , BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();
                           addCardTextView.setText("Add card");
                           addCardRelativeLayout.setClickable(true);
                       }

                   } catch (JsonProcessingException | JSONException e) {
                       e.printStackTrace();
                   }
                }, error -> {
                    loadingLottieAnimationView.setVisibility(View.GONE);
                    addCardTextView.setText("Add card");
                    addCardRelativeLayout.setClickable(true);
                    Snackbar.make(rootLayout, "Couldn't add card , connection error", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();
                })
                {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                        params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
                        return params;
                    }
                }
                 ;
                requestQueue.add(jsonObjectRequest);
            }else{
                loadingLottieAnimationView.setVisibility(View.GONE);
                addCardTextView.setText("Add card");
                addCardRelativeLayout.setClickable(true);
                String title1 = "Authentication error";
                String message = "We encountered a problem while authenticating your account";
                displayErrorAlert(title1, null ,message );
            }
        });
    }


    public void uploadImgToFirebaseStorage( String title,  String description,  String dueDate,  String importance, Uri imgUri , JSONObject mMention, HashMap<String, Integer> sharedAmounts) {

        if (imgUri == null) {
            addExpenseCard(title, description, dueDate, "" , "", importance, mMention,apartment.getApartmentID(),sharedAmounts);
        } else {

            String type =  null;
            switch (fileType){
                case "image":
                     type = "image/jpg";
                    break;
                case "pdf":
                     type = "application/pdf";
                    break;
            }
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType(type)
                    .build();

            StorageReference filePath = mStorage.getReference().child(apartment.getApartmentID()).child("Card post image").child(imgUri.getLastPathSegment() + fileExtension);
            filePath.putFile(imgUri , metadata).addOnCompleteListener(task -> task.getResult().getMetadata().getReference().getDownloadUrl().addOnSuccessListener(uri -> {
                addExpenseCard(title, description, dueDate, uri.toString()  , fileType, importance, mMention,apartment.getApartmentID(),sharedAmounts);
            })).addOnFailureListener(e -> {
                loadingLottieAnimationView.setVisibility(View.GONE);
                addCardTextView.setText("Add card");
                addCardRelativeLayout.setClickable(true);
                Snackbar.make(rootLayout, "Couldn't upload the image", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();
            });
        }

    }


    private void showImagePickDialog() {
        String[] options = {"Gallery","PDF","Camera"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose file from");
        builder.setIcon(R.drawable.ic_shroomies_yelllow_black_borders);
        builder.setCancelable(true);
        builder.setItems(options, (dialog, which) -> {
            switch (which){
                case 0:
                    pickFromGallery();
                    break;
                case  1:
                    pickFromPDF();
                    break;
                default:
                    pickFromCamera();
            }
        });
        builder.create().show();

    }


    private void pickFromPDF(){
        Dexter.withContext(getContext()).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                            // navigate user to app settings
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }else {
                            Intent intent = new Intent();
                            intent.setType("application/pdf");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select a PDF "), PDF_PICK_CODE);
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }

                }).onSameThread().check();



    }
    private void pickFromGallery() {
        Dexter.withContext(getContext()).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                            // navigate user to app settings
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }

                }).onSameThread().check();



    }

    private void pickFromCamera() {
    Dexter.withContext(getContext()).withPermissions(Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE , Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                    if(multiplePermissionsReport.areAllPermissionsGranted()){

                        Intent m_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        //get a unique id for the screenshot
                        Date date = new Date();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddhhmmssMs");
                        String datetime = simpleDateFormat.format(date);
                        captureFileName = datetime+ "newPhoto.jpg";
                        //TODO test
                        File file = new File(Environment.getExternalStorageDirectory(), captureFileName );
                        Uri uri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);
                        m_intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(m_intent, CAMERA_REQUEST_CODE);
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                    permissionToken.continuePermissionRequest();

                }
            }).check();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        getActivity();
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                File file = new File(Environment.getExternalStorageDirectory(), captureFileName);
                //check the size of the file
                if((file.length()/1048576)<= MAX_FILES_SIZE_IN_BYTES){
                    chosenImageUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);
                    fileExtension = ".jpg";
                    fileType  = "image";
                    addSelectedImageToImageView(chosenImageUri, false);
                }else{
                    Snackbar.make(rootLayout, "This file is too large", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();
                }
                return;
            }
            if (data.getData() != null) {
                if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                    if (fileSizeIsAllowed(data)) {
                        chosenImageUri = data.getData(); // load image from gallery
                        fileExtension = ".jpg";
                        fileType =  "image";
                        addSelectedImageToImageView(chosenImageUri, false);
                    } else {
                        Snackbar.make(rootLayout, "This file is too large", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();
                    }
                }
                if (requestCode == PDF_PICK_CODE) {

                    if (fileSizeIsAllowed(data)) {
                        chosenImageUri = data.getData();
                        fileExtension = ".pdf";
                        fileType = "pdf";
                        addSelectedImageToImageView(chosenImageUri, true);
                    } else {
                        Snackbar.make(rootLayout, "This file is too large", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();

                    }
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
    private boolean fileSizeIsAllowed( Intent data) {
        //get the file size
        Uri returnUri = data.getData();
        //convert the file size to
        long fileSizeInMB  = FileInformation.getSize(getActivity() , returnUri)/1048576;
        return fileSizeInMB <= MAX_FILES_SIZE_IN_BYTES;
    }

    @Override
    public void sendInput(HashMap<String, Integer> sharedSplit) {
        this.sharedAmountsHashMap =sharedSplit;
        if(sharedAmountsHashMap!=null) {
            if (!sharedAmountsHashMap.isEmpty()) {
                int totalAmount = 0;
                for (Map.Entry entry
                        : sharedAmountsHashMap.entrySet()) {
                    int amount = (int) entry.getValue();
                    totalAmount = Integer.sum(totalAmount, amount);
                }
                NumberFormat numberFormat = NumberFormat.getInstance();
                numberFormat.setGroupingUsed(true);
                expenseChip.setText(numberFormat.format(totalAmount) + " RM");
                expenseChip.setVisibility(View.VISIBLE);
            }
        }
    }

    private void addSelectedImageToImageView(Uri uri , boolean isPDF){
        // set the visibility of the
        // relative layout that holds
        // the image view  to visible

            imageRelativeLayout.setVisibility(View.VISIBLE);
            if (isPDF) {
                PDF pdf = new PDF(getActivity());
                Bitmap bmp  = pdf.getPDFBitmap(uri);
                if(bmp!=null){
                    selectedImageView.setImageBitmap(bmp);
                    Glide.with(getActivity())
                            .load(bmp)
                            .transform(new CenterCrop(),new RoundedCorners(5) )
                            .error(R.drawable.ic_no_file_added)
                            .into(selectedImageView);
                }else{
                    captureFileName = null;
                    Snackbar.make(rootLayout, "Couldn't load this file", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();

                }
            } else {
                Glide.with(getActivity())
                        .load(uri)
                        .transform(new CenterCrop(),new RoundedCorners(5) )
                        .error(R.drawable.ic_no_file_added)
                        .into(selectedImageView);
            }
        }

    void displayErrorAlert(String title  , @Nullable VolleyError error ,@Nullable String errorMessage){
        String message = null; // error message, show it in toast or dialog, whatever you want
        if(error!=null) {
            if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                message = "Cannot connect to Internet";
            } else if (error instanceof ServerError) {
                message = "The server could not be found. Please try again later";
            } else if (error instanceof ParseError) {
                message = "Parsing error! Please try again later";
            }
        }else{
            message = errorMessage;
        }
        new android.app.AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_alert)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("return", (dialog, which) -> {
                    AddNewCard.this.dismiss();
                    dialog.dismiss();
                })
                .create()
                .show();
    }



}





