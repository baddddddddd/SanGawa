package com.themabajogroup.sangawa.Overlays;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputEditText;
import com.themabajogroup.sangawa.Models.TaskDetails;
import com.themabajogroup.sangawa.Models.TaskVisibility;
import com.themabajogroup.sangawa.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AddTaskDialog {

    private final Dialog dialog;
    private final TextInputEditText titleInput, descInput, deadlineInput, locationInput;
    private final RadioGroup privacyGroup;
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;

    public AddTaskDialog(Context context, ImageButton btnAddTask, FragmentManager fragmentManager) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_add_task);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setCancelable(false);
        btnAddTask.setOnClickListener(view -> dialog.show());

        Button btnAdd = dialog.findViewById(R.id.add_button);
        Button btnCancel = dialog.findViewById(R.id.cancel_button);

        titleInput = dialog.findViewById(R.id.title);
        descInput = dialog.findViewById(R.id.description);
        deadlineInput = dialog.findViewById(R.id.deadline);
        ImageButton deadlinePicker = dialog.findViewById(R.id.deadline_picker);
        ImageButton locationPicker = dialog.findViewById(R.id.location_picker);
        locationInput = dialog.findViewById(R.id.location);
        privacyGroup = dialog.findViewById(R.id.privacy_group);

        deadlinePicker.setOnClickListener(v -> showDatePicker());
        deadlineInput.setOnClickListener(v -> showDatePicker());

        View.OnClickListener showPinLocationDialog = v -> {
            new PinLocationDialog(locationInput).show(fragmentManager, "PinLocationDialog");
        };

        locationPicker.setOnClickListener(showPinLocationDialog);
        locationInput.setOnClickListener(showPinLocationDialog);


        btnAdd.setOnClickListener(view -> {
            String title = titleInput.getText().toString().trim();
            String description = descInput.getText().toString().trim();
            String deadlineStr = deadlineInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();
            int selectedPrivacyId = privacyGroup.getCheckedRadioButtonId();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(deadlineStr) || TextUtils.isEmpty(location) || selectedPrivacyId == -1) {
                Toast.makeText(view.getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String privacy = ((RadioButton) dialog.findViewById(selectedPrivacyId)).getText().toString();

            try {
                String[] locationParts = location.split(",");
                double locationLat = Double.parseDouble(locationParts[0].trim());
                double locationLon = Double.parseDouble(locationParts[1].trim());

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date deadline = sdf.parse(deadlineStr);

                TaskVisibility visibility = TaskVisibility.valueOf(privacy);

                TaskDetails task = new TaskDetails(
                        "userIdPlaceholder",
                        title,
                        description,
                        locationLat,
                        locationLon,
                        visibility,
                        deadline
                );

                // TODO: Save the task object to your database
                Toast.makeText(context, "Task added successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            } catch (Exception e) {
                Toast.makeText(view.getContext(), "Invalid input. Please check your values.", Toast.LENGTH_SHORT).show();
            }
        });

        btnCancel.setOnClickListener(view -> dialog.dismiss());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(dialog.getContext(), (view, year, month, dayOfMonth) -> {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = dayOfMonth;
            showTimePicker();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(dialog.getContext(), (view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMinute = minute;
            updateDeadline();
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    @SuppressLint("DefaultLocale")
    private void updateDeadline() {
        deadlineInput.setText(String.format("%02d/%02d/%d %02d:%02d", selectedDay, selectedMonth + 1, selectedYear, selectedHour, selectedMinute));
    }
}