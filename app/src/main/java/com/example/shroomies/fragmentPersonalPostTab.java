package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class fragmentPersonalPostTab extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    DatabaseReference personalDatabaseRef;
    PersonalPostRecyclerAdapter personalPostRecyclerAdapter;
    List<PersonalPostModel> personalPostModelList;
    RecyclerView personalRecyclerView;



    public fragmentPersonalPostTab() {
        // Required empty public constructor
    }



    public static fragmentPersonalPostTab newInstance(String param1, String param2) {
        fragmentPersonalPostTab fragment = new fragmentPersonalPostTab();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);


        }
    }
    @Override
    public void onSaveInstanceState( Bundle outState) {
        super.onSaveInstanceState(outState);
        // getting query
        Bundle bun = getArguments();
        String personalQuery = (String) bun.get("myQuery");
        Toast.makeText(getContext(),"made",Toast.LENGTH_LONG).show();



        final List<String> userIds  = new ArrayList<>();
        personalPostModelList = new ArrayList<>();


        DatabaseReference myRootRef = FirebaseDatabase.getInstance().getReference();
        myRootRef.child("Users").orderByChild("name").startAt(personalQuery).endAt(personalQuery+"\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot dataSnapshot :snapshot.getChildren()){
                        User user = dataSnapshot.getValue(User.class);
                        userIds.add(user.getID());
                    }
                    for (String  id: userIds){

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("postPersonal");
                        Query query1 = reference.orderByChild("userID").equalTo(id);
                        query1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                setSearchAdapterData(snapshot);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getActivity(), error.getMessage() , Toast.LENGTH_SHORT).show();
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
    void setSearchAdapterData(DataSnapshot snapshot){

        if (snapshot.exists()) {
            for (DataSnapshot dataSnapshot :
                    snapshot.getChildren()) {

                PersonalPostModel personalPost = dataSnapshot.getValue(PersonalPostModel.class);
                personalPostModelList.add(personalPost);
            }

            personalPostRecyclerAdapter.notifyDataSetChanged();
            personalPostRecyclerAdapter = new PersonalPostRecyclerAdapter(personalPostModelList, getContext());
            personalRecyclerView.setAdapter(personalPostRecyclerAdapter);


        }


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_personal_post_tab, container, false);
        personalRecyclerView = v.findViewById(R.id.personalRecView);
        personalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        personalPostModelList = new ArrayList<>();
        personalDatabaseRef = FirebaseDatabase.getInstance().getReference("postPersonal");
        personalDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    PersonalPostModel personalPost = ds.getValue(PersonalPostModel.class);
                    personalPostModelList.add(personalPost);
                }
                personalPostRecyclerAdapter = new PersonalPostRecyclerAdapter(personalPostModelList, getContext());
                personalRecyclerView.setAdapter(personalPostRecyclerAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "An unexpected error occured", Toast.LENGTH_LONG);

            }
        });

        return v;
    }
}