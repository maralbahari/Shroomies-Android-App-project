package com.example.shroomies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CreateChatGroupDialogFrag2 extends DialogFragment {
    private FirebaseAuth mAuth;
   private ImageView groupChatImage;
   private ImageButton addGroupImage;
   private EditText groupChatTitle;
   private RecyclerView selectedMembers;
    private DatabaseReference rootRef;
    private List<User> selectedUsers;
    private UserRecyclerAdapter userRecyclerAdapter;
    private Button createGroupButton,cancelButton;
   private String saveCurrentDate,saveCurrentTime;
   private StorageReference storageReference;
   StorageReference filePath ;
   private Uri imageUri;
   CustomLoadingProgressBar customLoadingProgressBar;
    View v;

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_2_background);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         v= inflater.inflate(R.layout.fragment_create_chat_group_dialog_frag2, container, false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        return v;
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = this.getArguments();
        selectedUsers = new ArrayList<>();
        if (bundle != null) {
            selectedUsers = bundle.getParcelableArrayList("ListOfSelectedUsers");
        }
        customLoadingProgressBar= new CustomLoadingProgressBar(getActivity(), "Creating group..." , R.raw.loading_animation);
        customLoadingProgressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        groupChatImage=v.findViewById(R.id.group_chat_image);
        groupChatTitle=v.findViewById(R.id.group_chat_name);
        selectedMembers=v.findViewById(R.id.list_of_selected_members);
        createGroupButton=v.findViewById(R.id.create_group_chat_button);
        addGroupImage = v.findViewById(R.id.icon_add_group_image);
        cancelButton = v.findViewById(R.id.cancelButton);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        selectedMembers.setHasFixedSize(true);
        selectedMembers.setLayoutManager(linearLayoutManager);
        userRecyclerAdapter=new UserRecyclerAdapter(selectedUsers,getContext(),"CREATE_GROUP_FRAG_2");
        selectedMembers.setAdapter(userRecyclerAdapter);

        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customLoadingProgressBar.show();
                String groupName=groupChatTitle.getText().toString();
                List<String> listOfMembersID = new ArrayList<>();
                for(User user: selectedUsers){
                    listOfMembersID.add(user.getUserID());
                }
                if(TextUtils.isEmpty(groupName) || listOfMembersID.isEmpty()){
                    Toast.makeText(getContext(),"Please Enter Group Name",Toast.LENGTH_LONG).show();
                }else {
                    storeImage(groupName, listOfMembersID);
                }
            }
        });

        // once icon pressed open the gallery
        // for the user to upload one image
        addGroupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity() , MessageInbox.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);


            }
        });
    }
    void storeImage(final String groupName , final List<String>membersId){
        // storage referance to save the image path in firebase storage

        // if the image view hasn't been changed from the default drawable
        //then upload the image

        if(imageUri!=null){
            String postUniqueName = getUniqueName();
            filePath = storageReference.child("group profile image").child(imageUri.getLastPathSegment()
                    +postUniqueName+".jpg");
            filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()) {
                         task.getResult().getMetadata().getReference().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                createGroupDatabase(groupName, membersId, task.getResult().toString());
                            }
                        });

                    }else{
                        customLoadingProgressBar.dismiss();
                        Toast.makeText(getActivity(),task.getException().toString(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }

    public void createGroupDatabase(final String groupName, final List<String> membersID , String imagePath){

        // add the current user id to the list
        membersID.add(mAuth.getInstance().getCurrentUser().getUid());
        Map<String,Object> groupDetails=new HashMap<>();
        // create a unique group id
        Calendar calendarDate=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate=currentDate.format(calendarDate.getTime());
        Calendar calendarTime=Calendar.getInstance();
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
        saveCurrentTime=currentTime.format(calendarTime.getTime());
        final String groupID=groupName+"-"+saveCurrentDate+"-"+saveCurrentTime;
        // ad dthe group name , members and id
        groupDetails.put("groupName",groupName);
        groupDetails.put("groupMembers",membersID);
        groupDetails.put("groupID",groupID);
        groupDetails.put("groupImage", imagePath);

        rootRef.child("GroupChats").child(groupID).setValue(groupDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                customLoadingProgressBar.dismiss();
                if(task.isSuccessful()){
                    for (String memberId
                            :membersID){
                        HashMap<String,Object> newGroup = new HashMap<>();
                        newGroup.put(groupID , groupName);
                        rootRef.child("GroupChatList").child(memberId).updateChildren(newGroup).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getActivity(), "successfully added users", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), MessageInbox.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });

                    }
                }else{
                    Toast.makeText(getActivity(), "something went wrong", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), MessageInbox.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }

            }

    });

    }

    private void openGallery() {
        //add permisision denied handlers
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(gallery, 1);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedImageUri;
        if (resultCode == getActivity().RESULT_OK && requestCode == 1) {


            //get selected photo
            // load the image to the image view
            selectedImageUri = data.getData();

            // store the image uri
            imageUri = selectedImageUri;

            Glide.with(getContext())
                    .load(imageUri)
                    .circleCrop()
                    .into(groupChatImage);


        } else {
            Toast.makeText(getActivity(), "an error occured", Toast.LENGTH_SHORT).show();

                }

            }




    private String getUniqueName() {
        //create a unique id for the post by combining the date with uuid
        //get the date first
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat  currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        String saveCurrentDate = currentDate.format(calendarDate.getTime());

        //get the time in hours and minutes
        Calendar calendarTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        String saveCurrentTime = currentTime.format(calendarTime.getTime());

        //add the two string together

        return  saveCurrentDate+saveCurrentTime;
    }




    }

