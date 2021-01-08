package org.george.hall.model;

import org.george.auction.DeductionObserver;
import org.george.hall.ClientCloseEventObserver;
import org.george.hall.model.impl.PlayerModelImpl;
import org.george.hall.model.pojo.PlayerResult;

public interface PlayerModel extends DeductionObserver, ClientCloseEventObserver {

    String getPlayerNameByPlayerId(String userId);

    PlayerResult getPlayerByPlayerId(Integer playerId);

    void updatePlayerHP(Integer playerId, Integer hp);

    void updatePlayerGold(Integer playerId, Integer gold);

    /**
     * 获取触发一次更新
     * @param playerId
     * @return
     */
    Integer getPlayerCurrentHP(Integer playerId);

    String getUId(String hId);

    String getHId(String userId);

    void addUIdAndHId(String userId, String hId);

    void logout(String userId);

    static PlayerModel getInstance(){
        return PlayerModelImpl.getInstance();
    }
}
