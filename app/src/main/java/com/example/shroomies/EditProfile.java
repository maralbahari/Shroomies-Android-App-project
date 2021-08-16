package com.example.shroomies;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollUpdateListener;

public class EditProfile extends Fragment {

    View v;
    private ImageView profileImage;
    private ImageButton editImage;
    private Button  deleteAccount;
    private Button changeUsername, changeEmail,changePassword, changeBio;
    private IOverScrollDecor expensesDecor;
    private IOverScrollDecor tasksDecor;
    private IOverScrollUpdateListener onOverPullListener;


    private FirebaseDatabase dataRef;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private Uri imageUri;
    private StorageTask uploadTask;
    private StorageReference storageRef;
    private CustomLoadingProgressBar customLoadingProgressBar;

    private RequestQueue requestQueue;
    private User user;
    ImageView sadShroomie, stars;
    Button confirmDelete, keepAccount;
    private static final int IMAGE_REQUEST= 1;
    public static final int DIALOG_FRAGMENT_REQUEST_CODE = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        mAuth=FirebaseAuth.getInstance();
        mAuth.useEmulator("10.0.2.2",9099);
        dataRef=FirebaseDatabase.getInstance();
        dataRef.useEmulator("10.0.2.2",9000);
        rootRef=dataRef.getReference();
        requestQueue= Volley.newRequestQueue(getActivity());
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.btm_view.setBackgroundColor(getResources().getColor(R.color.lowerGradientColorForLoginBackground, getActivity().getTheme()));
        MainActivity.btm_view.setElevation(0);
        customLoadingProgressBar= new CustomLoadingProgressBar(getActivity(), "Updating" , R.raw.loading_animation);
        customLoadingProgressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        profileImage = v.findViewById(R.id.edit_profile_image_view);
        editImage = v.findViewById(R.id.change_profile_picture);
        changeUsername = v.findViewById(R.id.edit_username);
        changeEmail = v.findViewById(R.id.edit_email);
        changePassword = v.findViewById(R.id.change_password);
        changeBio = v.findViewById(R.id.edit_bio);
        deleteAccount = v.findViewById(R.id.delete_account_button);
        getUserToken();

        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
        changeBio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeBioDialog changeBio = new ChangeBioDialog();
                Bundle bundle=new Bundle();
                bundle.putParcelable("USER",user);
                changeBio.setArguments(bundle);
                changeBio.setTargetFragment(EditProfile.this,DIALOG_FRAGMENT_REQUEST_CODE );
                changeBio.show(getParentFragmentManager() ,null);
            }
        });

        changeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeUsernameDialog changeUsername = new ChangeUsernameDialog();
                Bundle bundle=new Bundle();
                bundle.putParcelable("USER",user);
                changeUsername.setArguments(bundle);
                changeUsername.setTargetFragment(EditProfile.this,DIALOG_FRAGMENT_REQUEST_CODE );
                changeUsername.show(getParentFragmentManager() ,null);
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeEmailDialog changeEmail = new ChangeEmailDialog();
                Bundle bundle=new Bundle();
                bundle.putParcelable("USER",user);
                changeEmail.setArguments(bundle);
                changeEmail.setTargetFragment(EditProfile.this,DIALOG_FRAGMENT_REQUEST_CODE );
                changeEmail.show(getParentFragmentManager() ,null);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser firebaseUser=mAuth.getCurrentUser();
                if (firebaseUser!=null) {
                    resetPass();
                } else {
//                    todo error handling
                }
            }
        });
        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getLayoutInflater();
                View alert = inflater.inflate(R.layout.are_you_sure,null);
                builder.setView(alert);
                final AlertDialog alertDialog = builder.create();
                alertDialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
                alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialogfragment_add_member);
                alertDialog.show();
                sadShroomie = ((AlertDialog) alertDialog).findViewById(R.id.sad_shroomie);
                stars = ((AlertDialog) alertDialog).findViewById(R.id.stars);
                confirmDelete = ((AlertDialog) alertDialog).findViewById(R.id.button_continue);
                keepAccount = ((AlertDialog) alertDialog).findViewById(R.id.button_no);
                confirmDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseUser firebaseUser=mAuth.getCurrentUser();
                        if (firebaseUser!=null) {
                            deleteAccount(firebaseUser);
                        }
                    }
                });
                keepAccount.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.cancel();
                    }
                });
            }
        });
    }
    @Override
    public void onDetach() {
        super.onDetach();
        MainActivity.btm_view.setBackgroundColor(getResources().getColor(R.color.LogoYellow, getActivity().getTheme()));
        MainActivity.btm_view.setElevation(0);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        getActivity();
        if (requestCode == IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data !=null) {
            imageUri = data.getData();

            if (uploadTask !=null && uploadTask.isInProgress()){
                customLoadingProgressBar.show();
                Toast.makeText(getContext(), "Upload is in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        } else {
//todo error handling
        }
    }

    private void deleteAccount(FirebaseUser firebaseUser) {
        firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()) {
//                    todo alert and finish activity or something
                    Toast.makeText(getContext(),"You are no longer shroomie",Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                displayErrorAlert("Request Rejected",e.getMessage());
            }
        });
    }
    private void openImage() {
        final CharSequence[] pictureOptions = { "Choose From Gallery", "Remove Profile Picture"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Change Profile Image");
        builder.setItems(pictureOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (pictureOptions[item].equals("Choose From Gallery")){
                    Intent pickPicture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPicture, IMAGE_REQUEST);
                } else if (pictureOptions[item].equals("Remove Profile Picture")){
                    // todo also delete picture from storage
                    GlideApp.with(getActivity().getApplicationContext())
                            .load(R.drawable.ic_user_profile_svgrepo_com)
                            .into(profileImage);
                }
            }
        });
        builder.show();
    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        customLoadingProgressBar.show();
        if (imageUri !=null){
            storageRef = FirebaseStorage.getInstance().getReference();
            final StorageReference fileRef = storageRef.child("profile pictures").child(user.getUserID()).child(getFileExtension(imageUri));
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task <Uri>>() {
                @Override
                public Task <Uri> then(@NonNull Task <UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }


            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUrl = downloadUri.toString();
                        HashMap <String, Object> imageDetails = new HashMap<>();
                        imageDetails.put("image", mUrl);
                        rootRef.child("users").child(user.getUserID()).updateChildren(imageDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Uploaded successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else{
                        Toast.makeText(getActivity(), "Upload failed",Toast.LENGTH_SHORT).show();
                    }
                    customLoadingProgressBar.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(),e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            Toast.makeText(getActivity(), "No image selected", Toast.LENGTH_SHORT).show();
        }

    }

    private void getUserToken(){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser!=null) {
            firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    getUserDetails(token);
                }else{
                    //todo handle error
                    String title = "Authentication error";
                    String message = "We encountered a problem while authenticating your account";
                    displayErrorAlert(title, message);
                }
            });
        } else {
            String title = "Authentication error";
            String message = "We encountered a problem while authenticating your account";
            displayErrorAlert(title, message);
        }
    }
    private void getUserDetails(String token){
        JSONObject jsonObject = new JSONObject();
        JSONObject data  = new JSONObject();
        FirebaseUser firebaseUser=mAuth.getCurrentUser();
        try {
            jsonObject.put(Config.id, firebaseUser.getUid());
            data.put(Config.data, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_GET_USER_DETAILS, data, response -> {
            final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            try {
                JSONObject result = response.getJSONObject(Config.result);
                boolean success = result.getBoolean(Config.success);
                if (success) {
                    result = (JSONObject) result.get(Config.user);
                    user= mapper.readValue(result.toString(), User.class);
                    changeBio.setText("Bio | " + user.getBio());
                    changeUsername.setText("Username | " + user.getName());
                    changeEmail.setText("Email | "+ user.getEmail());
                    if (user.getImage()!="") {
                        GlideApp.with(getActivity().getApplicationContext())
                                .load(user.getImage())
                                .fitCenter()
                                .placeholder(R.drawable.ic_user_profile_svgrepo_com)
                                .into(profileImage);
                        profileImage.setPadding(3,3,3,3);
                    }
                } else {
                    String title = "Unexpected error";
                    String message = "We have encountered an unexpected error ,try to check your internet connection and log in again.";
                    displayErrorAlert(title, message);
                }

            }catch (JSONException | JsonProcessingException e) {
                e.printStackTrace();
                String title = "Unexpected error";
                String message = "We have encountered an unexpected error ,try to check your internet connection and log in again.";
                displayErrorAlert(title, message);
            }
        }, error -> {
            String message = null; // error message, show it in toast or dialog, whatever you want
            if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                message = "Cannot connect to Internet";
            } else if (error instanceof ServerError) {
                message = "Server error. Please try again later";
            }  else if (error instanceof ParseError) {
                message = "Parsing error! Please try again later";
            }
            displayErrorAlert("Error" ,message);
        })
        {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
//                params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
                return params;
            }
        };
        requestQueue.add(jsonObjectRequest);

    }
    private void resetPass(){
        mAuth.sendPasswordResetEmail(user.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(getContext(),"An email has been sent to reset your password",Toast.LENGTH_LONG).show();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                displayErrorAlert("Request Rejected",e.getMessage());
            }
        });
    }
    void displayErrorAlert(String title, String message){
        tasksDecor.setOverScrollUpdateListener(onOverPullListener);
        expensesDecor.setOverScrollUpdateListener(onOverPullListener);
        customLoadingProgressBar.dismiss();
        new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_alert)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("return", (dialog, which) -> {
                    getActivity().finish();
                    dialog.dismiss();
                })
                .setPositiveButton("refresh", (dialog, which) -> getUserToken())
                .create()
                .show();
    }
}

