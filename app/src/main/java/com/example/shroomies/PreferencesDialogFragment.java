package com.example.shroomies;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class PreferencesDialogFragment extends DialogFragment {
    View v;
    NumberPicker budgetNumberPicker,numberOfRoomMatesNumberPicker;
    Button closeButton, setOptions;
    CheckBox maleCheckbox,femaleCheckBox,petAllowedCheckBox,nonSmokingCheckBox;
    OnPreferencesSet mOnPreferencesSet;
    LinearLayout numberOfRoomatesLayout;
    int tabSelected ;

    public interface OnPreferencesSet{

        void sendInput(int budget , int numberRoomMates ,List<Boolean> preferences);

    }


    PreferencesDialogFragment(int tabSelected){
        this.tabSelected = tabSelected;
     }


    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
        }
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.dialog_fragment_preferences, container, false);
       numberOfRoomatesLayout = v.findViewById(R.id.number_of_roommates_linear_layout);
        if(tabSelected == 1){
            numberOfRoomatesLayout.setVisibility(View.GONE);
        }
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        budgetNumberPicker = v.findViewById(R.id.budget_number_picker);
        closeButton = v.findViewById(R.id.close_button);
        setOptions = v.findViewById(R.id.set_options);
        maleCheckbox = v.findViewById(R.id.male_check_box_publish_post);
        femaleCheckBox = v.findViewById(R.id.female_check_box_post);
        petAllowedCheckBox = v.findViewById(R.id.pets_allowd_check_box_post);
        nonSmokingCheckBox =v.findViewById(R.id.non_smoking_check_box_post);
        numberOfRoomMatesNumberPicker = v.findViewById(R.id.roommate_number_picker);
        budgetNumberPicker = v.findViewById(R.id.budget_number_picker);
        numberOfRoomMatesNumberPicker = v.findViewById(R.id.roommate_number_picker);
        if(tabSelected!=1) {
            numberOfRoomMatesNumberPicker.setMinValue(1);
            numberOfRoomMatesNumberPicker.setMaxValue(10);

        }
        budgetNumberPicker.setMinValue(1);
        budgetNumberPicker.setMaxValue(100);
        // increase the step size of the number picker
        NumberPicker.Formatter formatter = new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                int temp = value * 100;
                return "" + temp;
            }
        };

        budgetNumberPicker.setFormatter(formatter);

        setOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               List <Boolean> preferences = new ArrayList<>();
               preferences.add(maleCheckbox.isChecked());
               preferences.add(femaleCheckBox.isChecked());
               preferences.add(petAllowedCheckBox.isChecked());
               preferences.add(nonSmokingCheckBox.isChecked());
                int budget = budgetNumberPicker.getValue()*100;
                int numberRooMates = numberOfRoomMatesNumberPicker.getValue();
                if(tabSelected!=1) {
                    mOnPreferencesSet.sendInput(budget, numberRooMates, preferences);
                }else{
                    mOnPreferencesSet.sendInput(budget, 0, preferences);
                }
                dismiss();
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mOnPreferencesSet = (OnPreferencesSet) getTargetFragment();
        }catch(ClassCastException e){

        }
    }
}
