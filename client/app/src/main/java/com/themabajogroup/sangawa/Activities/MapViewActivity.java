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
import android.util.Log;
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
import com.google.android.gms.maps.model.Marker;
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
import com.themabajogroup.sangawa.Models.RequestDetails;
import com.themabajogroup.sangawa.Models.RequestStatus;
import com.themabajogroup.sangawa.Models.TaskDetails;
import com.themabajogroup.sangawa.Models.TransactionType;
import com.themabajogroup.sangawa.Models.UserProfile;
import com.themabajogroup.sangawa.Overlays.TaskDialog;
import com.themabajogroup.sangawa.Overlays.TaskListAdapter;
import com.themabajogroup.sangawa.R;
import com.themabajogroup.sangawa.Utils.Converter;
import com.themabajogroup.sangawa.Utils.GeofenceBroadcastReceiver;
import com.themabajogroup.sangawa.Utils.NotificationSender;
import com.themabajogroup.sangawa.databinding.ActivityMapViewBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private Map<String, Map<String, CollabDetails>> collabRequests;
    private Map<String, TaskDetails> currentTasks;
    private Map<String, RequestDetails> currentRequests;
    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton userTab, sharedTab;
    private RecyclerView recyclerViewUserTasks, recyclerViewNearbyTasks;
    private UserProfile userProfile;
    private List<Marker> userTaskMarkers;
    private List<Marker> sharedTaskMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userController = UserController.getInstance();
        taskController = TaskController.getInstance();
        collabRequests = userController.getCollabRequests();
        currentTasks = new HashMap<>();
        currentRequests = new HashMap<>();
        userTaskMarkers = new ArrayList<>();
        sharedTaskMarkers = new ArrayList<>();

        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        ImageButton btnAddTask = findViewById(R.id.add_task_button);
        TaskDialog editTaskDialog = new TaskDialog(this, TransactionType.ADD);
        btnAddTask.setOnClickListener(view -> editTaskDialog.show(getSupportFragmentManager(), "MapFragment"));

        toggleGroup = findViewById(R.id.toggleGroup);
        userTab = findViewById(R.id.usertab);
        sharedTab = findViewById(R.id.sharedtab);
        recyclerViewUserTasks = findViewById(R.id.recyclerViewUserTasks);
        recyclerViewNearbyTasks = findViewById(R.id.recyclerViewNearbyTasks);
        LinearLayout shareTabList = findViewById(R.id.sharedTabList);

        toggleGroup.check(userTab.getId());
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            Log.d("ToggleGroup", "Checked ID: " + checkedId + ", Is Checked: " + isChecked);
            if (isChecked) {
                if (checkedId == userTab.getId()) {
                    recyclerViewUserTasks.setVisibility(View.VISIBLE);
                    shareTabList.setVisibility(View.GONE);
                } else if (checkedId == sharedTab.getId()) {
                    recyclerViewUserTasks.setVisibility(View.GONE);
                    shareTabList.setVisibility(View.VISIBLE);
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);

        checkLocationPermissions();
        userController.fetchProfile().thenAccept(isSuccess -> {
            initializeTaskList();

            if (isSuccess) {
                userProfile = userController.getProfile();
            } else {
                Toast.makeText(MapViewActivity.this, "Unable to get profile information.", Toast.LENGTH_SHORT).show();
            }
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
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLastLocation() == null) {
                    Toast.makeText(MapViewActivity.this, "Unable to get location updates.", Toast.LENGTH_SHORT).show();
                    return;
                }

                LatLng lastLocation = userController.getCurrentLocation();
                LatLng newLocation = new LatLng(
                        locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude()
                );
                userController.setCurrentLocation(newLocation);

                if (lastLocation != null) {
                    double distance = Converter.getDistance(lastLocation, newLocation);
                    final double SCAN_REFRESH_DISTANCE = 1000;

                    if (distance >= SCAN_REFRESH_DISTANCE) {
                        refreshNearbyTaskList();
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    // TODO: Setup Geofence for Join shared tasks only
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
    }


    @Override
    public void onMoreOptionClick(View view, TaskDetails task, Boolean isCurrentUserTask) {
        Context wrapper = new ContextThemeWrapper(this, R.style.popupMenuStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_task_options, popupMenu.getMenu());
        MenuItem acceptTaskMenuItem = popupMenu.getMenu().findItem(R.id.menu_request_task);
        MenuItem doneTaskMenuItem = popupMenu.getMenu().findItem(R.id.menu_done_task);
        MenuItem editTaskMenuItem = popupMenu.getMenu().findItem(R.id.menu_edit_task);
        MenuItem deleteTaskMenuItem = popupMenu.getMenu().findItem(R.id.menu_delete_task);
        acceptTaskMenuItem.setVisible(!isCurrentUserTask);
        doneTaskMenuItem.setVisible(isCurrentUserTask);
        editTaskMenuItem.setVisible(isCurrentUserTask);
        deleteTaskMenuItem.setVisible(isCurrentUserTask);

        popupMenu.setForceShowIcon(true);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_view_task) {
                LatLng taskLocation = new LatLng(task.getLocationLat(), task.getLocationLon());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(taskLocation, 16f));
                BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else if (itemId == R.id.menu_request_task) {
                sendCollabRequest(task).thenAccept(isSuccess -> {
                    Toast.makeText(this, "Request for" + task.getTitle() + "Sent", Toast.LENGTH_SHORT).show();
                    refreshPendingCollabList();
                });
            } else if (itemId == R.id.menu_done_task) {
                Toast.makeText(this, "Finished task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.menu_edit_task) {
                TaskDialog editTaskDialog = new TaskDialog(this, TransactionType.EDIT, task);
                editTaskDialog.show(getSupportFragmentManager(), "MapFragment");
                refreshUserTaskList();
            } else if (itemId == R.id.menu_delete_task) {
                taskController.deleteUserTask(task.getTaskId());
                refreshUserTaskList();
                Toast.makeText(this, "Deleted " + task.getTitle() + " successfully!", Toast.LENGTH_SHORT).show();
            } else {
                return false;
            }
            return true;
        });

        popupMenu.show();
    }

    public void initializeTaskList() {
        refreshUserTaskList().thenAccept(unused -> {
            setupCollabListener();
            setupPendingCollabRequests();
        });
        refreshNearbyTaskList();
        refreshPendingCollabList();

    }

    public CompletableFuture<Void> refreshUserTaskList() {
        CompletableFuture<Void> result = new CompletableFuture<>();

        recyclerViewUserTasks.setLayoutManager(new LinearLayoutManager(this));

        userController.fetchUserTasks().thenAccept(tasks -> {
            if (tasks == null) {
                Toast.makeText(this, "No tasks found", Toast.LENGTH_SHORT).show();
                return;
            }

            TaskListAdapter taskListAdapter = new TaskListAdapter(tasks, this, true);
            recyclerViewUserTasks.setAdapter(taskListAdapter);

            for (TaskDetails taskDetails : tasks) {
                currentTasks.put(taskDetails.getTaskId(), taskDetails);
            }

            refreshUserTaskMarkers(tasks);
            result.complete(null);
        });

        return result;
    }

    public void refreshNearbyTaskList() {
        recyclerViewNearbyTasks.setLayoutManager(new LinearLayoutManager(this));
        LinearLayout nearbyLayout = findViewById(R.id.layout_nearby);

        userController.fetchNearbyTasks().thenAccept(tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                List<TaskDetails> filteredTasks = new ArrayList<>();
                Set<String> pendingRequestIds = currentRequests.keySet();

                for (TaskDetails task : tasks) {
                    if (!pendingRequestIds.contains(task.getTaskId())) {
                        filteredTasks.add(task);
                    }
                }

                if (!filteredTasks.isEmpty()) {
                    nearbyLayout.setVisibility(View.VISIBLE);
                    TaskListAdapter taskListAdapter = new TaskListAdapter(filteredTasks, this, false);
                    recyclerViewNearbyTasks.setAdapter(taskListAdapter);

                    for (TaskDetails taskDetails : filteredTasks) {
                        currentTasks.put(taskDetails.getTaskId(), taskDetails);
                    }

                } else {
                    nearbyLayout.setVisibility(View.GONE);
                }

                refreshNearbyTaskMarkers(tasks);
            } else {
                nearbyLayout.setVisibility(View.GONE);
            }
        });
    }


    public void refreshPendingCollabList() {
        RecyclerView recyclerViewPendingRequests = findViewById(R.id.recyclerViewPendingRequest);
        LinearLayout pendingLayout = findViewById(R.id.layout_pending);
        recyclerViewPendingRequests.setLayoutManager(new LinearLayoutManager(this));

        taskController.getRequestHistory(userController.getCurrentUser().getUid()).thenAccept(request -> {
            if (request != null && !request.isEmpty()) {
                pendingLayout.setVisibility(View.VISIBLE);
                TaskListAdapter taskListAdapter = new TaskListAdapter(request, this, false);
                recyclerViewPendingRequests.setAdapter(taskListAdapter);

                for (RequestDetails requestDetails : request) {
                    if (requestDetails.getStatus() == RequestStatus.PENDING) {
                        currentRequests.put(requestDetails.getTaskId(), requestDetails);
                    }
                }

            } else{
                pendingLayout.setVisibility(View.GONE);
            }
        });

    }


    public void refreshUserTaskMarkers(List<TaskDetails> taskDetailsList) {
        List<Marker> markers = new ArrayList<>();

        for (TaskDetails taskDetails : taskDetailsList) {
            LatLng location = new LatLng(taskDetails.getLocationLat(), taskDetails.getLocationLon());

            // TODO: Do not create new geofence for existing tasks
            setupGeofence(taskDetails.getTitle(), location, userProfile.getFencingRadius());

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location)
                    .title(taskDetails.getTitle())
                    .snippet(taskDetails.getDescription());

            Marker marker = mMap.addMarker(markerOptions);
            markers.add(marker);
        }

        for (Marker marker : userTaskMarkers) {
            marker.remove();
        }

        userTaskMarkers = markers;
    }

    public void refreshNearbyTaskMarkers(List<TaskDetails> taskDetailsList) {
        List<Marker> markers = new ArrayList<>();
        // TODO: Remove existing geofence for nearby tasks, then proceed to add new ones
        for (TaskDetails taskDetails : taskDetailsList) {
            LatLng location = new LatLng(taskDetails.getLocationLat(), taskDetails.getLocationLon());

            setupGeofence(taskDetails.getTitle(), location, userProfile.getFencingRadius());

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(location)
                    .title(taskDetails.getTitle())
                    .snippet(taskDetails.getDescription());

            Marker marker = mMap.addMarker(markerOptions);
            markers.add(marker);
        }

        for (Marker marker : sharedTaskMarkers) {
            marker.remove();
        }

        sharedTaskMarkers = markers;
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

                if (status == RequestStatus.ACCEPTED) {
                    String title = "Collaboration request ACCEPTED!";
                    String description = "Your request to join " + taskDetails.getTitle() + " has been accepted";

                    sender.sendNotification(title, description);

                    removeCollabListener(taskDetails);

                } else if (status == RequestStatus.DECLINED) {
                    String title = "Collaboration request DECLINED!";
                    String description = "Your request to join " + taskDetails.getTitle() + " has been declined";

                    sender.sendNotification(title, description);

                    removeCollabListener(taskDetails);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setupPendingCollabRequests() {
        String userId = userController.getCurrentUser().getUid();

        taskController.getRequestHistory(userId)
                .thenAccept(requests -> {

                    for (RequestDetails details : requests) {
                        String taskId = details.getTaskId();

                        TaskDetails taskDetails = currentTasks.get(taskId);

                        if (taskDetails != null) {
                            addCollabReplyListener(taskDetails);
                            continue;
                        }

                        getTaskByIdForced(taskId).thenAccept(fetchedTaskDetails -> {
                           addCollabReplyListener(fetchedTaskDetails);
                        });
                    }
                });
    }

    public CompletableFuture<TaskDetails> getTaskByIdForced(String taskId) {
        CompletableFuture<TaskDetails> result = new CompletableFuture<>();

        if (currentTasks.containsKey(taskId)) {
            result.complete(currentTasks.get(taskId));
            return result;
        }

        taskController.getUserTask(taskId).thenAccept(taskDetails -> {
            currentTasks.put(taskId, taskDetails);
            result.complete(taskDetails);
        });

        return result;
    }

    public CompletableFuture<Boolean> removeCollabListener(TaskDetails taskDetails) {
        CompletableFuture<Boolean> result = new CompletableFuture<>();

        String requesterId = userController.getCurrentUser().getUid();
        String ownerId = taskDetails.getUserId();
        String taskId = taskDetails.getTaskId();
        taskController.removeJoinRequest(ownerId, taskId, requesterId).thenAccept(result::complete);

        return result;
    }
}
