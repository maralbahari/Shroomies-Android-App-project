package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends AppCompatActivity {
    private TextInputLayout emailPasswordRecoveryButton;
    private LottieAnimationView loadingAnimationView;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        emailPasswordRecoveryButton = findViewById(R.id.email_password_recovery);
        MaterialButton sendEmailButton = findViewById(R.id.send_password_recovery_button);
        loadingAnimationView = findViewById(R.id.reset_password_loading_animation_view);
        mAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.password_recovery_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());


        sendEmailButton.setOnClickListener(v -> {

            String email = emailPasswordRecoveryButton.getEditText().getText().toString().trim();
            if(!validEmail(email)) {
                emailPasswordRecoveryButton.setError("Please enter valid email");
            }else {
                emailPasswordRecoveryButton.setError(null);
                loadingAnimationView.setVisibility(View.VISIBLE);
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ResetPassword.this, "Check email for password reset instructions!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(ResetPassword.this , LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            } else {
                                Toast.makeText(ResetPassword.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            loadingAnimationView.setVisibility(View.GONE);
                        });
            }

        });

    }
    private boolean validEmail(String enteredEmail){
        return android.util.Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches();
    }

}