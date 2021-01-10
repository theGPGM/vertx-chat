package org.george.dungeon_game.util;

import redis.clients.jedis.Jedis;

import java.util.Properties;

public class JedisPool {

    private static redis.clients.jedis.JedisPool pool;

    static {
        Properties p = PropertiesUtils.loadProperties("src/main/resources/conf/redis.properties");
        String host = p.getProperty("host");
        Integer port = Integer.parseInt(p.getProperty("port"));
        pool = new redis.clients.jedis.JedisPool(host, port);
    }

    public static Jedis getJedis(){
        return pool.getResource();
    }

    public static void returnJedis(Jedis jedis){
        if(jedis != null)
            jedis.close();
    }
}
