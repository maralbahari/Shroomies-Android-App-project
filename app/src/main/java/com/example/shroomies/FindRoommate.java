 package com.example.shroomies;

 import android.content.Intent;
 import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.SearchView;
 import android.widget.Toast;

 import  androidx.appcompat.widget.Toolbar;

 import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
 import androidx.constraintlayout.motion.widget.MotionLayout;
 import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
 import androidx.recyclerview.widget.StaggeredGridLayoutManager;

 import com.factor.bouncy.BouncyRecyclerView;
 import com.factor.bouncy.util.OnOverPullListener;
 import com.google.android.gms.tasks.OnCompleteListener;
 import com.google.android.gms.tasks.OnFailureListener;
 import com.google.android.gms.tasks.Task;
 import com.google.android.material.tabs.TabLayout;
 import com.google.firebase.database.ChildEventListener;
 import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
 import com.google.firebase.firestore.FieldPath;
 import com.google.firebase.firestore.Query;

 import com.google.firebase.firestore.CollectionReference;
 import com.google.firebase.firestore.FirebaseFirestore;
 import com.google.firebase.firestore.QueryDocumentSnapshot;
 import com.google.firebase.firestore.QuerySnapshot;

 import java.util.ArrayList;
import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;

 public class FindRoommate extends Fragment {
    MotionLayout motionLayout;
    private static final String MAP_FRAGMENT = "MAP_FRAGMENT";
    private static final String  PERSONAL_SEARCH = "PERSONAL_SEARCH";
    private static final String  USER_SEARCH = "USER_SEARCH";

    public static final int APARTMENT_PER_PAGINATION =5;
    private View v;
    private BouncyRecyclerView recyclerView;
    private RadioGroup viewOptionsRadioGroup;
    private FirebaseFirestore mDocRef;
    private List<Apartment> apartmentList;
    private SearchView searchView;
    private TabLayout tabLayout;
    private ArrayAdapter searchArrayAdapter;
    private RefreshProgressBar refreshProgressBar;
    private int newContentRange, previousContentRange;
    private String lastCardKey ;
    private String searchUserId;
    private boolean paginate ,searchState;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private CustomLoadingProgressBar customLoadingProgressBar;
    private ExploreApartmentsAdapter exploreApartmentsAdapter;
    private RecycleViewAdapterApartments recycleViewAdapterApartments;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private LinearLayoutManager layoutManager;
    private OnOverPullListener onOverPullListener;
    private Toolbar toolbar;
    private CollectionReference apartmentPostReference;
    private ImageView optionsButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         v = inflater.inflate(R.layout.fragment_find_roommate, container, false);
         mDocRef = FirebaseFirestore.getInstance();
         apartmentPostReference = mDocRef.collection("postApartment");
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


//    @Override
//    public void onResume() {
//        super.onResume();
//        setSearchViewVisible();
//
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        motionLayout  = v.findViewById(R.id.find_roomMate_Motion_Layout);
        customLoadingProgressBar= new CustomLoadingProgressBar(getActivity(), "Searching..." , R.raw.search_anim);
        customLoadingProgressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        apartmentList = new ArrayList<>();
        recyclerView = v.findViewById(R.id.apartment_recycler_view);
        viewOptionsRadioGroup = v.findViewById(R.id.radio_view_options);
        toolbar  = getActivity().findViewById(R.id.toolbar);
        searchView = toolbar.findViewById(R.id.SVsearch_disc);
        optionsButton = toolbar.findViewById(R.id.search_settings);

        tabLayout = v.findViewById(R.id.tabLayout);
        staggeredGridLayoutManager =  new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        staggeredGridLayoutManager.invalidateSpanAssignments();
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setItemAnimator(null);

        motionLayout.addTransitionListener(new MotionLayout.TransitionListener() {
            @Override
            public void onTransitionStarted(MotionLayout motionLayout, int i, int i1) {

            }

            @Override
            public void onTransitionChange(MotionLayout motionLayout, int i, int i1, float v) {

            }

            @Override
            public void onTransitionCompleted(MotionLayout motionLayout, int i) {
                if(motionLayout.getCurrentState() == motionLayout.getEndState()){
                    setSearchViewVisible();
                }else{
                    setSearchViewGone();
                }
            }

            @Override
            public void onTransitionTrigger(MotionLayout motionLayout, int i, boolean b, float v) {
                if(motionLayout.getCurrentState() == motionLayout.getEndState()){
                    setSearchViewVisible();
                }else{
                    setSearchViewGone();
                }
            }
        });
        // adapter for mutliple cards
        exploreApartmentsAdapter = new ExploreApartmentsAdapter(getActivity() , apartmentList);
        // adapter for single cards
        recycleViewAdapterApartments = new RecycleViewAdapterApartments(apartmentList , getActivity() ,false);

        recyclerView.setAdapter(exploreApartmentsAdapter);

        customLoadingProgressBar.show();
        getApartments();
        fragmentManager =getParentFragmentManager();

         // get the bundle from the filter dialog fragment


        viewOptionsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.multi_card_option_radio:
                        apartmentList = new ArrayList<>();
                        lastCardKey=null;
                        recyclerView.setLayoutManager(staggeredGridLayoutManager);
                        exploreApartmentsAdapter = new ExploreApartmentsAdapter(getActivity() , apartmentList);
                        recyclerView.setAdapter(exploreApartmentsAdapter);
                        getApartments();
                        break;
                    case R.id.single_card_option_radio:
                        apartmentList = new ArrayList<>();
                        lastCardKey=null;
                        recyclerView.setLayoutManager(layoutManager);
                        recycleViewAdapterApartments = new RecycleViewAdapterApartments(apartmentList , getActivity() ,false);
                        recyclerView.setAdapter(recycleViewAdapterApartments);
                        getApartments();
                }
            }
        });

        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new SearchSettingsFragment())
                        .addToBackStack("SEARCH_FRAGMENT")
                        .commit();
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchState=true;
                // if  the user is searching in the apartment tab
                if (tabLayout.getSelectedTabPosition()==0) {
                    lastCardKey = null;
                    apartmentList = new ArrayList<>();
                    recyclerView.setLayoutManager(layoutManager);
                    recycleViewAdapterApartments = new RecycleViewAdapterApartments(apartmentList,getActivity()  , false);
                    recyclerView.setAdapter(recycleViewAdapterApartments);

                    searchView.clearFocus();
                    getUserSearchID(query);

                }
                else if(tabLayout.getSelectedTabPosition()==2) {
                    getPersonalPostsFromQuery(query);

                }else if(tabLayout.getSelectedTabPosition()==3) {
                    getUsersFromQuery(query);

                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {

                return false;
            }
        });

        // get the X button in the search view and use it to close the search view
        int searchCloseButtonId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        ImageView closeButton = (ImageView) this.searchView.findViewById(searchCloseButtonId);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if the user closed the search get  the starting page apartments
                if(tabLayout.getSelectedTabPosition()==0){
                    searchView.setQuery("", false);
                    searchView.clearFocus();
                    searchView.setIconifiedByDefault(false);
                    searchState= false;
                    apartmentList = new ArrayList<>();
                    recycleViewAdapterApartments.notifyDataSetChanged();
                    recyclerView.setOnOverPullListener(onOverPullListener);
                    //check which option was checked and
                    // apply the adapter accordingly
                    // if nothing is selected go to explore adapter

                    if(viewOptionsRadioGroup.getCheckedRadioButtonId()==R.id.single_card_option_radio ){
                        recyclerView.setLayoutManager(layoutManager);
                        recycleViewAdapterApartments = new RecycleViewAdapterApartments( apartmentList , getActivity(),false);
                        recyclerView.setAdapter(recycleViewAdapterApartments);

                    }else{
                        recyclerView.setLayoutManager(staggeredGridLayoutManager);
                        exploreApartmentsAdapter =new ExploreApartmentsAdapter(getActivity() , apartmentList);
                        recyclerView.setAdapter(exploreApartmentsAdapter);
                    }
                    lastCardKey =null;
                    getApartments();
                }
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        // find any fragment that is in the frame
                        Fragment fragment = fragmentManager.findFragmentById(R.id.frame_layout_search);
                        if(fragment!=null){

                            fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.remove(fragment);
                            fragmentTransaction.commit();
                            fragmentTransaction.addToBackStack(null);
                        }
                        searchView.setQuery("" , false);
                        searchView.clearFocus();
                        // display the recycler view
                        recyclerView.setVisibility(View.VISIBLE);
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
                        break;
                    case 3:
                        setSearchViewVisible();
                        searchView.requestFocus();
                        recyclerView.setVisibility(View.GONE);
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.frame_layout_search, new UserSearch());
                        fragmentTransaction.addToBackStack(USER_SEARCH);
                        fragmentTransaction.commit();
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // if the user reaches the end of the list load the next batch of data
        onOverPullListener = new OnOverPullListener() {
            @Override
            public void onOverPulledTop(float v) {

            }

            @Override
            public void onOverPulledBottom(float v) {


            }

            @Override
            public void onRelease() {
                // check if the user dragged from the bottom not  from the top
                if (!recyclerView.canScrollVertically(1)) {

                    // remove the on pull listener and set it again once the data  has been loaded
                    // prevents the database from retreiving the same data
                    recyclerView.setOnOverPullListener(null);
                    if (searchState) {

                        if (searchUserId != null) {
                            refreshProgressBar = new RefreshProgressBar();
                            refreshProgressBar.show(getParentFragmentManager(), null);
                            getUserApartments(searchUserId);

                        }
                    } else {
                        refreshProgressBar = new RefreshProgressBar();
                        refreshProgressBar.show(getParentFragmentManager(), null);
                        getApartments();
                    }

                }
            }
        };

    }
    void getPersonalPostsFromQuery(String query) {

        recyclerView.setVisibility(View.GONE);
        PersonalPostFragment personalFrag = new PersonalPostFragment();
        Bundle bundle = new Bundle();
        bundle.putString("myQuery",query);
        personalFrag.setArguments(bundle);
        fragmentTransaction.addToBackStack(null);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_search, personalFrag).commit();
    }
    void getUsersFromQuery(String query){

        recyclerView.setVisibility(View.GONE);
        UserSearch userSearch = new UserSearch();
        Bundle bundle = new Bundle();
        bundle.putString("myQuery",query);
        userSearch.setArguments(bundle);
        fragmentTransaction.addToBackStack(null);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_search, userSearch).commit();

    }


    void getApartments() {
        Query query;
        paginate = false;
        searchState= false;

        // gets the N most recent apartments in the users city
        // check if the last card key is empty
        // if its empty this is the first data to be loaded
        if (lastCardKey!=null) {
            query = apartmentPostReference.orderBy(FieldPath.documentId()).startAfter(lastCardKey).limit(APARTMENT_PER_PAGINATION);
            paginate=true;
        }else{
            // starts from the beginning
            query = apartmentPostReference.orderBy(FieldPath.documentId()).limit(APARTMENT_PER_PAGINATION);
        }

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                // if the snapshot returns less than or equal to one child
                // then no new apartment has been found
                closeProgressBarsSetOverPullListener();
                if(task.isSuccessful()){
                    if(task.getResult().size()<1){
                        return;
                    }
                    setAdapterData(task);
                }


            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                closeProgressBarsSetOverPullListener();
            }
        });


    }
    void closeProgressBarsSetOverPullListener(){
        recyclerView.setOnOverPullListener(onOverPullListener);
        if(customLoadingProgressBar!=null){
            customLoadingProgressBar.dismiss();
        }
        if(refreshProgressBar!=null){
            refreshProgressBar.dismiss();
        }
    }

    void setAdapterData(Task<QuerySnapshot> task) {
        if(apartmentList.size()==0){
            newContentRange=0;
            previousContentRange = 0;
        }
            for (QueryDocumentSnapshot document :
                    task.getResult()) {
                    lastCardKey = document.getId();
                    Apartment apartment = document.toObject(Apartment.class);
                    apartment.setApartmentID(document.getId());
                    apartmentList.add(apartment);
                    newContentRange++;
            }

            //check which adapter is being used
        if(viewOptionsRadioGroup.getCheckedRadioButtonId()== R.id.multi_card_option_radio) {
            exploreApartmentsAdapter.notifyItemRangeInserted(previousContentRange, newContentRange);
        }
        if (viewOptionsRadioGroup.getCheckedRadioButtonId()==R.id.single_card_option_radio || searchState){  // check if the single card has been selected or search state because we are  using single view for search state
            recycleViewAdapterApartments.notifyItemRangeInserted(previousContentRange , newContentRange);
        }
        previousContentRange = newContentRange;
        closeProgressBarsSetOverPullListener();
    }


    void getUserSearchID(String textQuery){
        searchState = true;
        // first get a list of user ids
        //of which their user names start with the query terms
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
       rootRef.child("Users").orderByChild("name").equalTo(textQuery).addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
               if(snapshot.exists()){
                   User user = snapshot.getValue(User.class);
                   searchUserId = user.getID();
                   getUserApartments(searchUserId);

               }else{
                   customLoadingProgressBar.dismiss();
               }
           }

           @Override
           public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

           }

           @Override
           public void onChildRemoved(@NonNull DataSnapshot snapshot) {

           }

           @Override
           public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });

    }
    void getUserApartments(String id){

        Query query;
        paginate = false;
        if (lastCardKey!=null) {
            query = apartmentPostReference.orderBy("time_stamp", Query.Direction.DESCENDING).whereEqualTo("userID" , id).orderBy(FieldPath.documentId()).startAfter(lastCardKey).limit(APARTMENT_PER_PAGINATION);
            paginate=true;
        }else{
            // starts from the beginning
            query = apartmentPostReference.orderBy("time_stamp", Query.Direction.DESCENDING).whereEqualTo("userID",id).orderBy(FieldPath.documentId()).limit(APARTMENT_PER_PAGINATION);
        }
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                // if the snapshot returns less than or equal to one child
                // then no new apartment has been found
                closeProgressBarsSetOverPullListener();
                if(task.isSuccessful()) {
                    if(task.getResult().size()<1){
                        return;
                    }
                    setAdapterData(task);

                }


            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                closeProgressBarsSetOverPullListener();
            }
        });
    }


    void setSearchViewVisible(){
        toolbar.findViewById(R.id.SVsearch_disc).animate().alpha(1.0f).setDuration(200);
        toolbar.findViewById(R.id.search_settings).animate().alpha(1.0f).setDuration(200);
        toolbar.findViewById(R.id.logo).animate().alpha(0.0f).setDuration(200);
        toolbar.findViewById(R.id.logo_toolbar).animate().alpha(0.0f).setDuration(200);
        toolbar.findViewById(R.id.logo).setVisibility(View.GONE);
        toolbar.findViewById(R.id.logo_toolbar).setVisibility(View.GONE);
        toolbar.findViewById(R.id.SVsearch_disc).setVisibility(View.VISIBLE);
        toolbar.findViewById(R.id.search_settings).setVisibility(View.VISIBLE);

    }

    void setSearchViewGone(){
        toolbar.findViewById(R.id.SVsearch_disc).animate().alpha(0.0f).setDuration(200);
        toolbar.findViewById(R.id.search_settings).animate().alpha(0.0f).setDuration(200);
        toolbar.findViewById(R.id.logo).animate().alpha(1.0f).setDuration(200);
        toolbar.findViewById(R.id.logo_toolbar).animate().alpha(1.0f).setDuration(200);
        toolbar.findViewById(R.id.SVsearch_disc).setVisibility(View.GONE);
        toolbar.findViewById(R.id.search_settings).setVisibility(View.GONE);
        toolbar.findViewById(R.id.logo).setVisibility(View.VISIBLE);
        toolbar.findViewById(R.id.logo_toolbar).setVisibility(View.VISIBLE);
    }


}