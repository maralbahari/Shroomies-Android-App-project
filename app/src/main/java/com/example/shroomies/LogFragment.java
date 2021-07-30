package com.example.shroomies;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import com.factor.bouncy.BouncyRecyclerView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class LogFragment extends Fragment {
    private View v;
    private ArrayList<apartmentLogs> apartmentLogs;
    private ArrayList<String> membersIDs;
    private BouncyRecyclerView logRecycler;
    private Bundle bundle;
    private LogAdapter logAdapter;
    private RequestQueue requestQueue;
    private FirebaseAuth mAuth;

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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        logRecycler.setHasFixedSize(true);
        logRecycler.setLayoutManager(linearLayoutManager);
        apartmentLogs =new ArrayList<>();

        Toolbar toolbar =getActivity().findViewById(R.id.my_shroomies_toolbar);
        toolbar.setTitle("Logs");
        toolbar.setTitleTextColor(getActivity().getColor(R.color.jetBlack));
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setElevation(5);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar.setTitle(null);
                toolbar.setNavigationIcon(null);
                getActivity().onBackPressed();
            }
        });


        bundle=this.getArguments();
        if(bundle!=null){
            apartmentLogs = bundle.getParcelableArrayList("LOG_LIST");
            membersIDs = bundle.getStringArrayList("MEMBERS");
            if(apartmentLogs!=null){
                getMemberDetails(membersIDs);
            }else{
                //todo display  empty logs
            }
        }
    }

    private void getMemberDetails(ArrayList<String> members) {
        JSONObject jsonObject=new JSONObject();
        JSONObject data = new JSONObject();
        JSONArray jsonArray = new JSONArray(members);

        try {
            jsonObject.put(Config.membersID,jsonArray);
            data.put(Config.data , jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
//
//        FirebaseFunctions mfunc = FirebaseFunctions.getInstance();
//        mfunc.useEmulator("10.0.2.2",5001);
        FirebaseUser firebaseUser = mAuth
                .getCurrentUser();
        firebaseUser.getIdToken(false)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        HashMap<String , User > usersMap = new HashMap();
                        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.POST, Config.FUNCTION_GET_MEMBER_DETAIL, data, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                                mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                                JSONArray users = null;
                                try {
                                    users = (JSONArray) response.get(Config.result);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if(users!=null) {
                                    for (int i = 0; i < users.length(); i++) {
                                        User user = null;
                                        try {
                                            user = mapper.readValue(((JSONObject) users.get(i)).toString(), User.class);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }

                                        usersMap.put(user.getUserID(), user);
                                        Log.d( "logs user" , usersMap.toString());
                                    }

                                    logAdapter = new LogAdapter(getContext(), apartmentLogs, usersMap, getParentFragmentManager(), getTargetFragment());
                                    logRecycler.setAdapter(logAdapter);
                                    logAdapter.notifyDataSetChanged();
                                }
                            }
                        }, error -> {
                            displayErrorAlert("Error" ,error, null);
                        });
                        requestQueue.add(jsonObjectRequest);

                    }else{
                        String title = "Authentication error";
                        String message = "We encountered a problem while authenticating your account";
                        displayErrorAlert(title ,null, message);
                    }
                });



    }



    void displayErrorAlert(String title  , @Nullable VolleyError error , @Nullable String errorMessage){
        String message = null; // error message, show it in toast or dialog, whatever you want
        if(error!=null) {
            if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
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
                .setNeutralButton("return", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogFragment.this.getActivity().onBackPressed();
                    }
                })
                .setPositiveButton("refresh", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getMemberDetails(membersIDs);
                    }
                })
                .create()
                .show();
    }
}



