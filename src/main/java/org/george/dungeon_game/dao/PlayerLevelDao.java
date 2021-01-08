package org.george.dungeon_game.dao;

import org.george.dungeon_game.dao.bean.PlayerLevelBean;
import org.george.dungeon_game.dao.impl.PlayerLevelDaoImpl;

public interface PlayerLevelDao {

    PlayerLevelBean loadPlayerLevelByPlayerId(Integer playerId);

    void updateRecordSelective(PlayerLevelBean level);

    void addPlayerLevel(Integer playerId);

    static PlayerLevelDao getInstance(){
        return PlayerLevelDaoImpl.getInstance();
    }
}
