package org.george.bag.cache.impl;

import org.apache.ibatis.session.SqlSession;

import org.george.bag.cache.BagCache;
import org.george.bag.cache.mapper.PlayerItemMapper;
import org.george.bag.cache.redis.BagRedisCache;
import org.george.bag.cache.redis.BagRedisCacheImpl;
import org.george.bag.pojo.PlayerItem;
import org.george.util.ThreadLocalSessionUtils;
import java.util.List;

public class BagCacheImpl implements BagCache {

    private BagCacheImpl(){}

    private static BagCacheImpl instance = new BagCacheImpl();

    public static BagCacheImpl getInstance(){
        return instance;
    }

    private BagRedisCache bagRedisCache = BagRedisCacheImpl.getInstance();

    @Override
    public void addPlayerItem(Integer playerId, PlayerItem item) {
        bagRedisCache.addPlayerItem(playerId, item);
        SqlSession session = ThreadLocalSessionUtils.getSession();
        PlayerItemMapper mapper = session.getMapper(PlayerItemMapper.class);
        mapper.addPlayerItem(playerId, item);
    }

    @Override
    public List<PlayerItem> getAllPlayerItem(Integer playerId) {
        if(bagRedisCache.getAllPlayerItems(playerId) == null){
            SqlSession session = ThreadLocalSessionUtils.getSession();
            PlayerItemMapper mapper = session.getMapper(PlayerItemMapper.class);
            List<PlayerItem> items = mapper.getPlayerItems(playerId);

            for(PlayerItem item : items){
                bagRedisCache.addPlayerItem(playerId, item);
            }

            return items;
        }else{
            return bagRedisCache.getAllPlayerItems(playerId);
        }
    }

    @Override
    public void updatePlayerItem(Integer playerId, PlayerItem item) {
        bagRedisCache.updatePlayerItem(playerId, item);
        SqlSession session = ThreadLocalSessionUtils.getSession();
        PlayerItemMapper mapper = session.getMapper(PlayerItemMapper.class);
        mapper.updatePlayerItem(playerId, item);
    }

    @Override
    public PlayerItem getPlayerItem(Integer playerId, Integer itemId) {
        if(bagRedisCache.getPlayerItem(playerId, itemId) == null){
            SqlSession session = ThreadLocalSessionUtils.getSession();
            PlayerItemMapper mapper = session.getMapper(PlayerItemMapper.class);
            PlayerItem item = mapper.getPlayerItem(playerId, itemId);
            if(item == null){
                return null;
            }else{
                bagRedisCache.addPlayerItem(playerId, item);
                return item;
            }
        }else{
            return bagRedisCache.getPlayerItem(playerId, itemId);
        }
    }

    @Override
    public void deletePlayerItem(Integer playerId, Integer itemId) {
        if(bagRedisCache.getPlayerItem(playerId, itemId) != null){
            bagRedisCache.deletePlayerItem(playerId, itemId);
        }
        SqlSession session = ThreadLocalSessionUtils.getSession();
        PlayerItemMapper mapper = session.getMapper(PlayerItemMapper.class);
        mapper.deletePlayerItem(playerId, itemId);
    }
}
