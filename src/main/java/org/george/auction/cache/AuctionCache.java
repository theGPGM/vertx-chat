package org.george.auction.cache;

import org.george.auction.cache.bean.AuctionCacheBean;
import org.george.auction.cache.impl.AuctionCacheImpl;

import java.util.List;

public interface AuctionCache {

    List<AuctionCacheBean> getAuctions();

    void addAuctionItemCacheBean(AuctionCacheBean item);

    void updateSelective(AuctionCacheBean item);

    void deleteAuctionItemCacheBean(Integer itemId);

    AuctionCacheBean getAuction(Integer itemId);
    
    static AuctionCache getInstance(){
        return AuctionCacheImpl.getInstance();
    }

    boolean timestampExpired();

    void addTimeStamp();
}
