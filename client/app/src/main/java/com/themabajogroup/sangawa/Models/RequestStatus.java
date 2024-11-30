package com.themabajogroup.sangawa.Models;

public enum RequestStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    CANCELLED,
    ;

    @Override
    public String toString() {
        return this.name();
    }
}
