package org.george.config.bean;

import org.george.pojo.Level;
import org.george.pojo.Monster;
import org.george.config.LevelInfoConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LevelInfoConfigImpl implements LevelInfoConfig {

    List<Level> levels = new ArrayList<>();

    @Override
    public void loadLevelInfo(String filename) {
        BufferedReader br = null;
        List<Level> list = new ArrayList<>();
        File file = new File(filename);
        if(file == null){
            throw new RuntimeException("文件为空");
        }else{
            try {
                br = new BufferedReader(new FileReader(file));
                // 第一行不读取
                br.readLine();
                String line = null;
                while((line = br.readLine()) != null){
                    String[] properties = line.split(",");
                    Level level = new Level();


                    String property = properties[0];
                    level.setLevel(Integer.parseInt(property));
                    level.setLevelName(properties[1]);
                    Monster monster = new Monster();
                    monster.setMonsterId(Integer.parseInt(properties[2]));
                    monster.setMonsterName(properties[3]);
                    level.setMonster(monster);
                    level.setWinningRate(Integer.parseInt(properties[4]));
                    level.setDroppingWinItemRate(Integer.parseInt(properties[5]));
                    list.add(level);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
