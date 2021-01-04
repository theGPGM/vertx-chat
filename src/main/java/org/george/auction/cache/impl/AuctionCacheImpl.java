package org.george.auction.cache.impl;

import org.george.auction.cache.AuctionCache;
import org.george.auction.cache.bean.AuctionItemCacheBean;
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
    public List<AuctionItemCacheBean> getAuctions() {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        List<AuctionItemCacheBean> list = new ArrayList<>();
        Set<String> items = jedis.smembers("auctions");

        if(items == null || items.size() == 0){
            return null;
        }else{
            for(String itemId : items){
                AuctionItemCacheBean item = getAuction(Integer.parseInt(itemId));
                if(item != null){
                    list.add(item);
                }
            }
            return list;
        }
    }

    @Override
    public void addAuctionItemCacheBean(AuctionItemCacheBean item) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.sadd("auctions", String.valueOf(item.getItemId()));
        jedis.hset("auctions_item#" + item.getItemId(), "cost", String.valueOf(item.getCost()));
        jedis.hset("auctions_item#" + item.getItemId(), "num", String.valueOf(item.getNum()));
    }

    @Override
    public void updateSelective(AuctionItemCacheBean item) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        if(item.getNum() != null){
            jedis.hset("auctions_item#" + item.getItemId(), "num", String.valueOf(item.getNum()));
        }
        if(item.getCost() != null){
            jedis.hset("auctions_item#" + item.getItemId(), "cost", String.valueOf(item.getCost()));
        }
    }

    @Override
    public void deleteAuctionItemCacheBean(Integer itemId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.srem("auctions", String.valueOf(itemId));
        jedis.del("auctions_item#" + itemId);
    }

    @Override
    public AuctionItemCacheBean getAuction(Integer itemId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        AuctionItemCacheBean item = null;
        if(jedis.sismember("auctions", String.valueOf(itemId))){
            item = new AuctionItemCacheBean();
            Integer cost = Integer.parseInt(jedis.hget("auctions_item#" + itemId, "cost"));
            Integer num = Integer.parseInt(jedis.hget("auctions_item#" + itemId, "num"));
            item.setCost(cost);
            item.setItemId(itemId);
            item.setNum(num);
        }
        return item;
    }
}
