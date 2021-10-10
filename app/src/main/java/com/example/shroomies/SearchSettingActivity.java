package com.example.shroomies;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class SearchSettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_setting);
        MaterialToolbar toolbar = findViewById(R.id.search_setting_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        this.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.search_settings_content_frame, new SearchSettingsFragment())
                .commit();

    }
}
