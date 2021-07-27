package com.example.shroomies;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private Context context;
    View v;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    private ArrayList<User> usersList;
    String currentUserName="";
    Boolean receiverUsers;
    String apartmentID;
    private List<String> members;
    RequestQueue requestQueue;




    public RequestAdapter(Context context,ArrayList<User> usersList,Boolean receiverUsers,String apartment) {
        this.context = context;
        this.usersList=usersList;
        this.receiverUsers=receiverUsers;
        this.apartmentID =apartment;
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

       if(imageUrl!=null){
           if(!imageUrl.isEmpty()) {
               GlideApp.with(context)
                       .load(usersList.get(position).getImage())
                       .fitCenter()
                       .circleCrop()
                       .into(holder.senderImage);
           }
       }

       if(receiverUsers){
           holder.reject.setVisibility(View.INVISIBLE);
           holder.accept.setVisibility(View.INVISIBLE);
           holder.requetsTv.setText("has been invited by you");
           holder.cancel.setVisibility(View.VISIBLE);

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
//            getCurrentUserName();
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(apartmentID!=null){
                        acceptRequest(usersList.get(getAdapterPosition()).getUserID(),usersList.get(getAdapterPosition()).getApartmentID());
                    }else{
                        //todo handle error
                    }
                }
            });
            reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        rejectRequest(usersList.get(getAdapterPosition()).getUserID(), getAdapterPosition());

                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rejectRequest(usersList.get(getAdapterPosition()).getUserID() , getAdapterPosition());

                }
            });
        }



        private void rejectRequest(final String senderID , int position){
//            rootRef.child("shroomieRequests").child(mAuth.getCurrentUser().getUid()).child(senderID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//                    rootRef.child("shroomieRequests").child(senderID).child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                           notifyItemRemoved(getAdapterPosition());
//
//                        }
//                    });
//                }
//            });
            JSONObject data = new JSONObject();
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("senderID" , senderID);
                jsonObject.put("receiverID" , mAuth.getCurrentUser().getUid());
                data.put("data" , jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_CANCEL_OR_REJECT_REQUEST, data, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    usersList.remove(position);
                    notifyItemRemoved(position);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("cancel reequest" , error.getLocalizedMessage());

                }
            });
            requestQueue.add(jsonObjectRequest);


        }
        private void acceptRequest(final String senderID,final String senderApartmentID){
//            rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").setValue(senderApartmentID).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                   if(task.isSuccessful()){
//                       final HashMap<String,Object> newMember=new HashMap<>();
//                       newMember.put(mAuth.getCurrentUser().getUid(), mAuth.getCurrentUser().getUid());
//                       rootRef.child("apartments").child(senderApartmentID).child("apartmentMembers").updateChildren(newMember).addOnCompleteListener(new OnCompleteListener<Void>() {
//                           @Override
//                           public void onComplete(@NonNull Task<Void> task) {
//                               if(task.isSuccessful()){
//                                   rootRef.child("apartments").child(apartment.getApartmentID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                       @Override
//                                       public void onSuccess(Void aVoid) {
//                                           rootRef.child("archive").child(apartment.getApartmentID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                               @Override
//                                               public void onSuccess(Void aVoid) {
//                                                   rootRef.child("shroomieRequests").child(mAuth.getCurrentUser().getUid()).child(senderID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                       @Override
//                                                       public void onSuccess(Void aVoid) {
//                                                           rootRef.child("shroomieRequests").child(senderID).child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                               @Override
//                                                               public void onSuccess(Void aVoid) {
//                                                                   Snackbar.make(v,"your personal cards are deleted, you are part of "+senderName+" apartment now", BaseTransientBottomBar.LENGTH_LONG).show();
//
//                                                                   sendNotification(senderID,currentUserName+" accepted your request");
//                                                                   saveToJoinedLog(apartment.getApartmentID());
//                                                                   notifyItemRemoved(getAdapterPosition());
//
//
//                                                               }
//                                                           });
//                                                       }
//                                                   });
//                                               }
//                                           });
//
//                                       }
//                                   });
//
//                               }
//                           }
//                       });
//
//                   }
//                }
//            });
            JSONObject jsonObject = new JSONObject();
            JSONObject data = new JSONObject();

            try {
                data.put("data" , jsonObject);
                jsonObject.put("receiverID"  , mAuth.getCurrentUser().getUid());
                jsonObject.put("receiverApartmentID"  ,apartmentID );
                jsonObject.put("senderID" , senderID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_ACCEPT_REQUEST, data, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Snackbar.make(v,"your personal cards are deleted, you are part of apartment now", BaseTransientBottomBar.LENGTH_LONG).show();
                    //todo display dialog and intent to the my shroomies page

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            requestQueue.add(jsonObjectRequest);

        }
//        private boolean checkEliglibity(){
//            if(apartment.getAdminID().equals(mAuth.getCurrentUser().getUid()) && !(apartment.getApartmentMembers().values().isEmpty())){
//               Snackbar.make(v,"You have existing Shroomies in your apartment and not allowed to accept request",BaseTransientBottomBar.LENGTH_LONG).show();
//
//                return false;
//            }if(apartment.getAdminID().equals(mAuth.getCurrentUser().getUid()) && apartment.getApartmentMembers().values().isEmpty()){
//                return true;
//            }if(apartment.getApartmentMembers().containsValue(mAuth.getCurrentUser().getUid())){
//                Snackbar.make(v,"You cannot accept requests, you are part of an apartment already",BaseTransientBottomBar.LENGTH_LONG).show();
//                return false;
//            }else{
//                return false;
//            }
//
//        }
//    }
//    private void sendNotification(final String receiverID,final String message) {
//        rootRef.child("Token").orderByKey().equalTo(receiverID).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()) {
//                    for (DataSnapshot ds : snapshot.getChildren()) {
//                        Token token = (Token) ds.getValue(Token.class);
//                        Data data = new Data( mAuth.getCurrentUser().getUid(),  message, "New Shroomie", receiverID, (R.drawable.ic_notification_icon)  ,"false","false","true" );
//                        Sender sender = new Sender(data, token.getToken());
//                        try {
//                            JSONObject senderJsonObj = new JSONObject(new Gson().toJson(sender));
//                            JsonObjectRequest jsonObjectRequest=new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", senderJsonObj, new Response.Listener<JSONObject>() {
//                                @Override
//                                public void onResponse(JSONObject response) {
//
//                                    Log.d("JSON_RESPONSE","onResponse:"+response.toString());
//                                }
//
//                            }, new Response.ErrorListener() {
//                                @Override
//                                public void onErrorResponse(VolleyError error) {
//                                    Log.d("JSON_RESPONSE","onResponse:"+error.toString());
//
//                                }
//                            })
//                            {
//                                @Override
//                                public Map<String,String> getHeaders() throws AuthFailureError {
//                                    Map<String,String> headers= new HashMap<>();
//                                    headers.put("Content-type","application/json");
//                                    headers.put("Authorization","Key=AAAAyn_kPyQ:APA91bGLxMB-HGP-qd_EPD3wz_apYs4ZJIB2vyAvH5JbaTVlyLExgYn7ye-076FJxjfrhQ-1HJBmptN3RWHY4FoBdY08YRgplZSAN0Mnj6sLbS6imKa7w0rqPsLtc-aXMaPOhlxnXqPs");
//                                    return headers;
//                                }
//
//                            };
//
//                            requestQueue.add(jsonObjectRequest);
//                            requestQueue.start();
//                        }catch (JSONException e){
//                            e.printStackTrace();
//                        }
//                    }
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//    }
//    private void getCurrentUserName(){
//        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()){
//                    currentUserName=snapshot.getValue().toString();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
//    private void saveToJoinedLog(final String apartmentID){
//        DatabaseReference ref=rootRef.child("logs").child(apartmentID).push();
//        String logID=ref.getKey();
//        final HashMap<String,Object> newRecord=new HashMap<>();
//        newRecord.put("actor",mAuth.getCurrentUser().getUid());
//        newRecord.put("action","joined");
//        newRecord.put("when", ServerValue.TIMESTAMP);
//        newRecord.put("logID",logID);
//        ref.updateChildren(newRecord).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if(task.isSuccessful()){
//
//                }
//            }
//        });
//
    }
}
