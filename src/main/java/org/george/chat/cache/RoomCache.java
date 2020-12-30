package org.george.chat.cache;

import java.util.List;

public interface RoomCache {

    void join(String roomId, String userId);

    /**
     * 清除用户特定房间信息
     * @param userId
     * @param roomId
     */
    void clearUserRoomCache(String userId, String roomId);

    /**
     * 获取房间中所有用户 ID
     * @param roomId
     * @return
     */
    List<String> getAllUserId(String roomId);

    /**
     * 获取用户的所有房间 ID
     * @param userId
     * @return
     */
    List<String> getUserRoomIds(String userId);

    /**
     * 查看用户是否存在房间中
     * @param roomId
     * @return
     */
    boolean existsRoom(String roomId);
}
