package com.example.shroomies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.sql.BatchUpdateException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    private View v;
    private Context context;
    private ArrayList<Log> logList;
    private String requestedUserName="";
    private DatabaseReference rootRef;
    private  Spannable cardName=new SpannableString("");
    private FragmentTransaction ft;
    private FragmentManager fm;

    public LogAdapter(Context context, ArrayList<Log> logList,FragmentManager fm) {
        this.context = context;
        this.logList = logList;
        this.fm=fm;
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

      if(!logList.get(position).getActorPic().isEmpty()){
          GlideApp.with(context)
                  .load(logList.get(position).getActorPic())
                  .fitCenter()
                  .circleCrop()
                  .into(holder.userPic);
          holder.userPic.setPadding(2,2,2,2);
      }
      String actorName=logList.get(position).getActorName();
      String action=logList.get(position).getAction();
      String cardTitle=logList.get(position).getCardTitle();
      String removedUser=logList.get(position).getRemovedUser();
      String receiverRequest=logList.get(position).getReceivedBy();
      String when=logList.get(position).getWhen();
      final String cardType=logList.get(position).getCardType();
      final String cardID=logList.get(position).getCardID();
        Spannable nameOfActor = new SpannableString(actorName);
        nameOfActor.setSpan(new ForegroundColorSpan(Color.BLUE), 0, nameOfActor.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.logMessage.setText(nameOfActor);
        ClickableSpan cardClick = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

                MyShroomies shroomies=new MyShroomies();
                Bundle bundle=new Bundle();
                bundle.putString("CARDID_SELECTED",cardID);
                bundle.putString("SELECTED_CARD_TYPE",cardType);
                shroomies.setArguments(bundle);
                ft = fm.beginTransaction();
                ft.addToBackStack(null);
                ft.replace(R.id.fragmentContainer, shroomies);
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
            cardName.setSpan(new ForegroundColorSpan(Color.BLACK), 0, cardTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            cardName.setSpan(new StyleSpan(Typeface.BOLD),0,cardTitle.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if(cardID!=null){
                holder.logMessage.setClickable(true);
                holder.logMessage.setMovementMethod(LinkMovementMethod.getInstance());
                cardName.setSpan(new ForegroundColorSpan(Color.BLACK), 0, cardTitle.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                cardName.setSpan(new StyleSpan(Typeface.BOLD),0,cardTitle.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                cardName.setSpan(cardClick,0,cardTitle.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            }


        }



      SimpleDateFormat format = new SimpleDateFormat("dd-MMMM-yyyy HH:MM:ss aa");
        try {
            holder.eventDate=format.parse(when);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        switch (action){
          case "deletingCard":
              if(cardType.equals("tasks")){

                  holder.logMessage.append(" deleted");
                  holder.logMessage.append(cardName);
                  holder.logMessage.append(" from tasks");
              }if(cardType.equals("expenses")){

              holder.logMessage.append(" deleted");
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
          case "markDone":
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
          case "unMarkDone":
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
        private Date eventDate;
        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            userPic=itemView.findViewById(R.id.log_actor_pic);
            logDate=itemView.findViewById(R.id.log_date);
            logMessage=itemView.findViewById(R.id.log_message);
            Calendar calendarDate = Calendar.getInstance();
            SimpleDateFormat mcurrentDate = new SimpleDateFormat("dd-MMMM-yyyy HH:MM:ss aa");
            mcurrentDate.format(calendarDate.getTime());
            if(eventDate!=null){
//              long diff=mcurrentDate.getTimeZone()-eventDate.getTime();
            }
        }
    }
    private void getRequestedUserName(String userID, final TextView message){
        rootRef.child("Users").child(userID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    requestedUserName=snapshot.getValue().toString();
                    SpannableString requestedUser=new SpannableString(requestedUserName);
                    requestedUser.setSpan(new ForegroundColorSpan(Color.BLACK),0,requestedUserName.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
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
}
