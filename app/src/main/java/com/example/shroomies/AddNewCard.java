package com.example.shroomies;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.NumberFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.shroomies.notifications.Data;
import com.example.shroomies.notifications.Sender;
import com.example.shroomies.notifications.Token;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import com.google.gson.JsonObject;
import com.hendraanggrian.appcompat.socialview.Mention;
import com.hendraanggrian.appcompat.widget.MentionArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.hendraanggrian.appcompat.widget.SocialEditText;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.hendraanggrian.appcompat.widget.SocialView;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;


import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class AddNewCard extends DialogFragment implements SplitExpenses.membersShares {
    //static
    private static final long MAX_FILES_SIZE_IN_BYTES =20 ;
    private static final int CAMERA_REQUEST_CODE = 100, IMAGE_PICK_GALLERY_CODE = 400,
            PICK_IMAGE_MULTIPLE = 1, PDF_PICK_CODE =500 ,DIALOG_RESULT=100;
    //views
    private View v;
    private Button attachFileButton, splitExpensesButton, dueDateButton;
    private TextView   dueDatePlaceholder , expensePlaceholder , addCardTextView;
    private RelativeLayout imageRelativeLayout, dueDateRelativeLayout, expenseRelativeLayout , addCardRelativeLayout;
    private ImageButton deleteImageButton, deleteDueDateImageButton, deleteExpenseImageButton,closeImageButton;
    private ImageView selectedImageView;
    private EditText titleEditText, descriptionEditText;
    private SocialAutoCompleteTextView mentionAutoCompleteTextView;
    private RadioGroup cardColorRadioGroup;
    private LottieAnimationView loadingLottieAnimationView;
    //fireBase
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private FirebaseFunctions mfunc;
    private RequestQueue requestQueue;
    private FirebaseStorage mStorage;
    //data structures
    private ArrayList<User> apartmentMembersArrayList =new ArrayList<>();
    private HashMap<String, String> nameAndIdHashMap =new HashMap<>(), apartmentMembersHashMap =new HashMap<>();
    public HashMap<String, Integer> sharedAmountsHashMap;
    //variables
    private ShroomiesApartment apartment;
    private boolean notify = false , expensesCardSelected;
    private String fileExtension,captureFileName , fileType;
    private Uri chosenImageUri = null;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestQueue= Volley.newRequestQueue(getActivity());
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mAuth.useEmulator("10.0.2.2" , 9099);
        mfunc=FirebaseFunctions.getInstance();
        mfunc.useEmulator("10.0.2.2",5001);
        mStorage = FirebaseStorage.getInstance();
        v = inflater.inflate(R.layout.fragment_add_new_card, container, false);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
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
        attachFileButton = v.findViewById(R.id.my_shroomies_attach_button);
        addCardTextView = v.findViewById(R.id.my_shroomies_add_text_view);
        titleEditText = v.findViewById(R.id.new_card_title);
        descriptionEditText = v.findViewById(R.id.new_card_description);
        cardColorRadioGroup = v.findViewById(R.id.newcard_radio_group);
        dueDateButton = v.findViewById(R.id.due_date);
        mentionAutoCompleteTextView = v.findViewById(R.id.tag_shroomie);
        closeImageButton=v.findViewById(R.id.x_button_new_card);
        splitExpensesButton =v.findViewById(R.id.split_expenses);
        addCardRelativeLayout = v.findViewById(R.id.my_shroomies_add_card_layout);
        dueDatePlaceholder = v.findViewById(R.id.date_text_view);
        dueDateRelativeLayout = v.findViewById(R.id.date_relative_layout);
        expensePlaceholder  = v.findViewById(R.id.expenses_text_view);
        expenseRelativeLayout = v.findViewById(R.id.expenses_relative_layout);
        selectedImageView = v.findViewById(R.id.attachment_image_view);
        imageRelativeLayout = v.findViewById(R.id.image_relative_layout);
        deleteImageButton = v.findViewById(R.id.delete_attached_image);
        deleteDueDateImageButton = v.findViewById(R.id.remove_date_image_button);
        deleteExpenseImageButton =  v.findViewById(R.id.remove_expense_image_button);
        loadingLottieAnimationView = v.findViewById(R.id.lottie_loading_animation);

        mentionAutoCompleteTextView.setMentionEnabled(true);
        mentionAutoCompleteTextView.setMentionColor(Color.BLUE);

        mentionAutoCompleteTextView.setHint("@mention");
        mentionAutoCompleteTextView.setTag("@");




        closeImageButton.setOnClickListener(view1 -> dismiss());

        final Bundle bundle = this.getArguments();
        if (bundle != null) {
            expensesCardSelected = bundle.getBoolean("Expenses");
            apartment=bundle.getParcelable("APARTMENT_DETAILS");
            if(apartment.apartmentMembers!=null){
                apartmentMembersHashMap.putAll(apartment.getApartmentMembers());
                getMemberUserNames(apartmentMembersHashMap);
            }
            Log.d("apartment add card" , apartment.getApartmentID());
            if (!expensesCardSelected) {
                attachFileButton.setVisibility(View.GONE);
                splitExpensesButton.setVisibility(View.GONE);
            }
        }
        deleteDueDateImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dueDatePlaceholder.setText("");
                dueDateRelativeLayout.setVisibility(View.GONE);
            }
        });
        deleteExpenseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedAmountsHashMap = null;
                expenseRelativeLayout.setVisibility(View.GONE);
            }
        });
        splitExpensesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!apartmentMembersArrayList.isEmpty()) {
                    SplitExpenses split = new SplitExpenses();
                    Bundle bundle1 = new Bundle();
                    bundle1.putParcelable("APARTMENT_DETAILS", apartment);
                    bundle1.putParcelableArrayList("MEMBERS", apartmentMembersArrayList);
                    split.setArguments(bundle1);
                    split.setTargetFragment(AddNewCard.this, DIALOG_RESULT);
                    split.show(getParentFragmentManager(), "split expenses");
                }
            }
        });

        attachFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // clear the image uri and set the visibilty  to gone
                chosenImageUri = null;
                imageRelativeLayout.setVisibility(View.GONE);
            }
        });

        addCardRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkMentions()) {
                    loadingLottieAnimationView.setVisibility(View.VISIBLE);
                    addCardTextView.setText("Uploading");
                    //disable click on layout to prevent uploading before the current task is done;
                    addCardRelativeLayout.setClickable(false);
                    String cardColor, mdescription, mtitle, mdueDate;
                    mdueDate = dueDateButton.getText().toString();

                    switch (cardColorRadioGroup.getCheckedRadioButtonId()) {
                        case R.id.newcard_red_radio_button:
                            cardColor = "2";
                            break;
                        case R.id.newcard_orange_radio_button:
                            cardColor = "1";
                            break;
                        default:
                            cardColor = "0";
                    }

                    mdescription = descriptionEditText.getText().toString();
                    mtitle = titleEditText.getText().toString();

                    //get the mentions as a map with keys and values of the user
                    JSONObject mMention = getMentionsJSONObject(mentionAutoCompleteTextView.getMentions());

                    if (mtitle.isEmpty() || mdescription.isEmpty()) {
                        Snackbar.make(getView(), "Please insert Title and Description", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();
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
            }

        });

        dueDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                        SimpleDateFormat patternFormat=new SimpleDateFormat("EEE, MMM d,yyyy");
                        Calendar c= Calendar.getInstance();
                        c.set(year,month,dayOfMonth);
                        String sDate=patternFormat.format(c.getTime());
                        //set the visibility of the due date to visible
                        dueDateRelativeLayout.setVisibility(View.VISIBLE);
                        dueDatePlaceholder.setText(sDate);

                    }
                }, year, month, day);
                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                dialog.show();
            }
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
                Snackbar.make(getView(), "Please ensure the mentioned members start with @", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();
                return false;
            }
        }
        //check if the mentioned users are in the member list
        for (Iterator<String>  iterator=
            mentionAutoCompleteTextView.getMentions().iterator(); iterator.hasNext();){
            String name = iterator.next().toLowerCase();
            if(!nameAndIdHashMap.containsKey(name)){
                Snackbar.make(getView(), "Member "+ name +" doesn't exist", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();
                return false;
            }
        }
        return true;
    }



    private void getMemberUserNames(final HashMap<String,String> membersHashMap) {
        ArrayList<String> members = new ArrayList<>();
        //add the the admin to the members
        if(apartment.getApartmentMembers()!=null){
            members.addAll(apartment.getApartmentMembers().values());
        }
        members.add(apartment.getAdminID());
        apartmentMembersArrayList = new ArrayList<>();
        JSONObject jsonObject=new JSONObject();
        JSONObject data = new JSONObject();
        JSONArray jsonArray = new JSONArray(members);

        try {
            jsonObject.put("membersID",jsonArray);
            data.put("data" , jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        FirebaseUser firebaseUser = mAuth.getCurrentUser();
//        firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//            @Override
//            public void onComplete(@NonNull Task<GetTokenResult> task) {
//                if(task.isSuccessful()){
//                    String token = task.getResult().getToken();

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.FUNCTION_GET_MEMBER_DETAIL, data, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                            JSONArray users = null;
                            try {
                                users = response.getJSONArray("result");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if(users!=null) {
                                if (users.length()!=0) {
                                    ArrayAdapter mentionAdapter = new MentionArrayAdapter(getActivity() , R.drawable.ic_user_profile_svgrepo_com);

                                    for (int i = 0 ; i<jsonArray.length();i++) {
                                        User user = null;
                                        try {
                                            user = mapper.readValue(users.get(i).toString(), User.class);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (JsonMappingException e) {
                                            e.printStackTrace();
                                        } catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }
                                        if(user!=null) {
                                            apartmentMembersArrayList.add(user);
                                            nameAndIdHashMap.put(user.getName() , user.getUserID());
                                            mentionAdapter.add(new Mention(user.getName(),null,user.getImage()));
                                            mentionAutoCompleteTextView.setMentionAdapter(mentionAdapter);
                                        }

                                    }
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //todo handle error
                        }
                    });
                    requestQueue.add(jsonObjectRequest);
//                }
//            }
//        });

    }
//    private void saveToAddLog(String apartmentID,HashMap<String,Object> newRecord){
//
//            DatabaseReference ref=rootRef.child("logs").child(apartmentID).push();
//            String logID=ref.getKey();
//            newRecord.put("logID",logID);
//            ref.updateChildren(newRecord);
//
//    }

    private void saveTaskCardToFirebase(String mtitle, String mdescription, String mdueDate, String importance, JSONObject mMention, String apartmentID) {
//        FirebaseUser firebaseUser = mAuth.getCurrentUser();
//        firebaseUser.getIdToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//            @Override
//            public void onComplete(@NonNull @NotNull Task<GetTokenResult> task) {
//                String token = task.getResult().getToken();
//                Log.d("task token" ,"recived" );
//                if (!mMention.isEmpty()) {
//                    notify = true;
//                }
                JSONObject jsonObject = new JSONObject();
                JSONObject data = new JSONObject();
                JSONObject cardDetails = new JSONObject();
                try {
                    cardDetails.put("description", mdescription);
                    cardDetails.put("title", mtitle);
                    cardDetails.put("dueDate", mdueDate);
                    cardDetails.put("importance", importance);
                    cardDetails.put("date", ServerValue.TIMESTAMP);
                    cardDetails.put("cardID", "");
                    cardDetails.put("done", "false");
                    cardDetails.put("mention", mMention);
                    cardDetails.put("actor",mAuth.getCurrentUser().getUid());

                    jsonObject.put("cardDetails",cardDetails);
                    jsonObject.put("apartmentID" , apartmentID);
                    data.put("data",jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_ADD_TASK_CARDS, data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dismiss();
                    }
                }, error -> Log.d("add card no works" , error.getMessage()) )
//                {
//                    @Override
//                    public Map<String, String> getHeaders() throws AuthFailureError {
//                        Map<String, String> params = new HashMap<>();
//                        params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
//                        params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
//                        return params;
//                    }
//                }
                ;
                requestQueue.add(jsonObjectRequest);
            }
//        });


//        ref.updateChildren(newCard).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//                    saveToAddLog(apartmentID,newRecord);
//                    dismiss();
//
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                //TODO handle error
//            }
//        });
//        if (notify) {
//            StringTokenizer names=new StringTokenizer(mMention,"@",true);
//            while (names.hasMoreTokens()) {
//                    if(names.nextToken().startsWith("@")){
//                        try {
//                            String tokenName= names.nextToken();
//                            if(nameAndIdHashMap.get(tokenName.trim())!=null){
//                                sendNotification(nameAndIdHashMap.get(tokenName.trim()), " Help! your shroomies need you");
//                            }
//
//                        }catch (NoSuchElementException e){
//
//                        }
//                    }
//            }
//
//        }
//        notify = false;
//    }


    public void addExpenseCard(String title, String description, String dueDate, String attachUrl , String fileType, String importance, JSONObject mMention, String apartmentID, HashMap<String, Integer> shareAmounts) {
       //get the authorization token
        //if authorization token is recived then proceed
//        FirebaseUser firebaseUser = mAuth.getCurrentUser();
//        firebaseUser.getIdToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//            @Override
//            public void onComplete(@NonNull @NotNull Task<GetTokenResult> task) {
//                String token = task.getResult().getToken();
                JSONObject jsonObject = new JSONObject();
                JSONObject data  = new JSONObject();
                JSONObject cardDetails = new JSONObject();

                try {
                    cardDetails.put("description", description);
                    cardDetails.put("title", title);
                    cardDetails.put("dueDate", dueDate);
                    cardDetails.put("importance", importance);
                    cardDetails.put("attachedFile", attachUrl);
                    cardDetails.put("fileType" ,fileType );
                    cardDetails.put("date", ServerValue.TIMESTAMP);
                    cardDetails.put("cardID", "");
                    cardDetails.put("done", "false");
                    cardDetails.put("mention", mMention);
                    cardDetails.put("actor" , mAuth.getCurrentUser().getUid());
                    if(shareAmounts!=null){
                        cardDetails.put("membersShares",shareAmounts);
                    }
                    jsonObject.put("cardDetails",cardDetails);
                    jsonObject.put("apartmentID" , apartmentID);
                    data.put("data",jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //TODO add progress dialog
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_ADD_EXPENSES_CARDS, data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        dismiss();
                    }
                }, error -> Log.d("apartment no works" , error.networkResponse.data.toString()) )
//                {
//                    @Override
//                    public Map<String, String> getHeaders() throws AuthFailureError {
//                        Map<String, String> params = new HashMap<String, String>();
//                        params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
//                        params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
//                        return params;
//                    }
//                }
                ;
                requestQueue.add(jsonObjectRequest);
//            }
//        });
    }


    public void uploadImgToFirebaseStorage( String title,  String description,  String dueDate,  String importance, Uri imgUri , JSONObject mMention, HashMap<String, Integer> sharedAmounts) {

        if (imgUri == null) {
            addExpenseCard(title, description, dueDate, "" , "", importance, mMention,apartment.getApartmentID(),sharedAmounts);
        } else {

            StorageReference filePath = mStorage.getReference().child(apartment.getApartmentID()).child("Card post image").child(imgUri.getLastPathSegment() + fileExtension);
            filePath.putFile(imgUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    task.getResult().getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            addExpenseCard(title, description, dueDate, uri.toString()  , fileType, importance, mMention,apartment.getApartmentID(),sharedAmounts);
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    //Todo handle error
                }
            });
        }

    }


    private void showImagePickDialog() {
        String[] options = {"Gallery","PDF","Camera"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose file from");
        builder.setIcon(R.drawable.ic_shroomies_yelllow_black_borders);
        builder.setCancelable(true);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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

        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                File file = new File(Environment.getExternalStorageDirectory(), captureFileName);
                //check the size of the file
                if((file.length()/1048576)<= MAX_FILES_SIZE_IN_BYTES){
                    chosenImageUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", file);
                    fileExtension = ".jpg";
                    fileType  = "image";
                    addSelectedImageToImageView(chosenImageUri, false);
                    return;
                }else{
                    Snackbar.make(getView(), "This file is too large", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();
                    return;
                }
            }
            if (data.getData() != null) {
                if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                    if (fileSizeIsAllowed(data)) {
                        chosenImageUri = data.getData(); // load image from gallery
                        fileExtension = ".jpg";
                        fileType =  "image";
                        addSelectedImageToImageView(chosenImageUri, false);
                    } else {
                        Snackbar.make(getView(), "This file is too large", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();
                    }
                }
                if (requestCode == PDF_PICK_CODE) {

                    if (fileSizeIsAllowed(data)) {
                        chosenImageUri = data.getData();
                        fileExtension = ".pdf";
                        fileType = "pdf";
                        addSelectedImageToImageView(chosenImageUri, true);
                    } else {
                        Snackbar.make(getView(), "This file is too large", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();

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
            if(fileSizeInMB<= MAX_FILES_SIZE_IN_BYTES){
                return true;
            }
            return false;
    }


    private void sendNotification(final String receiverID,final String message) {
        rootRef.child("Token").orderByKey().equalTo(receiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Token token = (Token) ds.getValue(Token.class);
                        Data data = new Data( mAuth.getCurrentUser().getUid(),  message, "Card Added", receiverID, (R.drawable.ic_notification_icon)  ,"true" );
                        Sender sender = new Sender(data, token.getToken());
                        try {
                            JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                            JsonObjectRequest jsonObjectRequest=new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    Log.d("JSON_RESPONSE","onResponse:"+response.toString());
                                }

                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("JSON_RESPONSE","onResponse:"+error.toString());

                                }
                            })
                            {
                                @Override
                                public Map<String,String> getHeaders() throws AuthFailureError {
                                    Map<String,String> headers= new HashMap<>();
                                    headers.put("Content-type","application/json");
                                    headers.put("Authorization","Key=AAAAyn_kPyQ:APA91bGLxMB-HGP-qd_EPD3wz_apYs4ZJIB2vyAvH5JbaTVlyLExgYn7ye-076FJxjfrhQ-1HJBmptN3RWHY4FoBdY08YRgplZSAN0Mnj6sLbS6imKa7w0rqPsLtc-aXMaPOhlxnXqPs");
                                    return headers;
                                }

                            };

                            requestQueue.add(jsonObjectRequest);
                            requestQueue.start();

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                //TODO handle error

            }
        });

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
                expensePlaceholder.setText(numberFormat.format(totalAmount) + " RM");
                expenseRelativeLayout.setVisibility(View.VISIBLE);
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
                    Snackbar.make(getView(), "Couldn't load this file", BaseTransientBottomBar.LENGTH_LONG).setAnchorView(R.id.my_shroomies_add_text_view).show();

                }
            } else {
                Glide.with(getActivity())
                        .load(uri)
                        .transform(new CenterCrop(),new RoundedCorners(5) )
                        .error(R.drawable.ic_no_file_added)
                        .into(selectedImageView);
            }
        }


}





