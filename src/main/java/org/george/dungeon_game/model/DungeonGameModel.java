package org.george.dungeon_game.model;

import org.george.hall.ClientCloseEventObserver;

public interface DungeonGameModel extends ClientCloseEventObserver {

    static DungeonGameModel getInstance(){
        return DungeonGameModelImpl.getInstance();
    }
}
