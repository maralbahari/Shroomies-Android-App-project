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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.factor.bouncy.BouncyRecyclerView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private RequestQueue requestQueue;
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
        requestQueue = Volley.newRequestQueue(getActivity());
//        mfunc.useEmulator("10.0.2.2",5001);
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
                memberSearchView.clearFocus();
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

        if(!query.trim().isEmpty()){

            suggestedUser = new ArrayList<>();
            userRecyclerAdapter= new UserAdapter(suggestedUser,getContext(),true,apartment);
            userRecyclerAdapter.setHasStableIds(true);
            addShroomieRecycler.setAdapter(userRecyclerAdapter);

            JSONObject jsonObject = new JSONObject();
            JSONObject data =  new JSONObject();

            try {
                jsonObject.put("name" , query.trim());
                jsonObject.put("currentUser" , mAuth.getCurrentUser().getUid());
                data.put("data" , jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.FUNCTION_SEARCH_USERS, data, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {


                    JSONArray result = null;
                    try {
                         result = (JSONArray) response.get("result");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if(result!=null){
                        //the result is a jsonn array of json arrays
                        //the nested json array contains 2 indeces
                        // first index is the user object
                        // and the second  index is a boolean value
                        //indacting wether the current user has already
                        // sent a request to the searched user
                        final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                        for(int i= 0 ;i< result.length();i++){
                            try {
                                JSONArray jsonArray =((JSONArray)(result.get(i)));
                                //cast the first index as a json object and the second as a boolean value
                                JSONObject userJsonObject = (JSONObject) jsonArray.get(0);
                                boolean requestSent = (boolean)jsonArray.get(1);
                                User user =mapper.readValue(userJsonObject.toString() , User.class);
                                user.setRequestSent(requestSent);

                                if(apartment.getApartmentMembers()!=null){
                                    if(!apartment.getApartmentMembers().containsKey(user.getUserID())){
                                        suggestedUser.add(user);
                                    }
                                }else{
                                    suggestedUser.add(user);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (JsonMappingException e) {
                                e.printStackTrace();
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                        }
                        userRecyclerAdapter.notifyDataSetChanged();
                        recommendedUsers.setVisibility(View.GONE);

                    }else{
                        Snackbar snack=Snackbar.make(getView(),"We couldn't find any matching user", BaseTransientBottomBar.LENGTH_SHORT);
                        snack.show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            requestQueue.add(jsonObjectRequest);

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
