package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.factor.bouncy.BouncyRecyclerView;
import com.factor.bouncy.util.OnOverPullListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class PersonalPostFragment extends Fragment {
    Query query;
    private CollectionReference personalPostReference;
    private FirebaseFirestore mDocRef;
    private  PersonalPostRecyclerAdapter personalPostRecyclerAdapter;
    private List<PersonalPostModel> personalPostModelList;
    private BouncyRecyclerView personalRecyclerView;
    private View v;
    private String lastCardKey;
    public static final int APARTMENT_PER_PAGINATION =15;
    private int newContentRange , previousContentRange;
    boolean searchState;
    User userSearchResult;
    OnOverPullListener mOnOverPullListener;
    CustomLoadingProgressBar customLoadingProgressBar;
    RefreshProgressBar refreshProgressBar;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_personal_post_tab, container, false);
        mDocRef = FirebaseFirestore.getInstance();
        personalPostReference =  mDocRef.collection("postPersonal");
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        customLoadingProgressBar = new CustomLoadingProgressBar(getActivity(), "Searching..." , R.raw.search_anim );
        customLoadingProgressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        personalRecyclerView = v.findViewById(R.id.personalRecView);
        personalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        Bundle bun = getArguments();


        personalPostModelList = new ArrayList<>();
        personalPostRecyclerAdapter = new PersonalPostRecyclerAdapter(personalPostModelList, getContext(), false);
        personalRecyclerView.setAdapter(personalPostRecyclerAdapter);

        if (bun != null) {
            String personalQuery = (String) bun.get("myQuery");
            customLoadingProgressBar.show();
            getSearch(personalQuery);
        }
        else{
            personalSearch();
        }


         mOnOverPullListener =  new OnOverPullListener() {
            @Override
            public void onOverPulledTop(float v) {

            }

            @Override
            public void onOverPulledBottom(float v) {

            }

            @Override
            public void onRelease() {
                // check if the user dragged from the bottom not  from the top
                if (!personalRecyclerView.canScrollVertically(1)) {
                    personalRecyclerView.setOnOverPullListener(null);
                    if(searchState){
                        if(userSearchResult!=null){
                            getUserPersonalPosts(userSearchResult);
                        }
                    }else{
                        personalSearch();
                    }

                }
                }
        };
        personalRecyclerView.setOnOverPullListener(mOnOverPullListener);


    }


    public void personalSearch(){
        searchState = false;
        // gets the N most recent posts in the users city
        // check if the last card key is empty
        // if its empty this is the first data to be loaded
        if (lastCardKey!=null) {
            refreshProgressBar = new RefreshProgressBar();
            refreshProgressBar.show(getParentFragmentManager() , null);
            query = personalPostReference.orderBy("time_stamp", Query.Direction.ASCENDING).orderBy(FieldPath.documentId()).startAfter(lastCardKey).limit(APARTMENT_PER_PAGINATION);
        }else{
            // starts from the beginning
            query = personalPostReference.orderBy("time_stamp", Query.Direction.ASCENDING).orderBy(FieldPath.documentId()).limit(APARTMENT_PER_PAGINATION);
        }

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                // if the snapshot returns less than or equal to one child
                // then no new apartment has been found
                if(task.isSuccessful()){
                    if(task.getResult().size()<1){
                        closeProgressBarsSetOverPullListener();
                        return;
                    }
                    setAdapterData(task);
                }else{
                    closeProgressBarsSetOverPullListener();
                }


            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                closeProgressBarsSetOverPullListener();
            }
        });


    }

    private void setAdapterData(Task<QuerySnapshot> task) {
        closeProgressBarsSetOverPullListener();
        if(personalPostModelList.size()==0){
            newContentRange=0;
            previousContentRange = 0;
        }
        for (QueryDocumentSnapshot document :
                task.getResult()) {
            lastCardKey = document.getId();
            PersonalPostModel personalPostModel = document.toObject(PersonalPostModel.class);
            personalPostModelList.add(personalPostModel);
            newContentRange++;
        }

        personalPostRecyclerAdapter.notifyItemRangeInserted(previousContentRange , newContentRange );
        previousContentRange = newContentRange;

    }

    private void getSearch(String personalQuery) {
        searchState = true;

        DatabaseReference myRootRef = FirebaseDatabase.getInstance().getReference();
        myRootRef.child("Users").orderByChild("name").equalTo(personalQuery).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    userSearchResult = snapshot.getValue(User.class);
                    getUserPersonalPosts(userSearchResult);
                }else{
                    closeProgressBarsSetOverPullListener();
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
                closeProgressBarsSetOverPullListener();
            }
        });

    }

    private void getUserPersonalPosts(User user) {
        if (lastCardKey!=null) {
            query = personalPostReference.whereEqualTo("userID" , user.getID() ).orderBy(FieldPath.documentId()).startAfter(lastCardKey).limit(APARTMENT_PER_PAGINATION);
        }else{
            // starts from the beginning
            query = personalPostReference.limit(APARTMENT_PER_PAGINATION);
        }
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
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
        personalRecyclerView.setOnOverPullListener(mOnOverPullListener);
        if(customLoadingProgressBar!=null){
            customLoadingProgressBar.dismiss();
        }
        if(refreshProgressBar!=null){
            refreshProgressBar.dismiss();
        }
    }



}