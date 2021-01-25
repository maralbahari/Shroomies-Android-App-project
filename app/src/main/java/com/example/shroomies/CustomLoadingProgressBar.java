package com.example.shroomies;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import org.w3c.dom.Text;

public class CustomLoadingProgressBar extends Dialog {

    TextView loadingText;

    LottieAnimationView lottieAnimationView;
    String text;
    int rawRes;

    public CustomLoadingProgressBar(Context context , String text , int rawRes) {
        super(context);
        this.text = text;
        this.rawRes = rawRes;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_progress_dialog);
        loadingText = findViewById(R.id.loading_text);
        lottieAnimationView = findViewById(R.id.lottie_loading_animation);
        setLoadingText(text);
        setLottieAnimationView(rawRes);

    }


    void setLoadingText(String text){
        this.loadingText.setText(text);
    }

    public void setLottieAnimationView(int rawRes) {
        this.lottieAnimationView.setAnimation(rawRes);
    }
}
