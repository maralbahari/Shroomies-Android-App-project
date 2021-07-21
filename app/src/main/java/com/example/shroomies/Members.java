package com.example.shroomies;

import android.content.DialogInterface;
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

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Members extends Fragment {
    //views
    private View v;
    private Button addMemberButton, leaveRoomButton;
    private RecyclerView membersRecyclerView;
    private TextView ownerName;
    private ImageView adminImageView , ghostImageView;
    private ImageButton msgOwnerImageButton;  // set visibility to gone if there are members in add members
    private RelativeLayout noMembersRelativeLayout;

    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private FirebaseFunctions mfunc;
    //data structures
    private ArrayList<User> membersList;
    private UserAdapter userAdapter;
   //model
    private ShroomiesApartment apartment;
    //variables
    private String isPartOfRoomID;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v =inflater.inflate(R.layout.fragment_shroomie_members, container, false);
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        mfunc=FirebaseFunctions.getInstance();
        mfunc.useEmulator("10.0.2.2",5001);
        return v;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addMemberButton =v.findViewById(R.id.add_shroomie_btn);
        leaveRoomButton =v.findViewById(R.id.leave_room_btn);
        adminImageView =v.findViewById(R.id.owner_image);
        ownerName=v.findViewById(R.id.admin_name);
        msgOwnerImageButton =v.findViewById(R.id.msg_admin);
        membersRecyclerView = v.findViewById(R.id.members_recyclerView);
        ghostImageView = v.findViewById(R.id.ghost_view);
        noMembersRelativeLayout = v.findViewById(R.id.no_members_relative_layout);

        Toolbar toolbar = getActivity().findViewById(R.id.my_shroomies_toolbar);
        toolbar.setTitle("Members");

        toolbar.setTitleTextColor(getActivity().getColor(R.color.jetBlack));
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setElevation(5);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar.setTitle(null);
                toolbar.setNavigationIcon(null);
                getActivity().onBackPressed();
            }
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

        addMemberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddShroomieMember add=new AddShroomieMember();
                Bundle bundle = new Bundle();
                bundle.putParcelable("APARTMENT_DETAILS",apartment);
                add.setArguments(bundle);
                add.show(getParentFragmentManager(),"add member to apartment");
            }
        });

        leaveRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Leave group");
                builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("leave", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        leaveApartment();
                    }
                });
                builder.setMessage("Leaving this group will remove all data and place you in an empty group.");
                builder.setIcon(R.drawable.ic_shroomies_yelllow_black_borders);


                builder.setCancelable(true);
                builder.create().show();

            }
        });

        msgOwnerImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChattingActivity.class);
                intent.putExtra("USERID",mAuth.getCurrentUser().getUid());
                startActivity(intent);
            }
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
        // Converting user to a map that can be read by cloud functions
        Map<String, String> map = new HashMap<>();
        map.put("apartmentID" , apartment.getApartmentID());
        map.put("userID" , mAuth.getCurrentUser().getUid());
        mfunc.getHttpsCallable(Config.FUNCTION_LEAVE_APARTMENT).call(map).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<HttpsCallableResult> task) {
                if(task.isSuccessful()){
                    getActivity().finish();
                    //todo add progress bar and close
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                //todo handle error
            }
        });
    }

//    private void leaveApartment(final String apartmentID) {
//
//        final CustomLoadingProgressBar customLoadingProgressBar = new CustomLoadingProgressBar(getActivity(),"Leaving room...",R.raw.loading_animation);
//        customLoadingProgressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//        customLoadingProgressBar.show();
//
//        final DatabaseReference ref= rootRef.child("apartments").push();
//        final String newApartmentID =ref.getKey();
//        final HashMap<String,Object> apartmentDetails=new HashMap<>();
//        apartmentDetails.put("apartmentID",newApartmentID);
//        apartmentDetails.put("ownerID",mAuth.getCurrentUser().getUid());
//        ref.updateChildren(apartmentDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if(task.isSuccessful()){
//                    rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").setValue(newApartmentID).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            rootRef.child("apartments").child(apartmentID).child("apartmentMembers").child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    customLoadingProgressBar.dismiss();
//                                    Intent intent = new Intent(getContext(),MainActivity.class);
//                                    startActivity(intent);
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    customLoadingProgressBar.dismiss();
//                                }
//                            });
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            customLoadingProgressBar.dismiss();
//                        }
//                    });
//                }
//            }
//        });

//
//
//    }


    private void getMemberDetail(ShroomiesApartment shroomiesApartment) {
        ArrayList<String> members = new ArrayList<>();
        //add the the admin to the members
        if(shroomiesApartment.getApartmentMembers()!=null){
            members.addAll(shroomiesApartment.getApartmentMembers().values());
        }
        members.add(shroomiesApartment.getAdminID());

        membersList = new ArrayList<>();
        userAdapter = new UserAdapter(membersList, getContext(),apartment,getView());
        membersRecyclerView.setAdapter(userAdapter);

        HashMap data=new HashMap();
        JSONArray jsonArray = new JSONArray(members);
        data.put("membersID",jsonArray);
        FirebaseFunctions mfunc = FirebaseFunctions.getInstance();
        mfunc.useEmulator("10.0.2.2",5001);
        mfunc.getHttpsCallable("getMembersDetails").call(data).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<HttpsCallableResult> task) {
                if(task.isSuccessful()){
                    final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                    ArrayList<HashMap> users = (ArrayList<HashMap>)task.getResult().getData();
                    if(users!=null) {
                        if (!users.isEmpty()) {
                            for (HashMap userObject :
                                    users) {
                                User user = mapper.convertValue(userObject, User.class);
                                //if the admin id corresponds to this user id then
                                // set the details of the admin without adding to the recycler view
                                if (user.getUserID().equals(apartment.getAdminID())) {
                                    setAdminDetails(user);
                                } else {
                                    membersList.add(user);
                                }
                            }
                            userAdapter.notifyDataSetChanged();
                        }else{
                            noMembersRelativeLayout.setVisibility(View.VISIBLE);
                        }
                    }else{
                        noMembersRelativeLayout.setVisibility(View.VISIBLE);
                    }

                }
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

}
