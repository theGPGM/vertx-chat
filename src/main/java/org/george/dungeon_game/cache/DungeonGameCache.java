package org.george.dungeon_game.cache;

public interface DungeonGameCache {

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
     * 获取玩家 hp 购买次数
     * @param playerId
     * @return
     */
    Integer getBuyHpCount(Integer playerId);

    void incrBuyHpCount(Integer playerId);
}
