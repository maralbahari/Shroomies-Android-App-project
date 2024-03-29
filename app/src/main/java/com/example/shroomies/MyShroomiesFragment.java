package com.example.shroomies;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skydoves.powerspinner.PowerSpinnerView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollStateListener;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;


public class MyShroomiesFragment extends Fragment  implements LogAdapterToMyshroomies , CardUploaded  {
    //views
    private View v;
    private TabLayout myShroomiesTablayout;
    private RecyclerView myExpensesRecyclerView,myTasksRecyclerView;
    private ImageButton expandButton;
    private SlidingUpPanelLayout slidingLayout;
    private LottieAnimationView progressView;
    private ImageButton logoImageButton;
    private IOverScrollDecor expensesDecor;
    private IOverScrollDecor tasksDecor;
    private IOverScrollStateListener onOverPullListener;
    private RelativeLayout noCardsLayout;
    private MaterialButton groupMessageButton;
    private FrameLayout messageButtonFrame;
    BadgeDrawable badgeDrawable;
    //data structures
    private ArrayList<TasksCard> tasksCardsList;
    private ArrayList<ExpensesCard> expensesCardsList;
    private ArrayList<ApartmentLogs> apartmentLogs;
    private HashMap<String,User> membersHashMap;
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
    private static final String TASK_CARD_LIST = "TASK_CARD_LIST" ,EXPENSE_CARD_LIST = "EXPENSE_CARD_LIST";
    private boolean cardFound =false;
    private int recyclerPosition=0;
    boolean scrollFromTop;
    private int unSeenMessagesNo=0;

    @Override
    public void sendData(TasksCard tasksCard , ExpensesCard expensesCard) {
        if(tasksCard!=null){
            if(tasksCardsList!=null && tasksCardAdapter!=null){
                tasksCardsList.add(tasksCard);
                tasksCardAdapter.notifyDataSetChanged();
                    removeNoCardsLayout();

            }
        }else if(expensesCard!=null){
            if(expensesCardsList!=null && expensesCardAdapter!=null){
                expensesCardsList.add(expensesCard);
                expensesCardAdapter.notifyDataSetChanged();
                    removeNoCardsLayout();

            }
        }


    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(getActivity());
        v = inflater.inflate(R.layout.fragment_my_shroomies, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myTasksRecyclerView = v.findViewById(R.id.my_tasks_recycler_view);

        myShroomiesTablayout = v.findViewById(R.id.my_shroomies_tablayout);
        myExpensesRecyclerView = v.findViewById(R.id.my_expenses_recycler_view);
        PowerSpinnerView shroomieSpinnerFilter = v.findViewById(R.id.shroomie_spinner_filter);
        expandButton = v.findViewById(R.id.expand_button);
        slidingLayout = v.findViewById(R.id.sliding_layout);
        noCardsLayout =  v.findViewById(R.id.no_cards_layout);
        groupMessageButton=v.findViewById(R.id.my_shroomies_group_message_btn);
        messageButtonFrame=v.findViewById(R.id.frame_layout_message_button);
        MaterialButton memberButton = v.findViewById(R.id.my_shroomies_member_btn);
        MaterialButton logButton = v.findViewById(R.id.my_shroomies_log);
        MaterialButton archiveButton = v.findViewById(R.id.my_shroomies_archive_btn);
        MaterialButton addMemberButton = v.findViewById(R.id.my_shroomies_add_member_btn);
        badgeDrawable = BadgeDrawable.create(getContext());
        Toolbar toolbar = getActivity().findViewById(R.id.my_shroomies_toolbar);
        progressView = toolbar.findViewById(R.id.loading_progress_view);
        logoImageButton= toolbar.findViewById(R.id.myshroomies_toolbar_logo);
        if (this.getArguments()!=null){
            Bundle bundle=this.getArguments();
            selectedCardID=bundle.getString("CARDID");
            selectedCardType=bundle.getString("CARDTYPE");
        }
        toolbar.setNavigationIcon(null);
        toolbar.setTitle(null);
        toolbar.setElevation(0);
        toolbar.findViewById(R.id.myshroomies_toolbar_logo).setVisibility(View.VISIBLE);
        toolbar.findViewById(R.id.my_shroomies_add_card_btn).setVisibility(View.VISIBLE);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setNavigationOnClickListener(view1 -> getActivity().onBackPressed());
        groupMessageButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint({"ResourceAsColor", "UnsafeExperimentalUsageError"})
            @Override
            public void onGlobalLayout() {
                badgeDrawable.setNumber(unSeenMessagesNo);
                badgeDrawable.setBackgroundColor(getActivity().getColor(R.color.red));
                BadgeUtils.attachBadgeDrawable(badgeDrawable, groupMessageButton, messageButtonFrame);
                badgeDrawable.setHorizontalOffset(100);
                badgeDrawable.setVerticalOffset(100);
                badgeDrawable.setHotspotBounds(100,100,100,100);
                badgeDrawable.setBadgeGravity(BadgeDrawable.BOTTOM_END);
                groupMessageButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            }
        });
//

