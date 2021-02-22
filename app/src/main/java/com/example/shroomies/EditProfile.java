package com.example.shroomies;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;


import java.util.HashMap;




public class EditProfile extends Fragment {

    FragmentManager fm;
    FragmentTransaction ft;

    View v;
    private ImageView profileImage;
    private ImageButton editImage;

    private EditText bio;

    private Button pw, save, username, email;
    private FirebaseUser mUser;
    private FirebaseDatabase mDataref;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootref;
    private Uri imageUri;
    private StorageTask uploadTask;
    private StorageReference storageRef;
    private CustomLoadingProgressBar customLoadingProgressBar;

    final String userUid =  mAuth.getInstance().getCurrentUser().getUid();
    AlertDialog.Builder builder;

    private static final int IMAGE_REQUEST= 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        MainActivity.btm_view.setBackgroundColor(getResources().getColor(R.color.lowerGradientColorForLoginBackground, getActivity().getTheme()));
        MainActivity.btm_view.setElevation(0);
        customLoadingProgressBar= new CustomLoadingProgressBar(getActivity(), "Updating..." , R.raw.loading_animation);
        customLoadingProgressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        profileImage = v.findViewById(R.id.edit_profile_image);
        editImage = v.findViewById(R.id.edit_profile_picture);
        username = v.findViewById(R.id.edit_username);
        email = v.findViewById(R.id.edit_email);
        pw = v.findViewById(R.id.edit_password);
        bio = v.findViewById(R.id.edit_bio);
        save = v.findViewById(R.id.btn_save);

        builder = new AlertDialog.Builder(getActivity());


        mRootref = mDataref.getInstance().getReference();

        mRootref.child("Users").child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getName());
                bio.setText(user.getBio());
                email.setText(user.getEmail());

                String url = snapshot.child("image").getValue(String.class);
                GlideApp.with(getActivity().getApplicationContext())
                        .load(url)
                        .circleCrop()
                        .placeholder(R.drawable.ic_user_profile_svgrepo_com)
                        .into(profileImage);
                profileImage.setPadding(3,3,3,3);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();

            }
        });

        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               openImage();

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customLoadingProgressBar.show();
                updateProfile();
            }
        });

        pw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragment(new ChangePassword());
            }
        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragment(new ChangeUsername());
            }
        });

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragment(new ChangeEmail());
            }
        });


        return v;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }



    private void updateProfile() {

        final String txtEmail = email.getText().toString();
        final String txtBio = bio.getText().toString();

//        if ((old_email.toString()).equals((txtEmail))){
//
//        } else {
//            sendEmailVerification();
//        }

        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("email", txtEmail);
        updateDetails.put("bio", txtBio);


        mDataref.getInstance().getReference().child("Users").child(userUid).updateChildren(updateDetails).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                customLoadingProgressBar.dismiss();
                if (task.isSuccessful()) {
                  Toast.makeText(getActivity(), "Updated successfully", Toast.LENGTH_SHORT).show();
                    MainActivity.btm_view.setBackgroundColor(getResources().getColor(R.color.lowerGradientColorForLoginBackground, getActivity().getTheme()));
                    MainActivity.btm_view.setElevation(0);
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                customLoadingProgressBar.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                MainActivity.btm_view.setBackgroundColor(getResources().getColor(R.color.lowerGradientColorForLoginBackground, getActivity().getTheme()));
                MainActivity.btm_view.setElevation(0);
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });


    }

    @Override
    public void onDetach() {
        super.onDetach();
        MainActivity.btm_view.setBackgroundColor(getResources().getColor(R.color.LogoYellow, getActivity().getTheme()));
        MainActivity.btm_view.setElevation(0);
    }


    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }



    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("Uploading");
        pd.show();
        if (imageUri !=null){
            storageRef = FirebaseStorage.getInstance().getReference();
            final StorageReference fileRef = storageRef.child("profile pictures").child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
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
                        mDataref.getInstance().getReference().child("Users").child(userUid).updateChildren(imageDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Uploaded successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        pd.dismiss();
                    }
                    else{
                        Toast.makeText(getActivity(), "Upload failed",Toast.LENGTH_SHORT).show();
                    }
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

        if (requestCode == IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data !=null) {
           // CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = data.getData();

            if (uploadTask !=null && uploadTask.isInProgress()){
                Toast.makeText(getContext(), "Upload is in progress", Toast.LENGTH_SHORT).show();
            } else {
                uploadImage();
            }
        } else {
            Toast.makeText(getActivity(), "Something went wrong!", Toast.LENGTH_SHORT).show();
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

}

