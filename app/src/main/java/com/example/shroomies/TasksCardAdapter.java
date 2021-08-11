package com.example.shroomies;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
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


public class TasksCardAdapter extends RecyclerView.Adapter<TasksCardAdapter.TasksCardViewHolder> implements ItemTouchHelperAdapter {
    private final View parentView;
    private final Context context;

    private final ArrayList<TasksCard> tasksCardsList;
    private final FragmentManager fragmentManager;

    private final RequestQueue requestQueue;
    private final FirebaseAuth mAuth;
    private final Boolean fromArchive;
    private static final String ARCHIVE= "archive", MARK = "mark",  DELETE = "delete";
    private final String apartmentID;
    private final HashMap<String, User> memberHashMap;



    public TasksCardAdapter(ArrayList<TasksCard> tasksCardsList, Context context, boolean fromArchive, String apartmentID, FragmentManager fragmentManager, View parentView , HashMap<String,  User> memberHashMap) {
        this.tasksCardsList = tasksCardsList;
        this.context = context;
        this.fromArchive = fromArchive;
        this.apartmentID = apartmentID;
        this.fragmentManager = fragmentManager;
        this.parentView = parentView;
        this.memberHashMap = memberHashMap;
        mAuth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(context);
    }


    @Override
    public void onItemSwiped(int position) {
        TasksCard tasksCard =  tasksCardsList.get(position);
        getUserToken(DELETE ,position , tasksCard ,false , null );

    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
    }

    public class TasksCardViewHolder extends RecyclerView.ViewHolder {

        private final View taskImportanceView;
        private final TextView titleTextView, descriptionTextView, dueDateTextView;
        private final ChipGroup mentionChipGroup;
        private final MaterialCheckBox done;
        private final HorizontalScrollView horizontalScrollView;

        public TasksCardViewHolder(@NonNull View v) {
            super(v);
            taskImportanceView = v.findViewById(R.id.task_importance_view);
            titleTextView = v.findViewById(R.id.title_card);
            descriptionTextView = v.findViewById(R.id.card_description);
            dueDateTextView = v.findViewById(R.id.dueDate_card);
            ImageButton cardOptions = v.findViewById(R.id.task_card_menu_image_button);
            done = v.findViewById(R.id.task_done);
            mentionChipGroup = v.findViewById(R.id.task_mention_chip_group);
            horizontalScrollView = v.findViewById(R.id.task_mention_et);


            CardView taskCardView = v.findViewById(R.id.task_card_view);
            taskCardView.setOnClickListener(view -> {
                ViewCards viewCard = new ViewCards();
                Bundle bundle = new Bundle();
                bundle.putSerializable(Config.members , memberHashMap);
                bundle.putParcelable("CARD_DETAILS", tasksCardsList.get(getAdapterPosition()));
                bundle.putBoolean("FROM_TASK_TAB", true);
                viewCard.setArguments(bundle);
                viewCard.show(fragmentManager, "VIEWCARD");
            });

            done.setOnClickListener(view -> {
                TasksCard tasksCard = tasksCardsList.get(getAdapterPosition());
                boolean checked = done.isChecked();
                tasksCard.setDone(checked);
                getUserToken(MARK , getAdapterPosition() , tasksCard , checked , TasksCardViewHolder.this);
            });

            cardOptions.setOnClickListener(view -> showPopup(v , getAdapterPosition(), tasksCardsList.get(getAdapterPosition())));


        }


    }

