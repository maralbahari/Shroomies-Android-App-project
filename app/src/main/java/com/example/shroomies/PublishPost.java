package com.example.shroomies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


public class PublishPost extends Fragment implements MapsFragment.OnLocationSet, OnPostTypeChange {
    public static final int MAPS_FRAGMENT_REQUEST_CODE = 2;
    private View v;
    private TextView searchForNameTextView, selectTypeofUnitTextView;
    private ImageView userImageView;
    private ChipGroup typeOfUnitChipGroup;
    private Chip locationChip, postTypeChip;
    private EditText descriptionEditText;
    private RelativeLayout apartmentPostLayout, mainLayout;

    private String buildingAddress, locality, subLocality, buildingName;
    private LatLng selectedLatLng;
    private String postType;
    private FirebaseAuth mAuth;

    @Override
    public void onPostTypeChanged(String postType) {
        this.postType = postType;
        if (postType.equals(Config.APARTMENT_POST)) {
            setApartmentPostState();
        } else {
            setPersonalPostState();
        }
    }


    // override the interface method "sendNewLocation" to get the location
    public void OnLocationForApartmentSet(LatLng selectedLatLng, String buildingName, String buildingAddress) {
        this.selectedLatLng = selectedLatLng;
        this.buildingName = buildingName;
        this.buildingAddress = buildingAddress;
        locationChip.setText(buildingName);

    }

    @Override
    public void OnLocationForTownHouseSet(LatLng selectedLatLng, String subLocality, String locality, String buildingAddress) {
        this.selectedLatLng = selectedLatLng;
        this.locality = locality;
        this.subLocality = subLocality;
        this.buildingAddress = buildingAddress;
        locationChip.setText(subLocality + ", " + subLocality);
    }

