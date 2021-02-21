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
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private Context context;
    View v;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    private ArrayList<User> usersList;
    String currentUserAppartmentId;
    Boolean receiverUsers;
    ShroomiesApartment apartment;
    private ArrayList<String> members;

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
        return  new RequestViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.RequestViewHolder holder, final int position) {
       holder.senderName.setText(usersList.get(position).getName());
       String imageUrl= usersList.get(position).getImage();
       if(imageUrl!=null){
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
                                usersList.remove(getAdapterPosition());
                                notifyItemRemoved(getAdapterPosition());

                        }
                    });
                }
            });
        }
        private void acceptRequest(final String senderID,final String senderName,final String senderApartmentID){
            members = new ArrayList<>();

            rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").setValue(senderApartmentID).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                   if(task.isSuccessful()){
                       rootRef.child("apartments").child(senderApartmentID).child("apartmentMembers").addListenerForSingleValueEvent(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot snapshot) {
                               if(snapshot.exists()){
                                   members = (ArrayList<String>) snapshot.getValue();
                                   members.add(mAuth.getCurrentUser().getUid());
                               }else{
                                   members.add(mAuth.getCurrentUser().getUid());
                               }
                               final HashMap<String,Object> newUsers=new HashMap<>();
                               newUsers.put("apartmentMembers", members);
                               rootRef.child("apartments").child(senderApartmentID).updateChildren(newUsers).addOnCompleteListener(new OnCompleteListener<Void>() {
                                   @Override
                                   public void onComplete(@NonNull Task<Void> task) {
                                       if(task.isSuccessful()){
                                           Toast.makeText(context,"your personal cards are deleted, you are part of "+senderName+" apartment now",Toast.LENGTH_LONG).show();
                                       }
                                   }
                               });
                           }

                           @Override
                           public void onCancelled(@NonNull DatabaseError error) {

                           }
                       });

                   }
                }
            });
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
        private boolean checkEliglibity(){
            if(apartment.getOwnerID().equals(mAuth.getCurrentUser().getUid()) && apartment.getMembersID()!=null){
                Toast.makeText(context,"You have existing Shroomies in your apartment and not allowed to accept request",Toast.LENGTH_LONG).show();
                return false;
            }if(apartment.getOwnerID().equals(mAuth.getCurrentUser().getUid()) && apartment.getMembersID()==null){
                return true;
            }if(apartment.getMembersID().contains(mAuth.getCurrentUser().getUid())){
                Toast.makeText(context,"You cannot accept requests, you are part of an apartment already",Toast.LENGTH_LONG).show();
                return false;
            }else{
                return false;
            }

        }
    }
}