    public void showPopup(View v , int position , TasksCard tasksCard) {
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
                    getUserToken(DELETE, position , tasksCard , false , null);
                    return true;
                case R.id.archive_option:
                    getUserToken(ARCHIVE ,position ,tasksCard,false,null);
                    return true;
                default:
                    return false;
            }
        });
        popup.show();
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
            dueDateTextView.setText(R.string.due);
        }else  if(days==0){
            dueDateTextView.setTextColor(context.getColor(R.color.red));
            dueDateTextView.setText(R.string.due_today);
        }
        else if(days==1){
            dueDateTextView.setTextColor(context.getColor(R.color.red));
            dueDateTextView.setText((Long.toString(days) +R.string.day_left));
        }else if(days<3){
            dueDateTextView.setTextColor(context.getColor(R.color.red));

            dueDateTextView.setText((Long.toString(days) + R.string.days_left));
        }else{
            dueDateTextView.setText(dueDateFromatted);
        }
    }


    @NonNull
    @Override
    public TasksCardAdapter.TasksCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.my_shroomie_tasks_card, parent, false);
        return new TasksCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TasksCardAdapter.TasksCardViewHolder holder, final int position) {

        holder.titleTextView.setText(tasksCardsList.get(position).getTitle());
        holder.descriptionTextView.setText(tasksCardsList.get(position).getDescription());
        if (tasksCardsList.get(position).getDueDate()==null) {
            holder.dueDateTextView.setText(R.string.no_due_date);
        } else {
            setDate(tasksCardsList.get(position).getDueDate() , holder.dueDateTextView);
        }
        if (tasksCardsList.get(position).getMention()!=null) {
            holder.horizontalScrollView.setVisibility(View.VISIBLE);
        }
        if(tasksCardsList.get(position).getMention()!=null){
            for(Map.Entry<String,String> entry
                    :tasksCardsList.get(position).getMention().entrySet()){
                if(memberHashMap.get(entry.getKey())!=null){
                    //create a new Chip for each mentioned user
                    String name = memberHashMap.get(entry.getKey()).getName();
                    Chip chip = new Chip(context);
                    chip.setText("@"+name);
                    chip.setTextColor(context.getColor(R.color.mentionBlue));
                    holder.mentionChipGroup.addView(chip);
                }
            }
        }

        String importanceView = tasksCardsList.get(position).getImportance();
        boolean cardStatus = tasksCardsList.get(position).getDone();
        if (cardStatus) {
            holder.done.setText(R.string.done);
        } else {
            holder.done.setText(R.string.mark_card_as_done);
        }
        holder.done.setChecked(cardStatus);


        if (fromArchive) {
            holder.done.setVisibility(View.GONE);
//            holder.mention.setText(tasksCardsList.get(position).getMention());
        }

        switch (importanceView) {
            case "2":
                holder.taskImportanceView.setBackgroundColor(context.getColor(R.color.canceRed));
                break;
            case "1":
                holder.taskImportanceView.setBackgroundColor(context.getColor(R.color.orange));
                break;
            default:
                holder.taskImportanceView.setBackgroundColor(context.getColor(R.color.okGreen));
        }
    }

    @Override
    public void onViewRecycled(@NonNull TasksCardViewHolder holder) {
        super.onViewRecycled(holder);
        holder.mentionChipGroup.removeAllViews();
    }

    @Override
    public int getItemCount() {
        return tasksCardsList.size();
    }

    private void getUserToken(String method , int position , TasksCard tasksCard , boolean checked ,  TasksCardViewHolder tasksCardViewHolder){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult().getToken();
                switch (method) {
                    case DELETE:
                        deleteTasksCard(position, tasksCard ,  token);
                        break;
                    case MARK:
                        markTaskCard(tasksCard  , checked , tasksCardViewHolder , token);
                        break;
                    case ARCHIVE:
                        archive(position , tasksCard , token);
                }
            }else{
                Snackbar.make(parentView,context.getString(R.string.authentication_error), BaseTransientBottomBar.LENGTH_LONG);
            }
        });
    }


    public void archive(final int position, final TasksCard tasksCard , String token) {
        final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        JSONObject cardDetails = new JSONObject(mapper.convertValue(tasksCard, new TypeReference<Map<String, Object>>() {}));
        JSONObject data = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Config.cardDetails, cardDetails);
            jsonObject.put(Config.apartmentID, apartmentID);
            data.put(Config.data, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_ARCHIVE_TASKS_CARD, data, response -> {

            try {
                boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                if(success){
                    Snackbar.make(parentView, context.getString(R.string.card_archived), BaseTransientBottomBar.LENGTH_SHORT)
                            .show();
                    tasksCardsList.remove(position);
                    notifyItemRemoved(position);
                }else{
                    Snackbar.make(parentView, context.getString(R.string.archving_error), BaseTransientBottomBar.LENGTH_SHORT)
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

    public void deleteTasksCard(final int position , TasksCard tasksCard, String token) {
        final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject cardDetails = new JSONObject(mapper.convertValue(tasksCard, new TypeReference<Map<String, Object>>() {}));

        try {
                jsonObject.put(Config.cardDetails, cardDetails);
                jsonObject.put(Config.apartmentID, apartmentID);
                data.put(Config.data, jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String url = fromArchive ? Config.URL_DELETE_TASK_CARD_ARCHIVE :Config.URL_DELETE_TASK_CARD;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url , data, response -> {

                try {
                    boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                    if(success) {
                        Snackbar snack = Snackbar.make(parentView, context.getString(R.string.card_deleted), BaseTransientBottomBar.LENGTH_SHORT);
                        snack.show();
                        tasksCardsList.remove(position);
                        notifyItemRemoved(position);
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
                    params.put(HttpHeaders.AUTHORIZATION,"Bearer "+token);
                    return params;
                }
            };
            requestQueue.add(jsonObjectRequest);


    }



    private void markTaskCard(TasksCard tasksCard, boolean checked , TasksCardViewHolder tasksCardViewHolder ,  String token) {
        final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject cardDetails = new JSONObject(mapper.convertValue(tasksCard, new TypeReference<Map<String, Object>>() {}));

        try {
            jsonObject.put(Config.cardDetails, cardDetails);
            jsonObject.put(Config.apartmentID, apartmentID);
            data.put(Config.data, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.URL_MARK_TASK_CARD, data, response -> {
            try {
                boolean success = response.getJSONObject(Config.result).getBoolean(Config.success);
                if(success){
                    if(checked){
                        Snackbar.make(parentView,context.getString(R.string.card_marked_as_done), BaseTransientBottomBar.LENGTH_SHORT)
                                .show();
                        tasksCardViewHolder.done.setText(R.string.done);
                    }else{
                        Snackbar.make(parentView,context.getString(R.string.card_marked_as_not_done), BaseTransientBottomBar.LENGTH_SHORT)
                                .show();
                        tasksCardViewHolder.done.setText(R.string.mark_card_as_done);
                    }


                }else{
                    tasksCardViewHolder.done.setChecked(!checked);
                    Snackbar.make(parentView,context.getString(R.string.marking_error),  BaseTransientBottomBar.LENGTH_SHORT)
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
