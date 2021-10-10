package com.example.shroomies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class FindRoomFragment extends Fragment {
    private View v;
    private RelativeLayout emptyLayout;
    private RecyclerView recyclerView;
    private List<Apartment> apartmentList;
    private SearchView searchView;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private LottieAnimationView emptyAnimation, overScrollAnimation;

    private RecycleViewAdapterApartments recycleViewAdapterApartments;
    private LinearLayoutManager layoutManager;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private CollectionReference apartmentPostReference;
    private QueryDocumentSnapshot lastDocument;

    private boolean loading = true, scrollFromTop, searchState;
    private int pastVisibleItems, visibleItemCount, totalItemCount, newContentRange, previousContentRange;
    private String searchQuery;
    private static final String MAP_FRAGMENT = "MAP_FRAGMENT";
    private static final String PERSONAL_SEARCH = "PERSONAL_SEARCH";
    private static final String USER_SEARCH = "USER_SEARCH";
    private static final int APARTMENT_PER_PAGINATION = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_find_roommate, container, false);
        FirebaseFirestore mDocRef = FirebaseFirestore.getInstance();
        apartmentPostReference = mDocRef.collection(Config.APARTMENT_POST);
        searchState = false;
        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        setSearchViewGone();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        setSearchViewGone();

    }

    @Override
    public void onResume() {
        super.onResume();
        //notify data set changed in
        //case the user navigated back from favourites
        //and removed any items
        if (recycleViewAdapterApartments != null) {
            recycleViewAdapterApartments.notifyDataSetChanged();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apartmentList = new ArrayList<>();
        MotionLayout motionLayout = v.findViewById(R.id.find_roomMate_Motion_Layout);
        recyclerView = v.findViewById(R.id.apartment_recycler_view);
        emptyLayout = v.findViewById(R.id.empty_layout);
        emptyAnimation = v.findViewById(R.id.empty_animation);
        toolbar = getActivity().findViewById(R.id.toolbar);
        searchView = toolbar.findViewById(R.id.SVsearch_disc);
        ImageView optionsButton = toolbar.findViewById(R.id.search_settings);
        tabLayout = v.findViewById(R.id.tabLayout);
        overScrollAnimation = v.findViewById(R.id.over_scroll_loading);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        IOverScrollDecor decor = OverScrollDecoratorHelper.setUpOverScroll(recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        decor.setOverScrollStateListener((decor1, oldState, newState) -> {
            if (oldState == 2) {
                scrollFromTop = true;
            }
            if (newState == 0 && scrollFromTop) {
                scrollFromTop = false;
                if (loading) {
                    loading = false;
                    //etch new data
                    if (searchState && searchQuery != null) {
                        getApartments(searchQuery, true);
                    } else {
                        getApartments(null, true);
                    }
                }
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NotNull RecyclerView recyclerView, int dx, int dy) {

                if (dy > 0) { //check for scroll down
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            //fetch new data
                            if (searchState && searchQuery != null) {
                                getApartments(searchQuery, false);
                            } else {
                                getApartments(null, false);
                            }
                        }
                    }
                }
            }
        });


        motionLayout.addTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {

            }

            @Override
            public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {


            }

            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int i) {
                if (i == motionLayout.getStartState()) {
                    setSearchViewGone();
                } else if (i == motionLayout.getEndState()) {
                    setSearchViewVisible();
                }
            }

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {

            }
        });
        fragmentManager = getParentFragmentManager();
        initAdapter();
        getApartments(null, false);

        optionsButton.setOnClickListener(v -> startActivity(new Intent(getActivity(), SearchSettingActivity.class)));


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // if  the user is searching in the apartment tab
                if (tabLayout.getSelectedTabPosition() == 0) {
                    lastDocument = null;
                    apartmentList.clear();
                    recycleViewAdapterApartments.notifyDataSetChanged();
                    searchView.clearFocus();
                    searchState = true;
                    searchQuery = query;
                    getApartments(searchQuery, false);

                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {

                return false;
            }
        });


        // get the X button in the search view and use it to close the search view
        int searchCloseButtonId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeButton = this.searchView.findViewById(searchCloseButtonId);
        closeButton.setOnClickListener(v -> {

            // if the user closed the search get  the starting page apartments
            if (tabLayout.getSelectedTabPosition() == 0) {
                searchView.setQuery("", false);
                searchView.clearFocus();
                searchView.setIconifiedByDefault(false);
                searchState = false;
                searchQuery = null;
                lastDocument = null;
                initAdapter();
                getApartments(null, false);
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setSearchViewGone();
                switch (tab.getPosition()) {
                    case 0:
                        // find any fragment that is in the frame
                        Fragment fragment = fragmentManager.findFragmentById(R.id.frame_layout_search);
                        if (fragment != null) {
                            fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.remove(fragment);
                            fragmentTransaction.commit();
                            fragmentTransaction.addToBackStack(null);
                            if (recycleViewAdapterApartments != null) {
                                recycleViewAdapterApartments.notifyDataSetChanged();
                            }
                        }
                        searchView.setQuery("", false);
                        searchView.clearFocus();
                        // display the recycler view
                        recyclerView.setVisibility(View.VISIBLE);
                        setSearchViewHint("Search properties");

                        break;
                    case 1:
                        recyclerView.setVisibility(View.GONE);
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_layout_search, new MapSearchFragment());
                        // add tag to clear off of backstack
                        fragmentTransaction.addToBackStack(MAP_FRAGMENT);
                        fragmentTransaction.commit();
                        break;
                    case 2:
                        recyclerView.setVisibility(View.GONE);
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_layout_search, new PersonalPostFragment());
                        fragmentTransaction.addToBackStack(PERSONAL_SEARCH);
                        fragmentTransaction.commit();
                        setSearchViewHint("Search terms");
                        break;

                    case 3:
                        setSearchViewVisible();
                        searchView.requestFocus();
                        recyclerView.setVisibility(View.GONE);
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_layout_search, new UserSearch());
                        fragmentTransaction.addToBackStack(USER_SEARCH);
                        fragmentTransaction.commit();
                        setSearchViewHint("Search username");

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

    private void setSearchViewHint(String hint) {
        searchView.setQueryHint(hint);
    }

    private void initAdapter() {
        apartmentList = new ArrayList<>();
        apartmentList.clear();
        lastDocument = null;
        recyclerView.setLayoutManager(layoutManager);
        recycleViewAdapterApartments = new RecycleViewAdapterApartments(apartmentList, getActivity(), false, false);
        recyclerView.setAdapter(recycleViewAdapterApartments);
    }


    void getApartments(String searchQuery, boolean overScroll) {
        hideEmptyResult();
        Query query = buildQuery(searchQuery);
        if (query != null) {
            query.get().addOnCompleteListener(task -> {
                // if the snapshot returns less than or equal to one child
                // then no new apartment has been found
                loading = true;
                if (task.isSuccessful()) {
                    if (task.getResult().size() < 1) {
                        if (apartmentList.isEmpty()) {
                            setSearchViewVisible();
                            showEmptyResult();
                        } else if (overScroll) {
                            new CustomToast((MainActivity) getActivity(), "Sorry, we ran out of posts to show ").showCustomToast();
                        }
                    } else {
                        setAdapterData(task);
                    }

                } else {
                    if (apartmentList.isEmpty()) {
                        showEmptyResult();
                    }
                    new CustomToast((MainActivity) getActivity(), "We encountered an error", R.drawable.ic_error_icon).showCustomToast();
                }
            });
        }
    }

    private void showEmptyResult() {
        recyclerView.setVisibility(View.GONE);
        emptyAnimation.playAnimation();
        emptyLayout.setVisibility(View.VISIBLE);
        overScrollAnimation.pauseAnimation();
        overScrollAnimation.setVisibility(View.GONE);
    }

    private void hideEmptyResult() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyAnimation.pauseAnimation();
        emptyLayout.setVisibility(View.GONE);
        overScrollAnimation.playAnimation();
        overScrollAnimation.setVisibility(View.VISIBLE);
    }

    private Query buildQuery(String searchQuery) {
        Query query;
        List<String> addedPostProperties = getPropertiesPreference();
        String selectedState = getSelectedState();
        MinMaxPrice minMaxPrice = getMinMaxPrice();

        query = apartmentPostReference;
        if (searchState) {
            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                new CustomToast((MainActivity) getActivity(), "Please enter a valid query", R.drawable.ic_error_icon).showCustomToast();
                return null;
            } else {
                ArrayList<String> filteredQuery = new ArrayList<>(Arrays.asList(searchQuery.trim().split(" ")));
                //firebase allows arrays of size 10 and less
                //for where array contains any queries

                filteredQuery.replaceAll(String::toLowerCase);
                if (filteredQuery.size() < 10) {
                    query = query.whereArrayContainsAny(Config.BUILDING_ADDRESS, filteredQuery);
                } else {
                    new CustomToast((MainActivity) getActivity(), "The query entered is too long", R.drawable.ic_error_icon).showCustomToast();
                    return null;
                }
                query = query.orderBy(Config.TIME_STAMP, Query.Direction.DESCENDING);
            }
            //if the user is not in the search state
            // limit the query with the price
            // whereArrayContainsAny cannot be combined with
            //whereGreaterThanOrEqualTo
        } else {

            if (!(selectedState == null || selectedState.equals(Config.NONE) || selectedState.isEmpty())) {
                query = query.whereEqualTo(Config.LOCALITY, selectedState);
            }
            if (minMaxPrice != null) {
                query = query.orderBy(Config.PRICE).whereGreaterThanOrEqualTo(Config.PRICE, minMaxPrice.getMin())
                        .whereLessThanOrEqualTo(Config.PRICE, minMaxPrice.getMax());
            }
            query = query.orderBy(Config.TIME_STAMP, Query.Direction.DESCENDING);
        }
        //addedPostProperties is never null
        if (!addedPostProperties.isEmpty()) {
            StringBuilder preferences = new StringBuilder("00000");
            for (String string
                    : addedPostProperties) {
                switch (string) {
                    case "Male":
                        preferences.setCharAt(0, '1');
                        break;
                    case "Female":
                        preferences.setCharAt(1, '1');
                        break;
                    case "Pet friendly":
                        preferences.setCharAt(2, '1');
                        break;
                    case "Non smoking":
                        preferences.setCharAt(3, '1');
                        break;
                    case "Alcohol friendly":
                        preferences.setCharAt(4, '1');
                }
            }
            //check if the user didn't enter any preferences
            if (!preferences.toString().equals("00000")) {
                query = query.whereEqualTo(Config.PREFERENCE, preferences.toString());
            }
        }


