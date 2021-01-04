package org.george.auction.cache;

import org.george.auction.cache.bean.AuctionItemCacheBean;
import org.george.auction.cache.impl.AuctionCacheImpl;

import java.util.List;

public interface AuctionCache {

    List<AuctionItemCacheBean> getAuctions();

    void addAuctionItemCacheBean(AuctionItemCacheBean item);

    void updateSelective(AuctionItemCacheBean item);

    void deleteAuctionItemCacheBean(Integer itemId);

    AuctionItemCacheBean getAuction(Integer itemId);
    
    static AuctionCache getInstance(){
        return AuctionCacheImpl.getInstance();
    }
}
