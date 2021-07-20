package com.example.shroomies;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.hendraanggrian.widget.SocialTextView;
import com.virgilsecurity.crypto.foundation.Hash;

import org.jetbrains.annotations.NotNull;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ExpensesCardAdapter extends RecyclerView.Adapter<ExpensesCardAdapter.ExpensesViewHolder> implements ItemTouchHelperAdapter {


    private  ArrayList<ExpensesCard> expensesCardArrayList;
    private Context context;
    private View view;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private ItemTouchHelper itemTouchHelper;
    private Boolean fromArchive;

    private ShroomiesApartment apartment;
    private FirebaseStorage storage;
    private FirebaseFunctions mfunc;
    private FragmentManager fragmentManager;
    private View parentView;

    public ExpensesCardAdapter(ArrayList<ExpensesCard> expensesCardArrayList, Context context, Boolean fromArchive,ShroomiesApartment apartment,FragmentManager fragmentManager,View parentView) {
        this.expensesCardArrayList = expensesCardArrayList;
        this.context=context;
        this.fromArchive = fromArchive;
        this.apartment=apartment;
        this.fragmentManager=fragmentManager;
        this.parentView=parentView;
    }

    public void setItemTouchHelper(ItemTouchHelper itemTouchHelper){
        this.itemTouchHelper = itemTouchHelper;
    }


    @Override
    public void onItemSwiped(int position) {
        if(fromArchive){
//            deleteFromArchive(position,expensesCardArrayList.get(position));
        }else{
            deleteExpensesCard(position);
        }


    }



    @NonNull
    @Override
    public ExpensesCardAdapter.ExpensesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view  = layoutInflater.inflate(R.layout.my_shroomie_expenses_card,parent,false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        storage=FirebaseStorage.getInstance();
        mfunc= FirebaseFunctions.getInstance();
        mfunc.useEmulator("10.0.2.2",5001);
        return new ExpensesViewHolder(view);

    }
    @Override
    public void onBindViewHolder(@NonNull final ExpensesCardAdapter.ExpensesViewHolder holder, final int position) {
        holder.title.setText(expensesCardArrayList.get(position).getTitle());
        holder.description.setText(expensesCardArrayList.get(position).getDescription());
        if(expensesCardArrayList.get(position).getDueDate().equals("Due date")){
            holder.dueDate.setText("None");
        }else {
            holder.dueDate.setText(" "+expensesCardArrayList.get(position).getDueDate());
            String dueString=expensesCardArrayList.get(position).getDueDate();
            ParsePosition pos = new ParsePosition(0);
            SimpleDateFormat simpledateformat = new SimpleDateFormat("EEE, MMM d,");
            Date dueDate=simpledateformat.parse(dueString,pos);
            Date currentDate=(new Date(System.currentTimeMillis()));
            long diff=currentDate.getTime()-dueDate.getTime();
            long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diff);
            diffInSec /= 60;
            diffInSec /= 60;
            diffInSec /= 24;
            long days = diffInSec;
            if(days<2){
                holder.dueDate.setTextColor(Color.RED);
            }

        }
        String importanceViewColor = expensesCardArrayList.get(position).getImportance();
        if(!expensesCardArrayList.get(position).getMention().isEmpty()) {
            holder.mention.setVisibility(View.VISIBLE);
            holder.mention.setText(expensesCardArrayList.get(position).getMention());
        }
        Boolean cardStatus = expensesCardArrayList.get(position).getDone().equals("true");
        if (cardStatus){
            holder.done.setChecked(true);
            holder.done.setText("Done!");
        }else{
            holder.done.setChecked(false);
            holder.done.setText("Mark as done");
        }


        if (fromArchive){
            holder.archive.setVisibility(view.GONE);
            holder.done.setVisibility(View.GONE);
        }

        switch (importanceViewColor) {
            case  "2":
                holder.importanceView.setBackgroundColor(context.getColor(R.color.canceRed));
                break;
            case  "1":
                holder.importanceView.setBackgroundColor(context.getColor(R.color.orange));
                break;
            default:
                holder.importanceView.setBackgroundColor(context.getColor(R.color.okGreen));
        }

        if (!expensesCardArrayList.get(position).getAttachedFile().isEmpty()) {
            if(expensesCardArrayList.get(position).getFileType().equals("pdf")){
                holder.cardImage.setImageDrawable(context.getDrawable(R.drawable.ic_pdf_icon));
            }else{
                GlideApp.with(context)
                        .load(expensesCardArrayList.get(position).getAttachedFile())
                        .transform( new CenterCrop() , new RoundedCorners(10))
                        .transition(DrawableTransitionOptions.withCrossFade()) //Here a fading animation
                        .into(holder.cardImage);
                holder.cardImage.setPadding(0,0,0,0);
            }
            holder.noFileAttached.setVisibility(View.GONE);

        }else{
            holder.cardImage.setImageDrawable(context.getDrawable(R.drawable.ic_no_file_added));
            holder.cardImage.setPadding(40,60,40,60);
            holder.noFileAttached.setVisibility(View.VISIBLE);

        }

    }


    @Override
    public int getItemCount() {
        return expensesCardArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(expensesCardArrayList.get(position).getCardID());
    }

    public class ExpensesViewHolder extends RecyclerView.ViewHolder {
        View importanceView;
        TextView title,description,dueDate , noFileAttached;
        private SocialTextView mention;
        ImageView cardImage,shroomieArch;
        ImageButton archive;
        Button yesBtn,noBtn , delete;
        CheckBox done;
        private CardView expensesCardView;

        public ExpensesViewHolder(@NonNull View v) {
            super(v);
            importanceView = v.findViewById(R.id.importance_view);
            title = v.findViewById(R.id.title_card);
            description = v.findViewById(R.id.card_description);
            dueDate = v.findViewById(R.id.dueDate_card);
            cardImage= v.findViewById(R.id.card_img);
            archive = v.findViewById(R.id.archive_card_btn);
            delete = v.findViewById(R.id.delete_button);
            mention = v.findViewById(R.id.task_mention_et);
            mention.setMentionColor(Color.BLUE);
            done = v.findViewById(R.id.task_done);
            noFileAttached = v.findViewById(R.id.no_file_attached);

            expensesCardView=v.findViewById(R.id.my_shroomie_expenses_card);

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteExpensesCard(getAdapterPosition());
                }
            });

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final ExpensesCard selectedCard=expensesCardArrayList.get(getLayoutPosition());
                    if (done.isChecked()){
                        rootRef.child("apartments").child(apartment.getApartmentID()).child("expensesCards").child(expensesCardArrayList.get(getAdapterPosition()).getCardID()).child("done").setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    done.setText("Done!");
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
                                            archive(getLayoutPosition(),selectedCard);
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
                            }
                        });

                    }else{
                        rootRef.child("apartments").child(apartment.getApartmentID()).child("expensesCards").child(expensesCardArrayList.get(getAdapterPosition()).getCardID()).child("done").setValue("false").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });
                    }
                }
            });

            archive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    archive(getAdapterPosition(),expensesCardArrayList.get(getAdapterPosition()));
                }
            });

            expensesCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ViewCards viewCard=new ViewCards();
                    Bundle bundle=new Bundle();
                    bundle.putParcelable("CARD_DETAILS",expensesCardArrayList.get(getAdapterPosition()));
                    bundle.putBoolean("FROM_TASK_TAB",false);
                    viewCard.setArguments(bundle);
                    viewCard.show(fragmentManager,"VIEWCARD");
                }
            });
        }


    }



