package org.george.auction.model.impl;

import org.george.auction.cache.AuctionCache;
import org.george.auction.cache.bean.AuctionItemCacheBean;
import org.george.auction.model.AuctionModel;

public class AuctionModelImpl implements AuctionModel {

    private AuctionModelImpl(){}

    private static AuctionModelImpl instance = new AuctionModelImpl();

    public static AuctionModelImpl getInstance(){
        return instance;
    }

    private AuctionCache auctionCache = AuctionCache.getInstance();

    @Override
    public void updateAuctionItemCost(Integer itemId, Integer cost) {

        AuctionItemCacheBean cacheBean = new AuctionItemCacheBean();
        cacheBean.setItemId(itemId);
        cacheBean.setCost(cost);
        auctionCache.updateSelective(cacheBean);
    }

    @Override
    public void updateAuctionItemNum(Integer itemId, Integer num) {
        AuctionItemCacheBean cacheBean = new AuctionItemCacheBean();
        cacheBean.setItemId(itemId);
        cacheBean.setNum(num);
        auctionCache.updateSelective(cacheBean);
    }
}
