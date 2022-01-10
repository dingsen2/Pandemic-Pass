package edu.illinois.cs465.pandemicpass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/*
* referenced login screen tutorial: https://www.youtube.com/watch?v=KB2BIm_m1Os
* */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView register;
    private TextView forgotPassword;
    private Button login;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = (EditText) findViewById(R.id.email);
        passwordEditText = (EditText) findViewById(R.id.password);

        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(this);

        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(this);

        register = (TextView) findViewById(R.id.register);
        register.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.login) {
            login();
        }
        else if (id == R.id.register) {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        }
        else if (id == R.id.forgotPassword) {
            startActivity(new Intent(MainActivity.this, ForgotPasswordActivity.class));

        }
    }

    private void login() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            return;
        }

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty()) {
            emailEditText.setError("Email is required.");
            emailEditText.requestFocus();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email is not valid.");
            emailEditText.requestFocus();
        }
        else if (password.isEmpty()) {
            passwordEditText.setError("Password is required.");
            passwordEditText.requestFocus();
        }
        else if (password.length() < 8) {
            passwordEditText.setError("Password must be at least 8 characters.");
            passwordEditText.requestFocus();
        }
        else {

            progressBar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // redirect to home screen
                                startActivity(new Intent(MainActivity.this, HomeScreenActivity.class));
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Failed to login.", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
        }
    }
}