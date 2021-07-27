package com.example.shroomies;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
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

import java.util.ArrayList;
import java.util.Map;


public class TasksCardAdapter extends RecyclerView.Adapter<TasksCardAdapter.TasksCardViewHolder> implements ItemTouchHelperAdapter {

    private ArrayList<TasksCard> tasksCardsList;
    private Context context;
    private View view;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private ItemTouchHelper itemTouchHelper;
    private Boolean fromArchive;
    private String apartmentID;
    private FragmentManager fragmentManager;
    private View parentView;
    private FirebaseFunctions mfunc;
    private RequestQueue requestQueue;


    public TasksCardAdapter(ArrayList<TasksCard> tasksCardsList, Context context, boolean fromArchive, String apartmentID, FragmentManager fragmentManager, View parentView) {
        this.tasksCardsList = tasksCardsList;
        this.context = context;
        this.fromArchive = fromArchive;
        this.apartmentID = apartmentID;
        this.fragmentManager = fragmentManager;
        this.parentView = parentView;
        mfunc = FirebaseFunctions.getInstance();
        mfunc.useEmulator("10.0.2.2", 5001);
        requestQueue = Volley.newRequestQueue(context);
    }


    @Override
    public void onItemSwiped(int position) {
        if (fromArchive) {
//            deleteFromArchive(position, tasksCardsList.get(position));
        } else {
            deleteTasksCard(position);
        }

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
//                    if (done.isChecked()){
//                        rootRef.child("apartments").child(apartment.getApartmentID()).child("tasksCards").child(tasksCardsList.get(getAdapterPosition()).getCardID()).child("done").setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                done.setText("Done!");
//                            }
//                        });
//                    }else{
//                        rootRef.child("apartments").child(apartment.getApartmentID()).child("tasksCards").child(tasksCardsList.get(getAdapterPosition()).getCardID()).child("done").setValue("false").addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                            }
//                        });
//                    }
                    markTaskCard(tasksCard.getCardID(), apartmentID, done.isChecked());
                }
            });

            archive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    archive(getAdapterPosition(), tasksCardsList.get(getAdapterPosition()));
                }
            });


        }


    }

