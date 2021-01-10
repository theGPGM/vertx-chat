package org.george.cmd.pojo;

public class Message {

    private String sendToUser;

    private String message;

    public String getSendToUser() {
        return sendToUser;
    }

    public void setSendToUser(String sendToUser) {
        this.sendToUser = sendToUser;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Message(String sendToUser, String message) {
        this.sendToUser = sendToUser;
        this.message = message;
    }

    public Message() {
    }
}
