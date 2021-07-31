package com.example.shroomies;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private final Context context;
    View v;
    FirebaseAuth mAuth;
    private final ArrayList<User> usersList;
    Boolean receiverUsers;
    String apartmentID;
    RequestQueue requestQueue;
    RelativeLayout rootLayout;




    public RequestAdapter(Context context, RelativeLayout rootLayout ,ArrayList<User> usersList, Boolean receiverUsers, String apartment) {
        this.context = context;
        this.usersList=usersList;
        this.receiverUsers=receiverUsers;
        this.apartmentID =apartment;
        this.rootLayout = rootLayout;
    }


    @NonNull
    @Override
    public RequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        v = layoutInflater.inflate(R.layout.request_card,parent,false);
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
            accept.setOnClickListener(v -> {
                if(apartmentID!=null){
                    acceptRequest(usersList.get(getAdapterPosition()).getUserID());
                }else{
                    //todo handle error
                }
            });
            reject.setOnClickListener(v -> rejectRequest(usersList.get(getAdapterPosition()).getUserID(), getAdapterPosition()));
            cancel.setOnClickListener(v -> rejectRequest(usersList.get(getAdapterPosition()).getUserID() , getAdapterPosition()));
        }



        private void rejectRequest(final String senderID , int position){

            JSONObject data = new JSONObject();
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("senderID" , senderID);
                jsonObject.put("receiverID" , mAuth.getCurrentUser().getUid());
                data.put("data" , jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_CANCEL_OR_REJECT_REQUEST, data, response -> {

                try {
                    boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                    if(success){
                        usersList.remove(position);
                        notifyItemRemoved(position);
                    }else{
                        Snackbar.make(rootLayout,"We encountered an error while performing your request", BaseTransientBottomBar.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }, error -> displayErrorAlert(error));

            requestQueue.add(jsonObjectRequest);


        }
        private void acceptRequest(final String senderID){

            mAuth.getCurrentUser().getIdToken(false).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    String token = task.getResult().getToken();

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
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_ACCEPT_REQUEST, data, response -> {

                        try {
                            boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                            if(success){
                                String message = response.getJSONObject(Config.result).getString(Config.message);
                                Snackbar.make(rootLayout,message, BaseTransientBottomBar.LENGTH_LONG).show();
                                //todo display dialog and intent to the my shroomies page
                            }else{
                                Snackbar.make(rootLayout,"We encountered an error while performing your request", BaseTransientBottomBar.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }, error -> {
                        displayErrorAlert(error);
                    })
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

                }
            });


        }

    }
    void displayErrorAlert(@Nullable VolleyError error){
        String message = null;
        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
            message = "Cannot connect to Internet";
        } else if (error instanceof ServerError) {
            message = "Server error. Please try again later";
        } else if (error instanceof ParseError) {
            message = "Parsing error! Please try again later";
        }else{
            message = "Unexpected error";
        }

        Snackbar.make(rootLayout,message, BaseTransientBottomBar.LENGTH_LONG).show();




    }

}