    @Override
    public void OnLocationForPersonalPostSet(LatLng selectedLatLng, String subLocality, String locality) {
        this.locality = locality;
        this.subLocality = locality;
        this.selectedLatLng = selectedLatLng;
        if (locality != null && subLocality != null) {
            locationChip.setText(subLocality + ", " + subLocality);
        }
    }




    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_publish_post, container, false);
        mAuth=FirebaseAuth.getInstance();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        locationChip = v.findViewById(R.id.location_chip_view);
        typeOfUnitChipGroup = v.findViewById(R.id.type_of_unit_chip_group);
        searchForNameTextView = v.findViewById(R.id.search_for_name_text_view);
        apartmentPostLayout = v.findViewById(R.id.apartment_post_layout);
        selectTypeofUnitTextView = v.findViewById(R.id.select_type_text_view);


        MaterialButton nextButton = v.findViewById(R.id.publish_post_next_button);
        mainLayout = v.findViewById(R.id.relative_layout);

        descriptionEditText = v.findViewById(R.id.post_description);
        userImageView = v.findViewById(R.id.user_image_publish_post);
        postTypeChip = v.findViewById(R.id.post_type_chip);
        postType = Config.APARTMENT_POST;

        getUserImage();
        nextButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();

            if (postType.equals(Config.APARTMENT_POST)) {
                if (checkDataForApartmentPost()) {
                    String buildingType = getBuildingType();
                    bundle.putString(Config.BUILDING_TYPE, buildingType);
                    bundle.putString(Config.BUILDING_NAME, buildingName);
                    bundle.putString(Config.BUILDING_ADDRESS, buildingAddress);
                    bundle.putString(Config.LOCALITY, locality);
                    bundle.putString(Config.SUB_LOCALITY, subLocality);
                    bundle.putParcelable(Config.SELECTED_LAT_LNG, selectedLatLng);
                    bundle.putString(Config.DESCRIPTION, descriptionEditText.getText().toString().trim());
                    bundle.putString(Config.POST_TYPE, postType);
                    getFragment(new PublishPostPreferences(), bundle);
                }
            } else if (checkDataForPersonalPost()) {
                bundle.putStringArrayList(Config.BUILDING_TYPES, getBuildingTypes());
                bundle.putString(Config.DESCRIPTION, descriptionEditText.getText().toString().trim());
                bundle.putString(Config.POST_TYPE, postType);
                bundle.putString(Config.LOCALITY, locality);
                bundle.putString(Config.SUB_LOCALITY, subLocality);
                bundle.putParcelable(Config.SELECTED_LAT_LNG, selectedLatLng);
                getFragment(new PublishPostPreferences(), bundle);
            }

        });


        // sets focus to the edit text as soon as the page is open
        descriptionEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(descriptionEditText, InputMethodManager.SHOW_IMPLICIT);

        }

        locationChip.setOnClickListener(v -> {
            if (postType.equals(Config.APARTMENT_POST)) {
                if (typeOfUnitChipGroup.getCheckedChipId() == View.NO_ID) {
                    selectTypeofUnitTextView.setTextColor(getActivity().getColor(R.color.canceRed));
                    Snackbar.make(mainLayout, "Please select the type of your unit first", Snackbar.LENGTH_LONG);
                } else {
                    selectTypeofUnitTextView.setTextColor(getActivity().getColor(R.color.jetBlack));
                    MapsFragment mapsFragment = new MapsFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Config.POST_TYPE, postType);
                    bundle.putBoolean(Config.TYPE_HOUSE, typeOfUnitChipGroup.getCheckedChipId() == R.id.house_type_chip);
                    mapsFragment.setArguments(bundle);
                    mapsFragment.setTargetFragment(PublishPost.this, MAPS_FRAGMENT_REQUEST_CODE);
                    mapsFragment.show(getParentFragmentManager(), null);
                }
            } else {
                MapsFragment mapsFragment = new MapsFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Config.POST_TYPE, postType);
                mapsFragment.setArguments(bundle);
                mapsFragment.setTargetFragment(PublishPost.this, MAPS_FRAGMENT_REQUEST_CODE);
                mapsFragment.show(getParentFragmentManager(), null);
            }

        });

        typeOfUnitChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            searchForNameTextView.setVisibility(View.VISIBLE);
            //if it not in single selection mode
            // then we know its in personal post mode
            if (!typeOfUnitChipGroup.isSingleSelection()) {
                searchForNameTextView.setText(R.string.search_for_the_nearest_place);
            } else {
                if (checkedId == R.id.house_type_chip) {
                    searchForNameTextView.setText(R.string.search_for_the_nearest_place);
                } else {
                    searchForNameTextView.setText(R.string.search_for_building_name);
                }
            }


        });


        postTypeChip.setOnCloseIconClickListener(v -> {
            PostTypeDialogFragment postTypeDialogFragment = new PostTypeDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Config.POST_TYPE, postType);
            postTypeDialogFragment.setArguments(bundle);
            postTypeDialogFragment.setTargetFragment(this, 0);
            postTypeDialogFragment.show(getParentFragmentManager(), null);
        });

    }

    void setApartmentPostState() {
        apartmentPostLayout.setVisibility(View.VISIBLE);
        searchForNameTextView.setVisibility(View.VISIBLE);
        typeOfUnitChipGroup.setSingleSelection(true);
        searchForNameTextView.setVisibility(View.GONE);
        postTypeChip.setText("Room post");
        selectTypeofUnitTextView.setText("Select the type of your residential unit");
    }

    void setPersonalPostState() {
        typeOfUnitChipGroup.setSingleSelection(false);
        searchForNameTextView.setVisibility(View.GONE);
        selectTypeofUnitTextView.setText("Select your preferred unit types");
        postTypeChip.setText("Roommate post");

    }

    @SuppressLint("NonConstantResourceId")
    private String getBuildingType() {
        switch (typeOfUnitChipGroup.getCheckedChipId()) {
            case R.id.apartment_type_chip:
                return Config.TYPE_APARTMENT;
            case R.id.flat_type_chip:
                return Config.TYPE_FLAT;
            case R.id.condo_type_chip:
                return Config.TYPE_CONDO;
            case R.id.house_type_chip:
                return Config.TYPE_HOUSE;
            default:
                return null;
        }
    }

    @SuppressLint("NonConstantResourceId")
    private ArrayList<String> getBuildingTypes() {
        ArrayList<String> buildingTypes = new ArrayList<>();

        for (int chipId :
                typeOfUnitChipGroup.getCheckedChipIds()) {
            switch (chipId) {
                case R.id.apartment_type_chip:
                    buildingTypes.add(Config.TYPE_APARTMENT);
                    break;
                case R.id.flat_type_chip:
                    buildingTypes.add(Config.TYPE_FLAT);
                    break;
                case R.id.condo_type_chip:
                    buildingTypes.add(Config.TYPE_CONDO);
                    break;
                case R.id.house_type_chip:
                    buildingTypes.add(Config.TYPE_HOUSE);
                    break;
            }
        }
        return buildingTypes;
    }

    private boolean checkDataForApartmentPost() {
        boolean status = true;
        ArrayList<String> errors = new ArrayList<>();

        if (typeOfUnitChipGroup.getCheckedChipId() == View.NO_ID) {
            selectTypeofUnitTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.canceRed));
            errors.add("Please select the type of your unit");
            status = false;
        } else {
            selectTypeofUnitTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.jetBlack));
        }
        //if the type of the unit is town house then there is no need for the building name
        if (buildingName == null && typeOfUnitChipGroup.getCheckedChipId() != R.id.house_type_chip) {
            errors.add("Please search for the name of the building");
            locationChip.setChipStrokeColor(getActivity().getColorStateList(R.color.canceRed));
            status = false;
        } else {
            locationChip.setChipStrokeColor(getActivity().getColorStateList(R.color.LogoYellow));
        }
        if (selectedLatLng == null) {
            errors.add("Please enter a valid location");
            locationChip.setChipStrokeColor(getActivity().getColorStateList(R.color.canceRed));
            status = false;
        } else {
            locationChip.setChipStrokeColor(getActivity().getColorStateList(R.color.LogoYellow));
        }
        if (descriptionEditText.getText().toString().trim().isEmpty()) {
            errors.add("Please enter a description");
            descriptionEditText.setHintTextColor(getActivity().getColor(R.color.canceRed));
            status = false;
        } else {
            descriptionEditText.setHintTextColor(getActivity().getColor(R.color.lightGrey));
        }

        if (!errors.isEmpty()) {
            Snackbar.make(mainLayout, errors.get(0), Snackbar.LENGTH_LONG).show();
        }

        return status;
    }

    private boolean checkDataForPersonalPost() {
        boolean status = true;
        ArrayList<String> errors = new ArrayList<>();
        if (descriptionEditText.getText().toString().trim().isEmpty()) {
            errors.add("Please enter a description");
            descriptionEditText.setHintTextColor(getActivity().getColor(R.color.canceRed));
            status = false;
        } else {
            descriptionEditText.setHintTextColor(getActivity().getColor(R.color.lightGrey));
        }
        if (selectedLatLng == null || locality == null || subLocality == null) {
            errors.add("Please add a locality");
            locationChip.setChipStrokeColor(getActivity().getColorStateList(R.color.canceRed));
            status = false;
        } else {
            locationChip.setChipStrokeColor(getActivity().getColorStateList(R.color.LogoYellow));
        }
        if (!errors.isEmpty()) {
            Snackbar.make(mainLayout, errors.get(0), Snackbar.LENGTH_LONG).show();
        }

        return status;
    }


    private void getFragment(Fragment fragment, Bundle bundle) {
        fragment.setArguments(bundle);
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.publish_post_container, fragment);
        ft.commit();
    }

    private void getUserImage() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            rootRef.child(Config.users).child(firebaseUser.getUid()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String url = task.getResult().getValue(User.class).getImage();
                    if (url != null) {
                        GlideApp.with(getActivity())
                                .load(url)
                                .centerCrop()
                                .circleCrop()
                                .transition(DrawableTransitionOptions.withCrossFade())
                                .into(userImageView);
                    }

                }
            });
        }

    }


}






