package com.example.shroomies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;



public class TasksCardAdapter extends RecyclerView.Adapter<TasksCardAdapter.TasksCardViewHolder> {

    private ArrayList<TasksCard> tasksCardsList;
    private Context context;
    private View view;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    public TasksCardAdapter(ArrayList<TasksCard> tasksCardsList, Context context) {
        this.tasksCardsList = tasksCardsList;
        this.context = context;
    }

    public class TasksCardViewHolder extends RecyclerView.ViewHolder {

        View taskImportanceView;
        TextView title,description,dueDate;
        ImageButton delete, archive;

        public TasksCardViewHolder(@NonNull View v) {
            super(v);
            taskImportanceView = v.findViewById(R.id.task_importance_view);
            title = v.findViewById(R.id.task_card_title);
            description = v.findViewById(R.id.task_card_description);
            dueDate = v.findViewById(R.id.task_card_duedate);
            delete = v.findViewById(R.id.task_delete);
            archive = v.findViewById(R.id.task_archive);

        }
    }

    @NonNull
    @Override
    public TasksCardAdapter.TasksCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view  = layoutInflater.inflate(R.layout.my_shroomie_tasks_card,parent,false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        return new TasksCardViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull TasksCardAdapter.TasksCardViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() { return tasksCardsList.size(); }
}
