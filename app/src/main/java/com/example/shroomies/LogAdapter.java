package com.example.shroomies;

import android.content.Context;
import android.graphics.Typeface;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
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

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;


import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    private final Context context;
    private final ArrayList<ApartmentLogs> apartmentLogsList;
    private final HashMap<String , User> usersMap;
    private Spannable cardName=new SpannableString("");
    private LogAdapterToMyshroomies logAdapterToMyshroomies;
    private final Fragment targetedFragment;
    private FragmentTransaction ft;
    private final FragmentManager fm;



    public LogAdapter(Context context, ArrayList<ApartmentLogs> apartmentLogsList , HashMap<String , User> usersMap, FragmentManager fm, Fragment targetedFragment) {
        this.context = context;
        this.apartmentLogsList = apartmentLogsList;
        this.targetedFragment = targetedFragment;
        this.usersMap = usersMap;
        this.fm = fm;


    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View v = layoutInflater.inflate(R.layout.log_card, parent, false);
        return new LogViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        String actorID = apartmentLogsList.get(position).getActor();
        //get the user's details
        User user = usersMap.get(actorID);

        if(user !=null){
            if(user.getImage()!=null){
                if(!user.getImage().isEmpty()) {
                    GlideApp.with(context)
                            .load(user.getImage())
                            .fitCenter()
                            .circleCrop()
                            .error(R.drawable.ic_user_profile_svgrepo_com)
                            .transition(DrawableTransitionOptions.withCrossFade()) //Here a fading animation
                            .into(holder.userPic);
                }
            }

        }
      String action= apartmentLogsList.get(position).getAction();
      String cardTitle= apartmentLogsList.get(position).getCardTitle();
      String removedUser= apartmentLogsList.get(position).getRemovedUser();
      String cardType= apartmentLogsList.get(position).getCardType();
      String cardID= apartmentLogsList.get(position).getCardID();

        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(ZoneOffset.UTC);
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        String currentDateString = formatter.format(now);

        final LocalDateTime secondDate = LocalDateTime.parse(apartmentLogsList.get(position).getWhen(), formatter);
        final LocalDateTime firstDate = LocalDateTime.parse(currentDateString  , formatter);


        final long days = ChronoUnit.DAYS.between(secondDate, firstDate);
        if(days>0){
            holder.logDate.setText(days+ "d");
        }else{
            long hours = ChronoUnit.HOURS.between(secondDate, firstDate);
            Log.d("time" , Long.toString(hours));

            if(hours>0){
                holder.logDate.setText(hours+ "h");
            }else{
                long minutes = ChronoUnit.MINUTES.between(secondDate, firstDate);
                if(minutes>0){
                    holder.logDate.setText(minutes+ "m");
                }else{
                    long seconds = ChronoUnit.SECONDS.between(secondDate, firstDate);
                    holder.logDate.setText(seconds+ "s");
                }
            }
        }

            if(user!=null){
                Spannable nameOfActor = new SpannableString(user.getName());
                nameOfActor.setSpan(new ForegroundColorSpan(context.getColor(R.color.Black)), 0, nameOfActor.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                nameOfActor.setSpan(new StyleSpan(Typeface.BOLD),0,nameOfActor.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.logMessage.setText(nameOfActor);
            }

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
          case Config.deletingCard:
              if(cardType.equals(Config.task)){
                  holder.logMessage.append(" deleted ");
                  holder.logMessage.append(cardName);
                  holder.logMessage.append(" from tasks");

              }if(cardType.equals(Config.expenses)){

              holder.logMessage.append(" deleted ");
              holder.logMessage.append(cardName);
              holder.logMessage.append(" from expenses");
                  }
              break;
          case Config.addingCard:
              if(cardType.equals(Config.task)){
                  holder.logMessage.append(" added a new task titled ");
                  holder.logMessage.append(cardName);


              }if(cardType.equals(Config.expenses)){
              holder.logMessage.append(" added a new expense titled ");
              holder.logMessage.append(cardName);
                     }

              break;
          case Config.archivingCard:
              if(cardType.equals(Config.task)){
                  holder.logMessage.append(" archived ");
                  holder.logMessage.append(cardName);
                  holder.logMessage.append(" from tasks");
              }if(cardType.equals(Config.expenses)){
              holder.logMessage.append(" archived ");
              holder.logMessage.append(cardName);
              holder.logMessage.append(" from expenses");
                     }
              break;
          case Config.deletingArchivedCard:
                  if(cardType.equals(Config.task)){
                      holder.logMessage.append(" deleted ");
                      holder.logMessage.append(cardName);
                      holder.logMessage.append(" from archived tasks");

                  }if(cardType.equals(Config.expenses)){
              holder.logMessage.append(" deleted ");
              holder.logMessage.append(cardName);
              holder.logMessage.append(" from archived expenses");
              }
              break;
          case Config.markingCard:
              if(cardType.equals(Config.task)){
                  holder.logMessage.append(" marked ");
                  holder.logMessage.append(cardName);
                  holder.logMessage.append(" as done in tasks");
              }if(cardType.equals(Config.expenses)){
              holder.logMessage.append(" marked ");
              holder.logMessage.append(cardName);
              holder.logMessage.append(" as done in expenses");
                 }
              break;
          case Config.unMarkingCard:
              if(cardType.equals(Config.task)){
                  holder.logMessage.append(" unmarked ");
                  holder.logMessage.append(cardName);
                  holder.logMessage.append(" in tasks");
              }if(cardType.equals(Config.expenses)){
              holder.logMessage.append(" unmarked ");
              holder.logMessage.append(cardName);
              holder.logMessage.append(" in expenses");
                   }
              break;
          case Config.removed:
              holder.logMessage.setText(removedUser);
              holder.logMessage.append(" removed ");
              //the actor id will be the name only in case of removing and leaving
              holder.logMessage.append(actorID);
              break;
          case Config.left:
              holder.logMessage.setText(actorID);
              holder.logMessage.append(" left");
              break;
          case Config.joined:
              holder.logMessage.append(" is a new shroomie ");
              break;
          default:
              holder.logMessage.setText("");
      }

    }


    @Override
    public int getItemCount() {
        return apartmentLogsList.size();
    }

    public static class LogViewHolder extends RecyclerView.ViewHolder {
        private final ImageView userPic;
        private final TextView logMessage;
        private final TextView logDate;

        public LogViewHolder(@NonNull View itemView) {
            super(itemView);
            userPic=itemView.findViewById(R.id.log_actor_pic);
            logDate=itemView.findViewById(R.id.log_date);
            logMessage=itemView.findViewById(R.id.log_message);

        }
    }
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        try {
            logAdapterToMyshroomies = (LogAdapterToMyshroomies) targetedFragment;
        }catch(ClassCastException e){
            e.printStackTrace();
        }
    }

}

interface LogAdapterToMyshroomies {
    void sendInput(String cardID,String cardType);

}