//          gets the N most recent apartments in the users city
//          check if the last card key is empty
//          if its empty this is the first data to be loaded

//         finally order by timestamp
//         query = query.orderBy("time_stamp", Query.Direction.ASCENDING);

        if (lastDocument != null) {
            query = query.startAfter(lastDocument)
                    .limit(APARTMENT_PER_PAGINATION);
        } else {
            // starts from the beginning
            query = query.limit(APARTMENT_PER_PAGINATION);
        }

        return query;
    }

    private String getSelectedState() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //check if the user selected to
        //show nearby posts
        //or selected a state
        if (prefs != null) {
            return prefs.getString(Config.STATE_PREFERENCE, null);
        } else {
            return null;
        }

    }


    private List<String> getPropertiesPreference() {
        List<String> addedPostProperties = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (prefs != null) {
            Set<String> selections = prefs.getStringSet(Config.PROPERTIES, null);
            boolean filterAccordingToPreferences = prefs.getBoolean(Config.FILTER_PREFERENCE, false);
            if (selections != null && filterAccordingToPreferences) {
                String[] selected = selections.toArray(new String[]{});
                if (selected != null) {
                    // create a new list to store all properties
                    addedPostProperties.addAll(Arrays.asList(selected));
                }
            }
        }
        return addedPostProperties;
    }

    private MinMaxPrice getMinMaxPrice() {
        MinMaxPrice minMaxPrice = new MinMaxPrice();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (prefs != null) {
            //check if the user checked the filter
            //according to price switch
            if (prefs.getBoolean(Config.FILTER_PRICE, false)) {
                minMaxPrice.setMin(prefs.getFloat(Config.LOW_VALUE, 0));
                minMaxPrice.setMax(prefs.getFloat(Config.HIGH_VALUE, 10000));
                return minMaxPrice;
            }
        }
        return null;
    }


    void setAdapterData(Task<QuerySnapshot> task) {
        if (apartmentList.size() == 0) {
            newContentRange = 0;
            previousContentRange = 0;
        }

        for (QueryDocumentSnapshot document :
                task.getResult()) {
            lastDocument = document;
            Apartment apartment = document.toObject(Apartment.class);
            apartment.setApartmentID(document.getId());
            apartmentList.add(apartment);
            newContentRange++;
        }

        recycleViewAdapterApartments.notifyItemRangeInserted(previousContentRange, newContentRange);
        previousContentRange = newContentRange;
    }


    void setSearchViewVisible() {
        toolbar.findViewById(R.id.SVsearch_disc).animate().alpha(1.0f).setDuration(200);
        toolbar.findViewById(R.id.search_settings).animate().alpha(1.0f).setDuration(200);
        toolbar.findViewById(R.id.logo).animate().alpha(0.0f).setDuration(200);
        toolbar.findViewById(R.id.logo_toolbar).animate().alpha(0.0f).setDuration(200);
        toolbar.findViewById(R.id.inbox_button).animate().alpha(0.0f).setDuration(200);
        toolbar.findViewById(R.id.inbox_button).setVisibility(View.GONE);
        toolbar.findViewById(R.id.logo).setVisibility(View.GONE);
        toolbar.findViewById(R.id.logo_toolbar).setVisibility(View.GONE);
        toolbar.findViewById(R.id.SVsearch_disc).setVisibility(View.VISIBLE);
        toolbar.findViewById(R.id.search_settings).setVisibility(View.VISIBLE);

    }

    void setSearchViewGone() {
        toolbar.findViewById(R.id.SVsearch_disc).animate().alpha(0.0f).setDuration(200);
        toolbar.findViewById(R.id.search_settings).animate().alpha(0.0f).setDuration(200);
        toolbar.findViewById(R.id.logo).animate().alpha(1.0f).setDuration(200);
        toolbar.findViewById(R.id.logo_toolbar).animate().alpha(1.0f).setDuration(200);
        toolbar.findViewById(R.id.inbox_button).animate().alpha(1.0f).setDuration(200);
        toolbar.findViewById(R.id.inbox_button).setVisibility(View.VISIBLE);
        toolbar.findViewById(R.id.SVsearch_disc).setVisibility(View.GONE);
        toolbar.findViewById(R.id.search_settings).setVisibility(View.GONE);
        toolbar.findViewById(R.id.logo).setVisibility(View.VISIBLE);
        toolbar.findViewById(R.id.logo_toolbar).setVisibility(View.VISIBLE);
    }


}

class MinMaxPrice {
    float min, max;

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }
}