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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FindRoommate extends Fragment {
    View v;
    RecyclerView recyclerView;
    RecycleViewAdapterApartments recycleViewAdapterApartment;
    List<Apartment> apartmentList;

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

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        getApartments();

    }


    void getApartments(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("postApartment");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

}