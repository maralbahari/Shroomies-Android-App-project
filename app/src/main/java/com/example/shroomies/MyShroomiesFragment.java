package com.example.shroomies;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollUpdateListener;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;


public class MyShroomiesFragment extends Fragment  implements LogAdapterToMyshroomies , CardUploaded {
    //views
    private View v;
    private TabLayout myShroomiesTablayout;
    private RecyclerView myExpensesRecyclerView,myTasksRecyclerView;
    private ImageButton expandButton;
    private SlidingUpPanelLayout slidingLayout;
    private CustomLoadingProgressBar customLoadingProgressBar;
    private IOverScrollDecor expensesDecor;
    private IOverScrollDecor tasksDecor;
    private IOverScrollUpdateListener onOverPullListener;

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
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;
    //fragment
    private FragmentTransaction ft;
    private FragmentManager fm;
    //values
    private String selectedCardID , selectedCardType;
    private boolean cardFound =false;
    private int recyclerPosition=0;

    @Override
    public void sendData(TasksCard tasksCard , ExpensesCard expensesCard) {
        if(tasksCard!=null){
            if(tasksCardsList!=null && tasksCardAdapter!=null){
                tasksCardsList.add(tasksCard);
                tasksCardAdapter.notifyDataSetChanged();
            }
        }else if(expensesCard!=null){
            if(expensesCardsList!=null && expensesCardAdapter!=null){
                expensesCardsList.add(expensesCard);
                expensesCardAdapter.notifyDataSetChanged();
            }
        }


    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        mAuth.useEmulator("10.0.2.2" , 9099);
        requestQueue = Volley.newRequestQueue(getActivity());
        v = inflater.inflate(R.layout.fragment_my_shroomies, container, false);
        return v;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
         getUserToken();
        myTasksRecyclerView = v.findViewById(R.id.my_tasks_recycler_view);
        Button memberButton = v.findViewById(R.id.my_shroomies_member_btn);
        ImageButton addCardButton = v.findViewById(R.id.my_shroomies_add_card_btn);
        Button logButton = v.findViewById(R.id.my_shroomies_log);
        myShroomiesTablayout = v.findViewById(R.id.my_shroomies_tablayout);
        myExpensesRecyclerView = v.findViewById(R.id.my_expenses_recycler_view);
        PowerSpinnerView shroomieSpinnerFilter = v.findViewById(R.id.shroomie_spinner_filter);
        expandButton = v.findViewById(R.id.expand_button);
        slidingLayout = v.findViewById(R.id.sliding_layout);
        Button archiveButton = v.findViewById(R.id.my_shroomies_archive_btn);

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


        expensesDecor = OverScrollDecoratorHelper.setUpOverScroll(myExpensesRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        tasksDecor = OverScrollDecoratorHelper.setUpOverScroll(myTasksRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        onOverPullListener = (decor, state, offset) -> {
            //recyceler view is overscrolled from the top
            if(offset>200){
                //fetch new data when over scrolled from top
                // remove the listener to prevent the user from over scrolling
                // again while the data is still being fetched
                //the listener will be set again when the data has been retrieved
                expensesDecor.setOverScrollUpdateListener(null);
                tasksDecor.setOverScrollUpdateListener(null);
                getUserToken();
            }
        };



        slidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState.equals(SlidingUpPanelLayout.PanelState.EXPANDED)){
                    expandButton.animate().rotation(180).setDuration(100).start();

                }else{
                    shroomieSpinnerFilter.dismiss();
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
                }

                else if(tab.getPosition()==1){
                    myShroomiesTablayout.setSelectedTabIndicator(R.drawable.tab_indicator_right);
                    myTasksRecyclerView.setVisibility(View.VISIBLE);
                    myExpensesRecyclerView.setVisibility(View.GONE);
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
                int selectedTab=  myShroomiesTablayout.getSelectedTabPosition();
                switch (i) {
                    case 0:
                        sortAccordingtoImportance(selectedTab);
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        Toast.makeText(getActivity(),"wowow" , Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        sortAccordingToLatest(selectedTab);
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        break;
                    case 2:
                        sortAccordingToOldest(selectedTab);
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

                        break;
                    case 3:
                        sortAccordingToTitle(selectedTab);
                        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        break;
                }
            }
        });

        addCardButton.setOnClickListener(view1 -> {

            AddNewCard addNewCard = new AddNewCard();
            addNewCard.setTargetFragment(this , 0);
            Bundle bundle=new Bundle();

            bundle.putBoolean("Expenses", myShroomiesTablayout.getSelectedTabPosition() == 0);
            bundle.putParcelable("APARTMENT_DETAILS",apartment);
            addNewCard.setArguments(bundle);

            addNewCard.show(getActivity().getSupportFragmentManager(),"add new card");

        });

        archiveButton.setOnClickListener(v -> {

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

        });
        memberButton.setOnClickListener(v -> {
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
        });
        logButton.setOnClickListener(view12 -> {
            LogFragment logFragment=new LogFragment();
            Bundle bundle=new Bundle();
            bundle.putParcelableArrayList("LOG_LIST", apartmentLogs);
            ArrayList<String> members;
            if(apartment.getApartmentMembers()!=null){
                //put the members and add the admin
                members = new ArrayList<>(apartment.getApartmentMembers().values());
            }else{
                members = new ArrayList<>();
            }
            members.add(apartment.getAdminID());
            bundle.putStringArrayList("MEMBERS" ,members);
            logFragment.setArguments(bundle);
            logFragment.setTargetFragment(MyShroomiesFragment.this,RESULT_CODE);
            fm = getParentFragmentManager();
            ft = fm.beginTransaction();
            ft.addToBackStack(null);
            ft.setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN );
            ft.replace(R.id.my_shroomies_container, logFragment);
            ft.commit();
        });
        scroll();

    }



    private void getUserToken(){
        customLoadingProgressBar = new CustomLoadingProgressBar(getActivity() , "null" , R.raw.myshroomies_loading);
        customLoadingProgressBar.getWindow()
                .setBackgroundDrawableResource(android.R.color.transparent);
        customLoadingProgressBar.show();

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.getIdToken(false).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                String token = task.getResult().getToken();
                getApartmentDetails(token);
            }else{
                //todo handle error
                String title = "Authentication error";
                String message = "We encountered a problem while authenticating your account";
                displayErrorAlert(title, message);
            }
        });
    }

    private void getApartmentDetails(String token){
        JSONObject jsonObject = new JSONObject();
        JSONObject data  = new JSONObject();
        try {
            jsonObject.put(Config.id, mAuth.getCurrentUser().getUid());
            data.put(Config.data, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        //TODO add progress dialog
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_GET_APARTMENT_DETAILS, data, response -> {

            final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            try {
                JSONObject result = response.getJSONObject(Config.result);
                boolean success = result.getBoolean(Config.success);

            if (success) {
                result = (JSONObject) result.get(Config.apartment);
                apartment = mapper.readValue(result.toString(), ShroomiesApartment.class);

                //initialize an empty list and
                //if the user add a new card the new card will be sent to this fragment
                // im checking if the list is not null to add the card
                //this will ensure that the list is not null
                expensesCardsList = new ArrayList<>();
                if (apartment.getExpensesCard() != null) {
                    if (isAdded()) {

                        expensesCardsList = new ArrayList<>(apartment.getExpensesCard().values());
                    }
                }
                expensesCardAdapter = new ExpensesCardAdapter(expensesCardsList, getActivity(), false, apartment.getApartmentID(), getChildFragmentManager(), slidingLayout);
                myExpensesRecyclerView.setAdapter(expensesCardAdapter);

                tasksCardsList = new ArrayList<>();
                if (apartment.getTaskCard() != null) {
                    if (isAdded()) {
                        tasksCardsList = new ArrayList<>(apartment.getTaskCard().values());
                    }
                }
                tasksCardAdapter = new TasksCardAdapter(tasksCardsList, getActivity(), false, apartment.getApartmentID(), getChildFragmentManager(), slidingLayout);
                myTasksRecyclerView.setAdapter(tasksCardAdapter);

                if (apartment.getLogs() != null) {
                    if (!apartment.getLogs().isEmpty()) {
                        apartmentLogs = new ArrayList<>(apartment.getLogs().values());
                    }
                }

                tasksDecor.setOverScrollUpdateListener(onOverPullListener);
                expensesDecor.setOverScrollUpdateListener(onOverPullListener);

                customLoadingProgressBar.dismiss();
            } else {
                String title = "Unexpected error";
                String message = "We have encountered an unexpected error ,try to check your internet connection and log in again.";
                displayErrorAlert(title, message);
            }

            } catch (JSONException | JsonProcessingException e) {
                e.printStackTrace();
                String title = "Unexpected error";
                String message = "We have encountered an unexpected error ,try to check your internet connection and log in again.";
                displayErrorAlert(title, message);
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
            displayErrorAlert("Error" ,message);
        })
                //todo uncomment when function  is deployed
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


    private void sortAccordingToOldest(int tab) {
        if(tab==0){
            expensesCardsList.sort((o1, o2) -> {
                Date dateO1 = new Date(o1.getDate());
                Date dateO2 = new Date(o2.getDate());
                return dateO1.compareTo(dateO2);
            });
            expensesCardAdapter.notifyDataSetChanged();
        }else if(tab==1){
            tasksCardsList.sort((o1, o2) -> {
                Date dateO1 = new Date(o1.getDate());
                Date dateO2 = new Date(o2.getDate());


                return dateO2.compareTo(dateO1);
            });
            tasksCardAdapter.notifyDataSetChanged();

        }
        }

   private void sortAccordingToLatest(int tab) {
        if(tab==0){
            expensesCardsList.sort((o1, o2) -> {
                Date dateO1 = new Date(o1.getDate());
                Date dateO2 = new Date(o2.getDate());
                return dateO2.compareTo(dateO1);

            });
            expensesCardAdapter.notifyDataSetChanged();
        }
        else if(tab==1){

            tasksCardsList.sort((o1, o2) -> {
                Date dateO1 =new Date(o1.getDate());
                Date dateO2 =new Date(o2.getDate());



                return dateO2.compareTo(dateO1);
            });
            tasksCardAdapter.notifyDataSetChanged();
        }

    }


   private void sortAccordingtoImportance(int tab){
        if(tab==0){
            if(expensesCardsList!=null) {
                expensesCardsList.sort((o1, o2) -> {
                    int colorO1 = Integer.parseInt(o1.getImportance());
                    int colorO2 = Integer.parseInt(o2.getImportance());
                    if (colorO1 < colorO2) {
                        return 1;
                    } else {
                        return -1;
                    }

                });
                expensesCardAdapter.notifyDataSetChanged();
            }

        }
        else if(tab==1){
            if(tasksCardsList!=null){
                tasksCardsList.sort((o1, o2) -> {
                    int colorO1=Integer.parseInt(o1.getImportance());
                    int colorO2=Integer.parseInt(o2.getImportance());
                    if (colorO1<colorO2){
                        return 1;
                    }else {
                        return -1;
                    }
                });
                tasksCardAdapter.notifyDataSetChanged();
            }
        }

    }

   private void sortAccordingToTitle(int tab){
        if(tab==0){
            expensesCardsList.sort((o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
            expensesCardAdapter.notifyDataSetChanged();

        }else if(tab==1){
            tasksCardsList.sort((o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));
            tasksCardAdapter.notifyDataSetChanged();
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
    void displayErrorAlert(String title, String message){
        tasksDecor.setOverScrollUpdateListener(onOverPullListener);
        expensesDecor.setOverScrollUpdateListener(onOverPullListener);
        customLoadingProgressBar.dismiss();
         new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_alert)
                .setTitle(title)
                .setMessage(message)
                 .setCancelable(false)
                 .setNeutralButton("return", (dialog, which) -> {
                     getActivity().finish();
                     dialog.dismiss();
                 })
                 .setPositiveButton("refresh", (dialog, which) -> getUserToken())
                .create()
                .show();
    }


}