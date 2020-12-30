package org.george.bag.cache.redis;


import org.george.bag.pojo.PlayerItem;
import org.george.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BagRedisCacheImpl implements BagRedisCache{

    private BagRedisCacheImpl(){}

    private static BagRedisCacheImpl instance = new BagRedisCacheImpl();

    public static BagRedisCacheImpl getInstance(){
        return instance;
    }

    @Override
    public List<PlayerItem> getAllPlayerItems(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        if(!jedis.exists("player#"+ playerId + "_items")){
            return null;
        }else{
            List<PlayerItem> list = new ArrayList<>();
            Map<String, String> map = jedis.hgetAll("player#" + playerId + "_items");
            for(Map.Entry<String, String> entry : map.entrySet()){
                list.add(getPlayerItem(playerId, Integer.parseInt(entry.getKey())));
            }
            return list;
        }
    }

    @Override
    public void addPlayerItem(Integer playerId, PlayerItem item) {
        // 添加道具数据
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.hset("player#"+ playerId + "_items", String.valueOf(item.getItemId()), String.valueOf(item.getNum()));
    }

    @Override
    public void updatePlayerItem(Integer playerId, PlayerItem item) {
        // 添加道具数据
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.hset("player#"+ playerId + "_items", String.valueOf(item.getItemId()), String.valueOf(item.getNum()));
    }

    @Override
    public PlayerItem getPlayerItem(Integer playerId, Integer itemId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        if(!jedis.hexists("player#"+ playerId + "_items", String.valueOf(itemId))){
            return null;
        }else{
            PlayerItem item = new PlayerItem();
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
