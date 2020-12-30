package org.george.dungeon_game.model;

import org.george.pojo.Level;

import java.util.List;

public interface DungeonGameModel {

    void addLevelInfo(List<Level> list);

    void quitGame(String userId);
}
