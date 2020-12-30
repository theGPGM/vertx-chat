package org.george.auction.cache;

import org.apache.ibatis.session.SqlSession;
import org.george.auction.cache.mapper.AuctionMapper;
import org.george.auction.cache.redis.AuctionRedisCache;
import org.george.auction.cache.redis.AuctionRedisCacheImpl;
import org.george.auction.pojo.AuctionItem;
import org.george.util.ThreadLocalSessionUtils;

import java.util.ArrayList;
import java.util.List;

public class AuctionCacheImpl implements AuctionCache{

    private AuctionCacheImpl(){}

    private static AuctionCacheImpl instance = new AuctionCacheImpl();

    public static AuctionCacheImpl getInstance(){
        return instance;
    }

    private AuctionRedisCache auctionRedisCache = AuctionRedisCacheImpl.getInstance();

    @Override
    public List<AuctionItem> getAuctions() {

        List<AuctionItem> list = auctionRedisCache.getAuctions();
        if(list == null || list.size() == 0){
            SqlSession session = ThreadLocalSessionUtils.getSession();
            AuctionMapper mapper = session.getMapper(AuctionMapper.class);
            List<AuctionItem> auctions = mapper.getAuctions();
            if(auctions == null || auctions.size() == 0){
                return new ArrayList<>();
            }else{
                for(AuctionItem item : auctions){
                    auctionRedisCache.addAuctionItem(item);
                }
                return auctions;
            }
        }else{
            return list;
        }
    }

    @Override
    public void addAuctionItem(AuctionItem item) {
        SqlSession session = ThreadLocalSessionUtils.getSession();
        AuctionMapper mapper = session.getMapper(AuctionMapper.class);
        mapper.addAuctionItem(item.getItemId(), item.getCost(), item.getNum());
        AuctionItem auctionItem = mapper.getAuction(item.getItemId());
        auctionRedisCache.addAuctionItem(auctionItem);
    }

    @Override
    public void updateAuctionItemCost(Integer itemId, Integer cost) {
        SqlSession session = ThreadLocalSessionUtils.getSession();
        AuctionMapper mapper = session.getMapper(AuctionMapper.class);
        if(auctionRedisCache.getAuction(itemId) == null){
            AuctionItem auc = mapper.getAuction(itemId);
            auctionRedisCache.addAuctionItem(auc);
        }else{
            auctionRedisCache.updateAuctionItemCost(itemId, cost);
        }
        mapper.updateAuctionItemCost(itemId, cost);
    }

    @Override
    public void updateAuctionItemNum(Integer itemId, Integer num) {
        SqlSession session = ThreadLocalSessionUtils.getSession();
        AuctionMapper mapper = session.getMapper(AuctionMapper.class);
        // 没有缓存
        if (auctionRedisCache.getAuction(itemId) == null) {
            AuctionItem auc = mapper.getAuction(itemId);
            auctionRedisCache.addAuctionItem(auc);
        }else{
            auctionRedisCache.updateAuctionItemNum(itemId, num);
        }
        mapper.updateAuctionItemNum(itemId, num);
    }

    @Override
    public void deleteAuctionItem(Integer itemId) {
        SqlSession session = ThreadLocalSessionUtils.getSession();
        AuctionMapper mapper = session.getMapper(AuctionMapper.class);
        if(auctionRedisCache.getAuction(itemId) != null){
            auctionRedisCache.deleteAuctionItem(itemId);
        }
        mapper.deleteAuctionItem(itemId);
    }

    @Override
    public AuctionItem getAuction(Integer itemId) {
        if(auctionRedisCache.getAuction(itemId) == null){
            SqlSession session = ThreadLocalSessionUtils.getSession();
            AuctionMapper mapper = session.getMapper(AuctionMapper.class);
            AuctionItem item = mapper.getAuction(itemId);
            if(item == null){
                return null;
            }else{
                auctionRedisCache.addAuctionItem(item);
                return item;
            }
        }else{
            return auctionRedisCache.getAuction(itemId);
        }
    }
}
