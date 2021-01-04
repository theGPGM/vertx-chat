package org.george.bag.cache;

import org.george.bag.cache.bean.PlayerItemCacheBean;
import org.george.bag.cache.impl.BagCacheImpl;

import java.util.List;

public interface BagCache {

    List<PlayerItemCacheBean> getAllPlayerItem(Integer playerId);

    void addPlayerItem(PlayerItemCacheBean item);

    void updatePlayerItem(PlayerItemCacheBean item);

    PlayerItemCacheBean getPlayerItem(Integer playerId, Integer itemId);

    void deletePlayerItem(Integer playerId, Integer itemId);

    static BagCache getInstance(){
        return BagCacheImpl.getInstance();
    }
}
