package com.example.shroomies;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
//import com.squareup.picasso.Picasso;
//import com.theartofdev.edmodo.cropper.CropImage;
//import com.theartofdev.edmodo.cropper.CropImageView;
//
//import java.util.HashMap;
//
//import de.hdodenhof.circleimageview.CircleImageView;


public class EditProfile extends Fragment {

    View v;
    //    private CircleImageView profileImage;
    private ImageButton editImage;
    private EditText username;
    private EditText email;
    private EditText bio;
    private EditText pw;
    private Button save;

    private FirebaseUser mUser;
    private FirebaseDatabase mDataref;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootref;
    private Uri imageUri;
    private StorageTask uploadTask;
    private StorageReference storageRef;

    final String userUid =  mAuth.getInstance().getCurrentUser().getUid();

    private static final int IMAGE_REQUEST= 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        MainActivity.btm_view.setBackgroundColor(getResources().getColor(R.color.lowerGradientColorForLoginBackground, getActivity().getTheme()));

//        profileImage = v.findViewById(R.id.edit_profile_image);
        editImage = v.findViewById(R.id.edit_profile_picture);
        username = v.findViewById(R.id.edit_username);
        email = v.findViewById(R.id.edit_email);
        pw = v.findViewById(R.id.edit_password);
        bio = v.findViewById(R.id.edit_bio);
        save = v.findViewById(R.id.btn_save);


        mRootref = mDataref.getInstance().getReference();
        //DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        final String userUid =  mAuth.getInstance().getCurrentUser().getUid();
        //mRootref = mDataref.getReference().child("Users").child(userUid);
        //mRootref.addValueEventListener(new ValueEventListener() {
        mRootref.child("Users").child(userUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getName());
                bio.setText(user.getBio());
                email.setText(user.getEmail());
//                    Glide.with(getActivity()).load(user.getImageurl()).into(profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//            profileImage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    openImage();
//                }
//            });

        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });
        return v;
    }


    private void updateProfile() {
        String txtName = username.getText().toString();
        String txtEmail = email.getText().toString();
        String txtBio = bio.getText().toString();

        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("name", txtName);
        updateDetails.put("email", txtEmail);
        updateDetails.put("bio", txtBio);
        mDataref.getInstance().getReference().child("Users").child(userUid).updateChildren(updateDetails).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Updated successfully", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
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

}