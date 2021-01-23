package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ChangeUsername extends Fragment {
    private FirebaseUser mUser;
    private FirebaseDatabase mDataref;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootref;
    final String userUid =  mAuth.getInstance().getCurrentUser().getUid();

    private EditText username;
    private TextView currentUsername;
    private Button saveUsername, exit;
    CardView cardUsername;

    View v;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_change_username, container, false);

        username = v.findViewById(R.id.new_username);
        currentUsername = v.findViewById(R.id.textview_cur_username);
        saveUsername = v.findViewById(R.id.change_username);
        cardUsername = v.findViewById(R.id.username_card);
        exit = v.findViewById(R.id.exit);

        mRootref = mDataref.getInstance().getReference();
        mRootref.child("Users").child(userUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    currentUsername.setText(user.getName());
                }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        saveUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUsername();
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                        new EditProfile()).commit();
            }
        });

        return v;
    }

    private void updateUsername() {
        String txtName = username.getText().toString();

        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("name", txtName);

        mDataref.getInstance().getReference().child("Users").child(userUid).updateChildren(updateDetails).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Updated username successfully", Toast.LENGTH_SHORT).show();

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer,
                new EditProfile()).commit();
    }

}