package org.george.config;

import org.george.config.bean.AuctionInfoBean;
import org.george.config.impl.AuctionConfigImpl;

import java.util.List;

public interface AuctionConfig {

    List<AuctionInfoBean> getAllAuctionInfo();

    AuctionInfoBean getAuctionInfo(Integer id);

    void loadAuctionInfo(String filename);

    static AuctionConfig getInstance(){
        return AuctionConfigImpl.getInstance();
    }
}
