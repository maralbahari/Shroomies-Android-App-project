package com.example.shroomies;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;


public class RefreshProgressBar extends DialogFragment {
    View v;

    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            WindowManager.LayoutParams layoutParams = getDialog().getWindow().getAttributes();
            layoutParams.y = 250; // bottom margin
            getDialog().getWindow().setAttributes(layoutParams);
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
            getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

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
        v= inflater.inflate(R.layout.fragment_refresh_progress_bar, container, false);
        return v;
    }


}