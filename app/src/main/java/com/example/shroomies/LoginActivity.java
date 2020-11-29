package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.io.File;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {
   protected EditText username;
   protected EditText password;
    Button login;
    TextView signup;
    FirebaseAuth mAuth;
    SessionManager sessionManager;
    boolean successBiometric = false;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private String user;
    TextView forgotPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
            sessionManager=new SessionManager();
            user=sessionManager.checkUsersLoggedIn(getApplicationContext());
        if(user!=null){
            Intent intent=new Intent(this,MainActivity.class);
            intent.putExtra("USERNAME",user);
            startActivity(intent);
            finish();
        }
            mAuth = FirebaseAuth.getInstance();
            username = findViewById(R.id.email_login);
            password = findViewById(R.id.password_login);
            login = findViewById(R.id.login_button);
            forgotPassword=findViewById(R.id.forgot_password_login);
            username.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }
                // check if the biometric is enbled from session manager and only prompt the authentication once clicking on password edit text
                @Override
                public void afterTextChanged(Editable s) {
                    final String completeUsername=username.getText().toString();
                    sessionManager=new SessionManager(getApplicationContext(),completeUsername);
                    sessionManager.createSession(completeUsername,completeUsername);
                    if(sessionManager.biometricIsEnabled()){
                        password.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                setBiometric();
                                promptInfo = new BiometricPrompt.PromptInfo.Builder()
                                        .setTitle("Biometric login for my app")
                                        .setSubtitle("Log in using your biometric credential")
                                        .setNegativeButtonText("Use account password")
                                        .build();
                                biometricPrompt.authenticate(promptInfo);
                                return false;
                            }
                        });
                    }

                }
            });
            forgotPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(getApplication(),ResetPassword.class);
                    startActivity(intent);
                    finish();
                }
            });
            signup = findViewById(R.id.sign_up_button);
            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplication(), SignUpActivity.class);
                    startActivity(intent);

                }
            });
    if(!successBiometric) {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    loginUser(successBiometric);

            }
        });
    }


    }

    private void loginUser(boolean sucessfulBiometric){
        final String uname_txt = username.getText().toString();
        String pw_txt = password.getText().toString();
        if (TextUtils.isEmpty(uname_txt) || TextUtils.isEmpty(pw_txt)) {
            Toast.makeText(LoginActivity.this, "Please fill in your details", Toast.LENGTH_SHORT).show();
        }else{

            mAuth.signInWithEmailAndPassword(uname_txt, pw_txt). addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        sessionManager=new SessionManager(getApplicationContext(),uname_txt);
                        sessionManager.createSession(uname_txt,uname_txt);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        Toast.makeText(LoginActivity.this, "Welcome to Shroomies!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(LoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                    } else if (e instanceof FirebaseAuthInvalidUserException) {

                        String errorCode =
                                ((FirebaseAuthInvalidUserException) e).getErrorCode();

                        if (errorCode.equals("ERROR_USER_NOT_FOUND")) {
                            Toast.makeText(LoginActivity.this, "The email does not belong to a registered account. Please proceed to Sign Up.", Toast.LENGTH_SHORT).show();
                        } else if (errorCode.equals("ERROR_USER_DISABLED")) {
                            Toast.makeText(LoginActivity.this, "User account has been disabled", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });

        }

        }
    boolean setBiometric(){
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricConstants.ERROR_USER_CANCELED){
                    password.setOnTouchListener(null);
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Authentication error: " + errString, Toast.LENGTH_SHORT)
                            .show();
                }
                successBiometric = false;
            }
            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                loginUser(successBiometric);
                successBiometric=true;
            }
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
                successBiometric=false;
            }
        });

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        return successBiometric;
    }
    //we check sessionManager if user has enabled the biometric function
    }

