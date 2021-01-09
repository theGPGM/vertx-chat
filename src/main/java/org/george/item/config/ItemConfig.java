package org.george.item.config;


import org.george.item.config.bean.ItemInfoBean;
import org.george.item.config.impl.ItemConfigImpl;

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
