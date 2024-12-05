package com.themabajogroup.sangawa.Models;

public class CollabDetails {
    private String taskId;
    private String requesterId;
    private String requesterName;
    private RequestStatus status;


    public CollabDetails(String taskId, String requesterId, String requesterName, RequestStatus status) {
        this.taskId = taskId;
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
