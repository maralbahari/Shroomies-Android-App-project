package com.example.shroomies;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;


public class RequestActivity extends AppCompatActivity {
    //views
   private RecyclerView requestRecyclerView, invitationRecyclerView;
   private RelativeLayout  noSentRequestsLayout , noReceivedRequestsLayout;
   private RelativeLayout rootLayout;
   private LottieAnimationView catAnimationView;
    //firebase
   private FirebaseAuth mAuth;
   private RequestQueue requestQueue;
   //data
   private RequestAdapter requestAdapter;
   private RequestAdapter invitationAdapter;
   private ArrayList<User> senderUsers;
   private ArrayList<User> receiverUsers;
   private TabLayout tabLayout;
   private Toolbar reqToolbar;

    //variables
   private String apartmentID;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.request_activity);
        mAuth=FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestRecyclerView =findViewById(R.id.request_recyclerview);
        tabLayout=findViewById(R.id.tab_layout_req);
        invitationRecyclerView=findViewById(R.id.invitation_recyclerview);
        rootLayout = findViewById(R.id.request_fragment_root_layout);
        noSentRequestsLayout = findViewById(R.id.no_sent_request_Layout);
        catAnimationView = findViewById(R.id.empty_animation_req);
        MaterialButton goToMyShroomiesButton = findViewById(R.id.go_to_shroomies_button);
