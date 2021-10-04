package com.example.shroomies;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.make.dots.dotsindicator.DotsIndicator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PublishPostImage extends Fragment {
    private static final int PICK_IMAGE_MULTIPLE = 1;
    public static final int NUMBER_OF_IMAGES_ALLOWED = 5;
    private View v;
    private ViewPager viewPager;
    private DotsIndicator dotsIndicator;
    private ImageButton deleteImageButton;
    private LottieAnimationView lottieAnimationView;
    private MaterialButton publishPostButton, addMoreImagesButton;
    private CardView addImageCardView;
    private AppCompatActivity appCompatActivity;
    private RelativeLayout rootLayout;


    private ArrayList<Uri> imageUris;
    private ViewPagerAdapter viewPagerAdapter;

    private int currentViewPagerPosition;

    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;

    private LatLng latLng;
    private String locality, subLocality, description,buildingAddress,buildingName,buildingType , preferences;
    private int budget, numberOfRoomMates;
//    private boolean male,female,nonSmoking,petFriendly;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        appCompatActivity =(AppCompatActivity)context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_publish_post_image, container, false);
        mAuth=FirebaseAuth.getInstance();
        requestQueue= Volley.newRequestQueue(getActivity());
        return v;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lottieAnimationView = v.findViewById(R.id.add_image_animation);
        viewPager = v.findViewById(R.id.view_pager);
        dotsIndicator = v.findViewById(R.id.dots_indicator);
        deleteImageButton = v.findViewById(R.id.delete_image_post);
        publishPostButton = v.findViewById(R.id.publish_post_button);
        addImageCardView = v.findViewById(R.id.add_image_card_view);
        publishPostButton = v.findViewById(R.id.publish_post_button);
        addMoreImagesButton = v.findViewById(R.id.add_more_images_button);
        rootLayout = v.findViewById(R.id.root_layout);

        if (this.getArguments()!=null) {
            Bundle bundle = this.getArguments();
//            male = bundle.getBoolean(Config.MALE);
//            female = bundle.getBoolean(Config.FEMALE);
//            nonSmoking = bundle.getBoolean(Config.NON_SMOKING);
//            petFriendly = bundle.getBoolean(Config.PET_FRIENDLY);
            preferences = bundle.getString(Config.PREFERENCE);
            latLng = bundle.getParcelable(Config.SELECTED_LAT_LNG);
            locality = bundle.getString(Config.LOCALITY);
            subLocality = bundle.getString(Config.SUB_LOCALITY);
            description = bundle.getString(Config.DESCRIPTION);
            budget = bundle.getInt(Config.BUDGET);
            numberOfRoomMates = bundle.getInt(Config.NUMBER_OF_ROOMMATES);
            buildingName = bundle.getString(Config.BUILDING_NAME);
            buildingAddress = bundle.getString(Config.BUILDING_ADDRESS);
            buildingType = bundle.getString(Config.BUILDING_TYPE);
        }
        imageUris = new ArrayList<>();
//        Log.d("prefs" , preferences.toString());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //set the index of the current image
                currentViewPagerPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        deleteImageButton.setOnClickListener(v -> deleteFromView());
        addImageCardView.setOnClickListener(v -> pickFromGallery());
        addMoreImagesButton.setOnClickListener(v -> pickFromGallery());
        publishPostButton.setOnClickListener(v -> {
            if (!imageUris.isEmpty()) {
                postImagesAddToDatabase(imageUris);
            } else {
                new CustomToast((PublishPostActivity)getActivity() , "Please add an image of your place" ).showCustomToast();
            }
        });
    }



    private void pickFromGallery() {
        Dexter.withContext(getActivity()).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                            // navigate user to app settings
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        } else {
                            Intent image = new Intent();
                            image.setAction(Intent.ACTION_GET_CONTENT);
                            image.setType("image/*");
                            image.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            startActivityForResult(image, PICK_IMAGE_MULTIPLE);
                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }

                }).onSameThread().check();


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean duplicateFound = false;
        Uri selectedImageUri;
        int spaceAvailable = NUMBER_OF_IMAGES_ALLOWED - imageUris.size();

        getActivity();
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_MULTIPLE) {
            // if more than one image is selected
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount(); //evaluate the count before the for loop
                // get the remaining amount of space left from the pictures that have already been
                //uploaded if any exist
                Snackbar.make(rootLayout, "space is  "+spaceAvailable , Snackbar.LENGTH_SHORT).show();

                for (int i = 0; i < count; i++) {
                    if(i>spaceAvailable){
                        break;
                    }
                    selectedImageUri = data.getClipData().getItemAt(i).getUri();
                    if (!imageUris.contains(selectedImageUri)) {
                        addToViewPager(selectedImageUri);
                    }
                }
            }
            // if one image is selected
            else if (data.getData() != null) {
                selectedImageUri = data.getData();
                //check if the selected uri already exists in tge list
                for (Uri uri : imageUris) {
                    //if duplicate found break
                    if (data.getData().equals(uri)) {
                        duplicateFound = true;
                        break;
                    }
                }
                if (!duplicateFound) {
                    addToViewPager(selectedImageUri);
                }
            }
        }

    }

    void addToViewPager(Uri newImageUri) {
        viewPager.setVisibility(View.VISIBLE);
        dotsIndicator.setVisibility(View.VISIBLE);
        deleteImageButton.setVisibility(View.VISIBLE);
        addImageCardView.setVisibility(View.GONE);
        lottieAnimationView.pauseAnimation();
        if(imageUris.size()==NUMBER_OF_IMAGES_ALLOWED-1){
            addMoreImagesButton.setVisibility(View.GONE);
        }else{
            addMoreImagesButton.setVisibility(View.VISIBLE);
        }
        //if no duplicate found  store the image
        imageUris.add(newImageUri);
        //initalize adapter with the list of uri
        viewPagerAdapter = new ViewPagerAdapter(getActivity(), imageUris);
        // set the view pager to the adapter
        viewPager.setAdapter(viewPagerAdapter);
        // add the indicator to the view pager and set to update on chage od dataset
        dotsIndicator.setViewPager(viewPager);
        viewPager.getAdapter().registerDataSetObserver(dotsIndicator.getDataSetObserver());
    }

    void deleteFromView() {
        if (imageUris.size() <= 1) {
            imageUris.remove(0);
        } else {
            imageUris.remove(currentViewPagerPosition);
        }
        if (imageUris.size() == 0) {
            viewPager.setVisibility(View.GONE);
            dotsIndicator.setVisibility(View.GONE);
            deleteImageButton.setVisibility(View.GONE);
            addMoreImagesButton.setVisibility(View.GONE);
            addImageCardView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        } else{
            if(imageUris.size()==NUMBER_OF_IMAGES_ALLOWED){
                addMoreImagesButton.setVisibility(View.GONE);
            }else{
                addMoreImagesButton.setVisibility(View.VISIBLE);
            }

            lottieAnimationView.pauseAnimation();
            //initalize adapter with the list of uri
            viewPagerAdapter = new ViewPagerAdapter(getActivity(), imageUris);
            // set the view pager to the adapter
            viewPager.setAdapter(viewPagerAdapter);
            // add the indicator to the view pager and set to update on chage od dataset
            dotsIndicator.setViewPager(viewPager);
            viewPager.getAdapter().registerDataSetObserver(dotsIndicator.getDataSetObserver());

            viewPager.setCurrentItem(currentViewPagerPosition);
        }



    }

    void postImagesAddToDatabase(final List<Uri> imageUri) {
        publishPostButton.setVisibility(View.INVISIBLE);
        getActivity().onBackPressed();
        // get the referance of the database
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        //create a storage referance
        final List<String> imageUrls = new ArrayList<>();
        int counter=0;

        String uniqueId = getUniqueName();

        for (Uri uri :
                imageUri) {
            StorageReference filePath = storageReference.child(Config.APARTMENT_POST_IMAGE).child(uniqueId);
          filePath.child(counter+".jpg").putFile(uri).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                imageUrls.add(task.getResult().getMetadata().getReference().getPath());
                // once all the pictures are added to the Storage add the rest of the posts using the urls to the database
                if (imageUrls.size() == imageUri.size()) {
                    publishApartmentPost(imageUrls , uniqueId);
                }
            }else{
                    showCustomToast("An error occurred while uploading your images ");
                }
          });
            counter++;

        }


    }






    private String getUniqueName() {
        //create a unique id for the post by combining the date with uuid
        //get the date first
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        String saveCurrentDate = currentDate.format(calendarDate.getTime());

        //get the time in hours and minutes
        Calendar calendarTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss.SSS");
        String saveCurrentTime = currentTime.format(calendarTime.getTime());

        //add the two string together

        return mAuth.getUid() + saveCurrentDate + saveCurrentTime;
    }

    void publishApartmentPost(List<String> imageUris , String imageFolderPath) {

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult().getToken();
                JSONObject jsonObject = new JSONObject();
                JSONObject data = new JSONObject();
                JSONObject postDetails = new JSONObject();
                JSONArray images = new JSONArray(imageUris);
                try {
                    String hash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(latLng.latitude, latLng.longitude));
                    ArrayList<String> buildingAddressForQuery = new ArrayList(Arrays.asList(buildingAddress.trim().split("[?U\\W]")));
                    if(buildingName!=null){
                        ArrayList<String> buildingNameForQuery = new ArrayList(Arrays.asList(buildingName.trim().split("[?U\\W]")));
                        buildingAddressForQuery.addAll(buildingNameForQuery);
                    }
                    //remove null and empty strings
                    buildingAddressForQuery.removeIf(item -> item == null || "".equals(item));
                    //create a set to remove duplicate strings
                    Set<String> set = new HashSet<>(buildingAddressForQuery);
                    List<String> filteredList = new ArrayList<>(set);
                    //convert strings to lower case
                    filteredList.replaceAll(String::toLowerCase);
                    postDetails.put(Config.IMAGE_FOLDER_PATH ,imageFolderPath);
                    postDetails.put(Config.DESCRIPTION, description);
                    postDetails.put(Config.userID, firebaseUser.getUid());
                    postDetails.put(Config.PRICE, budget);
                    postDetails.put(Config.NUMBER_OF_ROOMMATES, numberOfRoomMates);
                    postDetails.put(Config.LATITUDE, latLng.latitude);
                    postDetails.put(Config.LONGITUDE, latLng.longitude);
                    postDetails.put(Config.PREFERENCE, preferences);
                    postDetails.put(Config.IMAGE_URL, images);
                    postDetails.put(Config.LOCALITY, locality);
                    postDetails.put(Config.SUB_LOCALITY, subLocality);
                    postDetails.put(Config.TIME_STAMP, "");
                    postDetails.put(Config.GEO_HASH, hash);
                    postDetails.put(Config.BUILDING_NAME  , buildingName);
                    postDetails.put(Config.BUILDING_ADDRESS , new JSONArray(filteredList));
                    postDetails.put(Config.BUILDING_TYPE , buildingType);
                    jsonObject.put(Config.POST_DETAILS, postDetails);
                    jsonObject.put(Config.POST_TYPE, Config.APARTMENT_POST);
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
                new CustomToast((PublishPostActivity)getActivity() , "An error occurred while publishing your post" ).showCustomToast();
            }
        }).addOnFailureListener(e -> {
//                todo when token is null
        });

    }



    public void showCustomToast(String msg) {
        appCompatActivity.runOnUiThread(() -> {
            //inflate the custom toast
            LayoutInflater inflater = appCompatActivity.getLayoutInflater();
            // Inflate the Layout
            View layout = inflater.inflate(R.layout.custom_toast, appCompatActivity.findViewById(R.id.toast_layout));

            TextView text =layout.findViewById(R.id.toast_text);

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
}