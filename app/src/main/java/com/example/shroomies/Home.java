package com.example.shroomies;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.virgilsecurity.android.ethree.interaction.EThree;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class Home extends Application {
    FirebaseAuth mAuth;
    RequestQueue requestQueue;
    @Override
    public void onCreate() {
        super.onCreate();
        mAuth=FirebaseAuth.getInstance();
        FirebaseUser user=mAuth.getCurrentUser();
        if(user!=null && user.isEmailVerified()){
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            Intent intent= new Intent(getApplicationContext(),MainActivity.class);
            intent.putExtra("ID",user.getUid());
            intent.putExtra("EMAIL",user.getEmail());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getE3Token();
        }
    }

    private void getE3Token() {

        JSONObject data = new JSONObject();
        try {
            data.put(Config.data , "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String token = task.getResult().getToken();
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_GET_VIRGIL_JWT, data, response -> {
                    Log.d("ethree register ", response.toString());
                    try {
                        JSONObject result = response.getJSONObject(Config.result);
                        if(!result.isNull(Config.token)){
                            String ethreeToken  = result.getString(Config.token);
                            EThree eThree = EthreeSingleton.getInstance(getApplicationContext(),mAuth.getCurrentUser().getUid() , ethreeToken).getEthreeInstance();
                            eThree.register().addCallback(new com.virgilsecurity.common.callback.OnCompleteListener() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(getApplicationContext() , "yayyyy" , Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onError(@NotNull Throwable throwable) {
                                    Log.d( "boooo" , throwable.getMessage());
                                }
                            });

                        }else{
                            //todo handle error
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        //todo handle error
                    }


                }, error -> Log.d("ethree register ", error.toString()))
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
