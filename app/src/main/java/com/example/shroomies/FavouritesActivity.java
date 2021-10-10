package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class FavouritesActivity extends AppCompatActivity implements NotifyEmptyApartmentAdapter, NotifyEmptyPersonalPostAdapter {
    //data
    private Set<String> apartmentFavouriteSet;
    private Set<String> personalFavouritesSet;
    private List<PersonalPostModel> personalPostList;
    private List<Apartment> apartmentList;

    private RecyclerView favouriteRecyclerView;
    private RecycleViewAdapterApartments favAptRecyclerAdapter;
    private LottieAnimationView heartAnimationView;
    private RelativeLayout noFavouritesLayout;
    private TabLayout tabLayoutFavorite;

    //firebase
    private FirebaseAuth mAuth;
    private CollectionReference apartmentPostReference;
    private CollectionReference personalPostReference;
    private PersonalPostRecyclerAdapter personalPostRecyclerAdapter;

    @Override
    public void onEmptyApartmentAdapter() {
        if (tabLayoutFavorite.getSelectedTabPosition() == 0) {
            showEmptyLayout();
        }
    }

    @Override
    public void onEmptyPersonalPostAdapter() {
        if (tabLayoutFavorite.getSelectedTabPosition() == 1) {
            showEmptyLayout();
        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favourites_activity);

        FirebaseFirestore mDocRef = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        apartmentPostReference = mDocRef.collection(Config.APARTMENT_POST);
        personalPostReference = mDocRef.collection(Config.PERSONAL_POST);

        //views
        tabLayoutFavorite = findViewById(R.id.tab_layout_Fav);
        favouriteRecyclerView = findViewById(R.id.fav_recycler_view);
        heartAnimationView = findViewById(R.id.favourites_anim);
        MaterialButton goToSearch = findViewById(R.id.go_to_search);
        noFavouritesLayout = findViewById(R.id.no_favourited_posts_layout);

        Toolbar favouritesToolbar = findViewById(R.id.favourites_toolbar);
        setSupportActionBar(favouritesToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        favouriteRecyclerView.setLayoutManager(linearLayoutManager);

        apartmentList = new ArrayList<>();
        favAptRecyclerAdapter = new RecycleViewAdapterApartments(apartmentList, FavouritesActivity.this, true, false, FavouritesActivity.this);
        favouriteRecyclerView.setAdapter(favAptRecyclerAdapter);


        personalPostList = new ArrayList<>();
        personalPostRecyclerAdapter = new PersonalPostRecyclerAdapter(personalPostList, FavouritesActivity.this, true, false, FavouritesActivity.this);

        apartmentFavouriteSet = getFavouriteApartmentPosts();
        personalFavouritesSet = getFavouritePersonalFavourites();

        getApartmentPostFromId();

        goToSearch.setOnClickListener(v -> {
            //if no favourite posts, navigate to search
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });

        tabLayoutFavorite.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    getApartmentTab();
                } else if (tab.getPosition() == 1) {
                    getPersonalTab();
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

    private void getPersonalTab() {
        favouriteRecyclerView.setAdapter(personalPostRecyclerAdapter);
        getPersonalPostFromId();
    }

    private void getApartmentTab() {
        favouriteRecyclerView.setAdapter(favAptRecyclerAdapter);
        getApartmentPostFromId();
    }


    private Set<String> getFavouritePersonalFavourites() {
        if (mAuth.getCurrentUser() != null) {
            return UserFavourites
                    .getInstance(getApplication(), mAuth.getCurrentUser().getUid())
                    .getPersonalPostFavourites();
        }
        return null;
    }

    private Set<String> getFavouriteApartmentPosts() {
        if (mAuth.getCurrentUser() != null) {
            return UserFavourites
                    .getInstance(getApplication(), mAuth.getCurrentUser().getUid())
                    .getApartmentPostFavourites();
        }
        return null;
    }

    private void getApartmentPostFromId() {
        if (apartmentFavouriteSet != null && !apartmentFavouriteSet.isEmpty()) {
            for (String id : apartmentFavouriteSet) {
                apartmentPostReference.document(id).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        hideEmptyLayout();
                        Apartment apartment = (task.getResult().toObject(Apartment.class));
                        if (apartment != null) {
                            apartment.setApartmentID(task.getResult().getId());
                            apartmentList.add(apartment);
                            favAptRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        } else {
            if (tabLayoutFavorite.getSelectedTabPosition() == 0) {
                showEmptyLayout();
            }
        }
    }

    private void getPersonalPostFromId() {
        if (personalFavouritesSet != null && !personalFavouritesSet.isEmpty()) {
            for (String id : personalFavouritesSet) {
                personalPostReference.document(id).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            hideEmptyLayout();
                            PersonalPostModel personalPost = (task.getResult().toObject(PersonalPostModel.class));
                            if (personalPost != null) {
                                personalPost.setPostID(task.getResult().getId());
                                personalPostList.add(personalPost);
                                personalPostRecyclerAdapter.notifyDataSetChanged();
                            }

                        }
                    }

                });
            }
        } else {
            if (tabLayoutFavorite.getSelectedTabPosition() == 1) {
                showEmptyLayout();

            }
        }
    }

    void showEmptyLayout() {
        favouriteRecyclerView.setVisibility(View.GONE);
        heartAnimationView.playAnimation();
        heartAnimationView.setVisibility(View.VISIBLE);
        noFavouritesLayout.setVisibility(View.VISIBLE);
    }

    void hideEmptyLayout() {
        favouriteRecyclerView.setVisibility(View.VISIBLE);
        heartAnimationView.pauseAnimation();
        heartAnimationView.setVisibility(View.GONE);
        noFavouritesLayout.setVisibility(View.GONE);
    }


}