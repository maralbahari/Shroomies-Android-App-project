package com.example.shroomies;

import android.content.Intent;
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

public class ChangeEmailDialog extends DialogFragment {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private User user;
    private EditText newEmail;
    private TextView currentEmail;
    private Button saveEmail, exitEmailDialog;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.dialog_fragment_change_email, container, false);
        mAuth=FirebaseAuth.getInstance();
        rootRef=FirebaseDatabase.getInstance().getReference();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newEmail = v.findViewById(R.id.enter_new_email);
        currentEmail = v.findViewById(R.id.display_current_email);
        saveEmail = v.findViewById(R.id.change_email_button);
        exitEmailDialog = v.findViewById(R.id.exit_button);
        Bundle bundle=this.getArguments();
        if (bundle!=null) {
            user=bundle.getParcelable("USER");
            currentEmail.setText(user.getEmail());
        } else {
//            todo error handling when bundle is null
        }


        saveEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser firebaseUser=mAuth.getCurrentUser();
                if (firebaseUser!=null) {
                    String txtEmail = newEmail.getText().toString();
                    updateEmail(txtEmail);
                } else{
                    //            todo error handling when user is null or signed out

                }
            }
        });

        exitEmailDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }
    private void updateEmail(String txtEmail) {
        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("email", txtEmail);

        rootRef.child("users").child(user.getUserID()).updateChildren(updateDetails).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Updated email successfully", Toast.LENGTH_SHORT).show();
//                    FirebaseUser firebaseUser=mAuth.getCurrentUser();
//                    sendEmailVerification(firebaseUser);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void sendEmailVerification(FirebaseUser firebaseUser) {
//        firebaseUser.updateEmail(newEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if(task.isSuccessful()){
//                   // Toast.makeText(getContext(), "Error! Try again", Toast.LENGTH_SHORT).show();
                    firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Updated Successfully. Verification email sent to " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);

                            } else {
                                Toast.makeText(getActivity(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
//                }
//                else {
//
//                }
//            }
//        });
    }
}