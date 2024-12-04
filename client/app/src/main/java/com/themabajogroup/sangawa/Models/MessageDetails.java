package com.themabajogroup.sangawa.Models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MessageDetails {
    private String senderId;
    private String senderName;
    private String taskId;
    private String content;
    private Date dateSent;


    public MessageDetails(String senderId, String senderName, String taskId, String content) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.taskId = taskId;
        this.content = content;
        this.dateSent = new Date();
    }

    public MessageDetails(String senderId, String senderName, String taskId, String content, Date dateSent) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.taskId = taskId;
        this.content = content;
        this.dateSent = dateSent;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();

        map.put("senderId", senderId);
        map.put("senderName", senderName);
        map.put("taskId", taskId);
        map.put("content", content);
        map.put("dateSent", dateSent.toString());

        return map;
    }

    public static MessageDetails fromMap(Map<String, String> map) {
        return new MessageDetails(
                map.get("senderId"),
                map.get("senderName"),
                map.get("taskId"),
                map.get("content"),
                new Date(map.get("dateSent"))
        );
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDateSent() {
        return dateSent;
    }

    public void setDateSent(Date dateSent) {
        this.dateSent = dateSent;
    }

    @Override
    public String toString() {
        return "MessageDetails{" +
                "senderId='" + senderId + '\'' +
                ", senderName='" + senderName + '\'' +
                ", taskId='" + taskId + '\'' +
                ", content='" + content + '\'' +
                ", dateSent=" + dateSent +
                '}';
    }
}
