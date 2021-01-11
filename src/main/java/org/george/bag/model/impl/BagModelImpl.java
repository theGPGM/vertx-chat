package org.george.bag.model.impl;

import org.george.bag.cache.BagCache;
import org.george.bag.cache.bean.PlayerItemCacheBean;
import org.george.bag.model.BagModel;
import org.george.bag.model.pojo.PlayerItemResult;
import org.george.bag.util.JedisPool;
import org.george.bag.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class BagModelImpl implements BagModel {

    private BagModelImpl(){}

    private static BagModelImpl instance = new BagModelImpl();

    public static BagModelImpl getInstance(){
        return instance;
    }

    private BagCache bagCache = BagCache.getInstance();

    @Override
    public List<PlayerItemResult> getAllPlayerItems(Integer playerId) {

        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{

            List<PlayerItemResult> results = new ArrayList<>();
            List<PlayerItemCacheBean> list = bagCache.getAll(playerId);

            if(list == null || list.size() == 0){
                return new ArrayList<>();
            }else{
                for(PlayerItemCacheBean cacheBean : list){
                    PlayerItemResult result = new PlayerItemResult();
                    result.setItemId(cacheBean.getItemId());
                    result.setPlayerId(cacheBean.getPlayerId());
                    result.setNum(cacheBean.getNum());

                    results.add(result);
                }
                return results;
            }
        }finally {
            JedisPool.returnJedis(jedis);
        }
    }

    @Override
    public void addPlayerItem(PlayerItemResult item) {
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            PlayerItemCacheBean cacheBean = playerItemResult2CacheBean(item);
            bagCache.add(cacheBean);
        }finally {
            JedisPool.returnJedis(jedis);
        }
    }

    @Override
    public void updatePlayerItem(PlayerItemResult item) {
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            PlayerItemCacheBean cacheBean = playerItemResult2CacheBean(item);
            bagCache.updateSelective(cacheBean);
        }finally {
            JedisPool.returnJedis(jedis);
        }
    }

    @Override
    public PlayerItemResult getPlayerItem(Integer playerId, Integer itemId) {
        Jedis jedis = JedisPool.getJedis();
        ThreadLocalJedisUtils.addJedis(jedis);
        try{
            PlayerItemCacheBean cacheBean = bagCache.get(playerId, itemId);
            if(cacheBean == null){
                return null;
            }
            return playerItemCacheBean2Result(cacheBean);
        }finally {
            JedisPool.returnJedis(jedis);
        }
    }

    private PlayerItemCacheBean playerItemResult2CacheBean(PlayerItemResult result){
        PlayerItemCacheBean cacheBean = new PlayerItemCacheBean();
        cacheBean.setPlayerId(result.getPlayerId());
        cacheBean.setItemId(result.getItemId());
        cacheBean.setNum(result.getNum());
        return cacheBean;
    }

    private PlayerItemResult playerItemCacheBean2Result(PlayerItemCacheBean cacheBean){
        PlayerItemResult result = new PlayerItemResult();
        result.setPlayerId(cacheBean.getPlayerId());
        result.setItemId(cacheBean.getItemId());
        result.setNum(cacheBean.getNum());
        return result;
    }
}
