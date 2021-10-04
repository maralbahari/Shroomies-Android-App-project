package com.example.shroomies;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.checkbox.MaterialCheckBox;


public class PostTypeDialogFragment extends DialogFragment {

    private View v;
    private MaterialCheckBox roomCheckBox,roommateCheckBox;
    private OnPostTypeChange onPostTypeChange;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.onPostTypeChange = (PublishPost) getTargetFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_post_type, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        roomCheckBox = v.findViewById(R.id.check_box_room);
        roommateCheckBox = v.findViewById(R.id.check_box_roommate);
        LinearLayout dismissLayout = v.findViewById(R.id.collapse_layout);
            dismissLayout.setOnClickListener(v -> PostTypeDialogFragment.this.dismiss());

        if(getArguments()!=null){
            String postType = getArguments().getString(Config.POST_TYPE);
            if(postType.equals(Config.APARTMENT_POST)){
                roomCheckBox.setChecked(true);
                roommateCheckBox.setChecked(false);
            }else{
                roomCheckBox.setChecked(false);
                roommateCheckBox.setChecked(true);
            }
        }


        roomCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!isChecked){
                if(!roommateCheckBox.isChecked()){
                    roomCheckBox.setChecked(true);
                }
            }else{
                roommateCheckBox.setChecked(false);
                onPostTypeChange.onPostTypeChanged(Config.APARTMENT_POST);
            }
        });
        roommateCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(!isChecked){
                if(!roomCheckBox.isChecked()){
                    roommateCheckBox.setChecked(true);

                }
            }else{
                roomCheckBox.setChecked(false);
                onPostTypeChange.onPostTypeChanged(Config.PERSONAL_POST);
            }
        });


    }




}
interface OnPostTypeChange{
    void onPostTypeChanged(String postType);
}
