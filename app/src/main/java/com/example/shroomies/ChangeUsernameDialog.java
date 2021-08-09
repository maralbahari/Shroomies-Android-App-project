package com.example.shroomies;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.view.Gravity;
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

public class ChangeUsernameDialog extends DialogFragment {
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private EditText username;
    private TextView currentUsername;
    private Button saveUsername, exitUsernameDialog;

    private User user;

    View v;

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.dialog_fragment_change_username, container, false);
        mAuth=FirebaseAuth.getInstance();
        rootRef=FirebaseDatabase.getInstance().getReference();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        username = v.findViewById(R.id.enter_new_username);
        currentUsername = v.findViewById(R.id.display_current_username);
        saveUsername = v.findViewById(R.id.change_username_button);
        exitUsernameDialog = v.findViewById(R.id.exit_button);
        Bundle bundle=this.getArguments();
        if (bundle!=null) {
            user=bundle.getParcelable("USER");
            currentUsername.setText(user.getName());
        } else {
//            todo handle error when bundle is null
        }
        saveUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser firebaseUser=mAuth.getCurrentUser();
                if (firebaseUser!=null) {
                    String txtName = username.getText().toString().toLowerCase();
                    updateUsername(txtName);
                } else {
//                    todo error handling when user is null
                }
            }
        });

        exitUsernameDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               dismiss();
            }
        });
    }

    private void updateUsername(String txtName) {
        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("name", txtName);
        rootRef.child("users").child(user.getUserID()).updateChildren(updateDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Updated username successfully", Toast.LENGTH_SHORT).show();
                } else {
//                    todo handle error if task failes
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        dismiss();
    }
}