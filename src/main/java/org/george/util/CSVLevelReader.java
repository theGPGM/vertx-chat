package org.george.util;

import org.george.pojo.Level;
import org.george.pojo.Monster;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVLevelReader {

    public static List<Level> loadLevel(String file){
        BufferedReader br = null;
        List<Level> list = new ArrayList<>();
        try{
            br = new BufferedReader(new FileReader(new File(file)));
            // 第一行不读取
            br.readLine();

            String line = null;
            while((line = br.readLine()) != null){
                String[] properties = line.split(",");
                Level level = new Level();
                level.setLevel(Integer.parseInt(properties[0]));
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
        return list;
    }

    public static void main(String[] args) {
        System.out.println(loadLevel("src/main/resources/csv/level.csv"));
    }
}
