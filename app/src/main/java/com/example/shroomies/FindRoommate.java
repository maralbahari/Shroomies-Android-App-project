 package com.example.shroomies;

import android.app.Activity;
import android.content.Context;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    View v;
    RecyclerView recyclerView;
    RecycleViewAdapterApartments recycleViewAdapterApartment;
    List<Apartment> apartmentList;
    List<Address> addressList;
    SearchView searchView;
    TabLayout tabLayout;
    ArrayAdapter searchArrayAdapter;
    ListView locationListView;
    final int apartmentPerPagination = 2;
    String lastCardKey ;
    boolean paginate;
    //    LottieAnimationView lottieAnimationProgressBar;
//    LinearLayout animationWrapper;
    boolean searchState = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_find_roommate, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apartmentList = new ArrayList<>();
        recyclerView = v.findViewById(R.id.apartment_recycler_view);
        recycleViewAdapterApartment = new RecycleViewAdapterApartments(apartmentList, getActivity());
        searchView = v.findViewById(R.id.SVsearch_disc);
        tabLayout = v.findViewById(R.id.tabLayout);
        locationListView = v.findViewById(R.id.list_view_search);
//        lottieAnimationProgressBar = v.findViewById(R.id.find_room_mate_progress_bar);
//        animationWrapper = v.findViewById(R.id.linearLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recycleViewAdapterApartment);
        getApartments();


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchState=true;
                // if  the user is searching in the apartment tab
                if (tabLayout.getSelectedTabPosition() == 0) {
                    getApartmentsFromQuery(query);
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
                else if(tabLayout.getSelectedTabPosition() == 2) {
                    getPersonalPostsFromQuery(newText);

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
                if (tab.getPosition() == 1) {
                    recyclerView.setVisibility(View.GONE);

                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frame_layout_search, new MapSearchFragment());
                    fragmentTransaction.commit();
                }
                if (tab.getPosition() == 2){
                    recyclerView.setVisibility(View.GONE);


                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.frame_layout_search, new fragmentPersonalPostTab());
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();

                }

            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
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

        Bundle bundle = new Bundle();
        bundle.putString("myQuery",query);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentPersonalPostTab personalFrag = new fragmentPersonalPostTab();
        personalFrag.setArguments(bundle);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout_search, new fragmentPersonalPostTab());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();


    }


    void getApartments() {
        Query query;
        paginate = false;
//        animationWrapper.setVisibility(View.VISIBLE);
//        lottieAnimationProgressBar.resumeAnimation();
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

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recycleViewAdapterApartment = new RecycleViewAdapterApartments(apartmentList, getActivity());
                // if the snapshot returns less than or equal to one child
                // then no new apartment has been found
                if(snapshot.getChildrenCount()<=1){
//                    lottieAnimationProgressBar.pauseAnimation();
//                    animationWrapper.setVisibility(View.GONE);
                    return;
                }
                setAdapterData(snapshot , paginate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    void setAdapterData(DataSnapshot snapshot, boolean paginate) {
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
            recycleViewAdapterApartment = new RecycleViewAdapterApartments(apartmentList, getActivity());
            recycleViewAdapterApartment.notifyDataSetChanged();
            recyclerView.setAdapter(recycleViewAdapterApartment);
        }
//        lottieAnimationProgressBar.pauseAnimation();
//        animationWrapper.setVisibility(View.GONE);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //if personal != null{
        //
        // }
        super.onSaveInstanceState(outState);
    }

    void getApartmentsFromQuery(String query){
//        animationWrapper.setVisibility(View.VISIBLE);
//        lottieAnimationProgressBar.resumeAnimation();
        apartmentList = new ArrayList<>();
        recycleViewAdapterApartment = new RecycleViewAdapterApartments(apartmentList,getActivity());
        recycleViewAdapterApartment.notifyDataSetChanged();
        recyclerView.setAdapter(recycleViewAdapterApartment);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("postApartment");
        Query query1 = reference.orderByChild("userName").equalTo(query);
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
            recycleViewAdapterApartment = new RecycleViewAdapterApartments(apartmentList, getActivity());
            recycleViewAdapterApartment.notifyDataSetChanged();
            recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView,new RecyclerView.State(), recyclerView.getAdapter().getItemCount());

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


}