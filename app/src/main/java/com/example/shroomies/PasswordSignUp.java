package com.example.shroomies;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.virgilsecurity.android.common.callback.OnGetTokenCallback;
import com.virgilsecurity.android.ethree.interaction.EThree;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PasswordSignUp extends AppCompatActivity {
    //views
    private TextInputLayout passwordEditText;
    private TextInputLayout repPasswordEditText;
    private CheckBox termsCond;
    private MaterialButton register;
    private LottieAnimationView loadingAnimation;
    //variables
    public static EThree eThree;
    private  String token ="";
    //firebase
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_sign_up);
        mAuth=FirebaseAuth.getInstance();

        passwordEditText=findViewById(R.id.password_sign_up);
        repPasswordEditText=findViewById(R.id.confirm_password_sign_up);
        register = findViewById(R.id.register_button);
        termsCond=findViewById(R.id.terms_conditions_privacy_check_box);
        loadingAnimation =  findViewById(R.id.register_animation_view);

        Toolbar toolbar = findViewById(R.id.sign_up_password_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        Bundle bundle= getIntent().getExtras();
        if (bundle!=null) {
            String enteredEmail=bundle.getString("EMAIL");
            String enteredUsername=bundle.getString("USERNAME");
            if (enteredEmail!=null && enteredUsername!=null) {
                register.setOnClickListener(view -> {
                    String enteredPassword= Objects.requireNonNull(passwordEditText.getEditText()).getText().toString().trim();
                    String repeatedPassword= Objects.requireNonNull(repPasswordEditText.getEditText()).getText().toString().trim();
                    if (eligibaleToGetStarted(enteredPassword,repeatedPassword)) {
                        passwordEditText.setError(null);
                        repPasswordEditText.setError(null);
                        registerUser(enteredEmail,enteredPassword,enteredUsername);
                    }else {
                        if (!termsCond.isChecked()) {
                            new CustomToast(PasswordSignUp.this , "Please read and accept the our policy terms and condition" ,R.drawable.ic_error_icon).showCustomToast();
                        }
                        if(!enteredPassword.equals(repeatedPassword)) {
                            repPasswordEditText.setError("Password do not match");
                        }else{
                            repPasswordEditText.setError(null);
                        }
                        if(enteredPassword.length()<8){
                            passwordEditText.setError("Password must contain at least 8 characters");
                        }else{
                            passwordEditText.setError(null);
                        }
                    }
                });
            }
        }

    }
    private boolean eligibaleToGetStarted(String enteredPass,String repPass){
        return termsCond.isChecked() && enteredPass.equals(repPass) && enteredPass.length() >= 8;
    }
    private void registerUser(String email,String password,String username){
        register.setClickable(false);
        loadingAnimation.setVisibility(View.VISIBLE);
        loadingAnimation.playAnimation();
        HashMap<String,Object> usernameHash=new HashMap<>();
        usernameHash.put("username",username);
        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser=mAuth.getCurrentUser();
                if(firebaseUser!=null) {
                    rootRef.child(Config.users).child(firebaseUser.getUid()).updateChildren(usernameHash).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            loadingAnimation.setVisibility(View.GONE);
                            loadingAnimation.pauseAnimation();
                            new CustomToast(PasswordSignUp.this , "Registered Successfully" ,R.drawable.ic_accept_check).showCustomToast();
                            Intent intent = new Intent(PasswordSignUp.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    }).addOnFailureListener(e -> {
                        loadingAnimation.setVisibility(View.GONE);
                        loadingAnimation.pauseAnimation();
                        new CustomToast(PasswordSignUp.this , "We encountered an unexpected error" ,R.drawable.ic_error_icon).showCustomToast();
                    });
                }
            }

        }).addOnFailureListener(e -> {
            loadingAnimation.setVisibility(View.GONE);
            loadingAnimation.pauseAnimation();
            new CustomToast(PasswordSignUp.this , e.getMessage() ,R.drawable.ic_error_icon).showCustomToast();
        });
    }
    private void sendEmailVerification(){
        final FirebaseUser user = mAuth.getCurrentUser();
        if (user!=null) {
            user.sendEmailVerification().addOnCompleteListener(this, task -> {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Registered Successfully. kindly Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    getE3Token(user);
                    Intent intent = new Intent(PasswordSignUp.this, LoginActivity.class);
                    startActivity(intent);
                    Toast.makeText(PasswordSignUp.this, user.getDisplayName()+", you are a Shroomie now", Toast.LENGTH_SHORT).show();
                    //get public and private keys for Virgil e3 kit
                }
                else {
                    Log.e("Register", "Send Email verification failed!",task.getException());
                    new CustomToast(PasswordSignUp.this , "Failed to send verification email" ,R.drawable.ic_error_icon).showCustomToast();
                }
            });
        }
    }
    private void getE3Token(FirebaseUser user) {
        com.virgilsecurity.common.callback.OnCompleteListener registerListener = new com.virgilsecurity.common.callback.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Log.d("ethree register ", "success");

            }
            @Override
            public void onError(@NotNull Throwable throwable) {
                Log.d("ethree register ", throwable.toString());
            }
        };
        /*Map<String, String> data*/
        FirebaseFunctions.getInstance()
                .getHttpsCallable("getVirgilJwt")
                .call()
                .continueWith(task -> {
                    token = ((Map<String, String>) task.getResult().getData()).get("token");
                    eThree = new EThree(user.getUid()
                            , (OnGetTokenCallback) this::getToken
                            , getApplicationContext());
                    eThree.register().addCallback(registerListener);
                    return null;

                });
    }

    public String getToken(){
        return token;
    }
}