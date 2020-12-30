package org.george.dungeon_game.cache;

import org.george.pojo.Level;
import org.george.dungeon_game.dao.bean.PlayerLevelBean;

import java.util.List;

public interface DungeonGameCache {

    PlayerLevelBean getPlayerPlayerLevel(Integer playerId);

    void updatePlayerLevel(PlayerLevelBean level);

    void addPlayerLevel(PlayerLevelBean level);

    /**
     * 添加游戏玩家
     * @param userId
     */
    void addPlayer(String userId);

    void deletePlayer(String userId);

    /**
     * 查看用户是否处于游戏状态
     * @param userId
     * @return
     */
    boolean playerAtGame(String userId);

    /**
     * 添加关卡信息
     * @param levelList
     */
    void addLevelInfo(List<Level> levelList);

    /**
     * 获取关卡信息
     * @param level
     * @return
     */
    Level getLevelInfo(Integer level);

    /**
     * 获取关卡层数
     */
    Integer getLevelNum();

    /**
     * 获取玩家 hp 购买次数
     * @param playerId
     * @return
     */
    Integer getBuyHpCount(Integer playerId);

    void incrBuyHpCount(Integer playerId);
}
