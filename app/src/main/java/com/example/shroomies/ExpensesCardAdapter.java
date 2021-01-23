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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
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

public class ExpensesCardAdapter extends RecyclerView.Adapter<ExpensesCardAdapter.ExpensesViewHolder> implements ItemTouchHelperAdapter {


    private ArrayList<ExpensesCard> cardsList;
    private Context context;
    private View view;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private ItemTouchHelper itemTouchHelper;
    Boolean fromArchive;
    String currentUserAppartmentId = "";

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
        getUserRoomId();
        return new ExpensesViewHolder(view);

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
    public void onBindViewHolder(@NonNull final ExpensesViewHolder holder, final int position) {
            holder.title.setText(cardsList.get(position).getTitle());
            holder.description.setText(cardsList.get(position).getDescription());
            holder.dueDate.setText(cardsList.get(position).getDueDate());
            String importanceViewColor = cardsList.get(position).getImportance();
            holder.mention.setText(cardsList.get(position).getMention());



        Boolean cardStatus = cardsList.get(position).getDone().equals("true");
        if (cardStatus){
            holder.done.setChecked(true);
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
                    rootRef.child("apartments").child(currentUserAppartmentId).child("expensesCards").child(cardsList.get(position).getCardId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context,"Expenses card deleted",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
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


    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper){
        this.itemTouchHelper = itemTouchHelper;
    }




    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        ExpensesCard fExpensesCard = cardsList.get(fromPosition);
        cardsList.remove(fExpensesCard);
        cardsList.add(toPosition,fExpensesCard);
        notifyItemMoved(fromPosition,toPosition);
    }

    @Override
    public void onItemSwiped(int position) {
        deleteExpensesCard(position);

    }



    public class ExpensesViewHolder extends RecyclerView.ViewHolder implements  View.OnTouchListener, GestureDetector.OnGestureListener {
        View importanceView;
        GestureDetector gestureDetector;
        TextView title,description,dueDate,mention,markAsDone;
        ImageView cardImage;
        ImageButton archive, delete;
        ImageView sadShroomie, stars, shroomieArch;
        Button cont, no,yesBtn,noBtn;
        CheckBox done;

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
            done = v.findViewById(R.id.expense_done);
            markAsDone = v.findViewById(R.id.shroomie_markasdone);

            gestureDetector = new GestureDetector(v.getContext(),this);
            v.setOnTouchListener(this);

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (done.isChecked()){
                        rootRef.child("apartments").child(currentUserAppartmentId).child("expensesCards").child(cardsList.get(getAdapterPosition()).getCardId()).child("done").setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
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
                                yesBtn = ((AlertDialog) alertDialog).findViewById(R.id.btn_yes);
                                noBtn = ((AlertDialog) alertDialog).findViewById(R.id.no_btn);
                                shroomieArch = ((AlertDialog) alertDialog).findViewById(R.id.shroomie_archive);
                                yesBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        archive(cardsList.get(getAdapterPosition()).getCardId(),getAdapterPosition());
                                        alertDialog.cancel();
                                    }
                                });
                                noBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        alertDialog.cancel();
                                    }
                                });



                            }
                        });
                    }else{
                        rootRef.child("apartments").child(currentUserAppartmentId).child("expensesCards").child(cardsList.get(getAdapterPosition()).getCardId()).child("done").setValue("false").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(context,"expenses cards not done", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });




            archive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    archive(cardsList.get(getAdapterPosition()).getCardId(),getAdapterPosition());
                }
            });

//            delete.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    deleteCard(cardsList.get(getAdapterPosition()).getCardId(),getAdapterPosition());
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
                            deleteExpensesCard(getAdapterPosition());
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

    public void deleteExpensesCard( final int position){


        rootRef.child("apartments").child(currentUserAppartmentId).child("expensesCards").child( cardsList.get(position).getCardId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (cardsList.size()<=1){
                    cardsList = new ArrayList<>();
                    notifyDataSetChanged();
                }else {
                    Toast.makeText(context, "Card deleted", Toast.LENGTH_SHORT).show();
                    cardsList.remove(position);
                    notifyItemRemoved(position);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void archive(final String cardId, final int position){
        rootRef.child("apartments").child(currentUserAppartmentId).child("expensesCards").child(cardId).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                rootRef.child("apartments").child(currentUserAppartmentId).child("expensesCards").child(cardId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
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
