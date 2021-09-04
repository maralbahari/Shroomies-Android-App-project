package com.example.shroomies;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.icu.text.NumberFormat;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.make.dots.dotsindicator.DotsIndicator;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class PublishPost extends Fragment  implements MapsFragment.OnLocationSet {
    public static final int DIALOG_FRAGMENT_REQUEST_CODE = 1;
    public static final int MAPS_FRAGMENT_REQUEST_CODE = 2;
    public  static final int NUMBER_OF_IMAGES_ALLOWED = 5;
    private static final int PICK_IMAGE_MULTIPLE= 1;

    private View v;
    private TextView locationTextView ;
    private ImageView userImageView;
    private ChipGroup postTypeChipGroup;
    private EditText descriptionEditText;
    private MaterialButton nextButton;
//
//    private int currentViewPagerPosition;
    private String address,locality,subLocality;
    private LatLng selectedLatLng;
    private Geocoder geocoder;


//    private List<Uri> imageUri;
//    private List<String> preferences ;
//    private ViewPagerAdapter viewPagerAdapter;

    private FragmentManager fm;
    private FragmentTransaction ft;


    private RequestQueue requestQueue;
    //    String postUniqueName;

    private FirebaseAuth mAuth;
    private StorageReference filePath;
//
//    private Validate validate;

    // override the interface method "sendNewLocation" to get the preferances
    @Override
    public void sendNewLocation(LatLng selectedLatLng, String selectedAddress , String selectedLocationName) {
        this.selectedLatLng = selectedLatLng;
        this.address = selectedAddress;
        this.address = selectedAddress;
        locationTextView.setText(selectedLocationName +" "+ selectedAddress);
        geocoder = new Geocoder(getActivity());
        try {
            List<Address> addresses = geocoder.getFromLocation(selectedLatLng.latitude , selectedLatLng.longitude , 1);
            if(addresses!=null){
                if(addresses.size()>0) {
                    Address address =  addresses.get(0);
                    locality = address.getLocality();
                    subLocality = address.getSubLocality();
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    @Override
    public void onStart() {
        super.onStart();
//        MainActivity.btm_view.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
//        MainActivity.btm_view.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
//        MainActivity.btm_view.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        MainActivity.btm_view.setVisibility(View.VISIBLE);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        MainActivity.btm_view.setVisibility(View.VISIBLE);
    }



    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_publish_post, container, false);
        mAuth=FirebaseAuth.getInstance();
        requestQueue= Volley.newRequestQueue(getActivity());
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        locationTextView = v.findViewById(R.id.location_chip_view);

        postTypeChipGroup = v.findViewById(R.id.chip_group);
        nextButton = v.findViewById(R.id.publish_post_next_button);

        descriptionEditText = v.findViewById(R.id.post_description);


        userImageView = v.findViewById(R.id.user_image_publish_post);



        if(selectedLatLng ==null){
            getLocation();
        }else{
            setCurrentLocationAddress(selectedLatLng.latitude, selectedLatLng.longitude);
        }

        getUserImage();
        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity() , PublishPostPreferances.class);
            String postType;
            Toast.makeText(getActivity(),postTypeChipGroup.getCheckedChipId()+" "+R.id.apartment_post_chip, Toast.LENGTH_LONG).show();
            if(postTypeChipGroup.getCheckedChipId()==R.id.apartment_post_chip){
                postType = Config.APARTMENT_POST;
            }else{
                postType = Config.PERSONAL_POST;
            }
            if(postType.equals(Config.APARTMENT_POST)&&locality!=null&&subLocality!=null&& descriptionEditText.getText().toString().trim()!=null){
                intent.putExtra(Config.LOCALITY,  locality);
                intent.putExtra(Config.SUB_LOCALITY , subLocality);
                intent.putExtra(Config.SELECTED_LAT_LNG,  selectedLatLng);
                intent.putExtra(Config.DESCRIPTION , descriptionEditText.getText().toString().trim());
                intent.putExtra(Config.POST_TYPE ,postType);
                startActivity(intent);
            }else if( descriptionEditText.getText().toString().trim()!=null){
                intent.putExtra(Config.DESCRIPTION , descriptionEditText.getText().toString().trim());
                intent.putExtra(Config.POST_TYPE ,postType);
                startActivity(intent);


            }else{
                //todo handle
            }

        });


        // sets focus to the edit text as soon as the page is open
        descriptionEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(descriptionEditText, InputMethodManager.SHOW_IMPLICIT);

//        addImageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // get the photo chosen
//                openGallery();
//            }
//        });

        //when location button is pressed open the map fragement
        locationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapsFragment mapsFragment = new MapsFragment();
                mapsFragment.setTargetFragment(PublishPost.this ,MAPS_FRAGMENT_REQUEST_CODE );
                mapsFragment.show(getParentFragmentManager() , null);
            }
        });

