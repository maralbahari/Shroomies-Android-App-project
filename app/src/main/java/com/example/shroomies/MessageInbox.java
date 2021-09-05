package com.example.shroomies;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;


public class MessageInbox extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView inboxListRecyclerView;
    private ArrayList<RecieverInbox> recieverInboxArrayList;
    private LinearLayoutManager linearLayoutManager;
    private PrivateInboxRecycleViewAdapter messageInboxRecycleViewAdapter;
    private DatabaseReference rootRef;
    private FirebaseAuth mAuth;
    private HashMap<String , User>userHashMap;
    private HashSet<String> inboxUserIDs;
    private RequestQueue requestQueue;
    private ValueEventListener valueEventListener;
    private ImageButton newChatButton;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(rootRef!=null && valueEventListener!=null){
            rootRef.removeEventListener(valueEventListener);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_message_inbox);
        rootRef= FirebaseDatabase.getInstance().getReference();
        requestQueue = Volley.newRequestQueue(getApplication());
        mAuth = FirebaseAuth.getInstance();

        inboxListRecyclerView = findViewById(R.id.inbox_recycler);
        newChatButton = findViewById(R.id.find_user_button);
        toolbar = findViewById(R.id.message_inbox_tool_bar);

        linearLayoutManager=new LinearLayoutManager(getApplication());
        inboxListRecyclerView.setHasFixedSize(true);
        inboxListRecyclerView.setLayoutManager(linearLayoutManager);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        OverScrollDecoratorHelper.setUpOverScroll(inboxListRecyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        getPrivateChatList();
        newChatButton.setOnClickListener(v -> {
            NewChatFragment  newChatFragment  =  new NewChatFragment();
            newChatFragment.show(getSupportFragmentManager() ,null);
        });

    }

    public void getPrivateChatList(){
        userHashMap = new HashMap<>();

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    inboxUserIDs = new HashSet();
                    recieverInboxArrayList = new ArrayList<>();
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        RecieverInbox recieverInbox = ds.getValue(RecieverInbox.class);
                        recieverInboxArrayList.add(recieverInbox);

                        if(!userHashMap.containsKey(recieverInbox.getReceiverID())) {
                            inboxUserIDs.add(recieverInbox.getReceiverID());
                        }
                    }
                    sortAcoordingToDate(recieverInboxArrayList);
                    if(messageInboxRecycleViewAdapter!=null){
                        messageInboxRecycleViewAdapter.updateInbox(recieverInboxArrayList);
                    }else{
                        messageInboxRecycleViewAdapter=new PrivateInboxRecycleViewAdapter(recieverInboxArrayList,null,MessageInbox.this , getApplicationContext());
                        messageInboxRecycleViewAdapter.setHasStableIds(true);
                        inboxListRecyclerView.setAdapter(messageInboxRecycleViewAdapter);
                    }


                    getMemberData(inboxUserIDs , recieverInboxArrayList);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        rootRef.child(Config.inboxes)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .orderByChild(Config.receiverID)
                .addValueEventListener(valueEventListener);

    }

    private void sortAcoordingToDate(ArrayList<RecieverInbox> recieverInboxArrayList) {
        recieverInboxArrayList.sort((o1, o2) -> {
            Instant o1Instant = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(o1.getLastMessageTime(), Instant::from);
            Instant o2Instant = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(o2.getLastMessageTime(), Instant::from);
            return o2Instant.compareTo(o1Instant);
        });
    }

    void getMemberData(HashSet inboxIDs , ArrayList<RecieverInbox> recieverInboxArrayList){
        if(inboxIDs!=null) {
            FirebaseUser firebaseUser = mAuth
                    .getCurrentUser();
            firebaseUser.getIdToken(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String token= task.getResult().getToken();
                    JSONObject jsonObject = new JSONObject();
                    JSONObject data = new JSONObject();
                    JSONArray usersJSONArray = new JSONArray(inboxIDs);
                    try {
                        jsonObject.put(Config.membersID , usersJSONArray);
                        data.put(Config.data ,  jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JsonObjectRequest jsonObjectRequest  = new JsonObjectRequest(Request.Method.POST, Config.URL_GET_MEMBER_DETAIL, data, response -> {
                        try {
                            JSONObject result = response.getJSONObject(Config.result);
                            boolean success = result.getBoolean(Config.success);
                            Log.d("members",result.toString());
                            if(success){
                                final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                                mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                                JSONArray users1 = result.getJSONArray(Config.members);

                                for (int i = 0; i < users1.length(); i++) {
                                    User user = mapper.readValue(users1.get(i).toString(), User.class);
                                    userHashMap.put(user.getUserID(), user);
                                }
                                messageInboxRecycleViewAdapter.setUserHashMap(userHashMap);
                                messageInboxRecycleViewAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException | JsonProcessingException e) {
                            e.printStackTrace();
                        }

                    }, error -> {
                        //todo handle the error
                    }) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<>();
                            params.put(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
                            params.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                            return params;
                        }
                    };
                    requestQueue.add(jsonObjectRequest);
                }
            });
        }

    }


}