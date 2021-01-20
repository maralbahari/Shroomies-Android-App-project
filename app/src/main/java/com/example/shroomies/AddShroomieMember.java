package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddShroomieMember extends DialogFragment {
    View v;
    SearchView memberSearchView;
    RecyclerView addShroomieRecycler;
    UserAdapter userRecyclerAdapter;
    Button backToShroomie;
    DatabaseReference rootRef;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth mAuth;
    ArrayList<User> suggestedUser;
    ArrayList<User> selectedUser;
    ArrayList<String> inboxUser;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v =inflater.inflate(R.layout.add_shroomies, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        memberSearchView = v.findViewById(R.id.search_member);
        addShroomieRecycler = v.findViewById(R.id.add_member_recyclerview);
//        backToShroomie = v.findViewById(R.id.back_to_shroomie);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        addShroomieRecycler.setHasFixedSize(true);
        addShroomieRecycler.setLayoutManager(linearLayoutManager);
        rootRef = FirebaseDatabase.getInstance().getReference();
        getMessageInboxListIntoAdapter();
//        backToShroomie.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dismiss();
//            }
//        });
        memberSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                retreiveUser(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialogfragment_add_member);
        }
    }

    private void retreiveUser(String s) {
        suggestedUser = new ArrayList<>();
        userRecyclerAdapter= new UserAdapter(suggestedUser,getContext());
        addShroomieRecycler.setAdapter(userRecyclerAdapter);
        rootRef.child("Users").orderByChild("name").equalTo(s).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    for (DataSnapshot ds : snapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        suggestedUser.add(user);
                    }
                    //add the members already selected
                    userRecyclerAdapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }


    private void addInboxUsersToRecycler(final List<String> inboxListUsers) {
        suggestedUser = new ArrayList<>();
        suggestedUser.clear();
        userRecyclerAdapter=new UserAdapter(suggestedUser,getContext());
        addShroomieRecycler.setAdapter(userRecyclerAdapter);
        for(String id
                :inboxListUsers){
            rootRef.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        User user = snapshot.getValue(User.class);
                        if(!suggestedUser.contains(user)){
                            suggestedUser.add(user);
                            userRecyclerAdapter.notifyDataSetChanged();
                        }

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void getMessageInboxListIntoAdapter() {
        inboxUser = new ArrayList<>();
        rootRef.child("PrivateChatList").child(mAuth.getInstance().getCurrentUser().getUid()).orderByChild("receiverID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot dataSnapshot
                            :snapshot.getChildren()){
                        HashMap<String,String> recievers= (HashMap) dataSnapshot.getValue();
                        inboxUser.add(recievers.get("receiverID"));
                    }
                    addInboxUsersToRecycler(inboxUser);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
