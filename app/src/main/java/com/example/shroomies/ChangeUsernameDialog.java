package com.example.shroomies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChangeUsernameDialog extends DialogFragment {
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private TextInputLayout newUsernameEditText;
    private User user;
    private View v;
    private ChangeUsernameDialog.username changedUserUsername;
    private CardView helperTxt;
    private static final Pattern pattern = Pattern.compile(Config.USERNAME_PATTERN);

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
    public interface username {
        void sendUserNameBack(String nameTxt);
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            changedUserUsername =(ChangeUsernameDialog.username) getTargetFragment();

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
        newUsernameEditText = v.findViewById(R.id.enter_new_username);
        newUsernameEditText.requestFocus();
        ImageButton backButton = v.findViewById(R.id.change_username_back_button);
        Button doneButton = v.findViewById(R.id.change_username_done_button);
        helperTxt=v.findViewById(R.id.help_card_view_edit_username);
        Bundle bundle=this.getArguments();
        if (bundle!=null) {
            user=bundle.getParcelable("USER");
            newUsernameEditText.getEditText().setText(user.getUsername());
            newUsernameEditText.getEditText().setSelection(newUsernameEditText.getEditText().getText().length());
        } else {
//            todo handle error when bundle is null
            dismiss();
        }
        newUsernameEditText.setErrorIconOnClickListener(v -> {
            if (helperTxt.getVisibility() == View.VISIBLE) {
                helperTxt.setVisibility(View.GONE);
            } else {
                helperTxt.setVisibility(View.VISIBLE);
            }
        });
        newUsernameEditText.setEndIconOnClickListener(v -> {
            if (helperTxt.getVisibility() == View.VISIBLE) {
                helperTxt.setVisibility(View.GONE);
            } else {
                helperTxt.setVisibility(View.VISIBLE);
            }
        });
        doneButton.setOnClickListener(v -> {
            FirebaseUser firebaseUser=mAuth.getCurrentUser();
            if (firebaseUser!=null) {
                String txtName = newUsernameEditText.getEditText().getText().toString().toLowerCase().trim();
                if(validUsername(txtName)){
                    newUsernameEditText.setError(null);
                    checkDuplicateUserName(txtName).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            if(task.getResult().getValue()!=null){
                                newUsernameEditText.setError(txtName+ " is taken");
                            }else{
                                newUsernameEditText.setError(null);
                                newUsernameEditText.setError(null);
                                updateUsername(txtName);
                            }
                        }
                    });

                }else {
                    newUsernameEditText.setError("Please enter valid username");
                }

            } else {
//                    todo error handling when user is null
                dismiss();
            }
        });
        backButton.setOnClickListener(v -> {
            closeKeyboard();
            if (!user.getUsername().equals(newUsernameEditText.getEditText().getText().toString())) {
                newUsernameEditText.setError("Unsaved Changes");
            } else {
                newUsernameEditText.setError(null);
                dismiss();
            }
        });
    }

    private void updateUsername(String txtName) {
        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("username", txtName);
        rootRef.child(Config.users).child(user.getUserID()).updateChildren(updateDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                changedUserUsername.sendUserNameBack(txtName);
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
    public static boolean validUsername(final String username) {
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }
    private Task<DataSnapshot> checkDuplicateUserName(String userName){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference(Config.usersnames);
        return rootRef.orderByKey().equalTo(userName).get().addOnCompleteListener(task -> { });
    }
}