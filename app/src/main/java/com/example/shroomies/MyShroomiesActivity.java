package com.example.shroomies;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.EventListener;

public class MyShroomiesActivity extends AppCompatActivity {

    FragmentManager fm;
    FragmentTransaction ft;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    String apartmentID;
    ValueEventListener apartmentListener;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_shroomies);
        //load myshroomies fragment
        Fragment myshroomiesFragment = new MyShroomiesFragment();
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.addToBackStack(null);
        ft.replace(R.id.my_shroomies_container, myshroomiesFragment);
        ft.commit();
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            // add a listener to check if the user has been removed from the room
            String uID = firebaseUser.getUid();
            apartmentListener= rootRef.child("users").child(uID).child(Config.apartmentID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d("snapshot" , snapshot.toString());
                    if(snapshot.exists()){
                        if(snapshot.getKey()!=null){
                            if(snapshot.getKey().equals(Config.apartmentID)){
                                Log.d("apartmentID ll" , snapshot.toString());
                                if(apartmentID!=null){
                                    if(!apartmentID.equals(snapshot.getValue())){
                                        finish();
                                        Toast.makeText(getApplicationContext() , "You have been removed from this room" , Toast.LENGTH_LONG).show();
                                    }
                                }else{
                                    apartmentID = snapshot.getValue().toString();
                                }
                            }
                        }

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        rootRef.addValueEventListener(apartmentListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        rootRef.removeEventListener(apartmentListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rootRef.removeEventListener(apartmentListener);
    }
}