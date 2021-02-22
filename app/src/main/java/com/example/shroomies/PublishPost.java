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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.make.dots.dotsindicator.DotsIndicator;

import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
public class PublishPost extends Fragment  implements PreferencesDialogFragment.OnPreferencesSet , MapsFragment.OnLocationSet {
    public static final int DIALOG_FRAGMENT_REQUEST_CODE = 1;
    public static final int MAPS_FRAGMENT_REQUEST_CODE = 2;
    public  static final int NUMBER_OF_IMAGES_ALLOWED = 5;
    View v;
    FragmentManager fm;
    FragmentTransaction ft;
    String address;
    LatLng latLng;
    private static final int PICK_IMAGE_MULTIPLE= 1;
    List<Uri> imageUri;
    private TextView locationTextView , preferencesTextView;
    Geocoder geocoder;
    FirebaseFirestore mDocRef = FirebaseFirestore.getInstance();

    ImageView maleImage,femaleImage,petImage,smokingImage, userImageView;
    Button publishPostButton ,addImageButton,locationImageButton , preferencesImageButton;
    ViewPager viewPager;
    private DotsIndicator dotsIndicator;
    ImageView deleteImageButton;
    ViewPagerAdapter viewPagerAdapter;
    int currentViewPagerPosition;
    private TabLayout postTabLayout;
    EditText descriptionEditText;
    StorageReference filePath;
//    String postUniqueName;
    TextView budgetTextView, numberOfRoomMatesTextView ,addImageWarning;
    int budget , numberOfRoommates;
    List<Boolean> preferences ;
    Validate validate;



    // override the interface method "sendNewLocation" to get the preferances
    @Override
    public void sendNewLocation(LatLng selectedLatLng, String selectedAddress , String selectedLocationName) {
        this.latLng = selectedLatLng;
        setCurrentLocationAddress(selectedLatLng.latitude , selectedLatLng.longitude);
    }

    // override the interface method "sendInput" to get the preferances
    @Override
    public void sendInput(int budget, int numberRoomMates, List<Boolean> preferences) {
        //store the data from the dialog fragment
        this.budget = budget;
        this.preferences= preferences;
        this.numberOfRoommates=numberRoomMates;
        // display the data
        budgetTextView.setVisibility(View.VISIBLE);
        if(numberRoomMates!=0) {
            numberOfRoomMatesTextView.setVisibility(View.VISIBLE);
            numberOfRoomMatesTextView.setText(numberRoomMates + " roommates");
        }else{
            numberOfRoomMatesTextView.setVisibility(View.GONE);
        }
//      format the number and add commas
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);
        budgetTextView.setText(numberFormat.format(budget) + " RM");
        if(preferences.get(0)){
            maleImage.setVisibility(View.VISIBLE);
            preferencesTextView.setVisibility(View.VISIBLE);
        }else{maleImage.setVisibility(View.GONE);}
        if(preferences.get(1)){
            femaleImage.setVisibility(View.VISIBLE);
            preferencesTextView.setVisibility(View.VISIBLE);
        }else{femaleImage.setVisibility(View.GONE);}
        if(preferences.get(2)){
            petImage.setVisibility(View.VISIBLE);
            preferencesTextView.setVisibility(View.VISIBLE);
        }else{petImage.setVisibility(View.GONE);}
        if(preferences.get(3)){
            smokingImage.setVisibility(View.VISIBLE);
            preferencesTextView.setVisibility(View.VISIBLE);

        }else{smokingImage.setVisibility(View.GONE);}


