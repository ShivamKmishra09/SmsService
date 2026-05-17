package com.example.demo.model;

public class SmsEvent {

    private String phoneNumber;
    private String message;
    private String status;
    private String reason;
    private String userId;

    public SmsEvent() {
    }

    public SmsEvent(
            String userId,
            String phoneNumber,
            String message,
            String status,
            String reason
    ) {

        this.phoneNumber = phoneNumber;
        this.message = message;
        this.status = status;
        this.reason = reason;
        this.userId = userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}