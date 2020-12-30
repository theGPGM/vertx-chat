package org.george.config;

import org.george.config.bean.LevelInfoConfigImpl;
import org.george.pojo.LevelBean;

public interface LevelInfoConfig {

    void loadLevelInfo(String filename);

    /**
     * 获取关卡信息
     * @param level
     * @return
     */
    LevelBean getLevelInfo(Integer level);

    /**
     * 获取关卡层数
     */
    Integer getLevelNum();

    static LevelInfoConfig getInstance(){
        return LevelInfoConfigImpl.getInstance();
    }
}