//        publishPostButton.setOnClickListener(v -> {
//            // list to hold all selected values from properties
//            if(postTypeChipGroup.getCheckedChipId()==0) {
//                //check if the latlng , locality have been added correctly
//                // the rest of the  data is checked using the validate class
//                // this is done to display to the user that  the location is
//                //erroneous and must be changed
//                if(locality !=null && selectedLatLng!=null){
//                    postImagesAddToDatabase(selectedLatLng
//                            , descriptionEditText.getText().toString()
//                            , numberOfRoommates
//                            , budget
//                            , preferences
//                            , imageUri
//                            , locality
//                            , subLocality);
//                }else{
//                    PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
//                    postPublishedDialog.warningMessage(true);
//                    postPublishedDialog.setMessageText("the location entered is invalid");
//                    postPublishedDialog.show(getParentFragmentManager() , null);
//                }
//            }else{
//                publishPersonalPost(descriptionEditText.getText().toString()
//                        , budget
//                        , preferences);
//            }
//
//        });

//        deleteImageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                deleteFromView();
//            }
//        });

//        preferencesImageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //open a preference dialog fragment
//                PreferencesDialogFragment preferencesDialogFragment = new PreferencesDialogFragment(postTypeChipGroup.getCheckedChipId());
//                preferencesDialogFragment.setTargetFragment(PublishPost.this , DIALOG_FRAGMENT_REQUEST_CODE);
//                preferencesDialogFragment.show(getParentFragmentManager() ,null);
//            }
//        });
//
//        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
//            @Override
//            public void onPageSelected(int position) {
//                //set the index of the current image
//                currentViewPagerPosition = position;
//            }
//            @Override
//            public void onPageScrollStateChanged(int state) { }
//        });

//        postTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });
        postTypeChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId==1){
                //if user selects personal post remove some of the options
//                viewPager.setVisibility(View.GONE);
//                dotsIndicator.setVisibility(View.GONE);
//                deleteImageButton.setVisibility(View.GONE);
//                numberOfRoomMatesTextView.setVisibility(View.INVISIBLE);
//                addImageButton.setVisibility(View.GONE);
//                locationImageButton.setVisibility(View.GONE);
                locationTextView.setVisibility(View.GONE);
            }else{
//                if(imageUri!=null){
//                    if(imageUri.size()!=0){
//                        viewPager.setVisibility(View.VISIBLE);
//                        dotsIndicator.setVisibility(View.VISIBLE);
//                        deleteImageButton.setVisibility(View.VISIBLE);
//                    }
//                }
//
//                numberOfRoomMatesTextView.setVisibility(View.VISIBLE);
//                addImageButton.setVisibility(View.VISIBLE);
//                locationImageButton.setVisibility(View.VISIBLE);
                locationTextView.setVisibility(View.VISIBLE);

            }

        });



    }


    private  void getLocation() {
        new CurrentLocationAsync(getActivity()).execute();
    }



    private class CurrentLocationAsync extends AsyncTask<Void , String , LatLng> {
        FusedLocationProviderClient fusedLocationProviderClient;
        double latitude;
        double longitude;
        Context context;


        CurrentLocationAsync(Context context){
            this.context = context;
        }
        @Override
        protected LatLng doInBackground(Void... voids) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Check if permission to access fine location is granted
                if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //get the location of the phone in order to set the pin on the map to the current Location
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
                    //check the API level
                    //get the location
                    fusedLocationProviderClient.getLastLocation()
                            .addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        latitude = location.getLatitude();
                                        longitude = location.getLongitude();
                                        selectedLatLng = new LatLng(latitude, longitude);

                                        setCurrentLocationAddress(latitude , longitude);
                                        // get the icon and resize it to set it as the marker
                                    }
                                }
                            });
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    getLocation();
                }
            }
            return selectedLatLng;
        }
    }
    void setCurrentLocationAddress(double latitude, double longitude)  {
        geocoder = new Geocoder(getActivity());
        List<Address> addresses;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
           addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
           if(addresses!=null){
               if(addresses.size()>0){
                   locality = addresses.get(0).getLocality();
                   subLocality = addresses.get(0).getSubLocality();
                   String mAddress = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                   String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                   if(mAddress!=null || knownName!=null) {
                       address = knownName + " , " + mAddress;
                       locationTextView.setText(address);

                   }
               }
           }


        } catch (IOException e) {
            e.printStackTrace();
        }



    }



