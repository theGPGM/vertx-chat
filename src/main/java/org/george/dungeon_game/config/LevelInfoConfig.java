package org.george.dungeon_game.config;


import org.george.dungeon_game.config.bean.LevelBean;
import org.george.dungeon_game.config.impl.LevelInfoConfigImpl;

public interface LevelInfoConfig {

    /**
     * 获取关卡信息
     * @param level
     * @return
     */
    LevelBean getLevelBean(Integer level);

    /**
     * 获取关卡层数
     */
    Integer getLevelNum();

    static LevelInfoConfig getInstance(){
        return LevelInfoConfigImpl.getInstance();
    }
}
