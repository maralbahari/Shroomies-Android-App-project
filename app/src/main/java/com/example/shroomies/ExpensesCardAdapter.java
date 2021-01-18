package com.example.shroomies;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

public class ExpensesCardAdapter extends RecyclerView.Adapter<ExpensesCardAdapter.ExpensesViewHolder> {


    private ArrayList<ExpensesCard> cardsList;
    private Context context;
    private View view;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    Boolean fromArchive;

    public ExpensesCardAdapter(ArrayList<ExpensesCard> cardsList, Context context, Boolean fromArchive) {
        this.cardsList = cardsList;
        this.context=context;
        this.fromArchive = fromArchive;
    }


    @NonNull
    @Override
    public ExpensesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view  = layoutInflater.inflate(R.layout.my_shroomie_expenses_card,parent,false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        return new ExpensesViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ExpensesViewHolder holder, final int position) {
            holder.title.setText(cardsList.get(position).getTitle());
            holder.description.setText(cardsList.get(position).getDescription());
            holder.dueDate.setText(cardsList.get(position).getDueDate());
            String importanceViewColor = cardsList.get(position).getImportance();

            if (fromArchive){
                holder.archive.setVisibility(view.GONE);
            }

        switch (importanceViewColor) {
            case "0":
                holder.importanceView.setBackgroundColor(Color.GREEN);
                break;
            case  "2":
                holder.importanceView.setBackgroundColor(Color.RED);
                break;
            case  "1":
                holder.importanceView.setBackgroundColor(Color.parseColor("#F59C34"));
                break;
            default:
                holder.importanceView.setBackgroundColor(Color.parseColor("#F5CB5C"));
        }

        if (!cardsList.get(position).getAttachedFile().isEmpty()) {
            GlideApp.with(context)
                    .load(cardsList.get(position).getAttachedFile())
                    .fitCenter()
                    .centerCrop()
//                    .transform(new RoundedCornersTransformation(50))
                    .into(holder.cardImage);
        }

    }


    @Override
    public int getItemCount() {
        return cardsList.size();
    }

    public class ExpensesViewHolder extends RecyclerView.ViewHolder {
        View importanceView;
        TextView title,description,dueDate,mention;
        ImageView cardImage;
        ImageButton archive, delete;

        public ExpensesViewHolder(@NonNull View v) {
            super(v);
            importanceView = v.findViewById(R.id.importance_view);
            title = v.findViewById(R.id.title_card);
            description = v.findViewById(R.id.card_description);
            dueDate = v.findViewById(R.id.dueDate_card);
            cardImage= v.findViewById(R.id.card_img);
            archive = v.findViewById(R.id.archive_card_btn);
            delete = v.findViewById(R.id.delete_card_btn);
            mention = v.findViewById(R.id.shroomie_mention);

            archive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    archive(cardsList.get(getAdapterPosition()).getCardId(),getAdapterPosition());
                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteCard(cardsList.get(getAdapterPosition()).getCardId(),getAdapterPosition());
                }
            });
        }

    }

    public void deleteCard(String cardId, final int position){

        rootRef.child("apartments").child(mAuth.getCurrentUser().getUid()).child("expensesCards").child(cardId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Toast.makeText(context,"Card deleted",Toast.LENGTH_SHORT).show();
                cardsList.remove(position);
                notifyItemRemoved(position);
//                notifyItemRangeChanged(position,cardsList.size());
//                notifyDataSetChanged();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void archive(final String cardId, final int position){
        rootRef.child("apartments").child(mAuth.getCurrentUser().getUid()).child("expensesCards").child(cardId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ExpensesCard expensesCard = snapshot.getValue(ExpensesCard.class);
                    DatabaseReference ref = rootRef.child("archive").child(mAuth.getCurrentUser().getUid()).child("expensesCards").push();
                    HashMap<String ,Object> newCard = new HashMap<>();
                    Calendar calendarDate=Calendar.getInstance();
                    String saveCurrentDate;
                    String uniqueID = ref.getKey();
                    SimpleDateFormat mcurrentDate=new SimpleDateFormat("dd-MMMM-yyyy HH:MM:ss aa");
                    saveCurrentDate=mcurrentDate.format(calendarDate.getTime());
                    newCard.put("description" , expensesCard.getDescription());
                    newCard.put("title" ,expensesCard.getTitle());
                    newCard.put("dueDate", expensesCard.getDueDate());
                    newCard.put("importance", expensesCard.getImportance());
                    newCard.put("date",saveCurrentDate);
                    newCard.put("cardId",uniqueID);
                    newCard.put("attachedFile", expensesCard.getAttachedFile());
                    ref.updateChildren(newCard).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                rootRef.child("apartments").child(mAuth.getCurrentUser().getUid()).child("expensesCards").child(cardId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Card successfully Archived", Toast.LENGTH_SHORT).show();
                                        if (cardsList.size()==1){
                                            cardsList.clear();
                                            notifyItemRemoved(position);
                                        }else {
                                            cardsList.remove(position);
                                            notifyItemRemoved(position);
                                        }
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
