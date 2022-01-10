package edu.illinois.cs465.pandemicpass;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private EditText emailEditText;
    private Button send;

    public ForgotPasswordActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = (EditText) findViewById(R.id.email);

        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.send) {
            sendResetEmail();
        }
    }

    private void sendResetEmail() {
        String emailText = emailEditText.getText().toString().trim();

        mAuth.sendPasswordResetEmail(emailText).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(ForgotPasswordActivity.this, "Password reset email sent.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ForgotPasswordActivity.this, "Failed to send reset email.", Toast.LENGTH_LONG).show();
            }
        });
    }
}