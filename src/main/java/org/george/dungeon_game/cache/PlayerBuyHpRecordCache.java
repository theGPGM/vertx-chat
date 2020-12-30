package org.george.dungeon_game.cache;

import org.george.dungeon_game.cache.impl.PlayerBuyHpRecordCacheImpl;

public interface PlayerBuyHpRecordCache {

    Integer getBuyHpCount(Integer playerId);

    void addBuyHpCount(Integer playerId, Long expireTimestamp);

    void incrBuyHpCount(Integer playerId);

    static PlayerBuyHpRecordCache getInstance(){
        return PlayerBuyHpRecordCacheImpl.getInstance();
    }
}
