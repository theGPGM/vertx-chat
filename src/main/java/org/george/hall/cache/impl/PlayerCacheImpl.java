package org.george.hall.cache.impl;

import org.george.hall.cache.PlayerCache;

import org.george.hall.cache.bean.PlayerCacheBean;
import org.george.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

public class PlayerCacheImpl implements PlayerCache {

    private PlayerCacheImpl(){}

    private static PlayerCacheImpl instance = new PlayerCacheImpl();

    public static PlayerCacheImpl getInstance(){
        return instance;
    }

    @Override
    public PlayerCacheBean loadPlayerByPlayerName(String playerName) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        String playerId = jedis.get("player_name:" + playerName + ":player_id");
        if(playerId == null){
            return null;
        }else{
            PlayerCacheBean player = new PlayerCacheBean();
            Integer hp = Integer.parseInt(jedis.hget("player#" + playerId, "hp"));
            Integer gold = Integer.parseInt(jedis.hget("player#" + playerId, "gold"));

            player.setPlayerId(Integer.parseInt(playerId));
            player.setPlayerName(playerName);
            player.setHp(hp);
            player.setGold(gold);
            return player;
        }
    }

    @Override
    public void addPlayer(PlayerCacheBean player) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.sadd("player_id_set", String.valueOf(player.getPlayerId()));
        // 设置 player_id 和 player_name 的映射
        jedis.set("player_name:" + player.getPlayerName() + ":player_id", String.valueOf(player.getPlayerId()));
        // 设置 player 的各个字段
        jedis.hset("player#" + player.getPlayerId(), "player_name", player.getPlayerName());
        jedis.hset("player#" + player.getPlayerId(), "hp", String.valueOf(player.getHp()));
        jedis.hset("player#" + player.getPlayerId(), "gold", String.valueOf(player.getGold()));
    }

    @Override
    public PlayerCacheBean loadPlayerByPlayerId(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        if(!jedis.exists("player#" + playerId)){
            return null;
        }else{
            PlayerCacheBean cacheBean = new PlayerCacheBean();
            String playerName = jedis.hget("player#" + playerId, "player_name");
            Integer hp = Integer.parseInt(jedis.hget("player#" + playerId, "hp"));
            Integer gold = Integer.parseInt(jedis.hget("player#" + playerId, "gold"));

            cacheBean.setPlayerId(playerId);
            cacheBean.setPlayerName(playerName);
            cacheBean.setHp(hp);
            cacheBean.setGold(gold);
            return cacheBean;
        }
    }

    @Override
    public void updateSelective(PlayerCacheBean player) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        if(player.getPlayerName() != null){
            jedis.hset("player#" + player.getPlayerId(), "player_name", player.getPlayerName());
        }
        if(player.getGold() != null){
            jedis.hset("player#" + player.getPlayerId(), "gold", String.valueOf(player.getGold()));
        }
        if(player.getHp() != null){
            jedis.hset("player#" + player.getPlayerId(), "hp", String.valueOf(player.getHp()));
        }
    }

    @Override
    public void addTimeStampIfNotExisting(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        if(!jedis.exists("player_timestamp#" + playerId)){
            jedis.set("player_timestamp#" + playerId, String.valueOf(System.currentTimeMillis()));
        }
    }

    @Override
    public long getTimeStamp(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        return Long.parseLong(jedis.get("player_timestamp#" + playerId));
    }

    @Override
    public boolean existTimeStamp(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        return jedis.exists("player_timestamp#" + playerId);
    }
}
