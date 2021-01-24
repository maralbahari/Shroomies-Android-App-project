package com.example.shroomies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private Context context;
    View v;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    private ArrayList<User> usersList;
    String currentUserAppartmentId;
    Boolean receiverUsers;

    public RequestAdapter(Context context,ArrayList<User> usersList,Boolean receiverUsers) {
        this.context = context;
        this.usersList=usersList;
        this.receiverUsers=receiverUsers;
    }

    private void getUserRoomId(){
        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentUserAppartmentId=snapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @NonNull
    @Override
    public RequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        v = layoutInflater.inflate(R.layout.request_card,parent,false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        mAuth= FirebaseAuth.getInstance();
        getUserRoomId();
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
                               usersList.remove(position);
                               notifyItemRemoved(position);
                               }
                           });
                       }
                   });
               }
           });

           //set cancele request button here to visible

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



            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentUserAppartmentId.equals(mAuth.getCurrentUser().getUid())){
                        acceptRequest(usersList.get(getAdapterPosition()).getID(),usersList.get(getAdapterPosition()).getName(),usersList.get(getAdapterPosition()).getIsPartOfRoom());

                    }else{
                        Toast.makeText(context,"You cannot accept requests, you are part of an apartment already",Toast.LENGTH_LONG).show();

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
                                usersList.remove(getAdapterPosition());
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
                        rootRef.child("shroomieRequests").child(mAuth.getCurrentUser().getUid()).child(senderID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                rootRef.child("shroomieRequests").child(senderID).child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        final HashMap<String, Object> addUsers = new HashMap<>();
                                        addUsers.put(mAuth.getCurrentUser().getUid(),mAuth.getCurrentUser().getUid());
                                        rootRef.child("apartments").child(senderApartmentID).child("apartmentMembers").updateChildren(addUsers).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                rootRef.child("apartments").child(mAuth.getCurrentUser().getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(context,"Your personal cards have been deleted you are part of  "+senderName+" apartment",Toast.LENGTH_LONG).show();
                                                            usersList.remove(getAdapterPosition());
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
}
