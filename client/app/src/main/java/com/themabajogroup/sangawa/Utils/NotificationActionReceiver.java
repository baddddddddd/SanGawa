package com.themabajogroup.sangawa.Utils;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.themabajogroup.sangawa.Controllers.TaskController;
import com.themabajogroup.sangawa.Controllers.UserController;
import com.themabajogroup.sangawa.Models.RequestStatus;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String action = intent.getAction();
        if ("COLLAB_ACCEPT".equals(action)) {
            String taskId = intent.getStringExtra("TASK_ID");
            String requesterId = intent.getStringExtra("REQUESTER_ID");
            int notificationId = intent.getIntExtra("NOTIFICATION_ID", -1);

            if (notificationManager != null && notificationId != -1) {
                notificationManager.cancel(notificationId);
            }

            UserController userController = UserController.getInstance();
            String ownerId = userController.getCurrentUser().getUid();

            TaskController taskController = TaskController.getInstance();
            taskController.updateJoinRequest(ownerId, taskId, requesterId, RequestStatus.ACCEPTED)
                    .thenAccept(isSuccess -> {
                        String message = isSuccess
                                ? "Accepted collab request"
                                : "Failed to accept collab request";

                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    });

        } else if ("COLLAB_DECLINE".equals(action)) {
            String taskId = intent.getStringExtra("TASK_ID");
            String requesterId = intent.getStringExtra("REQUESTER_ID");
            int notificationId = intent.getIntExtra("NOTIFICATION_ID", -1);

            if (notificationManager != null && notificationId != -1) {
                notificationManager.cancel(notificationId);
            }

            UserController userController = UserController.getInstance();
            String ownerId = userController.getCurrentUser().getUid();

            TaskController taskController = TaskController.getInstance();
            taskController.updateJoinRequest(ownerId, taskId, requesterId, RequestStatus.DECLINED)
                    .thenAccept(isSuccess -> {
                        String message = isSuccess
                                ? "Declined collab request"
                                : "Failed to decline collab request";

                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
