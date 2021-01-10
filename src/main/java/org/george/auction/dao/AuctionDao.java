package org.george.auction.dao;

import org.george.auction.dao.bean.AuctionBean;
import org.george.auction.dao.impl.AuctionDaoImpl;

import java.util.List;

public interface AuctionDao {

    List<AuctionBean> getAuctions();

    void batchUpdateSelective(List<AuctionBean> list);

    void addAuctionItem(Integer itemId, Integer num);

    void updateSelective(AuctionBean bean);

    void deleteAuctionItem(Integer itemId);

    AuctionBean getAuction(Integer itemId);

    static AuctionDao getInstance(){
        return AuctionDaoImpl.getInstance();
    }
}
