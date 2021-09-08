package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;


public class LoginActivity extends AppCompatActivity {
    protected TextInputLayout emailEditText;
    private final int RC_SIGN_IN = 7;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(getApplicationContext());
        //google sign in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(LoginActivity.this,gso);

        mAuth = FirebaseAuth.getInstance();
        rootRef=FirebaseDatabase.getInstance().getReference();
        emailEditText =findViewById(R.id.email_login);
        //    protected EditText password;
        ImageButton login = findViewById(R.id.login_button);
        MaterialButton signup = findViewById(R.id.sign_up_button);
        //    TextView forgotPassword;
        ImageButton googleSignInButton = findViewById(R.id.google_sign_up);
        googleSignInButton.setOnClickListener(v -> {

            signIn();
        });
        signup.setOnClickListener(v -> {
            Intent intent=new Intent(getApplication(),SignUpActivity.class);
            startActivity(intent);

        });
        login.setOnClickListener(view -> {
            String enteredEmail=emailEditText.getEditText().getText().toString().trim();
            if(!validEmail(enteredEmail)){
                emailEditText.setError("Please enter a valid email");
            }else {
                emailEditText.setError(null);
                Intent intent = new Intent(this,LoginPassword.class);
                intent.putExtra("EMAIL",enteredEmail);
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
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account.getIdToken());

                } catch (ApiException e) {
                    Toast.makeText(LoginActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }

        }else {
            Toast.makeText(LoginActivity.this, "authentication failed, try again later"+requestCode, Toast.LENGTH_SHORT).show();
        }
    }
    private void firebaseAuthWithGoogle(String tokenID) {
        AuthCredential credential = GoogleAuthProvider.getCredential(tokenID, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()){
                FirebaseUser user = mAuth.getCurrentUser();
                if (user!=null) {
                    if(task.getResult().getAdditionalUserInfo().isNewUser()) {
                        Log.d("GOOGLE SIGN IN","new user");
                        startActivity(new Intent(getApplicationContext() , AddUsername.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                    }else{
                        Intent intent= new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        Toast.makeText(LoginActivity.this, "signed in successfully", Toast.LENGTH_SHORT).show();
                        Log.d("GOOGLE SIGN IN","not new user");
                    }
                }
            }
        });
    }

    private boolean validEmail(String enteredEmail){
        return android.util.Patterns.EMAIL_ADDRESS.matcher(enteredEmail).matches();
    }

}