package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class CreateChatGroupFragment extends DialogFragment {

    private   View v;
    private SearchView searchView;
    private RecyclerView userListSuggestion;
    private ImageButton createGroupButton;
    private DatabaseReference rootRef;
    private UserRecyclerAdapter userRecyclerAdapter;
    private LinearLayoutManager linearLayoutManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_create_chat_group, container, false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchView=v.findViewById(R.id.create_group_search_bar);
        userListSuggestion=v.findViewById(R.id.suggestion_list_create_group);
        createGroupButton=v.findViewById(R.id.confirm_button_create_group);
        linearLayoutManager=new LinearLayoutManager(getContext());
        userListSuggestion.setHasFixedSize(true);
        userListSuggestion.setLayoutManager(linearLayoutManager);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                userRecyclerAdapter=new UserRecyclerAdapter(getUsersListFromDatabase(query),getContext());
                userListSuggestion.setAdapter(userRecyclerAdapter);
                userRecyclerAdapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }
    public List<User> getUsersListFromDatabase(String query){
        final List<User> suggestedUser = null;
        rootRef.child("Users").orderByChild("name").equalTo(query+"\uf8ff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {

                        User user = snapshot.getValue(User.class);
                        suggestedUser.add(user);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
       return suggestedUser;
    }

}