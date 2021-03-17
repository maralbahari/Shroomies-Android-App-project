package com.example.shroomies;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class Favorite extends Fragment {
    private static final String PERSONAL_FAVOURITES = "PERSONAL_FAVOURITES";
    private static final String APARTMENT_FAVOURITES = "APARTMENT_FAVOURITES";
    private TabLayout tabLayoutFavorite;
    private List<PersonalPostModel> personalPostModelList;
    private List<Apartment> apartmentList;
    private RecyclerView favAptRecyclerView;
    private RecycleViewAdapterApartments favAptRecyclerAdapter;

    private View v;
    private FirebaseAuth mAuth;
    private CollectionReference apartmentPostReference;
    private CollectionReference personalPostReference;
    private PersonalPostRecyclerAdapter personalPostRecyclerAdapter;
    private Set<String> apartmentFavouriteSet;
    private Set<String> personalFavouritesSet;
    private FirebaseFirestore mDocRef;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_my_favorite, container, false);
        mDocRef = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        apartmentPostReference = mDocRef.collection("postApartment");
        personalPostReference = mDocRef.collection("postPersonal");

        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayoutFavorite = v.findViewById(R.id.tab_layout_Fav);
        favAptRecyclerView = v.findViewById(R.id.fav_recycler_view);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        favAptRecyclerView.setLayoutManager(linearLayoutManager);

        apartmentList = new ArrayList<>();
        favAptRecyclerAdapter = new RecycleViewAdapterApartments(apartmentList , getActivity() , mAuth.getCurrentUser().getUid() , true);
        favAptRecyclerView.setAdapter(favAptRecyclerAdapter);


        personalPostModelList = new ArrayList<>();
        personalPostRecyclerAdapter = new PersonalPostRecyclerAdapter(personalPostModelList , getActivity(),mAuth.getCurrentUser().getUid(),  true);

        apartmentFavouriteSet = getActivity().getSharedPreferences(mAuth.getCurrentUser().getUid(), Context.MODE_PRIVATE).getStringSet(APARTMENT_FAVOURITES, null);
        personalFavouritesSet  = getActivity().getSharedPreferences(mAuth.getCurrentUser().getUid(), Context.MODE_PRIVATE).getStringSet(PERSONAL_FAVOURITES, null);
        getApartmentPostFromId();

        tabLayoutFavorite.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    favAptRecyclerView.setAdapter(favAptRecyclerAdapter );
                    if(favAptRecyclerAdapter.getItemCount()<1) {
                        getApartmentPostFromId();
                    }
                }
                else if(tab.getPosition()==1){
                    favAptRecyclerView.setAdapter(personalPostRecyclerAdapter );
                    if(personalPostRecyclerAdapter.getItemCount()<1) {
                        getPersonalPostFromId();
                    }
               ;
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


    private void getApartmentPostFromId() {

        for (String id
                : apartmentFavouriteSet) {
            apartmentPostReference.document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    Apartment apartment = (task.getResult().toObject(Apartment.class));
                    apartment.setApartmentID(task.getResult().getId());
                    apartmentList.add(apartment);
                    favAptRecyclerAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void getPersonalPostFromId() {
        for (String id: personalFavouritesSet){
            personalPostReference.document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    PersonalPostModel personalPost = (task.getResult().toObject(PersonalPostModel.class));
                    personalPost.setPostID(task.getResult().getId());
                    personalPostModelList.add(personalPost);
                    personalPostRecyclerAdapter.notifyDataSetChanged();
                }
            });

        }

    }



}