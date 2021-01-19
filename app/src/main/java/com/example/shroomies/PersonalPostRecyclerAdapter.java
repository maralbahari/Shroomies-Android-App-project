package com.example.shroomies;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PersonalPostRecyclerAdapter extends RecyclerView.Adapter<PersonalPostRecyclerAdapter.PersonalPostViewHolder> {
    List <PersonalPostModel> personalPostModelList;

    public PersonalPostRecyclerAdapter(List<PersonalPostModel> personalPostModelList) {
        this.personalPostModelList = personalPostModelList;
    }

    @NonNull
    @Override
    public PersonalPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PersonalPostViewHolder();
    }

    @Override
    public void onBindViewHolder(@NonNull PersonalPostViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class PersonalPostViewHolder extends RecyclerView.ViewHolder{


        public PersonalPostViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
