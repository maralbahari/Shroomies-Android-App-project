package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CreateChatGroupFragment extends DialogFragment {

    private   View v;
    private SearchView searchView;
    private RecyclerView userListSuggestionRecyclerView;
    private ImageButton nextButton;
    private DatabaseReference rootRef;
    private UserRecyclerAdapter userRecyclerAdapter;
    private String groupID;
   private boolean fromGroupInfo;

    static ArrayList<User>  selectedMembers;

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
         getDialog().getWindow().setBackgroundDrawableResource(R.drawable.post_card_rectangle_round);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_create_chat_group, container, false);
        rootRef= FirebaseDatabase.getInstance().getReference();


        return v;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchView=v.findViewById(R.id.create_group_search_bar);
        userListSuggestionRecyclerView =v.findViewById(R.id.suggestion_list_create_group);
        userListSuggestionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        nextButton=v.findViewById(R.id.confirm_button_create_group);

        Bundle extras=getArguments();
        if(extras!=null) {
            fromGroupInfo = extras.getBoolean("FromGroupInfo");
            groupID = extras.getString("GROUPID");

        }
        selectedMembers = new ArrayList<>();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getUsersListFromDatabase(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fromGroupInfo){

                    Intent intent = new Intent(getContext(),GroupInfoActivity.class);
                    intent.putExtra("GROUPID" , groupID);
                    intent.putParcelableArrayListExtra("ListOfSelectedUsers",selectedMembers);
                    startActivity(intent);
                }
                    CreateChatGroupDialogFrag2 dialogFrag2=new CreateChatGroupDialogFrag2();
                    Bundle bundle= new Bundle();
                    bundle.putParcelableArrayList("ListOfSelectedUsers",selectedMembers);
                    dialogFrag2.setArguments(bundle);
                    dialogFrag2.show(getChildFragmentManager(),"create group dialog 2");
            }
        });

    }
    public void getUsersListFromDatabase(String query){

        //+"\uf8ff"
        rootRef.child("Users").orderByChild("name").equalTo(query).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List <User> suggestedUser = new ArrayList<>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        suggestedUser.add(user);
                    }

                    userRecyclerAdapter=new UserRecyclerAdapter(suggestedUser,getContext(),"SEARCH_PAGE");
                    userListSuggestionRecyclerView.setAdapter(userRecyclerAdapter);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public static void addSelectedMembers(User user){

       if(!selectedMembers.contains(user)){
           selectedMembers.add(user);
        }else{
           // add member already selected exception
       }

    }

}