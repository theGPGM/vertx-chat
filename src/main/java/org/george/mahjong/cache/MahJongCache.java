package org.george.mahjong.cache;

import org.george.mahjong.cache.bean.MahJongPlayerCacheBean;
import org.george.mahjong.cache.bean.MahJongRoomCacheBean;
import org.george.mahjong.cache.impl.MahJongCacheImpl;

public interface MahJongCache {

    MahJongRoomCacheBean getRoomByRoomId(int roomId);

    void addCacheBean(MahJongRoomCacheBean cacheBean);

    void deleteCacheBean(int roomId);

    static MahJongCache getInstance() {
        return MahJongCacheImpl.getInstance();
    }

    void addPlayer(Integer roomId, MahJongPlayerCacheBean player);

    Integer getPlayerIndex(Integer roomId, Integer playerId);

    MahJongPlayerCacheBean getPlayerByIndex(Integer roomId, Integer index);

    MahJongPlayerCacheBean getPlayerByPlayerId(Integer roomId, Integer playerId);
}