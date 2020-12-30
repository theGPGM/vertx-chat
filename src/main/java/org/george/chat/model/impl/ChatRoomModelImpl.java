package org.george.chat.model.impl;

import org.george.chat.cache.RoomCache;
import org.george.chat.cache.RoomCacheImpl;
import org.george.chat.model.ChatRoomModel;

import java.util.List;


public class ChatRoomModelImpl implements ChatRoomModel {

  private static ChatRoomModelImpl instance = new ChatRoomModelImpl();

  public static ChatRoomModelImpl getInstance(){
    return instance;
  }

  private RoomCache roomCache = RoomCacheImpl.getInstance();
  
  private ChatRoomModelImpl(){}

  @Override
  public List<String> getRoomUsers(String roomId) {
    return roomCache.getAllUserId(roomId);
  }

  @Override
  public boolean existChatRoom(String roomId) {
    return roomCache.existsRoom(roomId);
  }
}
