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

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {
    EditText username;
    EditText password;
    Button login;
    TextView signup;

    FirebaseAuth mAuth;

    SessionManager sessionManager;
    boolean successBiometric = false;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    boolean alredyLoggedin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username=findViewById(R.id.email_login);
        password=findViewById(R.id.password_login);
        login=findViewById(R.id.login_button);
        signup=findViewById(R.id.sign_up_button);

        mAuth = FirebaseAuth.getInstance();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplication(),SignUpActivity.class);
                startActivity(intent);

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             String uname_txt = username.getText().toString();
             String pw_txt = password.getText().toString();

             if (TextUtils.isEmpty(uname_txt) || TextUtils.isEmpty(pw_txt)){
                 Toast.makeText(LoginActivity.this, "Please fill in your details",Toast.LENGTH_SHORT).show();
             }
             else {
                loginUser(uname_txt, pw_txt);
             }
            }
        });

    }
    private void loginUser(String username, String password){
        mAuth.signInWithEmailAndPassword(username, password). addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "Welcome to Shroomies!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
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

