package com.example.shroomies;

import android.content.Context;
import android.graphics.Color;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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
   private static HashMap<String,Integer> sharedSplit=new HashMap<>();
   private TextView totalText;
   private boolean fromViewCard=false;
   private HashMap<String,Integer> sharesHashmap=new HashMap<>();
   shroomiesShares shares;
   private Fragment targetedFragment;
   private TextView ok;
    SplitExpenses.membersShares myMemberShares;



   public UserAdapterSplitExpenses(shroomiesShares shroomiesShares){
       this.shares=shroomiesShares;

   }

    public UserAdapterSplitExpenses(ShroomiesApartment apartment, ArrayList<User> shroomieList, Context context, String amount,TextView totalText,Fragment targetedFragment,TextView ok,SplitExpenses.membersShares myMemberShares) {
        this.apartment = apartment;
        this.shroomieList = shroomieList;
        this.context = context;
        this.amount = amount;
        this.totalText=totalText;
        this.targetedFragment=targetedFragment;
        this.ok=ok;
        this.myMemberShares=myMemberShares;
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
                shroomieList.get(position).setSharedAmount(holder.amountSeekBar.getProgress());
                sharesHashmap.put(shroomieList.get(position).getID(),shroomieList.get(position).getSharedAmount());
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
                    sharesHashmap.put(shroomieList.get(getAdapterPosition()).getID(),shroomieList.get(getAdapterPosition()).getSharedAmount());



                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {


                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
//                    shares.sendInput(sharesHashmap,totalText,context,ok,myMemberShares);
                    shares.sendInput(sharesHashmap);



                }
            });





        }

    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        try {
            shares = (shroomiesShares) targetedFragment;
        }catch(ClassCastException e){

        }
    }

    public static HashMap<String, Integer> getMembersSplitShares(){
        for(User user:shroomieList){
                sharedSplit.put(user.getID(),user.getSharedAmount());
        }
        return sharedSplit;
    }


    public interface shroomiesShares{
        void sendInput(HashMap<String,Integer> sharesHash);
    }
}
