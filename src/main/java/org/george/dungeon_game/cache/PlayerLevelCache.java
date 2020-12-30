package org.george.dungeon_game.cache;

import org.george.dungeon_game.cache.bean.PlayerLevelCacheBean;
import org.george.dungeon_game.cache.impl.PlayerLevelCacheImpl;

public interface PlayerLevelCache {

    PlayerLevelCacheBean getPlayerLevelByPlayerId(Integer playerId);

    void updatePlayerLevelSelective(PlayerLevelCacheBean level);

    void addPlayerLevel(PlayerLevelCacheBean level);

    static PlayerLevelCache getInstance(){
        return PlayerLevelCacheImpl.getInstance();
    }
}
