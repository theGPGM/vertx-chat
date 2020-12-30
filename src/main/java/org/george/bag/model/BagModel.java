package org.george.bag.model;


import org.george.bag.pojo.PlayerItem;

import java.util.List;

public interface BagModel {

    List<PlayerItem> getAllPlayerItems(Integer playerId);

    void addPlayerItem(Integer playerId, PlayerItem item);

    void updatePlayerItem(Integer playerId, PlayerItem item);

    PlayerItem getPlayerItem(Integer playerId, Integer itemId);
}
