package com.example.shroomies;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.signature.ObjectKey;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class UserAdapterSplitExpenses extends RecyclerView.Adapter<UserAdapterSplitExpenses.UserViewHolderSplit> {
   private ShroomiesApartment apartment;
   private static ArrayList<User> shroomieList;
   private static Context context;
   private FirebaseAuth mAuth;
   private static String amount="";
   private View v;
   private DatabaseReference rootRef;
   private static HashMap<String,Object> sharedSplit=new HashMap<>();
   private TextView totalText;
   private boolean fromViewCard=false;

    public UserAdapterSplitExpenses(ShroomiesApartment apartment, ArrayList<User> shroomieList, Context context, String amount,TextView totalText) {
        this.apartment = apartment;
        this.shroomieList = shroomieList;
        this.context = context;
        this.amount = amount;
        this.totalText=totalText;
    }
    public UserAdapterSplitExpenses(ArrayList<User> shroomieList,Context context,boolean fromViewCard){
        this.shroomieList=shroomieList;
        this.context=context;
        this.fromViewCard=fromViewCard;
    }

    @NonNull
    @Override
    public UserViewHolderSplit onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        v = layoutInflater.inflate(R.layout.shroomies_split_card,parent,false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        return new UserViewHolderSplit(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolderSplit holder, int position) {
    if(!shroomieList.get(position).getImage().isEmpty()){
        GlideApp.with(context)
                .load(shroomieList.get(position).getImage())
                .fitCenter()
                .circleCrop()
                .into(holder.profilePic);
        holder.profilePic.setPadding(2,2,2,2);
    }
    holder.userName.setText(shroomieList.get(position).getName());
    if(fromViewCard){
        holder.amountSeekBar.setVisibility(View.INVISIBLE);
        holder.sharedAmount.setText(shroomieList.get(position).getSharedAmount()+" RM");
    }if(!fromViewCard){
            if(!amount.isEmpty()){
                holder.amountSeekBar.setMax((Integer.parseInt(amount)));
                holder.amountSeekBar.setProgress(Integer.parseInt(amount)/shroomieList.size());
                holder.sharedAmount.setText((Integer.parseInt(amount)/shroomieList.size())+" RM");

            }
    }

    }

    @Override
    public int getItemCount() {
        return shroomieList.size();
    }


    public class UserViewHolderSplit extends RecyclerView.ViewHolder {
        private ImageView profilePic;
        private TextView userName,sharedAmount;
        private SeekBar amountSeekBar;
        private int total;
        public UserViewHolderSplit(@NonNull View itemView) {
            super(itemView);
            profilePic=itemView.findViewById(R.id.shroomie_split_pic);
            userName=itemView.findViewById(R.id.shroomie_split_name);
            sharedAmount=itemView.findViewById(R.id.split_amount_tv);
            amountSeekBar=itemView.findViewById(R.id.shroomie_split_seekbar);
            if(totalText!=null){
                totalText.setText("Total: "+amount+" RM");
            }

            amountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    sharedAmount.setText(amountSeekBar.getProgress()+" RM");
                    shroomieList.get(getAdapterPosition()).setSharedAmount(amountSeekBar.getProgress());

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    total=+shroomieList.get(getAdapterPosition()).getSharedAmount();
                    if(total==-1){
                        totalText.setText("above your specified amount");
                        totalText.setTextColor(Color.RED);

                    }else{
                        totalText.setText("Total: "+total+" RM");
                    }
                }
            });

        }
        private int getTotalAmount(){
            int total=0;
            for(User user:shroomieList){
                total=+user.getSharedAmount();
                if(total<=Integer.parseInt(amount)){

                }else{
                   return -1;
                }
            }
            return total;

        }


    }

    public static HashMap<String, Object> getMembersSplitShares(){
        int total=0;
        for(User user:shroomieList){
            total=+user.getSharedAmount();
            if(total<=Integer.parseInt(amount)){
                sharedSplit.put(user.getID(),user.getSharedAmount());
            }else{
                Toast.makeText(context,"the total amount of shared for each shroomie is bigger than you specified",Toast.LENGTH_LONG).show();
            }

        }
        return sharedSplit;
    }


}
