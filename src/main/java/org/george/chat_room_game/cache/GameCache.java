package org.george.chat_room_game.cache;

import java.util.List;
import java.util.Map;

public interface GameCache {

    /**
     * 加入游戏
     * @param roomId
     * @param userId
     */
    void addGameUser(String roomId, String userId);

    /**
     * 获取特定房间中所有参加游戏的玩家 ID
     * @param roomId
     * @return
     */
    List<String> getPlayUserList(String roomId);

    /**
     * 查看房间中是否在玩游戏
     * @param roomId
     * @return
     */
    boolean gameExists(String roomId);

    /**
     * 玩家猜拳
     * @param roomId
     * @param userId
     * @param action
     */
    void addUserAction(String roomId, String userId, String action);

    /**
     * 获取玩家猜拳的内容
     * @param roomId
     * @return
     */
    Map<String, String> getAllUserAction(String roomId);

    /**
     * 删除房间
     * @param roomId
     */
    void removeRoom(String roomId);

    /**
     * 获得用户出拳内容
     * @param roomId
     * @param userId
     * @return
     */
    String getUserAction(String roomId, String userId);
}