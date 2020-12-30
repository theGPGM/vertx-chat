package org.george.auction.cache.mapper;

import org.apache.ibatis.annotations.Param;
import org.george.auction.pojo.AuctionItem;

import java.util.List;

public interface AuctionMapper {

    List<AuctionItem> getAuctions();

    void addAuctionItem(@Param("itemId") Integer itemId,@Param("cost") Integer cost,@Param("num") Integer num);

    void updateAuctionItemCost(@Param("itemId") Integer itemId, @Param("cost") Integer cost);

    void updateAuctionItemNum(@Param("itemId") Integer itemId, @Param("num") Integer num);

    void deleteAuctionItem(Integer itemId);

    AuctionItem getAuction(Integer itemId);
}
