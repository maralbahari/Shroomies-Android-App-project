package com.example.shroomies;

import android.net.ParseException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;


public class MyShroomies extends Fragment {
    View v;
    Button  memberButton, addCardButton;
    TabLayout myShroomiesTablayout;
    RecyclerView myShroomiesRecyclerView;
    Spinner shroomieSpinnerFilter;
    ArrayList<String> options = new ArrayList<>();
    FragmentTransaction ft;
    FragmentManager fm;
    DatabaseReference rootRef;
    FirebaseAuth mAuth;
    ArrayList<TasksCard> tasksCardsList;
    ArrayList<ExpensesCard> expensesCardsList;
    TasksCardAdapter tasksCardAdapter;
    ExpensesCardAdapter expensesCardAdapter;
    String tabSelected="expenses";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_my_shroomies, container, false);
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        memberButton = v.findViewById(R.id.my_shroomies_member_btn);
        addCardButton = v.findViewById(R.id.my_shroomies_add_card_btn);
        myShroomiesTablayout = v.findViewById(R.id.my_shroomies_tablayout);
        myShroomiesRecyclerView = v.findViewById(R.id.my_shroomies_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        myShroomiesRecyclerView.setHasFixedSize(true);
        myShroomiesRecyclerView.setLayoutManager(linearLayoutManager);
        expensesCardsList = new ArrayList<>();
        expensesCardAdapter = new ExpensesCardAdapter(expensesCardsList,getContext(),false);
        shroomieSpinnerFilter = v.findViewById(R.id.shroomie_spinner_filter);
        retreiveExpensesCards();
        myShroomiesTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    tabSelected="expenses";
                    expensesCardsList = new ArrayList<>();
                    retreiveExpensesCards();


                }
                else if(tab.getPosition()==1){
                    tabSelected="tasks";
                    tasksCardsList=new ArrayList<>();
                   retrieveTaskCards();
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
                addNewCard.setArguments(bundle);
                addNewCard.show(getFragmentManager(),"add new card");

            }
        });

    }

    private void retrieveTaskCards() {
        tasksCardsList=new ArrayList<>();
        tasksCardAdapter= new TasksCardAdapter(tasksCardsList,getContext(),false);
        myShroomiesRecyclerView.setAdapter(tasksCardAdapter);
        rootRef.child("apartments").child(mAuth.getCurrentUser().getUid()).child("tasksCards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tasksCardsList.clear();
                if (snapshot.exists()) {

                    for (DataSnapshot sp : snapshot.getChildren()) {
                        TasksCard tasksCard = sp.getValue(TasksCard.class);
                        tasksCardsList.add(tasksCard);


                    }

                    tasksCardAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public void retreiveExpensesCards(){
//        Toast.makeText(getContext(),"HKOADKOSKAD",Toast.LENGTH_SHORT).show();
        expensesCardsList=new ArrayList<>();
        expensesCardAdapter = new ExpensesCardAdapter(expensesCardsList,getContext(), false);
        myShroomiesRecyclerView.setAdapter(expensesCardAdapter);
        rootRef.child("apartments").child(mAuth.getCurrentUser().getUid()).child("expensesCards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expensesCardsList.clear();
                if (snapshot.exists()) {

                    for (DataSnapshot sp : snapshot.getChildren()) {
                        ExpensesCard expensesCard = sp.getValue(ExpensesCard.class);
                        expensesCardsList.add(expensesCard);


                    }

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
                    Date dateO1 = null;
                    Date dateO2 = null;
                    String d1 = o1.getDate();
                    String d2 = o2.getDate();
                    SimpleDateFormat format = new SimpleDateFormat("dd-MMMM-yyyy HH:MM:ss aa");
                    try {
                        dateO1 = format.parse(d1);
                        dateO2 = format.parse(d2);

                    } catch (ParseException | java.text.ParseException e) {

                    }

                    return dateO2.compareTo(dateO1);

                }
            });
            expensesCardAdapter.notifyDataSetChanged();
        }else if(tab.equals("tasks")){
            tasksCardsList.sort(new Comparator<TasksCard>() {
                @Override
                public int compare(TasksCard o1, TasksCard o2) {
                    Date dateO1 = null;
                    Date dateO2 = null;
                    String d1 = o1.getDate();
                    String d2 = o2.getDate();
                    SimpleDateFormat format = new SimpleDateFormat("dd-MMMM-yyyy HH:MM:ss aa");
                    try {
                        dateO1 = format.parse(d1);
                        dateO2 = format.parse(d2);

                    } catch (ParseException | java.text.ParseException e) {

                    }

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
                    Date dateO1 = null;
                    Date dateO2 = null;
                    String d1 = o1.getDate();
                    String d2 = o2.getDate();
                    SimpleDateFormat format = new SimpleDateFormat("dd-MMMM-yyyy HH:MM:ss aa");
                    try {
                        dateO1 = format.parse(d1);
                        dateO2 = format.parse(d2);

                    } catch (ParseException | java.text.ParseException e) {

                    }

                    return dateO1.compareTo(dateO2);

                }
            });
            expensesCardAdapter.notifyDataSetChanged();
        }
        else if(tab.equals("tasks")){

            tasksCardsList.sort(new Comparator<TasksCard>() {
                @Override
                public int compare(TasksCard o1, TasksCard o2) {
                    Date dateO1 = null;
                    Date dateO2 = null;
                    String d1 = o1.getDate();
                    String d2 = o2.getDate();
                    SimpleDateFormat format = new SimpleDateFormat("dd-MMMM-yyyy HH:MM:ss aa");
                    try {
                        dateO1 = format.parse(d1);
                        dateO2 = format.parse(d2);

                    } catch (ParseException | java.text.ParseException e) {

                    }

                    return dateO1.compareTo(dateO2);
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


//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        if (myShroomiesTablayout.getSelectedTabPosition()==0) {
//            outState.putBoolean("expenses", true);
//
//        }else{
//            outState.putBoolean("expenses",false);
//        }
//    }




}