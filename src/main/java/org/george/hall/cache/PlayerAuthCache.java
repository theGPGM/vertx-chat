package org.george.hall.cache;

import org.george.hall.cache.bean.PlayerAuthCacheBean;
import org.george.hall.cache.impl.PlayerAuthCacheImpl;

public interface PlayerAuthCache {

    PlayerAuthCacheBean loadPlayerAuthCacheBeanByName(String playerName);

    void addPlayer(PlayerAuthCacheBean cacheBean);

    void updatePlayerSelective(PlayerAuthCacheBean cacheBean);

    void deletePlayer(Integer playerId);

    static PlayerAuthCache getInstance(){
        return PlayerAuthCacheImpl.getInstance();
    }
}
