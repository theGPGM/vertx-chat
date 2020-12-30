package org.george.bag.cache.redis;

import org.george.bag.pojo.PlayerItem;

import java.util.List;

public interface BagRedisCache {

    List<PlayerItem> getAllPlayerItems(Integer playerId);
    void addPlayerItem(Integer playerId, PlayerItem item);

    void updatePlayerItem(Integer playerId, PlayerItem item);

    PlayerItem getPlayerItem(Integer playerId, Integer itemId);

    void deletePlayerItem(Integer playerId, Integer itemId);
}
