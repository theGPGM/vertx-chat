package org.george.bag.cache.mapper;

import org.apache.ibatis.annotations.Param;
import org.george.bag.pojo.PlayerItem;

import java.util.List;

public interface PlayerItemMapper {

    void addPlayerItem(@Param("playerId") Integer playerId, @Param("item") PlayerItem item);

    List<PlayerItem> getPlayerItems(Integer player);

    void updatePlayerItem(@Param("playerId") Integer playerId, @Param("item") PlayerItem item);

    void deletePlayerItem(@Param("playerId") Integer playerId, @Param("itemId") Integer itemId);

    PlayerItem getPlayerItem(@Param("playerId") Integer playerId, @Param("itemId") Integer itemId);
}
