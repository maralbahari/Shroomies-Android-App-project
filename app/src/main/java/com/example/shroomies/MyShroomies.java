package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


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
    ArrayList<ExpensesCard>cardsList;
    ExpensesCardAdapter expensesCardAdapter;


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
        shroomieSpinnerFilter = v.findViewById(R.id.shroomie_spinner_filter);
        retreiveCards();
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.spinner_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shroomieSpinnerFilter.setAdapter(adapter);

        shroomieSpinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getSelectedItemPosition()==0){
                    sortAccordingtoImportance();
                }else if (adapterView.getSelectedItemPosition()==1){
                    sortAccordingToDate();
                }else{
                    sortAccordingToTitle();
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
                addNewCard.show(getFragmentManager(),"add new card");
            }
        });

    }

    public void retreiveCards(){
//        Toast.makeText(getContext(),"HKOADKOSKAD",Toast.LENGTH_SHORT).show();
        cardsList=new ArrayList<>();
        expensesCardAdapter = new ExpensesCardAdapter(cardsList,getContext());

        rootRef.child("expensesCard").child(mAuth.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()){

//                    for (DataSnapshot sp:snapshot.getChildren()){
                        ExpensesCard expensesCard= snapshot.getValue(ExpensesCard.class);
                        cardsList.add(expensesCard);
                        Toast.makeText(getContext(),""+cardsList,Toast.LENGTH_SHORT).show();

//                    }
                    myShroomiesRecyclerView.setAdapter(expensesCardAdapter);
                    expensesCardAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



    public void sortAccordingtoImportance(){

    }

    public void sortAccordingToTitle(){

    }

    public void sortAccordingToDate(){

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (myShroomiesTablayout.getSelectedTabPosition()==0) {
            outState.putBoolean("expenses", true);
        }else{
            outState.putBoolean("expenses",false);
        }
    }

    private void getFragment (Fragment fragment) {
        fm = getActivity().getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.addToBackStack(null);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();


    }


}