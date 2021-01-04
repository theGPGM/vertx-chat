package org.george.hall;

import java.util.ArrayList;
import java.util.List;

public class ClientCloseHandler {

    private static List<ClientCloseEventObserver> list = new ArrayList<>();

    public static void addObserver(ClientCloseEventObserver observer){
        list.add(observer);
    }

    public static void notify(String hId){
        for(ClientCloseEventObserver observer : list){
            observer.update(hId);
        }
    }
}
