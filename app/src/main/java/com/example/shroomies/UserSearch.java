package com.example.shroomies;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.factor.bouncy.BouncyRecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UserSearch extends Fragment {
    DatabaseReference rootRef;
    private CustomLoadingProgressBar customLoadingProgressBar;
    private RefreshProgressBar refreshProgressBar;
    private BouncyRecyclerView recyclerView;
    private SearchUserRecyclerViewAdapter searchUserRecyclerViewAdapter;
    private List<User> userList;
    private String searchTerm;
    private View v;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootRef =  FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v =   inflater.inflate(R.layout.fragment_user_search, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        customLoadingProgressBar = new CustomLoadingProgressBar(getActivity(), "Searching..." , R.raw.search_anim );
        customLoadingProgressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        recyclerView = v.findViewById(R.id.user_recycler_view);
        userList = new ArrayList<>();
        searchUserRecyclerViewAdapter =  new SearchUserRecyclerViewAdapter(getActivity() , userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchUserRecyclerViewAdapter.setHasStableIds(true);
        recyclerView.setAdapter(searchUserRecyclerViewAdapter);
        recyclerView.setHasFixedSize(true);


        Bundle bundle = getArguments();
        if (bundle != null) {
            customLoadingProgressBar.show();
            // save the search term for pagination
            searchTerm =  ((String) bundle.get("myQuery")).trim();
            getSearch(searchTerm);
        }

    }

    private void getSearch(String personalQuery) {

        Query query;
            // limit the data to 30 matching users
        query = rootRef.child("Users").limitToFirst(20).orderByChild("name").startAt(personalQuery)
                .endAt(personalQuery + "\uf8ff");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot dataSnapshot
                    :snapshot.getChildren()){
                        User user = dataSnapshot.getValue(User.class);
                        userList.add(user);
                    }
                    searchUserRecyclerViewAdapter.notifyDataSetChanged();

                }
               closeProgressBarsSetOverPullListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                closeProgressBarsSetOverPullListener();
            }
        });

    }

    void closeProgressBarsSetOverPullListener(){

        if(customLoadingProgressBar!=null){
            customLoadingProgressBar.dismiss();
        }
        if(refreshProgressBar!=null){
            refreshProgressBar.dismiss();
        }
    }
}