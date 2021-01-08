package org.george.hall.dao;

import org.george.hall.dao.bean.PlayerAuthBean;
import org.george.hall.dao.impl.PlayerAuthDaoImpl;

public interface PlayerAuthDao {

    PlayerAuthBean loadPlayerAuthBeanByPlayerName(String playerName);

    boolean addPlayer(String playerName, String password);

    boolean updatePlayerSelective(PlayerAuthBean bean);

    int deletePlayer(Integer playerId);

    static PlayerAuthDao getInstance(){
        return PlayerAuthDaoImpl.getInstance();
    }
}
