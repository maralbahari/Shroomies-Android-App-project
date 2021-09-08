package com.example.shroomies;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class ExpensesCardAdapter extends RecyclerView.Adapter<ExpensesCardAdapter.ExpensesViewHolder> implements ItemTouchHelperAdapter {
    private final Context context;
    private final View parentView;
    private FirebaseAuth mAuth;
    private final Boolean fromArchive;
    private final String apartmentID;
    private static final String DELETE = "delete", ARCHIVE = "archive", MARK = "mark";
    private final FragmentManager fragmentManager;
    private RequestQueue requestQueue;
    private final ArrayList<ExpensesCard> expensesCardArrayList;
    private final HashMap<String, User> memberHashMap;

    public ExpensesCardAdapter(ArrayList<ExpensesCard> expensesCardArrayList, Context context, Boolean fromArchive,String apartmentID,FragmentManager fragmentManager,View parentView , HashMap<String, User> memberHashMap) {
        this.expensesCardArrayList = expensesCardArrayList;
        this.context=context;
        this.fromArchive = fromArchive;
        this.apartmentID=apartmentID;
        this.fragmentManager=fragmentManager;
        this.parentView=parentView;
        this.memberHashMap = memberHashMap;
    }


    @Override
    public void onItemSwiped(int position) {
        try{
            ExpensesCard expensesCard = this.expensesCardArrayList.get(position);
            getUserToken(DELETE, position , expensesCard , false , null);
        }catch (IndexOutOfBoundsException e){
            notifyDataSetChanged();
            Log.d("itemSwiped" ,  e.toString());
        }

    }

    @NonNull
    @Override
    public ExpensesCardAdapter.ExpensesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.my_shroomie_expenses_card, parent, false);
        requestQueue = Volley.newRequestQueue(context);
        mAuth = FirebaseAuth.getInstance();

        return new ExpensesViewHolder(view);

    }
    @Override
    public void onBindViewHolder(@NonNull final ExpensesCardAdapter.ExpensesViewHolder holder, final int position) {
        holder.title.setText(expensesCardArrayList.get(position).getTitle());
        holder.description.setText(expensesCardArrayList.get(position).getDescription());
        if(expensesCardArrayList.get(position).getDueDate()==null){
            holder.dueDate.setText(R.string.no_due_date);
        }else {
            String dueString=expensesCardArrayList.get(position).getDueDate();
            //get the  dateformat with UTC
            setDate( dueString, holder.dueDate);
        }
        boolean cardStatus = expensesCardArrayList.get(position).getDone();
        if (cardStatus){
            holder.done.setText(R.string.done);
        }else{
            holder.done.setText(R.string.mark_card_as_done);
        }
        holder.done.setChecked(cardStatus);
        if(expensesCardArrayList.get(position).getMention()!=null) {
            //check the ids that have been mentioned
            //and get the respective name from the user hashmap
            for(Map.Entry<String,String> entry
                    :expensesCardArrayList.get(position).getMention().entrySet()){
                if(memberHashMap.get(entry.getKey())!=null){
                    //create a new Chip for each mentioned user
                    String name = memberHashMap.get(entry.getKey()).getUsername();
                    Chip chip = new Chip(context);
                    chip.setText("@"+name);
                    chip.setTextColor(context.getColor(R.color.mentionBlue));
                    holder.mentionChipGroup.addView(chip);
                }
            }
        }

        if (fromArchive){
            holder.done.setVisibility(View.GONE);
        }

        String importanceViewColor = expensesCardArrayList.get(position).getImportance();
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

        if (expensesCardArrayList.get(position).getAttachedFile()!=null) {
            if(!expensesCardArrayList.get(position).getAttachedFile().isEmpty()) {
                if(expensesCardArrayList.get(position).getFileType().equals("pdf")){
                    holder.cardImage.setImageDrawable(ContextCompat.getDrawable(context , R.drawable.ic_pdf_icon));
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
                holder.cardImage.setImageDrawable(ContextCompat.getDrawable(context , R.drawable.ic_no_file_added));
                holder.cardImage.setPadding(40,60,40,60);
                holder.noFileAttached.setVisibility(View.VISIBLE);
            }


        }else{
            holder.cardImage.setImageDrawable(ContextCompat.getDrawable(context , R.drawable.ic_no_file_added));
            holder.cardImage.setPadding(40,60,40,60);
            holder.noFileAttached.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewRecycled(@NonNull ExpensesViewHolder holder) {
        super.onViewRecycled(holder);
        holder.mentionChipGroup.removeAllViews();
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

        final long days = ChronoUnit.DAYS.between(secondDate.atStartOfDay(), firstDate.atStartOfDay());

        if(days<0){
          dueDateTextView.setTextColor(context.getColor(R.color.red));
          dueDateTextView.setText(context.getString(R.string.due));
        }else  if(days==0){
          dueDateTextView.setTextColor(context.getColor(R.color.red));
          dueDateTextView.setText(context.getString(R.string.due_today));
        }
        else if(days==1){
           dueDateTextView.setTextColor(context.getColor(R.color.red));
           dueDateTextView.setText((Long.toString(days) +" "+context.getString(R.string.day_left)));
        }else if(days<3){
            dueDateTextView.setTextColor(context.getColor(R.color.red));
            dueDateTextView.setText((Long.toString(days) +" "+context.getString(R.string.days_left)));
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
        private final View importanceView;
        private final TextView title,description,  dueDate,  noFileAttached;
        private final ChipGroup mentionChipGroup;
        private final ImageView cardImage;
//        private final MaterialButton archive;
        private final MaterialCheckBox done;

        public ExpensesViewHolder(@NonNull View v) {
            super(v);
            importanceView = v.findViewById(R.id.importance_view);
            title = v.findViewById(R.id.title_card);
            description = v.findViewById(R.id.card_description);
            dueDate = v.findViewById(R.id.dueDate_card);
            cardImage= v.findViewById(R.id.card_img);
            mentionChipGroup = v.findViewById(R.id.expense_mention_chip_group);
            done = v.findViewById(R.id.expense_done);
            noFileAttached = v.findViewById(R.id.no_file_attached);
            ImageButton optionsMenuButton = v.findViewById(R.id.card_menu_image_button);


            CardView expensesCardView = v.findViewById(R.id.my_shroomie_expenses_card);


            done.setOnClickListener(view -> {
                ExpensesCard selectedCard=expensesCardArrayList.get(getLayoutPosition());
                selectedCard.setDone(done.isChecked());
                getUserToken(MARK ,getAdapterPosition(),selectedCard, done.isChecked() , ExpensesViewHolder.this);
            });

            optionsMenuButton.setOnClickListener(v1 -> showPopup(v1, getAdapterPosition(), expensesCardArrayList.get(getAdapterPosition())));


            expensesCardView.setOnClickListener(view -> {
                ViewCards viewCard=new ViewCards();
                Bundle bundle=new Bundle();
                bundle.putSerializable(Config.members , memberHashMap);
                bundle.putParcelable("CARD_DETAILS",expensesCardArrayList.get(getAdapterPosition()));
                bundle.putBoolean("FROM_TASK_TAB",false);
                viewCard.setArguments(bundle);
                viewCard.show(fragmentManager,"VIEWCARD");
            });


        }


    }
    public void showPopup(View v , int position , ExpensesCard expensesCard) {
        PopupMenu popup = new PopupMenu(context, v);
        MenuInflater inflater = popup.getMenuInflater();
        if(!fromArchive){
            inflater.inflate(R.menu.card_options, popup.getMenu());

        }else{
            inflater.inflate(R.menu.archive_card_options, popup.getMenu());

        }
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.delete_option:
                    getUserToken(DELETE, position , expensesCard , false , null);
                    return true;
                case R.id.archive_option:
                    getUserToken(ARCHIVE ,position ,expensesCard,false,null);
                    return true;
                default:
                    return false;
            }
        });
        popup.show();
    }

     private void markExpenseCard( ExpensesCard expensesCard , boolean checked ,String token , ExpensesViewHolder expensesViewHolder) {
         final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
         mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

         JSONObject jsonObject = new JSONObject();
         JSONObject data = new JSONObject();
         JSONObject cardDetails = new JSONObject(mapper.convertValue(expensesCard, new TypeReference<Map<String, Object>>() {}));

         try {
             jsonObject.put(Config.cardDetails, cardDetails);
             jsonObject.put(Config.apartmentID, apartmentID);
             data.put(Config.data, jsonObject);
         } catch (JSONException e) {
             e.printStackTrace();
         }

         JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_MARK_EXPENSES_CARD, data, response -> {
             try {
                 boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                 if(success){
                     String done = context.getString(R.string.done);
                     if(!checked){
                         done = context.getString(R.string.mark_card_as_done);
                     }
                     Snackbar.make(parentView,context.getString(R.string.card_marked_as_done)+done, BaseTransientBottomBar.LENGTH_SHORT)
                             .show();
                     expensesViewHolder.done.setText(done);
                 }else{
                     expensesViewHolder.done.setChecked(!checked);
                     Snackbar.make(parentView, context.getString(R.string.marking_error),  BaseTransientBottomBar.LENGTH_SHORT)
                             .show();
                 }
             } catch (JSONException e) {
                 e.printStackTrace();
             }



         }, this::displayError)
         {
             @Override
             public Map<String, String> getHeaders() {
                 Map<String, String> params = new HashMap<>();
                 params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                 params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
                 return params;
             }
         };
                     requestQueue.add(jsonObjectRequest);

    }

    public void archive(final int position,final ExpensesCard expensesCard ,  String token){
        final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        JSONObject cardDetails = new JSONObject(mapper.convertValue(expensesCard , new TypeReference<Map<String, Object>>() {}));
        JSONObject data = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Config.apartmentID , apartmentID);
            jsonObject.put(Config.cardDetails, cardDetails);
            data.put(Config.data , jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_ARCHIVE_EXPENSES_CARD, data, response -> {
            try {
                boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                if(success){
                    Snackbar.make(parentView,context.getString(R.string.card_archived), BaseTransientBottomBar.LENGTH_SHORT)
                            .show();
                }else{
                    Snackbar.make(parentView,context.getString(R.string.archving_error), BaseTransientBottomBar.LENGTH_SHORT)
                            .show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, this::displayError)
        {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                params.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                return params;
        }
    };
        requestQueue.add(jsonObjectRequest);



    }
    void getUserToken(String method , int position ,  ExpensesCard expensesCard , boolean checked , ExpensesViewHolder expensesViewHolder){
        if(method.equals(DELETE)||method.equals(ARCHIVE)){
            expensesCardArrayList.remove(position);
            notifyItemRemoved(position);
        }
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult().getToken();
                switch (method){
                    case DELETE:
                        deleteExpensesCard(position , expensesCard , token);
                        break;
                    case MARK:
                        markExpenseCard(expensesCard ,checked ,  token , expensesViewHolder);
                        break;
                    case ARCHIVE:
                        archive(position , expensesCard , token);
                }
            }else{
                Snackbar.make(parentView,context.getString(R.string.authentication_error), BaseTransientBottomBar.LENGTH_LONG);
            }
        });

    }

    public void deleteExpensesCard(final int position ,  ExpensesCard expensesCard, String token) {

        final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject cardDetails = new JSONObject(mapper.convertValue(expensesCard, new TypeReference<Map<String, Object>>() {}));

        try {
            jsonObject.put(Config.cardDetails, cardDetails);
            jsonObject.put(Config.apartmentID, apartmentID);
            data.put(Config.data, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = fromArchive ? Config.URL_DELETE_EXPENSE_CARD_ARCHIVE :Config.URL_DELETE_EXPENSE_CARD;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url,  data, response -> {
            try {
                boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                if(success){
                    Snackbar.make(parentView, context.getString(R.string.card_deleted), BaseTransientBottomBar.LENGTH_SHORT).show();

                }else{
                    Snackbar snack = Snackbar.make(parentView, context.getString(R.string.delete_card_error), BaseTransientBottomBar.LENGTH_SHORT);
                    snack.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, this::displayError)
            {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                    params.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                    return params;
                }
            };
        requestQueue.add(jsonObjectRequest);

    }
    void displayError(VolleyError error){
        String message; // error message, show it in toast or dialog, whatever you want
        if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof TimeoutError) {
            message = context.getString(R.string.no_internet);
        } else if (error instanceof ServerError) {
            message = context.getString(R.string.server_error);
        }  else if (error instanceof ParseError) {
            message = context.getString(R.string.parsing_error);
        }else{
            message = context.getString(R.string.unexpected_error);
        }
        Snackbar.make(parentView,message, BaseTransientBottomBar.LENGTH_SHORT)
                .show();
    }

}

