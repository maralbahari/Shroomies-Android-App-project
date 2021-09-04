package com.example.shroomies;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;



import java.util.HashMap;

public class EditProfile extends DialogFragment implements ChangeBioDialog.bio, ChangeEmailDialog.email, ChangeUsernameDialog.name{

    private View v;
    private ImageView profileImage;
    private TextView username, email, bio;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference storageRef;
    private Uri imageUri;
    private StorageTask uploadTask;
    private CustomLoadingProgressBar customLoadingProgressBar;
    private User user;
    private static final int IMAGE_REQUEST= 1;
    public static final int DIALOG_FRAGMENT_REQUEST_CODE = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        mAuth=FirebaseAuth.getInstance();
        rootRef=FirebaseDatabase.getInstance().getReference();
        user=new User();
        storageRef = FirebaseStorage.getInstance().getReference();
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
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);

        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        customLoadingProgressBar= new CustomLoadingProgressBar(getActivity(), "Updating" , R.raw.loading_animation);
        customLoadingProgressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        profileImage = v.findViewById(R.id.edit_profile_image_view);
        ImageButton editImage = v.findViewById(R.id.change_profile_picture);
        username = v.findViewById(R.id.edit_username);
        email = v.findViewById(R.id.edit_email);
        Button changePassword = v.findViewById(R.id.change_password);
        bio = v.findViewById(R.id.edit_bio);
        LinearLayout changeBio = v.findViewById(R.id.bio_linear);
        LinearLayout changeEmail = v.findViewById(R.id.email_linear);
        LinearLayout changeUsername = v.findViewById(R.id.name_linear);
        Button deleteAccount = v.findViewById(R.id.delete_account_button);
        Button cancle = v.findViewById(R.id.edit_profile_cancel);
        Button done = v.findViewById(R.id.edit_profile_done);
        FirebaseUser firebaseUser=mAuth.getCurrentUser();
        if (firebaseUser!=null) {
            String userUid=firebaseUser.getUid();
            getUserDetails(userUid);
            editImage.setOnClickListener(v -> openImage());
            changeBio.setOnClickListener(v -> {
                ChangeBioDialog changeBioDialog = new ChangeBioDialog();
                Bundle bundle=new Bundle();
                bundle.putParcelable("USER",user);
                changeBioDialog.setArguments(bundle);
                changeBioDialog.setTargetFragment(EditProfile.this,DIALOG_FRAGMENT_REQUEST_CODE );
                changeBioDialog.show(getParentFragmentManager() ,null);
            });

            changeUsername.setOnClickListener(v -> {
                ChangeUsernameDialog changeUsernameDialog = new ChangeUsernameDialog();
                Bundle bundle=new Bundle();
                bundle.putParcelable("USER",user);
                changeUsernameDialog.setArguments(bundle);
                changeUsernameDialog.setTargetFragment(EditProfile.this,DIALOG_FRAGMENT_REQUEST_CODE );
                changeUsernameDialog.show(getParentFragmentManager() ,null);
            });
            changeEmail.setOnClickListener(v -> {
                ChangeEmailDialog changeEmailDialog = new ChangeEmailDialog();
                Bundle bundle=new Bundle();
                bundle.putParcelable("USER",user);
                changeEmailDialog.setArguments(bundle);
                changeEmailDialog.setTargetFragment(EditProfile.this,DIALOG_FRAGMENT_REQUEST_CODE );
                changeEmailDialog.show(getParentFragmentManager() ,null);
            });

            changePassword.setOnClickListener(v ->
                    resetPass()
            );
            deleteAccount.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Are you sure want to delete this account?");
                builder.setPositiveButton("Yes", (dialogInterface, i) -> {
                    DeleteUser deleteUser=new DeleteUser();
                    Bundle bundle=new Bundle();
                    bundle.putParcelable("USER",user);
                    deleteUser.setArguments(bundle);
                    deleteUser.setTargetFragment(EditProfile.this,DIALOG_FRAGMENT_REQUEST_CODE);
                    deleteUser.show(getParentFragmentManager(),null);
                });
                builder.setNegativeButton("cancel", (dialogInterface, i) -> dialogInterface.dismiss());
                builder.show();
            });
        } else {
            dismiss();
        }
        done.setOnClickListener(v -> dismiss());
        cancle.setOnClickListener(v -> dismiss());
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
        }
    }
    private void openImage() {
        final CharSequence[] pictureOptions = { "Choose From Gallery", "Remove Profile Picture"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Change Profile Image");
        builder.setItems(pictureOptions, (dialog, item) -> {
            if (pictureOptions[item].equals("Choose From Gallery")){
                Intent pickPicture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPicture, IMAGE_REQUEST);
            } else if (pictureOptions[item].equals("Remove Profile Picture")){
                storageRef.child("profile pictures").child(user.getUserID()).child(user.getImage()).delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        customLoadingProgressBar.dismiss();
                        GlideApp.with(getActivity().getApplicationContext())
                                .load(R.drawable.ic_user_profile_svgrepo_com)
                                .into(profileImage);
                    }
                }).addOnFailureListener(e -> Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show());
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
            final StorageReference fileRef = storageRef.child("profile pictures").child(user.getUserID()).child(getFileExtension(imageUri));
            uploadTask = fileRef.putFile(imageUri);
            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return fileRef.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()){
                    Uri downloadUri = task.getResult();
                    String mUrl = downloadUri.toString();
                    HashMap <String, Object> imageDetails = new HashMap<>();
                    imageDetails.put("image", mUrl);
                    rootRef.child(Config.users).child(user.getUserID()).updateChildren(imageDetails).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(getActivity(), "Uploaded successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else{
                    Toast.makeText(getActivity(), "Upload failed",Toast.LENGTH_SHORT).show();
                }
                customLoadingProgressBar.dismiss();
            }).addOnFailureListener(e -> Toast.makeText(getActivity(),e.getMessage(), Toast.LENGTH_SHORT).show());
        }
        else {
//            TODO ERROR HANDLING
            Toast.makeText(getActivity(), "No image selected", Toast.LENGTH_SHORT).show();
        }

    }

    private void getUserDetails(String userUid){
        rootRef.child(Config.users).child(userUid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                    user=task.getResult().getValue(User.class);
                    if (user!=null) {
                        if (!user.getBio().equals("")) {
                            bio.setText(user.getBio());
                        }
                        username.setText(user.getName());
                        email.setText(user.getEmail());
                        if (user.getImage()!=null) {
                            GlideApp.with(getActivity().getApplicationContext())
                                    .load(user.getImage())
                                    .fitCenter()
                                    .placeholder(R.drawable.ic_user_profile_svgrepo_com)
                                    .into(profileImage);
                            profileImage.setPadding(3,3,3,3);
                        }
                    }

                }
            }).addOnFailureListener(e -> Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show());
    }
    private void resetPass(){
        mAuth.sendPasswordResetEmail(user.getEmail()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(),"An email has been sent to reset your password",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
//                TODO ERROR HANDLING
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void sendBioBack(String bioTxt) {
        bio.setText(bioTxt);
    }

    @Override
    public void sendEmailBack(String emailTxt) {
        email.setText(emailTxt);
    }

    @Override
    public void sendNameBack(String nameTxt) {
        username.setText(nameTxt);
    }
}

