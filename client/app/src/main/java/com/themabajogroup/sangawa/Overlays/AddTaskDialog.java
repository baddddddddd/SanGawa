package com.themabajogroup.sangawa.Overlays;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.themabajogroup.sangawa.Activities.MapViewActivity;
import com.themabajogroup.sangawa.Controllers.TaskController;
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
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;

    public AddTaskDialog(Context context, ImageButton btnAddTask, FragmentManager fragmentManager, MapViewActivity mapViewActivity) {
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

        deadlinePicker.setOnClickListener(v -> showDatePicker());
        deadlineInput.setOnClickListener(v -> showDatePicker());

        View.OnClickListener showPinLocationDialog = v -> new PinLocationDialog(locationInput).show(fragmentManager, "PinLocationDialog");

        locationPicker.setOnClickListener(showPinLocationDialog);
        locationInput.setOnClickListener(showPinLocationDialog);


        btnAdd.setOnClickListener(view -> {
            String title = titleInput.getText().toString().trim();
            String description = descInput.getText().toString().trim();
            String deadlineStr = deadlineInput.getText().toString().trim();
            String location = locationInput.getText().toString().trim();
            int selectedPrivacyId = ((RadioGroup) dialog.findViewById(R.id.privacy_group)).getCheckedRadioButtonId();

            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) ||
                    TextUtils.isEmpty(deadlineStr) || TextUtils.isEmpty(location) || selectedPrivacyId == -1) {
                Toast.makeText(view.getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                String[] locationParts = location.split(",");
                if (locationParts.length != 2) {
                    throw new IllegalArgumentException("Location must have two parts: latitude and longitude");
                }
                double locationLat = Double.parseDouble(locationParts[0].trim());
                double locationLon = Double.parseDouble(locationParts[1].trim());

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                sdf.setLenient(false);
                Date deadline = sdf.parse(deadlineStr);

                TaskVisibility visibility = getTaskVisibility(selectedPrivacyId);

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(view.getContext(), "User is not logged in.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String userId = currentUser.getUid();

                TaskDetails task = new TaskDetails(
                        userId, title, description, locationLat, locationLon, visibility, deadline
                );

                TaskController.getInstance().createUserTask(task)
                        .thenAccept(success -> {
                            if (success) {
                                if (mapViewActivity != null) {
                                    mapViewActivity.refreshTaskList();
                                }
                                Toast.makeText(context, "Task added successfully!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(context, "Failed to add task. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (Exception e) {
                Toast.makeText(view.getContext(), "Invalid input. Please check your values.", Toast.LENGTH_SHORT).show();
                Log.e("TaskInputDebug", "Error: " + e.getMessage(), e);
            }
        });

        btnCancel.setOnClickListener(view -> dialog.dismiss());
    }

    @NonNull
    private static TaskVisibility getTaskVisibility(int selectedPrivacyId) {
        TaskVisibility visibility;
        if (selectedPrivacyId == R.id.OPEN_TO_ALL) {
            visibility = TaskVisibility.OPEN_TO_ALL;
        } else if (selectedPrivacyId == R.id.PRIVATE) {
            visibility = TaskVisibility.PRIVATE;
        } else if (selectedPrivacyId == R.id.REQUEST_TO_JOIN) {
            visibility = TaskVisibility.REQUEST_TO_JOIN;
        } else {
            throw new IllegalArgumentException("Invalid privacy option selected");
        }
        return visibility;
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
