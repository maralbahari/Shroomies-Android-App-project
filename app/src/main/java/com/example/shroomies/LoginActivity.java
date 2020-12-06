package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricConstants;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {
   protected EditText username;
   protected EditText password;
    Button login;
    TextView signup;
    private final int RC_SIGN_IN = 7;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    TextView forgotPassword;
    boolean successBiometric = false;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    Button google_sign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username=findViewById(R.id.email_login);
        password=findViewById(R.id.password_login);
        login=findViewById(R.id.login_button);
        signup=findViewById(R.id.sign_up_button);
        google_sign = findViewById(R.id.google_sign_up);
        forgotPassword=findViewById(R.id.forgot_password_login);
//        username.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
////                if(chekEnabledBiometric(s.toString())){
//
////                    password.setOnTouchListener(new View.OnTouchListener() {
////                        @Override
////                        public boolean onTouch(View v, MotionEvent event) {
////                            setBiometric();
////                            promptInfo = new BiometricPrompt.PromptInfo.Builder()
////                                    .setTitle("Biometric login for my app")
////                                    .setSubtitle("Log in using your biometric credential")
////                                    .setNegativeButtonText("Use account password")
////                                    .build();
////                            biometricPrompt.authenticate(promptInfo);
////                            return false;
////                        }
////                    });
////                }
//            }
//        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        google_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        mAuth = FirebaseAuth.getInstance();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplication(),SignUpActivity.class);
                startActivity(intent);
                finish();

            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             loginUser(successBiometric);

            }
        });

    }
    private void loginUser(final boolean biometricSuccessful){
        final String uname_txt = username.getText().toString();
        String pw_txt = password.getText().toString();

        if (TextUtils.isEmpty(uname_txt) || TextUtils.isEmpty(pw_txt)){
            Toast.makeText(LoginActivity.this, "Please fill in your details",Toast.LENGTH_SHORT).show();
        }
        else {
//            if(biometricSuccessful){
//                sessionManager=new SessionManager(getApplicationContext(),uname_txt);
//                // get name of the user
//                sessionManager.createSession(mAuth.getCurrentUser().getDisplayName().toString(),uname_txt);
//                Toast.makeText(LoginActivity.this, "Welcome to Shroomies!", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
//                finish();
//            }

            mAuth.signInWithEmailAndPassword(uname_txt, pw_txt). addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                               FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(LoginActivity.this, "Welcome to Shroomies! ", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(LoginActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                    } else if (e instanceof FirebaseAuthInvalidUserException) {

                        String errorCode =
                                ((FirebaseAuthInvalidUserException) e).getErrorCode();

                        if (errorCode.equals("ERROR_USER_NOT_FOUND")) {
                            Toast.makeText(LoginActivity.this, "The email does not belong to a registered account. Please proceed to Sign Up.", Toast.LENGTH_SHORT).show();
                        } else if (errorCode.equals("ERROR_USER_DISABLED")) {
                            Toast.makeText(LoginActivity.this, "User account has been disabled", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            });
        }

        }
    boolean setBiometric(){
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricConstants.ERROR_USER_CANCELED){
                    password.setOnTouchListener(null);
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Authentication error: " + errString, Toast.LENGTH_SHORT)
                            .show();
                }
                successBiometric = false;
            }
            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                successBiometric=true;
                loginUser(successBiometric);
            }
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
                successBiometric=false;
            }
        });

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        return successBiometric;
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("Login","firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = mAuth.getCurrentUser();
                    if(task.getResult().getAdditionalUserInfo().isNewUser()){
                        DatabaseReference mRootref = FirebaseDatabase.getInstance().getReference("Users");
                        HashMap<String, Object> userDetails = new HashMap<>();
                        userDetails.put("name","" );
                        userDetails.put("email", user.getEmail());
                        userDetails.put("ID", mAuth.getCurrentUser().getUid());
                        userDetails.put("biometricEnabled","False");
                        userDetails.put("image",""); //add later in edit profile
                        userDetails.put("isPartOfRoom","false");// change later
                        mRootref.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(userDetails);
                    }
                    Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(LoginActivity.this, "User Signed In", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.w("Login", "signInWithCredentail:Failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public boolean chekEnabledBiometric(String email){
        FirebaseUser user= mAuth.getCurrentUser();
        String userID="";
        if(user!=null){
            if(user.getEmail()==email){
                userID=user.getUid();
                Toast.makeText(getApplicationContext(),userID,Toast.LENGTH_SHORT).show();
            }
        }
        if(userID!=null){
            Query query= FirebaseDatabase.getInstance().getReference("Users").child(userID);
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                       if(dataSnapshot.hasChild("biometricEnabled")){
                           String trying= dataSnapshot.child("biometricEnabled").getValue().toString();
                           Toast.makeText(getApplicationContext(),trying,Toast.LENGTH_LONG).show();
                       }
                    }
//                        successBiometric= (boolean) snapshot.child("biometricEnabled").getValue();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        return successBiometric;
    }
}



