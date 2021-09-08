package com.example.shroomies;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.view.inputmethod.InputMethodManager;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

public class ChangeBioDialog extends DialogFragment {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private TextInputLayout newBio;
    private User user;
    private View v;
    private bio changedBio;
    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setGravity(Gravity.VERTICAL_GRAVITY_MASK);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);

        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Objects.requireNonNull(getDialog()).getWindow().setWindowAnimations(R.style.EditProfileOptionsAnimation);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.dialog_fragment_change_bio, container, false);
        mAuth=FirebaseAuth.getInstance();
        rootRef=FirebaseDatabase.getInstance().getReference();
        return v;
    }
    public interface bio{
        void sendBioBack(String bioTxt);
    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        try {
            changedBio=(bio) getTargetFragment();

        } catch (Exception e) {
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newBio = v.findViewById(R.id.enter_new_bio);
        newBio.requestFocus();
        showKeyboard();
        Button doneButton = v.findViewById(R.id.change_bio_done_button);
        ImageButton backButton = v.findViewById(R.id.change_bio_back_button);
        Bundle bundle=this.getArguments();
        if (bundle!=null) {
            user=bundle.getParcelable("USER");
            if (!user.getBio().equals("")) {
                newBio.getEditText().setText(user.getBio());
                newBio.getEditText().setSelection(newBio.getEditText().getText().length());
            } else {
                newBio.setHint("my bio");
            }
        } else {
//            todo handle error when bundle is null
            closeKeyboard();
            dismiss();

        }
        newBio.setEndIconOnClickListener(view1 -> newBio.getEditText().setText(""));
        doneButton.setOnClickListener(v -> {
            String txtBio = newBio.getEditText().getText().toString().trim();
            if(txtBio.equals(user.getBio())){
                newBio.setError("No changes have been made");
            } else{
                newBio.setError(null);
                FirebaseUser firebaseUser= mAuth.getCurrentUser();
                if (firebaseUser!=null) {
                        updateBio(txtBio);

                }
            }

        });
        backButton.setOnClickListener(v -> {
            if (!user.getBio().equals(newBio.getEditText().getText().toString())) {
//                    todo alert for unsaved changes
                Toast.makeText(getContext(),"unSaved Changes",Toast.LENGTH_LONG).show();
            } else {
                closeKeyboard();
                dismiss();
            }
        });


    }
    private void updateBio(String txtBio) {
        HashMap<String, Object> updateDetails = new HashMap<>();
        updateDetails.put("bio", txtBio);

        rootRef.child(Config.users).child(user.getUserID()).updateChildren(updateDetails).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                changedBio.sendBioBack(txtBio);
                Toast.makeText(getContext(), "Updated your bio", Toast.LENGTH_SHORT).show();
                closeKeyboard();
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

    @Override
    public void onStop() {
        super.onStop();
        closeKeyboard();
    }
}