package org.george.bag.dao;


import org.george.bag.dao.bean.PlayerItemBean;
import org.george.bag.dao.impl.BagDaoImpl;

import java.util.List;

public interface BagDao {

    void add(PlayerItemBean item);

    List<PlayerItemBean> getAll(Integer player);

    void updateSelective(PlayerItemBean item);

    void delete(Integer playerId, Integer itemId);

    PlayerItemBean get(Integer playerId, Integer itemId);

    static BagDao getInstance(){
        return BagDaoImpl.getInstance();
    }
}
