package com.example.shroomies;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

public class DeleteUser extends DialogFragment {
    private FirebaseAuth mAuth;
    private User user;
    private View v;
    private EditText userPassword;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_delete_user, container, false);
        mAuth=FirebaseAuth.getInstance();
        user=new User();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button cancelButton = v.findViewById(R.id.delete_account_cancel);
        Button doneButton = v.findViewById(R.id.delete_account_done);
        TextView userEmail = v.findViewById(R.id.delete_account_email);
        userPassword=v.findViewById(R.id.delete_account_password);
        Bundle bundle=this.getArguments();
        if (bundle!=null) {
            user=bundle.getParcelable("USER");
            if (user!=null) {
                userEmail.setText(user.getEmail());
                doneButton.setOnClickListener(view1 -> reAuthenticateUser(user.getEmail(), userPassword.getText().toString()));
                cancelButton.setOnClickListener(view12 -> dismiss());
            }

        } else {
            dismiss();
        }

    }

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
    public void onStop() {
        super.onStop();
        closeKeyboard();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }
    public void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }
    public void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(getContext().INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
    private void reAuthenticateUser(String email, String password) {
        FirebaseUser firebaseUser=mAuth.getCurrentUser();
        if (firebaseUser!=null) {
            AuthCredential credential= EmailAuthProvider.getCredential(email,password);
            firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
               if (task.isSuccessful()) {
                   deleteAccount(firebaseUser);
               }
            }).addOnFailureListener(e -> Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show());
        }
    }
    private void deleteAccount(FirebaseUser firebaseUser) {
        firebaseUser.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(),"You are no longer shroomie",Toast.LENGTH_LONG).show();
                mAuth.signOut();
                this.getTargetFragment().getActivity().finish();
            }
        }).addOnFailureListener(e -> Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show());
    }
}