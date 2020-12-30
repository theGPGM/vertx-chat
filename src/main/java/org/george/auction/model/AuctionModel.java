package org.george.auction.model;

public interface AuctionModel {

    void updateAuctionItemCost(Integer itemId, Integer cost);

    void updateAuctionItemNum(Integer itemId, Integer num);
}
