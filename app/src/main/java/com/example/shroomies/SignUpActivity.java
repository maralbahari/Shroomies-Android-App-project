package com.example.shroomies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        name = findViewById(R.id.fullname);
        email = findViewById(R.id.emailid);
        password = findViewById(R.id.password);
        confirmpw = findViewById(R.id.confirmpw);
        register = findViewById(R.id.registerbt);

        mRootref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);

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
                    registerUser( txtName , txtEmail , txtPass, txtConfpw);
                }
            }
        });
    }

    private void registerUser(final String name, final String email, String password, String confirmpw) {
        pd.setMessage("Please wait");
        pd.show();

        mAuth.createUserWithEmailAndPassword(email, password). addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                HashMap<String, Object> userDetails = new HashMap<>();
                userDetails.put("name", name);
                userDetails.put("email", email);
                userDetails.put("ID", mAuth.getCurrentUser().getUid());

                mRootref.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(userDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            pd.dismiss();
                            Toast.makeText(SignUpActivity.this, "Your profile has been created", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();


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
}
