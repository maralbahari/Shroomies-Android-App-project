package com.example.shroomies;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewChatFragment extends DialogFragment {
    private View v;
    private RequestQueue requestQueue;
    private FirebaseAuth mAuth;
    private SearchView searchView;
    private RecyclerView recyclerView;
    private LottieAnimationView lottieAnimationView;
    private ArrayList<User>suggestedUser;
    private SearchUserRecyclerViewAdapter searchUserRecyclerViewAdapter;
    private RelativeLayout rootLayout;
    private MaterialButton doneButton;



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.new_chat_background);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);

        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        requestQueue= Volley.newRequestQueue(getActivity());
        mAuth = FirebaseAuth.getInstance();
        v = inflater.inflate(R.layout.fragment_new_chat, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchView = v.findViewById(R.id.search_user);
        lottieAnimationView = v.findViewById(R.id.search_user_progress_view);
        recyclerView = v.findViewById(R.id.new_chat_recyclerview);
        rootLayout = v.findViewById(R.id.root_layout);
        doneButton = v.findViewById(R.id.new_chat_done);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                searchView.clearFocus();
                searchUsers(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        doneButton.setOnClickListener(v -> dismiss());


    }

    private void searchUsers(String query) {
        suggestedUser = new ArrayList<>();
        lottieAnimationView.setVisibility(View.VISIBLE);
        if(!query.trim().isEmpty()){
            FirebaseUser firebaseUser = mAuth
                    .getCurrentUser();
            firebaseUser.getIdToken(true)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            String token = task.getResult().getToken();
                            suggestedUser = new ArrayList<>();
                            JSONObject jsonObject = new JSONObject();
                            JSONObject data =  new JSONObject();
                            try {
                                jsonObject.put(Config.name, query.trim());
                                jsonObject.put(Config.currentUser, mAuth.getCurrentUser().getUid());
                                data.put(Config.data , jsonObject);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_SEARCH_USERS, data, response -> {
                                try {
                                    JSONObject result = response.getJSONObject(Config.result);
                                    boolean success = result.getBoolean(Config.success);
                                    if(success){
                                        JSONArray userArray = result.getJSONArray(Config.message);
                                        //the result is a json object containing
                                        //json array of json arrays
                                        //the nested json array contains 2 indices
                                        // first index is the user object
                                        // and the second  index is a boolean value
                                        //indicating whether the current user has already
                                        // sent a request to the searched user
                                        final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                                        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                                        for(int i= 0 ;i< userArray.length();i++){
                                            try {
                                                JSONArray jsonArray =((JSONArray)(userArray.get(i)));
                                                //cast the first index as a json object and the second as a boolean value
                                                JSONObject userJsonObject = (JSONObject) jsonArray.get(0);
                                                boolean requestSent = (boolean)jsonArray.get(1);
                                                User user =mapper.readValue(userJsonObject.toString() , User.class);
                                                user.setRequestSent(requestSent);
                                                suggestedUser.add(user);

                                            } catch (JSONException | JsonProcessingException e) {
                                                e.printStackTrace();
                                            }        lottieAnimationView.setVisibility(View.GONE);

                                        }
                                        if(suggestedUser.size()!=0){
                                            searchUserRecyclerViewAdapter  = new SearchUserRecyclerViewAdapter(getContext() , suggestedUser);
                                            searchUserRecyclerViewAdapter.setHasStableIds(true);
                                            recyclerView.setAdapter(searchUserRecyclerViewAdapter);

                                        }else{
                                            Snackbar.make(rootLayout ,"No such user found" , Snackbar.LENGTH_SHORT).show();

                                        }

                                    }else{
                                        String message = result.getString(Config.message);
                                        Snackbar.make(rootLayout ,message , Snackbar.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    lottieAnimationView.setVisibility(View.GONE);

                                }
                                lottieAnimationView.setVisibility(View.GONE);

                            }, error -> {
                                String message = null; // error message, show it in toast or dialog, whatever you want
                                if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError) {
                                    message = "Cannot connect to Internet";
                                } else if (error instanceof ServerError) {
                                    message = "The server could not be found. Please try again later";
                                }  else if (error instanceof ParseError) {
                                    message = "Parsing error! Please try again later";
                                }
                                displayErrorAlert("Error" ,message);
                            })
                            {
                                @Override
                                public Map<String, String> getHeaders() {
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                                    params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
                                    return params;
                                }
                            };
                            requestQueue.add(jsonObjectRequest);
                        }else{
                            String title = "Authentication error";
                            String message = "We encountered a problem while authenticating your account";
                            displayErrorAlert(title, message);
                        }
                    });

        }
    }

    void displayErrorAlert(String title ,  String message ){
        lottieAnimationView.setVisibility(View.GONE);
        new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_alert)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("Return", (dialog, which) -> {
                    NewChatFragment.this.getActivity().onBackPressed();
                    dialog.dismiss();
                })
                .create()
                .show();
    }

}
