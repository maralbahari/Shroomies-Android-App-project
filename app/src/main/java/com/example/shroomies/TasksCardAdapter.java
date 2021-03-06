package com.example.shroomies;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.widget.SocialTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class TasksCardAdapter extends RecyclerView.Adapter<TasksCardAdapter.TasksCardViewHolder> implements ItemTouchHelperAdapter  {

    private ArrayList<TasksCard> tasksCardsList;
    private Context context;
    private View view;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private ItemTouchHelper itemTouchHelper;
    private Boolean fromArchive;
    private ImageView sadShroomie, stars;
    private Button cont, no;
    private ShroomiesApartment apartment;
    private FragmentManager fragmentManager;


    public TasksCardAdapter(ArrayList<TasksCard> tasksCardsList, Context context,boolean fromArchive,ShroomiesApartment apartment,FragmentManager fragmentManager) {
        this.tasksCardsList = tasksCardsList;
        this.context = context;
        this.fromArchive = fromArchive;
        this.apartment=apartment;
        this.fragmentManager=fragmentManager;
    }


    @Override
    public void onItemSwiped(int position) {
        if(fromArchive){
            deleteFromArchive(position,tasksCardsList.get(position));
        }else{
            deleteCard(tasksCardsList.get(position),position);
        }

    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper){
        this.itemTouchHelper = itemTouchHelper;
    }

    public class TasksCardViewHolder extends RecyclerView.ViewHolder {

        View taskImportanceView;
        TextView title, description, dueDate, markAsDone;
        private SocialTextView mention;
        ImageButton delete, archive;
        ImageView sadShroomie, stars, shroomieArchive;
        Button cont, no, yesButton, noButton;
        CheckBox done;
        GestureDetector gestureDetector;
        private CardView taskCardView;

        public TasksCardViewHolder(@NonNull  View v) {
            super(v);
            taskImportanceView = v.findViewById(R.id.task_importance_view);
            title = v.findViewById(R.id.title_card);
            description = v.findViewById(R.id.card_description);
            dueDate = v.findViewById(R.id.dueDate_card);
            delete = v.findViewById(R.id.delete_card_btn);
            archive = v.findViewById(R.id.archive_card_btn);
            done = v.findViewById(R.id.expense_done);
            markAsDone = v.findViewById(R.id.shroomie_markasdone);
            mention = v.findViewById(R.id.expenses_mention_et);
            mention.setMentionColor(Color.BLUE);
            taskCardView=v.findViewById(R.id.task_card_view);
            taskCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ViewCards viewCard=new ViewCards();
                    Bundle bundle=new Bundle();
                    bundle.putParcelable("CARD_DETAILS",tasksCardsList.get(getAdapterPosition()));
                    bundle.putBoolean("FROM_TASK_TAB",true);
                    viewCard.setArguments(bundle);
                    viewCard.show(fragmentManager,"VIEWCARD");
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View alert = inflater.inflate(R.layout.are_you_sure,null);
                    builder.setView(alert);
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
                    alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialogfragment_add_member);
                    alertDialog.show();
                    sadShroomie = ((AlertDialog) alertDialog).findViewById(R.id.sad_shroomie);
                    stars = ((AlertDialog) alertDialog).findViewById(R.id.stars);
                    cont = ((AlertDialog) alertDialog).findViewById(R.id.button_continue);
                    no = ((AlertDialog) alertDialog).findViewById(R.id.button_no);

                    cont.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteCard(tasksCardsList.get(getAdapterPosition()),getAdapterPosition());
                            alertDialog.cancel();
                        }
                    });
                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.cancel();
                        }
                    });

                }
            });
            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (done.isChecked()){
                        rootRef.child("apartments").child(apartment.getApartmentID()).child("tasksCards").child(tasksCardsList.get(getAdapterPosition()).getCardId()).child("done").setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                markAsDone.setText("Done!");
                                saveToDoneLog(apartment.getApartmentID(),tasksCardsList.get(getAdapterPosition()));
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View alert = inflater.inflate(R.layout.do_you_want_to_archive,null);
                                builder.setView(alert);
                                final AlertDialog alertDialog = builder.create();
                                alertDialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
                                alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialogfragment_add_member);
                                alertDialog.show();
                                yesButton = ((AlertDialog) alertDialog).findViewById(R.id.btn_yes);
                                noButton = ((AlertDialog) alertDialog).findViewById(R.id.no_btn);
                                shroomieArchive = ((AlertDialog) alertDialog).findViewById(R.id.shroomie_archive);
                                yesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        archive(getAdapterPosition(),tasksCardsList.get(getAdapterPosition()));
                                        alertDialog.cancel();
                                    }
                                });
                                noButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        alertDialog.cancel();
                                    }
                                });

                            }
                        });
                    }else{
                        rootRef.child("apartments").child(apartment.getApartmentID()).child("tasksCards").child(tasksCardsList.get(getAdapterPosition()).getCardId()).child("done").setValue("false").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                saveToUnDoneLog(apartment.getApartmentID(),tasksCardsList.get(getAdapterPosition()));
                            }
                        });
                    }
                }
            });




            archive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    archive(getAdapterPosition(),tasksCardsList.get(getAdapterPosition()));
                }
            });



        }


    }

    public void deleteCard(final TasksCard taskcard, final int position){

        rootRef.child("apartments").child(apartment.getApartmentID()).child("tasksCards").child(taskcard.getCardId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                saveToDeleteLog(apartment.getApartmentID(),taskcard);
                notifyItemRemoved(position);
            }
        });


    }

    @NonNull
    @Override
    public TasksCardAdapter.TasksCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout task_card ;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.my_shroomie_tasks_card, parent, false);
        rootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        return new TasksCardViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull final TasksCardAdapter.TasksCardViewHolder holder, final int position) {

        holder.title.setText(tasksCardsList.get(position).getTitle());
        holder.description.setText(tasksCardsList.get(position).getDescription());
        holder.dueDate.setText(tasksCardsList.get(position).getDueDate());
        holder.mention.setText(tasksCardsList.get(position).getMention());
        String importanceView = tasksCardsList.get(position).getImportance();
        Boolean cardStatus = tasksCardsList.get(position).getDone().equals("true");
            if (cardStatus){
                holder.done.setChecked(cardStatus);
                holder.markAsDone.setText("Done!");
            }else{
                holder.done.setChecked(false);
                holder.markAsDone.setText("Mark as done");
            }


        if (fromArchive){
            holder.archive.setVisibility(view.GONE);
            holder.done.setVisibility(View.GONE);
            holder.markAsDone.setVisibility(View.GONE);
            holder.mention.setText(tasksCardsList.get(position).getMention());
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View alert = inflater.inflate(R.layout.are_you_sure,null);
                    builder.setView(alert);
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
                    alertDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialogfragment_add_member);
                    alertDialog.show();
                    sadShroomie = ((AlertDialog) alertDialog).findViewById(R.id.sad_shroomie);
                    stars = ((AlertDialog) alertDialog).findViewById(R.id.stars);
                    cont = ((AlertDialog) alertDialog).findViewById(R.id.button_continue);
                    no = ((AlertDialog) alertDialog).findViewById(R.id.button_no);

                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alertDialog.cancel();
                        }
                    });
                    cont.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                           deleteFromArchive(position,tasksCardsList.get(position));
                            alertDialog.cancel();
                        }
                    });

                }
            });
        }

        switch (importanceView) {
            case "0":
                holder.taskImportanceView.setBackgroundColor(Color.GREEN);
                break;
            case "2":
                holder.taskImportanceView.setBackgroundColor(Color.RED);
                break;
            case "1":
                holder.taskImportanceView.setBackgroundColor(Color.parseColor("#F59C34"));
                break;
            default:
                holder.taskImportanceView.setBackgroundColor(Color.parseColor("#F5CB5C"));
        }
    }

    private void deleteFromArchive(final int position, final TasksCard tasksCard) {
        rootRef.child("archive").child(apartment.getApartmentID()).child("tasksCards").child(tasksCardsList.get(position).getCardId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
               saveToDeleteArchiveLog(apartment.getApartmentID(),tasksCard);
                notifyItemRemoved(position);


            }

        });
    }


    @Override
    public int getItemCount() {
        return tasksCardsList.size();
    }


    public void archive(final int position,final TasksCard tasksCard){
        DatabaseReference ref = rootRef.child("archive").child(apartment.getApartmentID()).child("tasksCards").push();
        HashMap<String ,Object> newCard = new HashMap<>();
        Calendar calendarDate=Calendar.getInstance();
        String saveCurrentDate;
        String uniqueID = ref.getKey();
        SimpleDateFormat mcurrentDate=new SimpleDateFormat("dd-MMMM-yyyy HH:MM:ss aa");
        saveCurrentDate=mcurrentDate.format(calendarDate.getTime());
        newCard.put("description" , tasksCard.getDescription());
        newCard.put("title" ,tasksCard.getTitle());
        newCard.put("dueDate", tasksCard.getDueDate());
        newCard.put("importance", tasksCard.getImportance());
        newCard.put("date",saveCurrentDate);
        newCard.put("cardId",uniqueID);
        newCard.put("done", tasksCard.getDone());
        newCard.put("mention",tasksCard.getMention());
        ref.updateChildren(newCard).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    rootRef.child("apartments").child(apartment.getApartmentID()).child("tasksCards").child(tasksCard.getCardId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            saveToArchiveLog(apartment.getApartmentID(),tasksCard);
                            notifyItemRemoved(position);
                        }
                    });
                }
            }
        });
    }
    private void saveToDeleteLog(String apartmentID,TasksCard card){
        DatabaseReference ref=rootRef.child("logs").child(apartmentID).push();
        String logID=ref.getKey();
        final HashMap<String, Object> newRecord=new HashMap<>();
        newRecord.put("actor",mAuth.getCurrentUser().getUid());
        newRecord.put("when",ServerValue.TIMESTAMP);
        newRecord.put("cardTitle",card.getTitle());
        newRecord.put("action","deletingCard");
        newRecord.put("logID",logID);
        newRecord.put("cardType","tasks");
        ref.updateChildren(newRecord).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

    }
    private void saveToArchiveLog(String apartmentID,TasksCard card){
        DatabaseReference ref=rootRef.child("logs").child(apartmentID).push();
        String logID=ref.getKey();

        final HashMap<String, Object> newRecord=new HashMap<>();
        newRecord.put("actor",mAuth.getCurrentUser().getUid());
        newRecord.put("when",ServerValue.TIMESTAMP);
        newRecord.put("cardTitle",card.getTitle());
        newRecord.put("action","archivingCard");
        newRecord.put("logID",logID);
        newRecord.put("cardType","tasks");
        newRecord.put("cardID",card.getCardId());
        ref.updateChildren(newRecord).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

    }
    private void saveToDeleteArchiveLog(String apartmentID,TasksCard card){
        DatabaseReference ref=rootRef.child("logs").child(apartmentID).push();
        String logID=ref.getKey();
        final HashMap<String, Object> newRecord=new HashMap<>();
        newRecord.put("actor",mAuth.getCurrentUser().getUid());
        newRecord.put("when",ServerValue.TIMESTAMP);
        newRecord.put("cardTitle",card.getTitle());
        newRecord.put("action","deletingArchivedCard");
        newRecord.put("logID",logID);
        newRecord.put("cardType","tasks");
        ref.updateChildren(newRecord).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

    }
    private void saveToDoneLog(String apartmentID,TasksCard card){
        DatabaseReference ref=rootRef.child("logs").child(apartmentID).push();
        String logID=ref.getKey();
        final HashMap<String, Object> newRecord=new HashMap<>();
        newRecord.put("actor",mAuth.getCurrentUser().getUid());
        newRecord.put("when",ServerValue.TIMESTAMP);
        newRecord.put("cardTitle",card.getTitle());
        newRecord.put("action","markingDone");
        newRecord.put("logID",logID);
        newRecord.put("cardType","tasks");
        newRecord.put("cardID",card.getCardId());
        ref.updateChildren(newRecord).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

    }
    private void saveToUnDoneLog(String apartmentID,TasksCard card){
        DatabaseReference ref=rootRef.child("logs").child(apartmentID).push();
        String logID=ref.getKey();

        final HashMap<String, Object> newRecord=new HashMap<>();
        newRecord.put("actor",mAuth.getCurrentUser().getUid());
        newRecord.put("when", ServerValue.TIMESTAMP);
        newRecord.put("cardTitle",card.getTitle());
        newRecord.put("action","unMarkingDone");
        newRecord.put("logID",logID);
        newRecord.put("cardType","tasks");
        newRecord.put("cardID",card.getCardId());
        ref.updateChildren(newRecord).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

            }
        });

    }

    }


