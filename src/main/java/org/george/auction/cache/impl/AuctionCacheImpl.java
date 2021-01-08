package org.george.auction.cache.impl;

import org.george.auction.cache.AuctionCache;
import org.george.auction.cache.bean.AuctionCacheBean;
import org.george.util.CalendarUtils;
import org.george.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AuctionCacheImpl implements AuctionCache {

    private AuctionCacheImpl(){}

    private static AuctionCacheImpl instance = new AuctionCacheImpl();

    public static AuctionCacheImpl getInstance(){
        return instance;
    }

    @Override
    public List<AuctionCacheBean> getAuctions() {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        List<AuctionCacheBean> list = new ArrayList<>();
        Set<String> items = jedis.smembers("auctions");

        if(items == null || items.size() == 0){
            return null;
        }else{
            for(String auctionId : items){
                AuctionCacheBean item = getAuction(Integer.parseInt(auctionId));
                if(item != null){
                    list.add(item);
                }
            }
            return list;
        }
    }

    @Override
    public void addAuctionItemCacheBean(AuctionCacheBean item) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.sadd("auctions", String.valueOf(item.getAuctionId()));
        jedis.hset("auction#" + item.getAuctionId(), "auction_type", String.valueOf(item.getAuctionType()));
        jedis.hset("auction#" + item.getAuctionId(), "deduction_type", String.valueOf(item.getDeductionType()));
        jedis.hset("auction#" + item.getAuctionId(), "cost", String.valueOf(item.getCost()));
        jedis.hset("auction#" + item.getAuctionId(), "num", String.valueOf(item.getNum()));
    }

    @Override
    public void updateSelective(AuctionCacheBean item) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.sadd("auctions", String.valueOf(item.getAuctionId()));
        if(item.getNum() != null){
            jedis.hset("auction#" + item.getAuctionId(), "num", String.valueOf(item.getNum()));
        }
        if(item.getCost() != null){
            jedis.hset("auction#" + item.getAuctionId(), "cost", String.valueOf(item.getCost()));
        }
        if(item.getAuctionType() != null){
            jedis.hset("auction#" + item.getAuctionId(), "auction_type", String.valueOf(item.getAuctionType()));
        }
        if(item.getDeductionType() != null){
            jedis.hset("auction#" + item.getAuctionId(), "deduction_type", String.valueOf(item.getDeductionType()));
        }
    }

    @Override
    public void deleteAuctionItemCacheBean(Integer auctionId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.srem("auctions", String.valueOf(auctionId));
        jedis.del("auction#" + auctionId);
    }

    @Override
    public AuctionCacheBean getAuction(Integer auctionId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        AuctionCacheBean item = null;
        if(jedis.sismember("auctions", String.valueOf(auctionId))){
            item = new AuctionCacheBean();
            Integer auctionType = Integer.parseInt(jedis.hget("auction#" + auctionId, "auction_type"));
            Integer deductionType = Integer.parseInt(jedis.hget("auction#" + auctionId, "deduction_type"));
            Integer cost = Integer.parseInt(jedis.hget("auction#" + auctionId, "cost"));
            Integer num = Integer.parseInt(jedis.hget("auction#" + auctionId, "num"));
            item.setAuctionId(auctionId);
            item.setAuctionType(auctionType);
            item.setDeductionType(deductionType);
            item.setNum(num);
            item.setCost(cost);
        }
        return item;
    }

    @Override
    public boolean timestampExpired() {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        return !jedis.exists("auctions_time_stamp");
    }

    @Override
    public void addTimeStamp() {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.set("auctions_time_stamp", "***");
        jedis.expireAt("auctions_time_stamp", CalendarUtils.getNextEarlyMorningTime());
    }
}
