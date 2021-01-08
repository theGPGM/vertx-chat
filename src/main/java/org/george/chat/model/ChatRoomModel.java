package org.george.chat.model;

import org.george.chat.model.impl.ChatRoomModelImpl;
import org.george.hall.ClientCloseEventObserver;

import java.util.List;

public interface ChatRoomModel extends ClientCloseEventObserver {

    List<String> getRoomUsers(String roomId);

    boolean existChatRoom(String roomId);

    static ChatRoomModel getInstance(){
        return ChatRoomModelImpl.getInstance();
    }
}
