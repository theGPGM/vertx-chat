package org.george.util;

import org.george.auction.dao.AuctionDao;
import org.george.auction.dao.bean.AuctionBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.UUID;

public class RedisLockUtils {

    private static final String LOCK_SUCCESS = "OK";

    private static final Logger logger = LoggerFactory.getLogger(RedisLockUtils.class);

    public static boolean tryLock(String key, String requestId, int seconds){

        Jedis jedis = ThreadLocalJedisUtils.getJedis();

        SetParams params = new SetParams();
        params.nx();
        params.ex(seconds);
        String result = jedis.set(key, requestId, params);
        if(LOCK_SUCCESS.equals(result)){
            logger.info("获取锁:{}", Thread.currentThread().getName());
            return true;
        }
        return false;
    }

    private static final Long RELEASE_SUCCESS = 1l;

    public static boolean releaseLock(String key, String requestId){

        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Object result = jedis.eval(script, Collections.singletonList(key), Collections.singletonList(requestId));
        if(RELEASE_SUCCESS.equals(result)){
            logger.info("释放锁:{}", Thread.currentThread().getName());
            return true;
        }
        return false;
    }

    private static AuctionDao auctionDao = AuctionDao.getInstance();

    public static void main(String[] args) {

        JFinalUtils.initJFinalConfig();
        for(int i = 0; i < 1000; i++){
            new Thread(() -> {
                Jedis jedis = JedisPool.getJedis();
                ThreadLocalJedisUtils.addJedis(jedis);
                String requestId = UUID.randomUUID().toString();
                int count = 0;
                boolean locked = false;
                while(true){
                    // 加锁，10 秒过期
                    locked = RedisLockUtils.tryLock("buy_auction", requestId, 10);
                    if(locked){
                        break;
                    }
                }

                if(locked){
                    try{

                        AuctionBean bean = auctionDao.getAuction(1);

                        if(bean.getNum() > 0){
                            bean.setAuctionId(1);
                            bean.setNum(bean.getNum() - 1);
                            auctionDao.updateSelective(bean);
                        }
                    }finally {
                        // 解锁
                        RedisLockUtils.releaseLock("buy_auction", requestId);
                    }
                }
                JedisPool.returnJedis(jedis);
            }).start();
        }
    }
}
