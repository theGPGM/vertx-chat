package org.george.hall.dao;

import org.george.hall.dao.bean.PlayerInfoBean;

import org.george.hall.dao.impl.PlayerInfoDaoImpl;


public interface PlayerInfoDao {

    static PlayerInfoDao getInstance(){
        return PlayerInfoDaoImpl.getInstance();
    }

    void updateSelective(PlayerInfoBean bean);

    void addPlayer(PlayerInfoBean bean);

    PlayerInfoBean loadPlayerByPlayerId(Integer playerId);

    void deletePlayer(Integer playerId);
}
