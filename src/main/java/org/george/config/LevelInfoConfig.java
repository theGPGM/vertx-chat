package org.george.config;

import org.george.config.impl.LevelInfoConfigImpl;
import org.george.config.bean.LevelBean;

public interface LevelInfoConfig {

    void loadLevelInfo(String filename);

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
