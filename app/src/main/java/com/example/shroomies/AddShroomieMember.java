package com.example.shroomies;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.factor.bouncy.BouncyRecyclerView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AddShroomieMember extends DialogFragment {
    //views
    private View v;
    private SearchView memberSearchView;
    private BouncyRecyclerView addShroomieRecycler;
    private ImageButton closeButton;
    private TextView infoTextView;
    private TextView recommendedUsers;
    //firebase
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private FirebaseFunctions mfunc;
    //data
    private UserAdapter userRecyclerAdapter;
    private ArrayList<User> suggestedUser;
    private ArrayList<String> inboxUser;
    private Collection<String> listMemberId=new ArrayList<>();
    private ShroomiesApartment apartment;




    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
        }
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v =inflater.inflate(R.layout.add_shroomies, container, false);
        mAuth=FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        mfunc=FirebaseFunctions.getInstance();
        mfunc.useEmulator("10.0.2.2",5001);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        memberSearchView = v.findViewById(R.id.search_member);
        addShroomieRecycler = v.findViewById(R.id.add_member_recyclerview);
        closeButton = v.findViewById(R.id.close_button_add_shroomie);
        infoTextView = v.findViewById(R.id.information_text_view);
        recommendedUsers = v.findViewById(R.id.recommended_text_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        addShroomieRecycler.setHasFixedSize(true);
        addShroomieRecycler.setLayoutManager(linearLayoutManager);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        if (getArguments()!=null){
            Bundle bundle = getArguments();
            apartment=bundle.getParcelable("APARTMENT_DETAILS");
//            listMemberId.addAll(apartment.getApartmentMembers().values());
//            listMemberId.add(apartment.getAdminID());
        }
//        getMessageInboxListIntoAdapter();

        memberSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(suggestedUser!=null){
                    suggestedUser.clear();
                }
                searchUsers(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

    }

//    private void retreiveUser(String query) {
//
//        suggestedUser = new ArrayList<>();
//        userRecyclerAdapter= new UserAdapter(suggestedUser,getContext(),true,apartment,requestAlreadySent);
//        addShroomieRecycler.setAdapter(userRecyclerAdapter);
//
//        rootRef.child("Users").orderByChild("name").startAt(query)
//                .endAt(query+"\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    infoTextView.setVisibility(View.GONE);
//                    recommendedUsers.setVisibility(View.GONE);
//                    for (DataSnapshot ds : snapshot.getChildren()) {
//                        User user = ds.getValue(User.class);
//                        Boolean duplicate = false;
//                        for (User user1: suggestedUser){
//                            if (user1.getUserID().equals(user.getUserID())){
//                                duplicate = true;
//                            }
//                        }
//                        if (!duplicate&&!user.getUserID().equals(mAuth.getInstance().getCurrentUser().getUid())){
//                            if(listMemberId!=null){
//                                if(!listMemberId.contains(user.getUserID())){
//                                    suggestedUser.add(user);
////                                        checkRequestedUsers(user.getUserID());
//
//                                }
//
//                            }
//
//                        }
//                    }
//
//                }else{
//                    Snackbar snack=Snackbar.make(getView(),"This shroomie doesn't exist", BaseTransientBottomBar.LENGTH_SHORT);
//                    snack.show();
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
    void searchUsers(String query){
        suggestedUser = new ArrayList<>();
        userRecyclerAdapter= new UserAdapter(suggestedUser,getContext(),true,apartment);
        addShroomieRecycler.setAdapter(userRecyclerAdapter);
        if(!query.trim().isEmpty()){
            Map<String, String> map = new HashMap<>();
            map.put("name" , query.trim());
            mfunc.getHttpsCallable(Config.FUNCTION_SEARCH_USERS).call(map).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                @Override
                public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                    if(task.isSuccessful()){
                        final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                        ArrayList<HashMap> users = (ArrayList<HashMap>)task.getResult().getData();
                        if(users!=null) {
                            if (!users.isEmpty()) {
                                for (HashMap userObject :
                                        users) {
                                    User user = mapper.convertValue(userObject, User.class);
                                    //check if the user is already in this apartment
                                    if(apartment.getApartmentMembers()!=null){
                                        if(!apartment.getApartmentMembers().containsKey(user.getUserID())){
                                            suggestedUser.add(user);
                                        }

                                    }else{
                                        suggestedUser.add(user);
                                    }
                                }
                                Log.d("users" , suggestedUser.toString());
                                userRecyclerAdapter.notifyDataSetChanged();
                                recommendedUsers.setVisibility(View.GONE);

                            }else{
                                Snackbar snack=Snackbar.make(getView(),"We couldn't find any matching user ", BaseTransientBottomBar.LENGTH_SHORT);
                                snack.show();
                            }
                        }else{
                            Snackbar snack=Snackbar.make(getView(),"We couldn't find any matching user", BaseTransientBottomBar.LENGTH_SHORT);
                            snack.show();
                        }

                    }
                }
            });
        }
    }




//    private void addInboxUsersToRecycler(final List<String> inboxListUsers) {
//        suggestedUser = new ArrayList<>();
//        userRecyclerAdapter=new UserAdapter(suggestedUser,getContext(),true,apartment,requestAlreadySent);
//        addShroomieRecycler.setAdapter(userRecyclerAdapter);
//        for(final String id
//                :inboxListUsers){
//            rootRef.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                    suggestedUser.clear();
//                    if(snapshot.exists()){
//                        User user = snapshot.getValue(User.class);
//                        if (!listMemberId.contains(user.getUserID())) {
//                            suggestedUser.add(user);
//                            checkRequestedUsers(id);
//                            userRecyclerAdapter.notifyDataSetChanged();
//                        }
//
//                    }
//
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//        }
//    }

        //TODO add users from the  inbox
//    private void getMessageInboxListIntoAdapter() {
//        inboxUser = new ArrayList<>();
//        rootRef.child("PrivateChatList").child(mAuth.getInstance().getCurrentUser().getUid()).orderByChild("receiverID").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()){
//                    for(DataSnapshot dataSnapshot
//                            :snapshot.getChildren()){
//                        HashMap<String,String> recievers= (HashMap) dataSnapshot.getValue();
//                        inboxUser.add(recievers.get("receiverID"));
//
//                    }
//                    addInboxUsersToRecycler(inboxUser);
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//    private void checkRequestedUsers(final String id){
//
//        rootRef.child("shroomieRequests").child(mAuth.getCurrentUser().getUid()).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()){
//                    requestAlreadySent.put(id,true);
//                }else{
//                    requestAlreadySent.put(id,false);
//                }
//                userRecyclerAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }


}
