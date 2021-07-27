package com.example.shroomies;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ArchiveFragment extends Fragment {
    //views
    private View view;
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
    private ValueEventListener expensesCardListener;
    private ValueEventListener taskCardsListener;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private FirebaseFunctions mfunc;
    private RequestQueue requestQueue;
    //variable
    private String apartmentID;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_my_archive, container, false);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        mfunc=FirebaseFunctions.getInstance();
        mfunc.useEmulator("10.0.2.2",5001);
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
//        expensesCardAdapter = new ExpensesCardAdapter(expensesCardsList, getContext(), false, apartment, getParentFragmentManager(), getView());

        //get the apartment data
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            apartmentID=bundle.getString("apartmentID");
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

//
//    private void retrieveTaskCards(String apartmentID) {
//        tasksCardsList = new ArrayList<>();
//        tasksCardAdapter = new TasksCardAdapter(tasksCardsList, getContext(), true, apartment, getParentFragmentManager(), getView());
//        ItemTouchHelper.Callback callback = new CardsTouchHelper(tasksCardAdapter);
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
//        tasksCardAdapter.setItemTouchHelper(itemTouchHelper);
//        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);
//        tasksRecyclerView.setAdapter(tasksCardAdapter);
//        taskCardsListener = rootRef.child("archive").child(apartmentID).child("tasksCards").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                tasksCardsList.clear();
//                if (snapshot.exists()) {
//                    for (DataSnapshot sp : snapshot.getChildren()) {
//                        TasksCard tasksCard = sp.getValue(TasksCard.class);
//                        tasksCardsList.add(tasksCard);
//                    }
//                    Collections.reverse(tasksCardsList);
//                    tasksCardAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }
//
//    private void retreiveExpensesCards(String apartmentID) {
//
//        expensesCardsList = new ArrayList<>();
//        expensesCardAdapter = new ExpensesCardAdapter(expensesCardsList, getContext(), true, apartment, getParentFragmentManager(), getView());
//        ItemTouchHelper.Callback callback = new CardsTouchHelper(expensesCardAdapter);
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
//        expensesCardAdapter.setItemTouchHelper(itemTouchHelper);
//        itemTouchHelper.attachToRecyclerView(expensesRecyclerview);
//        expensesRecyclerview.setAdapter(expensesCardAdapter);
//        expensesCardListener = rootRef.child("archive").child(apartmentID).child("expensesCards").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                expensesCardsList.clear();
//                if (snapshot.exists()) {
//                    for (DataSnapshot sp : snapshot.getChildren()) {
//                        ExpensesCard expensesCard = sp.getValue(ExpensesCard.class);
//                        expensesCardsList.add(expensesCard);
//                    }
//                    Collections.reverse(expensesCardsList);
//                    expensesCardAdapter.notifyDataSetChanged();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }
//
//    private void getApartmentDetails() {
////        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").addListenerForSingleValueEvent(new ValueEventListener() {
////            @Override
////            public void onDataChange(@NonNull DataSnapshot snapshot) {
////                if (snapshot.exists()) {
////                    String apartmentID = snapshot.getValue().toString();
////                    rootRef.child("apartments").child(apartmentID).addListenerForSingleValueEvent(new ValueEventListener() {
////                        @Override
////                        public void onDataChange(@NonNull DataSnapshot snapshot) {
////                            if (snapshot.exists()) {
////                                apartment = snapshot.getValue(ShroomiesApartment.class);
////                                if (expensesCardsList.isEmpty()) {
////                                    retreiveExpensesCards(apartment.getApartmentID());
////                                }
////
////                            }
////
////                        }
////
////                        @Override
////                        public void onCancelled(@NonNull DatabaseError error) {
////
////                        }
////                    });
////
////
////                }
////            }
////
////            @Override
////            public void onCancelled(@NonNull DatabaseError error) {
////
////            }
////        });
//
//
//    }
    private void getCards(String apartmentID) {
        JSONObject data=new JSONObject();
        JSONObject jsonObject = new JSONObject();
        tasksCardsArrayList = new ArrayList<>();
        expensesCardsArrayList = new ArrayList<>();

        try {
            jsonObject.put("apartmentID",apartmentID);
            data.put("data" , jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        FirebaseUser firebaseUser  = mAuth.getCurrentUser();
//        firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//            @Override
//            public void onComplete(@NonNull Task<GetTokenResult> task) {
//                if(task.isSuccessful()){
//                    String token = task.getResult().getToken();

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.FUNCTION_GET_ARCHIVE, data, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                                mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                                //get the task card
                                JSONObject taskCardObject = new JSONObject();
                                JSONObject expensesCardObject = new JSONObject();
                                JSONObject result;
                                try {
                                    result = (JSONObject)response.get("result");
                                    taskCardObject = (JSONObject) result.get("taskCard");
                                    expensesCardObject = (JSONObject)result.get("expensesCard");

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if(expensesCardObject!=null){
                                    Iterator<String> keys = expensesCardObject.keys();
                                    while (keys.hasNext()){
                                        String key = keys.next();
                                        try {
                                            JSONObject expensesCard = (JSONObject) expensesCardObject.get(key);
                                            ExpensesCard expensesCardPOJO = mapper.readValue(expensesCard.toString(), ExpensesCard.class);
                                            expensesCardsArrayList.add(expensesCardPOJO);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (JsonMappingException e) {
                                            e.printStackTrace();
                                        } catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }

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
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (JsonMappingException e) {
                                            e.printStackTrace();
                                        } catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }

                                tasksCardAdapter = new TasksCardAdapter(tasksCardsArrayList , getActivity() , true , apartmentID , getChildFragmentManager() , rootLayout);
                                tasksRecyclerView.setAdapter(tasksCardAdapter);
                                tasksCardAdapter.notifyDataSetChanged();


                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                        requestQueue.add(jsonObjectRequest);



//                }
//            }
//        });

    }
}