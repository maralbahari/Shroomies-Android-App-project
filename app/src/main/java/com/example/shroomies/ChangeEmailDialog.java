package com.example.shroomies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ChangeEmailDialog extends DialogFragment {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private User user;
    private EditText newEmail;
    private View v;
    private email changedEmail;

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setGravity(Gravity.END);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
            showKeyboard();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            changedEmail=(email) getTargetFragment();
        }catch (Exception e) {
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.dialog_fragment_change_email, container, false);
        mAuth=FirebaseAuth.getInstance();
        rootRef=FirebaseDatabase.getInstance().getReference();
        return v;
    }
    public interface email{
        void sendEmailBack(String emailTxt);
    }
    public void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
    public void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newEmail = v.findViewById(R.id.enter_new_email);
        Button doneButton = v.findViewById(R.id.change_email_done_button);
        ImageButton backButton = v.findViewById(R.id.change_email_back_button);
        Bundle bundle=this.getArguments();
        if (bundle!=null) {
            user=bundle.getParcelable("USER");
            newEmail.setText(user.getEmail());
        } else {
//            todo error handling when bundle is null
            dismiss();
        }
        newEmail.setOnTouchListener((view1, motionEvent) -> {
            final int DRAWABLE_RIGHT = 2;
            if (motionEvent.getAction()== MotionEvent.ACTION_UP){
                if(motionEvent.getRawX()>=(newEmail.getRight()-newEmail.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    newEmail.setText("");
                    return true;
                }
            }
            return false;
        });


        doneButton.setOnClickListener(v -> {
            FirebaseUser firebaseUser=mAuth.getCurrentUser();
            if (firebaseUser!=null) {
                String txtEmail = newEmail.getText().toString();
                updateEmail(txtEmail);
            } else{
                //            todo error handling when user is null or signed out

            }
        });

        backButton.setOnClickListener(v -> {
            if (!user.getEmail().equals(newEmail.getText().toString())) {
                Toast.makeText(getContext(),"unSaved Changes",Toast.LENGTH_LONG).show();
            } else {
                dismiss();
            }
        });

    }
    private void updateEmail(String txtEmail) {
        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("email", txtEmail);
        rootRef.child(Config.users).child(user.getUserID()).updateChildren(updateDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                changedEmail.sendEmailBack(txtEmail);
                closeKeyboard();
                Toast.makeText(getActivity(), "Updated email successfully", Toast.LENGTH_SHORT).show();
                dismiss();
//                    FirebaseUser firebaseUser=mAuth.getCurrentUser();
//                    sendEmailVerification(firebaseUser);
            }
        }).addOnFailureListener(e -> Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void sendEmailVerification(FirebaseUser firebaseUser) {
//        firebaseUser.updateEmail(newEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if(task.isSuccessful()){
//                   // Toast.makeText(getContext(), "Error! Try again", Toast.LENGTH_SHORT).show();
                    firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Updated Successfully. Verification email sent to " + firebaseUser.getEmail(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                            startActivity(intent);

                        } else {
                            Toast.makeText(getActivity(), "Failed to send verification email", Toast.LENGTH_SHORT).show();
                        }
                    });
//                }
//                else {
//
//                }
//            }
//        });
    }

    @Override
    public void onStop() {
        super.onStop();
        closeKeyboard();
    }
}