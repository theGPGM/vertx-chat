package org.george.bag.cache.impl;


import org.george.bag.cache.BagCache;
import org.george.bag.cache.bean.PlayerItemCacheBean;
import org.george.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BagCacheImpl implements BagCache {

    private BagCacheImpl(){}

    private static BagCacheImpl instance = new BagCacheImpl();

    public static BagCacheImpl getInstance(){
        return instance;
    }

    @Override
    public List<PlayerItemCacheBean> getAllPlayerItem(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        if(!jedis.exists("player#"+ playerId + "_items")){
            return null;
        }else{
            List<PlayerItemCacheBean> list = new ArrayList<>();
            Map<String, String> map = jedis.hgetAll("player#" + playerId + "_items");
            for(Map.Entry<String, String> entry : map.entrySet()){
                list.add(getPlayerItem(playerId, Integer.parseInt(entry.getKey())));
            }
            return list;
        }
    }

    @Override
    public void addPlayerItem(PlayerItemCacheBean item) {
        // 添加道具数据
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.hset("player_items#"+ item.getPlayerId(), String.valueOf(item.getItemId()), String.valueOf(item.getNum()));
    }

    @Override
    public void updatePlayerItem(PlayerItemCacheBean item) {
        // 添加道具数据
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.hset("player_items#"+ item.getPlayerId(), String.valueOf(item.getItemId()), String.valueOf(item.getNum()));
    }

    @Override
    public PlayerItemCacheBean getPlayerItem(Integer playerId, Integer itemId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        if(!jedis.hexists("player#"+ playerId + "_items", String.valueOf(itemId))){
            return null;
        }else{
            PlayerItemCacheBean item = new PlayerItemCacheBean();
            Integer num = Integer.parseInt(jedis.hget("player#"+ playerId + "_items", String.valueOf(itemId)));
            item.setNum(num);
            item.setItemId(itemId);
            item.setPlayerId(playerId);
            return item;
        }
    }

    @Override
    public void deletePlayerItem(Integer playerId, Integer itemId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.hdel("player#"+ playerId + "_items", String.valueOf(itemId));
    }
}
