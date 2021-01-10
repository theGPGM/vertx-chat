package org.george.auction.config;

import org.george.auction.config.bean.AuctionInfoBean;
import org.george.auction.config.impl.AuctionConfigImpl;

import java.util.List;

public interface AuctionConfig {

    List<AuctionInfoBean> getAllAuctionInfo();

    AuctionInfoBean getAuctionInfo(Integer id);

    static AuctionConfig getInstance(){
        return AuctionConfigImpl.getInstance();
    }
}
