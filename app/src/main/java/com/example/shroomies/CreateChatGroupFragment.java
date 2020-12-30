package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

public class CreateChatGroupFragment extends DialogFragment {


    SearchView searchView;
    RecyclerView userListSuggestion;
    ImageView createGroupButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_chat_group, container, false);

    }
}