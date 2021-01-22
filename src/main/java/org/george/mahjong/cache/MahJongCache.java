package org.george.mahjong.cache;

import org.george.mahjong.cache.bean.PlayerCacheBean;
import org.george.mahjong.cache.bean.RoomCacheBean;
import org.george.mahjong.cache.impl.MahJongCacheImpl;

import java.util.List;

public interface MahJongCache {

    RoomCacheBean getRoomByRoomId(int roomId);

    void addCacheBean(RoomCacheBean cacheBean);

    void deleteCacheBean(int roomId);

    void addPlayer(Integer roomId, PlayerCacheBean player);

    Integer getPlayerIndex(Integer roomId, Integer playerId);

    PlayerCacheBean getPlayerByIndex(Integer roomId, Integer index);

    PlayerCacheBean getPlayerByPlayerId(Integer roomId, Integer playerId);

    List<PlayerCacheBean> getAllPlayer(Integer roomId);

    PlayerCacheBean getZhuang(Integer roomId);

    void changZhuang(Integer roomId);

    static MahJongCache getInstance() {
        return MahJongCacheImpl.getInstance();
    }
}