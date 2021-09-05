package com.example.shroomies;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.virgilsecurity.android.common.callback.OnGetTokenCallback;
import com.virgilsecurity.android.ethree.interaction.EThree;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity{
    private EditText name, email;
    private ImageButton register;
    CustomLoadingProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        name = findViewById(R.id.fullname);
        email = findViewById(R.id.emailid);
        register = findViewById(R.id.next_button);
        progressBar = new CustomLoadingProgressBar(SignUpActivity.this , "Creating account... " , R.raw.loading_animation);

//        FirebaseDatabase.getInstance().useEmulator("10.0.2.2",9000);
//        mAuth.useEmulator("10.0.2.2",9099);

        register.setOnClickListener(v -> {
            String validEmail = email.getText().toString().trim();

            if (TextUtils.isEmpty(name.getText().toString()) || TextUtils.isEmpty(email.getText().toString())){
                //ensure fields are filled in
                Toast.makeText(SignUpActivity.this, "Please fill in your details",Toast.LENGTH_SHORT).show();
            }
            else {
                //check if email format is valid
                if (emailIsValid(validEmail)) {
                    //if valid, on to setting up password
                    Intent intent = new Intent(getApplicationContext(), SignUpPassword.class);

                    //pass email and username for the database
                    //to sign up password activity
                    intent.putExtra("USERNAME", name.getText().toString().trim());
                    intent.putExtra("EMAIL", email.getText().toString().trim());
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please enter a valid email address", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public static boolean emailIsValid(String email){
        //check email format

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;

        return pat.matcher(email).matches();
    }
}
