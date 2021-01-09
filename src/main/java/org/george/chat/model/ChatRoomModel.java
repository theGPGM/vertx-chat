package org.george.chat.model;

import org.george.chat.model.impl.ChatRoomModelImpl;
import org.george.hall.ClientCloseEventObserver;

public interface ChatRoomModel extends ClientCloseEventObserver {

    static ChatRoomModel getInstance(){
        return ChatRoomModelImpl.getInstance();
    }
}
