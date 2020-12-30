package org.george.chat_room_game.cache;


import org.george.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

public class GameCacheImpl implements GameCache{

  private GameCacheImpl(){}

  private static GameCacheImpl instance = new GameCacheImpl();

  public static GameCacheImpl getInstance(){
    return instance;
  }

  @Override
  public void addGameUser(String roomId, String userId){
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    jedis.lpush("game#roomId#" + roomId, userId);
  }

  @Override
  public List<String> getPlayUserList(String roomId){
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    List<String> list = jedis.lrange("game#roomId#" + roomId, 0, -1);
    return list;
  }

  @Override
  public boolean gameExists(String roomId){
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    Boolean flag = jedis.exists("game#roomId#" + roomId);
    return flag.booleanValue();
  }

  @Override
  public void addUserAction(String roomId, String userId, String action){
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    jedis.hset("game_room_action_" + roomId, userId, action);
  }

  @Override
  public Map<String, String> getAllUserAction(String roomId){
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    Map<String, String> map = jedis.hgetAll("game_room_action_" + roomId);
    return map;
  }

  @Override
  public void removeRoom(String roomId) {
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    jedis.del("game_room_action_" + roomId);
    jedis.del("game#roomId#" + roomId);
  }

  @Override
  public String getUserAction(String roomId, String userId) {
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    return jedis.hget("game_room_action_" + roomId, userId);
  }
}
