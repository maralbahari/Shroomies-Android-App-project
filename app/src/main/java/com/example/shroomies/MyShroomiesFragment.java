package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.factor.bouncy.BouncyRecyclerView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class MyShroomiesFragment extends Fragment  implements LogAdapterToMyshroomies {
    //views
    private View v;
    private SlidingUpPanelLayout rootlayout;
    private Button memberButton,logButton;
    private TabLayout myShroomiesTablayout;
    private BouncyRecyclerView myExpensesRecyclerView,myTasksRecyclerView;
    private PowerSpinnerView shroomieSpinnerFilter;
    private ImageButton addCardButton , expandButton;

    SlidingUpPanelLayout slidingLayout;
    //data structures
    private ArrayList<TasksCard> tasksCardsList;
    private ArrayList<ExpensesCard> expensesCardsList;
    private ArrayList<Log> apartmentLogList;
    private TasksCardAdapter tasksCardAdapter;
    private ExpensesCardAdapter expensesCardAdapter;
    //final static
    private static final int RESULT_CODE=500;
    //model
    private ShroomiesApartment apartment;
    // firebase listeners
    private ValueEventListener expensesCardListener;
    private ValueEventListener tasksCardListener;
    private ValueEventListener apartmentListener;
    private ValueEventListener logListener;
    //firebase
    private DatabaseReference rootRef;
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

        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
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
                    expensesCardsList = new ArrayList<>();
                    retreiveExpensesCards(apartment.getApartmentID());

                }

                else if(tab.getPosition()==1){
                    myShroomiesTablayout.setSelectedTabIndicator(R.drawable.tab_indicator_right);
                    myTasksRecyclerView.setVisibility(View.VISIBLE);
                    myExpensesRecyclerView.setVisibility(View.GONE);
                    tabSelected="tasks";
                    tasksCardsList=new ArrayList<>();
                    retrieveTaskCards(apartment.getApartmentID());
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
                bundle.putParcelableArrayList("LOG_LIST",apartmentLogList);
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
        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                   final String  apartmentID=snapshot.getValue().toString();
                    apartmentListener=rootRef.child("apartments").child(apartmentID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                apartment=snapshot.getValue(ShroomiesApartment.class);
                                if(expensesCardsList.isEmpty()){
                                    retreiveExpensesCards(apartment.getApartmentID());
                                }
                                getApartmentLog(apartment.getApartmentID());
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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


   private void getApartmentLog(final String apartmentID){
       apartmentLogList =new ArrayList<>();
       logListener=rootRef.child("logs").child(apartmentID).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               apartmentLogList.clear();
               if(snapshot.exists()){
                   for(DataSnapshot sp:snapshot.getChildren()){
                       Log log=sp.getValue(Log.class);
                       apartmentLogList.add(log);
                   }
                   Collections.reverse(apartmentLogList);
                   getUserDetailsForLog(apartmentLogList);


               }else{
                //TODO add progress bar
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });

   }

    private void getUserDetailsForLog(final ArrayList<Log> apartmentlogList){
        for(final Log log:apartmentlogList){
            rootRef.child("Users").child(log.getActor()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        User user=snapshot.getValue(User.class);
                        log.setActorName(user.getName());
                        log.setActorPic(user.getImage());

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


    }

   private void retrieveTaskCards(String apartmentID) {
        tasksCardsList=new ArrayList<>();
        tasksCardAdapter= new TasksCardAdapter(tasksCardsList,getActivity(),false,apartment,getActivity().getSupportFragmentManager(),rootlayout);
        ItemTouchHelper.Callback callback = new CardsTouchHelper(tasksCardAdapter);
        ItemTouchHelper itemTouchHelperTask = new ItemTouchHelper(callback);
        tasksCardAdapter.setItemTouchHelper(itemTouchHelperTask);
        itemTouchHelperTask.attachToRecyclerView(myTasksRecyclerView);
        myTasksRecyclerView.setAdapter(tasksCardAdapter);

        tasksCardListener=rootRef.child("apartments").child(apartmentID).child("tasksCards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tasksCardsList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot sp : snapshot.getChildren()) {
                        TasksCard tasksCard = sp.getValue(TasksCard.class);
                        tasksCardsList.add(tasksCard);
                    }
                    Collections.reverse(tasksCardsList);
                    tasksCardAdapter.notifyDataSetChanged();

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

   public void retreiveExpensesCards(String apartmentID){
        expensesCardsList=new ArrayList<>();
        expensesCardAdapter = new ExpensesCardAdapter(expensesCardsList,getActivity().getApplicationContext(), false,apartment,getActivity().getSupportFragmentManager(),rootlayout);
        ItemTouchHelper.Callback callback = new CardsTouchHelper(expensesCardAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        expensesCardAdapter.setItemTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(myExpensesRecyclerView);
        myExpensesRecyclerView.setAdapter(expensesCardAdapter);
        expensesCardListener=rootRef.child("apartments").child(apartmentID).child("expensesCards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expensesCardsList.clear();
                if (snapshot.exists()) {

                    for (DataSnapshot sp : snapshot.getChildren()) {
                        ExpensesCard expensesCard = sp.getValue(ExpensesCard.class);
                        expensesCardsList.add(expensesCard);

                    }
                    Collections.reverse(expensesCardsList);
                    expensesCardAdapter.notifyDataSetChanged();


                }else{
                    //TODO add progress bar
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
    @Override
   public void onDestroy() {
        super.onDestroy();
        if(expensesCardListener!=null)
        rootRef.removeEventListener(expensesCardListener);
        rootRef.removeEventListener(apartmentListener);
        rootRef.removeEventListener(logListener);
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
                if(card.getCardId().equals(selectedCardID)){
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
                if(card.getCardId().equals(selectedCardID)){
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