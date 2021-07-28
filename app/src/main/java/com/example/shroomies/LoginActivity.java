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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.virgilsecurity.android.common.callback.OnGetTokenCallback;
import com.virgilsecurity.android.ethree.interaction.EThree;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    protected EditText username;
    protected EditText password;
    ImageButton login;
    TextView signup;
    private final int RC_SIGN_IN = 7;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    TextView forgotPassword;
    CustomLoadingProgressBar progressBar;
    Button google_sign;
    SessionManager sessionManager;
    public static EThree eThree;
    private String token;
    private DatabaseReference rootRef;


//    @Override
//    protected void onStart() {
//        super.onStart();
//        mAuth.addAuthStateListener(authStateListener);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        mAuth.removeAuthStateListener(authStateListener);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(getApplicationContext());
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase.getInstance().useEmulator("10.0.2.2",9000);
        rootRef=FirebaseDatabase.getInstance().getReference();
        mAuth.useEmulator("10.0.2.2" , 9099);

        setContentView(R.layout.activity_login);
        username=findViewById(R.id.email_login);
        password=findViewById(R.id.password_login);
        login=findViewById(R.id.login_button);
        signup=findViewById(R.id.sign_up_button);
        google_sign = findViewById(R.id.google_sign_up);
        forgotPassword=findViewById(R.id.forgot_password_login);
//        progressBar=findViewById(R.id.progressBar_login);

//        authStateListener = new FirebaseAuth.AuthStateListener() {
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser firebaseUser = mAuth.getCurrentUser();
//                if(firebaseUser!=null && firebaseUser.isEmailVerified()){
//                    String userID =mAuth.getCurrentUser().getUid();
//                    String userEmail =mAuth.getCurrentUser().getEmail();
//                    Toast.makeText(LoginActivity.this, "Welcome to Shroomies! ", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                    intent.putExtra("ID",userID);
//                    intent.putExtra("EMAIL",userEmail);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(intent);
//                }
//            }
//        };


        google_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this,gso);
                signIn();
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplication(),ResetPassword.class);
                startActivity(intent);
                finish();
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplication(),SignUpActivity.class);
                startActivity(intent);

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loginUser();

            }
        });

    }
    private void loginUser(){
        progressBar = new CustomLoadingProgressBar(LoginActivity.this , "Loading..." ,R.raw.loading_animation);
        progressBar.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressBar.show();
        final String uname_txt = username.getText().toString().trim();
        String pw_txt = password.getText().toString().trim();

        if (TextUtils.isEmpty(uname_txt) || TextUtils.isEmpty(pw_txt)){
            Toast.makeText(LoginActivity.this, "Please fill in your details",Toast.LENGTH_SHORT).show();
        }
        else {

            mAuth.signInWithEmailAndPassword(uname_txt, pw_txt). addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){
                        progressBar.dismiss();
                        if(mAuth.getCurrentUser().isEmailVerified()){
                            FirebaseUser user =mAuth.getCurrentUser();
                            String userID =user.getUid();
                            String userEmail =user.getEmail();
//                            sessionManager= new SessionManager(LoginActivity.this,userID);
//                            sessionManager.createSession(userID,userEmail);
//                            sessionManager.setVerifiedEmail(mAuth.getCurrentUser().isEmailVerified());
//                            sessionManager.setVerifiedEmail(true);
                            getE3Token();
                            Toast.makeText(LoginActivity.this, "Welcome to Shroomies! ", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("ID",userID);
                            intent.putExtra("EMAIL",userEmail);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                        if(!(mAuth.getCurrentUser().isEmailVerified())){
                            username.setText("");
                            password.setText("");
                            progressBar.dismiss();
                            Toast.makeText(LoginActivity.this, "Please verify your email address", Toast.LENGTH_SHORT).show();
                            final FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification().addOnCompleteListener(LoginActivity.this, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    login.setEnabled(true);
                                    if(task.isSuccessful()){
                                        Toast.makeText(LoginActivity.this, "a new verification email has been sent to"+user.getEmail(), Toast.LENGTH_SHORT).show();
                                        progressBar.dismiss();
                                    }
                                    else{
                                        Toast.makeText(LoginActivity.this, "failed to send email verification", Toast.LENGTH_SHORT).show();
                                        progressBar.dismiss();
                                    }
                                }
                            });
                        }

                    }else{
                        Toast.makeText(LoginActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        progressBar.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(LoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                        progressBar.dismiss();
                    } else if (e instanceof FirebaseAuthInvalidUserException) {

                        String errorCode =
                                ((FirebaseAuthInvalidUserException) e).getErrorCode();

                        if (errorCode.equals("ERROR_USER_NOT_FOUND")) {
                            Toast.makeText(LoginActivity.this, "The email does not belong to a registered account. Please proceed to Sign Up.", Toast.LENGTH_SHORT).show();
                            progressBar.dismiss();
                        } else if (errorCode.equals("ERROR_USER_DISABLED")) {
                            Toast.makeText(LoginActivity.this, "User account has been disabled", Toast.LENGTH_SHORT).show();
                            progressBar.dismiss();
                        } else {
                            Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.dismiss();
                        }

                    }
                }
            });
        }
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

        }else {
            Toast.makeText(LoginActivity.this, "authentication failed, try again later"+requestCode, Toast.LENGTH_SHORT).show();
        }
    }
    private void firebaseAuthWithGoogle(String tokenID) {
        AuthCredential credential = GoogleAuthProvider.getCredential(tokenID, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    if(task.getResult().getAdditionalUserInfo().isNewUser()){
                        addNewUserTodatabase(user,task);
                    }else{
                        Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("ID",user.getUid());
                        intent.putExtra("EMAIL",user.getEmail());
                        startActivity(intent);
                        Toast.makeText(LoginActivity.this, "signed in successfully", Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void addNewUserTodatabase(FirebaseUser user, Task<AuthResult> task){
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
        rootRef.child("users").child(user.getUid()).setValue(userDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                apartmentRef.updateChildren(apartmentDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(LoginActivity.this, "signed in successfully", Toast.LENGTH_SHORT).show();
                        Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("ID",user.getUid());
                        intent.putExtra("EMAIL",user.getEmail());
                        startActivity(intent);
                    }
                });
            }
        });
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