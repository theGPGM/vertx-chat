package org.george.hall.model;

import org.george.hall.model.impl.PlayerModelImpl;
import org.george.hall.model.pojo.PlayerResult;

import java.util.List;

public interface PlayerModel {

    String getPlayerNameByPlayerId(String userId);

    PlayerResult getPlayerByPlayerId(Integer playerId);

    void updatePlayerHP(Integer playerId, Integer hp);

    void updatePlayerGold(Integer playerId, Integer gold);

    List<Integer> getAllPlayerId();

    /**
     * 获取触发一次更新
     * @param playerId
     * @return
     */
    Integer getPlayerHP(Integer playerId);

    static PlayerModel getInstance(){
        return PlayerModelImpl.getInstance();
    }
}
