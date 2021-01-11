package org.george.dungeon_game.cache.impl;

import org.george.dungeon_game.cache.PlayerBuyHpRecordCache;
import org.george.dungeon_game.util.ThreadLocalJedisUtils;
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
        String count = jedis.get("buy_hp_record#" + playerId);
        if(count == null){
            return null;
        }else{
            return Integer.parseInt(count);
        }
    }

    @Override
    public void addBuyHpCount(Integer playerId, Long expireTimestamp) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.set("buy_hp_record#" + playerId, "0");
        jedis.expireAt("buy_hp_record#" + playerId, expireTimestamp);
    }

    @Override
    public void incrBuyHpCount(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.incr("buy_hp_record#" + playerId);
    }
}
