package com.example.shroomies;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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


public class RequestFragment extends Fragment {
   private View v;
   private RecyclerView requestRecyclerView;
   private RelativeLayout rootLayout;
   private FirebaseAuth mAuth;
   private TabLayout requestTab;
   private ArrayList<User> senderUsers;
   private ArrayList<User> receiverUsers;
   private String apartmentID;

   private RequestQueue requestQueue;

   private RecyclerView invitationRecyclerView;

   private RequestAdapter requestAdapter;
   private RequestAdapter invitationAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_my_requests, container, false);
        mAuth=FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(getActivity());
    return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestRecyclerView = v.findViewById(R.id.request_recyclerview);
        requestTab = v.findViewById(R.id.request_tablayout);
        invitationRecyclerView=v.findViewById(R.id.invitation_recyclerview);
        rootLayout = v.findViewById(R.id.request_fragment_root_layout);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        requestRecyclerView.setHasFixedSize(true);
        requestRecyclerView.setLayoutManager(linearLayoutManager);

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext());
        invitationRecyclerView.setLayoutManager(linearLayoutManager1);
        invitationRecyclerView.setHasFixedSize(true);


        requestTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    requestTab.setSelectedTabIndicator(R.drawable.tab_indicator_left);
                    invitationRecyclerView.setVisibility(View.VISIBLE);
                    requestRecyclerView.setVisibility(View.GONE);
                }
                else if(tab.getPosition()==1){
                    requestTab.setSelectedTabIndicator(R.drawable.tab_indicator_right);
                    invitationRecyclerView.setVisibility(View.GONE);
                    requestRecyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        getToken();


    }

    private void getRequests(String token) {
        senderUsers = new ArrayList<>();
        receiverUsers = new ArrayList<>();

        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            jsonObject.put("userID" , mAuth.getCurrentUser().getUid());
            data.put("data" , jsonObject);
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
                if(success){
                    JSONArray requests =response.getJSONObject(Config.result).getJSONArray(Config.requests);
                    JSONArray sentJsonArray = (JSONArray) requests.get(0);
                    JSONArray recievedJsonArray = (JSONArray) requests.get(1);
                    if(sentJsonArray!=null){
                        for(int i=0; i<sentJsonArray.length() ; i++){
                            User user = mapper.readValue(sentJsonArray.get(i).toString() , User.class);
                            senderUsers.add(user);
                        }
                        invitationAdapter= new RequestAdapter(getContext(),rootLayout, senderUsers,false,apartmentID);
                        invitationAdapter.notifyDataSetChanged();
                        invitationRecyclerView.setAdapter(invitationAdapter);

                    }else{
                        //todo display something for the user
                    }
                    if(recievedJsonArray!=null){
                        for(int i=0; i<recievedJsonArray.length() ; i++){
                            User user = mapper.readValue(recievedJsonArray.get(i).toString() , User.class);
                            receiverUsers.add(user);

                        }
                        requestAdapter= new RequestAdapter(getContext(),rootLayout , receiverUsers,true,apartmentID);
                        requestAdapter.notifyDataSetChanged();
                        requestRecyclerView.setAdapter(requestAdapter);

                    }else{
                        //todo display something for the user
                    }
                }else{
                    //todo display something for the user
                }

            } catch (JSONException | JsonProcessingException e) {
                e.printStackTrace();
            }

        }, this::displayErrorAlert){
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
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
     try {
         jsonObject.put("userID" , mAuth.getCurrentUser().getUid());
         data.put("data" , jsonObject);
     } catch (JSONException e) {
         e.printStackTrace();
     }
     FirebaseUser firebaseUser = mAuth.getCurrentUser();
     firebaseUser.getIdToken(false).addOnCompleteListener(task -> {
         if (task.isSuccessful()) {
             String token = task.getResult().getToken();
             apartmentID = (String) task.getResult().getClaims().get(Config.apartmentID);
             getRequests(token);

         }else{
             // todo display error
         }
     });


 }
    void displayErrorAlert(@Nullable VolleyError error){
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
            message = null;
        }
        new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_alert)
                .setTitle("Error")
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("return", (dialog, which) -> {
                    getActivity().finish();
                    dialog.dismiss();
                })
                .setPositiveButton("refresh", (dialog, which) -> {
                    getToken();
                })
                .create()
                .show();

    }


}