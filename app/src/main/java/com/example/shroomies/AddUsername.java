package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.virgilsecurity.sdk.cards.Card;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddUsername extends AppCompatActivity {
    private static final Pattern pattern = Pattern.compile(Config.USERNAME_PATTERN);
    private TextInputLayout addUsernameTextInputLayout;
    private LottieAnimationView loadingAnimationView;
    private CardView infoCardView;
    private MaterialButton continueButton;
    private DatabaseReference rootRef;
    private MaterialButton backToLogin;
   private FirebaseAuth mAuth;


    @Override
    public void onBackPressed() {
        //sign out the user
        if(mAuth!=null){
            mAuth.signOut();
        }
        //finish the activity
        finish();
    }

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_name);
        mAuth  = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        addUsernameTextInputLayout = findViewById(R.id.add_username_text_input);
        continueButton = findViewById(R.id.add_user_name_button);
        infoCardView = findViewById(R.id.help_card_view);
        loadingAnimationView = findViewById(R.id.add_user_name_animation_view);
        backToLogin = findViewById(R.id.back_to_login_button);

        addUsernameTextInputLayout.setEndIconOnClickListener(view -> {
            if(infoCardView.getVisibility()==View.VISIBLE){
                infoCardView.setVisibility(View.GONE);
            }else{
                infoCardView.setVisibility(View.VISIBLE);
            }
        });
        backToLogin.setOnClickListener(v -> {
            onBackPressed();
        });


        continueButton.setOnClickListener(view -> {
            String userName = addUsernameTextInputLayout.getEditText().getText().toString().toLowerCase().trim();
            if(validUsername(userName)){
                loadingAnimationView.setVisibility(View.VISIBLE);
                continueButton.setClickable(false);
                checkDuplicateUserName(userName).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        if(task.getResult().getValue()!=null){
                            addUsernameTextInputLayout.setError("This username is already taken");
                            continueButton.setClickable(true);
                            loadingAnimationView.setVisibility(View.GONE);
                        }else{
                            setUsername(userName);
                            addUsernameTextInputLayout.setError(null);
                        }
                    }
                });
            }else{
                addUsernameTextInputLayout.setError("Please enter a valid username");
            }
        });
    }
    private void setUsername(String userName) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if(firebaseUser!=null){
            rootRef.child(Config.users)
                    .child(firebaseUser.getUid())
                    .child(Config.username)
                    .setValue(userName)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }else{
                            //todo handle error
                            continueButton.setClickable(true);
                            loadingAnimationView.setVisibility(View.GONE);

                        }
                    });
                }else{
                    //todo handle error
             continueButton.setClickable(true);
            loadingAnimationView.setVisibility(View.GONE);

        }


    }

    public static boolean validUsername(final String username) {
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }
    private Task<DataSnapshot> checkDuplicateUserName(String userName){
        return rootRef.child(Config.usersnames).orderByKey().equalTo(userName).get().addOnCompleteListener(task -> { });
    }
}
