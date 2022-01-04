package com.example.shroomies;

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
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
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


public class PersonalPostFragment extends Fragment {
    private View v;
    private LottieAnimationView emptyAnimation, overScrollAnimation;
    private RelativeLayout emptyLayout;
    private RecyclerView personalRecyclerView;

    private PersonalPostRecyclerAdapter personalPostRecyclerAdapter;
    private List<PersonalPostModel> personalPostModelList;

    private QueryDocumentSnapshot lastDocument;
    private CollectionReference personalPostReference;

    private static final int POST_PER_PAGINATION = 15;
    private int newContentRange, previousContentRange;
    private boolean searchState, loading = true, scrollFromTop;
    private int pastVisibleItems, visibleItemCount, totalItemCount;
    private String searchQuery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_personal_post_tab, container, false);
        FirebaseFirestore mDocRef = FirebaseFirestore.getInstance();
        personalPostReference = mDocRef.collection(Config.PERSONAL_POST);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (personalPostRecyclerAdapter != null) {
            personalPostRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        personalRecyclerView = v.findViewById(R.id.personal_recycler_view);
        emptyAnimation = v.findViewById(R.id.empty_animation);
        emptyLayout = v.findViewById(R.id.empty_layout);
        overScrollAnimation = v.findViewById(R.id.over_scroll_loading);

        MaterialToolbar toolbar = getActivity().findViewById(R.id.toolbar);
        android.widget.SearchView searchView = toolbar.findViewById(R.id.SVsearch_disc);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        personalRecyclerView.setLayoutManager(layoutManager);

        personalPostModelList = new ArrayList<>();
        personalPostRecyclerAdapter = new PersonalPostRecyclerAdapter(personalPostModelList, getContext(), false, false);
        personalRecyclerView.setAdapter(personalPostRecyclerAdapter);

        IOverScrollDecor decor = OverScrollDecoratorHelper.setUpOverScroll(personalRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        decor.setOverScrollStateListener((decor1, oldState, newState) -> {

            if (oldState == 2) {
                scrollFromTop = true;
            }
            if (newState == 0 && scrollFromTop) {
                scrollFromTop = false;
                if (loading) {
                    loading = false;
                    //fetch new data
                    getPosts(true);
                }
            }
        });


        personalRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                            getPosts(false);
                        }
                    }
                }
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                lastDocument = null;
                personalPostModelList.clear();
                personalPostRecyclerAdapter.notifyDataSetChanged();
                searchView.clearFocus();
                searchQuery = query;
                searchState = true;
                loading = false;
                getPosts(false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        int searchCloseButtonId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeButton = searchView.findViewById(searchCloseButtonId);
        closeButton.setOnClickListener(v -> {
            // if the user closed the search get  the starting page apartments
            searchView.setQuery("", false);
            searchView.clearFocus();
            searchView.setIconifiedByDefault(false);
            searchState = false;
            personalPostModelList.clear();
            searchQuery = null;
            personalPostRecyclerAdapter.notifyDataSetChanged();
            lastDocument = null;
            loading = false;
            getPosts(false);
        });


        loading = false;
        getPosts(false);

    }



    private void setAdapterData(Task<QuerySnapshot> task) {

        if(personalPostModelList.size()==0){
            newContentRange=0;
            previousContentRange = 0;
        }
        for (QueryDocumentSnapshot document :
                task.getResult()) {
            lastDocument = document;
            PersonalPostModel personalPostModel = document.toObject(PersonalPostModel.class);
            personalPostModel.setPostID(document.getId());
            personalPostModelList.add(personalPostModel);
            ++newContentRange;
        }

        personalPostRecyclerAdapter.notifyItemRangeInserted(previousContentRange, newContentRange);
        previousContentRange = newContentRange;

    }


    private void getPosts(boolean overScroll) {
        hideEmptyResult();
        Query query = buildQuery();
        if (query != null) {
            query.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult().size() == 0) {
                        //if the list is empty then show the empty layout
                        //if its not the display a toast
                        if (personalPostModelList.isEmpty()) {
                            showEmptyResult();
                        } else if (overScroll) {
                            new CustomToast((MainActivity) getActivity(), "Sorry, we ran out of posts to show").showCustomToast();
                        }
                        loading = true;
                        return;
                    }
                    setAdapterData(task);
                } else {
                    if (personalPostModelList.isEmpty()) {
                        showEmptyResult();
                    }
                    new CustomToast((MainActivity) getActivity(), "We encountered an error", R.drawable.ic_error_icon).showCustomToast();

                }
            });
        }
    }


    private Query buildQuery() {
        Query query;
        List<String> addedPostProperties = getPropertiesPreference();
        String selectedState = getSelectedState();
        MinMaxPrice minMaxPrice = getMinMaxPrice();

        query = personalPostReference;
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
                    query = query.orderBy(Config.TIME_STAMP, Query.Direction.DESCENDING).whereArrayContainsAny(Config.KEY_WORDS, filteredQuery);
                } else {
                    new CustomToast((MainActivity) getActivity(), "The query entered is too long", R.drawable.ic_error_icon).showCustomToast();
                    return null;
                }
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
                    .limit(POST_PER_PAGINATION);
        } else {
            // starts from the beginning
            query = query.limit(POST_PER_PAGINATION);
        }

        return query;
    }

    private void showEmptyResult() {
        personalRecyclerView.setVisibility(View.GONE);
        emptyAnimation.playAnimation();
        emptyLayout.setVisibility(View.VISIBLE);
        overScrollAnimation.pauseAnimation();
        overScrollAnimation.setVisibility(View.GONE);
    }

    private void hideEmptyResult() {
        personalRecyclerView.setVisibility(View.VISIBLE);
        emptyAnimation.pauseAnimation();
        emptyLayout.setVisibility(View.GONE);
        overScrollAnimation.playAnimation();
        overScrollAnimation.setVisibility(View.VISIBLE);
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
            //check if the user selected to filter accooding to preference
            boolean filterAccordingToPreferences = prefs.getBoolean(Config.FILTER_PREFERENCE, false);
            Set<String> selections = prefs.getStringSet(Config.PROPERTIES, null);
            if (selections != null && filterAccordingToPreferences) {
                String[] selected = selections.toArray(new String[]{});
                // create a new list to store all properties
                addedPostProperties.addAll(Arrays.asList(selected));
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


}