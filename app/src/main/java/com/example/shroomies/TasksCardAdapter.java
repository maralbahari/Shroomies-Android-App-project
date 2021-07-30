package com.example.shroomies;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.hendraanggrian.appcompat.widget.SocialTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.TimeZone;


public class TasksCardAdapter extends RecyclerView.Adapter<TasksCardAdapter.TasksCardViewHolder> implements ItemTouchHelperAdapter {
    private View parentView;
    private Context context;
    private View view;
    private ItemTouchHelper itemTouchHelper;

    private ArrayList<TasksCard> tasksCardsList;
    private FragmentManager fragmentManager;

    private RequestQueue requestQueue;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    private Boolean fromArchive;
    private static final String DELETE = "delete";
    private static final String ARCHIVE = "archive";
    private static final String MARK = "mark";
    private String apartmentID;



    public TasksCardAdapter(ArrayList<TasksCard> tasksCardsList, Context context, boolean fromArchive, String apartmentID, FragmentManager fragmentManager, View parentView) {
        this.tasksCardsList = tasksCardsList;
        this.context = context;
        this.fromArchive = fromArchive;
        this.apartmentID = apartmentID;
        this.fragmentManager = fragmentManager;
        this.parentView = parentView;
        mAuth = FirebaseAuth.getInstance();
        mAuth.useEmulator("10.0.2.2", 9099);
        requestQueue = Volley.newRequestQueue(context);
    }


    @Override
    public void onItemSwiped(int position) {
        getUserToken(DELETE ,position , null ,false );

    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    public class TasksCardViewHolder extends RecyclerView.ViewHolder {

        private View taskImportanceView;
        private TextView titleTextView, descriptionTextView, dueDateTextView;
        private SocialTextView mention;
        private ImageButton archive;
        private CheckBox done;
        private CardView taskCardView;

        public TasksCardViewHolder(@NonNull View v) {
            super(v);
            taskImportanceView = v.findViewById(R.id.task_importance_view);
            titleTextView = v.findViewById(R.id.title_card);
            descriptionTextView = v.findViewById(R.id.card_description);
            dueDateTextView = v.findViewById(R.id.dueDate_card);
            archive = v.findViewById(R.id.archive_card_btn);
            done = v.findViewById(R.id.task_done);

            mention = v.findViewById(R.id.task_mention_et);
            mention.setMentionColor(Color.BLUE);
            taskCardView = v.findViewById(R.id.task_card_view);
            taskCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ViewCards viewCard = new ViewCards();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("CARD_DETAILS", tasksCardsList.get(getAdapterPosition()));
                    bundle.putBoolean("FROM_TASK_TAB", true);
                    viewCard.setArguments(bundle);
                    viewCard.show(fragmentManager, "VIEWCARD");
                }
            });

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TasksCard tasksCard = tasksCardsList.get(getAdapterPosition());
                    getUserToken(MARK , getAdapterPosition() , tasksCard , done.isChecked());
                }
            });

            archive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    archive(getAdapterPosition(), tasksCardsList.get(getAdapterPosition()));
                    getUserToken(ARCHIVE , getAdapterPosition() , tasksCardsList.get(getAdapterPosition()) , false);
                }
            });


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


    @NonNull
    @Override
    public TasksCardAdapter.TasksCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.my_shroomie_tasks_card, parent, false);
        rootRef = FirebaseDatabase.getInstance().getReference();
        return new TasksCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TasksCardAdapter.TasksCardViewHolder holder, final int position) {

        holder.titleTextView.setText(tasksCardsList.get(position).getTitle());
        holder.descriptionTextView.setText(tasksCardsList.get(position).getDescription());
        if (tasksCardsList.get(position).getDueDate()==null) {
            holder.dueDateTextView.setText("None");
        } else {
            setDate(tasksCardsList.get(position).getDueDate() , holder.dueDateTextView);
        }
        if (tasksCardsList.get(position).getMention()!=null) {
            holder.mention.setVisibility(View.VISIBLE);
//            holder.mention.setText(tasksCardsList.get(position).getMention());
        }
        String importanceView = tasksCardsList.get(position).getImportance();
        Boolean cardStatus = tasksCardsList.get(position).getDone().equals("true");
        if (cardStatus) {
            holder.done.setChecked(cardStatus);
            holder.done.setText("Done!");
        } else {
            holder.done.setChecked(false);
            holder.done.setText("Mark as done");
        }

        if (fromArchive) {
            holder.archive.setVisibility(view.GONE);
            holder.done.setVisibility(View.GONE);
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
    public int getItemCount() {
        return tasksCardsList.size();
    }

    private void getUserToken(String method , int position , TasksCard tasksCard , boolean checked){
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    switch (method) {
                        case DELETE:
                            deleteTasksCard(position);
                            break;
                        case MARK:
                            markTaskCard(tasksCard.getCardID(), apartmentID  , checked);
                        case ARCHIVE:
                            archive(position , tasksCard);
                    }
                }else{
                    Snackbar.make(parentView,"We encountered an error while authenticating your account", BaseTransientBottomBar.LENGTH_LONG);
                }
            }
        });
    }


    public void archive(final int position, final TasksCard tasksCard) {
        final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        JSONObject cardDetails = new JSONObject(mapper.convertValue(tasksCard, new TypeReference<Map<String, Object>>() {
        }));
        JSONObject data = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cardDetails", cardDetails);
            jsonObject.put("apartmentID", apartmentID);
            data.put("data", jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.FUNCTION_ARCHIVE_TASKS_CARD, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Snackbar.make(parentView, "Card archived", BaseTransientBottomBar.LENGTH_SHORT)
                        .show();
                tasksCardsList.remove(position);
                notifyItemRemoved(position);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayError(error);
            }
        });
        requestQueue.add(jsonObjectRequest);

    }

    public void deleteTasksCard(final int position) {

            JSONObject jsonObject = new JSONObject();
            JSONObject data = new JSONObject();
            try {
                jsonObject.put("apartmentID", apartmentID);
                jsonObject.put("cardID", tasksCardsList.get(position).getCardID());
                data.put("data", jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String url = fromArchive ? Config.FUNCTION_DELETE_TASK_CARD_ARCHIVE:Config.FUNCTION_DELETE_TASK_CARD;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url , data, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Snackbar snack = Snackbar.make(parentView, "Card deleted", BaseTransientBottomBar.LENGTH_SHORT);
                    snack.show();
                    tasksCardsList.remove(position);
                    notifyItemRemoved(position);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    displayError(error);
                }
            });
            requestQueue.add(jsonObjectRequest);


    }



    private void markTaskCard(String cardID, String apartmentID, boolean checked) {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            jsonObject.put("booleanValue" , checked);
            jsonObject.put("apartmentID" , apartmentID);
            jsonObject.put("cardID" ,cardID);
            data.put("data" , jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.FUNCTION_MARK_TASK_CARD, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String done = "done";
                if(!checked){ done = " not done"; }
                Snackbar.make(parentView,"Card marked as"+done, BaseTransientBottomBar.LENGTH_SHORT)
                        .show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                displayError(error);
            }
        });
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
