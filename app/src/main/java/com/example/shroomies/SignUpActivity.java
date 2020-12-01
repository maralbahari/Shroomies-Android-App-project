package com.example.shroomies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.Tag;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {
    private EditText name;
    private EditText email;
    private EditText password;
    private EditText confirmpw;
    private Button register;
    private DatabaseReference mRootref;
    private FirebaseAuth mAuth;
    ProgressDialog pd;
    Switch enableBiometric;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        enableBiometric=findViewById(R.id.enable_bio_signup);
        name = findViewById(R.id.fullname);
        email = findViewById(R.id.emailid);
        password = findViewById(R.id.password);
        confirmpw = findViewById(R.id.confirmpw);
        register = findViewById(R.id.registerbt);
        mRootref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);
        Boolean isEnabled;

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtName = name.getText().toString();
                String txtEmail = email.getText().toString();
                String txtPass = password.getText().toString();
                String txtConfpw = confirmpw.getText().toString();


                if (TextUtils.isEmpty(txtName) || TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPass)
                        || TextUtils.isEmpty(txtConfpw)){
                    Toast.makeText(SignUpActivity.this, "Empty credentials!", Toast.LENGTH_SHORT).show();
                }
                else if (txtPass.length() < 6){
                    Toast.makeText(SignUpActivity.this, "Password too short!", Toast.LENGTH_SHORT).show();
                }
                else {
                    // check if the  biometric is enabled
                    if(enableBiometric.isChecked()){
                        if(canAuthenticate()){
                            registerUser(txtName , txtEmail , txtPass, txtConfpw,true);
                        }else {
                            enableBiometric.setChecked(false);
                        }
                    }
                    else{
                        registerUser(txtName , txtEmail , txtPass, txtConfpw,false);
                    }

                }
            }
        });

    }



    private void registerUser(final String name, final String email, String password, String confirmpw, final boolean isEnabled) {
        pd.setMessage("Please wait");
        pd.show();

        mAuth.createUserWithEmailAndPassword(email, password). addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                HashMap<String, Object> userDetails = new HashMap<>();
                userDetails.put("name", name);
                userDetails.put("email", email);
                userDetails.put("ID", mAuth.getCurrentUser().getUid());
                userDetails.put("biometricEnabled",isEnabled);

                mRootref.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            sendEmailVerification();

                        }
                    }
                });
            }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(SignUpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            });
    }
    private void sendEmailVerification(){
        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                findViewById(R.id.registerbt).setEnabled(true);
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(), "Registered Successfully. Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                    //Toast.makeText(SignUpActivity.this, name+", you are a Shroomie now", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else {
                    Log.e("Register", "Send Email verification failed!",task.getException());
                    Toast.makeText(getApplicationContext(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    // check if the device can use user's biometric
    public boolean canAuthenticate(){
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Toast.makeText(SignUpActivity.this, "App can authenticate using biometrics.",Toast.LENGTH_LONG).show();
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(SignUpActivity.this, "No biometric features available on this device.",Toast.LENGTH_LONG).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(SignUpActivity.this, "Biometric features are currently unavailable.",Toast.LENGTH_LONG).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(SignUpActivity.this, "The user hasn't associated " +
                        "any biometric credentials with their account.",Toast.LENGTH_LONG).show();
                break;
        }
        return false;
    }
}
