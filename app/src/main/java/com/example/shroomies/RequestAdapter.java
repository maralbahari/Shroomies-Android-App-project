package com.example.shroomies;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private Context context;
    View v;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    private ArrayList<User> usersList;
    String currentUserName="";
    Boolean receiverUsers;
    ShroomiesApartment apartment;
    private List<String> members;
    RequestQueue requestQueue;



    public RequestAdapter(Context context,ArrayList<User> usersList,Boolean receiverUsers,ShroomiesApartment apartment) {
        this.context = context;
        this.usersList=usersList;
        this.receiverUsers=receiverUsers;
        this.apartment=apartment;
    }




    @NonNull
    @Override
    public RequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        v = layoutInflater.inflate(R.layout.request_card,parent,false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        mAuth= FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(context);
        return  new RequestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.RequestViewHolder holder, final int position) {
       holder.senderName.setText(usersList.get(position).getName());
       String imageUrl= usersList.get(position).getImage();
       if(!imageUrl.isEmpty()){
           GlideApp.with(context)
                   .load(usersList.get(position).getImage())
                   .fitCenter()
                   .circleCrop()
                   .into(holder.senderImage);
       }

       if(receiverUsers){
           holder.reject.setVisibility(View.INVISIBLE);
           holder.accept.setVisibility(View.INVISIBLE);
           holder.requetsTv.setText("has been invited by you");
           holder.cancel.setVisibility(View.VISIBLE);
           holder.cancel.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   rootRef.child("shroomieRequests").child(mAuth.getCurrentUser().getUid()).child(usersList.get(position).getID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                       @Override
                       public void onSuccess(Void aVoid) {
                           rootRef.child("shroomieRequests").child(usersList.get(position).getID()).child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {
                               notifyItemRemoved(position);
                               }
                           });
                       }
                   });
               }
           });

       }else{
           holder.reject.setVisibility(View.VISIBLE);
           holder.accept.setVisibility(View.VISIBLE);
           holder.requetsTv.setText("has invited you");
           holder.cancel.setVisibility(View.INVISIBLE);
       }
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class RequestViewHolder extends RecyclerView.ViewHolder {
        Button accept,reject,cancel;
        ImageView senderImage;
        TextView senderName,requetsTv;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            accept=itemView.findViewById(R.id.accept_button);
            reject=itemView.findViewById(R.id.decline_btn);
            senderImage=itemView.findViewById(R.id.request_user_photo);
            senderName=itemView.findViewById(R.id.user_name);
            requetsTv=itemView.findViewById(R.id.requested_tv);
            cancel=itemView.findViewById(R.id.cancel_request);
            getCurrentUserName();
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(checkEliglibity()){
                        acceptRequest(usersList.get(getAdapterPosition()).getID(),usersList.get(getAdapterPosition()).getName(),usersList.get(getAdapterPosition()).getIsPartOfRoom());

                    }
                }
            });
            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rejectRequest(usersList.get(getAdapterPosition()).getID());
                }
            });
        }



        private void rejectRequest(final String senderID){
            rootRef.child("shroomieRequests").child(mAuth.getCurrentUser().getUid()).child(senderID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    rootRef.child("shroomieRequests").child(senderID).child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                           notifyItemRemoved(getAdapterPosition());

                        }
                    });
                }
            });
        }
        private void acceptRequest(final String senderID,final String senderName,final String senderApartmentID){
            rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").setValue(senderApartmentID).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                   if(task.isSuccessful()){
                       final HashMap<String,Object> newMember=new HashMap<>();
                       newMember.put(mAuth.getCurrentUser().getUid(), mAuth.getCurrentUser().getUid());
                       rootRef.child("apartments").child(senderApartmentID).child("apartmentMembers").updateChildren(newMember).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if(task.isSuccessful()){
                                   rootRef.child("apartments").child(apartment.getApartmentID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                       @Override
                                       public void onSuccess(Void aVoid) {
                                           rootRef.child("archive").child(apartment.getApartmentID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                               @Override
                                               public void onSuccess(Void aVoid) {
                                                   rootRef.child("shroomieRequests").child(mAuth.getCurrentUser().getUid()).child(senderID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                       @Override
                                                       public void onSuccess(Void aVoid) {
                                                           rootRef.child("shroomieRequests").child(senderID).child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                               @Override
                                                               public void onSuccess(Void aVoid) {
                                                                   Toast.makeText(context,"your personal cards are deleted, you are part of "+senderName+" apartment now",Toast.LENGTH_LONG).show();
                                                                   sendNotification(senderID,currentUserName+" accepted your request");
                                                                   notifyItemRemoved(getAdapterPosition());



                                                               }
                                                           });
                                                       }
                                                   });
                                               }
                                           });

                                       }
                                   });

                               }
                           }
                       });

                   }
                }
            });


        }
        private boolean checkEliglibity(){
            if(apartment.getOwnerID().equals(mAuth.getCurrentUser().getUid()) && !(apartment.getApartmentMembers().values().isEmpty())){
                Toast.makeText(context,"You have existing Shroomies in your apartment and not allowed to accept request",Toast.LENGTH_LONG).show();
                return false;
            }if(apartment.getOwnerID().equals(mAuth.getCurrentUser().getUid()) && apartment.getApartmentMembers().values().isEmpty()){
                return true;
            }if(apartment.getApartmentMembers().containsValue(mAuth.getCurrentUser().getUid())){
                Toast.makeText(context,"You cannot accept requests, you are part of an apartment already",Toast.LENGTH_LONG).show();
                return false;
            }else{
                return false;
            }

        }
    }
    private void sendNotification(final String receiverID,final String message) {
        rootRef.child("Token").orderByKey().equalTo(receiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Token token = (Token) ds.getValue(Token.class);
                        Data data = new Data( mAuth.getCurrentUser().getUid(),  message, "New Shroomie", receiverID, (R.drawable.ic_notification_icon)  ,"false","false","true" );
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
    private void getCurrentUserName(){
        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentUserName=snapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
