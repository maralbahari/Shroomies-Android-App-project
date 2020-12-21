package com.example.shroomies;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FindRoommate extends Fragment {
    View v;
    RecyclerView recyclerView;
    RecycleViewAdapterApartments recycleViewAdapterApartment;
    List<Apartment> apartmentList;
    SearchView searchView;
    TabLayout tabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_find_roommate, container, false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apartmentList = new ArrayList<>();
        recyclerView = v.findViewById(R.id.apartment_recycler_view);
        recycleViewAdapterApartment = new RecycleViewAdapterApartments(apartmentList,getActivity());
        searchView = v.findViewById(R.id.SVsearch_disc);
        tabLayout = v.findViewById(R.id.tabLayout);


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getApartments();


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // if  the user is searching in the apartment tab
                if(tabLayout.getSelectedTabPosition()==0){
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                    Query query1 = reference.orderByChild("postApartment").equalTo(query);
                    query1.addValueEventListener(new ValueEventListener() {
                        @Override

                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            recycleViewAdapterApartment = new RecycleViewAdapterApartments(apartmentList , getActivity());
                            setAdapterData(snapshot);
                        }

                        @Override

                        public void onCancelled(@NonNull DatabaseError error) {}
                    });

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                recycleViewAdapterApartment = new RecycleViewAdapterApartments(apartmentList,getActivity());
                getApartments();
                return false;
            }
        });

    }


    void getApartments(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("postApartment");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recycleViewAdapterApartment = new RecycleViewAdapterApartments(apartmentList,getActivity());
                setAdapterData(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    void setAdapterData(DataSnapshot snapshot){
        if (snapshot.exists()){
            for (DataSnapshot dataSnapshot:
                    snapshot.getChildren()){
                Apartment apartment = dataSnapshot.getValue(Apartment.class);
                apartmentList.add(apartment);
            }
            recycleViewAdapterApartment = new RecycleViewAdapterApartments(apartmentList,getActivity());

            recycleViewAdapterApartment.notifyDataSetChanged();
            recyclerView.setAdapter(recycleViewAdapterApartment);
        }

    }

}