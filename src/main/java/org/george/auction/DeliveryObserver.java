package org.george.auction;

public interface DeliveryObserver {

    /**
     * @param playerId 玩家的 id
     * @param id       发送的物品 id 如：在道具模块中就是道具 id
     * @param num      发送的物品数量
     * @return
     */
    boolean deliveryNotify(Integer playerId, Integer id, Integer num);
}
