package com.example.shroomies;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class PublishPostPreferences extends Fragment {
    private View v;
    private TextInputEditText budgetEditText, roomMatesEditText;
    private LatLng latLng;
    private String locality, subLocality, description, postType, buildingAddress, buildingName, buildingType;
    ArrayList<String> buildingTypes;
    private CheckBox maleCB, femaleCB, smokingCB, petCB, alcoholCB;
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;
    private AppCompatActivity appCompatActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appCompatActivity = (AppCompatActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_publish_post_preferances, container, false);
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(getActivity());
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            postType = bundle.getString(Config.POST_TYPE);
            if (postType.equals(Config.APARTMENT_POST)) {
                buildingName = bundle.getString(Config.BUILDING_NAME);
                buildingAddress = bundle.getString(Config.BUILDING_ADDRESS);
                buildingType = bundle.getString(Config.BUILDING_TYPE);
            } else {
                buildingTypes = bundle.getStringArrayList(Config.BUILDING_TYPES);
            }
            latLng = bundle.getParcelable(Config.SELECTED_LAT_LNG);
            locality = bundle.getString(Config.LOCALITY);
            subLocality = bundle.getString(Config.SUB_LOCALITY);
            description = bundle.getString(Config.DESCRIPTION);

        }

        roomMatesEditText = v.findViewById(R.id.roommate_number_edit_text);

        LinearLayout numberOfRoommatesLayout = v.findViewById(R.id.number_of_roommates_linear_layout);

        budgetEditText = v.findViewById(R.id.budget_edit_text);
        MaterialButton nextButton = v.findViewById(R.id.publish_post_next_button);
        maleCB = v.findViewById(R.id.male_checkbox);
        femaleCB = v.findViewById(R.id.female_checkbox);
        smokingCB = v.findViewById(R.id.smoking_checkbox);
        petCB = v.findViewById(R.id.pet_friendly_checkbox);
        alcoholCB = v.findViewById(R.id.alcohol_checkbox);


        if (postType.equals(Config.PERSONAL_POST)) {
            numberOfRoommatesLayout.setVisibility(View.GONE);
            nextButton.setText("Publish post");
        }

        nextButton.setOnClickListener(v -> {
            int numberOfRoommates = 0;
            int budget = 0;
            StringBuilder preferences = new StringBuilder();
            if (maleCB.isChecked()) {
                preferences.append("1");
            } else {
                preferences.append("0");
            }
            if (femaleCB.isChecked()) {
                preferences.append("1");
            } else {
                preferences.append("0");
            }
            if (smokingCB.isChecked()) {
                preferences.append("1");
            } else {
                preferences.append("0");
            }
            if (petCB.isChecked()) {
                preferences.append("1");
            } else {
                preferences.append("0");
            }
            if (alcoholCB.isChecked()) {
                preferences.append(1);
            } else {
                preferences.append(0);
            }

            if (roomMatesEditText.getText() != null) {
                if (!roomMatesEditText.getText().toString().isEmpty()) {
                    numberOfRoommates = Integer.parseInt(roomMatesEditText.getText().toString());
                }
            }
            if (budgetEditText.getText() != null) {
                if (!budgetEditText.getText().toString().isEmpty()) {
                    budget = Integer.parseInt(budgetEditText.getText().toString());
                }
            }
            if (postType.equals(Config.PERSONAL_POST)) {
                publishPersonalPost(Integer.parseInt(budgetEditText.getText().toString()), preferences.toString());
            } else {
                Bundle bundleToPublishPostImage = new Bundle();
//                bundleToPublishPostImage.putString(Config.PREFERENCE , new JSONArray(preferences).toString());
                bundleToPublishPostImage.putString(Config.PREFERENCE, preferences.toString());
                bundleToPublishPostImage.putString(Config.LOCALITY, locality);
                bundleToPublishPostImage.putString(Config.SUB_LOCALITY, subLocality);
                bundleToPublishPostImage.putParcelable(Config.SELECTED_LAT_LNG, latLng);
                bundleToPublishPostImage.putString(Config.BUILDING_TYPE, buildingType);
                bundleToPublishPostImage.putString(Config.BUILDING_NAME, buildingName);
                bundleToPublishPostImage.putString(Config.BUILDING_ADDRESS, buildingAddress);
                bundleToPublishPostImage.putString(Config.DESCRIPTION, description);
                bundleToPublishPostImage.putInt(Config.BUDGET, budget);
                bundleToPublishPostImage.putInt(Config.NUMBER_OF_ROOMMATES, numberOfRoommates);
                Fragment publishPostImage = new PublishPostImage();
                publishPostImage.setArguments(bundleToPublishPostImage);
                getFragment(publishPostImage);
            }
        });
    }

    void publishPersonalPost(int price, String preferences) {
        getActivity().onBackPressed();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        //move to the publish post page while the data is being posted
        firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult().getToken();
                JSONObject jsonObject = new JSONObject();
                JSONObject data = new JSONObject();
                JSONObject postDetails = new JSONObject();
                String hash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(latLng.latitude, latLng.longitude));
                try {
                    //create an array of keywords
                    //for the search
                    ArrayList<String> keyWords = new ArrayList(Arrays.asList(description.trim().split("[?U\\W]")));
                    keyWords.add(locality);
                    keyWords.add(subLocality);
                    //remove null and empty strings
                    keyWords.removeIf(item -> item == null || "".equals(item));
                    //create a set to remove duplicate strings
                    Set<String> set = new HashSet<>(keyWords);
                    List<String> filteredList = new ArrayList<>(set);
                    //convert strings to lower case
                    filteredList.replaceAll(String::toLowerCase);
                    postDetails.put(Config.DESCRIPTION, description);
                    postDetails.put(Config.KEY_WORDS, new JSONArray(filteredList));
                    postDetails.put(Config.userID, firebaseUser.getUid());
                    postDetails.put(Config.PRICE, price);
                    postDetails.put(Config.PREFERENCE, preferences);
                    postDetails.put(Config.LOCALITY, locality);
                    postDetails.put(Config.SUB_LOCALITY, subLocality);
                    postDetails.put(Config.GEO_HASH, hash);
                    postDetails.put(Config.BUILDING_TYPES, new JSONArray(buildingTypes));
                    postDetails.put(Config.LATITUDE, latLng.latitude);
                    postDetails.put(Config.LONGITUDE, latLng.longitude);
                    // add the time of the post
                    postDetails.put(Config.TIME_STAMP, "");
                    jsonObject.put(Config.POST_DETAILS, postDetails);
                    jsonObject.put(Config.POST_TYPE, Config.PERSONAL_POST);
                    data.put(Config.data, jsonObject);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_PUBLISH_POST, data, response -> {
                    try {
                        boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                        String message = response.getJSONObject(Config.result).getString(Config.message);
                        if (success) {

                            showCustomToast(message);

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, error -> showCustomToast("An error occurred while publishing your post")) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                        params.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                        return params;
                    }
                };
                requestQueue.add(jsonObjectRequest);
            } else {
                new CustomToast((PublishPostActivity) getActivity(), "An error occurred while publishing your post").showCustomToast();
            }
        });


    }

    public void showCustomToast(String msg) {
        appCompatActivity.runOnUiThread(() -> {
            //inflate the custom toast
            LayoutInflater inflater = appCompatActivity.getLayoutInflater();
            // Inflate the Layout
            View layout = inflater.inflate(R.layout.custom_toast, appCompatActivity.findViewById(R.id.toast_layout));

            TextView text = layout.findViewById(R.id.toast_text);

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

    private void getFragment(Fragment fragment) {

        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.publish_post_container, fragment);
        ft.commit();
    }
}