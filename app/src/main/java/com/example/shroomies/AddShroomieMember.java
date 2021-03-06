package com.example.shroomies;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class AddShroomieMember extends DialogFragment {
    View v;
    SearchView memberSearchView;
    RecyclerView addShroomieRecycler;
    UserAdapter userRecyclerAdapter;
    DatabaseReference rootRef;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth mAuth;
    ArrayList<User> suggestedUser;
    ArrayList<User> selectedUser;
    ArrayList<String> inboxUser;
    Collection<String> listMemberId=new ArrayList<>();
    ShroomiesApartment apartment;
    private HashMap<String,Boolean> requestAlreadySent=new HashMap<>();
    ImageButton closeButton;
    TextView infoTextView;
    TextView recommendedUsers;


    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
        }
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v =inflater.inflate(R.layout.add_shroomies, container, false);
        mAuth=FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        memberSearchView = v.findViewById(R.id.search_member);
        addShroomieRecycler = v.findViewById(R.id.add_member_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        addShroomieRecycler.setHasFixedSize(true);
        addShroomieRecycler.setLayoutManager(linearLayoutManager);
        closeButton = v.findViewById(R.id.close_button_add_shroomie);
        infoTextView = v.findViewById(R.id.information_text_view);
        recommendedUsers = v.findViewById(R.id.recommended_text_view);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        if (getArguments()!=null){
            Bundle bundle = getArguments();
            apartment=bundle.getParcelable("APARTMENT_DETAILS");
            listMemberId.addAll(apartment.getApartmentMembers().values());
            listMemberId.add(apartment.getOwnerID());

        }
        getMessageInboxListIntoAdapter();

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





    private void retreiveUser(String query) {

        suggestedUser = new ArrayList<>();
        userRecyclerAdapter= new UserAdapter(suggestedUser,getContext(),true,apartment,requestAlreadySent);
        addShroomieRecycler.setAdapter(userRecyclerAdapter);

        rootRef.child("Users").orderByChild("name").startAt(query)
                .endAt(query+"\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    infoTextView.setVisibility(View.GONE);
                    recommendedUsers.setVisibility(View.GONE);
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        Boolean duplicate = false;
                        for (User user1: suggestedUser){
                            if (user1.getID().equals(user.getID())){
                                duplicate = true;
                            }
                        }
                            if (!duplicate&&!user.getID().equals(mAuth.getInstance().getCurrentUser().getUid())){
                                if(listMemberId!=null){
                                    if(!listMemberId.contains(user.getID())){
                                        suggestedUser.add(user);
                                        checkRequestedUsers(user.getID());

                                    }

                                }


                            }


                    }

                }else{
                    Snackbar snack=Snackbar.make(getView(),"This shroomie doesn't exist", BaseTransientBottomBar.LENGTH_SHORT);
                    snack.show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    private void addInboxUsersToRecycler(final List<String> inboxListUsers) {
        suggestedUser = new ArrayList<>();
        userRecyclerAdapter=new UserAdapter(suggestedUser,getContext(),true,apartment,requestAlreadySent);
        addShroomieRecycler.setAdapter(userRecyclerAdapter);
        for(final String id
                :inboxListUsers){
            rootRef.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    suggestedUser.clear();
                    if(snapshot.exists()){
                        User user = snapshot.getValue(User.class);
                        if (!listMemberId.contains(user.getID())) {
                            suggestedUser.add(user);
                            checkRequestedUsers(id);
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
    private void checkRequestedUsers(final String id){

        rootRef.child("shroomieRequests").child(mAuth.getCurrentUser().getUid()).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    requestAlreadySent.put(id,true);
                }else{
                    requestAlreadySent.put(id,false);
                }
                userRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