//        Todo add go to publish post
        reqToolbar=findViewById(R.id.request_toolbar);
        setSupportActionBar(reqToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        noReceivedRequestsLayout = findViewById(R.id.no_received_request_Layout);
        goToMyShroomiesButton.setOnClickListener(v -> startActivity(new Intent(this, MyShroomiesActivity.class)));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext() , RecyclerView.VERTICAL,false);
        requestRecyclerView.setHasFixedSize(true);
        requestRecyclerView.setLayoutManager(linearLayoutManager);

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL , false);
        invitationRecyclerView.setLayoutManager(linearLayoutManager1);
        invitationRecyclerView.setHasFixedSize(true);

        OverScrollDecoratorHelper.setUpOverScroll(invitationRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        OverScrollDecoratorHelper.setUpOverScroll(requestRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        getToken();
        Objects.requireNonNull(tabLayout.getTabAt(0)).select();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    noReceivedRequestsLayout.setVisibility(View.GONE);
                    noSentRequestsLayout.setVisibility(View.GONE);
                    requestRecyclerView.setVisibility(View.GONE);
                    invitationRecyclerView.setVisibility(View.VISIBLE);
                    catAnimationView.pauseAnimation();
                    catAnimationView.setVisibility(View.GONE);
                    if (senderUsers!=null){
                        if(senderUsers.isEmpty()){
                            invitationRecyclerView.setVisibility(View.GONE);
                            requestRecyclerView.setVisibility(View.GONE);
                            noSentRequestsLayout.setVisibility(View.GONE);
                            noReceivedRequestsLayout.setVisibility(View.VISIBLE);
                            catAnimationView.playAnimation();
                            catAnimationView.setVisibility(View.VISIBLE);
                        }
                    }
                } if (tab.getPosition()==1) {
                    noReceivedRequestsLayout.setVisibility(View.GONE);
                    noSentRequestsLayout.setVisibility(View.GONE);
                    invitationRecyclerView.setVisibility(View.GONE);
                    requestRecyclerView.setVisibility(View.VISIBLE);
                    catAnimationView.pauseAnimation();
                    catAnimationView.setVisibility(View.GONE);
                    if (receiverUsers!=null){
                        if(receiverUsers.isEmpty()){
                            requestRecyclerView.setVisibility(View.GONE);
                            invitationRecyclerView.setVisibility(View.GONE);
                            noReceivedRequestsLayout.setVisibility(View.GONE);
                            noSentRequestsLayout.setVisibility(View.VISIBLE);
                            catAnimationView.playAnimation();
                            catAnimationView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    private void getRequests(String token, String userUid) {
        senderUsers = new ArrayList<>();
        receiverUsers = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            jsonObject.put(Config.userID , userUid);
            data.put(Config.data , jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_GET_REQUESTS, data, response -> {
            // response is a json array containing  two json arrays
            // the first array contains the user objects of the
            // requests sent to this user
            // the second contains user objects of that the current
            // user has sent requests to
            final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

            try {
                boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                if (success) {
                    JSONArray requests = response.getJSONObject(Config.result).getJSONArray(Config.requests);
                    Log.d("requests" , requests.toString());
                    JSONArray receivedJsonArray = requests.getJSONArray(0);
                    JSONArray sentJsonArray =requests.getJSONArray(1);
                    if (receivedJsonArray != null) {
                        if(receivedJsonArray.length()>0){
                            for (int i = 0; i < receivedJsonArray.length(); i++) {
                                User user = mapper.readValue(receivedJsonArray.get(i).toString(), User.class);
                                senderUsers.add(user);
                            }
                            invitationAdapter = new RequestAdapter(getApplicationContext(), rootLayout, senderUsers, false, apartmentID);
                            invitationAdapter.notifyDataSetChanged();
                            invitationRecyclerView.setAdapter(invitationAdapter);
                        }else{
                            if (tabLayout.getSelectedTabPosition()==0){
                                invitationRecyclerView.setVisibility(View.GONE);
                                requestRecyclerView.setVisibility(View.GONE);
                                noSentRequestsLayout.setVisibility(View.GONE);
                                noReceivedRequestsLayout.setVisibility(View.VISIBLE);
                                catAnimationView.playAnimation();
                                catAnimationView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    if (sentJsonArray != null) {
                        if(sentJsonArray.length()>0){
                            for (int i = 0; i < sentJsonArray.length(); i++) {
                                User user = mapper.readValue(sentJsonArray.get(i).toString(), User.class);
                                receiverUsers.add(user);
                            }
                            requestAdapter = new RequestAdapter(getApplicationContext(), rootLayout, receiverUsers, true, apartmentID);
                            requestAdapter.notifyDataSetChanged();
                            requestRecyclerView.setAdapter(requestAdapter);

                        }else{
                            if (tabLayout.getSelectedTabPosition()==1) {
                                requestRecyclerView.setVisibility(View.GONE);
                                invitationRecyclerView.setVisibility(View.GONE);
                                noReceivedRequestsLayout.setVisibility(View.GONE);
                                noSentRequestsLayout.setVisibility(View.VISIBLE);
                                catAnimationView.playAnimation();
                                catAnimationView.setVisibility(View.VISIBLE);
                            }
                        }

                    }
                } else {
                    String message = "An Unexpected error occurred while performing your request";
                    displayErrorAlert(null , message);
                }

            } catch (JSONException | JsonProcessingException e) {
                e.printStackTrace();
            }

        }, error -> displayErrorAlert(error , null)){
            @Override
            public Map<String, String> getHeaders()  {
                Map<String, String> params = new HashMap<>();
                params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
                return params;
            }
        };
        requestQueue.add(jsonObjectRequest);

    }
 void getToken(){
     FirebaseUser firebaseUser = mAuth.getCurrentUser();
     if (firebaseUser!=null) {
         firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
             if (task.isSuccessful()) {
                 String token = task.getResult().getToken();
                 apartmentID = (String) task.getResult().getClaims().get(Config.apartmentID);
                 getRequests(token,firebaseUser.getUid());

             }else{
                 // todo display error
             }
         });
     }


 }
    void displayErrorAlert(@Nullable VolleyError error , String errorMessage){
        String message = null; // error message, show it in toast or dialog, whatever you want
        if(error!=null) {
            if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                message = "Cannot connect to Internet";
            } else if (error instanceof ServerError) {
                message = "Server error. Please try again later";
            } else if (error instanceof ParseError) {
                message = "Parsing error! Please try again later";
            }
        }else{
            message = errorMessage;
        }
        new AlertDialog.Builder(getApplicationContext())
                .setIcon(R.drawable.ic_alert)
                .setTitle("Error")
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("return", (dialog, which) -> {
                    this.finish();
                    dialog.dismiss();
                })
                .setPositiveButton("refresh", (dialog, which) -> getToken())
                .create()
                .show();

    }
    @Override
    protected void onStart() {
        super.onStart();
        getToken();
    }
}