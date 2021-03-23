package com.example.shroomies;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditProfile extends Fragment {

    FragmentManager fm;
    FragmentTransaction ft;

    View v;

    private ImageView profileImage;
    private ImageButton editImage;
    private Button  deleteAccount;
    private Button changeUsername, changeEmail,changePassword, changeBio;

    private FirebaseUser userRef;
    private FirebaseDatabase dataRef;
    private FirebaseAuth authRef;
    private DatabaseReference rootRef;
    private Uri imageUri;
    private StorageTask uploadTask;
    private StorageReference storageRef;
    private CustomLoadingProgressBar customLoadingProgressBar;

    final String userUid =  authRef.getInstance().getCurrentUser().getUid();
    FirebaseFirestore DocRef;

    private Context context;
    ImageView sadShroomie, stars;
    Button confirmDelete, keepAccount;

    private static final int IMAGE_REQUEST= 1;
    public static final int DIALOG_FRAGMENT_REQUEST_CODE = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);
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

        rootRef = dataRef.getInstance().getReference();

        rootRef.child("Users").child(userUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                changeBio.setText("Bio | " + user.getBio());
                changeUsername.setText("Username | " + user.getName());
                changeEmail.setText("Email | "+ user.getEmail());

                String url = snapshot.child("image").getValue(String.class);
                GlideApp.with(getActivity().getApplicationContext())
                        .load(url)
                        .fitCenter()
                        .placeholder(R.drawable.ic_user_profile_svgrepo_com)
                        .into(profileImage);
                profileImage.setPadding(3,3,3,3);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
                changeBio.setTargetFragment(EditProfile.this,DIALOG_FRAGMENT_REQUEST_CODE );
                changeBio.show(getParentFragmentManager() ,null);
            }
        });

        changeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeUsernameDialog changeUsername = new ChangeUsernameDialog();
                changeUsername.setTargetFragment(EditProfile.this,DIALOG_FRAGMENT_REQUEST_CODE );
                changeUsername.show(getParentFragmentManager() ,null);
            }
        });

        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeEmailDialog changeEmail = new ChangeEmailDialog();
                changeEmail.setTargetFragment(EditProfile.this,DIALOG_FRAGMENT_REQUEST_CODE );
                changeEmail.show(getParentFragmentManager() ,null);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragment(new ChangePassword());
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
                        deleteUserData();
                        deleteApartmentPosts();
                        deletePersonalPosts();
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

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        MainActivity.btm_view.setBackgroundColor(getResources().getColor(R.color.LogoYellow, getActivity().getTheme()));
        MainActivity.btm_view.setElevation(0);
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
                    // also delete picture from storage
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
            final StorageReference fileRef = storageRef.child("profile pictures").child(userUid).child(getFileExtension(imageUri));
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
                        dataRef.getInstance().getReference().child("Users").child(userUid).updateChildren(imageDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
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

        }
    }

    private void getFragment (Fragment fragment) {
        fm = getActivity().getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();
    }

    private void deleteUserData(){
        rootRef.child("archive").child(userUid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                rootRef.child("Favourite").child(userUid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        deleteUser();
                    }
                });
            }
        });
    }

    private void deleteUser(){
        rootRef.child("Users").child(userUid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                authRef.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getActivity(), "Account deleted", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    private void deleteApartmentPosts(){

        //delete Images from Storage
        List<Query> apartmentList;
        apartmentList = new ArrayList<>();

        DocRef  = FirebaseFirestore.getInstance();
        Query query = DocRef.collection("postApartment").whereEqualTo("userID", userUid);
        apartmentList.add(query);
        DocRef.collection("postApartment").document(apartmentList.toString()).
                delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
    }

    private void deletePersonalPosts(){
        List<Query> personalList;
        personalList = new ArrayList<>();

        DocRef  = FirebaseFirestore.getInstance();
        Query query = DocRef.collection("postPersonal").whereEqualTo("userID", userUid);
        personalList.add(query);
        DocRef.collection("postPersonal").document(personalList.toString()).
                delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });
    }
}

