package org.george.auction.dao.impl;

import org.george.auction.dao.AuctionDao;
import org.george.auction.pojo.AuctionItem;

import java.util.List;

public class AuctionDaoImpl implements AuctionDao {

    @Override
    public List<AuctionItem> getAuctions() {
        return null;
    }

    @Override
    public void addAuctionItem(Integer itemId, Integer cost, Integer num) {

    }

    @Override
    public void updateAuctionItemCost(Integer itemId, Integer cost) {

    }

    @Override
    public void updateAuctionItemNum(Integer itemId, Integer num) {

    }

    @Override
    public void deleteAuctionItem(Integer itemId) {

    }

    @Override
    public AuctionItem getAuction(Integer itemId) {
        return null;
    }
}
