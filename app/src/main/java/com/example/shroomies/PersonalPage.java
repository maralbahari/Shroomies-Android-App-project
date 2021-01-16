package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PersonalPage extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private RecyclerView myRecyclerView;
    private FirebaseUser loggedInUser;
    private FirebaseRecyclerAdapter myFirebaseAdapter;
    DatabaseReference favRef, favList;
    private String curUserId;
    Boolean favChecker = false;


    public PersonalPage() {
        // Required empty public constructor
    }


    public static PersonalPage newInstance(String param1, String param2) {
        PersonalPage fragment = new PersonalPage();
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

//        DatabaseReference userHasRoomRef = FirebaseDatabase.getInstance().getReference().
//                child("postApartment");
//        loggedInUser = FirebaseAuth.getInstance().getCurrentUser();
//        curUserId = loggedInUser.getUid();






        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_personal_page, container,
                false);
        myRecyclerView = (RecyclerView) v.findViewById(R.id.personal_recycler_view);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseRecyclerOptions<Model_personal> options =
                new FirebaseRecyclerOptions.Builder<Model_personal>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("postPersonal"),
                                Model_personal.class).build();


        myFirebaseAdapter = new FireBase_recycler_adapter(options);
        myRecyclerView.setAdapter(myFirebaseAdapter);





        return v;
    }

    @Override
    public void onSaveInstanceState( Bundle outState) {
        super.onSaveInstanceState(outState);
        // getting query
        Bundle bun = getArguments();
        String personalQuery = (String) bun.get("myQuery");

        // creating search list
        FirebaseRecyclerOptions<Model_personal> options =
                new FirebaseRecyclerOptions.Builder<Model_personal>()
                        .setQuery(FirebaseDatabase.getInstance().getReference().child("postPersonal").
                                orderByChild("date").equalTo(personalQuery),
                                Model_personal.class).build();
        myFirebaseAdapter = new FireBase_recycler_adapter(options);
        myFirebaseAdapter.startListening();
        myRecyclerView.setAdapter(myFirebaseAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        myFirebaseAdapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        myFirebaseAdapter.stopListening();
    }
}


