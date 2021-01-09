package org.george.hall.cache;

import org.george.hall.cache.bean.PlayerInfoCacheBean;

import org.george.hall.cache.impl.PlayerInfoInfoCacheImpl;


public interface PlayerInfoCache {

    void addPlayer(PlayerInfoCacheBean bean);

    PlayerInfoCacheBean loadPlayerByPlayerId(Integer playerId);

    void updateSelective(PlayerInfoCacheBean bean);

    Long getTimeStamp(Integer playerId);

    static PlayerInfoCache getInstance(){
        return PlayerInfoInfoCacheImpl.getInstance();
    }

    void addTimeStampIfNotExisting(Integer playerId);
}
