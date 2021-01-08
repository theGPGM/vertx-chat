package org.george.hall.cache;

import org.george.hall.cache.bean.PlayerCacheBean;

import org.george.hall.cache.impl.PlayerCacheImpl;


public interface PlayerCache {

    PlayerCacheBean loadPlayerByPlayerName(String playerName);

    void addPlayer(PlayerCacheBean bean);

    PlayerCacheBean loadPlayerByPlayerId(Integer playerId);

    void updateSelective(PlayerCacheBean bean);

    long getTimeStamp(Integer playerId);

    boolean existTimeStamp(Integer playerId);

    static PlayerCache getInstance(){
        return PlayerCacheImpl.getInstance();
    }

    void addTimeStampIfNotExisting(Integer playerId);
}
