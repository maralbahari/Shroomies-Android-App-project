package com.example.shroomies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.virgilsecurity.android.ethree.interaction.EThree;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginPassword extends AppCompatActivity {
    private String bundledEmail;
    private TextInputLayout passwordEditText;
    private FirebaseAuth mAuth;
    private LottieAnimationView loadingAnimationView;
    private RelativeLayout rootLayout;
    private TextInputLayout emailEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_password);
        mAuth=FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.user_email_text_view);
        passwordEditText=findViewById(R.id.password_login);
        Button forgotPasswordButton = findViewById(R.id.forgot_password_button);
        loadingAnimationView = findViewById(R.id.login_animation_view);
        loginButton = findViewById(R.id.login_button);
        rootLayout = findViewById(R.id.root_layout);

        Toolbar toolbar = findViewById(R.id.login_password_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            bundledEmail =bundle.getString("EMAIL");
            emailEditText.getEditText().setText(bundledEmail);
        }
        loginButton.setOnClickListener(view -> {
            String enteredPassword=passwordEditText.getEditText().getText().toString().trim();
            if(enteredPassword.isEmpty()){
                passwordEditText.setError("Please enter your password");
            }else {
                closeKeyboard();
                login(bundledEmail,enteredPassword);
//                getE3Token(bundledEmail,enteredPassword);
            }
        });
        forgotPasswordButton.setOnClickListener(view -> startActivity(new Intent(LoginPassword.this,ResetPassword.class)));

    }
    private void login(String enteredEmail,String password){
        loadingAnimationView.setVisibility(View.VISIBLE);
        loginButton.setClickable(false);
        mAuth.signInWithEmailAndPassword(enteredEmail, password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                FirebaseUser firebaseUser=mAuth.getCurrentUser();
                if(firebaseUser!=null){
//                    if(firebaseUser.isEmailVerified()){
                        Intent intent = new Intent(LoginPassword.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
//                    }else{
//                        Toast.makeText(this,"here",Toast.LENGTH_SHORT).show();
//                        loginButton.setClickable(true);
//                        loadingAnimationView.setVisibility(View.GONE);
//                        Snackbar mySnackbar = Snackbar.make(rootLayout,
//                                "Please verify your email", Snackbar.LENGTH_LONG);
//                        mySnackbar.setAction("Resend", v -> firebaseUser.sendEmailVerification().addOnCompleteListener(task1 -> {
//                            if (task1.isSuccessful()) {
//                                new CustomToast(LoginPassword.this , "Email sent" ,R.drawable.ic_accept_check).showCustomToast();
//                            }
//                        }));
//                        mySnackbar.show();
//                    }
                }else{
                    loginButton.setClickable(true);
                    loadingAnimationView.setVisibility(View.GONE);
                    new CustomToast(LoginPassword.this , "We encountered a problem authenticating your account" ,R.drawable.ic_error_icon).showCustomToast();
                }
            }else{
                loginButton.setClickable(true);
                loadingAnimationView.setVisibility(View.GONE);
                try {
                    throw task.getException();
                } catch(FirebaseAuthInvalidCredentialsException e) {
                    emailEditText.setError(" ");
                    passwordEditText.setError("The email or password is incorrect");
                } catch(Exception e) {
                    new CustomToast(LoginPassword.this , "We encountered an unexpected problem" ,R.drawable.ic_error_icon).showCustomToast();

                }
            }
        });
    }
    public void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}