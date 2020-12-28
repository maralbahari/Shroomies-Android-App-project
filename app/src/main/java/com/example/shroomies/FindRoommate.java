package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TabHost;
import android.widget.Toast;

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
    RecylerAdapter_personal RecylerAdapter_personal;
    List<Apartment> apartmentList;
    List<Address> addressList;
    SearchView searchView;
    TabLayout tabLayout;
    ArrayAdapter searchArrayAdapter;
    ListView locationListView;


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
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getApartments();


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // if  the user is searching in the apartment tab
                if (tabLayout.getSelectedTabPosition() == 0) {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("postApartment");
                    Query query1 = reference.orderByChild("userName").equalTo(query);
                    query1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            setAdapterData(snapshot);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getActivity(), error.getMessage() , Toast.LENGTH_SHORT).show();
                        }
                    });

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (tabLayout.getSelectedTabPosition() == 1) {
                    locationListView.setVisibility(View.VISIBLE);
                    new GetAdressListAsync(getActivity(), newText).execute();

                }
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //close the list view if its still open
                locationListView.setAdapter(null);
                locationListView.setVisibility(View.GONE);

                // if the user closed the search get  the starting page apartments
                if(tabLayout.getSelectedTabPosition()==0){
                    getApartments();
                }
                return false;
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

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                recycleViewAdapterApartment = new RecycleViewAdapterApartments(apartmentList, getActivity());
                getApartments();
                return false;
            }
        });

    }


    void getApartments() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("postApartment");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recycleViewAdapterApartment = new RecycleViewAdapterApartments(apartmentList, getActivity());
                setAdapterData(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    void setAdapterData(DataSnapshot snapshot) {
        apartmentList = new ArrayList<>();
        if (snapshot.exists()) {
            for (DataSnapshot dataSnapshot :
                    snapshot.getChildren()) {
                Apartment apartment = dataSnapshot.getValue(Apartment.class);
                apartmentList.add(apartment);
            }
            recycleViewAdapterApartment = new RecycleViewAdapterApartments(apartmentList, getActivity());

            recycleViewAdapterApartment.notifyDataSetChanged();
            recyclerView.setAdapter(recycleViewAdapterApartment);
        }

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

}