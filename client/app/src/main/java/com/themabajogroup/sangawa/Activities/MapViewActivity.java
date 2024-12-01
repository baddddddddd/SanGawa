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
import com.themabajogroup.sangawa.Models.TransactionType;
import com.themabajogroup.sangawa.Overlays.TaskDialog;
import com.themabajogroup.sangawa.Overlays.TaskListAdapter;
import com.themabajogroup.sangawa.R;
import com.themabajogroup.sangawa.Utils.GeofenceBroadcastReceiver;
import com.themabajogroup.sangawa.Utils.NotificationSender;
import com.themabajogroup.sangawa.databinding.ActivityMapViewBinding;

import java.util.HashMap;
import java.util.Map;

import kotlin.NotImplementedError;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userController = UserController.getInstance();
        taskController = TaskController.getInstance();
        collabRequests = userController.getCollabRequests();

        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        ImageButton btnAddTask = findViewById(R.id.add_task_button);
        TaskDialog editTaskDialog = new TaskDialog(this, TransactionType.ADD);
        btnAddTask.setOnClickListener(view -> editTaskDialog.show(getSupportFragmentManager(), "MapFragment"));

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroup);
        MaterialButton userTab = findViewById(R.id.usertab);
        toggleGroup.check(userTab.getId());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);

        checkLocationPermissions();
        setupCollabListener();

        refreshTaskList();
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
    public void onMoreOptionClick(View view, TaskDetails task) {
        Context wrapper = new ContextThemeWrapper(this, R.style.popupMenuStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_task_options, popupMenu.getMenu());
        popupMenu.setForceShowIcon(true);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_view_task) {
                Toast.makeText(this, "View task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
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

    public void refreshTaskList() {
        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));

        userController.fetchUserTasks().thenAccept(tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                TaskListAdapter taskListAdapter = new TaskListAdapter(tasks, this, userController.getCurrentUser());
                recyclerViewTasks.setAdapter(taskListAdapter);
            } else {
                Toast.makeText(this, "No tasks found", Toast.LENGTH_SHORT).show();
            }
        });
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

        // TODO: Use real name of the task for notifications
        // TODO: Add quick accept and decline button for collab requests notifications
        String title = "Collaboration request for " + collabDetails.getTaskId();
        String description = collabDetails.getRequesterName() + " wants to join you!";
        sender.sendNotification(title, description);
    }
}
