package org.george.config;

import org.george.config.bean.ItemInfoBean;
import org.george.config.impl.ItemConfigImpl;

/**
 * 道具配置类
 */
public interface ItemConfig {

    void loadItemInfo(String fileName);

    /**
     * 通过道具 ID 获取道具的相关信息，包括名称，描述
     * @param itemId
     * @return
     */
    ItemInfoBean getItemInfoBean(Integer itemId);

    static ItemConfig getInstance(){
        return ItemConfigImpl.getInstance();
    }
}