//    public void deleteCard(final TasksCard taskcard, final int position) {
//
//        rootRef.child("apartments").child(apartment.getApartmentID()).child("tasksCards").child(taskcard.getCardID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                saveToDeleteLog(apartment.getApartmentID(), taskcard);
//                Snackbar snack = Snackbar.make(parentView, "Card deleted", BaseTransientBottomBar.LENGTH_SHORT);
//                snack.setAnchorView(R.id.bottomNavigationView);
//                snack.show();
//                notifyItemRemoved(position);
//            }
//        });
//
//
//    }

    @NonNull
    @Override
    public TasksCardAdapter.TasksCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.my_shroomie_tasks_card, parent, false);
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        return new TasksCardViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull final TasksCardAdapter.TasksCardViewHolder holder, final int position) {

        holder.titleTextView.setText(tasksCardsList.get(position).getTitle());
        holder.descriptionTextView.setText(tasksCardsList.get(position).getDescription());
        if (tasksCardsList.get(position).getDueDate().equals("Due date")) {
            holder.dueDateTextView.setText(" None");
        } else {
            holder.dueDateTextView.setText(tasksCardsList.get(position).getDueDate());
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

//    private void deleteFromArchive(final int position, final TasksCard tasksCard) {
//        rootRef.child("archive").child(apartment.getApartmentID()).child("tasksCards").child(tasksCardsList.get(position).getCardID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                saveToDeleteArchiveLog(apartment.getApartmentID(), tasksCard);
//                Snackbar snack = Snackbar.make(parentView, "Card deleted", BaseTransientBottomBar.LENGTH_SHORT);
//                snack.setAnchorView(R.id.bottomNavigationView);
//                snack.show();
//                notifyItemRemoved(position);
//
//
//            }
//
//        });
//    }


    @Override
    public int getItemCount() {
        return tasksCardsList.size();
    }


//    public void archive(final int position,final TasksCard tasksCard){
//        DatabaseReference ref = rootRef.child("archive").child(apartment.getApartmentID()).child("tasksCards").push();
//        HashMap<String ,Object> newCard = new HashMap<>();
//        String uniqueID = ref.getKey();
//        newCard.put("description" , tasksCard.getDescription());
//        newCard.put("title" ,tasksCard.getTitle());
//        newCard.put("dueDate", tasksCard.getDueDate());
//        newCard.put("importance", tasksCard.getImportance());
//        newCard.put("date",ServerValue.TIMESTAMP);
//        newCard.put("cardId",uniqueID);
//        newCard.put("done", tasksCard.getDone());
//        newCard.put("mention",tasksCard.getMention());
//        ref.updateChildren(newCard).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()){
//                    rootRef.child("apartments").child(apartment.getApartmentID()).child("tasksCards").child(tasksCard.getCardID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            saveToArchiveLog(apartment.getApartmentID(),tasksCard);
//                            Snackbar.make(parentView,"Card archived", BaseTransientBottomBar.LENGTH_SHORT)
//                                    .setAnchorView(R.id.bottomNavigationView)
//                                    .show();
//                            notifyItemRemoved(position);
//                        }
//                    });
//                }
//            }
//        });
//    }
//    private void saveToDeleteLog(String apartmentID,TasksCard card){
//        DatabaseReference ref=rootRef.child("logs").child(apartmentID).push();
//        String logID=ref.getKey();
//        final HashMap<String, Object> newRecord=new HashMap<>();
//        newRecord.put("actor",mAuth.getCurrentUser().getUid());
//        newRecord.put("when",ServerValue.TIMESTAMP);
//        newRecord.put("cardTitle",card.getTitle());
//        newRecord.put("action","deletingCard");
//        newRecord.put("logID",logID);
//        newRecord.put("cardType","tasks");
//        ref.updateChildren(newRecord).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//
//            }
//        });
//
//    }
//    private void saveToArchiveLog(String apartmentID,TasksCard card){
//        DatabaseReference ref=rootRef.child("logs").child(apartmentID).push();
//        String logID=ref.getKey();
//
//        final HashMap<String, Object> newRecord=new HashMap<>();
//        newRecord.put("actor",mAuth.getCurrentUser().getUid());
//        newRecord.put("when",ServerValue.TIMESTAMP);
//        newRecord.put("cardTitle",card.getTitle());
//        newRecord.put("action","archivingCard");
//        newRecord.put("logID",logID);
//        newRecord.put("cardType","tasks");
//        newRecord.put("cardID",card.getCardID());
//        ref.updateChildren(newRecord).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//
//            }
//        });
//
//    }
//    private void saveToDeleteArchiveLog(String apartmentID,TasksCard card){
//        DatabaseReference ref=rootRef.child("logs").child(apartmentID).push();
//        String logID=ref.getKey();
//        final HashMap<String, Object> newRecord=new HashMap<>();
//        newRecord.put("actor",mAuth.getCurrentUser().getUid());
//        newRecord.put("when",ServerValue.TIMESTAMP);
//        newRecord.put("cardTitle",card.getTitle());
//        newRecord.put("action","deletingArchivedCard");
//        newRecord.put("logID",logID);
//        newRecord.put("cardType","tasks");
//        ref.updateChildren(newRecord).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//
//            }
//        });
//
//    }
//    private void saveToDoneLog(String apartmentID,TasksCard card){
//        DatabaseReference ref=rootRef.child("logs").child(apartmentID).push();
//        String logID=ref.getKey();
//        final HashMap<String, Object> newRecord=new HashMap<>();
//        newRecord.put("actor",mAuth.getCurrentUser().getUid());
//        newRecord.put("when",ServerValue.TIMESTAMP);
//        newRecord.put("cardTitle",card.getTitle());
//        newRecord.put("action","markingDone");
//        newRecord.put("logID",logID);
//        newRecord.put("cardType","tasks");
//        newRecord.put("cardID",card.getCardID());
//        ref.updateChildren(newRecord).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//
//            }
//        });
//
//    }
//    private void saveToUnDoneLog(String apartmentID,TasksCard card){
//        DatabaseReference ref=rootRef.child("logs").child(apartmentID).push();
//        String logID=ref.getKey();
//
//        final HashMap<String, Object> newRecord=new HashMap<>();
//        newRecord.put("actor",mAuth.getCurrentUser().getUid());
//        newRecord.put("when", ServerValue.TIMESTAMP);
//        newRecord.put("cardTitle",card.getTitle());
//        newRecord.put("action","unMarkingDone");
//        newRecord.put("logID",logID);
//        newRecord.put("cardType","tasks");
//        newRecord.put("cardID",card.getCardID());
//        ref.updateChildren(newRecord).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//
//            }
//        });
//
//    }

    public void archive(final int position, final TasksCard tasksCard) {
        final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        JSONObject cardDetails = new JSONObject(mapper.convertValue(tasksCard, new TypeReference<Map<String, Object>>() {
        }));
        JSONObject data = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        //        firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//            @Override
//            public void onComplete(@NonNull Task<GetTokenResult> task) {
//                if(task.isSuccessful()){
//                    String token = task.getResult().getToken();

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

            }
        });
        requestQueue.add(jsonObjectRequest);
//    }
//}
//}):


    }

    public void deleteTasksCard(final int position) {

        if(!fromArchive) {
            JSONObject jsonObject = new JSONObject();
            JSONObject data = new JSONObject();

//        firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//            @Override
//            public void onComplete(@NonNull Task<GetTokenResult> task) {
//                if(task.isSuccessful()){
//                    String token = task.getResult().getToken();
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

                }
            });
            requestQueue.add(jsonObjectRequest);
//    }
//}
//});
        }else{

        }

    }



    private void markTaskCard(String cardID, String apartmentID, boolean checked) {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
//        firebaseUser.getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
//            @Override
//            public void onComplete(@NonNull Task<GetTokenResult> task) {
//                if(task.isSuccessful()){
//                    String token = task.getResult().getToken();

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

            }
        });
        requestQueue.add(jsonObjectRequest);
//                }
//            }
//        });

    }


}