//    public void archive(final int position,final ExpensesCard expensesCard){
//        DatabaseReference ref = rootRef.child("archive").child(apartment.getApartmentID()).child("expensesCards").push();
//        HashMap<String ,Object> newCard = new HashMap<>();
//        String uniqueID = ref.getKey();
//        newCard.put("description" , expensesCard.getDescription());
//        newCard.put("title" ,expensesCard.getTitle());
//        newCard.put("dueDate", expensesCard.getDueDate());
//        newCard.put("importance", expensesCard.getImportance());
//        newCard.put("date",ServerValue.TIMESTAMP);
//        newCard.put("cardId",uniqueID);
//        newCard.put("attachedFile", expensesCard.getAttachedFile());
//        newCard.put("done", expensesCard.getDone());
//        newCard.put("mention",expensesCard.getMention());
//        ref.updateChildren(newCard).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//
//                rootRef.child("apartments").child(apartment.getApartmentID()).child("expensesCards").child(expensesCard.getCardID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Snackbar.make(parentView,"Card archived", BaseTransientBottomBar.LENGTH_SHORT)
//                                .setAnchorView(R.id.bottomNavigationView)
//                                .show();
//                        notifyItemRemoved(position);
//                    }
//                });
//
//            }
//        });
//
//    }
    public void archive(final int position,final ExpensesCard expensesCard){
        final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
        Map<String, Object> map =
        mapper.convertValue(expensesCard, new TypeReference<Map<String, Object>>() {});
        HashMap data = new HashMap();
        data.put("apartmentID" , apartment.getApartmentID());
        data.put("cardDetails" , map);
        mfunc.getHttpsCallable(Config.FUNCTION_ARCHIVE_EXPENSES_CARD).call(data).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<HttpsCallableResult> task) {
             if(task.isSuccessful()){
                 Snackbar.make(parentView,"Card archived", BaseTransientBottomBar.LENGTH_SHORT)
                         .show();
                 notifyItemRemoved(position);
             }
            }
        });

    }


    public void deleteExpensesCard(final int position){

        HashMap data = new HashMap();
        data.put("apartmentID" , apartment.getApartmentID());
        data.put("cardID" , expensesCardArrayList.get(position).getCardID());
        data.put("file" , expensesCardArrayList.get(position).getAttachedFile());

        mfunc.getHttpsCallable(Config.FUNCTION_DELETE_EXPENSE_CARD).call(data).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<HttpsCallableResult> task) {
                if(task.isSuccessful()){
                    Snackbar snack=Snackbar.make(parentView,"Card deleted", BaseTransientBottomBar.LENGTH_SHORT);
                    snack.show();
                    notifyItemRemoved(position);
                }
            }
        });

    }





}
