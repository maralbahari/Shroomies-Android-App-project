package com.example.shroomies;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;

public class MyShroomiesActivity extends AppCompatActivity {

    FragmentManager fm;
    FragmentTransaction ft;
    FirebaseAuth mAuth;
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


    }

}