package com.example.shroomies;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

public class ChangePassword extends Fragment {
    private EditText cur_password;
    private EditText new_password;
    private EditText rep_password;
    private ImageButton change_password;

    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootref;

    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_change_password, container, false);
        cur_password = v.findViewById(R.id.current_password);
        new_password = v.findViewById(R.id.edit_password);
        rep_password = v.findViewById(R.id.edit_repeat_password);

        change_password = v.findViewById(R.id.password_button);

        change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPassword();
            }
        });


        return v;
    }

    private void newPassword() {
        if (cur_password.toString().isEmpty() && new_password.toString().isEmpty() && rep_password.toString().isEmpty()){
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
        } else {
            if((new_password.getText().toString()).equals(rep_password.getText().toString())){
                //final String userUid =  mAuth.getInstance().getCurrentUser().getUid();
                mUser = mAuth.getInstance().getCurrentUser();

                AuthCredential credential = EmailAuthProvider.getCredential(mUser.getEmail(), cur_password.getText().toString());
                mUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mUser.updatePassword(new_password.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(getContext(), "Error! Password not updated", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else {
                            Toast.makeText(getContext(), "Authorization failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else {
                Toast.makeText(getContext(), "Password mismatch. Please enter the same password in new password and repeat password.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}