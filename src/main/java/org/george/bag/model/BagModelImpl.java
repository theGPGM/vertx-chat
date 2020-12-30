package org.george.bag.model;

import org.george.bag.cache.BagCache;
import org.george.bag.cache.impl.BagCacheImpl;
import org.george.bag.pojo.PlayerItem;

import java.util.List;

public class BagModelImpl implements BagModel {

    private BagModelImpl(){}

    private static BagModelImpl instance = new BagModelImpl();

    public static BagModelImpl getInstance(){
        return instance;
    }

    private BagCache bagCache = BagCacheImpl.getInstance();


    @Override
    public List<PlayerItem> getAllPlayerItems(Integer playerId) {
        return bagCache.getAllPlayerItem(playerId);
    }

    @Override
    public void addPlayerItem(Integer playerId, PlayerItem item) {
        bagCache.addPlayerItem(playerId, item);
    }

    @Override
    public void updatePlayerItem(Integer playerId, PlayerItem item) {
        bagCache.updatePlayerItem(playerId, item);
    }

    @Override
    public PlayerItem getPlayerItem(Integer playerId, Integer itemId) {
        return bagCache.getPlayerItem(playerId, itemId);
    }
}
