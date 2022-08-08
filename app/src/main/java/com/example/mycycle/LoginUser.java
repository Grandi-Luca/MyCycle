package com.example.mycycle;

import static com.example.mycycle.Utils.isUserLogin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginUser extends AppCompatActivity {

    private EditText editPassword, editEmail;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_user);

//        move to home if the user is already done the sign in
        if(isUserLogin()){
            Intent intent = new Intent(LoginUser.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        initActivity();
    }

    private void initActivity() {
        this.editEmail = findViewById(R.id.email);
        this.editPassword = findViewById(R.id.password);

        this.mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.register_now).setOnClickListener(v -> startActivity(new Intent(LoginUser.this, RegisterUser.class)));

        findViewById(R.id.login).setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = this.editEmail.getText().toString().trim();
        String password = this.editPassword.getText().toString().trim();

        if(this.isEmpty(email, editEmail, "email")
                || this.isEmpty(password, this.editPassword, "password")){
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            this.editEmail.setError("Please provide valid email");
            this.editEmail.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(authResultTask -> {
            if(authResultTask.isSuccessful()){
                Toast.makeText(LoginUser.this, "User has been login successfully", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginUser.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                Toast.makeText(LoginUser.this, "Failed to login! Try again", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isEmpty(String str, EditText editText, String nameField){
        if(str.isEmpty()){
            editText.setError(nameField + "is required");
            editText.requestFocus();
            return true;
        }
        return false;
    }
}