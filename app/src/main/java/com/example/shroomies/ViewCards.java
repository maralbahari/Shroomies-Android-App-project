package com.example.shroomies;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.widget.SocialTextView;

import java.util.ArrayList;
import java.util.HashMap;


public class ViewCards extends DialogFragment {
    private View v;
    private TasksCard tasksCard;
    private ExpensesCard expensesCard;
    private boolean taskCardSelected;
    private TextView title,dueDate,description;
    private ImageView attachedFile;
    private RecyclerView sharedAmounts;
    private SocialTextView mention;
    private View importanceView;
    private RecyclerView viewCardRecycler;
    private Bundle bundle;
    private ArrayList<User> shroomiesList;
    private UserAdapterSplitExpenses splitAdapter;
    private DatabaseReference rootRef;
    private Button close;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_view_cards, container, false);
        rootRef= FirebaseDatabase.getInstance().getReference();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title=v.findViewById(R.id.view_card_title);
        dueDate=v.findViewById(R.id.view_card_date);
        description=v.findViewById(R.id.view_card_description);
        mention=v.findViewById(R.id.view_card_mention);
        attachedFile=v.findViewById(R.id.view_card_image);
        importanceView=v.findViewById(R.id.view_card_importance);
        close=v.findViewById(R.id.close_view_card);
        viewCardRecycler=v.findViewById(R.id.view_card_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        viewCardRecycler.setHasFixedSize(true);
        viewCardRecycler.setLayoutManager(linearLayoutManager);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        bundle=this.getArguments();
        if(bundle!=null){
            taskCardSelected=bundle.getBoolean("FROM_TASK_TAB");
            if(taskCardSelected){
                tasksCard=bundle.getParcelable("CARD_DETAILS");
                viewCardRecycler.setVisibility(View.GONE);
                attachedFile.setVisibility(View.GONE);
                title.setText(tasksCard.getTitle());
                String due= tasksCard.getDueDate();
                String descriptionCard=tasksCard.getDescription();
                String mentions=tasksCard.getMention();
                String importance=tasksCard.getImportance();
                if(!due.isEmpty() ){
                    dueDate.setText(due);
                }if(!descriptionCard.isEmpty()){
                    description.setText(descriptionCard);
                }if(!mentions.isEmpty()){
                    mention.setText(mentions);
                }
                if(!importance.isEmpty() ){
                    switch (importance) {
                        case "0":
                            importanceView.setBackgroundColor(Color.GREEN);
                            close.setBackgroundColor(Color.GREEN);
                            break;
                        case "2":
                            importanceView.setBackgroundColor(Color.RED);
                            close.setBackgroundColor(Color.RED);
                            break;
                        case "1":
                            importanceView.setBackgroundColor(Color.parseColor("#F59C34"));
                            close.setBackgroundColor(Color.parseColor("#F59C34"));
                            break;
                        default:
                            importanceView.setBackgroundColor(Color.parseColor("#F5CB5C"));
                            close.setBackgroundColor(Color.parseColor("#F5CB5C"));
                    }

                }
            }else{
                expensesCard=bundle.getParcelable("CARD_DETAILS");
                title.setText(expensesCard.getTitle());
                String due= expensesCard.getDueDate();
                String descriptionCard=expensesCard.getDescription();
                String mentions=expensesCard.getMention();
                String importance=expensesCard.getImportance();
                String imagePath=expensesCard.getAttachedFile();
                HashMap <String,Object> membersShares=expensesCard.getMembersShares();
                if(!due.isEmpty() ){
                    dueDate.setText(due);
                }if(!descriptionCard.isEmpty()){
                    description.setText(descriptionCard);
                }if(!mentions.isEmpty()){
                    mention.setText(mentions);
                }
                if(!importance.isEmpty() ){
                    switch (importance) {
                        case "0":
                            importanceView.setBackgroundColor(Color.GREEN);
                            close.setBackgroundColor(Color.GREEN);
                            break;
                        case "2":
                            importanceView.setBackgroundColor(Color.RED);
                            close.setBackgroundColor(Color.RED);
                            break;
                        case "1":
                            importanceView.setBackgroundColor(Color.parseColor("#F59C34"));
                            close.setBackgroundColor(Color.parseColor("#F59C34"));
                            break;
                        default:
                            importanceView.setBackgroundColor(Color.parseColor("#F5CB5C"));
                            close.setBackgroundColor(Color.parseColor("#F5CB5C"));
                    }

                }
                if(!imagePath.isEmpty()){
                    GlideApp.with(getContext())
                            .load(imagePath)
                            .transform( new CircleCrop())
                            .into(attachedFile);
                }else if(imagePath.isEmpty()){
                    attachedFile.setVisibility(View.GONE);
                }
                if(!membersShares.isEmpty()){
                    getMembersShares(membersShares);
                }else if(membersShares.isEmpty()){
                    viewCardRecycler.setVisibility(View.GONE);
                }
            }
        }


    }

    private void getMembersShares(final HashMap<String, Object> membersShares) {
        shroomiesList=new ArrayList<>();
        splitAdapter= new UserAdapterSplitExpenses(shroomiesList,getContext(),true);
        viewCardRecycler.setAdapter(splitAdapter);
        for (final String id: membersShares.keySet()){
            rootRef.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        User user = snapshot.getValue(User.class);
                        user.setSharedAmount(Integer.valueOf(membersShares.get(id).toString()));
                        shroomiesList.add(user);

                    }
                    splitAdapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }
}