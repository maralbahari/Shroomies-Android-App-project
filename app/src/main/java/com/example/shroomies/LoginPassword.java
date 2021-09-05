package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginPassword extends AppCompatActivity {
    protected EditText password;
    Button login;
    TextView userEmail, forgotPassword;;

    FirebaseAuth mAuth;
    CustomLoadingProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_password);

        FirebaseApp.initializeApp(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();

        password=findViewById(R.id.password_login);
        forgotPassword=findViewById(R.id.forgot_password_login);
        login=findViewById(R.id.login_button);
        userEmail = findViewById(R.id.email_textview);
        String username = getIntent().getStringExtra("EMAIL");
        userEmail.setText(username);

        forgotPassword.setOnClickListener(v -> {
            // to reset password activity
            Intent intent=new Intent(getApplication(),ResetPassword.class);
            startActivity(intent);
            finish();
        });

        login.setOnClickListener(v -> {
            login.setBackgroundColor(getColor(R.color.LogoYellow));
            login.setTextColor(getColor(R.color.white));
            loginUser();
        });
    }

    private void loginUser(){
        progressBar = new CustomLoadingProgressBar(LoginPassword.this , "Loading..." ,R.raw.loading_animation);
        progressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressBar.show();
        final String username_txt = userEmail.getText().toString().trim();
        String password_txt = password.getText().toString().trim();

        //check fields are filled
        if (TextUtils.isEmpty(username_txt) || TextUtils.isEmpty(password_txt)){
            Toast.makeText(LoginPassword.this, "Please fill in your details",Toast.LENGTH_SHORT).show();
        }
        else {
            mAuth.signInWithEmailAndPassword(username_txt, password_txt). addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    progressBar.dismiss();

                    //check if user has verified their email
                    if(mAuth.getCurrentUser().isEmailVerified()){
                        FirebaseUser user =mAuth.getCurrentUser();
                        String userID =user.getUid();
                        String userEmail =user.getEmail();
//                            getE3Token();
                        Toast.makeText(LoginPassword.this, "Welcome to Shroomies! ", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginPassword.this, MainActivity.class);
                        intent.putExtra("ID",userID);
                        intent.putExtra("EMAIL",userEmail);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                    if(!(mAuth.getCurrentUser().isEmailVerified())){
                        progressBar.dismiss();

                        //send new verification email if needed
                        Toast.makeText(LoginPassword.this, "Please verify your email address", Toast.LENGTH_SHORT).show();
                        final FirebaseUser user = mAuth.getCurrentUser();
                        user.sendEmailVerification().addOnCompleteListener(LoginPassword.this, task1 -> {
                            login.setEnabled(true);
                            if(task1.isSuccessful()){
                                Toast.makeText(LoginPassword.this, "New verification email has been sent to"+ user.getEmail(), Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(LoginPassword.this, "Failed to send email verification", Toast.LENGTH_SHORT).show();
                            }
                            progressBar.dismiss();
                        });
                    }

                }
                else{
                    Toast.makeText(LoginPassword.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                    progressBar.dismiss();
                }
            }).addOnFailureListener(e -> {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    //wrong password
                    Toast.makeText(LoginPassword.this, "Invalid password", Toast.LENGTH_SHORT).show();
                    progressBar.dismiss();
                }
                else if (e instanceof FirebaseAuthInvalidUserException) {

                    //user account error
                    String errorCode = ((FirebaseAuthInvalidUserException) e).getErrorCode();

                    if (errorCode.equals("ERROR_USER_NOT_FOUND")) {
                        Toast.makeText(LoginPassword.this, "The email does not belong to a registered account. Please proceed to Sign Up.", Toast.LENGTH_SHORT).show();
                        progressBar.dismiss();
                    }
                    else if (errorCode.equals("ERROR_USER_DISABLED")) {
                        Toast.makeText(LoginPassword.this, "User account has been disabled. Please contact the administrator.", Toast.LENGTH_SHORT).show();
                        progressBar.dismiss();
                    }
                    else {
                        Toast.makeText(LoginPassword.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.dismiss();
                    }
                }
            });
        }
    }

}