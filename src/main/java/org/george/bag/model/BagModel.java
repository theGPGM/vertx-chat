package org.george.bag.model;


import org.george.bag.model.pojo.PlayerItemResult;
import org.george.bag.model.impl.BagModelImpl;

import java.util.List;

public interface BagModel {

    List<PlayerItemResult> getAllPlayerItems(Integer playerId);

    void addPlayerItem(PlayerItemResult item);

    void updatePlayerItem(PlayerItemResult item);

    PlayerItemResult getPlayerItem(Integer playerId, Integer itemId);

    static BagModel getInstance(){
        return BagModelImpl.getInstance();
    }
}
