package org.george.chat_room_game.cache.impl;


import org.george.chat_room_game.cache.GameCache;
import org.george.util.JedisPool;
import org.george.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameCacheImpl implements GameCache {

  private GameCacheImpl(){}

  private static GameCacheImpl instance = new GameCacheImpl();

  public static GameCacheImpl getInstance(){
    return instance;
  }

  @Override
  public void addGameUser(String roomId, String userId){
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    jedis.sadd("room_game_playerIds#" + roomId, userId);
  }

  @Override
  public List<String> getAllPlayerInRoom(String roomId){
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    Set<String> set = jedis.smembers("room_game_playerIds#" + roomId);
    return new ArrayList<>(set);
  }

  @Override
  public boolean gameExists(String roomId){
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    return jedis.exists("room_game_playerIds#" + roomId);
  }

  @Override
  public void addUserAction(String roomId, String userId, String action, Integer expireSecond) {
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    if(jedis.exists("room_game_player_action#" + roomId)){
      jedis.hset("room_game_player_action#" + roomId, userId, action);
    }else{
      jedis.hset("room_game_player_action#" + roomId, userId, action);
      jedis.expire("room_game_player_action#" + roomId, expireSecond);
    }
  }

  @Override
  public Map<String, String> getAllUserAction(String roomId){
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    Map<String, String> map = jedis.hgetAll("room_game_player_action#" + roomId);
    return map;
  }

  @Override
  public void clearCache(String roomId) {
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    jedis.del("room_game_player_action#" + roomId);
    jedis.del("room_game_playerIds#" + roomId);
    jedis.del("room_game_wait#" + roomId);
  }

  @Override
  public String getUserAction(String roomId, String userId) {
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    return jedis.hget("room_game_player_action#" + roomId, userId);
  }

  @Override
  public void createGame(String roomId, String userId, Integer expireSecond) {
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    jedis.sadd("room_game_playerIds#" + roomId, userId);
    jedis.expire("room_game_playerIds#" + roomId, expireSecond);
  }

  @Override
  public void addWaitingTime(String roomId, Integer expireSecond) {
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    jedis.set("room_game_wait#" + roomId, "***");
    jedis.expire("room_game_wait#" + roomId, expireSecond);
  }

  @Override
  public boolean existsWaitingTime(String roomId) {
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    return jedis.exists("room_game_wait#" + roomId);
  }

  public static void main(String[] args) {
    Jedis jedis = JedisPool.getJedis();
    ThreadLocalJedisUtils.addJedis(jedis);

    getInstance().addGameUser("123", "19");
  }
}
