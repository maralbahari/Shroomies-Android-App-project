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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class fragmentPersonalPostTab extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    PersonalPostRecyclerAdapter personalPostRecyclerAdapter;
    List<PersonalPostModel> personalPostModelList;
    RecyclerView personalRecyclerView;
    DatabaseReference personalDatabaseRef;

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