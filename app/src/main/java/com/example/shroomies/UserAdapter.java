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


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
   private ArrayList<User> userList;
   private Context context;
   View v;
   private DatabaseReference rootRef;
   private FirebaseAuth mAuth;
   Boolean fromSearchMember ;

   ShroomiesApartment apartment;
   Boolean fromMemberPageWithRequestMember;


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

       v = layoutInflater.inflate(R.layout.send_request_card,parent,false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
//        getUserRoomId();
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
            holder.sendRequest.setVisibility(View.VISIBLE);
            holder.msgMember.setVisibility(View.GONE);
            holder.removeMember.setVisibility(View.GONE);
            holder.userName.setText(userList.get(position).getName());

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

//    private void getUserRoomId(){
//        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists()){
//                    apartmentID=snapshot.getValue().toString();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }


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
                    rootRef.child("apartments").child(apartment.getApartmentID()).child("apartmentMembers").child(userList.get(getAdapterPosition()).getID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            final DatabaseReference ref= rootRef.child("apartments").push();
                            final String newApartmentID =ref.getKey();
                            final HashMap<String,Object> apartmentDetails=new HashMap<>();
                            apartmentDetails.put("apartmentID",newApartmentID);
                            apartmentDetails.put("ownerID",mAuth.getCurrentUser().getUid());
                            ref.updateChildren(apartmentDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    rootRef.child("Users").child(userList.get(getAdapterPosition()).getID()).child("isPartOfRoom").setValue(newApartmentID).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(context,"User deleted successfully",Toast.LENGTH_LONG).show();
                                            userList.remove(getAdapterPosition());
                                            notifyItemRemoved(getAdapterPosition());
                                            //add progress



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
                                }
                            }
                        });
                    }
                }
            });
    }

    }

}
