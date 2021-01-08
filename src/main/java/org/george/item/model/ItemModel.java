package org.george.item.model;

import org.george.auction.DeliveryObserver;
import org.george.item.model.impl.ItemModelImpl;
import org.george.item.model.pojo.ItemResult;

public interface ItemModel extends DeliveryObserver {

    ItemResult getItemByItemId(Integer id);

    static ItemModel getInstance(){
        return ItemModelImpl.getInstance();
    }
}
