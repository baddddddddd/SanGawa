package com.themabajogroup.sangawa.Overlays;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.themabajogroup.sangawa.Activities.MapViewActivity;
import com.themabajogroup.sangawa.Controllers.TaskController;
import com.themabajogroup.sangawa.Controllers.UserController;
import com.themabajogroup.sangawa.Models.TaskDetails;
import com.themabajogroup.sangawa.Models.TaskVisibility;
import com.themabajogroup.sangawa.Models.Transaction;
import com.themabajogroup.sangawa.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TaskDialog extends DialogFragment implements OnMapReadyCallback {

    private final MapViewActivity mapViewActivity;
    private final Transaction transaction;
    private GoogleMap mMap;
    private TextInputEditText titleInput, descInput, deadlineInput;
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;
    private UserController userController;

    public TaskDialog(MapViewActivity mapViewActivity, Transaction transaction) {
        this.mapViewActivity = mapViewActivity;
        this.transaction = transaction;
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_task, container, false);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView head = view.findViewById(R.id.head);
        titleInput = view.findViewById(R.id.title);
        descInput = view.findViewById(R.id.description);
        deadlineInput = view.findViewById(R.id.deadline);
        ImageButton deadlinePicker = view.findViewById(R.id.deadline_picker);
        Button btnAdd = view.findViewById(R.id.add_button);
        Button btnCancel = view.findViewById(R.id.cancel_button);

        deadlinePicker.setOnClickListener(v -> showDatePicker());
        deadlineInput.setOnClickListener(v -> showDatePicker());

        userController = UserController.getInstance();

        if (transaction == Transaction.EDIT) {
            head.setText("Edit Task");
            btnAdd.setText("Update");
//            btnAdd.setOnClickListener(v -> saveChanges());
        }
        else {
            btnAdd.setOnClickListener(v -> submitCreatedTask());
        }
        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.pin_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userController.getCurrentLocation(), 15));
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            selectedYear = year;
            selectedMonth = month;
            selectedDay = dayOfMonth;
            showTimePicker();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            selectedHour = hourOfDay;
            selectedMinute = minute;
            updateDeadline();
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    @SuppressLint("DefaultLocale")
    private void updateDeadline() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());
        String formattedDate = sdf.format(calendar.getTime());
        deadlineInput.setText(formattedDate);
    }

    private void submitCreatedTask() {
        String title = titleInput.getText().toString().trim();
        String description = descInput.getText().toString().trim();
        String deadlineStr = deadlineInput.getText().toString().trim();
        int selectedPrivacyId = ((RadioGroup) requireView().findViewById(R.id.privacy_group)).getCheckedRadioButtonId();

        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(description) || TextUtils.isEmpty(deadlineStr) || selectedPrivacyId == -1) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double locationLat = mMap.getCameraPosition().target.latitude;
            double locationLon = mMap.getCameraPosition().target.longitude;

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());
            Date deadline = sdf.parse(deadlineStr);

            TaskVisibility visibility = getTaskVisibility(selectedPrivacyId);

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(getContext(), "User is not logged in.", Toast.LENGTH_SHORT).show();
                return;
            }

            TaskDetails task = new TaskDetails(
                    currentUser.getUid(), title, description, locationLat, locationLon, visibility, deadline
            );

            TaskController.getInstance().createUserTask(task).thenAccept(success -> {
                if (success) {
                    mapViewActivity.refreshTaskList();
                    mapViewActivity.refreshTaskMarkers();
                    Toast.makeText(getContext(), "Task added successfully!", Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(getContext(), "Failed to add task. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("TaskInputError", "Error: ", e);
            Toast.makeText(getContext(), "Invalid input. Please check your values.", Toast.LENGTH_SHORT).show();
        }
    }

    private static TaskVisibility getTaskVisibility(int selectedPrivacyId) {
        if (selectedPrivacyId == R.id.OPEN_TO_ALL) {
            return TaskVisibility.OPEN_TO_ALL;
        } else if (selectedPrivacyId == R.id.PRIVATE) {
            return TaskVisibility.PRIVATE;
        } else if (selectedPrivacyId == R.id.REQUEST_TO_JOIN) {
            return TaskVisibility.REQUEST_TO_JOIN;
        } else {
            throw new IllegalArgumentException("Invalid privacy option selected");
        }
    }
}
