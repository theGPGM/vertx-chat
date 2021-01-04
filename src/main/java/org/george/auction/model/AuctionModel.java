package org.george.auction.model;

import org.george.auction.model.impl.AuctionModelImpl;

public interface AuctionModel {

    void updateAuctionItemCost(Integer itemId, Integer cost);

    void updateAuctionItemNum(Integer itemId, Integer num);

    static AuctionModel getInstance(){
        return AuctionModelImpl.getInstance();
    }
}
