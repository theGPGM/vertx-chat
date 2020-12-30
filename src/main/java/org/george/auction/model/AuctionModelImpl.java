package org.george.auction.model;

import org.george.auction.cache.AuctionCache;
import org.george.auction.cache.AuctionCacheImpl;

public class AuctionModelImpl implements AuctionModel{

    private AuctionModelImpl(){}

    private static AuctionModelImpl instance = new AuctionModelImpl();

    public static AuctionModelImpl getInstance(){
        return instance;
    }

    private AuctionCache auctionCache = AuctionCacheImpl.getInstance();

    @Override
    public void updateAuctionItemCost(Integer itemId, Integer cost) {
        auctionCache.updateAuctionItemCost(itemId, cost);
    }

    @Override
    public void updateAuctionItemNum(Integer itemId, Integer num) {
        auctionCache.updateAuctionItemNum(itemId, num);
    }
}
