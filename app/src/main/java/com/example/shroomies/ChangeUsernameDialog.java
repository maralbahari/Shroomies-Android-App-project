package com.example.shroomies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ChangeUsernameDialog extends DialogFragment {
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private EditText username;
    private User user;
    private final int maxChar=20;
    private View v;
    private name changedName;

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setGravity(Gravity.LEFT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
            showKeyboard();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        closeKeyboard();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.EditProfileOptionsAnimation);
    }
    public interface name{
        void sendNameBack(String nameTxt);
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            changedName=(name) getTargetFragment();

        } catch (Exception e) {
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.dialog_fragment_change_username, container, false);
        mAuth=FirebaseAuth.getInstance();
        rootRef=FirebaseDatabase.getInstance().getReference();
        return v;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        username = v.findViewById(R.id.enter_new_username);
        username.requestFocus();
        ImageButton backButton = v.findViewById(R.id.change_name_back_button);
        Button doneButton = v.findViewById(R.id.change_name_done_button);
        TextView nameLimit=v.findViewById(R.id.name_char_limit);
        Bundle bundle=this.getArguments();
        if (bundle!=null) {
            user=bundle.getParcelable("USER");
            username.setText(user.getName());
            username.setSelection(username.getText().length());
            if (!user.getName().equals("")) {
                int currentChar=user.getName().length();
                nameLimit.setText(String.valueOf(maxChar -currentChar));
            } else {
                username.setHint("Name");
                nameLimit.setText(String.valueOf(maxChar));
            }
        } else {
//            todo handle error when bundle is null
            dismiss();
        }
        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int newChar=editable.length();
                nameLimit.setText(String.valueOf((maxChar-newChar)));
            }
        });
        username.setOnTouchListener((view1, motionEvent) -> {
            final int DRAWABLE_RIGHT = 2;
            if (motionEvent.getAction()== MotionEvent.ACTION_UP){
                if(motionEvent.getRawX()>=(username.getRight()-username.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    username.setText("");
                    return true;
                }
            }
            return false;
        });
        doneButton.setOnClickListener(v -> {
            FirebaseUser firebaseUser=mAuth.getCurrentUser();
            if (firebaseUser!=null) {
                String txtName = username.getText().toString().toLowerCase().trim();
                if (txtName.length()>username.getMaxWidth()) {
                    Toast.makeText(getContext(),"your choosen name is too long",Toast.LENGTH_LONG).show();
                } else {
                    if (txtName.equals(user.getName())) {
                        Toast.makeText(getContext(),"No changes have been made",Toast.LENGTH_SHORT).show();
                    }else {
                        updateUsername(txtName);
                    }
                }
            } else {
//                    todo error handling when user is null
                dismiss();
            }
        });
        backButton.setOnClickListener(v -> {
            closeKeyboard();
            if (!user.getName().equals(username.getText().toString())) {
//                TODO ADD ALERT
                Toast.makeText(getContext(),"unSaved Changes",Toast.LENGTH_LONG).show();

            } else {
                dismiss();
            }
                });
    }

    private void updateUsername(String txtName) {
        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("name", txtName);
        rootRef.child(Config.users).child(user.getUserID()).updateChildren(updateDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                changedName.sendNameBack(txtName);
                Toast.makeText(getActivity(), "Updated username successfully", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        }).addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());

    }
    public void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
    public void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
}