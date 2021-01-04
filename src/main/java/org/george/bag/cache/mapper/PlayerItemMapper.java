package org.george.bag.cache.mapper;

import org.apache.ibatis.annotations.Param;
import org.george.bag.model.bean.PlayerItemResult;

import java.util.List;

public interface PlayerItemMapper {

    void addPlayerItem(@Param("playerId") Integer playerId, @Param("item") PlayerItemResult item);

    List<PlayerItemResult> getPlayerItems(Integer player);

    void updatePlayerItem(@Param("playerId") Integer playerId, @Param("item") PlayerItemResult item);

    void deletePlayerItem(@Param("playerId") Integer playerId, @Param("itemId") Integer itemId);

    PlayerItemResult getPlayerItem(@Param("playerId") Integer playerId, @Param("itemId") Integer itemId);
}