//    private void openGallery() {
//        //add permisision denied handlers
//        Intent image = new Intent();
//        image.setAction(Intent.ACTION_GET_CONTENT);
//        image.setType("image/*");
//        image.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//        startActivityForResult(image, PICK_IMAGE_MULTIPLE);
//
//    }
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data){
//        super.onActivityResult(requestCode, resultCode, data);
//        boolean duplicateFound = false;
//        Uri selectedImageUri;
//
//
//        if(imageUri.size()>NUMBER_OF_IMAGES_ALLOWED){
//            Toast.makeText(getActivity(), "too many photos" , Toast.LENGTH_SHORT).show();
//        }else {
//            if (resultCode == getActivity().RESULT_OK && requestCode == PICK_IMAGE_MULTIPLE) {
//                // if more than one image is selected
//                if (data.getClipData()!=null){
//                    int count = data.getClipData().getItemCount(); //evaluate the count before the for loop
//                    // get the remaining amount of space left from the pictures that have already been
//                    //uploaded if any exist
//                    int spaceAvailable = NUMBER_OF_IMAGES_ALLOWED-imageUri.size();
//                    if(count>spaceAvailable){
//                        count-= spaceAvailable;
//                    }
//                    for(int i = 0; i < count; i++) {
//                        selectedImageUri = data.getClipData().getItemAt(i).getUri();
//                        if (!imageUri.contains(selectedImageUri)) {
//                            addToViewPager(selectedImageUri);
//                        }
//                    }
//
//                }
//                 // if one image is selected
//               else  if (data.getData() != null){
//                    selectedImageUri = data.getData();
//                    //check if the selected uri already exists in tge list
//                    for (Uri uri : imageUri) {
//                        //if duplicate found break
//                        if (data.getData().equals(uri)) {
//                            duplicateFound = true;
//                            break;
//                        }
//                    }
//                if (!duplicateFound) {
//                    addToViewPager(selectedImageUri);
//                } else {
//
//                }
//            }
//            }
//        }
//    }


//    void postImagesAddToDatabase(final LatLng locationLtLng , final  String description , final int numberRoomMate , final int price , final  List<String> property , final List<Uri> imageUri  , final String locality , final String subLocality) {
//        getFragment(new FindRoommate());
//        publishPostButton.setVisibility(View.INVISIBLE);
//
//        // get the referance of the database
//        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
//
//        //create a storage referance
//
//        UploadTask uploadTask;
//
//        final List<String> IMAGE_URLS = new ArrayList<>();
//        for (Uri uri:
//                imageUri ) {
//            filePath = storageReference.child("apartment post image").child(uri.getLastPathSegment()
//                    +getUniqueName()+".jpg");
//            filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                    if(task.isSuccessful()) {
//                        IMAGE_URLS.add(task.getResult().getMetadata().getReference().getPath().toString());
//                        // once all the pictures are added to the Storage add the rest of the posts using the urls to the database
//                        if(IMAGE_URLS.size() == imageUri.size()){
//                            publishApartmentPost(locationLtLng ,  description ,  numberRoomMate ,  price , property ,IMAGE_URLS  ,  locality  , subLocality);
//                        }
//                    }
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
//                    postPublishedDialog.setMessageText("an error occured while publishing your post");
//                    postPublishedDialog.warningMessage(true);
//                }
//            });
//        }
//
//    }
//    private String getUniqueName(){
//        //create a unique id for the post by combining the date with uuid
//        //get the date first
//        Calendar calendarDate = Calendar.getInstance();
//        SimpleDateFormat  currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
//        String saveCurrentDate = currentDate.format(calendarDate.getTime());
//
//        //get the time in hours and minutes
//        Calendar calendarTime = Calendar.getInstance();
//        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss.SSS");
//        String saveCurrentTime = currentTime.format(calendarTime.getTime());
//
//        //add the two string together
//
//        return  saveCurrentDate+saveCurrentTime;
//    }

    void publishApartmentPost(LatLng locationLtLng , String description , int numberRoomMate , int price , List<String> property , List<String> IMAGE_URL , String locality , String subLocality){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    JSONObject jsonObject = new JSONObject();
                    JSONObject data = new JSONObject();
                    JSONObject postDetails = new JSONObject();
                    try {
                        String hash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(locationLtLng.latitude, locationLtLng.longitude));
                        postDetails.put("description" , description);
                        postDetails.put("userID" , firebaseUser.getUid());
                        postDetails.put("price", price);
                        postDetails.put("numberOfRoommates" , numberRoomMate);
                        postDetails.put("latitude", locationLtLng.latitude);
                        postDetails.put("longitude",locationLtLng.longitude);
                        postDetails.put("preferences" , property);
                        postDetails.put("image_url" , IMAGE_URL );
                        postDetails.put("locality" , locality);
                        postDetails.put("sub_locality" , subLocality);
                        postDetails.put("time_stamp", FieldValue.serverTimestamp());
                        postDetails.put("geoHash" , hash);
                        jsonObject.put("postDetails", postDetails);
                        jsonObject.put("postType", "postApartment");
                        data.put("data", jsonObject);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_PUBLISH_POST, data, response ->{
                        try {
                            boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                            String message = response.getJSONObject(Config.result).getString(Config.message);
                            if (success) {
                                PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
                                postPublishedDialog.setMessageText(message);
                                postPublishedDialog.show(getParentFragmentManager() , null);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, error -> {
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

//        mDocRef.collection("postApartment").add(post).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentReference> task) {
//                PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
//                postPublishedDialog.show(getParentFragmentManager() , null);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
//                postPublishedDialog.setMessageText("an error occured while publishing your post");
//                postPublishedDialog.warningMessage(true);
//            }
//        });

    }

//    void publishPersonalPost(String description , int price , List<String> property ){
//        FirebaseUser firebaseUser  =  mAuth.getCurrentUser();
//        //move to the publish post page while the data is being posted
//        getFragment(new PublishPost());
//        publishPostButton.setVisibility(View.INVISIBLE);
//        firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//            @Override
//            public void onComplete(@NonNull @NotNull Task<GetTokenResult> task) {
//                if (task.isSuccessful()) {
//                    String token = task.getResult().getToken();
//                    JSONObject jsonObject = new JSONObject();
//                    JSONObject data = new JSONObject();
//                    JSONObject postDetails = new JSONObject();
//                    try {
//                        postDetails.put("description" , description);
//                        postDetails.put("userID" , firebaseUser.getUid());
//                        postDetails.put("price", price);
//                        postDetails.put("preferences" , property);
//                        // add the time of the post
//                        postDetails.put("time_stamp",FieldValue.serverTimestamp());
//                        jsonObject.put("postDetails", postDetails);
//                        jsonObject.put("postType", "postPersonal");
//                        data.put("data", jsonObject);
//
//                    }catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_PUBLISH_POST, data, response ->{
//                        try {
//                            boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
//                            String message = response.getJSONObject(Config.result).getString(Config.message);
//                            if (success) {
//                                PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
//                                postPublishedDialog.setMessageText(message);
//                                postPublishedDialog.show(getParentFragmentManager() , null);
//                            }
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }, error -> {
////  todo later change this dialog thingy to snack bar or something similar

//                        PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
//                        postPublishedDialog.setMessageText("an error occured while publishing your post");
//                        postPublishedDialog.warningMessage(true);
//                    })
//                    {
//                        @Override
//                        public Map<String, String> getHeaders() {
//                            Map<String, String> params = new HashMap<>();
//                            params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
//                            params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
//                            return params;
//                        }
//                    };
//                    requestQueue.add(jsonObjectRequest);
//                } else {
////                    todo handle error
//                }
//                }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull @NotNull Exception e) {
////                todo when token is null
//            }
//        });
//
////        mDocRef.collection("postPersonal").add(post).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
////            @Override
////            public void onComplete(@NonNull Task<DocumentReference> task) {
////                PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
////                postPublishedDialog.show(getParentFragmentManager() , null);
////            }
////        }).addOnFailureListener(new OnFailureListener() {
////            @Override
////            public void onFailure(@NonNull Exception e) {
////                PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
////                postPublishedDialog.setMessageText("An error occurred while publishing your post");
////                postPublishedDialog.warningMessage(true);
////            }
////        });
////        idk why this get fragment is here
////        getFragment(new FindRoommate());
//
//
//    }

//    void addToViewPager(Uri newImageUri){
//
//        viewPager.setVisibility(View.VISIBLE);
//        dotsIndicator.setVisibility(View.VISIBLE);
//        deleteImageButton.setVisibility(View.VISIBLE);
//        //if no duplicate found  store the image
//        imageUri.add(newImageUri);
//        validate.notifyDataChanged();
//        //initalize adapter with the list of uri
//        viewPagerAdapter = new ViewPagerAdapter(getActivity(), imageUri);
//        // set the view pager to the adapter
//        viewPager.setAdapter(viewPagerAdapter);
//        // add the indicator to the view pager and set to update on chage od dataset
//        dotsIndicator.setViewPager(viewPager);
//        viewPager.getAdapter().registerDataSetObserver(dotsIndicator.getDataSetObserver());
//    }
//
//    void deleteFromView(){
//        if(imageUri.size()<=1){
//            imageUri.remove(0);
//        }else {
//            imageUri.remove(currentViewPagerPosition);
//        }
//        validate.notifyDataChanged();
//        //initalize adapter with the list of uri
//        viewPagerAdapter = new ViewPagerAdapter(getActivity(), imageUri);
//        // set the view pager to the adapter
//        viewPager.setAdapter(viewPagerAdapter);
//        // add the indicator to the view pager and set to update on chage od dataset
//        dotsIndicator.setViewPager(viewPager);
//        viewPager.getAdapter().registerDataSetObserver(dotsIndicator.getDataSetObserver());
//        if (imageUri.size() == 0) {
//            viewPager.setVisibility(View.GONE);
//            dotsIndicator.setVisibility(View.GONE);
//            deleteImageButton.setVisibility(View.GONE);
//        }
//        viewPager.setCurrentItem(currentViewPagerPosition);
//
//    }
    private void getFragment (Fragment fragment) {

        fm = getActivity().getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.addToBackStack(null);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();

    }
    private void getUserImage(){

        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser= mAuth.getCurrentUser();
        rootRef.child(Config.users).child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String url = snapshot.child("image").getValue(String.class);
                    if(!url.isEmpty()){
                        GlideApp.with(getContext())
                                .load(url)
                                .centerCrop()
                                .circleCrop()
                                .into(userImageView);
                        userImageView.setPadding(2,2,2,2);
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
//class Validate{
//    Context context;
//    DataChangedEvent listener;
//    EditText description;
//    TextView numberOfRoommates , budgetTextView , locationTextView, warningTextView;
//    List<Uri> imageUri;
//    Button publishPostButton;
//    ChipGroup chipGroup;
//    LatLng selectedLatLng;
//
//
//    public Validate(Context context , DataChangedEvent listener,  LatLng  selectedLatLng,EditText description, TextView numberOfRoommates, TextView budgetTextView, TextView locationTextView, List<Uri> imageUri ,Button  publishPostButton , TextView warningTextView , ChipGroup chipGroup) {
//        this.context = context;
//        this.listener = listener;
//        this.description = description;
//        this.numberOfRoommates = numberOfRoommates;
//        this.budgetTextView = budgetTextView;
//        this.locationTextView = locationTextView;
//        this.imageUri = imageUri;
//        this.publishPostButton = publishPostButton;
//        this.warningTextView = warningTextView;
//        this.chipGroup = chipGroup;
//        this.selectedLatLng = selectedLatLng;
//    }
//
//    void notifyDataChanged(){
//        boolean dataIsComplete = false;
//                if(chipGroup.getCheckedChipId()==0) {
//                  dataIsComplete= !locationTextView.getText().toString().trim().isEmpty()
//                            && !budgetTextView.getText().toString().trim().isEmpty()
//                            && !numberOfRoommates.getText().toString().trim().isEmpty()
//                            && !description.getText().toString().trim().isEmpty()
//                            && !imageUri.isEmpty()
//                            ;
//                }else{
//                    dataIsComplete= !budgetTextView.getText().toString().trim().isEmpty()
//                            && !description.getText().toString().trim().isEmpty();
//                }
//                listener.onDataComplete(context , dataIsComplete , publishPostButton , warningTextView);
//    }
//
//
//static class OnDataChangedListener implements DataChangedEvent {
//    @Override
//    public void onDataComplete(Context context , boolean dataIsComplete , Button publishPostButton , TextView warningTextView) {
//        if(!dataIsComplete){
//            //removes the shadow
//            publishPostButton.setOutlineProvider(null);
//            publishPostButton.setClickable(false);
//            publishPostButton.setBackground(context.getDrawable(R.drawable.button_round_greyish_timber_wolf));
//            warningTextView.setVisibility(View.VISIBLE);
//        }else{
//            //adds the shadow
//            publishPostButton.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
//            publishPostButton.setBackground(context.getDrawable(R.drawable.button_round_alabaster));
//            publishPostButton.setClickable(true);
//            warningTextView.setVisibility(View.GONE);
//        }
//    }
//}
//}



