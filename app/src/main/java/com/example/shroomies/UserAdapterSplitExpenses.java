package com.example.shroomies;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class UserAdapterSplitExpenses extends RecyclerView.Adapter<UserAdapterSplitExpenses.UserViewHolderSplit> {
   private View v;
   private Context context;
   private Fragment targetedFragment;

   private boolean fromViewCard=false;
   private String amount="";

   private ArrayList<User> shroomieList;
   private HashMap<String, Integer> sharesHashmap=new HashMap<String, Integer>();

   ShroomiesShares shares;


   public UserAdapterSplitExpenses(ShroomiesShares shroomiesShares){
       this.shares=shroomiesShares;
   }

    public UserAdapterSplitExpenses(Context context , ArrayList<User> shroomieList , String amount, Fragment targetedFragment) {
        this.shroomieList = shroomieList;
        this.context = context;
        this.amount = amount;
        this.targetedFragment=targetedFragment;
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
        return new UserViewHolderSplit(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolderSplit holder, int position) {

    if(!shroomieList.get(position).getImage().isEmpty()){
        GlideApp.with(context)
                .load(shroomieList.get(position).getImage())
                .centerCrop()
                .circleCrop()
                .error(R.drawable.ic_user_profile_svgrepo_com)
                .into(holder.profilePicImageView);
    }

    holder.userNameTextView.setText(shroomieList.get(position).getName());

    if(fromViewCard){
        holder.amountSeekBar.setVisibility(View.INVISIBLE);
        holder.sharedAmountEditText.setText(Integer.toString((int)shroomieList.get(position).getSharedAmount()));
    }else{
            if(!amount.isEmpty()){
                holder.amountSeekBar.setMinFloat(0);
                holder.amountSeekBar.setMaxFloat(Integer.parseInt(amount));
                holder.amountSeekBar.setValue(Integer.parseInt(amount)/shroomieList.size());
                holder.amountSeekBar.setKeyProgressIncrement(1);
                holder.sharedAmountEditText.setText(Integer.toString((int)holder.amountSeekBar.getValue()));
                shroomieList.get(position).setSharedAmount(holder.amountSeekBar.getProgress());
                sharesHashmap.put(shroomieList.get(position).getUserID(),((int)shroomieList.get(position).getSharedAmount()));
                shares.sendInput(sharesHashmap);
            }
    }

        // animate view changes
        setFadeAnimation(holder.itemView);


    }

    @Override
    public int getItemCount() {
        return shroomieList.size();
    }


    public class UserViewHolderSplit extends RecyclerView.ViewHolder {
        private ImageView profilePicImageView;
        private EditText sharedAmountEditText;
        private TextView userNameTextView;
        private FloatSeekBar amountSeekBar;

        public UserViewHolderSplit(@NonNull View itemView) {
            super(itemView);
            profilePicImageView =itemView.findViewById(R.id.shroomie_split_pic);
            userNameTextView =itemView.findViewById(R.id.shroomie_split_name);
            sharedAmountEditText =itemView.findViewById(R.id.split_amount_edit_text);
            amountSeekBar=itemView.findViewById(R.id.shroomie_split_seekbar);
            if(!fromViewCard) {
                amountSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        if (Integer.parseInt(sharedAmountEditText.getText().toString()) != seekBar.getProgress()) {
                            sharedAmountEditText.setText(Integer.toString((int) amountSeekBar.getValue()));
                            sharedAmountEditText.setSelection(sharedAmountEditText.getText().length());
                        }

                        shroomieList.get(getAdapterPosition()).setSharedAmount((int) amountSeekBar.getValue());
                        sharesHashmap.put(shroomieList.get(getAdapterPosition()).getUserID(), (int) shroomieList.get(getAdapterPosition()).getSharedAmount());
                        shares.sendInput(sharesHashmap);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {


                    }
                });
                sharedAmountEditText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!s.toString().isEmpty()) {
                            int amount = Integer.parseInt(s.toString());
                            if (amount > amountSeekBar.getMax()) {
                                sharedAmountEditText.setText(Integer.toString(amountSeekBar.getMax()));
                                sharedAmountEditText.setSelection(sharedAmountEditText.getText().length());

                                amountSeekBar.setValue(amountSeekBar.getMaxFloat());
                            } else {
                                amountSeekBar.setProgress(amount);
                            }


                        } else {
                            sharedAmountEditText.setText("0");
                            sharedAmountEditText.setSelection(sharedAmountEditText.getText().length());

                            amountSeekBar.setProgress(0);

                        }

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }


        }

    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        try {
            shares = (ShroomiesShares) targetedFragment;
        }catch(ClassCastException e){

        }
    }


    public interface ShroomiesShares {
        void sendInput(HashMap<String, Integer> sharesHash);
    }


    private void setFadeAnimation(View view) {
        Animation animation = new TranslateAnimation(0,0 , 0,view.getHeight());
        animation.setDuration(1000);
        view.startAnimation(animation);
    }
}
