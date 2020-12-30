package org.george.auction.cache;

import org.apache.ibatis.annotations.Param;
import org.george.auction.pojo.AuctionItem;

import java.util.List;

public interface AuctionCache {

    List<AuctionItem> getAuctions();

    void addAuctionItem(AuctionItem item);

    void updateAuctionItemCost(@Param("itemId") Integer itemId, @Param("cost") Integer cost);

    void updateAuctionItemNum(@Param("itemId") Integer itemId, @Param("num") Integer num);

    void deleteAuctionItem(Integer itemId);

    AuctionItem getAuction(Integer itemId);
}
