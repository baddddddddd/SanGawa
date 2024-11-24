package com.themabajogroup.sangawa;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class AuthActivity extends AppCompatActivity {
    private TextView titleTextView;
    private TextInputEditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private TextInputLayout usernameLayout, confirmPasswordLayout;
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
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        usernameLayout = findViewById(R.id.username_layout);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);
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
                } else {
                }
            } else {
                String username = Objects.requireNonNull(usernameEditText.getText()).toString();
                String email = Objects.requireNonNull(emailEditText.getText()).toString();
                String password = Objects.requireNonNull(passwordEditText.getText()).toString();
                String confirmPassword = Objects.requireNonNull(confirmPasswordEditText.getText()).toString();

                if (validateSignup(username, email, password, confirmPassword)) {
                } else {
                }
            }
        });

        findViewById(R.id.main).setOnTouchListener(this::onTouch);
    }

    @SuppressLint("SetTextI18n")
    private void updateUIForLogin() {
        isLogin = true;
        titleTextView.setText("Login");
        actionButton.setText("LOG IN");
        swapTextView.setText("Don't have an account? Sign up now!");
        usernameLayout.setVisibility(View.GONE);
        confirmPasswordLayout.setVisibility(View.GONE);
    }

    @SuppressLint("SetTextI18n")
    private void updateUIForSignup() {
        isLogin = false;
        titleTextView.setText("Sign Up");
        actionButton.setText("SIGN UP");
        swapTextView.setText("Already have an account? Log in now!");
        usernameLayout.setVisibility(View.VISIBLE);
        confirmPasswordLayout.setVisibility(View.VISIBLE);
    }

    private boolean validateLogin(String email, String password) {
        return !email.isEmpty() && !password.isEmpty();
    }

    private boolean validateSignup(String username, String email, String password, String confirmPassword) {
        return !username.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty();
    }

    private boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View focusedView = getCurrentFocus();
            if (focusedView != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
            }
        }
        return false;
    }
}
