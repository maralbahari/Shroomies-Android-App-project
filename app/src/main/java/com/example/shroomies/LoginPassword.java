package com.example.shroomies;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.virgilsecurity.android.ethree.interaction.EThree;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginPassword extends AppCompatActivity {
    private String bundledEmail;
    private TextInputLayout passwordEditText;
    private FirebaseAuth mAuth;
    public EThree eThree;
    private RequestQueue requestQueue;
    TextInputLayout emailEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_password);
        mAuth=FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        emailEditText = findViewById(R.id.user_email_text_view);
        passwordEditText=findViewById(R.id.password_login);
        Button forgotPasswordButton = findViewById(R.id.forgot_password_button);
        Button loginButton = findViewById(R.id.login_button);

        Toolbar toolbar = findViewById(R.id.login_password_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            bundledEmail =bundle.getString("EMAIL");
            emailEditText.getEditText().setText(bundledEmail);
        }
        loginButton.setOnClickListener(view -> {
            String enteredPassword=passwordEditText.getEditText().getText().toString().trim();
            if(enteredPassword.isEmpty()){
                passwordEditText.setError("Please enter your password");
            }else {
                login(bundledEmail,enteredPassword);
//                getE3Token(bundledEmail,enteredPassword);
            }
        });
        forgotPasswordButton.setOnClickListener(view -> startActivity(new Intent(LoginPassword.this,ResetPassword.class)));

    }
    private void login(String enteredEmail,String password){
        mAuth.signInWithEmailAndPassword(enteredEmail, password).addOnCompleteListener(task -> {
            FirebaseUser firebaseUser=mAuth.getCurrentUser();
            if(firebaseUser!=null){
                if(firebaseUser.isEmailVerified()){
                    Intent intent = new Intent(LoginPassword.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"please verify your email",Toast.LENGTH_SHORT).show();
                    firebaseUser.sendEmailVerification().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),"Kindly check your email",Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        }).addOnFailureListener(e ->{
                emailEditText.setError(" ");
                passwordEditText.setError("Wrong email or password");});
}
    private void getE3Token(String bundledEmail,String enteredPassword) {
        JSONObject data = new JSONObject();
        try {
            data.put(Config.data , "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser!=null) {
            firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    String token = task.getResult().getToken();
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_GET_VIRGIL_JWT, data, response -> {
                        try {
                            JSONObject result = response.getJSONObject(Config.result);
                            if(!result.isNull(Config.token)){
                                Log.d("ethree register ", response.toString());
                                String ethreeToken  = result.getString(Config.token);
                                eThree = EthreeSingleton.getInstance(getApplicationContext(),firebaseUser.getUid() , ethreeToken).getEthreeInstance();
                                eThree.register().addCallback(new com.virgilsecurity.common.callback.OnCompleteListener() {
                                    @Override
                                    public void onSuccess() {
                                        login(bundledEmail,enteredPassword);
                                        Toast.makeText(getApplicationContext() , "yayyyy" , Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onError(@NotNull Throwable throwable) {
                                        Log.d( "boooo" , throwable.getMessage());
                                    }
                                });

                            }else{
                                //todo handle error
                                Log.d("ethree register ", "error");

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            //todo handle error
                        }
                    }, error -> Log.d("ethree register  server error ", error.toString()))
                    {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<>();
                            params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                            params.put(HttpHeaders.AUTHORIZATION,"Bearer "+ token);
                            return params;
                        }
                    };
                    requestQueue.add(jsonObjectRequest);
                }
            });
        }
    }
}