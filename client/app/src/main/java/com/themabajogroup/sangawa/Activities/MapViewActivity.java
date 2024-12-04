package com.themabajogroup.sangawa.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.themabajogroup.sangawa.Models.TaskStatus;
import com.themabajogroup.sangawa.Models.TaskType;
import com.themabajogroup.sangawa.Models.TransactionType;
import com.themabajogroup.sangawa.Models.UserProfile;
import com.themabajogroup.sangawa.Overlays.ChatDialog;
import com.themabajogroup.sangawa.Overlays.SettingsDialog;
import com.themabajogroup.sangawa.Overlays.TaskDialog;
import com.themabajogroup.sangawa.Overlays.TaskListAdapter;
import com.themabajogroup.sangawa.R;
import com.themabajogroup.sangawa.Utils.Converter;
import com.themabajogroup.sangawa.Utils.GeofenceBroadcastReceiver;
import com.themabajogroup.sangawa.Utils.NotificationSender;
import com.themabajogroup.sangawa.databinding.ActivityMapViewBinding;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
    public static Map<String, TaskDetails> currentTasks;
    private Map<String, RequestDetails> currentRequests;
    private MaterialButton userTab, sharedTab;
    private RecyclerView recyclerViewNearbyTasks;
    private UserProfile userProfile;
    private List<Marker> userTaskMarkers;
    private List<Marker> sharedTaskMarkers;
    private Set<String> geofencedTasks;
    private LatLng lastRefreshLocation;
    private final float cameraZoomLevel = 17;

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
        geofencedTasks = new HashSet<>();

        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        ImageButton btnAddTask = findViewById(R.id.add_task_button);

        ImageButton editProfileButton = findViewById(R.id.settings_button);

        TaskDialog editTaskDialog = new TaskDialog(this, TransactionType.ADD);
        btnAddTask.setOnClickListener(view -> editTaskDialog.show(getSupportFragmentManager(), "MapFragment"));

        SettingsDialog settingProfile = new SettingsDialog(this);
        editProfileButton.setOnClickListener(view -> settingProfile.show());

        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.toggleGroup);
        userTab = findViewById(R.id.usertab);
        sharedTab = findViewById(R.id.sharedtab);
        recyclerViewNearbyTasks = findViewById(R.id.recyclerViewNearbyTasks);
        NestedScrollView shareTabList = findViewById(R.id.sharedTabList);
        NestedScrollView userTabList = findViewById(R.id.userTabList);

        toggleGroup.check(userTab.getId());
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            Log.d("ToggleGroup", "Checked ID: " + checkedId + ", Is Checked: " + isChecked);
            if (isChecked) {
                if (checkedId == userTab.getId()) {
                    userTabList.setVisibility(View.VISIBLE);
                    shareTabList.setVisibility(View.GONE);
                } else if (checkedId == sharedTab.getId()) {
                    userTabList.setVisibility(View.GONE);
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

                LatLng newLocation = new LatLng(
                        locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude()
                );

                userController.setCurrentLocation(newLocation);

                if (lastRefreshLocation != null) {
                    double distance = Converter.getDistance(lastRefreshLocation, newLocation);
                    final double SCAN_REFRESH_DISTANCE = 1000;

                    if (distance >= SCAN_REFRESH_DISTANCE) {
                        lastRefreshLocation = newLocation;
                        refreshSharedTaskLists();
                    }
                } else {
                    lastRefreshLocation = newLocation;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, cameraZoomLevel));
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void setupGeofence(TaskDetails taskDetails) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermissions();
            return;
        }

        if (geofencedTasks.contains(taskDetails.getTaskId())) {
            return;
        }

        if (!currentTasks.containsKey(taskDetails.getTaskId())) {
            currentTasks.put(taskDetails.getTaskId(), taskDetails);
        }

        float radius = userController.getProfile().getFencingRadius();
        Geofence geofence = new Geofence.Builder()
                .setRequestId(taskDetails.getTaskId())
                .setCircularRegion(taskDetails.getLocationLat(), taskDetails.getLocationLon(), radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        geofencingClient.addGeofences(
                new GeofencingRequest.Builder()
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                        .addGeofence(geofence)
                        .build(),
                pendingIntent
        ).addOnSuccessListener(aVoid ->
                geofencedTasks.add(taskDetails.getTaskId())
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to add task geofence: " + taskDetails.getTitle(), Toast.LENGTH_SHORT).show()
        );
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bsuAlangilan, cameraZoomLevel));
        userController.setCurrentLocation(bsuAlangilan);

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
    }


    @Override
    public void onMoreOptionClick(View view, TaskDetails task, TaskType taskType) {
        Context wrapper = new ContextThemeWrapper(this, R.style.popupMenuStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_task_options, popupMenu.getMenu());
        MenuItem requestTaskMenuItem = popupMenu.getMenu().findItem(R.id.menu_request_task);
        MenuItem cancelRequestMenuItem = popupMenu.getMenu().findItem(R.id.menu_cancel_request);
        MenuItem messageMenuItem = popupMenu.getMenu().findItem(R.id.menu_open_chatroom);
        MenuItem doneTaskMenuItem = popupMenu.getMenu().findItem(R.id.menu_done_task);
        MenuItem editTaskMenuItem = popupMenu.getMenu().findItem(R.id.menu_edit_task);
        MenuItem deleteTaskMenuItem = popupMenu.getMenu().findItem(R.id.menu_delete_task);
        taskType.setVisibilityFor(requestTaskMenuItem, cancelRequestMenuItem, doneTaskMenuItem, editTaskMenuItem, deleteTaskMenuItem, messageMenuItem);

        popupMenu.setForceShowIcon(true);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_view_task) {
                LatLng taskLocation = new LatLng(task.getLocationLat(), task.getLocationLon());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(taskLocation, cameraZoomLevel));
                BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet));
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else if (itemId == R.id.menu_request_task) {
                sendCollabRequest(task).thenAccept(isSuccess -> {
                    Toast.makeText(this, "Request for " + task.getTitle() + " Sent", Toast.LENGTH_SHORT).show();
                    refreshSharedTaskLists();
                });
            } else if (itemId == R.id.menu_done_task) {
                geofencingClient.removeGeofences(List.of(task.getTaskId()));
                geofencedTasks.remove(task.getTaskId());
                taskController.editUserTaskStatus(task.getTaskId(), TaskStatus.COMPLETED).thenAccept(isSuccess -> {
                    refreshUserTaskList();
                    Toast.makeText(this, "Finished task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
                });
                Toast.makeText(this, "Finished task: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.menu_edit_task) {
                TaskDialog editTaskDialog = new TaskDialog(this, TransactionType.EDIT, task);
                editTaskDialog.show(getSupportFragmentManager(), "MapFragment");
                refreshUserTaskList();
            } else if (itemId == R.id.menu_delete_task) {
                taskController.deleteUserTask(task.getTaskId());
                currentTasks.remove(task.getTaskId());
                geofencingClient.removeGeofences(List.of(task.getTaskId()));
                geofencedTasks.remove(task.getTaskId());
                refreshUserTaskList();
                Toast.makeText(this, "Deleted " + task.getTitle() + " successfully!", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.menu_open_chatroom){
                ChatDialog chatDialog = new ChatDialog(this, task.getTaskId(), userController.getCurrentUser().getUid(), task.getTitle());
                chatDialog.show();
            }
            else {
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
        startTaskRefresherLoop();
    }


    public CompletableFuture<Void> refreshUserTaskList() {
        CompletableFuture<Void> result = new CompletableFuture<>();

        RecyclerView recyclerViewActiveTask = findViewById(R.id.recyclerViewActiveTasks);
        LinearLayout activeLayout = findViewById(R.id.layout_active);
        recyclerViewActiveTask.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView recyclerViewDueTask = findViewById(R.id.recyclerViewOverdueTasks);
        LinearLayout dueLayout = findViewById(R.id.layout_due);
        recyclerViewDueTask.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView recyclerViewCompletedTask = findViewById(R.id.recyclerViewCompletedTasks);
        LinearLayout completedLayout = findViewById(R.id.layout_complete);
        recyclerViewCompletedTask.setLayoutManager(new LinearLayoutManager(this));

        userController.fetchUserTasks().thenAccept(tasks -> {
            if (tasks != null) {
                Set<String> processedTaskIds = new HashSet<>();

                List<TaskDetails> completedTask = tasks.stream()
                        .filter(r -> !processedTaskIds.contains(r.getTaskId()) && r.getStatus() == TaskStatus.COMPLETED)
                        .peek(r -> {
                            processedTaskIds.add(r.getTaskId());
                            currentTasks.put(r.getTaskId(), r);
                        })
                        .collect(Collectors.toList());

                List<TaskDetails> dueTask = tasks.stream()
                        .filter(r -> !processedTaskIds.contains(r.getTaskId())
                                && LocalDateTime.ofInstant(r.getDeadline().toInstant(), ZoneId.systemDefault()).isBefore(LocalDateTime.now()))
                        .peek(r -> {
                            processedTaskIds.add(r.getTaskId());
                            currentTasks.put(r.getTaskId(), r);
                        })
                        .collect(Collectors.toList());

                List<TaskDetails> activeTask = tasks.stream()
                        .filter(r -> !processedTaskIds.contains(r.getTaskId()) && r.getStatus() == TaskStatus.PENDING)
                        .peek(r -> {
                            processedTaskIds.add(r.getTaskId());
                            currentTasks.put(r.getTaskId(), r);
                        })
                        .collect(Collectors.toList());


                if (!activeTask.isEmpty()) {
                    activeLayout.setVisibility(View.VISIBLE);
                    recyclerViewActiveTask.setAdapter(new TaskListAdapter(activeTask, this, TaskType.ACTIVE));
                } else {
                    activeLayout.setVisibility(View.GONE);
                }

                if (!dueTask.isEmpty()) {
                    dueLayout.setVisibility(View.VISIBLE);
                    recyclerViewDueTask.setAdapter(new TaskListAdapter(dueTask, this, TaskType.DUE));
                } else {
                    dueLayout.setVisibility(View.GONE);
                }

                if (!completedTask.isEmpty()) {
                    completedLayout.setVisibility(View.VISIBLE);
                    recyclerViewCompletedTask.setAdapter(new TaskListAdapter(completedTask, this, TaskType.COMPLETE));
                } else {
                    completedLayout.setVisibility(View.GONE);
                }

                refreshUserTaskMarkers(tasks);
                result.complete(null);
            } else {
                activeLayout.setVisibility(View.GONE);
                dueLayout.setVisibility(View.GONE);
                completedLayout.setVisibility(View.GONE);
            }
        });

        return result;
    }

    public void refreshSharedTaskLists() {
        RecyclerView recyclerViewNearby = findViewById(R.id.recyclerViewNearbyTasks);
        LinearLayout nearbyLayout = findViewById(R.id.layout_nearby);
        recyclerViewNearby.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView recyclerViewPending = findViewById(R.id.recyclerViewPendingRequest);
        LinearLayout pendingLayout = findViewById(R.id.layout_pending);
        recyclerViewPending.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView recyclerViewJoined = findViewById(R.id.recyclerViewJoinedTasks);
        LinearLayout joinedLayout = findViewById(R.id.layout_joined);
        recyclerViewJoined.setLayoutManager(new LinearLayoutManager(this));

        userController.fetchNearbyTasks().thenAccept(tasks -> {
            taskController.getRequestHistory(userController.getCurrentUser().getUid()).thenAccept(request -> {
                if (tasks != null && request != null) {
                    List<TaskDetails> filteredNearbyTasks = new ArrayList<>();
                    List<RequestDetails> pendingRequests = new ArrayList<>();
                    List<RequestDetails> acceptedRequests = new ArrayList<>();
                    Set<String> excludedTaskIds = new HashSet<>();

                    pendingRequests = request.stream()
                            .filter(r -> r.getStatus() == RequestStatus.PENDING)
                            .peek(r -> excludedTaskIds.add(r.getTaskId()))
                            .collect(Collectors.toList());

                    acceptedRequests = request.stream()
                            .filter(r -> r.getStatus() == RequestStatus.ACCEPTED)
                            .peek(r -> excludedTaskIds.add(r.getTaskId()))
                            .collect(Collectors.toList());

                    for (TaskDetails task : tasks) {
                        if (!excludedTaskIds.contains(task.getTaskId())) {
                            filteredNearbyTasks.add(task);
                        }
                    }

                    if (!filteredNearbyTasks.isEmpty()) {
                        nearbyLayout.setVisibility(View.VISIBLE);
                        TaskListAdapter taskListAdapter = new TaskListAdapter(filteredNearbyTasks, this, TaskType.NEARBY);
                        recyclerViewNearby.setAdapter(taskListAdapter);
                        filteredNearbyTasks.forEach(task -> currentTasks.put(task.getTaskId(), task));
                    } else {
                        nearbyLayout.setVisibility(View.GONE);
                    }

                    if (!pendingRequests.isEmpty()) {
                        pendingLayout.setVisibility(View.VISIBLE);
                        recyclerViewPending.setAdapter(new TaskListAdapter(pendingRequests, this, TaskType.PENDING));
                    } else {
                        pendingLayout.setVisibility(View.GONE);
                    }

                    if (!acceptedRequests.isEmpty()) {
                        joinedLayout.setVisibility(View.VISIBLE);
                        recyclerViewJoined.setAdapter(new TaskListAdapter(acceptedRequests, this, TaskType.JOINED));
                    } else {
                        joinedLayout.setVisibility(View.GONE);
                    }

                    refreshNearbyTaskMarkers(filteredNearbyTasks);
                } else {
                    nearbyLayout.setVisibility(View.GONE);
                    pendingLayout.setVisibility(View.GONE);
                    joinedLayout.setVisibility(View.GONE);
                }
            });
        });
    }


    public void refreshUserTaskMarkers(List<TaskDetails> taskDetailsList) {
        List<Marker> markers = new ArrayList<>();

        for (TaskDetails taskDetails : taskDetailsList) {
            if (taskDetails.getStatus() == TaskStatus.COMPLETED) {
                continue;
            }

            LatLng location = new LatLng(taskDetails.getLocationLat(), taskDetails.getLocationLon());

            setupGeofence(taskDetails);

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

        for (TaskDetails taskDetails : taskDetailsList) {
            if (taskDetails.getStatus() == TaskStatus.COMPLETED) {
                continue;
            }

            LatLng location = new LatLng(taskDetails.getLocationLat(), taskDetails.getLocationLon());

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
                    } else {
                        addCollabReplyListener(taskDetails);
                        result.complete(true);
                    }
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
                    setupGeofence(taskDetails);

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

    public void startTaskRefresherLoop() {
        Handler handler = new Handler(Looper.getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                refreshSharedTaskLists();
                handler.postDelayed(this, 60000);
            }
        };

        handler.post(runnable);
    }

    public void signOutUser() {
        userController.signOutUser();
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
        finish();
    }
}
