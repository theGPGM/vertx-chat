package org.george.chat.model.impl;

import org.george.chat.cache.RoomCache;

import org.george.chat.cache.impl.RoomCacheImpl;
import org.george.chat.model.ChatRoomModel;
import org.george.hall.model.PlayerModel;
import java.util.List;


public class ChatRoomModelImpl implements ChatRoomModel {

  private static ChatRoomModelImpl instance = new ChatRoomModelImpl();

  public static ChatRoomModelImpl getInstance(){
    return instance;
  }

  private RoomCache roomCache = RoomCacheImpl.getInstance();

  private PlayerModel playerModel = PlayerModel.getInstance();
  
  private ChatRoomModelImpl(){}

  @Override
  public List<String> getRoomUsers(String roomId) {
    return roomCache.getAllUserId(roomId);
  }

  @Override
  public boolean existChatRoom(String roomId) {
    return roomCache.existsRoom(roomId);
  }

  @Override
  public void clientCloseNotify(String hId) {
    String userId = playerModel.getUId(hId);
    List<String> roomIds = roomCache.getUserRoomIds(userId);
    for(String roomId : roomIds){
      roomCache.clearUserRoomCache(userId, roomId);
    }
  }
}
