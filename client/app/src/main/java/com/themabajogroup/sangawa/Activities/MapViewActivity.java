package com.themabajogroup.sangawa.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.themabajogroup.sangawa.Controllers.TaskController;
import com.themabajogroup.sangawa.Controllers.UserController;
import com.themabajogroup.sangawa.Models.CollabDetails;
import com.themabajogroup.sangawa.Models.RequestStatus;
import com.themabajogroup.sangawa.Models.TaskDetails;
import com.themabajogroup.sangawa.Models.TaskVisibility;
import com.themabajogroup.sangawa.Models.TransactionType;
import com.themabajogroup.sangawa.Overlays.TaskDialog;
import com.themabajogroup.sangawa.Overlays.TaskListAdapter;
import com.themabajogroup.sangawa.R;
import com.themabajogroup.sangawa.Utils.GeofenceBroadcastReceiver;
import com.themabajogroup.sangawa.Utils.NotificationSender;
import com.themabajogroup.sangawa.databinding.ActivityMapViewBinding;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback, TaskListAdapter.TaskItemClickListener {

    private static final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 2;
    private GoogleMap mMap;
    private ActivityMapViewBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GeofencingClient geofencingClient;
    private UserController userController;
    private TaskController taskController;
    private RecyclerView recyclerViewTasks;
    private Map<String, Map<String, CollabDetails>> collabRequests;
    private Map<String, TaskDetails> currentTasks;
    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton userTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userController = UserController.getInstance();
        taskController = TaskController.getInstance();
        collabRequests = userController.getCollabRequests();
        currentTasks = new HashMap<>();

        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        ImageButton btnAddTask = findViewById(R.id.add_task_button);
        TaskDialog editTaskDialog = new TaskDialog(this, TransactionType.ADD);
        btnAddTask.setOnClickListener(view -> editTaskDialog.show(getSupportFragmentManager(), "MapFragment"));

        toggleGroup = findViewById(R.id.toggleGroup);
        userTab = findViewById(R.id.usertab);
        toggleGroup.check(userTab.getId());
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                refreshTaskList();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);

        checkLocationPermissions();

        initializeTaskList();
        userController.fetchProfile().thenAccept(aBoolean -> {


            // TODO: Remove test code
            TaskDetails details = new TaskDetails(
               "pQfsXyxqyxOPNTt1bZhAuQyBo4s1",
               "testing edited",
                    "try",
                    100,
                    100,
                    TaskVisibility.REQUEST_TO_JOIN,
                    new Date()
            );
            details.setTaskId("0B5cXHjol6wluosUuSt9");
            sendCollabRequest(details);

        });

    }

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, FINE_LOCATION_PERMISSION_REQUEST_CODE);

            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            }, BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE);

            return;
        }

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // Update every 5 seconds
        locationRequest.setFastestInterval(2000); // Fastest update every 2 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLastLocation() == null) {
                    Toast.makeText(MapViewActivity.this, "Unable to get location updates.", Toast.LENGTH_SHORT).show();
                    return;
                }

                userController.setCurrentLocation(new LatLng(
                        locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude()
                ));
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void setupGeofence(String id, LatLng latLng, float radius) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermissions();
            return;
        }

        Geofence geofence = new Geofence.Builder()
                .setRequestId(id)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        geofencingClient.addGeofences(
                new GeofencingRequest.Builder()
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .addGeofence(geofence)
                        .build(),
                getGeofencePendingIntent()
        ).addOnSuccessListener(aVoid ->
                Toast.makeText(this, "Geofence added successfully!", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to add task geofence: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_PERMISSION_REQUEST_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Fine location permission is required to use this feature.", Toast.LENGTH_SHORT).show();
                return;
            }

            startLocationUpdates();
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Background location permission is required to use this feature.", Toast.LENGTH_SHORT).show();
                return;
            }

            startLocationUpdates();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermissions();
            return;
        }

        mMap.setMyLocationEnabled(true);

        // Move camera to a default location until the location updates
        LatLng bsuAlangilan = new LatLng(13.7839623, 121.0740536);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bsuAlangilan, 15));
        userController.setCurrentLocation(bsuAlangilan);

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        refreshUserTaskMarkers();
    }


    @Override
    public void onMoreOptionClick(View view, TaskDetails task, Boolean isCurrentUserTask) {
        Context wrapper = new ContextThemeWrapper(this, R.style.popupMenuStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_task_options, popupMenu.getMenu());
        MenuItem acceptTaskMenuItem = popupMenu.getMenu().findItem(R.id.menu_accept_task);
        if (acceptTaskMenuItem != null) {
            acceptTaskMenuItem.setVisible(!isCurrentUserTask);
        }
        popupMenu.setForceShowIcon(true);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_view_task) {
                Toast.makeText(this, "View task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.menu_accept_task) {
                // TODO: Add logic here for accepting task
                Toast.makeText(this, task.getTitle() + " Accepted Successfully", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.menu_done_task) {
                Toast.makeText(this, "Finished task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.menu_edit_task) {
                TaskDialog editTaskDialog = new TaskDialog(this, TransactionType.EDIT, task);
                editTaskDialog.show(getSupportFragmentManager(), "MapFragment");
            } else if (itemId == R.id.menu_delete_task) {
                taskController.deleteUserTask(task.getTaskId());
                Toast.makeText(this, "Deleted " + task.getTitle() + " successfully!", Toast.LENGTH_SHORT).show();
            } else {
                return false;
            }
            refreshTaskList();
            refreshUserTaskMarkers();
            return true;
        });

        popupMenu.show();
    }

    public void initializeTaskList() {
        refreshTaskList().thenAccept(unused -> {
           setupCollabListener();
        });
    }

    public CompletableFuture<Void> refreshTaskList() {
        CompletableFuture<Void> result = new CompletableFuture<>();

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));

        userController.fetchUserTasks().thenAccept(tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                boolean isCurrentUserTask = toggleGroup.getCheckedButtonId() == userTab.getId();
                TaskListAdapter taskListAdapter = new TaskListAdapter(tasks, this, isCurrentUserTask);
                recyclerViewTasks.setAdapter(taskListAdapter);

                currentTasks = new HashMap<>();
                for (TaskDetails taskDetails : tasks) {
                    currentTasks.put(taskDetails.getTaskId(), taskDetails);
                }

                result.complete(null);

            } else {
                Toast.makeText(this, "No tasks found", Toast.LENGTH_SHORT).show();
            }
        });

        return result;
    }

    public void refreshUserTaskMarkers() {
        userController.fetchUserTasks()
                .thenAccept(taskDetailsList -> {
                    for (TaskDetails taskDetails : taskDetailsList) {
                        LatLng location = new LatLng(taskDetails.getLocationLat(), taskDetails.getLocationLon());

                        // TODO: Radius must be adjustable by users for own tasks
                        // TODO: Do not create new geofence for existing tasks
                        setupGeofence(taskDetails.getTitle(), location, 1000);

                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(location)
                                .title(taskDetails.getTitle())
                                .snippet(taskDetails.getDescription());

                        mMap.addMarker(markerOptions);
                    }
                });

        // TODO: Only call this function when user has moved a certain amount of distance to conserve resources
        refreshNearbyTaskMarkers();
    }

    public void refreshNearbyTaskMarkers() {
        // TODO: Remove existing nearby task markers, then proceed to add new ones
        // TODO: Remove existing geofence for nearby tasks, then proceed to add new ones
        userController.fetchNearbyTasks()
                .thenAccept(taskDetailsList -> {
                    for (TaskDetails taskDetails : taskDetailsList) {
                        LatLng location = new LatLng(taskDetails.getLocationLat(), taskDetails.getLocationLon());

                        // TODO: Radius must be adjustable by users for collaborative tasks
                        setupGeofence(taskDetails.getTitle(), location, 1000);

                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(location)
                                .title(taskDetails.getTitle())
                                .snippet(taskDetails.getDescription());

                        mMap.addMarker(markerOptions);
                    }
                });
    }

    public void setupCollabListener() {
        String userId = userController.getCurrentUser().getUid();
        taskController.attachJoinRequestListener(userId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> data = (Map<String, Object>) snapshot.getValue();

                if (data == null) {
                    return;
                }

                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    String taskId = entry.getKey();
                    Map<String, Object> requestDetails = (Map<String, Object>) entry.getValue();

                    Map.Entry<String, Object> singleEntry = requestDetails.entrySet().iterator().next();
                    String requesterId = singleEntry.getKey();

                    Map<String, String> details = (Map<String, String>) singleEntry.getValue();
                    String requesterName = details.get("requesterName");
                    RequestStatus status = RequestStatus.valueOf(details.get("status"));

                    if (status != RequestStatus.PENDING) {
                        continue;
                    }

                    CollabDetails collabDetails = new CollabDetails(taskId, requesterId, requesterName, status);

                    if (!collabRequests.containsKey(taskId)) {
                        collabRequests.put(taskId, new HashMap<>());
                    }

                    Map<String, CollabDetails> taskCollabs = collabRequests.get(taskId);

                    if (taskCollabs == null) {
                        continue;
                    }

                    if (!taskCollabs.containsKey(requesterId)) {
                        taskCollabs.put(requesterId, collabDetails);
                        notifyNewCollab(collabDetails);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void notifyNewCollab(CollabDetails collabDetails) {
        String channelId = "CollabRequestChannel";
        NotificationSender sender = NotificationSender.getInstance(channelId, this);

        TaskDetails task = currentTasks.get(collabDetails.getTaskId());
        if (task == null) {
            return;
        }

        String title = "Collaboration request for " + task.getTitle();
        String description = collabDetails.getRequesterName() + " wants to join you!";
        sender.sendCollabNotification(title, description, collabDetails.getTaskId(), collabDetails.getRequesterId());
    }

    public CompletableFuture<Boolean> sendCollabRequest(TaskDetails taskDetails) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        String requesterId = userController.getCurrentUser().getUid();
        String taskId = taskDetails.getTaskId();
        String ownerId = taskDetails.getUserId();
        String requesterName = userController.getProfile().getUsername();
        taskController.createJoinRequest(ownerId, taskId, requesterId, requesterName)
                .thenAccept(isSuccess -> {
                    if (!isSuccess) {
                        result.complete(false);
                        return;
                    }

                    addCollabReplyListener(taskDetails);
                });

        return result;
    }

    // TODO: Fetch firestore database for pending collab requests, and add listeners for all of them
    public void addCollabReplyListener(TaskDetails taskDetails) {
        String requesterId = userController.getCurrentUser().getUid();

        taskController.attachCollabReplyListener(requesterId, taskDetails, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, String> data = (Map<String, String>) snapshot.getValue();

                if (data == null) {
                    return;
                }

                RequestStatus status = RequestStatus.valueOf(data.get("status"));
                NotificationSender sender = NotificationSender.getInstance("CollabNotifications", MapViewActivity.this);

                // TODO: (Low Prio) Remove ValueEventListener on ACCEPT/DECLINE
                if (status == RequestStatus.ACCEPTED) {
                    String title = "Collaboration request ACCEPTED!";
                    String description = "Your request to join " + taskDetails.getTitle() + " has been accepted";

                    sender.sendNotification(title, description);

                } else if (status == RequestStatus.DECLINED) {
                    String title = "Collaboration request DECLINED!";
                    String description = "Your request to join " + taskDetails.getTitle() + " has been declined";

                    sender.sendNotification(title, description);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
