package com.themabajogroup.sangawa.Overlays;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.material.textfield.TextInputEditText;
import com.themabajogroup.sangawa.R;

import java.util.Objects;

public class PinMapDialog {
    private final Dialog dialog;
    public PinMapDialog(Context context, ImageButton locationPicker, TextInputEditText locationInput) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_pin_map);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);

        ImageButton closeButton = dialog.findViewById(R.id.close_button);
        Button confirmButton = dialog.findViewById(R.id.confirm_location_button);

        locationPicker.setOnClickListener(view -> dialog.show());
        locationInput.setOnClickListener(view -> dialog.show());
        closeButton.setOnClickListener(v -> dialog.dismiss());
    }
}
