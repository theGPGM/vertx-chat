package org.george.cmd.model.bean;

public class CmdMessageResult {

    private String hId;

    private String message;

    public String gethId() {
        return hId;
    }

    public void sethId(String hId) {
        this.hId = hId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CmdMessageResult(String hId, String message) {
        this.hId = hId;
        this.message = message;
    }

    @Override
    public String toString() {
        return "CmdMessageResult{" +
                "hId='" + hId + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
