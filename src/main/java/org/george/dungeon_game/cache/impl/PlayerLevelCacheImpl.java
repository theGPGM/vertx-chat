package org.george.dungeon_game.cache.impl;

import org.george.dungeon_game.cache.PlayerLevelCache;
import org.george.dungeon_game.cache.bean.PlayerLevelCacheBean;
import org.george.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

public class PlayerLevelCacheImpl implements PlayerLevelCache {

    private PlayerLevelCacheImpl(){}

    private static PlayerLevelCacheImpl instance = new PlayerLevelCacheImpl();

    public static PlayerLevelCacheImpl getInstance(){
        return instance;
    }

    @Override
    public PlayerLevelCacheBean getPlayerLevelByPlayerId(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        PlayerLevelCacheBean level = null;
        if(jedis.exists("player#" + playerId + ":level").booleanValue()){
            level = new PlayerLevelCacheBean();
            level.setPlayerId(playerId);
            Integer playerLevel = Integer.parseInt(jedis.hget("player#" + level.getPlayerId() + ":level", "level"));
            Integer loseCount = Integer.parseInt(jedis.hget("player#" + level.getPlayerId() + ":level", "lose_count"));
            level.setLevel(playerLevel);
            level.setLoseCount(loseCount);
        }
        return level;
    }

    @Override
    public void updatePlayerLevelSelective(PlayerLevelCacheBean level) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();

        if(level.getLevel() != null)
            jedis.hset("player#" + level.getPlayerId() + ":level", "level", String.valueOf(level.getLevel()));

        if(level.getLevel() != null)
            jedis.hset("player#" + level.getPlayerId() + ":level", "lose_count", String.valueOf(level.getLoseCount()));
    }

    @Override
    public void addPlayerLevel(PlayerLevelCacheBean level) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.hset("player#" + level.getPlayerId() + ":level", "level", String.valueOf(level.getLevel()));
        jedis.hset("player#" + level.getPlayerId() + ":level", "lose_count", String.valueOf(level.getLoseCount()));
    }
}
