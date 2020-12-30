package org.george.bag.cache;

import org.george.bag.cache.impl.BagCacheImpl;
import org.george.bag.pojo.PlayerItem;

import java.util.List;

public interface BagCache {

    List<PlayerItem> getAllPlayerItem(Integer playerId);

    void addPlayerItem(Integer playerId, PlayerItem item);

    void updatePlayerItem(Integer playerId, PlayerItem item);

    PlayerItem getPlayerItem(Integer playerId, Integer itemId);

    void deletePlayerItem(Integer playerId, Integer itemId);

    static BagCache getInstance(){
        return BagCacheImpl.getInstance();
    }
}
