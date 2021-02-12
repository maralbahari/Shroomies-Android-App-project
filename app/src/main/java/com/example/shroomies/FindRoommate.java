 package com.example.shroomies;

 import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
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
 import androidx.recyclerview.widget.LinearSmoothScroller;
 import androidx.recyclerview.widget.RecyclerView;
 import androidx.recyclerview.widget.StaggeredGridLayoutManager;

 import com.factor.bouncy.BouncyRecyclerView;
 import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FindRoommate extends Fragment {
    private static final String MAP_FRAGMENT = "MAP_FRAGMENT";
    private static final String  PERSONAL_SEARCH = "PERSONAL_SEARCH";
    View v;

    BouncyRecyclerView recyclerView;
    FrameLayout frame;

    List<Apartment> apartmentList;
    List<Address> addressList;
    SearchView searchView;
    TabLayout tabLayout;
    ArrayAdapter searchArrayAdapter;
    ListView locationListView;
    final int apartmentPerPagination =30;
    String lastCardKey ;
    boolean paginate;
    boolean searchState = false;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    CustomLoadingProgressBar customLoadingProgressBar;
    ExploreApartmentsAdapter exploreApartmentsAdapter;
    RecyclerView.SmoothScroller smoothScroller;

    StaggeredGridLayoutManager staggeredGridLayoutManager;
    Toolbar toolbar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_find_roommate, container, false);
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
        setSearchViewVisible();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        customLoadingProgressBar= new CustomLoadingProgressBar(getActivity(), "Searching..." , R.raw.search_anim);
        customLoadingProgressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        apartmentList = new ArrayList<>();
        recyclerView = v.findViewById(R.id.apartment_recycler_view);

        tabLayout = v.findViewById(R.id.tabLayout);
        locationListView = v.findViewById(R.id.list_view_search);
        toolbar  = getActivity().findViewById(R.id.toolbar);
        searchView = toolbar.findViewById(R.id.SVsearch_disc);
        setSearchViewVisible();




        //change this





        staggeredGridLayoutManager =  new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);

        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        exploreApartmentsAdapter = new ExploreApartmentsAdapter(getActivity() , apartmentList);

        recyclerView.setAdapter(exploreApartmentsAdapter);

        getApartments();
        fragmentManager =getParentFragmentManager();

         // get the bundle from the filter dialog fragment





        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchState=true;
                // if  the user is searching in the apartment tab
                if (tabLayout.getSelectedTabPosition() == 0) {
                    getApartmentsFromQuery(query);
                    hideSoftKeyBoard();

                }
                else if(tabLayout.getSelectedTabPosition() == 2) {
                    getPersonalPostsFromQuery(query);

                }
                else if(tabLayout.getSelectedTabPosition() == 2) {
                    getPersonalPostsFromQuery(query);

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchState=true;
                if (tabLayout.getSelectedTabPosition() == 1) {
                    locationListView.setVisibility(View.VISIBLE);
                    new GetAdressListAsync(getActivity(), newText).execute();

                }

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


                //close the list view if its still open
                if(tabLayout.getSelectedTabPosition()==1) {
                    locationListView.setAdapter(null);
                    locationListView.setVisibility(View.GONE);

                }
                // if the user closed the search get  the starting page apartments
                if(tabLayout.getSelectedTabPosition()==0){

                    searchView.setIconifiedByDefault(false);
                    searchView.clearFocus();
                    searchView.setQuery("", false);
                    hideSoftKeyBoard();
                    searchState= false;
                    apartmentList = new ArrayList<>();
                    exploreApartmentsAdapter = new ExploreApartmentsAdapter(getActivity(), apartmentList);
                    lastCardKey =null;
                    getApartments();
                }
            }
        });


        locationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LatLng latLng = new LatLng(addressList.get(position).getLatitude() , addressList.get(position).getLongitude());
                MapSearchFragment.setLocationView(latLng, 16);
            }
        });



        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition()==0){
                    // find any fragment that is in the frame
                    Fragment fragment = fragmentManager.findFragmentById(R.id.frame_layout_search);
                    if(fragment!=null){
                        Toast.makeText(getActivity(),"exists " , Toast.LENGTH_LONG).show();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.remove(fragment);
                        fragmentTransaction.commit();
                        fragmentTransaction.addToBackStack(null);

                    }
                    // display the recycler view
                    recyclerView.setVisibility(View.VISIBLE);
                }

                if (tab.getPosition() == 1) {
                    recyclerView.setVisibility(View.GONE);
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frame_layout_search, new MapSearchFragment());
                    // add tag to clear off of backstack
                    fragmentTransaction.addToBackStack(MAP_FRAGMENT);
                    fragmentTransaction.commit();
                }
                if (tab.getPosition() == 2){
                    recyclerView.setVisibility(View.GONE);
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frame_layout_search, new fragmentPersonalPostTab());
                    fragmentTransaction.addToBackStack(PERSONAL_SEARCH);
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
        recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (!recyclerView.canScrollVertically(1) && !searchState) {
                    Toast.makeText(getActivity(),"reached" , Toast.LENGTH_SHORT).show();
                    getApartments();
                }

            }
        });

    }



    void getPersonalPostsFromQuery(String query) {

        recyclerView.setVisibility(View.GONE);
        fragmentPersonalPostTab personalFrag = new fragmentPersonalPostTab();
        Bundle bundle = new Bundle();
        bundle.putString("myQuery",query);
        personalFrag.setArguments(bundle);
        fragmentTransaction.addToBackStack(null);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_search, personalFrag).commit();




    }


    void getApartments() {
        customLoadingProgressBar.show();
        Query query;
        paginate = false;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("postApartment");
        // gets the 10 most recent apartments in the users city
        // check if the last card key is empty
        // if its empty this is the first data to be loaded
        if (lastCardKey!=null) {
            query = reference.orderByKey().startAt(lastCardKey).limitToFirst(apartmentPerPagination);
            paginate=true;
        }else{
            // starts from the beginning
            query = reference.orderByKey().limitToFirst(apartmentPerPagination);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // if the snapshot returns less than or equal to one child
                // then no new apartment has been found
                customLoadingProgressBar.dismiss();
                if(snapshot.getChildrenCount()<=1){
                    return;
                }
                setAdapterData(snapshot , paginate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                customLoadingProgressBar.dismiss();
            }


        });


    }

    void setAdapterData(DataSnapshot snapshot, boolean paginate) {

        // check if the adapter is null
        // prevents the adapter from clearing the data
        //and adding it again
        if(exploreApartmentsAdapter==null){
            exploreApartmentsAdapter = new ExploreApartmentsAdapter( getActivity(),apartmentList);
            recyclerView.setAdapter(exploreApartmentsAdapter);
        }
        int count =0;
        if (snapshot.exists()) {
            for (DataSnapshot dataSnapshot :
                    snapshot.getChildren()) {
                lastCardKey = dataSnapshot.getKey();
                // checks if there is any pagination  because the query will include the last card key
                // when paginating
                if (!paginate || count != 0) {
                    Apartment apartment = dataSnapshot.getValue(Apartment.class);
                    apartmentList.add(apartment);
                }
                count++;
            }

            exploreApartmentsAdapter.notifyDataSetChanged();
        }


    }


    void getApartmentsFromQuery(String query){
        searchState = true;
        customLoadingProgressBar.show();
        final List<String> userIds  = new ArrayList<>();
        apartmentList = new ArrayList<>();
        exploreApartmentsAdapter = new ExploreApartmentsAdapter(getActivity() ,apartmentList);
        recyclerView.setAdapter(exploreApartmentsAdapter);

//        animationWrapper.setVisibility(View.VISIBLE);
//        lottieAnimationProgressBar.resumeAnimation();
        // the user will search for usernames
        // first get a list of user ids
        //of which their user names start with the query terms
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
       rootRef.child("Users").orderByChild("name").startAt(query)
               .endAt(query+"\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    //store  all the ids in a list
                    for (DataSnapshot dataSnapshot
                    :snapshot.getChildren()){
                      User user = dataSnapshot.getValue(User.class);
                      userIds.add(user.getID());
                    }
                    // once all users are found find and load the posts that match with these ids
                    for (String  id:
                    userIds){

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("postApartment");
                        Query query1 = reference.orderByChild("userID").equalTo(id);
                        query1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // if the user is searching , clear the all the
                                //stored data and add the new data to the adapter
                                setSearchAdapterData(snapshot);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(getActivity(), error.getMessage() , Toast.LENGTH_SHORT).show();
                                customLoadingProgressBar.dismiss();
                            }
                        });
                    }

                }else{
                    customLoadingProgressBar.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                customLoadingProgressBar.dismiss();
            }
        });





    }
    void setSearchAdapterData(DataSnapshot snapshot){

        if (snapshot.exists()) {
            for (DataSnapshot dataSnapshot :
                    snapshot.getChildren()) {
                lastCardKey = dataSnapshot.getKey();
                // checks if there is any pagination  because the query will include the last card key
                // when paginating
                Apartment apartment = dataSnapshot.getValue(Apartment.class);
                apartmentList.add(apartment);
            }

            exploreApartmentsAdapter.notifyDataSetChanged();
            customLoadingProgressBar.dismiss();
        }

//        lottieAnimationProgressBar.pauseAnimation();
//        animationWrapper.setVisibility(View.GONE);
    }

    public void addAddresses(List<Address> addresses, Context context) {
        addressList = addresses;
        // create a list of strings holding the adresses as strings
        List<String> stringAdresses = new ArrayList<>();
        for (Address address :
                addresses) {
            stringAdresses.add(address.getAddressLine(0));
        }
        searchArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, stringAdresses);
        locationListView.setAdapter(searchArrayAdapter);
    }


    class GetAdressListAsync extends AsyncTask<Void, String, List<Address>> {
        Context context;
        Geocoder geocoder;
        String query;
        List<Address> addresses;
        public GetAdressListAsync(Context context, String query) {
            this.context = context;
            this.query = query;
        }

        @Override
        protected List<Address> doInBackground(Void... voids) {
            geocoder = new Geocoder(context);
            addresses = new ArrayList<>();
            try {

                addresses = geocoder.getFromLocationName(query, 5);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            super.onPostExecute(addresses);
            addAddresses(addresses, context);
        }
    }

    void hideSoftKeyBoard(){
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }
    void setSearchViewVisible(){
        searchView.setVisibility(View.VISIBLE);
        toolbar.findViewById(R.id.logo).setVisibility(View.GONE);
        toolbar.findViewById(R.id.logo_toolbar).setVisibility(View.GONE);
    }

    void setSearchViewGone(){
        searchView.setVisibility(View.GONE);
        toolbar.findViewById(R.id.logo).setVisibility(View.VISIBLE);
        toolbar.findViewById(R.id.logo_toolbar).setVisibility(View.VISIBLE);
    }


}