package org.george.dungeon_game.dao;

import org.george.dungeon_game.dao.bean.PlayerLevelBean;
import org.george.dungeon_game.dao.impl.PlayerDGameRecordDaoImpl;

public interface PlayerDGameRecordDao {

    PlayerLevelBean getPlayerLevelByPlayerId(Integer playerId);

    void updateRecordSelective(PlayerLevelBean level);

    void addPlayerLevel(PlayerLevelBean level);

    static PlayerDGameRecordDao getInstance(){
        return PlayerDGameRecordDaoImpl.getInstance();
    }
}
