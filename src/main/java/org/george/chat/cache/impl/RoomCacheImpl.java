package org.george.chat.cache.impl;


import org.george.chat.cache.RoomCache;
import org.george.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RoomCacheImpl implements RoomCache {

  private RoomCacheImpl(){}

  private static RoomCacheImpl instance = new RoomCacheImpl();

  public static RoomCacheImpl getInstance(){
    return instance;
  }

  @Override
  public void join(String roomId, String userId){
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    jedis.hset("room#" + roomId, userId, "**");
    jedis.hset("user_room#" + userId, roomId, "**");
  }

  @Override
  public void clearUserRoomCache(String userId, String roomId){
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    jedis.hdel("room#" + roomId, userId);
    jedis.hdel("user_room#" + userId, roomId);
  }

  @Override
  public List<String> getAllUserId(String roomId){
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    Set<String> set = jedis.hgetAll("room#" + roomId).keySet();
    List<String> list = new ArrayList<>(set);
    return list;
  }

  @Override
  public List<String> getUserRoomIds(String userId){
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    Set<String> set = jedis.hgetAll("user_room#" + userId).keySet();
    return new ArrayList<>(set);
  }

  @Override
  public boolean existsRoom(String roomId){
    Jedis jedis = ThreadLocalJedisUtils.getJedis();
    boolean value = jedis.exists("room#" + roomId).booleanValue();
    return value;
  }
}
