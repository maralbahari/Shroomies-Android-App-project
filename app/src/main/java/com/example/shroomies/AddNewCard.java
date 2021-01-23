package com.example.shroomies;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.shroomies.notifications.Data;
import com.example.shroomies.notifications.Sender;
import com.example.shroomies.notifications.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class AddNewCard extends DialogFragment   {

    FragmentTransaction ft;
    FragmentManager fm;
    View v ;
    Button attachButton, addCard;
    TextView newCardTv, dueDate, attachedFile;
    EditText title , description;
    MultiAutoCompleteTextView mention;
    CheckBox done;
    RadioGroup newcardShroomieRadioGroup;
    Calendar calendar;
//    String[] spinnerOption = {"Importance","Date","Title"};
    String fTitle, fDescription, fDueDate, fAttachUrl, fImportance, fDone;
    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;
    private static final int IMAGE_PICK_CAMERA_CODE=300;
    private static final int IMAGE_PICK_GALLERY_CODE=400;
    private static final int PICK_IMAGE_MULTIPLE=1;
    String[] cameraPermissions;
    String[] storagePermissions;
    Uri chosenImage=null;
    StorageReference filePathName;
    private String imageUrl;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference rootRef;
    FirebaseAuth mAuth;
    Boolean expensesCardSelected;
    String saveCurrentDate, saveCurrentTime;
    String cdate, doneCheckbox ;
    String currentUserAppartmentId = "";
    ArrayList<String> memberList;
    ArrayList<String> memberUserNameList;
    HashMap<String, String> userNameAndID;
    ArrayAdapter<String> userNamesList;
    RequestQueue requestQueue ;




    private void getUserRoomId(){
        memberList = new ArrayList<>();
        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentUserAppartmentId=snapshot.getValue().toString();
                    rootRef.child("apartments").child(currentUserAppartmentId).child("apartmentMembers").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                for (DataSnapshot sp : snapshot.getChildren()){
                                    memberList.add(sp.getValue().toString());
                                }
                                getMemberUserNames(memberList);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        v =inflater.inflate(R.layout.fragment_add_new_card, container, false);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        getUserRoomId();
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
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
        }
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        attachButton = v.findViewById(R.id.my_shroomies_attach_button);
        addCard = v.findViewById(R.id.my_shroomies_add_button);
        title = v.findViewById(R.id.new_card_title);
        description = v.findViewById(R.id.new_card_description);
        newcardShroomieRadioGroup = v.findViewById(R.id.newcard_radio_group);
        dueDate = v.findViewById(R.id.due_date);
        mention = v.findViewById(R.id.tag_shroomie);

//        mention.setThreshold(6);
        done = v.findViewById(R.id.task_done);
        cameraPermissions=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        attachedFile = v.findViewById(R.id.attached_files);



       Bundle bundle=this.getArguments();
       if(bundle!=null){
           expensesCardSelected=bundle.getBoolean("Expenses");
           if(!expensesCardSelected){
               attachButton.setVisibility(v.GONE);
           }
       }

        fTitle = title.getText().toString();
        fDescription = description.getText().toString();
        attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseFile();
            }
        });
        addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String importantButton,mdescription,mtitle,mdueDate,mMention;
                mdueDate = dueDate.getText().toString();
                switch (newcardShroomieRadioGroup.getCheckedRadioButtonId()) {
                    case  R.id.newcard_green_radio_button:
                        importantButton = "0";
                        break;
                    case  R.id.newcard_red_radio_button:
                        importantButton = "2";
                        break;
                    case  R.id.newcard_orange_radio_button:
                        importantButton = "1";
                        break;
                    default:
                        importantButton = "0";
                }

                mdescription = description.getText().toString();
                mtitle = title.getText().toString();
                mMention = mention.getText().toString();

                if (mtitle.isEmpty()){

                    Toast.makeText(getContext(),"Please insert Title",Toast.LENGTH_SHORT).show();
                }else{
                    if(expensesCardSelected==false){
                        saveTaskCardToFirebase(mtitle,mdescription,mdueDate,importantButton,mMention);
                    }else if (expensesCardSelected==true){
                        uploadImgToFirebaseStorage(mtitle, mdescription, mdueDate, importantButton, chosenImage,mMention);
                    }
                    }

                }
        });


        dueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month = month+1;
                        String sDate = dayOfMonth +"/"+ month+"/"+year;

                        dueDate.setText(sDate);
                    }
                },year,month,day);
                dialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
                dialog.show();


            }
        });


        requestQueue= Volley.newRequestQueue(getActivity());
    }

    private void getMemberUserNames(final ArrayList<String> memberList) {
        memberUserNameList = new ArrayList<>();
        userNameAndID = new HashMap<>();
        userNamesList = new ArrayAdapter<String>(getContext(), R.layout.support_simple_spinner_dropdown_item, memberUserNameList);
        for (String id : memberList) {
            rootRef.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        memberUserNameList.add(user.getName());
                        userNameAndID.put(user.getName() , user.getID());
                    }
                    mention.setAdapter(userNamesList);
                    mention.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }
    }


    private void saveTaskCardToFirebase(String mtitle, String mdescription, String mdueDate, String importance, final String mMention) {

        DatabaseReference ref = rootRef.child("apartments").child(currentUserAppartmentId).child("tasksCards").push();
        HashMap<String ,Object> newCard = new HashMap<>();

        Calendar calendarDate=Calendar.getInstance();
        String uniqueID = ref.getKey();
        SimpleDateFormat mcurrentDate=new SimpleDateFormat("dd-MMMM-yyyy HH:MM:ss aa");
        saveCurrentDate=mcurrentDate.format(calendarDate.getTime());

        newCard.put("description" , mdescription);
        newCard.put("title" ,mtitle);
        newCard.put("dueDate", mdueDate);
        newCard.put("importance", importance);
        newCard.put("date",saveCurrentDate);
        newCard.put("cardId",uniqueID);
        newCard.put("done", "false");
        newCard.put("mention",mMention);


        ref.updateChildren(newCard).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    dismiss();
                    String [] names = mMention.split(",");
                    for(String name
                            :names) {
                        sendNotification(userNameAndID.get(name) , " Help! your shroomies need you" );
                    }
                }
            }
        });


    }


    private void chooseFile() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(gallery, PICK_IMAGE_MULTIPLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==getActivity().RESULT_OK) {
            if (requestCode == 1) {
                chosenImage = data.getData(); // load image from gallery
                //Save to firebase

                attachedFile.setVisibility(View.VISIBLE);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

//    public void saveTaskCardToFireBase(String taskTitle, String taskDescription, String taskDueDate, String taskImportance){
//
//        DatabaseReference ref = rootRef.child("tasksCards").child(mAuth.getCurrentUser().getUid()).push();
//        HashMap<String ,Object> newTaskCard = new HashMap<>();
//
//        Calendar mcalendarDate=Calendar.getInstance();
//        SimpleDateFormat mcurrentDate=new SimpleDateFormat("dd-MMMM-yyyy HH:MM:ss aa");
//        saveCurrentDate=mcurrentDate.format(calendarDate.getTime());
//        Calendar calendarTime=Calendar.getInstance();
//
//        newTaskCard.put("taskTitle", taskTitle);
//        newTaskCard.put("taskDescription", taskDescription);
//        newTaskCard.put("taskDueDate",taskDueDate);
//        newTaskCard.put("taskImportance",taskImportance);
//
//
//    }

    public void saveToFireBase(String title, String description, String dueDate, String attachUrl, String importance, final String mMention ){


        DatabaseReference ref = rootRef.child("apartments").child(currentUserAppartmentId).child("expensesCards").push();
        HashMap<String ,Object> newCard = new HashMap<>();

        Calendar calendarDate=Calendar.getInstance();
        String uniqueID = ref.getKey();
        SimpleDateFormat mcurrentDate=new SimpleDateFormat("dd-MMMM-yyyy HH:MM:ss aa");
        saveCurrentDate=mcurrentDate.format(calendarDate.getTime());

        newCard.put("description" , description);
        newCard.put("title" ,title);
        newCard.put("dueDate", dueDate);
        newCard.put("importance", importance);
        newCard.put("attachedFile", attachUrl);
        newCard.put("date",saveCurrentDate);
        newCard.put("cardId",uniqueID);
        newCard.put("done", "false");
        newCard.put("mention",mMention);

        ref.updateChildren(newCard).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    dismiss();
                    String [] names = mMention.split(",");
                    for(String name
                            :names) {
                        sendNotification(userNameAndID.get(name) , " Help! your shroomies need you" );
                    }
                }
            }
        });

    }



    public void uploadImgToFirebaseStorage(final String title,final String description,final String dueDate, final String importance,Uri imgUri,final String mMention){
        if (imgUri==null){
            saveToFireBase( title,  description,  dueDate,  "", importance,mMention);
        }else {
            StorageReference storageReference= FirebaseStorage.getInstance().getReference();
            StorageReference filePath = storageReference.child("Card post image").child(imgUri.getLastPathSegment()+".jpg");
            filePath.putFile(imgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                }
            }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                   task.getResult().getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            saveToFireBase(title,  description,  dueDate,  uri.toString(), importance,mMention);
                        }
                    });

                }
            });
        }

    }


    private void showImagePickDialog(){
        String[] options={"Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        builder.setTitle("Choose Image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which==0){
                    if(!checkStoragePermisson()){
                        requestStoragePermission();
                    }else{
                        pickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void pickFromGallery(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"temp describe");

        chosenImage=getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

    }

    private boolean checkStoragePermisson(){
        boolean result= ContextCompat.checkSelfPermission(getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return result;
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(getActivity(),storagePermissions,STORAGE_REQUEST_CODE);
    }
    private boolean checkCameraPermisson(){
        boolean resultCam= ContextCompat.checkSelfPermission(getContext(),Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        boolean resultStorage= ContextCompat.checkSelfPermission(getContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);

        return resultCam && resultStorage;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(getActivity(),cameraPermissions,CAMERA_REQUEST_CODE);
    }
    //this method is called when user oress allow or deny form permission request dialog
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }else{
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                        Toast.makeText(getContext(),"Camera and storage both permissions are neccessary....",Toast.LENGTH_SHORT).show();
                    }
                }else{

                }
            }break;
            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(storageAccepted){
                        pickFromGallery();
                    }else{
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
                        Toast.makeText(getContext()," storage permission is neccessary....",Toast.LENGTH_SHORT).show();
                        pickFromGallery();

                    }
                }
            }
            break;
        }
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

            }
        });

    }





}





