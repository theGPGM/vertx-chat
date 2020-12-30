package org.george.dungeon_game.model;

import org.george.pojo.LevelBean;

import java.util.List;

public interface DungeonGameModel {

    void addLevelInfo(List<LevelBean> list);

    void quitGame(String userId);
}
