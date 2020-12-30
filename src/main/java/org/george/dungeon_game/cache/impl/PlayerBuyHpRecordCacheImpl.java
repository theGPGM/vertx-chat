package org.george.dungeon_game.cache.impl;

import org.george.dungeon_game.cache.PlayerBuyHpRecordCache;
import org.george.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

public class PlayerBuyHpRecordCacheImpl implements PlayerBuyHpRecordCache {

    private PlayerBuyHpRecordCacheImpl(){}

    private static PlayerBuyHpRecordCacheImpl instance = new PlayerBuyHpRecordCacheImpl();

    public static PlayerBuyHpRecordCacheImpl getInstance(){
        return instance;
    }

    @Override
    public Integer getBuyHpCount(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        String count = jedis.get("player#" + playerId + "buy_hp_count");
        if(count == null){
            return null;
        }else{
            return Integer.parseInt(count);
        }
    }

    @Override
    public void addBuyHpCount(Integer playerId, Long expireTimestamp) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.set("player#" + playerId + "buy_hp_count", "0");
        jedis.expireAt("player#" + playerId + "buy_hp_count", expireTimestamp);
    }

    @Override
    public void incrBuyHpCount(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.incr("player#" + playerId + "buy_hp_count");
    }
}
