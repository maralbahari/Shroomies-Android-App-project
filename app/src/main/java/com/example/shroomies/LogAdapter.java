package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    private View v;
    private Context context;
    private ArrayList<Log> logList;
    private String requestedUserName="";
    private DatabaseReference rootRef;
    private Spannable cardName=new SpannableString("");
    private LogAdapterToMyshroomies logAdapterToMyshroomies;
    private Fragment targetedFragment;
    private FragmentTransaction ft;
    private FragmentManager fm;



    public LogAdapter(Context context, ArrayList<Log> logList , FragmentManager fm, Fragment targetedFragment) {
        this.context = context;
        this.logList = logList;
        this.targetedFragment = targetedFragment;
        this.fm = fm;

    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        v = layoutInflater.inflate(R.layout.log_card,parent,false);
        rootRef=FirebaseDatabase.getInstance().getReference();
        return new LogViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {

      if(logList.get(position).getActorPic()!=null){
          if(!logList.get(position).getActorPic().isEmpty()) {
              GlideApp.with(context)
                      .load(logList.get(position).getActorPic())
                      .fitCenter()
                      .circleCrop()
                      .into(holder.userPic);
          }
      }
      String actorName=logList.get(position).getActorName();
      String action=logList.get(position).getAction();
      String cardTitle=logList.get(position).getCardTitle();
      String removedUser=logList.get(position).getRemovedUser();
      String receiverRequest=logList.get(position).getReceivedBy();
      long when=logList.get(position).getWhen();
      final String cardType=logList.get(position).getCardType();
      final String cardID=logList.get(position).getCardID();

          Date happend=(new Date(when));
          Date currentDate=(new Date(System.currentTimeMillis()));
          long diff=currentDate.getTime()-happend.getTime();
          long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diff);
        long seconds = diffInSec % 60;
         diffInSec /= 60;
        long minutes = diffInSec % 60;
        diffInSec /= 60;
        long hours = diffInSec % 24;
        diffInSec /= 24;
        long days = diffInSec;
         if(days>0){
             holder.logDate.setText(days+"d");
         }else{
             if(hours>0){
                 holder.logDate.setText(hours+"h");
             }else{
                 if(minutes>0){
                     holder.logDate.setText(minutes+"m");
                 }else{
                     if(seconds>0){
                         holder.logDate.setText(seconds+"s");
                     }else{
                         holder.logDate.setText("now");
                     }
                 }
             }
         }

        Spannable nameOfActor = new SpannableString(actorName);
        nameOfActor.setSpan(new ForegroundColorSpan(Color.BLACK), 0, nameOfActor.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        nameOfActor.setSpan(new StyleSpan(Typeface.BOLD),0,nameOfActor.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.logMessage.setText(nameOfActor);
        ClickableSpan cardClick = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

                logAdapterToMyshroomies.sendInput(cardID,cardType);
                ft = fm.beginTransaction();
                ft.replace(R.id.my_shroomies_container, targetedFragment);
                ft.commit();

            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }



        };
        if(cardTitle!=null){
            cardName = new SpannableString(cardTitle);
            cardName.setSpan(new ForegroundColorSpan(context.getColor(R.color.LogoYellow)), 0, cardTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            cardName.setSpan(new StyleSpan(Typeface.BOLD),0,cardTitle.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if(cardID!=null){
                holder.logMessage.setClickable(true);
                holder.logMessage.setMovementMethod(LinkMovementMethod.getInstance());
                cardName.setSpan(cardClick,0,cardTitle.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                cardName.setSpan(new ForegroundColorSpan(context.getColor(R.color.LogoYellow)), 0, cardTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                cardName.setSpan(new StyleSpan(Typeface.BOLD),0,cardTitle.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            }



        }



        switch (action){
          case "deletingCard":
              if(cardType.equals("tasks")){

                  holder.logMessage.append(" deleted ");
                  holder.logMessage.append(cardName);
                  holder.logMessage.append(" from tasks");
              }if(cardType.equals("expenses")){

              holder.logMessage.append(" deleted ");
              holder.logMessage.append(cardName);
              holder.logMessage.append(" from expenses");
                  }
              break;
          case "addingCard":
              if(cardType.equals("tasks")){
                  holder.logMessage.append(" added a new task as ");
                  holder.logMessage.append(cardName);


              }if(cardType.equals("expenses")){
              holder.logMessage.append(" added a new expenses as ");
              holder.logMessage.append(cardName);
                     }

              break;
          case "archivingCard":
              if(cardType.equals("tasks")){
                  holder.logMessage.append(" archived ");
                  holder.logMessage.append(cardName);
                  holder.logMessage.append(" from tasks");
              }if(cardType.equals("expenses")){
              holder.logMessage.setText(nameOfActor);
              holder.logMessage.append(" archived ");
              holder.logMessage.append(cardName);
              holder.logMessage.append(" from expenses");
                     }
              break;
          case "deletingArchivedCard":
                  if(cardType.equals("tasks")){
                      holder.logMessage.append(" deleted ");
                      holder.logMessage.append(cardName);
                      holder.logMessage.append(" from archived tasks");

                  }if(cardType.equals("expenses")){
              holder.logMessage.append(" deleted ");
              holder.logMessage.append(cardName);
              holder.logMessage.append(" from archived expenses");
              }
              break;
          case "markingDone":
              if(cardType.equals("tasks")){
                  holder.logMessage.append(" marked ");
                  holder.logMessage.append(cardName);
                  holder.logMessage.append(" as done in tasks");
              }if(cardType.equals("expenses")){
              holder.logMessage.append(" marked ");
              holder.logMessage.append(cardName);
              holder.logMessage.append(" as done in expenses");
                 }
              break;
          case "unMarkingDone":
              if(cardType.equals("tasks")){
                  holder.logMessage.append(" unmarked ");
                  holder.logMessage.append(cardName);
                  holder.logMessage.append(" as done in tasks");
              }if(cardType.equals("expenses")){
              holder.logMessage.append(" unmarked ");
              holder.logMessage.append(cardName);
              holder.logMessage.append(" as done in expenses");
                   }
              break;
          case "requesting":
              getRequestedUserName(receiverRequest,holder.logMessage);

              break;
          case "removing":
              holder.logMessage.append(" removed ");
              holder.logMessage.append(removedUser);
              break;
          case "left":
              holder.logMessage.append(" left ");
              break;
          case "joined":
              holder.logMessage.append(" is a new shroomie ");
              break;
          default:
              holder.logMessage.setText("");

      }
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public class LogViewHolder extends RecyclerView.ViewHolder {
        private ImageView userPic;
        private TextView logMessage;
        private TextView logDate;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            userPic=itemView.findViewById(R.id.log_actor_pic);
            logDate=itemView.findViewById(R.id.log_date);
            logMessage=itemView.findViewById(R.id.log_message);

        }
    }
    private void getRequestedUserName(String userID, final TextView message){
        rootRef.child("Users").child(userID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    requestedUserName=snapshot.getValue().toString();
                    SpannableString requestedUser=new SpannableString(requestedUserName);
                    requestedUser.setSpan(new ForegroundColorSpan(context.getColor(R.color.LogoYellow)),0,requestedUserName.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
                    requestedUser.setSpan(new StyleSpan(Typeface.BOLD),0,requestedUserName.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    message.append(" requested ");
                    message.append(requestedUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        try {
            logAdapterToMyshroomies = (LogAdapterToMyshroomies) targetedFragment;
        }catch(ClassCastException e){

        }
    }

}

interface LogAdapterToMyshroomies {
    void sendInput(String cardID,String cardType);

}


