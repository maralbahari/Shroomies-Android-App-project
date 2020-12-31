package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CreateChatGroupDialogFrag2 extends DialogFragment {

   private ImageView groupChatImage;
   private EditText groupChatTitle;
   private RecyclerView selectedMembers;
    private DatabaseReference rootRef;
    private List<User> selectedUsers;
    private UserRecyclerAdapter userRecyclerAdapter;
    private Button createGroupButton;
   private String saveCurrentDate,saveCurrentTime;
    View v;

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         v= inflater.inflate(R.layout.fragment_create_chat_group_dialog_frag2, container, false);
        rootRef= FirebaseDatabase.getInstance().getReference();

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = this.getArguments();
        selectedUsers = new ArrayList<>();
        if (bundle != null) {
            selectedUsers = bundle.getParcelableArrayList("ListOfSelectedUsers");
        }
        groupChatImage=v.findViewById(R.id.group_chat_image);
        groupChatTitle=v.findViewById(R.id.group_chat_name);
        selectedMembers=v.findViewById(R.id.list_of_selected_members);
        createGroupButton=v.findViewById(R.id.create_group_chat_button);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        selectedMembers.setHasFixedSize(true);
        selectedMembers.setLayoutManager(linearLayoutManager);
        userRecyclerAdapter=new UserRecyclerAdapter(selectedUsers,getContext(),false);
        selectedMembers.setAdapter(userRecyclerAdapter);

        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String groupName=groupChatTitle.getText().toString();
                List<String> listOfMembersID = new ArrayList<>();
                for(User user: selectedUsers){
                    listOfMembersID.add(user.getID());

                }
                if(TextUtils.isEmpty(groupName) || listOfMembersID.isEmpty()){
                    Toast.makeText(getContext(),"Please Enter Group Name",Toast.LENGTH_LONG).show();
                }else {
                    createGroupDatabase(groupName, listOfMembersID);
                }
            }
        });


    }
    public void createGroupDatabase(String groupName, final List<String> membersID){
        Map<String,Object> groupDetails=new HashMap<>();
        groupDetails.put("groupName",groupName);
        groupDetails.put("groupMembers",membersID);
        Calendar calendarDate=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate=currentDate.format(calendarDate.getTime());
        Calendar calendarTime=Calendar.getInstance();
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
        saveCurrentTime=currentTime.format(calendarTime.getTime());
        String GroupID=groupName+"-"+saveCurrentDate+"-"+saveCurrentTime;
        rootRef.child("GroupChats").child(GroupID).setValue(groupDetails).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(getActivity(),MessageInbox.class);
                    startActivity(intent);
                }else{

                }

            }
        });
    }
    }

