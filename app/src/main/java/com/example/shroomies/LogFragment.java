package com.example.shroomies;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.appcompat.widget.Toolbar;

import com.factor.bouncy.BouncyRecyclerView;

import java.util.ArrayList;


public class LogFragment extends Fragment {
    private ArrayList<Log> logs;
    private BouncyRecyclerView logRecycler;
    private View v;
    private Bundle bundle;
    private LogAdapter logAdapter;
    private ImageButton back;
    private FragmentTransaction ft;
    private FragmentManager fm;


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
//       back.setOnClickListener(new View.OnClickListener() {
//           @Override
//           public void onClick(View view) {
//               fm=getParentFragmentManager();
//               ft = fm.beginTransaction();
//               ft.replace(R.id.fragmentContainer, new MyShroomies());
//               ft.disallowAddToBackStack();
//               ft.commit();
//           }
//       });
        bundle=this.getArguments();
        if(bundle!=null){
            logs= bundle.getParcelableArrayList("LOG_LIST");

            logAdapter=new LogAdapter(getContext(),logs,getParentFragmentManager(),getTargetFragment());
            logRecycler.setAdapter(logAdapter);
            logAdapter.notifyDataSetChanged();


        }
    }


}