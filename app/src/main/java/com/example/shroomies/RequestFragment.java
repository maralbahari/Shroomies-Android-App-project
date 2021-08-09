package com.example.shroomies;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

import com.android.volley.Response;
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

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;


public class RequestFragment extends Fragment {
    //views
   private View v;
   private RecyclerView requestRecyclerView, invitationRecyclerView;
   private RelativeLayout  noSentRequestsLayout , noReceivedRequestsLayout;
   private LinearLayout rootLayout;
   private MaterialButton goToMyShroomiesButton;
   //firebase
   private FirebaseAuth mAuth;
   private RequestQueue requestQueue;
   //data
   private RequestAdapter requestAdapter;
   private RequestAdapter invitationAdapter;
   private ArrayList<User> senderUsers;
   private ArrayList<User> receiverUsers;

    //variables
   private String apartmentID;
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

        invitationRecyclerView=v.findViewById(R.id.invitation_recyclerview);
        rootLayout = v.findViewById(R.id.request_fragment_root_layout);
        noSentRequestsLayout = v.findViewById(R.id.no_sent_request_Layout);
        goToMyShroomiesButton = v.findViewById(R.id.go_to_shroomies_button);
        noReceivedRequestsLayout = v.findViewById(R.id.no_received_request_Layout);

        goToMyShroomiesButton.setOnClickListener(v -> startActivity(new Intent(this.getActivity(), MyShroomiesActivity.class)));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext() , RecyclerView.HORIZONTAL,false);
        requestRecyclerView.setHasFixedSize(true);
        requestRecyclerView.setLayoutManager(linearLayoutManager);

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext() , RecyclerView.HORIZONTAL , false);
        invitationRecyclerView.setLayoutManager(linearLayoutManager1);
        invitationRecyclerView.setHasFixedSize(true);

        OverScrollDecoratorHelper.setUpOverScroll(invitationRecyclerView, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);
        OverScrollDecoratorHelper.setUpOverScroll(requestRecyclerView, OverScrollDecoratorHelper.ORIENTATION_HORIZONTAL);


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
                            invitationAdapter = new RequestAdapter(getContext(), rootLayout, senderUsers, false, apartmentID);
                            invitationAdapter.notifyDataSetChanged();
                            invitationRecyclerView.setAdapter(invitationAdapter);
                        }else{
                            invitationRecyclerView.setVisibility(View.GONE);
                            noReceivedRequestsLayout.setVisibility(View.VISIBLE);
                        }
                    }

                    if (sentJsonArray != null) {
                        if(sentJsonArray.length()>0){
                            for (int i = 0; i < sentJsonArray.length(); i++) {
                                User user = mapper.readValue(sentJsonArray.get(i).toString(), User.class);
                                receiverUsers.add(user);
                            }
                            requestAdapter = new RequestAdapter(getContext(), rootLayout, receiverUsers, true, apartmentID);
                            requestAdapter.notifyDataSetChanged();
                            requestRecyclerView.setAdapter(requestAdapter);

                        }else{
                            requestRecyclerView.setVisibility(View.GONE);
                            noSentRequestsLayout.setVisibility(View.VISIBLE);
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
     firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
         if (task.isSuccessful()) {
             String token = task.getResult().getToken();
             apartmentID = (String) task.getResult().getClaims().get(Config.apartmentID);
             getRequests(token);

         }else{
             // todo display error
         }
     });


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