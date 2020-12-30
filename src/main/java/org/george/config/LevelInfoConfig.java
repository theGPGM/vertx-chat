package org.george.config;

public interface LevelInfoConfig {

    static LevelInfoConfig getInstance(){
        return null;
    }

    void loadLevelInfo(String filename);
}
