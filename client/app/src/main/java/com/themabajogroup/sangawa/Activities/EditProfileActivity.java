package com.themabajogroup.sangawa.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.themabajogroup.sangawa.R;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.Slider;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profilePicture;
    private TextView emailTextView, usernameTextView;
    private EditText emailEditText, usernameEditText;
    private TextView fencingRadiusLabel, scanRadiusLabel;
    private Slider fencingRadiusSlider, scanRadiusSlider;
    private Button saveButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profilePicture = findViewById(R.id.profile_picture);
        emailTextView = findViewById(R.id.email_value);
        usernameTextView = findViewById(R.id.username_value);
        emailEditText = findViewById(R.id.email_edit);
        usernameEditText = findViewById(R.id.username_edit);
        fencingRadiusLabel = findViewById(R.id.fencing_radius_label);
        scanRadiusLabel = findViewById(R.id.scan_radius_label);
        fencingRadiusSlider = findViewById(R.id.fencing_radius_slider);
        scanRadiusSlider = findViewById(R.id.scan_radius_slider);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);

        emailTextView.setText("endrickmasarap@example.com");
        usernameTextView.setText("thatwassohot");

        fencingRadiusLabel.setText("Fencing Radius: 1000m");
        scanRadiusLabel.setText("Scan Radius: 500m");
        fencingRadiusSlider.setValue(1000);
        scanRadiusSlider.setValue(500);

        fencingRadiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            int radius = (int) value;
            fencingRadiusLabel.setText("Fencing Radius: " + radius + "m");
        });

        scanRadiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            int radius = (int) value;
            scanRadiusLabel.setText("Scan Radius: " + radius + "m");
        });

        saveButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String username = usernameEditText.getText().toString();

            Toast.makeText(EditProfileActivity.this, "Saved Changes:\nEmail: " + email + "\nUsername: " + username, Toast.LENGTH_LONG).show();

            emailTextView.setVisibility(View.VISIBLE);
            usernameTextView.setVisibility(View.VISIBLE);
            emailEditText.setVisibility(View.GONE);
            usernameEditText.setVisibility(View.GONE);
        });

        cancelButton.setOnClickListener(v -> {
            emailTextView.setVisibility(View.VISIBLE);
            usernameTextView.setVisibility(View.VISIBLE);
            emailEditText.setVisibility(View.GONE);
            usernameEditText.setVisibility(View.GONE);

            Toast.makeText(EditProfileActivity.this, "Changes Canceled", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.email_edit_icon).setOnClickListener(v -> {
            emailTextView.setVisibility(View.GONE);
            emailEditText.setVisibility(View.VISIBLE);
            emailEditText.setText(emailTextView.getText().toString()); // Pre-fill
        });

        findViewById(R.id.username_edit_icon).setOnClickListener(v -> {
            usernameTextView.setVisibility(View.GONE);
            usernameEditText.setVisibility(View.VISIBLE);
            usernameEditText.setText(usernameTextView.getText().toString()); // Pre-fill
        });
    }
}
