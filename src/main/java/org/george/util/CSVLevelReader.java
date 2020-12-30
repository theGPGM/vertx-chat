package org.george.util;

import org.george.pojo.LevelBean;
import org.george.pojo.Monster;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CSVLevelReader {

    public static List<LevelBean> loadLevel(String file){
        BufferedReader br = null;
        List<LevelBean> list = new ArrayList<>();
        try{
            br = new BufferedReader(new FileReader(new File(file)));
            // 第一行不读取
            br.readLine();

            String line = null;
            while((line = br.readLine()) != null){
                String[] properties = line.split(",");
                LevelBean levelBean = new LevelBean();
                levelBean.setLevel(Integer.parseInt(properties[0]));
                levelBean.setLevelName(properties[1]);
                Monster monster = new Monster();
                monster.setMonsterId(Integer.parseInt(properties[2]));
                monster.setMonsterName(properties[3]);
                levelBean.setMonster(monster);
                levelBean.setWinningRate(Integer.parseInt(properties[4]));
                levelBean.setDroppingWinItemRate(Integer.parseInt(properties[5]));
                list.add(levelBean);
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
