package org.george.chat.cache;

import org.george.chat.cache.impl.GameCacheImpl;

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
    List<String> getAllPlayerInRoom(String roomId);

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
    void addUserAction(String roomId, String userId, String action, Integer expireSecond);

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
    void clearCache(String roomId);

    /**
     * 获得用户出拳内容
     * @param roomId
     * @param userId
     * @return
     */
    String getUserAction(String roomId, String userId);

    void createGame(String roomId, String userId, Integer expireSecond);

    void addWaitingTime(String roomId, Integer expireSecond);

    boolean existsWaitingTime(String roomId);

    static GameCache getInstance(){
        return GameCacheImpl.getInstance();
    }
}
