package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.factor.bouncy.BouncyRecyclerView;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.JsonObject;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyShroomiesFragment extends Fragment  implements LogAdapterToMyshroomies {
    //views
    private View v;
    private SlidingUpPanelLayout rootlayout;
    private Button memberButton,logButton , archiveButton;
    private TabLayout myShroomiesTablayout;
    private BouncyRecyclerView myExpensesRecyclerView,myTasksRecyclerView;
    private PowerSpinnerView shroomieSpinnerFilter;
    private ImageButton addCardButton , expandButton;
    SlidingUpPanelLayout slidingLayout;
    //data structures
    private ArrayList<TasksCard> tasksCardsList;
    private ArrayList<ExpensesCard> expensesCardsList;
    private ArrayList<apartmentLogs> apartmentLogs;
    private TasksCardAdapter tasksCardAdapter;
    private ExpensesCardAdapter expensesCardAdapter;
    //final static
    private static final int RESULT_CODE=500;
    //model
    private ShroomiesApartment apartment;
    //firebase
    private DatabaseReference rootRef;
    private FirebaseDatabase mDatabase;
    private FirebaseFunctions mfunc;
    private FirebaseAuth mAuth;
    //fragment
    private FragmentTransaction ft;
    private FragmentManager fm;
    //values
    private String selectedCardID , selectedCardType, tabSelected="expenses";
    private boolean cardFound =false;
    private int recyclerPosition=0;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_my_shroomies, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

         mDatabase= FirebaseDatabase.getInstance();
//         mDatabase.useEmulator("10.0.2.2",9000);
         rootRef=mDatabase.getReference();
         mAuth = FirebaseAuth.getInstance();
//         mAuth.useEmulator("10.0.2.2",9099);
        mfunc=FirebaseFunctions.getInstance();
//         mfunc.useEmulator("10.0.2.2",5001);

         getApartmentDetails();

        myTasksRecyclerView = v.findViewById(R.id.my_tasks_recycler_view);
        memberButton = v.findViewById(R.id.my_shroomies_member_btn);
        addCardButton = v.findViewById(R.id.my_shroomies_add_card_btn);
        logButton= v.findViewById(R.id.my_shroomies_log);
        myShroomiesTablayout = v.findViewById(R.id.my_shroomies_tablayout);
        myExpensesRecyclerView = v.findViewById(R.id.my_expenses_recycler_view);
        shroomieSpinnerFilter = v.findViewById(R.id.shroomie_spinner_filter);
        expandButton = v.findViewById(R.id.expand_button);
        rootlayout  = v.findViewById(R.id.sliding_layout);
        slidingLayout = v.findViewById(R.id.sliding_layout);
        archiveButton = v.findViewById(R.id.my_shroomies_archive_btn);

        Toolbar toolbar = getActivity().findViewById(R.id.my_shroomies_toolbar);
        toolbar.setNavigationIcon(null);
        toolbar.setTitle(null);
        toolbar.setElevation(0);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        myExpensesRecyclerView.setHasFixedSize(true);
        myExpensesRecyclerView.setLayoutManager(linearLayoutManager);


        LinearLayoutManager linearLayoutManager1 =new LinearLayoutManager(getActivity());
        myTasksRecyclerView.setHasFixedSize(true);
        myTasksRecyclerView.setLayoutManager(linearLayoutManager1);

        expensesCardsList=new ArrayList<>();
        expensesCardAdapter = new ExpensesCardAdapter(expensesCardsList,getActivity(), false,apartment,getActivity().getSupportFragmentManager(),rootlayout);
        myExpensesRecyclerView.setAdapter(expensesCardAdapter);


        tasksCardsList=new ArrayList<>();
        tasksCardAdapter=new TasksCardAdapter(tasksCardsList,getActivity(),false,apartment,getActivity().getSupportFragmentManager(),rootlayout);
        myTasksRecyclerView.setAdapter(tasksCardAdapter);

        rootlayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState.equals(SlidingUpPanelLayout.PanelState.EXPANDED)){
                    expandButton.animate().rotation(180).setDuration(100).start();

                }else{
                    expandButton.animate().rotation(0).setDuration(100).start();
                }
            }
        });


        myShroomiesTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    myShroomiesTablayout.setSelectedTabIndicator(R.drawable.tab_indicator_left);
                    myExpensesRecyclerView.setVisibility(View.VISIBLE);
                    myTasksRecyclerView.setVisibility(View.GONE);
                    tabSelected="expenses";

                }

                else if(tab.getPosition()==1){
                    myShroomiesTablayout.setSelectedTabIndicator(R.drawable.tab_indicator_right);
                    myTasksRecyclerView.setVisibility(View.VISIBLE);
                    myExpensesRecyclerView.setVisibility(View.GONE);
                    tabSelected="tasks";
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        shroomieSpinnerFilter.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener<Object>() {
            @Override
            public void onItemSelected(int i, @org.jetbrains.annotations.Nullable Object o, int i1, Object t1) {
                switch (i) {
                    case 0:
                        sortAccordingtoImportance(tabSelected);
                        break;
                    case 1:
                        sortAccordingToLatest(tabSelected);
                        break;
                    case 2:
                        sortAccordingToOldest(tabSelected);
                        break;
                    case 3:
                        sortAccordingToTitle(tabSelected);
                        break;
                }
            }
        });

        addCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AddNewCard addNewCard = new AddNewCard();
                Bundle bundle=new Bundle();

                if(myShroomiesTablayout.getSelectedTabPosition()==0){

                    bundle.putBoolean("Expenses",true);
                }else {
                    bundle.putBoolean("Expenses",false);
                }
                bundle.putParcelable("APARTMENT_DETAILS",apartment);
                addNewCard.setArguments(bundle);
                addNewCard.show(getActivity().getSupportFragmentManager(),"add new card");

            }
        });

        archiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ArchiveFragment archiveFragment = new ArchiveFragment();
                Bundle bundle=new Bundle();

                bundle.putString("apartmentID",apartment.getApartmentID());
                archiveFragment.setArguments(bundle);
                fm = getActivity().getSupportFragmentManager();
                ft = fm.beginTransaction();
                ft.addToBackStack(null);
                ft.setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN );
                ft.replace(R.id.my_shroomies_container, archiveFragment);
                ft.commit();

            }
        });
        memberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Members members=new Members();
                Bundle bundle1=new Bundle();
                bundle1.putParcelable("APARTMENT_DETAILS",apartment);
                members.setArguments(bundle1);
                fm = getActivity().getSupportFragmentManager();
                ft = fm.beginTransaction();
                ft.addToBackStack(null);
                ft.setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN );
                ft.replace(R.id.my_shroomies_container, members);
                ft.commit();
            }
        });
        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogFragment logFragment=new LogFragment();
                Bundle bundle=new Bundle();
                bundle.putParcelableArrayList("LOG_LIST", apartmentLogs);
                if(apartment.getApartmentMembers()!=null){
                    //put the members and add the admin
                    ArrayList<String> members = new ArrayList(apartment.getApartmentMembers().values());
                    members.add(apartment.getAdminID());
                    bundle.putStringArrayList("MEMBERS" ,members);
                    Log.d("members" ,  new ArrayList(apartment.getApartmentMembers().values()).toString());

                }
                logFragment.setArguments(bundle);
                logFragment.setTargetFragment(MyShroomiesFragment.this,RESULT_CODE);
                fm = getParentFragmentManager();
                ft = fm.beginTransaction();
                ft.addToBackStack(null);
                ft.setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN );
                ft.replace(R.id.my_shroomies_container, logFragment);
                ft.commit();
            }
        });
        scroll();

    }



    private void getApartmentDetails(){
//       HashMap data=new HashMap();
//       data.put("ID",mAuth.getCurrentUser().getUid());
       JSONObject jsonObject = new JSONObject();
       JSONObject data  = new JSONObject();
        try {
            jsonObject.put("ID",mAuth.getCurrentUser().getUid());
            data.put("data",jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }


//        mfunc.getHttpsCallable("callable-"+Config.FUNCTION_GET_APARTMENT_DETAILS).call(data)
//                .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
//                    @Override
//                    public void onComplete(@NonNull @NotNull Task<HttpsCallableResult> task) {
//                        if (task.isSuccessful()){
//                            final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
//                            apartment = mapper.convertValue(((HashMap<Object, Object>) task.getResult().getData()), ShroomiesApartment.class);
//                            if (apartment.getExpensesCard() != null) {
//                                if (!apartment.getExpensesCard().isEmpty()) {
//                                    expensesCardsList = new ArrayList<>(apartment.getExpensesCard().values());
//                                    expensesCardAdapter = new ExpensesCardAdapter(expensesCardsList, getActivity(), false, apartment, getChildFragmentManager(), rootlayout);
//                                    myExpensesRecyclerView.setAdapter(expensesCardAdapter);
//                                    expensesCardAdapter.notifyDataSetChanged();
//                                } else {
//                                    //TODO display empty expenses
//                                }
//                            } else {
//                                //TODO display empty expenses
//                            }
//                            if (apartment.getTaskCard() != null) {
//                                if (!apartment.getTaskCard().isEmpty()) {
//                                    tasksCardsList = new ArrayList<>(apartment.getTaskCard().values());
//                                    tasksCardAdapter = new TasksCardAdapter(tasksCardsList, getActivity(), false, apartment, getChildFragmentManager(), rootlayout);
//                                    tasksCardAdapter.notifyDataSetChanged();
//                                    myTasksRecyclerView.setAdapter(tasksCardAdapter);
//                                } else {
//                                    //TODO display empty tasks
//                                }
//                            } else {
//                                //TODO display empty tasks
//
//                                Log.d("apartment error", "eror");
//
//                            }
//                            if (apartment.getLogs() != null) {
//                                if (!apartment.getLogs().isEmpty()) {
//                                    apartmentLogs = new ArrayList<>(apartment.getLogs().values());
//                                } else {
//                                    //todo display empty log in the log fragment
//                                }
//                            } else {
//                                //todo display empty log in the log fragment
//                            }
//                    }else{
//                            Log.d("apartment error", task.getException().toString());
//                        }
//                    }
//                });


        RequestQueue requestQueue= Volley.newRequestQueue(getActivity());
        String url = "https://us-central1-shroomies-e34d3.cloudfunctions.net/getApartmentDetails";
        //TODO add progress dialog
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("apartment works" , "yayyyy");

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("apartment no works" , error.networkResponse.data.toString());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                return super.getHeaders();
            }
        };
        requestQueue.add(jsonObjectRequest);




