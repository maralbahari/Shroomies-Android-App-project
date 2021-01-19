package com.example.shroomies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PersonalPostRecyclerAdapter extends RecyclerView.Adapter<PersonalPostRecyclerAdapter.PersonalPostViewHolder> {
    List <PersonalPostModel> personalPostModelList;
    Context context;

    public PersonalPostRecyclerAdapter(List<PersonalPostModel> personalPostModelList, Context context) {
        this.personalPostModelList = personalPostModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public PersonalPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PersonalPostViewHolder(LayoutInflater.from(context).inflate
                (R.layout.personal_post_custom_card, parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull PersonalPostViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() { return personalPostModelList.size(); }



    class PersonalPostViewHolder extends RecyclerView.ViewHolder{


        public PersonalPostViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
