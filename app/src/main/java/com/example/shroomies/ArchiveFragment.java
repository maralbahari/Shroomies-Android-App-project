package com.example.shroomies;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollStateListener;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class ArchiveFragment extends Fragment {
    private TabLayout archiveTablayout;
    private RecyclerView expensesRecyclerview;
    private RecyclerView tasksRecyclerView;
    private RelativeLayout rootLayout;
    private TextView noArchiveCardsTextView;
    //data
    private ArrayList<ExpensesCard> expensesCardsArrayList;
    private ArrayList<TasksCard> tasksCardsArrayList;
    private TasksCardAdapter tasksCardAdapter;
    private ExpensesCardAdapter expensesCardAdapter;
    private IOverScrollDecor expensesDecor;
    private IOverScrollDecor tasksDecor;
    private IOverScrollStateListener onOverPullListener;
    //firebase
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;
    //values
    private boolean scrollFromTop;
    private String apartmentID;
    private HashMap<String , User > memberHashMap;



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
        noArchiveCardsTextView = v.findViewById(R.id.no_archive_text_view);
        Toolbar toolbar =getActivity().findViewById(R.id.my_shroomies_toolbar);

        //get the apartment data
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            //variable
            apartmentID = bundle.getString(Config.apartmentID);
            memberHashMap = (HashMap<String, User>) bundle.getSerializable(Config.members);
            getCards(apartmentID);
        }

        toolbar.setTitle("Archive");
        toolbar.setTitleTextColor(getActivity().getColor(R.color.jetBlack));
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.findViewById(R.id.myshroomies_toolbar_logo).setVisibility(View.GONE);
        toolbar.findViewById(R.id.my_shroomies_add_card_btn).setVisibility(View.GONE);
        toolbar.setNavigationOnClickListener(view1 -> {
            toolbar.setTitle(null);
            toolbar.setNavigationIcon(null);
            getActivity().onBackPressed();
        });
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext());
        expensesRecyclerview.setHasFixedSize(true);
        expensesRecyclerview.setLayoutManager(linearLayoutManager);
        tasksRecyclerView.setHasFixedSize(true);
        tasksRecyclerView.setLayoutManager(linearLayoutManager1);
        expensesDecor = OverScrollDecoratorHelper.setUpOverScroll(expensesRecyclerview, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        tasksDecor = OverScrollDecoratorHelper.setUpOverScroll(tasksRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        //fetch new data when over scrolled from top
        // remove the listener to prevent the user from over scrolling
        // again while the data is still being fetched
        //the listener will be set again when the data has been retrieved
         onOverPullListener = (decor, oldState, newState) -> {
            if (oldState == 1) {
                scrollFromTop = true;
            }

            if (newState == 0 && scrollFromTop) {
                //fetch new data when over scrolled from top
                // remove the listener to prevent the user from over scrolling
                // again while the data is still being fetched
                //the listener will be set again when the data has been retrieved
                expensesDecor.setOverScrollUpdateListener(null);
                tasksDecor.setOverScrollUpdateListener(null);
                scrollFromTop = false;
                getCards(apartmentID);
            }
        };



        archiveTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    archiveTablayout.setSelectedTabIndicator(R.drawable.tab_indicator_left);
                    tasksRecyclerView.setVisibility(View.GONE);
                    expensesRecyclerview.setVisibility(View.VISIBLE);
                    if(expensesCardsArrayList.isEmpty()){
                      noArchiveCardsTextView.setVisibility(View.VISIBLE);
                    }else{
                        noArchiveCardsTextView.setVisibility(View.GONE);
                    }

                } else if (tab.getPosition() == 1) {
                    archiveTablayout.setSelectedTabIndicator(R.drawable.tab_indicator_right);
                    tasksRecyclerView.setVisibility(View.VISIBLE);
                    expensesRecyclerview.setVisibility(View.GONE);
                    if(tasksCardsArrayList.isEmpty()){
                        noArchiveCardsTextView.setVisibility(View.VISIBLE);
                    }else{
                        noArchiveCardsTextView.setVisibility(View.GONE);
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
                                JSONObject expensesCardObject;
                                JSONObject taskCardObject;
                                JSONObject cards =response.getJSONObject(Config.result).getJSONObject(Config.cards);
                                Log.d("archive" , cards.toString());


                                if(!cards.isNull(Config.expensesCards)){
                                    expensesCardObject = cards.getJSONObject(Config.expensesCards);
                                    if(expensesCardObject.length()!=0){
                                        Iterator<String> keys = expensesCardObject.keys();
                                        while (keys.hasNext()){
                                            String key = keys.next();
                                            JSONObject expensesCard = expensesCardObject.getJSONObject(key);
                                            ExpensesCard expensesCardPOJO = mapper.readValue(expensesCard.toString(), ExpensesCard.class);
                                            expensesCardsArrayList.add(expensesCardPOJO);
                                        }
                                    }else{
                                        noArchiveCardsTextView.setVisibility(View.VISIBLE);
                                    }

                                }else{
                                    noArchiveCardsTextView.setVisibility(View.VISIBLE);
                                }

                                expensesCardAdapter= new ExpensesCardAdapter(expensesCardsArrayList , getActivity() , true , apartmentID , getChildFragmentManager()  , rootLayout , memberHashMap);
                                expensesCardAdapter.notifyDataSetChanged();
                                expensesRecyclerview.setAdapter(expensesCardAdapter);


                                if(!cards.isNull(Config.taskCards)){
                                    taskCardObject = cards.getJSONObject(Config.taskCards);
                                    if(taskCardObject.length()!=0){
                                        Iterator<String> keys = taskCardObject.keys();
                                        while (keys.hasNext()){
                                            String key = keys.next();
                                            JSONObject taskCard = taskCardObject.getJSONObject(key);
                                            TasksCard tasksCardPOJO = mapper.readValue(taskCard.toString(), TasksCard.class);
                                            tasksCardsArrayList.add(tasksCardPOJO);
                                        }
                                    }
                                }

                                tasksCardAdapter = new TasksCardAdapter(tasksCardsArrayList , getActivity() , true , apartmentID , getChildFragmentManager() , rootLayout , memberHashMap);
                                tasksRecyclerView.setAdapter(tasksCardAdapter);
                                tasksCardAdapter.notifyDataSetChanged();
                                tasksDecor.setOverScrollStateListener(onOverPullListener);
                                expensesDecor.setOverScrollStateListener(onOverPullListener);

                            }else{
                                String message = "We encountered an error while retrieving the archive cards.";
                                displayErrorAlert(message);
                            }
                        } catch (JSONException | JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }, error -> {
                        String message = null; // error message, show it in toast or dialog, whatever you want
                        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError) {
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
        tasksDecor.setOverScrollStateListener(onOverPullListener);
        expensesDecor.setOverScrollStateListener(onOverPullListener);
        new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_alert)
                .setTitle("Error")
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("return", (dialog, which) -> {
                    if(getActivity()!=null){
                        getActivity().finish();
                    }
                    dialog.dismiss(); })
                .create()
                .show();
    }
}