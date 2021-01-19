package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddShroomieMember extends DialogFragment {
    View v;
    SearchView memberSearchView;
    RecyclerView addShroomieRecycler;
    UserRecyclerAdapter userRecyclerAdapter;
    Button backToShroomie;
    DatabaseReference rootRef;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth mAuth;
    ArrayList<User> suggestedUser;
    ArrayList<User> selectedUser;

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
        backToShroomie = v.findViewById(R.id.back_to_shroomie);

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

    private void retreiveUser(String s) {
        suggestedUser = new ArrayList<>();
        userRecyclerAdapter= new UserRecyclerAdapter(suggestedUser,getContext(),"");
        addShroomieRecycler.setAdapter(userRecyclerAdapter);
        rootRef.child("Users").orderByChild("name").equalTo(s).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }



}
