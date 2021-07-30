package com.example.shroomies;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.shroomies.notifications.Data;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.virgilsecurity.crypto.foundation.Hash;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ExpensesCardAdapter extends RecyclerView.Adapter<ExpensesCardAdapter.ExpensesViewHolder> implements ItemTouchHelperAdapter {
    private Context context;
    private View view;
    private ItemTouchHelper itemTouchHelper;
    private View parentView;

    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    private Boolean fromArchive;
    private String apartmentID;
    private static final String DELETE = "delete";
    private static final String ARCHIVE = "archive";
    private static final String MARK = "mark";

    private FragmentManager fragmentManager;
    private RequestQueue requestQueue;
    private ArrayList<ExpensesCard> expensesCardArrayList;

    public ExpensesCardAdapter(ArrayList<ExpensesCard> expensesCardArrayList, Context context, Boolean fromArchive,String apartmentID,FragmentManager fragmentManager,View parentView) {
        this.expensesCardArrayList = expensesCardArrayList;
        this.context=context;
        this.fromArchive = fromArchive;
        this.apartmentID=apartmentID;
        this.fragmentManager=fragmentManager;
        this.parentView=parentView;
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper){
        this.itemTouchHelper = itemTouchHelper;
    }


    @Override
    public void onItemSwiped(int position) {
        getUserToken(DELETE, position , null , false);
    }

    @NonNull
    @Override
    public ExpensesCardAdapter.ExpensesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view  = layoutInflater.inflate(R.layout.my_shroomie_expenses_card,parent,false);
        requestQueue = Volley.newRequestQueue(context);
        mAuth = FirebaseAuth.getInstance();
        mAuth.useEmulator("10.0.2.2",9099);

        return new ExpensesViewHolder(view);

    }
    @Override
    public void onBindViewHolder(@NonNull final ExpensesCardAdapter.ExpensesViewHolder holder, final int position) {
        holder.title.setText(expensesCardArrayList.get(position).getTitle());
        holder.description.setText(expensesCardArrayList.get(position).getDescription());
        if(expensesCardArrayList.get(position).getDueDate()==null){
            holder.dueDate.setText("None");
        }else {
            String dueString=expensesCardArrayList.get(position).getDueDate();
            //get the  dateformat with UTC
            setDate( dueString, holder.dueDate);
        }
        String importanceViewColor = expensesCardArrayList.get(position).getImportance();
        if(expensesCardArrayList.get(position).getMention()!=null) {
            holder.mention.setVisibility(View.VISIBLE);
//            holder.mention.setText(expensesCardArrayList.get(position).getMention());
        }
        boolean cardStatus = expensesCardArrayList.get(position).getDone().equals("true");
        if (cardStatus){
            holder.done.setChecked(true);
            holder.done.setText("Done!");
        }else{
            holder.done.setChecked(false);
            holder.done.setText("Mark as done");
        }

        if (fromArchive){
            holder.archive.setVisibility(view.GONE);
            holder.done.setVisibility(View.GONE);
        }

        switch (importanceViewColor) {
            case  "2":
                holder.importanceView.setBackgroundColor(context.getColor(R.color.canceRed));
                break;
            case  "1":
                holder.importanceView.setBackgroundColor(context.getColor(R.color.orange));
                break;
            default:
                holder.importanceView.setBackgroundColor(context.getColor(R.color.okGreen));
        }

        if (!expensesCardArrayList.get(position).getAttachedFile().isEmpty()) {
            if(expensesCardArrayList.get(position).getFileType().equals("pdf")){
                holder.cardImage.setImageDrawable(context.getDrawable(R.drawable.ic_pdf_icon));
            }else{
                GlideApp.with(context)
                        .load(expensesCardArrayList.get(position).getAttachedFile())
                        .transform( new CenterCrop() , new RoundedCorners(10))
                        .transition(DrawableTransitionOptions.withCrossFade()) //Here a fading animation
                        .into(holder.cardImage);
                holder.cardImage.setPadding(0,0,0,0);
            }
            holder.noFileAttached.setVisibility(View.GONE);

        }else{
            holder.cardImage.setImageDrawable(context.getDrawable(R.drawable.ic_no_file_added));
            holder.cardImage.setPadding(40,60,40,60);
            holder.noFileAttached.setVisibility(View.VISIBLE);

        }
    }

    private void setDate(String dueString, TextView dueDateTextView) {
        DateTimeFormatter dateformat =  DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss Z")
                .withZone(TimeZone.getDefault().toZoneId());
        ZonedDateTime now = ZonedDateTime.now();

        String dueDateFromatted  =  DateTimeFormatter.ofPattern("EEE, MMM d")
                .withZone(TimeZone.getDefault().toZoneId()).format(dateformat.parse(dueString));

        //check if the amount of days left  for  the due  date  is less than 1 day
        //and  set the text to red
        String currentDateString = dateformat.format(now);
        final LocalDate firstDate = LocalDate.parse(dueString, dateformat);
        final LocalDate secondDate = LocalDate.parse(currentDateString, dateformat);
        Log.d("current time" , secondDate.getDayOfWeek().toString());
        Log.d("due date" ,  firstDate.getDayOfWeek().toString());
        final long days = ChronoUnit.DAYS.between(secondDate.atStartOfDay(), firstDate.atStartOfDay());

        if(days<0){
          dueDateTextView.setTextColor(context.getColor(R.color.red));
          dueDateTextView.setText("Due");
        }else  if(days==0){
          dueDateTextView.setTextColor(context.getColor(R.color.red));
          dueDateTextView.setText("Due today");
        }
        else if(days==1){
           dueDateTextView.setTextColor(context.getColor(R.color.red));
           dueDateTextView.setText(days +" day left");
        }else if(days<3){
            dueDateTextView.setTextColor(context.getColor(R.color.red));
            dueDateTextView.setText(days +" day left");
        }else{
          dueDateTextView.setText(dueDateFromatted);
        }
    }

    @Override
    public int getItemCount() {
        return expensesCardArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(expensesCardArrayList.get(position).getCardID());
    }

    public class ExpensesViewHolder extends RecyclerView.ViewHolder {
        View importanceView;
        TextView title,description,dueDate , noFileAttached;
        private SocialAutoCompleteTextView mention;
        ImageView cardImage,shroomieArch;
        ImageButton archive;
        Button yesBtn,noBtn , delete;
        CheckBox done;
        private CardView expensesCardView;

        public ExpensesViewHolder(@NonNull View v) {
            super(v);
            importanceView = v.findViewById(R.id.importance_view);
            title = v.findViewById(R.id.title_card);
            description = v.findViewById(R.id.card_description);
            dueDate = v.findViewById(R.id.dueDate_card);
            cardImage= v.findViewById(R.id.card_img);
            archive = v.findViewById(R.id.archive_card_btn);
            delete = v.findViewById(R.id.delete_button);
            mention = v.findViewById(R.id.task_mention_et);
            mention.setMentionColor(Color.BLUE);
            done = v.findViewById(R.id.task_done);
            noFileAttached = v.findViewById(R.id.no_file_attached);

            expensesCardView=v.findViewById(R.id.my_shroomie_expenses_card);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ExpensesCard selectedCard=expensesCardArrayList.get(getLayoutPosition());
//                    markExpenseCard(selectedCard.getCardID() ,apartmentID, done.isChecked());
                    getUserToken(MARK ,0,selectedCard, done.isChecked());
                }
            });

            archive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getUserToken(ARCHIVE ,0,expensesCardArrayList.get(getAdapterPosition()),false );
                }
            });

            expensesCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ViewCards viewCard=new ViewCards();
                    Bundle bundle=new Bundle();
                    bundle.putParcelable("CARD_DETAILS",expensesCardArrayList.get(getAdapterPosition()));
                    bundle.putBoolean("FROM_TASK_TAB",false);
                    viewCard.setArguments(bundle);
                    viewCard.show(fragmentManager,"VIEWCARD");
                }
            });
        }


    }

     private void markExpenseCard(String cardID, String apartmentID, boolean checked ,String token) {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
         try {
             jsonObject.put("apartmentID" , apartmentID);
             jsonObject.put("cardID" ,cardID);
             jsonObject.put("booleanValue" , checked);
             data.put("data" , jsonObject);
         } catch (JSONException e) {
             e.printStackTrace();
         }

         JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.FUNCTION_MARK_EXPENSES_CARD, data, new Response.Listener<JSONObject>() {
             @Override
             public void onResponse(JSONObject response) {
                 String done = " done";
                 if(!checked){ done = " not done"; }
                 Snackbar.make(parentView,"Card marked as"+done, BaseTransientBottomBar.LENGTH_SHORT)
                         .show();

             }
         }, new Response.ErrorListener() {
             @Override
             public void onErrorResponse(VolleyError error) {
                 displayError(error);
             }
         })
