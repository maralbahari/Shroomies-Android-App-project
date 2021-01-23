package com.example.shroomies;

import android.content.Context;
import android.graphics.Color;
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
import com.google.firebase.database.ValueEventListener;

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
    Boolean fromArchive;
    String currentUserAppartmentId= "";

    public TasksCardAdapter(ArrayList<TasksCard> tasksCardsList, Context context,boolean fromArchive) {
        this.tasksCardsList = tasksCardsList;
        this.context = context;
        this.fromArchive = fromArchive;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        TasksCard fromTaskCard = tasksCardsList.get(fromPosition);
        tasksCardsList.remove(fromTaskCard);
        tasksCardsList.add(toPosition,fromTaskCard);
        notifyItemMoved(fromPosition,toPosition);
    }

    @Override
    public void onItemSwiped(int position) {
        deleteCard(tasksCardsList.get(position).getCardId(),position);
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper){
        this.itemTouchHelper = itemTouchHelper;
    }

    public class TasksCardViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, GestureDetector.OnGestureListener {

        View taskImportanceView;
        TextView title, description, dueDate, markAsDone;
        ImageButton delete, archive;
        ImageView sadShroomie, stars, shroomieArchive;
        Button cont, no, yesButton, noButton;
        CheckBox done;
        GestureDetector gestureDetector;




        public TasksCardViewHolder(@NonNull  View v) {
            super(v);
            taskImportanceView = v.findViewById(R.id.task_importance_view);
            title = v.findViewById(R.id.task_card_title);
            description = v.findViewById(R.id.task_card_description);
            dueDate = v.findViewById(R.id.task_card_duedate);
            delete = v.findViewById(R.id.task_delete);
            archive = v.findViewById(R.id.task_archive);
            done = v.findViewById(R.id.task_done);
            markAsDone = v.findViewById(R.id.task_mark_as_done);


            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (done.isChecked()){
                        rootRef.child("apartments").child(currentUserAppartmentId).child("tasksCards").child(tasksCardsList.get(getAdapterPosition()).getCardId()).child("done").setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                markAsDone.setText("Done!");
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
                                        archive(tasksCardsList.get(getAdapterPosition()).getCardId(),getAdapterPosition());
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
                        rootRef.child("apartments").child(currentUserAppartmentId).child("tasksCards").child(tasksCardsList.get(getAdapterPosition()).getCardId()).child("done").setValue("false").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context,"not done",Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });

            gestureDetector = new GestureDetector(v.getContext(),this);
            v.setOnTouchListener(this);

//            delete.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    deleteCard(tasksCardsList.get(getAdapterPosition()).getCardId(),getAdapterPosition());
//                }
//            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View alert = inflater.inflate(R.layout.are_you_sure,null);
                    builder.setView(alert);
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    sadShroomie = ((AlertDialog) alertDialog).findViewById(R.id.sad_shroomie);
                    stars = ((AlertDialog) alertDialog).findViewById(R.id.stars);
                    cont = ((AlertDialog) alertDialog).findViewById(R.id.button_continue);
                    no = ((AlertDialog) alertDialog).findViewById(R.id.button_no);



                    cont.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteCard(tasksCardsList.get(getAdapterPosition()).getCardId(),getAdapterPosition());
                            Toast.makeText(context,"Card deleted", Toast.LENGTH_LONG).show();
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

            String mtitle = title.getText().toString();
            String mDescription = description.getText().toString();

            archive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    archive(tasksCardsList.get(getAdapterPosition()).getCardId(),getAdapterPosition());
                }
            });



        }

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
            itemTouchHelper.startDrag(this);
        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            gestureDetector.onTouchEvent(motionEvent);
            return true;
        }
    }

    public void deleteCard(String cardID, final int position){
        rootRef.child("apartments").child(currentUserAppartmentId).child("tasksCards").child(cardID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (tasksCardsList.size()<=1){
                    tasksCardsList = new ArrayList<>();
                    notifyDataSetChanged();
                }else {
                    tasksCardsList.remove(position);
                    notifyItemRemoved(position);
                }
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
        getUserRoomId();
        return new TasksCardViewHolder(view);


    }


    private void getUserRoomId(){
        rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("isPartOfRoom").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    currentUserAppartmentId=snapshot.getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }




    @Override
    public void onBindViewHolder(@NonNull final TasksCardAdapter.TasksCardViewHolder holder, final int position) {

        holder.title.setText(tasksCardsList.get(position).getTitle());
        holder.description.setText(tasksCardsList.get(position).getDescription());
        holder.dueDate.setText(tasksCardsList.get(position).getDueDate());
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
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rootRef.child("apartments").child(currentUserAppartmentId).child("tasksCards").child(tasksCardsList.get(position).getCardId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context,"Task card deleted",Toast.LENGTH_LONG).show();
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





    @Override
    public int getItemCount() {
        return tasksCardsList.size();
    }


    public void archive(final String cardId, final int position){
        rootRef.child("apartments").child(currentUserAppartmentId).child("tasksCards").child(cardId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    TasksCard tasksCard = snapshot.getValue(TasksCard.class);
                    DatabaseReference ref = rootRef.child("archive").child(mAuth.getCurrentUser().getUid()).child("tasksCards").push();
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
                    ref.updateChildren(newCard).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                rootRef.child("apartments").child(currentUserAppartmentId).child("tasksCards").child(cardId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Card successfully Archived", Toast.LENGTH_SHORT).show();
                                        tasksCardsList.remove(position);
                                        notifyItemRemoved(position);
                                    }
                                });
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    }


