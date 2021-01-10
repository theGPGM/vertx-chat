package org.george.auction.config;

import org.george.auction.config.bean.ItemInfoBean;
import org.george.auction.config.impl.ItemConfigImpl;

/**
 * 道具配置类
 */
public interface ItemConfig {

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
