package com.example.shroomies;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class SplitExpenses extends DialogFragment {
  private View v;
  private TextView cancel,ok,totalText;
  private EditText amount;
  private RecyclerView splitRecycler;
  private ShroomiesApartment apartment;
  private ArrayList<User> shroomiesList;
  private DatabaseReference rootRef;
  private FirebaseAuth mAuth;
  private UserAdapterSplitExpenses splitAdapter;
  private String enteredAmount="0";
  private HashMap<String,Object> sharedSplit=new HashMap<>();
  membersShares myMembersShares;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.dialogfragment_add_member);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       v= inflater.inflate(R.layout.fragment_split_expenses, container, false);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
       return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cancel=v.findViewById(R.id.cancle_split);
        ok=v.findViewById(R.id.ok_split);
        amount=v.findViewById(R.id.amount_split);
        splitRecycler=v.findViewById(R.id.shroomie_split_recycler);
        totalText=v.findViewById(R.id.total_amount_split);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        splitRecycler.setHasFixedSize(true);
        splitRecycler.setLayoutManager(linearLayoutManager);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        final Bundle bundle = this.getArguments();

        if(bundle!=null){
            apartment=bundle.getParcelable("APARTMENT_DETAILS");
            shroomiesList=bundle.getParcelableArrayList("MEMBERS");
            setShroomiesToRecycler(shroomiesList,enteredAmount);
        }
        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               enteredAmount=charSequence.toString();
                splitAdapter= new UserAdapterSplitExpenses(apartment,shroomiesList,getContext(),enteredAmount,totalText);
                splitRecycler.setAdapter(splitAdapter);


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             sharedSplit= splitAdapter.getMembersSplitShares();
             myMembersShares.sendInput(sharedSplit);
             dismiss();
            }
        });


    }
    public interface membersShares{
        void sendInput(HashMap<String,Object> sharedSplit);
    }
    private void setShroomiesToRecycler(ArrayList<User> shroomies,String amount){
        splitAdapter= new UserAdapterSplitExpenses(apartment,shroomies,getContext(),amount,totalText);
        splitRecycler.setAdapter(splitAdapter);
        splitAdapter.notifyDataSetChanged();
    }
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
           myMembersShares = (membersShares) getTargetFragment();
        }catch(ClassCastException e){

        }
    }
}