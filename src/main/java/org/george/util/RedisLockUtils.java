package org.george.util;

import redis.clients.jedis.Jedis;

import java.util.UUID;

public class RedisLockUtils {

    private static ThreadLocal<String> threadLock = new ThreadLocal<>();

    private static ThreadLocal<Integer> threadLocalInteger = new ThreadLocal<>();

    public static boolean tryLock(String key, int seconds){

        Jedis jedis = org.george.util.ThreadLocalJedisUtils.getJedis();
        boolean isLocked = false;
        if(threadLock.get() == null){
            String uuid = UUID.randomUUID().toString();
            threadLock.set(uuid);
            isLocked = jedis.setnx(key, uuid) == 1;
            if(!isLocked){
                while(true){
                    isLocked = jedis.setnx(key, uuid) == 1;
                    if(isLocked){
                        break;
                    }
                }
            }
        }else{
            isLocked =  true;
        }

        // 可重入
        if (isLocked) {
            Integer count = threadLocalInteger.get() == null ? 0 : threadLocalInteger.get();
            threadLocalInteger.set(count++);
        }
        return isLocked;
    }

    public static void releaseLock(String key){

        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        if(threadLock.get().equals(jedis.get(key))){
            Integer count = threadLocalInteger.get();
            if(count == null || --count <= 0){
                jedis.del(key);
            }
        }
    }
}
