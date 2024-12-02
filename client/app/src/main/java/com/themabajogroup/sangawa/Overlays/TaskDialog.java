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
import com.themabajogroup.sangawa.Models.TransactionType;
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
    private final TransactionType transactionType;
    private TaskDetails taskDetails;
    private GoogleMap mMap;
    private TextInputEditText titleInput, descInput, deadlineInput;
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;
    private UserController userController;
    private LatLng location;

    public TaskDialog(MapViewActivity mapViewActivity, TransactionType transactionType) {
        this.mapViewActivity = mapViewActivity;
        this.transactionType = transactionType;
    }

    public TaskDialog(MapViewActivity mapViewActivity, TransactionType transactionType, TaskDetails taskDetails) {
        this(mapViewActivity, transactionType);
        this.taskDetails = taskDetails;
    }

    @SuppressLint("SetTextI18n")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_task, container, false);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        titleInput = view.findViewById(R.id.title);
        descInput = view.findViewById(R.id.description);
        deadlineInput = view.findViewById(R.id.deadline);
        ImageButton deadlinePicker = view.findViewById(R.id.deadline_picker);
        Button btnAdd = view.findViewById(R.id.add_button);
        Button btnCancel = view.findViewById(R.id.cancel_button);

        deadlinePicker.setOnClickListener(v -> showDatePicker());
        deadlineInput.setOnClickListener(v -> showDatePicker());

        userController = UserController.getInstance();
        location = userController.getCurrentLocation();

        if (transactionType == TransactionType.EDIT) {
            fillPastInputs(view, btnAdd);
            btnAdd.setOnClickListener(v -> saveChanges());
        }
        else {
            btnAdd.setOnClickListener(v -> submitCreatedTask());
        }
        btnCancel.setOnClickListener(v -> dismiss());

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void fillPastInputs(View view, Button btnAdd) {
        TextView head = view.findViewById(R.id.head);
        head.setText("Edit Task");
        btnAdd.setText("Update");
        titleInput.setText(taskDetails.getTitle());
        descInput.setText(taskDetails.getDescription());
        Date deadline = taskDetails.getDeadline();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault());
        String formattedDate = dateFormat.format(deadline);
        deadlineInput.setText(formattedDate);
        location = new LatLng(taskDetails.getLocationLat(), taskDetails.getLocationLon());
        RadioGroup privacyGroup = view.findViewById(R.id.privacy_group);
        setPrivacyGroupSelection(taskDetails.getVisibility(), privacyGroup);
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
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
        processTaskSubmission(false);
    }

    private void saveChanges() {
        processTaskSubmission(true);
    }

    private void processTaskSubmission(boolean isEditing) {
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

            if (isEditing) {
                TaskController.getInstance().editUserTask(taskDetails.getTaskId(), task).thenAccept(success -> handleTaskResult(success, "updated"));
            } else {
                TaskController.getInstance().createUserTask(task).thenAccept(success -> handleTaskResult(success, "added"));
            }
        } catch (Exception e) {
            Log.e("TaskInputError", "Error: ", e);
            Toast.makeText(getContext(), "Invalid input. Please check your values.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleTaskResult(boolean success, String action) {
        if (success) {
            mapViewActivity.refreshUserTaskList();
            Toast.makeText(getContext(), "Task " + action + " successfully!", Toast.LENGTH_SHORT).show();
            dismiss();
        } else {
            Toast.makeText(getContext(), "Failed to " + action + " task. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private static void setPrivacyGroupSelection(TaskVisibility visibility, RadioGroup privacyGroup) {
        privacyGroup.check(visibility == TaskVisibility.OPEN_TO_ALL ? R.id.OPEN_TO_ALL :
                visibility == TaskVisibility.PRIVATE ? R.id.PRIVATE :
                        R.id.REQUEST_TO_JOIN);
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
