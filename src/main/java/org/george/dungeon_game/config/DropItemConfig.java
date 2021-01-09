package org.george.dungeon_game.config;


import org.george.dungeon_game.config.bean.DropItemInfoBean;
import org.george.dungeon_game.config.impl.DropItemConfigImpl;

import java.util.List;

public interface DropItemConfig {

    List<DropItemInfoBean> getLevelDropItemInfo(Integer level);

    static DropItemConfig getInstance(){
        return DropItemConfigImpl.getInstance();
    }
}
