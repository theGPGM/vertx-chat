package org.george.item.uitl;

import redis.clients.jedis.Jedis;

public class ThreadLocalJedisUtils {

    private static final ThreadLocal<Jedis> threadLocal = new ThreadLocal<>();

    public static Jedis getJedis(){
        return threadLocal.get();
    }

    public static void addJedis(Jedis jedis){
        threadLocal.set(jedis);
    }
}
