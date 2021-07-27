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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.rpc.context.AttributeContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class RequestFragment extends Fragment {
   private View v;
   private RecyclerView requestRecyclerView;
   private FirebaseAuth mAuth;
   private DatabaseReference rootRef;
   private TabLayout requestTab;

   private ArrayList<User> senderUsers;
   private ArrayList<String> senderIDs;
   private ArrayList<String> receiverIDs;
   private ArrayList<User> receiverUsers;

   private String apartmentID;

   private ValueEventListener invitationsListener;
   private ValueEventListener requestsListener;
   private ValueEventListener apartmentListener;
   private RequestQueue requestQueue;

   private RecyclerView invitationRecyclerView;

   private RequestAdapter requestAdapter;
   private RequestAdapter invitationAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_my_requests, container, false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(getActivity());

//        getApartmentDetailsOfCurrentUser();
    return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestRecyclerView = v.findViewById(R.id.request_recyclerview);
        requestTab = v.findViewById(R.id.request_tablayout);
        invitationRecyclerView=v.findViewById(R.id.invitation_recyclerview);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        requestRecyclerView.setHasFixedSize(true);
        requestRecyclerView.setLayoutManager(linearLayoutManager);

        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext());
        invitationRecyclerView.setLayoutManager(linearLayoutManager1);
        invitationRecyclerView.setHasFixedSize(true);


        requestTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition()==0){
                    requestTab.setSelectedTabIndicator(R.drawable.tab_indicator_left);
                    invitationRecyclerView.setVisibility(View.VISIBLE);
                    requestRecyclerView.setVisibility(View.GONE);
//                    getSenderId();
                }
                else if(tab.getPosition()==1){
                    requestTab.setSelectedTabIndicator(R.drawable.tab_indicator_right);
                    invitationRecyclerView.setVisibility(View.GONE);
                    requestRecyclerView.setVisibility(View.VISIBLE);
//                    getReceiverID();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        getCurrentUserDetails();


    }

    private void getRequests() {
        senderUsers = new ArrayList<>();
        receiverUsers = new ArrayList<>();

        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            jsonObject.put("userID" , mAuth.getCurrentUser().getUid());
            data.put("data" , jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_GET_REQUESTS, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // response is a json array containing  two json arrays
                // the first array contains the user objects of the
                // requests sent to this user
                // the second contains user objects of that the current
                // user has sent requests to
                final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                JSONArray sentJsonArray = null;
                JSONArray recievedJsonArray = null;
                try {
                    JSONArray result = (JSONArray) response.get("result");
                    sentJsonArray = (JSONArray) result.get(0);
                    recievedJsonArray = (JSONArray) result.get(1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(sentJsonArray!=null){
                    for(int i=0; i<sentJsonArray.length() ; i++){
                        try {
                            User user = mapper.readValue(sentJsonArray.get(i).toString() , User.class);
                            senderUsers.add(user);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    invitationAdapter= new RequestAdapter(getContext(),senderUsers,false,apartmentID);
                    invitationAdapter.notifyDataSetChanged();
                    invitationRecyclerView.setAdapter(invitationAdapter);

                }else{
                    //todo display something for the user
                }
                if(recievedJsonArray!=null){
                    for(int i=0; i<recievedJsonArray.length() ; i++){
                        try {
                            User user = mapper.readValue(recievedJsonArray.get(i).toString() , User.class);
                            receiverUsers.add(user);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    requestAdapter= new RequestAdapter(getContext(),receiverUsers,true,apartmentID);
                    requestAdapter.notifyDataSetChanged();
                    requestRecyclerView.setAdapter(requestAdapter);

                }else{
                    //todo display something for the user
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);

    }

//    private void getApartmentDetailsOfCurrentUser() {
//        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()){
//                    final String apartmentID=snapshot.getValue().toString();
//                    apartmentListener=rootRef.child("apartments").child(apartmentID).addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            if(snapshot.exists()){
//                                apartment=snapshot.getValue(ShroomiesApartment.class);
//                                getSenderId();
//
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }


//    private void getSenderId(){
//        senderIDs=new ArrayList<>();
//        invitationsListener=rootRef.child("shroomieRequests").child(mAuth.getCurrentUser().getUid()).orderByChild("requestType").equalTo("received").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()){
//                    for(DataSnapshot ds:snapshot.getChildren()){
//                        senderIDs.add(ds.getKey());
//                    }
//                    getSenderDetails(senderIDs);
//                }else{
//                    senderUsers=new ArrayList<>();
//                    invitationAdapter= new RequestAdapter(getContext(),senderUsers,false,apartment);
//                    invitationRecyclerView.setAdapter(invitationAdapter);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//    private void getSenderDetails(final ArrayList<String> senderIDs){
//        senderUsers=new ArrayList<>();
//        invitationAdapter= new RequestAdapter(getContext(),senderUsers,false,apartment);
//       invitationRecyclerView.setAdapter(invitationAdapter);
//     for(String id: senderIDs){
//         rootRef.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
//             @Override
//             public void onDataChange(@NonNull DataSnapshot snapshot) {
//             if(snapshot.exists()){
//                 User user= snapshot.getValue(User.class);
//                 senderUsers.add(user);
//             }
//             invitationAdapter.notifyDataSetChanged();
//             }
//
//             @Override
//             public void onCancelled(@NonNull DatabaseError error) {
//
//             }
//         });
//     }
//    }
//    private void getReceiverID(){
//        receiverIDs= new ArrayList<>();
//        rootRef.child("shroomieRequests").child(mAuth.getCurrentUser().getUid()).orderByChild("requestType").equalTo("sent").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()){
//                    for(DataSnapshot ds:snapshot.getChildren()){
//                        receiverIDs.add(ds.getKey());
//                    }
//                    getReceiverDetails(receiverIDs);
////                    Toast.makeText(getContext(),"req ids:"+receiverIDs.size(),Toast.LENGTH_SHORT).show();
//
//                }else{
//                    receiverUsers=new ArrayList<>();
//                    requestAdapter= new RequestAdapter(getContext(),receiverUsers,true,apartment);
//                    requestRecyclerView.setAdapter(requestAdapter);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//    private void getReceiverDetails(final ArrayList<String> receiverIDs){
//        receiverUsers=new ArrayList<>();
//        requestAdapter = new RequestAdapter(getActivity() , receiverUsers ,true ,apartment);
//        requestRecyclerView.setAdapter(requestAdapter);
//        for(String id: receiverIDs){
//            rootRef.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                    if(snapshot.exists()){
//                        User user= snapshot.getValue(User.class);
//                        receiverUsers.add(user);
//
//                    }
//                    requestAdapter.notifyDataSetChanged();
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError error) {
//
//                }
//            });
//
//        }
//
//    }
 void getCurrentUserDetails(){
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
     try {
         jsonObject.put("userID" , mAuth.getCurrentUser().getUid());
         data.put("data" , jsonObject);
     } catch (JSONException e) {
         e.printStackTrace();
     }
//     FirebaseUser firebaseUser = mAuth.getCurrentUser();
//     firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//         @Override
//         public void onComplete(@NonNull Task<GetTokenResult> task) {
//             if (task.isSuccessful()) {
//                 String token = task.getResult().getToken();

                 JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_GET_USER_DETAILS, data, new Response.Listener<JSONObject>() {
                     @Override
                     public void onResponse(JSONObject response) {
                         try {
                             apartmentID = (String) ((JSONObject)response.get("result")).get("apartmentID");
                             getRequests();

                         } catch (JSONException e) {
                             e.printStackTrace();
                         }
                     }
                 }, new Response.ErrorListener() {
                     @Override
                     public void onErrorResponse(VolleyError error) {

                     }
                 });
             requestQueue.add(jsonObjectRequest);

//             }
//         }
//     });
//

 }


}