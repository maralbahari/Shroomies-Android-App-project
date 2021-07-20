package com.example.shroomies;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.factor.bouncy.BouncyRecyclerView;
import java.util.ArrayList;


public class LogFragment extends Fragment {
    View v;
    private ArrayList<Log> logs;
    private BouncyRecyclerView logRecycler;
    private Bundle bundle;
    private LogAdapter logAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_log, container, false);
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

        Toolbar toolbar =getActivity().findViewById(R.id.my_shroomies_toolbar);
        toolbar.setTitle("Logs");
        toolbar.setTitleTextColor(getActivity().getColor(R.color.jetBlack));
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setElevation(5);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar.setTitle(null);
                toolbar.setNavigationIcon(null);
                getActivity().onBackPressed();
            }
        });


        bundle=this.getArguments();
        if(bundle!=null){
            logs= bundle.getParcelableArrayList("LOG_LIST");

            logAdapter=new LogAdapter(getContext(),logs,getParentFragmentManager(),getTargetFragment());
            logRecycler.setAdapter(logAdapter);
            logAdapter.notifyDataSetChanged();
        }
    }
}



