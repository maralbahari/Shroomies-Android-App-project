package com.example.shroomies;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CustomToast {

    private final AppCompatActivity activity;
    private final String message;
    private int drawableIcon;

    public CustomToast(AppCompatActivity activity, String message , int drawableIcon) {
        this.activity = activity;
        this.message = message;
        this.drawableIcon  = drawableIcon;
    }
    public CustomToast(AppCompatActivity activity, String message ) {
        this.activity = activity;
        this.message = message;
    }


    public void showCustomToast() {
        activity.runOnUiThread(() -> {
            //inflate the custom toast
            LayoutInflater inflater = activity.getLayoutInflater();
            // Inflate the Layout
            View layout = inflater.inflate(R.layout.custom_toast, activity.findViewById(R.id.toast_layout));

            TextView text =layout.findViewById(R.id.toast_text);

            // Set the Text to show in TextView
            text.setText(message);
            //set the drawable
            if(drawableIcon!=0){
                text.setCompoundDrawablesWithIntrinsicBounds(drawableIcon, 0, 0, 0);
            }

            Toast toast = new Toast(activity.getApplication());
            //Setting up toast position, similar to Snackbar
            toast.setGravity(Gravity.BOTTOM | Gravity.START | Gravity.FILL_HORIZONTAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        });

    }
}
