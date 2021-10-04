package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
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
    private SearchUserRecyclerViewAdapter searchUserRecyclerViewAdapter;
    private List<User> userList;
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
        RecyclerView recyclerView = v.findViewById(R.id.user_recycler_view);
        userList = new ArrayList<>();
        searchUserRecyclerViewAdapter = new SearchUserRecyclerViewAdapter(getActivity(), userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        MaterialToolbar toolbar = getActivity().findViewById(R.id.toolbar);
        android.widget.SearchView searchView = toolbar.findViewById(R.id.SVsearch_disc);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!query.trim().equals("")) {
                    getSearch(query.trim().toLowerCase());
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        int searchCloseButtonId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeButton = searchView.findViewById(searchCloseButtonId);
        closeButton.setOnClickListener(v -> {
            // if the user closed the search get  the starting page apartments
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.setIconifiedByDefault(false);
        });


        searchUserRecyclerViewAdapter.setHasStableIds(true);
        recyclerView.setAdapter(searchUserRecyclerViewAdapter);
        recyclerView.setHasFixedSize(true);


    }

    private void getSearch(String personalQuery) {
        Query query;
            // limit the data to 30 matching users
        query = rootRef.child(Config.users).limitToFirst(30).orderByChild(Config.username).startAt(personalQuery)
                .endAt(personalQuery + "\uf8ff");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    userList.clear();
                    for (DataSnapshot dataSnapshot
                    :snapshot.getChildren()){
                        User user = dataSnapshot.getValue(User.class);
                        userList.add(user);
                    }
                    searchUserRecyclerViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }
}