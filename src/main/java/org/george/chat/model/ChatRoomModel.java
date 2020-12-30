package org.george.chat.model;

import org.george.chat.model.impl.ChatRoomModelImpl;

import java.util.List;

public interface ChatRoomModel {

    List<String> getRoomUsers(String roomId);

    boolean existChatRoom(String roomId);

    static ChatRoomModel getInstance(){
        return ChatRoomModelImpl.getInstance();
    }
}