        //check if the user didn't set any preferences
        //and remove the preferences text view
        if(!preferences.get(0)&&!preferences.get(1)&&!preferences.get(2)&&!preferences.get(3)){
            preferencesTextView.setVisibility(View.GONE);
        }

    }



    @Override
    public void onStart() {
        super.onStart();
        MainActivity.btm_view.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        MainActivity.btm_view.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.btm_view.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MainActivity.btm_view.setVisibility(View.VISIBLE);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        MainActivity.btm_view.setVisibility(View.VISIBLE);
    }



    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_publish_post, container, false);

        return v;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationTextView = (TextView)v.findViewById(R.id.location_text_view);
        preferencesImageButton= v.findViewById(R.id.preferences_image_button);
        publishPostButton =  v.findViewById(R.id.publish_post_button);
        viewPager = v.findViewById(R.id.view_pager);
        dotsIndicator = v.findViewById(R.id.dotsIndicator);
        deleteImageButton = v.findViewById(R.id.delete_image_post);
        postTabLayout = v.findViewById(R.id.tab_layout_publish_post);
        maleImage = v.findViewById(R.id.male_image_view_publish_post);
        femaleImage = v.findViewById(R.id.female_image_view_publish_post);
        smokingImage = v.findViewById(R.id.non_smoking_image_view_publish_post);
        petImage = v.findViewById(R.id.pets_allowd_image_view_publish_post);
        descriptionEditText = v.findViewById(R.id.post_description);
        locationImageButton = v.findViewById(R.id.location_image_button);
        budgetTextView = v.findViewById(R.id.price_text_view_apartment_card);
        numberOfRoomMatesTextView = v.findViewById(R.id.number_of_room_mates_text_view);
        addImageButton= v.findViewById(R.id.add_image_button);
        addImageWarning = v.findViewById(R.id.add_image_warning);
        userImageView = v.findViewById(R.id.user_image_publish_post);
        preferencesTextView = v.findViewById(R.id.preferences_text_view);
        imageUri = new ArrayList<>();

        // validate class will keep a check
        // of all submitted entries at the same time
        validate = new Validate(getActivity() , new Validate.OnDataChangedListener() , descriptionEditText , numberOfRoomMatesTextView , budgetTextView , locationTextView , imageUri  , publishPostButton,  addImageWarning , postTabLayout);

