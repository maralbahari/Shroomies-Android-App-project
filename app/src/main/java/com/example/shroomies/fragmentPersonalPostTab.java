package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.factor.bouncy.BouncyRecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class fragmentPersonalPostTab extends Fragment {
    DatabaseReference personalDatabaseRef;
    PersonalPostRecyclerAdapter personalPostRecyclerAdapter;
    List<PersonalPostModel> personalPostModelList;
    BouncyRecyclerView personalRecyclerView;
    View v;



    public fragmentPersonalPostTab() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        personalRecyclerView = v.findViewById(R.id.personalRecView);
        personalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Bundle bun = getArguments();
        if (bun != null) {
            String personalQuery = (String) bun.get("myQuery");
            Toast.makeText(getContext(),personalQuery+" ", Toast.LENGTH_LONG).show();
            getSearch(personalQuery);
        }
        else{
            personalSearch();
        }





    }
    public void personalSearch(){

        personalPostModelList = new ArrayList<>();
        personalPostRecyclerAdapter = new PersonalPostRecyclerAdapter(personalPostModelList, getContext(), false);
        personalRecyclerView.setAdapter(personalPostRecyclerAdapter);
        personalDatabaseRef = FirebaseDatabase.getInstance().getReference("postPersonal");
        personalDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    PersonalPostModel personalPost = ds.getValue(PersonalPostModel.class);
                    personalPostModelList.add(personalPost);
                }
                personalPostRecyclerAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "An unexpected error occurred", Toast.LENGTH_LONG);

            }
        });}
    private void getSearch(String personalQuery) {
        final List<String> userIds  = new ArrayList<>();
        personalPostModelList = new ArrayList<>();
        personalPostRecyclerAdapter = new PersonalPostRecyclerAdapter(personalPostModelList, getContext(), false);
        personalRecyclerView.setAdapter(personalPostRecyclerAdapter);
        DatabaseReference myRootRef = FirebaseDatabase.getInstance().getReference();
        myRootRef.child("Users").orderByChild("name").startAt(personalQuery).endAt(personalQuery+"\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        userIds.add(user.getID());

                    }
                    for (String id : userIds) {

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("postPersonal");
                        Query query1 = reference.orderByChild("userID").equalTo(id);
                        query1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot dataSnapshot :
                                            snapshot.getChildren()) {

                                        PersonalPostModel personalPost = dataSnapshot.getValue(PersonalPostModel.class);
                                        personalPostModelList.add(personalPost);
                                    }
                                    personalPostRecyclerAdapter.notifyDataSetChanged();


                            }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         v = inflater.inflate(R.layout.fragment_personal_post_tab, container, false);


        return v;
    }
}