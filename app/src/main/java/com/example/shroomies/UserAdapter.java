package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
   private final ArrayList<User> userList;
   private final Context context;
    private FirebaseAuth mAuth;
   private boolean fromSearchMember =false;
   private RequestQueue requestQueue;
   private final ShroomiesApartment apartment;
   private View parentView;


    public UserAdapter(ArrayList<User> userList, Context context,ShroomiesApartment apartment,View parentView) {
        this.userList = userList;
        this.context = context;
        this.apartment=apartment;
        this.parentView=parentView;
    }
    public UserAdapter(ArrayList<User> userList, Context context, Boolean fromSearchMember,ShroomiesApartment apartment) {
        this.userList = userList;
        this.context = context;
        this.fromSearchMember=fromSearchMember;
        this.apartment=apartment;
    }

    @NonNull
    @Override
    public UserAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View v = layoutInflater.inflate(R.layout.send_request_card, parent, false);
        mAuth=FirebaseAuth.getInstance();
//        mAuth.useEmulator("10.0.2.2" , 9099);
        requestQueue = Volley.newRequestQueue(context);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.UserViewHolder holder, final int position) {
        if(userList.get(position).getImage()!=null) {
            if (!userList.get(position).getImage().isEmpty()) {
                GlideApp.with(context)
                        .load(userList.get(position).getImage())
                        .fitCenter()
                        .circleCrop()
                        .transition(DrawableTransitionOptions.withCrossFade()) //Here a fading animation
                        .into(holder.userImage);
                holder.userImage.setPadding(2, 2, 2, 2);
            }
        }
        if(!mAuth.getCurrentUser().getUid().equals(apartment.getAdminID())){
            holder.removeMember.setVisibility(View.INVISIBLE);
        }
        if(fromSearchMember){
            holder.msgMember.setVisibility(View.GONE);
            holder.removeMember.setVisibility(View.GONE);
            holder.userName.setText(userList.get(position).getName());
            holder.msgMember.setVisibility(View.GONE);
            holder.sendRequest.setVisibility(View.VISIBLE);

            if(userList.get(position).requestSent()){
                holder.sendRequest.setClickable(false);
                holder.sendRequest.setText("Sent!");
            }
        }else{
            if (userList.get(position).getUserID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                holder.msgMember.setVisibility(View.INVISIBLE);
                holder.removeMember.setVisibility(View.INVISIBLE);
                holder.userName.setText("You");

            }

        }

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public int getItemViewType(int position) {
        return position;
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
            msgMember.setOnClickListener(view -> {
                Intent intent = new Intent(context, ChattingActivity.class);
                intent.putExtra("USERID",userList.get(getAdapterPosition()).getUserID());
                context.startActivity(intent);
            });

            removeMember.setOnClickListener(view -> removeMember(apartment.getApartmentID() , getAdapterPosition()));

            sendRequest.setOnClickListener(v -> sendRequestToUser(userList.get(getAdapterPosition()).getUserID() , apartment.getApartmentID()));

        }

        private void removeMember(String apartmentID, int position) {
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            firebaseUser.getIdToken(false).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    String token = task.getResult().getToken();
                    JSONObject jsonObject = new JSONObject();
                    JSONObject data = new JSONObject();
                    try {
                        jsonObject.put("apartmentID" , apartmentID);
                        jsonObject.put("userID" , userList.get(position).getUserID());
                        data.put("data" , jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_REMOVE_MEMBER, data, response -> {
                        try {
                            boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                            if(success){
                                Snackbar snack=Snackbar.make(parentView,userList.get(getAdapterPosition()).getName()+" has been removed", BaseTransientBottomBar.LENGTH_SHORT);
                                snack.show();
                                userList.remove(position);
                                notifyItemRemoved(position);
                            }else{
                                Snackbar snack=Snackbar.make(parentView,"We encountered an error while deleting the user", BaseTransientBottomBar.LENGTH_SHORT);
                                snack.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, error -> displayErrorAlert( error , null))
                    {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<>();
                            params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                            params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
                            return params;
                        }
                    };
                    requestQueue.add(jsonObjectRequest);

                }else{
                    String message = "We encountered a problem while authenticating your account";
                    displayErrorAlert(null, message);
                }
            });
        }

        private void sendRequestToUser( String id , String  apartmentID) {
            JSONObject  data = new JSONObject();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("senderID" , mAuth.getCurrentUser().getUid());
                jsonObject.put ("receiverID" , id);
                jsonObject.put("apartmentID" , apartmentID);
                data.put("data" , jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            firebaseUser.getIdToken(false).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_SEND_REQUEST, data, response -> {

                        try {
                            boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                            if(success){
                                sendRequest.setText("Sent!");
                                sendRequest.setClickable(false);
                            }else{
                                Snackbar snack=Snackbar.make(parentView,"We encountered an error while sending the request", BaseTransientBottomBar.LENGTH_SHORT);
                                snack.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }, error -> displayErrorAlert(error , null))
                    {
                        @Override
                        public Map<String, String> getHeaders()  {
                            Map<String, String> params = new HashMap<>();
                            params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                            params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
                            return params;
                        }
                    };
                    requestQueue.add(jsonObjectRequest);

                }else{
                    String message = "We encountered a problem while authenticating your account";
                    displayErrorAlert(null, message);
                }
            });


    }
        }
    void displayErrorAlert(@Nullable VolleyError error , String errorMessage){
        String message = null; // error message, show it in toast or dialog, whatever you want
        if(error!=null) {
            if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                message = "Cannot connect to Internet";
            } else if (error instanceof ServerError) {
                message = "The server could not be found. Please try again later";
            } else if (error instanceof ParseError) {
                message = "Parsing error! Please try again later";
            }
        }else{
            message = errorMessage;
        }
        Snackbar snack=Snackbar.make(parentView,message, BaseTransientBottomBar.LENGTH_SHORT);
        snack.show();

    }


}
