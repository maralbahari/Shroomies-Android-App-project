package com.example.shroomies;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.factor.bouncy.BouncyRecyclerView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;


public class LogFragment extends Fragment {
    View v;
    private ArrayList<apartmentLogs> apartmentLogs;
    private ArrayList<String> membersIDs;
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
        apartmentLogs =new ArrayList<>();

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
            apartmentLogs = bundle.getParcelableArrayList("LOG_LIST");
            membersIDs = bundle.getStringArrayList("MEMBERS");
            getMemberDetails(membersIDs);
        }
    }

    private void getMemberDetails(ArrayList<String> members) {
        HashMap data=new HashMap();
        JSONArray jsonArray = new JSONArray(members);
        data.put("membersID",jsonArray);
        FirebaseFunctions mfunc = FirebaseFunctions.getInstance();
        mfunc.useEmulator("10.0.2.2",5001);
        mfunc.getHttpsCallable(Config.FUNCTION_GET_USER_DETAIL).call(data).addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<HttpsCallableResult> task) {
                if(task.isSuccessful()){
                    final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                    ArrayList<HashMap> users = (ArrayList<HashMap>)task.getResult().getData();
                    HashMap<String , User > usersMap = new HashMap();

                    for (HashMap userObject:
                         users) {
                        User user = mapper.convertValue(userObject, User.class);
                        usersMap.put(user.getUserID() , user);
                    }


                    logAdapter=new LogAdapter(getContext(), apartmentLogs , usersMap ,getParentFragmentManager(),getTargetFragment());
                    logRecycler.setAdapter(logAdapter);
                    logAdapter.notifyDataSetChanged();
                }
            }
        });


    }
}



