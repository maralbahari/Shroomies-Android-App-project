package com.example.shroomies;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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


public class TasksCardAdapter extends RecyclerView.Adapter<TasksCardAdapter.TasksCardViewHolder> {

    private ArrayList<TasksCard> tasksCardsList;
    private Context context;
    private View view;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    Boolean fromArchive;

    public TasksCardAdapter(ArrayList<TasksCard> tasksCardsList, Context context,boolean fromArchive) {
        this.tasksCardsList = tasksCardsList;
        this.context = context;
        this.fromArchive = fromArchive;
    }

    public class TasksCardViewHolder extends RecyclerView.ViewHolder {

        View taskImportanceView;
        TextView title, description, dueDate;
        ImageButton delete, archive;

        public TasksCardViewHolder(@NonNull View v) {
            super(v);
            taskImportanceView = v.findViewById(R.id.task_importance_view);
            title = v.findViewById(R.id.task_card_title);
            description = v.findViewById(R.id.task_card_description);
            dueDate = v.findViewById(R.id.task_card_duedate);
            delete = v.findViewById(R.id.task_delete);
            archive = v.findViewById(R.id.task_archive);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteCard(tasksCardsList.get(getAdapterPosition()).getCardId(),getAdapterPosition());
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
    }

    public void deleteCard(String cardID, final int position){
        rootRef.child("apartments").child(mAuth.getCurrentUser().getUid()).child("tasksCards").child(cardID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                tasksCardsList.remove(position);
                notifyItemRemoved(position);
            }
        });

    }

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

        holder.title.setText(tasksCardsList.get(position).getTitle());
        holder.description.setText(tasksCardsList.get(position).getDescription());
        holder.dueDate.setText(tasksCardsList.get(position).getDueDate());
        String importanceView = tasksCardsList.get(position).getImportance();

        if (fromArchive){
            holder.archive.setVisibility(view.GONE);
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
        rootRef.child("apartments").child(mAuth.getCurrentUser().getUid()).child("tasksCards").child(cardId).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                rootRef.child("apartments").child(mAuth.getCurrentUser().getUid()).child("tasksCards").child(cardId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
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