package com.themabajogroup.sangawa.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.themabajogroup.sangawa.Controllers.AuthController;
import com.themabajogroup.sangawa.R;

import java.util.Objects;

public class AuthActivity extends AppCompatActivity {
    private TextView titleTextView;
    private TextInputEditText usernameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private TextInputLayout usernameLayout, confirmPasswordLayout;
    private Button actionButton;
    private TextView swapTextView;
    private boolean isLogin = true;
    private AuthController authController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        authController = AuthController.getInstance();

        titleTextView = findViewById(R.id.title);
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        usernameLayout = findViewById(R.id.username_layout);
        confirmPasswordLayout = findViewById(R.id.confirm_password_layout);
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

                if (validateLogin(AuthActivity.this, email, password)) {
                    actionButton.setVisibility(View.GONE);
                    findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);

                    authController.verifyCredentials(email, password)
                            .thenAccept(isSuccess -> runOnUiThread(() -> {
                                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                                actionButton.setVisibility(View.VISIBLE);

                                if (isSuccess) {
                                    Intent intent = new Intent(AuthActivity.this, MapViewActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    showToast(AuthActivity.this, "Incorrect email or password");
                                }
                            }));
                } else {
                    showToast(AuthActivity.this, "Please check the entered details and try again.");
                }
            } else {
                String username = Objects.requireNonNull(usernameEditText.getText()).toString();
                String email = Objects.requireNonNull(emailEditText.getText()).toString();
                String password = Objects.requireNonNull(passwordEditText.getText()).toString();
                String confirmPassword = Objects.requireNonNull(confirmPasswordEditText.getText()).toString();

                if (validateSignup(AuthActivity.this, username, email, password, confirmPassword)) {
                    actionButton.setVisibility(View.GONE);
                    findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);

                    authController.registerCredentials(username, email, password)
                            .thenAccept(isSuccess -> runOnUiThread(() -> {
                                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                                actionButton.setVisibility(View.VISIBLE);

                                if (isSuccess) {
                                    Intent intent = new Intent(AuthActivity.this, MapViewActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    showToast(AuthActivity.this, "Registration failed. Please try again.");
                                }
                            }));
                } else {
                    showToast(AuthActivity.this, "Please check the entered details and try again.");
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

    private boolean validateLogin(Context context, String email, String password) {
        if (email.isEmpty()) {
            showToast(context, "Email cannot be empty");
            return false;
        }
        if (isNotValidEmail(email)) {
            showToast(context, "Please enter a valid email address");
            return false;
        }
        if (password.isEmpty()) {
            showToast(context, "Password cannot be empty");
            return false;
        }
        if (password.length() < 6) {
            showToast(context, "Password must be at least 6 characters long");
            return false;
        }
        return true;
    }

    private boolean validateSignup(Context context, String username, String email, String password, String confirmPassword) {
        if (username.isEmpty()) {
            showToast(context, "Username cannot be empty");
            return false;
        }
        if (email.isEmpty()) {
            showToast(context, "Email cannot be empty");
            return false;
        }
        if (isNotValidEmail(email)) {
            showToast(context, "Enter a valid email address");
            return false;
        }
        if (password.isEmpty()) {
            showToast(context, "Password cannot be empty");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            showToast(context, "Passwords do not match");
            return false;
        }
        if (password.length() < 6) {
            showToast(context, "Password must be at least 6 characters");
            return false;
        }
        return true;
    }

    private boolean isNotValidEmail(String email) {
        return !email.matches("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    }

    private void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
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
