package com.themabajogroup.sangawa;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class AuthActivity extends AppCompatActivity {
    private TextView titleTextView;
    private TextInputEditText usernameEditText, emailEditText, passwordEditText;
    private TextInputLayout usernameLayout;
    private Button actionButton;
    private TextView swapTextView;
    private boolean isLogin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        titleTextView = findViewById(R.id.title);
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        usernameLayout = findViewById(R.id.username_layout);
        TextInputLayout emailLayout = findViewById(R.id.email_layout);
        TextInputLayout passwordLayout = findViewById(R.id.password_layout);
        actionButton = findViewById(R.id.action_button);
        swapTextView = findViewById(R.id.swap);

        updateUIForLogin();

        swapTextView.setOnClickListener(v -> {
            if (isLogin) {
                updateUIForSignup();
            } else {
                updateUIForLogin();
            }
        });

        actionButton.setOnClickListener(v -> {
            if (isLogin) {
                String email = Objects.requireNonNull(emailEditText.getText()).toString();
                String password = Objects.requireNonNull(passwordEditText.getText()).toString();

                if (validateLogin(email, password)) {
                    // TODO: Implement login logic then proceed to next activity
                } else {
                    // TODO: Show error message
                }
            } else {
                String username = Objects.requireNonNull(usernameEditText.getText()).toString();
                String email = Objects.requireNonNull(emailEditText.getText()).toString();
                String password = Objects.requireNonNull(passwordEditText.getText()).toString();

                if (validateSignup(username, email, password)) {
                    // TODO: Implement signup logic then save user data
                } else {
                    // TODO: Show error message
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void updateUIForLogin() {
        isLogin = true;
        titleTextView.setText("Login");
        actionButton.setText("LOG IN");
        swapTextView.setText("Don't have an account? Sign up now!");
        usernameLayout.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    private void updateUIForSignup() {
        isLogin = false;
        titleTextView.setText("Sign Up");
        actionButton.setText("SIGN UP");
        swapTextView.setText("Already have an account? Log in now!");
        usernameLayout.setVisibility(View.VISIBLE);
    }

    private boolean validateLogin(String email, String password) {
        // TODO: Implement login validation
        return !email.isEmpty() && !password.isEmpty();
    }

    private boolean validateSignup(String username, String email, String password) {
        // TODO: Implement signup validation
        return !username.isEmpty() && !email.isEmpty() && !password.isEmpty();
    }
}