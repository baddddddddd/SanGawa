package com.themabajogroup.sangawa;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText;
    private TextInputLayout emailLayout;
    private TextInputEditText emailEditText;
    private TextInputLayout passwordLayout;
    private TextInputEditText passwordEditText;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText confirmPasswordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        TextView titleText = findViewById(R.id.title);
        TextInputLayout usernameLayout = findViewById(R.id.username_layout);
        usernameEditText = findViewById(R.id.username);
        emailLayout = findViewById(R.id.email_layout);
        emailEditText = findViewById(R.id.email);
        passwordLayout = findViewById(R.id.password_layout);
        passwordEditText = findViewById(R.id.password);
        Button signUpButton = findViewById(R.id.action_button);
        TextView swapTextView = findViewById(R.id.swap);

        swapTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        titleText.setText("Sign Up");
        signUpButton.setText("Sign Up");

        signUpButton.setOnClickListener(v -> {
            String username = Objects.requireNonNull(usernameEditText.getText()).toString();
            String email = Objects.requireNonNull(emailEditText.getText()).toString();
            String password = Objects.requireNonNull(passwordEditText.getText()).toString();

            // TODO: Add login logic here (e.g., check credentials, perform authentication)
        });
    }
}
