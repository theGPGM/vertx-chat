package org.george.bag.cache;

import org.george.bag.cache.bean.PlayerItemCacheBean;
import org.george.bag.cache.impl.BagCacheImpl;

import java.util.List;

public interface BagCache {

    List<PlayerItemCacheBean> getAll(Integer playerId);

    void add(PlayerItemCacheBean item);

    void updateSelective(PlayerItemCacheBean item);

    PlayerItemCacheBean get(Integer playerId, Integer itemId);

    void delete(Integer playerId, Integer itemId);

    static BagCache getInstance(){
        return BagCacheImpl.getInstance();
    }
}
