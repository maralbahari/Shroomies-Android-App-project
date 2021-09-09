package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SignUpActivity extends AppCompatActivity{
    private TextInputLayout usernameEditText;
    private TextInputLayout emailEditText;
    private CardView helpCardView;
    private boolean userNameIsTaken;
    private static final Pattern pattern = Pattern.compile(Config.USERNAME_PATTERN);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        usernameEditText = findViewById(R.id.username_sign_up);
        emailEditText = findViewById(R.id.email_sign_up);
        ImageButton nextButton = findViewById(R.id.next_button_sign_up);
        helpCardView = findViewById(R.id.help_card_view);
        Toolbar toolbar = findViewById(R.id.sign_up_tool_bar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        if (usernameEditText.isErrorEnabled() && usernameEditText.isSelected()) {
            usernameEditText.setErrorEnabled(false);
        }
        usernameEditText.setErrorIconOnClickListener(view -> {
            if (helpCardView.getVisibility() == View.VISIBLE) {
                helpCardView.setVisibility(View.GONE);
            } else {
                helpCardView.setVisibility(View.VISIBLE);
            }
        });
        nextButton.setOnClickListener(view -> {
            String enteredEmail = Objects.requireNonNull(emailEditText.getEditText()).getText().toString().trim();
            String enteredUsername = Objects.requireNonNull(usernameEditText.getEditText()).getText().toString().toLowerCase().trim();
            if(eligibaleToContinue(enteredEmail,enteredUsername)) {

                    checkDuplicateUserName(enteredUsername).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            if(task.getResult().getValue()!=null){
                                usernameEditText.setError(enteredUsername + " is taken");
                            }else{
                                usernameEditText.setError(null);
                                emailEditText.setError(null);
                                usernameEditText.setError(null);
                                Intent intent = new Intent(SignUpActivity.this, PasswordSignUp.class);
                                intent.putExtra("EMAIL", enteredEmail);
                                intent.putExtra("USERNAME", enteredUsername);
                                startActivity(intent);
                            }
                        }else{
                            new CustomToast(SignUpActivity.this , "We encountered a problem" ,R.drawable.ic_error_icon).showCustomToast();

                        }
                    });

            }else {
                if (!validUsername(enteredUsername)) {
                    usernameEditText.setError("Please enter valid username");
                }else{
                    usernameEditText.setError(null);
                }
                if(!validEmail(enteredEmail)) {
                    emailEditText.setError("Please enter valid email");
                }else {
                    emailEditText.setError(null);
                }
            }
        });
        usernameEditText.setEndIconOnClickListener(view -> {
            if (helpCardView.getVisibility() == View.VISIBLE) {
                helpCardView.setVisibility(View.GONE);
            } else {
                helpCardView.setVisibility(View.VISIBLE);
            }
        });

    }
    private Task<DataSnapshot> checkDuplicateUserName(String userName){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference(Config.usersnames);
        return rootRef.orderByKey().equalTo(userName).get().addOnCompleteListener(task -> { });
    }
    private boolean eligibaleToContinue(String enteredEmail,String enteredUsername){
        return validUsername(enteredUsername) && validEmail(enteredEmail) && !userNameIsTaken;
    }
    private boolean validEmail(String enteredEmail){
        return android.util.Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches();
    }

    public static boolean validUsername(final String username) {
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

}
