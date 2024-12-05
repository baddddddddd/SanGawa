package com.themabajogroup.sangawa.Models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class CollabRequest {
    public String requesterName;
    public RequestStatus status;

    public CollabRequest() {

    }

    public CollabRequest(String requesterName, RequestStatus status) {
        this.requesterName = requesterName;
        this.status = status;
    }
}
