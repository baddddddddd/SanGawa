package com.themabajogroup.sangawa.Utils;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.themabajogroup.sangawa.R;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null) {
            Log.d("GeofenceIntent", "Extras: " + intent.getExtras().toString());
        } else {
            Log.d("GeofenceIntent", "No extras in the intent");
        }

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent == null) {
            Log.e("GeofenceEvent", "GeofencingEvent is null");
            return;
        }

        if (geofencingEvent.hasError()) {
            Log.e("GeofenceBroadcast", "Error in geofence event: " + geofencingEvent.getErrorCode());
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // TODO: Notify when user enters a geofence of a task
            // TODO: Add task to "Nearby Tasks"

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            for (Geofence geofence : triggeringGeofences) {
                String geofenceId = geofence.getRequestId();
                sendNotification(context, "Geofence Entered: " + geofenceId, "You have entered the geofence area!");
            }

        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // TODO: Remove task from "Nearby Tasks"
            sendNotification(context, "Geofence Exited", "You have exited the geofence area!");
        }
    }

    private void sendNotification(Context context, String title, String message) {
        // TODO: Use NotificationSender class instead
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "GeofenceChannel")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(1, builder.build());
        }
    }
}
