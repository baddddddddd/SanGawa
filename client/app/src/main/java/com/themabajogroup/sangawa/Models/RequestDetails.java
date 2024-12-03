package com.themabajogroup.sangawa.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Map;

public class RequestDetails {
    private String requesterId;
    private String ownerId;
    private String taskId;
    private RequestStatus status;

    public RequestDetails(String requesterId, String ownerId, String taskId, RequestStatus status) {
        this.requesterId = requesterId;
        this.ownerId = ownerId;
        this.taskId = taskId;
        this.status = status;
    }

    public static RequestDetails fromDocumentSnapshot(DocumentSnapshot documentSnapshot) {
        Map<String, Object> data = documentSnapshot.getData();
        RequestDetails requestDetails = new RequestDetails(
                (String) data.get("requesterId"),
                (String) data.get("ownerId"),
                (String) data.get("taskId"),
                RequestStatus.valueOf((String) data.get("status"))
        );

        return requestDetails;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
