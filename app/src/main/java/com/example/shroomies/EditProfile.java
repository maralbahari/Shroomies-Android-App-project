package com.example.shroomies;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;


import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class EditProfile extends DialogFragment implements ChangeBioDialog.bio, ChangeEmailDialog.email, ChangeUsernameDialog.username, ChangeNameDialogFragment.name {

    private View v;
    private ImageView profileImage;
    private TextView nameTxtView, emailTxtView, bioTxt;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference storageRef;
    private StorageTask uploadTask;
    private CustomLoadingProgressBar customLoadingProgressBar;
    private User user;
    private MaterialButton changeUserNameButton;
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
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
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
        nameTxtView = v.findViewById(R.id.edit_username);
        emailTxtView = v.findViewById(R.id.edit_email);
        changeUserNameButton = v.findViewById(R.id.change_username_button);
        Button changePassword = v.findViewById(R.id.change_password);
        bioTxt = v.findViewById(R.id.edit_bio);
        LinearLayout changeBio = v.findViewById(R.id.bio_linear);
        LinearLayout changeEmail = v.findViewById(R.id.email_linear);
        LinearLayout changeName = v.findViewById(R.id.name_linear);
        Button deleteAccount = v.findViewById(R.id.delete_account_button);
        Button cancle = v.findViewById(R.id.edit_profile_cancel);
        Button done = v.findViewById(R.id.edit_profile_done);
        FirebaseUser firebaseUser=mAuth.getCurrentUser();
            if (firebaseUser!=null) {
                getUserDetails(firebaseUser.getUid());
                editImage.setOnClickListener(v -> openImage());
                changeBio.setOnClickListener(v -> {
                    ChangeBioDialog changeBioDialog = new ChangeBioDialog();
                    Bundle bundle=new Bundle();
                    bundle.putParcelable("USER",user);
                    changeBioDialog.setArguments(bundle);
                    changeBioDialog.setTargetFragment(EditProfile.this,DIALOG_FRAGMENT_REQUEST_CODE );
                    changeBioDialog.show(getParentFragmentManager() ,null);
                });
                changeName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ChangeNameDialogFragment changeNameDialogFragment=new ChangeNameDialogFragment();
                        Bundle bundle=new Bundle();
                        bundle.putParcelable("USER",user);
                        changeNameDialogFragment.setArguments(bundle);
                        changeNameDialogFragment.setTargetFragment(EditProfile.this,DIALOG_FRAGMENT_REQUEST_CODE );
                        changeNameDialogFragment.show(getParentFragmentManager() ,null);
                    }
                });

                changeUserNameButton.setOnClickListener(v -> {
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
            Uri imageUri = data.getData();
            if (uploadTask !=null && uploadTask.isInProgress()){
                customLoadingProgressBar.show();
                customLoadingProgressBar.setLoadingText("Uploading...");
            } else {
                uploadImage(imageUri);
            }
        }
    }
    private void openImage() {
        final CharSequence[] pictureOptions = {"Choose From Gallery", "Remove Profile Picture"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Change Profile Image");
        builder.setItems(pictureOptions, (dialog, item) -> {
            if (pictureOptions[item].equals("Choose From Gallery")) {
                Intent pickPicture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPicture, IMAGE_REQUEST);
            } else if (pictureOptions[item].equals("Remove Profile Picture")) {
                removeProfileImage();
            }
        });
        builder.show();
    }
    private void removeProfileImage(){
        rootRef.child(Config.users).child(user.getUserID()).child("iamge").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task) {
                if (task.isSuccessful()) {
                    GlideApp.with(getContext())
                            .load(R.drawable.ic_user_profile_svgrepo_com)
                            .transform(new CircleCrop())
                            .into(profileImage);
                    StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(user.getImage());
                    if (storageReference != null) {
                        storageReference.delete().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(getContext(), "Image deleted successfully.", Toast.LENGTH_SHORT).show();

                            }
                        }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());

                    }
                    customLoadingProgressBar.dismiss();
                }
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());
    }
    private void uploadImage(Uri imageUri){
        Bitmap bitmap=null;
        if (Build.VERSION.SDK_INT >= 29) {
            ImageDecoder.Source source = ImageDecoder.createSource(getContext().getContentResolver(), imageUri);
            try {
                bitmap = ImageDecoder.decodeBitmap(source);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        customLoadingProgressBar.show();
        if (imageUri !=null){
            final StorageReference fileRef = storageRef.child("profile pictures").child(user.getUserID()).child(imageUri.getLastPathSegment());
            uploadTask = fileRef.putBytes(byteArray);
            uploadTask.continueWithTask((Continuation<UploadTask.TaskSnapshot, Task<Uri>>) task -> {
                if(!task.isSuccessful()){
                    throw Objects.requireNonNull(task.getException());
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
                            GlideApp.with(getContext())
                                    .load(mUrl)
                                    .transform(new CircleCrop())
                                    .placeholder(R.drawable.ic_user_profile_svgrepo_com)
                                    .into(profileImage);
                            customLoadingProgressBar.dismiss();
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

    private void getUserDetails(String  userUid){
        rootRef.child(Config.users).child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    user= snapshot.getValue(User.class);
                    if (user!=null) {
                        if (!user.getBio().equals("")) {
                            bioTxt.setText(user.getBio());
                        }
                        nameTxtView.setText(user.getName());
                        changeUserNameButton.setText("@"+user.getUsername());
                        emailTxtView.setText(user.getEmail());
                        if (user.getImage()!=null) {
                            GlideApp.with(getActivity().getApplicationContext())
                                    .load(user.getImage())
                                    .fitCenter()
                                    .transform(new CircleCrop())
                                    .placeholder(R.drawable.ic_user_profile_svgrepo_com)
                                    .into(profileImage);
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

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
        this.bioTxt.setText(bioTxt);
    }

    @Override
    public void sendEmailBack(String emailTxt) {
        emailTxtView.setText(emailTxt);
    }

    @Override
    public void sendUserNameBack(String nameTxt) {
        changeUserNameButton.setText(nameTxt);
    }

    @Override
    public void sendbackName(String changedName) {
        nameTxtView.setText(changedName);
    }
}

