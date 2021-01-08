package org.george.config;

import org.george.config.bean.DropItemInfoBean;
import org.george.config.impl.DropItemConfigImpl;

import java.util.List;

public interface DropItemConfig {

    void loadFile(String filename);

    List<DropItemInfoBean> getLevelDropItemInfo(Integer level);

    static DropItemConfig getInstance(){
        return DropItemConfigImpl.getInstance();
    }
}
