package org.george.hall.dao;

import org.apache.ibatis.annotations.Param;
import org.george.hall.dao.bean.PlayerBean;
import org.george.hall.dao.impl.PlayerDaoImpl;
import org.george.hall.pojo.Player;

import java.util.List;

public interface PlayerDao {

    static PlayerDao getInstance(){
        return PlayerDaoImpl.getInstance();
    }

    PlayerBean loadPlayerByPlayerName(String playerName);

    void updatePlayerSelective(PlayerBean bean);

    void addPlayer(PlayerBean bean);

    PlayerBean loadPlayerByPlayerId(Integer playerId);

    void deletePlayer(Integer playerId);

    List<Integer> getAllPlayerId();
}
