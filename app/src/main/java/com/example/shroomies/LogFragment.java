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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.factor.bouncy.BouncyRecyclerView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class LogFragment extends Fragment {
    private View v;
    private ArrayList<apartmentLogs> apartmentLogs;
    private ArrayList<String> membersIDs;
    private BouncyRecyclerView logRecycler;
    private Bundle bundle;
    private LogAdapter logAdapter;
    private RequestQueue requestQueue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requestQueue = Volley.newRequestQueue(getActivity());
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
        JSONObject jsonObject=new JSONObject();
        JSONObject data = new JSONObject();
        JSONArray jsonArray = new JSONArray(members);

        try {
            jsonObject.put("membersID",jsonArray);
            data.put("data" , jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
//
//        FirebaseFunctions mfunc = FirebaseFunctions.getInstance();
//        mfunc.useEmulator("10.0.2.2",5001);
        HashMap<String , User > usersMap = new HashMap();
        JsonObjectRequest jsonObjectRequest= new JsonObjectRequest(Request.Method.POST, Config.FUNCTION_GET_MEMBER_DETAIL, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                JSONArray users = null;
                try {
                    users = (JSONArray) response.get("result");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(users!=null) {
                    for (int i = 0; i < users.length(); i++) {
                        User user = null;
                        try {
                            user = mapper.readValue(((JSONObject) users.get(i)).toString(), User.class);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }

                        usersMap.put(user.getUserID(), user);
                        Log.d( "logs user" , usersMap.toString());
                    }

                    logAdapter = new LogAdapter(getContext(), apartmentLogs, usersMap, getParentFragmentManager(), getTargetFragment());
                    logRecycler.setAdapter(logAdapter);
                    logAdapter.notifyDataSetChanged();
                }
            }
        }, error -> {
            //todo handle error
        });
        requestQueue.add(jsonObjectRequest);

    }
}



