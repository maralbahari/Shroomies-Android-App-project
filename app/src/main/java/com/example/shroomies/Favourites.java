package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;


public class Favourites extends AppCompatActivity {
    //data
    private static final String PERSONAL_FAVOURITES = "PERSONAL_FAVOURITES";
    private static final String APARTMENT_FAVOURITES = "APARTMENT_FAVOURITES";
    private Set<String> apartmentFavouriteSet;
    private Set<String> personalFavouritesSet;
    private List<PersonalPostModel> personalPostList;
    private List<Apartment> apartmentList;

    //views
    private TabLayout tabLayoutFavorite;
    private RecyclerView favAptRecyclerView;
    private RecycleViewAdapterApartments favAptRecyclerAdapter;
    private LottieAnimationView heartAnimationView;
    private RelativeLayout noFavouritesLayout;

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDocRef;
    private CollectionReference apartmentPostReference;
    private CollectionReference personalPostReference;
    private PersonalPostRecyclerAdapter personalPostRecyclerAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favourites_activity);

        mDocRef = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        apartmentPostReference = mDocRef.collection("postApartment");
        personalPostReference = mDocRef.collection("postPersonal");

        tabLayoutFavorite = findViewById(R.id.tab_layout_Fav);
        favAptRecyclerView = findViewById(R.id.fav_recycler_view);
        heartAnimationView = findViewById(R.id.favourites_anim);
        MaterialButton goToSearch = findViewById(R.id.go_to_search);
        noFavouritesLayout = findViewById(R.id.no_favourited_posts_layout);

        Toolbar favouritesToolbar = findViewById(R.id.favourites_toolbar);
        setSupportActionBar(favouritesToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        favAptRecyclerView.setLayoutManager(linearLayoutManager);

        apartmentList = new ArrayList<>();
        favAptRecyclerAdapter = new RecycleViewAdapterApartments(apartmentList, getApplicationContext(), true, false);
        //favAptRecyclerView.setAdapter(favAptRecyclerAdapter);

        personalPostList = new ArrayList<>();
        personalPostRecyclerAdapter = new PersonalPostRecyclerAdapter(personalPostList, getApplicationContext(), true, false);

        apartmentFavouriteSet = getApplicationContext().getSharedPreferences(mAuth.getCurrentUser().getUid(), Context.MODE_PRIVATE).getStringSet(APARTMENT_FAVOURITES, null);
        personalFavouritesSet = getApplicationContext().getSharedPreferences(mAuth.getCurrentUser().getUid(), Context.MODE_PRIVATE).getStringSet(PERSONAL_FAVOURITES, null);
       // getApartmentPostFromId();

        goToSearch.setOnClickListener(v -> {
            //if no favourite posts, navigate to search
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        tabLayoutFavorite.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    favAptRecyclerView.setAdapter(favAptRecyclerAdapter);
                    favAptRecyclerView.setVisibility(View.VISIBLE);
                    heartAnimationView.pauseAnimation();
                    heartAnimationView.setVisibility(View.GONE);
                    noFavouritesLayout.setVisibility(View.GONE);

                    if (apartmentList!=null){
                        if(apartmentList.isEmpty()){
                            favAptRecyclerView.setVisibility(View.GONE);
                            heartAnimationView.playAnimation();
                            heartAnimationView.setVisibility(View.VISIBLE);
                            noFavouritesLayout.setVisibility(View.VISIBLE);
                        }
                        else {
                            getApartmentPostFromId();
                        }
                    }
//                    if (favAptRecyclerAdapter.getItemCount() < 1) {
//                        getApartmentPostFromId();
//                    }
                }
                else if (tab.getPosition() == 1) {
                    favAptRecyclerView.setAdapter(personalPostRecyclerAdapter);
                    favAptRecyclerView.setVisibility(View.VISIBLE);
                    heartAnimationView.pauseAnimation();
                    heartAnimationView.setVisibility(View.GONE);
                    noFavouritesLayout.setVisibility(View.GONE);

                    if (personalPostList !=null){
                        if(personalPostList.isEmpty()){
                            favAptRecyclerView.setVisibility(View.GONE);
                            heartAnimationView.playAnimation();
                            heartAnimationView.setVisibility(View.VISIBLE);
                            noFavouritesLayout.setVisibility(View.VISIBLE);
                        } else {
                            getPersonalPostFromId();
                        }
                    }

//                    if (personalPostRecyclerAdapter.getItemCount() < 1) {
//                        getPersonalPostFromId();
//                    }
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

    private void getApartmentPostFromId () {
        for (String id : apartmentFavouriteSet) {
            apartmentPostReference.document(id).get().addOnCompleteListener(task -> {
                Apartment apartment = (task.getResult().toObject(Apartment.class));
                apartment.setApartmentID(task.getResult().getId());
                apartmentList.add(apartment);
                favAptRecyclerAdapter.notifyDataSetChanged();
            });
        }
    }

    private void getPersonalPostFromId () {
        for (String id : personalFavouritesSet) {
            personalPostReference.document(id).get().addOnCompleteListener(task -> {
                PersonalPostModel personalPost = (task.getResult().toObject(PersonalPostModel.class));
                personalPost.setPostID(task.getResult().getId());
                personalPostList.add(personalPost);
                personalPostRecyclerAdapter.notifyDataSetChanged();
            });

        }

    }
}