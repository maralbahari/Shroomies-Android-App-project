package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.shroomies.notifications.Data;
import com.example.shroomies.notifications.Sender;
import com.example.shroomies.notifications.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
   private ArrayList<User> userList;
   private Context context;
   View v;
   private DatabaseReference rootRef;
   private FirebaseAuth mAuth;
   Boolean fromSearchMember ;
   RequestQueue requestQueue;
   ShroomiesApartment apartment;
   private String senderName="";
   private HashMap<String,Boolean> requestAlreadySent=new HashMap<>();


    public UserAdapter(ArrayList<User> userList, Context context, Boolean fromSearchMember,ShroomiesApartment apartment) {
        this.userList = userList;
        this.context = context;
        this.fromSearchMember=fromSearchMember;
        this.apartment=apartment;
    }
    public UserAdapter(ArrayList<User> userList, Context context, Boolean fromSearchMember,ShroomiesApartment apartment,HashMap<String,Boolean> requestAlreadySent) {
        this.userList = userList;
        this.context = context;
        this.fromSearchMember=fromSearchMember;
        this.apartment=apartment;
        this.requestAlreadySent=requestAlreadySent;
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);

       v = layoutInflater.inflate(R.layout.send_request_card,parent,false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(context);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, final int position) {
        if(!userList.get(position).getImage().isEmpty()){
            GlideApp.with(context)
                    .load(userList.get(position).getImage())
                    .fitCenter()
                    .circleCrop()
                    .into(holder.userImage);
            holder.userImage.setPadding(2,2,2,2);
        }
        if(!mAuth.getCurrentUser().getUid().equals(apartment.getOwnerID())){
            holder.removeMember.setVisibility(View.INVISIBLE);
        }
        if(fromSearchMember){
            holder.msgMember.setVisibility(View.GONE);
            holder.removeMember.setVisibility(View.GONE);
            holder.userName.setText(userList.get(position).getName());
            if(!requestAlreadySent.isEmpty()){
                if(requestAlreadySent.get(userList.get(position).getID())){
                    holder.sendRequest.setVisibility(View.VISIBLE);
                    holder.sendRequest.setClickable(false);
                    holder.sendRequest.setText("requested");
                }else{
                    holder.sendRequest.setVisibility(View.VISIBLE);
                }
            }

        }if(!fromSearchMember){
            if (userList.get(position).getID().equals(mAuth.getInstance().getCurrentUser().getUid())){
                holder.msgMember.setVisibility(View.INVISIBLE);
                holder.removeMember.setVisibility(View.INVISIBLE);
                holder.userName.setText("You");

            }else{
                holder.msgMember.setVisibility(View.VISIBLE);
                holder.userName.setText(userList.get(position).getName());

            }
            }

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
    public class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView userImage;
        TextView userName;
        Button sendRequest;
        ImageButton msgMember, removeMember;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userImage=itemView.findViewById(R.id.request_member_photo);
            userName=itemView.findViewById(R.id.request_member_name);
            sendRequest=itemView.findViewById(R.id.send_request_btn);
            msgMember = itemView.findViewById(R.id.msg_member);
            removeMember = itemView.findViewById(R.id.remove_member);
            sendRequest.setClickable(true);
            getSenderName();
            msgMember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ChattingActivity.class);
                    intent.putExtra("USERID",userList.get(getAdapterPosition()).getID());
                    context.startActivity(intent);
                }
            });

            removeMember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final DatabaseReference ref= rootRef.child("apartments").push();
                    final String newApartmentID =ref.getKey();
                    final HashMap<String,Object> apartmentDetails=new HashMap<>();
                    apartmentDetails.put("apartmentID",newApartmentID);
                    apartmentDetails.put("ownerID",userList.get(getAdapterPosition()).getID());
                    ref.updateChildren(apartmentDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            rootRef.child("Users").child(userList.get(getAdapterPosition()).getID()).child("isPartOfRoom").setValue(newApartmentID).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    rootRef.child("apartments").child(apartment.getApartmentID()).child("apartmentMembers").child(userList.get(getAdapterPosition()).getID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            saveToRemovedLog(apartment.getApartmentID(),userList.get(getAdapterPosition()).getName());
                                            Toast.makeText(context,"User deleted successfully",Toast.LENGTH_LONG).show();
                                            notifyItemRemoved(getAdapterPosition());

                                        }
                                    });
                                }
                            });
                        }
                    });

                }
            });

            sendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendRequestToUser(userList.get(getAdapterPosition()).getID());
                }
            });

        }
        private void sendRequestToUser(final String id) {
            rootRef.child("shroomieRequests").child(mAuth.getCurrentUser().getUid()).child(id).child("requestType").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        rootRef.child("shroomieRequests").child(id).child(mAuth.getCurrentUser().getUid()).child("requestType").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(context,"invitation has been sent",Toast.LENGTH_LONG).show();
                                    sendRequest.setText("requested");
                                    sendRequest.setClickable(false);
                                    saveToRequestsLog(apartment.getApartmentID(),id);
                                    sendNotification(id,senderName+" wants to be your shroomie");
                                }else{
                                    sendRequest.setClickable(true);
                                }
                            }
                        });
                    }
                }
            });
    }
    private void getSenderName(){
            rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        senderName=snapshot.getValue().toString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }
        private void sendNotification(final String receiverID,final String message) {
            rootRef.child("Token").orderByKey().equalTo(receiverID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Token token = (Token) ds.getValue(Token.class);
                            Data data = new Data( mAuth.getCurrentUser().getUid(),  message, "New request", receiverID, (R.drawable.ic_notification_icon)  ,"false","true" );
                            Sender sender = new Sender(data, token.getToken());
                            try {
                                JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
                                JsonObjectRequest jsonObjectRequest=new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {

                                        Log.d("JSON_RESPONSE","onResponse:"+response.toString());
                                    }

                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d("JSON_RESPONSE","onResponse:"+error.toString());

                                    }
                                })
                                {
                                    @Override
                                    public Map<String,String> getHeaders() throws AuthFailureError {
                                        Map<String,String> headers= new HashMap<>();
                                        headers.put("Content-type","application/json");
                                        headers.put("Authorization","Key=AAAAyn_kPyQ:APA91bGLxMB-HGP-qd_EPD3wz_apYs4ZJIB2vyAvH5JbaTVlyLExgYn7ye-076FJxjfrhQ-1HJBmptN3RWHY4FoBdY08YRgplZSAN0Mnj6sLbS6imKa7w0rqPsLtc-aXMaPOhlxnXqPs");
                                        return headers;
                                    }

                                };

                                requestQueue.add(jsonObjectRequest);
                                requestQueue.start();
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }
    private void saveToRemovedLog(final String apartmentID,String username){
        DatabaseReference ref=rootRef.child("logs").child(apartmentID).push();
        String logID=ref.getKey();
        final HashMap<String,Object> newRecord=new HashMap<>();
        newRecord.put("actor",apartment.getOwnerID());
        newRecord.put("action","removing");
        newRecord.put("when",ServerValue.TIMESTAMP);
        newRecord.put("removedUser",username);
        newRecord.put("logID",logID);
        ref.updateChildren(newRecord).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(context,"I am here",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private void saveToRequestsLog(final String apartmentID,String userID){

        DatabaseReference ref=rootRef.child("logs").child(apartmentID).push();
        String uniqueID=ref.getKey();
        final HashMap<String,Object> newRecord=new HashMap<>();
        newRecord.put("when", ServerValue.TIMESTAMP);
        newRecord.put("actor",mAuth.getCurrentUser().getUid());
        newRecord.put("receivedBy",userID);
        newRecord.put("logID",uniqueID);
        newRecord.put("action","requesting");
        ref.updateChildren(newRecord).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(context,"I am here",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



}
