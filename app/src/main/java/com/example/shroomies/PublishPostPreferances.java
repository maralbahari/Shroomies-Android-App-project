package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.FieldValue;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PublishPostPreferances extends Fragment {
    private View v;
    private NumberPicker budgetNumberPicker , numberOfRoomMatesNumberPicker;
    private LatLng latLng;
    private String locality,subLocality , description , postType;
    private MaterialButton nextButton;
    private CheckBox maleCB,femaleCB,smokingCB,petCB;
    private LinearLayout numberOfRoommatesLayout;
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;
    private AppCompatActivity appCompatActivity;
    private FragmentManager fm;
    private FragmentTransaction ft;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appCompatActivity = (AppCompatActivity)context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_publish_post_preferances, container, false);
        mAuth=FirebaseAuth.getInstance();
        requestQueue= Volley.newRequestQueue(getActivity());
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();
        if(bundle!=null){
            postType = bundle.getString(Config.POST_TYPE);
            if(postType.equals(Config.APARTMENT_POST)){
                latLng = bundle.getParcelable(Config.SELECTED_LAT_LNG);
                locality = bundle.getString(Config.LOCALITY);
                subLocality = bundle.getString(Config.SUB_LOCALITY);
            }
            description = bundle.getString(Config.DESCRIPTION);

        }else{
            //todo display error
        }

        numberOfRoomMatesNumberPicker = v.findViewById(R.id.roommate_number_picker);

        numberOfRoommatesLayout = v.findViewById(R.id.number_of_roommates_linear_layout);

        budgetNumberPicker = v.findViewById(R.id.budget_number_picker);
        nextButton = v.findViewById(R.id.publish_post_next_button);
        maleCB = v.findViewById(R.id.male_checkbox);
        femaleCB = v.findViewById(R.id.female_checkbox);
        smokingCB = v.findViewById(R.id.smoking_checkbox);
        petCB = v.findViewById(R.id.pet_friendly_checkbox);


        if(postType.equals(Config.PERSONAL_POST)){
            numberOfRoommatesLayout.setVisibility(View.GONE);
            nextButton.setText("Publish post");
        }else{
            numberOfRoomMatesNumberPicker.setMinValue(1);
            numberOfRoomMatesNumberPicker.setMaxValue(10);
        }
        budgetNumberPicker.setMinValue(1);
        budgetNumberPicker.setMaxValue(100);
        // increase the step size of the number picker
        NumberPicker.Formatter formatter = value -> {
            int temp = value * 100;
            return "" + temp;
        };
        budgetNumberPicker.setFormatter(formatter);

        nextButton.setOnClickListener(v -> {
            ArrayList<String> preferences = new ArrayList<>();
            if(maleCB.isChecked()){
                preferences.add(Config.MALE);
            }
            if(femaleCB.isChecked()){
                preferences.add(Config.FEMALE);
            }
            if(smokingCB.isChecked()){
                preferences.add(Config.NON_SMOKING);
            }
            if(petCB.isChecked()){
                preferences.add(Config.PET_FRIENDLY);
            }

            if(postType.equals(Config.PERSONAL_POST)){
                publishPersonalPost(description , budgetNumberPicker.getValue() ,preferences);
            }else{
                int budget = budgetNumberPicker.getValue();
                int numberOfRoommates = numberOfRoomMatesNumberPicker.getValue();
                Bundle bundleToPublishPostImage = new Bundle();
                bundleToPublishPostImage.putString(Config.LOCALITY,  locality);
                bundleToPublishPostImage.putString(Config.SUB_LOCALITY , subLocality);
                bundleToPublishPostImage.putParcelable(Config.SELECTED_LAT_LNG,  latLng);
                bundleToPublishPostImage.putString(Config.DESCRIPTION , description);
                bundleToPublishPostImage.putInt(Config.BUDGET , budget);
                bundleToPublishPostImage.putInt(Config.NUMBER_OF_ROOMMATES  , numberOfRoommates);
                Fragment publishPostImage =  new PublishPostImage();
                publishPostImage.setArguments(bundle);
                getFragment(publishPostImage);

            }
        });
    }

    void publishPersonalPost(String description , int price , List<String> property ){
        FirebaseUser firebaseUser  =  mAuth.getCurrentUser();
        //move to the publish post page while the data is being posted
        firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    JSONObject jsonObject = new JSONObject();
                    JSONObject data = new JSONObject();
                    JSONObject postDetails = new JSONObject();
                    JSONArray preferences  = new JSONArray(property);

                    try {
                        postDetails.put("description" , description);
                        postDetails.put("userID" , firebaseUser.getUid());
                        postDetails.put("price", price);
                        postDetails.put("preferences" , preferences);
                        // add the time of the post
                        postDetails.put("time_stamp", FieldValue.serverTimestamp());
                        jsonObject.put("postDetails", postDetails);
                        jsonObject.put("postType", "postPersonal");
                        data.put("data", jsonObject);

                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_PUBLISH_POST, data, response ->{
                        try {
                            boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                            String message = response.getJSONObject(Config.result).getString(Config.message);
                            if (success) {

                            showCustomToast(message);

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, error -> {

                        showCustomToast("An error occurred while publishing your post");

                    })
                    {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<>();
                            params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                            params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
                            return params;
                        }
                    };
                    requestQueue.add(jsonObjectRequest);
                } else {
//                    todo handle error
                }
                }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
//                todo when token is null
            }
        });


    }
    public void showCustomToast(String msg) {
        appCompatActivity.runOnUiThread(() -> {
            //inflate the custom toast
            LayoutInflater inflater = appCompatActivity.getLayoutInflater();
            // Inflate the Layout
            View layout = inflater.inflate(R.layout.custom_toast,(ViewGroup) appCompatActivity.findViewById(R.id.toast_layout));

            TextView text = (TextView) layout.findViewById(R.id.toast_text);

            // Set the Text to show in TextView
            text.setText(msg);

            Toast toast = new Toast(appCompatActivity.getApplication());

            //Setting up toast position, similar to Snackbar
            toast.setGravity(Gravity.BOTTOM | Gravity.START | Gravity.FILL_HORIZONTAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        });

    }
    private void getFragment (Fragment fragment ) {

        fm = getActivity().getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.replace(R.id.publish_post_container, fragment);
        ft.commit();
    }
}