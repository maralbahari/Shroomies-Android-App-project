package com.example.shroomies;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Members extends Fragment {
    //views
    private View v;
    private RecyclerView membersRecyclerView;
    private TextView ownerName;
    private ImageView adminImageView , ghostImageView;
    private RelativeLayout noMembersRelativeLayout , rootLayout;

    //firebase
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;
    //data structures
    private ArrayList<User> membersList;
    private UserAdapter userAdapter;
   //model
    private ShroomiesApartment apartment;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v =inflater.inflate(R.layout.fragment_shroomie_members, container, false);
        requestQueue = Volley.newRequestQueue(getActivity());
        mAuth = FirebaseAuth.getInstance();
        mAuth.useEmulator("10.0.2.2" , 9009);
        return v;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button addMemberButton = v.findViewById(R.id.add_shroomie_btn);
        Button leaveRoomButton = v.findViewById(R.id.leave_room_btn);
        adminImageView =v.findViewById(R.id.owner_image);
        ownerName=v.findViewById(R.id.admin_name);
        // set visibility to gone if there are members in add members
        ImageButton msgOwnerImageButton = v.findViewById(R.id.msg_admin);
        membersRecyclerView = v.findViewById(R.id.members_recyclerView);
        ghostImageView = v.findViewById(R.id.ghost_view);
        noMembersRelativeLayout = v.findViewById(R.id.no_members_relative_layout);
        rootLayout = v.findViewById(R.id.relative_layout_member);

        Toolbar toolbar = getActivity().findViewById(R.id.my_shroomies_toolbar);
        toolbar.setTitle("Members");

        toolbar.setTitleTextColor(getActivity().getColor(R.color.jetBlack));
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setElevation(5);
        toolbar.setNavigationOnClickListener(view1 -> {
            toolbar.setTitle(null);
            toolbar.setNavigationIcon(null);
            getActivity().onBackPressed();
        });


        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity());
        membersRecyclerView.setHasFixedSize(true);
        membersRecyclerView.setLayoutManager(linearLayoutManager);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            apartment=bundle.getParcelable("APARTMENT_DETAILS");
            getMemberDetail(apartment);
            if(mAuth.getCurrentUser().getUid().equals(apartment.getAdminID())){
                leaveRoomButton.setVisibility(View.GONE);
                msgOwnerImageButton.setVisibility(View.INVISIBLE);
            }
            //this check is if the admin removed a member and that user is in member page so it refreshes
        }

        addMemberButton.setOnClickListener(v -> {
            AddShroomieMember add=new AddShroomieMember();
            Bundle bundle1 = new Bundle();
            bundle1.putParcelable("APARTMENT_DETAILS",apartment);
            add.setArguments(bundle1);
            add.show(getParentFragmentManager(),"add member to apartment");
        });

        leaveRoomButton.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Leave group");
            builder.setPositiveButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.setNegativeButton("leave", (dialog, which) -> leaveApartment());
            builder.setMessage("Leaving this group will remove all data and place you in an empty group.");
            builder.setIcon(R.drawable.ic_shroomies_yelllow_black_borders);
            builder.setCancelable(true);
            builder.create().show();

        });

        msgOwnerImageButton.setOnClickListener(view12 -> {
            Intent intent = new Intent(getContext(), ChattingActivity.class);
            intent.putExtra("USERID",mAuth.getCurrentUser().getUid());
            startActivity(intent);
        });

       Animation animUpDown = AnimationUtils.loadAnimation(getActivity(),
                R.anim.up_down);
        animUpDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ghostImageView.startAnimation(animation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        // start the animation
        ghostImageView.startAnimation(animUpDown);
    }

    private void leaveApartment(){
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            jsonObject.put(Config.apartmentID , apartment.getApartmentID());
            jsonObject.put(Config.userID , mAuth.getCurrentUser().getUid());
            data.put(Config.data, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.getIdToken(false).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult().getToken();
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.FUNCTION_LEAVE_APARTMENT, data, response -> {
                    try {
                        JSONObject result = (JSONObject) response.get(Config.result);
                        boolean success = result.getBoolean(Config.success);
                        if(success){
                            String message = result.getString(Config.message);
                            getActivity().finish();
                            Snackbar.make(rootLayout ,message , Snackbar.LENGTH_LONG );
                        }else{
                            String title = "Unexpected error";
                            String message = "We have encountered an unexpected error, try to check your internet connection and log in again.";
                            displayErrorAlert(title, message , null);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                        , error -> displayErrorAlert("Error", null, error)) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                        params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
                        return params;
                    }
                };
                requestQueue.add(jsonObjectRequest);
            }else{
                String title = "Authentication error";
                String message = "We encountered a problem while authenticating your account";
                displayErrorAlert(title, message , null);
            }
        });

    }


    private void getMemberDetail(ShroomiesApartment shroomiesApartment) {

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.getIdToken(false).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                ArrayList<String> members = new ArrayList<>();
                //add the the admin to the members
                if(shroomiesApartment.getApartmentMembers()!=null){
                    members.addAll(shroomiesApartment.getApartmentMembers().values());
                }
                members.add(shroomiesApartment.getAdminID());

                membersList = new ArrayList<>();
                userAdapter = new UserAdapter(membersList, getContext(),apartment,getView());
                membersRecyclerView.setAdapter(userAdapter);

                JSONObject jsonObject=new JSONObject();
                JSONObject data = new JSONObject();
                JSONArray jsonArray = new JSONArray(members);

                try {
                    jsonObject.put(Config.membersID,jsonArray);
                    data.put(Config.data , jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String token = task.getResult().getToken();
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.FUNCTION_GET_MEMBER_DETAIL, data, response -> {
                    final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                    try {
                        boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                        if (success){
                            JSONArray users = response.getJSONObject(Config.result).getJSONArray(Config.members);
                        if (users != null) {
                            if (users.length() != 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    User user = mapper.readValue((users.get(i)).toString(), User.class);
                                    //if the admin id corresponds to this user id then
                                    // set the details of the admin without adding to the recycler view
                                    if (user != null) {
                                        if (user.getUserID().equals(apartment.getAdminID())) {
                                            setAdminDetails(user);
                                        } else {
                                            membersList.add(user);
                                        }
                                    }
                                }
                                userAdapter.notifyDataSetChanged();
                            } else {
                                noMembersRelativeLayout.setVisibility(View.VISIBLE);
                            }
                        } else {
                            noMembersRelativeLayout.setVisibility(View.VISIBLE);
                        }
                    }else{
                        String title = "Unexpected error";
                        String message = "We have encountered an unexpected error, try to check your internet connection and log in again.";
                        displayErrorAlert(title, message , null);
                    }
                    } catch (JSONException | JsonProcessingException e) {
                        e.printStackTrace();
                    }

                }, error -> displayErrorAlert("Error" ,null , error )){
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                        params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
                        return params;
                    }
                };
                requestQueue.add(jsonObjectRequest);
            }else{
                String title = "Authentication error";
                String message = "We encountered a problem while authenticating your account";
                displayErrorAlert(title, message , null);
            }
        });

    }

    private void setAdminDetails(User user) {
        if(mAuth.getCurrentUser().getUid().equals(user.getUserID())){
            ownerName.setText("You");
        }else{
            ownerName.setText(user.getName());
        }
        if(user.getImage()!=null){
            if(!user.getImage().isEmpty()) {
                GlideApp.with(getContext())
                        .load(user.getImage())
                        .fitCenter()
                        .circleCrop()
                        .error(R.drawable.ic_user_profile_svgrepo_com)
                        .transition(DrawableTransitionOptions.withCrossFade()) //Here a fading animation
                        .into(adminImageView);
            }
        }
    }

    void displayErrorAlert(String title ,  String errorMessage  , VolleyError error){
        String message = null; // error message, show it in toast or dialog, whatever you want
        if(error!=null) {
            if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                message = "Cannot connect to Internet";
            } else if (error instanceof ServerError) {
                message = "Server error, Please try again later";
            } else if (error instanceof ParseError) {
                message = "Parsing error! Please try again later";
            }
        }else{
            message = errorMessage;
        }
        new android.app.AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_alert)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("return", (dialog, which) -> {
                    Members.this.getActivity().onBackPressed();
                    dialog.dismiss();
                })
                .create()
                .show();
    }

}
