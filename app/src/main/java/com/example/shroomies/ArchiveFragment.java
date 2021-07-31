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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.google.android.material.tabs.TabLayout;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ArchiveFragment extends Fragment {
    private TabLayout archiveTablayout;
    private RecyclerView expensesRecyclerview;
    private RecyclerView tasksRecyclerView;
    private RelativeLayout rootLayout;
    //data
    private ArrayList<ExpensesCard> expensesCardsArrayList;
    private ArrayList<TasksCard> tasksCardsArrayList;
    private TasksCardAdapter tasksCardAdapter;
    private ExpensesCardAdapter expensesCardAdapter;
    //firebase
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //views
        View view = inflater.inflate(R.layout.fragment_my_archive, container, false);
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(getActivity());
        return view;
    }



    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        expensesRecyclerview = v.findViewById(R.id.archive_recyclerview_expenses);
        tasksRecyclerView = v.findViewById(R.id.archive_recyclerview_task);
        archiveTablayout = v.findViewById(R.id.my_tablayout_archive);
        rootLayout = v.findViewById(R.id.archive_root_layout);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext());
        expensesRecyclerview.setHasFixedSize(true);
        expensesRecyclerview.setLayoutManager(linearLayoutManager);
        tasksRecyclerView.setHasFixedSize(true);
        tasksRecyclerView.setLayoutManager(linearLayoutManager1);

        //get the apartment data
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            //variable
            String apartmentID = bundle.getString(Config.apartmentID);
            getCards(apartmentID);
        }

        archiveTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    archiveTablayout.setSelectedTabIndicator(R.drawable.tab_indicator_left);
                    tasksRecyclerView.setVisibility(View.GONE);
                    expensesRecyclerview.setVisibility(View.VISIBLE);
                    expensesCardsArrayList = new ArrayList<>();

                } else if (tab.getPosition() == 1) {
                    archiveTablayout.setSelectedTabIndicator(R.drawable.tab_indicator_right);
                    tasksRecyclerView.setVisibility(View.VISIBLE);
                    expensesRecyclerview.setVisibility(View.GONE);
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

    private void getCards(String apartmentID) {
        JSONObject data=new JSONObject();
        JSONObject jsonObject = new JSONObject();
        tasksCardsArrayList = new ArrayList<>();
        expensesCardsArrayList = new ArrayList<>();

        try {
            jsonObject.put(Config.apartmentID,apartmentID);
            data.put(Config.data , jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FirebaseUser firebaseUser  = mAuth.getCurrentUser();
        firebaseUser.getIdToken(false).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String token = task.getResult().getToken();

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.FUNCTION_GET_ARCHIVE, data, response -> {
                        final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                        //get the task card
                        try {
                            boolean success =  response.getJSONObject(Config.result).getBoolean(Config.success);
                            if(success) {
                                JSONObject cards =response.getJSONObject(Config.result).getJSONObject(Config.cards);
                                JSONObject taskCardObject = (JSONObject) cards.get(Config.taskCards);
                                JSONObject expensesCardObject = (JSONObject)cards.get(Config.expensesCards);
                                if(expensesCardObject!=null){
                                    Iterator<String> keys = expensesCardObject.keys();
                                    while (keys.hasNext()){
                                        String key = keys.next();
                                        JSONObject expensesCard = (JSONObject) expensesCardObject.get(key);
                                        ExpensesCard expensesCardPOJO = mapper.readValue(expensesCard.toString(), ExpensesCard.class);
                                        expensesCardsArrayList.add(expensesCardPOJO);

                                    }
                                }
                                expensesCardAdapter= new ExpensesCardAdapter(expensesCardsArrayList , getActivity() , true , apartmentID , getChildFragmentManager()  , rootLayout);
                                expensesCardAdapter.notifyDataSetChanged();
                                expensesRecyclerview.setAdapter(expensesCardAdapter);

                                if(taskCardObject!=null){
                                    Iterator<String> keys = taskCardObject.keys();
                                    while (keys.hasNext()){
                                        String key = keys.next();
                                        try {
                                            JSONObject taskCard = (JSONObject) taskCardObject.get(key);

                                            TasksCard tasksCardPOJO = mapper.readValue(taskCard.toString(), TasksCard.class);

                                            tasksCardsArrayList.add(tasksCardPOJO);
                                        } catch (JSONException | JsonProcessingException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                tasksCardAdapter = new TasksCardAdapter(tasksCardsArrayList , getActivity() , true , apartmentID , getChildFragmentManager() , rootLayout);
                                tasksRecyclerView.setAdapter(tasksCardAdapter);
                                tasksCardAdapter.notifyDataSetChanged();
                            }else{
                                String message = "We encountered an error while retrieving the archive cards.";
                                displayErrorAlert(message);
                            }
                        } catch (JSONException | JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }, error -> {
                        String message = null; // error message, show it in toast or dialog, whatever you want
                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                            message = "Cannot connect to Internet";
                        } else if (error instanceof ServerError) {
                            message = "Server error. Please try again later";
                        }  else if (error instanceof ParseError) {
                            message = "Parsing error! Please try again later";
                        }
                        displayErrorAlert(message);
                    }) {
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
        });

    }
    void displayErrorAlert(String message){
        new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_alert)
                .setTitle("Error")
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("return", (dialog, which) -> {
                    getActivity().finish();
                    dialog.dismiss(); })
                .create()
                .show();
    }
}