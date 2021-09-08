package com.example.shroomies;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import com.virgilsecurity.android.common.callback.OnGetTokenCallback;
import com.virgilsecurity.android.ethree.interaction.EThree;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;

public class PasswordSignUp extends AppCompatActivity {
    //views
    private TextInputLayout passwordEditText;
    private TextInputLayout repPasswordEditText;
    private CheckBox termsCond;
    //variables
    public static EThree eThree;
    private  String token ="";
    private RequestQueue requestQueue;
    //firebase
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_sign_up);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        mAuth=FirebaseAuth.getInstance();

        passwordEditText=findViewById(R.id.password_sign_up);
        repPasswordEditText=findViewById(R.id.confirm_password_sign_up);
        MaterialButton register = findViewById(R.id.register_button);
        termsCond=findViewById(R.id.terms_conditions_privacy_check_box);

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
                            Toast.makeText(getApplicationContext(), "please read and accept the our policy terms and condition", Toast.LENGTH_SHORT).show();
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
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            jsonObject.put("email" , email);
            jsonObject.put("username", username);
            jsonObject.put("password" , password);
            data.put("data", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.POST, Config.URL_REGISTER_USER, data, response -> {

                    try {
                        String message = response.getJSONObject(Config.result).getString(Config.message);
                        boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                        if (success){
//                          sendEmailVerification();
                            Toast.makeText(PasswordSignUp.this, message, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(PasswordSignUp.this, LoginActivity.class);
                            startActivity(intent);
                        }else{
                            Toast.makeText(PasswordSignUp.this,message,Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {

                });
        requestQueue.add(jsonObjectRequest);
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
                    Toast.makeText(getApplicationContext(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
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