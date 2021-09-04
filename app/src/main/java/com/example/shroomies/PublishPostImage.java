package com.example.shroomies;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.make.dots.dotsindicator.DotsIndicator;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublishPostImage extends AppCompatActivity {
    private static final int PICK_IMAGE_MULTIPLE = 1;
    public static final int NUMBER_OF_IMAGES_ALLOWED = 5;

    private ViewPager viewPager;
    private DotsIndicator dotsIndicator;
    private ImageButton deleteImageButton;
    private LottieAnimationView lottieAnimationView;
    private TextView addImageTextView;
    private MaterialButton publishPostButton;
    private CardView addImageCardView;

    private ArrayList<Uri> imageUris;
    private ViewPagerAdapter viewPagerAdapter;

    private int currentViewPagerPosition;

    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;

    private LatLng latLng;
    private String locality, subLocality, description, postType;
    private int budget, numberOfRoomMates;
    private ArrayList<String> preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_post_image1);
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(getApplication());

        lottieAnimationView = findViewById(R.id.add_image_animation);
        addImageTextView = findViewById(R.id.add_image_text_view);
        viewPager = findViewById(R.id.view_pager);
        dotsIndicator = findViewById(R.id.dots_indicator);
        deleteImageButton = findViewById(R.id.delete_image_post);
        publishPostButton = findViewById(R.id.publish_post_button);
        addImageCardView = findViewById(R.id.add_image_card_view);
        publishPostButton = findViewById(R.id.publish_post_button);

        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            latLng = bundle.getParcelable(Config.SELECTED_LAT_LNG);
            locality = bundle.getString(Config.LOCALITY);
            subLocality = bundle.getString(Config.SUB_LOCALITY);
            description = bundle.getString(Config.DESCRIPTION);
            budget = bundle.getInt(Config.BUDGET);
            numberOfRoomMates = bundle.getInt(Config.NUMBER_OF_ROOMMATES);
            preferences = bundle.getStringArrayList(Config.PREFERENCES);
        } else {
            //todo display error
        }


        imageUris = new ArrayList<>();

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
        publishPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!imageUris.isEmpty()) {

                    postImagesAddToDatabase(latLng, description, numberOfRoomMates, budget, preferences, imageUris, locality, subLocality);
                } else {
                    //todo display error
                }
            }
        });

    }

    private void pickFromGallery() {
        Dexter.withContext(getApplication()).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied()) {
                            // navigate user to app settings
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getApplication().getPackageName(), null);
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

        if (imageUris.size() > NUMBER_OF_IMAGES_ALLOWED) {
            Toast.makeText(getApplication(), "too many photos", Toast.LENGTH_SHORT).show();
        } else {
            if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_MULTIPLE) {
                // if more than one image is selected
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount(); //evaluate the count before the for loop
                    // get the remaining amount of space left from the pictures that have already been
                    //uploaded if any exist
                    int spaceAvailable = NUMBER_OF_IMAGES_ALLOWED - imageUris.size();
                    if (count > spaceAvailable) {
                        count -= spaceAvailable;
                    }

                    for (int i = 0; i < count; i++) {
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
                    } else {
                        //todo show error
                    }
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
        //if no duplicate found  store the image
        imageUris.add(newImageUri);
        //initalize adapter with the list of uri
        viewPagerAdapter = new ViewPagerAdapter(getApplication(), imageUris);
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
            addImageCardView.setVisibility(View.VISIBLE);
            lottieAnimationView.playAnimation();
        } else {
            addImageCardView.setVisibility(View.GONE);
            lottieAnimationView.pauseAnimation();
            //initalize adapter with the list of uri
            viewPagerAdapter = new ViewPagerAdapter(getApplication(), imageUris);
            // set the view pager to the adapter
            viewPager.setAdapter(viewPagerAdapter);
            // add the indicator to the view pager and set to update on chage od dataset
            dotsIndicator.setViewPager(viewPager);
            viewPager.getAdapter().registerDataSetObserver(dotsIndicator.getDataSetObserver());

            viewPager.setCurrentItem(currentViewPagerPosition);
        }


    }

    void postImagesAddToDatabase(final LatLng locationLtLng, final String description, final int numberRoomMate, final int price, final ArrayList<String> property, final List<Uri> imageUri, final String locality, final String subLocality) {
        publishPostButton.setVisibility(View.INVISIBLE);
        // get the referance of the database
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        //create a storage referance

        UploadTask uploadTask;

        final List<String> IMAGE_URLS = new ArrayList<>();
        for (Uri uri :
                imageUri) {
            StorageReference filePath = storageReference.child("apartment post image").child(uri.getLastPathSegment()
                    + getUniqueName() + ".jpg");
            filePath.putFile(uri).addOnCompleteListener((OnCompleteListener<UploadTask.TaskSnapshot>) task -> {
                if (task.isSuccessful()) {
                    IMAGE_URLS.add(task.getResult().getMetadata().getReference().getPath().toString());
                    // once all the pictures are added to the Storage add the rest of the posts using the urls to the database
                    if (IMAGE_URLS.size() == imageUri.size()) {
                        publishApartmentPost(locationLtLng, description, numberRoomMate, price, property, IMAGE_URLS, locality, subLocality);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
                    postPublishedDialog.setMessageText("an error occured while publishing your post");
                    postPublishedDialog.warningMessage(true);
                }
            });
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

    void publishApartmentPost(LatLng locationLtLng, String description, int numberRoomMate, int price, ArrayList<String>property, List<String> imageUris, String locality, String subLocality) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    JSONObject jsonObject = new JSONObject();
                    JSONObject data = new JSONObject();
                    JSONObject postDetails = new JSONObject();
                    JSONArray preferences  = new JSONArray(property);
                    JSONArray images = new JSONArray(imageUris);
                    try {
                        String hash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(locationLtLng.latitude, locationLtLng.longitude));
                        postDetails.put("description", description);
                        postDetails.put("userID", firebaseUser.getUid());
                        postDetails.put("price", price);
                        postDetails.put("numberOfRoommates", numberRoomMate);
                        postDetails.put("latitude", locationLtLng.latitude);
                        postDetails.put("longitude", locationLtLng.longitude);
                        postDetails.put("preferences", preferences);
                        postDetails.put("image_url", images);
                        postDetails.put("locality", locality);
                        postDetails.put("sub_locality", subLocality);
                        postDetails.put("time_stamp", FieldValue.serverTimestamp());
                        postDetails.put("geoHash", hash);
                        jsonObject.put("postDetails", postDetails);
                        jsonObject.put("postType", "postApartment");
                        data.put("data", jsonObject);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_PUBLISH_POST, data, response -> {
                        try {
                            boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                            String message = response.getJSONObject(Config.result).getString(Config.message);
                            if (success) {
//                                PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
//                                postPublishedDialog.setMessageText(message);
//                                postPublishedDialog.show(getParentFragmentManager(), null);
                                //todo change
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, error -> {
                        PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
                        postPublishedDialog.setMessageText("an error occured while publishing your post");
                        postPublishedDialog.warningMessage(true);
                    }) {
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