//        create a text watcher to check for non empty editTexts
//         to prevent submitting empty data
        TextWatcher postTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                validate.notifyDataChanged();
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validate.notifyDataChanged();
            }
            @Override
            public void afterTextChanged(Editable s) {
                validate.notifyDataChanged();
            }
        };

        descriptionEditText.addTextChangedListener(postTextWatcher);
        numberOfRoomMatesTextView.addTextChangedListener(postTextWatcher);
        budgetTextView.addTextChangedListener(postTextWatcher);
        locationTextView.addTextChangedListener(postTextWatcher);




        if(latLng==null){
            getLocation();
        }else{
            setCurrentLocationAddress(latLng.latitude,latLng.longitude);
        }

        getUserImage();


        // sets focus to the edit text as soon as the page is open
        descriptionEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(descriptionEditText, InputMethodManager.SHOW_IMPLICIT);

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the photo chosen
                openGallery();
            }
        });

        //when location button is pressed open the map fragement
        locationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapsFragment mapsFragment = new MapsFragment();
                mapsFragment.setTargetFragment(PublishPost.this ,MAPS_FRAGMENT_REQUEST_CODE );
                mapsFragment.show(getParentFragmentManager() , null);
            }
        });

        publishPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // list to hold all selected values from properties
                if(postTabLayout.getSelectedTabPosition()==0) {
                    postImagesAddToDatabase(latLng
                            , descriptionEditText.getText().toString()
                            , numberOfRoommates
                            , budget
                            , preferences
                            , imageUri);
                }else{
                    publishPersonalPost(descriptionEditText.getText().toString()
                            , budget
                            , preferences);
                }

            }
        });

        deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFromView();
            }
        });

        preferencesImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open a preference dialog fragment
                PreferencesDialogFragment preferencesDialogFragment = new PreferencesDialogFragment(postTabLayout.getSelectedTabPosition());
                preferencesDialogFragment.setTargetFragment(PublishPost.this , DIALOG_FRAGMENT_REQUEST_CODE);
                preferencesDialogFragment.show(getParentFragmentManager() ,null);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override
            public void onPageSelected(int position) {
                //set the index of the current image
                currentViewPagerPosition = position;
            }
            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        postTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==1){
                    //if user selects personal post remove some of the options
                    viewPager.setVisibility(View.GONE);
                    dotsIndicator.setVisibility(View.GONE);
                    deleteImageButton.setVisibility(View.GONE);
                    numberOfRoomMatesTextView.setVisibility(View.INVISIBLE);
                    addImageButton.setVisibility(View.GONE);
                    locationImageButton.setVisibility(View.GONE);
                    locationTextView.setVisibility(View.GONE);
                }else{
                    if(imageUri!=null){
                        if(imageUri.size()!=0){
                            viewPager.setVisibility(View.VISIBLE);
                            dotsIndicator.setVisibility(View.VISIBLE);
                            deleteImageButton.setVisibility(View.VISIBLE);
                        }
                    }

                    numberOfRoomMatesTextView.setVisibility(View.VISIBLE);
                    addImageButton.setVisibility(View.VISIBLE);
                    locationImageButton.setVisibility(View.VISIBLE);
                    locationTextView.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



    }

    @Override
    public void onResume() {
        super.onResume();
        //locks the screen to portarait mode
        if(getActivity()!=null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
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
                                        latLng = new LatLng(latitude, longitude);
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
            return latLng;
        }
    }
    void setCurrentLocationAddress(double latitude, double longitude)  {
        geocoder = new Geocoder(getActivity());
        List<Address> addresses;
        geocoder = new Geocoder(getActivity(), Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String maddress = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            address = knownName +" , " +maddress;
            locationTextView.setText(address);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }



    private void openGallery() {
        //add permisision denied handlers
        Intent image = new Intent();
        image.setAction(Intent.ACTION_GET_CONTENT);
        image.setType("image/*");
        image.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(image, PICK_IMAGE_MULTIPLE);

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        boolean duplicateFound = false;
        Uri selectedImageUri;


        if(imageUri.size()>NUMBER_OF_IMAGES_ALLOWED){
            Toast.makeText(getActivity(), "too many photos" , Toast.LENGTH_SHORT).show();
        }else {
            if (resultCode == getActivity().RESULT_OK && requestCode == PICK_IMAGE_MULTIPLE) {
                // if more than one image is selected
                if (data.getClipData()!=null){
                    int count = data.getClipData().getItemCount(); //evaluate the count before the for loop
                    // get the remaining amount of space left from the pictures that have already been
                    //uploaded if any exist
                    int spaceAvailable = NUMBER_OF_IMAGES_ALLOWED-imageUri.size();
                    if(count>spaceAvailable){
                        count-= spaceAvailable;
                    }
                    for(int i = 0; i < count; i++) {
                        selectedImageUri = data.getClipData().getItemAt(i).getUri();
                        if (!imageUri.contains(selectedImageUri)) {
                            addToViewPager(selectedImageUri);
                        }
                    }

                }
                 // if one image is selected
               else  if (data.getData() != null){
                    selectedImageUri = data.getData();
                    //check if the selected uri already exists in tge list
                    for (Uri uri : imageUri) {
                        //if duplicate found break
                        if (data.getData().equals(uri)) {
                            duplicateFound = true;
                            break;
                        }
                    }
                if (!duplicateFound) {
                    addToViewPager(selectedImageUri);
                } else {


                }

            }
            }
        }
    }


    void postImagesAddToDatabase(final LatLng locationLtLng , final  String description , final int numberRoomMate , final int price , final  List<Boolean> property , final List<Uri> imageUri ) {
        getFragment(new FindRoommate());
        publishPostButton.setVisibility(View.INVISIBLE);

        // get the referance of the database
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        //create a storage referance
        UploadTask uploadTask;
        final List<String> IMAGE_URLS = new ArrayList<>();
        for (Uri uri:
                imageUri ) {
            filePath = storageReference.child("apartment post image").child(uri.getLastPathSegment()
                    +getUniqueName()+".jpg");
            filePath.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()) {
                        IMAGE_URLS.add(task.getResult().getMetadata().getReference().getPath().toString());
                        // once all the pictures are added to the Storage add the rest of the posts using the urls to the database
                        if(IMAGE_URLS.size() == imageUri.size()){
                            addToRealTimeDatabase(locationLtLng ,  description ,  numberRoomMate ,  price , property ,IMAGE_URLS);
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
                    postPublishedDialog.setMessageText("an error occured while publishing your post");
                    postPublishedDialog.setMessageImage(getActivity().getDrawable(R.drawable.ic_error_icon));
                }
            });
        }


    }
    private String getUniqueName(){
        //create a unique id for the post by combining the date with uuid
        //get the date first
        Calendar calendarDate = Calendar.getInstance();
        SimpleDateFormat  currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        String saveCurrentDate = currentDate.format(calendarDate.getTime());

        //get the time in hours and minutes
        Calendar calendarTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss.SSS");
        String saveCurrentTime = currentTime.format(calendarTime.getTime());

        //add the two string together

        return  saveCurrentDate+saveCurrentTime;
    }

    void addToRealTimeDatabase(LatLng locationLtLng , String description , int numberRoomMate , int price , List<Boolean> property ,List<String> IMAGE_URL){

        String userUid =  FirebaseAuth.getInstance().getCurrentUser().getUid();
        HashMap<String ,Object> post = new HashMap<>();
        post.put("description" , description);
        post.put("userID" , userUid);
        post.put("price", price);
        post.put("numberOfRoommates" , numberRoomMate);
        post.put("latitude", locationLtLng.latitude);
        post.put("longitude",locationLtLng.longitude);
        post.put("preferences" , property);
        post.put("image_url" , IMAGE_URL );
        Calendar calendar = Calendar.getInstance();
        post.put("date",new SimpleDateFormat("dd-MMMM-yyyy HH:mm:ss aa").format(calendar.getTime()));

        mDocRef.collection("postApartment").add(post).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
                postPublishedDialog.show(getParentFragmentManager() , null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
                postPublishedDialog.setMessageText("an error occured while publishing your post");
                postPublishedDialog.setMessageImage(getActivity().getDrawable(R.drawable.ic_error_icon));
            }
        });

    }

    void publishPersonalPost(String description , int price , List<Boolean> property ){
        String userUid =  FirebaseAuth.getInstance().getCurrentUser().getUid();
        //move to the publish post page while the data is being posted
        getFragment(new PublishPost());
        publishPostButton.setVisibility(View.INVISIBLE);


        final HashMap<String ,Object> post = new HashMap<>();

        post.put("description" , description);
        post.put("userID" , userUid);
        post.put("price", price);
        post.put("preferences" , property);
        // add the time of the post
        Calendar calendar = Calendar.getInstance();
        post.put("date",new SimpleDateFormat("dd-MMMM-yyyy HH:mm:ss aa").format(calendar.getTime()));

        mDocRef.collection("postPersonal").add(post).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
                postPublishedDialog.show(getParentFragmentManager() , null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                PostPublishedDialog postPublishedDialog = new PostPublishedDialog();
                postPublishedDialog.setMessageText("an error occurred while publishing your post");
                postPublishedDialog.setMessageImage(getActivity().getDrawable(R.drawable.ic_error_icon));
            }
        });
        getFragment(new FindRoommate());


    }

    void addToViewPager(Uri newImageUri){

        viewPager.setVisibility(View.VISIBLE);
        dotsIndicator.setVisibility(View.VISIBLE);
        deleteImageButton.setVisibility(View.VISIBLE);
        //if no duplicate found  store the image
        imageUri.add(newImageUri);
        validate.notifyDataChanged();
        //initalize adapter with the list of uri
        viewPagerAdapter = new ViewPagerAdapter(getActivity(), imageUri);
        // set the view pager to the adapter
        viewPager.setAdapter(viewPagerAdapter);
        // add the indicator to the view pager and set to update on chage od dataset
        dotsIndicator.setViewPager(viewPager);
        viewPager.getAdapter().registerDataSetObserver(dotsIndicator.getDataSetObserver());
    }

    void deleteFromView(){


        if(imageUri.size()<=1){
            imageUri.remove(0);
        }else {
            imageUri.remove(currentViewPagerPosition);
        }

        validate.notifyDataChanged();

        //initalize adapter with the list of uri
        viewPagerAdapter = new ViewPagerAdapter(getActivity(), imageUri);
        // set the view pager to the adapter
        viewPager.setAdapter(viewPagerAdapter);
        // add the indicator to the view pager and set to update on chage od dataset
        dotsIndicator.setViewPager(viewPager);
        viewPager.getAdapter().registerDataSetObserver(dotsIndicator.getDataSetObserver());
        if (imageUri.size() == 0) {
            viewPager.setVisibility(View.GONE);
            dotsIndicator.setVisibility(View.GONE);
            deleteImageButton.setVisibility(View.GONE);
        }
        viewPager.setCurrentItem(currentViewPagerPosition);

    }
    private void getFragment (Fragment fragment) {

        fm = getActivity().getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.addToBackStack(null);
        ft.replace(R.id.fragmentContainer, fragment);
        ft.commit();

    }


    private void getUserImage(){
        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();
        String currUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rootRef.child("Users").child(currUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String url = snapshot.child("image").getValue(String.class);
                    if(!url.isEmpty()){
                        GlideApp.with(getActivity())
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

class Validate{
    Context context;
    DataChangedEvent listener;
    EditText description;
    TextView numberOfRoommates , budgetTextView , locationTextView, warningTextView;
    List<Uri> imageUri;
    Button publishPostButton;
    TabLayout tabLayout;


    public Validate(Context context , DataChangedEvent listener, EditText description, TextView numberOfRoommates, TextView budgetTextView, TextView locationTextView, List<Uri> imageUri ,Button  publishPostButton , TextView warningTextView , TabLayout tabLayout) {
        this.context = context;
        this.listener = listener;
        this.description = description;
        this.numberOfRoommates = numberOfRoommates;
        this.budgetTextView = budgetTextView;
        this.locationTextView = locationTextView;
        this.imageUri = imageUri;
        this.publishPostButton = publishPostButton;
        this.warningTextView = warningTextView;
        this.tabLayout = tabLayout;
    }

    void notifyDataChanged(){
        boolean dataIsComplete = false;
                if(tabLayout.getSelectedTabPosition()==0) {
                  dataIsComplete= !locationTextView.getText().toString().trim().isEmpty()
                            && !budgetTextView.getText().toString().trim().isEmpty()
                            && !numberOfRoommates.getText().toString().trim().isEmpty()
                            && !description.getText().toString().trim().isEmpty()
                            && !imageUri.isEmpty();
                }else{
                    dataIsComplete= !budgetTextView.getText().toString().trim().isEmpty()
                            && !description.getText().toString().trim().isEmpty();
                }
                listener.onDataComplete(context , dataIsComplete , publishPostButton , warningTextView);
    }


static class OnDataChangedListener implements DataChangedEvent {
    @Override
    public void onDataComplete(Context context , boolean dataIsComplete , Button publishPostButton , TextView warningTextView) {
        if(!dataIsComplete){
            //removes the shadow
            publishPostButton.setOutlineProvider(null);
            publishPostButton.setClickable(false);
            publishPostButton.setBackground(context.getDrawable(R.drawable.button_round_greyish_timber_wolf));
            warningTextView.setVisibility(View.VISIBLE);
        }else{
            //adds the shadow
            publishPostButton.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
            publishPostButton.setBackground(context.getDrawable(R.drawable.button_round_alabaster));
            publishPostButton.setClickable(true);
            warningTextView.setVisibility(View.GONE);
        }
    }
}

}



