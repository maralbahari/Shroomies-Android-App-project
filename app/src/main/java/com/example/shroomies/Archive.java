package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class Archive extends Fragment {
   View view;
   TabLayout archiveTablayout;
   RecyclerView archiveRecyclerview;
   FirebaseAuth mAuth;
   DatabaseReference rootRef ;
   ArrayList<ExpensesCard> expensesCardsList;
   ArrayList<TasksCard> tasksCardsList ;
   TasksCardAdapter tasksCardAdapter;
   ExpensesCardAdapter expensesCardAdapter;
   ShroomiesApartment apartment;
   String apartmentID="";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       view=inflater.inflate(R.layout.fragment_my_archive, container, false);
       mAuth = FirebaseAuth.getInstance();
       rootRef = FirebaseDatabase.getInstance().getReference();

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
        archiveRecyclerview = v.findViewById(R.id.archive_recyclerview);
        archiveTablayout = v.findViewById(R.id.my_tablayout_archive);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        archiveRecyclerview.setHasFixedSize(true);
        archiveRecyclerview.setLayoutManager(linearLayoutManager);
        getApartmentDetails();
        archiveTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    expensesCardsList = new ArrayList<>();
                    retreiveExpensesCards(apartment.getApartmentID());
                }
                else if(tab.getPosition()==1){
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
        tasksCardAdapter = new TasksCardAdapter(tasksCardsList, getContext(),true,apartment,getParentFragmentManager());
        archiveRecyclerview.setAdapter(tasksCardAdapter);

        rootRef.child("archive").child(apartmentID).child("tasksCards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tasksCardsList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot sp: snapshot.getChildren()){
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

    private void retreiveExpensesCards(String apartmentID){

        expensesCardsList = new ArrayList<>();
        expensesCardAdapter = new ExpensesCardAdapter(expensesCardsList, getContext(), true,apartment,getParentFragmentManager());
        archiveRecyclerview.setAdapter(expensesCardAdapter);
        rootRef.child("archive").child(apartmentID).child("expensesCards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                expensesCardsList.clear();
                if (snapshot.exists()){
                    for (DataSnapshot sp: snapshot.getChildren()){
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



}