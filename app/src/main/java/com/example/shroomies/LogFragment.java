package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
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
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollStateListener;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;


public class LogFragment extends Fragment {
    private View v;
    private ArrayList<ApartmentLogs> apartmentLogs;
    private HashMap<String , User > usersMap;
    private ArrayList<String> membersIDs;
    private RecyclerView logRecycler;
    private TextView noLogsTextView;
    private LogAdapter logAdapter;
    private RequestQueue requestQueue;
    private FirebaseAuth mAuth;
    private IOverScrollDecor logDecor;
    private IOverScrollStateListener onOverPullListener;
    private boolean scrollFromTop;
    private LottieAnimationView progressbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requestQueue = Volley.newRequestQueue(getActivity());
        mAuth = FirebaseAuth.getInstance();
        v = inflater.inflate(R.layout.fragment_log, container, false);
        return v;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logRecycler=v.findViewById(R.id.log_recycler);
        noLogsTextView = v.findViewById(R.id.no_logs_text_view);
        Toolbar toolbar = getActivity().findViewById(R.id.my_shroomies_toolbar);

        progressbar  =toolbar.findViewById(R.id.loading_progress_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        logRecycler.setHasFixedSize(true);
        logRecycler.setLayoutManager(linearLayoutManager);
        logDecor = OverScrollDecoratorHelper.setUpOverScroll(logRecycler, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        apartmentLogs =new ArrayList<>();

        toolbar.setTitle("Logs");
        toolbar.setTitleTextColor(getActivity().getColor(R.color.jetBlack));
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.findViewById(R.id.myshroomies_toolbar_logo).setVisibility(View.GONE);
        toolbar.findViewById(R.id.my_shroomies_add_card_btn).setVisibility(View.GONE);
        toolbar.setElevation(5);
        toolbar.setNavigationOnClickListener(view1 -> {
            toolbar.setTitle(null);
            toolbar.setNavigationIcon(null);
            getActivity().onBackPressed();
        });

        Bundle bundle = this.getArguments();
        if(bundle !=null){
            apartmentLogs = bundle.getParcelableArrayList("LOG_LIST");
            membersIDs = bundle.getStringArrayList("MEMBERS");
            if(apartmentLogs!=null){
                getMemberDetails(membersIDs);
            }else{
                noLogsTextView.animate().setDuration(300).alpha(1.0f);
                noLogsTextView.setVisibility(View.VISIBLE);
            }
        }

        onOverPullListener = (decor, oldState, newState) -> {
            if(oldState== 1){
                scrollFromTop=true;
            }

            if (newState == 0 && scrollFromTop) {
                //fetch new data when over scrolled from top
                // remove the listener to prevent the user from over scrolling
                // again while the data is still being fetched
                //the listener will be set again when the data has been retrieved
                logDecor.setOverScrollUpdateListener(null);
                scrollFromTop=false;
                getLogs();
            }

        };
        logDecor.setOverScrollStateListener(onOverPullListener);
    }


    void getLogs(){
        progressbar.setVisibility(View.VISIBLE);
        if(usersMap!=null){
            FirebaseUser firebaseUser = mAuth
                    .getCurrentUser();
            firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    String token = task.getResult().getToken();
                    String apartmentID = (String) task.getResult().getClaims().get(Config.apartmentID);
                    JSONObject jsonObject = new JSONObject();
                    JSONObject data = new JSONObject();
                    try {
                        jsonObject.put(Config.apartmentID , apartmentID);
                        data.put(Config.data , jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_GET_LOGS, data, response -> {

                        try {
                            JSONObject result = response.getJSONObject(Config.result);
                            boolean success = result.getBoolean(Config.success);

                            if(success){

                                final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                                mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                                apartmentLogs = new ArrayList<>();

                                JSONArray logsJsonArray = result.getJSONArray(Config.logs);
                                for (int i = 0; i < logsJsonArray.length(); i++) {
                                    ApartmentLogs updatedLog = mapper.readValue(((JSONObject) logsJsonArray.get(i)).toString(), ApartmentLogs.class);
                                    apartmentLogs.add(updatedLog);
                                }
                                Collections.reverse(apartmentLogs);
                                if (getContext() != null) {
                                    logAdapter = new LogAdapter(getActivity(), apartmentLogs, usersMap, getParentFragmentManager(), getTargetFragment());
                                    logAdapter.notifyDataSetChanged();
                                    logRecycler.setAdapter(logAdapter);
                                }

                            }else{
                                //todo display error

                            }
                            logDecor.setOverScrollStateListener(onOverPullListener);
                            progressbar.setVisibility(View.GONE);

                        } catch (JSONException | JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }, error -> {

                    }
                    )
                    {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<>();
                            params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                            params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
                            return params;
                        }
                    };



                    requestQueue.add(jsonObjectRequest);


                }
            });
        }


    }
    private void getMemberDetails(ArrayList<String> members) {

        FirebaseUser firebaseUser = mAuth
                .getCurrentUser();
        firebaseUser.getIdToken(true)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        String token = task.getResult().getToken();
                        JSONObject jsonObject=new JSONObject();
                        JSONObject data = new JSONObject();
                        JSONArray jsonArray = new JSONArray(members);
                        try {
                            jsonObject.put(Config.membersID,jsonArray);
                            data.put(Config.data , jsonObject);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            return;
                        }
                        usersMap = new HashMap<>();
                        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.POST,
                                Config.URL_GET_MEMBER_DETAIL,
                                data,
                                response -> {
                                    try {
                                        JSONObject result = response.getJSONObject(Config.result);
                                        boolean success = result.getBoolean(Config.success);
                                        if (success) {
                                            final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                                            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                                            JSONArray users = result.getJSONArray(Config.members);
                                            if (users != null) {
                                                for (int i = 0; i < users.length(); i++) {

                                                    User user = mapper.readValue(((JSONObject) users.get(i)).toString(), User.class);
                                                    usersMap.put(user.getUserID(), user);

                                                }
                                                Collections.reverse(apartmentLogs);
                                                logAdapter = new LogAdapter(getContext(), apartmentLogs, usersMap, getParentFragmentManager(), getTargetFragment());
                                                logRecycler.setAdapter(logAdapter);
                                                logAdapter.notifyDataSetChanged();
                                            }

                                        } else {

                                            String title = "Unexpected error";
                                            String message = "We have encountered an unexpected error, try to check your internet connection and log in again.";
                                            displayErrorAlert(title, null, message);

                                        }
                                    } catch (JSONException | JsonProcessingException e) {
                                        e.printStackTrace();
                                    }

                                }, error -> {

                                    displayErrorAlert("Error" ,error, null);
                                })
                        {
                            @Override
                            public Map<String, String> getHeaders() {
                                Map<String, String> params = new HashMap<>();
                                params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                                params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
                                return params;
                            }
                        };
                        requestQueue.add(jsonObjectRequest);

                    }else{
                        String title = "Authentication error";
                        String message = "We encountered a problem while authenticating your account";
                        displayErrorAlert(title ,null, message);
                    }
        });
    }



    void displayErrorAlert(String title  , VolleyError error , String errorMessage){
        String message = null; // error message, show it in toast or dialog, whatever you want
        if(error!=null) {
            if (error instanceof NetworkError ||
                    error instanceof AuthFailureError ||
                    error instanceof NoConnectionError ||
                    error instanceof TimeoutError) {
                message = "Cannot connect to Internet";
            } else if (error instanceof ServerError) {
                message = "The server could not be found. Please try again later";
            } else if (error instanceof ParseError) {
                message = "Parsing error! Please try again later";
            }
        }else{
            message = errorMessage;
        }
        new android.app.AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_alert)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("return", (dialog, which) -> LogFragment.this.getActivity().onBackPressed())
                .setPositiveButton("refresh", (dialog, which) -> getMemberDetails(membersIDs))
                .create()
                .show();
    }
}



