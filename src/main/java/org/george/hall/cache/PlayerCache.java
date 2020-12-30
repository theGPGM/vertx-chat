package org.george.hall.cache;

import org.george.hall.cache.bean.PlayerCacheBean;
import org.george.hall.cache.impl.PlayerCacheImpl;

import java.util.List;

public interface PlayerCache {

    PlayerCacheBean loadPlayerByPlayerName(String playerName);

    void addPlayer(PlayerCacheBean bean);

    PlayerCacheBean loadPlayerByPlayerId(Integer playerId);

    void updateSelective(PlayerCacheBean bean);

    void addTimeStamp(Integer playerId);

    long getTimeStamp(Integer playerId);

    boolean existTimeStamp(Integer playerId);

    Integer getPlayerHp(Integer playerId);

    List<Integer> getAllPlayerId();

    static PlayerCache getInstance(){
        return PlayerCacheImpl.getInstance();
    }
}
