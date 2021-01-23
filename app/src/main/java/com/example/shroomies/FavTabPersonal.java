package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavTabPersonal extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;


    PersonalPostRecyclerAdapter personalPostRecyclerAdapter;
    List<PersonalPostModel> personalPostModelList;
    RecyclerView favPersonalRecyclerView;
    TextView personalPostText;

    public FavTabPersonal() {
        // Required empty public constructor
    }

    public static FavTabPersonal newInstance(String param1, String param2) {
        FavTabPersonal fragment = new FavTabPersonal();
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
         final View v = inflater.inflate(R.layout.fragment_fav_tab_personal, container, false);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String CurrUserId = firebaseUser.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Favorite").child(CurrUserId)
                .child("PersonalPost");
        ref.addListenerForSingleValueEvent(new ValueEventListener(){


            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.getValue() != null){
                    favPersonalRecyclerView = v.findViewById(R.id.favPersonalList);
                    favPersonalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    personalPostModelList = new ArrayList<>();
                    DatabaseReference DatabaseRef = FirebaseDatabase.getInstance().getReference("Favorite");
                    DatabaseReference ref = DatabaseRef.child(CurrUserId).child("PersonalPost");
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot ds : snapshot.getChildren()){
                                PersonalPostModel personalPost = ds.getValue(PersonalPostModel.class);
                                personalPostModelList.add(personalPost);
                            }
                            personalPostRecyclerAdapter = new PersonalPostRecyclerAdapter(personalPostModelList, getContext());
                            favPersonalRecyclerView.setAdapter(personalPostRecyclerAdapter);

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "An unexpected error occured", Toast.LENGTH_LONG);

                        }
                    });
                }

                else{
                    personalPostText = v.findViewById(R.id.TV_noFavPersonalPost);
                    personalPostText.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

    return v;
    }
}