        ImageButton addCardButton = toolbar.findViewById(R.id.my_shroomies_add_card_btn);
        addCardButton.setVisibility(View.VISIBLE);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        myExpensesRecyclerView.setHasFixedSize(true);
        myExpensesRecyclerView.setLayoutManager(linearLayoutManager);


        LinearLayoutManager linearLayoutManager1 =new LinearLayoutManager(getActivity());
        myTasksRecyclerView.setHasFixedSize(true);
        myTasksRecyclerView.setLayoutManager(linearLayoutManager1);


        expensesDecor = OverScrollDecoratorHelper.setUpOverScroll(myExpensesRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        tasksDecor = OverScrollDecoratorHelper.setUpOverScroll(myTasksRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        onOverPullListener = (decor, oldState, newState) -> {
            if(oldState== 1){
                scrollFromTop=true;
            }
            if (newState == 0 && scrollFromTop) {
                //fetch new data when over scrolled from top
                // remove the listener to prevent the user from over scrolling
                // again while the data is still being fetched
                //the listener will be set again when the data has been retrieved
                expensesDecor.setOverScrollStateListener(null);
                tasksDecor.setOverScrollStateListener(null);
                scrollFromTop=false;
                getUserToken();
            }
        };

        groupMessageButton.setOnClickListener(view13 -> {
            Intent intent=new Intent(getActivity(),GroupChatting.class);
            Bundle bundle=new Bundle();
            bundle.putSerializable("MEMBERS",membersHashMap);
            intent.putExtra("extras",bundle);
            startActivity(intent);

        });


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
                    myExpensesRecyclerView.setVisibility(View.VISIBLE);
                    myTasksRecyclerView.setVisibility(View.GONE);

                    if(expensesCardsList!=null){
                        if(expensesCardsList.isEmpty()){
                            displayNoCards();
                        }else{
                            removeNoCardsLayout();
                        }
                    }
                }

                else if(tab.getPosition()==1){
                    myShroomiesTablayout.setSelectedTabIndicator(R.drawable.tab_indicator_right);
                    myTasksRecyclerView.setVisibility(View.VISIBLE);
                    myExpensesRecyclerView.setVisibility(View.GONE);
                    if(tasksCardsList!=null){
                        if(tasksCardsList.isEmpty()){
                            displayNoCards();
                        }else{
                            removeNoCardsLayout();
                        }
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

        shroomieSpinnerFilter.setOnSpinnerItemSelectedListener((i, o, i1, t1) -> {
            int selectedTab=  myShroomiesTablayout.getSelectedTabPosition();
            switch (i) {
                case 0:
                    sortAccordingtoImportance(selectedTab);
                    slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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
        });

        addCardButton.setOnClickListener(view1 -> {
            if(apartment!=null) {
                AddNewCard addNewCard = new AddNewCard();
                addNewCard.setTargetFragment(this, 0);
                Bundle bundle = new Bundle();
                bundle.putBoolean("Expenses", myShroomiesTablayout.getSelectedTabPosition() == 0);
                bundle.putParcelable("APARTMENT_DETAILS", apartment);

                addNewCard.setArguments(bundle);

                addNewCard.show(getActivity().getSupportFragmentManager(), "add new card");
            }

        });

        addMemberButton.setOnClickListener(v -> {
            if(apartment!=null){
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                AddShroomieMember add=new AddShroomieMember();
                Bundle bundle1 = new Bundle();
                bundle1.putParcelable("APARTMENT_DETAILS",apartment);
                add.setArguments(bundle1);
                add.show(getParentFragmentManager(),"add member to apartment");
            }else{
//                Toast.makeText(getActivity() , "apartment null", Toast.LENGTH_SHORT).show();
            }

        });


        archiveButton.setOnClickListener(v -> {
            if(apartment!=null){
                ArchiveFragment archiveFragment = new ArchiveFragment();
                Bundle bundle=new Bundle();
                bundle.putString(Config.apartmentID,apartment.getApartmentID());
                bundle.putSerializable(Config.members , membersHashMap);
                archiveFragment.setArguments(bundle);
                fm = getActivity().getSupportFragmentManager();
                ft = fm.beginTransaction();
                ft.addToBackStack(null);
                ft.setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN );
                ft.replace(R.id.my_shroomies_container, archiveFragment);
                ft.commit();
            }

        });
        memberButton.setOnClickListener(v -> {
            if(apartment!=null) {

                Members members = new Members();
                Bundle bundle1 = new Bundle();
                bundle1.putParcelable("APARTMENT_DETAILS", apartment);
                members.setArguments(bundle1);
                fm = getActivity().getSupportFragmentManager();
                ft = fm.beginTransaction();
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.replace(R.id.my_shroomies_container, members);
                ft.commit();
            }
        });
        logButton.setOnClickListener(view12 -> {
            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            if(apartment!=null) {
                LogFragment logFragment = new LogFragment();
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("LOG_LIST", apartmentLogs);
                ArrayList<String> members;
                if (apartment.getApartmentMembers() != null) {
                    //put the members and add the admin
                    members = new ArrayList<>(apartment.getApartmentMembers().values());
                } else {
                    members = new ArrayList<>();
                }
                members.add(apartment.getAdminID());
                bundle.putStringArrayList("MEMBERS", members);
                logFragment.setArguments(bundle);
                logFragment.setTargetFragment(MyShroomiesFragment.this, RESULT_CODE);
                fm = getParentFragmentManager();
                ft = fm.beginTransaction();
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.replace(R.id.my_shroomies_container, logFragment);
                ft.commit();
            }
        });

        getUserToken();


    }



    private void getUserToken(){
        displayProgressView();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if(firebaseUser!=null){
            firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    String apartmentID = (String) task.getResult().getClaims().get(Config.apartmentID);
                    Log.d("apartment ID" , apartmentID);
                    getApartmentDetails(token, apartmentID);
                    getUnseenMessageNo(apartmentID,firebaseUser.getUid());
                }else{
                    String title = "Authentication error";
                    String message = "We encountered a problem while authenticating your account";
                    displayErrorAlert(title, message);
                }
            });
        }

    }
    private void getUnseenMessageNo(String apartmentID, String userID){
        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        rootRef.child("groupMessages").child(apartmentID).child("unSeenMessageCount")
                .child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            unSeenMessagesNo=Integer.parseInt(snapshot.getValue().toString());
                            if(unSeenMessagesNo==0){
                                badgeDrawable.setVisible(false);
                            }else{
                                badgeDrawable.setVisible(true);
                                badgeDrawable.setNumber(unSeenMessagesNo);
                            }
                        }else{
                            badgeDrawable.setVisible(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void getApartmentDetails(String token , String apartmentID){
        JSONObject jsonObject = new JSONObject();
        JSONObject data  = new JSONObject();
        try {
            jsonObject.put(Config.apartmentID, apartmentID);
            data.put(Config.data, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_GET_APARTMENT_DETAILS, data, response -> {

            final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            try {
                JSONObject result = response.getJSONObject(Config.result);
                boolean success = result.getBoolean(Config.success);

            if (success) {
                membersHashMap = new HashMap<>();
                JSONObject apartmentJsonObject = (JSONObject) result.get(Config.apartment);
                JSONArray membersJson= result.getJSONArray(Config.members);
                for(int i =0;i<membersJson.length();i++){
                    User user = mapper.readValue(membersJson.getJSONObject(i).toString(), User.class);
                    membersHashMap.put(user.getUserID() , user);
                }

                apartment = new ShroomiesApartment();
                apartment = mapper.readValue(apartmentJsonObject.toString(), ShroomiesApartment.class);

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


                expensesCardAdapter = new ExpensesCardAdapter(expensesCardsList, getActivity(), false, apartment.getApartmentID(), getParentFragmentManager(), slidingLayout , membersHashMap);
                myExpensesRecyclerView.setAdapter(expensesCardAdapter);
                ItemTouchHelper.Callback expenseCalback =
                        new CardsTouchHelper(expensesCardAdapter);
                ItemTouchHelper expenseTouchHelper = new ItemTouchHelper(expenseCalback);
                expenseTouchHelper.attachToRecyclerView(myExpensesRecyclerView);


                tasksCardsList = new ArrayList<>();
                if (apartment.getTaskCard() != null) {
                    if (isAdded()) {
                        tasksCardsList = new ArrayList<>(apartment.getTaskCard().values());
                    }
                }



                tasksCardAdapter = new TasksCardAdapter(tasksCardsList, getActivity(), false, apartment.getApartmentID(), getParentFragmentManager(), slidingLayout , membersHashMap);
                myTasksRecyclerView.setAdapter(tasksCardAdapter);
                ItemTouchHelper.Callback callback =
                        new CardsTouchHelper(tasksCardAdapter);
                ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
                touchHelper.attachToRecyclerView(myTasksRecyclerView);

                if (apartment.getLogs() != null) {
                    if (!apartment.getLogs().isEmpty()) {
                        apartmentLogs = new ArrayList<>(apartment.getLogs().values());
                    }
                }

                tasksDecor.setOverScrollStateListener(onOverPullListener);
                expensesDecor.setOverScrollStateListener(onOverPullListener);
                setListenersForemptyLists();

                removeProgressView();

                scroll();


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

    private void setListenersForemptyLists() {
        tasksCardAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if(myShroomiesTablayout.getSelectedTabPosition()==1){
                    if(tasksCardsList.isEmpty()){
                        displayNoCards();
                    }else{
                        if(noCardsLayout.getVisibility()==View.VISIBLE){
                            removeNoCardsLayout();
                        }
                    }
                }
            }
        });
        tasksCardAdapter.notifyDataSetChanged();
        expensesCardAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if(myShroomiesTablayout.getSelectedTabPosition()==0){
                    if(expensesCardsList.isEmpty()){
                        displayNoCards();
                    }else{
                        if(noCardsLayout.getVisibility()==View.VISIBLE){
                            removeNoCardsLayout();
                        }
                    }
                }
            }
        });
        expensesCardAdapter.notifyDataSetChanged();
    }


    private void scroll(){
    if(selectedCardID!=null){
        sendInput(selectedCardID,selectedCardType);
        if(selectedCardType.equals(Config.task)){
            if(cardFound){
                myShroomiesTablayout.getTabAt(1).select();
                myShroomiesTablayout.setSelectedTabIndicator(R.drawable.tab_indicator_right);
                myTasksRecyclerView.setVisibility(View.VISIBLE);
                myExpensesRecyclerView.setVisibility(View.GONE);
                myTasksRecyclerView.post(() -> myTasksRecyclerView.smoothScrollToPosition(recyclerPosition));
            }else{
                Snackbar snack=Snackbar.make(slidingLayout,"This card doesn't exist anymore", BaseTransientBottomBar.LENGTH_SHORT);
                snack.show();
            }
        }
        if(selectedCardType.equals(Config.expenses)){

            if(cardFound){
                myShroomiesTablayout.getTabAt(0).select();
                myShroomiesTablayout.setSelectedTabIndicator(R.drawable.tab_indicator_left);
                myTasksRecyclerView.setVisibility(View.GONE);
                myExpensesRecyclerView.setVisibility(View.VISIBLE);
                myExpensesRecyclerView.smoothScrollToPosition(recyclerPosition);
                myExpensesRecyclerView.post(() -> myExpensesRecyclerView.smoothScrollToPosition(recyclerPosition));

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
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss Z")
                        .withZone(TimeZone.getDefault().toZoneId());
                LocalDateTime date1 = LocalDateTime.parse(o1.getDate() , dateFormat);
                LocalDateTime date2 = LocalDateTime.parse(o2.getDate() , dateFormat);
                return date2.compareTo(date1);
            });
            expensesCardAdapter.notifyDataSetChanged();
        }else if(tab==1){
            tasksCardsList.sort((o1, o2) -> {
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss Z")
                        .withZone(TimeZone.getDefault().toZoneId());
                LocalDateTime date1 = LocalDateTime.parse(o1.getDate() , dateFormat);
                LocalDateTime date2 = LocalDateTime.parse(o2.getDate() , dateFormat);
                return date2.compareTo(date1);
            });
            tasksCardAdapter.notifyDataSetChanged();

        }
        }

   private void sortAccordingToLatest(int tab) {
        if(tab==0){
            expensesCardsList.sort((o1, o2) -> {
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss Z")
                        .withZone(TimeZone.getDefault().toZoneId());
                LocalDateTime date1 = LocalDateTime.parse(o1.getDate() , dateFormat);
                LocalDateTime date2 = LocalDateTime.parse(o2.getDate() , dateFormat);
                return date1.compareTo(date2);
            });
            expensesCardAdapter.notifyDataSetChanged();
        }
        else if(tab==1){

            tasksCardsList.sort((o1, o2) -> {
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss Z")
                        .withZone(TimeZone.getDefault().toZoneId());
                LocalDateTime date1 = LocalDateTime.parse(o1.getDate() , dateFormat);
                LocalDateTime date2 = LocalDateTime.parse(o2.getDate() , dateFormat);
                return date1.compareTo(date2);
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

    @Override
    public void sendInput(String cardID, String cardType) {
        this.selectedCardID = cardID;
        this.selectedCardType=cardType;
        if(selectedCardType.equals(Config.task)){
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

        }if(selectedCardType.equals(Config.expenses)){

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
    private void displayProgressView() {
      logoImageButton.setVisibility(View.GONE);
      progressView.setVisibility(View.VISIBLE);

    }
    private void removeProgressView(){
        progressView.setVisibility(View.GONE);

        logoImageButton.setVisibility(View.VISIBLE);


    }
    private void displayNoCards(){
        noCardsLayout.animate().alpha(1.0f).setDuration(400);
        noCardsLayout.setVisibility(View.VISIBLE);
    }
    private void removeNoCardsLayout(){
        noCardsLayout.animate().alpha(0.0f).setDuration(400);
        noCardsLayout.setVisibility(View.GONE);
    }
    private void displayErrorAlert(String title, String message){
        tasksDecor.setOverScrollStateListener(onOverPullListener);
        expensesDecor.setOverScrollStateListener(onOverPullListener);
        removeProgressView();
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