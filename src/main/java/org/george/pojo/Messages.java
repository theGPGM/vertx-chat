package org.george.pojo;


import java.util.List;

public class Messages {

    private List<Message> messageList;

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    public Messages(List<Message> messageList) {
        this.messageList = messageList;
    }
}
