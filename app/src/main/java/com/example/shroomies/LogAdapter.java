package com.example.shroomies;

import android.content.Context;
import android.graphics.Typeface;
import android.icu.lang.UScript;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {
    private View v;
    private Context context;
    private ArrayList<apartmentLogs> apartmentLogsList;
    private HashMap<String , User> usersMap;
    private Spannable cardName=new SpannableString("");
    private LogAdapterToMyshroomies logAdapterToMyshroomies;
    private Fragment targetedFragment;
    private FragmentTransaction ft;
    private FragmentManager fm;



    public LogAdapter(Context context, ArrayList<apartmentLogs> apartmentLogsList , HashMap<String , User> usersMap, FragmentManager fm, Fragment targetedFragment) {
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
        v = layoutInflater.inflate(R.layout.log_card,parent,false);
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

      final String cardType= apartmentLogsList.get(position).getCardType();
      final String cardID= apartmentLogsList.get(position).getCardID();
//      long when= apartmentLogsList.get(position).getWhen();
//          Date happend=(new Date(when));
//          Date currentDate=(new Date(System.currentTimeMillis()));
//          long diff=currentDate.getTime()-happend.getTime();
//          long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diff);
//        long seconds = diffInSec % 60;
//         diffInSec /= 60;
//        long minutes = diffInSec % 60;
//        diffInSec /= 60;
//        long hours = diffInSec % 24;
//        diffInSec /= 24;
//        long days = diffInSec;
//         if(days>0){
//             holder.logDate.setText(days+"d");
//         }else{
//             if(hours>0){
//                 holder.logDate.setText(hours+"h");
//             }else{
//                 if(minutes>0){
//                     holder.logDate.setText(minutes+"m");
//                 }else{
//                     if(seconds>0){
//                         holder.logDate.setText(seconds+"s");
//                     }else{
//                         holder.logDate.setText("now");
//                     }
//                 }
//             }
//         }

            Spannable nameOfActor = new SpannableString(user.getName());
            nameOfActor.setSpan(new ForegroundColorSpan(context.getColor(R.color.Black)), 0, nameOfActor.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
                  holder.logMessage.append(" added a new task titled ");
                  holder.logMessage.append(cardName);


              }if(cardType.equals("expenses")){
              holder.logMessage.append(" added a new expense titled ");
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
        return apartmentLogsList.size();
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


