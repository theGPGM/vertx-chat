package org.george.auction.cache.redis;

import org.george.auction.pojo.AuctionItem;
import org.george.util.ThreadLocalJedisUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AuctionRedisCacheImpl implements AuctionRedisCache {

    private AuctionRedisCacheImpl(){}

    private static AuctionRedisCacheImpl instance = new AuctionRedisCacheImpl();

    public static AuctionRedisCacheImpl getInstance(){
        return instance;
    }

    @Override
    public List<AuctionItem> getAuctions() {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        List<AuctionItem> list = new ArrayList<>();
        Set<String> items = jedis.smembers("auctions");

        if(items == null || items.size() == 0){
            return null;
        }else{
            for(String itemId : items){
                AuctionItem item = getAuction(Integer.parseInt(itemId));
                if(item != null){
                    list.add(item);
                }
            }
            return list;
        }
    }

    @Override
    public void addAuctionItem(AuctionItem item) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.sadd("auctions", String.valueOf(item.getItemId()));
        jedis.hset("auctions_item#" + item.getItemId(), "cost", String.valueOf(item.getCost()));
        jedis.hset("auctions_item#" + item.getItemId(), "num", String.valueOf(item.getNum()));
    }

    @Override
    public void updateAuctionItemCost(Integer itemId, Integer cost) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.hset("auctions_item#" + itemId, "cost", String.valueOf(cost));
    }

    @Override
    public void updateAuctionItemNum(Integer itemId, Integer num) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.hset("auctions_item#" + itemId, "num", String.valueOf(num));
    }

    @Override
    public void deleteAuctionItem(Integer itemId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        jedis.srem("auctions", String.valueOf(itemId));
        jedis.del("auctions_item#" + itemId);
    }

    @Override
    public AuctionItem getAuction(Integer itemId) {
        Jedis jedis = ThreadLocalJedisUtils.getJedis();
        AuctionItem item = null;
        if(jedis.sismember("auctions", String.valueOf(itemId))){
            item = new AuctionItem();
            Integer cost = Integer.parseInt(jedis.hget("auctions_item#" + itemId, "cost"));
            Integer num = Integer.parseInt(jedis.hget("auctions_item#" + itemId, "num"));
            item.setCost(cost);
            item.setItemId(itemId);
            item.setNum(num);
        }
        return item;
    }
}
