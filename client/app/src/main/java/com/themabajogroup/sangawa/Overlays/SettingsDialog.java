package com.themabajogroup.sangawa.Overlays;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.themabajogroup.sangawa.Activities.AuthActivity;
import com.themabajogroup.sangawa.Activities.MapViewActivity;
import com.themabajogroup.sangawa.Controllers.UserController;
import com.themabajogroup.sangawa.Models.UserProfile;
import com.themabajogroup.sangawa.R;

import java.util.Objects;

public class SettingsDialog extends Dialog {
    private final Context context;
    private TextInputEditText displayNameInput;
    private ImageButton editDisplayNameButton;
    private Slider fencingRadiusSlider;
    private Slider scanRadiusSlider;
    private TextView fencingRadiusValue;
    private TextView scanRadiusValue;
    private Button saveButton, logoutButton;
    private ImageButton cancelButton;

    private UserProfile profile;
    private final UserController userManager;

    public SettingsDialog(Context context) {
        super(context);
        this.context = context;
        this.userManager = UserController.getInstance();
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_settings);
        Objects.requireNonNull(getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        displayNameInput = findViewById(R.id.setting_dname);
        editDisplayNameButton = findViewById(R.id.edit_email);
        fencingRadiusSlider = findViewById(R.id.fencing_radius_slider);
        fencingRadiusValue = findViewById(R.id.fencing_radius_value);
        scanRadiusSlider = findViewById(R.id.scan_radius_slider);
        scanRadiusValue = findViewById(R.id.scan_radius_value);
        cancelButton = findViewById(R.id.cancel_button);
        saveButton = findViewById(R.id.save_button);
        logoutButton = findViewById(R.id.logout_button);

        fetchAndPopulateProfile();

        fencingRadiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            fencingRadiusValue.setText(String.format("%d m", (int) value));
        });

        scanRadiusSlider.addOnChangeListener((slider, value, fromUser) -> {
            scanRadiusValue.setText(String.format("%d m", (int) value));
        });

        editDisplayNameButton.setOnClickListener(v -> {
            displayNameInput.setEnabled(true);
            displayNameInput.requestFocus();
        });

        logoutButton.setOnClickListener(v -> signOutUser());

        cancelButton.setOnClickListener(v -> dismiss());

        saveButton.setOnClickListener(v -> saveSettings());
    }


    void signOutUser() {
        UserController.getInstance().signOutUser();
        Intent intent = new Intent(context, AuthActivity.class);
        context.startActivity(intent);
        ((MapViewActivity) context).finish();
    }

    @SuppressLint("DefaultLocale")
    private void fetchAndPopulateProfile() {
        userManager.fetchProfile().thenAccept(success -> {
            if (!success || userManager.getProfile() == null) {
                return;
            }

            profile = userManager.getProfile();

            displayNameInput.setText(profile.getUsername());
            fencingRadiusSlider.setValue(profile.getFencingRadius());
            scanRadiusSlider.setValue(profile.getScanRadius());

            fencingRadiusValue.setText(String.format("%d m", (int) profile.getFencingRadius()));
            scanRadiusValue.setText(String.format("%d m", (int) profile.getScanRadius()));
        });
    }

    private void saveSettings() {
        if (profile == null) {
            return;
        }

        profile.setUsername(Objects.requireNonNull(displayNameInput.getText()).toString().trim());
        profile.setFencingRadius(fencingRadiusSlider.getValue());
        profile.setScanRadius(scanRadiusSlider.getValue());

        userManager.uploadProfile().thenAccept(success -> {
            if (success) {
                dismiss();
            }
        });
    }
}
