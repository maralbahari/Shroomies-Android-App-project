package com.example.shroomies;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;

public class LogFragment extends Fragment {
    private ArrayList<Log> logs;
    private RecyclerView logRecycler;
    private View v;
    private Bundle bundle;
    private LogAdapter logAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       v= inflater.inflate(R.layout.fragment_log, container, false);
       return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logRecycler=v.findViewById(R.id.log_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        logRecycler.setHasFixedSize(true);
        logRecycler.setLayoutManager(linearLayoutManager);
        logs=new ArrayList<>();

        bundle=this.getArguments();
        if(bundle!=null){
            logs= bundle.getParcelableArrayList("LOG_LIST");
            logAdapter=new LogAdapter(getContext(),logs,getParentFragmentManager());
            logRecycler.setAdapter(logAdapter);
            logAdapter.notifyDataSetChanged();


        }
    }
}