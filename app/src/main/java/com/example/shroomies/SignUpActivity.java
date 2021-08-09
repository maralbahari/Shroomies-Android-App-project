package com.example.shroomies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.L;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.virgilsecurity.android.common.callback.OnGetTokenCallback;
import com.virgilsecurity.android.common.callback.OnKeyChangedCallback;
import com.virgilsecurity.android.common.model.EThreeParams;
import com.virgilsecurity.android.ethree.interaction.EThree;
import com.virgilsecurity.common.callback.OnResultListener;
import com.virgilsecurity.common.model.Data;
import com.virgilsecurity.sdk.crypto.KeyPairType;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import kotlin.Function;
import kotlin.jvm.functions.Function0;

public class SignUpActivity extends AppCompatActivity{
    private EditText name;
    private EditText email;
    private EditText password;
    private EditText confirmpw;
    private Button register;
    private DatabaseReference mRootref;
    private FirebaseAuth mAuth;
    CustomLoadingProgressBar pd;
    SessionManager sessionManager;
    public static EThree eThree;
    private  String token ="";
   private String apartmentID;
   private RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        name = findViewById(R.id.fullname);
        email = findViewById(R.id.emailid);
        password = findViewById(R.id.password);
        confirmpw = findViewById(R.id.confirmpw);
        register = findViewById(R.id.registerbt);
        FirebaseDatabase.getInstance().useEmulator("10.0.2.2",9000);
        mRootref = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        mAuth.useEmulator("10.0.2.2",9099);
        pd = new CustomLoadingProgressBar(SignUpActivity.this , "Creating account... " , R.raw.loading_animation);

        Boolean isEnabled;

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtName = name.getText().toString();
                String txtEmail = email.getText().toString().trim();
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
                        registerUser(txtName , txtEmail , txtPass, txtConfpw);
                }
            }
        });

    }



    private void registerUser(final String name, final String email, String password, String confirmpw) {
        pd.show();
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
                    pd.dismiss();
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                    Toast.makeText(SignUpActivity.this, name+", you are a Shroomie now", Toast.LENGTH_SHORT).show();
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
    private void sendEmailVerification(){
        final FirebaseUser user = mAuth.getCurrentUser();
         user.sendEmailVerification().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                findViewById(R.id.registerbt).setEnabled(true);
                if(task.isSuccessful()){

                    Toast.makeText(getApplicationContext(), "Registered Successfully. Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                    getE3Token();
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    startActivity(intent);
                    Toast.makeText(SignUpActivity.this, name+", you are a Shroomie now", Toast.LENGTH_SHORT).show();
                    //get public and private keys for Virgil e3 kit
                }
                else {
                    Log.e("Register", "Send Email verification failed!",task.getException());
                    Toast.makeText(getApplicationContext(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
                }
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
        if (pd != null) {
            pd.dismiss();
            pd = null;
        }
    }



}
