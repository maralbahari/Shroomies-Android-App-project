package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class Archive extends Fragment {
   View view;
   TabLayout archiveTablayout;
   private RecyclerView expensesRecyclerview;
   private RecyclerView tasksRecyclerView;
   FirebaseAuth mAuth;
   DatabaseReference rootRef ;
   ArrayList<ExpensesCard> expensesCardsList;
   ArrayList<TasksCard> tasksCardsList ;
   TasksCardAdapter tasksCardAdapter;
   ExpensesCardAdapter expensesCardAdapter;
   ShroomiesApartment apartment;
   String apartmentID="";
   private ValueEventListener expensesCardListener;
   private ValueEventListener taskCardsListener;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       view=inflater.inflate(R.layout.fragment_my_archive, container, false);
       mAuth = FirebaseAuth.getInstance();
       rootRef = FirebaseDatabase.getInstance().getReference();
      getApartmentDetails();
    return view;
    }
    private void getApartmentDetails (){
        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    apartmentID=snapshot.getValue().toString();
                    rootRef.child("apartments").child(apartmentID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                apartment=snapshot.getValue(ShroomiesApartment.class);
                                retreiveExpensesCards(apartment.getApartmentID());
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



    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        expensesRecyclerview= v.findViewById(R.id.archive_recyclerview_expenses);
        tasksRecyclerView=v.findViewById(R.id.archive_recyclerview_task);
        archiveTablayout = v.findViewById(R.id.my_tablayout_archive);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext());
        expensesRecyclerview.setHasFixedSize(true);
        expensesRecyclerview.setLayoutManager(linearLayoutManager);
        tasksRecyclerView.setHasFixedSize(true);
        tasksRecyclerView.setLayoutManager(linearLayoutManager1);
        expensesCardsList=new ArrayList<>();
        expensesCardAdapter = new ExpensesCardAdapter(expensesCardsList,getContext(), false,apartment,getParentFragmentManager(),getView());
        expensesRecyclerview.setAdapter(expensesCardAdapter);


        archiveTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    archiveTablayout.setSelectedTabIndicator(R.drawable.tab_indicator_left);
                    tasksRecyclerView.setVisibility(View.GONE);
                    expensesRecyclerview.setVisibility(View.VISIBLE);
                    expensesCardsList = new ArrayList<>();
                    retreiveExpensesCards(apartment.getApartmentID());
                }
                else if(tab.getPosition()==1){
                    archiveTablayout.setSelectedTabIndicator(R.drawable.tab_indicator_right);
                    tasksRecyclerView.setVisibility(View.VISIBLE);
                    expensesRecyclerview.setVisibility(View.GONE);
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
    }

    private void retrieveTaskCards(String apartmentID) {
        tasksCardsList = new ArrayList<>();
        tasksCardAdapter = new TasksCardAdapter(tasksCardsList, getContext(),true,apartment,getParentFragmentManager(),getView());
        ItemTouchHelper.Callback callback = new CardsTouchHelper(tasksCardAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        tasksCardAdapter.setItemTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(tasksRecyclerView);
        tasksRecyclerView.setAdapter(tasksCardAdapter);
       taskCardsListener= rootRef.child("archive").child(apartmentID).child("tasksCards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tasksCardsList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot sp: snapshot.getChildren()){
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

    private void retreiveExpensesCards(String apartmentID){

        expensesCardsList = new ArrayList<>();
        expensesCardAdapter = new ExpensesCardAdapter(expensesCardsList, getContext(), true,apartment,getParentFragmentManager(),getView());
        ItemTouchHelper.Callback callback = new CardsTouchHelper(expensesCardAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        expensesCardAdapter.setItemTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(expensesRecyclerview);
        expensesRecyclerview.setAdapter(expensesCardAdapter);
        expensesCardListener=rootRef.child("archive").child(apartmentID).child("expensesCards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expensesCardsList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot sp: snapshot.getChildren()){
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        rootRef.removeEventListener(expensesCardListener);
        rootRef.removeEventListener(taskCardsListener);
    }
}