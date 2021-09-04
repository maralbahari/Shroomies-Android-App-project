package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
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
import com.google.gson.JsonArray;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PublishPostPreferances extends AppCompatActivity {
    private NumberPicker budgetNumberPicker , numberOfRoomMatesNumberPicker;
    private LatLng latLng;
    private String locality,subLocality , description , postType;
    private MaterialButton nextButton;
    private CheckBox maleCB,femaleCB,smokingCB,petCB;
    private LinearLayout numberOfRoommatesLayout;
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_post_preferances);
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(getApplication());
        Bundle bundle = getIntent().getExtras();
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

        numberOfRoomMatesNumberPicker = findViewById(R.id.roommate_number_picker);

        numberOfRoommatesLayout = findViewById(R.id.number_of_roommates_linear_layout);

        budgetNumberPicker = findViewById(R.id.budget_number_picker);
        nextButton = findViewById(R.id.publish_post_next_button);
        maleCB = findViewById(R.id.male_checkbox);
        femaleCB = findViewById(R.id.female_checkbox);
        smokingCB = findViewById(R.id.smoking_checkbox);
        petCB =  findViewById(R.id.pet_friendly_checkbox);


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
                Intent intent = new Intent(getApplication() , PublishPostImage.class);
                intent.putExtra(Config.LOCALITY,  locality);
                intent.putExtra(Config.SUB_LOCALITY , subLocality);
                intent.putExtra(Config.SELECTED_LAT_LNG,  latLng);
                intent.putExtra(Config.DESCRIPTION , description);
                intent.putExtra(Config.BUDGET , budget);
                intent.putExtra(Config.NUMBER_OF_ROOMMATES  , numberOfRoommates);
                intent.putStringArrayListExtra(Config.PREFERENCES ,preferences);
                startActivity(intent);
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
//                                PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
//                                postPublishedDialog.setMessageText(message);
//                                postPublishedDialog.show(getParentFragmentManager() , null);
                                Toast.makeText(getApplicationContext() ,"post published" ,Toast.LENGTH_SHORT).show();
                                // todo show success
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, error -> {
//  todo later change this dialog thingy to snack bar or something similar

                        PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
                        postPublishedDialog.setMessageText("an error occured while publishing your post");
                        postPublishedDialog.warningMessage(true);
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
}