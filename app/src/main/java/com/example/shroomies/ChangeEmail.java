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

public class ChangeEmail extends Fragment {
    private FirebaseUser mUser;
    private FirebaseDatabase mDataref;
    private FirebaseAuth mAuth;
    private DatabaseReference mRootref;
    final String userUid =  mAuth.getInstance().getCurrentUser().getUid();

    private EditText  email;
    private TextView currentEmail;
    private Button saveEmail, exit;
    CardView cardEmail;
    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_change_email, container, false);
        email = v.findViewById(R.id.new_email);
        currentEmail = v.findViewById(R.id.textview_cur_email);
        saveEmail = v.findViewById(R.id.change_email);
        cardEmail = v.findViewById(R.id.email_card);
        exit = v.findViewById(R.id.exit);

        mRootref = mDataref.getInstance().getReference();
        mRootref.child("Users").child(userUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                currentEmail.setText(user.getEmail());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        saveEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEmail();
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
    private void updateEmail() {

        String txtEmail = email.getText().toString();

        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("email", txtEmail);

        mDataref.getInstance().getReference().child("Users").child(userUid).updateChildren(updateDetails).addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Updated email successfully", Toast.LENGTH_SHORT).show();
                    sendEmailVerification();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendEmailVerification() {
        mUser = mAuth.getInstance().getCurrentUser();

        mUser.updateEmail(email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                   // Toast.makeText(getContext(), "Error! Try again", Toast.LENGTH_SHORT).show();
                    final FirebaseUser user = mAuth.getInstance().getCurrentUser();
                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Updated Successfully. Verification email sent to " + mUser.getEmail(), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                startActivity(intent);

                            } else {
                                Toast.makeText(getActivity(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else {

                }
            }
        });

    }
}