package org.george.auction.dao;

import org.george.auction.pojo.AuctionItem;

import java.util.List;

public interface AuctionDao {

    List<AuctionItem> getAuctions();

    void addAuctionItem(Integer itemId, Integer cost,Integer num);

    void updateAuctionItemCost(Integer itemId, Integer cost);

    void updateAuctionItemNum(Integer itemId, Integer num);

    void deleteAuctionItem(Integer itemId);

    AuctionItem getAuction(Integer itemId);
}
