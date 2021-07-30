package com.example.shroomies;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.factor.bouncy.BouncyRecyclerView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AddShroomieMember extends DialogFragment {
    //views
    private View v;
    private SearchView memberSearchView;
    private BouncyRecyclerView addShroomieRecycler;
    private ImageButton closeButton;
    private TextView infoTextView;
    private TextView recommendedUsers;
    //firebase
    private FirebaseAuth mAuth;
    private RequestQueue requestQueue;
    //data
    private UserAdapter userRecyclerAdapter;
    private ArrayList<User> suggestedUser;
    private ArrayList<String> inboxUser;
    private ShroomiesApartment apartment;


    //TODO add users from the  inbox


    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null) {
            getDialog().getWindow().setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setBackgroundDrawableResource(R.drawable.create_group_fragment_background);
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            getDialog().getWindow().setGravity(Gravity.BOTTOM);
        }
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v =inflater.inflate(R.layout.add_shroomies, container, false);
        mAuth=FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(getActivity());
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        memberSearchView = v.findViewById(R.id.search_member);
        addShroomieRecycler = v.findViewById(R.id.add_member_recyclerview);
        closeButton = v.findViewById(R.id.close_button_add_shroomie);
        infoTextView = v.findViewById(R.id.information_text_view);
        recommendedUsers = v.findViewById(R.id.recommended_text_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        addShroomieRecycler.setHasFixedSize(true);
        addShroomieRecycler.setLayoutManager(linearLayoutManager);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        if (getArguments()!=null){
            Bundle bundle = getArguments();
            apartment=bundle.getParcelable("APARTMENT_DETAILS");

        }
//        getMessageInboxListIntoAdapter();

        memberSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(suggestedUser!=null){
                    suggestedUser.clear();
                }
                memberSearchView.clearFocus();
                searchUsers(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

    }
    void searchUsers(String query){
        if(!query.trim().isEmpty()){
            FirebaseUser firebaseUser = mAuth
                    .getCurrentUser();
            firebaseUser.getIdToken(false)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                @Override
                public void onComplete(@NonNull Task<GetTokenResult> task) {
                    if (task.isSuccessful()){
                        String token = task.getResult().getToken();
                        suggestedUser = new ArrayList<>();
                        userRecyclerAdapter= new UserAdapter(suggestedUser,getContext(),true,apartment);
                        userRecyclerAdapter.setHasStableIds(true);
                        addShroomieRecycler.setAdapter(userRecyclerAdapter);

                        JSONObject jsonObject = new JSONObject();
                        JSONObject data =  new JSONObject();
                        try {
                            jsonObject.put(Config.name, query.trim());
                            jsonObject.put(Config.currentUser, mAuth.getCurrentUser().getUid());
                            data.put(Config.data , jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Config.FUNCTION_SEARCH_USERS, data, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                JSONArray result = null;
                                try {
                                    result = (JSONArray) response.get(Config.result);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if(result!=null){
                                    //the result is a jsonn array of json arrays
                                    //the nested json array contains 2 indeces
                                    // first index is the user object
                                    // and the second  index is a boolean value
                                    //indacting wether the current user has already
                                    // sent a request to the searched user
                                    final ObjectMapper mapper = new ObjectMapper(); // jackson's objectmapper
                                    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                                    for(int i= 0 ;i< result.length();i++){
                                        try {
                                            JSONArray jsonArray =((JSONArray)(result.get(i)));
                                            //cast the first index as a json object and the second as a boolean value
                                            JSONObject userJsonObject = (JSONObject) jsonArray.get(0);
                                            boolean requestSent = (boolean)jsonArray.get(1);
                                            User user =mapper.readValue(userJsonObject.toString() , User.class);
                                            user.setRequestSent(requestSent);

                                            if(apartment.getApartmentMembers()!=null){
                                                if(!apartment.getApartmentMembers().containsKey(user.getUserID())){
                                                    suggestedUser.add(user);
                                                }
                                            }else{
                                                suggestedUser.add(user);
                                            }

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (JsonMappingException e) {
                                            e.printStackTrace();
                                        } catch (JsonProcessingException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    userRecyclerAdapter.notifyDataSetChanged();
                                    recommendedUsers.setVisibility(View.GONE);

                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String message = null; // error message, show it in toast or dialog, whatever you want
                                if (error instanceof NetworkError || error instanceof AuthFailureError || error instanceof NoConnectionError || error instanceof TimeoutError) {
                                    message = "Cannot connect to Internet";
                                } else if (error instanceof ServerError) {
                                    message = "The server could not be found. Please try again later";
                                }  else if (error instanceof ParseError) {
                                    message = "Parsing error! Please try again later";
                                }
                                displayErrorAlert("Error" ,message);
                            }
                        });
                        requestQueue.add(jsonObjectRequest);
                    }else{
                        String title = "Authentication error";
                        String message = "We encountered a problem while authenticating your account";
                        displayErrorAlert(title, message);
                    }
                }
            });

        }
    }
    void displayErrorAlert(String title ,  String message ){
        new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_alert)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("return", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddShroomieMember.this.getActivity().onBackPressed();
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }


}
