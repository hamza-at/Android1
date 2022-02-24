package com.example.geosafe.model;

public class Chat {
    private String sender;
    private String receiver;
    private String message;
    private Boolean seen;

    public Chat(String sender,String receiver,String message,Boolean seen) {
        this.sender = sender;
        this.receiver=receiver;
        this.message=message;
        this.seen=seen;
    }
    public Chat(){}

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
