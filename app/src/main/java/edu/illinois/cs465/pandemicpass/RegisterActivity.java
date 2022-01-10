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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private Button register;
    private EditText emailEditText, passwordEditText, confirmPasswordEditText;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = (EditText) findViewById(R.id.email);
        passwordEditText = (EditText) findViewById(R.id.password);
        confirmPasswordEditText = (EditText) findViewById(R.id.confirmPassword);

        register = (Button) findViewById(R.id.register);
        register.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.register) {
            registerUser();
        }
    }

    private void registerUser() {
        if (progressBar.getVisibility() == View.VISIBLE) {
            return;
        }

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

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
        else if (!confirmPassword.equals(password)) {
            confirmPasswordEditText.setError("Confirmation does not match password.");
            confirmPasswordEditText.requestFocus();
        }
        else {
            progressBar.setVisibility(View.VISIBLE);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                User user = new User(email);

                                FirebaseDatabase.getInstance()
                                        .getReference("User")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    //progressBar.setVisibility(View.INVISIBLE);
                                                    // start new activity to home screen and destroy this one instead of keeping in stack
//                                                    startActivity(new Intent(RegisterActivity.this, HomeScreenActivity.class));
                                                    Intent intent = new Intent(RegisterActivity.this, MemberListActivity.class);
                                                    intent.putExtra("new_member", "new_member");
                                                    startActivity(intent);
                                                    RegisterActivity.this.finish();
                                                }
                                                else {
                                                    Toast.makeText(RegisterActivity.this, "Failed to register user.", Toast.LENGTH_LONG).show();
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                }

                                            }
                                        });

                            } else {
                                Toast.makeText(RegisterActivity.this, "Failed to register user.", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });


        }
    }
}