package org.george.hall.cache.impl;

import org.george.hall.cache.PlayerCache;
import org.george.hall.cache.bean.PlayerCacheBean;
import org.george.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

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
            String password = jedis.hget("player#" + playerId, "password");
            Integer hp = Integer.parseInt(jedis.hget("player#" + playerId, "hp"));
            Integer gold = Integer.parseInt(jedis.hget("player#" + playerId, "gold"));

            player.setPlayerId(Integer.parseInt(playerId));
            player.setPlayerName(playerName);
            player.setPassword(password);
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
        jedis.hset("player#" + player.getPlayerId(), "password", player.getPassword());
        jedis.hset("player#" + player.getPlayerId(), "hp", String.valueOf(player.getHp()));
        jedis.hset("player#" + player.getPlayerId(), "gold", String.valueOf(player.getGold()));
    }

    @Override
    public PlayerCacheBean loadPlayerByPlayerId(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        if(jedis.exists("player#" + playerId)){
            return null;
        }else{
            PlayerCacheBean player = new PlayerCacheBean();
            String password = jedis.hget("player#" + player.getPlayerId(), "password");
            String playerName = jedis.hget("player#" + player.getPlayerId(), "player_name");
            Integer hp = Integer.parseInt(jedis.hget("player#" + player.getPlayerId(), "hp"));
            Integer gold = Integer.parseInt(jedis.hget("player#" + player.getPlayerId(), "gold"));

            player.setPlayerId(playerId);
            player.setPlayerName(playerName);
            player.setPassword(password);
            player.setHp(hp);
            player.setGold(gold);
            return player;
        }
    }

    @Override
    public void updateSelective(PlayerCacheBean player) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        if(player.getPlayerName() != null){
            jedis.hset("player#" + player.getPlayerId(), "player_name", player.getPlayerName());
        }
        if(player.getPassword() != null){
            jedis.hset("player#" + player.getPlayerId(), "password", player.getPassword());
        }
        if(player.getGold() != null){
            jedis.hset("player#" + player.getPlayerId(), "gold", String.valueOf(player.getGold()));
        }
        if(player.getHp() != null){
            jedis.hset("player#" + player.getPlayerId(), "hp", String.valueOf(player.getHp()));
        }
    }

    @Override
    public void addTimeStamp(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.set("player#" + playerId + "_timestamp", String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public long getTimeStamp(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        return Long.parseLong(jedis.get("player#" + playerId + "_timestamp"));
    }

    @Override
    public boolean existTimeStamp(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        return jedis.exists("player#" + playerId + "_timestamp");
    }

    @Override
    public Integer getPlayerHp(Integer playerId) {

        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        Integer oldHp = Integer.parseInt(jedis.hget("player#" + playerId, "hp"));

        // 更新时间戳
        updateTimeStamp(playerId);
        int increment = (int)(System.currentTimeMillis() - getTimeStamp(playerId)) / (1000 * 10);
        Integer curHp = oldHp + increment;
        if(curHp <= 100){
            jedis.hset("player#" + playerId, "hp", String.valueOf(curHp));
            return curHp;
        }else{
            jedis.hset("player#" + playerId, "hp", String.valueOf(100));
            return 100;
        }
    }

    @Override
    public List<Integer> getAllPlayerId() {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        if(!jedis.exists("player_id_set")){
            return null;
        }else{
            List<Integer> list = new ArrayList<>();
            for(String id : jedis.smembers("player_id_set")){
                list.add(Integer.parseInt(id));
            }
            return list;
        }
    }

    private void updateTimeStamp(Integer playerId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.set("player#" + playerId + "_timestamp", String.valueOf(System.currentTimeMillis()));
    }
}
