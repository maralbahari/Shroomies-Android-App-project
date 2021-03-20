 package com.example.shroomies;

 import android.content.SharedPreferences;
 import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
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
 import androidx.preference.PreferenceManager;
 import androidx.recyclerview.widget.LinearLayoutManager;
 import androidx.recyclerview.widget.StaggeredGridLayoutManager;

 import com.factor.bouncy.BouncyRecyclerView;
 import com.factor.bouncy.util.OnOverPullListener;
 import com.google.android.gms.tasks.OnCompleteListener;
 import com.google.android.gms.tasks.OnFailureListener;
 import com.google.android.gms.tasks.Task;
 import com.google.android.material.tabs.TabLayout;
 import com.google.firebase.auth.FirebaseAuth;
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
 import com.nfx.android.rangebarpreference.RangeBarHelper;

 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;

 public class FindRoommate extends Fragment {
    MotionLayout motionLayout;
    private static final String MAP_FRAGMENT = "MAP_FRAGMENT";
    private static final String  PERSONAL_SEARCH = "PERSONAL_SEARCH";
    private static final String  USER_SEARCH = "USER_SEARCH";
    FirebaseAuth mAuth;

    public static final int APARTMENT_PER_PAGINATION =20;
    private View v;
    private BouncyRecyclerView recyclerView;
    private FirebaseFirestore mDocRef;
    private List<Apartment> apartmentList;
    private SearchView searchView;
    private TabLayout tabLayout;
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
    private List<String> addedPostProperties;
    private String selectedCity;
    private float maxPrice;
    private float minPrice;
    private boolean gridViewOn;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         v = inflater.inflate(R.layout.fragment_find_roommate, container, false);
         mDocRef = FirebaseFirestore.getInstance();
         mAuth = FirebaseAuth.getInstance();
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


        toolbar  = getActivity().findViewById(R.id.toolbar);
        searchView = toolbar.findViewById(R.id.SVsearch_disc);
        optionsButton = toolbar.findViewById(R.id.search_settings);

        tabLayout = v.findViewById(R.id.tabLayout);
        staggeredGridLayoutManager =  new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        staggeredGridLayoutManager.invalidateSpanAssignments();
        layoutManager = new LinearLayoutManager(getActivity());
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
        fragmentManager =getParentFragmentManager();

        //check which view option has been selected
        gridViewOn = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("grid_view_preference" , false );
        apartmentList = new ArrayList<>();
        lastCardKey = null;
        // adjust the view options
        if(gridViewOn) {
            // adapter for mutliple cards
             recyclerView.setLayoutManager(staggeredGridLayoutManager);
               exploreApartmentsAdapter = new ExploreApartmentsAdapter(getActivity(), apartmentList , mAuth.getCurrentUser().getUid());
               recyclerView.setAdapter(exploreApartmentsAdapter);

        }
            else{
            // adapter for single cards
            recyclerView.setLayoutManager(layoutManager);
               recycleViewAdapterApartments = new RecycleViewAdapterApartments(apartmentList, getActivity(), mAuth.getCurrentUser().getUid() , false);
               recyclerView.setAdapter(recycleViewAdapterApartments);
        }
        customLoadingProgressBar.show();
        getApartments();



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
                    recycleViewAdapterApartments = new RecycleViewAdapterApartments(apartmentList,getActivity()  , mAuth.getCurrentUser().getUid() , false);
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

                    if(!gridViewOn ){
                        recyclerView.setLayoutManager(layoutManager);
                        recycleViewAdapterApartments = new RecycleViewAdapterApartments( apartmentList , getActivity(),mAuth.getCurrentUser().getUid() , false);
                        recyclerView.setAdapter(recycleViewAdapterApartments);

                    }else{
                        recyclerView.setLayoutManager(staggeredGridLayoutManager);
                        exploreApartmentsAdapter =new ExploreApartmentsAdapter(getActivity() , apartmentList , mAuth.getCurrentUser().getUid());
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
        paginate = false;
        searchState= false;
        Query query = buildQuery();

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

     private Query buildQuery() {
        Query  query = null;
        addedPostProperties =new ArrayList<>();
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
         if(prefs!=null){
             selectedCity= prefs.getString("city_preference" ,null );
             Set<String> selections = prefs.getStringSet("properties", null);
             if(selections!=null){
             String []  selected = selections.toArray(new String[]{});
             if(selected.length>0) {
                 // create a new list to store all properties
                 for (int i = 0; i < selected.length; i++) {
                     addedPostProperties.add(selected[i]);
                 }
             }
             }
             String seekBar = prefs.getString("range_preference" , null);
             if (seekBar == null){
                 maxPrice=10000;
                 minPrice= 100;
             }else {
                 maxPrice = RangeBarHelper.getHighValueFromJsonString(seekBar);
                 minPrice = RangeBarHelper.getLowValueFromJsonString(seekBar);
             }
         }

         query =  apartmentPostReference
                   .orderBy(FieldPath.documentId());
//         if(selectedCity!=null){
//             if (!selectedCity.isEmpty()){
//               query = query.whereEqualTo("locality" , selectedCity);
//             }
//         }
         if(searchState){
             if(searchUserId!=null){
                 query = query.whereEqualTo("userID" ,  searchUserId);
             }
         }
         if(addedPostProperties!=null) {
             if (!addedPostProperties.isEmpty()) {
                 query = query.whereArrayContainsAny("preferences", addedPostProperties);
             }
         }
         // gets the N most recent apartments in the users city
         // check if the last card key is empty
         // if its empty this is the first data to be loaded

         //finally order by timestamp
//         query = query.orderBy("time_stamp", Query.Direction.ASCENDING);

         if (lastCardKey!=null) {
             query = query.startAfter(lastCardKey)
                     .limit(APARTMENT_PER_PAGINATION);
             paginate=true;
         }else{
             // starts from the beginning
             query =query.limit(APARTMENT_PER_PAGINATION);
         }

         return query;
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
        // initialize an integer to check if the filtered
        // data is less than the required per pagination
        // this is needed because we have to filter for price on the  client side
        // and if the amount of data retrieved is too low the the user has to pull down and retrieve  a small sets of data

        int dataRetrieved = 0;
        if(apartmentList.size()==0){
            newContentRange=0;
            previousContentRange = 0;
        }
            for (QueryDocumentSnapshot document :
                    task.getResult()) {
                    lastCardKey = document.getId();
                    Apartment apartment = document.toObject(Apartment.class);
                    apartment.setDate();
                    apartment.setApartmentID(document.getId());

                    //filter the result for  price range
                if(apartment.getPrice()>=minPrice  && apartment.getPrice()<=maxPrice) {
                    apartmentList.add(apartment);
                    dataRetrieved++;
                    newContentRange++;
                }

            }
//            if data retrieved is low then load more data
            if(dataRetrieved<APARTMENT_PER_PAGINATION){
                if(!searchState){
                    getApartments();
                }
            }
            //check which adapter is being used
        if(gridViewOn) {
            exploreApartmentsAdapter.notifyItemRangeInserted(previousContentRange, newContentRange);
        }
        if (!gridViewOn || searchState){  // check if the single card has been selected or search state because we are  using single view for search state
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


        paginate = false;
        Query query = buildQuery();
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