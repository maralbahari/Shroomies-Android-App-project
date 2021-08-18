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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MessageInbox extends Fragment {
    private View v;
    private RecyclerView inboxListRecyclerView;
    private List<String> usersArrayList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private PrivateInboxRecycleViewAdapter messageInboxRecycleViewAdapter;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_message_inbox, container, false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inboxListRecyclerView =v.findViewById(R.id.inbox_recycler);
        usersArrayList = new ArrayList<>();
        messageInboxRecycleViewAdapter=new PrivateInboxRecycleViewAdapter(usersArrayList,getActivity());
        linearLayoutManager=new LinearLayoutManager(getActivity());
        inboxListRecyclerView.setHasFixedSize(true);
        inboxListRecyclerView.setLayoutManager(linearLayoutManager);
        inboxListRecyclerView.setAdapter(messageInboxRecycleViewAdapter);

        getPrivateChatList();

    }

    public void getPrivateChatList(){
        rootRef.child("PrivateChatList").child(mAuth.getInstance().getCurrentUser().getUid()).orderByChild("receiverID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        HashMap<String,String> recivers= (HashMap) ds.getValue();
                        usersArrayList.add(recivers.get("receiverID"));
                        Toast.makeText(getActivity(), recivers.toString(), Toast.LENGTH_SHORT).show();
                    }

                    messageInboxRecycleViewAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}