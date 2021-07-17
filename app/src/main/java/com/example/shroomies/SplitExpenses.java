package com.example.shroomies;

import android.content.Context;
import android.graphics.Color;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class SplitExpenses extends DialogFragment implements UserAdapterSplitExpenses.ShroomiesShares {
  //views
  private View v;
  private TextView totalAmountTextView;
  private ImageButton closeImageButton;
  private Button nextButtonSplitExpenses;
  private EditText amountEditText;
  private RecyclerView splitExpensesRecyclerView;
  private Fragment splitExpenses;
  //model
  private ShroomiesApartment apartment;
  //data structures
  private ArrayList<User> memebersList =new ArrayList<>();
  private HashMap<String, Integer> sharedSplitMap = new HashMap<>();
  private UserAdapterSplitExpenses splitAdapter;

  //values
  private boolean acceptedInput;
  int totalAmount = 0;
  private String enteredAmount="0";
  //interface
  private membersShares myMembersShares;

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
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       v= inflater.inflate(R.layout.fragment_split_expenses, container, false);
       return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        closeImageButton =v.findViewById(R.id.cancle_split);
        nextButtonSplitExpenses =v.findViewById(R.id.next_button_split_expenses);
        amountEditText =v.findViewById(R.id.amount_split);
        splitExpensesRecyclerView =v.findViewById(R.id.shroomie_split_recycler);
        totalAmountTextView =v.findViewById(R.id.total_amount_split);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        splitExpensesRecyclerView.setHasFixedSize(true);
        splitExpensesRecyclerView.setLayoutManager(linearLayoutManager);

        splitAdapter= new UserAdapterSplitExpenses(this);
        splitExpensesRecyclerView.setAdapter(splitAdapter);

        final Bundle bundle = this.getArguments();
        splitExpenses=this;

        if(bundle!=null){
            apartment=bundle.getParcelable("APARTMENT_DETAILS");
            memebersList =bundle.getParcelableArrayList("MEMBERS");
            setShroomiesToRecycler(memebersList,enteredAmount);
        }

        closeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enteredAmount=charSequence.toString();
                splitAdapter= new UserAdapterSplitExpenses(getContext(),memebersList , enteredAmount,SplitExpenses.this);
                splitExpensesRecyclerView.setAdapter(splitAdapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        nextButtonSplitExpenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(acceptedInput){
                    if(sharedSplitMap !=null){
                        myMembersShares.sendInput(sharedSplitMap);
                        dismiss();
                    }

                }else{
                   Snackbar snack= Snackbar.make(view,"please split properly", BaseTransientBottomBar.LENGTH_SHORT);
                    View v = snack.getView();
                    FrameLayout.LayoutParams params =(FrameLayout.LayoutParams)v.getLayoutParams();
                    params.gravity = Gravity.TOP;
                    v.setLayoutParams(params);
                    snack.show();


                }

            }
        });


    }

    @Override
    public void sendInput(HashMap<String, Integer> sharesHash) {
        this.totalAmount = 0;
        this.sharedSplitMap =sharesHash;
        if (!sharedSplitMap.isEmpty()) {
            for (Integer amount : sharedSplitMap.values()) {
                this.totalAmount += amount;

            }
            if (this.totalAmount > Integer.parseInt(enteredAmount)) {
                acceptedInput=false;
                totalAmountTextView.setText( totalAmount + ">" + enteredAmount);
                totalAmountTextView.setTextColor(Color.RED);

            }else if(this.totalAmount<Integer.parseInt(enteredAmount)){
                acceptedInput=false;
                totalAmountTextView.setText( totalAmount + " < " + enteredAmount);
                totalAmountTextView.setTextColor(Color.RED);

            } else {
                acceptedInput=true;
                totalAmountTextView.setTextColor(getActivity().getColor(R.color.lightGrey));
                totalAmountTextView.setText( totalAmount + "RM");
            }
        }
    }


    public interface membersShares{
        void sendInput(HashMap<String, Integer> sharedSplit);
    }
    private void setShroomiesToRecycler(ArrayList<User> shroomies,String amount){
        splitAdapter= new UserAdapterSplitExpenses(getContext(),shroomies,amount,this);
        splitExpensesRecyclerView.setAdapter(splitAdapter);
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