package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.virgilsecurity.android.common.callback.OnGetTokenCallback;
import com.virgilsecurity.android.ethree.interaction.EThree;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    //views
    protected EditText userName;
    ImageButton nextButton;
    private String token;
    TextView signUp;
    CustomLoadingProgressBar progressBar;

    //Google
    private final int RC_SIGN_IN = 7;
    GoogleSignInClient mGoogleSignInClient;
    Button google_sign;

    //firebase
    FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    public static EThree eThree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseApp.initializeApp(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        rootRef=FirebaseDatabase.getInstance().getReference();

//        FirebaseDatabase.getInstance().useEmulator("10.0.2.2",9000);
//        mAuth.useEmulator("10.0.2.2" , 9099);

        userName =findViewById(R.id.email_login);
        nextButton =findViewById(R.id.next_button);
        signUp =findViewById(R.id.signup_button);
        google_sign = findViewById(R.id.google_sign_up);
        progressBar = new CustomLoadingProgressBar(LoginActivity.this , "Loading..." , R.raw.loading_animation);

        google_sign.setOnClickListener(v -> {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this,gso);
            signIn();
        });

        signUp.setOnClickListener(v -> {
            // to sign up activity
            Intent intent=new Intent(getApplication(),SignUpActivity.class);
            startActivity(intent);

        });

        nextButton.setOnClickListener(v -> {
            //to login password activity

            String email = userName.getText().toString().trim();

            //check fields are filled
            if (TextUtils.isEmpty(email)){
                Toast.makeText(LoginActivity.this, "Please fill in your details",Toast.LENGTH_SHORT).show();
            }
            else {
                Intent intent = new Intent(getApplication(), LoginPassword.class);
                intent.putExtra("EMAIL", email);
                startActivity(intent);
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            if (task.isSuccessful()) {
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());

                } catch (ApiException e) {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
//            } else {
//                Toast.makeText(LoginActivity.this, "authentication failed, try again later", Toast.LENGTH_SHORT).show();
//            }

        }
        else {
            Toast.makeText(LoginActivity.this, "Authentication failed, try again later" + requestCode, Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String tokenID) {
        //Google authorisation

        AuthCredential credential = GoogleAuthProvider.getCredential(tokenID, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    if(task.getResult().getAdditionalUserInfo().isNewUser()){
                        addNewUserTodatabase(user,task);
                    }
                    else {
                        Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("ID",user.getUid());
                        intent.putExtra("EMAIL",user.getEmail());
                        startActivity(intent);
                        Toast.makeText(LoginActivity.this, "Signed in successfully", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addNewUserTodatabase(FirebaseUser user, Task<AuthResult> task){
        //adding Google user to database

        final DatabaseReference apartmentRef = rootRef.child("apartments").push();
        String apartmentID = apartmentRef.getKey();
        HashMap<String, Object> userDetails = new HashMap<>();
        userDetails.put("name",task.getResult().getAdditionalUserInfo().getUsername());
        userDetails.put("email", user.getEmail());
        userDetails.put("userID", user.getUid());
        userDetails.put("image",""); //add later in edit profile
        userDetails.put("apartmentID", user.getUid());// change later
        HashMap<String, Object> apartmentDetails = new HashMap<>();
        apartmentDetails.put("apartmentID", apartmentID);
        apartmentDetails.put("adminID", user.getUid());

        rootRef.child("users").child(user.getUid()).setValue(userDetails).addOnSuccessListener(aVoid -> apartmentRef.updateChildren(apartmentDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(LoginActivity.this, "Signed in successfully", Toast.LENGTH_SHORT).show();
                Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("ID",user.getUid());
                intent.putExtra("EMAIL",user.getEmail());
                startActivity(intent);
            }
        }));
    }

    private void getE3Token() {
        com.virgilsecurity.common.callback.OnCompleteListener registerListener = new com.virgilsecurity.common.callback.OnCompleteListener() {
            @Override
            public void onSuccess() {
                Log.d("ethree register ", "success");

            }
            @Override
            public void onError(@NotNull Throwable throwable) {
                Log.d("ethree register ", throwable.toString());
            }
        };
        /*Map<String, String> data*/
        FirebaseFunctions.getInstance()
                .getHttpsCallable("getVirgilJwt")
                .call()
                .continueWith(new Continuation<HttpsCallableResult, Object>() {
                    @Override
                    public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        token = ((Map<String, String>) task.getResult().getData()).get("token");
                        eThree = new EThree(mAuth.getCurrentUser().getUid()
                                , (OnGetTokenCallback) () -> getToken()
                                , getApplicationContext());
                        return null;

                    }
                });
    }

    public String getToken(){
        return token;
    }

}