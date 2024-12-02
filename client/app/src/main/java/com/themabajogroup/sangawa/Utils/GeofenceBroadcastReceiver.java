package com.themabajogroup.sangawa.Utils;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

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
        NotificationSender sender = NotificationSender.getInstance("GeofenceNotifcations", context);
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            for (Geofence geofence : triggeringGeofences) {
                String geofenceId = geofence.getRequestId();
                sender.sendNotification("Geofence Entered: " + geofenceId, "You have entered the geofence area!");
            }

        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            sender.sendNotification("Geofence Exited", "You have exited the geofence area!");
        }
    }
}
