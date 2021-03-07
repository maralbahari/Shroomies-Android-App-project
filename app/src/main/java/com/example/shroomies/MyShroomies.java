package com.example.shroomies;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;



public class MyShroomies extends Fragment implements LogAdapter.cardBundles  {
    View v;
    private Button  memberButton, addCardButton;
    private TabLayout myShroomiesTablayout;
    private RecyclerView myExpensesRecyclerView;
    private RecyclerView myTasksRecyclerView;
    private Spinner shroomieSpinnerFilter;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private ArrayList<TasksCard> tasksCardsList;
    private ArrayList<ExpensesCard> expensesCardsList;
    private TasksCardAdapter tasksCardAdapter;
    private  ExpensesCardAdapter expensesCardAdapter;
    private String tabSelected="expenses";
    private String apartmentID="";
    private ShroomiesApartment apartment;
    private ValueEventListener expensesCardListener;
    private ValueEventListener tasksCardListener;
    private ValueEventListener apartmentListener;
    private ValueEventListener logListener;
    private Button logButton;
    private ArrayList<Log> apartmentlogList;
    private  FragmentTransaction ft;
    private FragmentManager fm;
    private String selectedCardID;
    private String selectedCardType;
    private final int RESULT_CODE=500;
    private boolean cardFound =false;
    private int recyclerPosition=0;
    private CustomLoadingProgressBar customLoadingProgressBar;


    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_my_shroomies, container, false);
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        getApartmentDetails();
        return v;
    }




    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        myTasksRecyclerView = v.findViewById(R.id.my_tasks_recycler_view);
        memberButton = v.findViewById(R.id.my_shroomies_member_btn);
        addCardButton = v.findViewById(R.id.my_shroomies_add_card_btn);
        logButton=v.findViewById(R.id.my_shroomies_log);
        myShroomiesTablayout = v.findViewById(R.id.my_shroomies_tablayout);
        myExpensesRecyclerView = v.findViewById(R.id.my_expenses_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        myExpensesRecyclerView.setHasFixedSize(true);
        myExpensesRecyclerView.setLayoutManager(linearLayoutManager);
        shroomieSpinnerFilter = v.findViewById(R.id.shroomie_spinner_filter);
        LinearLayoutManager linearLayoutManager1 =new LinearLayoutManager(getContext());
        myTasksRecyclerView.setHasFixedSize(true);
        myTasksRecyclerView.setLayoutManager(linearLayoutManager1);
        expensesCardsList=new ArrayList<>();
        expensesCardAdapter = new ExpensesCardAdapter(expensesCardsList,getContext(), false,apartment,getParentFragmentManager(),getView());
        myExpensesRecyclerView.setAdapter(expensesCardAdapter);
        tasksCardsList=new ArrayList<>();
        tasksCardAdapter=new TasksCardAdapter(tasksCardsList,getContext(),false,apartment,getParentFragmentManager(),getView());
        myTasksRecyclerView.setAdapter(tasksCardAdapter);

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
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shroomieSpinnerFilter.setAdapter(adapter);
        shroomieSpinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
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
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

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
                addNewCard.show(getParentFragmentManager(),"add new card");

            }
        });
        memberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Members members=new Members();
                Bundle bundle1=new Bundle();
                bundle1.putParcelable("APARTMENT_DETAILS",apartment);
                members.setArguments(bundle1);
                members.show(getParentFragmentManager(),"show member");
            }
        });
        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogFragment logFragment=new LogFragment();
                Bundle bundle=new Bundle();
                bundle.putParcelableArrayList("LOG_LIST",apartmentlogList);
                logFragment.setArguments(bundle);
                logFragment.setTargetFragment(MyShroomies.this,RESULT_CODE);
                fm = getParentFragmentManager();
                ft = fm.beginTransaction();
                ft.addToBackStack(null);
                ft.replace(R.id.fragmentContainer, logFragment);
                ft.commit();
            }
        });
        scroll();


    }
    private void getApartmentDetails (){
        customLoadingProgressBar = new CustomLoadingProgressBar(getActivity(),"loading...",R.raw.loading_animation);
        customLoadingProgressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        customLoadingProgressBar.show();
        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    apartmentID=snapshot.getValue().toString();
                    apartmentListener=rootRef.child("apartments").child(apartmentID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                apartment=snapshot.getValue(ShroomiesApartment.class);
                                retreiveExpensesCards(apartment.getApartmentID());

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

                Snackbar snack=Snackbar.make(v,"this card doesn't exsit anymore", BaseTransientBottomBar.LENGTH_SHORT);
                snack.setAnchorView(R.id.bottomNavigationView);
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
                Snackbar snack=Snackbar.make(v,"this card doesn't exsit anymore", BaseTransientBottomBar.LENGTH_SHORT);
                snack.setAnchorView(R.id.bottomNavigationView);
                snack.show();
            }
        }
    }
}
   private void getApartmentLog(final String apartmentID){
       apartmentlogList=new ArrayList<>();
       logListener=rootRef.child("logs").child(apartmentID).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               apartmentlogList.clear();
               if(snapshot.exists()){
                   for(DataSnapshot sp:snapshot.getChildren()){
                       Log log=sp.getValue(Log.class);
                       apartmentlogList.add(log);
                   }
                   Collections.reverse(apartmentlogList);
                   getUserDetailsForLog(apartmentlogList);


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
                        customLoadingProgressBar.dismiss();
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
        tasksCardAdapter= new TasksCardAdapter(tasksCardsList,getContext(),false,apartment,getParentFragmentManager(),getView());
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
        expensesCardAdapter = new ExpensesCardAdapter(expensesCardsList,getContext(), false,apartment,getParentFragmentManager(),getView());
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


                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
        rootRef.removeEventListener(expensesCardListener);
        rootRef.removeEventListener(tasksCardListener);
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

    @Override
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