//        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.POST, url, new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                try {
//                    response = response.replaceAll("NaN", "-1" /*Or whatever you need*/);
//
//                    JSONArray jsonArray = new JSONArray(response);
//
//                    for (int i = 0; i < response.length(); i++) {
//                        JSONObject jsonObject = jsonArray.getJSONObject(i);
//                        AppModel apps = new AppModel();
//                        try {
//                            apps.setName(jsonObject.getString("title"));
//                            apps.setDescription(jsonObject.getString("summary"));
//                            apps.setRating(jsonObject.getDouble("score"));
//                            apps.setIconURL(jsonObject.getString("icon"));
//                            apps.setAndroidVersion(jsonObject.getString("androidVersion"));
//                            apps.setVideoImage(jsonObject.getString("videoImage"));
//                            apps.setVideo(jsonObject.getString("video"));
//                            apps.setContentRating(jsonObject.getString("contentRating"));
//                            apps.setInstalls(jsonObject.getString("installs"));
//                            apps.setDeveloper(jsonObject.getString("developer"));
//                            apps.setVersion(jsonObject.getString("version"));
//                            apps.setSize(jsonObject.getString("size"));
//                            apps.setDescription(jsonObject.getString("description"));
//                            apps.setUrl(jsonObject.getString("url"));
//                            apps.setAppID(jsonObject.getString("App Id"));
//                            apps.setCluster(jsonObject.getString("cluster"));
//
////                                    //TODO change this from cloud functions
//                            String screenShotsString  = jsonObject.getString("screenShots");
//                            String str[]  =screenShotsString.substring(1, screenShotsString.length() - 1).split(",") ;
//                            List<String> screenShots = new ArrayList<>();
//                            int count  = 0;
//                            for(String x:
//                                    str){
//                                if(x.length()>10){
//                                    if(count==0){
//                                        screenShots.add(x.substring(1, x.length() - 1));
//                                    }else{
//                                        screenShots.add(x.substring(2, x.length() - 1));
//                                    }
//                                }
//                                count++;
//                            }
//                            apps.setScreenShots(screenShots);
//                            if(!appsList.contains(apps)){
//                                appsList.add(apps);
//                            }
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            refreshProgressBar.dismiss();
//
//                        }
//
//                    }
//
//                    recyclerView.setOnOverPullListener(onOverPullListener);
//                    getActivity().runOnUiThread(new Runnable() {
//                        public void run() {
////                            searchAdapter.notifyItemRangeInserted(lastAdapterPosition , searchAdapter.getItemCount());
//                            searchAdapter.notifyDataSetChanged();
//                        }
//                    });
//                    refreshProgressBar.dismiss();
//
//                }catch (JSONException e) {
//                    e.printStackTrace();
//                    refreshProgressBar.dismiss();
//                    recyclerView.setOnOverPullListener(onOverPullListener);
//
//
//                }
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Snackbar.make(rootLayout , "Something went wrong" , Snackbar.LENGTH_LONG).show();
//            }
//        }){
//            @Override
//            public String getBodyContentType() {
//                return "application/json; charset=utf-8";
//            }
//
//            @Override
//            public byte[] getBody() throws AuthFailureError {
//                JSONObject jsonBody = new JSONObject();
//                try {
//                    jsonBody.put(Config.CLUSTER, cluster);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                final String mRequestBody = jsonBody.toString();
//                try {
//                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
//                } catch (UnsupportedEncodingException uee) {
//                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
//                    return null;
//                }
//            }
//
//            @Override
//            protected Response<String> parseNetworkResponse(NetworkResponse response) {
//                String responseString = "";
//                if (response != null) {
//                    responseString = String.valueOf(response.statusCode);
//                    // can get more details such as response.headers
//                }
//                return super.parseNetworkResponse(response);
//            }
//
//        };
//
//        jsonArrayRequest.setRetryPolicy(new RetryPolicy() {
//            @Override
//            public int getCurrentTimeout() {
//                return 50000;
//            }
//
//            @Override
//            public int getCurrentRetryCount() {
//                return 50000;
//            }
//
//            @Override
//            public void retry(VolleyError error) throws VolleyError {
//
//            }
//        });
//        // Access the RequestQueue through your singleton class.
//        mRequestQueue.add(jsonArrayRequest);;









    }

   private void scroll(){
    if(selectedCardID!=null){
        if(selectedCardType.equals("tasks")){
            if(cardFound){
                myShroomiesTablayout.getTabAt(1).select();
                myShroomiesTablayout.setSelectedTabIndicator(R.drawable.tab_indicator_right);
                myTasksRecyclerView.setVisibility(View.VISIBLE);
                myExpensesRecyclerView.setVisibility(View.GONE);
                myTasksRecyclerView.scrollToPosition(recyclerPosition);
            }else{

                Snackbar snack=Snackbar.make(slidingLayout,"This card doesn't exist anymore", BaseTransientBottomBar.LENGTH_SHORT);
                snack.show();

            }
        }
        if(selectedCardType.equals("expenses")){
            if(cardFound){
                myShroomiesTablayout.getTabAt(0).select();
                myShroomiesTablayout.setSelectedTabIndicator(R.drawable.tab_indicator_left);
                myTasksRecyclerView.setVisibility(View.GONE);
                myExpensesRecyclerView.setVisibility(View.VISIBLE);
                myExpensesRecyclerView.scrollToPosition(recyclerPosition);
            }else{
                Snackbar snack=Snackbar.make(slidingLayout,"This card doesn't exist anymore", BaseTransientBottomBar.LENGTH_SHORT);
                snack.show();
            }
        }
    }
    selectedCardID=null;
    selectedCardType= null;

}

    @Override
    public void onResume() {
        super.onResume();
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }


    private void sortAccordingToOldest(String tab) {
        if(tab.equals("expenses")){
            expensesCardsList.sort(new Comparator<ExpensesCard>() {
                @Override
                public int compare(ExpensesCard o1, ExpensesCard o2) {
                    Date dateO1 = new Date(o1.getDate());
                    Date dateO2 = new Date(o2.getDate());


                    return dateO1.compareTo(dateO2);

                }
            });
            expensesCardAdapter.notifyDataSetChanged();
        }else if(tab.equals("tasks")){
            tasksCardsList.sort(new Comparator<TasksCard>() {
                @Override
                public int compare(TasksCard o1, TasksCard o2) {
                    Date dateO1 = new Date(o1.getDate());
                    Date dateO2 = new Date(o2.getDate());


                    return dateO2.compareTo(dateO1);
                }

            });
            tasksCardAdapter.notifyDataSetChanged();

        }
        }

   private void sortAccordingToLatest(String tab) {
        if(tab.equals("expenses")){
            expensesCardsList.sort(new Comparator<ExpensesCard>() {
                @Override
                public int compare(ExpensesCard o1, ExpensesCard o2) {
                    Date dateO1 = new Date(o1.getDate());
                    Date dateO2 = new Date(o2.getDate());
                    return dateO2.compareTo(dateO1);

                }
            });
            expensesCardAdapter.notifyDataSetChanged();
        }
        else if(tab.equals("tasks")){

            tasksCardsList.sort(new Comparator<TasksCard>() {
                @Override
                public int compare(TasksCard o1, TasksCard o2) {
                    Date dateO1 =new Date(o1.getDate());
                    Date dateO2 =new Date(o2.getDate());



                    return dateO2.compareTo(dateO1);
                }

            });
            tasksCardAdapter.notifyDataSetChanged();
        }

    }


   private void sortAccordingtoImportance(String tab){
        if(tab.equals("expenses")){
            expensesCardsList.sort(new Comparator<ExpensesCard>() {
                @Override
                public int compare(ExpensesCard o1, ExpensesCard o2) {
                    int colorO1=Integer.parseInt(o1.getImportance());
                    int colorO2=Integer.parseInt(o2.getImportance());

                    if (colorO1<colorO2){
                        return 1;
                    }else {
                        return -1;
                    }

                }

            });
            expensesCardAdapter.notifyDataSetChanged();
        }
        else if(tab.equals("tasks")){
            tasksCardsList.sort(new Comparator<TasksCard>() {
                @Override
                public int compare(TasksCard o1, TasksCard o2) {
                    int colorO1=Integer.parseInt(o1.getImportance());
                    int colorO2=Integer.parseInt(o2.getImportance());

                    if (colorO1<colorO2){
                        return 1;
                    }else {
                        return -1;
                    }

                }
            });
            tasksCardAdapter.notifyDataSetChanged();

        }else {

        }


    }

   private void sortAccordingToTitle(String tab){
        if(tab.equals("expenses")){
            expensesCardsList.sort(new Comparator<ExpensesCard>() {
                @Override
                public int compare(ExpensesCard o1, ExpensesCard o2) {

                    return o1.getTitle().compareToIgnoreCase(o2.getTitle());
                }
            });
            expensesCardAdapter.notifyDataSetChanged();

        }else if(tab.equals("tasks")){
            tasksCardsList.sort(new Comparator<TasksCard>() {
                @Override
                public int compare(TasksCard o1, TasksCard o2) {
                    return o1.getTitle().compareToIgnoreCase(o2.getTitle());
                }
            });
            tasksCardAdapter.notifyDataSetChanged();
        }else{

        }

    }

   public void sendInput(String cardID, String cardType) {
        this.selectedCardID = cardID;
        this.selectedCardType=cardType;



        if(selectedCardType.equals("tasks")){
            for (TasksCard card:tasksCardsList){
                if(card.getCardID().equals(selectedCardID)){
                    final int position=tasksCardsList.indexOf(card);
                    if(position!=-1){
                        this.recyclerPosition=position;
                        cardFound =true;
                    }else{
                        cardFound=false;
                    }

                }
            }

        }if(selectedCardType.equals("expenses")){

            for(ExpensesCard card:expensesCardsList){
                if(card.getCardID().equals(selectedCardID)){
                    final int position=expensesCardsList.indexOf(card);
                    if(position!=-1){
                        this.recyclerPosition=position;
                        cardFound =true;
                    }else{
                        cardFound=false;

                    }


                }
            }

        }
    }

}