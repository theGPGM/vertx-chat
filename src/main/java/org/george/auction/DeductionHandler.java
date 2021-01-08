package org.george.auction;

import java.util.HashMap;
import java.util.Map;

public class DeductionHandler {

    private static Map<Integer, DeductionObserver> map = new HashMap<>();

    public static void addObserver(Integer type, DeductionObserver observer){
        map.put(type, observer);
    }

    public static boolean deductionHandle(Integer type, Integer playerId, Integer num){
        return map.get(type).deductionNotify(playerId, num);
    }
}
