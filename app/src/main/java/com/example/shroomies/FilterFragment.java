package com.example.shroomies;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;

public class FilterFragment extends DialogFragment {
    RadioGroup sortByRadioGroup;
    CheckBox maleCheckBox , femaleCheckBox ,  petsAllowedCheckBox , smokeFreeCheckBox;
    Button  okButton;
    View v;
    boolean newest , oldest , male , female , pets, nonSmoking = false;
    Bundle bundle;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         v = inflater.inflate(R.layout.fragment_filter, container, false);
        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        okButton = v.findViewById(R.id.ok_button_filter);
        sortByRadioGroup = v.findViewById(R.id.radio_group);
        maleCheckBox = v.findViewById(R.id.male_check_box_filter);
        femaleCheckBox  =  v.findViewById(R.id.female_check_box_filter);
        petsAllowedCheckBox = v.findViewById(R.id.pets_allowd_check_box_filter);
        smokeFreeCheckBox = v.findViewById(R.id.non_smoking_check_box_filter);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle =  getFilters();

                dismiss();
            }


        });

    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        getParentFragment().setArguments(bundle);

    }

    private Bundle getFilters () {
        switch (sortByRadioGroup.getCheckedRadioButtonId()){
            case R.id.newest_r_button:
                newest = true;
                break;
            case R.id.oldest_r_button:
                oldest = true;
                break;
        }
        male = maleCheckBox.isChecked();
        female = femaleCheckBox.isChecked();
        pets = petsAllowedCheckBox .isChecked();
        nonSmoking = smokeFreeCheckBox.isChecked();
        Bundle bundle = new Bundle ();
        bundle.putBoolean("MALE" , male);
        bundle.putBoolean("FEMALE" , female);
        bundle.putBoolean("PETS" , pets);
        bundle.putBoolean("NON_SMOKING" , nonSmoking);
        return bundle;

    }




}