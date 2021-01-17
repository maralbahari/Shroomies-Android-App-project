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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ExpensesCardAdapter extends RecyclerView.Adapter<ExpensesCardAdapter.ExpensesViewHolder> {


    private ArrayList<ExpensesCard> cardsList;
    private Context context;
    private View view;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;

    public ExpensesCardAdapter(ArrayList<ExpensesCard> cardsList, Context context) {
        this.cardsList = cardsList;
        this.context=context;
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
                    .circleCrop()
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

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    rootRef.child("expensesCards").child(mAuth.getCurrentUser().getUid()).child(cardsList.get(getAdapterPosition()).getCardId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(context,"Card deleted",Toast.LENGTH_SHORT).show();
//                cardsList.remove(getAdapterPosition());
                            cardsList.remove(getAdapterPosition());
                            notifyItemRemoved(getAdapterPosition());

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
            });
        }




    }

    private void deleteCard(final int position){

    }
}
