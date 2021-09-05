package com.example.shroomies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.virgilsecurity.android.common.callback.OnGetTokenCallback;
import com.virgilsecurity.android.ethree.interaction.EThree;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUpPassword extends AppCompatActivity {
    //views
    private EditText password, confirmPassword;
    private Button register;
    CustomLoadingProgressBar progressBar;

    //data
    private String email,name, token ="", apartmentID;

    //firebase
    private DatabaseReference mRootref;
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;

    public static EThree eThree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_password);

        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirm_password);
        register = findViewById(R.id.register_button);
        progressBar = new CustomLoadingProgressBar(SignUpPassword.this , "Creating account... " , R.raw.loading_animation);

        mRootref = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        Boolean isEnabled;

        //to add to database
        email = getIntent().getStringExtra("EMAIL");
        name = getIntent().getStringExtra("USERNAME");


        register.setOnClickListener(v -> {
            String txtPassword = password.getText().toString();
            String txtConfirmPassword = confirmPassword.getText().toString();

            //check fields are filled in
            if (TextUtils.isEmpty(txtPassword) || TextUtils.isEmpty(txtConfirmPassword)){
                Toast.makeText(SignUpPassword.this, "Empty credentials!", Toast.LENGTH_SHORT).show();
            }
            else {
                //check password for security reasons
                if (passwordIsValid(txtPassword)) {
                    register.setBackgroundColor(getColor(R.color.LogoYellow));
                    register.setTextColor(getColor(R.color.white));
                    registerUser(name, email, txtPassword, txtConfirmPassword);
                }
                else {
                    //show password requirements
                    Toast.makeText(getApplicationContext(), "Password length must be at least 8 and contain at least " +
                            " one upper case letter, one digit, and one special character", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void registerUser(final String name, final String email, String password, String confirmpw) {
        //add user details to database

        progressBar.show();
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            jsonObject.put("email" , email);
            jsonObject.put("name", name);
            jsonObject.put("password" , password);
            data.put("data", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.POST, Config.URL_REGISTER_USER, data, response -> {
                    progressBar.dismiss();
                    //                              sendEmailVerification();
                    Intent intent = new Intent(SignUpPassword.this, LoginActivity.class);
                    startActivity(intent);
                    Toast.makeText(SignUpPassword.this, name + ", you are a Shroomie now", Toast.LENGTH_SHORT).show();
                }, error -> {

                })
                {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                        return params;
                    }
                };
        requestQueue.add(jsonObjectRequest);
    }

// TODO set to active before submitting

    private void sendEmailVerification(){
        //verify emails for security purposes

        final FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(this, task -> {
            findViewById(R.id.register_button).setEnabled(true);
            if(task.isSuccessful()){

                Toast.makeText(getApplicationContext(), "Registered Successfully. Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                getE3Token();

                //go to login activity
                Intent intent = new Intent(SignUpPassword.this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(SignUpPassword.this, name + ", you are a Shroomie now", Toast.LENGTH_SHORT).show();
                //get public and private keys for Virgil e3 kit
            }
            else {
                Log.e("Register", "Send Email verification failed!",task.getException());
                Toast.makeText(getApplicationContext(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getE3Token() {
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
                .continueWith(new Continuation<HttpsCallableResult, Object>() {
                    @Override
                    public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        token = ((Map<String, String>) task.getResult().getData()).get("token");
                        eThree = new EThree(mAuth.getCurrentUser().getUid()
                                , (OnGetTokenCallback) () -> getToken()
                                , getApplicationContext());
                        eThree.register().addCallback(registerListener);
                        return null;

                    }
                });
    }

    public String getToken(){
        return token;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (progressBar != null) {
            progressBar.dismiss();
            progressBar = null;
        }
    }

    private boolean passwordIsValid(String password) {
        // password length should be more than 8 characters

        if (password.length() < 8) {
            return false;
        }
        // to check space
        if (password.contains(" ")) {
            return false;
        }
        if (true) {
            int count = 0;

            // check digits from 0 to 9
            for (int i = 0; i <= 9; i++) {

                // to convert int to string
                String str1 = Integer.toString(i);

                if (password.contains(str1)) {
                    count = 1;
                }
            }
            if (count == 0) {
                return false;
            }
        }

        // for special characters
        if (!(password.contains("@") || password.contains("#")
                || password.contains("!") || password.contains("~")
                || password.contains("$") || password.contains("%")
                || password.contains("^") || password.contains("&")
                || password.contains("*") || password.contains("(")
                || password.contains(")") || password.contains("-")
                || password.contains("+") || password.contains("/")
                || password.contains(":") || password.contains(".")
                || password.contains(",") || password.contains("<")
                || password.contains(">") || password.contains("?")
                || password.contains("|"))) {
            return false;
        }

        if (true) {
            int count = 0;

            // checking capital letters
            for (int i = 65; i <= 90; i++) {

                // type casting
                char c = (char)i;

                String str1 = Character.toString(c);
                if (password.contains(str1)) {
                    count = 1;
                }
            }
            if (count == 0) {
                return false;
            }
        }

        if (true) {
            int count = 0;
            // checking small letters
            for (int i = 90; i <= 122; i++) {
                // type casting
                char c = (char)i;
                String str1 = Character.toString(c);
                if (password.contains(str1)) {
                    count = 1;
                }
            }
            if (count == 0) {
                return false;
            }
        }

        // if all conditions fail
        return true;
    }

}