package org.george.bag.dao;


import org.george.bag.dao.bean.PlayerItemBean;
import org.george.bag.dao.impl.BagDaoImpl;

import java.util.List;

public interface BagDao {

    void addPlayerItem(PlayerItemBean item);

    List<PlayerItemBean> getPlayerItems(Integer player);

    void updateSelective(PlayerItemBean item);

    void deletePlayerItem(Integer playerId, Integer itemId);

    PlayerItemBean getPlayerItem(Integer playerId, Integer itemId);

    static BagDao getInstance(){
        return BagDaoImpl.getInstance();
    }
}