//                     {
//                         @Override
//                         public Map<String, String> getHeaders() throws AuthFailureError {
//                             Map<String, String> params = new HashMap<String, String>();
//                             params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
//                             params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
//                             return params;
//                         }
//                     }
         ;
                     requestQueue.add(jsonObjectRequest);

    }

    public void archive(final int position,final ExpensesCard expensesCard ,  String token){
        final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        JSONObject cardDetails = new JSONObject(mapper.convertValue(expensesCard , new TypeReference<Map<String, Object>>() {}));
        JSONObject data = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("apartmentID" , apartmentID);
            jsonObject.put("cardDetails" , cardDetails);
            data.put("data" , jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.FUNCTION_ARCHIVE_EXPENSES_CARD, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Snackbar.make(parentView,"Card archived", BaseTransientBottomBar.LENGTH_SHORT)
                        .show();
                expensesCardArrayList.remove(position);
                notifyItemRemoved(position);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayError(error);
            }
        })
//        {
//
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
//                params.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);
//                return params;
//
//        }
//    }
;
        requestQueue.add(jsonObjectRequest);



    }
    void getUserToken(String method , int position , @Nullable ExpensesCard expensesCard , @Nullable boolean checked){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    switch (method){
                        case DELETE:
                            deleteExpensesCard(position , token);
                            break;
                        case MARK:
                            markExpenseCard(expensesCard.getCardID(),apartmentID ,checked ,  token);
                            break;
                        case ARCHIVE:
                            archive(position , expensesCard , token);
                    }
                }else{
                    Snackbar.make(parentView,"We encountered an error while authenticating your account", BaseTransientBottomBar.LENGTH_LONG);
                }
            }
        });

    }

    public void deleteExpensesCard(final int position , String token) {

        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();

            try {
                jsonObject.put("apartmentID", apartmentID);
                jsonObject.put("cardID", expensesCardArrayList.get(position).getCardID());
                jsonObject.put("attachedFile", expensesCardArrayList.get(position).getAttachedFile());
                data.put("data", jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        String url  =  fromArchive ? Config.FUNCTION_DELETE_EXPENSE_CARD_ARCHIVE:Config.FUNCTION_DELETE_EXPENSE_CARD;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                expensesCardArrayList.remove(position);
                notifyItemRemoved(position);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayError(error);
            }
        })
//                    {
//                        @Override
//                        public Map<String, String> getHeaders() throws AuthFailureError {
//                            Map<String, String> params = new HashMap<String, String>();
//                            params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
//                            params.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);
//                            return params;
//                        }
//                    }
                ;
        requestQueue.add(jsonObjectRequest);

    }
    void displayError(VolleyError error){
        String message = null; // error message, show it in toast or dialog, whatever you want
        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
            message = "Cannot connect to Internet";
        } else if (error instanceof ServerError) {
            message = "Server error. Please try again later";
        }  else if (error instanceof ParseError) {
            message = "Parsing error! Please try again later";
        }
        Snackbar.make(parentView,message, BaseTransientBottomBar.LENGTH_SHORT)
                .show();
    }

}

