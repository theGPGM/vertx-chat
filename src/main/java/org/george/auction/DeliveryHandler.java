package org.george.auction;

import java.util.HashMap;
import java.util.Map;

public class DeliveryHandler {

    private static Map<Integer, DeliveryObserver> map = new HashMap<>();

    public static void addObserver(Integer type, DeliveryObserver observer){
        map.put(type, observer);
    }

    public static boolean handle(Integer type, Integer playerId, Integer id, Integer num){
        return map.get(type).deliveryNotify(playerId, id, num);
    }
}
